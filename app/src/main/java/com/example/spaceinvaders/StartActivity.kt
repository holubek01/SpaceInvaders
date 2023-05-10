package com.example.spaceinvaders

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputEditText
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        setContentView(R.layout.activity_start)

        findViewById<Button>(R.id.startBtn).setOnClickListener{
            val pseudonim = findViewById<TextInputEditText>(R.id.pseudonim).text.toString()

            if(pseudonim.isBlank()) {
                MotionToast.createColorToast(
                    this,
                    "Uwaga",
                    "Pseudonim nie może być pusty",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            } else {
                val myIntent = Intent(this, MainActivity::class.java).apply {
                    putExtra("pseudonim", pseudonim)
                }
                startActivity(myIntent)
            }
        }

        findViewById<Button>(R.id.resultsBtn).setOnClickListener{
            val myIntent = Intent(this, ShowResultsActivity::class.java)
            startActivity(myIntent)
        }
    }
}
