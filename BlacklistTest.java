import com.litelist.blacklist.manager.BlacklistManager;
import com.litelist.blacklist.model.BlacklistItem;
import com.litelist.blacklist.model.BlacklistType;
import com.litelist.blacklist.storage.FileBlacklistStorage;

import java.time.LocalDateTime;

/**
 * 简单的黑名单系统测试程序
 */
public class BlacklistTest {
    public static void main(String[] args) {
        System.out.println("=== 黑名单系统测试 ===");

        // 创建黑名单管理器
        BlacklistManager manager = new BlacklistManager(new FileBlacklistStorage("test_blacklist.txt"));

        // 测试添加黑名单条目
        System.out.println("\n1. 添加黑名单条目测试:");
        boolean result1 = manager.addItem("192.168.1.100", BlacklistType.IP, "恶意访问");
        boolean result2 = manager.addItem("baduser", BlacklistType.USERNAME, "违规操作");
        boolean result3 = manager.addItem("spam@example.com", BlacklistType.EMAIL, "垃圾邮件");

        System.out.println("添加IP黑名单: " + (result1 ? "成功" : "失败"));
        System.out.println("添加用户名黑名单: " + (result2 ? "成功" : "失败"));
        System.out.println("添加邮箱黑名单: " + (result3 ? "成功" : "失败"));

        // 测试重复添加
        boolean duplicateResult = manager.addItem("192.168.1.100", BlacklistType.IP, "重复添加");
        System.out.println("重复添加测试: " + (duplicateResult ? "成功" : "失败（预期结果）"));

        // 测试查询功能
        System.out.println("\n2. 查询测试:");
        System.out.println("检查 192.168.1.100 是否在黑名单中: " + manager.contains("192.168.1.100"));
        System.out.println("检查 gooduser 是否在黑名单中: " + manager.contains("gooduser"));

        // 测试按类型查找
        System.out.println("\n3. 按类型查找测试:");
        java.util.List<com.litelist.blacklist.model.BlacklistItem> ipItems = manager.findByType(BlacklistType.IP);
        System.out.println("IP类型黑名单条目数量: " + ipItems.size());

        // 测试统计信息
        System.out.println("\n4. 统计信息:");
        java.util.Map<String, Object> stats = manager.getStatistics();
        System.out.println("总条目数: " + stats.get("totalItems"));
        System.out.println("有效条目数: " + stats.get("validItems"));

        // 测试过期功能
        System.out.println("\n5. 过期功能测试:");
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        BlacklistItem expiredItem = new BlacklistItem("expired@example.com", BlacklistType.EMAIL, "过期测试", pastTime);
        manager.addItem(expiredItem);

        System.out.println("添加过期条目前的总数: " + manager.size());
        int cleanedCount = manager.cleanupExpiredItems();
        System.out.println("清理过期条目数量: " + cleanedCount);
        System.out.println("清理后的总数: " + manager.size());

        // 测试删除功能
        System.out.println("\n6. 删除功能测试:");
        System.out.println("删除前的总数: " + manager.size());
        boolean removeResult = manager.removeItemByValue("baduser");
        System.out.println("删除 baduser: " + (removeResult ? "成功" : "失败"));
        System.out.println("删除后的总数: " + manager.size());

        // 显示所有条目
        System.out.println("\n7. 当前所有黑名单条目:");
        java.util.List<com.litelist.blacklist.model.BlacklistItem> allItems = manager.getAllItems();
        for (BlacklistItem item : allItems) {
            System.out.println("- " + item.getValue() + " (" + item.getType() + ") - " + item.getReason());
        }

        System.out.println("\n=== 测试完成 ===");
    }
}