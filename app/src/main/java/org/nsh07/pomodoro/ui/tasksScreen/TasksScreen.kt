/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.tasksScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Task
import org.nsh07.pomodoro.ui.AppViewModelProvider
import org.nsh07.pomodoro.ui.tasksScreen.viewModel.TasksViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.theme.ZingTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory),
    showInternalFab: Boolean = true,
    onProvideBottomSheetController: ((open: () -> Unit) -> Unit)? = null,
) {
    val incompleteTasks by viewModel.incompleteTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    TasksScreenContent(
        incompleteTasks = incompleteTasks,
        completedTasks = completedTasks,
        newTaskTitle = newTaskTitle,
        onTaskTitleChange = { title -> newTaskTitle = title },
        onAddTask = { title ->
            if (editingTask != null) {
                viewModel.updateTask(editingTask!!.copy(title = title))
                editingTask = null
            } else {
                viewModel.addTask(title)
            }
            newTaskTitle = ""
        },
        onUpdateTask = viewModel::updateTask,
        onDeleteTask = viewModel::deleteTask,
        onStartEditing = { task ->
            editingTask = task
            newTaskTitle = task.title
        },
        onStopEditing = {
            editingTask = null
            newTaskTitle = ""
        },
        isEditing = editingTask != null,
    modifier = modifier,
    showInternalFab = showInternalFab,
    onProvideBottomSheetController = onProvideBottomSheetController
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class
)
@Composable
private fun TasksScreenContent(
    incompleteTasks: List<Task>,
    completedTasks: List<Task>,
    newTaskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onStartEditing: (Task) -> Unit,
    onStopEditing: () -> Unit,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    showInternalFab: Boolean = true,
    onProvideBottomSheetController: ((open: () -> Unit) -> Unit)? = null,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var completedTasksVisible by remember { mutableStateOf(false) }

    if (isEditing) {
        showBottomSheet = true
    }

    // Provide the opener back up to AppScreen so a toolbar FAB can trigger it
    LaunchedEffect(Unit) {
        onProvideBottomSheetController?.invoke { showBottomSheet = true }
    }


    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tasks",
                        style = LocalTextStyle.current.copy(
                            fontFamily = robotoFlexTopBar,
                            fontSize = 32.sp,
                            lineHeight = 32.sp
                        )
                    )
                },
                subtitle = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceContainer),
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (showInternalFab) {
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 80.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Add task"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.surfaceContainer)
                .padding(paddingValues)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(12.dp))
                }

                if (incompleteTasks.isNotEmpty()) {
                    item {
                        Text(
                            "Tasks",
                            style = typography.titleMediumEmphasized,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    itemsIndexed(incompleteTasks.reversed()) { index, task ->
                        TaskItem(
                            task = task,
                            onUpdate = onUpdateTask,
                            onDelete = onDeleteTask,
                            onEdit = { onStartEditing(task) },
                            shape = when {
                                incompleteTasks.size == 1 -> shapes.large
                                index == 0 -> topListItemShape
                                index == incompleteTasks.lastIndex -> bottomListItemShape
                                else -> middleListItemShape
                            }
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { completedTasksVisible = !completedTasksVisible }
                        ) {
                            Text(
                                "Completed",
                                style = typography.titleMediumEmphasized,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (completedTasksVisible) "Collapse" else "Expand",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    if (completedTasksVisible) {
                        itemsIndexed(completedTasks.reversed()) { index, task ->
                            TaskItem(
                                task = task,
                                onUpdate = onUpdateTask,
                                onDelete = onDeleteTask,
                                onEdit = { onStartEditing(task) },
                                shape = when {
                                    completedTasks.size == 1 -> shapes.large
                                    index == 0 -> topListItemShape
                                    index == completedTasks.lastIndex -> bottomListItemShape
                                    else -> middleListItemShape
                                }
                            )
                        }
                    }
                }

                if (incompleteTasks.isEmpty() && completedTasks.isEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "No tasks yet",
                            style = typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Tap the + button to add your first task",
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(86.dp))
                }
            }
        }

        // Bottom Sheet for adding tasks
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    onStopEditing()
                },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                AddTaskBottomSheetContent(
                    taskTitle = newTaskTitle,
                    isEditing = isEditing,
                    onTaskTitleChange = onTaskTitleChange,
                    onAddTask = { title ->
                        onAddTask(title)
                        showBottomSheet = false
                    },
                    onCancel = {
                        showBottomSheet = false
                        onStopEditing()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .clip(shape)
            .combinedClickable(
                onClick = { onEdit() },
                onLongClick = { showContextMenu = true }
            ),
        leadingContent = {
            if (task.isCompleted) {
                IconButton(
                    onClick = { onUpdate(task.copy(isCompleted = !task.isCompleted)) },
                    modifier = Modifier.size(25.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check),
                        contentDescription = "Mark as incomplete",
                        tint = colorScheme.primary
                    )
                }
            } else {
                OutlinedIconButton(
                    onClick = { onUpdate(task.copy(isCompleted = !task.isCompleted)) },
                    modifier = Modifier.size(22.dp)
                ) {
                    // Empty content for outlined button when task is not completed
                }
            }
        },
        headlineContent = {
            Text(
                text = task.title,
                style = if (task.isCompleted) {
                    typography.bodyLarge.copy(
                        textDecoration = TextDecoration.LineThrough,
                        color = colorScheme.onSurfaceVariant
                    )
                } else {
                    typography.bodyLarge
                },
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        trailingContent = {
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete(task)
                        showContextMenu = false
                    }
                )
            }
        }
    )
}

@Composable
fun AddTaskBottomSheetContent(
    taskTitle: String,
    isEditing: Boolean,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isEditing) "Edit Task" else "Add New Task",
            style = typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = taskTitle,
            onValueChange = onTaskTitleChange,
            label = { Text("New Task") },
            placeholder = { Text("Enter task description") },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surfaceContainerLow,
                unfocusedContainerColor = colorScheme.surfaceContainerLow,
                disabledContainerColor = colorScheme.surfaceContainerHigh
            ),
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            Button(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onAddTask(taskTitle.trim())
                    }
                },
                enabled = taskTitle.isNotBlank()
            ) {
                Text(if (isEditing) "Update Task" else "Add Task")
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class
)
@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TasksScreenPreview() {
    ZingTheme {
        TasksScreenContent(
            incompleteTasks = listOf(
                Task(id = 1, title = "Complete project documentation", isCompleted = false),
                Task(id = 2, title = "Review code changes", isCompleted = false)
            ),
            completedTasks = listOf(
                Task(id = 3, title = "Schedule team meeting", isCompleted = true),
                Task(id = 4, title = "Update dependencies", isCompleted = true),
                Task(id = 5, title = "Write unit tests", isCompleted = true)
            ),
            newTaskTitle = "",
            onTaskTitleChange = {},
            onAddTask = {},
            onUpdateTask = {},
            onDeleteTask = {},
            onStartEditing = {},
            onStopEditing = {},
            isEditing = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TasksScreenEmptyPreview() {
    ZingTheme {
        TasksScreenContent(
            incompleteTasks = emptyList(),
            completedTasks = emptyList(),
            newTaskTitle = "",
            onTaskTitleChange = {},
            onAddTask = {},
            onUpdateTask = {},
            onDeleteTask = {},
            onStartEditing = {},
            onStopEditing = {},
            isEditing = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}
