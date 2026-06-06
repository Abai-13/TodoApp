package com.todoapp.ui;

import com.todoapp.model.Todo;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 自定义表格模型，将 ArrayList<Todo> 映射到 JTable 的 5 列视图。
 */
public class TodoTableModel extends AbstractTableModel {

    /** 列名 */
    private static final String[] COLUMN_NAMES = {
            "标题", "优先级", "分类", "完成状态", "提醒时间"
    };

    /** 提醒时间的日期格式化器 */
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /** 数据源引用 */
    private final ArrayList<Todo> todoList;

    public TodoTableModel(ArrayList<Todo> todoList) {
        this.todoList = todoList;
    }

    // ========== AbstractTableModel 抽象方法实现 ==========

    @Override
    public int getRowCount() {
        return todoList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) {
            return Boolean.class;   // 完成状态列渲染为复选框
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // 只允许直接在表格中切换完成状态
        return columnIndex == 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Todo todo = todoList.get(rowIndex);
        switch (columnIndex) {
            case 0: return todo.getTitle();
            case 1: return todo.getPriority();
            case 2: return todo.getCategory();
            case 3: return todo.isCompleted();
            case 4: return formatRemindTime(todo.getRemindTime());
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 3 && aValue instanceof Boolean) {
            todoList.get(rowIndex).setCompleted((Boolean) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    // ========== 刷新方法 ==========

    /** 刷新整个表格（数据发生结构性变化时调用） */
    public void refresh() {
        fireTableDataChanged();
    }

    /** 刷新指定行 */
    public void updateRow(int row) {
        if (row >= 0 && row < todoList.size()) {
            fireTableRowsUpdated(row, row);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 将提醒时间戳转为可读字符串。
     * @param remindTime 毫秒时间戳，-1 表示无提醒
     * @return 格式化后的日期字符串，或 "无" / "已过期"
     */
    private String formatRemindTime(long remindTime) {
        if (remindTime == -1) {
            return "无";
        }
        if (remindTime < System.currentTimeMillis()) {
            return DATE_FORMAT.format(new Date(remindTime)) + " (已过期)";
        }
        return DATE_FORMAT.format(new Date(remindTime));
    }
}
