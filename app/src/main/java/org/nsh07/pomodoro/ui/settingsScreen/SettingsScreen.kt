/*
 * Copyright (c) 2025 Abhishek Kumar Maurya
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.WRShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.theme.ZingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val focusTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.focusTimeTextFieldState
    }
    val shortBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.shortBreakTimeTextFieldState
    }
    val longBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.longBreakTimeTextFieldState
    }

    val sessionsSliderState = rememberSaveable(
        saver = SliderState.Saver(
            viewModel.sessionsSliderState.onValueChangeFinished,
            viewModel.sessionsSliderState.valueRange
        )
    ) {
        viewModel.sessionsSliderState
    }

    // collect theme state from ViewModel
    val isSystemTheme = viewModel.isSystemTheme
    val isDarkTheme = viewModel.isDarkTheme
    SettingsScreen(
        focusTimeInputFieldState = focusTimeInputFieldState,
        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
        sessionsSliderState = sessionsSliderState,
        isSystemTheme = isSystemTheme,
        isDarkTheme = isDarkTheme,
        onSystemThemeChange = { viewModel.updateSystemTheme(it) },
        onDarkThemeChange = { viewModel.updateDarkTheme(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsScreen(
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    isSystemTheme: Boolean,
    isDarkTheme: Boolean,
    onSystemThemeChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTopBar,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceContainer),
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .background(colorScheme.surfaceContainer)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Focus",
                            style = typography.titleSmallEmphasized
                        )
                        MinuteInputField(
                            state = focusTimeInputFieldState,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                bottomStart = 16.dp,
                                topEnd = 4.dp,
                                bottomEnd = 4.dp
                            ),
                            imeAction = ImeAction.Next
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Short break",
                            style = typography.titleSmallEmphasized
                        )
                        MinuteInputField(
                            state = shortBreakTimeInputFieldState,
                            shape = RoundedCornerShape(4.dp),
                            imeAction = ImeAction.Next
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Long break",
                            style = typography.titleSmallEmphasized
                        )
                        MinuteInputField(
                            state = longBreakTimeInputFieldState,
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                bottomStart = 4.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp
                            ),
                            imeAction = ImeAction.Done
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
            }
            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.clocks),
                            null
                        )
                    },
                    headlineContent = {
                        Text("Session length")
                    },
                    supportingContent = {
                        Column {
                            Text("Focus intervals in one session: ${sessionsSliderState.value.toInt()}")
                            Slider(
                                state = sessionsSliderState,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier.clip(shapes.large)
                )
            }
            item {
                var expanded by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    FilledTonalIconToggleButton(
                        checked = expanded,
                        onCheckedChange = { expanded = it },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier.width(52.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.info),
                            null
                        )
                    }
                    AnimatedVisibility(expanded) {
                        Text(
                            "A \"session\" is a sequence of pomodoro intervals that contain focus" +
                                    " intervals, short break intervals, and a long break interval. The " +
                                    "last break of a session is always a long break.",
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            item {
                Text(
                    "Theme",
                    style = typography.titleMediumEmphasized,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            // Theme switches
            item {
                ListItem(
                    leadingContent = { Icon(
                            painterResource(R.drawable.contrast),
                            null
                        ) },
                    headlineContent = { Text("Follow system theme") },
                    trailingContent = {
                        Switch(
                            checked = isSystemTheme,
                            onCheckedChange = onSystemThemeChange,
                            colors = SwitchDefaults.colors(),
                            thumbContent = {
                                if (
                                    isSystemTheme
                                ) {
                                    Icon(
                                        painterResource(R.drawable.baseline_check),
                                        null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.clip(topListItemShape)
                )
            }
            //dark theme
            item {
                ListItem(
                    leadingContent = { Icon(
                        painterResource(R.drawable.dark_mode),
                        null
                    ) },
                    headlineContent = { Text("Dark theme") },
                    trailingContent = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onDarkThemeChange,
                            enabled = !isSystemTheme,
                            colors = SwitchDefaults.colors(),
                            thumbContent = {
                                if (
                                    isDarkTheme
                                ) {
                                    Icon(
                                        painterResource(R.drawable.baseline_check),
                                        null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.clip(bottomListItemShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun SettingsScreenPreview() {
    ZingTheme {
        SettingsScreen(
            focusTimeInputFieldState = rememberTextFieldState((25 * 60 * 1000).toString()),
            shortBreakTimeInputFieldState = rememberTextFieldState((5 * 60 * 1000).toString()),
            longBreakTimeInputFieldState = rememberTextFieldState((15 * 60 * 1000).toString()),
            sessionsSliderState = rememberSliderState(value = 3f, steps = 3, valueRange = 1f..5f),
            isSystemTheme = true,
            isDarkTheme = false,
            onSystemThemeChange = {},
            onDarkThemeChange = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
