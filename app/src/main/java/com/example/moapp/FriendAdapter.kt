package com.example.moapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moapp.User

class FriendAdapter(val itemList : ArrayList<User>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    val checkedItem = ArrayList<Int>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendAdapter.FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_group, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendAdapter.FriendViewHolder, position: Int) {
        holder.img.setImageURI(Uri.parse(itemList[position].img))
        holder.name.text = itemList[position].name
        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkedItem.add(itemList[position].id)
            } else {
                checkedItem.remove(itemList[position].id)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class FriendViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById<ImageView>(R.id.userImg)
        val name = itemView.findViewById<TextView>(R.id.userName)
        val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
    }
}