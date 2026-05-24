package com.accessible.spreadsheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.accessible.spreadsheet.data.SpreadsheetViewModel
import com.accessible.spreadsheet.ui.components.AboutDialog
import com.accessible.spreadsheet.ui.screens.SettingsManager
import com.accessible.spreadsheet.ui.screens.SettingsScreen
import com.accessible.spreadsheet.ui.screens.SpreadsheetScreen
import com.accessible.spreadsheet.ui.theme.AccessibleSpreadsheetTheme

class MainActivity : ComponentActivity() {

    private val viewModel = SpreadsheetViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle intent from other apps (VIEW/EDIT action)
        val intentUri: Uri? = when (intent?.action) {
            Intent.ACTION_VIEW, Intent.ACTION_EDIT -> intent.data
            else -> null
        }

        val intentFileName: String? = intentUri?.let { getFileNameFromIntent(it) }

        setContent {
            val settingsManager = remember { SettingsManager(this@MainActivity) }
            AccessibleSpreadsheetTheme(
                themeMode = settingsManager.theme,
                useDynamicColor = settingsManager.useDynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(
                        viewModel = viewModel,
                        initialUri = intentUri,
                        initialFileName = intentFileName
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun getFileNameFromIntent(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }
}

/**
 * Top-level app content managing screen navigation.
 */
@Composable
private fun AppContent(
    viewModel: SpreadsheetViewModel,
    initialUri: Uri?,
    initialFileName: String?
) {
    var currentScreen by remember { mutableStateOf("main") }
    var showAboutDialog by remember { mutableStateOf(false) }

    when (currentScreen) {
        "settings" -> {
            SettingsScreen(
                onBack = { currentScreen = "main" }
            )
        }
        else -> {
            SpreadsheetScreen(
                viewModel = viewModel,
                onNavigateToSettings = { currentScreen = "settings" },
                onShowAbout = { showAboutDialog = true },
                initialUri = initialUri,
                initialFileName = initialFileName
            )

            if (showAboutDialog) {
                AboutDialog(
                    onDismiss = { showAboutDialog = false }
                )
            }
        }
    }
}
