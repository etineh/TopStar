package com.pixel.chatapp.games.whot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pixel.chatapp.R
import com.pixel.chatapp.adapters.AwaitPlayerA
import com.pixel.chatapp.api.Dao_interface.GameAPI
import com.pixel.chatapp.api.model.incoming.ResultApiM
import com.pixel.chatapp.api.model.outgoing.TwoValueM
import com.pixel.chatapp.constants.AllConstants
import com.pixel.chatapp.home.MainActivity
import com.pixel.chatapp.model.AwaitPlayerM
import com.pixel.chatapp.model.SignalPlayerM
import com.pixel.chatapp.utils.GameUtils
import com.pixel.chatapp.utils.IdTokenUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create


class AwaitPlayersActivity : AppCompatActivity(), AwaitPlayerA.RemovePlayerListener {

    private lateinit var backPressIV: ImageView
    private lateinit var quitTimeCountTV: TextView
    private lateinit var recyclerPlayers: RecyclerView
    private lateinit var onboardingTV: TextView
    private lateinit var numberJoinedTV: TextView
    private lateinit var startGameButton: TextView
    private lateinit var rewardAmountTV: TextView
    private lateinit var addPlayerIV: ImageView


    private var startOrQuitGameView: View? = null
    private lateinit var quitOrStart: String

    private var quitTimeCount: CountDownTimer? = null

    private lateinit var playerList: MutableList<AwaitPlayerM>
    private lateinit var awaitPlayerA: AwaitPlayerA

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    private var hostUid: String? = null

    private var titleTV: TextView? = null
    object PlayersUid {
        @JvmField
        var playerUidList: MutableList<String?> = mutableListOf()
    }

    private var removePlayerList: MutableList<AwaitPlayerM> = mutableListOf()

    private var eventListener: ValueEventListener? = null
    private lateinit var refGameAlert: DatabaseReference

    private var finalQuit: Boolean = false
    private var notifyNow: Boolean = false

    private lateinit var gameMode: String
    private lateinit var stakeMount: String
    private lateinit var hostNote: String

    private var playerJoinNumber: Int = 1
    private var removePlayerUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_await_players)

        backPressIV = findViewById(R.id.arrowBackS)
        quitTimeCountTV = findViewById(R.id.quitTimeCount_TV)
        onboardingTV = findViewById(R.id.onboardingTV)
        numberJoinedTV = findViewById(R.id.numberJoined_TV)
        startGameButton = findViewById(R.id.startGamebutton)
        recyclerPlayers = findViewById(R.id.recyclerPlayers)
        rewardAmountTV = findViewById(R.id.rewardAmount_TV)
        addPlayerIV = findViewById(R.id.addPlayerIV)
//        rewardAmountTV = findViewById(R.id.rewardAmount_TV)

        refGameAlert = FirebaseDatabase.getInstance().getReference("GameAlert")


        val mode = intent.getStringExtra("mode")
        val hostName = intent.getStringExtra("hostName")
        hostUid = intent.getStringExtra("hostUid")

        waitPlayerTime(quitTimeCountTV)

        recyclerPlayers.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)

        awaitPlayerA = AwaitPlayerA(MainActivity.selectedPlayerMList, hostUid, this)
        awaitPlayerA.setRemovePlayerListener(this)
        recyclerPlayers.adapter = awaitPlayerA

        updateNumberOfPlayerJoined()

        when(hostUid){
            user?.uid -> {
                quitTimeCount?.start()
                Handler(Looper.getMainLooper()).postDelayed(
                    {if(numberJoinedTV.text == getString(R.string.joined)) closePage()}, 5000
                )

            }
            else -> {
                startGameButton.visibility = View.INVISIBLE
                addPlayerIV.visibility = View.INVISIBLE
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if(numberJoinedTV.text == getString(R.string.joined)) {
                            finish()
                            refGameAlert.child(user?.uid!!).removeValue()
                            MainActivity.onGameNow = false
                        } else {
                            affirmJoin()
                        }
                    }, 3000
                )
            }
        }

        startGameButton.setOnClickListener {
            if(playerJoinNumber > 1) {
                quitOrStart = "start"
                setStartOrQuitGameView()
                startOrQuitGameView?.visibility = View.VISIBLE
                val numberOfPlayer = "${getString(R.string.startGameWith)} $playerJoinNumber ${getString(R.string.players_)}?"
                titleTV?.text = numberOfPlayer
            } else {
                Toast.makeText(this@AwaitPlayersActivity, getString(R.string.allowOneMore), Toast.LENGTH_LONG).show()
            }
        }

        quitTimeCountTV.setOnClickListener {
            if(numberJoinedTV.text == getString(R.string.joined)) finish()
            else {
                quitOrStart = "quit"
                setStartOrQuitGameView()
                when(hostUid){
                    user?.uid -> titleTV?.text = getString(R.string.quitGameForEveryone)
                    else -> titleTV?.text = getString(R.string.quitGameSure)
                }

                startOrQuitGameView?.visibility = View.VISIBLE
            }
        }

        addPlayerIV.setOnClickListener { addNewPlayer(it) }

//        AnimUtils.linearSlidingAnimation(onboardingTV, 2000)

        backPressIV.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        onBackPressedDispatcher.addCallback(backPress)

    }

    //  =========   method

    private fun affirmJoin(){
        IdTokenUtil.generateToken({ token: String? ->
            val twoValueM = TwoValueM(token, hostUid!!)
            val gameAPI = AllConstants.retrofit.create(GameAPI::class.java)

            gameAPI.join(twoValueM).enqueue(object : Callback<ResultApiM> {
                override fun onResponse(call: Call<ResultApiM>, response: Response<ResultApiM>) {
                    if (response.isSuccessful) {
                        if (response.body()!!.result == "success") {
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.awaitingPlayer), Toast.LENGTH_SHORT).show()
                        } else if (response.body()!!.result == "insufficient funds") {
                            rejectGame()
                            quitTimeCount?.start()
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.insufficientBal), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<ResultApiM>, throwable: Throwable) {
                    Toast.makeText(this@AwaitPlayersActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                    println("what is wallet error occur MainActivity L1700: " + throwable.message)
                }
            })
        }, this@AwaitPlayersActivity)
    }

    private fun addNewPlayer(it: View)
    {
        it.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction{

            MainActivity.onSelectNewPlayer = true
            MainActivity.newPlayerMList.clear()
            MainActivity.forwardChatUserId.clear()
            MainActivity.forwardChatUserId.addAll(PlayersUid.playerUidList)

            WhotOptionActivity.TriggerInterface.triggerOnForward.openOnForwardView()

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(mainActivityIntent)
//                MainActivity.onGameNow = false

            Handler(Looper.getMainLooper()).postDelayed({
                it.scaleX = 1.0f
                it.scaleY = 1.0f
            }, 1000)

//            mainActivityIntent.putExtra("gameMode", gameMode)
//            mainActivityIntent.putExtra("stakeAmount", stakeMount)
//            mainActivityIntent.putExtra("hostNote", hostNote)
//            mainActivityIntent.putExtra("refreshList", false)
        }
    }

    private fun removePlayer(removePlayerUid: String?, context: Context? = this@AwaitPlayersActivity)
    {
        val removePlayerMap: MutableMap<String, Any> = HashMap()
        removePlayerUid?.let {removeUid ->

            removePlayerMap[removeUid] = GameUtils.newPlayerMap(removePlayerList)

            GameUtils.rejectGameOrAddNewPlayer(this, hostUid?:"", "removePlayerByHost", removePlayerMap,
                object : GameUtils.RejectGameInterface
                {
                    override fun onSuccess() {

                        // remove from other player list
                        hostUid?.let {hostId ->
                            GameUtils.removePlayer(context, hostId, removePlayerUid, object : GameUtils.RejectGameInterface{
                                override fun onSuccess() {
                                    if(quitOrStart != "quit")
                                        Toast.makeText(context, getString(R.string.playerRemoved), Toast.LENGTH_LONG).show()
                                }

                                override fun onFailure() {
                                    Toast.makeText(context, getString(R.string.errorOccur), Toast.LENGTH_LONG).show()
                                }

                            })
                        }

                        startOrQuitGameView?.visibility = View.GONE
                        removePlayerList.clear()
                    }

                    override fun onFailure() {
                        println(getString(R.string.errorOccur))
                    }
                })
        }
    }

    private fun updateNumberOfPlayerJoined(){

        hostUid?.let {hostId ->
            user?.let {userInit->

                eventListener = object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        PlayersUid.playerUidList.clear()
                        playerJoinNumber = 1
                        val signalPlayerM = snapshot.getValue(SignalPlayerM::class.java)

                        val totalPlayer = snapshot.child("players").children.count()

                        val stakeAmount = signalPlayerM?.stakeAmount?.toDoubleOrNull() ?: 0.0
                        val totalStake = stakeAmount * totalPlayer
                        val stakingAndWins = "${getString(R.string.entryFee)} $${stakeAmount} " +
                                "\n${getString(R.string.estWin)} $${totalStake}"
                        rewardAmountTV.text = stakingAndWins

                        var count = 0
                        if(notifyNow) MainActivity.selectedPlayerMList.clear()

                        for (playerSnapshot in snapshot.child("players").children)
                        {
                            val player = playerSnapshot.getValue(AwaitPlayerM::class.java)
                            if(notifyNow) MainActivity.selectedPlayerMList.add(player)

                            if(player?.signalUpdate == "join") playerJoinNumber++
                            val numberPlayerJoin = "$playerJoinNumber/$totalPlayer ${getString(R.string.joined)}"
                            numberJoinedTV.text = numberPlayerJoin

                            PlayersUid.playerUidList.add(player?.playerUid)

                            count++
                            if(count == totalPlayer) {
//                                println("I am done count:")
                                if(notifyNow) awaitPlayerA.notifyDataSetChanged()
                                count = 0
                                notifyNow = true
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                }

                eventListener?.let { refGameAlert.child(userInit.uid).child(hostId).addValueEventListener(it) }

            }
        }

    }

    private fun setStartOrQuitGameView(){

        if(startOrQuitGameView == null){
            val startGameLayoutVS = findViewById<ViewStub>(R.id.startGameLayout)
            startOrQuitGameView = startGameLayoutVS.inflate()

            val yesTV = startOrQuitGameView?.findViewById<TextView>(R.id.yesTV)
            val noTV = startOrQuitGameView?.findViewById<TextView>(R.id.noTV)
            titleTV = startOrQuitGameView?.findViewById(R.id.sureNoticeTV)

            yesTV?.setOnClickListener {

                it.animate().scaleX(1.3f).scaleY(1.3f).setDuration(1000).withEndAction(Runnable {
                    it.scaleX = 1.0f
                    it.scaleY = 1.0f
                })

                if(quitOrStart == "start"){
                    startGame()
                    quitTimeCount?.cancel()
                } else if (quitOrStart == "quit"){

                    quitTimeCount?.cancel()
                    when(user?.uid){
                        hostUid -> {
                            PlayersUid.playerUidList.forEach {eachUid -> removePlayer(eachUid) }
                            finish()
                        }
                        else -> rejectGame()
                    }
                    MainActivity.onGameNow = false

                } else if (quitOrStart == "removePlayer") {
                    if(hostUid == user?.uid) removePlayer(removePlayerUid)
                }

                if (quitOrStart != "removePlayer") {
                    user?.let {userInit->   // remove listener
                        hostUid?.let {hostId->
                            eventListener?.let { eventL-> refGameAlert.child(userInit.uid).child(hostId).removeEventListener(eventL)}
                        }
                    }
                }
                MainActivity.forwardChatUserId.clear()
                MainActivity.selectedUserNames.clear()
                PlayersUid.playerUidList.clear()

            }

            noTV?.setOnClickListener {
                startOrQuitGameView?.visibility = View.GONE
                if (quitOrStart == "removePlayer") {
                    removePlayerList.clear()
                } else {
                    if(finalQuit) quitTimeCount?.start()
                }
            }

        }
    }

    private fun startGame(){
        WhotGameActivity.PlayMode.playMode = "quickPlay"
        val mainActivityIntent = Intent(this, WhotGameActivity::class.java)
        startActivity(mainActivityIntent)
        startOrQuitGameView?.visibility = View.GONE
        finish()
    }

    private fun rejectGame(){
        hostUid?.let { uid ->   // --- later, if hostUid is my uid, quit for everyone
            GameUtils.rejectGameOrAddNewPlayer(this, uid, null, null, object : GameUtils.RejectGameInterface{
                override fun onSuccess() {
                    quitTimeCount?.cancel()
                    finish()
                }

                override fun onFailure() {
                    Toast.makeText(this@AwaitPlayersActivity,
                            getString(R.string.noInternetConnection), Toast.LENGTH_LONG
                    ).show()

                }
            })
        }
    }

    private fun waitPlayerTime(quitTimeTV: TextView?) {     // 10 min
        quitTimeCount = object : CountDownTimer(600_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Calculate minutes and seconds
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60

                val timeCount = if (minutes > 0) {
                    // Display "X min Ys" format when minutes > 0
                    getString(R.string.quit) + " (" + minutes + "min " + seconds + "s)"
                } else {
                    // Display only seconds when minutes = 0
                    getString(R.string.quit) + " (" + seconds + "s)"
                }

                quitTimeTV?.text = timeCount
            }

            override fun onFinish() {

                if(!finalQuit){

                    quitOrStart = "quit"
                    setStartOrQuitGameView()
                    titleTV?.text = getString(R.string.quitGameSure)

                    startOrQuitGameView?.visibility = View.VISIBLE


                    finalQuit = true

                    if(numberJoinedTV.text == getString(R.string.joined)){
                        MainActivity.forwardChatUserId.clear()
                        MainActivity.selectedUserNames.clear()
                        MainActivity.selectedPlayerMList.clear()
                        finish()
                    } else WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(false)

                } else {
                    closePage()
                }
            }
        }
    }

    private fun closePage(){
        PlayersUid.playerUidList.forEach {eachUid -> removePlayer(eachUid) }
        quitTimeCount?.cancel()
        user?.let {userInit->   // remove listener
            hostUid?.let {hostId->
                eventListener?.let { eventL-> refGameAlert.child(userInit.uid).child(hostId).removeEventListener(eventL) }
            }
        }
//        refGameAlert.child(user?.uid!!).removeValue()
        MainActivity.onGameNow = false
        MainActivity.forwardChatUserId.clear()
        MainActivity.selectedUserNames.clear()
        finish()
    }

    private val backPress : OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if(numberJoinedTV.text == getString(R.string.joined)) finish()
            else{
                val mainActivityIntent = Intent(this@AwaitPlayersActivity, MainActivity::class.java)
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(mainActivityIntent)

                WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(true)
            }

        }

    }

    override fun removePlayer(playerModel: AwaitPlayerM) {

        quitOrStart = "removePlayer"
        removePlayerUid = playerModel.playerUid
        removePlayerList.clear()
        removePlayerList.add(playerModel)

        setStartOrQuitGameView()
        startOrQuitGameView?.visibility = View.VISIBLE
        val playerName = "${getString(R.string.remove)} ${playerModel.playerName}?"
        titleTV?.text = playerName

    }

    override fun alertMeWhenHostRemoveMe() {
        user?.let {userInit->   // remove listener
            hostUid?.let {hostId->
                eventListener?.let { eventL-> refGameAlert.child(userInit.uid).child(hostId).removeEventListener(eventL)}
            }
        }
        Toast.makeText(this@AwaitPlayersActivity, getString(R.string.hostRemovePlayer), Toast.LENGTH_LONG).show()

        MainActivity.forwardChatUserId.clear()
        MainActivity.selectedUserNames.clear()
        finish()
//        println("finish here")
    }

}









