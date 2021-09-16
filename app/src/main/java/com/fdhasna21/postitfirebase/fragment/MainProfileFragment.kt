package com.fdhasna21.postitfirebase.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.activity.CreateProfileActivity
import com.fdhasna21.postitfirebase.activity.MainActivity
import com.fdhasna21.postitfirebase.activity.SignInActivity
import com.fdhasna21.postitfirebase.databinding.FragmentMainProfileBinding
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.android.material.textfield.TextInputEditText
import java.lang.Exception

class MainProfileFragment : Fragment(), View.OnClickListener {
    private var _binding : FragmentMainProfileBinding? = null
    private val binding get() = _binding!!
    private var imageUri : Uri? = null
    private lateinit var profile : Profile
    private var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.profile)
        setHasOptionsMenu(true)

        if((requireActivity() as MainActivity).isExist){
            profile = (requireActivity() as MainActivity).profile
            isEditable = false
            binding.profileName.setText(profile.name)
            binding.profileBio.setText(profile.bio)
            binding.profileEmail.setText(profile.email)
            imageUri = profile.url!!.toUri()
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.profileImage)
            setupListener()
            imageUri = null

            uid = (requireActivity() as MainActivity).currentUser!!.uid
        } else{
            startActivity(Intent(requireContext(), CreateProfileActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main_profile, menu)
        menu.findItem(R.id.menu_edit)?.isVisible = !isEditable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> {
                isEditable = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.profileSignout -> {
                val alertDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Logout")
                    setMessage("Are you sure to logout?")
                    setPositiveButton("Logout"){ _,_ ->
                        (requireActivity() as MainActivity).auth.signOut()
                        startActivity(Intent(requireContext(), SignInActivity::class.java))
                        requireActivity().finish()
                    }
                    setNegativeButton("Cancel"){_,_ ->}
                }
                alertDialog.create()
                alertDialog.show()
            }
            binding.profileImage -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startForResult.launch(intent)
            }
            binding.profileEditSubmit -> {
                profile.name = binding.profileName.text.toString()
                profile.bio = binding.profileBio.text.toString()

                binding.profileEditSubmit.startAnimation()
                if(imageUri != null){
                    val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType((requireActivity() as MainActivity).contentResolver.getType(imageUri!!))
                    val reference = (requireActivity() as MainActivity).storage.getReference("photo_url").child(System.currentTimeMillis().toString() + ".${fileExtension}")
                    val uploadTask = reference.putFile(imageUri!!)
                    uploadTask.continueWithTask {
                        if(!it.isSuccessful){
                            throw it.exception!!.cause!!
                        }
                        reference.downloadUrl
                    }.addOnCompleteListener {
                        if(it.isSuccessful) {
                            it.result?.let{
                                profile.url = it.toString()
                            }
                        }
                    }
                }

                val profileMap = HashMap<String,String>()
                profileMap["name"] = profile.name!!
                profileMap["bio"] = profile.bio!!
                profileMap["email"] = profile.email!!
                profileMap["url"] = profile.url!!
                profileMap["uid"] = uid!!

                uid?.let {
                    (requireActivity() as MainActivity).database.getReference("users").child(it).setValue(profile)
                    (requireActivity() as MainActivity).firestore.collection("users").document(it).set(profileMap).addOnCompleteListener {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                            binding.profileEditSubmit.revertAnimation()
                            isEditable = false }, 2000)
                    }
                }
            }
        }
    }

    private fun setupListener() {
        binding.profileSignout.setOnClickListener(this)
        binding.profileImage.setOnClickListener(this)
        binding.profileEditSubmit.setOnClickListener(this)
    }

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
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private var isEditable : Boolean = false
        set(value) {
            requireActivity().invalidateOptionsMenu()
            val editableEditText : ArrayList<TextInputEditText> = arrayListOf(
                binding.profileName,
                binding.profileBio
            )
            binding.profileImage.isEnabled = value
            editableEditText.forEach {
                it.isCursorVisible = value
                it.isFocusable = value
                it.isFocusableInTouchMode = value
            }

            (requireActivity() as MainActivity).bottomNavigation.visibility = if(!value){
                View.VISIBLE
            } else{
                View.GONE
            }

            binding.profileEditSubmit.visibility = if(value){
                View.VISIBLE
            } else{
                View.GONE
            }
            binding.profileSignout.visibility = if(!value){
                View.VISIBLE
            } else{
                View.GONE
            }

            requireActivity().title = getString(
                if(value){
                    R.string.edit_profile
                }else{
                    R.string.profile
                })

            (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(value)
            (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(!value)
            field = value
        }
}