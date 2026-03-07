package com.wavora.app.core.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Base ViewModel providing:
 *  - A typed [UiState] via [StateFlow] (observed in Compose with collectAsStateWithLifecycle)
 *  - A one-shot [UiEvent] channel via [SharedFlow] (for Snackbars, navigation, toasts)
 *  - [safeLaunch] — a coroutine launcher that captures exceptions and emits error events
 *
 * Usage:
 * ```kotlin
 * class MyViewModel @Inject constructor(...) : BaseViewModel<MyUiState, MyUiEvent>(MyUiState()) {
 *
 *     fun doSomething() = safeLaunch {
 *         val result = repository.getData()
 *         updateState { copy(data = result) }
 *     }
 * }
 * ```
 */

abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {
    // State
    private val _uiState = MutableStateFlow(initialState)

    /**
     * The current screen state. Collect in Compose with:
     * ```kotlin
     * val state by viewModel.uiState.collectAsStateWithLifecycle()
     * ```
     */
    val uiState = _uiState.asStateFlow()

    // Current state value for synchronous reads inside the ViewModel.
    protected val currentState get() = _uiState.value

    // Atomically update [uiState] using a reducer lambda on the current state.
    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { it.reducer() }
    }

    // Events
    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 10, // prevent dropping events under load
    )

    /**
     * One-shot events (Snackbars, navigation triggers, toasts).
     * Collect in Compose with:
     * ```kotlin
     * LaunchedEffect(Unit) {
     *     viewModel.events.collect { event -> handleEvent(event) }
     * }
     * ```
     */
    val events = _events.asSharedFlow()

    // Emit a one-shot event to the UI. Fire-and-forget.
    protected fun emitEvent(event: Event) {
        viewModelScope.launch { _events.emit(event) }
    }

    // Coroutine helpers
    /**
     * Launches a coroutine on [dispatcher] that catches all [Throwable]s and
     * routes them to [onError]. Default dispatcher is [Dispatchers.IO] which
     * is appropriate for repository calls. UI updates are posted back to Main
     * automatically through StateFlow.
     */
    protected fun safeLaunch(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch(dispatcher) {
            runCatching { block() }
                .onFailure { onError(it) }
        }
    }

    /**
     * Default error handler. Override in subclasses to emit specific error events.
     * Base implementation logs the exception (safe for production — no PII logged).
     */
    protected open fun handleError(error: Throwable) {
        Log.e("WavoraViewModel", "Unhandled error in ${this::class.simpleName}", error)
    }
}