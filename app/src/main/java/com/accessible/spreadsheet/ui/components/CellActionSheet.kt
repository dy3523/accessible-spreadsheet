package com.accessible.spreadsheet.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.accessible.spreadsheet.model.CellData
import com.accessible.spreadsheet.model.CellType

/**
 * Bottom sheet dialog for cell actions, fully accessible.
 * Actions: Edit, Copy, View Properties
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellActionSheet(
    cell: CellData,
    editValue: String,
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onEditValueChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Cell info header
            val colLetter = getColumnName(cell.col)
            val position = "${colLetter}${cell.row + 1}"
            val typeLabel = when (cell.type) {
                CellType.NUMERIC -> "数值"
                CellType.STRING -> "文本"
                CellType.BOOLEAN -> "布尔值"
                CellType.FORMULA -> "公式"
                CellType.DATE -> "日期"
                CellType.EMPTY -> "空"
            }

            Text(
                text = "单元格 $position",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics {
                    contentDescription = "当前操作: 单元格 $position, 类型 $typeLabel"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current value display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "当前值",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cell.value.ifBlank { "(空)" },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            contentDescription = "当前值: ${cell.value.ifBlank { "空" }}"
                        }
                    )
                    if (cell.formula != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "公式: ${cell.formula}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.semantics {
                                contentDescription = "公式: ${cell.formula}"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
                // Edit mode
                OutlinedTextField(
                    value = editValue,
                    onValueChange = onEditValueChange,
                    label = { Text("编辑单元格值") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "编辑单元格 $position 的值"
                        },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.semantics {
                            contentDescription = "取消编辑"
                        }
                    ) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            onSaveEdit()
                            Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "保存编辑"
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存")
                    }
                }
            } else {
                // Action buttons
                // Edit button
                FilledTonalButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "编辑单元格 $position 的值"
                        }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑单元格")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy value button
                FilledTonalButton(
                    onClick = {
                        copyToClipboard(context, cell.value)
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "复制单元格 $position 的值: ${cell.value.ifBlank { "空" }}"
                        }
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("复制值")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy position button
                FilledTonalButton(
                    onClick = {
                        copyToClipboard(context, position)
                        Toast.makeText(context, "已复制位置", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "复制单元格位置 $position"
                        }
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("复制位置")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // View properties button
                var showProperties by remember { mutableStateOf(false) }

                FilledTonalButton(
                    onClick = { showProperties = !showProperties },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "查看单元格 $position 的属性"
                        }
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("查看属性")
                }

                if (showProperties) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = buildString {
                                    append("单元格属性: ")
                                    append("位置 $position, ")
                                    append("类型 $typeLabel, ")
                                    append("值 ${cell.value.ifBlank { "空" }}")
                                    if (cell.formula != null) append(", 公式 ${cell.formula}")
                                    if (cell.format != null) append(", 格式 ${cell.format}")
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            PropertyRow("位置", position)
                            PropertyRow("类型", typeLabel)
                            PropertyRow("值", cell.value.ifBlank { "(空)" })
                            if (cell.formula != null) {
                                PropertyRow("公式", cell.formula)
                            }
                            if (cell.format != null) {
                                PropertyRow("格式", cell.format)
                            }
                            PropertyRow("行", "${cell.row + 1}")
                            PropertyRow("列", "${cell.col + 1}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Close button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "关闭"
                    }
            ) {
                Text("关闭")
            }
        }
    }
}

@Composable
private PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("spreadsheet_cell", text)
    clipboard.setPrimaryClip(clip)
}
