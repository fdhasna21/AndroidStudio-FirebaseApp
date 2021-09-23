package com.fdhasna21.postitfirebase.activity

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.NotificationUtils
import com.fdhasna21.postitfirebase.adapter.RoomChatViewHolder
import com.fdhasna21.postitfirebase.databinding.ActivityMessagesBinding
import com.fdhasna21.postitfirebase.databinding.RowMessageBubbleBinding
import com.fdhasna21.postitfirebase.dataclass.ChatMessage
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessagesActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private lateinit var binding : ActivityMessagesBinding
    private var auth : FirebaseAuth = Firebase.auth
    private var database : FirebaseDatabase = Firebase.database
    private var firestore : FirebaseFirestore = Firebase.firestore
    private var storage : FirebaseStorage = Firebase.storage
    private var currentUser : FirebaseUser? = auth.currentUser
    private var receiver : Profile? = Profile()
    private var message : ChatMessage = ChatMessage()
    private var messages : ArrayList<ChatMessage> = arrayListOf()
    private var selectedUri : Uri? = null

    private lateinit var notif : NotificationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        receiver = intent.getParcelableExtra<Profile>("target")
        supportActionBar?.title = receiver?.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        binding.apply{
            messagesSend.setOnClickListener(this@MessagesActivity)
            messagesEditText.setOnTouchListener(this@MessagesActivity)
            messageAttachCancel.setOnClickListener(this@MessagesActivity)
        }

        setupFirebaseRecycler()
        notif = NotificationUtils(this, "messageActivity", NotificationManager.IMPORTANCE_HIGH, "judul", "deskripsi")
    }

    private fun setupFirebaseRecycler() {
        val reference = database.getReference("messages")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (data: DataSnapshot in snapshot.children) {
                    val message: ChatMessage? = data.getValue(ChatMessage::class.java)
                    message?.let {
                        if ((it.senderUID == currentUser?.uid && it.receiverUID == receiver?.uid)
                            || (it.receiverUID == currentUser?.uid && it.senderUID == receiver?.uid)
                        ) {
                            messages.add(it)
                        }
                    }
                }

                val firebaseAdapter = object : RecyclerView.Adapter<RoomChatViewHolder>(){
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomChatViewHolder {
                        return RoomChatViewHolder(RowMessageBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                    }

                    override fun onBindViewHolder(holder: RoomChatViewHolder, position: Int) {
                        val item = messages[position]
                        if(position!=0){
                            holder.setBubbleChat(this@MessagesActivity, item, currentUser?.uid, receiver, item.date != messages[position-1].date)
                        } else {
                            holder.setBubbleChat(this@MessagesActivity, item, currentUser?.uid, receiver, true)
                        }
                    }

                    override fun getItemCount(): Int {
                        return messages.size
                    }
                }

                firebaseAdapter.notifyDataSetChanged()
                binding.messagesHistory.adapter = firebaseAdapter
                binding.messagesHistory.layoutManager = LinearLayoutManager(this@MessagesActivity)
                binding.messagesHistory.smoothScrollToPosition(firebaseAdapter.itemCount)

                val lastItem : ChatMessage = messages[firebaseAdapter.itemCount-1]
                if(lastItem.senderUID != currentUser?.uid){
                    notif.sendNotification(receiver?.name!!, lastItem.content!!, receiver!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("messagesActivity", "onCancelled: $error")
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        if(isTaskRoot){
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            onBackPressed()
        }

        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.messagesSend -> {
                v.isEnabled = false
                val timestamp = Calendar.getInstance().time
                val date = SimpleDateFormat("dd-MMMM-yyyy").format(timestamp)
                val time = SimpleDateFormat("hh:mm").format(timestamp)

                message.date = date
                message.time = time
                message.receiverUID = receiver?.uid
                message.senderUID = currentUser?.uid
                message.content = binding.messagesEditText.text.toString()
//                notif.sendNotification(receiver?.name!!, message.content!!, receiver!!)

                if(selectedUri == null){
                    message.messageUrl = ""
                    message.type = "text"
                    sendDataMessage()
                } else{
                    val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(selectedUri!!))
                    val storageReference : StorageReference = storage.getReference("messageUrl").child(System.currentTimeMillis().toString() + ".${fileExtension}")
                    val uploadTask = storageReference.putFile(selectedUri!!)
                    uploadTask.continueWithTask {
                        if(!it.isSuccessful){
                            throw it.exception!!.cause!!
                        }
                        storageReference.downloadUrl
                    }.addOnCompleteListener {
                        if (it.isSuccessful) {
                            it.result?.let {
                                message.messageUrl = it.toString()
                                sendDataMessage()
                            }
                        }
                    }
                }
            }
            binding.messageAttachCancel -> {
                selectedUri = null
                message.type = "text"
                binding.messageMedia.visibility = View.GONE
                binding.messagePlayerView.visibility = View.GONE
                binding.messageImageView.visibility = View.GONE
                binding.messageAttachCancel.visibility = View.GONE
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(v){
            binding.messagesEditText -> {
                if(event?.action == MotionEvent.ACTION_UP){
                    if(event.rawX >= (binding.messagesEditText.right - binding.messagesEditText.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                        val intent = Intent()
                        intent.type = "image/* video/* audio/*"
                        intent.putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            arrayOf("image/*", "video/*", "audio/*")
                        )
                        intent.action = Intent.ACTION_GET_CONTENT
                        startForResult.launch(intent)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun sendDataMessage() {
        val reference = database.getReference("messages")
        val messageID = reference.push().key!!
        reference.child(messageID).setValue(message).addOnCompleteListener {
            if(it.isSuccessful){
                binding.messagesEditText.text = null
                message = ChatMessage()
                binding.messageAttachCancel.performClick()
                binding.messagesSend.isEnabled = true
            }
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
        try{
            if(data?.resultCode == Activity.RESULT_OK){
                data.data?.let {
                    selectedUri = it.data
                    val fileType = contentResolver.getType(selectedUri!!)
                    if(fileType!!.contains("image")) {
                        binding.messagePlayerView.visibility = View.GONE
                        binding.messageImageView.visibility = View.VISIBLE
                        binding.messageAttachCancel.visibility = View.VISIBLE
                        binding.messageMedia.visibility = View.VISIBLE

                        Glide.with(this).load(selectedUri!!).into(binding.messageImageView)
                        message.type = "image"
                    }
                    else if(fileType.contains("video") || fileType.contains("audio"))
                    {
                        binding.messagePlayerView.visibility = View.VISIBLE
                        binding.messageImageView.visibility = View.GONE
                        binding.messageAttachCancel.visibility = View.VISIBLE
                        binding.messageMedia.visibility = View.VISIBLE

                        val exoPlayer = SimpleExoPlayer.Builder(this).build()
                        val dataSourceFactory = DefaultDataSourceFactory(this)
                        val mediaItem = MediaItem.fromUri(selectedUri!!)
                        val mediaSource = ProgressiveMediaSource
                            .Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)

                        exoPlayer.setMediaSource(mediaSource)
                            exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                        message.type =
                            if(fileType.contains("video")) {
                            "video"
                            } else {
                                "audio"
                            }
                    }
                }
            }
        } catch (e: Exception){
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    
    companion object{
        val DRAWABLE_LEFT = 0
        val DRAWABLE_TOP = 1
        val DRAWABLE_RIGHT = 2
        val DRAWABLE_BOTTOM = 3
    }
}