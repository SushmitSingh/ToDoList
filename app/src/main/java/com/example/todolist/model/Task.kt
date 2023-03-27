package com.example.todolist.model

data class Task(
    var id: String? = null,
    var title: String = "",
    var completed: Boolean = false,
    var timestamp: Long = System.currentTimeMillis()
)

