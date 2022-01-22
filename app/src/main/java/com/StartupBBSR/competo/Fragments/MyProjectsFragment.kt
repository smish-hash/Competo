package com.StartupBBSR.competo.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.StartupBBSR.competo.Adapters.ProjectAdapter
import com.StartupBBSR.competo.Fragments.MyProjectsFragmentDirections.ActionMyProjectsFragmentToAddProjectFragment
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.FragmentMyProjectsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject


class MyProjectsFragment : Fragment() {

    private lateinit var binding: FragmentMyProjectsBinding
    private lateinit var navController: NavController

    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var constant: Constant
    private lateinit var myProjectRef: CollectionReference
    private lateinit var userID: String

    private var myProjectList: ArrayList<ProjectModel> = ArrayList()
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
        // Inflate the layout for this fragment
        binding = FragmentMyProjectsBinding.inflate(inflater, container, false)

        firestoreDB = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        userID = firebaseAuth.uid.toString()
        constant = Constant()
        myProjectRef = firestoreDB.collection(constant.project).document(userID).collection(constant.createdProjects)

        loadMyProjects()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setListeners()
    }

    private fun setListeners() {
        binding.myProjectsSwipeToRefresh.setOnRefreshListener {
            loadMyProjects()
            binding.myProjectsSwipeToRefresh.isRefreshing = false
        }
    }

    private fun loadMyProjects() {
        isLoading(true)
        myProjectRef
            .orderBy(constant.projectTimeStamp, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {


                myProjectList.clear()

                if (it != null && !it.isEmpty) {
                    for (document in it.documents) {
                        val project = document.toObject<ProjectModel>()

                        if (project != null)
                            myProjectList.add(project)
                    }

                    if (myProjectList.size > 0) {
                        isLoading(false)
                        binding.tvMyProjectStatus.visibility = View.GONE
                        binding.myProjectsRecyclerView.visibility = View.VISIBLE

                        adapter = ProjectAdapter(myProjectList)
                        adapter.setOnProjectClickListener(object: ProjectAdapter.OnProjectItemClickListener{

                            override fun onLikeClick(project: ProjectModel, position: Int, isLiked: Boolean) {
//                                TODO("Not yet implemented")
                            }

                            override fun onMenuClick(project: ProjectModel, position: Int) {
                                openMenu(project, position)
                            }

                            override fun onContactMeClick(organizerID: String) {
                                openSendMessageDialog(organizerID)
                            }

                        })
                        binding.myProjectsRecyclerView.adapter = adapter
                        binding.myProjectsRecyclerView.setHasFixedSize(true)

                    } else {
                        isLoading(false)
                        binding.myProjectsRecyclerView.visibility = View.GONE
                        binding.tvMyProjectStatus.visibility = View.VISIBLE
                    }
                } else {
                    isLoading(false)
                    binding.myProjectsRecyclerView.visibility = View.GONE
                    binding.tvMyProjectStatus.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                isLoading(false)
                Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openMenu(project: ProjectModel, position: Int) {
        val bottomDialog = BottomSheetDialog(requireContext(), R.style.bottom_dialog)
        bottomDialog.setContentView(R.layout.project_bottom_dialog)

        val tvEdit = bottomDialog.findViewById<TextView>(R.id.tvEdit)
        val tvDelete = bottomDialog.findViewById<TextView>(R.id.tvDelete)
        val tvReport = bottomDialog.findViewById<TextView>(R.id.tvReport)

        tvEdit?.setOnClickListener {
            val action = MyProjectsFragmentDirections.actionMyProjectsFragmentToAddProjectFragment()
            action.projectData = project

            bottomDialog.dismiss()
            navController.navigate(action)
        }


        tvDelete?.setOnClickListener {
            bottomDialog.dismiss()

            val deleteBottomDialog = BottomSheetDialog(requireContext(), R.style.bottom_dialog)
            deleteBottomDialog.setContentView(R.layout.project_delete_bottom_dialog)

            val btnCancelDelete = deleteBottomDialog.findViewById<MaterialButton>(R.id.btnCancelDelete)
            val btnDeleteProject = deleteBottomDialog.findViewById<MaterialButton>(R.id.btnDeleteProject)

            btnDeleteProject?.setOnClickListener {
                deleteProject(project.projectID, position)
                deleteBottomDialog.dismiss()
            }

            btnCancelDelete?.setOnClickListener {
                deleteBottomDialog.dismiss()
            }

            deleteBottomDialog.show()
        }

        tvReport?.setOnClickListener {
            Toast.makeText(context, "report ${project.projectID}", Toast.LENGTH_SHORT).show()
            bottomDialog.dismiss()
        }

        bottomDialog.show()
    }

    private fun openSendMessageDialog(organizerID: String) {

    }

    private fun deleteProject(projectID: String?, position: Int) {
        isLoading(true)
        myProjectRef.document(projectID.toString()).delete()
            .addOnSuccessListener {

                if (myProjectList.size != 0) {
                    myProjectList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyDataSetChanged()
                }

                isLoading(false)
                Toast.makeText(context, "Project Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                isLoading(false)
                Toast.makeText(context, "Error while deleting", Toast.LENGTH_SHORT).show()
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