package com.StartupBBSR.competo.Fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.R.array.eventTags
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.FragmentAddProjectBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class AddProjectFragment : Fragment() {

    private lateinit var binding: FragmentAddProjectBinding
    private lateinit var navController: NavController

    private var encodedImage: String? = null

    private lateinit var projectTagDataset: List<String>
    private var projectSelectedTags: ArrayList<String> = ArrayList()


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var constant: Constant

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
        binding = FragmentAddProjectBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firestoreDB = FirebaseFirestore.getInstance()
        userID = firebaseAuth.uid.toString()
        constant = Constant()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setTags()
        setListeners()
    }

    private fun setListeners() {
        binding.ivProjectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnUploadProject.setOnClickListener {
            isLoading(true)
            if (allFieldsCorrect()) {

                GlobalScope.launch {
                    createProject()
                }

            } else {
                isLoading(false)
            }
        }
    }

    private suspend fun createProject() {
        val projectCollectionReference = firestoreDB.collection(constant.project)
        val projectID = projectCollectionReference.document().id

        val project = ProjectModel(
            projectID,
            userID,
            encodedImage,
            binding.etPostName.text.toString(),
            binding.Description.text.toString(),
            projectSelectedTags,
            Date().time, 0
        )

        projectCollectionReference.document(userID).collection(constant.createdProjects).document(projectID).set(project)
            .addOnSuccessListener {
                isLoading(false)

//                Ancestral document for reference
                val documentMap = hashMapOf("id" to userID)
                projectCollectionReference.document(userID).set(documentMap, SetOptions.merge())

                toast("Project Created")
                navController.navigateUp()
            }.addOnFailureListener {
                isLoading(false)
                toast("There was some error while creating your project")
            }
    }

    private fun setTags() {
        projectTagDataset = resources.getStringArray(eventTags).toList()

        for (tag in projectTagDataset) {
            val chip = Chip(context)
            chip.text = tag
            chip.isCheckable = true

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    projectSelectedTags.add(tag)
                } else
                    projectSelectedTags.remove(tag)
            }

            binding.projectTagsGroup.addView(chip)
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 180
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivProjectImage.setImageBitmap(bitmap)
                binding.ivProjectImage.scaleType = ImageView.ScaleType.CENTER_CROP
                encodedImage = encodeImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun allFieldsCorrect(): Boolean {
        if (encodedImage == null) {
            toast("Add a photo")
            return false
        } else if (binding.etPostName.text.isEmpty()) {
            toast("Enter Title")
            return false
        } else if (binding.Description.text?.isEmpty() == true) {
            toast("Enter Description")
            return false
        } else if (projectSelectedTags.size == 0) {
            toast("Add Tags")
            return false
        }
        return true
    }

    private fun toast(s: String) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }


    private fun isLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnUploadProject.visibility = View.GONE
            binding.uploadingProgressBar.visibility = View.VISIBLE
        } else {
            binding.btnUploadProject.visibility = View.VISIBLE
            binding.uploadingProgressBar.visibility = View.GONE
        }
    }
}
