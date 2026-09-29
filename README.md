# 云舟健身

把网页打包成 Android APK 的轻量健身计划 App（Capacitor + Vite）。

## 功能

- 多套健身方案：方案 → 栏目 → 动作 三级磁贴
- 打卡 / 取消打卡，父级状态由子项自动推导
- 磁贴拖拽排序（`≡` 把手或长按）
- 运动会话、日历打卡记录
- 回收站恢复
- 方案导出 / 导入（`.yzplan.json`）
  - Android 10+ 通过 MediaStore 写入系统「下载」目录
  - 失败时回落到系统分享保存

## 目录结构

```
src/index.html          # 应用源码（单文件 HTML/CSS/JS）
dist/                   # vite 构建输出（git 忽略）
index.html              # 根目录副本，便于手机模拟器预览
android/                # Capacitor Android 工程（含自定义插件）
  app/src/main/java/com/example/yunzhou/
    MainActivity.java   # 注册插件
    YzExportPlugin.java # 导出到系统下载目录（MediaStore）
capacitor.config.ts
vite.config.js
```

## 环境要求

- Node.js 18+
- JDK 17
- Android SDK（compileSdk 34）
- Windows 下可用 `setenv.bat` 配置 `JAVA_HOME` / `ANDROID_HOME`（本机路径，请复制 `setenv.example.bat` 修改）

## 构建

```bash
npm install
npm run build          # 产出 dist/
npx cap sync android   # 同步到 Android 工程
cd android
gradlew.bat assembleDebug
# APK: android/app/build/outputs/apk/debug/app-debug.apk
```

或一键：

```bash
npm run apk
```

## 发布签名（可选）

1. 复制 `keystore.properties.example` 为 `keystore.properties`
2. 填入你的 keystore 路径与密码（**不要提交**）
3. 将 keystore 文件放在项目根目录（**不要提交**）
4. 按需在 `android/app/build.gradle` 使用 release signing

## 环境变量脚本

复制并修改：

```bat
copy setenv.example.bat setenv.bat
```

## 说明

- 数据保存在浏览器 `localStorage`，卸载应用会丢失
- 导出文件可在系统文件管理的「下载」目录查找
