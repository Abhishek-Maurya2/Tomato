/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package org.nsh07.pomodoro.ui.stopwatchScreen

import androidx.compose.foundation.background
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.ZingTheme
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchAction
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchState

@ExperimentalMaterial3ExpressiveApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    stopwatchState: StopwatchState,
    onAction: (StopwatchAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    // Adjust system UI (fullscreen) and brightness when ambient changes (skip during preview)
    LaunchedEffect(stopwatchState.isAmbient) {
        if (!view.isInEditMode) {
            val activity = view.context as? android.app.Activity
            val window = activity?.window
            window?.let { w ->
                val controller = WindowCompat.getInsetsController(w, w.decorView)
                if (stopwatchState.isAmbient) {
                    controller.hide(
                        WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                    )
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    val lp = w.attributes
                    lp.screenBrightness = 0.02f
                    w.attributes = lp
                } else {
                    controller.show(
                        WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                    )
                    val lp = w.attributes
                    lp.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    w.attributes = lp
                }
            }
        }
    }

    Crossfade(targetState = stopwatchState.isAmbient, label = "ambientMode") { isAmbient ->
        if (isAmbient) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { onAction(StopwatchAction.ExitAmbient) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stopwatchState.timeStr,
                    style = TextStyle(
                        fontFamily = openRundeClock,
                        fontSize = if (stopwatchState.isOverAnHour) 68.sp else 96.sp,
                        lineHeight = if (stopwatchState.isOverAnHour) 68.sp else 96.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        } else {
            StopwatchNormalContent(stopwatchState, onAction, modifier)
        }
    }
}

@ExperimentalMaterial3ExpressiveApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StopwatchNormalContent(
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
                        .widthIn(max = 450.dp)
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1f)
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    if (stopwatchState.isRunning) {
                        LoadingIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.3f),
                            color = colorScheme.primary,
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(300.dp),
                            shape = CircleShape,
                            color = colorScheme.secondaryContainer,
                            tonalElevation = 3.dp
                        ) {}
                    }

                    if (stopwatchState.isOverAnHour) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stopwatchState.hoursMinutesStr,
                                style = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontSize = 80.sp,
                                    lineHeight = 76.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stopwatchState.isRunning) colorScheme.onPrimary else colorScheme.onSecondaryContainer
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = stopwatchState.secondsStr,
                                style = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontSize = 50.sp,
                                    lineHeight = 54.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stopwatchState.isRunning) colorScheme.onPrimary else colorScheme.onSecondaryContainer
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    } else {
                        Text(
                            text = stopwatchState.timeStr,
                            style = TextStyle(
                                fontFamily = openRundeClock,
                                fontSize = 85.sp,
                                lineHeight = 76.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stopwatchState.isRunning) colorScheme.onPrimary else colorScheme.onSecondaryContainer
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                val interactionSources = remember { List(3) { MutableInteractionSource() } }
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
                    modifier = Modifier.padding(bottom = 120.dp)
                ) {
                    // Play / Pause
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
                                            contentDescription = "Start"
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

                    // Reset
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
                                        contentDescription = "Restart"
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

                    // Ambient
                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = { onAction(StopwatchAction.EnterAmbient) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorScheme.secondaryContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[2],
                                modifier = Modifier
                                    .size(64.dp, 96.dp)
                                    .animateWidth(interactionSources[2])
                            ) {
                                Icon(
                                    painterResource(R.drawable.info),
                                    contentDescription = "Ambient Mode",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.info),
                                        contentDescription = "Ambient"
                                    )
                                },
                                text = { Text("Ambient Mode") },
                                onClick = {
                                    onAction(StopwatchAction.EnterAmbient)
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
                elapsedTime = 3675000L, // 1 hour, 1 minute, 15 seconds
                timeStr = "01:01:15",
                hoursMinutesStr = "01:01",
                secondsStr = "15",
                isOverAnHour = true,
                isRunning = true
            ),
            onAction = {}
        )
    }
}

@Preview(device = Devices.PHONE)
@Composable
private fun StopwatchScreenUnderHourPreview() {
    ZingTheme {
        StopwatchScreen(
            stopwatchState = StopwatchState(
                elapsedTime = 75000L, // 1 minute, 15 seconds
                timeStr = "01:15",
                hoursMinutesStr = "",
                secondsStr = "",
                isOverAnHour = false,
                isRunning = true
            ),
            onAction = {}
        )
    }
}
