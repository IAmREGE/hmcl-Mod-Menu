# HMCL Mod Menu

[English](#english) | [简体中文](#简体中文)

---

<a name="english"></a>
## English

A lightweight, elegant Fabric mod viewer and manager for [Hello Minecraft! Launcher (HMCL)](https://github.com/HMCL-dev/HMCL), inspired by Minecraft's beloved [Mod Menu](https://github.com/TerraformersMC/ModMenu).

### Features

1. **Seamless HMCL Sidebar Integration**:
   - Injects a dedicated **"Fabric Mods"** navigation entry directly into HMCL's left sidebar under the "General" category via `RootPageSkinMixin`.
   - Adheres strictly to HMCL's native Material / Monet UI design and JavaFX component architecture.
2. **Comprehensive Mod Browser**:
   - Lists all loaded Fabric mods with custom mod icons, names, mod IDs, and version tags.
   - Quick search bar: filter mods instantly by name, ID, or description.
   - Detailed metadata view: displays authors, licenses, contact info, issue trackers, and source repositories.
   - One-click **"Open Mods Folder"** button for easy mod management.
3. **Mod Menu API (`ModMenuApi`) for Developers**:
   - Provides a clean API allowing other HMCL mods to integrate their custom configuration screens into Mod Menu.
   - Mods providing a config screen will display an interactive settings button on their card.

### Developer API: Adding a Settings Screen

In your mod project, implement `ModMenuApi`:

```java
package com.example.mymod;

import javafx.scene.Node;
import rege.rege.hmclmods.modmenu.api.ConfigScreenFactory;
import rege.rege.hmclmods.modmenu.api.ModMenuApi;

public class MyModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new MyModConfigPane(parent);
    }
}
```

Register the entrypoint in your `fabric.mod.json`:

```json
{
  "entrypoints": {
    "modmenu": [
      "com.example.mymod.MyModMenuIntegration"
    ]
  }
}
```

### Building

```bash
./gradlew build
```

The compiled mod JAR will be located at `build/libs/hmcl-mod-menu-1.0.0.jar`. Place it into `.hmcl/mods/` (or `mods/`).

---

<a name="简体中文"></a>
## 简体中文

专为 [Hello Minecraft! Launcher (HMCL)](https://github.com/HMCL-dev/HMCL) 打造的轻量级 Fabric 模组查看器与管理界面，灵感源自 Minecraft 著名模组 [Mod Menu](https://github.com/TerraformersMC/ModMenu)。

### 功能特性

1. **无缝融入 HMCL 侧边栏**：
   - 通过 `RootPageSkinMixin` 在 HMCL 侧边栏“通用”分类下无缝注入 **“Fabric 模组”** 入口。
   - 界面设计严格对齐 HMCL 原生 Material / Monet 设计规范，操作手感自然流畅。
2. **全功能模组浏览器**：
   - 完整展示当前已加载的所有 Fabric 模组（包括模组图标、显示名称、Mod ID、版本号及标签）。
   - **快速搜索框**：支持按名称、ID 或描述即时检索模组。
   - **元数据详情**：查看模组作者、开源许可证、官方主页、问题反馈（Issues）及源码仓库链接。
   - **一键打开目录**：提供“打开模组文件夹”按钮，方便快捷管理安装的 Jar 包。
3. **面向开发者的配置界面 API (`ModMenuApi`)**：
   - 参考原版 Mod Menu 的 API 设计，允许其他 HMCL 模组接入自己的配置页面。
   - 实现了该接口的模组在列表中会高亮展示“设置”齿轮按钮，点击即可直接打开模组专属设置面板。

### 开发者接入：提供模组设置页面

在你的模组中实现 `ModMenuApi`：

```java
package com.example.mymod;

import javafx.scene.Node;
import rege.rege.hmclmods.modmenu.api.ConfigScreenFactory;
import rege.rege.hmclmods.modmenu.api.ModMenuApi;

public class MyModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new MyModConfigPane(parent);
    }
}
```

在你的 `fabric.mod.json` 中注册 `modmenu` 入口点：

```json
{
  "entrypoints": {
    "modmenu": [
      "com.example.mymod.MyModMenuIntegration"
    ]
  }
}
```

### 构建

```bash
./gradlew build
```

编译产物位于 `build/libs/hmcl-mod-menu-1.0.0.jar`。将生成的 Jar 包放入 HMCL 根目录下的 `.hmcl/mods/`（或 `mods/`）即可生效。
