package com.wavora.app.domain.repository.results

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
data class ScanResult(
    val added: Int,
    val updated: Int,
    val removed: Int,
) {
    val hasChanges: Boolean
        get() = added > 0 || updated > 0 || removed > 0
}