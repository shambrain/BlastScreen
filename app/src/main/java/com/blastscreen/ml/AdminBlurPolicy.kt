package com.blastscreen.ml

data class FrameDetections(
    val hasFace: Boolean,
    val hasHead: Boolean,
    val hasWaist: Boolean,
    val poseConfidence: Float
)

object AdminBlurPolicy {
    fun shouldBlur(isAdmin: Boolean, detections: FrameDetections): Boolean {
        if (!isAdmin || !detections.hasFace) return false
        val fullBodyFraming = detections.hasHead && detections.hasWaist && detections.poseConfidence >= 0.6f
        return fullBodyFraming
    }
}
