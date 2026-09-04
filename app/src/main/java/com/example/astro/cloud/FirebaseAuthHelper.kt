package com.example.astro.cloud

import android.content.Context
import android.util.Log
import com.example.astro.model.UserBirthProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication Helper.
 * Supports anonymous sign-in and Google Sign-In state tracking.
 */
class FirebaseAuthHelper {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            trySend(fbAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw IllegalStateException("Firebase user is null")
            Result.success(user)
        } catch (e: Exception) {
            Log.w("FirebaseAuthHelper", "Anonymous sign in failed or offline: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
