package com.example.todolist.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todolist.databinding.ActivityTaskListBinding
import com.example.todolist.databinding.AddTaskDailogBinding
import com.example.todolist.databinding.TaskDetailDailogBinding
import com.example.todolist.model.Task
import com.example.todolist.view.adapter.TaskListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TaskListActivity : AppCompatActivity(), TaskListAdapter.OnTaskCheckedListener {

    private lateinit var binding: ActivityTaskListBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var addTaskDialogBinding: AddTaskDailogBinding
    private lateinit var taskDetailDailogBinding: TaskDetailDailogBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Set the task list adapter in the recycler view
        binding.recyclerView.adapter = viewModel.taskListAdapter
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)
        viewModel.taskListAdapter.onTaskCheckedListener = this
        binding.recyclerView.removeItemDecorationAt(0)

        binding.button.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        addTaskDialogBinding = AddTaskDailogBinding.inflate(layoutInflater)
        val dialog =
            MaterialAlertDialogBuilder(this).setView(addTaskDialogBinding.root).setTitle("Add Task")
                .setPositiveButton("Yes") { _, _ ->
                    val title = addTaskDialogBinding.editText.text.toString().trim()
                    val description = addTaskDialogBinding.descriptionText.text.toString().trim()
                    val imageUri = addTaskDialogBinding.imageView.tag as? Uri
                    if (title.isNotEmpty()) {
                        viewModel.taskTitle.value = title
                        viewModel.taskDescription.value = description
                        viewModel.taskImageUrl.value = imageUri.toString()
                        viewModel.addTaskToFirebase()
                    }
                }.setNegativeButton("Not Yet", null).create()

        addTaskDialogBinding.imageView.setOnClickListener {
            pickImageFromGallery()
        }

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            addTaskDialogBinding.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = !s.isNullOrEmpty()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        dialog.show()
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


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            selectedImage?.let {
                Glide.with(this).load(it).into(addTaskDialogBinding.imageView)
                addTaskDialogBinding.imageView.tag = it
            }

            if (selectedImage != null) {
                viewModel.uploadImage(selectedImage)
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onTaskChecked(taskId: String, isCompleted: Boolean) {
        viewModel.onTaskChecked(taskId, isCompleted)
    }

    override fun onTaskClicked(taskId: String) {
        //Show Details In A Dialog Message Box
        viewModel.getTask(taskId)
        showTaskDetailDialog()
    }

    override fun onTaskSwiped(taskId: String) {
        viewModel.deleteTask(taskId)
    }

    override fun onTaskEdited(task: Task) {
        showUpdateTaskDialog(task)
    }


    private fun showUpdateTaskDialog(task: Task) {
        addTaskDialogBinding = AddTaskDailogBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this).setView(addTaskDialogBinding.root)
            .setTitle("Task Details").setPositiveButton("Yes") { _, _ ->
                if (task.title?.isNotEmpty()!!) {
                    viewModel.taskTitle.value = task.title
                    viewModel.taskDescription.value = task.description
                    viewModel.taskImageUrl.value = task.imageUrl
                    viewModel.onTaskEdited(task)
                }
            }.setNegativeButton("Not Yet", null).create()


        dialog.show()
    }

    private fun showTaskDetailDialog() {
        taskDetailDailogBinding = TaskDetailDailogBinding.inflate(layoutInflater)
        taskDetailDailogBinding.viewModel = viewModel
        val dialog = MaterialAlertDialogBuilder(this).setView(taskDetailDailogBinding.root)
            .setTitle("Task Details").setPositiveButton("Close") { _, _ ->
                val title = taskDetailDailogBinding.textView.text.toString().trim()
                val description = taskDetailDailogBinding.descriptionText.text.toString().trim()
                val imageUri = taskDetailDailogBinding.imageView2.tag as? Uri
                if (title.isNotEmpty()) {
                    viewModel.taskTitle.value = title
                    viewModel.taskDescription.value = description
                    viewModel.taskImageUrl.value = imageUri?.toString()
                }
            }.create()

        dialog.show()
    }

    // Set up the ItemTouchHelper for swipe-to-delete functionality
    private val itemTouchHelperCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Do nothing
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = viewModel.taskListAdapter.getItem(position)
                AlertDialog.Builder(viewHolder.itemView.context).setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        viewModel.taskListAdapter.getRef(position).removeValue()
                        viewModel.taskListAdapter.onSwipe(item)
                    }.setNegativeButton(android.R.string.no) { _, _ ->
                        viewModel.taskListAdapter.notifyItemChanged(position)
                    }.setIcon(android.R.drawable.ic_dialog_alert).show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val ICON_SIZE = 4
                val iconMargin = (itemHeight - ICON_SIZE) / 2
                val iconTop = itemView.top + (itemHeight - ICON_SIZE) / 2
                val iconBottom = iconTop + ICON_SIZE

                if (dX > 0) {
                    // Swiping to the right
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = iconLeft + ICON_SIZE
                    val background = ColorDrawable(Color.parseColor("#388E3C"))
                    background.setBounds(
                        itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom
                    )
                    background.draw(c)
                    val icon = ContextCompat.getDrawable(
                        itemView.context, android.R.drawable.ic_menu_delete
                    )
                    icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                } else {
                    // Swiping to the left
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = iconRight - ICON_SIZE
                    val background = ColorDrawable(Color.parseColor("#D32F2F"))
                    background.setBounds(
                        itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom
                    )
                    background.draw(c)
                    val icon = ContextCompat.getDrawable(
                        itemView.context, android.R.drawable.ic_menu_delete
                    )
                    icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                }

                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                )
            }
        }
}
