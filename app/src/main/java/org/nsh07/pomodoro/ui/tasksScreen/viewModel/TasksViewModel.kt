/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.tasksScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.data.Task
import org.nsh07.pomodoro.data.TaskRepository

class TasksViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    private val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val incompleteTasks: StateFlow<List<Task>> = allTasks
        .map { tasks -> tasks.filter { !it.isCompleted }.sortedBy { it.timestamp } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val completedTasks: StateFlow<List<Task>> = allTasks
        .map { tasks -> tasks.filter { it.isCompleted }.sortedByDescending { it.timestamp } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addTask(title: String) {
        viewModelScope.launch {
            taskRepository.insert(Task(title = title))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) {
                val completed = allTasks.value.filter { it.isCompleted }
                if (completed.size >= 5) {
                    val oldestCompleted = completed.minByOrNull { it.timestamp }
                    if (oldestCompleted != null) {
                        taskRepository.delete(oldestCompleted)
                    }
                }
            }
            taskRepository.update(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task)
        }
    }
}
