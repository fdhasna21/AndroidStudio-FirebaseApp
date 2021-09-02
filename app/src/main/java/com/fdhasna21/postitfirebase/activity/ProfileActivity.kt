package com.fdhasna21.postitfirebase.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.databinding.ActivityProfileBinding
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
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
import com.google.firebase.storage.ktx.storage
import java.lang.Exception

//TODO : SEMUANYA HARUS DIISI BARU BISA, FOTO DARI URL GAK KE LOAD!
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileBinding
    private var auth : FirebaseAuth = Firebase.auth
    private var database : FirebaseDatabase = Firebase.database
    private var firestore : FirebaseFirestore = Firebase.firestore
    private var storage : FirebaseStorage = Firebase.storage
    private var currentUser : FirebaseUser? = auth.currentUser

    private var uid : String? = currentUser?.uid
    private var imageUri : Uri?=null
    private lateinit var profile : Profile

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        try{
            if(it?.resultCode == Activity.RESULT_OK){
                it.data?.let {
                    imageUri = it.data
                    Glide.with(this)
                        .load(imageUri)
                        .into(binding.profileImage)
                }
            }
        } catch (e: Exception){
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        isEditable = false

        profile = intent.getParcelableExtra<Profile>("data")!!
        binding.profileName.setText(profile.name)
        binding.profileBio.setText(profile.bio)
        binding.profileEmail.setText(profile.email)

        val reference = storage.getReference("users").child(currentUser!!.uid)
        reference.downloadUrl.addOnCompleteListener {
            OnCompleteListener<Uri> {
                if(it.isSuccessful){
                    imageUri = it.result
                    Glide.with(this).load(imageUri).into(binding.profileImage)
                } else{
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if(isEditable){
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Discard change")
                setMessage("Do you want to discard your change?")
                setPositiveButton("Discard"){ _,_ ->
                    onBackPressed()
                }
                setNegativeButton("Cancel"){_,_ ->}
            }
            alertDialog.create()
            alertDialog.show()
        }else{
            onBackPressed()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        menu?.findItem(R.id.menu_edit)?.isVisible = !isEditable
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> {
                isEditable = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun submitEdit(view: android.view.View) {
        val name = binding.profileName.text.toString()
        val bio = binding.profileBio.text.toString()
        val email = binding.profileEmail.text.toString()

        isEditable = false
        binding.editSubmit.startAnimation()

        imageUri?.let {
            val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(it))
            val reference = storage.getReference("profile_images").child(System.currentTimeMillis().toString() + ".${fileExtension}")
            val uploadTask = reference.putFile(it)
            uploadTask.continueWith {
                if(!it.isSuccessful){
                    throw it.exception!!.cause!!
                }
                reference.downloadUrl
            }.addOnCompleteListener {
                if(it.isSuccessful){
                    it.result?.let{
                        profile.name = name
                        profile.photoUrl = it.toString()
                        profile.uid = uid!!

                        val profileMap = HashMap<String,String>()
                        profileMap["name"] = name
                        profileMap["bio"] = bio
                        profileMap["email"] = email
                        profileMap["photo_url"] = profile.photoUrl!!
                        profileMap["uid"] = profile.uid!!

                        uid?.let {
                            database.getReference("users").child(it).setValue(profile)
                            firestore.collection("users").document(it).set(profileMap).addOnCompleteListener {
                                binding.editSubmit.revertAnimation()
                                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                                isEditable = false
                            }
                        }
                    }
                }
            }
        }
    }

    fun getImage(view: android.view.View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(intent)
    }

    private var isEditable : Boolean = false
        set(value) {
            invalidateOptionsMenu()
            val editableEditText : ArrayList<TextInputEditText> = arrayListOf(
                binding.profileName,
                binding.profileBio
            )
            binding.profileImage.isClickable = value
            binding.editSubmit.visibility = if(value){
                View.VISIBLE
            } else{
                View.INVISIBLE
            }
            editableEditText.forEach {
                it.isCursorVisible = value
                it.isFocusable = value
                it.isFocusableInTouchMode = value
            }
            supportActionBar?.setHomeButtonEnabled(value)
            field = value
        }
}