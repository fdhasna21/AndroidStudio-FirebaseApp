package com.fdhasna21.postitfirebase

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.fdhasna21.postitfirebase.databinding.RowPostBinding
import com.fdhasna21.postitfirebase.dataclass.Post
import com.google.firebase.database.DatabaseReference

class PostViewHolder(val binding: RowPostBinding) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var popUpMenu : PopupMenu
    fun setPost(context: Context, post:Post, userId:String?, postReference:DatabaseReference){
        binding.rowContent.text = post.content
        binding.rowTime.text = post.time
        binding.rowUserEmail.text = post.userEmail
        binding.rowUsername.text = post.userName
        Glide.with(context)
            .load(post.userPhotoUrl)
            .circleCrop()
            .into(binding.rowImageUser)

        if(userId != post.userUid){
            binding.rowOptions.visibility = View.GONE
        }

        when(post.type){
            "image" -> {
                binding.rowImagePost.visibility = View.VISIBLE
                Glide.with(context)
                    .load(post.postUrl)
                    .into(binding.rowImagePost)
            }
        }

        binding.rowOptions.setOnClickListener { view ->
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