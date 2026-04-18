import com.litelist.blacklist.manager.BlacklistManager;
import com.litelist.blacklist.model.BlacklistItem;
import com.litelist.blacklist.model.BlacklistType;
import com.litelist.blacklist.storage.FileBlacklistStorage;

import java.time.LocalDateTime;

/**
 * 黑名单系统演示程序
 * 展示系统的所有主要功能
 */
public class BlacklistDemo {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║      轻量级黑名单管理系统演示        ║");
        System.out.println("╚════════════════════════════════════════╝");

        // 创建黑名单管理器
        BlacklistManager manager = new BlacklistManager(new FileBlacklistStorage("demo_blacklist.txt"));

        System.out.println("\n📝 创建黑名单管理器成功！");
        System.out.println("存储文件: demo_blacklist.txt");

        // 演示添加不同类型的黑名单条目
        System.out.println("\n🔍 === 添加黑名单条目演示 ===");

        // 添加IP黑名单
        manager.addItem("192.168.1.100", BlacklistType.IP, "恶意扫描");
        manager.addItem("10.0.0.50", BlacklistType.IP, "DDoS攻击");
        System.out.println("✅ 添加了2个IP黑名单条目");

        // 添加用户名黑名单
        manager.addItem("hacker123", BlacklistType.USERNAME, "暴力破解尝试");
        manager.addItem("spammer_bot", BlacklistType.USERNAME, "自动垃圾邮件");
        System.out.println("✅ 添加了2个用户名黑名单条目");

        // 添加邮箱黑名单
        manager.addItem("spam@evil.com", BlacklistType.EMAIL, "垃圾邮件发送者");
        manager.addItem("phishing@scam.com", BlacklistType.EMAIL, "钓鱼邮件");
        System.out.println("✅ 添加了2个邮箱黑名单条目");

        // 添加设备ID黑名单
        manager.addItem("DEVICE-001", BlacklistType.DEVICE_ID, "恶意设备");
        System.out.println("✅ 添加了1个设备ID黑名单条目");

        // 演示查询功能
        System.out.println("\n🔍 === 查询功能演示 ===");
        System.out.println("查询 '192.168.1.100' 是否在黑名单中: " + manager.contains("192.168.1.100"));
        System.out.println("查询 'gooduser' 是否在黑名单中: " + manager.contains("gooduser"));

        // 演示按类型查找
        System.out.println("\n📋 === 按类型查找演示 ===");
        System.out.println("IP类型黑名单条目:");
        java.util.List<BlacklistItem> ipItems = manager.findByType(BlacklistType.IP);
        for (BlacklistItem item : ipItems) {
            System.out.println("  - " + item.getValue() + " (原因: " + item.getReason() + ")");
        }

        // 演示统计功能
        System.out.println("\n📊 === 统计信息演示 ===");
        java.util.Map<String, Object> stats = manager.getStatistics();
        System.out.println("总条目数: " + stats.get("totalItems"));
        System.out.println("有效条目数: " + stats.get("validItems"));
        System.out.println("过期条目数: " + stats.get("expiredItems"));

        // 演示过期功能
        System.out.println("\n⏰ === 过期功能演示 ===");
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        // 添加一个已过期的条目
        BlacklistItem expiredItem = new BlacklistItem("expired@test.com", BlacklistType.EMAIL, "测试过期", yesterday);
        manager.addItem(expiredItem);

        // 添加一个未来的过期条目
        BlacklistItem futureItem = new BlacklistItem("future@test.com", BlacklistType.EMAIL, "测试未来过期", tomorrow);
        manager.addItem(futureItem);

        System.out.println("添加过期和未来过期条目后:");
        System.out.println("总条目数: " + stats.get("totalItems"));
        System.out.println("有效条目数: " + manager.size());

        // 清理过期条目
        int cleanedCount = manager.cleanupExpiredItems();
        System.out.println("清理了 " + cleanedCount + " 个过期条目");

        // 演示删除功能
        System.out.println("\n🗑️  === 删除功能演示 ===");
        System.out.println("删除前条目数: " + manager.size());
        boolean removed = manager.removeItemByValue("hacker123");
        System.out.println("删除 'hacker123': " + (removed ? "成功" : "失败"));
        System.out.println("删除后条目数: " + manager.size());

        // 演示数据持久化
        System.out.println("\n💾 === 数据持久化演示 ===");
        boolean saved = manager.saveToStorage();
        System.out.println("保存数据到文件: " + (saved ? "成功" : "失败"));

        // 显示最终结果
        System.out.println("\n🎉 === 最终黑名单状态 ===");
        java.util.List<BlacklistItem> allItems = manager.getAllItems();
        System.out.println("当前黑名单包含 " + allItems.size() + " 个有效条目:");
        for (BlacklistItem item : allItems) {
            String expiryInfo = item.getExpiryTime() != null ?
                " (过期时间: " + item.getExpiryTime().toString() + ")" : " (永不过期)";
            System.out.println("  • " + item.getValue() +
                             " [" + item.getType().getDescription() + "] " +
                             item.getReason() + expiryInfo);
        }

        // 验证文件保存
        System.out.println("\n📄 === 文件内容验证 ===");
        java.io.File file = new java.io.File("demo_blacklist.txt");
        if (file.exists()) {
            System.out.println("✅ 黑名单文件已成功创建");
            System.out.println("文件大小: " + file.length() + " 字节");
        } else {
            System.out.println("❌ 黑名单文件创建失败");
        }

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           演示完成！                   ║");
        System.out.println("║  轻量级黑名单系统功能正常！           ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
}