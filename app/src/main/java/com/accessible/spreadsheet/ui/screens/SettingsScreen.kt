package com.accessible.spreadsheet.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Announcement item types for TalkBack cell reading.
 */
enum class AnnouncementType(val key: String, val label: String, val defaultEnabled: Boolean) {
    POSITION("position", "单元格坐标", true),
    VALUE("value", "单元格内容", true),
    TYPE("type", "数据类型", true),
    FORMULA("formula", "公式", true),
    SELECTED("selected", "选中状态", false);

    companion object {
        val DEFAULT_ORDER = listOf(POSITION, VALUE, TYPE, FORMULA, SELECTED)

        fun fromKey(key: String): AnnouncementType? = entries.find { it.key == key }
    }
}

/**
 * Settings helper using SharedPreferences.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_ANNOUNCEMENT_ORDER = "announcement_order"
        private const val KEY_ENABLED_ANNOUNCEMENTS = "enabled_announcements"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    var theme: String
        get() = prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var useDynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()

    fun getAnnouncementOrder(): List<AnnouncementType> {
        val saved = prefs.getString(KEY_ANNOUNCEMENT_ORDER, null)
        if (saved != null) {
            val keys = saved.split(",")
            val types = keys.mapNotNull { AnnouncementType.fromKey(it) }
            if (types.isNotEmpty()) return types
        }
        return AnnouncementType.DEFAULT_ORDER
    }

    fun setAnnouncementOrder(order: List<AnnouncementType>) {
        prefs.edit()
            .putString(KEY_ANNOUNCEMENT_ORDER, order.joinToString(",") { it.key })
            .apply()
    }

    fun getEnabledAnnouncements(): Set<String> {
        val saved = prefs.getStringSet(KEY_ENABLED_ANNOUNCEMENTS, null)
        if (saved != null) return saved
        return AnnouncementType.entries.filter { it.defaultEnabled }.map { it.key }.toSet()
    }

    fun setEnabledAnnouncements(enabled: Set<String>) {
        prefs.edit().putStringSet(KEY_ENABLED_ANNOUNCEMENTS, enabled).apply()
    }

    fun isAnnouncementEnabled(type: AnnouncementType): Boolean {
        return getEnabledAnnouncements().contains(type.key)
    }

    fun setAnnouncementEnabled(type: AnnouncementType, enabled: Boolean) {
        val current = getEnabledAnnouncements().toMutableSet()
        if (enabled) current.add(type.key) else current.remove(type.key)
        setEnabledAnnouncements(current)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    var currentTheme by remember { mutableStateOf(settingsManager.theme) }
    var useDynamicColor by remember { mutableStateOf(settingsManager.useDynamicColor) }
    var announcementOrder by remember { mutableStateOf(settingsManager.getAnnouncementOrder()) }
    var enabledAnnouncements by remember { mutableStateOf(settingsManager.getEnabledAnnouncements()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "返回"
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                .verticalScroll(rememberScrollState())
        ) {
            // ===== 外观设置 =====
            SettingsSection(title = "外观") {
                // 主题选择
                Text(
                    text = "主题模式",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "主题模式选择"
                        }
                ) {
                    val options = listOf(
                        SettingsManager.THEME_SYSTEM to "跟随系统",
                        SettingsManager.THEME_LIGHT to "浅色",
                        SettingsManager.THEME_DARK to "深色"
                    )
                    options.forEachIndexed { index, (value, label) ->
                        SegmentedButton(
                            selected = currentTheme == value,
                            onClick = {
                                currentTheme = value
                                settingsManager.theme = value
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = label +
                                        if (currentTheme == value) "，当前选中" else ""
                            }
                        ) {
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 动态颜色开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "动态取色，" +
                                    if (useDynamicColor) "已开启" else "已关闭" +
                                            "，使用壁纸颜色生成主题色"
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "动态取色",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "使用壁纸颜色生成主题色（Android 12+）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = {
                            useDynamicColor = it
                            settingsManager.useDynamicColor = it
                        }
                    )
                }
            }

            // ===== 无障碍设置 =====
            SettingsSection(title = "无障碍 - TalkBack 朗读") {
                Text(
                    text = "配置单元格被选中时的朗读内容和顺序。关闭不需要的项目可以减少冗余信息。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                announcementOrder.forEachIndexed { index, type ->
                    val isEnabled = enabledAnnouncements.contains(type.key)

                    AnnouncementItem(
                        type = type,
                        isEnabled = isEnabled,
                        onToggle = {
                            val newEnabled = enabledAnnouncements.toMutableSet()
                            if (isEnabled) newEnabled.remove(type.key)
                            else newEnabled.add(type.key)
                            enabledAnnouncements = newEnabled
                            settingsManager.setAnnouncementEnabled(type, !isEnabled)
                        },
                        canMoveUp = index > 0,
                        canMoveDown = index < announcementOrder.size - 1,
                        onMoveUp = {
                            val newOrder = announcementOrder.toMutableList()
                            val temp = newOrder[index]
                            newOrder[index] = newOrder[index - 1]
                            newOrder[index - 1] = temp
                            announcementOrder = newOrder
                            settingsManager.setAnnouncementOrder(newOrder)
                        },
                        onMoveDown = {
                            val newOrder = announcementOrder.toMutableList()
                            val temp = newOrder[index]
                            newOrder[index] = newOrder[index + 1]
                            newOrder[index + 1] = temp
                            announcementOrder = newOrder
                            settingsManager.setAnnouncementOrder(newOrder)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reset button
                OutlinedButton(
                    onClick = {
                        announcementOrder = AnnouncementType.DEFAULT_ORDER
                        enabledAnnouncements =
                            AnnouncementType.entries.filter { it.defaultEnabled }.map { it.key }.toSet()
                        settingsManager.setAnnouncementOrder(announcementOrder)
                        settingsManager.setEnabledAnnouncements(enabledAnnouncements)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "恢复默认朗读设置"
                        }
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("恢复默认")
                }
            }

            // ===== 朗读预览 =====
            SettingsSection(title = "朗读预览") {
                val previewOrder = announcementOrder.filter { enabledAnnouncements.contains(it.key) }
                val previewText = previewOrder.joinToString("，") { type ->
                    when (type) {
                        AnnouncementType.POSITION -> "A1"
                        AnnouncementType.VALUE -> "42"
                        AnnouncementType.TYPE -> "数值"
                        AnnouncementType.FORMULA -> "公式: =SUM(A1:A10)"
                        AnnouncementType.SELECTED -> "已选中"
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "朗读预览: $previewText"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "示例单元格朗读效果：",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .semantics { heading() }
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AnnouncementItem(
    type: AnnouncementType,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val example = when (type) {
        AnnouncementType.POSITION -> "如: A1, B3"
        AnnouncementType.VALUE -> "如: 42, 你好"
        AnnouncementType.TYPE -> "如: 数值, 文本, 公式"
        AnnouncementType.FORMULA -> "如: =SUM(A1:A10)"
        AnnouncementType.SELECTED -> "已选中"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle switch
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.semantics {
                contentDescription = "${type.label}，" +
                        if (isEnabled) "已开启" else "已关闭" +
                                "，示例: $example"
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Label and example
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEnabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Move up/down buttons
        Column {
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier
                    .size(32.dp)
                    .semantics {
                        contentDescription = "上移 ${type.label}"
                    }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier
                    .size(32.dp)
                    .semantics {
                        contentDescription = "下移 ${type.label}"
                    }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
