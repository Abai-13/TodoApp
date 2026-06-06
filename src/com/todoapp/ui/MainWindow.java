package com.todoapp.ui;

import com.todoapp.model.Todo;
import com.todoapp.storage.Storage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 主窗口，包含待办事项表格和操作按钮面板。
 */
public class MainWindow extends JFrame {

    // ========== 数据 ==========
    private final ArrayList<Todo> todoList;

    // ========== UI 组件 ==========
    private final TodoTableModel tableModel;
    private final JTable table;
    /** 表格排序器（保存为字段以支持动态筛选） */
    private TableRowSorter<TodoTableModel> sorter;

    // ========== 搜索与筛选 ==========
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> statusFilterCombo = new JComboBox<>(
            new String[]{"全部", "未完成", "已完成"});

    // ========== 系统托盘 ==========
    /** 系统托盘图标，null 表示系统不支持托盘 */
    private TrayIcon trayIcon;
    /** 托盘是否已初始化（用于判断是否需要清理） */
    private boolean trayEnabled = false;

    // ========== 提醒定时器 ==========
    /** 后台定时扫描器，每 60 秒检查一次是否有到期的提醒 */
    private final Timer reminderTimer;

    // ========== 按钮 ==========
    private final JButton addButton    = new JButton("添加");
    private final JButton editButton   = new JButton("编辑");
    private final JButton deleteButton = new JButton("删除");
    private final JButton toggleButton = new JButton("标记完成/未完成");
    private final JButton clearButton  = new JButton("清理已完成");

    // ========== 构造 ==========

    public MainWindow() {
        // ---- 加载数据 ----
        todoList = Storage.load();

        // ---- 表格模型与表格 ----
        tableModel = new TodoTableModel(todoList);
        table = new JTable(tableModel);
        setupTable();

        // ---- 窗口设置 ----
        setTitle("TodoApp — 待办事项管理");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);                // 居中
        setLayout(new BorderLayout(5, 5));

        // 关闭窗口 → 隐藏到系统托盘
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hideToTray();
            }
        });

        // ---- 布局 ----
        add(createFilterPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        // ---- 键盘快捷键 ----
        setupKeyBindings();

        // ---- 系统托盘 ----
        setupSystemTray();

        // ---- 提醒定时扫描 ----
        reminderTimer = new Timer(60_000, e -> scanReminders());
        reminderTimer.setInitialDelay(5_000);   // 启动 5 秒后首次扫描
        reminderTimer.start();
    }

    // ========== 表格配置 ==========

    private void setupTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        // 列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(250); // 标题
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  // 优先级
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // 分类
        table.getColumnModel().getColumn(3).setPreferredWidth(70);  // 完成状态
        table.getColumnModel().getColumn(4).setPreferredWidth(160); // 提醒时间

        // ---- 排序器 ----
        sorter = new TableRowSorter<>(tableModel);

        // 优先级列（索引 1）自定义比较器：高 > 中 > 低
        Map<String, Integer> priorityOrder = new HashMap<>();
        priorityOrder.put("高", 1);
        priorityOrder.put("中", 2);
        priorityOrder.put("低", 3);
        sorter.setComparator(1, Comparator.comparing(
                (String p) -> priorityOrder.getOrDefault(p, 99)));

        // 完成状态列（索引 3）Boolean 排序（已完成排前面）
        sorter.setComparator(3, (a, b) -> {
            if (a instanceof Boolean && b instanceof Boolean) {
                return ((Boolean) b).compareTo((Boolean) a); // true 在前
            }
            return 0;
        });

        // 提醒时间列（索引 4）排序
        // 格式化字符串形如 "yyyy-MM-dd HH:mm"、"无"、"... (已过期)"
        sorter.setComparator(4, (o1, o2) -> {
            String a = (String) o1, b = (String) o2;
            if (a.equals("无") && b.equals("无")) return 0;
            if (a.equals("无")) return 1;  // "无" 排最后
            if (b.equals("无")) return -1;
            a = a.replace(" (已过期)", "");
            b = b.replace(" (已过期)", "");
            return a.compareTo(b);
        });

        // 默认按标题列（索引 0）升序
        sorter.setSortKeys(java.util.List.of(
                new RowSorter.SortKey(0, SortOrder.ASCENDING)));

        table.setRowSorter(sorter);

        // 双击行触发编辑（注意：排序后需转换行索引）
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelected();
                }
            }
        });
    }

    // ========== 按钮面板 ==========

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(toggleButton);
        panel.add(clearButton);

        addButton.addActionListener(e -> addTodo());
        editButton.addActionListener(e -> editSelected());
        deleteButton.addActionListener(e -> deleteSelected());
        toggleButton.addActionListener(e -> toggleSelected());
        clearButton.addActionListener(e -> clearCompleted());

        return panel;
    }

    // ========== 搜索与筛选面板 ==========

    /** 创建顶部搜索 + 筛选面板。 */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // 搜索标题
        panel.add(new JLabel("搜索标题："));
        panel.add(searchField);

        // 完成状态筛选
        panel.add(new JLabel("状态："));
        panel.add(statusFilterCombo);

        // 清除筛选
        JButton clearFilterButton = new JButton("清除筛选");
        panel.add(clearFilterButton);

        // ---- 监听器 ----

        // 搜索框：DocumentListener 实时筛选
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // 状态下拉框：选择变化时重新筛选
        statusFilterCombo.addActionListener(e -> applyFilter());

        // 清除按钮
        clearFilterButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
        });

        return panel;
    }

    /**
     * 组合搜索文字（标题列，索引 0）与完成状态（列索引 3）的 RowFilter，
     * 通过 {@code RowFilter.andFilter} 叠加后应用到表格排序器。
     */
    @SuppressWarnings("unchecked")
    private void applyFilter() {
        String keyword = searchField.getText().trim();
        String status = (String) statusFilterCombo.getSelectedItem();

        List<RowFilter<TodoTableModel, Integer>> filters = new ArrayList<>();

        // 1. 标题搜索（大小写不敏感，正则转义防特殊字符）
        if (!keyword.isEmpty()) {
            String escaped = Pattern.quote(keyword);
            filters.add(RowFilter.regexFilter("(?i)" + escaped, 0));
        }

        // 2. 完成状态筛选
        if ("未完成".equals(status)) {
            filters.add(RowFilter.regexFilter("^false$", 3));
        } else if ("已完成".equals(status)) {
            filters.add(RowFilter.regexFilter("^true$", 3));
        }
        // "全部" 不添加状态筛选

        // 应用组合筛选（空 filters 时清除筛选）
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // ========== 键盘快捷键 ==========

    private void setupKeyBindings() {
        JRootPane root = getRootPane();

        // Ctrl+N / Cmd+N : 添加
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl N"), "add");
        root.getActionMap().put("add", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                addTodo();
            }
        });

        // Delete 键 : 删除
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("DELETE"), "delete");
        root.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelected();
            }
        });
    }

    // ========== 系统托盘 ==========

    /** 初始化系统托盘：创建图标、右键菜单，注册到 SystemTray。 */
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("[Tray] 当前系统不支持系统托盘，将使用普通窗口模式。");
            return;
        }

        // 创建托盘图标（16×16 程序化生成，避免依赖外部图片文件）
        Image image = createTrayImage();

        // 右键弹出菜单
        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("显示主窗口");
        showItem.addActionListener(e -> showMainWindow());
        popup.add(showItem);

        popup.addSeparator();

        MenuItem exitItem = new MenuItem("退出程序");
        exitItem.addActionListener(e -> exitApp());
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "TodoApp — 待办事项管理", popup);
        trayIcon.setImageAutoSize(true);  // 根据系统托盘区域自适应大小

        // 双击托盘图标 → 显示主窗口
        trayIcon.addActionListener(e -> showMainWindow());

        try {
            SystemTray.getSystemTray().add(trayIcon);
            trayEnabled = true;
            System.out.println("[Tray] 系统托盘已就绪。");
        } catch (AWTException e) {
            System.err.println("[Tray] 无法添加到系统托盘：" + e.getMessage());
            trayIcon = null;
        }
    }

    /** 程序化生成 16×16 托盘图标：渐变蓝色圆角方块 + 白色勾。 */
    private Image createTrayImage() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 蓝色圆角背景
        g2.setColor(new Color(0x2196F3));
        g2.fillRoundRect(0, 0, size, size, 4, 4);

        // 白色勾号
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(4, 8, 7, 11);
        g2.drawLine(7, 11, 12, 5);

        g2.dispose();
        return img;
    }

    /** 隐藏窗口到系统托盘，显示提示气泡。 */
    private void hideToTray() {
        setVisible(false);
        if (trayEnabled && trayIcon != null) {
            trayIcon.displayMessage("TodoApp",
                    "程序已最小化到系统托盘，双击图标可重新打开。",
                    TrayIcon.MessageType.INFO);
        }
    }

    /** 从托盘恢复主窗口。 */
    private void showMainWindow() {
        setVisible(true);
        setExtendedState(NORMAL);
        toFront();
        requestFocus();
    }

    /** 退出程序：清理托盘并退出 JVM。 */
    private void exitApp() {
        if (trayEnabled && trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        System.exit(0);
    }

    // ========== 操作逻辑 ==========

    /** 添加新的待办事项 */
    private void addTodo() {
        TodoDialog dialog = new TodoDialog(this);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            todoList.add(dialog.createTodo());
            Storage.save(todoList);
            tableModel.refresh();
            // 滚动到新增行（转为视图索引以兼容排序状态）
            int modelRow = todoList.size() - 1;
            int viewRow = table.convertRowIndexToView(modelRow);
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
        }
    }

    /**
     * 获取当前选中行在模型（todoList）中的索引。
     * 若表格已排序，自动将视图索引转为模型索引。
     * @return 模型行索引，-1 表示无选中
     */
    private int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return -1;
        return table.convertRowIndexToModel(viewRow);
    }

    /** 编辑选中的待办事项 */
    private void editSelected() {
        int row = getSelectedModelRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一条待办事项。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Todo todo = todoList.get(row);
        TodoDialog dialog = new TodoDialog(this, todo);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            dialog.fillTodo(todo);
            Storage.save(todoList);
            tableModel.updateRow(row);
        }
    }

    /** 删除选中的待办事项 */
    private void deleteSelected() {
        int row = getSelectedModelRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一条待办事项。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Todo todo = todoList.get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "确定要删除 \"" + todo.getTitle() + "\" 吗？",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            todoList.remove(row);
            Storage.save(todoList);
            tableModel.refresh();
        }
    }

    /** 切换选中行的完成状态 */
    private void toggleSelected() {
        int row = getSelectedModelRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一条待办事项。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Todo todo = todoList.get(row);
        todo.setCompleted(!todo.isCompleted());
        Storage.save(todoList);
        tableModel.updateRow(row);
    }

    /** 清理所有已完成的待办事项 */
    private void clearCompleted() {
        long count = todoList.stream().filter(Todo::isCompleted).count();
        if (count == 0) {
            JOptionPane.showMessageDialog(this,
                    "没有已完成的待办事项。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "确定要删除所有 " + count + " 条已完成的待办事项吗？\n此操作不可撤销。",
                "确认清理", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            todoList.removeIf(Todo::isCompleted);
            Storage.save(todoList);
            tableModel.refresh();
        }
    }

    // ========== 提醒扫描 ==========

    /** 后台定时扫描：找出所有到期且未提醒的待办，触发通知。 */
    private void scanReminders() {
        long now = System.currentTimeMillis();
        List<Todo> dueList = new ArrayList<>();

        for (Todo todo : todoList) {
            if (todo.getRemindTime() != -1
                    && todo.getRemindTime() <= now
                    && !todo.isReminded()) {
                dueList.add(todo);
            }
        }

        if (dueList.isEmpty()) return;

        // 标记已提醒并持久化
        for (Todo todo : dueList) {
            todo.setReminded(true);
        }
        Storage.save(todoList);

        // 展示通知（阶段 6 将改为系统托盘气泡通知）
        showReminderNotification(dueList);
    }

    /**
     * 弹出提醒通知。优先使用系统托盘气泡；若托盘不可用则降级为对话框。
     * 将多个到期的提醒合并到一条消息中展示。
     */
    private void showReminderNotification(List<Todo> dueList) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");

        // 构建标题行
        StringBuilder title = new StringBuilder();
        title.append(dueList.get(0).getTitle());
        if (dueList.size() > 1) {
            title.append(" 等 ").append(dueList.size()).append(" 条待办");
        }

        // 构建消息体
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < dueList.size(); i++) {
            Todo t = dueList.get(i);
            String timeStr = (t.getRemindTime() != -1)
                    ? fmt.format(new Date(t.getRemindTime()))
                    : "";
            body.append("• ").append(t.getTitle())
                    .append("  [").append(t.getPriority()).append("]");
            if (!timeStr.isEmpty()) {
                body.append("  ⏰ ").append(timeStr);
            }
            if (i < dueList.size() - 1) body.append("\n");
        }

        if (trayEnabled && trayIcon != null) {
            // 系统托盘气泡通知
            trayIcon.displayMessage(
                    "⏰ " + title.toString(),
                    body.toString(),
                    TrayIcon.MessageType.INFO);
        } else {
            // 降级：对话框通知
            JOptionPane.showMessageDialog(this,
                    "以下 " + dueList.size() + " 条待办事项已到期：\n\n" + body,
                    "⏰ 待办提醒",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // 刷新表格以反映 reminded 状态变化（虽然该字段不在表格列中，但保持一致性）
        tableModel.refresh();
    }

    /** 停止提醒定时器（阶段 6 窗口隐藏到托盘时调用） */
    public void stopReminderTimer() {
        if (reminderTimer != null && reminderTimer.isRunning()) {
            reminderTimer.stop();
        }
    }

    /** 重新启动提醒定时器（阶段 6 从托盘恢复窗口时调用） */
    public void restartReminderTimer() {
        if (reminderTimer != null && !reminderTimer.isRunning()) {
            reminderTimer.start();
        }
    }
}
