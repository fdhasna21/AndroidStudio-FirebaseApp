package com.fdhasna21.postitfirebase.adapter

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.Post
import com.google.firebase.database.DatabaseReference

class PostViewHolder(val binding: RowUserBinding) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var popUpMenu : PopupMenu
    fun setPost(context: Context, post:Post, userId:String?, postReference:DatabaseReference){
        binding.apply {
            rowContent.text = post.content
            rowTime.text = post.time
            rowUserEmail.text = post.userEmail
            rowUsername.text = post.userName
            Glide.with(context)
                .load(post.userPhotoUrl)
                .circleCrop()
                .into(rowImageUser)

            if(userId != post.userUid){
                rowOptions.visibility = View.GONE
            }

            when(post.type){
            "image" -> {
                binding.rowImagePost.visibility = View.VISIBLE
                Glide.with(context)
                    .load(post.postUrl)
                    .into(rowImagePost)
                }
            }

            rowOptions.setOnClickListener { view ->
                popUpMenu = PopupMenu(context, view)
                popUpMenu.menuInflater.inflate(R.menu.menu_post, popUpMenu.menu)
                popUpMenu.setOnMenuItemClickListener {
                    popUpMenu = PopupMenu(context, view)
                    when(it.itemId){
                        R.id.menu_edit -> {
                            Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.menu_post_delete -> {
                            Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                            postReference.removeValue()
                            true
                        }
                        else -> true
                    }
                }
//            if(userId != post.userUid){
//                popUpMenu.menu.findItem(R.id.menu_post_delete).isVisible = false
//
//            }
                popUpMenu.show()
            }
        }





    }
}