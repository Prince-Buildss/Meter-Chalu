import React, { useState, useEffect, useRef } from 'react';
import {
  LiveKitRoom,
  RoomAudioRenderer,
  StartAudio,
  useRoomContext,
  useVoiceAssistant,
} from '@livekit/components-react';
import { RoomEvent, Track, Participant, RemoteTrackPublication, RemoteParticipant } from 'livekit-client';
import {
  getBackendUrl,
  getStableRoomName,
  getDriverName,
  getHelperLanguage,
} from '../services/storage';
import { Volume2, Copy, ArrowLeft, RefreshCw, AlertCircle, Check } from 'lucide-react';

interface AudioTestScreenProps {
  onBack: () => void;
}

/**
 * Plain Diagnostic view mounted inside LiveKitRoom.
 * NO custom UI, NO orb, NO AudioContext/Analyser attached to tracks.
 * Uses <RoomAudioRenderer /> and <StartAudio />.
 */
const PlainDiagnosticView: React.FC<{
  addLog: (msg: string) => void;
  logs: string[];
  onPlayBeep: () => void;
  onCopyLogs: () => void;
  copied: boolean;
  onBack: () => void;
  helperLanguage: string;
}> = ({
  addLog,
  logs,
  onPlayBeep,
  onCopyLogs,
  copied,
  onBack,
  helperLanguage,
}) => {
  const room = useRoomContext();
  const voiceAssistant = useVoiceAssistant();

  const [connectionState, setConnectionState] = useState<string>(room.state);
  const [canPlaybackAudio, setCanPlaybackAudio] = useState<boolean>(room.canPlaybackAudio);
  const [isMicPublished, setIsMicPublished] = useState<boolean>(false);
  const [remoteParticipants, setRemoteParticipants] = useState<string[]>([]);
  const [remoteTracksInfo, setRemoteTracksInfo] = useState<{ id: string; kind: string; subscribed: boolean }[]>([]);

  // Update room state on room events
  useEffect(() => {
    const updateParticipants = () => {
      const pList = Array.from(room.remoteParticipants.values()).map(p => `${p.identity} (${p.kind})`);
      setRemoteParticipants(pList);

      const tracks: { id: string; kind: string; subscribed: boolean }[] = [];
      room.remoteParticipants.forEach(p => {
        p.trackPublications.forEach(pub => {
          tracks.push({
            id: `${p.identity}/${pub.trackSid || pub.source}`,
            kind: pub.kind,
            subscribed: pub.isSubscribed,
          });
        });
      });
      setRemoteTracksInfo(tracks);
    };

    const handleConnected = () => {
      addLog(`RoomEvent.Connected - state: ${room.state}`);
      setConnectionState(room.state);
      updateParticipants();
    };

    const handleParticipantConnected = (participant: Participant) => {
      addLog(`RoomEvent.ParticipantConnected: ${participant.identity} (kind: ${participant.kind})`);
      updateParticipants();
    };

    const handleParticipantDisconnected = (participant: Participant) => {
      addLog(`RoomEvent.ParticipantDisconnected: ${participant.identity}`);
      updateParticipants();
    };

    const handleTrackPublished = (pub: any, participant: Participant) => {
      addLog(`RoomEvent.TrackPublished: source=${pub.source} kind=${pub.kind} by ${participant.identity}`);
      if (participant === room.localParticipant && pub.kind === Track.Kind.Audio) {
        setIsMicPublished(true);
      }
      updateParticipants();
    };

    const handleTrackSubscribed = (track: Track, pub: RemoteTrackPublication, participant: RemoteParticipant) => {
      addLog(`RoomEvent.TrackSubscribed: kind=${track.kind} sid=${pub.trackSid} from ${participant.identity}`);
      updateParticipants();
    };

    const handleTrackUnsubscribed = (track: Track, pub: RemoteTrackPublication, participant: RemoteParticipant) => {
      addLog(`RoomEvent.TrackUnsubscribed: kind=${track.kind} sid=${pub.trackSid} from ${participant.identity}`);
      updateParticipants();
    };

    const handleAudioPlaybackStatusChanged = () => {
      addLog(`RoomEvent.AudioPlaybackStatusChanged: canPlaybackAudio=${room.canPlaybackAudio}`);
      setCanPlaybackAudio(room.canPlaybackAudio);
    };

    const handleMediaDevicesError = (e: Error) => {
      addLog(`RoomEvent.MediaDevicesError: ${e.name} - ${e.message}`);
    };

    const handleDisconnected = (reason?: any) => {
      addLog(`RoomEvent.Disconnected: reason=${reason}`);
      setConnectionState('disconnected');
      updateParticipants();
    };

    const handleDataReceived = (payload: Uint8Array, participant?: RemoteParticipant, _kind?: any, topic?: string) => {
      try {
        const text = new TextDecoder().decode(payload);
        addLog(`RoomEvent.DataReceived: topic=${topic} from=${participant?.identity} payload=${text.slice(0, 100)}`);
      } catch (err) {
        addLog(`RoomEvent.DataReceived: topic=${topic} error decoding payload`);
      }
    };

    // Check initial local mic track
    const localAudioPub = Array.from(room.localParticipant.trackPublications.values()).find(
      p => p.kind === Track.Kind.Audio
    );
    if (localAudioPub && localAudioPub.track) {
      setIsMicPublished(true);
    }
    updateParticipants();

    room.on(RoomEvent.Connected, handleConnected);
    room.on(RoomEvent.ParticipantConnected, handleParticipantConnected);
    room.on(RoomEvent.ParticipantDisconnected, handleParticipantDisconnected);
    room.on(RoomEvent.TrackPublished, handleTrackPublished);
    room.on(RoomEvent.TrackSubscribed, handleTrackSubscribed);
    room.on(RoomEvent.TrackUnsubscribed, handleTrackUnsubscribed);
    room.on(RoomEvent.AudioPlaybackStatusChanged, handleAudioPlaybackStatusChanged);
    room.on(RoomEvent.MediaDevicesError, handleMediaDevicesError);
    room.on(RoomEvent.Disconnected, handleDisconnected);
    room.on(RoomEvent.DataReceived, handleDataReceived);

    return () => {
      room.off(RoomEvent.Connected, handleConnected);
      room.off(RoomEvent.ParticipantConnected, handleParticipantConnected);
      room.off(RoomEvent.ParticipantDisconnected, handleParticipantDisconnected);
      room.off(RoomEvent.TrackPublished, handleTrackPublished);
      room.off(RoomEvent.TrackSubscribed, handleTrackSubscribed);
      room.off(RoomEvent.TrackUnsubscribed, handleTrackUnsubscribed);
      room.off(RoomEvent.AudioPlaybackStatusChanged, handleAudioPlaybackStatusChanged);
      room.off(RoomEvent.MediaDevicesError, handleMediaDevicesError);
      room.off(RoomEvent.Disconnected, handleDisconnected);
      room.off(RoomEvent.DataReceived, handleDataReceived);
    };
  }, [room]);

  return (
    <div className="flex flex-col h-full bg-black text-white p-4 font-mono text-xs overflow-y-auto">
      {/* Top action buttons */}
      <div className="flex items-center justify-between gap-2 mb-4 pb-3 border-b border-neutral-800">
        <button
          onClick={onBack}
          className="px-3 py-2 bg-neutral-800 hover:bg-neutral-700 rounded-lg text-white font-bold flex items-center gap-1.5"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Exit Audio Test</span>
        </button>

        <div className="flex gap-2">
          <button
            onClick={onPlayBeep}
            className="px-3 py-2 bg-amber-600 hover:bg-amber-500 rounded-lg text-black font-extrabold flex items-center gap-1.5"
          >
            <Volume2 className="w-4 h-4" />
            <span>Play test beep (440Hz)</span>
          </button>

          <button
            onClick={onCopyLogs}
            className="px-3 py-2 bg-neutral-800 hover:bg-neutral-700 rounded-lg text-white font-bold flex items-center gap-1.5"
          >
            {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
            <span>{copied ? 'Copied!' : 'Copy debug log'}</span>
          </button>
        </div>
      </div>

      {/* Start Audio Button if audio playback is blocked */}
      <div className="mb-4">
        <StartAudio label="Tap to enable sound / आवाज़ चालू करा (StartAudio)" className="w-full py-3 bg-[#FFC400] text-black font-black text-sm rounded-xl" />
      </div>

      {/* Plain text status list */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-xl p-3 mb-4 space-y-1.5 text-xs">
        <div className="text-[#FFC400] font-bold text-sm uppercase mb-2">Live Status Checklist:</div>
        <div>Connection State: <span className="font-bold text-white">{connectionState}</span></div>
        <div>Local Mic Published: <span className={isMicPublished ? 'text-emerald-400 font-bold' : 'text-amber-400 font-bold'}>{isMicPublished ? 'YES' : 'NO'}</span></div>
        <div>Can Playback Audio: <span className={canPlaybackAudio ? 'text-emerald-400 font-bold' : 'text-red-400 font-bold'}>{canPlaybackAudio ? 'YES' : 'NO'}</span></div>
        <div>Agent Voice State: <span className="text-cyan-400 font-bold">{voiceAssistant.state || '(none)'}</span></div>
        <div>Helper Language Sent: <span className="text-white font-bold">{helperLanguage}</span></div>
        <div>
          Remote Participants ({remoteParticipants.length}):
          <div className="ml-3 text-neutral-300">
            {remoteParticipants.length > 0 ? remoteParticipants.join(', ') : '(None yet - waiting for saathi)'}
          </div>
        </div>
        <div>
          Remote Audio Tracks ({remoteTracksInfo.length}):
          <div className="ml-3 text-neutral-300">
            {remoteTracksInfo.length > 0 ? (
              remoteTracksInfo.map(t => (
                <div key={t.id}>
                  {t.id} - {t.kind} - Subscribed: <span className={t.subscribed ? 'text-emerald-400' : 'text-amber-400'}>{t.subscribed ? 'YES' : 'NO'}</span>
                </div>
              ))
            ) : (
              '(None yet)'
            )}
          </div>
        </div>
      </div>

      {/* Live Event Log */}
      <div className="flex-1 flex flex-col bg-neutral-950 border border-neutral-900 rounded-xl p-3 overflow-hidden">
        <div className="text-neutral-400 font-bold mb-1 flex items-center justify-between">
          <span>Console / Room Event Log ({logs.length} entries):</span>
        </div>
        <div className="flex-1 overflow-y-auto space-y-1 text-[11px] text-neutral-300 pr-1">
          {logs.map((log, i) => (
            <div key={i} className="leading-tight font-mono whitespace-pre-wrap">
              {log}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

/**
 * Minimal Diagnostic Screen (TASK 1)
 */
export const AudioTestScreen: React.FC<AudioTestScreenProps> = ({ onBack }) => {
  const backendUrl = getBackendUrl();
  const roomName = getStableRoomName();
  const driverName = getDriverName();
  const helperLanguage = getHelperLanguage();

  const [serverUrl, setServerUrl] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoadingToken, setIsLoadingToken] = useState<boolean>(true);
  const [tokenError, setTokenError] = useState<string | null>(null);
  const [logs, setLogs] = useState<string[]>([]);
  const [copied, setCopied] = useState<boolean>(false);

  const connectionAttemptedRef = useRef<boolean>(false);

  const addLog = (msg: string) => {
    const timestamp = new Date().toISOString().substring(11, 23);
    const line = `[${timestamp}] ${msg}`;
    console.log(line);
    setLogs(prev => [...prev, line]);
  };

  // Play test beep (1-second 440Hz tone via Web Audio)
  const handlePlayBeep = () => {
    try {
      addLog('Playing 440Hz test beep...');
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      const ctx = new AudioCtx();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, ctx.currentTime);
      gain.gain.setValueAtTime(0.3, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 1);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 1);
      osc.onended = () => {
        addLog('440Hz test beep finished playing.');
        ctx.close().catch(() => {});
      };
    } catch (e: any) {
      addLog(`Test beep failed: ${e?.message}`);
    }
  };

  // Copy debug logs to clipboard
  const handleCopyLogs = () => {
    const text = logs.join('\n');
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }).catch(err => {
      addLog(`Clipboard copy error: ${err?.message}`);
    });
  };

  // Request token and join room once
  const initAudioTest = async () => {
    setIsLoadingToken(true);
    setTokenError(null);
    addLog(`Starting Audio Test for room ${roomName}...`);

    if (!backendUrl) {
      const err = 'Backend URL is empty. Please set it in Settings.';
      addLog(`ERROR: ${err}`);
      setTokenError(err);
      setIsLoadingToken(false);
      return;
    }

    try {
      // Pre-request mic permission, then IMMEDIATELY stop all tracks
      addLog('Requesting getUserMedia audio permission...');
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        stream.getTracks().forEach(t => t.stop());
        addLog('Mic permission granted; pre-check stream stopped cleanly.');
      } catch (permErr: any) {
        addLog(`Mic permission error: ${permErr?.message}`);
      }

      // Fetch token
      const body = {
        roomName,
        participantName: driverName,
        agentName: 'saathi',
        metadata: {
          name: driverName,
          helper_language: helperLanguage,
        },
      };

      const tokenUrl = `${backendUrl}/token`;
      addLog(`POST ${tokenUrl} with roomName=${roomName}, helper_language=${helperLanguage}`);

      const resp = await fetch(tokenUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });

      addLog(`POST ${tokenUrl} response HTTP ${resp.status}`);

      if (!resp.ok) {
        const text = await resp.text();
        const err = `HTTP ${resp.status}: ${text || resp.statusText}`;
        addLog(`Token fetch failed: ${err}`);
        setTokenError(err);
        setIsLoadingToken(false);
        return;
      }

      const data = await resp.json();
      addLog(`Token received successfully. serverUrl=${data.serverUrl}`);

      if (!data.serverUrl || !data.participantToken) {
        const err = 'serverUrl or participantToken missing from server response';
        addLog(`ERROR: ${err}`);
        setTokenError(err);
        setIsLoadingToken(false);
        return;
      }

      setServerUrl(data.serverUrl);
      setToken(data.participantToken);
      setIsLoadingToken(false);
    } catch (e: any) {
      const err = `Fetch token error: ${e?.message}`;
      addLog(`ERROR: ${err}`);
      setTokenError(err);
      setIsLoadingToken(false);
    }
  };

  useEffect(() => {
    if (connectionAttemptedRef.current) return;
    connectionAttemptedRef.current = true;
    initAudioTest();
  }, []);

  if (isLoadingToken) {
    return (
      <div className="flex flex-col items-center justify-center h-full bg-black text-white p-6 font-mono text-center">
        <div className="w-12 h-12 border-4 border-[#FFC400] border-t-transparent rounded-full animate-spin mb-4" />
        <h2 className="text-lg font-bold mb-2">Connecting to Audio Test...</h2>
        <p className="text-neutral-400 text-xs mb-4">Requesting token from {backendUrl}/token</p>
        <button
          onClick={onBack}
          className="px-4 py-2 bg-neutral-800 text-white rounded-lg text-xs font-bold"
        >
          Cancel
        </button>
      </div>
    );
  }

  if (tokenError || !serverUrl || !token) {
    return (
      <div className="flex flex-col items-center justify-center h-full bg-black text-white p-6 font-mono text-center">
        <AlertCircle className="w-12 h-12 text-red-500 mb-3" />
        <h2 className="text-lg font-bold mb-2">Token Request Failed</h2>
        <p className="text-xs text-red-400 bg-neutral-900 p-3 rounded-lg border border-neutral-800 mb-4 max-w-md">
          {tokenError}
        </p>
        <div className="flex gap-2">
          <button
            onClick={() => initAudioTest()}
            className="px-4 py-2 bg-[#FFC400] text-black font-bold text-xs rounded-lg flex items-center gap-1.5"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Retry</span>
          </button>
          <button
            onClick={onBack}
            className="px-4 py-2 bg-neutral-800 text-white font-bold text-xs rounded-lg"
          >
            Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <LiveKitRoom
      serverUrl={serverUrl}
      token={token}
      connect={true}
      audio={true}
      video={false}
      onDisconnected={() => addLog('LiveKitRoom.onDisconnected fired')}
      onError={(err) => addLog(`LiveKitRoom.onError: ${err?.message}`)}
      className="h-full w-full"
    >
      <RoomAudioRenderer />
      <PlainDiagnosticView
        addLog={addLog}
        logs={logs}
        onPlayBeep={handlePlayBeep}
        onCopyLogs={handleCopyLogs}
        copied={copied}
        onBack={onBack}
        helperLanguage={helperLanguage}
      />
    </LiveKitRoom>
  );
};
