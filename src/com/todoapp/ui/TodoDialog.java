package com.todoapp.ui;

import com.todoapp.model.Todo;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

/**
 * 添加 / 编辑 Todo 的对话框。
 * 使用 GridBagLayout 布局，包含标题、优先级、分类、提醒时间四个输入项。
 */
public class TodoDialog extends JDialog {

    // ========== 输入组件 ==========
    private final JTextField titleField = new JTextField(20);
    private final JComboBox<String> priorityCombo = new JComboBox<>(
            new String[]{"高", "中", "低"});
    private final JComboBox<String> categoryCombo = new JComboBox<>(
            new String[]{"工作", "个人", "购物", "学习", "健康"});
    private final JCheckBox reminderCheck = new JCheckBox("设置提醒");
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner.DateEditor dateEditor =
            new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm");

    /** 对话框返回结果：true = 确认, false = 取消 */
    private boolean confirmed = false;

    /**
     * 构造"添加"对话框。
     * @param owner 父窗口
     */
    public TodoDialog(Frame owner) {
        super(owner, "添加待办事项", true);
        initUI();
        // 默认提醒设为明天此时
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        dateSpinner.setValue(cal.getTime());
        reminderCheck.setSelected(false);
        updateSpinnerState();
    }

    /**
     * 构造"编辑"对话框，预填原有数据。
     * @param owner 父窗口
     * @param todo  要编辑的 Todo 对象
     */
    public TodoDialog(Frame owner, Todo todo) {
        super(owner, "编辑待办事项", true);
        initUI();
        // 预填数据
        titleField.setText(todo.getTitle());
        priorityCombo.setSelectedItem(todo.getPriority());
        categoryCombo.setSelectedItem(todo.getCategory());

        if (todo.getRemindTime() != -1) {
            reminderCheck.setSelected(true);
            dateSpinner.setValue(new Date(todo.getRemindTime()));
        } else {
            reminderCheck.setSelected(false);
        }
        updateSpinnerState();
    }

    /** 初始化界面布局 */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ---- 表单面板 ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("标题："), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

        // 优先级
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("优先级："), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(priorityCombo, gbc);

        // 分类
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("分类："), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        categoryCombo.setEditable(true);
        JButton newCategoryButton = new JButton("新分类");
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
        categoryPanel.add(categoryCombo, BorderLayout.CENTER);
        categoryPanel.add(newCategoryButton, BorderLayout.EAST);
        formPanel.add(categoryPanel, gbc);
        // "新分类"按钮：弹出输入框添加自定义分类
        newCategoryButton.addActionListener(e -> addNewCategory());

        // 提醒
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("提醒："), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel reminderPanel = new JPanel(new BorderLayout(5, 0));
        reminderPanel.add(reminderCheck, BorderLayout.WEST);
        dateSpinner.setEditor(dateEditor);
        reminderPanel.add(dateSpinner, BorderLayout.CENTER);
        formPanel.add(reminderPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ---- 按钮面板 ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        okButton.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "标题不能为空！", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 提醒复选框切换日期选择器可用状态
        reminderCheck.addActionListener(e -> updateSpinnerState());

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    /**
     * 弹出输入框让用户添加新分类，添加到下拉框并选中。
     * 若用户取消或输入空字符串则不做任何操作。
     */
    private void addNewCategory() {
        String newCat = JOptionPane.showInputDialog(this,
                "请输入新分类名称：", "添加新分类", JOptionPane.PLAIN_MESSAGE);
        if (newCat != null && !newCat.trim().isEmpty()) {
            String trimmed = newCat.trim();
            // 避免重复添加
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).equals(trimmed)) {
                    categoryCombo.setSelectedIndex(i);
                    return;
                }
            }
            categoryCombo.addItem(trimmed);
            categoryCombo.setSelectedItem(trimmed);
        }
    }

    /** 根据复选框状态启用/禁用日期选择器 */
    private void updateSpinnerState() {
        dateSpinner.setEnabled(reminderCheck.isSelected());
    }

    // ========== 结果获取 ==========

    public boolean isConfirmed() {
        return confirmed;
    }

    /** 将用户输入填入已有 Todo 对象（编辑模式） */
    public void fillTodo(Todo todo) {
        todo.setTitle(titleField.getText().trim());
        todo.setPriority((String) priorityCombo.getSelectedItem());
        todo.setCategory((String) categoryCombo.getSelectedItem());
        if (reminderCheck.isSelected()) {
            todo.setRemindTime(((Date) dateSpinner.getValue()).getTime());
        } else {
            todo.setRemindTime(-1);
        }
    }

    /** 根据用户输入创建新的 Todo 对象（添加模式） */
    public Todo createTodo() {
        Todo todo = new Todo(titleField.getText().trim());
        todo.setPriority((String) priorityCombo.getSelectedItem());
        todo.setCategory((String) categoryCombo.getSelectedItem());
        if (reminderCheck.isSelected()) {
            todo.setRemindTime(((Date) dateSpinner.getValue()).getTime());
        }
        return todo;
    }
}
