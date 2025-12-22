package edu.sustech.xiangqi.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sustech.xiangqi.XiangqiApplication;
import edu.sustech.xiangqi.model.ChessBoardModel;
import edu.sustech.xiangqi.model.GameConfig;
import edu.sustech.xiangqi.model.Save;
import edu.sustech.xiangqi.model.SaveManager;
import edu.sustech.xiangqi.model.user.User;
import edu.sustech.xiangqi.model.user.UserManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainMenuFrame extends JFrame {
    private User currentUser;
    private UserManager userManager;
    private SaveManager saveManager;

    public MainMenuFrame(User user, UserManager userManager) {
        this.currentUser = user;
        this.userManager = userManager;
        this.saveManager = new SaveManager();

        setTitle("中国象棋 - 主菜单 (" + user.getUsername() + ")");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeUI();
    }

    private void initializeUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 开始游戏页
        JPanel gamePanel = new JPanel(new GridLayout(4, 1, 10, 10));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton btnNormal = new JButton("普通模式 (无限制)");
        JButton btnTimed = new JButton("计时赛 (快棋/包干)");
        JButton btnAI = new JButton("人机对战");
        JButton btnOnline = new JButton("联网对战");

        styleButton(btnNormal, new Color(70, 130, 180));
        styleButton(btnTimed, new Color(255, 140, 0));
        styleButton(btnAI, Color.GRAY);
        styleButton(btnOnline, Color.GRAY);

        btnNormal.addActionListener(e -> startGame(new GameConfig()));

        btnTimed.addActionListener(e -> showTimeSelectionDialog());

        btnAI.addActionListener(e -> JOptionPane.showMessageDialog(this, "人机功能开发中..."));
        btnOnline.addActionListener(e -> JOptionPane.showMessageDialog(this, "联网功能开发中..."));

        gamePanel.add(btnNormal);
        gamePanel.add(btnTimed);
        gamePanel.add(btnAI);
        gamePanel.add(btnOnline);

        tabbedPane.addTab("开始游戏", gamePanel);

        // 账户管理页
        if (!currentUser.isGuest) {
            JPanel settingsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

            JButton btnChangePwd = new JButton("修改密码");
            JButton btnManageSaves = new JButton("存档管理");

            btnChangePwd.addActionListener(e -> showChangePasswordDialog());
            btnManageSaves.addActionListener(e -> showSaveManagerDialog());

            settingsPanel.add(btnChangePwd);
            settingsPanel.add(btnManageSaves);
            // 占位
            settingsPanel.add(new JLabel(""));

            JButton btnLogout = new JButton("注销登录");
            btnLogout.setForeground(Color.RED);
            btnLogout.addActionListener(e -> {
                dispose();
                XiangqiApplication.main(null); // 重启应用
            });
            settingsPanel.add(btnLogout);

            tabbedPane.addTab("账户管理", settingsPanel);
        } else {
            JPanel guestPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("游客模式无法管理账户", JLabel.CENTER);
            JButton btnLogout = new JButton("退出登录");
            btnLogout.addActionListener(e -> {
                dispose();
                XiangqiApplication.main(null);
            });
            guestPanel.add(label, BorderLayout.CENTER);
            guestPanel.add(btnLogout, BorderLayout.SOUTH);
            tabbedPane.addTab("账户管理", guestPanel);
        }

        add(tabbedPane);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("宋体", Font.BOLD, 16));
        btn.setFocusPainted(false);
    }

    // 弹出时间选择对话框
    private void showTimeSelectionDialog() {
        JDialog dialog = new JDialog(this, "选择赛制", true);
        dialog.setLayout(new GridLayout(3, 1, 10, 10));
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        JButton btnRapid = new JButton("快棋 (10分钟包干)");
        btnRapid.addActionListener(e -> {
            dialog.dispose();
            startGame(new GameConfig(10, 0));
        });

        JButton btnBlitz = new JButton("超快棋 (5分钟 + 3秒)");
        btnBlitz.addActionListener(e -> {
            dialog.dispose();
            startGame(new GameConfig(5, 3));
        });

        JButton btnCustom = new JButton("自定义...");
        btnCustom.addActionListener(e -> {
            String minStr = JOptionPane.showInputDialog(dialog, "输入初始分钟数:");
            String incStr = JOptionPane.showInputDialog(dialog, "输入每步加秒数:");
            try {
                int min = Integer.parseInt(minStr);
                int inc = Integer.parseInt(incStr);
                dialog.dispose();
                startGame(new GameConfig(min, inc));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "输入无效");
            }
        });

        dialog.add(btnRapid);
        dialog.add(btnBlitz);
        dialog.add(btnCustom);
        dialog.setVisible(true);
    }

    private void startGame(GameConfig config) {
        // 关闭主菜单，打开游戏窗口
        this.dispose();

        // 修改 GameFrame 构造函数来接收 config
        GameFrame gameFrame = new GameFrame("中国象棋", currentUser, config);

        // 设置关闭操作 (返回主菜单而不是退出程序!!!)
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (gameFrame.checkAndSaveOnExit()) {
                    gameFrame.dispose();
                    new MainMenuFrame(currentUser, userManager).setVisible(true);
                }
            }
        });

        gameFrame.setVisible(true);
    }

    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JPasswordField oldPf = new JPasswordField();
        JPasswordField newPf = new JPasswordField();
        panel.add(new JLabel("旧密码:")); panel.add(oldPf);
        panel.add(new JLabel("新密码:")); panel.add(newPf);

        int result = JOptionPane.showConfirmDialog(this, panel, "修改密码", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            boolean success = userManager.changePassword(currentUser.getUsername(),
                    new String(oldPf.getPassword()), new String(newPf.getPassword()));
            if (success) JOptionPane.showMessageDialog(this, "密码修改成功");
            else JOptionPane.showMessageDialog(this, "旧密码错误");
        }
    }

    private void showSaveManagerDialog() {
        JDialog dialog = new JDialog(this, "存档管理", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        List<Save> saves = saveManager.getUserSaves(currentUser.getUsername());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Save save : saves) {
            String modeInfo = save.isTimedGame()
                    ? String.format("[计时 %d分+%d秒]", save.getInitialTime()/60, save.getIncrementTime())
                    : "[普通模式]";
            listModel.addElement(save.getSaveName() + modeInfo + " (" + save.getSaveTime() + ")");
        }

        JList<String> list = new JList<>(listModel);
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnDel = new JButton("删除");
        JButton btnRename = new JButton("重命名");
        JButton btnExport = new JButton("导出(.txt)");
        JButton btnImport = new JButton("导入(.txt)");

        // 删除
        btnDel.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                String saveName = saves.get(idx).getSaveName();
                int confirm = JOptionPane.showConfirmDialog(
                        dialog,
                        "确定要删除存档 [" + saveName + "] 吗？\n此操作无法撤销！",
                        "删除确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    if (saveManager.deleteSave(saveName, currentUser.getUsername())) {
                        listModel.remove(idx);
                        saves.remove(idx);
                        JOptionPane.showMessageDialog(dialog, "删除成功");
                    } else {
                        JOptionPane.showMessageDialog(dialog, "删除失败");
                    }
                }
            }
        });

        //重命名
        btnRename.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                Save save = saves.get(idx);
                String oldName = save.getSaveName();


                String newName = JOptionPane.showInputDialog(dialog, "请输入新名称:", oldName);//默认显示旧名字

                if (newName != null && !newName.trim().isEmpty() && !newName.equals(oldName)) {
                    newName = newName.trim();

                    // 简单的文件名合法性检查 (Windows不支持冒号等)
                    if (newName.contains(":") || newName.contains("/") || newName.contains("\\")) {
                        JOptionPane.showMessageDialog(dialog, "名称包含非法字符");
                        return;
                    }

                    try {
                        //  更新 Save 对象内部的名字
                        save.setSaveName(newName);

                        // 保存为新文件

                        File saveDir = new File("resources/saves");
                        File newFile = new File(saveDir, currentUser.getUsername() + "_" + newName + ".json");

                        // 写入新文件
                        new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValue(newFile, save);

                        //  删除旧文件
                        saveManager.deleteSave(oldName, currentUser.getUsername());

                        //  更新 UI 列表显示
                        String modeInfo = save.isTimedGame()
                                ? String.format("[计时 %d分+%d秒]", save.getInitialTime()/60, save.getIncrementTime())
                                : "[普通模式]";
                        String displayStr = newName + modeInfo + " (" + save.getSaveTime() + ")";

                        listModel.set(idx, displayStr);

                        JOptionPane.showMessageDialog(dialog, "重命名成功");

                    } catch (IOException ex) {
                        // 如果失败，尝试把名字改回去（内存中），避免数据不一致
                        save.setSaveName(oldName);
                        JOptionPane.showMessageDialog(dialog, "重命名失败: " + ex.getMessage());
                    }
                }
            }
        });
        // 导出
        btnExport.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                Save save = saves.get(idx);
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("导出棋谱");
                // 默认文件名
                chooser.setSelectedFile(new File(save.getSaveName() + ".txt"));

                if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    File target = chooser.getSelectedFile();
                    // 自动补全后缀
                    if (!target.getName().toLowerCase().endsWith(".txt")) {
                        target = new File(target.getParentFile(), target.getName() + ".txt");
                    }

                    if (saveManager.exportSaveToText(save, target)) {
                        JOptionPane.showMessageDialog(dialog, "导出成功！\n路径: " + target.getAbsolutePath());
                    } else {
                        JOptionPane.showMessageDialog(dialog, "导出失败");
                    }
                }
            }
        });

        // 导入
        btnImport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("导入棋谱 (.txt)");
            chooser.setFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));

            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File source = chooser.getSelectedFile();
                Save importedSave = saveManager.importSaveFromText(source, currentUser.getUsername());

                if (importedSave != null) {
                    try {
                        File saveFile = new File("resources/saves",
                                currentUser.getUsername() + "_" + importedSave.getSaveName() + ".json");
                        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(saveFile, importedSave);

                        // 刷新列表
                        listModel.addElement(importedSave.getSaveName() + " (" + importedSave.getSaveTime() + ")");
                        saves.add(importedSave);

                        JOptionPane.showMessageDialog(dialog, "导入成功！\n已存为: " + importedSave.getSaveName());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, "导入保存失败");
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "导入失败：格式错误或文件为空");
                }
            }
        });

        btnPanel.add(btnDel);
        btnPanel.add(btnRename);
        btnPanel.add(btnExport);
        btnPanel.add(btnImport);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

}