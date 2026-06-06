package com.todoapp;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.todoapp.ui.MainWindow;

import javax.swing.*;

/**
 * 程序入口：设置 FlatLaf 苹果风格主题，启动主窗口。
 *
 * 运行方式（项目根目录下）：
 *   javac -encoding UTF-8 -cp "lib/*" -d out src/com/todoapp/*.java src/com/todoapp/model/*.java src/com/todoapp/storage/*.java src/com/todoapp/ui/*.java
 *   java -cp "out;lib/*" com.todoapp.Main
 */
public class Main {

    public static void main(String[] args) {
        // 在创建任何 Swing 组件之前设置 Look & Feel
        FlatMacLightLaf.setup();

        // 可选：微调全局字体
        UIManager.put("defaultFont", new java.awt.Font(
                "Microsoft YaHei", java.awt.Font.PLAIN, 14));

        // 在 EDT 线程中启动窗口
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
