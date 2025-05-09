package com.xtenalyze.numble

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Define the game title
private val gameTitle = "Numble"

// Define the main theme colors for SumRush with more vibrant blues
private val primaryColor = Color(0xFF2196F3)  // Brighter main blue
private val accentColor = Color(0xFFFFC107)   // Brighter yellow for better contrast

// Operation colors with enhanced vibrancy
private val operationColors = listOf(
    Color(0xFF1E88E5),  // Addition - Vibrant Blue
    Color(0xFFE53935),  // Subtraction - Vibrant Red
    Color(0xFF43A047),  // Multiplication - Vibrant Green
    Color(0xFFFF8F00)   // Division - Vibrant Orange
)

// Enhanced gradient colors with more vibrant blues
private val gradientColors = listOf(
    Color(0xFF1A237E),   // Deep blue (darker)
    Color(0xFF1976D2),   // Vibrant medium blue
    Color(0xFF29B6F6),   // Bright lighter blue
    Color(0xFF0D47A1)    // Rich deep blue
)

// Math symbols to animate around the screen
private val mathSymbols = listOf("+", "-", "×", "÷", "=", "7", "3", "9", "5")

// Data class for floating particles
private data class ParticleData(
    val symbol: String,
    val xPos: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

// Add this function for glossy text effect
@Composable
private fun GlossyText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    alpha: Float = 1f
) {
    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        // Main text
        Text(
            text = text,
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = fontSize,
                color = color,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.7f),
                    offset = Offset(4f, 4f),
                    blurRadius = 6f
                )
            )
        )

        // Glossy overlay - white gradient on top portion
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.6f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = fontSize.value / 2
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .height((fontSize.value / 2).dp)
        )

        // Bottom glossy effect (subtle reflection)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = (fontSize.value * 0.8f).dp)
                .alpha(0.3f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = fontSize.value / 3
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
                .height((fontSize.value / 3).dp)
        )
    }
}

@Composable
fun SumRushSplashScreen(onSplashFinished: () -> Unit) {
    var letterIndex by remember { mutableStateOf(-1) }
    var showMathSymbols by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var allAnimationsComplete by remember { mutableStateOf(false) }

    // Track which symbols are visible
    val visibleSymbols = remember { mutableStateListOf<Int>() }

    // Logo animation values
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }

    // Initialize game font - Luckiest Guy is a fun, bold font perfect for games
    val gameFont = FontFamily(
        Font(R.font.luckiest_guy, FontWeight.Normal)
    )

    // For the floating math elements, with different positions and timings
    val floatingSymbols = remember {
        List(15) {
            Triple(
                Random.nextFloat() * 0.8f + 0.1f, // x position
                Random.nextFloat() * 0.8f + 0.1f, // y position
                mathSymbols[it % mathSymbols.size] // symbol
            )
        }
    }

    val symbolAnimatables = remember {
        List(floatingSymbols.size) { Animatable(0f) }
    }

    // Tagline animation
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    // Math symbols animation scale - more gentle appearance
    val mathSymbolsScale by animateFloatAsState(
        targetValue = if (showMathSymbols) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        )
    )

    // Company logo animation values with enhanced visibility
    val companyLogoScale = remember { Animatable(0.4f) } // Start even smaller for more dramatic effect
    val companyLogoAlpha = remember { Animatable(0f) }
    val logoBackgroundAlpha = remember { Animatable(0f) }

    // Add a more noticeable pulsating glow effect for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "LogoPulse")
    val pulsateAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Add loading progress indicator
    val loadingProgress = remember { Animatable(0f) }

    // Add particle effects
    val particleCount = 20
    val particles = remember {
        List(particleCount) {
            val symbol = mathSymbols[it % mathSymbols.size]
            val xPos = Random.nextFloat() * 0.9f + 0.05f
            val startYPos = 1.2f + (Random.nextFloat() * 0.5f)
            val speed = Random.nextFloat() * 0.3f + 0.1f
            val size = Random.nextFloat() * 0.4f + 0.3f
            val alpha = Random.nextFloat() * 0.3f + 0.1f

            ParticleData(symbol, xPos, startYPos, speed, size, alpha)
        }
    }

    // Fade in animation for "Tap to continue" text
    val tapToContinueAlpha by animateFloatAsState(
        targetValue = if (allAnimationsComplete) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    // Handle animation sequence
    LaunchedEffect(key1 = true) {
        // Start with circle logo animation
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        // Animate progress simultaneously with other animations
        launch {
            loadingProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = LinearEasing
                )
            )
        }

        // Enable math symbols to appear
        delay(300)
        showMathSymbols = true

        // Gradually show the floating symbols one by one with delays
        for (i in 0 until floatingSymbols.size) {
            launch {
                delay(i * 150L) // Longer delay between symbols (was 75ms)
                visibleSymbols.add(i)

                symbolAnimatables[i].animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }

        // Start letter animations
        delay(800)
        for (i in 0..gameTitle.length) {
            letterIndex = i
            delay(120)
        }

        // Show tagline
        delay(500)
        showTagline = true

        // Animate the company logo with improved visibility
        delay(300) // Add a short delay after the tagline before showing the logo

        // Parallel animations for scale, alpha and background
        launch {
            logoBackgroundAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }

        launch {
            companyLogoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800) // Longer fade-in
            )
        }

        launch {
            companyLogoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // More bouncy
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        // Set all animations complete flag to show tap to continue
        delay(700)
        allAnimationsComplete = true

        // Navigate to main screen after everything is shown
        delay(2500) // Increase to 2.5 seconds to give the logo more visibility time
        onSplashFinished()
    }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
            .pointerInput(allAnimationsComplete) {
                detectTapGestures {
                    if (allAnimationsComplete) {
                        onSplashFinished()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Animated number grid pattern in background
        val gridAlpha by animateFloatAsState(
            targetValue = if (showMathSymbols) 0.15f else 0f,
            animationSpec = tween(durationMillis = 1000)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(gridAlpha)
        ) {
            val cellSize = 40.dp.toPx()
            val rows = size.height / cellSize
            val cols = size.width / cellSize

            for (row in 0..rows.toInt()) {
                for (col in 0..cols.toInt()) {
                    // Draw grid lines
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(col * cellSize, 0f),
                        end = Offset(col * cellSize, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(0f, row * cellSize),
                        end = Offset(size.width, row * cellSize),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // Add floating particles (math symbols moving upward)
        for (particle in particles) {
            val infiniteTransition = rememberInfiniteTransition(label = "Particle${particle.symbol}")
            val animatedY by infiniteTransition.animateFloat(
                initialValue = particle.startY,
                targetValue = -0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (8000 / particle.speed).toInt(),
                        easing = LinearEasing
                    )
                ),
                label = "ParticleY"
            )

            Text(
                text = particle.symbol,
                color = operationColors[particle.symbol.hashCode() % operationColors.size]
                    .copy(alpha = particle.alpha * gridAlpha * 0.7f),
                fontSize = (20 * particle.size).sp,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(
                        x = (particle.xPos * 400).dp,
                        y = (animatedY * 800).dp
                    )
            )
        }

        // Animated floating math symbols - now appear one by one
        for (index in visibleSymbols) {
            val (x, y, symbol) = floatingSymbols[index]
            val symbolColor = operationColors[index % operationColors.size]
            val size = remember { Random.nextFloat() * 0.5f + 0.7f }
            val animValue = symbolAnimatables[index].value

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = symbol,
                    fontFamily = gameFont,
                    color = symbolColor.copy(alpha = 0.7f * animValue),
                    fontSize = (36 * size).sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (x * 800).dp,
                            y = (y * 1600).dp
                        )
                        .alpha(animValue * 0.7f)
                        .scale(mathSymbolsScale * animValue * size)
                )
            }
        }

        // Main content column
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Math Operations Symbol (like a logo)
            Box(
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .size(150.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value),
                contentAlignment = Alignment.Center
            ) {
                // Draw a circular math operations symbol that looks like a logo
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background circle
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 8.dp.toPx())
                    )

                    // Inner glowing circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.minDimension / 2.2f
                    )

                    // Addition, subtraction, multiplication and division symbols in a quadrant layout
                    val radius = size.minDimension / 3
                    val center = Offset(size.width / 2, size.height / 2)

                    // Addition
                    drawLine(
                        color = operationColors[0],
                        start = Offset(center.x, center.y - radius / 2),
                        end = Offset(center.x, center.y + radius / 2),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = operationColors[0],
                        start = Offset(center.x - radius / 2, center.y),
                        end = Offset(center.x + radius / 2, center.y),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Equal sign at the bottom
                    drawLine(
                        color = accentColor,
                        start = Offset(center.x - radius / 2, center.y + radius / 1.5f),
                        end = Offset(center.x + radius / 2, center.y + radius / 1.5f),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = accentColor,
                        start = Offset(center.x - radius / 2, center.y + radius / 1.5f + 12.dp.toPx()),
                        end = Offset(center.x + radius / 2, center.y + radius / 1.5f + 12.dp.toPx()),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Enhanced 3D glossy SUMRUSH title with glow effect
            Box(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .drawBehind {
                        // Draw a subtle glow behind the entire title
                        drawRoundRect(
                            color = Color(0xFF0091EA).copy(alpha = 0.3f),
                            cornerRadius = CornerRadius(20f, 20f),
                            size = size.copy(
                                width = size.width + 20.dp.toPx(),
                                height = size.height + 20.dp.toPx()
                            ),
                            topLeft = Offset(-10.dp.toPx(), -10.dp.toPx()),
                            style = Stroke(width = 10.dp.toPx(), join = StrokeJoin.Round)
                        )
                    }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    gameTitle.forEachIndexed { index, letter ->
                        val isVisible = index <= letterIndex
                        val letterAlpha by animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0f,
                            animationSpec = tween(durationMillis = 150)
                        )

                        val letterScale by animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        // Enhanced more vibrant colors with blue emphasis
                        val letterColor = when (index) {
                            // Make all letters a more vibrant blue except a few accent letters
                            0, 1, 2, 5 -> Color(0xFF0091EA) // Brighter, more vibrant blue
                            3, 6 -> Color(0xFF2979FF)       // Slightly different blue for contrast
                            4 -> operationColors[1]         // Keep accent color for 'U'
                            else -> accentColor             // Keep accent for remaining
                        }

                        // Use our glossy text component
                        GlossyText(
                            text = letter.toString(),
                            color = letterColor,
                            fontSize = 80.sp, // Increased size from 72 to 80
                            fontFamily = gameFont,
                            modifier = Modifier
                                .alpha(letterAlpha)
                                .padding(horizontal = 2.dp),
                            scale = letterScale
                        )
                    }
                }
            }

            // Tagline
            Text(
                text = "Quick Math Challenge",
                style = TextStyle(
                    fontFamily = gameFont,
                    fontSize = 22.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(taglineAlpha)
            )

            // Add progress indicator
            LinearProgressIndicator(
                progress = loadingProgress.value,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .width(200.dp)
                    .height(4.dp)
                    .alpha(taglineAlpha * 0.7f),
                color = accentColor,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }

        // Enhanced company logo at the bottom with better visibility
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .drawBehind {
                    // Draw an outer glow effect
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f * pulsateAlpha),
                        radius = size.maxDimension * 0.65f,
                        center = center + Offset(0f, 30f)
                    )
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .alpha(companyLogoAlpha.value)
                .scale(companyLogoScale.value)
        ) {
            // Use your company logo resource with progressive animation
            Image(
                painter = painterResource(id = R.drawable.xtenalyze_logo),
                contentDescription = "XTenalyze Logo",
                modifier = Modifier
                    .width(240.dp)  // Further increased size
                    .height(90.dp)   // Further increased size
            )
        }

        // Add "Tap to continue" text when animations are complete
        Text(
            text = "Tap to continue",
            color = Color.White.copy(alpha = tapToContinueAlpha * 0.8f),
            fontSize = 16.sp,
            fontFamily = gameFont,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .alpha(tapToContinueAlpha)
        )
    }
}