
/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.nsh07.pomodoro.ZingApplication
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchViewModel
import org.nsh07.pomodoro.ui.tasksScreen.viewModel.TasksViewModel
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            SettingsViewModel(
                zingApplication().container.preferenceRepository,
                zingApplication().container.timerRepository
            )
        }
        initializer {
            StatsViewModel(
                zingApplication().container.statRepository
            )
        }
        initializer {
            TimerViewModel(
                zingApplication().container.preferenceRepository,
                zingApplication().container.statRepository,
                zingApplication().container.timerRepository
            )
        }
        initializer {
            StopwatchViewModel(
                zingApplication().container.statRepository,
                zingApplication().applicationContext
            )
        }
        initializer {
            TasksViewModel(
                zingApplication().container.taskRepository
            )
        }
    }
}

fun CreationExtras.zingApplication(): ZingApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ZingApplication)
