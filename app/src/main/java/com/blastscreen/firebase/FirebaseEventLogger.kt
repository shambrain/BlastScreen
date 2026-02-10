package com.blastscreen.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseEventLogger(private val analytics: FirebaseAnalytics) {
    fun appOpen() = analytics.logEvent("app_open", null)
    fun startCall() = analytics.logEvent("start_call", null)
    fun matchFound() = analytics.logEvent("match_found", null)
    fun callDrop(reason: String) = analytics.logEvent("call_drop_reason", Bundle().apply { putString("reason", reason) })
    fun purchaseAttempt(productId: String) = analytics.logEvent("purchase_attempt", Bundle().apply { putString("product", productId) })
    fun purchaseSuccess(productId: String) = analytics.logEvent("purchase_success", Bundle().apply { putString("product", productId) })
}
