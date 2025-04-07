package com.xtenalyze.sumrush

import android.Manifest
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
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

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

    // UI elements
    private lateinit var targetNumberText: TextView
    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView
    private lateinit var currentSumText: TextView
    private lateinit var resetButton: Button
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

        // Initialize UI elements
        targetNumberText = findViewById(R.id.targetNumberText)
        timerText = findViewById(R.id.timerText)
        scoreText = findViewById(R.id.scoreText)
        currentSumText = findViewById(R.id.currentSumText)
        resetButton = findViewById(R.id.resetButton)

        // Initialize vibrator and tone generator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

        // Start a new game
        startNewGame()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeButtonGrid() {
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
    }

    private fun startNewGame() {
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
    }

    private fun resetGameGrid() {
        // Clear selected buttons
        clearSelection()

        // Generate a valid target and fill the grid
        generateTargetAndFillGrid()
    }

    private fun generateTargetAndFillGrid() {
        // Fill the grid with random numbers 1-9
        val gridNumbers = Array(4) { Array(4) { Random.nextInt(1, 10) } }

        // Choose a number of buttons to include in the solution (2-4 buttons)
        val solutionSize = Random.nextInt(2, 5)

        // Pick random positions for the solution
        val solutionPositions = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until solutionSize) {
            var row: Int
            var col: Int
            do {
                row = Random.nextInt(4)
                col = Random.nextInt(4)
            } while (solutionPositions.contains(Pair(row, col)))
            solutionPositions.add(Pair(row, col))
        }

        // Set the target number as the sum of the numbers at solution positions
        targetNumber = solutionPositions.sumOf { gridNumbers[it.first][it.second] }

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
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun onNumberButtonClick(button: Button) {
        if (!isGameActive || button.isSelected) return

        // Apply selection
        button.isSelected = true
        button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))
        selectedButtons.add(button)

        // Update current sum
        updateCurrentSum()

        // Check if current sum equals target
        checkForMatch()
    }

    private fun updateCurrentSum() {
        currentSum = selectedButtons.sumOf { it.text.toString().toInt() }
        currentSumText.text = currentSum.toString()
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkForMatch() {
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
        } else if (currentSum > targetNumber) {
            // Sum exceeds target - provide feedback
            playIncorrectSound()
        }
    }

    private fun adjustDifficulty() {
        // Reduce available time for each correct answer
        currentTime = (currentTime - timeReductionRate).coerceAtLeast(minimumTime)
    }

    private fun clearSelection() {
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
    }

    private fun startCountdownTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(currentTime, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = (millisUntilFinished / 1000.0).toInt().toString()

                // Change color when time is running low
                if (millisUntilFinished < 3000) {
                    timerText.setTextColor(Color.RED)
                } else {
                    timerText.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
                }
            }

            override fun onFinish() {
                timerText.text = "0"
                isGameActive = false
                showGameOverDialog()
            }
        }.start()
    }

    private fun showGameOverDialog() {
        // Check if we have a new high score
        if (score > highScore) {
            highScore = score

            // Save high score to SharedPreferences
            val sharedPref = getSharedPreferences("SumRushPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("highScore", highScore)
                apply()
            }
        }

        // Create game over dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_game_over)
        dialog.setCancelable(false)

        // Set score and high score
        val finalScoreText = dialog.findViewById<TextView>(R.id.finalScoreText)
        finalScoreText.text = score.toString()

        val highScoreText = dialog.findViewById<TextView>(R.id.highScoreText)
        highScoreText.text = highScore.toString()

        // Set up play again button
        val playAgainButton = dialog.findViewById<Button>(R.id.playAgainButton)
        playAgainButton.setOnClickListener {
            dialog.dismiss()
            startNewGame()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun playCorrectSound() {
        // Play a tone
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)

        // Vibrate
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun playIncorrectSound() {
        // Play a different tone for incorrect
        toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 100)

        // Vibrate with a different pattern
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        toneGenerator.release()
        super.onDestroy()
    }
}

