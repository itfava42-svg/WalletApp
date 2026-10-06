package com.example.walletapp.ui.screens

import com.example.walletapp.ui.theme.AppDimens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.TransactionSyncViewModel
import com.example.walletapp.ui.components.TransactionSyncCard

/**
 * Standalone Sync screen.
 *
 * The ViewModel is supplied by the caller so this screen does not
 * require the lifecycle-viewmodel-compose dependency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSyncScreen(
    viewModel: TransactionSyncViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.state.collectAsState()

    val pendingCount by
        viewModel.pendingCount.collectAsState()

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("همگام‌سازی")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(AppDimens.PagePadding)
        ) {

            TransactionSyncCard(
                state = state,
                pendingCount = pendingCount,
                onSyncClick = {
                    viewModel.sync()
                }
            )
        }
    }
}

