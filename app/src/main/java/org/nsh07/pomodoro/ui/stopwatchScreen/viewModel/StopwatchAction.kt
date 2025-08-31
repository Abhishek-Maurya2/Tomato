/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.stopwatchScreen.viewModel

sealed interface StopwatchAction {
    data object ToggleStopwatch : StopwatchAction
    data object ResetStopwatch : StopwatchAction
    data object SaveAndResetStopwatch : StopwatchAction
    data object EnterAmbient : StopwatchAction
    data object ExitAmbient : StopwatchAction
}
