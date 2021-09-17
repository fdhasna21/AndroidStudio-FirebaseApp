package com.fdhasna21.postitfirebase.adapter

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.databinding.RowMessageBubbleBinding
import com.fdhasna21.postitfirebase.dataclass.ChatMessage
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

class RoomChatViewHolder(val binding: RowMessageBubbleBinding) : RecyclerView.ViewHolder(binding.root), View.OnLongClickListener {
    fun setBubbleChat(context: Context, message:ChatMessage, userId : String?, target:Profile?, isDateChanged:Boolean=false){
        binding.apply {
            senderLayout.setOnLongClickListener(this@RoomChatViewHolder)
            receiverLayout.setOnLongClickListener(this@RoomChatViewHolder)

            val timestamp = Calendar.getInstance().time
            val todayDate = SimpleDateFormat("dd-MMMM-yyyy").format(timestamp)

            if(isDateChanged){
                dateLayout.visibility = View.VISIBLE
                dateContent.text = if(todayDate == message.date){
                    "Today"
                } else {
                    message.date?.replace("-", " ")
                }
            } else {
                dateLayout.visibility = View.GONE
            }

            if(userId == message.senderUID){
                receiverLayout.visibility = View.GONE
                senderLayout.visibility = View.VISIBLE

                if(message.content.isNullOrEmpty()){
                    senderMessage.visibility = View.GONE
                } else {
                    senderMessage.text = message.content
                }
                senderTimestamp.text = message.time
                when(message.type){
                    "image" -> {
                        senderImageView.visibility = View.VISIBLE
                        Glide.with(context)
                            .load(message.messageUrl)
                            .into(senderImageView)
                    }
                }
            } else {
                senderLayout.visibility = View.GONE
                receiverLayout.visibility = View.VISIBLE

                if(message.content.isNullOrEmpty()){
                    receiverMessage.visibility = View.GONE
                } else {
                    receiverMessage.text = message.content
                }
                receiverTimestamp.text = message.time
                Glide.with(context)
                    .load(target?.url)
                    .circleCrop()
                    .into(receiverImage)

                when(message.type){
                    "image" -> {
                        receiverImageView.visibility = View.VISIBLE
                        Glide.with(context)
                            .load(message.messageUrl)
                            .into(receiverImageView)
                    }
                }
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        Log.i("messageActivity", "onLongClick: $v")
        return true
    }

}
