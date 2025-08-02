/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import android.content.Context

interface AppContainer {
    val preferenceRepository: PreferenceRepository
    val statRepository: StatRepository
    val timerRepository: TimerRepository
    val taskRepository: TaskRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val preferenceRepository: PreferenceRepository by lazy {
        AppPreferenceRepository(AppDatabase.getDatabase(context).preferenceDao())
    }

    override val statRepository: StatRepository by lazy {
        AppStatRepository(AppDatabase.getDatabase(context).statDao())
    }

    override val timerRepository: TimerRepository by lazy {
        AppTimerRepository()
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepository(AppDatabase.getDatabase(context).taskDao())
    }
}
