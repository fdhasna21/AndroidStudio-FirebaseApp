package com.fdhasna21.postitfirebase.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.activity.MainActivity
import com.fdhasna21.postitfirebase.activity.SearchActivity
import com.fdhasna21.postitfirebase.adapter.MessagesViewHolder
import com.fdhasna21.postitfirebase.databinding.FragmentMainMessagesBinding
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.ChatMessage
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
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
import com.google.firebase.storage.ktx.storage

class MainMessagesFragment : Fragment(), View.OnClickListener {
    private var _binding : FragmentMainMessagesBinding? = null
    private val binding get() = _binding!!
    private var messages : MutableList<ChatMessage> = arrayListOf()
    private var users : MutableList<String> = arrayListOf()
    private var auth : FirebaseAuth = Firebase.auth
    private var database : FirebaseDatabase = Firebase.database
    private var firestore : FirebaseFirestore = Firebase.firestore
    private var storage : FirebaseStorage = Firebase.storage
    private var currentUser : FirebaseUser? = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMessagesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.messages)
        setHasOptionsMenu(true)

        setupFirebaseRecycler()
        binding.apply{
            mainMessageSearch.setOnClickListener(this@MainMessagesFragment)
        }
    }

    private fun setupFirebaseRecycler() {
        val reference = database.getReference("messages")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                users.clear()
                for (data:DataSnapshot in snapshot.children){
                    val message: ChatMessage? = data.getValue(ChatMessage::class.java)
                    message?.let {
                        when {
                            it.senderUID == currentUser?.uid -> {
                                users.add(message.receiverUID!!)
                                messages.add(it)
                            }
                            it.receiverUID == currentUser?.uid -> {
                                users.add(message.senderUID!!)
                                messages.add(it)
                            }
                            else -> {

                            }
                        }
                    }
                }

                users = users.distinct().reversed().toMutableList()
                messages = messages.reversed() as MutableList<ChatMessage>

                val firebaseAdapter = object : RecyclerView.Adapter<MessagesViewHolder>(){
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
                        return MessagesViewHolder(RowUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                    }

                    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
                        val message = messages.filter { it.senderUID == users[position] || it.receiverUID == users[position] }
                        Log.i("mainActivity", "${users[position]} $message")
                        database.getReference("users").child(users[position]).addValueEventListener(
                            object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    holder.setChat(requireContext(),
                                        message = message[0],
                                        target = snapshot.getValue(Profile::class.java)
                                    )
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.i("mainActivity", "messagesOnCancelled: userReference $error")
                                }

                            }
                        )
                    }

                    override fun getItemCount(): Int {
                        return users.size
                    }

                }

                firebaseAdapter.notifyDataSetChanged()
                binding.mainMessageRecycler.adapter = firebaseAdapter
                binding.mainMessageRecycler.layoutManager = LinearLayoutManager(requireActivity())
                binding.mainMessageRecycler.addItemDecoration(object : DividerItemDecoration(requireActivity(), VERTICAL) {})
                binding.mainMessageRecycler.setHasFixedSize(true)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("mainActivity", "messagesOnCancelled: $error")
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main_messages, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sortby -> {
                Toast.makeText(requireContext(), "filter", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.mainMessageSearch -> {
                requireContext().startActivity(Intent(requireContext(), SearchActivity::class.java))
            }
        }
    }
}