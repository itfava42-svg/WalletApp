package com.example.walletapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.sp
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

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = AppTextStyles.bodyMd.copy(fontSize = 15.sp),
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
        Text(text, style = AppTextStyles.headlineSm.copy(fontSize = 15.dp.value.sp))
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
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
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

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text("حساب‌های من", style = AppTextStyles.headlineSm)

            Row {
                IconButton(
                    onClick = {
                        if (selectedIndex > 0) {
                            selectedIndex--
                            coroutineScope.launch { listState.animateScrollToItem(selectedIndex) }
                            onCardSelected?.invoke(selectedIndex)
                        }
                    },
                    enabled = selectedIndex > 0,
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "حساب قبلی")
                }
                IconButton(
                    onClick = {
                        if (selectedIndex < items.lastIndex) {
                            selectedIndex++
                            coroutineScope.launch { listState.animateScrollToItem(selectedIndex) }
                            onCardSelected?.invoke(selectedIndex)
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
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            itemsIndexed(items) { index, item ->
                AppCard(
                    modifier = Modifier.width(292.dp),
                    containerColor = if (index == selectedIndex) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = if (index == selectedIndex) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    } else null,
                    onClick = {
                        selectedIndex = index
                        onCardSelected?.invoke(index)
                    },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            item.title,
                            style = AppTextStyles.headlineSm,
                            modifier = Modifier.weight(1f).padding(start = AppDimens.ItemGap),
                        )
                    }
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Text(
                        item.subtitle,
                        style = AppTextStyles.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(AppDimens.CompactPadding))
                    Text(
                        item.balance,
                        style = AppTextStyles.numeralXl,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
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
                Text(title, style = AppTextStyles.headlineSm.copy(fontSize = 15.sp))
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
) {
    AppCard(modifier = modifier) {
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
            .padding(horizontal = AppDimens.CardPadding, vertical = 12.dp),
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
