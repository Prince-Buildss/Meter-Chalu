export type ScenarioId = 'dadar' | 'fare' | 'directions' | 'elderly' | 'rto';

export interface Scenario {
  id: ScenarioId;
  titleMarathi: string;
  titleHindi: string;
  icon: string;
  descriptionHindi: string;
  firstLineMarathi: string;
  firstLineHindi: string;
}

export interface TargetSentence {
  marathi: string;
  meaning: string;
}

export type WordState = 'great' | 'almost' | 'miss';

export interface ClosenessWord {
  w: string;
  state: WordState;
}

export interface ClosenessData {
  score: number;
  words: ClosenessWord[];
  heard: string;
  mastered: boolean;
}

export interface ReadinessSummary {
  percent: number;
  per_scenario: Record<string, number>;
  ready: boolean;
  note: string;
  scenario?: string;
  score?: number;
  detected_language?: string;
}

export interface FixItem {
  phrase: string;
  meaning: string;
  tip: string;
  count: number;
}

export interface RecentSession {
  date: string;
  scenario: string;
  score: number;
}

export interface HomeReport {
  name: string;
  percent: number;
  days_since_last: number;
  streak_days: number;
  total_sessions: number;
  closeness_avg: number;
  trend: 'up' | 'down' | 'steady';
  fix_items: FixItem[];
  fixed_count: number;
  strong_scenarios: string[];
  carry_forward_phrase?: string;
  recent_sessions: RecentSession[];
}

export interface ConversationMessage {
  id: string;
  text: string;
  isAgent: boolean;
  meaning?: string;
  timestamp: number;
  isSummaryCard?: boolean;
  summaryData?: ReadinessSummary;
}

export interface PhraseItem {
  id: number;
  category: string;
  marathi: string;
  phoneticHindi: string;
  meaningHindi: string;
  meaningEnglish: string;
}

export type ScreenState = 'SPLASH' | 'ONBOARDING' | 'HOME' | 'CONVERSATION' | 'AUDIO_TEST';
