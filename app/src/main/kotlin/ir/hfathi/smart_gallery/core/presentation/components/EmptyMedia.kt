package ir.hfathi.smart_gallery.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.ui.core.Icons
import ir.hfathi.smart_gallery.ui.core.icons.NoImage

@Composable
fun EmptyMedia(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.no_media_title),
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier
                .size(128.dp),
            imageVector = Icons.NoImage,
            contentDescription = stringResource(R.string.no_media_cd),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
    }
}