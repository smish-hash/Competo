package com.StartupBBSR.competo.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.StartupBBSR.competo.Activity.ChatDetailActivity
import com.StartupBBSR.competo.Adapters.ProjectAdapter
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.Models.RequestModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.R.layout.fragment_liked_projects
import com.StartupBBSR.competo.R.layout.project_bottom_dialog
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.ViewModel.messagingViewModel
import com.StartupBBSR.competo.databinding.FragmentProjectMainBinding
import com.StartupBBSR.competo.databinding.ProjectBottomDialogBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import java.util.*
import kotlin.collections.ArrayList

class ProjectMainFragment : Fragment() {

    private lateinit var binding: FragmentProjectMainBinding
    private lateinit var navController: NavController

    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var constant: Constant
    private lateinit var projectRef: CollectionReference
    private lateinit var userID: String

    private var projectList: ArrayList<ProjectModel> = ArrayList()
    private lateinit var adapter: ProjectAdapter

//    Animating FAB
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var rotateForward: Animation
    private lateinit var rotateBackward: Animation
    private var isFabOpen: Boolean = false

    //messaging viewmodel
    lateinit var messagingViewModel: messagingViewModel

    private val rotation = 180

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navHostFragment = parentFragment as NavHostFragment
                val projectFragment = navHostFragment.parentFragment as ProjectFragment
                projectFragment.onGoHomeBackPressed()

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
        userID = FirebaseAuth.getInstance().uid.toString()

        isFabOpen = false

        projectList = ArrayList()

        messagingViewModel = ViewModelProvider(this).get(com.StartupBBSR.competo.ViewModel.messagingViewModel::class.java)

        messagingViewModel.notification("UbIkDkNJoXPlsW81HjyjA7acM393","UbIkDkNJoXPlsW81HjyjA7acM393","this is a test2")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setAnimators()
        setListeners()
        loadProjects()
    }

    private fun setListeners() {


        binding.btnAddProject.setOnClickListener {
            if (!isFabOpen) {
                    animateFAB()
            } else {
                navController.navigate(R.id.action_projectMainFragment_to_addProjectFragment)
            }
        }

        binding.btnMyProjects.setOnClickListener {
            navController.navigate(R.id.action_projectMainFragment_to_myProjectsFragment)
        }

        binding.btnLikedProjects.setOnClickListener {
            navController.navigate(R.id.action_projectMainFragment_to_likedProjectsFragment)
        }

        binding.viewBackgroundGradient.setOnClickListener {
            animateFAB()
        }
    }

    private fun setAnimators() {
        fabOpen = AnimationUtils.loadAnimation(context, R.anim.project_fab_open)
        fabClose = AnimationUtils.loadAnimation(context, R.anim.project_fab_close)
        rotateBackward = AnimationUtils.loadAnimation(context, R.anim.project_fab_rotate_backward)
        rotateForward = AnimationUtils.loadAnimation(context, R.anim.project_fab_rotate_forward)
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

                            projectList.sortByDescending { p ->
                                p.projectTimeStamp
                            }

                            adapter = ProjectAdapter(projectList)

                            adapter.setOnProjectClickListener(object : ProjectAdapter.OnProjectItemClickListener{
                                override fun onLikeClick(project: ProjectModel, position: Int, isLiked: Boolean) {
                                    like(project, isLiked)
                                }

                                override fun onMenuClick(project: ProjectModel, position: Int) {
                                    if (project.projectID != null)
                                        openMenu(project)
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

                            binding.projectRecyclerView.adapter = adapter
                            binding.projectRecyclerView.setHasFixedSize(true)
                            binding.projectRecyclerView.visibility = View.VISIBLE
                        }
                    }
            }
            isLoading(false)
        }
    }

    private fun openMenu(project: ProjectModel) {
        val bottomDialog = BottomSheetDialog(requireContext(), R.style.bottom_dialog)
        bottomDialog.setContentView(project_bottom_dialog)

        val tvEdit = bottomDialog.findViewById<TextView>(R.id.tvEdit)
        val tvDelete = bottomDialog.findViewById<TextView>(R.id.tvDelete)
        val tvReport = bottomDialog.findViewById<TextView>(R.id.tvReport)

        tvEdit?.visibility = View.GONE
        tvDelete?.visibility = View.GONE

        tvReport?.setOnClickListener {
            projectRef.document(project.projectOrganizerID.toString()).collection(constant.createdProjects)
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

                        //send message request
                        messagingViewModel.notification(organizerID,userID,requestMessage)
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

    private fun like(project: ProjectModel, liked: Boolean) {
        if (liked) {
            projectRef.document(userID).collection(constant.likedProjects)
                .document(project.projectID.toString()).set(project)
                .addOnSuccessListener {
                    Toast.makeText(context, "Added to liked projects", Toast.LENGTH_SHORT).show()
                }
        } else {
            projectRef.document(userID).collection(constant.likedProjects)
                .document(project.projectID.toString()).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Removed from liked projects", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun animateFAB() {
        if (isFabOpen) {
//            close fab

            binding.btnAddProject.animate()
                .rotationBy(-rotation.toFloat())
                .setDuration(100)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction {
                    binding.btnAddProject.setImageResource(R.drawable.ic_round_workspaces_24)

                    binding.btnAddProject.animate()
                        .rotationBy(-rotation.toFloat())
                        .scaleX(1F)
                        .scaleY(1F)
                        .setDuration(100)
                        .start()
                }
                .start()


//            binding.btnAddProject.startAnimation(rotateForward)
            binding.btnLikedProjects.startAnimation(fabClose)
            binding.btnMyProjects.startAnimation(fabClose)
            binding.btnLikedProjects.isClickable = false
            binding.btnMyProjects.isClickable = false
            binding.projectRecyclerView.isClickable = true
            binding.viewBackgroundGradient.visibility = View.GONE

            binding.tvLikedProjects.visibility = View.GONE
            binding.tvMyProjects.visibility = View.GONE
            binding.tvAddProject.visibility = View.GONE
            isFabOpen = !isFabOpen
        } else {
//            open fab
            binding.btnAddProject.animate()
                .rotationBy(rotation.toFloat())
                .setDuration(100)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction {
                    binding.btnAddProject.setImageResource(R.drawable.ic_baseline_post_add_24)

                    binding.btnAddProject.animate()
                        .rotationBy(rotation.toFloat())
                        .scaleX(1F)
                        .scaleY(1F)
                        .setDuration(100)
                        .start()
                }
                .start()
//            binding.btnAddProject.startAnimation(rotateBackward)
            binding.btnLikedProjects.startAnimation(fabOpen)
            binding.btnMyProjects.startAnimation(fabOpen)
            binding.btnLikedProjects.isClickable = true
            binding.btnMyProjects.isClickable = true
            binding.projectRecyclerView.isClickable = false
            binding.viewBackgroundGradient.visibility = View.VISIBLE

            binding.tvLikedProjects.visibility = View.VISIBLE
            binding.tvMyProjects.visibility = View.VISIBLE
            binding.tvAddProject.visibility = View.VISIBLE
            isFabOpen = !isFabOpen
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