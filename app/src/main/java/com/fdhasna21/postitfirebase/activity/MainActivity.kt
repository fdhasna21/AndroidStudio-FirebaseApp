package com.fdhasna21.postitfirebase.activity

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.databinding.ActivityMainBinding
import com.fdhasna21.postitfirebase.dataclass.Post
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    lateinit var bottomNavigation : BottomNavigationView
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navController : NavController
    private lateinit var bottomSheetProfile: BottomSheetBehavior<CardView>
    private lateinit var bottomSheetPost : BottomSheetBehavior<CardView>
    var auth : FirebaseAuth = Firebase.auth
    var database : FirebaseDatabase = Firebase.database
    var firestore : FirebaseFirestore = Firebase.firestore
    var storage : FirebaseStorage = Firebase.storage
    var currentUser : FirebaseUser? = auth.currentUser
    private var post = Post()
    private var postType = ""
    private var postSelectedUri : Uri? = null
    var isExist : Boolean = false
    var profile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainFragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        binding.mainBottomNavigation.setupWithNavController(navController)
        bottomNavigation = binding.mainBottomNavigation

        getCurrentUserData()
//        setupBottomSheetPost()
//        setupFirebaseRecycler()
    }

    private fun getCurrentUserData(){
        currentUser?.let {
            val reference = firestore.collection("users").document(it.uid)
            reference.get().addOnCompleteListener {
                it.result?.let {
                    if(it.exists()){
                        isExist = true
                        profile.name = it.getString("name")
                        profile.url = it.getString("url")
                        profile.uid = it.getString("uid")
                        profile.bio = it.getString("bio")
                        profile.email = it.getString("email")
                        //todo : selesai loading
                    } else{
                        isExist = false
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if(navController.currentDestination?.id == R.id.mainProfileFragment){//(supportFragmentManager.findFragmentById(R.id.mainProfileFragment) as Fragment)?.isVisible == true){
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Discard change")
                setMessage("Do you want to discard your change?")
                setPositiveButton("Discard"){ _,_ ->
                    navController.navigate(R.id.action_mainProfileFragment_self)
                }
                setNegativeButton("Cancel"){_,_ ->}
            }
            alertDialog.create()
            alertDialog.show()
        }
        return true
    }


//    private fun setupBottomSheetPost(){
//        bottomSheetPost = BottomSheetBehavior.from(binding.mainBottomSheetPost.bottomSheetPostLayout).apply {
//            peekHeight = 0
//            state = BottomSheetBehavior.STATE_COLLAPSED
//            isHideable = false
//            isDraggable = false
//        }
//    }

//
//    private fun setupFirebaseRecycler() {
//        val reference = database.getReference("posts")
//        val options = FirebaseRecyclerOptions.Builder<Post>()
//            .setQuery(reference, Post::class.java)
//            .build()
//
//        val firebaseAdapter = object : FirebaseRecyclerAdapter<Post, PostViewHolder>(options){
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//                return PostViewHolder(RowPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//            }
//
//            override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
//                val postKey = getRef(position).key!!
//                val postReference = database.getReference("posts").child(postKey)
//
//                holder.setPost(this@MainActivity, model, currentUser?.uid, postReference)
//            }
//        }
//
//        firebaseAdapter.startListening()
//        binding.mainPostRecycler.adapter = firebaseAdapter
//        binding.mainPostRecycler.layoutManager = LinearLayoutManager(this)
//        binding.mainPostRecycler.addItemDecoration(object : DividerItemDecoration(this@MainActivity, VERTICAL) {})
//        binding.mainPostRecycler.setHasFixedSize(true)
//    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.menu_main -> {
//                bottomSheetProfile.state = if(bottomSheetProfile.state == BottomSheetBehavior.STATE_COLLAPSED){
//                    BottomSheetBehavior.STATE_EXPANDED
//                } else{
//                    BottomSheetBehavior.STATE_COLLAPSED
//                }
//            }
//            R.id.menu_message -> {
//                startActivity(Intent(this, MessagesActivity::class.java))
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null){
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

//
//    fun openNewPost(view: android.view.View) {
//        if(binding.mainBottomSheetPost.bottomSheetPostContent.text.toString().isNotBlank()){
//            binding.mainBottomSheetPost.bottomSheetPostClose.isEnabled = false
//            binding.mainBottomSheetPost.bottomSheetPostSubmit.startAnimation()
//            currentUser?.let {
//                val formatter = SimpleDateFormat("dd-MMMM-yyyy hh:mm:ss")
//                val time = formatter.format(Calendar.getInstance().time)
//                post.content = binding.mainBottomSheetPost.bottomSheetPostContent.text.toString()
//                post.time = time
//                post.userUid = it.uid
//                post.userEmail = it.email
//                post.userName = profile.name
//                post.userPhotoUrl = profile.url
//                post.type = postType
//
//                if(postSelectedUri == null){
//                    post.postUrl = ""
//                    post.type = "text"
//                    sendDataPost()
//                }
//                else {
//                    val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(postSelectedUri!!))
//                    val storageReference : StorageReference = storage.getReference("postUrl").child(System.currentTimeMillis().toString() + ".${fileExtension}")
//                    val uploadTask = storageReference.putFile(postSelectedUri!!)
//                    uploadTask.continueWithTask {
//                        if(!it.isSuccessful){
//                            throw it.exception!!.cause!!
//                        }
//                        storageReference.downloadUrl
//                    }.addOnCompleteListener {
//                        if (it.isSuccessful) {
//                            it.result?.let {
//                                post.postUrl = it.toString()
//                                sendDataPost()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        else{
//            disableBottomSheetPost()
//        }
//    }
//
//    fun closeNewPost(view: android.view.View) {
//        if(binding.mainBottomSheetPost.bottomSheetPostContent.text.toString().isNotBlank()){
//            val alertDialog = AlertDialog.Builder(this@MainActivity).apply {
//                setTitle("Discard change")
//                setMessage("Are you sure want to close? Your change will be discard")
//                setPositiveButton("Discard"){ _,_ ->
//                    bottomSheetPost.state = BottomSheetBehavior.STATE_COLLAPSED
//                    binding.mainBottomSheetPost.bottomSheetPostContent.setText("")
//                }
//                setNegativeButton("Cancel"){_,_ ->}
//            }
//            alertDialog.create()
//            alertDialog.show()
//        }
//        else{
//            bottomSheetPost.state = BottomSheetBehavior.STATE_COLLAPSED
//        }
//    }
//
//    fun openBottomSheetPost(view: android.view.View) {
//        if(binding.mainBottomSheetPost.bottomSheetPostContent.text.toString().isBlank() && bottomSheetPost.state == BottomSheetBehavior.STATE_COLLAPSED) {
//            bottomSheetPost.state = BottomSheetBehavior.STATE_EXPANDED
//        }
//    }
//
//    fun attachFile(view: android.view.View){
//        val intent = Intent()
//        intent.type = "image/* video/* audio/*"
//        intent.putExtra(
//            Intent.EXTRA_MIME_TYPES,
//            arrayOf("image/*", "video/*", "audio/*")
//        )
//        intent.action = Intent.ACTION_GET_CONTENT
//        startForResult.launch(intent)
//    }
//
//    fun deleteAttachFile(view: android.view.View) {
//        postSelectedUri = null
//        postType = "text"
//        binding.mainBottomSheetPost.bottomSheetPostPlayerView.visibility = View.GONE
//        binding.mainBottomSheetPost.bottomSheetPostImageView.visibility = View.GONE
//        view.visibility = View.GONE
//    }
//
//    private fun sendDataPost(){
//        val reference = database.getReference("posts")
//        val postId = reference.push().key!!
//        reference.child(postId).setValue(post).addOnCompleteListener {
//            if(it.isSuccessful){
//                Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show()
//                disableBottomSheetPost()
//                binding.mainBottomSheetPost.bottomSheetPostContent.setText("")
//                bottomSheetPost.state = BottomSheetBehavior.STATE_COLLAPSED
//            } else{
//                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
//                binding.mainBottomSheetPost.bottomSheetPostClose.isEnabled = true
//            }
//        }
//    }
//
//    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
//        try{
//            if(data?.resultCode == Activity.RESULT_OK){
//                data.data?.let {
//                    postSelectedUri = it.data
//                    val fileType = contentResolver.getType(postSelectedUri!!)
//                    if(fileType!!.contains("image")) {
//                        binding.mainBottomSheetPost.bottomSheetPostPlayerView.visibility = View.GONE
//                        binding.mainBottomSheetPost.bottomSheetPostImageView.visibility = View.VISIBLE
//                        binding.mainBottomSheetPost.bottomSheetPostAttachCancel.visibility = View.VISIBLE
//
//                        Glide.with(this).load(postSelectedUri!!).into(binding.mainBottomSheetPost.bottomSheetPostImageView)
//                        postType = "image"
//                    }
//                    else if(fileType.contains("video") || fileType.contains("audio"))
//                    {
//                        binding.mainBottomSheetPost.bottomSheetPostPlayerView.visibility = View.VISIBLE
//                        binding.mainBottomSheetPost.bottomSheetPostImageView.visibility = View.GONE
//                        binding.mainBottomSheetPost.bottomSheetPostAttachCancel.visibility = View.VISIBLE
//
//                        val exoPlayer = SimpleExoPlayer.Builder(this).build()
//                        val dataSourceFactory = DefaultDataSourceFactory(this)
//                        val mediaItem = MediaItem.fromUri(postSelectedUri!!)
//                        val mediaSource = ProgressiveMediaSource
//                            .Factory(dataSourceFactory)
//                            .createMediaSource(mediaItem)
//
//                        exoPlayer.setMediaSource(mediaSource)
//                            exoPlayer.prepare()
//                        exoPlayer.playWhenReady = true
//                        postType =
//                            if(fileType.contains("video")) {
//                            "video"
//                            } else {
//                                "audio"
//                            }
//                    }
//                }
//            }
//        } catch (e: Exception){
//            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
//        }
//    }
}