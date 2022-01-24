package com.StartupBBSR.competo.Fragments

import android.content.Context
import android.content.Intent
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
import com.StartupBBSR.competo.Activity.ChatDetailActivity
import com.StartupBBSR.competo.Adapters.ProjectAdapter
import com.StartupBBSR.competo.Fragments.MyProjectsFragmentDirections.ActionMyProjectsFragmentToAddProjectFragment
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.Models.RequestModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.FragmentMyProjectsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import java.util.*
import kotlin.collections.ArrayList


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
                                like(project, isLiked)
                            }

                            override fun onMenuClick(project: ProjectModel, position: Int) {
                                openMenu(project, position)
                            }

                            override fun onContactMeClick(organizerID: String) {
                                firestoreDB.collection(constant.chatConnections).document(userID)
                                    .get().addOnCompleteListener { task ->
                                        if (task.isSuccessful && task.result != null) {
                                            val connectionListSnapshot: DocumentSnapshot = task.result
                                            val connectionList = connectionListSnapshot.get(constant.connections) as? List<String>

                                            if (connectionList?.isNotEmpty() == true) {
                                                if (connectionList.contains(organizerID)) {
//                                                        Chat present
                                                    firestoreDB.collection(constant.users).document(organizerID)
                                                        .get().addOnCompleteListener { user ->
                                                            if (user.isSuccessful && user.result != null) {
                                                                val intent = Intent(context, ChatDetailActivity::class.java)
                                                                intent.putExtra("receiverID", organizerID)
                                                                intent.putExtra("receiverName", user.result.getString(constant.userNameField))
                                                                intent.putExtra("receiverPhoto", user.result.getString(constant.userPhotoField))
                                                                startActivity(intent)
                                                            }
                                                        }
                                                }
                                                else {
//                                                        Create new request
                                                    openSendMessageDialog(organizerID)
                                                }
                                            } else {
                                                openSendMessageDialog(organizerID)
                                            }
                                        }
                                    }
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

    private fun like(project: ProjectModel, liked: Boolean) {
        if (liked) {
            firestoreDB.collection(constant.project).document(userID).collection(constant.likedProjects)
                .document(project.projectID.toString()).set(project)
                .addOnSuccessListener {
                    Toast.makeText(context, "Added to liked projects", Toast.LENGTH_SHORT).show()
                }
        } else {
            firestoreDB.collection(constant.project).document(userID).collection(constant.likedProjects)
                .document(project.projectID.toString()).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Removed from liked projects", Toast.LENGTH_SHORT).show()
                }
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
            firestoreDB.collection(constant.project).document(project.projectOrganizerID.toString()).collection(constant.createdProjects)
                .document(project.projectID.toString()).update("reportCount", FieldValue.increment(1)).addOnSuccessListener {
                    Toast.makeText(context, "Project reported", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(context, "Error occured", Toast.LENGTH_SHORT).show()
                }
            bottomDialog.dismiss()
        }

        bottomDialog.show()
    }

    private fun openSendMessageDialog(organizerID: String) {
        val sendMessageBottomDialog = BottomSheetDialog(requireContext(), R.style.bottom_dialog)
        sendMessageBottomDialog.setContentView(R.layout.send_message_bottom_dialog)

        val tvContact = sendMessageBottomDialog.findViewById<TextView>(R.id.tvContact)
        val etMessage = sendMessageBottomDialog.findViewById<TextInputEditText>(R.id.etInputMessage)

        val cancelButton = sendMessageBottomDialog.findViewById<MaterialButton>(R.id.btnCancelMessage)
        val sendButton = sendMessageBottomDialog.findViewById<MaterialButton>(R.id.btnSendMessage)

        var name: String = ""

        firestoreDB.collection(constant.users).document(organizerID).get().addOnSuccessListener {
            name = it[constant.userNameField].toString()
            tvContact?.text = getString(R.string.contactMe, name)
        }

        sendButton?.setOnClickListener {
            if (etMessage?.text?.trim().isNullOrEmpty()) {
                Toast.makeText(context, "Enter Message", Toast.LENGTH_SHORT).show()
            } else {
                sendButton.isEnabled = false
                val requestMessage: String = etMessage?.text.toString().trim()
                val requestModel = RequestModel(userID, requestMessage, Date().time)

                firestoreDB.collection(constant.requests)
                    .document(organizerID)
                    .collection(constant.requests)
                    .document(userID)
                    .set(requestModel)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show()
                        sendMessageBottomDialog.dismiss();
//                        todo by aditya for notification
                        /*firestoreDB.collection("token").document(organizerID).get()
                            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document.exists()) {
                                        Log.d(
                                            "data",
                                            "DocumentSnapshot data: " + document.getString("token")
                                        )
                                        firestoreDB.collection("Users").document(userID).get()
                                            .addOnCompleteListener { task3: Task<DocumentSnapshot> ->
                                                if (task3.isSuccessful) {
                                                    val document3 = task3.result
                                                    if (document3.exists()) {
                                                        Log.d(
                                                            "data",
                                                            "DocumentSnapshot data: " + document3.getString(
                                                                "Name"
                                                            )
                                                        )
                                                        sendfcm(
                                                            document.getString("token"),
                                                            document3.getString("Name")
                                                        )
                                                    }
                                                }
                                            }
                                    } else {
                                        Log.d("data", "No such document")
                                    }
                                } else {
                                    Log.d("data", "get failed with ", task.exception)
                                }
                            }*/
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error sending request", Toast.LENGTH_SHORT).show()
                        sendButton.isEnabled = true
                    }
            }
        }

        cancelButton?.setOnClickListener {
            sendMessageBottomDialog.dismiss()
        }

        sendMessageBottomDialog.show()
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