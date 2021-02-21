package com.example.burgertracker.firebase

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

private const val TAG = "FCMService"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun getStartCommandIntent(p0: Intent?): Intent {
        Log.d(TAG, "getStartCommandIntent() called")
        return super.getStartCommandIntent(p0)
    }

    override fun handleIntentOnMainThread(p0: Intent?): Boolean {
        Log.d(TAG, "handleIntentOnMainThread() called")
        return super.handleIntentOnMainThread(p0)
    }

    override fun handleIntent(p0: Intent?) {
        Log.d(TAG, "handleIntent() called")
        super.handleIntent(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d(TAG, "onMessageReceived() called -> ${p0.notification?.title}, ${p0.data}")
        FCMServiceEvents.placeFavoritesLiveData.postValue(p0.data["favorites"])
        super.onMessageReceived(p0)
    }

    override fun onDeletedMessages() {
        Log.d(TAG, "onDeletedMessages() called")
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String) {
        Log.d(TAG, "onMessageSent() called")
        super.onMessageSent(p0)
    }

    override fun onSendError(p0: String, p1: Exception) {
        Log.d(TAG, "onSendError() called")
        super.onSendError(p0, p1)
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken() called, token is $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
    }
}