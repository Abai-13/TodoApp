package com.todoapp.model;

import java.util.Date;
import java.util.UUID;

/**
 * Todo 数据模型类，表示一条待办事项。
 * 所有字段均为 private，通过 getter/setter 访问。
 */
public class Todo {

    // ========== 字段 ==========

    /** 唯一标识，使用 UUID 字符串 */
    private String id;

    /** 标题 */
    private String title;

    /** 是否已完成 */
    private boolean completed;

    /** 优先级：高 / 中 / 低 */
    private String priority;

    /** 分类：工作 / 个人 / 购物 等 */
    private String category;

    /** 提醒时间（毫秒时间戳），-1 表示无提醒 */
    private long remindTime;

    /** 是否已提醒过 */
    private boolean reminded;

    /** 创建时间 */
    private Date createTime;

    // ========== 构造方法 ==========

    /** 无参构造，自动生成 id 和 createTime，设置默认值 */
    public Todo() {
        this.id = UUID.randomUUID().toString();
        this.completed = false;
        this.priority = "中";
        this.category = "未分类";
        this.remindTime = -1;
        this.reminded = false;
        this.createTime = new Date();
    }

    /**
     * 带标题的构造方法
     * @param title 待办事项标题
     */
    public Todo(String title) {
        this();
        this.title = title;
    }

    /**
     * 全参构造方法
     * @param id         唯一标识
     * @param title      标题
     * @param completed  是否完成
     * @param priority   优先级
     * @param category   分类
     * @param remindTime 提醒时间戳（-1 表示无提醒）
     * @param reminded   是否已提醒
     * @param createTime 创建时间
     */
    public Todo(String id, String title, boolean completed, String priority,
                String category, long remindTime, boolean reminded, Date createTime) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();
        this.title = title;
        this.completed = completed;
        this.priority = (priority != null) ? priority : "中";
        this.category = (category != null) ? category : "未分类";
        this.remindTime = remindTime;
        this.reminded = reminded;
        this.createTime = (createTime != null) ? createTime : new Date();
    }

    // ========== Getter / Setter ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(long remindTime) {
        this.remindTime = remindTime;
    }

    public boolean isReminded() {
        return reminded;
    }

    public void setReminded(boolean reminded) {
        this.reminded = reminded;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    // ========== 重写方法 ==========

    @Override
    public String toString() {
        return "Todo{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", priority='" + priority + '\'' +
                ", category='" + category + '\'' +
                ", remindTime=" + remindTime +
                ", reminded=" + reminded +
                ", createTime=" + createTime +
                '}';
    }
}
