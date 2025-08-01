/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

fun millisecondsToStr(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(t),
        TimeUnit.MILLISECONDS.toSeconds(t) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun millisecondsToHours(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%dh",
        TimeUnit.MILLISECONDS.toHours(t)
    )
}

fun millisecondsToHoursMinutes(t: Long): String {
    require(t >= 0L)
    return String.format(
        Locale.getDefault(),
        "%dh %dm", TimeUnit.MILLISECONDS.toHours(t),
        TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    )
}

fun millisecondsToStopwatchStr(t: Long): String {
    require(t >= 0L)
    val hours = TimeUnit.MILLISECONDS.toHours(t)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(t) % TimeUnit.MINUTES.toSeconds(1)
    val centiseconds = (t % 1000) / 10
    
    return if (hours > 0) {
        String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours, minutes, seconds
        )
    } else {
        String.format(
            Locale.getDefault(),
            "%02d:%02d.%02d",
            minutes, seconds, centiseconds
        )
    }
}