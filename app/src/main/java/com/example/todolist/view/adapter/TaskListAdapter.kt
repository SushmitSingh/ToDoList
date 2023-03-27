package com.example.todolist.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.databinding.ListItemTaskBinding
import com.example.todolist.model.Task
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class TaskListAdapter(
    options: FirebaseRecyclerOptions<Task>,
) :
    FirebaseRecyclerAdapter<Task, TaskListAdapter.TaskViewHolder>(options) {

    var onTaskCheckedListener: OnTaskCheckedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int, task: Task) {
        holder.bind(task)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ListItemTaskBinding.bind(itemView)

        fun bind(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskCheckBox.isChecked = task.completed
            binding.taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedListener?.onTaskChecked(task.id!!, isChecked)
            }
        }
    }

    interface OnTaskCheckedListener {
        fun onTaskChecked(taskId: String, isCompleted: Boolean)
    }
}
