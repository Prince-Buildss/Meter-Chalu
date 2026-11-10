import { PhraseItem } from '../types';

export const ESSENTIAL_PHRASES: PhraseItem[] = [
  // 1. Greeting & Acceptance
  {
    id: 1,
    category: 'सुरुवात (Greetings)',
    marathi: 'नमस्कार साहेब, कुठे जायचं?',
    phoneticHindi: 'नमस्कार साहेब, कुठे जायचं?',
    meaningHindi: 'नमस्ते साहब, कहाँ जाना है?',
    meaningEnglish: 'Hello sir, where do you want to go?'
  },
  {
    id: 2,
    category: 'सुरुवात (Greetings)',
    marathi: 'बसा साहेब, चालू करूया.',
    phoneticHindi: 'बसा साहेब, चालू करूया.',
    meaningHindi: 'बैठिए साहब, शुरू करते हैं।',
    meaningEnglish: 'Please sit sir, let us start.'
  },
  {
    id: 3,
    category: 'सुरुवात (Greetings)',
    marathi: 'मीटरनेच जाणार, काळजी नको.',
    phoneticHindi: 'मीटरनेच जाणार, काळजी नको.',
    meaningHindi: 'मीटर से ही चलेंगे, चिंता मत करो।',
    meaningEnglish: 'Will go by meter only, do not worry.'
  },
  // 2. Directions & Route
  {
    id: 4,
    category: 'रस्ता (Directions)',
    marathi: 'पुढून डावीकडे वळू का?',
    phoneticHindi: 'पुढून डावीकडे वळू का?',
    meaningHindi: 'आगे से बाएं मुडूँ क्या?',
    meaningEnglish: 'Should I turn left ahead?'
  },
  {
    id: 5,
    category: 'रस्ता (Directions)',
    marathi: 'सिग्नलला उजवीकडे जायचं आहे.',
    phoneticHindi: 'सिग्नलला उजवीकडे जायचं आहे.',
    meaningHindi: 'सिग्नल पर दाएं जाना है।',
    meaningEnglish: 'Have to go right at the traffic signal.'
  },
  {
    id: 6,
    category: 'रस्ता (Directions)',
    marathi: 'रस्त्यावर खूप ट्रॅफिक आहे.',
    phoneticHindi: 'रस्त्यावर खूप ट्रॅफिक आहे.',
    meaningHindi: 'रास्ते पर बहुत ट्रैफिक है।',
    meaningEnglish: 'There is heavy traffic on the road.'
  },
  {
    id: 7,
    category: 'रस्ता (Directions)',
    marathi: 'शॉर्टकटने जाऊया का साहेब?',
    phoneticHindi: 'शॉर्टकटने जाऊया का साहेब?',
    meaningHindi: 'शॉर्टकट से चलें क्या साहब?',
    meaningEnglish: 'Shall we take a shortcut sir?'
  },
  {
    id: 8,
    category: 'रस्ता (Directions)',
    marathi: 'इथेच थांबवू का?',
    phoneticHindi: 'इथेच थांबवू का?',
    meaningHindi: 'यहीं रोक दूँ क्या?',
    meaningEnglish: 'Shall I stop right here?'
  },
  // 3. Fares & Payment
  {
    id: 9,
    category: 'भाडे (Fare & Payment)',
    marathi: 'मीटरप्रमाणे पन्नास रुपये झाले.',
    phoneticHindi: 'मीटरप्रमाणे पन्नास रुपये झाले.',
    meaningHindi: 'मीटर के हिसाब से पचास रुपये हुए।',
    meaningEnglish: 'Fifty rupees as per meter.'
  },
  {
    id: 10,
    category: 'भाडे (Fare & Payment)',
    marathi: 'सुट्टे पैसे आहेत का तुमच्याकडे?',
    phoneticHindi: 'सुट्टे पैसे आहेत का तुमच्याकडे?',
    meaningHindi: 'छुट्टे पैसे हैं क्या आपके पास?',
    meaningEnglish: 'Do you have change with you?'
  },
  {
    id: 11,
    category: 'भाडे (Fare & Payment)',
    marathi: 'ऑनलाईन किंवा QR कोड चालेल.',
    phoneticHindi: 'ऑनलाईन किंवा क्यूआर कोड चालेल.',
    meaningHindi: 'ऑनलाइन या QR कोड चलेगा।',
    meaningEnglish: 'Online or QR code will work.'
  },
  {
    id: 12,
    category: 'भाडे (Fare & Payment)',
    marathi: 'हे घ्या तुमचे उरलेले पैसे.',
    phoneticHindi: 'हे घ्या तुमचे उरलेले पैसे.',
    meaningHindi: 'यह लीजिए आपके बाकी बचे पैसे।',
    meaningEnglish: 'Here is your balance change.'
  },
  // 4. Passenger Care & Courtesy
  {
    id: 13,
    category: 'काळजी (Care & Courtesy)',
    marathi: 'हळूच चालवतो, काळजी करू नका.',
    phoneticHindi: 'हळूच चालवतो, काळजी करू नका.',
    meaningHindi: 'धीरे ही चलाता हूँ, चिंता मत कीजिए।',
    meaningEnglish: 'Driving slowly, please do not worry.'
  },
  {
    id: 14,
    category: 'काळजी (Care & Courtesy)',
    marathi: 'सामान डिक्कीत ठेवू का?',
    phoneticHindi: 'सामान डिक्कीत ठेवू का?',
    meaningHindi: 'सामान डिक्की में रख दूँ क्या?',
    meaningEnglish: 'Shall I put the luggage in the boot?'
  },
  {
    id: 15,
    category: 'काळजी (Care & Courtesy)',
    marathi: 'काही विसरला नाहीत ना साहेब?',
    phoneticHindi: 'काही विसरला नाहीत ना साहेब?',
    meaningHindi: 'कुछ भूले तो नहीं न साहब?',
    meaningEnglish: 'Did you forget anything sir?'
  },
  {
    id: 16,
    category: 'काळजी (Care & Courtesy)',
    marathi: 'धन्यवाद, काळजीपूर्वक उतरा.',
    phoneticHindi: 'धन्यवाद, काळजीपूर्वक उतरा.',
    meaningHindi: 'धन्यवाद, संभलकर उतरिए।',
    meaningEnglish: 'Thank you, please alight carefully.'
  },
  // 5. RTO & Documents
  {
    id: 17,
    category: 'RTO (Inspection & Rules)',
    marathi: 'माझं लायसन्स आणि बॅज वैध आहे.',
    phoneticHindi: 'माझं लायसन्स आणि बॅज वैध आहे.',
    meaningHindi: 'मेरा लाइसेंस और बैज वैध है।',
    meaningEnglish: 'My licence and badge are valid.'
  },
  {
    id: 18,
    category: 'RTO (Inspection & Rules)',
    marathi: 'गाडीचे सर्व कागदपत्रे सोबत आहेत.',
    phoneticHindi: 'गाडीचे सर्व कागदपत्रे सोबत आहेत.',
    meaningHindi: 'गाड़ी के सभी कागज़ात साथ में हैं।',
    meaningEnglish: 'All vehicle documents are with me.'
  },
  {
    id: 19,
    category: 'RTO (Inspection & Rules)',
    marathi: 'मीटरचं सील तपासू शकता साहेब.',
    phoneticHindi: 'मीटरचं सील तपासू शकता साहेब.',
    meaningHindi: 'मीटर की सील चेक कर सकते हैं साहब।',
    meaningEnglish: 'You can inspect the meter seal, sir.'
  },
  {
    id: 20,
    category: 'RTO (Inspection & Rules)',
    marathi: 'मी नियमांचे पूर्ण पालन करतो.',
    phoneticHindi: 'मी नियमांचे पूर्ण पालन करतो.',
    meaningHindi: 'मैं नियमों का पूरा पालन करता हूँ।',
    meaningEnglish: 'I follow all regulations strictly.'
  }
];
