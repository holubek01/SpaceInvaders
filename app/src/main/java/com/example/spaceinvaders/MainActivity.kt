package com.example.spaceinvaders

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.hardware.display.DisplayManagerCompat
import com.example.spaceinvaders.db.ResultDAO
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    //cuntomowy widok będzie odpowiedzialny za wyświetlanie oraz logikę
    var customView:CustomView? = null
    lateinit var pseudonim: String
    //private lateinit var itemDao: ResultDAO

    @Inject
    lateinit var itemDao: ResultDAO

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

        val defaultDisplay = DisplayManagerCompat.getInstance(this@MainActivity).getDisplay(Display.DEFAULT_DISPLAY)
        val size = Point()

        pseudonim = intent.getStringExtra("pseudonim")!!

        //pobierz size ekranu to zmiennej size
        defaultDisplay!!.getRealSize(size)
        (applicationContext as MyApplication).appComponent.inject(this)
        customView = CustomView(this, size, itemDao, pseudonim, supportFragmentManager)
        setContentView(customView)

    }


    override fun onResume() {
        super.onResume()
        customView?.resume()
    }

    //gdy użytkownik przechodzi do nowego ekranu to pauzuj aktywność (zwalnia zasoby)
    override fun onPause() {
        super.onPause()
        customView?.pause()
    }
}