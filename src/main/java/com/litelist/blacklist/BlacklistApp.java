package com.litelist.blacklist;

import com.litelist.blacklist.manager.BlacklistManager;
import com.litelist.blacklist.model.BlacklistItem;
import com.litelist.blacklist.model.BlacklistType;
import com.litelist.blacklist.storage.FileBlacklistStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 黑名单系统主应用程序
 * 提供命令行界面来管理黑名单
 */
public class BlacklistApp {
    private static final String DEFAULT_STORAGE_FILE = "blacklist.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BlacklistManager manager;
    private final Scanner scanner;
    private boolean running;

    public BlacklistApp() {
        this.manager = new BlacklistManager(new FileBlacklistStorage(DEFAULT_STORAGE_FILE));
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public static void main(String[] args) {
        BlacklistApp app = new BlacklistApp();
        app.start();
    }

    /**
     * 启动应用程序
     */
    public void start() {
        System.out.println("=== 轻量级黑名单管理系统 ===");
        System.out.println("输入 'help' 查看可用命令");
        System.out.println();

        while (running) {
            try {
                printPrompt();
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                processCommand(input);
            } catch (Exception e) {
                System.err.println("处理命令时出错: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("应用程序已退出");
    }

    /**
     * 处理用户命令
     */
    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String arguments = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "help":
            case "?":
                showHelp();
                break;
            case "add":
                addItem(arguments);
                break;
            case "remove":
                removeItem(arguments);
                break;
            case "check":
                checkItem(arguments);
                break;
            case "list":
                listItems(arguments);
                break;
            case "find":
                findItems(arguments);
                break;
            case "stats":
                showStatistics();
                break;
            case "cleanup":
                cleanupExpired();
                break;
            case "clear":
                clearAll();
                break;
            case "save":
                saveData();
                break;
            case "load":
                loadData();
                break;
            case "exit":
            case "quit":
                exit();
                break;
            default:
                System.out.println("未知命令: " + command);
                System.out.println("输入 'help' 查看可用命令");
        }
    }

    /**
     * 显示帮助信息
     */
    private void showHelp() {
        System.out.println("=== 命令列表 ===");
        System.out.println("add <值> <类型> [原因] [过期时间] - 添加黑名单条目");
        System.out.println("  示例: add 192.168.1.1 IP '恶意访问' '2024-12-31 23:59:59'");
        System.out.println("remove <ID或值> - 删除黑名单条目");
        System.out.println("check <值> - 检查值是否在黑名单中");
        System.out.println("list [类型] - 列出所有黑名单条目（可选按类型过滤）");
        System.out.println("find <类型> - 按类型查找黑名单条目");
        System.out.println("stats - 显示统计信息");
        System.out.println("cleanup - 清理已过期的条目");
        System.out.println("clear - 清空所有黑名单条目");
        System.out.println("save - 手动保存数据");
        System.out.println("load - 重新加载数据");
        System.out.println("exit/quit - 退出程序");
        System.out.println("help/? - 显示此帮助");
        System.out.println();
        System.out.println("支持的类型: IP, USERNAME, EMAIL, PHONE, DOMAIN, DEVICE_ID, OTHER");
    }

    /**
     * 添加黑名单条目
     */
    private void addItem(String arguments) {
        if (arguments.isEmpty()) {
            System.out.println("用法: add <值> <类型> [原因] [过期时间]");
            return;
        }

        String[] parts = arguments.split("\\s+", 4);
        if (parts.length < 2) {
            System.out.println("用法: add <值> <类型> [原因] [过期时间]");
            return;
        }

        String value = parts[0];
        BlacklistType type = BlacklistType.fromString(parts[1]);
        String reason = parts.length > 2 ? parts[2] : "未指定原因";
        String expiryStr = parts.length > 3 ? parts[3] : null;

        LocalDateTime expiryTime = null;
        if (expiryStr != null && !expiryStr.isEmpty()) {
            try {
                expiryTime = LocalDateTime.parse(expiryStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("过期时间格式错误，使用格式: yyyy-MM-dd HH:mm:ss");
                return;
            }
        }

        boolean success;
        if (expiryTime != null) {
            success = manager.addItem(value, type, reason, expiryTime);
        } else {
            success = manager.addItem(value, type, reason);
        }

        if (success) {
            System.out.println("成功添加黑名单条目: " + value);
        } else {
            System.out.println("添加失败，可能该条目已存在");
        }
    }

    /**
     * 删除黑名单条目
     */
    private void removeItem(String arguments) {
        if (arguments.isEmpty()) {
            System.out.println("用法: remove <ID或值>");
            return;
        }

        boolean success;
        // 首先尝试按ID删除
        success = manager.removeItem(arguments);

        if (!success) {
            // 如果按ID删除失败，尝试按值删除
            success = manager.removeItemByValue(arguments);
        }

        if (success) {
            System.out.println("成功删除黑名单条目");
        } else {
            System.out.println("未找到指定的黑名单条目");
        }
    }

    /**
     * 检查值是否在黑名单中
     */
    private void checkItem(String arguments) {
        if (arguments.isEmpty()) {
            System.out.println("用法: check <值>");
            return;
        }

        boolean inBlacklist = manager.contains(arguments);
        if (inBlacklist) {
            BlacklistItem item = manager.findByValue(arguments);
            System.out.println("值 '" + arguments + "' 在黑名单中");
            System.out.println("详细信息: " + item);
        } else {
            System.out.println("值 '" + arguments + "' 不在黑名单中");
        }
    }

    /**
     * 列出黑名单条目
     */
    private void listItems(String arguments) {
        List<BlacklistItem> items;

        if (arguments.isEmpty()) {
            items = manager.getAllItems();
        } else {
            BlacklistType type = BlacklistType.fromString(arguments);
            items = manager.findByType(type);
        }

        if (items.isEmpty()) {
            System.out.println("没有找到黑名单条目");
            return;
        }

        System.out.println("=== 黑名单条目列表 ===");
        for (int i = 0; i < items.size(); i++) {
            BlacklistItem item = items.get(i);
            System.out.printf("%d. ID: %s\n", i + 1, item.getId());
            System.out.printf("   值: %s\n", item.getValue());
            System.out.printf("   类型: %s\n", item.getType().getDescription());
            System.out.printf("   创建时间: %s\n", item.getCreateTime().format(DATE_FORMATTER));
            System.out.printf("   原因: %s\n", item.getReason());
            if (item.getExpiryTime() != null) {
                System.out.printf("   过期时间: %s\n", item.getExpiryTime().format(DATE_FORMATTER));
            }
            System.out.printf("   状态: %s\n", item.isValid() ? "有效" : "已过期");
            System.out.println();
        }
    }

    /**
     * 按类型查找条目
     */
    private void findItems(String arguments) {
        if (arguments.isEmpty()) {
            System.out.println("用法: find <类型>");
            return;
        }

        BlacklistType type = BlacklistType.fromString(arguments);
        List<BlacklistItem> items = manager.findByType(type);

        if (items.isEmpty()) {
            System.out.println("没有找到类型为 '" + type.getDescription() + "' 的黑名单条目");
            return;
        }

        System.out.println("=== 类型为 '" + type.getDescription() + "' 的黑名单条目 ===");
        for (int i = 0; i < items.size(); i++) {
            BlacklistItem item = items.get(i);
            System.out.printf("%d. %s (创建时间: %s)\n",
                    i + 1,
                    item.getValue(),
                    item.getCreateTime().format(DATE_FORMATTER));
        }
    }

    /**
     * 显示统计信息
     */
    private void showStatistics() {
        Map<String, Object> stats = manager.getStatistics();

        System.out.println("=== 黑名单统计信息 ===");
        System.out.printf("总条目数: %d\n", stats.get("totalItems"));
        System.out.printf("有效条目数: %d\n", stats.get("validItems"));
        System.out.printf("过期条目数: %d\n", stats.get("expiredItems"));

        @SuppressWarnings("unchecked")
        Map<BlacklistType, Long> typeStats = (Map<BlacklistType, Long>) stats.get("typeStatistics");
        System.out.println("\n按类型统计:");
        for (Map.Entry<BlacklistType, Long> entry : typeStats.entrySet()) {
            System.out.printf("  %s: %d\n", entry.getKey().getDescription(), entry.getValue());
        }
    }

    /**
     * 清理过期条目
     */
    private void cleanupExpired() {
        int removedCount = manager.cleanupExpiredItems();
        System.out.println("清理完成，删除了 " + removedCount + " 个过期条目");
    }

    /**
     * 清空所有条目
     */
    private void clearAll() {
        System.out.print("确定要清空所有黑名单条目吗？(yes/no): ");
        String confirmation = scanner.nextLine().trim();

        if ("yes".equalsIgnoreCase(confirmation)) {
            manager.clear();
            System.out.println("所有黑名单条目已清空");
        } else {
            System.out.println("操作已取消");
        }
    }

    /**
     * 保存数据
     */
    private void saveData() {
        boolean success = manager.saveToStorage();
        if (success) {
            System.out.println("数据保存成功");
        } else {
            System.out.println("数据保存失败");
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        boolean success = manager.loadFromStorage();
        if (success) {
            System.out.println("数据加载成功");
        } else {
            System.out.println("数据加载失败");
        }
    }

    /**
     * 退出程序
     */
    private void exit() {
        System.out.print("确定要退出吗？未保存的更改可能会丢失 (yes/no): ");
        String confirmation = scanner.nextLine().trim();

        if ("yes".equalsIgnoreCase(confirmation)) {
            running = false;
        }
    }

    /**
     * 打印命令提示符
     */
    private void printPrompt() {
        System.out.printf("blacklist[%d]> ", manager.size());
    }
}