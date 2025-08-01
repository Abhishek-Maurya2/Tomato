/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.stopwatchScreen.viewModel

data class StopwatchState(
    val elapsedTime: Long = 0L,
    val timeStr: String = "00:00.00",
    val isRunning: Boolean = false
)
