package com.StartupBBSR.competo.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.StartupBBSR.competo.Models.ProjectModel
import com.StartupBBSR.competo.R
import com.StartupBBSR.competo.Utils.Constant
import com.StartupBBSR.competo.databinding.ProjectItemBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProjectAdapter(var mList: List<ProjectModel>) :
    RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {

    private lateinit var binding: ProjectItemBinding
    private lateinit var listener: OnProjectItemClickListener

    interface OnProjectItemClickListener {
        fun onLikeClick(project: ProjectModel, position: Int)
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
            getOrganizerData(project.projectOrganizerID, context)
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
                        loadUsingGlide(it[constant.userPhotoField].toString(), context)
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
                listener.onLikeClick(mList[position], position)
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