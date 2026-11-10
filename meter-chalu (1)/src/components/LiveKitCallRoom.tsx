import React, { useEffect, useState, useRef, useMemo } from 'react';
import {
  LiveKitRoom,
  RoomAudioRenderer,
  StartAudio,
  useRoomContext,
  useVoiceAssistant,
  useTranscriptions,
  useTrackVolume,
} from '@livekit/components-react';
import { RoomEvent, Track, Participant, RemoteTrackPublication, RemoteParticipant } from 'livekit-client';
import {
  TargetSentence,
  ClosenessData,
  ReadinessSummary,
  ConversationMessage,
  ScenarioId,
} from '../types';
import { SCENARIOS } from '../data/scenarios';
import { ConversationOrb } from './ConversationOrb';
import { ClosenessMeter } from './ClosenessMeter';
import { Mic, MicOff, PhoneOff, AlertCircle, Copy, Check } from 'lucide-react';
import { setCachedReport } from '../services/storage';

interface LiveKitCallRoomProps {
  serverUrl: string;
  token: string;
  roomName: string;
  backendUrl: string;
  initialScenarioId?: ScenarioId | null;
  helperLanguage: string;
  onEndCall: () => void;
  onError: (error: string) => void;
}

/**
 * Inner component mounted inside LiveKitRoom.
 * Accesses room context, tracks audio playback, listens for data messages,
 * drives the audio-reactive orb and scrolling transcripts.
 * ZERO custom AudioContext/AnalyserNode touching the remote track.
 */
const LiveCallContent: React.FC<{
  roomName: string;
  backendUrl: string;
  initialScenarioId?: ScenarioId | null;
  helperLanguage: string;
  onEndCall: () => void;
}> = ({
  roomName,
  backendUrl,
  initialScenarioId,
  helperLanguage,
  onEndCall,
}) => {
  const room = useRoomContext();
  const voiceAssistant = useVoiceAssistant();
  const transcriptions = useTranscriptions();

  // Volume from LiveKit's safe built-in hook - NO custom AudioContext
  const agentVolume = useTrackVolume(voiceAssistant.audioTrack);

  // Playback state
  const [canPlaybackAudio, setCanPlaybackAudio] = useState<boolean>(room.canPlaybackAudio);
  const [hasFirstAudioPlayed, setHasFirstAudioPlayed] = useState<boolean>(false);

  // Debug strip states
  const [connectionState, setConnectionState] = useState<string>(room.state);
  const [isMicPublished, setIsMicPublished] = useState<boolean>(false);
  const [remoteIdentities, setRemoteIdentities] = useState<string[]>([]);
  const [isAgentAudioSubscribed, setIsAgentAudioSubscribed] = useState<boolean>(false);
  const [lastError, setLastError] = useState<string | null>(null);

  // Timestamped log buffer for developer copy
  const [eventLogs, setEventLogs] = useState<string[]>([]);
  const [copiedLog, setCopiedLog] = useState<boolean>(false);

  // Mute state
  const [isMuted, setIsMuted] = useState<boolean>(false);

  // Practice state
  const [targetSentence, setTargetSentence] = useState<TargetSentence | null>(null);
  const [closenessData, setClosenessData] = useState<ClosenessData | null>(null);
  const [messages, setMessages] = useState<ConversationMessage[]>([]);
  const [activeScenario, setActiveScenario] = useState<ScenarioId | null>(initialScenarioId || null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const addLog = (msg: string) => {
    const timestamp = new Date().toISOString().substring(11, 23);
    const line = `[${timestamp}] ${msg}`;
    console.log(line);
    setEventLogs(prev => [...prev.slice(-90), line]);
  };

  // Copy debug log to clipboard
  const handleCopyLogs = () => {
    const summary = [
      `=== METER CHALU CALL DEBUG LOG ===`,
      `Time: ${new Date().toISOString()}`,
      `Room: ${roomName}`,
      `Backend URL: ${backendUrl}`,
      `Helper Language Sent: ${helperLanguage}`,
      `Connection State: ${connectionState}`,
      `Mic Published: ${isMicPublished ? (isMuted ? 'MUTED' : 'YES') : 'NO'}`,
      `Agent Subscribed: ${isAgentAudioSubscribed ? 'YES' : 'NO'}`,
      `Can Playback Audio: ${canPlaybackAudio ? 'YES' : 'NO'}`,
      `Agent State: ${voiceAssistant.state}`,
      `Remote Participants: ${remoteIdentities.join(', ') || '(none)'}`,
      `Last Error: ${lastError || 'None'}`,
      `\n--- Event History ---`,
      ...eventLogs,
    ].join('\n');

    navigator.clipboard.writeText(summary).then(() => {
      setCopiedLog(true);
      setTimeout(() => setCopiedLog(false), 2500);
    }).catch(err => {
      console.error('Failed to copy debug log', err);
    });
  };

  // Auto scroll chat transcript
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, transcriptions]);

  // Audio start & Playback status tracking
  useEffect(() => {
    setCanPlaybackAudio(room.canPlaybackAudio);
    if (!room.canPlaybackAudio) {
      room.startAudio().then(() => {
        setCanPlaybackAudio(room.canPlaybackAudio);
        addLog(`startAudio called. canPlaybackAudio=${room.canPlaybackAudio}`);
      }).catch((e) => {
        addLog(`startAudio error: ${e?.message}`);
      });
    }

    const onAudioPlaybackStatusChanged = () => {
      addLog(`RoomEvent.AudioPlaybackStatusChanged: canPlaybackAudio=${room.canPlaybackAudio}`);
      setCanPlaybackAudio(room.canPlaybackAudio);
    };

    room.on(RoomEvent.AudioPlaybackStatusChanged, onAudioPlaybackStatusChanged);
    return () => {
      room.off(RoomEvent.AudioPlaybackStatusChanged, onAudioPlaybackStatusChanged);
    };
  }, [room]);

  // Track first audio played
  useEffect(() => {
    if (voiceAssistant.state === 'speaking' || agentVolume > 0.05) {
      setHasFirstAudioPlayed(true);
    }
  }, [voiceAssistant.state, agentVolume]);

  // Sync transcription stream to message history
  useEffect(() => {
    if (transcriptions && transcriptions.length > 0) {
      const formatted: ConversationMessage[] = transcriptions.map((t, idx) => {
        const isAgent = t.participantInfo.identity !== room.localParticipant.identity;
        return {
          id: `trans_${idx}_${t.streamInfo.id || idx}`,
          text: t.text,
          isAgent,
          timestamp: Date.now(),
        };
      });
      setMessages((prev) => {
        const existingIds = new Set(prev.map(m => m.id));
        const newOnes = formatted.filter(f => !existingIds.has(f.id));
        if (newOnes.length > 0) {
          return [...prev, ...newOnes];
        }
        return prev;
      });
    }
  }, [transcriptions, room.localParticipant.identity]);

  // Main Room Events & Data Messages
  useEffect(() => {
    const updateParticipants = () => {
      const pList = Array.from(room.remoteParticipants.values()).map(p => p.identity);
      setRemoteIdentities(pList);
    };

    const handleConnected = () => {
      addLog(`RoomEvent.Connected - state: ${room.state}`);
      setConnectionState(room.state);
      setLastError(null);
      updateParticipants();
    };

    const handleParticipantConnected = (participant: Participant) => {
      addLog(`RoomEvent.ParticipantConnected: ${participant.identity} (${participant.kind})`);
      updateParticipants();
    };

    const handleParticipantDisconnected = (participant: Participant) => {
      addLog(`RoomEvent.ParticipantDisconnected: ${participant.identity}`);
      updateParticipants();
    };

    const handleTrackSubscribed = (
      track: Track,
      pub: RemoteTrackPublication,
      participant: RemoteParticipant
    ) => {
      addLog(`RoomEvent.TrackSubscribed: ${track.kind} from ${participant.identity}`);
      if (track.kind === Track.Kind.Audio) {
        setIsAgentAudioSubscribed(true);
        room.startAudio().catch(() => {});
      }
    };

    const handleTrackPublished = (pub: any, participant: Participant) => {
      addLog(`RoomEvent.TrackPublished: ${pub.kind} by ${participant.identity}`);
      if (participant === room.localParticipant && pub.kind === Track.Kind.Audio) {
        setIsMicPublished(true);
      }
    };

    const handleMediaDevicesError = (e: Error) => {
      addLog(`RoomEvent.MediaDevicesError: ${e.name} - ${e.message}`);
      setLastError(`Mic error: ${e.message}`);
    };

    const handleDisconnected = (reason?: any) => {
      addLog(`RoomEvent.Disconnected: reason=${reason}`);
      setConnectionState('disconnected');
    };

    const handleDataReceived = (
      payload: Uint8Array,
      participant?: RemoteParticipant,
      _kind?: any,
      topic?: string
    ) => {
      try {
        const decoded = new TextDecoder().decode(payload);
        addLog(`RoomEvent.DataReceived [${topic}]: ${decoded.slice(0, 100)}`);
        const data = JSON.parse(decoded);

        if (topic === 'target') {
          setTargetSentence({
            marathi: data.marathi || '',
            meaning: data.meaning || '',
          });
        } else if (topic === 'closeness') {
          setClosenessData({
            score: typeof data.score === 'number' ? data.score : 0,
            words: Array.isArray(data.words) ? data.words : [],
            heard: data.heard || '',
            mastered: Boolean(data.mastered),
          });
        } else if (topic === 'readiness') {
          const summary: ReadinessSummary = {
            percent: data.percent ?? 50,
            per_scenario: data.per_scenario ?? {},
            ready: Boolean(data.ready),
            note: data.note || 'सराव गुण (Practice score)',
            scenario: data.scenario,
            score: data.score,
            detected_language: data.detected_language,
          };
          setMessages(prev => [
            ...prev,
            {
              id: `readiness_${Date.now()}`,
              text: '',
              isAgent: true,
              timestamp: Date.now(),
              isSummaryCard: true,
              summaryData: summary,
            },
          ]);
        } else if (topic === 'report') {
          setCachedReport(data);
        }
      } catch (err: any) {
        addLog(`Failed to parse data message: ${err?.message}`);
      }
    };

    // Initial check of local mic
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
    room.on(RoomEvent.TrackSubscribed, handleTrackSubscribed);
    room.on(RoomEvent.TrackPublished, handleTrackPublished);
    room.on(RoomEvent.MediaDevicesError, handleMediaDevicesError);
    room.on(RoomEvent.Disconnected, handleDisconnected);
    room.on(RoomEvent.DataReceived, handleDataReceived);

    return () => {
      room.off(RoomEvent.Connected, handleConnected);
      room.off(RoomEvent.ParticipantConnected, handleParticipantConnected);
      room.off(RoomEvent.ParticipantDisconnected, handleParticipantDisconnected);
      room.off(RoomEvent.TrackSubscribed, handleTrackSubscribed);
      room.off(RoomEvent.TrackPublished, handleTrackPublished);
      room.off(RoomEvent.MediaDevicesError, handleMediaDevicesError);
      room.off(RoomEvent.Disconnected, handleDisconnected);
      room.off(RoomEvent.DataReceived, handleDataReceived);
    };
  }, [room]);

  // Toggle Mute
  const handleToggleMute = async () => {
    try {
      const nextState = !isMuted;
      await room.localParticipant.setMicrophoneEnabled(!nextState);
      setIsMuted(nextState);
      addLog(`Microphone set to enabled=${!nextState}`);
    } catch (e: any) {
      addLog(`Toggle mute failed: ${e?.message}`);
      setLastError(e?.message || 'Mute toggle failed');
    }
  };

  // End Call: disconnect room and callback
  const handleEnd = () => {
    try {
      addLog('User pressed End. Disconnecting room...');
      room.disconnect();
    } catch (e) {
      console.warn('[LiveKit] Disconnect error', e);
    }
    onEndCall();
  };

  // Publish Shortcut chip scenario
  const handleSelectScenario = async (scId: ScenarioId) => {
    setActiveScenario(scId);
    try {
      const payload = JSON.stringify({ scenario: scId });
      await room.localParticipant.publishData(new TextEncoder().encode(payload), {
        reliable: true,
        topic: 'client',
      });
      addLog(`Published shortcut topic 'client': ${payload}`);
    } catch (e: any) {
      addLog(`Failed to publish scenario shortcut: ${e?.message}`);
      setLastError(e?.message || 'Failed to switch scenario');
    }
  };

  // Derive Orb State without any custom AudioContext
  const derivedOrbState = useMemo<'connecting' | 'listening' | 'thinking' | 'speaking'>(() => {
    if (!hasFirstAudioPlayed && connectionState !== 'connected') {
      return 'connecting';
    }
    const agentState = voiceAssistant.state;
    if (agentState === 'speaking' || agentVolume > 0.05) {
      return 'speaking';
    }
    if (agentState === 'thinking') {
      return 'thinking';
    }
    if (agentState === 'listening') {
      return 'listening';
    }
    if (!hasFirstAudioPlayed) {
      return 'connecting';
    }
    return 'listening';
  }, [voiceAssistant.state, agentVolume, hasFirstAudioPlayed, connectionState]);

  return (
    <div className="flex flex-col h-full bg-[#121212] text-white">
      {/* 6. ALWAYS-VISIBLE DEBUG STRIP (TASK 3) */}
      <div className="bg-neutral-950/95 border-b border-neutral-800 px-3 py-1.5 text-[11px] font-mono leading-tight flex flex-col gap-0.5">
        <div className="flex items-center justify-between text-neutral-300">
          <div className="flex items-center gap-2">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span className="font-bold text-[#FFC400]">LIVE</span>
            <span className="text-neutral-400">State:</span>
            <span className="text-white font-semibold">{connectionState}</span>
            <span className="text-neutral-500">|</span>
            <span className="text-neutral-400">Agent:</span>
            <span className="text-cyan-400 font-bold">{voiceAssistant.state || (remoteIdentities.length > 0 ? 'joined' : 'waiting')}</span>
          </div>
          <button
            onClick={handleCopyLogs}
            className="px-2 py-0.5 bg-neutral-800 hover:bg-neutral-700 rounded text-[10px] text-neutral-200 font-bold flex items-center gap-1 border border-neutral-700"
            title="Copy debug log"
          >
            {copiedLog ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
            <span>{copiedLog ? 'Copied' : 'Copy log'}</span>
          </button>
        </div>

        <div className="flex items-center justify-between text-neutral-400 text-[10px]">
          <div>
            <span>Mic: </span>
            <span className={isMicPublished ? (isMuted ? 'text-amber-400 font-bold' : 'text-emerald-400 font-bold') : 'text-red-400 font-bold'}>
              {isMicPublished ? (isMuted ? 'MUTED' : 'YES') : 'NO'}
            </span>
            <span className="ml-2">AudioSub: </span>
            <span className={isAgentAudioSubscribed ? 'text-emerald-400 font-bold' : 'text-amber-400'}>
              {isAgentAudioSubscribed ? 'YES' : 'NO'}
            </span>
            <span className="ml-2">Playable: </span>
            <span className={canPlaybackAudio ? 'text-emerald-400 font-bold' : 'text-red-400 font-bold'}>
              {canPlaybackAudio ? 'YES' : 'NO'}
            </span>
          </div>
          <div>
            <span>Lang: </span>
            <span className="text-[#FFC400] font-bold">{helperLanguage}</span>
          </div>
        </div>

        <div className="text-[10px] text-neutral-400 truncate flex items-center justify-between">
          <span>Remotes: <span className="text-neutral-200">{remoteIdentities.length > 0 ? remoteIdentities.join(', ') : '(waiting for saathi...)'}</span></span>
          <span className="text-neutral-500">{roomName}</span>
        </div>

        {lastError && (
          <div className="text-amber-400 text-[10px] flex items-center gap-1 mt-0.5 truncate">
            <AlertCircle className="w-3 h-3 flex-shrink-0" />
            <span>{lastError}</span>
          </div>
        )}
      </div>

      {/* LiveKit StartAudio button if audio playback is blocked */}
      {!canPlaybackAudio && (
        <div className="p-2 bg-amber-500/10 border-b border-amber-500/30">
          <StartAudio
            label="Tap to enable sound / आवाज़ चालू करा (Start Audio)"
            className="w-full py-3 bg-[#FFC400] text-black font-extrabold text-base rounded-xl shadow-lg animate-pulse"
          />
        </div>
      )}

      {/* Main Call View */}
      <div className="flex-1 overflow-y-auto px-4 py-2 flex flex-col">
        {/* Shortcut chips to trigger scenarios hands-free */}
        <div className="flex gap-2 overflow-x-auto py-1 mb-1 no-scrollbar select-none">
          {SCENARIOS.map((sc) => {
            const isSelected = activeScenario === sc.id;
            return (
              <button
                key={sc.id}
                onClick={() => handleSelectScenario(sc.id)}
                className={`px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap transition-all flex items-center gap-1.5 ${
                  isSelected
                    ? 'bg-[#FFC400] text-black shadow-md'
                    : 'bg-neutral-900 border border-neutral-800 text-neutral-300 hover:border-neutral-700'
                }`}
              >
                <span>{sc.icon}</span>
                <span>{sc.titleMarathi}</span>
              </button>
            );
          })}
        </div>

        {/* Pinned Target Sentence and Closeness Bar */}
        <ClosenessMeter
          targetSentence={targetSentence}
          closenessData={closenessData}
        />

        {/* Center: Audio-Reactive Orb driven strictly by useVoiceAssistant + useTrackVolume */}
        <div className="my-auto py-2">
          <ConversationOrb
            state={derivedOrbState}
            audioLevel={agentVolume}
            helperLanguage={helperLanguage}
          />
        </div>

        {/* Scrolling Chat Transcript: Agent in 28sp (28px), Driver smaller */}
        <div className="w-full flex-1 min-h-[160px] max-h-[300px] overflow-y-auto bg-neutral-950/60 rounded-2xl p-3 border border-neutral-900 flex flex-col gap-2.5">
          {messages.length === 0 ? (
            <div className="m-auto text-center text-neutral-500 text-sm font-medium">
              {derivedOrbState === 'connecting'
                ? 'साथीशी जोडत आहे... (Connecting to Saathi)'
                : 'बोला, साथी ऐकत आहे... (Speak, Saathi is listening)'}
            </div>
          ) : (
            messages.map((m) => {
              if (m.isSummaryCard && m.summaryData) {
                const s = m.summaryData;
                return (
                  <div
                    key={m.id}
                    className="w-full my-2 bg-[#1c1c1c] border-2 border-[#FFC400] rounded-2xl p-4 shadow-xl text-center"
                  >
                    <div className="text-xs font-bold text-[#FFC400] uppercase tracking-wider mb-1">
                      सराव सारांश (Practice Summary)
                    </div>
                    <div className="text-4xl font-black text-[#FFC400] my-1">
                      {s.percent}%
                    </div>
                    <div className="text-sm text-neutral-300 font-semibold mb-2">
                      {s.note}
                    </div>
                    {s.ready ? (
                      <div className="inline-block bg-emerald-600/30 text-emerald-400 border border-emerald-500 px-3 py-1 rounded-full text-xs font-bold">
                        RTO परीक्षेसाठी उत्तम तयारी!
                      </div>
                    ) : (
                      <div className="inline-block bg-amber-500/20 text-amber-300 border border-amber-400 px-3 py-1 rounded-full text-xs font-semibold">
                        रोज १० मिनिटे सराव करा!
                      </div>
                    )}
                  </div>
                );
              }

              return (
                <div
                  key={m.id}
                  className={`flex flex-col ${m.isAgent ? 'items-start' : 'items-end'}`}
                >
                  <div className="text-[10px] text-neutral-500 mb-0.5 px-1 font-semibold">
                    {m.isAgent ? 'साथी (Saathi)' : 'तुम्ही (Driver)'}
                  </div>
                  <div
                    className={`max-w-[88%] rounded-2xl px-4 py-2.5 ${
                      m.isAgent
                        ? 'bg-neutral-900 text-white border border-neutral-800 rounded-tl-none'
                        : 'bg-[#FFC400] text-black font-bold rounded-tr-none shadow-md'
                    }`}
                  >
                    <div
                      className={
                        m.isAgent
                          ? 'text-[26px] sm:text-[28px] font-extrabold leading-tight text-white'
                          : 'text-base font-semibold leading-snug text-black'
                      }
                    >
                      {m.text}
                    </div>
                    {m.meaning && (
                      <div className="text-xs text-neutral-400 mt-1 italic">
                        "{m.meaning}"
                      </div>
                    )}
                  </div>
                </div>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* 5. CONTROLS: Only Two Big Buttons: Mute and End (56dp+, one-handed at bottom) */}
      <div className="p-4 bg-neutral-950 border-t border-neutral-900 grid grid-cols-2 gap-3 pb-8 select-none">
        <button
          onClick={handleToggleMute}
          className={`h-16 rounded-2xl flex items-center justify-center gap-2 text-lg font-bold transition-all shadow-md active:scale-95 ${
            isMuted
              ? 'bg-amber-600 text-white'
              : 'bg-neutral-800 text-white border border-neutral-700'
          }`}
          style={{ minHeight: '56px' }}
        >
          {isMuted ? <MicOff className="w-6 h-6 text-white" /> : <Mic className="w-6 h-6 text-[#FFC400]" />}
          <span>{isMuted ? 'Unmute' : 'Mute'}</span>
        </button>

        <button
          onClick={handleEnd}
          className="h-16 rounded-2xl bg-red-600 hover:bg-red-700 text-white font-black text-lg flex items-center justify-center gap-2 shadow-lg active:scale-95"
          style={{ minHeight: '56px' }}
        >
          <PhoneOff className="w-6 h-6 text-white" />
          <span>बंद करा (End)</span>
        </button>
      </div>
    </div>
  );
};

/**
 * Top-level LiveKitCallRoom container for the REAL call screen.
 * Directly renders <LiveKitRoom> with <RoomAudioRenderer />.
 * Guarantees exactly one room instance on screen.
 * ZERO custom AudioContext/AnalyserNode.
 */
export const LiveKitCallRoom: React.FC<LiveKitCallRoomProps> = ({
  serverUrl,
  token,
  roomName,
  backendUrl,
  initialScenarioId,
  helperLanguage,
  onEndCall,
  onError,
}) => {
  return (
    <LiveKitRoom
      serverUrl={serverUrl}
      token={token}
      connect={true}
      audio={true}
      video={false}
      onDisconnected={() => {
        console.log('[LiveKit] onDisconnected callback fired');
        onEndCall();
      }}
      onError={(err) => {
        console.error('[LiveKit] onError callback fired:', err);
        onError(err?.message || 'LiveKit room error');
      }}
      className="h-full w-full"
    >
      {/* 2. PLAY THE AGENT'S VOICE: Render <RoomAudioRenderer /> always inside <LiveKitRoom> */}
      <RoomAudioRenderer />

      {/* Inner Screen Content */}
      <LiveCallContent
        roomName={roomName}
        backendUrl={backendUrl}
        initialScenarioId={initialScenarioId}
        helperLanguage={helperLanguage}
        onEndCall={onEndCall}
      />
    </LiveKitRoom>
  );
};
