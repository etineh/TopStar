package com.pixel.chatapp.view_controller.games.whot

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
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
import com.pixel.chatapp.constants.Ki
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


class WhotLandscapeActivity : AppCompatActivity() {

    private var myUid: String? = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var refGameStarts: DatabaseReference
    private var startGameEventListener: ValueEventListener? = null

    private var gameMenuView: View? = null
    private var quitGameView: View? = null


    private lateinit var myCardRecycler: RecyclerView
    private lateinit var rewardPoolTV: TextView
    private lateinit var watchNumTV: TextView
    private lateinit var myTimeCountTV: TextView
    private lateinit var myCameraIV: ImageView

    private lateinit var moreIV: ImageView
    private lateinit var shareGameLinkIV: ImageView
    private lateinit var chatIV: ImageView
    private lateinit var audioCallMic: ImageView
    private lateinit var videoCall: ImageView

    private lateinit var drawMarketCardIV: ImageView
    private lateinit var playedCardIV: ImageView

    // player 1 for me
    private lateinit var playerNameTV1: TextView

    // player 2 properties
    private lateinit var playerNameTV2: TextView
    private lateinit var cardLeftAndCountTV2: TextView
    private lateinit var opponentCamera2: ImageView

    // player 3 properties
    private lateinit var playerNameTV3: TextView
    private lateinit var cardLeftAndCountTV3: TextView
    private lateinit var opponentCamera3: ImageView

    // player 4 properties
    private lateinit var playerNameTV4: TextView
    private lateinit var cardLeftAndCountTV4: TextView
    private lateinit var opponentCamera4: ImageView

    // player 5 properties
    private lateinit var playerNameTV5: TextView
    private lateinit var cardLeftAndCountTV5: TextView
    private lateinit var opponentCamera5: ImageView

    // player 6 properties
    private lateinit var playerNameTV6: TextView
    private lateinit var cardLeftAndCountTV6: TextView
    private lateinit var opponentCamera6: ImageView

    private var playerDetailList: ArrayList<AwaitPlayerM>? = null
    private var gameId: String? = null
    private var hostUid: String? = null

    private lateinit var playerNameTVs: List<TextView>
    private lateinit var cardLeftTextViews: List<TextView>
    private lateinit var playerPhotoIVs: List<ImageView>
    private val arrangedPlayerUids = mutableListOf<String>() // List to hold other player UIDs
    private val arrangedPlayerDetails = mutableListOf<AwaitPlayerM>() // List to track cards left for other players
//    private val arrangedPlayerCards = mutableListOf<Int>() // List to track cards left for other players

    private lateinit var cardCounts: MutableList<Int>

    private var totalStake: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whot_landscape)


        myCardRecycler = findViewById(R.id.myCardRecycler)
        rewardPoolTV = findViewById(R.id.rewardPoolTV)
        moreIV = findViewById(R.id.moreIV)
        shareGameLinkIV = findViewById(R.id.shareGameLinkIV)
        chatIV = findViewById(R.id.chatIV)
        myTimeCountTV = findViewById(R.id.myTimeCountTV)
        audioCallMic = findViewById(R.id.audioCall)
        videoCall = findViewById(R.id.videoCall)
        drawMarketCardIV = findViewById(R.id.marketCard_IV)
        playedCardIV = findViewById(R.id.cardPlayedIV)
        watchNumTV = findViewById(R.id.watchNumTV)

        // player 1 ids
        playerNameTV1 = findViewById(R.id.playerNameTV1)
        myCameraIV = findViewById(R.id.myCameraIV)

        // player 2 ids
        playerNameTV2 = findViewById(R.id.playerNameTV2)
        cardLeftAndCountTV2 = findViewById(R.id.cardLeftAndCountTV2)
        opponentCamera2 = findViewById(R.id.opponentCamera2)

        // player 3 ids
        playerNameTV3 = findViewById(R.id.playerNameTV3)
        cardLeftAndCountTV3 = findViewById(R.id.cardLeftAndCountTV3)
        opponentCamera3 = findViewById(R.id.opponentCamera3)

        // player 4 ids
        playerNameTV4 = findViewById(R.id.playerNameTV4)
        cardLeftAndCountTV4 = findViewById(R.id.cardLeftAndCountTV4)
        opponentCamera4 = findViewById(R.id.opponentCamera4)

        // player 5 ids
        playerNameTV5 = findViewById(R.id.playerNameTV5)
        cardLeftAndCountTV5 = findViewById(R.id.cardLeftAndCountTV5)
        opponentCamera5 = findViewById(R.id.opponentCamera5)

        // player 6 ids
        playerNameTV6 = findViewById(R.id.playerNameTV6)
        cardLeftAndCountTV6 = findViewById(R.id.cardLeftAndCountTV6)
        opponentCamera6 = findViewById(R.id.opponentCamera6)

//        rewardPoolTV = findViewById(R.id.rewardPoolTV)
//        rewardPoolTV = findViewById(R.id.rewardPoolTV)

        playerNameTVs = listOf(playerNameTV2, playerNameTV3, playerNameTV4, playerNameTV5, playerNameTV6)
        playerPhotoIVs = listOf(opponentCamera2, opponentCamera3, opponentCamera4, opponentCamera5, opponentCamera6)
        cardLeftTextViews = listOf(cardLeftAndCountTV2, cardLeftAndCountTV3, cardLeftAndCountTV4, cardLeftAndCountTV5, cardLeftAndCountTV6)

        refGameStarts = FirebaseDatabase.getInstance().getReference("GameStarts")


        cardCounts = mutableListOf(5, 5, 5, 5, 5) // Assuming each player starts with 5 cards, adjust as needed

        var currentIndex = 0 // Track index for non-user players


        val gridLayoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        myCardRecycler.layoutManager = gridLayoutManager

        PhoneUtils.enableFullScreenMode(this)

        hideViews()
        changeMyCameraMarginBottom(position = 120f, widthHeight = 120f)

        // get the list of both players who joined the game from intent
        playerDetailList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("playerDetailList", AwaitPlayerM::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("playerDetailList")
        }

        gameId = intent.getStringExtra("gameId")
        hostUid = intent.getStringExtra("hostId")

        // check player size;
        showPlayersView()

        // Loop through the playerDetailList
        playerDetailList?.forEach { player ->

            when(player.playerUid) {
                myUid -> {
                    if(player.photoUri != "null") Picasso.get().load(player.photoUri).into(myCameraIV)

                }
                else -> {
                    if (currentIndex < playerNameTVs.size) { // Ensure we don't overflow the list
                        playerNameTVs[currentIndex].text = ProfileUtils.getOtherDisplayOrUsername(player.playerUid, player.playerName)  // Set player name
                        if(player.photoUri != "null") Picasso.get().load(player.photoUri).into(playerPhotoIVs[currentIndex]) // set player photo

                        cardLeftTextViews[currentIndex].text = "3 cards " //  (edit later)
                        arrangedPlayerUids.add(player.playerUid) // Add their UID to the arrangedPlayerUids list
                        arrangedPlayerDetails.add(player) // Add the AwaitPlayerM object to arrangedPlayerDetails
                        currentIndex++
                    }
                }
            }

        }

        val whotList = mutableListOf(WhotGameModel(0, R.drawable.cross_card1))
        whotList.add(WhotGameModel(0, R.drawable.cross_card10))
        whotList.add(WhotGameModel(0, R.drawable.circle_card7))
        whotList.add(WhotGameModel(0, R.drawable.triangle_card14))
        whotList.add(WhotGameModel(0, R.drawable.star_card2))
        whotList.add(WhotGameModel(0, R.drawable.square_card11))
        whotList.add(WhotGameModel(0, R.drawable.cross_card7))


        val whotGameAdapter = WhotGameAdapter(whotList, this)

        myCardRecycler.adapter = whotGameAdapter


        //  ===========     onClicks

        rewardPoolTV.setOnClickListener {
            if(rewardPoolTV.text.toString().contains("*"))
            {
                val totalAmountView = "${getString(R.string.reward)} $$totalStake  ${getString(R.string.eye)}"
                rewardPoolTV.text = totalAmountView
            } else {
                rewardPoolTV.text = getString(R.string.hash)
            }
        }

        moreIV.setOnClickListener {
            AnimUtils.slideInFromTop(gameMenuView, 300)
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

        setGameMenuView()

        onBackPressedDispatcher.addCallback(backPress)

    }


    //  =====================   methods

    private fun showPlayersView() {
        when(playerDetailList?.size) {
            3 -> {
                println("it has 3 size")
            }
            4 -> {
                playerNameTV4.visibility = View.VISIBLE
                cardLeftAndCountTV4.visibility = View.VISIBLE
                opponentCamera4.visibility = View.VISIBLE
            }
            5 -> {
                playerNameTV4.visibility = View.VISIBLE
                cardLeftAndCountTV4.visibility = View.VISIBLE
                opponentCamera4.visibility = View.VISIBLE

                playerNameTV5.visibility = View.VISIBLE
                cardLeftAndCountTV5.visibility = View.VISIBLE
                opponentCamera5.visibility = View.VISIBLE
            }
            6 -> {
                playerNameTV4.visibility = View.VISIBLE
                cardLeftAndCountTV4.visibility = View.VISIBLE
                opponentCamera4.visibility = View.VISIBLE

                playerNameTV5.visibility = View.VISIBLE
                cardLeftAndCountTV5.visibility = View.VISIBLE
                opponentCamera5.visibility = View.VISIBLE

                playerNameTV6.visibility = View.VISIBLE
                cardLeftAndCountTV6.visibility = View.VISIBLE
                opponentCamera6.visibility = View.VISIBLE

                changeMyCameraMarginBottom()
            }

        }
    }

    private fun changeMyCameraMarginBottom(position: Float = 5f, widthHeight: Float = 100f) {
        // Cast layoutParams to ViewGroup.MarginLayoutParams
        val layoutParams = myCameraIV.layoutParams as MarginLayoutParams

        // Convert 30dp to pixels for the bottom margin
        val marginInDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, position, resources.displayMetrics
        ).toInt()

        // Convert 120dp to pixels for width and height
        val sizeInDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, widthHeight, resources.displayMetrics
        ).toInt()

        // Set the new marginBottom value
        layoutParams.bottomMargin = marginInDp

        // Set new width and height values
        layoutParams.width = sizeInDp
        layoutParams.height = sizeInDp

        // Apply the updated layoutParams back to the view
        myCameraIV.layoutParams = layoutParams
    }

    private fun hideViews(){
        playerNameTV4.visibility = View.GONE
        cardLeftAndCountTV4.visibility = View.GONE
        opponentCamera4.visibility = View.GONE

        playerNameTV5.visibility = View.GONE
        cardLeftAndCountTV5.visibility = View.GONE
        opponentCamera5.visibility = View.GONE

        playerNameTV6.visibility = View.GONE
        cardLeftAndCountTV6.visibility = View.GONE
        opponentCamera6.visibility = View.GONE

    }


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

                            val numPlayer = "${getString(R.string.gameId)}: ${signalPlayerM.gameID}"
                            expectedPlayerNumTV.text = numPlayer

                            val stakeAmount = "${getString(R.string.entryFee)} $${signalPlayerM.stakeAmount}"
                            stakeAmountTV.text = stakeAmount

                            totalStake = signalPlayerM.totalStake

                            val totalAmountView = "${getString(R.string.reward)} $$totalStake"
                            rewardPrizeTV.text = totalAmountView
                            if(!rewardPoolTV.text.toString().contains("*")) {
                                val tAmount = "${getString(R.string.reward)} $$totalStake  ${getString(R.string.eye)}"
                                rewardPoolTV.text = tAmount
                            }
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

                val gameAPI = Ki.retrofit.create(GameAPI::class.java)
                IdTokenUtil.generateToken({ token ->
                    val threeValueM = ThreeValueM(token, hostUid, gameId)

                    gameAPI.quitGame(threeValueM).enqueue(object : Callback<Void> {
                        override fun onResponse(p0: Call<Void>, p1: Response<Void>) {

                            if(p1.isSuccessful) {
                                MainActivity.onGameNow = false
                                MainActivity.isOnGameNow = false
                                startGameEventListener?.let { listen -> refGameStarts.child(gameId!!).child(hostUid!!).removeEventListener(listen) }
                                finish()
                            } else {
                                progressBar?.visibility = View.GONE
                                yesTV.visibility = View.VISIBLE
                                Toast.makeText(this@WhotLandscapeActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                            }

                        }

                        override fun onFailure(p0: Call<Void>, p1: Throwable) {
                            progressBar?.visibility = View.GONE
                            yesTV.visibility = View.VISIBLE
                            Toast.makeText(this@WhotLandscapeActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                        }

                    })

                }, this)

            }

            noTV?.setOnClickListener {
                quitGameView?.visibility = View.GONE
            }

        }
    }

    // After a player plays a card
    fun updateCardCount(playerUid: String) {
        val playerIndex = arrangedPlayerUids.indexOf(playerUid) // Find the player's index based on Uid
        if (playerIndex != -1 && playerIndex < cardLeftTextViews.size) {
            cardCounts[playerIndex] = cardCounts[playerIndex] - 1 // Decrease the card count
            cardLeftTextViews[playerIndex].text = cardCounts[playerIndex].toString() // Update the TextView
        }
    }


    private val backPress : OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            val mainActivityIntent = Intent(this@WhotLandscapeActivity, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(mainActivityIntent)

            WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(true, gameHasStarted = true, whichGameActivity = "whotLandscape")

        }

    }

}





