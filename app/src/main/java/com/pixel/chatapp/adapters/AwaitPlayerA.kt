package com.pixel.chatapp.adapters

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.pixel.chatapp.R
import com.pixel.chatapp.dataModel.AwaitPlayerM
import com.pixel.chatapp.utilities.GameUtils
import com.pixel.chatapp.utilities.ProfileUtils
import com.squareup.picasso.Picasso

class AwaitPlayerA (
    private val playerList: MutableList<AwaitPlayerM>,
    private val hostUid: String?,
    private val context : Context
) : RecyclerView.Adapter<AwaitPlayerA.ViewHolder>() {

    var myUid : String? = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val rejectTimeMap: MutableMap<String, Long> = mutableMapOf()
    private lateinit var removePlayerListener: RemovePlayerListener

    fun setRemovePlayerListener(removePlayer: RemovePlayerListener){
        removePlayerListener = removePlayer
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerPhotoIV: ImageView = itemView.findViewById(R.id.userPhotoIV)
        val playerNameTV: TextView = itemView.findViewById(R.id.playerName_TV)
        val signalUpdateTV: TextView = itemView.findViewById(R.id.signalUpdate_TV)
        val re_addTV: TextView = itemView.findViewById(R.id.readdTV)
        val removePlayerIV: ImageView = itemView.findViewById(R.id.removePlayerIV)
        val micStatusIV: ImageView = itemView.findViewById(R.id.micStatusIV)

        var countdownTimer: CountDownTimer? = null

        // Clear the timer when the view is recycled
        fun clearCountdown() {
            countdownTimer?.cancel()
            countdownTimer = null
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.await_player_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = playerList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val playerM = playerList[position]

        resetLayout(holder, playerM)

        val playerName = playerM.playerName
        val playerPhotoUri = playerM.photoUri
        val playerUid = playerM.playerUid
        val playerSignal = playerM.signalUpdate

        if(playerPhotoUri != "null"){
            Picasso.get().load(playerPhotoUri).into(holder.playerPhotoIV)
        }

        if(playerName == ProfileUtils.getMyDisplayOrUsername()){
            holder.playerNameTV.text = context.getString(R.string.you)
        } else {
            holder.playerNameTV.text = ProfileUtils.getOtherDisplayOrUsername(playerUid, playerName)
        }

        if(hostUid == myUid) {
            holder.removePlayerIV.visibility = View.VISIBLE
            if (playerSignal == "hostAdmin") holder.removePlayerIV.visibility = View.GONE
        }

        if(playerUid == myUid && playerSignal == "reject") {
            removePlayerListener.alertMeWhenHostRemoveMe()
        }

        // Cancel any existing countdown for the ViewHolder
        holder.clearCountdown()

        when (playerSignal) {
            "join" -> holder.signalUpdateTV.text = context.getString(R.string.joined)
            "await" -> holder.signalUpdateTV.text = context.getString(R.string.awaiting)
            "signal" -> holder.signalUpdateTV.text = context.getString(R.string.signalling)
            "hostAdmin" -> holder.signalUpdateTV.text = context.getString(R.string.joinHost)
            "reject" -> {
                holder.signalUpdateTV.text = context.getString(R.string.rejected)
                rejectTimeCount(holder, playerUid)
            }
        }

        holder.re_addTV.setOnClickListener {
            Toast.makeText(context, context.getString(R.string.reAddNotice), Toast.LENGTH_LONG).show()
        }

        holder.removePlayerIV.setOnClickListener {
            removePlayerListener.removePlayer(playerList[position])
        }

    }


    // ========     methods

    private fun resetLayout( holder: ViewHolder, playerM: AwaitPlayerM ){
        holder.playerNameTV.text = null
        holder.playerPhotoIV.setImageResource(com.sp.shuftipro_sdk.R.drawable.sp_face_in_oval)
        holder.signalUpdateTV.text = context.getString(R.string.signalling)
        holder.re_addTV.visibility = View.GONE
        holder.removePlayerIV.visibility = View.GONE
    }


    private fun rejectTimeCount(holder: ViewHolder, playerUid: String){

        if(hostUid == myUid) {
            holder.re_addTV.visibility = View.VISIBLE

            // Check if playerUid is already in the map
            val rejectTime = if (rejectTimeMap.containsKey(playerUid)) {
                // Use the existing reject time if already present
                rejectTimeMap[playerUid]!!
            } else {
                // Save the current time in the map if not already present
                val currentTime = System.currentTimeMillis()
                rejectTimeMap[playerUid] = currentTime
                currentTime
            }

            // Start a countdown, subtracting the current time from the reject time
            holder.countdownTimer = object : CountDownTimer(15000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - rejectTime
                    val remainingTime = (elapsedTime - 15_000) / 1000
                    val reAddTime = "${context.getString(R.string.re_add)} $remainingTime s"
                    holder.re_addTV.text = reAddTime
                }

                override fun onFinish() {

                    val position = holder.adapterPosition
                    if (position != RecyclerView.NO_POSITION  && position < playerList.size) {
                        playerList.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        rejectTimeMap.remove(playerUid)
                        
                        // remove from other player list
                        hostUid?.let {hostId ->
                            GameUtils.removePlayer(context, hostId, playerUid, object : GameUtils.RejectGameInterface{
                                override fun onSuccess() {
                                    Toast.makeText(context, context.getString(R.string.playerRemoved), Toast.LENGTH_LONG).show()
                                }

                                override fun onFailure() {
                                    Toast.makeText(context, context.getString(R.string.errorOccur), Toast.LENGTH_LONG).show()
                                }

                            })
                        }

                    }
                }
            }.start()
        }
    }


    interface RemovePlayerListener{
        fun removePlayer(playerModel: AwaitPlayerM)
        fun alertMeWhenHostRemoveMe()
    }


}








