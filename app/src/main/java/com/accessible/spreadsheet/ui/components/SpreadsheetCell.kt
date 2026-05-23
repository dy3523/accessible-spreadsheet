package com.accessible.spreadsheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.accessible.spreadsheet.model.CellData
import com.accessible.spreadsheet.model.CellType

/**
 * A single spreadsheet cell optimized for screen reader accessibility.
 * Each cell is a focusable, clickable element with full semantic information.
 */
@Composable
fun SpreadsheetCell(
    cell: CellData,
    isSelected: Boolean,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester()
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isFocused -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isFocused -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        cell.type == CellType.NUMERIC -> MaterialTheme.colorScheme.primary
        cell.type == CellType.FORMULA -> MaterialTheme.colorScheme.tertiary
        cell.type == CellType.BOOLEAN -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

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

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .fillMaxWidth()
            .background(backgroundColor)
            .border(1.dp, borderColor)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onCellClick() }
            .semantics(mergeDescendants = true) {
                contentDescription = "单元格 $position" +
                        ", ${cell.value.ifBlank { "空" }}" +
                        ", $typeLabel" +
                        if (cell.formula != null) ", 公式: ${cell.formula}" else ""
                role = Role.Button
                stateDescription = if (isSelected) "已选中" else ""
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = cell.value.ifBlank { "" },
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (cell.type == CellType.NUMERIC) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Row header showing row numbers - accessible for screen readers.
 */
@Composable
fun RowHeader(
    rowNumber: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .width(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .semantics {
                contentDescription = "第 ${rowNumber + 1} 行"
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${rowNumber + 1}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Column header showing column letters - accessible for screen readers.
 */
@Composable
fun ColumnHeader(
    colIndex: Int,
    modifier: Modifier = Modifier
) {
    val colLetter = getColumnName(colIndex)
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .semantics {
                contentDescription = "列 $colLetter"
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = colLetter,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Corner cell at top-left of the grid.
 */
@Composable
fun CornerHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .width(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .semantics {
                contentDescription = "表格角落"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getColumnName(col: Int): String {
    var result = ""
    var c = col
    while (c >= 0) {
        result = ('A' + c % 26) + result
        c = c / 26 - 1
    }
    return result
}
