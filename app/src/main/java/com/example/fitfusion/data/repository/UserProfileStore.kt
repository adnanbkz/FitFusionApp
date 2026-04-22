package com.example.fitfusion.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "profile_prefs"
private const val KEY_PHOTO_URI = "profile_photo_uri"

object UserProfileStore {

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private var initialized = false

    fun ensureInitialized(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _photoUri.value = prefs.getString(KEY_PHOTO_URI, null)?.let { Uri.parse(it) }
        initialized = true
    }

    fun updatePhotoUri(context: Context, uri: Uri?) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            if (uri != null) putString(KEY_PHOTO_URI, uri.toString())
            else remove(KEY_PHOTO_URI)
        }
        _photoUri.value = uri
    }
}
