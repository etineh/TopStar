package com.pixel.chatapp.view_controller.games.whot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewStub
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.pixel.chatapp.R
import com.pixel.chatapp.services.api.model.incoming.AssetsModel
import com.pixel.chatapp.view_controller.games.whot.WhotOptionActivity.TriggerInterface.triggerOnForward
import com.pixel.chatapp.view_controller.MainActivity
import com.pixel.chatapp.interface_listeners.TriggerOnForward
import com.pixel.chatapp.interface_listeners.WalletCallBack
import com.pixel.chatapp.utilities.AnimUtils
import com.pixel.chatapp.utilities.PhoneUtils
import com.pixel.chatapp.utilities.PhoneUtils.CheckInternet
import com.pixel.chatapp.utilities.WalletUtils

class WhotOptionActivity : AppCompatActivity() {

    val mainActivity = MainActivity()

    private lateinit var gameMode: String

    private var chooseModeView: View? = null
    private var gameAsset: String? = null
    private var selectPlayerButton: TextView? = null

    object TriggerInterface {
        lateinit var triggerOnForward : TriggerOnForward
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whot_option)

        val quickPlayButton: ImageView = findViewById(R.id.whot_quickplay_button)
        val multiPlayButton: ImageView = findViewById(R.id.whot_multiplay_button)
        val tournamentButton: ImageView = findViewById(R.id.whot_tournament_button)
        val leagueButton: ImageView = findViewById(R.id.whot_league_button)
        val settingButton: ImageView = findViewById(R.id.settingButton)
        val closePageIV: ImageView = findViewById(R.id.closePageIV)

        gameMode = "free"

        if(MainActivity.targetPlayer) {
            setChooseModeView()
            chooseModeView?.visibility = View.VISIBLE

            selectPlayerButton?.let {
                selectPlayerButton?.text = getString(R.string.proceed)
            }
        } else {
            selectPlayerButton?.text = getString(R.string.selectPlayer)
        }

        quickPlayButton.setOnClickListener{
            it.animate().scaleX(1.1f).scaleY(1.1f).withEndAction {

                WhotGameActivity.PlayMode.playMode = "quickPlay"
                val mainActivityIntent = Intent(this, WhotGameActivity::class.java)
//                val mainActivityIntent = Intent(this, WhotLandscapeActivity::class.java)
                startActivity(mainActivityIntent)

                it.scaleX = 1f
                it.scaleY = 1f

//                Handler(Looper.getMainLooper()).postDelayed({
//                    finish()
//                }, 1000)
            }
        }

        multiPlayButton.setOnClickListener{
            it.animate().scaleX(1.1f).scaleY(1.1f).withEndAction{

                setChooseModeView()
                chooseModeView?.visibility = View.VISIBLE

                it.scaleX = 1f
                it.scaleY = 1f
            }
        }

        tournamentButton.setOnClickListener{
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show()
        }

        leagueButton.setOnClickListener{
            it.animate().scaleX(1.1f).scaleY(1.1f).withEndAction {

                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show()

                it.scaleX = 1f
                it.scaleY = 1f
            }
        }

        settingButton.setOnClickListener{
            it.animate().scaleX(1.1f).scaleY(1.1f).withEndAction{

                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show()

                it.scaleX = 1f
                it.scaleY = 1f
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        closePageIV.setOnClickListener { onBackPressedDispatcher.onBackPressed() }


    }

    //  ===========     methods

    private fun setChooseModeView(){
        if(chooseModeView == null){
            val chooseModeViewStub: ViewStub = findViewById(R.id.gameModeLayout)
            chooseModeView = chooseModeViewStub.inflate()

            val arrowBack = chooseModeView?.findViewById<ImageView>(R.id.arrowBackS)
            val freeRadioButton = chooseModeView?.findViewById<RadioButton>(R.id.freeRadioButton)
            val stakeRadioButton = chooseModeView?.findViewById<RadioButton>(R.id.stakeRadioButton)
            val stakeInfoContainer = chooseModeView?.findViewById<ConstraintLayout>(R.id.stakeInfoContainer)
            val stakeFeeET = chooseModeView?.findViewById<EditText>(R.id.entryFee_ET)
            val avaliableBalTV = chooseModeView?.findViewById<TextView>(R.id.avaliableBal_TV)
            val topupTV = chooseModeView?.findViewById<TextView>(R.id.topup_TV)
            val noteET = chooseModeView?.findViewById<EditText>(R.id.note_ET)
            val loadProgressBar = chooseModeView?.findViewById<ProgressBar>(R.id.loadProgressBar)
            selectPlayerButton = chooseModeView?.findViewById(R.id.selectPlayerbutton)
            selectPlayerButton?.text = getString(R.string.selectPlayer)

            updateGameBalance(object : GoToPlayerListener {
                override fun proceedToPlayer(assetsModel: AssetsModel) {
                    gameAsset = assetsModel.gameAsset
                    val gameBal = "${getString(R.string.gameAsset_)} $${assetsModel.gameAsset}"
                    avaliableBalTV?.text = gameBal
                }
            })

            freeRadioButton?.isChecked = true

            arrowBack?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            topupTV?.setOnClickListener {
                Toast.makeText(this, getString(R.string.workInProgress), Toast.LENGTH_LONG).show()
            }

            freeRadioButton?.setOnClickListener {
//                gameMode = getString(R.string.mode_) + " " + getString(R.string.free)
                gameMode = "free"
                AnimUtils.slideOutToBottom(stakeInfoContainer, 300)
                stakeFeeET?.text?.clear()
            }

            stakeRadioButton?.setOnClickListener {
                if(stakeInfoContainer?.visibility == View.GONE) AnimUtils.slideInFromBottom(stakeInfoContainer, 300)
                gameMode = "stake"
//                gameMode = getString(R.string.mode_) + " " + getString(R.string.stake)
                stakeFeeET?.requestFocus()

                updateGameBalance(object : GoToPlayerListener {
                    override fun proceedToPlayer(assetsModel: AssetsModel) {
                        gameAsset = assetsModel.gameAsset
                        val gameBal = "${getString(R.string.gameAsset_)} $${assetsModel.gameAsset}"
                        avaliableBalTV?.text = gameBal
                    }
                })
            }

            stakeInfoContainer?.setOnClickListener {
                PhoneUtils.hideKeyboard(this, stakeFeeET)
            }


            selectPlayerButton?.setOnClickListener {

                WhotGameActivity.PlayMode.playMode = "multiPlay"
                MainActivity.onGameNow = true
                var proceed = true

                if(!MainActivity.targetPlayer) {
                    MainActivity.selectedUserNames.clear()
                    MainActivity.forwardChatUserId.clear()
                    MainActivity.selectedPlayerMList.clear()

                    MainActivity.onSelectPlayer = true
                }

                val stakeMount = if((stakeFeeET?.text?.length ?: 0) < 1) "0" else stakeFeeET?.text.toString()
                val hostNote = if((noteET?.text?.length ?: 0) < 2) getString(R.string.dash) else noteET?.text.toString()

                if(stakeRadioButton?.isChecked == true){
                    proceed = true
                    if (stakeMount.toDouble() < 0.1){
                        Toast.makeText(this, getString(R.string.invalidAmount), Toast.LENGTH_LONG).show()
                        proceed = false
                    } else if (gameAsset != null && (gameAsset?.toDouble() ?: -9.0) < stakeMount.toDouble()){
                        Toast.makeText(this, getString(R.string.insufficientBal), Toast.LENGTH_LONG).show()
                        proceed = false
                    }
                }

                if(proceed){

                    if(gameAsset == null){

                        updateGameBalance(object : GoToPlayerListener {
                            override fun proceedToPlayer(assetsModel: AssetsModel) {
                                gameAsset = assetsModel.gameAsset
                                val gameBal = "${getString(R.string.gameAsset_)} $${assetsModel.gameAsset}"
                                avaliableBalTV?.text = gameBal

                                goToSelectPlayer(stakeMount, hostNote, stakeFeeET, loadProgressBar,
                                    noteET, stakeInfoContainer, freeRadioButton, selectPlayerButton)

                            }
                        })

                        loadProgressBar?.visibility = View.VISIBLE
                        selectPlayerButton?.visibility = View.INVISIBLE

                        PhoneUtils.hasInternetConnectivity(object : CheckInternet{
                            override fun networkIsTrue() {
                                loadProgressBar?.visibility = View.GONE
                                selectPlayerButton?.visibility = View.VISIBLE
                            }

                            override fun networkIsFalse() {
                                Toast.makeText(this@WhotOptionActivity, getString(R.string.noInternetConnection), Toast.LENGTH_LONG).show()
                                loadProgressBar?.visibility = View.GONE
                                selectPlayerButton?.visibility = View.VISIBLE
                            }
                        })

                    } else {

                        goToSelectPlayer(stakeMount, hostNote, stakeFeeET, loadProgressBar, noteET,
                            stakeInfoContainer, freeRadioButton, selectPlayerButton)

                    }
                }

            }

//            AnimUtils.linearSlidingAnimation(headingTV, 2000)

        }
    }

    private fun goToSelectPlayer(stakeMount: String, hostNote: String, stakeFeeET: EditText?,
                                 loadProgressBar: ProgressBar?, noteET: EditText?, stakeInfoContainer: ConstraintLayout?,
                                 freeRadioButton: RadioButton?, selectPlayerButton: TextView?)
    {
        if(MainActivity.targetPlayer) {
            loadProgressBar?.visibility = View.VISIBLE
            selectPlayerButton?.visibility = View.INVISIBLE

            PhoneUtils.hasInternetConnectivity(object : CheckInternet{
                override fun networkIsTrue() {
                    triggerOnForward.proceedToAwaitActivity(stakeMount, gameMode, hostNote)
                    finish()
                    MainActivity.targetPlayer = false

                    closeModeView(noteET, stakeFeeET, stakeInfoContainer, freeRadioButton)
                }

                override fun networkIsFalse() {
                    Toast.makeText(this@WhotOptionActivity, getString(R.string.noInternetConnection), Toast.LENGTH_LONG).show()
                    loadProgressBar?.visibility = View.GONE
                    selectPlayerButton?.visibility = View.VISIBLE
                }
            })

        } else {
            triggerOnForward.openOnForwardView()

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            mainActivityIntent.putExtra("gameMode", gameMode)
            mainActivityIntent.putExtra("stakeAmount", stakeMount)
            mainActivityIntent.putExtra("hostNote", hostNote)
            mainActivityIntent.putExtra("refreshList", true)

            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(mainActivityIntent)

            closeModeView(noteET, stakeFeeET, stakeInfoContainer, freeRadioButton)
        }

    }

    private fun closeModeView(noteET: EditText?, stakeFeeET: EditText?, stakeInfoContainer: ConstraintLayout?, freeRadioButton: RadioButton?)
    {
        Handler(Looper.getMainLooper()).postDelayed({

            chooseModeView?.visibility = View.GONE
            stakeFeeET?.text?.clear()
            noteET?.text?.clear()
            AnimUtils.slideOutToBottom(stakeInfoContainer, 300)
            gameMode = "free"

            freeRadioButton?.isChecked = true

        }, 2000)
    }
    private fun updateGameBalance(goToPlayerListener: GoToPlayerListener)
    {
        WalletUtils.balance(this, object : WalletCallBack{
            override fun onSuccess(assetsModel: AssetsModel) {
                goToPlayerListener.proceedToPlayer(assetsModel)
            }

            override fun onFailure() {
                Toast.makeText(this@WhotOptionActivity, getString(R.string.noInternetConnection), Toast.LENGTH_LONG).show()
            }
        })
    }


    val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if(chooseModeView?.visibility == View.VISIBLE && !MainActivity.targetPlayer){
                chooseModeView?.visibility = View.GONE
            } else {
                triggerOnForward.openSelectGameOption()
                MainActivity.targetPlayer = false
                MainActivity.onGameNow = false
                finish()
            }
        }
    }

    interface GoToPlayerListener{
        fun proceedToPlayer(assetsModel: AssetsModel)
    }

}














