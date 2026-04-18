package com.litelist.blacklist.model;

/**
 * 黑名单类型枚举
 * 定义不同类型的黑名单条目
 */
public enum BlacklistType {
    IP("IP地址"),
    USERNAME("用户名"),
    EMAIL("邮箱地址"),
    PHONE("手机号码"),
    DOMAIN("域名"),
    DEVICE_ID("设备ID"),
    OTHER("其他");

    private final String description;

    BlacklistType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据字符串值获取对应的枚举
     * @param type 类型字符串
     * @return 对应的BlacklistType，如果找不到返回OTHER
     */
    public static BlacklistType fromString(String type) {
        if (type == null) {
            return OTHER;
        }
        try {
            return BlacklistType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}