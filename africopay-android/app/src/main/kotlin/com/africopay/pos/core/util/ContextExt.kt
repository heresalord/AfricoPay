package com.africopay.pos.core.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Unwraps a Compose [Context] (which may be a wrapper) down to its hosting [Activity], if any. */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
