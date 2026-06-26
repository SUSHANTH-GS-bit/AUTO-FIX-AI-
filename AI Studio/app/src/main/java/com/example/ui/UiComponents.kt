package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Garage
import com.example.ui.theme.*

// Draw elegant minimal brand emblems via canvas/graphics to avoid missing PNG link exceptions
@Composable
fun CompanyLogoBadge(company: String, modifier: Modifier = Modifier) {
    val upper = company.uppercase()
    val badgeColor = when (upper) {
        "TESLA" -> Color(0xFFE82127)
        "BMW" -> Color(0xFF1E88E5)
        "MERCEDES" -> Color(0xFF78909C)
        "PORSCHE" -> Color(0xFFFFB300)
        "TOYOTA" -> Color(0xFFC62828)
        "HONDA" -> Color(0xFF1565C0)
        "HYUNDAI" -> Color(0xFF0D47A1)
        "FORD" -> Color(0xFF1E3A8A)
        else -> ChevronOrange
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.5.dp, badgeColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (upper) {
                    "TESLA" -> Icons.Default.Bolt
                    "BMW" -> Icons.Default.Settings
                    "MERCEDES" -> Icons.Default.Shield
                    "PORSCHE" -> Icons.Default.Star
                    "TOYOTA" -> Icons.Default.Build
                    "HONDA" -> Icons.Default.Speed
                    "HYUNDAI" -> Icons.Default.DirectionsCar
                    else -> Icons.Default.Garage
                },
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = upper.take(3),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = badgeColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// Interactive Map drawing on-canvas with smooth radar sweeps and step-by-step direction routes
@Composable
fun SimulatedGarageRadarMap(
    userLat: Double,
    userLng: Double,
    garages: List<Garage>,
    selectedGarage: Garage?,
    activeRoute: Garage?,
    onGarageClick: (Garage) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonDark)
            .border(1.5.dp, SteelLow, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .clickable {
                // If user clicks overall canvas, find nearest pin to click
                if (garages.isNotEmpty()) {
                    onGarageClick(garages.first())
                }
            }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width.coerceAtMost(size.height) / 2.1f

            // 1. Draw concentric grid circles
            drawCircle(
                color = SteelLow.copy(alpha = 0.6f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = SteelLow.copy(alpha = 0.4f),
                radius = maxRadius * 0.66f,
                center = center,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = SteelLow.copy(alpha = 0.25f),
                radius = maxRadius * 0.33f,
                center = center,
                style = Stroke(width = 2f)
            )

            // Radial axes lines
            drawLine(
                color = SteelLow.copy(alpha = 0.3f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 2f
            )
            drawLine(
                color = SteelLow.copy(alpha = 0.3f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 2f
            )

            // 2. Draw active glowing radar sweep
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        ElectricBlue.copy(alpha = 0.25f),
                        ElectricBlue.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = sweepAngle,
                sweepAngle = 45f,
                useCenter = true,
                size = Size(maxRadius * 2f, maxRadius * 2f),
                topLeft = Offset(center.x - maxRadius, center.y - maxRadius)
            )

            // 3. Draw user location center ring & ping pulse
            drawCircle(
                color = ElectricBlue.copy(alpha = 0.2f * (1f - pulseScale)),
                radius = maxRadius * 0.25f * pulseScale,
                center = center
            )
            drawCircle(
                color = ElectricBlue,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = SteelHigh,
                radius = 3.dp.toPx(),
                center = center
            )

            // 4. Plot Garage entries based on mock distance calculations
            garages.forEachIndexed { index, garage ->
                // Distribute garages visually in concentric patterns based on index distance
                val angleRad = (60.0 * index + 35.0) * (Math.PI / 180.0)
                val visualDist = maxRadius * (0.35f + (index * 0.12f).coerceAtMost(0.55f))
                val pinX = center.x + (visualDist * Math.cos(angleRad)).toFloat()
                val pinY = center.y + (visualDist * Math.sin(angleRad)).toFloat()
                val pinPos = Offset(pinX, pinY)

                val isSelectedOrRoute = (garage.id == selectedGarage?.id) || (garage.id == activeRoute?.id)
                val pinColor = if (isSelectedOrRoute) DiagnosticAmber else ChevronOrange

                // Draw route line if this garage has ACTIVE step-by-step guidance
                if (activeRoute?.id == garage.id) {
                    // Draw a jagged dashed neon blue path from user center to this pin!
                    val midPoint1 = Offset(center.x + (pinX - center.x) * 0.4f, center.y + (pinY - center.y) * 0.2f)
                    val midPoint2 = Offset(center.x + (pinX - center.x) * 0.7f, center.y + (pinY - center.y) * 0.8f)

                    drawLine(
                        color = ElectricBlue,
                        start = center,
                        end = midPoint1,
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = ElectricBlue,
                        start = midPoint1,
                        end = midPoint2,
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = ElectricBlue,
                        start = midPoint2,
                        end = pinPos,
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Draw external glowing marker Ring
                drawCircle(
                    color = pinColor.copy(alpha = 0.35f),
                    radius = if (isSelectedOrRoute) 14.dp.toPx() else 10.dp.toPx(),
                    center = pinPos
                )
                // Draw Inner Solid Core
                drawCircle(
                    color = pinColor,
                    radius = if (isSelectedOrRoute) 7.dp.toPx() else 5.dp.toPx(),
                    center = pinPos
                )
            }
        }

        // Overlay text coordinates
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(CarbonDark.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = "GPS LIVE: ENABLED",
                color = ElectricBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "COORDS: " + String.format("%.4f", userLat) + " , " + String.format("%.4f", userLng),
                color = SteelMedium,
                fontSize = 9.sp
            )
        }
    }
}

// Visual feedback respiration orb
@Composable
fun RelaxationBreathingBalloon(
    phrase: String,
    scaleProgress: Float,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleFactor by animateFloatAsState(
        targetValue = if (isActive) scaleProgress else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BalloonScale"
    )

    val pulsingColor = if (isActive) {
        if (phrase.contains("Inhale")) ElectricBlue else DiagnosticAmber
    } else {
        ChevronOrange
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Roadside Stress Dissolver",
            style = MaterialTheme.typography.titleMedium,
            color = SteelHigh,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Car troubles can be stressful. Re-align with a quick 4-7-8 deep breathing helper.",
            style = MaterialTheme.typography.bodySmall,
            color = SteelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(170.dp)
                .scale(scaleFactor)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            pulsingColor.copy(alpha = 0.8f),
                            pulsingColor.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, pulsingColor, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.HourglassEmpty else Icons.Default.Spa,
                    contentDescription = null,
                    tint = SteelHigh,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = phrase,
                    color = SteelHigh,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isActive) "MATCH BREATH" else "TAP TO START",
                    color = SteelHigh.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) SteelLow else ChevronOrange
            )
        ) {
            Text(if (isActive) "End Breathing Session" else "Activate Breath Guide")
        }
    }
}

// Interactive tap relaxation engine tool
@Composable
fun ClunkyEngineTappingGame(
    clickCount: Int,
    onTap: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EngineShaking")
    // Shake the engine coordinate offsets when clicks < 20 (still clunky)
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (clickCount >= 20) 0 else 150 - (clickCount * 5).coerceAtLeast(120)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Shake"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PetrolSlate, RoundedCornerShape(16.dp))
            .border(1.dp, SteelLow, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔩 Roadside Stress-Relief Mini-Game",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SteelHigh
        )
        Text(
            text = "Tighten the clunky engine bolts to stop the stress-inducing rattles!",
            style = MaterialTheme.typography.bodySmall,
            color = SteelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .offset(y = if (clickCount >= 20) 0.dp else shakeOffset.dp)
                .background(CarbonDark, RoundedCornerShape(12.dp))
                .border(2.dp, if (clickCount >= 20) BioGreen else DiagnosticAmber, RoundedCornerShape(12.dp))
                .clickable { if (clickCount < 20) onTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (clickCount >= 20) Icons.Default.CheckCircle else Icons.Default.Build,
                    contentDescription = null,
                    tint = if (clickCount >= 20) BioGreen else DiagnosticAmber,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (clickCount >= 20) "TUNED & STABLE!" else "VIBRATING CLUNKER ENGINE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (clickCount >= 20) BioGreen else DiagnosticAmber
                )
                Text(
                    text = if (clickCount >= 20) "Bolts fully tightened." else "${20 - clickCount} loose bolts remaining!",
                    fontSize = 11.sp,
                    color = SteelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onTap,
                enabled = clickCount < 20,
                colors = ButtonDefaults.buttonColors(containerColor = BulletTightenColor)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tighten (Tap)")
            }

            TextButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Engine")
            }
        }
    }
}

val BulletTightenColor = Color(0xFFFF9800)
