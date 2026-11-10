import React from 'react';

interface ConversationOrbProps {
  state: 'connecting' | 'listening' | 'thinking' | 'speaking';
  audioLevel?: number; // 0.0 to 1.0
  helperLanguage?: string;
}

const STATE_LABELS: Record<string, Record<string, string>> = {
  hi: {
    connecting: 'जोड़ रहा हूँ...',
    listening: 'सुन रहा हूँ',
    thinking: 'सोच रहा हूँ',
    speaking: 'बोल रहा हूँ',
  },
  en: {
    connecting: 'Connecting...',
    listening: 'Listening',
    thinking: 'Thinking',
    speaking: 'Speaking',
  },
  mr: {
    connecting: 'जोडत आहे...',
    listening: 'ऐकत आहे',
    thinking: 'विचार करत आहे',
    speaking: 'बोलत आहे',
  },
  gu: {
    connecting: 'જોડાઈ રહ્યું છે...',
    listening: 'સાંભળી રહ્યો છું',
    thinking: 'વિચારી રહ્યો છું',
    speaking: 'બોલી રહ્યો છું',
  },
};

export const ConversationOrb: React.FC<ConversationOrbProps> = ({
  state,
  audioLevel = 0,
  helperLanguage = 'hi',
}) => {
  const langLabels = STATE_LABELS[helperLanguage] || STATE_LABELS['hi'];
  const stateText = langLabels[state] || state;

  // Dynamic scale based on audio activity
  const clampedLevel = Math.min(1, Math.max(0, audioLevel));
  const dynamicScale = 1 + clampedLevel * 0.35;

  return (
    <div className="flex flex-col items-center justify-center my-3 select-none">
      <div className="relative w-44 h-44 flex items-center justify-center">
        {/* Outer Glow Pulse */}
        <div
          className={`absolute inset-0 rounded-full transition-all duration-300 ${
            state === 'listening'
              ? 'bg-[#FFC400]/20 animate-ping'
              : state === 'speaking'
              ? 'bg-[#FFC400]/30 animate-pulse'
              : state === 'thinking'
              ? 'bg-amber-500/20'
              : 'bg-neutral-800/20'
          }`}
          style={{ transform: `scale(${dynamicScale})` }}
        />

        {/* Mid Ring Wave */}
        <div
          className={`absolute inset-3 rounded-full border-2 transition-all duration-500 ${
            state === 'listening'
              ? 'border-[#FFC400]/50 animate-pulse'
              : state === 'speaking'
              ? 'border-[#FFC400] scale-105'
              : state === 'thinking'
              ? 'border-amber-400/40 border-dashed animate-spin'
              : 'border-neutral-700/50'
          }`}
          style={{ animationDuration: state === 'thinking' ? '4s' : '1.5s' }}
        />

        {/* Main Solid Center Core */}
        <div
          className={`relative w-28 h-28 rounded-full shadow-2xl flex items-center justify-center transition-all duration-300 ${
            state === 'listening'
              ? 'bg-gradient-to-tr from-[#E6AF00] to-[#FFE066] shadow-[#FFC400]/50'
              : state === 'speaking'
              ? 'bg-gradient-to-br from-[#FFC400] via-[#FFD700] to-[#FFA000] shadow-[#FFC400]/80'
              : state === 'thinking'
              ? 'bg-gradient-to-tr from-amber-600 to-yellow-500 shadow-amber-500/40'
              : 'bg-neutral-700 shadow-neutral-900'
          }`}
          style={{ transform: `scale(${state === 'speaking' ? dynamicScale : 1})` }}
        >
          {/* Inner Visual Waveform or Indicator */}
          {state === 'speaking' ? (
            <div className="flex items-center gap-1.5 h-10">
              <span className="w-1.5 bg-black rounded-full animate-bounce [animation-delay:-0.3s]" style={{ height: `${20 + clampedLevel * 25}px` }} />
              <span className="w-1.5 bg-black rounded-full animate-bounce [animation-delay:-0.15s]" style={{ height: `${28 + clampedLevel * 30}px` }} />
              <span className="w-1.5 bg-black rounded-full animate-bounce [animation-delay:0s]" style={{ height: `${18 + clampedLevel * 20}px` }} />
              <span className="w-1.5 bg-black rounded-full animate-bounce [animation-delay:-0.2s]" style={{ height: `${24 + clampedLevel * 28}px` }} />
            </div>
          ) : state === 'listening' ? (
            <div className="w-7 h-7 rounded-full bg-black/80 flex items-center justify-center animate-pulse">
              <div className="w-2.5 h-2.5 rounded-full bg-[#FFC400]" />
            </div>
          ) : state === 'thinking' ? (
            <div className="w-6 h-6 border-3 border-black border-t-transparent rounded-full animate-spin" />
          ) : (
            <div className="w-3 h-3 rounded-full bg-neutral-400 animate-pulse" />
          )}
        </div>
      </div>

      {/* Single Word State Label in Helper Language */}
      <div className="mt-2 text-center">
        <span
          className={`px-4 py-1 rounded-full text-sm font-bold tracking-wide uppercase shadow-sm ${
            state === 'listening'
              ? 'bg-[#FFC400] text-black'
              : state === 'speaking'
              ? 'bg-yellow-400 text-black animate-pulse'
              : state === 'thinking'
              ? 'bg-amber-900/60 text-amber-300 border border-amber-600/40'
              : 'bg-neutral-800 text-neutral-400'
          }`}
        >
          {stateText}
        </span>
      </div>
    </div>
  );
};
