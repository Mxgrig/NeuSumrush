package com.xtenalyze.sumrush

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val TAG = "SumRush"

    // Game state variables
    private var score = 0
    private var targetNumber = 0
    private var currentSum = 0
    private var startTime = 10000L // 10 seconds in milliseconds
    private var currentTime = startTime
    private var timeReductionRate = 500L // Reduce time by 500ms per level
    private var minimumTime = 3000L // Minimum time of 3 seconds
    private var isGameActive = false
    private var highScore = 0

    // Operation mode
    private enum class Operation {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION
    }
    private var currentOperation = Operation.ADDITION

    // UI elements
    private lateinit var operationIndicator: TextView
    private lateinit var instructionsText: TextView
    private lateinit var targetNumberText: TextView
    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView
    private lateinit var currentSumText: TextView
    private lateinit var resetButton: Button
    private lateinit var changeOperationButton: Button
    private lateinit var buttonGrid: Array<Array<Button>>
    private var selectedButtons = mutableListOf<Button>()

    // Timer
    private var countDownTimer: CountDownTimer? = null

    // Vibrator for haptic feedback
    private lateinit var vibrator: Vibrator

    // Tone generator for sound feedback
    private lateinit var toneGenerator: ToneGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // Initialize UI elements
            operationIndicator = findViewById(R.id.operationIndicator)
            instructionsText = findViewById(R.id.instructionsText)
            targetNumberText = findViewById(R.id.targetNumberText)
            timerText = findViewById(R.id.timerText)
            scoreText = findViewById(R.id.scoreText)
            currentSumText = findViewById(R.id.currentSumText)
            resetButton = findViewById(R.id.resetButton)
            changeOperationButton = findViewById(R.id.changeOperationButton)

            // Initialize vibrator using the appropriate method based on Android version
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

            // Initialize button grid
            initializeButtonGrid()

            // Get high score from SharedPreferences
            val sharedPref = getSharedPreferences("SumRushPrefs", Context.MODE_PRIVATE)
            highScore = sharedPref.getInt("highScore", 0)

            // Set up reset button
            resetButton.setOnClickListener {
                clearSelection()
            }

            // Set up change operation button
            changeOperationButton.setOnClickListener {
                changeOperation()
            }

            // Update operation display initially
            updateOperationDisplay()

            // Start a new game
            startNewGame()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            // Show a toast with the error message
            Toast.makeText(this, "Error initializing game: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

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
            currentTime = startTime
            isGameActive = true

            // Update UI
            scoreText.text = score.toString()

            // Fill grid with numbers and set target
            resetGameGrid()

            // Start the countdown timer
            startCountdownTimer()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting new game: ${e.message}")
        }
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
            // Change to next operation
            currentOperation = when (currentOperation) {
                Operation.ADDITION -> Operation.SUBTRACTION
                Operation.SUBTRACTION -> Operation.MULTIPLICATION
                Operation.MULTIPLICATION -> Operation.DIVISION
                Operation.DIVISION -> Operation.ADDITION
            }

            // Update operation display
            updateOperationDisplay()

            // Generate new numbers and target
            resetGameGrid()

            // Reset timer
            countDownTimer?.cancel()
            startCountdownTimer()

            // Provide feedback for operation change
            playCorrectSound()
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
            val scaleAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            operationIndicator.startAnimation(scaleAnimation)
            instructionsText.startAnimation(scaleAnimation)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating operation display: ${e.message}")
        }
    }

    private fun generateTargetAndFillGrid() {
        try {
            // Generate grid numbers based on operation
            val gridNumbers = Array(4) { Array(4) { 0 } }

            // Fill the grid with appropriate numbers based on operation
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    gridNumbers[row][col] = when (currentOperation) {
                        Operation.ADDITION -> Random.nextInt(1, 10)  // 1-9
                        Operation.SUBTRACTION -> Random.nextInt(1, 16) // 1-15
                        Operation.MULTIPLICATION -> Random.nextInt(1, 6) // 1-5
                        Operation.DIVISION -> {
                            // For division, use numbers that often divide evenly
                            listOf(2, 3, 4, 5, 6, 8, 10).random()
                        }
                    }
                }
            }

            // Choose a number of buttons to include in the solution
            val solutionSize = when (currentOperation) {
                Operation.ADDITION -> Random.nextInt(2, 5)  // 2-4 numbers
                Operation.SUBTRACTION -> 2  // Usually 2 numbers for subtraction
                Operation.MULTIPLICATION -> Random.nextInt(2, 4)  // 2-3 numbers
                Operation.DIVISION -> 2  // Usually 2 numbers for division
            }

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
                    val numbers = solutionPositions.map { gridNumbers[it.first][it.second] }
                    numbers.first() - numbers.last()
                }
                Operation.MULTIPLICATION -> {
                    var result = 1
                    for (pos in solutionPositions) {
                        result *= gridNumbers[pos.first][pos.second]
                    }
                    result
                }
                Operation.DIVISION -> {
                    val numbers = solutionPositions.map { gridNumbers[it.first][it.second] }
                    val result = numbers.first() / numbers.last()
                    // Ensure we get a clean division by setting the first number to be product
                    gridNumbers[solutionPositions.first().first][solutionPositions.first().second] =
                        numbers.last() * result
                    result
                }
            }

            // Update the target number text
            targetNumberText.text = targetNumber.toString()

            // Fill the button grid with the generated numbers
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    buttonGrid[row][col].text = gridNumbers[row][col].toString()

                    // Apply animation to button
                    val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                    buttonGrid[row][col].startAnimation(animation)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating target and filling grid: ${e.message}")
        }
    }

    private fun onNumberButtonClick(button: Button) {
        try {
            if (!isGameActive || button.isSelected) return

            // Apply selection
            button.isSelected = true
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_selected))
            selectedButtons.add(button)

            // Update current sum
            updateCurrentSum()

            // Check if current selection equals target
            checkForMatch()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling button click: ${e.message}")
        }
    }

    private fun updateCurrentSum() {
        try {
            val selectedNumbers = selectedButtons.map { it.text.toString().toInt() }

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
        } catch (e: Exception) {
            Log.e(TAG, "Error updating current sum: ${e.message}")
        }
    }

    private fun checkForMatch() {
        try {
            if (currentSum == targetNumber) {
                // Correct match
                playCorrectSound()

                // Increase score
                score++
                scoreText.text = score.toString()

                // Reset game grid with new target and numbers
                resetGameGrid()

                // Increase difficulty (reduce time)
                adjustDifficulty()

                // Reset countdown timer with new difficulty
                countDownTimer?.cancel()
                startCountdownTimer()
            } else if ((currentOperation == Operation.ADDITION && currentSum > targetNumber) ||
                (currentOperation == Operation.SUBTRACTION && selectedButtons.size >= 2) ||
                (currentOperation == Operation.MULTIPLICATION && currentSum > targetNumber && selectedButtons.size >= 2) ||
                (currentOperation == Operation.DIVISION && selectedButtons.size >= 2)) {
                // Provide feedback for incorrect combination
                playIncorrectSound()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for match: ${e.message}")
        }
    }

    private fun adjustDifficulty() {
        try {
            // Reduce available time for each correct answer
            currentTime = (currentTime - timeReductionRate).coerceAtLeast(minimumTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting difficulty: ${e.message}")
        }
    }

    private fun clearSelection() {
        try {
            // Deselect all buttons
            for (button in selectedButtons) {
                button.isSelected = false
                button.setBackgroundColor(Color.WHITE)
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

            countDownTimer = object : CountDownTimer(currentTime, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    timerText.text = (millisUntilFinished / 1000.0).toInt().toString()

                    // Change color when time is running low
                    if (millisUntilFinished < 3000) {
                        timerText.setTextColor(Color.RED)
                    } else {
                        timerText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.timer_normal))
                    }
                }

                override fun onFinish() {
                    timerText.text = "0"
                    isGameActive = false
                    showGameOverDialog()
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting countdown timer: ${e.message}")
        }
    }

    private fun showGameOverDialog() {
        try {
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

                // Set score and high score
                val finalScoreText = dialog.findViewById<TextView>(R.id.finalScoreText)
                finalScoreText?.text = score.toString()

                val highScoreText = dialog.findViewById<TextView>(R.id.highScoreText)
                highScoreText?.text = highScore.toString()

                // Set up play again button
                val playAgainButton = dialog.findViewById<Button>(R.id.playAgainButton)
                playAgainButton?.setOnClickListener {
                    dialog.dismiss()
                    startNewGame()
                }

                dialog.show()
                Log.d(TAG, "Game over dialog shown")
            } catch (e: Exception) {
                // If dialog fails, fallback to a toast and restart
                Log.e(TAG, "Error showing game over dialog: ${e.message}")
                Toast.makeText(this, "Game Over! Score: $score, High Score: $highScore", Toast.LENGTH_LONG).show()
                startNewGame()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in game over handling: ${e.message}")
            Toast.makeText(this, "Game Over! Score: $score", Toast.LENGTH_LONG).show()
            startNewGame()
        }
    }

    private fun playCorrectSound() {
        try {
            // Play a tone
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)

            // Vibrate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing correct sound: ${e.message}")
        }
    }

    private fun playIncorrectSound() {
        try {
            // Play a different tone for incorrect
            toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 100)

            // Vibrate with a different pattern
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing incorrect sound: ${e.message}")
        }
    }

    override fun onDestroy() {
        try {
            countDownTimer?.cancel()
            toneGenerator.release()
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
            super.onDestroy()
        }
    }
}