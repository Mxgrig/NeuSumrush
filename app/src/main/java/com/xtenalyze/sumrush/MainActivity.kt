package com.xtenalyze.sumrush

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlin.random.Random
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "SumRush"

    // Game state variables
    private var score = 0
    private var targetNumber = 0
    private var currentSum = 0
    private var level = 1
    private var isGameActive = false
    private var highScore = 0
    private var combo = 0
    private var maxCombo = 0
    private var lives = 3

    // Difficulty settings
    private data class LevelSettings(
        val timeInSeconds: Int,
        val minNumber: Int,
        val maxNumber: Int,
        val specialItemFrequency: Float,  // Probability of special items (0.0 - 1.0)
        val solutionSize: IntRange, // Range of numbers needed for solution
        val operations: List<Operation> // Available operations for this level
    )

    private val levelSettings = mapOf(
        1 to LevelSettings(20, 1, 5, 0.05f, 2..2, listOf(Operation.ADDITION)),
        2 to LevelSettings(18, 1, 9, 0.10f, 2..3, listOf(Operation.ADDITION)),
        3 to LevelSettings(
            16,
            1,
            10,
            0.15f,
            2..3,
            listOf(Operation.ADDITION, Operation.SUBTRACTION)
        ),
        4 to LevelSettings(
            15,
            1,
            12,
            0.20f,
            2..4,
            listOf(Operation.ADDITION, Operation.SUBTRACTION)
        ),
        5 to LevelSettings(
            14,
            2,
            15,
            0.25f,
            2..4,
            listOf(Operation.ADDITION, Operation.SUBTRACTION, Operation.MULTIPLICATION)
        ),
        6 to LevelSettings(
            13,
            2,
            15,
            0.30f,
            2..4,
            listOf(Operation.ADDITION, Operation.SUBTRACTION, Operation.MULTIPLICATION)
        ),
        7 to LevelSettings(
            12,
            2,
            20,
            0.35f,
            2..5,
            listOf(Operation.ADDITION, Operation.SUBTRACTION, Operation.MULTIPLICATION)
        ),
        8 to LevelSettings(
            11,
            2,
            25,
            0.40f,
            2..5,
            listOf(
                Operation.ADDITION,
                Operation.SUBTRACTION,
                Operation.MULTIPLICATION,
                Operation.DIVISION
            )
        ),
        9 to LevelSettings(
            10,
            3,
            30,
            0.45f,
            3..5,
            listOf(
                Operation.ADDITION,
                Operation.SUBTRACTION,
                Operation.MULTIPLICATION,
                Operation.DIVISION
            )
        ),
        10 to LevelSettings(
            8,
            3,
            50,
            0.50f,
            3..6,
            listOf(
                Operation.ADDITION,
                Operation.SUBTRACTION,
                Operation.MULTIPLICATION,
                Operation.DIVISION
            )
        )
    )

    // Special items
    enum class SpecialItem {
        NONE, BOMB, DOUBLE_POINTS, TIME_BONUS, LIFE
    }

    private val specialButtons = mutableMapOf<Button, SpecialItem>()

    // Operation mode
    private enum class Operation {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION
    }

    private var currentOperation = Operation.ADDITION
    private var availableOperations = listOf(Operation.ADDITION)

    // UI elements
    private lateinit var levelText: TextView
    private lateinit var livesText: TextView
    private lateinit var operationIndicator: TextView
    private lateinit var instructionsText: TextView
    private lateinit var targetNumberText: TextView
    private lateinit var timerText: TextView
    private lateinit var timerProgressBar: ProgressBar
    private lateinit var scoreText: TextView
    private lateinit var comboText: TextView
    private lateinit var currentSumText: TextView
    private lateinit var resetButton: Button
    private lateinit var changeOperationButton: Button
    private lateinit var buttonGrid: Array<Array<Button>>
    private var selectedButtons = mutableListOf<Button>()

    // Timer
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis = 0L
    private var maxTimeInMillis = 0L

    // Vibrator for haptic feedback
    private var vibrator: Vibrator? = null

    // SoundPool for sound effects
    private lateinit var soundPool: SoundPool
    private var soundCorrect = 0
    private var soundIncorrect = 0
    private var soundLevelUp = 0
    private var soundGameOver = 0
    private var soundBomb = 0
    private var soundBonus = 0
    private var soundClick = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            // Initialize UI elements
            levelText = findViewById(R.id.levelText)
            livesText = findViewById(R.id.livesText)
            operationIndicator = findViewById(R.id.operationIndicator)
            instructionsText = findViewById(R.id.instructionsText)
            targetNumberText = findViewById(R.id.targetNumberText)
            timerText = findViewById(R.id.timerText)
            timerProgressBar = findViewById(R.id.timerProgressBar)
            scoreText = findViewById(R.id.scoreText)
            comboText = findViewById(R.id.comboText)
            currentSumText = findViewById(R.id.currentSumText)
            resetButton = findViewById(R.id.resetButton)
            changeOperationButton = findViewById(R.id.changeOperationButton)

            if (!validateResources()) {
                Toast.makeText(this, "Unable to load game resources", Toast.LENGTH_LONG).show()
                finish()
                return
            }


            // Initialize vibrator using the appropriate method based on Android version

            // In the onCreate method, replace the current vibrator initialization with this:

// Initialize vibrator safely
            // Replace the vibrator initialization code in onCreate() with this simpler approach:

            vibrator = null

            // Initialize sound pool with minimal functionality
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(6)
                    .setAudioAttributes(audioAttributes)
                    .build()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SoundPool", e)
                // Create a minimal SoundPool as fallback
                soundPool = SoundPool.Builder().setMaxStreams(1).build()
            }












            // Initialize SoundPool


            // Replace the entire SoundPool initialization with this minimal version
            // Set sound IDs to 0 initially - they'll play silently if loading fails
            soundCorrect = 0
            soundIncorrect = 0
            soundLevelUp = 0
            soundGameOver = 0
            soundBomb = 0
            soundBonus = 0
            soundClick = 0

            // Try to load sounds, but continue if they fail
            try {
                soundCorrect = safeLoadSound(R.raw.correct)
                soundIncorrect = safeLoadSound(R.raw.incorrect)
                soundLevelUp = safeLoadSound(R.raw.level_up)
                soundGameOver = safeLoadSound(R.raw.game_over)
                soundBomb = safeLoadSound(R.raw.bomb)
                soundBonus = safeLoadSound(R.raw.bonus)
                soundClick = safeLoadSound(R.raw.click)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sound effects", e)
                // Continue without sound effects
            }

            // Initialize button grid
            try {
                initializeButtonGrid()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing button grid", e)
                Toast.makeText(this, "Error setting up game board", Toast.LENGTH_LONG).show()
            }

            // Get high score from SharedPreferences
            try {
                val sharedPref = getSharedPreferences("SumRushPrefs", Context.MODE_PRIVATE)
                highScore = sharedPref.getInt("highScore", 0)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading high score", e)
                highScore = 0
            }

            // Set up reset button
            try {
                resetButton.setOnClickListener {
                    try {
                        if (soundClick > 0) {
                            soundPool.play(soundClick, 1f, 1f, 1, 0, 1f)
                        }
                        clearSelection()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in reset button click", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up reset button", e)
            }

            // Set up change operation button
            try {
                changeOperationButton.setOnClickListener {
                    try {
                        if (soundClick > 0) {
                            soundPool.play(soundClick, 1f, 1f, 1, 0, 1f)
                        }
                        changeOperation()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in change operation button click", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up change operation button", e)
            }


            // Start a new game
            startNewGame()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            // Show a toast with the error message
            Toast.makeText(this, "Error initializing game: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    // Add this method to the class
    // Fallback vibrator for devices with vibration issues





    private fun initializeButtonGrid() {
        try {
            buttonGrid = Array(4) { row ->
                Array(4) { col ->
                    val buttonId = resources.getIdentifier(
                        "button$row$col",
                        "id",
                        packageName
                    )
                    val button = findViewById<Button>(buttonId)

                    // Set click listener for each button
                    button.setOnClickListener { onNumberButtonClick(button) }
                    button
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing button grid: ${e.message}")
            Toast.makeText(this, "Error setting up game board", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startNewGame() {
        try {
            // Reset game state
            score = 0
            level = 1
            combo = 0
            maxCombo = 0
            lives = 3
            isGameActive = true

            // Update available operations for level 1
            availableOperations = levelSettings[1]?.operations ?: listOf(Operation.ADDITION)
            currentOperation = availableOperations.first()

            // Set time based on level
            val timeInSeconds = levelSettings[level]?.timeInSeconds ?: 10
            timeLeftInMillis = timeInSeconds * 1000L
            maxTimeInMillis = timeLeftInMillis

            // Update UI
            updateUI()

            // Animate level text
            animateTextScale(levelText)

            // Fill grid with numbers and set target
            resetGameGrid()

            // Start the countdown timer
            startCountdownTimer()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting new game: ${e.message}")
        }
    }

    private fun updateUI() {
        // Update all UI elements with current game state
        levelText.text = "Level $level"
        livesText.text = "❤".repeat(lives)
        scoreText.text = score.toString()
        comboText.text = "×$combo"
        comboText.visibility = if (combo > 1) View.VISIBLE else View.INVISIBLE

        // Update operation display
        updateOperationDisplay()
    }



    private fun resetGameGrid() {
        try {
            // Clear selected buttons
            clearSelection()

            // Generate a valid target and fill the grid
            generateTargetAndFillGrid()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting game grid: ${e.message}")
        }
    }

    private fun changeOperation() {
        try {
            if (availableOperations.size <= 1) {
                // If only one operation available, just reset grid
                resetGameGrid()
                return
            }

            // Get all operations except current
            val others = availableOperations.filter { it != currentOperation }
            // Choose a random new operation
            currentOperation = others.random()

            // Update operation display
            updateOperationDisplay()

            // Generate new numbers and target
            resetGameGrid()

            // Reset timer
            restartTimer()

            // Provide feedback for operation change
            soundPool.play(soundClick, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            Log.e(TAG, "Error changing operation: ${e.message}")
        }
    }

    private fun updateOperationDisplay() {
        try {
            when (currentOperation) {
                Operation.ADDITION -> {
                    operationIndicator.text = "+"
                    instructionsText.text = "Tap numbers that ADD UP to the target"
                }

                Operation.SUBTRACTION -> {
                    operationIndicator.text = "-"
                    instructionsText.text = "Tap numbers to SUBTRACT and reach the target"
                }

                Operation.MULTIPLICATION -> {
                    operationIndicator.text = "×"
                    instructionsText.text = "Tap numbers to MULTIPLY and reach the target"
                }

                Operation.DIVISION -> {
                    operationIndicator.text = "÷"
                    instructionsText.text = "Tap numbers to DIVIDE and reach the target"
                }
            }

            // Animate the operation indicator to draw attention
            animateTextScale(operationIndicator)
            val fadeAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            instructionsText.startAnimation(fadeAnimation)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating operation display: ${e.message}")
        }
    }

    private fun generateTargetAndFillGrid() {
        try {
            // Clear special buttons map
            specialButtons.clear()

            // Get current level settings
            val settings = levelSettings[level] ?: levelSettings[1]!!

            // Generate grid numbers based on operation and level
            val gridNumbers = Array(4) { Array(4) { 0 } }

            // Fill the grid with appropriate numbers based on operation and level
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    gridNumbers[row][col] = when (currentOperation) {
                        Operation.ADDITION -> Random.nextInt(
                            settings.minNumber,
                            settings.maxNumber + 1
                        )
                        Operation.SUBTRACTION -> Random.nextInt(
                            settings.minNumber,
                            settings.maxNumber + 1
                        )
                        Operation.MULTIPLICATION -> {
                            // For multiplication, use smaller numbers to keep result manageable
                            val max = minOf(settings.maxNumber, 12)
                            Random.nextInt(settings.minNumber, max + 1)
                        }
                        Operation.DIVISION -> {
                            // For division, use numbers that often divide evenly
                            listOf(2, 3, 4, 5, 6, 8, 10, 12).filter { it <= settings.maxNumber }
                                .random()
                        }
                    }
                }
            }

            // Choose a solution size within the level's range
            val solutionSize =
                Random.nextInt(settings.solutionSize.first, settings.solutionSize.last + 1)

            // Pick random positions for the solution
            val solutionPositions = mutableListOf<Pair<Int, Int>>()
            while (solutionPositions.size < solutionSize) {
                val row = Random.nextInt(4)
                val col = Random.nextInt(4)
                val position = Pair(row, col)
                if (!solutionPositions.contains(position)) {
                    solutionPositions.add(position)
                }
            }

            // Calculate the target number based on the operation
            targetNumber = when (currentOperation) {
                Operation.ADDITION -> {
                    solutionPositions.sumOf { gridNumbers[it.first][it.second] }
                }
                Operation.SUBTRACTION -> {
                    val numbers =
                        solutionPositions.map { gridNumbers[it.first][it.second] }.sorted()
                            .reversed()
                    numbers.first() - numbers.subList(1, numbers.size).sum()
                }
                Operation.MULTIPLICATION -> {
                    var result = 1
                    for (pos in solutionPositions) {
                        result *= gridNumbers[pos.first][pos.second]
                    }
                    result
                }
                Operation.DIVISION -> {
                    val numbers =
                        solutionPositions.map { gridNumbers[it.first][it.second] }.sorted()
                    // For division, ensure we get a whole number by making first number a multiple
                    val divisors = numbers.subList(1, numbers.size)
                    var result = numbers.first()
                    for (divisor in divisors) {
                        if (divisor != 0) {
                            if (result % divisor != 0) {
                                // Adjust the first number to ensure clean division
                                gridNumbers[solutionPositions.first().first][solutionPositions.first().second] *= divisor
                                result =
                                    gridNumbers[solutionPositions.first().first][solutionPositions.first().second]
                            }
                            result /= divisor
                        }
                    }
                    result
                }
            }

            // Update the target number text
            targetNumberText.text = targetNumber.toString()
            animateTextScale(targetNumberText)

            // Add special items based on level's frequency
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    val position = Pair(row, col)
                    // Don't add special items to solution positions to avoid confusion
                    if (!solutionPositions.contains(position) && Random.nextFloat() < settings.specialItemFrequency) {
                        val specialItem =
                            SpecialItem.values().filter { it != SpecialItem.NONE }.random()
                        val button = buttonGrid[row][col]
                        specialButtons[button] = specialItem
                    }
                }
            }

            // Fill the button grid with the generated numbers and set special icons
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    val button = buttonGrid[row][col]
                    button.text = gridNumbers[row][col].toString()
                    button.isSelected = false

                    // Set background to glossy button style
                    button.setBackgroundResource(R.drawable.glossy_button)

                    // Add special item icon if applicable
                    if (specialButtons.containsKey(button)) {
                        val drawable = when (specialButtons[button]) {
                            SpecialItem.BOMB -> R.drawable.ic_bomb
                            SpecialItem.DOUBLE_POINTS -> R.drawable.ic_double_points
                            SpecialItem.TIME_BONUS -> R.drawable.ic_time
                            SpecialItem.LIFE -> R.drawable.ic_heart
                            else -> null
                        }

                        drawable?.let {
                            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, it)
                        }
                    } else {
                        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }

                    // Apply animation to button
                    val animation = AnimationUtils.loadAnimation(this, R.anim.button_pop)
                    animation.startOffset = (row * 100 + col * 50).toLong()
                    button.startAnimation(animation)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating target and filling grid: ${e.message}")
        }
    }

    private fun onNumberButtonClick(button: Button) {
        try {
            if (!isGameActive) return

            // Play button click sound
            soundPool.play(soundClick, 0.5f, 0.5f, 1, 0, 1f)

            if (button.isSelected) {
                // Deselect if already selected
                button.isSelected = false
                button.setBackgroundResource(R.drawable.glossy_button)
                selectedButtons.remove(button)
                updateCurrentSum()
                return
            }

            // Apply selection
            button.isSelected = true
            button.setBackgroundResource(R.drawable.glossy_button_selected)
            selectedButtons.add(button)

            // Check if it's a special button
            if (specialButtons.containsKey(button)) {
                handleSpecialItem(button, specialButtons[button]!!)
                return
            }

            // Update current sum
            updateCurrentSum()

            // Check if current selection equals target
            checkForMatch()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling button click: ${e.message}")
        }
    }

    private fun handleSpecialItem(button: Button, item: SpecialItem) {
        try {
            when (item) {
                SpecialItem.BOMB -> {
                    // Bomb: Clear the grid and generate new numbers
                    soundPool.play(soundBomb, 1f, 1f, 1, 0, 1f)
                    showSpecialEffect(R.drawable.explosion_effect)

                    // Strong vibration for bomb
                    vibrateFeedback(300)

                    // Clear and reset grid
                    resetGameGrid()
                }

                SpecialItem.DOUBLE_POINTS -> {
                    // Double points for next correct answer
                    soundPool.play(soundBonus, 1f, 1f, 1, 0, 1f)
                    showFloatingText("DOUBLE POINTS!", button)
                    vibrateFeedback(100)

                    // Remove from selection so it doesn't affect calculation
                    button.isSelected = false
                    selectedButtons.remove(button)

                    // Set tag for double points
                    button.tag = "double_points"
                }

                SpecialItem.TIME_BONUS -> {
                    // Add time bonus
                    soundPool.play(soundBonus, 1f, 1f, 1, 0, 1f)

                    // Add 5 seconds
                    val bonusTime = 5000L
                    timeLeftInMillis = minOf(maxTimeInMillis, timeLeftInMillis + bonusTime)

                    // Update timer and progress
                    updateTimerUI()

                    showFloatingText("+5 SEC", button)
                    vibrateFeedback(100)

                    // Remove from selection
                    button.isSelected = false
                    selectedButtons.remove(button)
                }

                SpecialItem.LIFE -> {
                    // Extra life
                    soundPool.play(soundBonus, 1f, 1f, 1, 0, 1f)
                    if (lives < 3) {
                        lives++
                        livesText.text = "❤".repeat(lives)
                        animateTextScale(livesText)
                    }
                    showFloatingText("+1 LIFE", button)
                    vibrateFeedback(100)

                    // Remove from selection
                    button.isSelected = false
                    selectedButtons.remove(button)
                }

                else -> { /* Do nothing for NONE */
                }
            }

            // Remove special item from button
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            specialButtons.remove(button)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling special item: ${e.message}")
        }
    }

    private fun showFloatingText(text: String, sourceView: View) {
        // Create a TextView and animate it floating up and fading out
        val floatingText = TextView(this)
        floatingText.text = text
        floatingText.setTextColor(Color.YELLOW)
        floatingText.textSize = 18f
        floatingText.setShadowLayer(3f, 2f, 2f, Color.BLACK)

        // Set font to Luckiest Guy for exciting text
        if (text.contains("DOUBLE") || text.contains("LEVEL") || text.contains("+")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                floatingText.typeface = resources.getFont(R.font.luckiest_guy)
            } else {
                floatingText.typeface = ResourcesCompat.getFont(this, R.font.luckiest_guy)
            }
        }

        // Add to layout at the same position as source view
        val rootView = findViewById<View>(android.R.id.content)
        val containerLayout = rootView as android.view.ViewGroup
        containerLayout.addView(floatingText)

        // Position the text
        val location = IntArray(2)
        sourceView.getLocationInWindow(location)
        floatingText.x = location[0].toFloat()
        floatingText.y = location[1].toFloat()

        // Animate floating up and fading out
        floatingText.animate()
            .translationYBy(-200f)
            .alpha(0f)
            .setDuration(1000)
            .withEndAction {
                containerLayout.removeView(floatingText)
            }
            .start()
    }

    private fun showSpecialEffect(resourceId: Int) {
        lifecycleScope.launch {  // Launch a coroutine in the lifecycle scope
            // Create an ImageView for the effect
            val effectImage = ImageView(this@MainActivity)  // Use this@MainActivity
            effectImage.setImageResource(resourceId)

            // Add to layout centered
            val rootView = findViewById<View>(android.R.id.content)
            val containerLayout = rootView as android.view.ViewGroup
            containerLayout.addView(effectImage)

            // Center the effect
            effectImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
            val params = effectImage.layoutParams as android.view.ViewGroup.LayoutParams
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            params.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            effectImage.layoutParams = params

            // Start with small size and expand
            effectImage.scaleX = 0.1f
            effectImage.scaleY = 0.1f
            effectImage.alpha = 0.9f

            // Animate explosion
            effectImage.animate()
                .scaleX(2f)
                .scaleY(2f)
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    containerLayout.removeView(effectImage)
                }
                .start()
        }
    }

    private fun updateCurrentSum() {
        try {
            // Filter out special items from calculation
            val selectedNumbers = selectedButtons
                .filter { !specialButtons.containsKey(it) }
                .map { it.text.toString().toInt() }

            currentSum = when (currentOperation) {
                Operation.ADDITION -> selectedNumbers.sum()
                Operation.SUBTRACTION -> {
                    if (selectedNumbers.isEmpty()) 0
                    else {
                        var result = selectedNumbers.first()
                        for (i in 1 until selectedNumbers.size) {
                            result -= selectedNumbers[i]
                        }
                        result
                    }
                }

                Operation.MULTIPLICATION -> {
                    if (selectedNumbers.isEmpty()) 0
                    else {
                        var result = 1
                        for (num in selectedNumbers) {
                            result *= num
                        }
                        result
                    }
                }

                Operation.DIVISION -> {
                    if (selectedNumbers.size < 2) {
                        if (selectedNumbers.isEmpty()) 0 else selectedNumbers.first()
                    } else {
                        var result = selectedNumbers.first().toFloat()
                        for (i in 1 until selectedNumbers.size) {
                            if (selectedNumbers[i] != 0) {
                                result /= selectedNumbers[i]
                            }
                        }
                        result.toInt()
                    }
                }
            }

            currentSumText.text = currentSum.toString()

            // Animate if close to target
            if (currentSum > 0 && Math.abs(currentSum - targetNumber) <= 3) {
                animateTextScale(currentSumText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating current sum: ${e.message}")
        }
    }

    private fun checkForMatch() {
        try {
            if (currentSum == targetNumber) {
                // Correct match
                try {
                    if (soundCorrect > 0) {
                        soundPool.play(soundCorrect, 1f, 1f, 1, 0, 1f)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing correct sound: ${e.message}")
                }

                try {
                    vibrateFeedback(100)
                } catch (e: Exception) {
                    Log.e(TAG, "Error with vibration: ${e.message}")
                }

                // Check for double points
                var pointMultiplier = 1
                for (button in selectedButtons) {
                    if (button.tag == "double_points") {
                        pointMultiplier = 2
                        button.tag = null
                        break
                    }
                }

                // Increase score with combo multiplier
                combo++
                val comboMultiplier = minOf(combo, 5) // Cap combo multiplier at 5x
                val earnedPoints = level * pointMultiplier * comboMultiplier
                score += earnedPoints

                // Track max combo
                if (combo > maxCombo) {
                    maxCombo = combo
                }

                // Show floating score
                try {
                    val button = selectedButtons.first()
                    showFloatingText("+$earnedPoints", button)
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing floating text: ${e.message}")
                }

                // Update UI
                updateUI()

                // Show combo effect if combo > 1
                if (combo > 1) {
                    try {
                        animateTextScale(comboText)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error animating combo text: ${e.message}")
                    }
                }

                // Check for level up
                val levelThreshold = level * 5 // 5 points per level
                if (score >= levelThreshold && level < 10) {
                    levelUp()
                }

                // Reset game grid with new target and numbers
                resetGameGrid()

                // Add a little time as reward
                val bonusTime = 1000L
                timeLeftInMillis = minOf(maxTimeInMillis, timeLeftInMillis + bonusTime)
                updateTimerUI()
            } else if ((currentOperation == Operation.ADDITION && currentSum > targetNumber) ||
                (currentOperation == Operation.SUBTRACTION && selectedButtons.size >= 2 && currentSum != targetNumber) ||
                (currentOperation == Operation.MULTIPLICATION && currentSum > targetNumber && selectedButtons.size >= 2) ||
                (currentOperation == Operation.DIVISION && selectedButtons.size >= 2 && currentSum != targetNumber)
            ) {
                // Wrong combination
                try {
                    if (soundIncorrect > 0) {
                        soundPool.play(soundIncorrect, 0.7f, 0.7f, 1, 0, 1f)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing incorrect sound: ${e.message}")
                }

                try {
                    vibrateFeedback(50)
                } catch (e: Exception) {
                    Log.e(TAG, "Error with vibration: ${e.message}")
                }

                // Reset combo
                combo = 0
                comboText.visibility = View.INVISIBLE

                // Shake the current sum text to indicate error
                try {
                    val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                    currentSumText.startAnimation(shakeAnimation)
                } catch (e: Exception) {
                    Log.e(TAG, "Error with shake animation: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for match: ${e.message}")
        }
    }

    private fun levelUp() {
        try {
            level++

            // Play level up sound
            soundPool.play(soundLevelUp, 1f, 1f, 1, 0, 1f)

            // Show level up effect
            showLevelUpAnimation()

            // Get new level settings
            val settings = levelSettings[level]
                ?: levelSettings[10]!! // Use level 10 settings if beyond
            availableOperations = settings.operations

            // Update time for the new level
            val timeInSeconds = settings.timeInSeconds
            timeLeftInMillis = timeInSeconds * 1000L
            maxTimeInMillis = timeLeftInMillis

            // Update UI
            updateUI()
            animateTextScale(levelText)

            // Reset game grid with new operations and numbers
            resetGameGrid()

            // Reset timer
            countDownTimer?.cancel()
            startCountdownTimer()
        } catch (e: Exception) {
            Log.e(TAG, "Error leveling up: ${e.message}")
        }
    }

    private fun showLevelUpAnimation() {
        try {
            // Create level up text animation
            val levelUpText = TextView(this)
            levelUpText.text = "LEVEL UP!"
            levelUpText.setTextColor(Color.YELLOW)
            levelUpText.textSize = 36f
            levelUpText.setShadowLayer(5f, 3f, 3f, Color.BLACK)

            // Try to set the font, but don't worry if it fails
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    levelUpText.typeface = resources.getFont(R.font.luckiest_guy)
                } else {
                    levelUpText.typeface = ResourcesCompat.getFont(this, R.font.luckiest_guy)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting font: ${e.message}")
                // Continue with default font
            }

            // Add to layout
            try {
                val rootView = findViewById<View>(android.R.id.content)
                val containerLayout = rootView as android.view.ViewGroup
                containerLayout.addView(levelUpText)

                // Center the text
                val params = levelUpText.layoutParams as android.view.ViewGroup.LayoutParams
                params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
                params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                levelUpText.layoutParams = params
                levelUpText.gravity = android.view.Gravity.CENTER
                levelUpText.y = rootView.height / 2 - 100f

                // Use a simpler animation approach
                levelUpText.scaleX = 0.1f
                levelUpText.scaleY = 0.1f
                levelUpText.alpha = 1f

                // Use a handler for animation sequence instead of chaining
                val handler = android.os.Handler(android.os.Looper.getMainLooper())

                // First animation - scale up
                val scaleAnim = android.animation.ValueAnimator.ofFloat(0.1f, 2f)
                scaleAnim.duration = 500
                scaleAnim.addUpdateListener { animator ->
                    val value = animator.animatedValue as Float
                    levelUpText.scaleX = value
                    levelUpText.scaleY = value
                }
                scaleAnim.start()

                // Second animation after delay - fade out
                handler.postDelayed({
                    val fadeAnim = android.animation.ValueAnimator.ofFloat(1f, 0f)
                    fadeAnim.duration = 500
                    fadeAnim.addUpdateListener { animator ->
                        val value = animator.animatedValue as Float
                        levelUpText.alpha = value
                    }
                    fadeAnim.start()

                    // Remove view after full animation
                    handler.postDelayed({
                        try {
                            containerLayout.removeView(levelUpText)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error removing level up text: ${e.message}")
                        }
                    }, 500)
                }, 500)

            } catch (e: Exception) {
                Log.e(TAG, "Error with level up animation: ${e.message}")
            }

            // Vibration feedback
            try {
                vibrateFeedback(200)
            } catch (e: Exception) {
                Log.e(TAG, "Error with vibration: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in showLevelUpAnimation: ${e.message}")
        }
    }

    private fun clearSelection() {
        try {
            // Deselect all buttons
            for (button in selectedButtons) {
                button.isSelected = false
                button.setBackgroundResource(R.drawable.glossy_button)
            }

            // Clear the selected buttons list
            selectedButtons.clear()

            // Reset current sum
            currentSum = 0
            currentSumText.text = "0"
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing selection: ${e.message}")
        }
    }

    private fun startCountdownTimer() {
        try {
            countDownTimer?.cancel()

            countDownTimer = object : CountDownTimer(timeLeftInMillis, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerUI()
                }

                override fun onFinish() {
                    timeLeftInMillis = 0
                    updateTimerUI()
                    handleTimeOut()
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting countdown timer: ${e.message}")
        }
    }

    private fun updateTimerUI() {
        // Update timer text
        val secondsLeft = (timeLeftInMillis / 1000f).toInt()
        timerText.text = secondsLeft.toString()

        // Update progress bar
        timerProgressBar.max = maxTimeInMillis.toInt()
        timerProgressBar.progress = timeLeftInMillis.toInt()

        // Change color when time is running low
        if (timeLeftInMillis < 3000) {
            timerText.setTextColor(Color.RED)

            // Pulse animation when time is low
            if (timeLeftInMillis < 2000 && timeLeftInMillis % 500 < 250) {
                val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
                timerText.startAnimation(pulseAnimation)
            }
        } else {
            timerText.setTextColor(ContextCompat.getColor(this, R.color.timer_normal))
        }
    }

    private fun restartTimer() {
        countDownTimer?.cancel()
        startCountdownTimer()
    }

    private fun handleTimeOut() {
        try {
            // Vibrate to indicate time out
            vibrateFeedback(300)

            // Play timeout sound
            soundPool.play(soundIncorrect, 1f, 1f, 1, 0, 1f)

            // Lose a life
            lives--
            livesText.text = "❤".repeat(lives)

            // Shake lives indicator
            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
            livesText.startAnimation(shakeAnimation)

            // Reset combo
            combo = 0
            comboText.visibility = View.INVISIBLE

            if (lives <= 0) {
                // Game over
                isGameActive = false
                showGameOverDialog()
            } else {
                // Continue with new time
                val settings = levelSettings[level] ?: levelSettings[1]!!
                timeLeftInMillis = settings.timeInSeconds * 1000L
                maxTimeInMillis = timeLeftInMillis

                // Reset grid and start timer
                resetGameGrid()
                startCountdownTimer()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling timeout: ${e.message}")
        }
    }

    private fun showGameOverDialog() {
        try {
            // Play game over sound
            soundPool.play(soundGameOver, 1f, 1f, 1, 0, 1f)

            // Check if we have a new high score
            if (score > highScore) {
                highScore = score

                // Save high score to SharedPreferences
                val sharedPref = getSharedPreferences("SumRushPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("highScore", highScore)
                    apply()
                }
                Log.d(TAG, "New high score: $highScore")
            }

            try {
                // Create game over dialog
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog_game_over)
                dialog.setCancelable(false)

                // Set window to match parent width
                val layoutParams = android.view.WindowManager.LayoutParams()
                layoutParams.copyFrom(dialog.window?.attributes)
                layoutParams.width = android.view.WindowManager.LayoutParams.MATCH_PARENT
                dialog.window?.attributes = layoutParams

                // Apply custom animation
                dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

                // Set score and high score
                val finalScoreText = dialog.findViewById<TextView>(R.id.finalScoreText)
                finalScoreText?.text = score.toString()

                val highScoreText = dialog.findViewById<TextView>(R.id.highScoreText)
                highScoreText?.text = highScore.toString()

                // Show max level reached
                val levelReachedText = dialog.findViewById<TextView>(R.id.levelReachedText)
                levelReachedText?.text = level.toString()

                // Show max combo
                val maxComboText = dialog.findViewById<TextView>(R.id.maxComboText)
                maxComboText?.text = maxCombo.toString()

                // Set up play again button
                val playAgainButton = dialog.findViewById<Button>(R.id.playAgainButton)
                playAgainButton?.setOnClickListener {
                    soundPool.play(soundClick, 1f, 1f, 1, 0, 1f)
                    dialog.dismiss()
                    startNewGame()
                }

                dialog.show()
                Log.d(TAG, "Game over dialog shown")
            } catch (e: Exception) {
                // If dialog fails, fallback to a toast and restart
                Log.e(TAG, "Error showing game over dialog: ${e.message}")
                Toast.makeText(
                    this,
                    "Game Over! Score: $score, High Score: $highScore",
                    Toast.LENGTH_LONG
                ).show()
                startNewGame()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in game over handling: ${e.message}")
            Toast.makeText(this, "Game Over! Score: $score", Toast.LENGTH_LONG).show()
            startNewGame()
        }
    }

    private fun animateTextScale(textView: TextView) {
        // Create scale animation
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_bounce)
        textView.startAnimation(scaleAnimation)
    }

    private fun vibrateFeedback(duration: Long) {
        try {
            // Check Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use the new VibrationEffect API for Android Oreo and above
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // Use the deprecated method with a suppression annotation for older versions
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with vibration: ${e.message}")
        }
    }

    // Helper method to validate critical resources
    private fun validateResources(): Boolean {
        return try {
            // Check critical resources
            resources.getResourceName(R.layout.activity_main)
            resources.getResourceName(R.drawable.glossy_button)
            resources.getResourceName(R.raw.correct)
            true
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Missing critical resources", e)
            false
        }
    }

    // Safe sound loading method
    private fun safeLoadSound(resourceId: Int): Int {
        return try {
            soundPool.load(this, resourceId, 1)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound $resourceId", e)
            0 // Return 0 if sound loading fails
        }
    }

    // Safe UI element initialization
    private fun initializeUIElements() {
        try {
            levelText = findViewById(R.id.levelText) ?: throw IllegalStateException("levelText not found")
            livesText = findViewById(R.id.livesText) ?: throw IllegalStateException("livesText not found")
            // ... (continue with other UI element initializations)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UI elements", e)
            throw e
        }
    }

    // Safe button setup
    private fun setupButtons() {
        try {
            // Setup reset button
            resetButton = findViewById(R.id.resetButton)
            resetButton.setOnClickListener {
                try {
                    soundPool.play(soundClick, 1f, 1f, 1, 0, 1f)
                    vibrateFeedback(50)
                    clearSelection()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in reset button click", e)
                }
            }

            // Setup change operation button
            changeOperationButton = findViewById(R.id.changeOperationButton)
            changeOperationButton.setOnClickListener {
                try {
                    soundPool.play(soundClick, 1f, 1f, 1, 0, 1f)
                    vibrateFeedback(50)
                    changeOperation()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in change operation button click", e)
                }
            }

            // Initialize button grid
            initializeButtonGrid()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up buttons", e)
            throw e
        }
    }

    override fun onDestroy() {
        try {
            countDownTimer?.cancel()
            soundPool.release()
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
            super.onDestroy()
        }
    }
} // End of MainActivity class