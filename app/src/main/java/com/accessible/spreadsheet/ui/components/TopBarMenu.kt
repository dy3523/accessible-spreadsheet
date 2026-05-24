package com.accessible.spreadsheet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Top bar dropdown menu with Settings and About options.
 */
@Composable
fun TopBarMenu(
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics {
                contentDescription = "更多选项"
            }
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("设置") },
                onClick = {
                    expanded = false
                    onSettingsClick()
                },
                leadingIcon = {
                    Icon(Icons.Default.Settings, contentDescription = null)
                },
                modifier = Modifier.semantics {
                    contentDescription = "打开设置"
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("关于") },
                onClick = {
                    expanded = false
                    onAboutClick()
                },
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                modifier = Modifier.semantics {
                    contentDescription = "关于本应用"
                }
            )
        }
    }
}
