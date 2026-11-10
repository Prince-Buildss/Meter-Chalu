# Meter Chalu (मीटर चालू)
> **"भाषा चालू, मीटर चालू!"** (Get your language going, and your meter keeps running!)

A mobile-first hands-free Marathi practice voice coach designed specifically for auto and taxi drivers in Maharashtra. It pairs drivers with simulated Marathi-speaking passengers and an empathetic voice coach ("Saathi") to help them speak everyday Marathi with confidence and comply with RTO spoken Marathi requirements.

---

## 🌟 Key Features

1. **Voice-First Conversation Flow**: Feels like talking on a phone call. Tap "बात करें" -> agent connects and speaks first -> hands-free practice until End.
2. **Interactive Audio-Reactive Orb**: Center animated orb with 3 live states:
   - `Listening` (glowing yellow breathing pulse)
   - `Thinking` (slow amber orbital spin)
   - `Speaking` (vibrant speech waveform ripple)
3. **Live "How Close Am I" Meter**:
   - Pinned target sentence in large **28sp** Marathi text above orb.
   - Individual word state coloring: `great` (green), `almost` (yellow), `miss` (grey outline, never red).
   - Animated score bar (0 to 100), "मैंने सुना: <heard>", and "शाबास!" (75+) or "थोड़ा और" (<75).
   - Live mic waveform while speaking.
4. **Scrolling Chat Transcript**: Agent Marathi lines in **28sp** on the left; driver speech on the right.
5. **Inline Practice Summary**: One clean card appended to chat upon receiving topic `readiness` payload.
6. **Sunlight High-Contrast UI**: #FFC400 and #121212, 20sp+ text, and bottom-anchored 56dp+ Mute & End buttons.
   - Single **Backend URL** field in Settings (e.g. `https://your-domain.com`), deriving token, report, and delete calls.
   - Integrated Mock Mode runtime toggle in Settings (default is **OFF** for immediate live backend connectivity).

---

## 🔌 Backend Integration & Protocol

### 1. Unified Backend URL
Configure a single **Backend URL** in Settings (e.g. `https://my-backend-domain.com`). All backend endpoints are derived from it:
- `POST {BASE}/token` — Fetch LiveKit server URL and participant token
- `GET {BASE}/report?room=<roomName>` — Fetch driver's persistent progress report
- `DELETE {BASE}/driver?room=<roomName>` — Forget me / delete driver data
- `GET {BASE}/health` — Quick connection test in Settings

### 2. Room Name
- `roomName = "driver-" + <8-character lowercase hex id>` (e.g. `driver-a1b2c3d4`).
- Generated once, saved in local storage, and reused for EVERY session.
- Strictly ASCII (no spaces, no driver name, no Devanagari).

### 3. Token Request
When a call starts, the app sends:
```json
POST {BASE}/token
Content-Type: application/json

{
  "roomName": "driver-a1b2c3d4",
  "participantName": "रमेश भाई",
  "agentName": "saathi",
  "metadata": {
    "name": "रमेश भाई",
    "helper_language": "hi"
  }
}
```
*(Note: `metadata.scenario` is included ONLY if the driver tapped a shortcut chip before connecting: `"dadar"`, `"fare"`, `"directions"`, `"elderly"`, or `"rto"`).*

Response:
```json
{
  "serverUrl": "wss://your-livekit-server.com",
  "participantToken": "eyJhbGciOi..."
}
```

### 4. Data Messages
Received on LiveKit data channel:
- **`topic: "target"`**: `{ "marathi": string, "meaning": string }` — Pinned above the orb in big 28sp text.
- **`topic: "closeness"`**: `{ "score": number, "words": [{"w": string, "state": "great"|"almost"|"miss"}], "heard": string, "mastered": boolean }` — Live meter with word highlights (great = green, almost = yellow, miss = grey outline). Uses `mastered` for green "शाबास!".
- **`topic: "readiness"`**: `{ percent, per_scenario, ready, note, scenario, score, detected_language }` — Appends an inline summary card to chat.
- **`topic: "report"`**: Full Home Report cached and shown on Home screen.
- **`topic: "client"`**: Published when driver taps a shortcut chip: `{"scenario": "<id>"}`.

### 5. Home Report
Fetched on Home screen load (`GET {BASE}/report?room=<roomName>`), cached locally, and refreshed after End:
- If `days_since_last >= 2`, shows "आपकी याद आई!" banner.
- Readiness percent & small practice disclaimer note.
- "आज तक" stats: sessions, streak days, closeness average with trend arrow.
- "इन्हें ठीक करो" cards with big Marathi text, meaning, tip, and count.
- "ठीक हो गया" line with fixed count.
- "मज़बूत" green scenario chips.
- "पिछली बार" carry forward phrase and recent session scores.

---

## 🛠️ Testing With Your Backend & Mock Mode

- **Live Mode (Default)**:
  1. Open **Settings** (gear icon on Home screen).
  2. Enter your **Backend URL** (e.g., `https://my-agent.example.com`).
  3. Tap **Test connection** (pings `GET {BASE}/health`).
  4. Tap **बात करें** on Home to connect. The screen will show the step-by-step debug strip (`Requesting token...` -> `Token OK` -> `Connecting to room...` -> `Room connected` -> `Agent joined` -> `Agent speaking`).

- **Mock Mode (Offline fallback)**:
  - Toggle **Mock mode** to ON in Settings to test without a running LiveKit server. Uses Android TTS and simulated conversation flow.

---

## 🗣️ Supported Helper Languages
- **Hindi (`hi`)** — Default & fully featured
- **English (`en`)** — Full support
- **Marathi (`mr`)**
- Gujarati, Bengali, Telugu, Tamil, Kannada, Punjabi, Malayalam (fallback to English)
