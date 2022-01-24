package com.StartupBBSR.competo.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.StartupBBSR.competo.R

class ProjectFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val navHostFragment = parentFragment as NavHostFragment
        val startFragment = navHostFragment.parentFragment as StartFragment
        startFragment.setTitleText(4)
        return inflater.inflate(R.layout.fragment_project, container, false)
    }


    fun onGoHomeBackPressed() {
        val navHostFragment = parentFragment as NavHostFragment
        val startFragment = navHostFragment.parentFragment as StartFragment
        startFragment.loadFragment(1)
    }
}