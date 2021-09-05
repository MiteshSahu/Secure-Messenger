package com.example.securemessanger.messages

import android.media.tv.TvContract.Programs.Genres.encode
import android.net.Uri.encode
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.securemessanger.R
import com.example.securemessanger.model.ChatMessage
import com.example.securemessanger.model.User
import com.example.securemessanger.views.ChatItem
import com.google.android.gms.common.util.Base64Utils.encode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.net.URLEncoder.encode
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val aes = AESAlgorithm("akjekqw7gfdazao9")

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message....")
            performSendMessage()
        }
    }
//    private fun AESEncryptionMethod(mssg: String): String? {
//        val stringByte = mssg.toByteArray()
//        var encryptedByte = ByteArray(stringByte.size)
//        try {
//            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
//            encryptedByte = cipher.doFinal(stringByte)
//        } catch (e: InvalidKeyException) {
//            e.printStackTrace()
//        } catch (e: IllegalBlockSizeException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        var returnMessage: String? = null
//        val ISO_8859_1: Charset = Charset.forName("ISO-8859-1")
//        try {
//            returnMessage = String(encryptedByte, ISO_8859_1)
//        } catch (e: UnsupportedEncodingException) {
//            e.printStackTrace()
//        }
//        Log.d("Cryptography","chatMessage Encrypted:- ${returnMessage}")
//        return returnMessage
//    }

//    private fun AESDecryptionMethod(mssg: String): String? {
//        var encryptedByte = ByteArray(0)
//            encryptedByte = mssg.toByteArray(charset("ISO-8859-1"))
//        var decryptedMessage: String? = mssg
//        val decryption: ByteArray
//            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
//            decryption = decipher.doFinal(encryptedByte)
//            decryptedMessage = String(decryption)
//        Log.d("Cryptography","chatMessage Decrypted:- ${decryptedMessage}")
//        return decryptedMessage
//    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")




        ref.addChildEventListener(object: ChildEventListener {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                var chatMessage = p0.getValue(ChatMessage::class.java)


//                chatMessage?.text = chatMessage?.text?.substring(1, chatMessage?.text?.length!! -1).toString()
//                val stringMessageArray = chatMessage?.text?.split(", ".toRegex())?.toTypedArray()
//                val finalMessage = arrayOfNulls<String>(stringMessageArray?.size!! * 2)
//                for (i in stringMessageArray.indices) {
//                    val stringKeyValue = stringMessageArray[i].split("=".toRegex(), 2).toTypedArray()
                  //  finalMessage[2*i] = android.text.format.DateFormat.format("dd-MM-YYYY hh:mm:ss",stringKeyValue[0].toLong()).toString()
  //                  chatMessage?.text = AESDecryptionMethod(stringKeyValue[1]).toString()

 //               }
                chatMessage?.text = aes.decrypt(chatMessage?.text)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatItem.ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatItem.ChatToItem(chatMessage.text, toUser!!))
                    }
                }
               // chatMessage?.text = AESDecryptionMethod(chatMessage!!.text).toString()
                //Log.d("Cryptography","chatMessage:- ${chatMessage.text}")
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performSendMessage() {

  //      val text = AESEncryptionMethod(editText_chat_log.text.toString())
        var text = editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        try {
            val aes = AESAlgorithm("akjekqw7gfdazao9")
    //        text = aes.encrypt(text)
        } catch (e: Exception) {
            println("e:- $e")
        }

        text = aes.encrypt(text)





        if (fromId == null) return

//    val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text!!, fromId, toId!!, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                editText_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}
class AESAlgorithm(key: String){
    val ALGO = "AES"
    private var keyValue: ByteArray

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun encrypt(Data: String): String {
        val key = generateKey()
        val c = Cipher.getInstance(ALGO)
        c.init(Cipher.ENCRYPT_MODE, key)
        val encVal = c.doFinal(Data.toByteArray())
        return Base64.getEncoder().encodeToString(encVal)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun decrypt(encryptedData: String?): String {
        val key = generateKey()
        val c = Cipher.getInstance(ALGO)
        c.init(Cipher.DECRYPT_MODE, key)
        val decodedValue = Base64.getDecoder().decode(encryptedData)
        val decVal = c.doFinal(decodedValue)
        return String(decVal)
    }


    @Throws(Exception::class)
    private fun generateKey(): Key {
        return SecretKeySpec(keyValue, ALGO)
    }
    init {
        keyValue = key.toByteArray()
    }
}