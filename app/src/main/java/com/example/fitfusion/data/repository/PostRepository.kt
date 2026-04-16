package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.UserPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object PostRepository {

    private val _posts = MutableStateFlow<List<UserPost>>(emptyList())
    val posts: StateFlow<List<UserPost>> = _posts.asStateFlow()

    fun addPost(post: UserPost) {
        _posts.update { listOf(post) + it }
    }
}
