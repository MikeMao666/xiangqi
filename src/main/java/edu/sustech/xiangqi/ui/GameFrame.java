package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.*;
import edu.sustech.xiangqi.model.user.User;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private ChessBoardModel model;
    private ChessBoardPanel boardPanel;
    private NotationPanel notationPanel;
    private JLabel label;
    private User currentUser;
    private SaveManager saveManager;

    public GameFrame(String title, User user) {
        this.currentUser = user;
        this.saveManager = new SaveManager();
        this.setTitle(title + " - " + user.getUsername());
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        model = new ChessBoardModel();
        boardPanel = new ChessBoardPanel(model);
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // 右侧面板（状态和按钮）
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(200, 600));

        label = new JLabel("红方回合");
        boardPanel.setLabel(label);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 棋谱面板
        notationPanel = new NotationPanel(model);
        boardPanel.setNotationPanel(notationPanel);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        JButton undoButton = new JButton("悔棋");
        JButton restartButton = new JButton("重新开始");
        JButton saveButton = new JButton("存档");
        JButton loadButton = new JButton("读档");

        saveButton.setEnabled(!currentUser.isGuest);
        loadButton.setEnabled(!currentUser.isGuest);

        buttonPanel.add(undoButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        rightPanel.add(label);
        rightPanel.add(notationPanel);
        rightPanel.add(buttonPanel);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        this.add(mainPanel);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null);

        // 添加事件监听器
        undoButton.addActionListener(e -> handleUndo());
        restartButton.addActionListener(e -> handleRestart());
        saveButton.addActionListener(e -> handleSave());
        loadButton.addActionListener(e -> handleLoad());
    }

    private void handleUndo() {
        if (model.canUndo()) {
            boolean success = model.undoMove();
            if (success) {
                label.setText("悔棋成功，" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                boardPanel.repaint();
                notationPanel.updateNotation();
            } else {
                label.setText("悔棋失败");
            }
        } else {
            label.setText("无法悔棋，没有历史记录");
        }
    }

    private void handleRestart() {
        model.resetGame();
        label.setText("红方回合");
        boardPanel.repaint();
        notationPanel.updateNotation();
    }

    private void handleSave() {
        if (currentUser.isGuest) {
            JOptionPane.showMessageDialog(this, "游客模式无法存档");
            return;
        }

        String saveName = JOptionPane.showInputDialog(this, "请输入存档名称:");
        if (saveName != null && !saveName.trim().isEmpty()) {
            boolean success = saveManager.saveGame(saveName.trim(), currentUser.getUsername(), model);
            if (success) {
                JOptionPane.showMessageDialog(this, "存档成功");
            } else {
                JOptionPane.showMessageDialog(this, "存档失败");
            }
        }
    }

    private void handleLoad() {
        if (currentUser.isGuest) {
            JOptionPane.showMessageDialog(this, "游客模式无法读档");
            return;
        }

        java.util.List<String> userSaves = saveManager.getUserSaves(currentUser.getUsername());
        if (userSaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有找到存档");
            return;
        }

        String[] saveArray = userSaves.toArray(new String[0]);
        String selectedSave = (String) JOptionPane.showInputDialog(
                this, "选择要加载的存档:", "读档",
                JOptionPane.QUESTION_MESSAGE, null, saveArray, saveArray[0]);

        if (selectedSave != null) {
            Save save = saveManager.loadGame(selectedSave, currentUser.getUsername());
            if (save != null && model.loadFromNotationSave(save)) {
                label.setText("读档成功，" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                boardPanel.repaint();
                notationPanel.updateNotation();
                JOptionPane.showMessageDialog(this, "读档成功");
            } else {
                JOptionPane.showMessageDialog(this, "读档失败，存档可能已损坏");
            }
        }
    }
}