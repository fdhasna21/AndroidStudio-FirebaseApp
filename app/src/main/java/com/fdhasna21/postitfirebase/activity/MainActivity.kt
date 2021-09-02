package com.fdhasna21.postitfirebase.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdhasna21.postitfirebase.PostViewHolder
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.databinding.ActivityMainBinding
import com.fdhasna21.postitfirebase.databinding.RowPostBinding
import com.fdhasna21.postitfirebase.dataclass.Post
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var bottomSheetProfile: BottomSheetBehavior<CardView>
    private lateinit var bottomSheetPost : BottomSheetBehavior<CardView>
    private var auth : FirebaseAuth = Firebase.auth
    private var database : FirebaseDatabase = Firebase.database
    private var firestore : FirebaseFirestore = Firebase.firestore
    private var currentUser : FirebaseUser? = auth.currentUser
    private var profile = Profile()
    private var post = Post()
    private var postType = "text"
    private var isExist : Boolean = false

    private fun getCurrentUserData(){
        auth.currentUser?.let {
            val reference = firestore.collection("users").document(it.uid)
            reference.get().addOnCompleteListener {
                it.result?.let {
                    if(it.exists()){
                        isExist = true
                        profile.name = it.getString("name")
                        profile.photoUrl = it.getString("photo_url")
                        profile.uid = it.getString("uid")
                        profile.bio = it.getString("bio")
                        profile.email = it.getString("email")
                    } else{
                        isExist = false
                    }
                }
            }
        }
    }

    private fun setupBottomSheetPost(){
        bottomSheetPost = BottomSheetBehavior.from(binding.mainBottomSheetPost.bottomSheetPostLayout).apply {
            peekHeight = 0
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false
            isDraggable = false
        }
    }

    private fun setupBottomSheetProfile(){
        bottomSheetProfile = BottomSheetBehavior.from(binding.mainBottomSheetMenu.bottomSheetLayout).apply {
            peekHeight = 0
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.mainBottomSheetMenu.bottomSheetMenu.itemIconTintList = null
        binding.mainBottomSheetMenu.bottomSheetMenu.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_profile -> {
                    if(isExist){
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("data", profile)
                        startActivity(intent)
                    } else{
                        startActivity(Intent(this, CreateProfileActivity::class.java))
                    }
                }
                R.id.menu_logout ->{
                    val alertDialog = AlertDialog.Builder(this@MainActivity).apply {
                        setTitle("Logout")
                        setMessage("Are you sure to logout?")
                        setPositiveButton("Logout"){ _,_ ->
                            auth.signOut()
                            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                        }
                        setNegativeButton("Cancel"){_,_ ->}
                    }
                    alertDialog.create()
                    alertDialog.show()
                }
            }
            true
        }
    }

    private fun setupFirebaseRecycler() {
        val reference = database.getReference("posts")
        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(reference, Post::class.java)
            .build()

        val firebaseAdapter = object : FirebaseRecyclerAdapter<Post, PostViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                return PostViewHolder(RowPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
                val postKey = getRef(position).key!!
                val postReference = database.getReference("posts").child(postKey)

                holder.setPost(this@MainActivity, model, currentUser?.uid, postReference)
            }
        }

        firebaseAdapter.startListening()
        binding.mainPostRecycler.adapter = firebaseAdapter
        binding.mainPostRecycler.layoutManager = LinearLayoutManager(this)
        binding.mainPostRecycler.addItemDecoration(object : DividerItemDecoration(this@MainActivity, VERTICAL) {})
        binding.mainPostRecycler.setHasFixedSize(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCurrentUserData()
        setupBottomSheetProfile()
        setupBottomSheetPost()
        setupFirebaseRecycler()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main -> {
                bottomSheetProfile.state = if(bottomSheetProfile.state == BottomSheetBehavior.STATE_COLLAPSED){
                    BottomSheetBehavior.STATE_EXPANDED
                } else{
                    BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null){
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            if (bottomSheetProfile.state == BottomSheetBehavior.STATE_EXPANDED) {
                val outRect = Rect()
                binding.mainBottomSheetMenu.bottomSheetLayout.getGlobalVisibleRect(outRect)
                if (!outRect.contains(
                        ev.rawX.toInt(),
                        ev.rawY.toInt()
                    )
                ) bottomSheetProfile.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun disableBottomSheetPost(){
        binding.mainBottomSheetPost.bottomSheetPostClose.isEnabled = true
        binding.mainBottomSheetPost.bottomSheetPostSubmit.revertAnimation()
    }

    fun openNewPost(view: android.view.View) {
        if(binding.mainBottomSheetPost.bottomSheetPostContent.text.toString().isNotBlank()){
            binding.mainBottomSheetPost.bottomSheetPostClose.isEnabled = false
            binding.mainBottomSheetPost.bottomSheetPostSubmit.startAnimation()
            currentUser?.let {
                val formatter = SimpleDateFormat("dd-MMMM-yyyy hh:mm:ss")
                val time = formatter.format(Calendar.getInstance().time)
                post.content = binding.mainBottomSheetPost.bottomSheetPostContent.text.toString()
                post.type = postType
                post.time = time
                post.postUrl = ""
                post.userUid = it.uid
                post.userEmail = it.email
                post.userName = profile.name
                post.userPhotoUrl = profile.photoUrl

                val reference = database.getReference("posts")
                val postId = reference.push().key!!
                reference.child(postId).setValue(post).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show()
                        disableBottomSheetPost()
                        binding.mainBottomSheetPost.bottomSheetPostContent.setText("")
                        bottomSheetPost.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        binding.mainBottomSheetPost.bottomSheetPostClose.isEnabled = true
                    }
                }
            }
        }
        else{
            disableBottomSheetPost()
        }

    }

    fun closeNewPost(view: android.view.View) {
        if(binding.mainBottomSheetPost.bottomSheetPostContent.text.toString().isNotBlank()){
            val alertDialog = AlertDialog.Builder(this@MainActivity).apply {
                setTitle("Discard change")
                setMessage("Are you sure want to close? Your change will be discard")
                setPositiveButton("Discard"){ _,_ ->
                    bottomSheetPost.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.mainBottomSheetPost.bottomSheetPostContent.setText("")
                }
                setNegativeButton("Cancel"){_,_ ->}
            }
            alertDialog.create()
            alertDialog.show()
        }
        else{
            bottomSheetPost.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    fun openBottomSheetPost(view: android.view.View) {
        if(binding.mainBottomSheetPost.bottomSheetPostContent.text.toString().isBlank() && bottomSheetPost.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetPost.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun attachFile(view: android.view.View){
        Toast.makeText(this, "click!", Toast.LENGTH_SHORT).show()
    }
}