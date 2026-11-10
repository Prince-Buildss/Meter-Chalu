package com.example.data

data class PracticeTurn(
    val passengerMarathi: String,
    val passengerMeaning: String,
    val sampleDriverReply: String,
    val saathiTip: String
)

data class Scenario(
    val id: String,
    val titleMarathi: String,
    val titleEnglish: String,
    val voice: String,
    val openingMarathi: String,
    val openingMeaning: String,
    val persona: String,
    val topic: String,
    val turns: List<PracticeTurn>
)

object ScenariosRepository {
    val ALL: Map<String, Scenario> = mapOf(
        "dadar" to Scenario(
            id = "dadar",
            titleMarathi = "दादर स्टेशनला चला",
            titleEnglish = "Dadar station ride",
            voice = "Zahira",
            openingMarathi = "नमस्कार भाऊ, दादर स्टेशनला चलाल का?",
            openingMeaning = "नमस्ते भाई, दादर स्टेशन चलोगे क्या?",
            persona = "A young office-goer in a hurry who wants to go to Dadar station.",
            topic = "A passenger asking an auto driver to go to Dadar station in Mumbai, in Marathi.",
            turns = listOf(
                PracticeTurn(
                    passengerMarathi = "नमस्कार भाऊ, दादर स्टेशनला चलाल का?",
                    passengerMeaning = "नमस्ते भाई, दादर स्टेशन चलोगे क्या?",
                    sampleDriverReply = "हो, चला बसा.",
                    saathiTip = "शाबास! तुम्ही 'हो, चला बसा' किंवा 'होय, चला' बोलू शकता."
                ),
                PracticeTurn(
                    passengerMarathi = "मीटरने चालणार ना? किती वेळ लागेल?",
                    passengerMeaning = "मीटर से चलोगे ना? कितना समय लगेगा?",
                    sampleDriverReply = "हो, मीटरनेच चालणार. 15 मिनिटे लागतील.",
                    saathiTip = "छान! 'मीटरनेच चालणार' अगदी बरोबर वाक्य आहे."
                ),
                PracticeTurn(
                    passengerMarathi = "दादर स्टेशनच्या पश्चिमेला सोडा.",
                    passengerMeaning = "दादर स्टेशन के पश्चिम की तरफ छोड़िए.",
                    sampleDriverReply = "ठीक आहे, वेस्टला सोडतो.",
                    saathiTip = "उत्तम! मराठीत पश्चिमेला सोडतो असं म्हणतात."
                ),
                PracticeTurn(
                    passengerMarathi = "किती पैसे झाले भाऊ?",
                    passengerMeaning = "कितने पैसे हुए भाई?",
                    sampleDriverReply = "पन्नास रुपये झाले.",
                    saathiTip = "खूप छान! पैशांचे आकडे मराठीत बोलल्यामुळे विश्वास वाढतो."
                ),
                PracticeTurn(
                    passengerMarathi = "हे घ्या साठ रुपये, दहा रुपये सुट्टे द्या.",
                    passengerMeaning = "यह लो साठ रुपये, दस रुपये छुट्टे दो.",
                    sampleDriverReply = "हे घ्या तुमचे दहा रुपये. धन्यवाद!",
                    saathiTip = "उत्कृष्ट! संपूर्ण प्रवास यशस्वीरीत्या पूर्ण झाला!"
                )
            )
        ),
        "fare" to Scenario(
            id = "fare",
            titleMarathi = "भाडे ठरवणे",
            titleEnglish = "Fare haggling",
            voice = "Ishaan",
            openingMarathi = "भाऊ, अंधेरी स्टेशनला किती भाडे घ्याल?",
            openingMeaning = "भाई, अंधेरी स्टेशन का कितना भाड़ा लोगे?",
            persona = "A passenger who politely haggles over the fare to Andheri station.",
            topic = "A passenger and an auto driver negotiating the fare in Marathi, amounts in rupees.",
            turns = listOf(
                PracticeTurn(
                    passengerMarathi = "भाऊ, अंधेरी स्टेशनला किती भाडे घ्याल?",
                    passengerMeaning = "भाई, अंधेरी स्टेशन का कितना भाड़ा लोगे?",
                    sampleDriverReply = "मीटरप्रमाणे होईल भाऊ.",
                    saathiTip = "शाबास! 'मीटरप्रमाणे होईल' किंवा 'मीटरने चला' अगदी बरोबर."
                ),
                PracticeTurn(
                    passengerMarathi = "दोनशे रुपये खूप जास्त वाटतात, दीडशे कराल का?",
                    passengerMeaning = "दो सौ रुपये बहुत ज्यादा लग रहे हैं, डेढ़ सौ करोगे क्या?",
                    sampleDriverReply = "नाही भाऊ, ट्रॅफिक खूप आहे, मीटरनेच या.",
                    saathiTip = "छान! नम्रपणे 'ट्रॅफिक आहे' किंवा 'मीटरनेच योग्य राहील' सांगा."
                ),
                PracticeTurn(
                    passengerMarathi = "बरं ठीक आहे, मीटर चालू करा आणि चला.",
                    passengerMeaning = "अच्छा ठीक है, मीटर चालू करो और चलो.",
                    sampleDriverReply = "हो, मीटर डाऊन केला आहे. चला.",
                    saathiTip = "उत्तम! 'मीटर चालू केला आहे' म्हणा."
                ),
                PracticeTurn(
                    passengerMarathi = "सुट्टे शंभर आहेत का तुमच्याकडे?",
                    passengerMeaning = "छुट्टे सौ रुपये हैं क्या आपके पास?",
                    sampleDriverReply = "होय, सुट्टे पैसे आहेत.",
                    saathiTip = "खूप छान! सुट्टे पैसे आहेत असे स्पष्ट सांगावे."
                ),
                PracticeTurn(
                    passengerMarathi = "हे घ्या भाडे, खूप छान चालवली गाडी.",
                    passengerMeaning = "यह लीजिए भाड़ा, बहुत अच्छी गाड़ी चलाई.",
                    sampleDriverReply = "आभार भाऊ, काळजी घ्या!",
                    saathiTip = "अतिशय उत्तम! 'आभार' किंवा 'धन्यवाद' म्हटल्याने प्रवासी खूश होतात."
                )
            )
        ),
        "directions" to Scenario(
            id = "directions",
            titleMarathi = "रस्ता दाखवणे",
            titleEnglish = "Giving directions",
            voice = "Zahira",
            openingMarathi = "भाऊ, पुढचा सिग्नल आला की कुठे वळायचं?",
            openingMeaning = "भाई, अगला सिग्नल आया तो कहाँ मुड़ना है?",
            persona = "A passenger who does not know the area and asks where to turn.",
            topic = "Directions in Marathi: डावीकडे, उजवीकडे, सरळ, सिग्नल, इथे थांबा.",
            turns = listOf(
                PracticeTurn(
                    passengerMarathi = "भाऊ, पुढचा सिग्नल आला की कुठे वळायचं?",
                    passengerMeaning = "भाई, अगला सिग्नल आया तो कहाँ मुड़ना है?",
                    sampleDriverReply = "सिग्नलवरून उजवीकडे वळा.",
                    saathiTip = "शाबास! 'उजवीकडे वळा' (Right turn) आणि 'डावीकडे' (Left turn)."
                ),
                PracticeTurn(
                    passengerMarathi = "त्यानंतर रस्ता सरळ जातो का?",
                    passengerMeaning = "उसके बाद रास्ता सीधा जाता है क्या?",
                    sampleDriverReply = "हो, पुढे सरळ जा आणि फ्लायओव्हर चढा.",
                    saathiTip = "छान! 'सरळ जा' म्हणजे सीधा जाइए."
                ),
                PracticeTurn(
                    passengerMarathi = "इथे डावीकडे एक मेडिकल दुकान आहे का?",
                    passengerMeaning = "यहाँ बाईं तरफ एक मेडिकल दुकान है क्या?",
                    sampleDriverReply = "हो, डाव्या बाजूला मेडिकल आहे.",
                    saathiTip = "उत्तम! 'डाव्या बाजूला' अचूक दिशा दाखवली."
                ),
                PracticeTurn(
                    passengerMarathi = "इथेच गाडी बाजूला घ्या आणि थांबा.",
                    passengerMeaning = "यहीं गाड़ी किनारे लीजिए और रुकिए.",
                    sampleDriverReply = "हो, इथे बाजूला थांबवतो.",
                    saathiTip = "खूप छान! 'थांबवतो' म्हणजे रोकता हूँ."
                ),
                PracticeTurn(
                    passengerMarathi = "धन्यवाद भाऊ, योग्य रस्त्याने आणलंत.",
                    passengerMeaning = "धन्यवाद भाई, सही रास्ते से लाए.",
                    sampleDriverReply = "काही हरकत नाही, पुन्हा या!",
                    saathiTip = "उत्कृष्ट! दिशा दाखवण्याचा सराव पूर्ण झाला!"
                )
            )
        ),
        "elderly" to Scenario(
            id = "elderly",
            titleMarathi = "ज्येष्ठ नागरिक प्रवासी",
            titleEnglish = "Elderly rider",
            voice = "Ishaan",
            openingMarathi = "बाळा, हळू चालवशील ना? मला जरा त्रास होतो.",
            openingMeaning = "बेटा, धीरे चलाओगे ना? मुझे थोड़ी तकलीफ होती है.",
            persona = "A kind elderly man going to a hospital who speaks slowly and gently.",
            topic = "An elderly passenger speaking slowly in Marathi to an auto driver.",
            turns = listOf(
                PracticeTurn(
                    passengerMarathi = "बाळा, हळू चालवशील ना? मला जरा त्रास होतो.",
                    passengerMeaning = "बेटा, धीरे चलाओगे ना? मुझे थोड़ी तकलीफ होती है.",
                    sampleDriverReply = "हो काका, काळजी करू नका, अगदी हळू चालवेन.",
                    saathiTip = "खूप छान! ज्येष्ठ नागरिकांना 'काका' किंवा 'आजी' म्हणून आदर द्या."
                ),
                PracticeTurn(
                    passengerMarathi = "मला लीलावती हॉस्पिटलला जायचं आहे, ओळखतोस ना?",
                    passengerMeaning = "मुझे लीलावती अस्पताल जाना है, पहचानते हो ना?",
                    sampleDriverReply = "हो, माहीत आहे. थेट गेटसमोर सोडतो.",
                    saathiTip = "उत्तम! 'थेट गेटसमोर सोडतो' ने प्रवाशाला धीर मिळतो."
                ),
                PracticeTurn(
                    passengerMarathi = "रस्त्यात खड्डे असतील तर जरा सांभाळून चालव.",
                    passengerMeaning = "रास्ते में गड्ढे हों तो जरा संभालकर चलाना.",
                    sampleDriverReply = "नक्कीच काका, मी गाडी सावकाश नेतो.",
                    saathiTip = "छान! 'सावकाश' म्हणजे हळूहळू आणि काळजीपूर्वक."
                ),
                PracticeTurn(
                    passengerMarathi = "आलो का आपण? मला उतरायला जरा हात देशील?",
                    passengerMeaning = "आ गए क्या हम? मुझे उतरने में थोड़ा हाथ दोगे?",
                    sampleDriverReply = "हो, मी हात देतो, आरामात उतरा.",
                    saathiTip = "शाबास! अशी मदत केल्याने तुमचा आदर आणि आशीर्वाद मिळतो."
                ),
                PracticeTurn(
                    passengerMarathi = "खूप चांगला मुलगा आहेस. देव तुझं भलं करो!",
                    passengerMeaning = "बहुत अच्छे लड़के हो. भगवान तुम्हारा भला करे!",
                    sampleDriverReply = "धन्यवाद काका, तब्येत सांभाळा!",
                    saathiTip = "उत्कृष्ट! सेवाभावी संवादामुळे प्रवासी खूप आनंदी झाले!"
                )
            )
        ),
        "rto" to Scenario(
            id = "rto",
            titleMarathi = "आरटीओ मॉक चाचणी",
            titleEnglish = "RTO mock test",
            voice = "Ishaan",
            openingMarathi = "नमस्कार. मी आरटीओ अधिकारी आहे. तुमचं नाव आणि तुम्ही कुठे राहता ते सांगा.",
            openingMeaning = "नमस्कार. मैं आरटीओ अधिकारी हूँ. आपका नाम और आप कहाँ रहते हैं बताइए.",
            persona = "A calm RTO officer doing a spoken practice check: name, license, signal rules.",
            topic = "A spoken RTO driving check in Marathi: signals, road signs, name, address.",
            turns = listOf(
                PracticeTurn(
                    passengerMarathi = "नमस्कार. मी आरटीओ अधिकारी आहे. तुमचं नाव आणि तुम्ही कुठे राहता ते सांगा.",
                    passengerMeaning = "नमस्कार. मैं आरटीओ अधिकारी हूँ. आपका नाम और आप कहाँ रहते हैं बताइए.",
                    sampleDriverReply = "माझं नाव रमेश आहे, मी कुर्ल्याला राहतो.",
                    saathiTip = "शाबास! 'माझं नाव...' आणि 'मी ... राहतो' स्पष्ट सांगा."
                ),
                PracticeTurn(
                    passengerMarathi = "तुम्ही किती वर्षांपासून महाराष्ट्रात रिक्षा चालवत आहात?",
                    passengerMeaning = "आप कितने साल से महाराष्ट्र में रिक्शा चला रहे हैं?",
                    sampleDriverReply = "मी पाच वर्षांपासून रिक्षा चालवतो.",
                    saathiTip = "उत्तम! आकडा आणि वर्ष स्पष्ट उच्चारले."
                ),
                PracticeTurn(
                    passengerMarathi = "लाल दिवा लागला की काय करायचं?",
                    passengerMeaning = "लाल बत्ती जले तो क्या करना है?",
                    sampleDriverReply = "लाल दिवा लागला की गाडी थांबवायची.",
                    saathiTip = "छान! 'गाडी झेब्रा क्रॉसिंगच्या मागे थांबवायची'."
                ),
                PracticeTurn(
                    passengerMarathi = "'वाहनांना प्रवेश बंद' या पाटीचा अर्थ काय?",
                    passengerMeaning = "'वाहनांना प्रवेश बंद' इस बोर्ड का क्या मतलब है?",
                    sampleDriverReply = "इथे गाडी नेण्यास मनाई आहे, नो एन्ट्री.",
                    saathiTip = "खूप छान! नो एन्ट्री म्हणजे वाहनांना प्रवेश नाही."
                ),
                PracticeTurn(
                    passengerMarathi = "छान! तुमची बोलण्याची तयारी चांगली आहे. सराव चालू ठेवा.",
                    passengerMeaning = "अच्छा! आपकी बोलने की तैयारी अच्छी है. अभ्यास जारी रखिए.",
                    sampleDriverReply = "धन्यवाद साहेब, मी रोज सराव करेन!",
                    saathiTip = "उत्कृष्ट! आरटीओ मॉक सराव यशस्वी झाला! (सराव गुण)"
                )
            )
        )
    )
}
