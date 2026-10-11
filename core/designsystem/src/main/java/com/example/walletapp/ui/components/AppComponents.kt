package com.example.walletapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(AppDimens.CardPadding),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.CardRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content,
                )
            },
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.CardRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content,
                )
            },
        )
    }
}

@Composable
fun AppTile(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppDimens.TileRadius)
    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            content = {
                Row(
                    modifier = Modifier.padding(AppDimens.CompactPadding),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
                    content = content,
                )
            },
        )
    } else {
        androidx.compose.material3.Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            content = {
                Row(
                    modifier = Modifier.padding(AppDimens.CompactPadding),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
                    content = content,
                )
            },
        )
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(title, style = AppTextStyles.headlineSm)
        }
        action?.invoke()
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    centerAligned: Boolean = true,
    colors: androidx.compose.material3.TopAppBarColors =
        androidx.compose.material3.TopAppBarDefaults.topAppBarColors(),
    windowInsets: androidx.compose.foundation.layout.WindowInsets =
        androidx.compose.material3.TopAppBarDefaults.windowInsets,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior? = null,
) {
    if (centerAligned) {
        androidx.compose.material3.CenterAlignedTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
        )
    } else {
        androidx.compose.material3.TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(AppDimens.IconBox),
        enabled = enabled,
        content = content,
    )
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = AppTextStyles.titleMd,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(AppDimens.ButtonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(AppDimens.ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
        ),
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) Spacer(Modifier.width(AppDimens.ItemGap))
        Text(text, style = textStyle)
    }
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit,
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier.height(AppDimens.ButtonHeight),
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = contentColor),
        content = content,
    )
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(AppDimens.ButtonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(AppDimens.ButtonRadius),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) Spacer(Modifier.width(AppDimens.ItemGap))
        Text(text, style = AppTextStyles.titleMd)
    }
}


@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppDimens.SmallCardRadius),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
    )
}

@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(AppDimens.ButtonHeight),
        enabled = enabled,
        label = label,
    )
}


@Composable
fun AppAssistChip(
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        label = label,
    )
}

@Composable
fun AppSegmentedButton(
    label: String, selected: Boolean, selectedColor: Color, onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, modifier = modifier.height(AppDimens.TabHeight),
        shape = RoundedCornerShape(AppDimens.SmallCardRadius),
        color = if (selected) selectedColor else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Text(label, style = AppTextStyles.titleSm) }
    }
}

@Composable
fun AppSelectionTile(
    selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(onClick = onClick, shape = RoundedCornerShape(AppDimens.SmallCardRadius),
        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainer,
        border = if (selected) BorderStroke(1.5.dp, selectedColor) else null,
        modifier = modifier.height(AppDimens.SelectionTileHeight)) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center, content = content)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    readOnly: Boolean = false,
    placeholder: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = AppTextStyles.bodyMd,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(if (singleLine) Modifier.height(AppDimens.InputHeight) else Modifier),
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        readOnly = readOnly,
        placeholder = placeholder,
        suffix = suffix,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        minLines = minLines,
        maxLines = maxLines,
        label = if (label.isBlank()) null else ({ Text(label) }),
        shape = RoundedCornerShape(AppDimens.FieldRadius),
        textStyle = textStyle,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}


data class AppBankCardItem(
    val title: String,
    val subtitle: String,
    val balance: String,
    val icon: ImageVector,
)

@Composable
fun AppBankCardCarousel(
    items: List<AppBankCardItem>,
    modifier: Modifier = Modifier,
    onCardSelected: ((Int) -> Unit)? = null,
) {
    if (items.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Keep the highlighted account in sync with manual horizontal swipes as well
    // as the previous/next controls.
    androidx.compose.runtime.LaunchedEffect(listState, items.size) {
        androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { visibleIndex ->
                val safeIndex = visibleIndex.coerceIn(items.indices)
                if (safeIndex != selectedIndex) {
                    selectedIndex = safeIndex
                    onCardSelected?.invoke(safeIndex)
                }
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column {
                Text("حساب‌های من", style = AppTextStyles.headlineSm)
                Text(
                    "${items.size} حساب ثبت‌شده",
                    style = AppTextStyles.labelMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                AppIconButton(
                    onClick = {
                        if (selectedIndex > 0) {
                            val next = selectedIndex - 1
                            selectedIndex = next
                            coroutineScope.launch { listState.animateScrollToItem(next) }
                            onCardSelected?.invoke(next)
                        }
                    },
                    enabled = selectedIndex > 0,
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "حساب قبلی")
                }
                AppIconButton(
                    onClick = {
                        if (selectedIndex < items.lastIndex) {
                            val next = selectedIndex + 1
                            selectedIndex = next
                            coroutineScope.launch { listState.animateScrollToItem(next) }
                            onCardSelected?.invoke(next)
                        }
                    },
                    enabled = selectedIndex < items.lastIndex,
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "حساب بعدی")
                }
            }
        }

        Spacer(Modifier.height(AppDimens.ItemGap))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(end = AppDimens.ItemGap, start = AppDimens.ItemGap),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            itemsIndexed(items) { index, item ->
                val selected = index == selectedIndex
                AppCard(
                    modifier = Modifier.width(AppDimens.BankCardWidth),
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    onClick = {
                        selectedIndex = index
                        coroutineScope.launch { listState.animateScrollToItem(index) }
                        onCardSelected?.invoke(index)
                    },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(AppDimens.ItemGap))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = AppTextStyles.titleMd,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                item.subtitle,
                                style = AppTextStyles.labelMd,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                        if (selected) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(8.dp),
                            ) {}
                        }
                    }

                    Spacer(Modifier.height(AppDimens.CompactPadding))
                    Text(
                        "موجودی حساب",
                        style = AppTextStyles.labelMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.balance,
                        style = AppTextStyles.numeralXl,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            "حساب مالی",
                            style = AppTextStyles.labelMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${index + 1} از ${items.size}",
                            style = AppTextStyles.labelMd,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppListRow(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(AppDimens.SmallCardRadius),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = AppTextStyles.titleMd)
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = AppTextStyles.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun AppStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onClick: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        if (leadingIcon != null) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
            ) {
                leadingIcon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = AppTextStyles.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        value,
                        style = AppTextStyles.numeralXl,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!supportingText.isNullOrBlank()) {
                        Text(
                            supportingText,
                            style = AppTextStyles.labelMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            Text(
                title,
                style = AppTextStyles.bodyMd,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = AppTextStyles.numeralXl,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!supportingText.isNullOrBlank()) {
                Text(
                    supportingText,
                    style = AppTextStyles.labelMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun AppReportCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AppCard(modifier = modifier) {
        Text(title, style = AppTextStyles.headlineSm)
        content()
    }
}

@Composable
fun AppTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        AppTableRow(headers, header = true)
        rows.forEach { row -> AppTableRow(row) }
    }
}

@Composable
private fun AppTableRow(
    cells: List<String>,
    header: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (header) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
            )
            .padding(horizontal = AppDimens.CardPadding, vertical = AppDimens.TableRowVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
    ) {
        cells.forEach { cell ->
            Text(
                cell,
                modifier = Modifier.weight(1f),
                style = if (header) AppTextStyles.labelMd else AppTextStyles.bodyMd,
                color = if (header) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}


@Composable
fun AppIconBadge(icon: ImageVector, contentDescription: String? = null, modifier: Modifier = Modifier, containerColor: Color = MaterialTheme.colorScheme.primaryContainer, iconTint: Color = MaterialTheme.colorScheme.primary, onClick: (() -> Unit)? = null, size: Dp = AppDimens.BadgeSize, iconSize: Dp = AppDimens.BadgeIconSize) {
    val content: @Composable () -> Unit = { Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Icon(icon, contentDescription, tint = iconTint, modifier = Modifier.size(iconSize)) } }
    if (onClick == null) Surface(modifier = modifier.size(size), shape = MaterialTheme.shapes.medium, color = containerColor, content = content) else Surface(onClick = onClick, modifier = modifier.size(size), shape = MaterialTheme.shapes.medium, color = containerColor, content = content)
}


@Composable
fun AppEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppDimens.CardPadding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            if (icon != null) {
                AppIconBadge(icon = icon, size = AppDimens.IconBox, iconSize = AppDimens.Icon)
            }
            Text(title, style = AppTextStyles.titleMd)
            if (!supportingText.isNullOrBlank()) {
                Text(
                    supportingText,
                    style = AppTextStyles.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            action?.invoke()
        }
    }
    if (onClick != null) {
        AppCard(modifier = modifier, onClick = onClick, content = { content() })
    } else {
        AppCard(modifier = modifier, content = { content() })
    }
}

@Composable
fun AppLoadingState(
    message: String = "در حال بارگذاری…",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(AppDimens.CardPadding),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
    ) {
        CircularProgressIndicator()
        Text(message, style = AppTextStyles.bodyMd, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AppErrorState(
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    AppCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.errorContainer) {
        Text(message, style = AppTextStyles.bodyMd, color = MaterialTheme.colorScheme.onErrorContainer)
        action?.invoke()
    }
}

@Composable
fun AppStatusPill(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.SmallCardRadius),
        color = accentColor.copy(alpha = 0.08f),
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = AppDimens.CompactPadding,
                vertical = AppDimens.StatusPillVerticalPadding,
            ),
            contentAlignment = androidx.compose.ui.Alignment.CenterStart,
        ) {
            Text(text, style = AppTextStyles.labelMd, color = accentColor)
        }
    }
}
