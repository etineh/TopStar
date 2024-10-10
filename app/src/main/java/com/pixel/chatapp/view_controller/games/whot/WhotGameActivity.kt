package com.pixel.chatapp.view_controller.games.whot

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pixel.chatapp.R
import com.pixel.chatapp.adapters.WhotGameAdapter
import com.pixel.chatapp.services.api.dao_interface.GameAPI
import com.pixel.chatapp.services.api.model.outgoing.ThreeValueM
import com.pixel.chatapp.constants.K
import com.pixel.chatapp.view_controller.MainActivity
import com.pixel.chatapp.dataModel.AwaitPlayerM
import com.pixel.chatapp.dataModel.SignalPlayerM
import com.pixel.chatapp.dataModel.WhotGameModel
import com.pixel.chatapp.utilities.AnimUtils
import com.pixel.chatapp.utilities.IdTokenUtil
import com.pixel.chatapp.utilities.PhoneUtils
import com.pixel.chatapp.utilities.ProfileUtils
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WhotGameActivity : AppCompatActivity() {

    private var gameMenuView: View? = null

    private lateinit var gameBackground: ImageView
    private lateinit var whotRecycler: RecyclerView
    private lateinit var opponentCameraIV: ImageView
    private lateinit var myCameraIV: ImageView
    private lateinit var moreIV: ImageView
    private lateinit var shareGameLinkIV: ImageView
    private lateinit var chatIV: ImageView
    private lateinit var oppCardIV: ImageView
    private lateinit var numberOfCardTV: TextView
    private lateinit var opponentTimeCountTV: TextView
    private lateinit var myTimeCountTV: TextView
    private lateinit var audioCallMic: ImageView
    private lateinit var videoCall: ImageView
    private lateinit var drawMarketCardIV: ImageView
    private lateinit var playedCardIV: ImageView

    private var quitGameView: View? = null
    private var gameId: String? = null
    private var hostUid: String? = null

    private var quitTimeCount: CountDownTimer? = null

    private var myUid: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var refGameStarts: DatabaseReference
    private var startGameEventListener: ValueEventListener? = null

    private var totalStake: String? = null

    object PlayMode {
        var playMode: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whot_game)


        gameBackground = findViewById(R.id.gameBackground)
        opponentCameraIV = findViewById(R.id.opponentCamera)
        myCameraIV = findViewById(R.id.myCameraIV)
        moreIV = findViewById(R.id.moreIV)
        shareGameLinkIV = findViewById(R.id.shareGameLinkIV)
        chatIV = findViewById(R.id.chatIV)
        oppCardIV = findViewById(R.id.oppCardIV)
        numberOfCardTV = findViewById(R.id.numberOfCardTV)
        opponentTimeCountTV = findViewById(R.id.opponentTimeCountTV)
        myTimeCountTV = findViewById(R.id.myTimeCountTV)
        audioCallMic = findViewById(R.id.audioCall)
        videoCall = findViewById(R.id.videoCall)
        drawMarketCardIV = findViewById(R.id.marketCard_IV)
        playedCardIV = findViewById(R.id.cardPlayedIV)
//        drawMarketCard_IV = findViewById(R.id.marketCard_IV)

        whotRecycler = findViewById(R.id.recyclerView)

        val gridLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        whotRecycler.layoutManager = gridLayoutManager

        refGameStarts = FirebaseDatabase.getInstance().getReference("GameStarts")


        // get the list of both players who joined the game from intent
        val playerDetailList: ArrayList<AwaitPlayerM>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("playerDetailList", AwaitPlayerM::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("playerDetailList")
        }
        gameId = intent.getStringExtra("gameId")
        hostUid = intent.getStringExtra("hostId")


        val whotList = mutableListOf(WhotGameModel(0, R.drawable.cross_card1))
        whotList.add(WhotGameModel(0, R.drawable.cross_card10))
        whotList.add(WhotGameModel(0, R.drawable.circle_card7))
        whotList.add(WhotGameModel(0, R.drawable.triangle_card14))
        whotList.add(WhotGameModel(0, R.drawable.star_card2))
        whotList.add(WhotGameModel(0, R.drawable.square_card11))
        whotList.add(WhotGameModel(0, R.drawable.cross_card7))

        val whotGameAdapter = WhotGameAdapter(whotList, this)

        whotRecycler.adapter = whotGameAdapter

        playerDetailList?.forEach { // 2 players    -- arrange photo/video call camera
            if (it.playerUid == myUid) {
                if(it.photoUri != "null") Picasso.get().load(it.photoUri).into(myCameraIV)
            } else {
                if(it.photoUri != "null") Picasso.get().load(it.photoUri).into(opponentCameraIV)
            }
        }

        // ==========================    onClicks

        moreIV.setOnClickListener {
            hostUid?.let { AnimUtils.slideInFromLeft(gameMenuView, 300) }
        }

        shareGameLinkIV.setOnClickListener {
            it.animate().scaleY(1.2f).scaleX(1.2f).withEndAction {
                Toast.makeText(this, getString(R.string.workInProgress), Toast.LENGTH_SHORT).show()

                it.scaleX = 1.0f
                it.scaleY = 1.0f
            }
        }

        chatIV.setOnClickListener {
            it.animate().scaleY(1.2f).scaleX(1.2f).withEndAction {
                Toast.makeText(this, getString(R.string.workInProgress), Toast.LENGTH_SHORT).show()

                it.scaleX = 1.0f
                it.scaleY = 1.0f
            }
        }

        drawMarketCardIV.setOnClickListener {
            it.animate().scaleY(1.2f).scaleX(1.2f).withEndAction {
                Toast.makeText(this, getString(R.string.workInProgress), Toast.LENGTH_SHORT).show()

                it.scaleX = 1.0f
                it.scaleY = 1.0f
            }
        }

        audioCallMic.setOnClickListener {
            Toast.makeText(this, getString(R.string.workInProgress), Toast.LENGTH_SHORT).show()
        }

        videoCall.setOnClickListener {
            Toast.makeText(this, getString(R.string.workInProgress), Toast.LENGTH_SHORT).show()
        }

        hostUid?.let { setGameMenuView() }


        onBackPressedDispatcher.addCallback(backPress)

    }

    //  =======     setViews

    private fun waitPlayerTime(quitTimeTV : TextView?) {

        quitTimeCount = object : CountDownTimer(120_000, 1000) {    // 60s
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


    private fun setQuitGameView(){

        if(quitGameView == null){
            val quitGameViewStub = findViewById<ViewStub>(R.id.quit_game_layout)
            quitGameView = quitGameViewStub.inflate()

            val yesTV = quitGameView?.findViewById<TextView>(R.id.yesTV)
            val noTV = quitGameView?.findViewById<TextView>(R.id.noTV)
            val progressBar = quitGameView?.findViewById<ProgressBar>(R.id.progressBar9)

            yesTV?.setOnClickListener {

                progressBar?.visibility = View.VISIBLE
                yesTV.visibility = View.INVISIBLE

                val gameAPI = K.retrofit.create(GameAPI::class.java)
                IdTokenUtil.generateToken({token ->
                    val threeValueM = ThreeValueM(token, hostUid, gameId)

                    gameAPI.quitGame(threeValueM).enqueue(object : Callback<Void>{
                        override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                            if(p1.isSuccessful) {
                                MainActivity.onGameNow = false
                                MainActivity.isOnGameNow = false
                                startGameEventListener?.let { listen -> refGameStarts.child(gameId!!).child(hostUid!!).removeEventListener(listen) }

                                finish()
                            } else {
                                progressBar?.visibility = View.GONE
                                yesTV.visibility = View.VISIBLE
                                Toast.makeText(this@WhotGameActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                            }

                        }

                        override fun onFailure(p0: Call<Void>, p1: Throwable) {
                            progressBar?.visibility = View.GONE
                            yesTV.visibility = View.VISIBLE
                            Toast.makeText(this@WhotGameActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                        }

                    })

                }, this)

            }

            noTV?.setOnClickListener {
                quitGameView?.visibility = View.GONE
            }

        }
    }

    //  ========    methods

    private fun setGameMenuView() {
        if (gameMenuView == null) {
            val gameMenuViewStub = findViewById<ViewStub>(R.id.incomingGameLayout)
            gameMenuView = gameMenuViewStub.inflate()

            val quitTV = gameMenuView!!.findViewById<TextView>(R.id.acceptGameTV)
            val muteIV = gameMenuView!!.findViewById<ImageView>(R.id.muteIV)
            val rejectTV = gameMenuView!!.findViewById<TextView>(R.id.rejectGameTV)
            val expectedPlayerNumTV = gameMenuView!!.findViewById<TextView>(R.id.expectedPlayerNumTV)
            val hostByTV = gameMenuView!!.findViewById<TextView>(R.id.hostGameTV)
            val gameModeTV = gameMenuView!!.findViewById<TextView>(R.id.modeTV)
            val gameTypeTV = gameMenuView!!.findViewById<TextView>(R.id.gameTypeTV)
            val stakeAmountTV = gameMenuView!!.findViewById<TextView>(R.id.stakeAmountTV)
            val rewardPrizeTV = gameMenuView!!.findViewById<TextView>(R.id.rewardTV)
            val hostNoteTV = gameMenuView!!.findViewById<TextView>(R.id.hostNoteTV)

            val minimizeIV = gameMenuView!!.findViewById<ImageView>(R.id.minimizeIV)
            val gameIdCopyIV = gameMenuView!!.findViewById<ImageView>(R.id.gameIdCopyIV)
//            minimizedContainer = gameMenuView!!.findViewById(R.id.minimizedContainer)
//            var signalCardView = gameMenuView!!.findViewById<CardView>(R.id.signalCardView)
//            minimizedTV = gameMenuView!!.findViewById(R.id.minimizedTV)

            muteIV.visibility = View.GONE
            rejectTV.visibility = View.GONE
            gameIdCopyIV.visibility = View.VISIBLE

            gameIdCopyIV.setOnClickListener {
                PhoneUtils.copyText(this, expectedPlayerNumTV)
            }

            quitTV.text = getString(R.string.quitGame)

            quitTV.setOnClickListener {   // end the game
                setQuitGameView()
                quitGameView?.visibility = View.VISIBLE
                gameMenuView?.visibility = View.GONE
            }

            minimizeIV.setImageResource(R.drawable.baseline_cancel_24)

            minimizeIV.setOnClickListener {
                gameMenuView?.visibility = View.GONE
            }

            // get game details
            startGameEventListener = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val signalPlayerM = snapshot.getValue(SignalPlayerM::class.java)
                        if(signalPlayerM != null){
                            // set host by name
                            val hostName = "${getString(R.string.hostBy)} ${ProfileUtils.getOtherDisplayOrUsername(hostUid!!, signalPlayerM.senderName )}"
                            hostByTV.text = hostName

                            val gameMode = "${getString(R.string.mode_)} ${signalPlayerM.gameMode}"
                            gameModeTV.text = gameMode

                            //set game type
                            val gameType = "${getString(R.string.game_)} ${signalPlayerM.message}"
                            gameTypeTV.text= gameType

                            val hostNote = "${getString(R.string.hostRemark)} ${signalPlayerM.hostNote}"
                            hostNoteTV.text= hostNote

                            val numPlayer = "${getString(R.string.gameId)}: $gameId"
                            expectedPlayerNumTV.text = numPlayer

                            val stakeAmount = "${getString(R.string.entryFee)} $${signalPlayerM.stakeAmount}"
                            stakeAmountTV.text = stakeAmount

                            totalStake = signalPlayerM.totalStake

                            val totalAmountView = "${getString(R.string.reward)} $$totalStake"
                            rewardPrizeTV.text = totalAmountView
//                            rewardPoolTV.text = totalAmountView
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }

            startGameEventListener?.let { listen -> refGameStarts.child(gameId!!).child(hostUid!!).addValueEventListener(listen) }

        }
    }


    private val backPress : OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            val mainActivityIntent = Intent(this@WhotGameActivity, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(mainActivityIntent)

            WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(true, gameHasStarted = true, whichGameActivity = "whotPortrait")

//            if(numberJoinedTV.text == getString(R.string.joined)) finish()
//            else{
//                val mainActivityIntent = Intent(this@AwaitPlayersActivity, MainActivity::class.java)
//                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                startActivity(mainActivityIntent)
//
//                WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(true)
//            }

        }

    }

}








