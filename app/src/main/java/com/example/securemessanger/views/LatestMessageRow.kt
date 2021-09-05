package com.example.securemessanger.views

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.securemessanger.R
import com.example.securemessanger.messages.AESAlgorithm
import com.example.securemessanger.messages.LatestMessagesActivity
import com.example.securemessanger.model.ChatMessage
import com.example.securemessanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {
    var chatPartnerUser: User? = null
    var count: Int = 0
    val aes = AESAlgorithm("akjekqw7gfdazao9")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bind(viewHolder: ViewHolder, position: Int) {
        chatMessage.text = aes.decrypt(chatMessage.text)
        viewHolder.itemView.message_textview_latest_message.text = chatMessage.text

        val chatPartnerId: String
        Log.d("MessageRow","id:- ${chatMessage.fromId}")
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(chatMessage.text!=""){
                    chatPartnerUser = p0.getValue(User::class.java)
                    viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.username

                    val targetImageView = viewHolder.itemView.imageview_latest_message
                    if(chatPartnerUser?.profileImageUrl!="")
                    {
                        Log.d("MessageRow","profileImageUrl :- ${chatPartnerUser?.profileImageUrl}")
                        Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)

                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}