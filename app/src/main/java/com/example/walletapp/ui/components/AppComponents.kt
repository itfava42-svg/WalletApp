package com.example.walletapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = {
            Column(
                modifier = Modifier.padding(AppDimens.CardPadding),
                content = content,
            )
        },
    )
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
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = AppTextStyles.headlineSm)
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
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(AppDimens.ButtonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(AppDimens.ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) Spacer(Modifier.width(AppDimens.ItemGap))
        Text(text, style = AppTextStyles.bodyMd.copy(fontSize = 15.sp))
    }
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
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(if (singleLine) Modifier.height(AppDimens.InputHeight) else Modifier),
        enabled = enabled,
        singleLine = singleLine,
        label = { Text(label) },
        shape = RoundedCornerShape(AppDimens.FieldRadius),
        textStyle = AppTextStyles.bodyMd,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
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
