package com.roaa.shops_customers.NotificationSetup

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "NotificationClass"

class NotificationClass {

    public fun createMessage(title:String,message:String,recipientToken:String){
        PushNotification(
                NotificationData(title, message),
                recipientToken
        ).also {
            sendNotification(it)
        }

    }

    public fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}