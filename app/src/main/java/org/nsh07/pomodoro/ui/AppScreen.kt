/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.nsh07.pomodoro.MainActivity.Companion.screens
import org.nsh07.pomodoro.ui.settingsScreen.SettingsScreenRoot
import org.nsh07.pomodoro.ui.statsScreen.StatsScreenRoot
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.stopwatchScreen.StopwatchScreen
import org.nsh07.pomodoro.ui.stopwatchScreen.viewModel.StopwatchViewModel
import org.nsh07.pomodoro.ui.tasksScreen.TasksScreen
import org.nsh07.pomodoro.ui.timerScreen.TimerScreen
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    statsViewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    stopwatchViewModel: StopwatchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val remainingTime by timerViewModel.time.collectAsStateWithLifecycle()
    val stopwatchState by stopwatchViewModel.stopwatchState.collectAsStateWithLifecycle()

    val progress by rememberUpdatedState((uiState.totalTime.toFloat() - remainingTime) / uiState.totalTime)

    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val motionScheme = motionScheme

    LaunchedEffect(uiState.timerMode) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val backStack = rememberNavBackStack<Screen>(Screen.Stopwatch)
    var openAddTaskSheet: (() -> Unit)? by remember { mutableStateOf(null) }

    Scaffold { contentPadding ->
        Box(Modifier.fillMaxSize()) {
            NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.defaultEffectsSpec()),
                    fadeOut(motionScheme.defaultEffectsSpec())
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.defaultEffectsSpec()),
                    fadeOut(motionScheme.defaultEffectsSpec())
                )
            },
            predictivePopTransitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.defaultEffectsSpec()),
                    fadeOut(motionScheme.defaultEffectsSpec()) +
                            scaleOut(targetScale = 0.7f),
                )
            },
            entryProvider = entryProvider {
                entry<Screen.Stopwatch> {
                    StopwatchScreen(
                        stopwatchState = stopwatchState,
                        onAction = stopwatchViewModel::onAction,
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Timer> {
                    TimerScreen(
                        timerState = uiState,
                        progress = { progress },
                        onAction = timerViewModel::onAction,
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Settings> {
                    SettingsScreenRoot(
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Stats> {
                    StatsScreenRoot(
                        viewModel = statsViewModel,
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }

                entry<Screen.Tasks> {
                    TasksScreen(
                        showInternalFab = false, // We'll provide FAB via the floating toolbar
                        onProvideBottomSheetController = { opener ->
                            openAddTaskSheet = opener
                        },
                        modifier = modifier.padding(
                            start = contentPadding.calculateStartPadding(layoutDirection),
                            end = contentPadding.calculateEndPadding(layoutDirection),
                            bottom = contentPadding.calculateBottomPadding()
                        )
                    )
                }
            }
        )

            // Floating Action Toolbar for navigation (overlay)
            val isTasks = backStack.last() == Screen.Tasks
        if (isTasks) {
                HorizontalFloatingToolbar(
            expanded = true,
                    floatingActionButton = {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = { openAddTaskSheet?.invoke() }
                        ) {
                            Icon(
                                painter = painterResource(id = org.nsh07.pomodoro.R.drawable.baseline_add_24),
                                contentDescription = "Add task"
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .minimumInteractiveComponentSize()
                        .shadow(elevation = 92.dp, shape = MaterialTheme.shapes.extraLarge)
                        .zIndex(1f),
                    colors = FloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        toolbarContentColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        fabContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        fabContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                    floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.End,
                    content = {
                        screens.forEach { item ->
                            val selected = backStack.last() == item.route
                            FilledIconToggleButton(
                                checked = selected,
                                onCheckedChange = { _ ->
                                    if (item.route != Screen.Stopwatch) {
                                        if (backStack.size < 2) backStack.add(item.route)
                                        else backStack[1] = item.route
                                    } else {
                                        if (backStack.size > 1) backStack.removeAt(1)
                                    }
                                },
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.size(46.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Crossfade(targetState = selected, label = "icon_selection_fade") { isSelected ->
                                        Icon(
                                            painter = painterResource(id = if (isSelected) item.selectedIcon else item.unselectedIcon),
                                            contentDescription = item.label,
                                        )
                                        if (isSelected) {
                                            LaunchedEffect(Unit) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        else {
                HorizontalFloatingToolbar(
                    expanded = false,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(26.dp)
                        .minimumInteractiveComponentSize()
                        .shadow(elevation = 32.dp, shape = MaterialTheme.shapes.extraLarge)
                        .zIndex(1f),
                    colors = FloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        toolbarContentColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        fabContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        fabContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                    content = {
                        screens.forEach { item ->
                            val selected = backStack.last() == item.route
                            FilledIconToggleButton(
                                checked = selected,
                                onCheckedChange = { _ ->
                                    if (item.route != Screen.Stopwatch) {
                                        if (backStack.size < 2) backStack.add(item.route)
                                        else backStack[1] = item.route
                                    } else {
                                        if (backStack.size > 1) backStack.removeAt(1)
                                    }
                                },
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.size(46.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Crossfade(targetState = selected, label = "icon_selection_fade") { isSelected ->
                                        Icon(
                                            painter = painterResource(id = if (isSelected) item.selectedIcon else item.unselectedIcon),
                                            contentDescription = item.label,
                                        )
                                        if (isSelected) {
                                            LaunchedEffect(Unit) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
