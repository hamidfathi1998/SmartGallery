package ir.hfathi.smart_gallery.feature_node.domain.util

sealed class OrderType {
    data object Ascending : OrderType()
    data object Descending : OrderType()
}
