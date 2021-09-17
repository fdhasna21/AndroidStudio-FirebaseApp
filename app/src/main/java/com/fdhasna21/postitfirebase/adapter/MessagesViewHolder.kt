package com.fdhasna21.postitfirebase.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.activity.MessagesActivity
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.ChatMessage
import com.fdhasna21.postitfirebase.dataclass.Profile

class MessagesViewHolder(val binding : RowUserBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setChat(context: Context, message: ChatMessage?=null, target : Profile?){
        binding.apply {
            rowContent.visibility = View.GONE
            rowTime.visibility = View.GONE
            rowOptions.visibility = View.GONE

            rowUsername.text = target?.name
            Glide.with(context)
                .load(target?.url)
                .circleCrop()
                .into(rowImageUser)

            if(message?.content.isNullOrEmpty()){
                when(message?.type){
                    "image" -> {
                        rowUserEmail.text = "Photo"
                        rowUserEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo, 0, 0, 0)
                        rowUserEmail.compoundDrawablePadding = 12
                    }
                    "video" -> "Video"
                }
            } else {
                rowUserEmail.text = message?.content
            }

            rowLayout.setOnClickListener {
                val intent = Intent(context, MessagesActivity::class.java)
                intent.putExtra("target", target)
                context.startActivity(intent)
            }
        }
    }
}
