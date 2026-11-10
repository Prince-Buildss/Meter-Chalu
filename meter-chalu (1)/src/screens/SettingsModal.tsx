import React, { useState } from 'react';
import {
  getBackendUrl,
  setBackendUrl,
  getIsMockMode,
  setIsMockMode,
  getDriverName,
  setDriverName,
  getHelperLanguage,
  setHelperLanguage,
  getStableRoomName,
  clearDriverData,
} from '../services/storage';
import { ESSENTIAL_PHRASES } from '../data/phrases';
import { X, Check, AlertCircle, BookOpen, Trash2, CheckCircle2, Volume2, Activity } from 'lucide-react';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDataCleared?: () => void;
  onOpenAudioTest?: () => void;
}

const LANGUAGES = [
  { code: 'hi', name: 'हिंदी (Hindi)' },
  { code: 'en', name: 'English' },
  { code: 'mr', name: 'मराठी (Marathi)' },
  { code: 'gu', name: 'ગુજરાતી (Gujarati)' },
  { code: 'bn', name: 'বাংলা (Bengali)' },
  { code: 'te', name: 'తెలుగు (Telugu)' },
  { code: 'ta', name: 'தமிழ் (Tamil)' },
  { code: 'kn', name: 'ಕನ್ನಡ (Kannada)' },
  { code: 'pa', name: 'ਪੰਜਾਬੀ (Punjabi)' },
  { code: 'ml', name: 'മലയാളം (Malayalam)' },
];

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  onClose,
  onDataCleared,
  onOpenAudioTest,
}) => {
  if (!isOpen) return null;

  const [url, setUrl] = useState<string>(getBackendUrl());
  const [mock, setMock] = useState<boolean>(getIsMockMode());
  const [name, setName] = useState<string>(getDriverName());
  const [lang, setLang] = useState<string>(getHelperLanguage());

  const [testResult, setTestResult] = useState<{ ok: boolean; message: string } | null>(null);
  const [isTesting, setIsTesting] = useState<boolean>(false);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);
  const [showPhrasebook, setShowPhrasebook] = useState<boolean>(false);
  const [phraseSearch, setPhraseSearch] = useState<string>('');

  const roomName = getStableRoomName();

  const handleSave = () => {
    setBackendUrl(url);
    setIsMockMode(mock);
    setDriverName(name);
    setHelperLanguage(lang);
    onClose();
  };

  // 1. Test connection with GET {BASE}/health
  const handleTestConnection = async () => {
    const cleanUrl = url.trim().replace(/\/+$/, '');
    if (!cleanUrl) {
      setTestResult({ ok: false, message: 'URL रिकामी आहे (URL is empty)' });
      return;
    }
    if (!cleanUrl.startsWith('https://')) {
      setTestResult({ ok: false, message: 'URL must start with https://' });
      return;
    }

    setIsTesting(true);
    setTestResult(null);
    try {
      const resp = await fetch(`${cleanUrl}/health`);
      if (resp.ok) {
        setTestResult({ ok: true, message: `OK (${resp.status}) - सर्व्हर चालू आहे!` });
      } else {
        setTestResult({ ok: false, message: `HTTP ${resp.status}: ${resp.statusText}` });
      }
    } catch (e: any) {
      setTestResult({ ok: false, message: e?.message || 'Connection failed' });
    } finally {
      setIsTesting(false);
    }
  };

  // Delete driver data: DELETE {BASE}/driver?room=<roomName>
  const handleDeleteData = async () => {
    if (!window.confirm('तुमचा सर्व सराव डेटा मिटवायचा आहे का? (Are you sure you want to delete your practice data?)')) {
      return;
    }

    setIsDeleting(true);
    const cleanUrl = url.trim().replace(/\/+$/, '');
    if (!mock && cleanUrl) {
      try {
        await fetch(`${cleanUrl}/driver?room=${roomName}`, { method: 'DELETE' });
      } catch (e) {
        console.warn('Backend delete failed', e);
      }
    }
    clearDriverData();
    setIsDeleting(false);
    if (onDataCleared) onDataCleared();
    alert('डेटा यशस्वीरित्या मिटवला गेला. (Data cleared successfully)');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="bg-[#181818] border border-neutral-800 rounded-3xl w-full max-w-lg max-h-[90vh] flex flex-col overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="p-4 border-b border-neutral-800 flex items-center justify-between">
          <h2 className="text-xl font-extrabold text-white flex items-center gap-2">
            <span>⚙️</span>
            <span>सेटिंग्ज (Settings)</span>
          </h2>
          <button
            onClick={onClose}
            className="w-9 h-9 rounded-full bg-neutral-800 flex items-center justify-center text-neutral-300 hover:text-white"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-6">
          {/* Driver Name & Language */}
          <div className="space-y-4 bg-neutral-900/60 p-4 rounded-2xl border border-neutral-800/80">
            <div>
              <label className="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">
                चालकाचे नाव (Driver Name)
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full bg-neutral-950 border border-neutral-700 rounded-xl px-3.5 py-2.5 text-white font-bold focus:border-[#FFC400] outline-none"
                placeholder="रमेश भाई"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">
                मदतीची भाषा (Helper Language)
              </label>
              <select
                value={lang}
                onChange={(e) => setLang(e.target.value)}
                className="w-full bg-neutral-950 border border-neutral-700 rounded-xl px-3.5 py-2.5 text-white font-bold focus:border-[#FFC400] outline-none"
              >
                {LANGUAGES.map((l) => (
                  <option key={l.code} value={l.code}>
                    {l.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* 1. Single Backend URL Field */}
          <div className="bg-neutral-900/60 p-4 rounded-2xl border border-neutral-800/80 space-y-3">
            <div>
              <label className="block text-xs font-bold text-[#FFC400] mb-1.5 uppercase">
                Backend URL (LiveKit & Token Base)
              </label>
              <input
                type="url"
                value={url}
                onChange={(e) => {
                  setUrl(e.target.value);
                  setTestResult(null);
                }}
                className="w-full bg-neutral-950 border border-neutral-700 rounded-xl px-3.5 py-2.5 text-white font-mono text-sm focus:border-[#FFC400] outline-none"
                placeholder="https://your-agent-server.com"
              />
              <p className="text-[11px] text-neutral-400 mt-1">
                Must start with HTTPS. Automatically derives <code className="text-neutral-300">/token</code>, <code className="text-neutral-300">/report</code>, and <code className="text-neutral-300">/driver</code>.
              </p>
            </div>

            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={handleTestConnection}
                disabled={isTesting}
                className="px-4 py-2 bg-neutral-800 hover:bg-neutral-700 border border-neutral-600 rounded-xl text-xs font-bold text-white transition-all"
              >
                {isTesting ? 'तपासत आहे...' : 'Test connection (तपासा)'}
              </button>

              {testResult && (
                <div
                  className={`text-xs font-bold flex items-center gap-1.5 ${
                    testResult.ok ? 'text-emerald-400' : 'text-amber-400'
                  }`}
                >
                  {testResult.ok ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
                  <span>{testResult.message}</span>
                </div>
              )}
            </div>
          </div>

          {/* 1. Mock Mode Runtime Setting */}
          <div className="bg-neutral-900/60 p-4 rounded-2xl border border-neutral-800/80 flex items-center justify-between">
            <div>
              <div className="text-sm font-bold text-white">Mock mode (ऑफलाइन चाचणी)</div>
              <div className="text-xs text-neutral-400">
                Live backend नसताना चाचणी करण्यासाठी. डिफ़ॉल्ट: बंद (OFF)
              </div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={mock}
                onChange={(e) => setMock(e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-12 h-6 bg-neutral-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-neutral-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#FFC400]"></div>
            </label>
          </div>

          {/* Stable Room ID Info */}
          <div className="text-xs font-mono text-neutral-500 bg-neutral-950 p-3 rounded-xl border border-neutral-900">
            <div>Stable Room ID: <span className="text-neutral-300">{roomName}</span></div>
            <div className="text-[10px] text-neutral-600 mt-0.5">Used as driver identity for memory & progress.</div>
          </div>

          {/* TASK 1: MINIMAL DIAGNOSTIC AUDIO TEST BUTTON */}
          <button
            type="button"
            onClick={() => {
              handleSave();
              if (onOpenAudioTest) onOpenAudioTest();
            }}
            className="w-full py-3.5 px-4 rounded-2xl bg-gradient-to-r from-amber-600 to-[#FFC400] text-black font-extrabold text-sm flex items-center justify-between shadow-lg hover:brightness-105 active:scale-98 transition-all"
          >
            <div className="flex items-center gap-2.5">
              <Activity className="w-5 h-5 text-black" />
              <div className="text-left">
                <div className="leading-tight">Audio test (ऑडिओ टेस्ट)</div>
                <div className="text-[11px] font-medium opacity-80">Minimal diagnostic screen for LiveKit sound</div>
              </div>
            </div>
            <span className="text-xs bg-black text-[#FFC400] font-bold px-2.5 py-1 rounded-lg">
              Start Test
            </span>
          </button>

          {/* Phrasebook Accordion Button */}
          <button
            type="button"
            onClick={() => setShowPhrasebook(!showPhrasebook)}
            className="w-full py-3 px-4 bg-neutral-900 border border-neutral-700 rounded-2xl flex items-center justify-between text-sm font-bold text-white hover:border-[#FFC400]"
          >
            <div className="flex items-center gap-2">
              <BookOpen className="w-4 h-4 text-[#FFC400]" />
              <span>महत्त्वाचे २० मराठी संवाद (20 Ride Phrases)</span>
            </div>
            <span className="text-xs text-neutral-400">{showPhrasebook ? 'लपवा' : 'पहा'}</span>
          </button>

          {showPhrasebook && (
            <div className="p-3 bg-neutral-950 rounded-2xl border border-neutral-800 space-y-3">
              <input
                type="text"
                placeholder="वाक्य शोधा (Search phrase)..."
                value={phraseSearch}
                onChange={(e) => setPhraseSearch(e.target.value)}
                className="w-full bg-neutral-900 border border-neutral-700 rounded-xl px-3 py-2 text-xs text-white outline-none"
              />
              <div className="max-h-60 overflow-y-auto space-y-2 pr-1">
                {ESSENTIAL_PHRASES.filter(
                  p => p.marathi.includes(phraseSearch) || p.meaningHindi.includes(phraseSearch)
                ).map((p) => (
                  <div key={p.id} className="p-2.5 rounded-xl bg-neutral-900/90 border border-neutral-800">
                    <div className="text-xs text-[#FFC400] font-semibold">{p.category}</div>
                    <div className="text-base font-bold text-white mt-0.5">{p.marathi}</div>
                    <div className="text-xs text-neutral-400 italic">{p.meaningHindi}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Clear Driver Data Button */}
          <div className="pt-2">
            <button
              type="button"
              onClick={handleDeleteData}
              disabled={isDeleting}
              className="w-full py-3 px-4 rounded-2xl bg-neutral-900 hover:bg-red-950/40 text-red-400 border border-red-900/40 font-bold text-sm flex items-center justify-center gap-2 transition-all"
            >
              <Trash2 className="w-4 h-4" />
              <span>{isDeleting ? 'मिटवत आहे...' : 'माझा डेटा मिटवा (Forget me / Delete data)'}</span>
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-neutral-800 flex justify-end gap-3 bg-neutral-950">
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl bg-neutral-800 text-neutral-300 font-bold text-sm"
          >
            रद्द करा (Cancel)
          </button>
          <button
            onClick={handleSave}
            className="px-6 py-2.5 rounded-xl bg-[#FFC400] text-black font-extrabold text-sm flex items-center gap-1.5 shadow-lg"
          >
            <Check className="w-4 h-4 text-black" />
            <span>जतन करा (Save)</span>
          </button>
        </div>
      </div>
    </div>
  );
};
