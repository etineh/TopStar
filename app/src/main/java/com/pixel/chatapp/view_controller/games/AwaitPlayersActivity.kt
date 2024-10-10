package com.pixel.chatapp.view_controller.games

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pixel.chatapp.R
import com.pixel.chatapp.adapters.AwaitPlayerA
import com.pixel.chatapp.services.api.dao_interface.GameAPI
import com.pixel.chatapp.services.api.model.incoming.ResultApiM
import com.pixel.chatapp.services.api.model.outgoing.TwoValueM
import com.pixel.chatapp.constants.K
import com.pixel.chatapp.view_controller.games.whot.WhotGameActivity
import com.pixel.chatapp.view_controller.games.whot.WhotLandscapeActivity
import com.pixel.chatapp.view_controller.games.whot.WhotOptionActivity
import com.pixel.chatapp.view_controller.MainActivity
import com.pixel.chatapp.dataModel.AwaitPlayerM
import com.pixel.chatapp.dataModel.SignalPlayerM
import com.pixel.chatapp.utilities.GameUtils
import com.pixel.chatapp.utilities.IdTokenUtil
import com.pixel.chatapp.utilities.PhoneUtils
import com.pixel.chatapp.utilities.PhoneUtils.CheckInternet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicInteger


class AwaitPlayersActivity : AppCompatActivity(), AwaitPlayerA.RemovePlayerListener {

    private lateinit var backPressIV: ImageView
    private lateinit var quitTimeCountTV: TextView
    private lateinit var recyclerPlayers: RecyclerView
    private lateinit var onboardingTV: TextView
    private lateinit var numberJoinedTV: TextView
    private lateinit var startGameButton: TextView
    private lateinit var rewardAmountTV: TextView
    private lateinit var addPlayerIV: ImageView
    private var swipeRefreshLayout: SwipeRefreshLayout? = null


    private var startOrQuitGameView: View? = null
    private lateinit var quitOrStart: String
    private var quitTimeCount: CountDownTimer? = null

    private lateinit var awaitPlayerA: AwaitPlayerA

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    private var hostUid: String? = null

    private var progressBar: ProgressBar? = null
    private var titleTV: TextView? = null
    private var yesStartTV: TextView? = null

    object PlayersUid {
        @JvmField
        var playerUidList: MutableList<String?> = mutableListOf()
    }

    private var removePlayerList: MutableList<AwaitPlayerM> = mutableListOf()
    private val joinedPlayersList : MutableList<AwaitPlayerM> = mutableListOf()

    private var eventListener: ValueEventListener? = null
    private lateinit var refGameAlert: DatabaseReference

    private var gameStartEventListener: ValueEventListener? = null
    private lateinit var refGameStarts: DatabaseReference

    private var finalQuit: Boolean = false
    private var notifyNow: Boolean = false

    private var playerJoinNumber: Int = 1
    private var playerCount: Int = 1

    private var removePlayerUid : String? = null

    private var gameId : String? = null
//    private val startGameMap = mutableMapOf<String, Any?>()

    private var signalPlayerM : SignalPlayerM? = null

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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
//        rewardAmountTV = findViewById(R.id.rewardAmount_TV)

        refGameAlert = FirebaseDatabase.getInstance().getReference("GameAlert")
        refGameStarts = FirebaseDatabase.getInstance().getReference("GameStarts")

        // Setup the pull-to-refresh listener
        swipeRefreshLayout?.setOnRefreshListener{

            PhoneUtils.hasInternetConnectivity(object : CheckInternet{
                override fun networkIsTrue() {
                    user?.let {userInit->   // remove listener
                        hostUid?.let {hostId->
                            eventListener?.let { eventL-> refGameAlert.child(userInit.uid).child(hostId).removeEventListener(eventL)}
                        }
                    }
                    updateNumberOfPlayerJoined()    // refresh

                    swipeRefreshLayout?.isRefreshing = false
                    Toast.makeText(this@AwaitPlayersActivity, getString(R.string.refreshed), Toast.LENGTH_SHORT).show()
                    if(hostUid != user?.uid) affirmJoin(true)
                }

                override fun networkIsFalse() {
                    swipeRefreshLayout?.isRefreshing = false
                    Toast.makeText(this@AwaitPlayersActivity, getString(R.string.noInternetConnection), Toast.LENGTH_LONG).show()
                }
            })

        }

//        val mode = intent.getStringExtra("mode")
        gameId = intent.getStringExtra("gameID")
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

                            user?.let {userInit->   // remove listener
                                hostUid?.let {hostId->
                                    eventListener?.let { eventL-> refGameAlert.child(userInit.uid).child(hostId).removeEventListener(eventL)}
                                }
                            }
                            updateNumberOfPlayerJoined()    // refresh

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

                joinedPlayersList.clear()

                val players = MainActivity.selectedPlayerMList
                    .filter { it.signalUpdate == "join" || it.signalUpdate == "hostAdmin" }  // Filter "join" and "hostAdmin"
                    .also { joinedPlayers -> joinedPlayersList.addAll(joinedPlayers) }
                    .joinToString("\n") { player ->
                        if (player.playerUid == user?.uid) " ${getString(R.string.you)}" else player.playerName
                    }

                val numberOfPlayer = "${getString(R.string.startGameWith)} $playerJoinNumber ${getString(R.string.players_)}? \n $players"
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

        checkIfGameHasStarted()

        onBackPressedDispatcher.addCallback(backPress)

    }

    //  =========   method

    private fun affirmJoin(fromRefresh: Boolean? = false){
        IdTokenUtil.generateToken({ token: String? ->
            val twoValueM = TwoValueM(token, hostUid!!)
            val gameAPI = K.retrofit.create(GameAPI::class.java)

            gameAPI.join(twoValueM).enqueue(object : Callback<ResultApiM> {
                override fun onResponse(call: Call<ResultApiM>, response: Response<ResultApiM>) {
                    if (response.isSuccessful) {
                        if (response.body()!!.result == "success" && fromRefresh == true)
                        {
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.awaitingPlayer), Toast.LENGTH_SHORT).show()
                        } else if (response.body()!!.result == "insufficient funds")
                        {
                            rejectGame()
                            quitTimeCount?.start()
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.insufficientBal), Toast.LENGTH_SHORT).show()
                            MainActivity.onGameNow = false
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<ResultApiM>, throwable: Throwable) {
                    Toast.makeText(this@AwaitPlayersActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                    println("what is wallet error occur AwaitPlayerActivity L207: " + throwable.message)
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
                        progressBar?.visibility = View.INVISIBLE
                        yesStartTV?.visibility = View.VISIBLE
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
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {

                        PlayersUid.playerUidList.clear()
                        playerJoinNumber = 1
                        signalPlayerM = snapshot.getValue(SignalPlayerM::class.java)

                        var count = 0
                        val totalPlayer = snapshot.child("players").children.count()

                        val stakeAmount = signalPlayerM?.stakeAmount?.toDoubleOrNull() ?: 0.0
                        val totalStake = stakeAmount * totalPlayer
                        val stakingAndWins = "${getString(R.string.entryFee)} $${stakeAmount} " +
                                "\n${getString(R.string.estWin)} $${totalStake}"
                        rewardAmountTV.text = stakingAndWins

                        if(notifyNow) MainActivity.selectedPlayerMList.clear()

                        for (playerSnapshot in snapshot.child("players").children)
                        {
                            val player = playerSnapshot.getValue(AwaitPlayerM::class.java)
                            if(notifyNow) MainActivity.selectedPlayerMList.add(player)

                            if(player?.signalUpdate == "join"){
                                playerJoinNumber++
                                playerCount = playerJoinNumber
                            }
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

    private fun checkIfGameHasStarted(){

        gameId?.let {gameId ->

            gameStartEventListener = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists()) {

                        val atomicInteger = AtomicInteger(0)
//                        signalPlayerM = snapshot.getValue(SignalPlayerM::class.java)

                        if(snapshot.child("players").child(user?.uid!!).exists()) {

                            val playersInGame : MutableList<AwaitPlayerM> = mutableListOf()

                            for (playerSnapshot in snapshot.child("players").children) {

                                val getPlayer = playerSnapshot.getValue(AwaitPlayerM::class.java)
                                playersInGame.add(getPlayer!!)

                                if(atomicInteger.incrementAndGet() == playerCount) {
                                    startGame(playersInGame)
                                }
                            }

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            }

            gameStartEventListener?.let { listen-> refGameStarts.child(gameId).child(hostUid!!).addValueEventListener(listen) }

        }

    }

    private fun setStartOrQuitGameView(){

        if(startOrQuitGameView == null){
            val startGameLayoutVS = findViewById<ViewStub>(R.id.startGameLayout)
            startOrQuitGameView = startGameLayoutVS.inflate()

            yesStartTV = startOrQuitGameView?.findViewById(R.id.yesTV)
            val noTV = startOrQuitGameView?.findViewById<TextView>(R.id.noTV)
            titleTV = startOrQuitGameView?.findViewById(R.id.sureNoticeTV)
            progressBar = startOrQuitGameView?.findViewById(R.id.progressBar9)

            yesStartTV?.setOnClickListener {
                progressBar?.visibility = View.VISIBLE
                yesStartTV?.visibility = View.INVISIBLE

                if(quitOrStart == "start"){
                    MainActivity.isOnGameNow = true
                    prepareStartGame()
                    quitTimeCount?.cancel()
                } else if (quitOrStart == "quit"){

                    quitTimeCount?.cancel()
                    when(user?.uid){
                        hostUid -> {    // remove all players
                            PlayersUid.playerUidList.forEach { eachUid -> removePlayer(eachUid) }
                            finish()
                        }
                        else -> rejectGame()
                    }
                    MainActivity.onGameNow = false
                    gameStartEventListener?.let { listen-> refGameStarts.child(gameId!!).child(hostUid!!).removeEventListener(listen) }

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

    private fun startGame(playersInGameList : MutableList<AwaitPlayerM>){

        val goToWhotGameIntent : Intent = if(playersInGameList.size > 2) {
            Intent(this, WhotLandscapeActivity::class.java)
        } else {
            Intent(this, WhotGameActivity::class.java)
        }
        // Put the list as a Parcelable extra
        goToWhotGameIntent.putParcelableArrayListExtra("playerDetailList", ArrayList(playersInGameList))
        goToWhotGameIntent.putExtra("gameId", gameId!!)
        goToWhotGameIntent.putExtra("hostId", hostUid)
        startActivity(goToWhotGameIntent)

        Handler(Looper.getMainLooper()).postDelayed({startOrQuitGameView?.visibility = View.GONE}, 2000)

        gameStartEventListener?.let { refGameStarts.child(gameId!!).child(hostUid!!).removeEventListener(it) }

        MainActivity.forwardChatUserId.clear()
        MainActivity.selectedUserNames.clear()

        finish()

    }

    private fun prepareStartGame() {

        IdTokenUtil.generateToken({token ->

            val gameAPI = K.retrofit.create(GameAPI::class.java)

            val twoValueM = TwoValueM(token, hostUid!!)

            gameAPI.startGame(twoValueM).enqueue(object : Callback<ResultApiM> {
                override fun onResponse(p0: Call<ResultApiM>, p1: Response<ResultApiM>) {
                    if (p1.isSuccessful) {
                        when (p1.body()?.result) {
                            "success" -> {
                                Toast.makeText(this@AwaitPlayersActivity, getString(R.string.startingGame), Toast.LENGTH_SHORT).show()
                            }
                            "insufficient funds" -> {
                                progressBar?.visibility = View.INVISIBLE
                                yesStartTV?.visibility = View.VISIBLE
                                MainActivity.isOnGameNow = false
                                Toast.makeText(this@AwaitPlayersActivity, getString(R.string.insufficientBal), Toast.LENGTH_SHORT).show()
                            }
                            else -> {   // Handle unexpected result
                                progressBar?.visibility = View.INVISIBLE
                                yesStartTV?.visibility = View.VISIBLE
                                MainActivity.isOnGameNow = false
                                Toast.makeText(this@AwaitPlayersActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {    // Handle error response from server
                        progressBar?.visibility = View.INVISIBLE
                        yesStartTV?.visibility = View.VISIBLE
                        MainActivity.isOnGameNow = false
                        if(p1.errorBody()?.string() == "user not found"){
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.userNotFound), Toast.LENGTH_SHORT).show()
                        } else if(p1.errorBody()?.string() == "no player found" || p1.errorBody()?.string() == "Game details not found") {
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.no_player_found), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AwaitPlayersActivity, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                        }

                    }
                }

                override fun onFailure(p0: Call<ResultApiM>, p1: Throwable) {
                    progressBar?.visibility = View.INVISIBLE
                    yesStartTV?.visibility = View.VISIBLE
                    Toast.makeText(this@AwaitPlayersActivity, p1.message ?: getString(R.string.errorOccur), Toast.LENGTH_SHORT).show()
                }

            })

        }, this)

    }

    private fun rejectGame(){
        hostUid?.let { uid ->
            GameUtils.rejectGameOrAddNewPlayer(this, uid, null, null, object : GameUtils.RejectGameInterface
            {
                override fun onSuccess() {
                    quitTimeCount?.cancel()
                    finish()
                }

                override fun onFailure() {
                    progressBar?.visibility = View.INVISIBLE
                    yesStartTV?.visibility = View.VISIBLE
                    Toast.makeText(this@AwaitPlayersActivity, getString(R.string.noInternetConnection), Toast.LENGTH_SHORT).show()
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
                    } else WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(false, gameHasStarted = false)

                } else {
                    closePage()
                }
            }
        }
    }

    private fun closePage(){
        PlayersUid.playerUidList.forEach { eachUid -> removePlayer(eachUid) }
        quitTimeCount?.cancel()
        user?.let {userInit->   // remove listener
            hostUid?.let {hostId->
                eventListener?.let { eventL-> refGameAlert.child(userInit.uid).child(hostId).removeEventListener(eventL) }
            }
        }
        gameStartEventListener?.let { listen -> refGameStarts.child(gameId!!).child(hostUid!!).removeEventListener(listen) }

//        refGameAlert.child(user?.uid!!).removeValue()
        MainActivity.onGameNow = false
        MainActivity.isOnGameNow = false
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

                WhotOptionActivity.TriggerInterface.triggerOnForward.showMinimiseGameAlert(true, gameHasStarted = false)
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









