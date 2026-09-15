package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.CaptionLanguage
import com.example.data.LiveCaption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AndroidSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var remoteSpeechJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentSentence = MutableStateFlow("Listening for speech...")
    val currentSentence: StateFlow<String> = _currentSentence.asStateFlow()

    private val _isRemoteSpeaking = MutableStateFlow(false)
    val isRemoteSpeaking: StateFlow<Boolean> = _isRemoteSpeaking.asStateFlow()

    private val _activeSpeakerName = MutableStateFlow("Remote Caller")
    val activeSpeakerName: StateFlow<String> = _activeSpeakerName.asStateFlow()

    private val _soundLevelRms = MutableStateFlow(0f)
    val soundLevelRms: StateFlow<Float> = _soundLevelRms.asStateFlow()

    private val _captionsTranscript = MutableStateFlow<List<LiveCaption>>(emptyList())
    val captionsTranscript: StateFlow<List<LiveCaption>> = _captionsTranscript.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(CaptionLanguage.ENGLISH)
    val selectedLanguage: StateFlow<CaptionLanguage> = _selectedLanguage.asStateFlow()

    private val _recognitionAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val recognitionAvailable: StateFlow<Boolean> = _recognitionAvailable.asStateFlow()

    // Conversational demo scripts for simulated caller speech stream
    private val englishCallerScript = listOf(
        "Hello! I can see and hear you clearly over SmartVideocall.",
        "Notice how each spoken sentence instantly appears in the live text captions panel below.",
        "This makes video calling in text effortless, even in noisy halls or for hearing accessibility.",
        "SmartVideocall delivers ultra-low latency with continuous speech recognition.",
        "Would you like me to switch and demonstrate speech captions in Tamil now?",
        "Everything spoken and typed during our call is saved directly into the exportable transcript.",
        "You can also type text messages directly on screen while we video call!"
    )

    private val tamilCallerScript = listOf(
        "வணக்கம்! SmartVideocall நேரடி தலைப்புகள் திரையில் தெளிவாக தோன்றுகின்றன.",
        "நிகழ்நேர உரைபெயர்ப்பு மற்றும் ஒலி பகுப்பாய்வு மிக விரைவாக இயங்குகிறது.",
        "இந்த அம்சம் வீடியோ அழைப்புகளில் உரையாடல்களை எளிதாக புரிந்து கொள்ள உதவுகிறது.",
        "தமிழ் மொழி பேச்சு அங்கீகாரம் மிக துல்லியமாக வேலை செய்கிறது!",
        "அழைப்பில் நேரடியாக உரையும் தட்டச்சு செய்து அனுப்பலாம்!"
    )

    fun setLanguage(lang: CaptionLanguage) {
        _selectedLanguage.value = lang
        if (_isListening.value) {
            restartListening()
        }
    }

    fun startListening(remoteCallerName: String = "Alex Rivera") {
        _activeSpeakerName.value = remoteCallerName
        _isListening.value = true
        _currentSentence.value = "Listening for live speech..."

        // Initialize Android system SpeechRecognizer if available
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }
                startAndroidRecognizer()
            }
        } catch (e: Exception) {
            Log.e("SpeechManager", "Failed to start hardware recognizer: ${e.message}")
        }

        // Start remote participant conversation stream
        startRemoteCallerSpeechStream(remoteCallerName)
    }

    private fun startAndroidRecognizer() {
        val recognizer = speechRecognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, _selectedLanguage.value.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.w("SpeechManager", "Recognize intent error: ${e.message}")
        }
    }

    fun stopListening() {
        _isListening.value = false
        _isRemoteSpeaking.value = false
        remoteSpeechJob?.cancel()
        remoteSpeechJob = null
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w("SpeechManager", "Error stopping recognizer: ${e.message}")
        }
    }

    private fun restartListening() {
        try {
            speechRecognizer?.stopListening()
            startAndroidRecognizer()
        } catch (e: Exception) {
            Log.w("SpeechManager", "Error restarting recognizer: ${e.message}")
        }
    }

    private fun startRemoteCallerSpeechStream(callerName: String) {
        remoteSpeechJob?.cancel()
        remoteSpeechJob = coroutineScope.launch {
            delay(1200) // Initial conversational pause
            var scriptIndex = 0

            while (isActive && _isListening.value) {
                val script = if (_selectedLanguage.value == CaptionLanguage.TAMIL) tamilCallerScript else englishCallerScript
                val sentence = script[scriptIndex % script.size]

                // Remote caller starts speaking
                _isRemoteSpeaking.value = true
                _activeSpeakerName.value = callerName
                _soundLevelRms.value = 6.5f

                // Word-by-word streaming effect for realistic live captioning
                val words = sentence.split(" ")
                val builder = StringBuilder()
                for (word in words) {
                    if (!isActive) break
                    if (builder.isNotEmpty()) builder.append(" ")
                    builder.append(word)
                    _currentSentence.value = builder.toString()
                    delay(180)
                }

                // Add to transcript
                val timeNow = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())
                val newCaption = LiveCaption(
                    id = UUID.randomUUID().toString(),
                    speakerName = callerName,
                    isRemote = true,
                    text = sentence,
                    timestamp = timeNow,
                    language = _selectedLanguage.value
                )

                val list = _captionsTranscript.value.toMutableList()
                list.add(newCaption)
                _captionsTranscript.value = list

                _isRemoteSpeaking.value = false
                _soundLevelRms.value = 1.0f
                scriptIndex++

                // Pause before next sentence
                delay(3500)
            }
        }
    }

    /**
     * User speaks via mic or triggers a recognized user caption
     */
    fun recordUserSpeech(text: String) {
        if (text.isBlank()) return
        _currentSentence.value = text
        _activeSpeakerName.value = "You"
        val timeNow = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())
        val caption = LiveCaption(
            id = UUID.randomUUID().toString(),
            speakerName = "You",
            isRemote = false,
            text = text,
            timestamp = timeNow,
            language = _selectedLanguage.value
        )
        val list = _captionsTranscript.value.toMutableList()
        list.add(caption)
        _captionsTranscript.value = list
    }

    // Records live in-call text message sent during the video call
    fun recordInCallTextMessage(senderName: String, text: String, isRemote: Boolean = false) {
        if (text.isBlank()) return
        val label = if (isRemote) senderName else "You (In-Call Text)"
        _currentSentence.value = "💬 $text"
        _activeSpeakerName.value = label
        val timeNow = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())
        val caption = LiveCaption(
            id = UUID.randomUUID().toString(),
            speakerName = label,
            isRemote = isRemote,
            text = "💬 $text",
            timestamp = timeNow,
            language = _selectedLanguage.value
        )
        val list = _captionsTranscript.value.toMutableList()
        list.add(caption)
        _captionsTranscript.value = list
    }

    // Simulates speech or caption from any specific user participant
    fun simulateSpeaker(speakerName: String, text: String) {
        if (text.isBlank()) return
        _currentSentence.value = text
        _activeSpeakerName.value = speakerName
        _isRemoteSpeaking.value = true
        val timeNow = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())
        val caption = LiveCaption(
            id = UUID.randomUUID().toString(),
            speakerName = speakerName,
            isRemote = true,
            text = text,
            timestamp = timeNow,
            language = _selectedLanguage.value
        )
        val list = _captionsTranscript.value.toMutableList()
        list.add(caption)
        _captionsTranscript.value = list
    }

    fun clearTranscript() {
        _captionsTranscript.value = emptyList()
        _currentSentence.value = "Transcript cleared. Listening for speech..."
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechManager", "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            _activeSpeakerName.value = "You (Live Mic)"
            _isRemoteSpeaking.value = false
        }

        override fun onRmsChanged(rmsdB: Float) {
            if (!_isRemoteSpeaking.value) {
                _soundLevelRms.value = (rmsdB.coerceAtLeast(0f) / 10f).coerceIn(0f, 10f)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            Log.w("SpeechManager", "Speech recognition error code: $error")
            // Auto restart listening after transient timeout
            if (_isListening.value) {
                coroutineScope.launch {
                    delay(1000)
                    if (_isListening.value) startAndroidRecognizer()
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spoken = matches[0]
                recordUserSpeech(spoken)
            }
            if (_isListening.value) {
                startAndroidRecognizer()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _currentSentence.value = matches[0]
                _activeSpeakerName.value = "You (Live Mic)"
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
