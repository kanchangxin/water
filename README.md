# 💧 喝水提醒应用

一个纯原生安卓喝水提醒应用，支持震动、声音和屏幕通知，可在后台常驻运行。

## ✨ 功能特性

- ⏰ **自定义提醒间隔** - 支持 1-120 分钟自定义设置
- 🔔 **声音提醒** - 系统通知声音提示
- 📳 **震动提醒** - 三段式震动模式
- 📱 **屏幕通知** - Toast 弹窗提示
- 📩 **状态栏通知** - 点击可返回应用
- 🔄 **后台常驻** - 使用 Foreground Service 确保后台运行
- 🔃 **开机自启** - 设备重启后自动恢复提醒
- 💾 **数据持久化** - 提醒间隔和喝水记录自动保存
- ✅ **喝水记录** - 记录上次喝水时间

## 📁 项目结构

```
├── android/
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/waterremind/app/
│   │       │   ├── MainActivity.java      # 主活动
│   │       │   ├── WaterReminderService.java  # 后台服务
│   │       │   └── BootReceiver.java     # 开机广播接收器
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml # 界面布局
│   │       │   ├── drawable/             # 资源文件
│   │       │   └── values/               # 配置文件
│   │       └── AndroidManifest.xml       # 权限配置
│   └── build.gradle                      # 构建配置
└── .github/workflows/
    └── build.yml                         # GitHub Actions 配置
```

## 🚀 编译方式

### 方法一：GitHub Actions 自动编译（推荐）

1. 将代码推送到 GitHub 仓库
2. GitHub Actions 会自动触发构建
3. 在仓库的 Actions 页面下载生成的 APK

### 方法二：本地编译

```bash
# 安装 Java 17（如果未安装）
sudo apt install openjdk-17-jdk  # Ubuntu/Debian
brew install openjdk@17          # macOS

# 进入安卓目录
cd android

# 编译 debug 版本
./gradlew assembleDebug

# 编译 release 版本
./gradlew assembleRelease
```

## 📱 APK 位置

```
android/app/build/outputs/apk/debug/app-debug.apk
android/app/build/outputs/apk/release/app-release.apk
```

## 🎨 界面预览

应用采用渐变紫色主题，界面包含：
- 水滴图标和应用标题
- 大字号倒计时显示
- 提醒间隔设置输入框
- 开始/暂停/重置按钮
- 状态指示（绿色运行中/红色未启动）
- 喝水记录显示
- "我喝完了"按钮

## 📋 权限说明

- `POST_NOTIFICATIONS` - 发送通知
- `VIBRATE` - 震动提醒
- `FOREGROUND_SERVICE` - 前台服务
- `RECEIVE_BOOT_COMPLETED` - 开机自启
- `SCHEDULE_EXACT_ALARM` - 精确闹钟（Android 12+）
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - 忽略电池优化

## 🔋 电池优化设置

为确保应用在熄屏和后台运行时能正常接收提醒，请按照以下步骤设置：

1. **打开手机设置** → **电池** → **后台应用耗电管理**（或类似选项）
2. 找到 **喝水提醒** 应用
3. 将其设置为 **允许后台耗电** 或 **不受电池优化限制**

**不同品牌手机的设置路径可能略有不同：**
- **华为/荣耀**: 设置 > 电池 > 应用启动管理 > 找到喝水提醒 > 关闭"自动管理" > 手动打开"允许后台活动"
- **小米/红米**: 设置 > 电池与性能 > 应用耗电管理 > 喝水提醒 > 设为"无限制"
- **OPPO/一加**: 设置 > 电池 > 耗电保护 > 喝水提醒 > 关闭"后台冻结"和"深度睡眠"
- **vivo/iqoo**: 设置 > 电池 > 后台高耗电 > 找到喝水提醒 > 开启开关
- **三星**: 设置 > 电池和设备维护 > 电池 > 后台应用程序 > 添加喝水提醒
- **原生安卓**: 设置 > 电池 > 电池优化 > 不优化 > 选择喝水提醒

## 📝 使用说明

1. 打开应用后设置提醒间隔（默认为 20 分钟）
2. 点击"开始"按钮启动提醒
3. 到达提醒时间时会触发震动、声音和通知
4. 点击"我喝完了"记录喝水时间并重置倒计时
5. 应用会在后台持续运行，即使关闭应用也能收到提醒

## 🛠 技术栈

- Android SDK 33
- Java 17
- Gradle 7.5
- AndroidX 库

## 📄 License

MIT License
