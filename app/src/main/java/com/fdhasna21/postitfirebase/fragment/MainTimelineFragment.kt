package com.fdhasna21.postitfirebase.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdhasna21.postitfirebase.adapter.PostViewHolder
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.activity.MainActivity
import com.fdhasna21.postitfirebase.databinding.FragmentMainTimelineBinding
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.Post
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class MainTimelineFragment : Fragment() {
    private var _binding : FragmentMainTimelineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainTimelineBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.timeline)
        setHasOptionsMenu(true)

        setupFirebaseRecycler()
    }

    private fun setupFirebaseRecycler() {
        val reference = (requireActivity() as MainActivity).database.getReference("posts")
        //.orderByChild("time") tapi di convert ke long dulu
        // https://stackoverflow.com/questions/43584244/how-to-save-the-current-date-time-when-i-add-new-value-to-firebase-realtime-data
        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(reference, Post::class.java)
            .build()
        val firebaseAdapter = object : FirebaseRecyclerAdapter<Post, PostViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                return PostViewHolder(RowUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
                val postKey = getRef(position).key!!
                val postReference = (requireActivity() as MainActivity).database.getReference("posts").child(postKey)

                holder.setPost(requireContext(), model, (requireActivity() as MainActivity).currentUser?.uid, postReference)
            }
        }

        firebaseAdapter.startListening()
        binding.mainTimelineRecycler.adapter = firebaseAdapter
        binding.mainTimelineRecycler.layoutManager = LinearLayoutManager(requireActivity())
        binding.mainTimelineRecycler.addItemDecoration(object : DividerItemDecoration(requireActivity(), VERTICAL) {})
        binding.mainTimelineRecycler.setHasFixedSize(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main_timeline, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_notif -> {
                Toast.makeText(requireContext(), "notif", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}