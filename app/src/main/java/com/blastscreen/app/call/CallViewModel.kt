package com.blastscreen.app.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blastscreen.app.data.BackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CallUiState(
    val loading: Boolean = false,
    val backendStatus: String? = null,
    val roomUrl: String? = null,
    val error: String? = null
)

class CallViewModel(
    private val repository: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    fun findMatch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                val health = repository.checkHealth()
                val match = repository.createMatch(userId = UUID.randomUUID().toString())
                health.status to match.roomUrl
            }.onSuccess { (status, roomUrl) ->
                _uiState.value = CallUiState(
                    loading = false,
                    backendStatus = status,
                    roomUrl = roomUrl,
                    error = null
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = throwable.message ?: "Unknown error"
                )
            }
        }
    }
}
