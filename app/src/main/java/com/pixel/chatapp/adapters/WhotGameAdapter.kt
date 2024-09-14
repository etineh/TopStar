package com.pixel.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pixel.chatapp.R
import com.pixel.chatapp.model.WhotGameModel

class WhotGameAdapter(private val itemList: MutableList<WhotGameModel>, private val context : Context)
    : RecyclerView.Adapter<WhotGameAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.whotImage_IV)
//        val textView: TextView = itemView.findViewById(R.id.imageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.whot_card_recy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.imageView.setImageResource(item.imageName)
//        holder.textView.text = item.imageName

        holder.itemView.setOnClickListener{
            Toast.makeText(context, "card in progress", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int = itemList.size
}
