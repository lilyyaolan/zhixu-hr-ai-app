知序 · HR AI工作管家 Android App V1

这是可用 Android Studio 打开的 App 工程，不是网页压缩包。

已完成：
1. App壳
2. 内置现有HR工作台
3. 原生Android语音识别桥接
4. 麦克风权限
5. 本地数据保存
6. 本地免费模式 + 自带AI模式继续保留

如何生成APK：
1. 安装 Android Studio
2. 打开本文件夹
3. 等待 Gradle Sync 完成
4. 连接安卓手机或使用模拟器测试
5. 菜单：Build → Build App Bundles or APKs → Build APKs
6. 生成的 APK 通常位于：
   app/build/outputs/apk/debug/app-debug.apk

正式销售前：
- 需要生成自己的签名证书
- 使用 release 签名APK
- 不要直接把debug APK作为正式销售包
- 建议先在3-5台不同安卓手机测试语音、文件导出、备份恢复

当前限制：
- 这是Android App工程版。
- 当前环境没有完整Android SDK/Gradle构建环境，因此此处未直接生成APK。
- iOS需要单独用Xcode封装和签名。
