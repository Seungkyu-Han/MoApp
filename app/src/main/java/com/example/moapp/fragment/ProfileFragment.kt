package com.example.moapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.moapp.databinding.FragmentProfileBinding

class ProfileFragment : DialogFragment() {

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_USER_NAME = "user_name"
        const val ARG_USER_IMG = "user_img"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        arguments?.let {
            val userId = it.getInt(ARG_USER_ID, 0)
            val userName = it.getString(ARG_USER_NAME, "")
            val userImg = it.getString(ARG_USER_IMG, "")

            // 프로필 정보를 화면에 표시
            binding.profileTextviewId.text = "ID: $userId"
            binding.profileTextviewName.text = "Name: $userName"
            Glide.with(binding.profileImageview.context)
                .load(userImg)
                .into(binding.profileImageview)
        }

        return binding.root
    }
}
