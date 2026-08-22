package com.example.walletapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.data.ToolAction
import com.example.walletapp.data.ToolItem
import com.example.walletapp.data.toolsGrid
import com.example.walletapp.data.toolsTopRow
import com.example.walletapp.ui.components.DashboardBottomBar
import com.example.walletapp.ui.components.appNavItems
import com.example.walletapp.ui.theme.OnSurface
import com.example.walletapp.ui.theme.OnSurfaceVariant
import com.example.walletapp.ui.theme.Primary
import kotlinx.coroutines.launch

// Soft pastel background colors for the tools grid, one per card, cycled if
// there are more cards than colors — matches the reference design's playful
// but gentle palette rather than the app's bold primary accent colors.
private val toolCardPalette = listOf(
    Color(0xFFDCEBFB), // soft blue
    Color(0xFFDBF3EA), // soft mint
    Color(0xFFDFF3E3), // soft green
    Color(0xFFE1F3E6), // soft green 2
    Color(0xFFD9E9FB), // soft sky blue
    Color(0xFFE7DEF6), // soft lavender
    Color(0xFFF1E0F3), // soft pink-purple
    Color(0xFFE3DFF6), // soft indigo
    Color(0xFFFBEAD2), // soft peach
)

private val toolIconPalette = listOf(
    Color(0xFF3A7CC7), Color(0xFF2FA37E), Color(0xFF3FA85C), Color(0xFF3FA173),
    Color(0xFF3A7CC7), Color(0xFF7C5CC4), Color(0xFFB2529C), Color(0xFF6B5CC4),
    Color(0xFFC48A2F),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    selectedNavIndex: Int,
    onNavSelect: (Int) -> Unit,
    onOpenSettingsList: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(text: String) {
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    fun handleClick(action: ToolAction, title: String) {
        when (action) {
            ToolAction.OPEN_SETTINGS_LIST -> onOpenSettingsList()
            else -> showMessage("$title — به‌زودی")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            DashboardBottomBar(items = appNavItems, selectedIndex = selectedNavIndex, onSelect = onNavSelect)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Top quick-access row: پروفایل / اشتراک / شخصی‌سازی / تنظیمات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                toolsTopRow.forEachIndexed { index, item ->
                    TopIconButton(
                        item = item,
                        background = toolCardPalette[index % toolCardPalette.size],
                        tint = toolIconPalette[index % toolIconPalette.size],
                        onClick = { handleClick(item.action, item.title) },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("همه ابزارها", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.Apps, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Primary),
                )
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                toolsGrid.forEachIndexed { index, item ->
                    // "قیمت‌ها" spans the full width, like the reference design.
                    val fullWidth = item.action == ToolAction.PRICES
                    item(
                        span = { GridItemSpan(if (fullWidth) maxLineSpan else 1) },
                    ) {
                        ToolGridCard(
                            item = item,
                            background = toolCardPalette[index % toolCardPalette.size],
                            iconTint = toolIconPalette[index % toolIconPalette.size],
                            onClick = { handleClick(item.action, item.title) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopIconButton(
    item: ToolItem,
    background: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            color = background,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(item.icon, contentDescription = item.title, tint = tint, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.title, style = MaterialTheme.typography.labelMedium, color = OnSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ToolGridCard(
    item: ToolItem,
    background: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = background,
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp),
            )
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}