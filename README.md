# 轻量级黑名单管理系统 (Lite Blacklist System)

一个基于 Java 的轻量级黑名单管理系统，支持多种类型的黑名单条目管理、持久化存储和基本读写功能。

## 功能特性

✅ **多种黑名单类型支持**
- IP地址 (IP)
- 用户名 (USERNAME)
- 邮箱地址 (EMAIL)
- 手机号码 (PHONE)
- 域名 (DOMAIN)
- 设备ID (DEVICE_ID)
- 其他 (OTHER)

✅ **核心功能**
- 添加黑名单条目
- 删除黑名单条目
- 查询黑名单
- 按类型筛选
- 过期时间管理
- 自动清理过期条目
- 数据统计

✅ **数据持久化**
- 基于文件的存储
- 文本格式存储
- 自动保存/加载

## 快速开始

### 编译项目

```bash
# 编译所有Java文件
javac -encoding UTF-8 -d . src/main/java/com/litelist/blacklist/model/*.java src/main/java/com/litelist/blacklist/storage/*.java src/main/java/com/litelist/blacklist/manager/*.java src/main/java/com/litelist/blacklist/*.java

# 编译测试程序
javac -encoding UTF-8 BlacklistTest.java
javac -encoding UTF-8 BlacklistDemo.java
```

### 运行演示

```bash
# 运行功能演示
java BlacklistDemo

# 运行基础测试
java BlacklistTest
```

### 命令行界面

```bash
# 启动交互式命令行界面
java com.litelist.blacklist.BlacklistApp
```

## 命令行界面使用说明

启动后会进入交互式命令行界面，支持以下命令：

| 命令 | 说明 | 示例 |
|------|------|------|
| `add` | 添加黑名单条目 | `add 192.168.1.1 IP "恶意访问"` |
| `remove` | 删除黑名单条目 | `remove 192.168.1.1` |
| `check` | 检查值是否在黑名单中 | `check 192.168.1.1` |
| `list` | 列出所有黑名单条目 | `list` 或 `list IP` |
| `find` | 按类型查找 | `find IP` |
| `stats` | 显示统计信息 | `stats` |
| `cleanup` | 清理过期条目 | `cleanup` |
| `clear` | 清空所有条目 | `clear` |
| `save` | 手动保存数据 | `save` |
| `load` | 重新加载数据 | `load` |
| `exit`/`quit` | 退出程序 | `exit` |
| `help`/`?` | 显示帮助 | `help` |

## API 使用示例

```java
import com.litelist.blacklist.manager.BlacklistManager;
import com.litelist.blacklist.model.BlacklistType;
import com.litelist.blacklist.storage.FileBlacklistStorage;

// 创建黑名单管理器
BlacklistManager manager = new BlacklistManager(new FileBlacklistStorage("blacklist.txt"));

// 添加黑名单条目
manager.addItem("192.168.1.100", BlacklistType.IP, "恶意访问");
manager.addItem("baduser", BlacklistType.USERNAME, "违规操作");

// 检查值是否在黑名单中
boolean isBlocked = manager.contains("192.168.1.100"); // true

// 按类型查找
List<BlacklistItem> ipItems = manager.findByType(BlacklistType.IP);

// 获取统计信息
Map<String, Object> stats = manager.getStatistics();

// 清理过期条目
int cleanedCount = manager.cleanupExpiredItems();

// 删除条目
manager.removeItemByValue("baduser");
```

## 项目结构

```
lite-list/
├── src/main/java/com/litelist/blacklist/
│   ├── model/
│   │   ├── BlacklistItem.java      # 黑名单条目模型
│   │   └── BlacklistType.java      # 黑名单类型枚举
│   ├── manager/
│   │   └── BlacklistManager.java   # 核心管理器
│   ├── storage/
│   │   ├── BlacklistStorage.java   # 存储接口
│   │   └── FileBlacklistStorage.java # 文件存储实现
│   └── BlacklistApp.java          # 主应用程序
├── BlacklistTest.java             # 测试程序
├── BlacklistDemo.java             # 演示程序
└── README.md                      # 说明文档
```

## 数据格式

黑名单数据以文本格式存储，格式如下：

```
# Blacklist Data File
# Format: id|value|type|createTime|reason|expiryTime
# Created: 2024-01-01 00:00:00
#
id1|192.168.1.100|IP|2024-01-01 00:00:00|恶意访问|
id2|baduser|USERNAME|2024-01-01 00:00:00|违规操作|2024-12-31 23:59:59
```

## 扩展性

系统具有良好的扩展性：

1. **存储扩展**：实现 `BlacklistStorage` 接口即可支持新的存储方式（如数据库、Redis等）
2. **类型扩展**：在 `BlacklistType` 枚举中添加新的类型
3. **功能扩展**：在 `BlacklistManager` 中添加新的业务逻辑

## 许可证

MIT License

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个项目！