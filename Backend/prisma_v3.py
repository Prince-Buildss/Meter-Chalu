"""
Prisma v3 streaming STT for LiveKit.

Why this file exists
--------------------
The official livekit-plugins-gnani STT (v1.8.11) opens the websocket with only
four headers: api key, language, sample rate and format. That means it talks
to the DEFAULT model (Prisma v2.5) and gives us no way to ask for:
  - model=prisma-v3.0          (auto language detection, code-mixed speech)
  - keyterms                   (Marathi place names as custom terms)
  - VAD tuning headers         (noisy auto-stand audio, faster end of speech)
It also throws away `detected_language_code` from the transcript message.

So I subclass the plugin's own STT and SpeechStream and only replace the two
methods that matter (_run builds the headers, _recv_messages reads replies).
Audio sending (_send_audio) is inherited unchanged, so the 1024-byte frame
rules from the Gnani docs are still handled by their code.

Header names come from https://docs.gnani.ai/api/STT/stt-websocket
"""

import asyncio
import contextlib
import json
import logging
from dataclasses import replace
from typing import Callable, Optional

from livekit.agents import (
    APIConnectionError,
    APIConnectOptions,
    APIStatusError,
    APITimeoutError,
    DEFAULT_API_CONNECT_OPTIONS,
    LanguageCode,
    stt,
    utils,
)
from livekit.agents.types import NOT_GIVEN, NotGivenOr
from livekit.agents.utils.misc import is_given
from livekit.plugins.gnani.stt import STT, SpeechStream

log = logging.getLogger("prisma_v3")


class PrismaV3STT(STT):
    def __init__(
        self,
        *,
        api_key: str,
        language_code: str = "indic-auto",
        keyterms: Optional[list[str]] = None,
        topic: Optional[str] = None,
        vad_threshold: float = 0.6,
        min_silence_ms: int = 350,
        min_speech_ms: int = 150,
        min_segment_ms: int = 150,
        on_language: Optional[Callable[[str], None]] = None,
        sample_rate: int = 16000,
    ) -> None:
        # the parent still needs a "language" for its own bookkeeping
        super().__init__(
            language="hi-IN",
            api_key=api_key,
            sample_rate=sample_rate,
            use_streaming=True,
        )
        self.language_code = language_code
        self.keyterms = keyterms or []
        self.topic = topic
        self.vad_threshold = vad_threshold
        self.min_silence_ms = min_silence_ms
        self.min_speech_ms = min_speech_ms
        self.min_segment_ms = min_segment_ms
        self.on_language = on_language
        # last language Prisma reported, read by the agent when scoring
        self.last_detected_language: Optional[str] = None

    @property
    def model(self) -> str:
        return "prisma-v3.0"

    def stream(
        self,
        *,
        language: NotGivenOr[str] = NOT_GIVEN,
        conn_options: APIConnectOptions = DEFAULT_API_CONNECT_OPTIONS,
    ) -> "PrismaV3Stream":
        opts = replace(self._opts)
        if is_given(language):
            opts.language = language
        return PrismaV3Stream(
            stt=self,
            opts=opts,
            conn_options=self._single_attempt(conn_options),
        )


class PrismaV3Stream(SpeechStream):
    def __init__(self, *, stt: PrismaV3STT, opts, conn_options) -> None:
        super().__init__(stt=stt, opts=opts, conn_options=conn_options)
        self._v3 = stt

    def _headers(self) -> dict[str, str]:
        v3 = self._v3
        headers = {
            "x-api-key-id": self._opts.api_key,
            "model": "prisma-v3.0",
            "language_code": v3.language_code,
            "x-sample-rate": str(self._opts.sample_rate),
            "format": "verbatim",
            # --- VAD / endpointing: this is where most of the STT latency is.
            # The transcript can't arrive before min-silence has passed.
            "x-vad-threshold": str(v3.vad_threshold),
            "x-min-silence-ms": str(v3.min_silence_ms),
            "x-min-speech-ms": str(v3.min_speech_ms),
            "x-min-segment-ms": str(v3.min_segment_ms),
            "x-idle-timeout-sec": "300",
        }
        if v3.keyterms:
            # ensure_ascii=True turns Devanagari into \uXXXX so the header
            # stays plain ASCII (the docs recommend exactly this)
            headers["keyterms"] = json.dumps(v3.keyterms[:200], ensure_ascii=True)
        if v3.topic:
            headers["topic"] = v3.topic[:1600]
        return headers

    async def _run(self) -> None:
        import websockets

        try:
            async with websockets.connect(
                self._build_ws_url(),
                additional_headers=self._headers(),
                ping_interval=20,
                ping_timeout=20,
                close_timeout=10,
            ) as ws:
                first = json.loads(await asyncio.wait_for(ws.recv(), timeout=10))
                if first.get("type") != "connected":
                    log.warning("unexpected first STT message: %s", first)
                else:
                    log.info("prisma v3 connected: %s", first.get("config"))

                send_task = asyncio.create_task(self._send_audio(ws), name="prisma-send")
                recv_task = asyncio.create_task(self._recv_messages(ws), name="prisma-recv")
                try:
                    done, _ = await asyncio.wait(
                        [send_task, recv_task], return_when=asyncio.FIRST_COMPLETED
                    )
                    for t in done:
                        t.result()
                    if send_task.done() and not recv_task.done():
                        with contextlib.suppress(asyncio.TimeoutError):
                            await asyncio.wait_for(asyncio.shield(recv_task), timeout=1.0)
                finally:
                    await utils.aio.gracefully_cancel(send_task, recv_task)

        except websockets.exceptions.ConnectionClosed as e:
            raise APIConnectionError(f"Prisma websocket closed: {e}") from e
        except asyncio.TimeoutError as e:
            raise APITimeoutError("Prisma websocket connect timed out") from e
        except (APIConnectionError, APIStatusError, APITimeoutError):
            raise
        except Exception as e:
            raise APIConnectionError(f"Prisma websocket error: {e}") from e

    async def _recv_messages(self, ws) -> None:
        async for raw in ws:
            if isinstance(raw, bytes):
                continue
            data = json.loads(raw)
            kind = data.get("type", "")

            if kind == "transcript":
                self._mark_provider_progress()
                text = (data.get("text") or "").strip()
                if not text:
                    continue
                lang = data.get("detected_language_code") or "hi-IN"
                self._v3.last_detected_language = lang
                if self._v3.on_language:
                    self._v3.on_language(lang)
                log.info(
                    "prisma transcript [%s] %r (stt latency %s ms)",
                    lang, text, data.get("latency"),
                )
                self._event_ch.send_nowait(
                    stt.SpeechEvent(
                        type=stt.SpeechEventType.FINAL_TRANSCRIPT,
                        request_id=data.get("segment_id", ""),
                        alternatives=[
                            stt.SpeechData(
                                language=LanguageCode(lang), text=text, confidence=1.0
                            )
                        ],
                    )
                )
                # Gnani sends no "speech_end" message, only "processing" then
                # "transcript". With turn_detection="stt" LiveKit waits for an
                # END_OF_SPEECH event to close the user's turn, so without this
                # the transcript arrives and nothing ever answers. Every Prisma
                # transcript is already a finished VAD segment, so it is safe
                # to close the turn right after it.
                self._event_ch.send_nowait(
                    stt.SpeechEvent(type=stt.SpeechEventType.END_OF_SPEECH)
                )
            elif kind in ("speech_start", "vad_start"):
                self._mark_provider_progress()
                self._event_ch.send_nowait(
                    stt.SpeechEvent(type=stt.SpeechEventType.START_OF_SPEECH)
                )
            elif kind in ("speech_end", "vad_end"):
                self._mark_provider_progress()
                self._event_ch.send_nowait(
                    stt.SpeechEvent(type=stt.SpeechEventType.END_OF_SPEECH)
                )
            elif kind == "processing":
                self._mark_provider_progress()
            elif kind == "error":
                msg = data.get("message", "unknown error")
                log.error("prisma stream error: %s", msg)
                raise APIStatusError(message=f"Prisma error: {msg}", status_code=500, body=msg)