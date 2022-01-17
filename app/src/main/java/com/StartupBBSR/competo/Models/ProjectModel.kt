package com.StartupBBSR.competo.Models

import android.os.Parcelable

data class ProjectModel(
    val projectID: String? = null,
    val projectOrganizerID: String? = null,
    val projectImage: String? = null,
    val projectTitle: String? = null,
    val projectDescription: String? = null,
    val projectTags: List<String>? = null,
    val projectTimeStamp: Long? = null,
    val likeCount: Int? = null
)