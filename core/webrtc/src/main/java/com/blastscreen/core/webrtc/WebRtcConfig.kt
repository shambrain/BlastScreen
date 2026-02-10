package com.blastscreen.core.webrtc

data class IceServerConfig(
    val url: String,
    val username: String? = null,
    val credential: String? = null
)

data class WebRtcSessionConfig(
    val sessionId: String,
    val iceServers: List<IceServerConfig>
)
