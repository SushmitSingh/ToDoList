package com.example.todolist.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.model.Task
import com.example.todolist.view.adapter.TaskListAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class TaskViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference.child("tasks").apply {
        keepSynced(true)
    }

    private val _taskListAdapterOptions: FirebaseRecyclerOptions<Task> =
        FirebaseRecyclerOptions.Builder<Task>()
            .setQuery(database, Task::class.java)
            .build()

    private val _taskListAdapter = TaskListAdapter(_taskListAdapterOptions)

    val taskListAdapter: TaskListAdapter
        get() = _taskListAdapter

    val taskTitle = MutableLiveData<String>()

    fun addTaskToFirebase() {
        val title = taskTitle.value?.trim()
        if (!title.isNullOrEmpty()) {
            val taskId = database.push().key
            val task = Task(
                id = taskId,
                title = title,
                completed = false,
                timestamp = System.currentTimeMillis()
            )
            taskId?.let { database.child(it).setValue(task) }
        }
    }

    fun updateTaskInFirebase(taskId: String, isCompleted: Boolean) {
        database.child(taskId).child("completed").setValue(isCompleted)
    }
}
