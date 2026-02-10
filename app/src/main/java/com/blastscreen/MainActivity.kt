package com.blastscreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blastscreen.admin.AdminTokenStore
import com.blastscreen.core.designsystem.BlastTheme
import com.blastscreen.data.BackendRepository
import com.blastscreen.firebase.FirebaseEventLogger
import com.blastscreen.recording.ScreenRecordService
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class MainUiState(
    val remainingMinutes: Int = 30,
    val sessionId: String? = null,
    val backendStatus: String = "idle",
    val isAdmin: Boolean = false,
    val error: String? = null,
    val layoutMode: Int = 0
)

class MainViewModel : ViewModel() {
    private val repo = BackendRepository()
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    fun fetchUsage(token: String) = viewModelScope.launch {
        runCatching { repo.usage(token) }
            .onSuccess { _state.value = _state.value.copy(remainingMinutes = it.remainingMinutes, backendStatus = "usage_ok") }
            .onFailure { _state.value = _state.value.copy(error = it.message, backendStatus = "usage_fail") }
    }

    fun joinQueue(token: String) = viewModelScope.launch {
        runCatching { repo.join(token) }
            .onSuccess { _state.value = _state.value.copy(sessionId = it.sessionId, backendStatus = it.status, error = null) }
            .onFailure { _state.value = _state.value.copy(error = it.message, backendStatus = "queue_fail") }
    }

    fun hiddenAdminProvision(username: String, store: AdminTokenStore) = viewModelScope.launch {
        runCatching { repo.adminProvision(username) }
            .onSuccess {
                store.save(it.accessToken)
                _state.value = _state.value.copy(isAdmin = true, backendStatus = "admin_ok", error = null)
            }
            .onFailure { _state.value = _state.value.copy(error = it.message, backendStatus = "admin_fail") }
    }

    fun cycleLayoutMode() {
        _state.value = _state.value.copy(layoutMode = (_state.value.layoutMode + 1) % 3)
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val logger = FirebaseEventLogger(FirebaseAnalytics.getInstance(this))
        logger.appOpen()
        Firebase.performance.isPerformanceCollectionEnabled = true

        setContent { BlastTheme { BlastScreenRoot(viewModel, logger) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlastScreenRoot(viewModel: MainViewModel, logger: FirebaseEventLogger) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var hiddenTap by remember { mutableIntStateOf(0) }
    val tokenStore = remember { AdminTokenStore(context) }
    val sessionToken = remember { mutableStateOf(UUID.randomUUID().toString()) }

    val projectionManager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val projectionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            context.startForegroundService(Intent(context, ScreenRecordService::class.java).apply {
                action = ScreenRecordService.ACTION_START
                putExtra(ScreenRecordService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenRecordService.EXTRA_DATA_INTENT, result.data)
            })
        }
    }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            listOf("Profile", "History", "Messages", "Billing", "Settings").forEach {
                NavigationDrawerItem(label = { Text(it) }, selected = false, onClick = {})
            }
            if (state.isAdmin) {
                NavigationDrawerItem(label = { Text("Admin Hidden Tools") }, selected = false, onClick = {})
            }
        }
    }) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("BlastScreen 3.0") }, actions = {
                Button(onClick = { scope.launch { drawerState.open() } }) { Text("Menu") }
            })
        }) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("One-tap random call", style = MaterialTheme.typography.titleLarge)
                Text("Remaining free minutes: ${state.remainingMinutes}")
                Text("Layout mode: ${listOf("Big", "Split", "Centered")[state.layoutMode]}")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        logger.startCall()
                        viewModel.joinQueue(sessionToken.value)
                    }) { Text("Tap to Random Call") }

                    Button(onClick = {
                        viewModel.cycleLayoutMode()
                    }) { Text("Toggle Layout") }
                }

                Button(onClick = { viewModel.fetchUsage(sessionToken.value) }) { Text("Refresh Minutes") }
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    projectionLauncher.launch(projectionManager?.createScreenCaptureIntent())
                }) { Text("Screen Record/Share") }

                Button(onClick = { context.startService(Intent(context, ScreenRecordService::class.java).apply { action = ScreenRecordService.ACTION_STOP }) }) {
                    Text("Stop Recording")
                }

                Button(onClick = {
                    state.sessionId?.let {
                        logger.matchFound()
                        context.startActivity(Intent(Intent.ACTION_VIEW, "https://meet.jit.si/$it".toUri()))
                    }
                }) { Text("Open Call Session") }

                Text("In-call chat overlay + reconnect cooldown are backend-controlled in the 3.0 flow.")
                state.error?.let {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException(it))
                }
                Text("Status: ${state.backendStatus}")

                Text("Admin trigger", modifier = Modifier.padding(top = 20.dp))
                Button(onClick = {
                    hiddenTap += 1
                    if (hiddenTap >= 7) {
                        hiddenTap = 0
                        val username = BuildConfig.ADMIN_PROVISION_USERNAME_1
                        viewModel.hiddenAdminProvision(username, tokenStore)
                    }
                }) { Text("●") }
            }
        }
    }
}

@Composable
private fun <T> StateFlow<T>.collectAsStateWithLifecycleCompat(): androidx.compose.runtime.State<T> {
    return androidx.compose.runtime.collectAsState(this)
}
