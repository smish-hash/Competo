package com.StartupBBSR.competo.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.StartupBBSR.competo.Adapters.ProjectAdapter
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.FragmentProjectMainBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class ProjectMainFragment : Fragment() {

    private lateinit var binding: FragmentProjectMainBinding
    private lateinit var navController: NavController

    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var constant: Constant
    private lateinit var projectRef: CollectionReference

    private var projectList: ArrayList<ProjectModel> = ArrayList()
    private lateinit var adapter: ProjectAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProjectMainBinding.inflate(inflater, container, false)

        firestoreDB = FirebaseFirestore.getInstance()
        constant = Constant()
        projectRef = firestoreDB.collection(constant.project)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setListeners()

        loadProjects()
    }

    private fun loadProjects() {
        isLoading(true)
        projectRef.get().addOnCompleteListener {
        projectList = ArrayList()

            for (documents in it.result) {
                documents.reference.collection(constant.createdProjects)
                    .get().addOnSuccessListener { result ->
                        isLoading(false)
                        for (document in result) {
                            Log.d("ids", "loadProjects: ${document.id} => ${document[constant.projectTitle]}")
                            val project = document.toObject<ProjectModel>()
                            projectList.add(project)
                        }
                    }
                    .addOnCompleteListener {
                        Log.d("adapter", "loadProjects: ${projectList.size}")
                        if (projectList.size > 0) {

                            projectList.sortBy { constant.projectTimeStamp }

                            adapter = ProjectAdapter(projectList)

                            adapter.setOnProjectClickListener(object : ProjectAdapter.OnProjectItemClickListener{
                                override fun onLikeClick(project: ProjectModel, position: Int) {
                                    // TODO: 1/17/2022 implement like count
                                }
                            })

                            binding.projectRecyclerView.adapter = adapter
                            binding.projectRecyclerView.setHasFixedSize(true)
                            binding.projectRecyclerView.visibility = View.VISIBLE
                        }
                    }
            }

            isLoading(false)
        }
    }

    private fun setListeners() {
        binding.btnAddProject.setOnClickListener {
            navController.navigate(R.id.action_projectMainFragment_to_addProjectFragment)
        }
    }

    private fun isLoading(loading: Boolean) {
        if (loading) {
            binding.projectProgressBar.visibility = View.VISIBLE
        } else {
            binding.projectProgressBar.visibility = View.GONE
        }
    }
}