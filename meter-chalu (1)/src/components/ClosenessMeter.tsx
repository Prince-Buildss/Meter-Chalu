import React from 'react';
import { TargetSentence, ClosenessData } from '../types';
import { CheckCircle2 } from 'lucide-react';

interface ClosenessMeterProps {
  targetSentence?: TargetSentence | null;
  closenessData?: ClosenessData | null;
}

export const ClosenessMeter: React.FC<ClosenessMeterProps> = ({
  targetSentence,
  closenessData,
}) => {
  if (!targetSentence && !closenessData) return null;

  return (
    <div className="w-full bg-[#1c1c1c] border border-neutral-800 rounded-2xl p-3.5 mb-2 shadow-lg">
      {/* Pinned Target Sentence (28sp bold Marathi) */}
      {targetSentence && (
        <div className="text-center mb-2.5">
          <div className="text-xs font-semibold text-[#FFC400] uppercase tracking-wider mb-1">
            लक्ष्य वाक्य (Target Sentence)
          </div>
          <div className="text-[26px] leading-tight font-extrabold text-white">
            {targetSentence.marathi}
          </div>
          {targetSentence.meaning && (
            <div className="text-sm text-neutral-400 mt-1 font-medium">
              "{targetSentence.meaning}"
            </div>
          )}
        </div>
      )}

      {/* Closeness Feedback */}
      {closenessData && (
        <div className="space-y-2.5 pt-2 border-t border-neutral-800/80">
          {/* Individual Word States */}
          {closenessData.words && closenessData.words.length > 0 && (
            <div className="flex flex-wrap items-center justify-center gap-1.5 py-1">
              {closenessData.words.map((w, idx) => {
                const state = w.state;
                return (
                  <span
                    key={idx}
                    className={`px-2.5 py-1 rounded-lg text-sm font-bold transition-all ${
                      state === 'great'
                        ? 'bg-emerald-600/30 text-emerald-400 border border-emerald-500/60 shadow-sm'
                        : state === 'almost'
                        ? 'bg-amber-500/30 text-amber-300 border border-amber-400/60'
                        : 'bg-neutral-900/60 text-neutral-400 border border-neutral-700/80'
                    }`}
                  >
                    {w.w}
                  </span>
                );
              })}
            </div>
          )}

          {/* Mastered Badge or Score bar */}
          <div className="flex items-center justify-between gap-3 px-1">
            <div className="flex-1">
              <div className="flex justify-between text-xs text-neutral-400 mb-1 font-semibold">
                <span>सराव गुण (Closeness)</span>
                <span className="text-[#FFC400] font-bold">{closenessData.score}%</span>
              </div>
              <div className="w-full h-2.5 bg-neutral-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-amber-500 to-[#FFC400] rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(100, Math.max(0, closenessData.score))}%` }}
                />
              </div>
            </div>

            {closenessData.mastered ? (
              <div className="flex items-center gap-1.5 px-3 py-1 bg-emerald-600 text-white rounded-full font-bold text-sm shadow-md animate-bounce">
                <CheckCircle2 className="w-4 h-4 text-white" />
                <span>शाबास!</span>
              </div>
            ) : closenessData.score >= 70 ? (
              <div className="px-2.5 py-1 bg-amber-500/20 text-amber-300 border border-amber-400/40 rounded-full font-semibold text-xs">
                खूप छान!
              </div>
            ) : (
              <div className="px-2.5 py-1 bg-neutral-800 text-neutral-400 rounded-full text-xs">
                थोडा प्रयत्न करा
              </div>
            )}
          </div>

          {/* Heard Transcription line */}
          {closenessData.heard && (
            <div className="text-center text-xs text-neutral-400 bg-neutral-900/80 py-1 px-2 rounded-lg font-mono truncate">
              मैंने सुना: <span className="text-neutral-200 font-semibold">{closenessData.heard}</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
