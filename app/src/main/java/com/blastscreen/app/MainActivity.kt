package com.blastscreen.app

import android.Manifest
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.blastscreen.app.recording.ScreenRecordService
import com.blastscreen.app.ui.BlastScreenTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlastScreenTheme {
                BlastScreenApp()
            }
        }
    }
}

@Composable
private fun BlastScreenApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }

    val projectionManager = remember {
        context.getSystemService(MediaProjectionManager::class.java)
    }

    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
                action = ScreenRecordService.ACTION_START
                putExtra(ScreenRecordService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenRecordService.EXTRA_DATA_INTENT, result.data)
            }
            context.startForegroundService(serviceIntent)
            isRecording = true
        }
    }

    fun startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    fun stopRecording() {
        val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_STOP
        }
        context.startService(serviceIntent)
        isRecording = false
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
                isRecording = isRecording,
                onStartRecording = ::startRecording,
                onStopRecording = ::stopRecording
            )
            else -> CallTab(padding)
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
        Text("BlastScreen", style = MaterialTheme.typography.headlineMedium)
        Text("• One-tap screen recording with MediaProjection")
        Text("• Foreground service + Android 14/15 compatible flow")
        Text("• Random-call launcher backed by Jitsi Meet")
    }
}

@Composable
private fun RecordTab(
    padding: PaddingValues,
    isRecording: Boolean,
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
        Text(if (isRecording) "Recording in progress" else "Ready to record")
        Button(onClick = { if (isRecording) onStopRecording() else onStartRecording() }) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }
        Text("Videos are saved to Movies/BlastScreen as MP4.")
    }
}

@Composable
private fun CallTab(padding: PaddingValues) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Random Video Call")
        Button(onClick = {
            val room = "BlastScreen-${Random.nextInt(100000, 999999)}"
            val uri = android.net.Uri.parse("https://meet.jit.si/$room")
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }) {
            Text("Start Random Call")
        }
        Text("Opens a unique Jitsi room each tap.")
    }
}
