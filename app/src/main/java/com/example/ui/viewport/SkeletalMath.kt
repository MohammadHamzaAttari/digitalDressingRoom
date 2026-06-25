package com.example.ui.viewport

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.log
import kotlin.math.tan
import kotlin.math.acos
import kotlin.math.asin
import com.example.data.UserProfile


data class Vector3D(val x: Float, val y: Float, val z: Float) {
    fun rotateY(angleRad: Float): Vector3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3D(
            x = x * cosA - z * sinA,
            y = y,
            z = x * sinA + z * cosA
        )
    }

    fun rotateX(angleRad: Float): Vector3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3D(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }

    fun scale(sx: Float, sy: Float, sz: Float): Vector3D {
        return Vector3D(x * sx, y * sy, z * sz)
    }

    fun translate(tx: Float, ty: Float, tz: Float): Vector3D {
        return Vector3D(x + tx, y + ty, z + tz)
    }
}

// Skin tone extraction and adaptation functions
fun extractSkinToneComponents(primaryColorInt: Int): SkinToneComponents {
    val red = (primaryColorInt shr 16) and 0xFF
    val green = (primaryColorInt shr 8) and 0xFF
    val blue = primaryColorInt and 0xFF

    // Convert to HSV for better skin tone analysis
    val (h, s, v) = rgbToHsv(red, green, blue)

    // Extract skin tone characteristics
    val lightness = v
    val saturation = s
    val hue = h

    // Calculate skin tone adjustments
    val warmth = calculateWarmth(hue, lightness)
    val depth = calculateDepth(blue, lightness)
    val undertones = determineUndertones(hue)

    return SkinToneComponents(
        hue = hue,
        saturation = saturation,
        value = lightness,
        warmth = warmth,
        depth = depth,
        undertones = undertones
    )
}

fun rgbToHsv(r: Int, g: Int, b: Int): Triple<Float, Float, Float> {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f

    val max = maxOf(rf, gf, bf)
    val min = minOf(rf, gf, bf)
    val delta = max - min

    val h = when (delta) {
        0f -> 0f
        else -> {
            when (max) {
                rf -> ((gf - bf) / delta) % 6f
                gf -> ((bf - rf) / delta) + 2f
                else -> ((rf - gf) / delta) + 4f
            } * 60f
        }
    }

    val s = if (max == 0f) 0f else (delta / max)
    val v = max

    return Triple(h, s, v)
}

fun calculateWarmth(hue: Float, value: Float): Float {
    // Warm tones are in the yellow/orange range (20-40 degrees)
    val warmthRange = when (hue) {
        in 20f..40f -> 1f
        in 10f..20f -> 0.8f
        in 40f..60f -> 0.5f
        in 0f..10f -> 0.3f
        in 280f..360f -> 0.4f
        else -> 0.2f
    }
    return warmthRange * (value / 255f)
}

fun calculateDepth(blue: Int, value: Float): Float {
    // Deeper skin has more blue component and higher value
    val blueFactor = blue.toFloat() / 255f
    val depth = (blueFactor * 0.6f) + (value / 255f * 0.4f)
    return depth.coerceIn(0f, 1f)
}

fun determineUndertones(hue: Float): String {
    return when (hue) {
        in 0f..30f -> "cool"
        in 30f..90f -> "neutral"
        in 90f..150f -> "warm"
        in 150f..210f -> "neutral"
        in 210f..270f -> "cool"
        else -> "neutral"
    }
}

fun interpolateBodyColor(baseColor: Int, garmentType: String): Int {
    val baseRed = (baseColor shr 16) and 0xFF
    val baseGreen = (baseColor shr 8) and 0xFF
    val baseBlue = baseColor and 0xFF

    val hue = atan2(baseBlue.toFloat() - baseGreen.toFloat(), baseRed.toFloat() - baseGreen.toFloat()) * 180f / PI.toFloat()

    val adjustmentFactor = when (garmentType.lowercase()) {
        "jacket", "t-shirt", "shirt" -> 0.1f
        "jeans", "trousers", "pants" -> 0.2f
        "dress" -> 0.15f
        else -> 0.1f
    }

    val newRed = (baseRed * (1 - adjustmentFactor) + 255 * adjustmentFactor).toInt().coerceIn(0, 255)
    val newGreen = (baseGreen * (1 - adjustmentFactor) + 220 * adjustmentFactor).toInt().coerceIn(0, 255)
    val newBlue = (baseBlue * (1 - adjustmentFactor) + 200 * adjustmentFactor).toInt().coerceIn(0, 255)

    return AndroidColor.rgb(newRed, newGreen, newBlue)
}

// Skin tone components data class
data class SkinToneComponents(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val warmth: Float,
    val depth: Float,
    val undertones: String
)
data class Polygon3D(
    val vertices: List<Vector3D>,
    val color: Int, // Hex ARGB color
    val label: String = "" // e.g. "Body", "Garment"
) {
    // Average Z depth of rotated vertices to perform painter's algorithm
    fun averageZ(): Float {
        if (vertices.isEmpty()) return 0f
        return vertices.map { it.z }.sum() / vertices.size
    }

    // Normal vector calculation of the face (for flat/diffuse shading)
    fun normal(): Vector3D {
        if (vertices.size < 3) return Vector3D(0f, 1f, 0f)
        val v0 = vertices[0]
        val v1 = vertices[1]
        val v2 = vertices[2]

        // Vector v1 - v0
        val ax = v1.x - v0.x
        val ay = v1.y - v0.y
        val az = v1.z - v0.z

        // Vector v2 - v0
        val bx = v2.x - v0.x
        val by = v2.y - v0.y
        val bz = v2.z - v0.z

        // Cross product
        val nx = ay * bz - az * by
        val ny = az * bx - ax * bz
        val nz = ax * by - ay * bx

        val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
        return if (len > 0f) Vector3D(nx / len, ny / len, nz / len) else Vector3D(0f, 1f, 0f)
    }
}

object AvatarGenerator {
    // Generates a 3D avatar mesh based on user metrics
    fun generateAvatarMesh(profile: UserProfile): List<Polygon3D> {
        val polygons = mutableListOf<Polygon3D>()

        // 1. Structural scaling coefficients relative to average template (170cm)
        val heightScale = profile.heightCm / 170f
        val chestWidth = (profile.chestCm / 95f) * 45f
        val waistWidth = (profile.waistCm / 80f) * 38f
        val hipsWidth = (profile.hipsCm / 95f) * 46f
        val shoulderWidth = (profile.shoulderCm / 42f) * 50f
        val legLength = (profile.inseamCm / 75f) * 75f

        // Height distribution of the body elements (root is at ground y = 0)
        val hipsY = legLength
        val waistY = hipsY + (20f * heightScale)
        val chestY = waistY + (22f * heightScale)
        val shoulderY = chestY + (12f * heightScale)
        val headY = shoulderY + (15f * heightScale)

        // Body skin color (premium champagne-bronze satin mannequin look)
        val skinColor = 0xFFD1C7BD.toInt() 

        // Function to create a 16-vertex smooth ring for realistic curves
        fun createRing(y: Float, rx: Float, rz: Float): List<Vector3D> {
            val ring = mutableListOf<Vector3D>()
            val steps = 16
            for (i in 0 until steps) {
                val angle = (2 * Math.PI * i / steps).toFloat()
                ring.add(Vector3D(rx * cos(angle), y, rz * sin(angle)))
            }
            return ring
        }

        // Generate rings for Pelvis/Hips, Waist, Chest, Shoulders/Upper-Chest, and Neck Base
        val ringHips = createRing(hipsY, hipsWidth / 2f, (chipsDepth(hipsWidth)) / 2f)
        val ringWaist = createRing(waistY, waistWidth / 2f, (waistWidth * 0.75f) / 2f)
        val ringChest = createRing(chestY, chestWidth / 2f, (chestWidth * 0.78f) / 2f)
        val ringShoulders = createRing(shoulderY, shoulderWidth / 2f, (shoulderWidth * 0.7f) / 2f)
        val ringNeckBase = createRing(shoulderY + 4f * heightScale, 9f * heightScale, 9f * heightScale)

        // Function to skin two rings (join them with side polygons)
        fun linkRings(ring1: List<List<Vector3D>>, color: Int, label: String) {
            for (r in 0 until ring1.size - 1) {
                val lower = ring1[r]
                val upper = ring1[r + 1]
                val size = lower.size
                for (i in 0 until size) {
                    val next = (i + 1) % size
                    polygons.add(
                        Polygon3D(
                            vertices = listOf(lower[i], lower[next], upper[next], upper[i]),
                            color = color,
                            label = label
                        )
                    )
                }
            }
        }

        // Link torso rings sequentially to build smooth body profile
        linkRings(listOf(ringHips, ringWaist, ringChest, ringShoulders, ringNeckBase), skinColor, "Body")

        // 2. Generate head (a beautifully rounded spherical ellipsoid mannequin head)
        val headR = 12f * heightScale
        val latSteps = 6
        val lonSteps = 12
        val headCenterY = headY + 5f * heightScale
        
        val headVertices = Array(latSteps) { lat ->
            val phi = (Math.PI * lat / (latSteps - 1)).toFloat() // 0 (top) to PI (bottom)
            val hY = headCenterY + headR * 1.3f * cos(phi)
            val r = headR * sin(phi)
            
            Array(lonSteps) { lon ->
                val theta = (2 * Math.PI * lon / lonSteps).toFloat()
                val rx = r * 0.95f
                val rz = r * 1.15f
                Vector3D(rx * cos(theta), hY, rz * sin(theta))
            }
        }
        
        // Link sphere layers
        for (lat in 0 until latSteps - 1) {
            for (lon in 0 until lonSteps) {
                val nextLon = (lon + 1) % lonSteps
                val v1 = headVertices[lat][lon]
                val v2 = headVertices[lat][nextLon]
                val v3 = headVertices[lat + 1][nextLon]
                val v4 = headVertices[lat + 1][lon]
                polygons.add(Polygon3D(listOf(v1, v2, v3, v4), skinColor, "Body"))
            }
        }

        // Tapered Cylinder generator (12 steps for perfectly smooth organic limbs)
        fun makeTaperedCylinder(
            p0: Vector3D, 
            p1: Vector3D, 
            r0: Float, 
            r1: Float, 
            col: Int, 
            lbl: String, 
            steps: Int = 12
        ) {
            val vRing0 = mutableListOf<Vector3D>()
            val vRing1 = mutableListOf<Vector3D>()
            for (i in 0 until steps) {
                val angle = (2 * Math.PI * i / steps).toFloat()
                val dx = cos(angle)
                val dz = sin(angle)
                vRing0.add(Vector3D(p0.x + r0 * dx, p0.y, p0.z + r0 * dz))
                vRing1.add(Vector3D(p1.x + r1 * dx, p1.y, p1.z + r1 * dz))
            }
            for (i in 0 until steps) {
                val next = (i + 1) % steps
                polygons.add(Polygon3D(listOf(vRing0[i], vRing0[next], vRing1[next], vRing1[i]), col, lbl))
            }
        }

        // 3. Generate legs (articulated into thigh and calf with organic knee taper)
        val leftHip = Vector3D(-hipsWidth * 0.22f, hipsY - 2f, 0f)
        val rightHip = Vector3D(hipsWidth * 0.22f, hipsY - 2f, 0f)
        
        val leftKnee = Vector3D(-hipsWidth * 0.22f, hipsY * 0.5f, 0f)
        val rightKnee = Vector3D(hipsWidth * 0.22f, hipsY * 0.5f, 0f)
        
        val leftAnkle = Vector3D(-hipsWidth * 0.22f, 4f, 0f)
        val rightAnkle = Vector3D(hipsWidth * 0.22f, 4f, 0f)

        // Upper legs (Thighs: 13f down to 8.5f)
        makeTaperedCylinder(leftKnee, leftHip, 8.5f, 13f, skinColor, "Body")
        makeTaperedCylinder(rightKnee, rightHip, 8.5f, 13f, skinColor, "Body")
        
        // Lower legs (Calves: 8.5f down to 5.8f ankle)
        makeTaperedCylinder(leftAnkle, leftKnee, 5.8f, 8.5f, skinColor, "Body")
        makeTaperedCylinder(rightAnkle, rightKnee, 5.8f, 8.5f, skinColor, "Body")

        // Wedge foot/shoes shapes (prevents legs from looking abruptly cut off)
        fun makeFoot(ankleNode: Vector3D) {
            val steps = 8
            val midPoints = mutableListOf<Vector3D>()
            val toePoints = mutableListOf<Vector3D>()
            val baseLength = 14f
            val baseWidth = 6.5f
            
            for (i in 0 until steps) {
                val angle = (2 * Math.PI * i / steps).toFloat()
                val fwdZ = if (cos(angle) > 0) baseLength * cos(angle) else baseLength * 0.4f * cos(angle)
                val latX = baseWidth * sin(angle)
                midPoints.add(Vector3D(ankleNode.x + latX, 1.2f, fwdZ + 2f))
                toePoints.add(Vector3D(ankleNode.x + latX * 0.6f, 0f, fwdZ * 1.25f + 3f))
            }
            
            for (i in 0 until steps) {
                val next = (i + 1) % steps
                polygons.add(Polygon3D(listOf(midPoints[i], midPoints[next], toePoints[next], toePoints[i]), skinColor, "Body"))
            }
        }
        makeFoot(leftAnkle)
        makeFoot(rightAnkle)

        // 4. Generate arms (bicep & forearm taper blocks)
        val leftShoulderNode = Vector3D(-shoulderWidth * 0.5f, shoulderY, -0.5f)
        val rightShoulderNode = Vector3D(shoulderWidth * 0.5f, shoulderY, -0.5f)
        
        val leftElbow = Vector3D(-shoulderWidth * 0.62f, shoulderY - (18f * heightScale), -1.2f)
        val rightElbow = Vector3D(shoulderWidth * 0.62f, shoulderY - (18f * heightScale), -1.2f)
        
        val leftWrist = Vector3D(-shoulderWidth * 0.68f, hipsY + (6f * heightScale), -2.2f)
        val rightWrist = Vector3D(shoulderWidth * 0.68f, hipsY + (6f * heightScale), -2.2f)

        // Upper arms (Shoulder to Elbow: 7.5f down to 5.5f)
        makeTaperedCylinder(leftElbow, leftShoulderNode, 5.5f, 7.5f, skinColor, "Body")
        makeTaperedCylinder(rightElbow, rightShoulderNode, 5.5f, 7.5f, skinColor, "Body")
        
        // Forearms (Elbow to Wrist: 5.5f down to 4.2f)
        makeTaperedCylinder(leftWrist, leftElbow, 4.2f, 5.5f, skinColor, "Body")
        makeTaperedCylinder(rightWrist, rightElbow, 4.2f, 5.5f, skinColor, "Body")

        // Hand shapes (tetrahedral mannequin endings)
        fun makeHand(wrist: Vector3D, isLeft: Boolean) {
            val direction = if (isLeft) -1.8f else 1.8f
            val handTip = Vector3D(wrist.x + direction * 4.2f, wrist.y - 5.5f, wrist.z - 1.2f)
            val thumbTip = Vector3D(wrist.x + direction * 2.8f, wrist.y - 2.8f, wrist.z + 1.8f)
            
            polygons.add(Polygon3D(listOf(wrist, handTip, thumbTip), skinColor, "Body"))
            polygons.add(Polygon3D(listOf(wrist, thumbTip, Vector3D(wrist.x, wrist.y - 3.8f, wrist.z - 2f)), skinColor, "Body"))
        }
        makeHand(leftWrist, true)
        makeHand(rightWrist, false)

        return polygons
    }

    private fun chipsDepth(hipsWidth: Float): Float {
        return hipsWidth * 0.72f
    }

    // Generates a 3D garment overlay draped around the active morphing avatar, representing different garments
    fun generateGarmentMesh(profile: UserProfile, garmentType: String, fitHex: Int): List<Polygon3D> {
        val polygons = mutableListOf<Polygon3D>()

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

        // Function to create a 16-step garment ring for gorgeous smooth drapes
        fun createGarmentRing(y: Float, rx: Float, rz: Float, padding: Float): List<Vector3D> {
            val ring = mutableListOf<Vector3D>()
            val steps = 16
            for (i in 0 until steps) {
                val angle = (2 * Math.PI * i / steps).toFloat()
                val finalRx = rx + padding
                val finalRz = rz + padding
                ring.add(Vector3D(finalRx * cos(angle), y, finalRz * sin(angle)))
            }
            return ring
        }

        // Link two garment rings
        fun linkGarmentRings(ring1: List<List<Vector3D>>, color: Int) {
            for (r in 0 until ring1.size - 1) {
                val lower = ring1[r]
                val upper = ring1[r + 1]
                val size = lower.size
                for (i in 0 until size) {
                    val next = (i + 1) % size
                    polygons.add(
                        Polygon3D(
                            vertices = listOf(lower[i], lower[next], upper[next], upper[i]),
                            color = color,
                            label = "Garment"
                        )
                    )
                }
            }
        }

        when (garmentType.lowercase()) {
            "jacket", "t-shirt", "shirt" -> {
                // Top garment: spans shoulder down to waist/hips
                val jacketPadding = if (garmentType.lowercase() == "jacket") 3.0f else 1.5f
                val lengthY = if (garmentType.lowercase() == "jacket") hipsY + 8f else waistY + 2f

                val ringBodyShoulder = createGarmentRing(shoulderY, shoulderWidth / 2f, (shoulderWidth * 0.7f) / 2f, jacketPadding)
                val ringBodyChest = createGarmentRing(chestY, chestWidth / 2f, (chestWidth * 0.75f) / 2f, jacketPadding)
                val ringBodyWaist = createGarmentRing(waistY, waistWidth / 2f, (waistWidth * 0.75f) / 2f, jacketPadding)
                val ringBodyHips = createGarmentRing(lengthY, hipsWidth / 2f, (hipsWidth * 0.7f) / 2f, jacketPadding)

                linkGarmentRings(listOf(ringBodyHips, ringBodyWaist, ringBodyChest, ringBodyShoulder), fitHex)

                // Add short sleeve columns
                val leftShoulder = Vector3D(-shoulderWidth * 0.5f, shoulderY, -0.5f)
                val leftElbow = Vector3D(-shoulderWidth * 0.62f, shoulderY - (20f * heightScale), -1.2f)
                val rightShoulder = Vector3D(shoulderWidth * 0.5f, shoulderY, -0.5f)
                val rightElbow = Vector3D(shoulderWidth * 0.62f, shoulderY - (20f * heightScale), -1.2f)

                fun makeGarmentSleeve(p0: Vector3D, p1: Vector3D, r: Float, col: Int) {
                    val steps = 12
                    val v0 = mutableListOf<Vector3D>()
                    val v1 = mutableListOf<Vector3D>()
                    for (i in 0 until steps) {
                        val angle = (2 * Math.PI * i / steps).toFloat()
                        val dx = (r + 1.5f) * cos(angle)
                        val dz = (r + 1.5f) * sin(angle)
                        v0.add(Vector3D(p0.x + dx, p0.y, p0.z + dz))
                        v1.add(Vector3D(p1.x + dx, p1.y, p1.z + dz))
                    }
                    for (i in 0 until steps) {
                        val next = (i + 1) % steps
                        polygons.add(Polygon3D(listOf(v0[i], v0[next], v1[next], v1[i]), col, "Garment"))
                    }
                }

                makeGarmentSleeve(leftElbow, leftShoulder, 8f, fitHex)
                makeGarmentSleeve(rightElbow, rightShoulder, 8f, fitHex)
            }
            "jeans", "trousers", "pants" -> {
                // Bottom garment: hips down to ankles
                val pantPadding = 2.0f
                val ringGhips = createGarmentRing(hipsY + 5f, hipsWidth / 2f, (hipsWidth * 0.75f) / 2f, pantPadding)
                val ringGwaist = createGarmentRing(waistY, waistWidth / 2f, (waistWidth * 0.75f) / 2f, pantPadding)

                // Join hips and waist
                linkGarmentRings(listOf(ringGhips, ringGwaist), fitHex)

                // Left and right leg tubes (hips to knees)
                val leftHip = Vector3D(-hipsWidth * 0.22f, hipsY, 0f)
                val rightHip = Vector3D(hipsWidth * 0.22f, hipsY, 0f)
                val leftKnee = Vector3D(-hipsWidth * 0.22f, hipsY * 0.35f, 0f)
                val rightKnee = Vector3D(hipsWidth * 0.22f, hipsY * 0.35f, 0f)

                fun makeGarmentTube(p0: Vector3D, p1: Vector3D, r: Float, col: Int) {
                    val steps = 12
                    val v0 = mutableListOf<Vector3D>()
                    val v1 = mutableListOf<Vector3D>()
                    for (i in 0 until steps) {
                        val angle = (2 * Math.PI * i / steps).toFloat()
                        val dx = (r + 2f) * cos(angle)
                        val dz = (r + 2f) * sin(angle)
                        v0.add(Vector3D(p0.x + dx, p0.y, p0.z + dz))
                        v1.add(Vector3D(p1.x + dx, p1.y, p1.z + dz))
                    }
                    for (i in 0 until steps) {
                        val next = (i + 1) % steps
                        polygons.add(Polygon3D(listOf(v0[i], v0[next], v1[next], v1[i]), col, "Garment"))
                    }
                }

                makeGarmentTube(leftKnee, leftHip, 11f, fitHex)
                makeGarmentTube(rightKnee, rightHip, 11f, fitHex)
            }
            else -> {
                // Dress / Full bodysuit: shoulder all the way down to above knees
                val dressPadding = 2.5f
                val ringBodyShoulder = createGarmentRing(shoulderY, shoulderWidth / 2f, (shoulderWidth * 0.7f) / 2f, dressPadding)
                val ringBodyChest = createGarmentRing(chestY, chestWidth / 2f, (chestWidth * 0.75f) / 2f, dressPadding)
                val ringBodyWaist = createGarmentRing(waistY, waistWidth / 2f, (waistWidth * 0.75f) / 2f, dressPadding)
                val ringBodyHips = createGarmentRing(hipsY, hipsWidth / 2f, (hipsWidth * 0.7f) / 2f, dressPadding)
                
                // Bottom skirt flare of dress
                val skirtHemY = hipsY - (25f * heightScale)
                val ringBodySkirt = createGarmentRing(skirtHemY, (hipsWidth * 1.3f) / 2f, (hipsWidth * 1.0f) / 2f, dressPadding)

                linkGarmentRings(listOf(ringBodySkirt, ringBodyHips, ringBodyWaist, ringBodyChest, ringBodyShoulder), fitHex)
            }
        }

        return polygons
    }
}
