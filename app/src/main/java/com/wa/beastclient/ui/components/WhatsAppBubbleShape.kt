package com.wa.beastclient.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Custom WhatsApp bubble shape supporting an 8.dp curved bezier tail on the leading cluster item.
 */
class WhatsAppBubbleShape(
    private val isFromMe: Boolean,
    private val showTail: Boolean
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (!showTail) {
            return Outline.Rounded(
                RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    cornerRadius = CornerRadius(with(density) { 13.dp.toPx() })
                )
            )
        }

        val tailW = with(density) { 8.dp.toPx() }
        val r = with(density) { 13.dp.toPx() }
        val w = size.width
        val h = size.height

        val path = Path().apply {
            if (isFromMe) {
                val bodyRight = (w - tailW).coerceAtLeast(r * 2f)
                moveTo(r, 0f)
                lineTo(w - 2f, 0f)
                quadraticTo(w + 2f, 0f, bodyRight, tailW * 1.4f)
                lineTo(bodyRight, h - r)
                quadraticTo(bodyRight, h, bodyRight - r, h)
                lineTo(r, h)
                quadraticTo(0f, h, 0f, h - r)
                lineTo(0f, r)
                quadraticTo(0f, 0f, r, 0f)
                close()
            } else {
                val bodyLeft = tailW.coerceAtMost(w * 0.25f)
                moveTo(2f, 0f)
                lineTo(w - r, 0f)
                quadraticTo(w, 0f, w, r)
                lineTo(w, h - r)
                quadraticTo(w, h, w - r, h)
                lineTo(bodyLeft + r, h)
                quadraticTo(bodyLeft, h, bodyLeft, h - r)
                lineTo(bodyLeft, tailW * 1.4f)
                quadraticTo(-2f, 0f, 2f, 0f)
                close()
            }
        }
        return Outline.Generic(path)
    }
}

val WhatsAppClusterInnerShape = RoundedCornerShape(12.dp)
