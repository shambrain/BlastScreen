package com.blastscreen.app

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.blastscreen.app.call.CallUiState
import com.blastscreen.app.call.CallViewModel
import com.blastscreen.app.recording.ScreenRecordService
import com.blastscreen.app.ui.BlastScreenTheme

class MainActivity : ComponentActivity() {
    private val callViewModel by viewModels<CallViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlastScreenTheme {
                BlastScreenApp(callViewModel = callViewModel)
            }
        }
    }
}

@Composable
private fun BlastScreenApp(callViewModel: CallViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val callState by callViewModel.uiState.collectAsState()

    val projectionManager = remember {
        context.getSystemService(MediaProjectionManager::class.java)
    }

    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    val audioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                projectionManager?.let {
                    startProjection(it)
                }
            }
        }
    )

    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
                action = ScreenRecordService.ACTION_START
                putExtra(ScreenRecordService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenRecordService.EXTRA_DATA_INTENT, result.data)
            }
            context.startForegroundService(serviceIntent)
        }
    }

    fun startProjection(manager: MediaProjectionManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        projectionLauncher.launch(manager.createScreenCaptureIntent())
    }

    fun startRecording() {
        audioPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun stopRecording() {
        val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_STOP
        }
        context.startService(serviceIntent)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                listOf("Home", "Record", "Call").forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTab(padding)
            1 -> RecordTab(
                padding = padding,
                onStartRecording = ::startRecording,
                onStopRecording = ::stopRecording
            )
            else -> CallTab(
                padding = padding,
                state = callState,
                onFindMatch = callViewModel::findMatch,
                onOpenRoom = { roomUrl ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, roomUrl.toUri()))
                }
            )
        }
    }
}

@Composable
private fun HomeTab(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("BlastScreen 3.0", style = MaterialTheme.typography.headlineMedium)
        Text("• MediaProjection based screen recording")
        Text("• Foreground service architecture for Android 14+")
        Text("• Backend-driven random room match API")
        Text("• Compose + ViewModel + Retrofit structure")
    }
}

@Composable
private fun RecordTab(
    padding: PaddingValues,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen Recorder")
        Button(onClick = onStartRecording) { Text("Start Recording") }
        Button(onClick = onStopRecording) { Text("Stop Recording") }
        Text("Saved to Movies/BlastScreen")
    }
}

@Composable
private fun CallTab(
    padding: PaddingValues,
    state: CallUiState,
    onFindMatch: () -> Unit,
    onOpenRoom: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Random Matchmaking")
        Button(onClick = onFindMatch, enabled = !state.loading) {
            Text("Find Random Match")
        }
        if (state.loading) CircularProgressIndicator()
        state.backendStatus?.let { Text("Backend: $it") }
        state.roomUrl?.let {
            Button(onClick = { onOpenRoom(it) }) { Text("Open Matched Room") }
            Text("Room: $it")
        }
        state.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
    }
}
