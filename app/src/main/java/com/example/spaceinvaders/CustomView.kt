package com.example.spaceinvaders

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.spaceinvaders.db.Result
import com.example.spaceinvaders.db.ResultDAO
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

//Surface View pozwala na obliczanie różnych rzeczy w czasie rysowania (inny wątek)

class CustomView(
    context: Context,
    private val size: Point,
    private val itemDao: ResultDAO,
    private val pseudonim: String,
    private val supportFragmentManager: FragmentManager

):SurfaceView(context), Runnable{

    private val gameThread = Thread(this)
    private var playing = false
    private var invadersize = 35f

    private var uhOrOh: Boolean = false
    private var pause = true
    private var canvas: Canvas = Canvas()
    private val paint: Paint = Paint()
    private val invadersBullets = ArrayList<Bullet>()
    private var nextBullet = 0
    private val maxInvaderBullets = 10
    private var playerBullet = Bullet(size.y, 1200f, 40f)
    private var playerShip: PlayerShip = PlayerShip(context, size.x, size.y )
    private var score = 0
    private var waves = 1
    private var lost = false
    private var lives = 3
    private var highScore = 0
    private val invaders = ArrayList<Invader>()
    private var numInvaders = 0
    private val bricks = ArrayList<Brick>()
    private var numBricks: Int = 0
    private var menaceInterval: Long = 1000
    private var lastMenaceTime = System.currentTimeMillis()


    @OptIn(DelicateCoroutinesApi::class)
    override fun run() {

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            invadersize = 20F
        }
        var fps: Long = 0

        GlobalScope.launch { highScore = itemDao.getBest() }

        while (playing) {
            val startTime = System.currentTimeMillis()

            if (!pause) {
                update(fps)
            }

            draw()

            val timeThisFrame = System.currentTimeMillis() - startTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame
            }

            if (!pause && ((startTime - lastMenaceTime) > menaceInterval))
                menacePlayer()
        }
    }


    private fun menacePlayer() {
        lastMenaceTime = System.currentTimeMillis()
        uhOrOh = !uhOrOh
    }


    private fun update(fps: Long) {
        playerShip.update(fps)
        var bumped = false
        lost = false

        for (invader in invaders) {
            if (invader.isVisible) {
                invader.update(fps)

                if (invader.takeAim(playerShip.position.left, playerShip.width, waves)) {
                    val invaderBullet = invadersBullets[nextBullet]
                    if (invaderBullet.shoot(
                            invader.position.left + invader.width / 2,
                            invader.position.top,
                            playerBullet.down
                        )
                    ) {
                        nextBullet = (nextBullet + 1) % maxInvaderBullets
                    }
                }

                if (invader.position.left > size.x - invader.width || invader.position.left < 0) {
                    bumped = true
                }
            }
        }

        playerBullet.update(fps)

        for (bullet in invadersBullets) {
            bullet.update(fps)
        }

        if (bumped) {
            for (invader in invaders) {
                invader.dropDownAndReverse(waves)
                if (invader.position.bottom >= size.y && invader.isVisible) {
                    lost = true
                }
            }
        }

        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive = false
        }

        for (bullet in invadersBullets) {
            if (bullet.position.top > size.y) {
                bullet.isActive = false
            }
        }

        if (playerBullet.isActive) {
            for (invader in invaders) {
                if (invader.isVisible && RectF.intersects(playerBullet.position, invader.position)) {
                    invader.isVisible = false
                    playerBullet.isActive = false
                    Invader.numberOfInvaders--

                    score += if (uhOrOh) {
                        20
                    } else {
                        10
                    }

                    if (score > highScore) {
                        highScore = score
                    }

                    if (Invader.numberOfInvaders == 0) {
                        pause = true
                        lives++
                        invaders.clear()
                        bricks.clear()
                        invadersBullets.clear()
                        prepareLevel()
                        waves++
                        break
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                for (brick in bricks) {
                    if (brick.isVisible && RectF.intersects(bullet.position, brick.position)) {
                        bullet.isActive = false
                        brick.isVisible = false
                        //soundPlayer.playSound(SoundPlayer.damageShelterID)
                    }
                }
            }
        }

        if (playerBullet.isActive) {
            for (brick in bricks) {
                if (brick.isVisible && RectF.intersects(playerBullet.position, brick.position)) {
                    playerBullet.isActive = false
                    brick.isVisible = false
                    //soundPlayer.playSound(SoundPlayer.damageShelterID)
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive && RectF.intersects(playerShip.position, bullet.position)) {
                bullet.isActive = false
                lives--

                if (lives == 0) {
                    lost = true
                    break
                }
            }
        }

        if (lost) {
            val currentScore = score

            invaders.clear()
            bricks.clear()
            invadersBullets.clear()
            lives = 3
            score = 0
            waves = 1
            prepareLevel()
            pause = true

            val item = Result(currentScore, LocalDate.now().toString(), pseudonim)
            GlobalScope.launch { itemDao.insertAll(item) }

            class MyCustomDialog : DialogFragment() {
                override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?
                ): View {
                    dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
                    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val rootView: View = inflater.inflate(R.layout.dialog_score, container, false)
                    rootView.findViewById<Button>(R.id.exit_btn).setOnClickListener { requireActivity().finishAffinity() }
                    rootView.findViewById<Button>(R.id.restart_btn).setOnClickListener {
                        dismiss()
                    }

                    val name = rootView.findViewById<TextView>(R.id.info)
                    name.append(currentScore.toString())
                    return rootView
                }

                override fun onStart() {
                    super.onStart()
                    val defaultDisplay = DisplayManagerCompat.getInstance(requireContext()).getDisplay(
                        Display.DEFAULT_DISPLAY
                    )
                    val displayContext = requireContext().createDisplayContext(defaultDisplay!!)
                    val width = displayContext.resources.displayMetrics.widthPixels
                    val height = displayContext.resources.displayMetrics.heightPixels

                    dialog?.window?.setLayout((width * 0.9).toInt(), (height * 0.8).toInt())
                }
            }

            val dialog = MyCustomDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.show(supportFragmentManager, "CustomDialog")
            }, 500)
        }
    }




/*
    private fun update(fps: Long) {
        playerShip.update(fps)
        var bumped = false
        lost = false

        for (invader in invaders) {
            if (invader.isVisible) {
                invader.update(fps)

                if (invader.takeAim(playerShip.position.left, playerShip.width, waves)) {
                    if (invadersBullets[nextBullet].shoot(
                            invader.position.left
                                    + invader.width / 2,
                            invader.position.top,
                            playerBullet.down)) {

                        nextBullet++

                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0
                        }
                    }
                }

                if (invader.position.left > size.x - invader.width
                    || invader.position.left < 0) {
                    bumped = true

                }
            }
        }

        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                bullet.update(fps)
            }
        }

        if (bumped) {

            for (invader in invaders) {
                invader.dropDownAndReverse(waves)
                if (invader.position.bottom >= size.y && invader.isVisible) {
                    lost = true
                }
            }
        }

        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive = false
        }

        for (bullet in invadersBullets) {
            if (bullet.position.top > size.y) {
                bullet.isActive = false
            }
        }

        if (playerBullet.isActive) {
            for (invader in invaders) {
                if (invader.isVisible) {
                    if (RectF.intersects(playerBullet.position, invader.position)) {
                        invader.isVisible = false

                        playerBullet.isActive = false
                        Invader.numberOfInvaders--

                        score += if (uhOrOh) {
                            20
                        } else {
                            10
                        }

                        if (score > highScore) {
                            highScore = score
                        }

                        if (Invader.numberOfInvaders == 0) {
                            pause = true
                            lives++
                            invaders.clear()
                            bricks.clear()
                            invadersBullets.clear()
                            prepareLevel()
                            waves++
                            break
                        }

                        break
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                for (brick in bricks) {
                    if (brick.isVisible) {
                        if (RectF.intersects(bullet.position, brick.position)) {
                            // A collision has occurred
                            bullet.isActive = false
                            brick.isVisible = false
                            //soundPlayer.playSound(SoundPlayer.damageShelterID)
                        }
                    }
                }
            }

        }

        if (playerBullet.isActive) {
            for (brick in bricks) {
                if (brick.isVisible) {
                    if (RectF.intersects(playerBullet.position, brick.position)) {
                        // A collision has occurred
                        playerBullet.isActive = false
                        brick.isVisible = false
                        //soundPlayer.playSound(SoundPlayer.damageShelterID)
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                if (RectF.intersects(playerShip.position, bullet.position)) {
                    bullet.isActive = false
                    lives--

                    if (lives == 0) {
                        lost = true
                        break
                    }
                }
            }
        }




        if (lost) {

            var currentScore = score

            invaders.clear()
            bricks.clear()
            invadersBullets.clear()
            lives = 3
            score = 0
            waves = 1
            prepareLevel()
            pause = true
            val item = Result(currentScore, LocalDate.now().toString(), pseudonim)
            GlobalScope.launch { itemDao.insertAll(item) }


            class MyCustomDialog: DialogFragment() {


                override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?
                ): View {

                    dialog?.window?.attributes!!.windowAnimations = R.style.DialogAnimation
                    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val rootView : View = inflater.inflate(R.layout.dialog_score, container, false)
                    rootView.findViewById<Button>(R.id.exit_btn).setOnClickListener{ requireActivity().finishAffinity()}
                    rootView.findViewById<Button>(R.id.restart_btn).setOnClickListener{
                        dismiss()
                    }

                    val name = rootView.findViewById<TextView>(R.id.info)
                    name.append(currentScore.toString())
                    return rootView
                }

                override fun onStart() {
                    super.onStart()
                    val defaultDisplay = DisplayManagerCompat.getInstance(requireContext()).getDisplay(
                        Display.DEFAULT_DISPLAY)
                    val displayContext = requireContext().createDisplayContext(defaultDisplay!!)
                    val width = displayContext.resources.displayMetrics.widthPixels
                    val height = displayContext.resources.displayMetrics.heightPixels

                    dialog?.window
                        ?.setLayout((width*0.9).toInt(), (height*0.8).toInt())
                }
            }

            val dialog  = MyCustomDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.show(supportFragmentManager, "CustomDialog")
            }, 500)
        }
    }

 */



    private fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()

            canvas.drawColor(Color.argb(255, 0, 0, 0))

            paint.color = Color.argb(255, 0, 255, 0)

            canvas.drawBitmap(
                playerShip.bitmap, playerShip.position.left,
                playerShip.position.top, paint
            )

            for (invader in invaders) {
                if (invader.isVisible) {
                    if (uhOrOh) {
                        Invader.bitmap1?.let {
                            canvas.drawBitmap(
                                it,
                                invader.position.left,
                                invader.position.top,
                                paint
                            )
                        }
                    } else {
                        Invader.bitmap2?.let {
                            canvas.drawBitmap(
                                it,
                                invader.position.left,
                                invader.position.top,
                                paint
                            )
                        }
                    }
                }
            }

            for (brick in bricks) {
                if (brick.isVisible) {
                    canvas.drawRect(brick.position, paint)
                }
            }

            if (playerBullet.isActive) {
                canvas.drawRect(playerBullet.position, paint)
            }

            for (bullet in invadersBullets) {
                if (bullet.isActive) {
                    canvas.drawRect(bullet.position, paint)
                }
            }

            paint.color = Color.argb(255, 0, 255, 0)
            paint.textSize = 70f



            canvas.drawText(
                "Score: $score   Lives: $lives Wave: " +
                        "$waves HI: $highScore", 20f, 75f, paint
            )

            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawFrame() {
        //jesli jest gotowe do narysowania
        if (holder.surface.isValid) {
            //uzyskaj canvas
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK)                          //czarne tło
            paint.color = Color.GREEN

            paint.textSize = 60F
            canvas.drawText(
                "Score: $score   Lives: $lives Wave: " +
                        "$waves HI: $highScore", 20f, 75f, paint
            )


            paint.color = Color.argb(255, 0, 255, 0)


            canvas.drawBitmap(
                playerShip.bitmap, playerShip.position.left,
                playerShip.position.top, paint
            )


            // Draw the invaders
            for (invader in invaders) {
                if (invader.isVisible) {
                    if (uhOrOh) {
                        Invader.bitmap1?.let {
                            canvas.drawBitmap(
                                it,
                                invader.position.left,
                                invader.position.top,
                                paint)
                        }
                    } else {
                        Invader.bitmap2?.let {
                            canvas.drawBitmap(
                                it,
                                invader.position.left,
                                invader.position.top,
                                paint)
                        }
                    }
                }
            }





            holder.unlockCanvasAndPost(canvas)              //odblokuj canvas i narysuj

        }
    }

    private fun updateFPS(fps: Long) {
        playerShip.update(fps)

        var bumped = false

        var lost = false

        for (invader in invaders) {

            if (invader.isVisible) {
                // Move the next invader
                invader.update(fps)

                if (invader.takeAim(
                        playerShip.position.left,
                        playerShip.width,
                        waves
                    )
                ) {
                }

                if (invader.position.left > size.x - invader.width
                    || invader.position.left < 0
                ) {

                    bumped = true

                }
            }
        }

        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive = false
        }


        // Did an invader bump into the edge of the screen
        if (bumped) {

            // Move all the invaders down and change direction
            for (invader in invaders) {
                invader.dropDownAndReverse(waves)
                // Have the invaders landed
                if (invader.position.bottom >= size.y && invader.isVisible) {
                    lost = true
                }
            }
        }


    }


    // If SpaceInvadersActivity is paused/stopped
    // then shut down our thread.
    fun pause() {
        playing = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }
    }

    fun resume() {

        println("hejhej")
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            invadersize = 20F
        }
        playing = true
        prepareLevel()
        gameThread.start()
    }

    private fun prepareLevel() {
        // Here we will initialize the game objects
        // Build an army of invaders
        Invader.numberOfInvaders = 0
        numInvaders = 0
        for (column in 0..10) {
            for (row in 0..4) {
                invaders.add(
                    Invader(
                        context,
                        row,
                        column,
                        size.x,
                        size.y,
                        invadersize
                    )
                )

                numInvaders++
            }
        }

        // Build the shelters
        numBricks = 0
        for (shelterNumber in 0..3) {
            for (column in 0..18) {
                for (row in 0..8) {
                    bricks.add(
                        Brick(
                            row,
                            column,
                            shelterNumber,
                            size.x,
                            size.y
                        )
                    )

                    numBricks++
                }
            }
        }

        // Initialize the invadersBullets array
        for (i in 0 until maxInvaderBullets) {
            invadersBullets.add(Bullet(size.y))
        }
    }

    private fun prepareLevel2() {

        //budowanie armii obcych
        Invader.numberOfInvaders = 0
        numInvaders = 0
        for (column in 0..10) {
            for (row in 0..4) {
                invaders.add(
                    Invader(
                        context,
                        row,
                        column,
                        size.x,
                        size.y,
                        invadersize
                    )
                )

                numInvaders++
            }
        }

        //budowanie schonu z cegiełek
        numBricks = 0
        for (shelterNumber in 0..3) {
            for (column in 0..18) {
                for (row in 0..8) {
                    bricks.add(
                        Brick(
                            row,
                            column,
                            shelterNumber,
                            size.x,
                            size.y
                        )
                    )

                    numBricks++
                }
            }
        }


    }


    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val motionArea = size.y - (size.y / 6)

        var act = motionEvent.actionMasked

        when (motionEvent.action and act) {

            // Player has touched the screen
            // Or moved their finger while touching screen
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                pause = false

                println(motionEvent.y)
                println(motionArea)
                if (motionEvent.y > motionArea) {

                    if (motionEvent.x > size.x / 2) {
                        playerShip.moving = PlayerShip.right
                    } else {
                        playerShip.moving = PlayerShip.left
                    }

                }

                if (motionEvent.y < motionArea) {
                    // Shots fired

                    if (playerBullet.shoot(
                            playerShip.position.left + playerShip.width / 2f,
                            playerShip.position.top,
                            playerBullet.up
                        )
                    ) {

                    }


                }
            }

            // Player has removed finger from screen
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                if (motionEvent.y > motionArea) {
                    playerShip.moving = PlayerShip.stopped
                }
            }

        }
        return true
    }


}




