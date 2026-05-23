package com.accessible.spreadsheet.util

import android.content.Context
import android.net.Uri
import com.accessible.spreadsheet.model.CellData
import com.accessible.spreadsheet.model.CellType
import com.accessible.spreadsheet.model.SheetData
import com.accessible.spreadsheet.model.WorkbookData
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

/**
 * Parses Excel files (.xls and .xlsx) into our data model.
 * Uses Apache POI for full format compatibility.
 */
object ExcelParser {

    fun parse(context: Context, uri: Uri, fileName: String): Result<WorkbookData> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("无法打开文件"))

            val workbook = createWorkbook(inputStream, fileName)
            val sheets = mutableListOf<SheetData>()

            for (i in 0 until workbook.numberOfSheets) {
                val sheet = workbook.getSheetAt(i)
                sheets.add(parseSheet(sheet))
            }

            workbook.close()
            inputStream.close()

            Result.success(
                WorkbookData(
                    fileName = fileName,
                    sheets = sheets,
                    sheetCount = sheets.size,
                    filePath = uri.toString()
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("解析Excel文件失败: ${e.localizedMessage}"))
        }
    }

    private fun createWorkbook(inputStream: InputStream, fileName: String): Workbook {
        return if (fileName.endsWith(".xls", ignoreCase = true)) {
            HSSFWorkbook(inputStream)
        } else {
            XSSFWorkbook(inputStream)
        }
    }

    private fun parseSheet(sheet: Sheet): SheetData {
        val lastRow = sheet.lastRowNum.toInt()
        var maxCol = 0

        // Find max columns
        for (rowIdx in 0..lastRow) {
            val row = sheet.getRow(rowIdx)
            if (row != null && row.lastCellNum > maxCol) {
                maxCol = row.lastCellNum.toInt()
            }
        }

        val cells = mutableListOf<List<CellData>>()
        val evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()

        for (rowIdx in 0..lastRow) {
            val row = sheet.getRow(rowIdx)
            val rowData = mutableListOf<CellData>()
            for (colIdx in 0 until maxCol) {
                val cell = row?.getCell(colIdx)
                rowData.add(parseCell(cell, rowIdx, colIdx, evaluator))
            }
            cells.add(rowData)
        }

        return SheetData(
            name = sheet.sheetName,
            cells = cells,
            rowCount = lastRow + 1,
            colCount = maxCol
        )
    }

    private fun parseCell(cell: Cell?, row: Int, col: Int, evaluator: FormulaEvaluator): CellData {
        if (cell == null) {
            return CellData(value = "", row = row, col = col, type = CellType.EMPTY)
        }

        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    CellData(
                        value = cell.dateCellValue.toString(),
                        row = row,
                        col = col,
                        type = CellType.DATE,
                        format = cell.cellStyle?.dataFormatString
                    )
                } else {
                    val numValue = cell.numericCellValue
                    val displayValue = if (numValue == numValue.toLong().toDouble()) {
                        numValue.toLong().toString()
                    } else {
                        numValue.toString()
                    }
                    CellData(
                        value = displayValue,
                        row = row,
                        col = col,
                        type = CellType.NUMERIC,
                        format = cell.cellStyle?.dataFormatString
                    )
                }
            }
            org.apache.poi.ss.usermodel.CellType.STRING -> CellData(
                value = cell.stringCellValue,
                row = row,
                col = col,
                type = CellType.STRING
            )
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> CellData(
                value = if (cell.booleanCellValue) "TRUE" else "FALSE",
                row = row,
                col = col,
                type = CellType.BOOLEAN
            )
            org.apache.poi.ss.usermodel.CellType.FORMULA -> {
                try {
                    val evaluated = evaluator.evaluate(cell)
                    CellData(
                        value = formatEvaluatedValue(evaluated),
                        row = row,
                        col = col,
                        type = CellType.FORMULA,
                        formula = cell.cellFormula
                    )
                } catch (e: Exception) {
                    CellData(
                        value = cell.cellFormula,
                        row = row,
                        col = col,
                        type = CellType.FORMULA,
                        formula = cell.cellFormula
                    )
                }
            }
            org.apache.poi.ss.usermodel.CellType.BLANK -> CellData(value = "", row = row, col = col, type = CellType.EMPTY)
            else -> CellData(value = cell.toString(), row = row, col = col)
        }
    }

    private fun formatEvaluatedValue(cellValue: CellValue): String {
        return when (cellValue.cellType) {
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                val num = cellValue.numberValue
                if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
            }
            org.apache.poi.ss.usermodel.CellType.STRING -> cellValue.stringValue
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> if (cellValue.booleanValue) "TRUE" else "FALSE"
            else -> cellValue.toString()
        }
    }

    /**
     * Returns a human-readable description of a cell for screen readers.
     */
    fun getCellAccessibilityDescription(cell: CellData, row: Int, col: Int): String {
        val colLetter = getColumnName(col)
        val position = "${colLetter}${row + 1}"
        val valueDesc = if (cell.value.isBlank()) "空单元格" else cell.value
        val typeDesc = when (cell.type) {
            CellType.NUMERIC -> "数值"
            CellType.STRING -> "文本"
            CellType.BOOLEAN -> "布尔值"
            CellType.FORMULA -> "公式: ${cell.formula ?: ""}"
            CellType.DATE -> "日期"
            CellType.EMPTY -> ""
        }
        return "单元格 $position, $valueDesc${if (typeDesc.isNotEmpty()) ", $typeDesc" else ""}"
    }

    private fun getColumnName(col: Int): String {
        var result = ""
        var c = col
        while (c >= 0) {
            result = ('A' + c % 26) + result
            c = c / 26 - 1
        }
        return result
    }
}
