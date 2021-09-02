package com.fdhasna21.postitfirebase.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title=null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        isEditable = true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
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
    
    fun gotoMain(view: android.view.View) {
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPassword.text.toString()
        val passwordConfirmation = binding.signupPasswordConfirmation.text.toString()
        isEditable=false

        if(email.isNotBlank() && password.isNotBlank() && passwordConfirmation.isNotBlank()){
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val intent = Intent(this, CreateProfileActivity::class.java)
                        intent.putExtra("tag", "signupter")
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                    isEditable=true
                }
        }
        else {
            if(email.isBlank()){
                binding.signupEmailLayout.error = "Email cannot be null or empty"
            }
            if(password.isBlank()){
                binding.signupPasswordLayout.error = "Password cannot be null or empty"
            }
            if(password != passwordConfirmation){
                binding.signupPasswordConfirmationLayout.error = "Password and Password Confirmation not match"
            }
            isEditable=true
        }
    }
    
    fun gotoSignIn(view: android.view.View) = startActivity(Intent(this, SignInActivity::class.java))

    private var isEditable : Boolean = false
        set(value) {
            val editableEditText : ArrayList<TextInputEditText> = arrayListOf(
                binding.signupEmail,
                binding.signupPassword,
                binding.signupPasswordConfirmation
            )
            binding.signupSignIn.isClickable = value
            editableEditText.forEach {
                it.isCursorVisible = value
                it.isFocusable = value
                it.isFocusableInTouchMode = value
            }
            supportActionBar?.setHomeButtonEnabled(value)
            if(!field){
                binding.signupSubmit.revertAnimation()
            }else{
                binding.signupEmailLayout.error = null
                binding.signupPasswordLayout.error = null
                binding.signupPasswordConfirmation.error = null
                binding.signupSubmit.startAnimation()
            }
            field = value
        }
}