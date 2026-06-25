@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.R
import com.example.data.GarmentScan
import com.example.data.UserProfile
import com.example.ui.viewport.DressingRoomViewport
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.math.roundToInt

// Navigation Targets
object Destinations {
    const val ONBOARDING = "onboarding"
    const val PROFILE = "profile"
    const val CAMERA = "camera"
    const val PROCESSING = "processing"
    const val STU_VIEWPORT = "viewport"
    const val HISTORY = "history"
}

// Global active scan cache to pass data cleanly
object ActiveScanCache {
    var activeImagePath: String = ""
    var activePrompt: String = ""
    var selectedScan: GarmentScan? = null
}

// ─────────────────────────────────────────────────────────────
// 1. ONBOARDING SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.SleekBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // High-fidelity generated hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dressing_room_hero_1782128233577),
                    contentDescription = "Futuristic 3D Virtual Showroom",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Elegant white/slate gradient overlay fading to light background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, com.example.ui.theme.SleekBg),
                                startY = 300f
                            )
                        )
                )
            }

            // Text copy & structure layout
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(
                        containerColor = com.example.ui.theme.SleekBlueBg,
                        contentColor = com.example.ui.theme.SleekBlueText
                    ) {
                        Text("B2C SOLO EDITION", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "DIGITAL\nDRESSING ROOM",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.SleekTextDark,
                    lineHeight = 36.sp,
                )

                Text(
                    text = "High-Fidelity AI Fitting Studio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = com.example.ui.theme.SleekPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Three-pillar benefit layout
                OnboardingFeatureItem(
                    icon = Icons.Default.AccessibilityNew,
                    title = "Localized 3D Avatar Modeling",
                    desc = "Your physical metrics are compiled locally to generate a fully rotatable, accurate body contour representation."
                )

                OnboardingFeatureItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Instant Photographic Scanning",
                    desc = "Place any garment on a flat backdrop and capture it. The AI automatically fits and scales it to your body frame."
                )

                OnboardingFeatureItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Zero-Cloud Privacy Guard",
                    desc = "Your biometric profiles are persisted on-device in specialized secure SQLite models, keeping your data confidential."
                )

                Spacer(Modifier.height(40.dp))

                // Navigation button
                Button(
                    onClick = { navController.navigate(Destinations.PROFILE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.SleekPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("get_started_button")
                ) {
                    Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun OnboardingFeatureItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(com.example.ui.theme.SleekBlueBg, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = com.example.ui.theme.SleekPrimary, modifier = Modifier.size(24.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = com.example.ui.theme.SleekTextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = com.example.ui.theme.SleekTextLight, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}


// ─────────────────────────────────────────────────────────────
// 2. BODY PROFILE SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
fun BodyProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val height by viewModel.heightState.collectAsStateWithLifecycle()
    val chest by viewModel.chestState.collectAsStateWithLifecycle()
    val waist by viewModel.waistState.collectAsStateWithLifecycle()
    val hips by viewModel.hipsState.collectAsStateWithLifecycle()
    val shoulder by viewModel.shoulderState.collectAsStateWithLifecycle()
    val inseam by viewModel.inseamState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BODY DIMENSIONS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = com.example.ui.theme.SleekTextDark) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = com.example.ui.theme.SleekSurface,
                    titleContentColor = com.example.ui.theme.SleekTextDark
                )
            )
        },
        containerColor = com.example.ui.theme.SleekBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.SleekSurface, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, com.example.ui.theme.SleekBorderDark, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = com.example.ui.theme.SleekGreenText, modifier = Modifier.size(24.dp))
                Column {
                    Text("Local Privacy Compliant", color = com.example.ui.theme.SleekTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("These biometric dimensions are only saved locally inside SQLite. Absolutely zero external tracking.", color = com.example.ui.theme.SleekTextLight, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Body visualization diagram
            Text("MANNEQUIN MODEL", color = com.example.ui.theme.SleekTextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // Range sliders for different metrics
            ProfileSlider(
                label = "Total Height",
                value = height,
                min = 120f,
                max = 220f,
                unit = "cm",
                testTag = "height_slider",
                onValueChange = { viewModel.heightState.value = it }
            )

            ProfileSlider(
                label = "Chest Circumference",
                value = chest,
                min = 50f,
                max = 150f,
                unit = "cm",
                testTag = "chest_slider",
                onValueChange = { viewModel.chestState.value = it }
            )

            ProfileSlider(
                label = "Waist Circumference",
                value = waist,
                min = 40f,
                max = 140f,
                unit = "cm",
                testTag = "waist_slider",
                onValueChange = { viewModel.waistState.value = it }
            )

            ProfileSlider(
                label = "Hips Width",
                value = hips,
                min = 50f,
                max = 150f,
                unit = "cm",
                testTag = "hips_slider",
                onValueChange = { viewModel.hipsState.value = it }
            )

            ProfileSlider(
                label = "Shoulder Arc Span",
                value = shoulder,
                min = 30f,
                max = 65f,
                unit = "cm",
                testTag = "shoulder_slider",
                onValueChange = { viewModel.shoulderState.value = it }
            )

            ProfileSlider(
                label = "Leg Inseam",
                value = inseam,
                min = 50f,
                max = 110f,
                unit = "cm",
                testTag = "inseam_slider",
                onValueChange = { viewModel.inseamState.value = it }
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.saveProfile {
                        navController.navigate(Destinations.CAMERA)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.SleekPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_profile_button")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Proceed to Garment Studio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    unit: String,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = com.example.ui.theme.SleekTextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("${value.roundToInt()} $unit", color = com.example.ui.theme.SleekPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value,
            valueRange = min..max,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = com.example.ui.theme.SleekPrimary,
                activeTrackColor = com.example.ui.theme.SleekPrimary,
                inactiveTrackColor = com.example.ui.theme.SleekBorderDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}


// ─────────────────────────────────────────────────────────────
// 3. SMART GARMENT CAMERA
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var forceBypass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.SleekBg)
    ) {
        if (cameraPermissionState.status.isGranted || forceBypass) {
            CameraWithGuideContent(navController = navController, isSimulated = forceBypass || !cameraPermissionState.status.isGranted)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Camera, contentDescription = null, tint = com.example.ui.theme.SleekPrimary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Camera Access Required", color = com.example.ui.theme.SleekTextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "To photograph your clothing items and render them on your avatar, we need permission to use your camera.",
                    color = com.example.ui.theme.SleekTextLight,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.SleekPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("request_camera_perm_button").fillMaxWidth()
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { forceBypass = true },
                    border = BorderStroke(1.dp, com.example.ui.theme.SleekBorderDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("bypass_camera_button").fillMaxWidth()
                ) {
                    Text("Use Demo Scanner (Bypass)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CameraWithGuideContent(navController: NavController, isSimulated: Boolean = false) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember { ImageCapture.Builder().build() }
    var inputPrompt by remember { mutableStateOf("") }

    val previewView = remember { PreviewView(context) }

    if (!isSimulated) {
        LaunchedEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("CameraScreen", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isSimulated) {
            // Fullscreen Camera Preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Simulated live scanning visualizer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                // Background futuristic grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 40.dp.toPx()
                    val cols = (size.width / step).toInt()
                    val rows = (size.height / step).toInt()
                    for (i in 0..cols) {
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.5f),
                            start = Offset(i * step, 0f),
                            end = Offset(i * step, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 0..rows) {
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.5f),
                            start = Offset(0f, i * step),
                            end = Offset(size.width, i * step),
                            strokeWidth = 1f
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Simulated Garment",
                        tint = com.example.ui.theme.SleekPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Demo / Virtual Scanner",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ready to scan garments, segment silhouettes, and perform cloth-matching simulation",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Neon active scan bar moving infinitely
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val scanOffset by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scan_offset"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.005f)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            translationY = size.height * scanOffset
                        }
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF4F46E5),
                                    Color(0xFF818CF8),
                                    Color(0xFF4F46E5),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Alignment Overlay Guide Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Render boundary safe box
            val rectWidth = w * 0.75f
            val rectHeight = h * 0.5f
            val left = (w - rectWidth) / 2f
            val top = (h - rectHeight) / 2.3f

            drawRoundRect(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )

            // Center target indicator
            drawCircle(
                color = Color.Green.copy(alpha = 0.5f),
                radius = 8.dp.toPx(),
                center = Offset(w / 2f, top + rectHeight / 2f)
            )
        }

        // Top Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                    .align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "CENTER YOUR GARMENT",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = { navController.navigate(Destinations.HISTORY) },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                    .align(Alignment.CenterEnd)
                    .testTag("nav_history_button")
            ) {
                Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
            }
        }

        // Bottom input layout & trigger controls
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
                .fillMaxWidth()
                .border(1.dp, com.example.ui.theme.SleekBorderDark, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Describe the item (color, fabric, structure):",
                    color = com.example.ui.theme.SleekTextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("e.g. Classic vintage brown leather jacket", color = com.example.ui.theme.SleekTextMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("garment_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = com.example.ui.theme.SleekBg,
                        unfocusedContainerColor = com.example.ui.theme.SleekBg,
                        disabledContainerColor = com.example.ui.theme.SleekBg,
                        focusedTextColor = com.example.ui.theme.SleekTextDark,
                        unfocusedTextColor = com.example.ui.theme.SleekTextDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val finalPrompt = inputPrompt.ifBlank { "Smart Casual Fit Suit" }
                        val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                        if (isSimulated) {
                            ActiveScanCache.activeImagePath = ""
                            ActiveScanCache.activePrompt = finalPrompt
                            navController.navigate(Destinations.PROCESSING)
                        } else {
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        ActiveScanCache.activeImagePath = file.absolutePath
                                        ActiveScanCache.activePrompt = finalPrompt
                                        navController.navigate(Destinations.PROCESSING)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        // Fallback if hardware trigger crashes on simulators
                                        ActiveScanCache.activeImagePath = ""
                                        ActiveScanCache.activePrompt = finalPrompt
                                        navController.navigate(Destinations.PROCESSING)
                                    }
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.SleekPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("capture_shutter_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Capture & Analyze", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────
// 4. SCANNING / PROCESSING SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
fun ProcessingScreen(navController: NavController, viewModel: ScanViewModel = viewModel()) {
    val state by viewModel.scanState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.processGarmentScan(ActiveScanCache.activeImagePath, ActiveScanCache.activePrompt)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.SleekBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            when (state) {
                is ScanState.Processing -> {
                    CircularProgressIndicator(
                        color = com.example.ui.theme.SleekPrimary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("AI VIRTUAL TRY-ON FITTING", color = com.example.ui.theme.SleekTextDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = "Gemini is performing backdrop segmentation, mapping bone transformations, and computing drape dynamics specifically to your body frame...",
                        color = com.example.ui.theme.SleekTextLight,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(Modifier.height(48.dp))
                    Text(
                        text = "Analyzing: \"${ActiveScanCache.activePrompt}\"",
                        color = com.example.ui.theme.SleekPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
                is ScanState.Success -> {
                    val scan = (state as ScanState.Success).scan
                    LaunchedEffect(scan) {
                        ActiveScanCache.selectedScan = scan
                        viewModel.resetState()
                        navController.navigate(Destinations.STU_VIEWPORT) {
                            popUpTo(Destinations.ONBOARDING)
                        }
                    }
                }
                is ScanState.Error -> {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Drape Simulation Blocked", color = com.example.ui.theme.SleekTextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text((state as ScanState.Error).message, color = com.example.ui.theme.SleekTextLight, fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.resetState()
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.SleekPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Retry")
                    }
                }
                else -> {}
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────
// 5. VIEWPORT / RESULTS SCREEN
// ─────────────────────────────────────────────────────────────
@Composable
fun ViewportResultScreen(navController: NavController, profileViewModel: ProfileViewModel = viewModel()) {
    val scan = ActiveScanCache.selectedScan ?: return

    val height by profileViewModel.heightState.collectAsStateWithLifecycle()
    val chest by profileViewModel.chestState.collectAsStateWithLifecycle()
    val waist by profileViewModel.waistState.collectAsStateWithLifecycle()
    val hips by profileViewModel.hipsState.collectAsStateWithLifecycle()
    val shoulder by profileViewModel.shoulderState.collectAsStateWithLifecycle()
    val inseam by profileViewModel.inseamState.collectAsStateWithLifecycle()

    val dynamicProfile = UserProfile(
        id = 1,
        heightCm = height,
        chestCm = chest,
        waistCm = waist,
        hipsCm = hips,
        shoulderCm = shoulder,
        inseamCm = inseam
    )

    var showAdjustSliders by remember { mutableStateOf(false) }

    // --- FastAPI Fashion AI States ---
    val fashionViewModel: FashionViewModel = viewModel()
    val fashionState by fashionViewModel.fashionState.collectAsStateWithLifecycle()

    var activeViewTab by remember { mutableStateOf(0) } // 0 = 3D Skeleton, 1 = Photorealistic AI
    var bottomCardTab by remember { mutableStateOf(0) } // 0 = 3D Fit Report, 1 = Fashion AI Studio

    var selectedStudioTool by remember { mutableStateOf(0) } // 0 = Virtual Try-On, 1 = Accessory Overlay, 2 = Restyle Canvas, 3 = Pure Generation
    var garmentTypeOption by remember { mutableStateOf("Outfit") } // "Upper_Body", "Lower_Body", "Overall", "Outfit"
    var accessoryTypeOption by remember { mutableStateOf("Watch") } // "Watch", "Hair", "Shoes", "Bag", "Makeup", "Jewelry"
    var stylePromptInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(scan.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 14.sp, color = com.example.ui.theme.SleekTextDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = com.example.ui.theme.SleekTextDark)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAdjustSliders = !showAdjustSliders },
                        modifier = Modifier.testTag("toggle_sliders_button")
                    ) {
                        Icon(
                            imageVector = if (showAdjustSliders) Icons.Default.Close else Icons.Default.Tune,
                            contentDescription = "Morph Avatar Options",
                            tint = if (showAdjustSliders) com.example.ui.theme.SleekPrimary else com.example.ui.theme.SleekTextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = com.example.ui.theme.SleekSurface,
                    titleContentColor = com.example.ui.theme.SleekTextDark
                )
            )
        },
        containerColor = com.example.ui.theme.SleekBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Partition: The glorious interactive Viewports
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            ) {
                if (activeViewTab == 0) {
                    // Standard 3D skeletal tailor viewport
                    DressingRoomViewport(
                        profile = dynamicProfile,
                        garmentType = scan.type,
                        garmentColorHex = scan.colorHex,
                        fitStyle = scan.fitType,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Modern photorealistic generative Canvas preview
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A)) // elegant deep dark background
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val state = fashionState) {
                            is FashionState.Idle -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = com.example.ui.theme.SleekPrimary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "PHOTOREAL GENERATOR IDLE",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Use the 'Fashion AI' tab below to trigger a custom virtual try-on or select stylish watches, shoes, hair styles and bags!",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                            is FashionState.Loading -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator(
                                        color = com.example.ui.theme.SleekPrimary,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(50.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "SYNTHESIZING STYLES...",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Forwarding to Cloudflare AI Worker via FastAPI gateway. Constructing garment drapes & overlaying accessories directly to matches...",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 6.dp)
                                    )
                                }
                            }
                            is FashionState.Success -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = state.imageUrl,
                                        contentDescription = "AI Render Result",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    )

                                    // Clear Render Floating Action button
                                    IconButton(
                                        onClick = { fashionViewModel.resetState() },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reset Style", tint = Color.White)
                                    }

                                    // Informative Bottom Banner
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                            .fillMaxWidth(0.8f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(state.type.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                            }
                                            Text(state.prompt, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                            is FashionState.Error -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "STUDIO FAILED",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = state.message,
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = { fashionViewModel.resetState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.SleekPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Dismiss", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Dual View Switcher Segment Control
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { activeViewTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeViewTab == 0) com.example.ui.theme.SleekPrimary else Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.AccessibilityNew, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("3D Skeleton", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { activeViewTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeViewTab == 1) com.example.ui.theme.SleekPrimary else Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Photoreal AI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Optional floating sliders to morph body outline real-time
                if (showAdjustSliders) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.98f))
                            .border(1.dp, com.example.ui.theme.SleekBorderDark, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("MORPH MODEL IN REAL-TIME:", color = com.example.ui.theme.SleekPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Height: ${height.toInt()}cm", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp)
                                    Slider(
                                        value = height,
                                        valueRange = 130f..220f,
                                        onValueChange = { profileViewModel.heightState.value = it },
                                        colors = SliderDefaults.colors(
                                            thumbColor = com.example.ui.theme.SleekPrimary,
                                            activeTrackColor = com.example.ui.theme.SleekPrimary,
                                            inactiveTrackColor = com.example.ui.theme.SleekBorderDark
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Chest: ${chest.toInt()}cm", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp)
                                    Slider(
                                        value = chest,
                                        valueRange = 60f..140f,
                                        onValueChange = { profileViewModel.chestState.value = it },
                                        colors = SliderDefaults.colors(
                                            thumbColor = com.example.ui.theme.SleekPrimary,
                                            activeTrackColor = com.example.ui.theme.SleekPrimary,
                                            inactiveTrackColor = com.example.ui.theme.SleekBorderDark
                                        )
                                    )
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Waist: ${waist.toInt()}cm", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp)
                                    Slider(
                                        value = waist,
                                        valueRange = 50f..130f,
                                        onValueChange = { profileViewModel.waistState.value = it },
                                        colors = SliderDefaults.colors(
                                            thumbColor = com.example.ui.theme.SleekPrimary,
                                            activeTrackColor = com.example.ui.theme.SleekPrimary,
                                            inactiveTrackColor = com.example.ui.theme.SleekBorderDark
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Shoulder: ${shoulder.toInt()}cm", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp)
                                    Slider(
                                        value = shoulder,
                                        valueRange = 30f..60f,
                                        onValueChange = { profileViewModel.shoulderState.value = it },
                                        colors = SliderDefaults.colors(
                                            thumbColor = com.example.ui.theme.SleekPrimary,
                                            activeTrackColor = com.example.ui.theme.SleekPrimary,
                                            inactiveTrackColor = com.example.ui.theme.SleekBorderDark
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Partition: Details & Generative Controls Card
            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .border(1.dp, com.example.ui.theme.SleekBorderLight, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // bottom controller tab bar
                    TabRow(
                        selectedTabIndex = bottomCardTab,
                        containerColor = Color.Transparent,
                        contentColor = com.example.ui.theme.SleekPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = bottomCardTab == 0,
                            onClick = { bottomCardTab = 0 },
                            text = { Text("3D Fit Report", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                            icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = bottomCardTab == 1,
                            onClick = { bottomCardTab = 1 },
                            text = { Text("Fashion AI Studio", fontSize = 11.sp, fontWeight = FontWeight.Black) },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (bottomCardTab == 0) {
                            // Standard 3D tailored evaluatives
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = scan.type.uppercase(),
                                        color = com.example.ui.theme.SleekPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Fit Suitability",
                                        color = com.example.ui.theme.SleekTextDark,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Fit Style Tag
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (scan.fitType) {
                                                "Slim" -> Color(0xFFFEE2E2)
                                                "Oversized" -> Color(0xFFDBEAFE)
                                                else -> Color(0xFFD1FAE5)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when (scan.fitType) {
                                                "Slim" -> Color(0xFFFCA5A5)
                                                "Oversized" -> Color(0xFF93C5FD)
                                                else -> Color(0xFF6EE7B7)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = scan.fitType.uppercase(),
                                        color = when (scan.fitType) {
                                            "Slim" -> Color(0xFFB91C1C)
                                            "Oversized" -> Color(0xFF1D4ED8)
                                            else -> Color(0xFF047857)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "AI Drape & Fabric Evaluation",
                                color = com.example.ui.theme.SleekTextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = scan.fittingAdvice,
                                color = com.example.ui.theme.SleekTextMedium,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        } else {
                            // FAST-API ACTIVE Generative Customizer Pane
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                var showConnectionSettings by remember { mutableStateOf(false) }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showConnectionSettings = !showConnectionSettings },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Settings, contentDescription = null, tint = com.example.ui.theme.SleekPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("FastAPI Server Settings", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Icon(
                                            imageVector = if (showConnectionSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = com.example.ui.theme.SleekTextMedium,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (showConnectionSettings) {
                                        Spacer(Modifier.height(8.dp))
                                        
                                        var currentUrlInput by remember { mutableStateOf(com.example.data.FashionAiClient.baseUrl) }
                                        var currentKeyInput by remember { mutableStateOf(com.example.data.FashionAiClient.apiKey) }

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Column {
                                                Text("FastAPI Endpoint Base URL:", color = com.example.ui.theme.SleekTextMedium, fontSize = 10.sp)
                                                OutlinedTextField(
                                                    value = currentUrlInput,
                                                    onValueChange = { 
                                                        currentUrlInput = it
                                                        com.example.data.FashionAiClient.baseUrl = it
                                                    },
                                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = com.example.ui.theme.SleekPrimary,
                                                        unfocusedBorderColor = com.example.ui.theme.SleekBorderDark
                                                    )
                                                )
                                            }

                                            Column {
                                                Text("X-API-Key Header Value:", color = com.example.ui.theme.SleekTextMedium, fontSize = 10.sp)
                                                OutlinedTextField(
                                                    value = currentKeyInput,
                                                    onValueChange = { 
                                                        currentKeyInput = it
                                                        com.example.data.FashionAiClient.apiKey = it
                                                    },
                                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = com.example.ui.theme.SleekPrimary,
                                                        unfocusedBorderColor = com.example.ui.theme.SleekBorderDark
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "SELECT AI GENERATION TOOL",
                                    color = com.example.ui.theme.SleekPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )

                                // Generation Tool Selection Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Virtual Try-On", "Accessory Overlay", "Restyle Image", "Text-To-Garment").forEachIndexed { index, toolName ->
                                        FilterChip(
                                            selected = selectedStudioTool == index,
                                            onClick = {
                                                selectedStudioTool = index
                                                // auto load corresponding starter prompts to make testing quick
                                                stylePromptInput = when (index) {
                                                    0 -> "a premium designer jacket in rich chocolate brown, modern look"
                                                    1 -> "luxury golden chronograph watch with steel links"
                                                    2 -> "futuristic holographic cyberpunk outerwear jacket"
                                                    else -> "A sleek minimalist knitted dress in light beige, professional model portrait"
                                                }
                                            },
                                            label = { Text(toolName, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = com.example.ui.theme.SleekPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                // Sub options depending on selected tools
                                if (selectedStudioTool == 0) {
                                    // Try-On Garment Type Option picker
                                    Column {
                                        Text("Garment Placement Placement:", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Outfit", "Upper_Body", "Lower_Body", "Overall").forEach { type ->
                                                FilterChip(
                                                    selected = garmentTypeOption == type,
                                                    onClick = { garmentTypeOption = type },
                                                    label = { Text(type.replace("_", " "), fontSize = 9.sp) }
                                                )
                                            }
                                        }
                                    }
                                } else if (selectedStudioTool == 1) {
                                    // Accessory type picker
                                    Column {
                                        Text("Accessory Styling Category:", color = com.example.ui.theme.SleekTextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Watch", "Shoes", "Bag", "Hair", "Makeup", "Jewelry").forEach { acc ->
                                                FilterChip(
                                                    selected = accessoryTypeOption == acc,
                                                    onClick = {
                                                        accessoryTypeOption = acc
                                                        // adjust presets matching details
                                                        stylePromptInput = when (acc) {
                                                            "Watch" -> "minimalist elegant stainless steel ladies watch"
                                                            "Shoes" -> "chic high-heeled velvet slip-on shoes in deep burgundy"
                                                            "Bag" -> "designer leather quilted messenger shoulder handbag with gold accents"
                                                            "Hair" -> "beautiful modern wavy bob hairstyle styled blonde highlighted gloss"
                                                            "Makeup" -> "premium natural makeup style soft rose lipstick smooth skin"
                                                            else -> "delicate modern platinum pendant necklace with subtle shine"
                                                        }
                                                    },
                                                    label = { Text(acc, fontSize = 9.sp) }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Text entry box for custom prompt
                                Column {
                                    Text(
                                        text = "AI DESIGN PROMPT:",
                                        color = com.example.ui.theme.SleekTextDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    OutlinedTextField(
                                        value = stylePromptInput,
                                        onValueChange = { stylePromptInput = it },
                                        placeholder = { Text("Describe the look (e.g. materials, color, style aesthetics)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = com.example.ui.theme.SleekPrimary,
                                            unfocusedBorderColor = com.example.ui.theme.SleekBorderDark
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                    )
                                }

                                // Floating quick recommendation chips
                                Column {
                                    Text("Style Recommendations:", color = com.example.ui.theme.SleekTextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 2.dp)
                                    ) {
                                        val suggestions = when (selectedStudioTool) {
                                            0 -> listOf("crimson winter puffer coat", "casual forest green linen shirt", "dark denim trousers")
                                            1 -> listOf("silver chain link luxury watch", "glossy black leather combat boots", "vintage brown leather tote bag", "sleek blonde braids hair styling")
                                            2 -> listOf("charcoal tweed structural blazer", "bohemian floral resort dress")
                                            else -> listOf("pure cashmere designer autumn turtleneck")
                                        }
                                        suggestions.forEach { desc ->
                                            Button(
                                                onClick = { stylePromptInput = desc },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = com.example.ui.theme.SleekTextDark),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text(desc, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Active Studio trigger action button
                                Button(
                                    onClick = {
                                        activeViewTab = 1 // redirect camera to photoreal partition automatically!
                                        val finalPrompt = stylePromptInput.ifBlank { "custom stylish looks" }
                                        val imageToUse = ActiveScanCache.activeImagePath

                                        when (selectedStudioTool) {
                                            0 -> fashionViewModel.runVirtualTryOn(imageToUse, finalPrompt, garmentTypeOption)
                                            1 -> fashionViewModel.runAccessoryOverlay(imageToUse, finalPrompt, accessoryTypeOption)
                                            2 -> fashionViewModel.runImg2Img(imageToUse, finalPrompt)
                                            3 -> fashionViewModel.runTextToImage(finalPrompt)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = com.example.ui.theme.SleekPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Synthesize Selected Style", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { navController.navigate(Destinations.ONBOARDING) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.SleekPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("action_studio_reset")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Finish Styling Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────
// 6. SCANNED GARMENTS HISTORY
// ─────────────────────────────────────────────────────────────
@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = viewModel()) {
    val scans by viewModel.scansFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FITTING ARCHIVE", fontWeight = FontWeight.Black, fontSize = 15.sp, color = com.example.ui.theme.SleekTextDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = com.example.ui.theme.SleekTextDark)
                    }
                },
                actions = {
                    if (scans.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearScans() },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color(0xFFEF4444))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = com.example.ui.theme.SleekSurface,
                    titleContentColor = com.example.ui.theme.SleekTextDark
                )
            )
        },
        containerColor = com.example.ui.theme.SleekBg
    ) { padding ->
        if (scans.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AllInbox,
                    contentDescription = null,
                    tint = com.example.ui.theme.SleekBorderDark,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("No Scans Saved Yet", color = com.example.ui.theme.SleekTextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Any garment photographs you take and try on will be compiled and shown here for immediate side-by-side fit review.",
                    color = com.example.ui.theme.SleekTextLight,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("history_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scans, key = { it.id }) { item ->
                    HistoryCard(
                        scan = item,
                        onSelect = {
                            ActiveScanCache.selectedScan = item
                            navController.navigate(Destinations.STU_VIEWPORT)
                        },
                        onDelete = { viewModel.deleteScan(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCard(scan: GarmentScan, onSelect: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, com.example.ui.theme.SleekBorderLight, shape = RoundedCornerShape(16.dp))
            .testTag("history_card_${scan.id}"),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Colored swatch to signify scanned color
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(scan.colorHex))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (scan.type.lowercase()) {
                        "jeans", "trousers" -> Icons.Default.DryCleaning
                        else -> Icons.Default.Checkroom
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.name,
                    color = com.example.ui.theme.SleekTextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = scan.type.uppercase(),
                        color = com.example.ui.theme.SleekPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = com.example.ui.theme.SleekBorderDark
                    )
                    Text(
                        text = scan.fitType.uppercase(),
                        color = when (scan.fitType) {
                            "Slim" -> Color(0xFFB91C1C)
                            "Oversized" -> Color(0xFF1D4ED8)
                            else -> Color(0xFF047857)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_scan_${scan.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete from history",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}
