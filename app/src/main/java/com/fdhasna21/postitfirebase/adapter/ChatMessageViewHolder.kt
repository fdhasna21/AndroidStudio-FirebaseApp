package com.fdhasna21.postitfirebase.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.ChatMessage
import com.google.firebase.database.DatabaseReference

class ChatMessageViewHolder(val binding : RowUserBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setChat(context: Context, message: ChatMessage, userId:String?, messageReference : DatabaseReference){
        binding.apply {
            rowContent.visibility = View.GONE
            rowTime.visibility = View.GONE
            rowOptions.visibility = View.GONE
        }
    }
}
