package com.example.util

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberSafePainterResource(@DrawableRes id: Int): Painter? {
    val context = LocalContext.current
    return remember(id, context) {
        try {
            val drawable = context.getDrawable(id)
            if (drawable is BitmapDrawable) {
                BitmapPainter(drawable.bitmap.asImageBitmap())
            } else if (drawable != null) {
                val bitmap = BitmapFactory.decodeResource(context.resources, id)
                if (bitmap != null) {
                    BitmapPainter(bitmap.asImageBitmap())
                } else null
            } else null
        } catch (e: Throwable) {
            android.util.Log.e("SafePainterResource", "Failed to load drawable $id", e)
            null
        }
    }
}
