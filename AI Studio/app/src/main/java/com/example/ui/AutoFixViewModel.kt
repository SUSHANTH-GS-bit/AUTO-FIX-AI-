package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AutoFixViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // UI Observables from DB
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatHistory: StateFlow<List<DiagnosticMessage>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nearbyGarages: StateFlow<List<Garage>> = repository.nearbyGarages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParts: StateFlow<List<PartPrice>> = repository.allParts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selection State
    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()

    // Parts Search Query
    private val _partsQuery = MutableStateFlow("")
    val partsQuery: StateFlow<String> = _partsQuery.asStateFlow()

    val filteredParts: StateFlow<List<PartPrice>> = _partsQuery
        .combine(allParts) { query, partsList ->
            if (query.isBlank()) {
                partsList
            } else {
                partsList.filter {
                    it.partName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.compatibleCars.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini API Request Status
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Camera Upload state (multimodal diagnostics)
    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    // Map Locations (Simulated Location Tracking)
    private val _userLatitude = MutableStateFlow(37.7749) // SF default
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(-122.4194)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    private val _isTrackingLocation = MutableStateFlow(true)
    val isTrackingLocation: StateFlow<Boolean> = _isTrackingLocation.asStateFlow()

    private val _activeDirections = MutableStateFlow<Garage?>(null)
    val activeDirections: StateFlow<Garage?> = _activeDirections.asStateFlow()

    // Roadside Relaxation States
    private val _relaxationSoundscape = MutableStateFlow("None") // "None", "Windshield Rain", "V8 Purr", "Radio Lofi"
    val relaxationSoundscape: StateFlow<String> = _relaxationSoundscape.asStateFlow()

    private val _isBreathingActive = MutableStateFlow(false)
    val isBreathingActive: StateFlow<Boolean> = _isBreathingActive.asStateFlow()

    private val _breathingPhrase = MutableStateFlow("Ready")
    val breathingPhrase: StateFlow<String> = _breathingPhrase.asStateFlow()

    private val _breathingProgress = MutableStateFlow(1f) // 1f = neutral, 2f = expand (inhale), 0.5f = collapse (exhale)
    val breathingProgress: StateFlow<Float> = _breathingProgress.asStateFlow()

    private val _clunkyEngineClickCount = MutableStateFlow(0)
    val clunkyEngineClickCount: StateFlow<Int> = _clunkyEngineClickCount.asStateFlow()

    init {
        // Trigger Seeding
        viewModelScope.launch {
            try {
                repository.seedDatabase()
                // Auto-resolve active vehicle selection based on database
                userProfile.collectLatest { profile ->
                    if (profile != null && profile.selectedVehicleId != null) {
                        val v = repository.getVehicleById(profile.selectedVehicleId)
                        _selectedVehicle.value = v
                    } else {
                        val vLatest = repository.getLatestVehicle()
                        if (vLatest != null) {
                            _selectedVehicle.value = vLatest
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AutoFixVM", "Failed seeding database: ${e.message}")
            }
        }

        // Simulating Breathing cycle animation and stress relaxation indicators
        viewModelScope.launch {
            while (true) {
                if (_isBreathingActive.value) {
                    _breathingPhrase.value = "Inhale Comfort..."
                    animateBreathingProgress(1f, 1.8f)
                    kotlinx.coroutines.delay(4000)

                    _breathingPhrase.value = "Hold..."
                    kotlinx.coroutines.delay(2000)

                    _breathingPhrase.value = "Exhale Tension..."
                    animateBreathingProgress(1.8f, 1f)
                    kotlinx.coroutines.delay(4000)
                } else {
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }

    private suspend fun animateBreathingProgress(from: Float, to: Float) {
        val steps = 40
        val change = (to - from) / steps
        for (i in 1..steps) {
            _breathingProgress.value = from + (change * i)
            kotlinx.coroutines.delay(100)
        }
    }

    // Google Sign-In simulation saves actual details info inside Room database
    fun simulateGoogleSignIn(email: String, displayName: String) {
        viewModelScope.launch {
            val user = UserProfile(
                name = displayName,
                email = email,
                avatarIdentifier = "avatar_car_icon",
                isSignedIn = true,
                selectedVehicleId = _selectedVehicle.value?.id
            )
            repository.saveProfile(user)
        }
    }

    fun selectUserProfileVehicle(v: Vehicle) {
        viewModelScope.launch {
            _selectedVehicle.value = v
            repository.updateSelectedVehicle(v.id)
        }
    }

    fun addNewVehicle(company: String, model: String, year: String, variant: String) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                company = company,
                model = model,
                year = year,
                variant = variant,
                companyLogo = company.lowercase()
            )
            val id = repository.addVehicle(vehicle)
            val createdVehicle = vehicle.copy(id = id)
            selectUserProfileVehicle(createdVehicle)
        }
    }

    fun updatePartsQuery(q: String) {
        _partsQuery.value = q
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setLocationTracking(enabled: Boolean) {
        _isTrackingLocation.value = enabled
        // Mock user coordinate changes when simulated movement is active
        if (enabled) {
            _userLatitude.value = 37.7749 + (Math.random() - 0.5) * 0.005
            _userLongitude.value = -122.4194 + (Math.random() - 0.5) * 0.005
        }
    }

    fun selectDirectionsToGarage(g: Garage?) {
        _activeDirections.value = g
    }

    fun setRelaxationSoundscape(soundType: String) {
        _relaxationSoundscape.value = soundType
    }

    fun toggleBreathing(active: Boolean) {
        _isBreathingActive.value = active
        if (!active) {
            _breathingPhrase.value = "Relaxed"
            _breathingProgress.value = 1f
        }
    }

    fun tapRepairGameEngine() {
        _clunkyEngineClickCount.value += 1
    }

    fun resetGame() {
        _clunkyEngineClickCount.value = 0
    }

    fun setCapturedPhoto(bitmap: Bitmap?) {
        _capturedPhoto.value = bitmap
    }

    // Virtual Automotive Mechanic AI Assistant chatbot query using live vehicle details
    fun submitMechanicQuestion(userQuestion: String) {
        if (userQuestion.isBlank() && _capturedPhoto.value == null) return

        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null

            // 1. Store User Message in Room DB
            val userMsg = DiagnosticMessage(
                sender = "user",
                text = userQuestion,
                imageUri = if (_capturedPhoto.value != null) "captured_image_attachment" else null
            )
            repository.sendMessage(userMsg)

            // Convert Bitmap attachment if available
            val base64Img = _capturedPhoto.value?.let { convertBitmapToBase64(it) }
            _capturedPhoto.value = null // clear scanner box

            // 2. Fetch past conversation turns for context retention (Limit last 5 turns)
            val currentHistory = chatHistory.value.takeLast(10)
            val contentsList = mutableListOf<GeminiContent>()

            // Append historical chats
            currentHistory.forEach { msg ->
                if (msg.text.isNotBlank()) {
                    contentsList.add(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = msg.text))
                        )
                    )
                }
            }

            // 3. Construct current query
            val currentVehicle = _selectedVehicle.value
            val currentPartList = mutableListOf<GeminiPart>()

            val promptHeader = if (currentVehicle != null) {
                "[Selected Car Detailing: ${currentVehicle.year} ${currentVehicle.company} ${currentVehicle.model} (${currentVehicle.variant})]\n"
            } else {
                "[No vehicle configured yet]\n"
            }

            val questionText = "$promptHeader$userQuestion"
            currentPartList.add(GeminiPart(text = questionText))

            // Multimodal input! Attach scanned photo
            if (base64Img != null) {
                currentPartList.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = base64Img
                        )
                    )
                )
            }
            contentsList.add(GeminiContent(parts = currentPartList))

            // 4. Configure System prompt ensuring beautiful professional answers
            val systemPrompt = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are an ultimate, friendly Master Auto Mechanic AI called 'AutoFix Assistant' built into a mobile app. " +
                                "Answer structural defects, odd sounds, dashboard symptoms, error codes, fluids or routine diagnostics. " +
                                "Structure answers in elegant markdown with bullet points, safety guidelines (⚠️ Safety first), recommended diagnostic DIY actions, timing, estimated costs, and low-cost spare availability. " +
                                "If the selected vehicle is a gas/electric car, tailor mechanical concepts appropriately (e.g. Electric vehicles don't use spark plugs or engine oils!). Keep descriptions extremely friendly, clear, and focused on keeping the driver safe and stress-free."
                    )
                )
            )

            // 5. Send API Request directly via REST
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _isGenerating.value = false
                // Place helpful mock backup message if system API key is set to placeholder
                val errorMsg = "API Key not configured. (To fix: Click the Secrets Tab, add GEMINI_API_KEY). \nLet me help you locally: For a common symptom, check your fuses, fuel caps, battery connectors, and engine oil levels."
                repository.sendMessage(
                    DiagnosticMessage(
                        sender = "assistant",
                        text = "🔧 **AutoFix Simulation Diagnostic Service**:\n\n* **Primary Diagnosis**: $errorMsg\n\n* **Safety Tip**: Never touch engine coolant caps when hot! Clear fluid reservoir slowly after 1 hour cool-down."
                    )
                )
                return@launch
            }

            try {
                val reqBody = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = systemPrompt
                )
                val response = GeminiRetrofitClient.api.generateContent(apiKey, reqBody)
                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (generatedText != null) {
                    repository.sendMessage(
                        DiagnosticMessage(sender = "assistant", text = generatedText)
                    )
                } else {
                    repository.sendMessage(
                        DiagnosticMessage(
                            sender = "assistant",
                            text = "❌ I received an empty analysis from the mechanic system. Please check your query or retake the image scanner capture."
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AutoFixVM", "Gemini API failure", e)
                _errorMessage.value = e.message
                repository.sendMessage(
                    DiagnosticMessage(
                        sender = "assistant",
                        text = "⚠️ **Diagnose Connection Error**: ${e.message ?: "Failed to reach the mechanic servers."}\n\n*Troubleshooting checkpoint: check your network connection and verify your GEMINI_API_KEY placeholder is correctly configured.*"
                    )
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearProfile()
        }
    }
}
