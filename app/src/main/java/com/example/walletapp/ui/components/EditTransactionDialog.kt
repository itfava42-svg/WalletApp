package com.example.walletapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionCategories
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.suggestCategoryFromTitle
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppError
import com.example.walletapp.ui.theme.OnSurfaceVariant
import com.example.walletapp.ui.theme.AppTextStyles

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    var title by remember { mutableStateOf(transaction.title) }
    var selectedCategory by remember { mutableStateOf(transaction.category ?: transaction.iconKey) }
    var categoryManuallySet by remember { mutableStateOf(true) } // don't overwrite an existing category on open
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(transaction.tags) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categories = if (transaction.type == TransactionType.EXPENSE) TransactionCategories.expense else TransactionCategories.income

    fun addTagFromInput() {
        val cleaned = tagInput.trim().removePrefix("#")
        if (cleaned.isNotEmpty() && cleaned !in tags) {
            tags = tags + cleaned
        }
        tagInput = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش تراکنش", style = AppTextStyles.titleLg) },
        text = {
            Column {
                Text("عنوان", style = AppTextStyles.labelMd, color = OnSurfaceVariant)
                Spacer(Modifier.height(AppDimens.ItemGap))
                OutlinedTextField(
                    value = title,
                    onValueChange = { input ->
                        title = input
                        if (!categoryManuallySet) {
                            suggestCategoryFromTitle(input, transaction.type)?.let { selectedCategory = it }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(AppDimens.SectionGap))
                Text("دسته‌بندی", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    categories.forEach { cat ->
                        AppFilterChip(
                            selected = selectedCategory == cat.label,
                            onClick = {
                                selectedCategory = cat.label
                                categoryManuallySet = true
                            },
                            label = { Text(cat.label) },
                            modifier = Modifier.padding(end = AppDimens.IntroGap),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("هشتگ‌ها", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = { Text("مثلاً سفر — اینتر بزنید تا اضافه شود") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTagFromInput() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { tags = tags - tag },
                                label = { Text("#$tag") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "حذف",
                                        modifier = Modifier.size(AppDimens.IconSmall),
                                    )
                                },
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                AppTextButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = AppError, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(AppDimens.IntroGap))
                    Text("حذف تراکنش", color = AppError)
                }
            }
        },
        confirmButton = {
            AppTextButton(onClick = {
                addTagFromInput()
                onSave(
                    transaction.copy(
                        title = title.trim().ifBlank { transaction.title },
                        iconKey = selectedCategory,
                        category = selectedCategory,
                        tags = tags,
                        subtitle = rebuildSubtitle(transaction.subtitle, selectedCategory),
                    ),
                )
            }) { Text("ذخیره") }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف تراکنش؟") },
            text = { Text("این عمل قابل بازگشت نیست.") },
            confirmButton = {
                AppTextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(transaction)
                }) { Text("حذف", color = AppError) }
            },
            dismissButton = {
                AppTextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") }
            },
        )
    }
}

/** Replaces the leading "<category> • ..." part of a subtitle with the newly selected category, keeping the rest. */
private fun rebuildSubtitle(oldSubtitle: String, newCategory: String): String {
    val parts = oldSubtitle.split(" • ").toMutableList()
    if (parts.isEmpty()) return newCategory
    parts[0] = newCategory
    return parts.joinToString(" • ")
}
