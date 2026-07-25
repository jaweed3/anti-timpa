package com.factlens.overlay

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.factlens.model.FlaggedItem

object OverlayCardViewFactory {

    fun createCard(context: Context, px: (Int) -> Int): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(0xFFFFFFFF.toInt())
            setCornerRadius(px(20).toFloat())
        }
        background = bg
        setPadding(px(20), px(20), px(20), px(20))
        elevation = px(8).toFloat()
    }

    fun getVerdictColor(verdict: String): Int = when (verdict.lowercase()) {
        "supported" -> 0xFF00AA44.toInt()
        "contradicted" -> 0xFFDD3333.toInt()
        else -> 0xFFFF8C00.toInt()
    }

    fun createBadge(context: Context, verdict: String, color: Int, px: (Int) -> Int) = createTextView(context) {
        val label = when (verdict.lowercase()) {
            "supported" -> "\u2705 Aman"
            "misleading" -> "\u26A0\uFE0F Mencurigakan"
            "contradicted" -> "\uD83D\uDEA8 Terindikasi Penipuan"
            else -> verdict
        }
        text = label; setTextColor(0xFFFFFFFF.toInt()); textSize = 12f; typeface = Typeface.DEFAULT_BOLD
        background = GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; setColor(color); setCornerRadius(px(6).toFloat()) }
        setPadding(px(8), px(4), px(8), px(4))
    }

    fun createConfidenceText(context: Context, confidence: Double, color: Int, px: (Int) -> Int) = createTextView(context) {
        text = "${(confidence * 100).toInt()}%"; textSize = 32f; typeface = Typeface.DEFAULT_BOLD; setTextColor(color)
        setPadding(0, px(8), 0, px(4))
    }

    fun createVerdictLabel(context: Context, verdict: String, px: (Int) -> Int) = createTextView(context) {
        text = verdict; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(0xFF191C20.toInt())
    }

    fun createFlaggedItemsView(context: Context, items: List<FlaggedItem>, px: (Int) -> Int): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, px(8), 0, 0)
        }

        createTextView(context) {
            text = "Item Terdeteksi:"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFDD3333.toInt())
            setPadding(0, 0, 0, px(4))
        }.also { container.addView(it) }

        items.forEach { item ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, px(2), 0, px(2))
            }

            val icon = when (item.type) {
                "account" -> "\uD83C\uDFE6"
                "phone" -> "\uD83D\uDCDE"
                "url" -> "\uD83D\uDD17"
                "pattern" -> "\u26A0\uFE0F"
                else -> "\u2022"
            }

            row.addView(TextView(context).apply {
                text = icon
                textSize = 13f
                setPadding(0, 0, px(4), 0)
            })

            row.addView(TextView(context).apply {
                text = "${item.value} — ${item.reason}"
                textSize = 13f
                setTextColor(0xFF5C6168.toInt())
            })

            container.addView(row)
        }

        return container
    }

    fun createExplanationView(context: Context, explanation: String, px: (Int) -> Int) = createTextView(context) {
        text = explanation; textSize = 14f; setTextColor(0xFF5C6168.toInt()); setPadding(0, px(8), 0, 0)
    }

    fun createSourcesText(context: Context, count: Int, px: (Int) -> Int) = createTextView(context) {
        text = "$count sources"; textSize = 12f; setTextColor(0xFF999999.toInt()); setPadding(0, px(8), 0, 0)
    }

    fun createButtonRow(context: Context, px: (Int) -> Int, onDismiss: () -> Unit, onViewDetails: () -> Unit) = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END; setPadding(0, px(12), 0, 0)
        addView(makeButton(context, "Dismiss", 0xFF5C6168.toInt(), 14f, false) { onDismiss() })
        addView(makeButton(context, "View Details", 0xFF00497D.toInt(), 14f, true) { onViewDetails() })
    }

    fun createWrapper(context: Context, card: View, px: (Int) -> Int, onDismiss: () -> Unit) = FrameLayout(context).apply {
        setBackgroundColor(0x4D000000.toInt())
        setOnClickListener { onDismiss() }
        addView(card, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }

    private fun createTextView(context: Context, init: TextView.() -> Unit) = TextView(context).apply(init)

    private fun makeButton(context: Context, text: String, color: Int, size: Float, bold: Boolean, onClick: () -> Unit) = Button(context).apply {
        this.text = text; setTextColor(color); this.textSize = size; background = null
        if (bold) typeface = Typeface.DEFAULT_BOLD
        setOnClickListener { onClick() }
    }
}
