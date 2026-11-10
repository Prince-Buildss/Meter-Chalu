import React, { useState, useEffect, useRef } from 'react';
import {
  getBackendUrl,
  getIsMockMode,
  getStableRoomName,
  getDriverName,
  getHelperLanguage,
  setCachedReport,
} from '../services/storage';
import { ScenarioId, TargetSentence, ClosenessData, ReadinessSummary, ConversationMessage } from '../types';
import { SCENARIOS } from '../data/scenarios';
import { LiveKitCallRoom } from '../components/LiveKitCallRoom';
import { ConversationOrb } from '../components/ConversationOrb';
import { ClosenessMeter } from '../components/ClosenessMeter';
import { Mic, MicOff, PhoneOff, AlertTriangle, RefreshCw } from 'lucide-react';

interface ConversationScreenProps {
  initialScenarioId?: ScenarioId | null;
  onExitCall: () => void;
}

export const ConversationScreen: React.FC<ConversationScreenProps> = ({
  initialScenarioId,
  onExitCall,
}) => {
  // Read fresh every time screen mounts
  const backendUrl = getBackendUrl();
  const isMockMode = getIsMockMode();
  const roomName = getStableRoomName();
  const driverName = getDriverName();
  const helperLanguage = getHelperLanguage();

  // LiveKit Connection credentials
  const [serverUrl, setServerUrl] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoadingToken, setIsLoadingToken] = useState<boolean>(!isMockMode);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [micPermissionDenied, setMicPermissionDenied] = useState<boolean>(false);

  // Guard against React StrictMode double execution
  const connectionAttemptedRef = useRef<boolean>(false);

  // --- MOCK MODE STATES (Only active when isMockMode is true) ---
  const [mockOrbState, setMockOrbState] = useState<'listening' | 'thinking' | 'speaking'>('speaking');
  const [mockTargetSentence, setMockTargetSentence] = useState<TargetSentence | null>(null);
  const [mockCloseness, setMockCloseness] = useState<ClosenessData | null>(null);
  const [mockMessages, setMockMessages] = useState<ConversationMessage[]>([]);
  const [mockIsMuted, setMockIsMuted] = useState<boolean>(false);
  const mockTimeoutRef = useRef<any>(null);

  // Refresh home report after session
  const refreshHomeReport = async () => {
    if (!isMockMode && backendUrl) {
      try {
        const resp = await fetch(`${backendUrl}/report?room=${roomName}`);
        if (resp.ok) {
          const rep = await resp.json();
          setCachedReport(rep);
        }
      } catch (e) {
        console.warn('Failed to refresh report after call', e);
      }
    }
  };

  const handleEndCall = () => {
    refreshHomeReport();
    onExitCall();
  };

  // 1. CONNECT ONCE on mount (Read fresh, request token, request mic)
  const connectLiveKit = async () => {
    setIsLoadingToken(true);
    setErrorMessage(null);
    setMicPermissionDenied(false);

    // Validate Backend URL
    if (!backendUrl) {
      setErrorMessage('Backend URL is empty. Please set it in Settings (सेटिंग्ज).');
      setIsLoadingToken(false);
      return;
    }
    if (!backendUrl.startsWith('https://')) {
      setErrorMessage('Backend URL must be HTTPS (https:// आवश्यक आहे). Please check Settings.');
      setIsLoadingToken(false);
      return;
    }

    try {
      // Step A: Request microphone permission inside the same action
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        // Release immediate test stream; LiveKit will open the actual mic track
        stream.getTracks().forEach(t => t.stop());
      } catch (permErr: any) {
        console.error('Microphone permission denied', permErr);
        setMicPermissionDenied(true);
        setIsLoadingToken(false);
        return;
      }

      // Step B: POST {BASE}/token
      const metadata: Record<string, string> = {
        name: driverName,
        helper_language: helperLanguage,
      };
      if (initialScenarioId) {
        metadata.scenario = initialScenarioId;
      }

      const bodyPayload = {
        roomName,
        participantName: driverName,
        agentName: 'saathi',
        metadata,
      };

      const tokenUrl = `${backendUrl}/token`;
      console.log(`[Token Request] POST ${tokenUrl}`, { roomName, participantName: driverName });

      const resp = await fetch(tokenUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bodyPayload),
      });

      console.log(`[Token Response] HTTP ${resp.status}`);

      if (!resp.ok) {
        const errText = await resp.text();
        const err = `Token request failed: HTTP ${resp.status} - ${errText || resp.statusText}`;
        console.error(err);
        setErrorMessage(err);
        setIsLoadingToken(false);
        return;
      }

      const data = await resp.json();
      console.log('[Token Success] serverUrl:', data.serverUrl, 'token received');

      if (!data.serverUrl || !data.participantToken) {
        setErrorMessage('Server response missing serverUrl or participantToken.');
        setIsLoadingToken(false);
        return;
      }

      setServerUrl(data.serverUrl);
      setToken(data.participantToken);
      setIsLoadingToken(false);
    } catch (e: any) {
      console.error('[Connection Error]', e);
      setErrorMessage(`Connection failed: ${e?.message || 'Network error'}. Check Backend URL.`);
      setIsLoadingToken(false);
    }
  };

  useEffect(() => {
    if (connectionAttemptedRef.current) return;
    connectionAttemptedRef.current = true;

    if (!isMockMode) {
      connectLiveKit();
    } else {
      // Start Mock Simulation
      startMockSimulation();
    }

    return () => {
      if (mockTimeoutRef.current) {
        clearTimeout(mockTimeoutRef.current);
      }
    };
  }, []);

  // --- Mock Mode Offline Simulator ---
  const startMockSimulation = () => {
    const sc = SCENARIOS.find(s => s.id === initialScenarioId) || SCENARIOS[0];
    setMockTargetSentence({
      marathi: sc.firstLineMarathi,
      meaning: sc.firstLineHindi,
    });
    setMockMessages([
      {
        id: 'mock_1',
        text: `नमस्ते ${driverName}! चला मराठीचा सराव करूया.`,
        isAgent: true,
        meaning: 'नमस्ते! चलिए मराठी का अभ्यास करते हैं।',
        timestamp: Date.now(),
      },
      {
        id: 'mock_2',
        text: sc.firstLineMarathi,
        isAgent: true,
        meaning: sc.firstLineHindi,
        timestamp: Date.now() + 500,
      },
    ]);

    mockTimeoutRef.current = setTimeout(() => {
      setMockOrbState('listening');
      // Simulate closeness result
      setMockCloseness({
        score: 85,
        words: [
          { w: 'दादा', state: 'great' },
          { w: 'दादर', state: 'great' },
          { w: 'स्टेशनला', state: 'great' },
          { w: 'जाणार', state: 'almost' },
          { w: 'का', state: 'great' },
        ],
        heard: sc.firstLineMarathi,
        mastered: true,
      });
    }, 2500);
  };

  // --- Render Error State (e.g. Mic Denied or Network Error) ---
  if (micPermissionDenied) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-6 text-center bg-[#121212]">
        <AlertTriangle className="w-16 h-16 text-amber-500 mb-4 animate-bounce" />
        <h2 className="text-2xl font-black text-white mb-2">मायक्रोफोन परवानगी आवश्यक आहे</h2>
        <p className="text-neutral-300 text-sm mb-6 max-w-sm">
          Please allow microphone permission in your browser to talk hands-free with Saathi.
        </p>
        <div className="flex gap-4">
          <button
            onClick={() => connectLiveKit()}
            className="px-6 py-3 rounded-xl bg-[#FFC400] text-black font-extrabold text-base flex items-center gap-2 shadow-lg"
          >
            <RefreshCw className="w-5 h-5 text-black" />
            <span>पुन्हा प्रयत्न करा (Retry)</span>
          </button>
          <button
            onClick={onExitCall}
            className="px-6 py-3 rounded-xl bg-neutral-800 text-white font-bold text-base"
          >
            मागे जा (Back)
          </button>
        </div>
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-6 text-center bg-[#121212]">
        <AlertTriangle className="w-16 h-16 text-red-500 mb-4" />
        <h2 className="text-2xl font-black text-white mb-2">कनेक्शन एरर (Connection Error)</h2>
        <p className="text-neutral-300 text-sm mb-2 max-w-md font-mono bg-neutral-900 p-3 rounded-lg border border-neutral-800">
          {errorMessage}
        </p>
        <p className="text-xs text-neutral-400 mb-6">
          Make sure your Backend URL is set in Settings and is returning a valid LiveKit token.
        </p>
        <div className="flex gap-3">
          <button
            onClick={() => connectLiveKit()}
            className="px-6 py-3 rounded-xl bg-[#FFC400] text-black font-extrabold text-base flex items-center gap-2 shadow-lg"
          >
            <RefreshCw className="w-5 h-5 text-black" />
            <span>Retry Token</span>
          </button>
          <button
            onClick={onExitCall}
            className="px-6 py-3 rounded-xl bg-neutral-800 text-white font-bold text-base"
          >
            सेटिंग्ज तपासा (Settings)
          </button>
        </div>
      </div>
    );
  }

  // --- Render Loading Token ---
  if (isLoadingToken) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-6 text-center bg-[#121212]">
        <div className="w-16 h-16 border-4 border-[#FFC400] border-t-transparent rounded-full animate-spin mb-4" />
        <h3 className="text-xl font-bold text-white mb-1">टोकन विनंती करत आहे...</h3>
        <p className="text-sm text-neutral-400">Requesting token from backend...</p>
        <div className="text-xs text-neutral-500 mt-4 font-mono">
          Room: {roomName}
        </div>
      </div>
    );
  }

  // --- LIVE MODE: Render <LiveKitCallRoom> containing <LiveKitRoom> & <RoomAudioRenderer> ---
  if (!isMockMode && serverUrl && token) {
    return (
      <LiveKitCallRoom
        serverUrl={serverUrl}
        token={token}
        roomName={roomName}
        backendUrl={backendUrl}
        initialScenarioId={initialScenarioId}
        helperLanguage={helperLanguage}
        onEndCall={handleEndCall}
        onError={(err) => setErrorMessage(err)}
      />
    );
  }

  // --- MOCK MODE: Offline simulated call screen ---
  return (
    <div className="flex flex-col h-full bg-[#121212] text-white">
      {/* Mock Badge */}
      <div className="bg-neutral-900 border-b border-neutral-800 px-3 py-1 text-xs font-mono flex items-center justify-between text-amber-400">
        <span className="font-bold">MOCK MODE (Offline)</span>
        <span className="text-neutral-400 text-[11px]">Settings मध्ये Live चालू करा</span>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-2 flex flex-col">
        <ClosenessMeter
          targetSentence={mockTargetSentence}
          closenessData={mockCloseness}
        />

        <div className="my-auto py-2">
          <ConversationOrb
            state={mockOrbState}
            audioLevel={mockOrbState === 'speaking' ? 0.7 : 0.1}
            helperLanguage={helperLanguage}
          />
        </div>

        <div className="w-full flex-1 min-h-[160px] max-h-[300px] overflow-y-auto bg-neutral-950/60 rounded-2xl p-3 border border-neutral-900 flex flex-col gap-2.5">
          {mockMessages.map((m) => (
            <div
              key={m.id}
              className={`flex flex-col ${m.isAgent ? 'items-start' : 'items-end'}`}
            >
              <div className="text-[10px] text-neutral-500 mb-0.5 px-1 font-semibold">
                {m.isAgent ? 'साथी (Mock Saathi)' : 'तुम्ही (Driver)'}
              </div>
              <div
                className={`max-w-[88%] rounded-2xl px-4 py-2.5 ${
                  m.isAgent
                    ? 'bg-neutral-900 text-white border border-neutral-800 rounded-tl-none'
                    : 'bg-[#FFC400] text-black font-bold rounded-tr-none shadow-md'
                }`}
              >
                <div className={m.isAgent ? 'text-[26px] font-extrabold text-white' : 'text-base font-semibold text-black'}>
                  {m.text}
                </div>
                {m.meaning && (
                  <div className="text-xs text-neutral-400 mt-1 italic">"{m.meaning}"</div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="p-4 bg-neutral-950 border-t border-neutral-900 grid grid-cols-2 gap-3 pb-8">
        <button
          onClick={() => setMockIsMuted(!mockIsMuted)}
          className={`h-16 rounded-2xl flex items-center justify-center gap-2 text-lg font-bold transition-all ${
            mockIsMuted ? 'bg-amber-600 text-white' : 'bg-neutral-800 text-white border border-neutral-700'
          }`}
        >
          {mockIsMuted ? <MicOff className="w-6 h-6 text-white" /> : <Mic className="w-6 h-6 text-[#FFC400]" />}
          <span>{mockIsMuted ? 'Unmute' : 'Mute'}</span>
        </button>

        <button
          onClick={handleEndCall}
          className="h-16 rounded-2xl bg-red-600 text-white font-black text-lg flex items-center justify-center gap-2"
        >
          <PhoneOff className="w-6 h-6 text-white" />
          <span>बंद करा (End)</span>
        </button>
      </div>
    </div>
  );
};
