package com.example.walletapp.data

import android.content.Context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel bridge between the application UI and Sync service.
 *
 * No SMS, parser, receiver or financial calculation logic belongs here.
 */
class TransactionSyncViewModel(
    context: Context,
    private val service: TransactionSyncService =
        TransactionSyncService(
            context = context.applicationContext
        )
) : ViewModel() {

    private val _state =
        MutableStateFlow<TransactionSyncState>(
            service.state()
        )

    val state: StateFlow<TransactionSyncState> =
        _state.asStateFlow()

    private val _pendingCount =
        MutableStateFlow(
            service.pendingCount()
        )

    val pendingCount: StateFlow<Int> =
        _pendingCount.asStateFlow()

    /**
     * Refreshes the number of pending transactions.
     */
    fun refreshPendingCount() {
        _pendingCount.value =
            service.pendingCount()
    }

    /**
     * Runs one synchronization cycle.
     */
    fun sync(
        updatedAfter: Long = 0L
    ) {
        viewModelScope.launch {

            _state.value =
                TransactionSyncState.Syncing(
                    pendingCount =
                        service.pendingCount()
                )

            try {

                service.sync(
                    updatedAfter = updatedAfter
                )

                _state.value =
                    service.state()

                refreshPendingCount()

            } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {

                _state.value =
                    service.state()

                refreshPendingCount()
            }
        }
    }
}


