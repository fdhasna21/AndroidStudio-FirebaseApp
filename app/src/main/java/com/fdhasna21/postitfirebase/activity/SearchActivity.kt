package com.fdhasna21.postitfirebase.activity

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fdhasna21.postitfirebase.R
import com.fdhasna21.postitfirebase.adapter.UserViewHolder
import com.fdhasna21.postitfirebase.databinding.ActivitySearchBinding
import com.fdhasna21.postitfirebase.databinding.RowUserBinding
import com.fdhasna21.postitfirebase.dataclass.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding : ActivitySearchBinding
    private lateinit var searchView: SearchView
    var auth : FirebaseAuth = Firebase.auth
    var database : FirebaseDatabase = Firebase.database
    var currentUser : FirebaseUser? = auth.currentUser
    private var exceptCurrent : ArrayList<Profile> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.search)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        setupFirebaseRecycler()
    }

    private fun setupFirebaseRecycler() {
        val reference = database.getReference("users")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                exceptCurrent.clear()
                for(data : DataSnapshot in snapshot.children){
                    val user = data.getValue(Profile::class.java)
                    if(!user!!.uid.equals(currentUser!!.uid)){
                        exceptCurrent.add(user)
                    }
                }
                val firebaseAdapter = object : RecyclerView.Adapter<UserViewHolder>(){
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                        return UserViewHolder(RowUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                    }

                    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
                        val item = exceptCurrent[position]
//                val messageKey = getRef(position).key!!
//                val messageReference = database.getReference("users").child(messageKey)
                        holder.setUser(this@SearchActivity, item, currentUser?.uid)
                    }

                    override fun getItemCount(): Int {
                        return exceptCurrent.size
                    }

                }

                firebaseAdapter.notifyDataSetChanged()
                binding.searchRecycler.adapter = firebaseAdapter
                binding.searchRecycler.layoutManager = LinearLayoutManager(this@SearchActivity)
                binding.searchRecycler.addItemDecoration(object : DividerItemDecoration(this@SearchActivity, VERTICAL) {})
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("searchActivity", "onCancelled: $error")
            }

        })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.menu_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = searchItem?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(true)
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }
}