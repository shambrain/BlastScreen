package com.blastscreen

import com.blastscreen.ml.AdminBlurPolicy
import com.blastscreen.ml.FrameDetections
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminBlurPolicyTest {
    @Test
    fun blurOnlyForAdminWithHeadToWaist() {
        assertTrue(AdminBlurPolicy.shouldBlur(true, FrameDetections(true, true, true, 0.9f)))
        assertFalse(AdminBlurPolicy.shouldBlur(true, FrameDetections(true, true, false, 0.9f)))
        assertFalse(AdminBlurPolicy.shouldBlur(false, FrameDetections(true, true, true, 0.9f)))
    }
}
