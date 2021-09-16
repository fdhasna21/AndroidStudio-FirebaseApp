package com.fdhasna21.postitfirebase.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.activity.MessagesActivity
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.Profile

class UserViewHolder(val binding : RowUserBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setUser(context: Context, user:Profile, userId:String?){
        binding.apply {
            if (user.uid != userId) {
                rowContent.visibility = View.GONE
                rowUserEmail.visibility = View.GONE
                rowTime.visibility = View.GONE
                rowOptions.visibility = View.GONE

                rowUsername.text = user.name
                Glide.with(context)
                    .load(user.url)
                    .circleCrop()
                    .into(rowImageUser)

                rowLayout.setOnClickListener {
                    val intent = Intent(context, MessagesActivity::class.java)
                    intent.putExtra("other_user", user)
                    context.startActivity(intent)
                }
            } else {
//                rowLayout.visibility = View.GONE
            }
        }
    }
}