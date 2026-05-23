package com.accessible.spreadsheet.model

/**
 * Represents a single cell in a spreadsheet.
 * All values are stored as strings for display; original type is preserved for editing.
 */
data class CellData(
    val value: String,
    val row: Int,
    val col: Int,
    val type: CellType = CellType.STRING,
    val formula: String? = null,
    val format: String? = null
)

enum class CellType {
    STRING, NUMERIC, BOOLEAN, FORMULA, DATE, EMPTY
}

/**
 * Represents a single sheet within a workbook.
 */
data class SheetData(
    val name: String,
    val cells: List<List<CellData>>,
    val rowCount: Int,
    val colCount: Int
)

/**
 * Represents an entire Excel workbook.
 */
data class WorkbookData(
    val fileName: String,
    val sheets: List<SheetData>,
    val sheetCount: Int,
    val filePath: String
)
