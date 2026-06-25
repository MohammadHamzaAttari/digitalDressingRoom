package com.example.ui.viewport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import kotlin.math.max
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DressingRoomViewport(
    profile: UserProfile,
    garmentType: String,
    garmentColorHex: String,
    fitStyle: String, // "Slim", "Standard", "Oversized"
    modifier: Modifier = Modifier
) {
    // Rotation state in degrees
    var rotationY by remember { mutableStateOf(20f) }
    var rotationX by remember { mutableStateOf(5f) }

    // Display modes: "Solid Match", "Heatmap Fit", "Wireframe Skeleton"
    var displayMode by remember { mutableStateOf("Solid Match") }
    // Layers toggle
    var showBody by remember { mutableStateOf(true) }
    var showGarment by remember { mutableStateOf(true) }

    // Parse color or fallback
    val primaryColorInt = try {
        android.graphics.Color.parseColor(garmentColorHex)
    } catch (e: Exception) {
        0xFF805AD5.toInt() // beautiful fashion purple
    }

    val fitHex = when (displayMode) {
        "Heatmap Fit" -> {
            // Heatmap color code: green is excellent fit, red is tight, blue is loose
            when (fitStyle) {
                "Slim" -> 0xFFE53E3E.toInt() // red (snug/tight)
                "Oversized" -> 0xFF3182CE.toInt() // blue (roomy/loose)
                else -> 0xFF38A169.toInt() // green (perfect fit ratio)
            }
        }
        else -> primaryColorInt
    }

    val lightSourceDir = Vector3D(0.5f, -0.6f, -0.8f) // directional studio light source

    // Generate mesh arrays inside Compose side effects or simple state calculations
    val allPolygons = remember(profile, garmentType, fitHex, showBody, showGarment, displayMode) {
        val list = mutableListOf<Polygon3D>()
        if (showBody) {
            list.addAll(AvatarGenerator.generateAvatarMesh(profile))
        }
        if (showGarment) {
            list.addAll(AvatarGenerator.generateGarmentMesh(profile, garmentType, fitHex))
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(com.example.ui.theme.SleekCodeBg) // Sleek Code style dark background
    ) {
        // Controls Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Select Button
            OutlinedButton(
                onClick = {
                    displayMode = when (displayMode) {
                        "Solid Match" -> "Heatmap Fit"
                        "Heatmap Fit" -> "Wireframe Skeleton"
                        else -> "Solid Match"
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SleekCodeBorder),
                modifier = Modifier.testTag("toggle_mode_button")
            ) {
                Icon(
                    Icons.Default.Cached,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(displayMode, fontSize = 12.sp, color = Color.White)
            }

            // Layer Toggle Buttons
            Row {
                IconButton(
                    onClick = { showBody = !showBody },
                    modifier = Modifier.testTag("toggle_body_visibility")
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Toggle Body Avatar",
                        tint = if (showBody) com.example.ui.theme.SleekGreenText else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showGarment = !showGarment },
                    modifier = Modifier.testTag("toggle_garment_visibility")
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Toggle Garment Overlay",
                        tint = if (showGarment) com.example.ui.theme.SleekPrimary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // The interactive 3D Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .semantics {
                    contentDescription = "3D avatar try-on model of $garmentType. Drag to orbit 360 degrees."
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, dragAmount, _, _ ->
                        rotationY = (rotationY + dragAmount.x * 0.4f) % 360f
                        rotationX = (rotationX - dragAmount.y * 0.4f).coerceIn(-45f, 45f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f + 50f
                val scaleFactor = size.height / 350f // auto scaling to available container

                // Rotations mapped to radians
                val angleRadY = Math.toRadians(rotationY.toDouble()).toFloat()
                val angleRadX = Math.toRadians(rotationX.toDouble()).toFloat()

                // 1. Transform & Shade Polygons
                val transformedPolygons = allPolygons.map { poly ->
                    val rotatedVertices = poly.vertices.map { vertex ->
                        // Translate to center modeling pivot, scale, rotate, then shift
                        val scaled = vertex.scale(scaleFactor, scaleFactor, scaleFactor)
                        // Dynamic pivot center offset
                        val offset = scaled.translate(0f, -size.height * 0.3f, 0f)
                        offset.rotateY(angleRadY).rotateX(angleRadX)
                    }
                    poly.copy(vertices = rotatedVertices)
                }

                // 2. Depth Sorting (Painter's Algorithm: Furthest first)
                val sortedPolygons = transformedPolygons.sortedBy { it.averageZ() }

                // 3. Render sorted faces
                for (poly in sortedPolygons) {
                    if (poly.vertices.size < 3) continue

                    // Shading logic
                    val normal = poly.normal()
                    val dot = normal.x * lightSourceDir.x + normal.y * lightSourceDir.y + normal.z * lightSourceDir.z
                    val intensity = 0.4f + 0.6f * max(0f, dot)

                    // Retrieve original colors and apply lighting modulation
                    val curColor = poly.color
                    val a = (curColor shr 24) and 0xFF
                    val r = (((curColor shr 16) and 0xFF) * intensity).toInt().coerceIn(0, 255)
                    val g = (((curColor shr 8) and 0xFF) * intensity).toInt().coerceIn(0, 255)
                    val b = ((curColor and 0xFF) * intensity).toInt().coerceIn(0, 255)
                    val finalColor = Color(r, g, b, a)

                    val path = Path().apply {
                        moveTo(cx + poly.vertices[0].x, cy - poly.vertices[0].y)
                        for (i in 1 until poly.vertices.size) {
                            lineTo(cx + poly.vertices[i].x, cy - poly.vertices[i].y)
                        }
                        close()
                    }

                    if (displayMode == "Wireframe Skeleton") {
                        drawPath(
                            path = path,
                            color = if (poly.label == "Garment") Color(0xFFC084FC) else Color(0xFF94A3B8),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    } else {
                        drawPath(
                            path = path,
                            color = finalColor
                        )
                    }
                }

                // Draw standard grid floor shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = 80f * scaleFactor,
                    center = Offset(cx, cy + 10f)
                )

                // 4. Draw high-fidelity tailor measurement loops and callouts!
                // Project 3D model coordinates to 2D Canvas space in real-time
                fun projectPoint(v: Vector3D): Offset {
                    val scaled = v.scale(scaleFactor, scaleFactor, scaleFactor)
                    val offset = scaled.translate(0f, -size.height * 0.3f, 0f)
                    val rotated = offset.rotateY(angleRadY).rotateX(angleRadX)
                    return Offset(cx + rotated.x, cy - rotated.y)
                }

                val heightScale = profile.heightCm / 170f
                val chestWidth = (profile.chestCm / 95f) * 45f
                val waistWidth = (profile.waistCm / 80f) * 38f
                val hipsWidth = (profile.hipsCm / 95f) * 46f
                val shoulderWidth = (profile.shoulderCm / 42f) * 50f
                val legLength = (profile.inseamCm / 75f) * 75f

                val hipsY = legLength
                val waistY = hipsY + (20f * heightScale)
                val chestY = waistY + (22f * heightScale)
                val shoulderY = chestY + (12f * heightScale)
                val headY = shoulderY + (15f * heightScale)

                val tapeColor = Color(0xFFFBBF24) // Elegant golden tailor tape

                fun drawTailorTape(y: Float, rx: Float, rz: Float, label: String, value: Float, color: Color = tapeColor) {
                    val ringPoints = mutableListOf<Offset>()
                    val stepsForTape = 32
                    for (i in 0..stepsForTape) {
                        val angle = (2 * Math.PI * i / stepsForTape).toFloat()
                        // Slightly larger than body radius to envelope it beautifully
                        val v = Vector3D((rx + 0.6f) * cos(angle), y, (rz + 0.6f) * sin(angle))
                        ringPoints.add(projectPoint(v))
                    }

                    // Draw dashed loop
                    for (i in 0 until ringPoints.size - 1) {
                        drawLine(
                            color = color,
                            start = ringPoints[i],
                            end = ringPoints[i + 1],
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }

                    // Find a perfect index on the front-right for the leader callout
                    val leaderIndex = (stepsForTape * 0.125f).toInt() 
                    val anchor = ringPoints[leaderIndex]
                    
                    val offsetDirection = if (anchor.x > cx) 1f else -1f
                    val destX = anchor.x + (25.dp.toPx() * offsetDirection)
                    val destY = anchor.y - 12.dp.toPx()
                    val lineEndX = destX + (40.dp.toPx() * offsetDirection)

                    // Draw thin pointer lines
                    drawLine(
                        color = color.copy(alpha = 0.8f),
                        start = anchor,
                        end = Offset(destX, destY),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = color.copy(alpha = 0.8f),
                        start = Offset(destX, destY),
                        end = Offset(lineEndX, destY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw custom tag label
                    val fontSizePx = 10.sp.toPx()
                    val textPaint = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = fontSizePx
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                        setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
                    }

                    val tagText = "$label: ${value.toInt()} cm"
                    val textWidth = textPaint.measureText(tagText)
                    val textX = if (offsetDirection > 0) destX + 4.dp.toPx() else destX - textWidth - 4.dp.toPx()
                    val textY = destY - 4.dp.toPx()

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            tagText,
                            textX,
                            textY,
                            textPaint
                        )
                    }
                }

                // Call for Shoulders, Chest, Waist, and Hips
                drawTailorTape(shoulderY, shoulderWidth / 2f, (shoulderWidth * 0.7f) / 2f, "SHOULDER", profile.shoulderCm)
                drawTailorTape(chestY, chestWidth / 2f, (chestWidth * 0.78f) / 2f, "CHEST", profile.chestCm)
                drawTailorTape(waistY, waistWidth / 2f, (waistWidth * 0.75f) / 2f, "WAIST", profile.waistCm)
                drawTailorTape(hipsY, hipsWidth / 2f, (hipsWidth * 0.72f) / 2f, "HIPS", profile.hipsCm)

                // Draw vertical Height ruler on left side
                val heightColor = Color(0xFF60A5FA) // couture baby blue
                val footerY = projectPoint(Vector3D(0f, 0f, 0f)).y
                val headerY = projectPoint(Vector3D(0f, headY + 16f * heightScale, 0f)).y
                val barX = cx - 110.dp.toPx()

                // Draw main vertical axis line
                drawLine(
                    color = heightColor.copy(alpha = 0.5f),
                    start = Offset(barX, footerY),
                    end = Offset(barX, headerY),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Top tick
                drawLine(
                    color = heightColor.copy(alpha = 0.5f),
                    start = Offset(barX - 8.dp.toPx(), headerY),
                    end = Offset(barX + 8.dp.toPx(), headerY),
                    strokeWidth = 2.dp.toPx()
                )
                // Bottom tick
                drawLine(
                    color = heightColor.copy(alpha = 0.5f),
                    start = Offset(barX - 8.dp.toPx(), footerY),
                    end = Offset(barX + 8.dp.toPx(), footerY),
                    strokeWidth = 2.dp.toPx()
                )

                val heightPaint = android.graphics.Paint().apply {
                    this.color = 0xFF93C5FD.toInt()
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                    setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
                }

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "HEIGHT: ${profile.heightCm.toInt()} cm",
                        barX + 8.dp.toPx(),
                        (footerY + headerY) / 2f + 3.dp.toPx(),
                        heightPaint
                    )
                }
            }

            // Legend / HUD indicator overlays
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                Text(
                    text = "3D VIEWPORT",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Orbit: Y=${rotationY.toInt()}° X=${rotationX.toInt()}°",
                    color = Color.White,
                    fontSize = 11.sp
                )
                if (displayMode == "Heatmap Fit") {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(Color(0xFFE53E3E))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Tight", color = Color.White, fontSize = 9.sp)
                        Spacer(Modifier.width(8.dp))
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(Color(0xFF38A169))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Perfect", color = Color.White, fontSize = 9.sp)
                        Spacer(Modifier.width(8.dp))
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(Color(0xFF3182CE))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Loose", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
