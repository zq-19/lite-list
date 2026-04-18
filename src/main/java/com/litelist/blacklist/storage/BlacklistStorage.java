package com.litelist.blacklist.storage;

import com.litelist.blacklist.model.BlacklistItem;

import java.util.List;

/**
 * 黑名单存储接口
 * 定义黑名单数据的持久化操作
 */
public interface BlacklistStorage {

    /**
     * 保存黑名单条目列表
     * @param items 黑名单条目列表
     * @return true 如果保存成功，false 如果保存失败
     */
    boolean save(List<BlacklistItem> items);

    /**
     * 从存储中加载所有黑名单条目
     * @return 黑名单条目列表，如果加载失败返回空列表
     */
    List<BlacklistItem> load();

    /**
     * 检查存储是否可用
     * @return true 如果存储可用，false 如果不可用
     */
    boolean isAvailable();

    /**
     * 获取存储类型描述
     * @return 存储类型描述
     */
    String getStorageType();
}