package com.accessible.spreadsheet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.accessible.spreadsheet.data.SpreadsheetViewModel
import com.accessible.spreadsheet.model.CellData
import com.accessible.spreadsheet.model.WorkbookData
import com.accessible.spreadsheet.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetScreen(
    viewModel: SpreadsheetViewModel,
    initialUri: Uri? = null,
    initialFileName: String? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it) ?: "未知文件"
            scope.launch {
                viewModel.loadFile(context, it, fileName)
            }
        }
    }

    // Load initial file if provided via intent
    LaunchedEffect(initialUri) {
        initialUri?.let { uri ->
            val fileName = initialFileName ?: getFileNameFromUri(context, uri) ?: "未知文件"
            viewModel.loadFile(context, uri, fileName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.fileName.ifBlank { "无障碍表格" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            contentDescription = "当前文件: ${uiState.fileName.ifBlank { "无障碍表格" }}"
                        }
                    )
                },
                navigationIcon = {
                    if (uiState.workbook != null) {
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.semantics {
                                contentDescription = "返回"
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    // Open file button
                    IconButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel.sheet.macroEnabled.12"
                                )
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "打开Excel文件"
                        }
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                    }

                    // Info button
                    uiState.workbook?.let { workbook ->
                        IconButton(
                            onClick = { /* Show file info */ },
                            modifier = Modifier.semantics {
                                contentDescription = "文件信息: ${workbook.fileName}, ${workbook.sheetCount}个工作表"
                            }
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                )
                            )
                        },
                        onDismiss = { viewModel.clearError() }
                    )
                }
                uiState.workbook == null -> {
                    WelcomeState(
                        onOpenFile = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel.sheet.macroEnabled.12"
                                )
                            )
                        }
                    )
                }
                else -> {
                    SpreadsheetContent(
                        workbook = uiState.workbook!!,
                        currentSheetIndex = uiState.currentSheetIndex,
                        selectedCellRow = uiState.selectedCellRow,
                        selectedCellCol = uiState.selectedCellCol,
                        onCellClick = { row, col -> viewModel.selectCell(row, col) },
                        onSheetSelect = { viewModel.selectSheet(it) },
                        getCellDescription = { row, col -> viewModel.getCellDescription(row, col) }
                    )
                }
            }
        }

        // Cell action bottom sheet
        if (uiState.showCellDialog && uiState.selectedCell != null) {
            CellActionSheet(
                cell = uiState.selectedCell!!,
                editValue = uiState.editValue,
                isEditMode = uiState.isEditMode,
                onEdit = { viewModel.startEdit() },
                onEditValueChange = { viewModel.updateEditValue(it) },
                onSaveEdit = { viewModel.saveEdit() },
                onCancelEdit = { viewModel.cancelEdit() },
                onDismiss = { viewModel.dismissDialog() }
            )
        }
    }
}

@Composable
private fun WelcomeState(onOpenFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.TableChart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "无障碍表格查看器",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics {
                heading()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "支持 .xls 和 .xlsx 格式的 Excel 文件\n完全适配屏幕阅读器",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                contentDescription = "支持 xls 和 xlsx 格式的 Excel 文件，完全适配屏幕阅读器"
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(
            onClick = onOpenFile,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "打开Excel文件"
                }
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("打开 Excel 文件")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "使用提示",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                AccessibilityTip("点击单元格可查看操作选项")
                AccessibilityTip("支持编辑、复制和查看属性")
                AccessibilityTip("使用屏幕阅读器手势在单元格间导航")
                AccessibilityTip("从其他应用也可直接打开 Excel 文件")
            }
        }
    }
}

@Composable
private fun AccessibilityTip(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics {
                contentDescription = "正在加载文件，请稍候"
            }
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在加载文件...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "加载失败",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                contentDescription = "错误信息: $error"
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    contentDescription = "忽略错误"
                }
            ) {
                Text("忽略")
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalButton(
                onClick = onRetry,
                modifier = Modifier.semantics {
                    contentDescription = "重新选择文件"
                }
            ) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun SpreadsheetContent(
    workbook: WorkbookData,
    currentSheetIndex: Int,
    selectedCellRow: Int,
    selectedCellCol: Int,
    onCellClick: (Int, Int) -> Unit,
    onSheetSelect: (Int) -> Unit,
    getCellDescription: (Int, Int) -> String
) {
    val currentSheet = workbook.sheets[currentSheetIndex]
    val rowScrollState = rememberLazyListState()
    val colScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Sheet tabs
        if (workbook.sheetCount > 1) {
            ScrollableTabRow(
                selectedTabIndex = currentSheetIndex,
                modifier = Modifier.semantics {
                    contentDescription = "工作表标签，当前: ${currentSheet.name}"
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                edgePadding = 8.dp
            ) {
                workbook.sheets.forEachIndexed { index, sheet ->
                    Tab(
                        selected = index == currentSheetIndex,
                        onClick = { onSheetSelect(index) },
                        text = {
                            Text(
                                text = sheet.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "工作表: ${sheet.name}" +
                                    if (index == currentSheetIndex) "，当前选中" else ""
                        }
                    )
                }
            }
        }

        // File info bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currentSheet.name} · ${currentSheet.rowCount}行 × ${currentSheet.colCount}列",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "工作表 ${currentSheet.name}，共 ${currentSheet.rowCount} 行 ${currentSheet.colCount} 列"
                    }
                )
                Text(
                    text = "文件: ${workbook.fileName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.semantics {
                        contentDescription = "文件名: ${workbook.fileName}"
                    }
                )
            }
        }

        // Spreadsheet grid using LazyColumn for performance
        // Each row is rendered as needed, with full accessibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            LazyColumn(
                state = rowScrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "表格内容，${currentSheet.rowCount}行 ${currentSheet.colCount}列。" +
                                "使用屏幕阅读器导航功能在单元格间移动。" +
                                "点击任意单元格可查看操作选项。"
                    }
            ) {
                // Column headers row
                item(key = "header") {
                    LazyRow {
                        item(key = "corner") {
                            CornerHeader()
                        }
                        items(currentSheet.colCount) { colIndex ->
                            ColumnHeader(colIndex = colIndex)
                        }
                    }
                }

                // Data rows
                items(
                    count = currentSheet.rowCount,
                    key = { "row_$it" }
                ) { rowIndex ->
                    val row = currentSheet.cells.getOrNull(rowIndex) ?: emptyList()
                    LazyRow {
                        // Row header
                        item(key = "row_header_$rowIndex") {
                            RowHeader(rowNumber = rowIndex)
                        }
                        // Cells in row
                        items(
                            count = row.size,
                            key = { "cell_${rowIndex}_$it" }
                        ) { colIndex ->
                            val cell = row.getOrNull(colIndex) ?: CellData("", rowIndex, colIndex)
                            SpreadsheetCell(
                                cell = cell,
                                isSelected = rowIndex == selectedCellRow && colIndex == selectedCellCol,
                                onCellClick = { onCellClick(rowIndex, colIndex) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to get file name from URI.
 */
private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) it.getString(nameIndex) else null
        } else null
    }
}
