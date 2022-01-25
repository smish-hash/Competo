package com.StartupBBSR.competo.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class messagingViewModel : ViewModel() {

    val auth = Firebase.auth

    val firebaseDB = Firebase.firestore

    fun notification(receiverID : String, senderID : String, requestMessage : String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val token = firebaseDB.collection("token").document(receiverID).get()
            token.addOnCompleteListener()
            {
                receiverData ->
                if(receiverData.isSuccessful)
                {
                    val fcmToken = receiverData.result
                    if(fcmToken.exists())
                    {
                        val receiverToken = fcmToken.getString("token")
                        firebaseDB.collection("Users").document(senderID).get().addOnCompleteListener()
                        {
                            senderData ->
                            if(senderData.isSuccessful)
                            {
                                val name = senderData.result

                                if(name.exists())
                                {
                                    val senderName = name.getString("Name")
                                    sendMessage(receiverToken!!,senderName!!,requestMessage)
                                }
                            }
                            else{
                                Log.d("Name fetching failed for senderID",senderID)
                            }
                        }
                    }
                }
                else{
                    Log.d("Token fetching failed for receiverID",receiverID)
                }
            }
        }
    }

    fun sendMessage(token : String, name : String, requestMessage : String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(
                JSON, """{
                                "data" : {
                                "id" : "${auth.uid.toString()}"
                                "category" : "request",
                                "title" : "New Message Request"
                                "body" : "$name : $requestMessage"
                                },
                                "to":"$token"
                                }"""
            )
            val request = Request.Builder()
                .url(FCM_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader(
                    "Authorization",
                    "key=$FCM_API_KEY"
                )
                .build()
            try {
                val response = client.newCall(request).execute()
                Log.d("response", response.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object{
        const val FCM_API_KEY = "AAAABmOW__8:APA91bFEiWxr4rRQa3M_5n-w-5XDjLnQ9nf2IgAs1r0ppfwgTLZoGgOJmRAF1pt59hHqdMZ74AmAx1lkk0HaCuLwUCsHi_M_BWEZAGwkXyp-57YJk_pGmGWwJKNEU_bnJLl7bv7VDPzy"
        const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
    }
}