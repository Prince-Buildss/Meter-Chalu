package com.example.data

data class PhraseItem(
    val id: Int,
    val marathi: String,
    val phonetic: String,
    val meaningHindi: String,
    val meaningEnglish: String,
    val category: String
)

object PhrasebookRepository {
    val PHRASES = listOf(
        PhraseItem(1, "नमस्कार भाऊ, कुठे जायचं?", "Namaskar bhau, kuthe jaycha?", "नमस्ते भाई, कहाँ जाना है?", "Hello brother, where do you want to go?", "संवाद (Greetings)"),
        PhraseItem(2, "हो, चला बसा.", "Ho, chala basa.", "हाँ, चलिए बैठिए.", "Yes, please get in.", "संवाद (Greetings)"),
        PhraseItem(3, "मीटरनेच चालणार.", "Meter-nech chalnar.", "मीटर से ही चलेंगे.", "Will go by meter only.", "भाडे (Fare)"),
        PhraseItem(4, "पन्नास रुपये झाले.", "Pannas rupaye jhale.", "पचास रुपये हुए.", "It's fifty rupees.", "भाडे (Fare)"),
        PhraseItem(5, "सुट्टे पैसे द्या ना.", "Sutte paise dya na.", "छुट्टे पैसे दीजिए ना.", "Please give change.", "भाडे (Fare)"),
        PhraseItem(6, "दहा रुपये सुट्टे नाहीत.", "Daha rupaye sutte nahit.", "दस रुपये छुट्टे नहीं हैं.", "Don't have ten rupees change.", "भाडे (Fare)"),
        PhraseItem(7, "पुढच्या सिग्नलला उजवीकडे वळा.", "Pudhchya signalla ujvikade vala.", "अगले सिग्नल पर दाएं मुड़िए.", "Turn right at the next signal.", "दिशा (Directions)"),
        PhraseItem(8, "डाव्या बाजूला थांबा.", "Davya bajula thamba.", "बाईं तरफ रुकिए.", "Stop on the left side.", "दिशा (Directions)"),
        PhraseItem(9, "सरळ पुढे जा.", "Saral pudhe ja.", "सीधे आगे जाइए.", "Go straight ahead.", "दिशा (Directions)"),
        PhraseItem(10, "इथेच उतराल का?", "Ithech utral ka?", "यहीं उतरेंगे क्या?", "Will you get off here?", "दिशा (Directions)"),
        PhraseItem(11, "ट्रॅफिक खूप आहे.", "Traffic khoop aahe.", "ट्रैफिक बहुत ज्यादा है.", "There is heavy traffic.", "रस्ता (Road)"),
        PhraseItem(12, "पाच मिनिटे थांबा, येतो.", "Paach minute thamba, yeto.", "पाँच मिनट रुकिए, आता हूँ.", "Wait five minutes, I'm coming.", "संवाद (Greetings)"),
        PhraseItem(13, "सामान मागे डिकीत ठेवा.", "Saman mage dickyt theva.", "सामान पीछे डिक्की में रखिए.", "Put luggage in the back trunk.", "मदत (Assistance)"),
        PhraseItem(14, "काळजीपूर्वक उतरा काका.", "Kaljipurvak utra kaka.", "संभलकर उतरिए काका.", "Please alight carefully uncle.", "मदत (Assistance)"),
        PhraseItem(15, "माझं नाव रमेश आहे.", "Majha naav Ramesh aahe.", "मेरा नाम रमेश है.", "My name is Ramesh.", "आरटीओ (RTO)"),
        PhraseItem(16, "माझ्याकडे ड्रायव्हिंग लायसन्स आहे.", "Majhyakade driving license aahe.", "मेरे पास ड्राइविंग लाइसेंस है.", "I have a driving license.", "आरटीओ (RTO)"),
        PhraseItem(17, "गाडीचे कागदपत्रे पूर्ण आहेत.", "Gadiche kagadpatre purna aahet.", "गाड़ी के कागजात पूरे हैं.", "Vehicle papers are complete.", "आरटीओ (RTO)"),
        PhraseItem(18, "ऑनलाइन पेमेंट चालेल (क्यूआर कोड).", "Online payment chalel (QR code).", "ऑनलाइन पेमेंट चलेगा (क्यूआर कोड).", "Online payment works (QR).", "भाडे (Fare)"),
        PhraseItem(19, "काही हरकत नाही भाऊ.", "Kahi harkat nahi bhau.", "कोई बात नहीं भाई.", "No problem brother.", "संवाद (Greetings)"),
        PhraseItem(20, "खूप धन्यवाद, पुन्हा या!", "Khoop dhanyavad, punha ya!", "बहुत धन्यवाद, फिर आइएगा!", "Thank you very much, come again!", "संवाद (Greetings)")
    )
}
