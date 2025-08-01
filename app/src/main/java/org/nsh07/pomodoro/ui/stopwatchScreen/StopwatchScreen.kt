/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.stopwatchScreen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.ZingTheme
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchAction
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StopwatchScreen(
    stopwatchState: StopwatchState,
    onAction: (StopwatchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    "Stopwatch",
                    style = TextStyle(
                        fontFamily = robotoFlexTopBar,
                        fontSize = 32.sp,
                        lineHeight = 32.sp,
                        color = colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(210.dp)
                )
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (stopwatchState.isRunning) {
                        // Animated LoadingIndicator when running
                        LoadingIndicator(
                            modifier = Modifier
                                .widthIn(max = 400.dp)
                                .fillMaxWidth(1.0f)
                                .aspectRatio(1f),
                            color = colorScheme.primary,
                        )
                    } else {
                        // Static Surface when stopped/paused
                        Surface(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            shape = CircleShape,
                            color = colorScheme.secondaryContainer,
                            tonalElevation = 2.dp
                        ) {}
                    }
                    
                    Text(
                        text = stopwatchState.timeStr,
                        style = TextStyle(
                            fontFamily = openRundeClock,
                            fontSize = 70.sp,
                            lineHeight = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stopwatchState.isRunning) colorScheme.onPrimary else colorScheme.onSecondaryContainer
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }


                val interactionSources = remember { List(2) { MutableInteractionSource() } }
               ButtonGroup(
                    overflowIndicator = { state ->
                        FilledTonalIconButton(
                            onClick = {
                                if (state.isExpanded) {
                                    state.dismiss()
                                } else {
                                    state.show()
                                }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = colorScheme.secondaryContainer
                            ),
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier
                                .size(64.dp, 96.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.more_vert_large),
                                contentDescription = "More",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    customItem(
                        {
                            FilledIconToggleButton(
                                onCheckedChange = { onAction(StopwatchAction.ToggleStopwatch) },
                                checked = stopwatchState.isRunning,
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    checkedContainerColor = colorScheme.primary,
                                    checkedContentColor = colorScheme.onPrimary
                                ),
                                shapes = IconButtonDefaults.toggleableShapes(),
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .size(width = 128.dp, height = 96.dp)
                                    .animateWidth(interactionSources[0])
                            ) {
                                if (stopwatchState.isRunning) {
                                    Icon(
                                        painterResource(R.drawable.pause_large),
                                        contentDescription = "Pause",
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        painterResource(R.drawable.play_large),
                                        contentDescription = "Start",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (stopwatchState.isRunning) {
                                        Icon(
                                            painterResource(R.drawable.pause),
                                            contentDescription = "Pause"
                                        )
                                    } else {
                                        Icon(
                                            painterResource(R.drawable.play),
                                            contentDescription = "Play"
                                        )
                                    }
                                },
                                text = { Text(if (stopwatchState.isRunning) "Pause" else "Start") },
                                onClick = {
                                    onAction(StopwatchAction.ToggleStopwatch)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = { onAction(StopwatchAction.ResetStopwatch) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorScheme.secondaryContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .size(96.dp)
                                    .animateWidth(interactionSources[1])
                            ) {
                                Icon(
                                    painterResource(R.drawable.restart_large),
                                    contentDescription = "Restart",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.restart),
                                        "Restart"
                                    )
                                },
                                text = { Text("Restart") },
                                onClick = {
                                    onAction(StopwatchAction.ResetStopwatch)
                                    state.dismiss()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(device = Devices.PHONE)
@Composable
private fun StopwatchScreenPreview() {
    ZingTheme {
        StopwatchScreen(
            stopwatchState = StopwatchState(
                elapsedTime = 75000L,
                timeStr = "01:15",
                isRunning = true
            ),
            onAction = {}
        )
    }
}
