package com.example.todolist.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityTaskListBinding
import com.example.todolist.view.adapter.TaskListAdapter

class TaskListActivity : AppCompatActivity(), TaskListAdapter.OnTaskCheckedListener {

    private lateinit var binding: ActivityTaskListBinding
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Set the task list adapter in the recycler view
        binding.recyclerView.adapter = viewModel.taskListAdapter

        viewModel.taskListAdapter.onTaskCheckedListener = this

        binding.button.setOnClickListener {
            val title = binding.editText.text.toString().trim()
            if (title.isNotEmpty()) {
                viewModel.taskTitle.value = title
                viewModel.addTaskToFirebase()
                binding.editText.setText("")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.taskListAdapter.startListening()
    }

    override fun onResume() {
        super.onResume()
        viewModel.taskListAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.taskListAdapter.stopListening()
    }

    override fun onTaskChecked(taskId: String, completed: Boolean) {
        viewModel.updateTaskInFirebase(taskId, completed)
    }
}
