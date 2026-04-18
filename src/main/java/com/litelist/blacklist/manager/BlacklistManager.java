package com.litelist.blacklist.manager;

import com.litelist.blacklist.model.BlacklistItem;
import com.litelist.blacklist.model.BlacklistType;
import com.litelist.blacklist.storage.BlacklistStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 黑名单管理器
 * 负责黑名单的核心业务逻辑
 */
public class BlacklistManager {
    private final List<BlacklistItem> blacklistItems;
    private final BlacklistStorage storage;
    private boolean autoSave;

    public BlacklistManager(BlacklistStorage storage) {
        this.blacklistItems = new ArrayList<>();
        this.storage = storage;
        this.autoSave = true;
        loadFromStorage();
    }

    /**
     * 添加黑名单条目
     * @param item 黑名单条目
     * @return true 如果添加成功，false 如果已存在或添加失败
     */
    public boolean addItem(BlacklistItem item) {
        if (item == null || item.getValue() == null || item.getValue().trim().isEmpty()) {
            return false;
        }

        // 检查是否已存在
        if (contains(item.getValue())) {
            System.out.println("黑名单条目已存在: " + item.getValue());
            return false;
        }

        blacklistItems.add(item);

        if (autoSave) {
            saveToStorage();
        }

        return true;
    }

    /**
     * 添加黑名单条目（便捷方法）
     * @param value 黑名单值
     * @param type 类型
     * @param reason 原因
     * @return true 如果添加成功，false 如果失败
     */
    public boolean addItem(String value, BlacklistType type, String reason) {
        BlacklistItem item = new BlacklistItem(value, type, reason);
        return addItem(item);
    }

    /**
     * 添加带过期时间的黑名单条目
     * @param value 黑名单值
     * @param type 类型
     * @param reason 原因
     * @param expiryTime 过期时间
     * @return true 如果添加成功，false 如果失败
     */
    public boolean addItem(String value, BlacklistType type, String reason, LocalDateTime expiryTime) {
        BlacklistItem item = new BlacklistItem(value, type, reason, expiryTime);
        return addItem(item);
    }

    /**
     * 根据ID删除黑名单条目
     * @param id 条目ID
     * @return true 如果删除成功，false 如果未找到
     */
    public boolean removeItem(String id) {
        if (id == null) {
            return false;
        }

        boolean removed = blacklistItems.removeIf(item -> id.equals(item.getId()));

        if (removed && autoSave) {
            saveToStorage();
        }

        return removed;
    }

    /**
     * 根据值删除黑名单条目
     * @param value 黑名单值
     * @return true 如果删除成功，false 如果未找到
     */
    public boolean removeItemByValue(String value) {
        if (value == null) {
            return false;
        }

        boolean removed = blacklistItems.removeIf(item -> value.equals(item.getValue()));

        if (removed && autoSave) {
            saveToStorage();
        }

        return removed;
    }

    /**
     * 检查值是否在黑名单中
     * @param value 要检查的值
     * @return true 如果在黑名单中且未过期，false 如果不在或已过期
     */
    public boolean contains(String value) {
        if (value == null) {
            return false;
        }

        return blacklistItems.stream()
                .anyMatch(item -> value.equals(item.getValue()) && item.isValid());
    }

    /**
     * 根据类型查找黑名单条目
     * @param type 黑名单类型
     * @return 匹配类型的有效黑名单条目列表
     */
    public List<BlacklistItem> findByType(BlacklistType type) {
        return blacklistItems.stream()
                .filter(item -> item.getType() == type && item.isValid())
                .collect(Collectors.toList());
    }

    /**
     * 根据值查找黑名单条目
     * @param value 黑名单值
     * @return 匹配的黑名单条目，如果未找到返回null
     */
    public BlacklistItem findByValue(String value) {
        if (value == null) {
            return null;
        }

        return blacklistItems.stream()
                .filter(item -> value.equals(item.getValue()) && item.isValid())
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有有效的黑名单条目
     * @return 所有未过期的黑名单条目列表
     */
    public List<BlacklistItem> getAllItems() {
        return blacklistItems.stream()
                .filter(BlacklistItem::isValid)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有黑名单条目（包括已过期的）
     * @return 所有黑名单条目列表
     */
    public List<BlacklistItem> getAllItemsIncludingExpired() {
        return new ArrayList<>(blacklistItems);
    }

    /**
     * 清理已过期的黑名单条目
     * @return 清理的条目数量
     */
    public int cleanupExpiredItems() {
        int initialSize = blacklistItems.size();
        blacklistItems.removeIf(BlacklistItem::isExpired);
        int removedCount = initialSize - blacklistItems.size();

        if (removedCount > 0 && autoSave) {
            saveToStorage();
        }

        return removedCount;
    }

    /**
     * 获取黑名单统计信息
     * @return 包含统计信息的Map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalItems = blacklistItems.size();
        long validItems = getAllItems().size();
        long expiredItems = totalItems - validItems;

        stats.put("totalItems", totalItems);
        stats.put("validItems", validItems);
        stats.put("expiredItems", expiredItems);

        // 按类型统计
        Map<BlacklistType, Long> typeStats = blacklistItems.stream()
                .collect(Collectors.groupingBy(BlacklistItem::getType, Collectors.counting()));
        stats.put("typeStatistics", typeStats);

        return stats;
    }

    /**
     * 启用或禁用自动保存
     * @param autoSave true 启用自动保存，false 禁用
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    /**
     * 手动保存到存储
     * @return true 如果保存成功，false 如果失败
     */
    public boolean saveToStorage() {
        if (storage == null) {
            return false;
        }
        return storage.save(blacklistItems);
    }

    /**
     * 从存储加载数据
     * @return true 如果加载成功，false 如果失败
     */
    public boolean loadFromStorage() {
        if (storage == null || !storage.isAvailable()) {
            return false;
        }

        List<BlacklistItem> loadedItems = storage.load();
        blacklistItems.clear();
        blacklistItems.addAll(loadedItems);

        return true;
    }

    /**
     * 清空所有黑名单条目
     * @return true 如果清空成功，false 如果失败
     */
    public boolean clear() {
        blacklistItems.clear();

        if (autoSave) {
            return saveToStorage();
        }

        return true;
    }

    /**
     * 获取黑名单大小（仅有效条目）
     * @return 有效黑名单条目数量
     */
    public int size() {
        return (int) blacklistItems.stream().filter(BlacklistItem::isValid).count();
    }

    /**
     * 检查黑名单是否为空
     * @return true 如果为空，false 如果不为空
     */
    public boolean isEmpty() {
        return size() == 0;
    }
}