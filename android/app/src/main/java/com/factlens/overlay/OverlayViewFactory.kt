package com.factlens.overlay

import android.content.Context
import android.view.View
import com.factlens.model.Source

object OverlayViewFactory {

    fun createResultOverlayView(
        context: Context,
        explanation: String,
        verdict: String,
        confidence: Double,
        sources: List<Source>,
        onDismiss: () -> Unit,
        onViewDetails: () -> Unit
    ): View {
        val density = context.resources.displayMetrics.density
        val px = { v: Int -> (v * density).toInt() }

        val card = OverlayCardViewFactory.createCard(context, px)
        val verdictColor = OverlayCardViewFactory.getVerdictColor(verdict)

        card.addView(OverlayCardViewFactory.createBadge(context, verdict, verdictColor, px))
        card.addView(OverlayCardViewFactory.createConfidenceText(context, confidence, verdictColor, px))
        card.addView(OverlayCardViewFactory.createVerdictLabel(context, verdict, px))
        if (explanation.isNotEmpty()) {
            card.addView(OverlayCardViewFactory.createExplanationView(context, explanation, px))
        }
        card.addView(OverlayCardViewFactory.createSourcesText(context, sources.size, px))
        card.addView(OverlayCardViewFactory.createButtonRow(context, px, onDismiss, onViewDetails))

        return OverlayCardViewFactory.createWrapper(context, card, px, onDismiss)
    }
}
