/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.tasksScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }

    TasksScreenContent(
        tasks = tasks,
        newTaskTitle = newTaskTitle,
        onTaskTitleChange = { title -> newTaskTitle = title },
        onAddTask = { title ->
            viewModel.addTask(title)
            newTaskTitle = ""
        },
        onUpdateTask = viewModel::updateTask,
        onDeleteTask = viewModel::deleteTask,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TasksScreenContent(
    tasks: List<Task>,
    newTaskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .background(colorScheme.surfaceContainer)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
            }
            
            // Add task section
            item {
                Text(
                    "Add New Task",
                    style = typography.titleMediumEmphasized,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = onTaskTitleChange,
                        label = { Text("Task title") },
                        placeholder = { Text("Enter task description") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                onAddTask(newTaskTitle.trim())
                            }
                        },
                        enabled = newTaskTitle.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
            
            if (tasks.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tasks",
                        style = typography.titleMediumEmphasized,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                itemsIndexed(tasks) { index, task ->
                    TaskItem(
                        task = task,
                        onUpdate = onUpdateTask,
                        onDelete = onDeleteTask,
                        shape = when {
                            tasks.size == 1 -> shapes.large
                            index == 0 -> topListItemShape
                            index == tasks.lastIndex -> bottomListItemShape
                            else -> middleListItemShape
                        }
                    )
                }
            } else {
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
                }
            }
            
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clip(shape),
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
                    modifier = Modifier.size(25.dp)
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
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        trailingContent = {
            IconButton(
                onClick = { onDelete(task) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_close_24),
                    contentDescription = "Delete task",
                    tint = colorScheme.error
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TasksScreenPreview() {
    ZingTheme {
        TasksScreenContent(
            tasks = listOf(
                Task(id = 1, title = "Complete project documentation", isCompleted = false),
                Task(id = 2, title = "Review code changes", isCompleted = true),
                Task(id = 3, title = "Schedule team meeting", isCompleted = false),
                Task(id = 4, title = "Update dependencies", isCompleted = true),
                Task(id = 5, title = "Write unit tests", isCompleted = false)
            ),
            newTaskTitle = "",
            onTaskTitleChange = {},
            onAddTask = {},
            onUpdateTask = {},
            onDeleteTask = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}