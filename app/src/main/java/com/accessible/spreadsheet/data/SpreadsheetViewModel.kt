package com.accessible.spreadsheet.data

import android.content.Context
import android.net.Uri
import com.accessible.spreadsheet.model.CellData
import com.accessible.spreadsheet.model.WorkbookData
import com.accessible.spreadsheet.util.ExcelParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * UI state for the spreadsheet screen.
 */
data class SpreadsheetUiState(
    val isLoading: Boolean = false,
    val workbook: WorkbookData? = null,
    val currentSheetIndex: Int = 0,
    val error: String? = null,
    val selectedCell: CellData? = null,
    val selectedCellRow: Int = -1,
    val selectedCellCol: Int = -1,
    val isEditMode: Boolean = false,
    val editValue: String = "",
    val showCellDialog: Boolean = false,
    val fileName: String = ""
)

/**
 * ViewModel managing spreadsheet state and operations.
 */
class SpreadsheetViewModel {
    private val _uiState = MutableStateFlow(SpreadsheetUiState())
    val uiState: StateFlow<SpreadsheetUiState> = _uiState.asStateFlow()

    suspend fun loadFile(context: Context, uri: Uri, fileName: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, fileName = fileName)

        withContext(Dispatchers.IO) {
            val result = ExcelParser.parse(context, uri, fileName)
            result.fold(
                onSuccess = { workbook ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        workbook = workbook,
                        currentSheetIndex = 0,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "未知错误"
                    )
                }
            )
        }
    }

    fun selectSheet(index: Int) {
        val workbook = _uiState.value.workbook ?: return
        if (index in 0 until workbook.sheetCount) {
            _uiState.value = _uiState.value.copy(currentSheetIndex = index)
        }
    }

    fun selectCell(row: Int, col: Int) {
        val workbook = _uiState.value.workbook ?: return
        val sheet = workbook.sheets.getOrNull(_uiState.value.currentSheetIndex) ?: return
        if (row in 0 until sheet.rowCount && col in 0 until sheet.colCount) {
            val cell = sheet.cells[row][col]
            _uiState.value = _uiState.value.copy(
                selectedCell = cell,
                selectedCellRow = row,
                selectedCellCol = col,
                showCellDialog = true,
                editValue = cell.value,
                isEditMode = false
            )
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showCellDialog = false,
            selectedCell = null,
            isEditMode = false
        )
    }

    fun startEdit() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
    }

    fun updateEditValue(value: String) {
        _uiState.value = _uiState.value.copy(editValue = value)
    }

    fun saveEdit() {
        val state = _uiState.value
        val workbook = state.workbook ?: return
        val sheetIndex = state.currentSheetIndex
        val row = state.selectedCellRow
        val col = state.selectedCellCol

        if (row < 0 || col < 0) return

        val sheet = workbook.sheets[sheetIndex]
        val oldCell = sheet.cells[row][col]
        val newCell = oldCell.copy(value = state.editValue)

        val newRow = sheet.cells[row].toMutableList()
        newRow[col] = newCell
        val newCells = sheet.cells.toMutableList()
        newCells[row] = newRow

        val newSheet = sheet.copy(cells = newCells)
        val newSheets = workbook.sheets.toMutableList()
        newSheets[sheetIndex] = newSheet
        val newWorkbook = workbook.copy(sheets = newSheets)

        _uiState.value = state.copy(
            workbook = newWorkbook,
            selectedCell = newCell,
            isEditMode = false,
            showCellDialog = false
        )
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(isEditMode = false, editValue = _uiState.value.selectedCell?.value ?: "")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getCellDescription(row: Int, col: Int): String {
        val sheet = _uiState.value.workbook?.sheets?.getOrNull(_uiState.value.currentSheetIndex)
        val cell = sheet?.cells?.getOrNull(row)?.getOrNull(col)
        return if (cell != null) {
            ExcelParser.getCellAccessibilityDescription(cell, row, col)
        } else {
            "空单元格"
        }
    }

    fun getCurrentSheet() = _uiState.value.workbook?.sheets?.getOrNull(_uiState.value.currentSheetIndex)
}
