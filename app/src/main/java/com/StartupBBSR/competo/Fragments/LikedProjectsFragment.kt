package com.StartupBBSR.competo.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.StartupBBSR.competo.Adapters.ProjectAdapter
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.FragmentLikedProjectsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject


class LikedProjectsFragment : Fragment() {

    private lateinit var binding: FragmentLikedProjectsBinding
    private lateinit var navController: NavController

    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var constant: Constant
    private lateinit var likedProjectsRef: CollectionReference
    private lateinit var userID: String

    private var likedProjectsList: ArrayList<ProjectModel> = ArrayList()
    private lateinit var adapter: ProjectAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigateUp()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLikedProjectsBinding.inflate(inflater, container, false)

        firestoreDB = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        userID = firebaseAuth.uid.toString()
        constant = Constant()

        likedProjectsRef = firestoreDB.collection(constant.project).document(userID).collection(constant.likedProjects)

        loadProjects()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setListeners()
    }

    private fun setListeners() {
        binding.swipeToRefresh.setOnRefreshListener {
            loadProjects()
            binding.swipeToRefresh.isRefreshing = false
        }
    }


    private fun loadProjects() {
        isLoading(true)
        likedProjectsRef
            .orderBy(constant.projectTimeStamp, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                likedProjectsList.clear()
                if (it != null && !it.isEmpty) {
                    for (document in it.documents) {
                        val project = document.toObject<ProjectModel>()

                        if (project != null)
                            likedProjectsList.add(project)
                    }

                    if (likedProjectsList.size > 0) {
                        isLoading(false)
                        binding.tvLikedProjectStatus.visibility = View.GONE
                        binding.likedProjectsRecyclerView.visibility = View.VISIBLE

                        adapter = ProjectAdapter(likedProjectsList)
                        adapter.setOnProjectClickListener(object: ProjectAdapter.OnProjectItemClickListener{

                            override fun onLikeClick(project: ProjectModel, position: Int, isLiked: Boolean) {
//                                TODO("Not yet implemented")
                            }

                            override fun onMenuClick(project: ProjectModel, position: Int) {
                                openMenu(project)
                            }

                            override fun onContactMeClick(organizerID: String) {
//                                TODO("Not yet implemented")
                            }

                        })
                        binding.likedProjectsRecyclerView.adapter = adapter
                        binding.likedProjectsRecyclerView.setHasFixedSize(true)

                    } else {
                        isLoading(false)
                        binding.likedProjectsRecyclerView.visibility = View.GONE
                        binding.tvLikedProjectStatus.visibility = View.VISIBLE
                    }
                } else {
                    isLoading(false)
                    binding.likedProjectsRecyclerView.visibility = View.GONE
                    binding.tvLikedProjectStatus.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                isLoading(false)
                Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openMenu(project: ProjectModel) {
        val bottomDialog = BottomSheetDialog(requireContext(), R.style.bottom_dialog)
        bottomDialog.setContentView(R.layout.project_bottom_dialog)

        val tvEdit = bottomDialog.findViewById<TextView>(R.id.tvEdit)
        val tvDelete = bottomDialog.findViewById<TextView>(R.id.tvDelete)
        val tvReport = bottomDialog.findViewById<TextView>(R.id.tvReport)

        tvEdit?.visibility = View.GONE
        tvDelete?.visibility = View.GONE

        tvReport?.setOnClickListener {
            Toast.makeText(context, "report ${project.projectID}", Toast.LENGTH_SHORT).show()
            bottomDialog.dismiss()
        }

        bottomDialog.show()
    }

    private fun isLoading(loading: Boolean) {
        if (loading) {
            binding.projectProgressBar.visibility = View.VISIBLE
        } else {
            binding.projectProgressBar.visibility = View.GONE
        }
    }
}