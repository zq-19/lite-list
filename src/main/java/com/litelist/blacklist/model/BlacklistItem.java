package com.litelist.blacklist.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 黑名单条目类
 * 表示一个黑名单中的项目
 */
public class BlacklistItem {
    private String id;
    private String value;
    private BlacklistType type;
    private LocalDateTime createTime;
    private String reason;
    private LocalDateTime expiryTime;

    public BlacklistItem() {
        this.id = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
    }

    public BlacklistItem(String value, BlacklistType type, String reason) {
        this();
        this.value = value;
        this.type = type;
        this.reason = reason;
    }

    public BlacklistItem(String value, BlacklistType type, String reason, LocalDateTime expiryTime) {
        this(value, type, reason);
        this.expiryTime = expiryTime;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BlacklistType getType() {
        return type;
    }

    public void setType(BlacklistType type) {
        this.type = type;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * 检查黑名单条目是否已过期
     * @return true 如果已过期，false 如果未过期或永不过期
     */
    public boolean isExpired() {
        if (expiryTime == null) {
            return false; // 永不过期
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * 检查黑名单条目是否有效（未过期）
     * @return true 如果有效，false 如果已过期
     */
    public boolean isValid() {
        return !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlacklistItem that = (BlacklistItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(value, that.value) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, type);
    }

    @Override
    public String toString() {
        return "BlacklistItem{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                ", type=" + type +
                ", createTime=" + createTime +
                ", reason='" + reason + '\'' +
                ", expiryTime=" + expiryTime +
                ", expired=" + isExpired() +
                '}';
    }
}