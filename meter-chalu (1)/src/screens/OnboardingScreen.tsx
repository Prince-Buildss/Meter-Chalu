import React, { useState } from 'react';
import { setDriverName, setHelperLanguage, setOnboardingCompleted } from '../services/storage';

interface OnboardingScreenProps {
  onCompleted: () => void;
}

const LANGUAGES = [
  { code: 'hi', name: 'हिंदी (Hindi)', icon: '🇮🇳' },
  { code: 'en', name: 'English', icon: '🌐' },
  { code: 'gu', name: 'ગુજરાતી (Gujarati)', icon: '🔷' },
  { code: 'bn', name: 'বাংলা (Bengali)', icon: '🟢' },
  { code: 'te', name: 'తెలుగు (Telugu)', icon: '🟡' },
  { code: 'ta', name: 'தமிழ் (Tamil)', icon: '🔴' },
  { code: 'kn', name: 'ಕನ್ನಡ (Kannada)', icon: '🟣' },
  { code: 'pa', name: 'ਪੰਜਾਬੀ (Punjabi)', icon: '🟠' },
  { code: 'ml', name: 'മലയാളം (Malayalam)', icon: '🟤' },
];

export const OnboardingScreen: React.FC<OnboardingScreenProps> = ({ onCompleted }) => {
  const [selectedLang, setSelectedLang] = useState<string>('hi');
  const [name, setName] = useState<string>('रमेश भाई');
  const [step, setStep] = useState<number>(1);

  const handleFinish = () => {
    setHelperLanguage(selectedLang);
    setDriverName(name.trim() || 'रमेश भाई');
    setOnboardingCompleted();
    onCompleted();
  };

  return (
    <div className="flex flex-col h-full bg-[#121212] text-white p-6 justify-between select-none max-w-md mx-auto w-full">
      <div className="pt-8">
        <div className="flex items-center gap-2 mb-2">
          <div className="w-8 h-8 rounded-xl bg-[#FFC400] text-black font-black flex items-center justify-center text-sm shadow-md">
            MC
          </div>
          <span className="text-sm font-bold text-[#FFC400]">साथी सेटअप (Setup)</span>
        </div>

        {step === 1 ? (
          <div>
            <h2 className="text-2xl font-black text-white mb-2">
              तुमची सोयीची भाषा निवडा
            </h2>
            <p className="text-sm text-neutral-400 mb-6">
              Select your helper language. Saathi will explain in this language.
            </p>

            <div className="grid grid-cols-2 gap-3 max-h-[50vh] overflow-y-auto pr-1">
              {LANGUAGES.map((l) => {
                const isSelected = selectedLang === l.code;
                return (
                  <button
                    key={l.code}
                    onClick={() => setSelectedLang(l.code)}
                    className={`p-3.5 rounded-2xl border text-left flex items-center gap-2.5 transition-all ${
                      isSelected
                        ? 'bg-[#FFC400] text-black border-[#FFC400] font-black shadow-lg scale-102'
                        : 'bg-neutral-900 border-neutral-800 text-white font-bold hover:border-neutral-700'
                    }`}
                  >
                    <span className="text-xl">{l.icon}</span>
                    <span className="text-sm">{l.name}</span>
                  </button>
                );
              })}
            </div>
          </div>
        ) : (
          <div>
            <h2 className="text-2xl font-black text-white mb-2">
              तुमचे नाव काय आहे?
            </h2>
            <p className="text-sm text-neutral-400 mb-6">
              What is your name? Saathi will address you with this name.
            </p>

            <div className="bg-neutral-900 p-4 rounded-2xl border border-neutral-800">
              <label className="block text-xs font-bold text-neutral-400 mb-2 uppercase">
                चालकाचे नाव (Driver Name)
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full bg-neutral-950 border border-neutral-700 rounded-xl px-4 py-3 text-lg font-black text-white outline-none focus:border-[#FFC400]"
                placeholder="रमेश भाई"
                autoFocus
              />
            </div>
          </div>
        )}
      </div>

      <div className="pb-6">
        {step === 1 ? (
          <button
            onClick={() => setStep(2)}
            className="w-full h-14 rounded-2xl bg-[#FFC400] text-black font-black text-lg shadow-xl active:scale-95 transition-all flex items-center justify-center"
          >
            पुढे जा (Next)
          </button>
        ) : (
          <button
            onClick={handleFinish}
            className="w-full h-14 rounded-2xl bg-[#FFC400] text-black font-black text-lg shadow-xl active:scale-95 transition-all flex items-center justify-center"
          >
            सुरू करा (Get Started)
          </button>
        )}
      </div>
    </div>
  );
};
