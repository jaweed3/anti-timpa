package com.factlens.overlay

import android.content.Context
import android.view.View
import android.view.WindowManager

data class OverlayFabState(
    val container: View,
    val params: WindowManager.LayoutParams,
    val views: OverlayFabViews
)

class OverlayFabHelper(private val context: Context) {

    private var state: OverlayFabState? = null
    private var touchHandler: OverlayFabTouchHandler? = null
    var onTriggerCapture: (() -> Unit)? = null

    fun createOverlayView(): View {
        val fabViews = OverlayFabViewFactory.createOverlayView(context)
        val container = fabViews.container
        val params = OverlayFabViewFactory.createLayoutParams()

        val activator = OverlayFabActivator(context, fabViews.pulseView, fabViews.iconView)
        touchHandler = OverlayFabTouchHandler(context, activator)

        state = OverlayFabState(container, params, fabViews)
        return container
    }

    fun getLayoutParams(): WindowManager.LayoutParams? = state?.params

    fun getContainer(): View? = state?.container

    fun setupDraggable(view: View, params: WindowManager.LayoutParams) {
        touchHandler?.setupDraggable(view, params)
        touchHandler?.onTriggerCapture = onTriggerCapture
    }

    fun snapToEdge() {
        val s = state ?: return
        touchHandler?.snapToEdge(s.container, s.params)
    }

    fun destroy() {
        state?.views?.animator?.cancel()
        touchHandler?.destroy()
        state = null
        touchHandler = null
    }
}
