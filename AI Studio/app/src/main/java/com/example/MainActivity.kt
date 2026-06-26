package com.example

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize Room Database using application context
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = AppRepository(
                    userDao = database.userDao(),
                    vehicleDao = database.vehicleDao(),
                    chatDao = database.chatDao(),
                    garageDao = database.garageDao(),
                    partDao = database.partDao()
                )

                // Standard bulletproof Factory Injection pattern
                val mainViewModel: AutoFixViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AutoFixViewModel(application, repository) as T
                        }
                    }
                )

                MainNavigationHost(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun MainNavigationHost(viewModel: AutoFixViewModel) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val isUserSignedIn = profile?.isSignedIn == true

    if (!isUserSignedIn) {
        GoogleSignInScreen(
            onAccountSelected = { email, name ->
                viewModel.simulateGoogleSignIn(email, name)
                Toast.makeText(context, "Welcome, $name!", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        ScaffoldAppContent(viewModel = viewModel, profile = profile)
    }
}

// 1. SIGN IN SCREEN WITH GOOGLE SELECTION SIMULATION
@Composable
fun GoogleSignInScreen(onAccountSelected: (String, String) -> Unit) {
    var showAccountDialog by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(CarbonDark, PetrolSlate)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Speedometer Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ChevronOrange.copy(alpha = 0.15f))
                    .border(2.dp, ChevronOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Logo Speedometer",
                    tint = ChevronOrange,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AUTOFIX AI",
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = SteelHigh,
                letterSpacing = 6.sp
            )

            Text(
                text = "Virtual Assistant & Roadside Mechanic",
                fontSize = 13.sp,
                color = SteelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 48.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SteelLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connect Your Account",
                        fontWeight = FontWeight.Bold,
                        color = SteelHigh,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Access tailored car diagnostics, local repair directories, spares comparison, and roadside relaxation aids.",
                        color = SteelMedium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Simulated Google account trigger
                    Button(
                        onClick = { showAccountDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ChevronOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Icon",
                            tint = CarbonDark
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign In with Google",
                            fontWeight = FontWeight.Bold,
                            color = CarbonDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Protected by secure local state encryption",
                fontSize = 10.sp,
                color = SteelMedium.copy(alpha = 0.6f)
            )
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text(
                    text = "Choose Google Account",
                    fontWeight = FontWeight.Bold,
                    color = SteelHigh
                )
            },
            containerColor = PetrolSlate,
            text = {
                Column {
                    Text(
                        text = "AutoFix Assistant will connect to your account details:",
                        color = SteelMedium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val accounts = listOf(
                        Pair("sushanthgs28@gmail.com", "Sushanth G S"),
                        Pair("aistudio.coder@gmail.com", "DeepMind Developer"),
                        Pair("guest.motorist@gmail.com", "Guest Driver")
                    )

                    accounts.forEach { (email, name) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showAccountDialog = false
                                    onAccountSelected(email, name)
                                },
                            border = BorderStroke(1.dp, SteelLow)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ChevronOrange.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = ChevronOrange,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        color = SteelHigh,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = email,
                                        color = SteelMedium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// 2. MASTER APP SCAFFOLD WRAPPING BOTTOM NAVIGATION
@Composable
fun ScaffoldAppContent(viewModel: AutoFixViewModel, profile: UserProfile?) {
    var activeTab by remember { mutableStateOf(0) }
    var showVehicleSheet by remember { mutableStateOf(false) }

    val activeVehicle by viewModel.selectedVehicle.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(CarbonDark)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUTOFIX AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ChevronOrange,
                            letterSpacing = 2.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Text(
                                text = "Online Assistant",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SteelMedium
                            )
                        }
                    }

                    // Active Car Selector Pill header
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SteelLow),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { showVehicleSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Car",
                                tint = ChevronOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeVehicle?.let { "${it.company} ${it.model}" } ?: "Setup Car",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SteelHigh,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 110.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = SteelLow.copy(alpha = 0.5f), thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                contentColor = SteelMedium,
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                val menuItems = listOf(
                    Triple("Chat", Icons.Default.Forum, Icons.Outlined.Forum),
                    Triple("Garage", Icons.Default.Map, Icons.Outlined.Map),
                    Triple("Parts", Icons.Default.ShoppingBag, Icons.Outlined.ShoppingBag),
                    Triple("Relax", Icons.Default.Spa, Icons.Outlined.Spa),
                    Triple("Guides", Icons.Default.PlayCircle, Icons.Outlined.PlayCircle)
                )

                menuItems.forEachIndexed { idx, item ->
                    NavigationBarItem(
                        selected = idx == activeTab,
                        onClick = { activeTab = idx },
                        label = { Text(item.first, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = if (idx == activeTab) item.second else item.third,
                                contentDescription = item.first,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricBlue,
                            selectedTextColor = ElectricBlue,
                            indicatorColor = ActiveBlueContainer,
                            unselectedTextColor = SteelMedium,
                            unselectedIconColor = SteelMedium
                        )
                    )
                }
            }
        },
        containerColor = CarbonDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (activeTab) {
                0 -> AssistantTabScreen(viewModel = viewModel)
                1 -> GarageTabScreen(viewModel = viewModel)
                2 -> PartsFinderTabScreen(viewModel = viewModel)
                3 -> StressReliefTabScreen(viewModel = viewModel)
                4 -> GuidesEmergencyTabScreen(viewModel = viewModel)
            }
        }
    }

    if (showVehicleSheet) {
        VehicleConfigurationModal(
            viewModel = viewModel,
            onDismiss = { showVehicleSheet = false }
        )
    }
}

// 2A. CHAT AND IMAGE ENGINE DIAGNOSTIC SCANNER VIEW (VIRTUAL MECHANIC)
@Composable
fun AssistantTabScreen(viewModel: AutoFixViewModel) {
    val messages by viewModel.chatHistory.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val photoAttachment by viewModel.capturedPhoto.collectAsState()
    val activeVehicle by viewModel.selectedVehicle.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Permissions configuration
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Trigger customized camera capture simulation bitmap
            val mockBitmap = createMockDiagnosticSnapshot()
            viewModel.setCapturedPhoto(mockBitmap)
            Toast.makeText(context, "Scanned: Engine leak photo attached!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission is denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // AutoScroll to bottom of chat
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Conversation log
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BuildCircle,
                        contentDescription = "Fix",
                        tint = SteelLow,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Virtual AI Mechanic is Ready",
                        fontWeight = FontWeight.Bold,
                        color = SteelHigh,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Select an issue chip or type/snap a query below.",
                        color = SteelMedium,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Symptom Chips
                    val symptoms = listOf(
                        "Check Engine glow",
                        "White exhaust smoke",
                        "High squeaking brake sounds",
                        "Battery died, won't start",
                        "AC blowing warm air"
                    )

                    symptoms.forEach { symptom ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SteelLow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                                .clickable {
                                    textInput = "Why is my car experiencing a $symptom?"
                                    viewModel.submitMechanicQuestion(textInput)
                                    textInput = ""
                                }
                        ) {
                            Text(
                                text = "💡 $symptom",
                                fontSize = 12.sp,
                                color = SteelHigh,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    messages.forEach { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) ChevronOrange else SurfaceCard
                                ),
                                modifier = Modifier.widthIn(max = 290.dp),
                                border = if (isUser) null else BorderStroke(1.dp, SteelLow)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (msg.imageUri != null) {
                                        // Draw the simulated photograph attachment indicator
                                        Row(
                                            modifier = Modifier
                                                .background(CarbonDark.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = "Image",
                                                tint = ChevronOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "[Visual Scan: Engine Leak]",
                                                fontSize = 10.sp,
                                                color = SteelHigh,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    Text(
                                        text = msg.text,
                                        fontSize = 12.sp,
                                        color = if (isUser) CarbonDark else SteelHigh,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = BorderStroke(1.dp, SteelLow)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = ChevronOrange,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "AutoFix is analyzing diagnostic specifications...",
                                        fontSize = 11.sp,
                                        color = SteelHigh
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scanner Box attachment preview
        if (photoAttachment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, DiagnosticAmber, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = photoAttachment!!.asImageBitmap(),
                        contentDescription = "Mock image",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Diagnostic Photo Attached",
                            fontWeight = FontWeight.Bold,
                            color = SteelHigh,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Awaiting prompt submission",
                            fontSize = 10.sp,
                            color = DiagnosticAmber
                        )
                    }
                }

                IconButton(onClick = { viewModel.setCapturedPhoto(null) }) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel attachment",
                        tint = ChevronOrange
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera attachment scanner
            IconButton(
                onClick = {
                    // Requests camera permission contract. Once granted, seeds simulated image scanner
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .background(SurfaceCard, RoundedCornerShape(8.dp))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera Scan",
                    tint = ChevronOrange,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Text input field
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Describe car symptom...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    disabledContainerColor = SurfaceCard,
                    focusedIndicatorColor = ChevronOrange,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = SteelHigh,
                    unfocusedTextColor = SteelHigh,
                    focusedPlaceholderColor = SteelMedium,
                    unfocusedPlaceholderColor = SteelMedium
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Submitter
            val activeStateTrigger = textInput.isNotBlank() || photoAttachment != null
            IconButton(
                onClick = {
                    if (activeStateTrigger) {
                        viewModel.submitMechanicQuestion(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(if (activeStateTrigger) ChevronOrange else SteelLow, RoundedCornerShape(8.dp))
                    .size(44.dp),
                enabled = activeStateTrigger
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (activeStateTrigger) CarbonDark else SteelMedium,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// 2B. RADAR MAP & LOCAL GARAGE PATH DIRECTIONS VIEW
@Composable
fun GarageTabScreen(viewModel: AutoFixViewModel) {
    val garages by viewModel.nearbyGarages.collectAsState()
    val isTracking by viewModel.isTrackingLocation.collectAsState()
    val routeToGarage by viewModel.activeDirections.collectAsState()

    var selectedGarage by remember { mutableStateOf<Garage?>(null) }

    val userLat by viewModel.userLatitude.collectAsState()
    val userLng by viewModel.userLongitude.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📍 Local Garage Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SteelHigh
            )

            // Live Location toggle switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Live GPS",
                    fontSize = 11.sp,
                    color = SteelMedium,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Switch(
                    checked = isTracking,
                    onCheckedChange = { viewModel.setLocationTracking(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ChevronOrange,
                        checkedTrackColor = ChevronOrange.copy(alpha = 0.3f),
                        uncheckedThumbColor = SteelMedium,
                        uncheckedTrackColor = SteelLow
                    )
                )
            }
        }

        Text(
            text = "Track nearest certified repair centers around your current location coordinates dynamically.",
            style = MaterialTheme.typography.bodySmall,
            color = SteelMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Render custom animated sweeping radar map with directions route overlays!
        SimulatedGarageRadarMap(
            userLat = userLat,
            userLng = userLng,
            garages = garages,
            selectedGarage = selectedGarage,
            activeRoute = routeToGarage,
            onGarageClick = {
                selectedGarage = it
                viewModel.selectDirectionsToGarage(null) // Reset old directions to point to this new one!
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Route instruction panel if user triggered directions
        if (routeToGarage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.5.dp, ElectricBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "Routing",
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ROUTE ACTIVE: ${routeToGarage!!.name.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Walkthrough guidance: Turn left on Silicon Parkway towards ${routeToGarage!!.specialty.split(",").firstOrNull() ?: "Garage"}. Arrival in 3 mins.",
                            color = SteelHigh,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Selected Garage Bottom Detail Panel
        val targetShow = selectedGarage ?: garages.firstOrNull()
        targetShow?.let { garage ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SteelLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = garage.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SteelHigh
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = DiagnosticAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", garage.rating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SteelHigh
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🏡 Address: ${garage.address}",
                        fontSize = 12.sp,
                        color = SteelMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔧 Specialty: ${garage.specialty}",
                        fontSize = 12.sp,
                        color = ChevronOrange,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⚡ Price range: ${garage.priceTier}  •  Distance: ${garage.distanceKm} km away",
                        fontSize = 11.sp,
                        color = SteelMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Directions trigger
                        Button(
                            onClick = { viewModel.selectDirectionsToGarage(garage) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Directions")
                        }

                        // Fake Contact tool
                        OutlinedButton(
                            onClick = { /* Simulated call */ },
                            border = BorderStroke(1.dp, SteelLow),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SteelHigh)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Service")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // List representation
        Text(
            text = "Repair Center Contact Index:",
            fontWeight = FontWeight.Bold,
            color = SteelMedium,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        garages.forEach { garage ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, if (garage.id == selectedGarage?.id) ChevronOrange else SteelLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedGarage = garage }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = garage.name,
                            fontWeight = FontWeight.Bold,
                            color = SteelHigh,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${garage.specialty.split(",").firstOrNull() ?: "General"}  •  ${garage.distanceKm} km",
                            fontSize = 11.sp,
                            color = SteelMedium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Select",
                        tint = SteelMedium,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// 2C. PARTS COMPARATIVE PRICING FINDER SCREEN
@Composable
fun PartsFinderTabScreen(viewModel: AutoFixViewModel) {
    val query by viewModel.partsQuery.collectAsState()
    val partsList by viewModel.filteredParts.collectAsState()
    val activeVehicle by viewModel.selectedVehicle.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "🔧 Smart Spare Parts Finder",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SteelHigh
        )
        Text(
            text = "Search replacement parts with real-time local wholesale discount pricing matches.",
            style = MaterialTheme.typography.bodySmall,
            color = SteelMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search bar
        TextField(
            value = query,
            onValueChange = { viewModel.updatePartsQuery(it) },
            placeholder = { Text("Search by part name or category...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { viewModel.updatePartsQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "ClearIcon")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedIndicatorColor = ChevronOrange,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SteelHigh,
                unfocusedTextColor = SteelHigh
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Quick Chips
        val categories = listOf("All", "Brakes", "Engine", "Electrical", "Suspension")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = (cat == "All" && query.isBlank()) || (query.equals(cat, ignoreCase = true))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) ChevronOrange else SurfaceCard
                    ),
                    modifier = Modifier.clickable {
                        viewModel.updatePartsQuery(if (cat == "All") "" else cat)
                    },
                    border = BorderStroke(1.dp, SteelLow)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CarbonDark else SteelHigh,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Listing parts
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(partsList) { part ->
                // Check compatibility
                val isCarCompatible = activeVehicle?.let { car ->
                    part.compatibleCars.contains(car.model, ignoreCase = true) ||
                    part.compatibleCars.contains(car.company, ignoreCase = true)
                } ?: true

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, if (isCarCompatible) ElectricBlue.copy(alpha = 0.3f) else SteelLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = part.partName,
                                    fontWeight = FontWeight.Bold,
                                    color = SteelHigh,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Category: ${part.category}",
                                    fontSize = 11.sp,
                                    color = SteelMedium
                                )
                            }

                            // Pricing indicators
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${part.regularPrice}",
                                    fontSize = 11.sp,
                                    color = SteelMedium,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                )
                                Text(
                                    text = "$${part.lowCostShopPrice}",
                                    fontWeight = FontWeight.Black,
                                    color = BioGreen,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Compatibility Check Pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isCarCompatible) BioGreen else DiagnosticAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCarCompatible) {
                                    "Compatible with your Selected Car"
                                } else {
                                    "Commonly fits: ${part.compatibleCars}"
                                },
                                fontSize = 10.sp,
                                color = if (isCarCompatible) BioGreen else SteelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = SteelLow, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Low-cost shop recommendation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🛒 Best Local wholesale rate:",
                                    fontSize = 9.sp,
                                    color = SteelMedium
                                )
                                Text(
                                    text = part.lowCostShopName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SteelHigh,
                                    fontSize = 11.sp
                                )
                            }

                            val savings = ((1.0 - (part.lowCostShopPrice / part.regularPrice)) * 100).toInt()
                            Box(
                                modifier = Modifier
                                    .background(BioGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SAVE $savings%",
                                    color = BioGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            if (partsList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No direct parts found for \"$query\"",
                            color = SteelMedium,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// 2D. APP ROADSIDE RELAXATION STATE SCREEN
@Composable
fun StressReliefTabScreen(viewModel: AutoFixViewModel) {
    val soundscape by viewModel.relaxationSoundscape.collectAsState()
    val isBreathingActive by viewModel.isBreathingActive.collectAsState()
    val breathingPhrase by viewModel.breathingPhrase.collectAsState()
    val breathingScale by viewModel.breathingProgress.collectAsState()
    val engineClicks by viewModel.clunkyEngineClickCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "🧘 Roadside Calm & Relaxation Center",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SteelHigh
        )
        Text(
            text = "Stress relief aids designed to soothe driving anxieties and calm nervous systems during roadside defects.",
            style = MaterialTheme.typography.bodySmall,
            color = SteelMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Soundscape Box
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, SteelLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔊 Ambient Soothing Generator",
                    fontWeight = FontWeight.Bold,
                    color = SteelHigh,
                    fontSize = 14.sp
                )
                Text(
                    text = "Synthesize looping white-noises blocking environmental highway sounds.",
                    fontSize = 11.sp,
                    color = SteelMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val sounds = listOf("Windshield Rain", "Gentle V8 Idle", "Garage Radio Lofi", "None")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sounds.forEach { sound ->
                        val isPlaying = soundscape == sound
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPlaying) ChevronOrange else CarbonDark)
                                .border(1.dp, if (isPlaying) ChevronOrange else SteelLow, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setRelaxationSoundscape(sound) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sound.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlaying) CarbonDark else SteelHigh,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (soundscape != "None") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .background(CarbonDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Playing",
                            tint = ChevronOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Now Synthesizing Ambient Sound: $soundscape...",
                            color = SteelHigh,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Paced Respiration Guide Balloon
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, SteelLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            RelaxationBreathingBalloon(
                phrase = breathingPhrase,
                scaleProgress = breathingScale,
                isActive = isBreathingActive,
                onClick = { viewModel.toggleBreathing(!isBreathingActive) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Engine Tap Tighter Game
        ClunkyEngineTappingGame(
            clickCount = engineClicks,
            onTap = { viewModel.tapRepairGameEngine() },
            onReset = { viewModel.resetGame() }
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// 2E. CRITICAL EMERGENCY WALKTHROUGH SLIDES LIST VIEW
@Composable
fun GuidesEmergencyTabScreen(viewModel: AutoFixViewModel) {
    val context = LocalContext.current
    val activeVehicle by viewModel.selectedVehicle.collectAsState()

    var activeWalkthroughIndex by remember { mutableStateOf<Int?>(null) }

    val guides = listOf(
        Pair(
            "How to change a flat tire",
            "Detailed checklist for utilizing car jacks, loosening lug bolts with cross wrenches, and sliding on standard spare treads."
        ),
        Pair(
            "Safe auxiliary jumpstarting guide",
            "Safely bridging jumper cables: connecting Red (+/Anode) to batteries first, then Black ground clamps to engine block steel avoids short-circuits."
        ),
        Pair(
            "Engine Overheating emergency control",
            "Indicators when white water vapor expands from hoods. Pulling over instantly saves piston block heads from critical cracks. Let it cool 45 mins before checking fluids."
        ),
        Pair(
            "Replacing wiper blades",
            "Flipping safety retainers, sliding old rubber gaskets off metal hooks, and snapping synthetic weather strips onto tracks."
        )
    )

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "🎬 Emergency Walkthrough Guides",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SteelHigh
        )
        Text(
            text = "Step-by-step interactive procedures for tackling standard car issues without calling expensive tow trucks.",
            style = MaterialTheme.typography.bodySmall,
            color = SteelMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(guides.size) { idx ->
                val (title, info) = guides[idx]
                val isExpanded = activeWalkthroughIndex == idx

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, if (isExpanded) ChevronOrange else SteelLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { activeWalkthroughIndex = if (isExpanded) null else idx }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                                    contentDescription = "Play status",
                                    tint = if (isExpanded) ChevronOrange else SteelMedium,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    color = SteelHigh,
                                    fontSize = 13.sp
                                )
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Arrow",
                                tint = SteelMedium,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Expanded walkthrough instructions simulating interactive video player!
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = info,
                                    fontSize = 12.sp,
                                    color = SteelMedium,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Draw simulated progress media player bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CarbonDark, RoundedCornerShape(6.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speaker",
                                        tint = ChevronOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "VIDEO WALKTHROUGH PLAYING...",
                                        fontSize = 9.sp,
                                        color = SteelHigh,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(4.dp)
                                            .background(SteelLow, RoundedCornerShape(2.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(44.dp)
                                                .background(ChevronOrange, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Interactive diagnostic button
                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Guide completed! Keep your Selected ${activeVehicle?.company ?: "Car"} tuned.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChevronOrange),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mark as Learned", color = CarbonDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. VEHICLE SETUP MODAL (REGISTER COMPANY, MODEL, YEAR, VARIANT)
@Composable
fun VehicleConfigurationModal(viewModel: AutoFixViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.allVehicles.collectAsState()
    val activeVehicle by viewModel.selectedVehicle.collectAsState()

    var customCompany by remember { mutableStateOf("") }
    var customModel by remember { mutableStateOf("") }
    var customYear by remember { mutableStateOf("2023") }
    var customVariant by remember { mutableStateOf("Mid-Range / Luxury") }

    val presetBrands = listOf("Tesla", "BMW", "Porsche", "Toyota", "Honda", "Hyundai", "Ford")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Vehicle Management Profile",
                fontWeight = FontWeight.Black,
                color = SteelHigh,
                fontSize = 16.sp
            )
        },
        containerColor = PetrolSlate,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Preset Selector
                Text(
                    text = "Select From Registered Vehicles:",
                    fontSize = 11.sp,
                    color = SteelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                vehicles.forEach { v ->
                    val isSelected = v.id == activeVehicle?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (isSelected) ChevronOrange.copy(alpha = 0.15f) else SurfaceCard,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) ChevronOrange else SteelLow,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                viewModel.selectUserProfileVehicle(v)
                                onDismiss()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompanyLogoBadge(company = v.company)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${v.company} ${v.model}",
                                fontWeight = FontWeight.Bold,
                                color = SteelHigh,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Year: ${v.year}  •  Variant: ${v.variant}",
                                fontSize = 10.sp,
                                color = SteelMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = SteelLow, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Registry Form
                Text(
                    text = "🔧 Configure New Vehicle Detailing:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChevronOrange,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // presets row
                Text(
                    text = "Tap Logo Preset:",
                    fontSize = 11.sp,
                    color = SteelMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetBrands.forEach { brand ->
                        Column(
                            modifier = Modifier
                                .clickable { customCompany = brand }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CompanyLogoBadge(company = brand)
                            Text(
                                text = brand,
                                fontSize = 10.sp,
                                color = if (customCompany.equals(brand, true)) ChevronOrange else SteelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Text fields
                OutlinedTextField(
                    value = customCompany,
                    onValueChange = { customCompany = it },
                    label = { Text("Car Brand (e.g. Toyota)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SteelHigh,
                        unfocusedTextColor = SteelHigh,
                        focusedBorderColor = ChevronOrange,
                        unfocusedBorderColor = SteelLow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customModel,
                    onValueChange = { customModel = it },
                    label = { Text("Car Model (e.g. Fortuner)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SteelHigh,
                        unfocusedTextColor = SteelHigh,
                        focusedBorderColor = ChevronOrange,
                        unfocusedBorderColor = SteelLow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customYear,
                        onValueChange = { customYear = it },
                        label = { Text("Model Year") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelHigh,
                            unfocusedTextColor = SteelHigh,
                            focusedBorderColor = ChevronOrange,
                            unfocusedBorderColor = SteelLow
                        )
                    )

                    OutlinedTextField(
                        value = customVariant,
                        onValueChange = { customVariant = it },
                        label = { Text("Variant Trim") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelHigh,
                            unfocusedTextColor = SteelHigh,
                            focusedBorderColor = ChevronOrange,
                            unfocusedBorderColor = SteelLow
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (customCompany.isNotBlank() && customModel.isNotBlank()) {
                            viewModel.addNewVehicle(
                                company = customCompany,
                                model = customModel,
                                year = customYear,
                                variant = customVariant
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChevronOrange),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = customCompany.isNotBlank() && customModel.isNotBlank()
                ) {
                    Text("Register & Activate Car", color = CarbonDark, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {}
    )
}

// Visual diagnostic snapshot generator (drawing a mock canvas)
fun createMockDiagnosticSnapshot(): Bitmap {
    val width = 400
    val height = 300
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = AndroidPaint()

    // background
    paint.color = android.graphics.Color.rgb(20, 24, 30)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    // draw metallic pipes
    paint.color = android.graphics.Color.rgb(100, 110, 120)
    paint.strokeWidth = 16f
    canvas.drawLine(40f, 120f, 360f, 120f, paint)
    canvas.drawLine(120f, 40f, 120f, 260f, paint)

    // draw engine cylinder layout outlines
    paint.color = android.graphics.Color.rgb(50, 60, 70)
    paint.style = AndroidPaint.Style.STROKE
    paint.strokeWidth = 4f
    canvas.drawRect(80f, 80f, 320f, 220f, paint)

    // draw check oil yellow cap
    paint.color = android.graphics.Color.rgb(255, 179, 0)
    paint.style = AndroidPaint.Style.FILL
    canvas.drawCircle(260f, 130f, 24f, paint)

    // draw engine diagnostic warning glow (leak zones)
    paint.color = android.graphics.Color.rgb(255, 87, 34)
    canvas.drawCircle(130f, 210f, 12f, paint)

    return bitmap
}
