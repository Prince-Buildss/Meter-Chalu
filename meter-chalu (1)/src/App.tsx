import React, { useState, useEffect } from 'react';
import { ScreenState, ScenarioId } from './types';
import { isOnboardingCompleted } from './services/storage';
import { SplashScreen } from './screens/SplashScreen';
import { OnboardingScreen } from './screens/OnboardingScreen';
import { HomeScreen } from './screens/HomeScreen';
import { ConversationScreen } from './screens/ConversationScreen';
import { SettingsModal } from './screens/SettingsModal';
import { AudioTestScreen } from './screens/AudioTestScreen';

export const App: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<ScreenState>('SPLASH');
  const [activeScenario, setActiveScenario] = useState<ScenarioId | null>(null);
  const [isSettingsOpen, setIsSettingsOpen] = useState<boolean>(false);

  // Check hidden route on load (#audio-test or ?audio-test)
  useEffect(() => {
    if (window.location.hash === '#audio-test' || window.location.search.includes('audio-test')) {
      setCurrentScreen('AUDIO_TEST');
    }
  }, []);

  // Splash Finish Handler
  const handleSplashFinish = () => {
    if (window.location.hash === '#audio-test' || window.location.search.includes('audio-test')) {
      setCurrentScreen('AUDIO_TEST');
    } else if (isOnboardingCompleted()) {
      setCurrentScreen('HOME');
    } else {
      setCurrentScreen('ONBOARDING');
    }
  };

  // Start Talk from Home
  const handleStartTalk = (scenarioId?: ScenarioId) => {
    setActiveScenario(scenarioId || null);
    setCurrentScreen('CONVERSATION');
  };

  // End Talk from Conversation
  const handleExitCall = () => {
    setCurrentScreen('HOME');
    setActiveScenario(null);
  };

  return (
    <div className="h-full w-full bg-[#121212] text-white flex flex-col font-['Yantramanav',sans-serif]">
      {currentScreen === 'SPLASH' && (
        <SplashScreen onFinish={handleSplashFinish} />
      )}

      {currentScreen === 'ONBOARDING' && (
        <OnboardingScreen onCompleted={() => setCurrentScreen('HOME')} />
      )}

      {currentScreen === 'HOME' && (
        <HomeScreen
          onStartTalk={handleStartTalk}
          onOpenSettings={() => setIsSettingsOpen(true)}
        />
      )}

      {currentScreen === 'CONVERSATION' && (
        <ConversationScreen
          initialScenarioId={activeScenario}
          onExitCall={handleExitCall}
        />
      )}

      {currentScreen === 'AUDIO_TEST' && (
        <AudioTestScreen onBack={() => setCurrentScreen('HOME')} />
      )}

      {/* Global Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        onOpenAudioTest={() => {
          setIsSettingsOpen(false);
          setCurrentScreen('AUDIO_TEST');
        }}
      />
    </div>
  );
};

export default App;
