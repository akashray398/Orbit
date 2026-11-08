package com.orbit.app.feature.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.components.OrbitLogo
import com.orbit.app.core.designsystem.components.OrbitLogoMode
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.VoiceInputState
import com.orbit.app.feature.chat.ui.components.ChatInputBar
import com.orbit.app.feature.chat.ui.components.ChatMessageItem
import com.orbit.app.feature.chat.ui.components.VoiceInputPanel
import com.orbit.app.feature.chat.ui.components.VoiceLanguageSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceInputState by viewModel.voiceInputState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current

    var showMenu by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val isEmpty = uiState.messages.isEmpty()
    val isVoiceActive = voiceInputState is VoiceInputState.Listening || voiceInputState is VoiceInputState.Preparing
    val voiceAmplitude = (voiceInputState as? VoiceInputState.Listening)?.amplitude ?: 0f
    val voicePartialText = (voiceInputState as? VoiceInputState.Listening)?.partialText ?: ""

    // Screen Keep Awake management
    DisposableEffect(isVoiceActive, uiState.keepScreenOn) {
        view.keepScreenOn = isVoiceActive && uiState.keepScreenOn
        onDispose {
            view.keepScreenOn = false
        }
    }

    val orbSize by animateDpAsState(
        targetValue = if (isEmpty) 130.dp else 40.dp,
        animationSpec = OrbitMotionTokens.bouncy(),
        label = "orbSizeAnimation"
    )

    // Status Subtitle
    val statusText = when (val state = uiState.agentState) {
        is AgentState.Idle -> if (isSpeaking) "Speaking..." else "Ready"
        is AgentState.Listening -> "Listening..."
        is AgentState.Thinking -> "Thinking..."
        is AgentState.Acting -> state.toolLabel
        is AgentState.Error -> "Connection issue"
    }

    // Auto-scroll logic
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleItem >= totalItems - 2
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    if (showLanguageSheet) {
        VoiceLanguageSheet(
            currentLocale = uiState.voiceLocale,
            spokenRepliesEnabled = uiState.spokenRepliesEnabled,
            autoSendVoice = uiState.autoSendVoice,
            keepScreenOn = uiState.keepScreenOn,
            onLocaleSelected = viewModel::setVoiceLocale,
            onSpokenRepliesToggled = viewModel::setSpokenRepliesEnabled,
            onAutoSendToggled = viewModel::setAutoSendVoice,
            onKeepScreenOnToggled = viewModel::setKeepScreenOn,
            onDismissRequest = { showLanguageSheet = false }
        )
    }

    OrbitBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    if (!uiState.isOnline) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(OrbitTheme.colors.warning)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Offline Mode - Agent responses limited", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }

                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    if (isSpeaking) viewModel.stopSpeaking()
                                }
                            ) {
                                if (!isEmpty) {
                                    AgentOrb(
                                        state = uiState.agentState,
                                        amplitude = voiceAmplitude,
                                        size = orbSize
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                } else {
                                    OrbitLogo(size = 32.dp, mode = OrbitLogoMode.Mark)
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Column {
                                    Text("Orbit", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = viewModel::toggleSpokenReplies) {
                                Icon(
                                    imageVector = if (uiState.spokenRepliesEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = if (uiState.spokenRepliesEnabled) "Disable spoken replies" else "Enable spoken replies",
                                    tint = if (uiState.spokenRepliesEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Voice & Speech Settings") },
                                    onClick = {
                                        showMenu = false
                                        showLanguageSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Conversation") },
                                    onClick = {
                                        viewModel.clearConversation()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add 300 Sample Messages") },
                                    onClick = {
                                        viewModel.addSampleMessages(300)
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        showMenu = false
                                        onSettingsClick()
                                    }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            bottomBar = {
                if (isVoiceActive) {
                    VoiceInputPanel(
                        amplitude = voiceAmplitude,
                        partialText = voicePartialText,
                        currentLocale = uiState.voiceLocale,
                        isSlideToCancel = false,
                        onCancelClick = viewModel::cancelVoiceInput,
                        onStopClick = viewModel::stopVoiceInput,
                        onLanguageClick = { showLanguageSheet = true }
                    )
                } else {
                    ChatInputBar(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputTextChange,
                        onSendClick = { viewModel.sendMessage() },
                        onStopClick = { viewModel.cancelStreaming() },
                        onMicClick = viewModel::startVoiceInput,
                        isStreaming = uiState.isStreaming
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isEmpty) {
                    // Empty Conversation Hero Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AgentOrb(
                            state = uiState.agentState,
                            amplitude = voiceAmplitude,
                            size = orbSize,
                            modifier = Modifier.clickable {
                                if (isSpeaking) viewModel.stopSpeaking()
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Hello! How can Orbit help you today?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.suggestionChips.forEach { chip ->
                                OrbitChip(
                                    label = chip.label,
                                    onClick = { viewModel.sendMessage(chip.promptText) }
                                )
                            }
                        }
                    }
                } else {
                    // Active Conversation List
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { msg ->
                                ChatMessageItem(
                                    message = msg,
                                    onRetry = { viewModel.retryMessage(it) },
                                    onEdit = { viewModel.onInputTextChange(it) }
                                )
                            }

                            // Suggestions underneath last agent message
                            if (!uiState.isStreaming) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        uiState.suggestionChips.take(3).forEach { chip ->
                                            OrbitChip(
                                                label = chip.label,
                                                onClick = { viewModel.sendMessage(chip.promptText) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Scroll-To-Latest Button
                    AnimatedVisibility(
                        visible = !isAtBottom,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(uiState.messages.size - 1)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom")
                        }
                    }
                }
            }
        }
    }
}
