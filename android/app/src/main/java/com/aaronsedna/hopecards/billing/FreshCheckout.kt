package com.aaronsedna.hopecards.billing

/** One user-initiated checkout. Product details are fetched anew and never cached here. */
internal class FreshCheckout<T>(private val onBusy: (Boolean) -> Unit) {
    private var active: Any? = null
    private var closed = false

    fun start(query: ((T?) -> Unit) -> Unit, canLaunch: () -> Boolean, launch: (T) -> Unit) {
        if (closed || active != null) return
        val request = Any()
        active = request
        onBusy(true)
        try {
            query { product ->
                if (active !== request || closed) return@query
                try {
                    if (product != null && canLaunch()) launch(product)
                } finally {
                    active = null
                    onBusy(false)
                }
            }
        } catch (error: Exception) {
            active = null
            onBusy(false)
            throw error
        }
    }

    fun close() {
        closed = true
        active = null
        onBusy(false)
    }
}
