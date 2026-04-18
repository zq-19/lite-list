package com.litelist.blacklist.storage;

import com.litelist.blacklist.model.BlacklistItem;
import com.litelist.blacklist.model.BlacklistType;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件的黑名单存储实现
 * 使用文本格式存储黑名单数据
 */
public class FileBlacklistStorage implements BlacklistStorage {
    private final String filePath;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FIELD_SEPARATOR = "|";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    public FileBlacklistStorage(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean save(List<BlacklistItem> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // 写入文件头
            writer.println("# Blacklist Data File");
            writer.println("# Format: id|value|type|createTime|reason|expiryTime");
            writer.println("# Created: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
            writer.println("#");

            // 写入数据
            for (BlacklistItem item : items) {
                writer.println(formatItem(item));
            }

            return true;
        } catch (IOException e) {
            System.err.println("保存黑名单数据失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<BlacklistItem> load() {
        List<BlacklistItem> items = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("黑名单文件不存在，将创建新文件");
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // 跳过注释行和空行
                if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }

                // 跳过文件头
                if (isFirstLine && line.contains("|")) {
                    isFirstLine = false;
                }

                BlacklistItem item = parseItem(line);
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("加载黑名单数据失败: " + e.getMessage());
        }

        return items;
    }

    @Override
    public boolean isAvailable() {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                return parent.mkdirs();
            }

            // 尝试创建/写入文件
            if (!file.exists()) {
                return file.createNewFile();
            }

            return file.canRead() && file.canWrite();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "File Storage (" + filePath + ")";
    }

    /**
     * 格式化黑名单条目为字符串
     */
    private String formatItem(BlacklistItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getId()).append(FIELD_SEPARATOR);
        sb.append(escapeValue(item.getValue())).append(FIELD_SEPARATOR);
        sb.append(item.getType()).append(FIELD_SEPARATOR);
        sb.append(item.getCreateTime().format(DATE_TIME_FORMATTER)).append(FIELD_SEPARATOR);
        sb.append(escapeValue(item.getReason() != null ? item.getReason() : "")).append(FIELD_SEPARATOR);

        if (item.getExpiryTime() != null) {
            sb.append(item.getExpiryTime().format(DATE_TIME_FORMATTER));
        }

        return sb.toString();
    }

    /**
     * 解析字符串为黑名单条目
     */
    private BlacklistItem parseItem(String line) {
        try {
            String[] parts = line.split("\\|", -1); // -1 保留空字段

            if (parts.length < 4) {
                System.err.println("无效的数据行: " + line);
                return null;
            }

            BlacklistItem item = new BlacklistItem();
            item.setId(parts[0]);
            item.setValue(unescapeValue(parts[1]));
            item.setType(BlacklistType.fromString(parts[2]));
            item.setCreateTime(LocalDateTime.parse(parts[3], DATE_TIME_FORMATTER));

            if (parts.length > 4 && !parts[4].isEmpty()) {
                item.setReason(unescapeValue(parts[4]));
            }

            if (parts.length > 5 && !parts[5].isEmpty()) {
                item.setExpiryTime(LocalDateTime.parse(parts[5], DATE_TIME_FORMATTER));
            }

            return item;
        } catch (Exception e) {
            System.err.println("解析数据行失败: " + line + ", 错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 转义值中的特殊字符
     */
    private String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\\n", "\\\\n");
    }

    /**
     * 反转义值中的特殊字符
     */
    private String unescapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\|", "|").replace("\\\\n", "\\n");
    }
}