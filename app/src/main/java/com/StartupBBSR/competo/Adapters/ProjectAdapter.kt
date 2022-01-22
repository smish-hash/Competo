package com.StartupBBSR.competo.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import androidx.recyclerview.widget.RecyclerView
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.ProjectItemBinding
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProjectAdapter(var mList: List<ProjectModel>) :
    RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {

    private lateinit var binding: ProjectItemBinding
    private lateinit var listener: OnProjectItemClickListener

    private val tagColorList = arrayOf(
        R.color.onboarding_pink,
        R.color.onboarding_green,
        R.color.onboarding_blue
    )

    interface OnProjectItemClickListener {
        fun onLikeClick(project: ProjectModel, position: Int, isLiked: Boolean)
        fun onMenuClick(project: ProjectModel, position: Int)
        fun onContactMeClick(organizerID: String)
    }

    fun setOnProjectClickListener(listener: OnProjectItemClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectAdapter.ViewHolder {
        binding = ProjectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectAdapter.ViewHolder, position: Int) {
        val context = holder.itemView.context
        with(holder) {
            with(mList[position]) {
                setProjectData(this, context)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(private val binding: ProjectItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.btnFavoriteProject.setOnClickListener(this)
            binding.projectMenu.setOnClickListener(this)
            binding.btnContactMe.setOnClickListener(this)
        }

        private val dateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault())


        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())


        fun setProjectData(project: ProjectModel, context: Context) {
            isLoading(true)
            binding.tvProjectTitle.text = project.projectTitle
            binding.tvProjectDescription.text = project.projectDescription
            binding.projectPoster.setImageBitmap(getImage(project.projectImage))
            binding.tvProjectTime.text = context.resources.getString(
                R.string.projectDateFormat,
                dateFormat.format(project.projectTimeStamp),
                timeFormat.format(project.projectTimeStamp)
            )

            if (project.projectTags != null) {
                for (i in project.projectTags) {
                    val chip = Chip(context)
                    chip.text = i
                    chip.setTextColor(context.resources.getColor(R.color.white, context.theme))
                    chip.setChipBackgroundColorResource(tagColorList.random())
                    binding.projectTags.addView(chip)
                }
            }


            getOrganizerData(project.projectOrganizerID, context)
            getLikedData(project.projectID, context)
            isLoading(false)
        }

        private fun getOrganizerData(organizerID: String?, context: Context) {
            val fireStoreDB = FirebaseFirestore.getInstance()
            val constant = Constant()
            if (organizerID != null) {
                fireStoreDB.collection(constant.users)
                    .document(organizerID)
                    .get()
                    .addOnSuccessListener {
                        binding.tvProfileName.text = it[constant.userNameField].toString()
                        if (FirebaseAuth.getInstance().uid == it[constant.userIdField].toString()) {
                            binding.btnContactMe.visibility = View.GONE
                        }
                        loadUsingGlide(it[constant.userPhotoField].toString(), context)
                    }
            }
        }

        private fun getLikedData(projectID: String?, context: Context) {
            val firestoreDB = FirebaseFirestore.getInstance()
            val userID = FirebaseAuth.getInstance().uid
            val constant = Constant()
            firestoreDB.collection(constant.project).document(userID.toString())
                .collection(constant.likedProjects).document(projectID.toString()).get().addOnSuccessListener {
                    if (it.exists() && it != null) {
                        binding.btnFavoriteProject.isChecked = true
                    }
                }
        }

        private fun loadUsingGlide(imageURL: String?, context: Context) {
            if (imageURL != null) {
                Glide.with(context).load(imageURL).into(binding.profileImage)
            }
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (p0 != null) {
                    when(p0) {
                        binding.btnFavoriteProject -> {
                            if (!binding.btnFavoriteProject.isChecked) {
                                binding.btnFavoriteProject.animate()
                                    .setDuration(100)
                                    .scaleX(1.1f)
                                    .scaleY(1.1f)
                                    .withEndAction {

                                        listener.onLikeClick(mList[position], position, false)

                                        binding.btnFavoriteProject.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(100)
                                            .start()
                                    }
                                    .start()
                            } else {
                                binding.btnFavoriteProject.animate()
                                    .setDuration(100)
                                    .scaleX(1.1f)
                                    .scaleY(1.1f)
                                    .withEndAction {

                                        listener.onLikeClick(mList[position], position, true)

                                        binding.btnFavoriteProject.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(100)
                                            .start()
                                    }
                                    .start()
                            }
                        }
                        binding.projectMenu -> listener.onMenuClick(mList[position], position)
                        binding.btnContactMe -> listener.onContactMeClick(mList[position].projectOrganizerID.toString())
                    }
                }
            }
        }
    }

    private fun isLoading(loading: Boolean) {
        if (loading) {
            binding.projectItemProgress.visibility = View.VISIBLE
        } else {
            binding.projectItemProgress.visibility = View.GONE
        }
    }

    private fun getImage(encodedImage: String?): Bitmap? {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}