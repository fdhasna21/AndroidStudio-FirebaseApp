package com.fdhasna21.postitfirebase.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.fdhasna21.postitfirebase.databinding.ActivitySignInBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private var auth : FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title=null
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
        val email = binding.signinEmail.text.toString()
        val password = binding.signinPassword.text.toString()
        isEditable = false

        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        binding.signinSubmit.revertAnimation()
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                    isEditable=true
                }
        } else {
            if (email.isBlank()) {
                binding.signinEmailLayout.error = "Email cannot be null or empty"
            }
            if (password.isBlank()) {
                binding.signinPasswordLayout.error = "Password cannot be null or empty"
            }
            isEditable=true
        }
    }

    fun gotoSignUp(view: android.view.View) = startActivity(Intent(this, SignUpActivity::class.java))

    private var isEditable : Boolean = false
        set(value){
            val editableEditText : ArrayList<TextInputEditText> = arrayListOf(
                binding.signinEmail,
                binding.signinPassword
            )
            binding.signinSignUp.isClickable = value
            editableEditText.forEach {
                it.isCursorVisible = value
                it.isFocusable = value
                it.isFocusableInTouchMode = value
            }
            if(!field){
                binding.signinSubmit.revertAnimation()
            }else{
                binding.signinEmailLayout.error = null
                binding.signinPasswordLayout.error = null
                binding.signinSubmit.startAnimation()
            }
            field = value
        }
}