package ir.hfathi.smart_gallery.feature_node.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import ir.hfathi.smart_gallery.R

fun Context.launchMap(lat: Double, lang: Double) {
    startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=$lat,$lang(${getString(R.string.media_location)})")
        }
    )
}