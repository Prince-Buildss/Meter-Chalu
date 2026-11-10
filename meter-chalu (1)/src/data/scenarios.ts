import { Scenario, ScenarioId } from '../types';

export const SCENARIOS: Scenario[] = [
  {
    id: 'dadar',
    titleMarathi: 'दादर स्टेशन',
    titleHindi: 'दादर स्टेशन',
    icon: '🚉',
    descriptionHindi: 'स्टेशन जाने वाले यात्री और मीटर पर चर्चा',
    firstLineMarathi: 'दादा, दादर स्टेशनला जाणार का?',
    firstLineHindi: 'भैया, दादर स्टेशन जाओगे क्या?'
  },
  {
    id: 'fare',
    titleMarathi: 'भाडे आणि सुट्टे',
    titleHindi: 'किराया और छुट्टे',
    icon: '💵',
    descriptionHindi: 'मीटर भाड़ा, सुट्टे पैसे और ऑनलाइन पेमेंट',
    firstLineMarathi: 'भाडं किती झालं? सुट्टे पैसे आहेत का?',
    firstLineHindi: 'किराया कितना हुआ? छुट्टे पैसे हैं क्या?'
  },
  {
    id: 'directions',
    titleMarathi: 'रस्ता विचारणे',
    titleHindi: 'रास्ता और मोड़',
    icon: '🧭',
    descriptionHindi: 'डावीकडे, उजवीकडे, पुढे जा - दिशा निर्देश',
    firstLineMarathi: 'इथून डावीकडे वळा, मग पुढच्या सिग्नलला उजवीकडे.',
    firstLineHindi: 'यहाँ से बाएं मुड़ो, फिर अगले सिग्नल पर दाएं।'
  },
  {
    id: 'elderly',
    titleMarathi: 'ज्येष्ठ नागरिक',
    titleHindi: 'बुजुर्ग सवारी',
    icon: '👴',
    descriptionHindi: 'हळू चालवा, आधार देणे आणि आदरयुक्त संभाषण',
    firstLineMarathi: 'बाळा, गाडी जरा हळू चालव ना, पाठीत दुखतंय.',
    firstLineHindi: 'बेटा, गाड़ी ज़रा धीरे चलाओ, पीठ में दर्द है।'
  },
  {
    id: 'rto',
    titleMarathi: 'RTO तपासणी',
    titleHindi: 'RTO चेकिंग',
    icon: '👮',
    descriptionHindi: 'लायसन्स, बॅज, परमिट आणि तपासणी उत्तरे',
    firstLineMarathi: 'चालक साहेब, गाडी बाजूला घ्या. लायसन्स आणि बॅज दाखवा.',
    firstLineHindi: 'ड्राइवर साहब, गाड़ी किनारे लो। लाइसेंस और बैज दिखाओ।'
  }
];

export function getScenarioById(id: ScenarioId): Scenario | undefined {
  return SCENARIOS.find(s => s.id === id);
}
