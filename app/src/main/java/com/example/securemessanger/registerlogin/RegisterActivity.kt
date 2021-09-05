package com.example.securemessanger.registerlogin

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.securemessanger.R
import com.example.securemessanger.messages.LatestMessagesActivity
import com.example.securemessanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var selectedPhotoUri:Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }
        already_have_account_textview.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val getaction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                selectedPhotoUri = uri
                select_circle_imageview_register.setImageURI(uri)
            }
        )
        select_circle_imageview_register.setOnClickListener {
            getaction.launch("image/*")
            selectphoto_textview_register.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if(email.isEmpty()||password.isEmpty()) {
            Toast.makeText(this,"Please Enter the Email/Password",Toast.LENGTH_SHORT).show()
            return
        }
        if(password.length<6)
        {
            Toast.makeText(this,"Password length is < 6",Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MainActivity", "Email :- $email")
        Log.d("MainActivity", "Password :- $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Toast.makeText(this,"Registered Successfully",Toast.LENGTH_SHORT).show()
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Toast.makeText(this,"Failed to create user",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri==null){
            Log.d("MainActivity", "uri is null")
            return
        }
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("MainActivity", "Image uploaded :- ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                Log.d("MainActivity", "Image Location :- $it")
                saveUserToDatabase(it.toString())
            }
            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,username_edittext_register.text.toString(),profileImageUrl )
        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                Log.d("MainActivity", "Saved in Firebase database successfully")
            }.addOnFailureListener{
                Log.d("MainActivity", "Failed to Save in database ${it.message}")
            }
    }
}
