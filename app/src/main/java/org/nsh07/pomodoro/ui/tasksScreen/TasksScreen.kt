/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.tasksScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Task
import org.nsh07.pomodoro.ui.AppViewModelProvider
import org.nsh07.pomodoro.ui.tasksScreen.viewModel.TasksViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val tasks by viewModel.tasks.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        CenterAlignedTopAppBar(
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
            scrollBehavior = scrollBehavior
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                label = { Text("New Task") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addTask(newTaskTitle)
                        newTaskTitle = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onUpdate = viewModel::updateTask,
                    onDelete = viewModel::deleteTask
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onUpdate(task.copy(isCompleted = it)) }
        )
        Text(
            text = task.title,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
        IconButton(onClick = { onDelete(task) }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_check),
                contentDescription = "Delete Task"
            )
        }
    }
}
