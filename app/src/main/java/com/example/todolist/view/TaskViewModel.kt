package com.example.todolist.view

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.model.Task
import com.example.todolist.view.adapter.TaskListAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class TaskViewModel : ViewModel() {

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val tasksRef = databaseRef.child("tasks")
    private val storageRef = FirebaseStorage.getInstance().reference.child("uploads")

    val taskImageUrl: MutableLiveData<String> = MutableLiveData()
    val taskDescription: MutableLiveData<String> = MutableLiveData()
    val taskTitle: MutableLiveData<String?> = MutableLiveData()

    private val taskListAdapterOptions: FirebaseRecyclerOptions<Task> =
        FirebaseRecyclerOptions.Builder<Task>().setQuery(tasksRef, Task::class.java).build()

    val taskListAdapter: TaskListAdapter by lazy {
        TaskListAdapter(taskListAdapterOptions)
    }

    fun uploadImage(imageUri: Uri) {
        //Add File to Firebase Storage and get the download URL
        val imageRef = storageRef.child(imageUri.lastPathSegment.toString())
        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener {
                taskImageUrl.value = it.toString()
            }
        }
    }

    fun addTaskToFirebase() {
        val title = taskTitle.value?.trim()
        if (!title.isNullOrEmpty()) {
            val taskId = tasksRef.push().key
            val task = createTask(taskId, title)
            saveTaskToDatabase(task)
        }
    }

    private fun createTask(taskId: String?, title: String): Task {
        val imageUrl = taskImageUrl.value ?: ""
        val description = taskDescription.value ?: ""
        return Task(
            id = taskId.toString(),
            title = title,
            completed = false,
            timestamp = System.currentTimeMillis(),
            imageUrl = imageUrl,
            description = description
        )
    }

    private fun saveTaskToDatabase(task: Task) {
        tasksRef.child(task.id.toString()).setValue(task)
    }

    fun deleteTask(taskId: String) {
        tasksRef.child(taskId).removeValue()
    }

    fun onTaskChecked(taskId: String, isCompleted: Boolean) {
        tasksRef.child(taskId).child("completed").setValue(isCompleted)
    }

    fun onTaskEdited(task: Task) {
        tasksRef.child(task.id.toString()).setValue(task)
    }

    fun getTask(taskId: String) {
        tasksRef.child(taskId).get().addOnSuccessListener {
            val task = it.getValue(Task::class.java)
            taskTitle.value = task?.title
            taskImageUrl.value = task?.imageUrl
            taskDescription.value = task?.description
        }
    }
}
