package ir.hfathi.smart_gallery.feature_node.presentation.util

import android.util.Size
import ir.hfathi.smart_gallery.BuildConfig

object MapBoxURL {

    private const val apiKey: String = BuildConfig.MAPS_TOKEN

    operator fun invoke(
        latitude: Double,
        longitude: Double,
        darkTheme: Boolean,
        zoom: Double = 11.21,
        size: Size = Size(300,200)
    ): String {
        val themeStyle = if (darkTheme) "dark-v10" else "outdoors-v11"
        return "https://api.mapbox.com/styles/v1/mapbox/" +
                "$themeStyle/static/pin-s+555555(" +
                "$longitude,$latitude)/$longitude,$latitude," +
                "$zoom,0/${size.width}x${size.height}@2x" +
                "?access_token=${apiKey}"
    }
}