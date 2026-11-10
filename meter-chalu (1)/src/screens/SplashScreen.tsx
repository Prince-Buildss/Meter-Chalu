import React, { useEffect } from 'react';

interface SplashScreenProps {
  onFinish: () => void;
}

export const SplashScreen: React.FC<SplashScreenProps> = ({ onFinish }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onFinish();
    }, 2000);
    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div className="flex flex-col items-center justify-center h-full bg-[#121212] text-white p-6 select-none">
      <div className="w-28 h-28 rounded-3xl bg-gradient-to-tr from-[#E6AF00] to-[#FFE066] flex items-center justify-center shadow-[0_10px_35px_rgba(255,196,0,0.35)] mb-6 animate-pulse">
        <span className="text-5xl">🚖</span>
      </div>

      <h1 className="text-4xl font-black tracking-tight text-white mb-2">
        Meter Chalu
      </h1>

      <div className="text-lg font-extrabold text-[#FFC400] mb-8 tracking-wide">
        "भाषा चालू, मीटर चालू!"
      </div>

      <div className="flex items-center gap-2 text-xs text-neutral-400 font-medium">
        <div className="w-2 h-2 rounded-full bg-[#FFC400] animate-ping" />
        <span>महाराष्ट्र ऑटोरिक्षा व टॅक्सी सराव साथी</span>
      </div>
    </div>
  );
};
