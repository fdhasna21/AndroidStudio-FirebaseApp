package com.fdhasna21.postitfirebase.activity

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.databinding.ActivityCreateProfileBinding
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.fdhasna21.postitfirebase.ResourceUtils
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.lang.Exception

class CreateProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreateProfileBinding
    private var auth : FirebaseAuth = Firebase.auth
    private var database : FirebaseDatabase = Firebase.database
    private var firestore : FirebaseFirestore = Firebase.firestore
    private var storage : FirebaseStorage = Firebase.storage
    private var currentUser : FirebaseUser? = auth.currentUser

    private var uid : String? = currentUser?.uid
    private var profile = Profile()
    private var imageUri : Uri? = null
    private var email :String = currentUser!!.email.toString()

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
        try{
            if(data?.resultCode == Activity.RESULT_OK){
                data.data?.let {
                    imageUri = it.data
                    Glide.with(this)
                        .load(imageUri!!)
                        .into(binding.createImage)
                }
            }
        } catch (e: Exception){
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Create Account"

        Glide.with(this)
            .load(R.drawable.profile_pict)
            .circleCrop()
            .into(binding.createImage)

        binding.createEmail.setText(email)
        imageUri = ResourceUtils().getUriToDrawable(this, R.drawable.profile_pict)
        isEditable = true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun getImage(view: android.view.View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(intent)
    }

    fun gotoMain(view: android.view.View) {
        val name = binding.createName.text.toString()
        val bio = binding.createBio.text.toString()
        isEditable = false

        if(name.isNotEmpty()){
            profile.name = name
            profile.uid = uid!!
            profile.bio = bio
            if(imageUri == null){
                profile.url = ""
                sendData()
            } else{
                val mimeTypeMap = MimeTypeMap.getSingleton()
                val fileExtension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri!!))
                val storageReference : StorageReference = storage.getReference("profile_images").child(System.currentTimeMillis().toString() + ".${fileExtension}")
                val uploadTask = storageReference.putFile(imageUri!!)
                uploadTask.continueWithTask {
                    if(!it.isSuccessful){
                        throw it.exception!!.cause!!
                    }
                    storageReference.downloadUrl
                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.let {
                            profile.url = it.toString()
                            sendData()
                        }
                    }
                }
            }
        } else {
            binding.createNameLayout.error = "Name cannot be null or empty"
            isEditable = true
        }

    }

    private fun sendData(){
        val profileMap = HashMap<String,String>()
        profileMap["name"] = profile.name!!
        profileMap["bio"] = profile.bio!!
        profileMap["email"] = email
        profileMap["url"] = profile.url!!
        profileMap["uid"] = profile.uid!!

        uid?.let {
            database.getReference("users").child(it).setValue(profile)
            firestore.collection("users").document(it).set(profileMap).addOnCompleteListener {
                Toast.makeText(this, "Profile created", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish() }, 1000)
            }
        }
    }

    private var isEditable : Boolean = false
        set(value) {
            val editableEditText : ArrayList<TextInputEditText> = arrayListOf(
                binding.createBio,
                binding.createName
            )
            binding.createImage.isClickable = value
            editableEditText.forEach {
                it.isCursorVisible = value
                it.isFocusable = value
                it.isFocusableInTouchMode = value
            }
            supportActionBar?.setHomeButtonEnabled(value)
            if(!field){
                binding.createSubmit.revertAnimation()
            }else{
                binding.createBioLayout.error = null
                binding.createNameLayout.error = null
                binding.createSubmit.startAnimation()
            }
            field = value
        }
}