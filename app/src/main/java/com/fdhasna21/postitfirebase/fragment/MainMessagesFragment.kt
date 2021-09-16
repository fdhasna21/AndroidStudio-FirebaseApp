package com.fdhasna21.postitfirebase.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.activity.MainActivity
import com.fdhasna21.postitfirebase.activity.SearchActivity
import com.fdhasna21.postitfirebase.adapter.ChatMessageViewHolder
import com.fdhasna21.postitfirebase.databinding.FragmentMainMessagesBinding
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.ChatMessage
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class MainMessagesFragment : Fragment(), View.OnClickListener {
    private var _binding : FragmentMainMessagesBinding? = null
    private val binding get() = _binding!!

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
        val reference = (requireActivity() as MainActivity).database.getReference("messages")
        //.orderByChild("time") tapi di convert ke long dulu
        // https://stackoverflow.com/questions/43584244/how-to-save-the-current-date-time-when-i-add-new-value-to-firebase-realtime-data

        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(reference, ChatMessage::class.java)
            .build()
        val firebaseAdapter = object : FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
                return ChatMessageViewHolder(RowUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int, model: ChatMessage) {
                val messageKey = getRef(position).key!!
                val messageReference = (requireActivity() as MainActivity).database.getReference("messages").child(messageKey)
                holder.setChat(requireContext(), model, (requireActivity() as MainActivity).currentUser?.uid, messageReference)
            }
        }

        firebaseAdapter.startListening()
        binding.mainMessageRecycler.adapter = firebaseAdapter
        binding.mainMessageRecycler.layoutManager = LinearLayoutManager(requireActivity())
        binding.mainMessageRecycler.addItemDecoration(object : DividerItemDecoration(requireActivity(), VERTICAL) {})
        binding.mainMessageRecycler.setHasFixedSize(true)
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