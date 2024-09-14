package com.pixel.chatapp.games.whot

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewStub
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixel.chatapp.R
import com.pixel.chatapp.adapters.WhotGameAdapter
import com.pixel.chatapp.home.MainActivity
import com.pixel.chatapp.model.WhotGameModel
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class WhotGameActivity : AppCompatActivity() {

    private lateinit var gameBackground: ImageView
    private lateinit var whotRecycler: RecyclerView
//    private lateinit var awaitPlayerView?: View
    private var awaitPlayerView: View? = null
    private var quitGameView: View? = null

    private var quitTimeCount: CountDownTimer? = null

    object PlayMode {
        var playMode: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whot_game)


        gameBackground = findViewById(R.id.gameBackground)
//        gameBackground = findViewById(R.id.gameBackground)
//        gameBackground = findViewById(R.id.gameBackground)
//        gameBackground = findViewById(R.id.gameBackground)
        whotRecycler = findViewById(R.id.recyclerView)


        val gridLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        whotRecycler.layoutManager = gridLayoutManager

        val whotList = mutableListOf(WhotGameModel(0, R.drawable.cross_card1))
        whotList.add(WhotGameModel(0, R.drawable.cross_card10))
        whotList.add(WhotGameModel(0, R.drawable.circle_card7))
        whotList.add(WhotGameModel(0, R.drawable.triangle_card14))
        whotList.add(WhotGameModel(0, R.drawable.star_card2))
        whotList.add(WhotGameModel(0, R.drawable.square_card11))
        whotList.add(WhotGameModel(0, R.drawable.cross_card7))

        val whotGameAdapter = WhotGameAdapter(whotList, this)

        whotRecycler.adapter = whotGameAdapter

//        updateImageBasedOnOrientation(resources.configuration.orientation)

//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        enableFullScreenMode()


        onBackPressedDispatcher.addCallback(backPress)

        when (PlayMode.playMode){
            "quickPlay" -> "hello"
            "multiPlay" -> setAwaitPlayerView()
        }

        awaitPlayerView?.visibility = View.VISIBLE

    }

    //  =======     setViews
    private fun setAwaitPlayerView() {

        if (awaitPlayerView == null) {
            val awaitPlayerViewStub = findViewById<ViewStub>(R.id.await_player_layout)
            awaitPlayerView = awaitPlayerViewStub.inflate()

            // Directly access the views after inflation
            val closeAwaitingIv = awaitPlayerView?.findViewById<ImageView>(R.id.closeAwaitingIv)
            val circleImageView = awaitPlayerView?.findViewById<CircleImageView>(R.id.circleImageView)
            val awaitingWhoTV = awaitPlayerView?.findViewById<TextView>(R.id.awaitingWho_TV)
            val quitTimeTV = awaitPlayerView?.findViewById<TextView>(R.id.autoQuitTime_TV)

            closeAwaitingIv?.setOnClickListener {
                setQuitGameView()
                quitGameView?.visibility = View.VISIBLE
                awaitPlayerView?.visibility = View.GONE
            }


            awaitingWhoTV?.text = getNames()

            MainActivity.selectedPlayerMList?.let{    // set user image
                if(it.size > 0) Picasso.get().load(MainActivity.selectedPlayerMList[0].photoUri).into(circleImageView)
            }

//            awaitPlayerView?.setOnClickListener {
//                quitTimeCount?.cancel()
//            }

            waitPlayerTime(quitTimeTV)  // start waiting counting time
            quitTimeCount?.start()

        }

    }

    private fun waitPlayerTime(quitTimeTV : TextView?) {

        quitTimeCount = object : CountDownTimer(120_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val timeCount = getString(R.string.quit) + " (" + millisUntilFinished / 1000 + "s)"
                quitTimeTV?.text = timeCount
            }

            override fun onFinish() {
//                quitTimeTV?.text = getString(R.string.quit) + " (0s)"  // Update to show "0s" on finish
                onBackPressedDispatcher.onBackPressed()
            }
        }

    }

    private fun getNames() : SpannableString {
        // add name
        val selectedUserNames = MainActivity.selectedUserNames.toString()
        val awaitingUser = getString(R.string.waiting) + " " + selectedUserNames + " " + getString(R.string.toJoin)

        val spannableString = SpannableString(awaitingUser)

        // Calculate the start and end positions of the selectedUserNames part
        val start = awaitingUser.indexOf(selectedUserNames)
        val end = start + selectedUserNames.length

        spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    private fun setQuitGameView(){

        if(quitGameView == null){
            val quitGameViewStub = findViewById<ViewStub>(R.id.quit_game_layout)
            quitGameView = quitGameViewStub.inflate()

            val yesTV = quitGameView?.findViewById<TextView>(R.id.yesTV)
            val noTV = quitGameView?.findViewById<TextView>(R.id.noTV)

            yesTV?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            noTV?.setOnClickListener {
                awaitPlayerView?.visibility = View.VISIBLE
                quitGameView?.visibility = View.GONE
            }

        }
    }

    //  ========    methods

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        updateImageBasedOnOrientation(newConfig.orientation)
    }

    private fun updateImageBasedOnOrientation(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gameBackground.setImageResource(R.drawable.card_background_landscape)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            gameBackground.setImageResource(R.drawable.card_background_portraite)
        }
    }

    private fun transparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            window.statusBarColor = Color.TRANSPARENT
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun enableFullScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }


    private val backPress : OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

//            val mainActivityIntent = Intent(this@WhotGameActivity, WhotOptionActivity::class.java)
//            startActivity(mainActivityIntent)

            finish()

            MainActivity.selectedUserNames.clear()
            MainActivity.forwardChatUserId.clear()
            MainActivity.selectedUserNames.clear()

        }

    }

}








