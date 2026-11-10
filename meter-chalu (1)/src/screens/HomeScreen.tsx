import React, { useState, useEffect } from 'react';
import {
  getDriverName,
  getOverallReadiness,
  getCachedReport,
  setCachedReport,
  getBackendUrl,
  getStableRoomName,
  getIsMockMode,
} from '../services/storage';
import { HomeReport, ScenarioId } from '../types';
import { SCENARIOS } from '../data/scenarios';
import { Settings, Sparkles, TrendingUp, TrendingDown, Minus, CheckCircle, Award, Flame, Calendar } from 'lucide-react';

interface HomeScreenProps {
  onStartTalk: (scenarioId?: ScenarioId) => void;
  onOpenSettings: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  onStartTalk,
  onOpenSettings,
}) => {
  const driverName = getDriverName();
  const [readiness, setReadiness] = useState<number>(getOverallReadiness());
  const [report, setReport] = useState<HomeReport | null>(getCachedReport());
  const [selectedScenario, setSelectedScenario] = useState<ScenarioId | null>(null);

  // Fetch report on load if backend URL is available
  useEffect(() => {
    const isMock = getIsMockMode();
    const base = getBackendUrl();
    const room = getStableRoomName();

    if (!isMock && base) {
      fetch(`${base}/report?room=${room}`)
        .then((res) => {
          if (res.ok) return res.json();
          throw new Error(`HTTP ${res.status}`);
        })
        .then((data: HomeReport) => {
          setCachedReport(data);
          setReport(data);
          if (typeof data.percent === 'number') {
            setReadiness(data.percent);
          }
        })
        .catch((err) => {
          console.warn('Could not fetch home report from backend', err);
        });
    }
  }, []);

  // Use fallback values if report is empty
  const daysSinceLast = report?.days_since_last ?? 0;
  const streakDays = report?.streak_days ?? 3;
  const totalSessions = report?.total_sessions ?? 12;
  const closenessAvg = report?.closeness_avg ?? readiness;
  const trend = report?.trend ?? 'up';
  const fixItems = report?.fix_items ?? [
    {
      phrase: 'इथून उजवीकडे वळा',
      meaning: 'यहाँ से दायें मुड़िए',
      tip: 'उच्चारात स्पष्टता ठेवा',
      count: 2,
    },
  ];
  const fixedCount = report?.fixed_count ?? 4;
  const strongScenarios = report?.strong_scenarios ?? ['दादर स्टेशन', 'भाडे आणि सुट्टे'];

  return (
    <div className="flex flex-col h-full bg-[#121212] text-white overflow-y-auto">
      {/* Top Bar: Driver Greeting & Settings */}
      <div className="px-5 pt-6 pb-2 flex items-center justify-between border-b border-neutral-900">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            नमस्ते {driverName}!
          </h1>
          {/* Small readiness percent line under greeting (never as a big gauge here) */}
          <div className="text-xs text-neutral-400 font-semibold flex items-center gap-1.5 mt-0.5">
            <span>सराव तयारी:</span>
            <span className="text-[#FFC400] font-bold">{readiness}% सराव गुण</span>
            <span className="text-[10px] text-neutral-500">• (Practice score)</span>
          </div>
        </div>

        <button
          onClick={onOpenSettings}
          className="w-11 h-11 rounded-2xl bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-300 hover:text-white hover:border-[#FFC400] transition-all shadow-md active:scale-95"
          title="सेटिंग्ज (Settings)"
        >
          <Settings className="w-5 h-5 text-[#FFC400]" />
        </button>
      </div>

      <div className="p-5 flex flex-col items-center flex-1 max-w-md mx-auto w-full space-y-6">
        {/* Welcome Back Banner if days_since_last >= 2 */}
        {daysSinceLast >= 2 && (
          <div className="w-full bg-gradient-to-r from-amber-500/20 to-yellow-500/10 border border-[#FFC400]/40 rounded-2xl p-3.5 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#FFC400] text-black font-black flex items-center justify-center text-lg shadow-md">
              👋
            </div>
            <div>
              <div className="text-sm font-extrabold text-[#FFC400]">आपकी याद आई!</div>
              <div className="text-xs text-neutral-300">
                {daysSinceLast} दिवस झाले सराव केला नाही. ५ मिनिटे बोलूया का?
              </div>
            </div>
          </div>
        )}

        {/* 2. HOME: ONE HUGE ROUND BUTTON "बात करें" (Talk) */}
        <div className="flex flex-col items-center justify-center pt-2">
          <button
            onClick={() => onStartTalk(selectedScenario || undefined)}
            className="w-48 h-48 rounded-full bg-gradient-to-tr from-[#E6AF00] via-[#FFC400] to-[#FFE066] text-black font-black text-3xl shadow-[0_12px_40px_rgba(255,196,0,0.35)] flex flex-col items-center justify-center gap-1.5 active:scale-95 hover:scale-105 transition-all select-none border-4 border-[#121212]"
            style={{ minHeight: '56px', minWidth: '56px' }}
          >
            <span className="text-4xl">🎙️</span>
            <span className="tracking-wide">बात करें</span>
            <span className="text-xs font-bold text-black/70 tracking-normal">(सराव सुरू करा)</span>
          </button>
        </div>

        {/* Optional Shortcut Chips */}
        <div className="w-full">
          <div className="text-xs font-bold text-neutral-400 mb-2 px-1 uppercase tracking-wider">
            किंवा थेट विषयावर बोला (Topic Shortcut):
          </div>
          <div className="flex gap-2 overflow-x-auto pb-1 no-scrollbar">
            {SCENARIOS.map((sc) => {
              const isSelected = selectedScenario === sc.id;
              return (
                <button
                  key={sc.id}
                  onClick={() => {
                    setSelectedScenario(isSelected ? null : sc.id);
                  }}
                  className={`px-3.5 py-2 rounded-2xl text-xs font-bold whitespace-nowrap transition-all flex items-center gap-1.5 ${
                    isSelected
                      ? 'bg-[#FFC400] text-black shadow-lg scale-105'
                      : 'bg-neutral-900 border border-neutral-800 text-neutral-300 hover:border-neutral-700'
                  }`}
                >
                  <span>{sc.icon}</span>
                  <span>{sc.titleMarathi}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Full Scrollable Progress Report */}
        <div className="w-full space-y-4 pt-2 border-t border-neutral-800/80">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-extrabold text-white flex items-center gap-2">
              <Award className="w-4 h-4 text-[#FFC400]" />
              <span>प्रगती अहवाल (Progress Report)</span>
            </h2>
            <span className="text-xs text-neutral-400">रोजचा सराव</span>
          </div>

          {/* "आज तक" Stats Grid */}
          <div className="grid grid-cols-3 gap-2.5">
            <div className="bg-neutral-900/80 border border-neutral-800 rounded-2xl p-3 text-center">
              <div className="text-xs text-neutral-400 flex items-center justify-center gap-1">
                <Calendar className="w-3 h-3 text-[#FFC400]" />
                <span>सत्र</span>
              </div>
              <div className="text-xl font-black text-white mt-0.5">{totalSessions}</div>
            </div>

            <div className="bg-neutral-900/80 border border-neutral-800 rounded-2xl p-3 text-center">
              <div className="text-xs text-neutral-400 flex items-center justify-center gap-1">
                <Flame className="w-3 h-3 text-amber-500" />
                <span>सातत्य</span>
              </div>
              <div className="text-xl font-black text-[#FFC400] mt-0.5">{streakDays} दिवस</div>
            </div>

            <div className="bg-neutral-900/80 border border-neutral-800 rounded-2xl p-3 text-center">
              <div className="text-xs text-neutral-400 flex items-center justify-center gap-1">
                {trend === 'up' ? (
                  <TrendingUp className="w-3 h-3 text-emerald-400" />
                ) : trend === 'down' ? (
                  <TrendingDown className="w-3 h-3 text-red-400" />
                ) : (
                  <Minus className="w-3 h-3 text-neutral-400" />
                )}
                <span>सरासरी</span>
              </div>
              <div className="text-xl font-black text-white mt-0.5">{closenessAvg}%</div>
            </div>
          </div>

          {/* "इन्हें ठीक करो" Cards */}
          {fixItems.length > 0 && (
            <div className="bg-neutral-900/70 border border-neutral-800 rounded-2xl p-4">
              <div className="text-xs font-bold text-amber-400 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                <span>इन्हें ठीक करो (Phrases to Polish)</span>
              </div>
              <div className="space-y-2.5">
                {fixItems.map((item, idx) => (
                  <div key={idx} className="bg-neutral-950 p-3 rounded-xl border border-neutral-800">
                    <div className="text-base font-extrabold text-white">{item.phrase}</div>
                    <div className="text-xs text-neutral-400 mt-0.5">{item.meaning}</div>
                    <div className="text-[11px] text-[#FFC400] font-semibold mt-1">
                      💡 टिप: {item.tip}
                    </div>
                  </div>
                ))}
              </div>

              {/* "ठीक हो गया" line with fixed count */}
              <div className="mt-3 pt-2 border-t border-neutral-800/80 flex items-center justify-between text-xs text-emerald-400 font-bold">
                <div className="flex items-center gap-1.5">
                  <CheckCircle className="w-4 h-4 text-emerald-400" />
                  <span>ठीक हो गया: {fixedCount} वाक्य आत्मसात केले!</span>
                </div>
              </div>
            </div>
          )}

          {/* "मज़बूत" Green Scenario Chips */}
          {strongScenarios.length > 0 && (
            <div className="bg-neutral-900/70 border border-neutral-800 rounded-2xl p-4">
              <div className="text-xs font-bold text-neutral-400 uppercase tracking-wider mb-2">
                मजबूत प्रसंग (Mastered Scenarios):
              </div>
              <div className="flex flex-wrap gap-2">
                {strongScenarios.map((sc, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1 bg-emerald-950/60 text-emerald-400 border border-emerald-800/80 rounded-full text-xs font-bold flex items-center gap-1.5"
                  >
                    <span>✓</span>
                    <span>{sc}</span>
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Disclaimer Note */}
          <div className="text-[11px] text-neutral-500 text-center pb-6">
            हे अधिकृत प्रमाणपत्र नसून दैनंदिन सराव गुण (practice score) आहे.
          </div>
        </div>
      </div>
    </div>
  );
};
