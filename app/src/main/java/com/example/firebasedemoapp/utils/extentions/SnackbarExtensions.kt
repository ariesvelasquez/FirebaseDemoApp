package com.example.firebasedemoapp.utils.extentions

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

inline fun View.snack(@StringRes  messageRes: Int, length: Int = Snackbar.LENGTH_LONG, hasMargin: Boolean = true, f: Snackbar.() -> Unit) {
    snack(resources.getString(messageRes), length, hasMargin, f)
}

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, hasMargin: Boolean = true, f: Snackbar.() -> Unit) {
    val snack = Snackbar.make(this, message, length)
    snack.f()

    snack.show()
}

fun Snackbar.action(@StringRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
    action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}