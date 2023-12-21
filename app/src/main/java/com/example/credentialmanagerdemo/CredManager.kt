package com.example.credentialmanagerdemo

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

const val TAG = "CredManager"

class CredManager(
    private val credentialManager: CredentialManager,
    private val auth: FirebaseAuth
) {
    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId("1068124218956-hc956hr1c2q41ck3vf0v3e0c5v424t05.apps.googleusercontent.com")
        .build()

    suspend fun login(
        context: Context
    ) {
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error getting credential", e)
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Received google id token: $idToken")
                        loginWithFirebase(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private suspend fun loginWithFirebase(idToken: String) {
        val googleCredentials = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.signInWithCredential(googleCredentials).await().user
        Log.d(
            TAG, """
            Successfully signed in user ${user?.uid}
            with email ${user?.email}
            and photo url ${user?.photoUrl}
            and username ${user?.displayName}
        """.trimIndent()
        )
    }
}