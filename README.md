# 无障碍表格查看器 (Accessible Spreadsheet Viewer)

一款专为视障用户设计的 Android Excel 表格查看器，完全适配屏幕阅读器（TalkBack）。

## 功能特性

- ✅ **完全无障碍** - 每个单元格都是独立的可聚焦控件，屏幕阅读器可逐个朗读
- ✅ **支持 .xls 和 .xlsx** - 兼容旧版和新版 Excel 格式
- ✅ **打开方式多样** - 支持从文件管理器选择、从其他应用直接打开
- ✅ **单元格操作** - 点击单元格可编辑、复制值、查看属性
- ✅ **Material 3 Expressive** - 遵循最新 Material Design 设计规范
- ✅ **深色模式** - 自动跟随系统深色/浅色模式
- ✅ **动态颜色** - Android 12+ 支持动态取色
- ✅ **多工作表** - 支持切换不同工作表标签
- ✅ **性能优化** - 使用 LazyColumn 懒加载，支持大表格

## 系统要求

- Android 8.0 (API 26) 或更高版本
- 推荐 Android 12+ 以获得最佳体验

## 构建方式

### 方式一：GitHub Actions 自动构建（推荐）

1. 将项目推送到 GitHub 仓库
2. 推送到 `main` 分支后，GitHub Actions 会自动构建
3. 在 Actions 页面下载 APK artifact

### 方式二：本地构建

1. 安装 Android Studio 或 Android SDK
2. 确保 JDK 17 已安装
3. 运行以下命令：

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease
```

4. APK 位于 `app/build/outputs/apk/`

### 方式三：Android Studio

1. 用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 点击 Build > Build Bundle(s) / APK(s) > Build APK(s)

## 安装使用

1. 将 APK 传输到手机
2. 打开 APK 文件安装（需要允许安装未知来源应用）
3. 打开"无障碍表格"应用
4. 点击"打开 Excel 文件"选择文件
5. 或从文件管理器直接点击 .xls/.xlsx 文件打开

## 无障碍使用说明

### 屏幕阅读器操作

- **浏览单元格** - 使用屏幕阅读器的左/右滑动手势在单元格间移动
- **点击单元格** - 双击任意单元格打开操作菜单
- **操作选项** - 菜单中包含：编辑、复制值、复制位置、查看属性
- **切换工作表** - 在顶部标签栏选择不同工作表
- **朗读信息** - 屏幕阅读器会自动朗读单元格位置、值和类型

### 手势说明

- **单指左/右滑** - 在单元格间移动焦点
- **单指双击** - 激活当前单元格（打开操作菜单）
- **双指上/下滑** - 滚动表格内容
- **三指上/下滑** - 切换工作表

## 技术架构

- **UI框架** - Jetpack Compose + Material 3
- **Excel解析** - Apache POI (支持 .xls 和 .xlsx)
- **架构模式** - MVVM
- **无障碍** - Compose Semantics API
- **最低API** - 26 (Android 8.0)
- **目标API** - 36 (Android 16)

## 项目结构

```
app/src/main/java/com/accessible/spreadsheet/
├── MainActivity.kt              # 主入口
├── model/
│   └── SpreadsheetData.kt      # 数据模型
├── data/
│   └── SpreadsheetViewModel.kt # 状态管理
├── ui/
│   ├── theme/
│   │   ├── Theme.kt            # Material 3 主题
│   │   └── Type.kt             # 字体排版
│   ├── screens/
│   │   └── SpreadsheetScreen.kt # 主屏幕
│   └── components/
│       ├── SpreadsheetCell.kt  # 单元格组件
│       └── CellActionSheet.kt  # 操作底部菜单
└── util/
    └── ExcelParser.kt          # Excel 解析器
```

## 许可证

MIT License - 自由使用和修改
