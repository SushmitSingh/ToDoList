package com.example.todolist.model

data class Task(
    var id: String? = "",
    var title: String? = "",
    var completed: Boolean = false,
    var timestamp: Long = System.currentTimeMillis(),
    var imageUrl: String = "",
    var description: String = "",
)

