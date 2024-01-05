package ir.hfathi.smart_gallery.core.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import ir.hfathi.smart_gallery.R

@Composable
fun NavigationButton(
    albumId: Long,
    target: String?,
    navigateUp: () -> Unit,
    clearSelection: () -> Unit,
    selectionState: MutableState<Boolean>,
    alwaysGoBack: Boolean,
) {
    val isChildRoute = albumId != -1L || target != null
    val onClick: () -> Unit =
        if (isChildRoute && !selectionState.value) navigateUp
        else clearSelection
    val icon = if (isChildRoute && !selectionState.value) Icons.AutoMirrored.Filled.ArrowBack
    else Icons.Default.Close
    if (isChildRoute || selectionState.value || alwaysGoBack) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.back_cd)
            )
        }
    }
}