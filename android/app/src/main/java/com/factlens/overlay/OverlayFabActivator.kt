package com.factlens.overlay

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.TextView

class OverlayFabActivator(private val context: Context, private val pulseView: View, private val iconView: TextView) {

    fun activate() {
        val gd = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0x4D00AA44.toInt())
        }
        pulseView.background = gd
        iconView.setTextColor(0xFF00AA44.toInt())
        vibrate()
    }

    fun deactivate() {
        val gd = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0x4D00497D.toInt())
        }
        pulseView.background = gd
        iconView.setTextColor(0xFFFFFFFF.toInt())
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibrator = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibrator?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (_: Exception) {}
    }
}
