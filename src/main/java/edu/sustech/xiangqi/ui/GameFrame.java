package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.*;
import edu.sustech.xiangqi.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameFrame extends JFrame {
    private ChessBoardModel model;
    private ChessBoardPanel boardPanel;
    private NotationPanel notationPanel;
    private JLabel label;
    private User currentUser;
    private SaveManager saveManager;
    private JButton surrenderButton;
    private JButton drawButton;
    private Timer victoryTimer;
    private boolean victoryDialogShowing = false;

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
        notationPanel = new NotationPanel(model);
        boardPanel.setNotationPanel(notationPanel);
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
        surrenderButton = new JButton("投降");
        drawButton = new JButton("和棋");

        saveButton.setEnabled(!currentUser.isGuest);
        loadButton.setEnabled(!currentUser.isGuest);

        // 设置按钮颜色
        surrenderButton.setBackground(new Color(220, 20, 60));
        surrenderButton.setForeground(Color.WHITE);
        drawButton.setBackground(new Color(30, 144, 255));
        drawButton.setForeground(Color.WHITE);

        buttonPanel.add(undoButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(surrenderButton);
        buttonPanel.add(drawButton);

        undoButton.addActionListener(e -> handleUndo());
        restartButton.addActionListener(e -> handleRestart());
        saveButton.addActionListener(e -> handleSave());
        loadButton.addActionListener(e -> handleLoad());
        surrenderButton.addActionListener(e -> handleSurrender());
        drawButton.addActionListener(e -> handleDraw());

        rightPanel.add(label);
        rightPanel.add(notationPanel);
        rightPanel.add(buttonPanel);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        this.add(mainPanel);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null);

        // 创建胜利播报计时器
        victoryTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGameState();
            }
        });
        victoryTimer.start();
    }

    private void handleUndo() {
        if (model.canUndo()) {
            boolean success = model.undoMove();
            if (success) {
                label.setText("悔棋成功，" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                boardPanel.repaint();
                notationPanel.updateNotation();
                // 更新将军状态显示
                boardPanel.updateCheckStatus();
            } else {
                label.setText("悔棋失败");
            }
        } else {
            label.setText("无法悔棋，没有历史记录");
        }
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

    private void handleSurrender() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要投降吗？",
                "确认投降",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean isRedSurrendering = model.isRedTurn(); // 当前回合的对方投降
            boolean success = model.surrender(isRedSurrendering);

            if (success) {
                // 不需要立即调用 showVictoryMessage()，因为 timer 会检查
                // 停止计时器，防止重复检查
                if (victoryTimer != null) {
                    victoryTimer.stop();
                }
                // 等待 timer 下一次检查（或者直接调用 checkGameState）
                checkGameState();
            }
        }
    }

    /**
     * 处理和棋
     */
    private void handleDraw() {
        // 弹出对话框让对方确认
        String message = "向对方提议和棋？";
        if (currentUser.isGuest) {
            // 游客模式自动同意
            int response = JOptionPane.showConfirmDialog(
                    this,
                    "提议和棋？（对方为AI，将自动同意）",
                    "和棋提议",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                boolean success = model.proposeDraw();
                if (success) {
                    showVictoryMessage();
                }
            }
        } else {
            // 双人模式需要对方确认
            JOptionPane.showMessageDialog(
                    this,
                    "和棋功能在双人模式中需要双方同意\n请在另一台设备上操作",
                    "和棋提议",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * 检查游戏状态并显示胜利消息
     */
    void checkGameState() {
        ChessBoardModel.GameState state = model.getGameState();

        if (state != ChessBoardModel.GameState.PLAYING && !victoryDialogShowing) {
            // 添加调试信息
            System.out.println("检测到游戏结束状态: " + state + ", 消息: " + model.getVictoryMessage());

            // 停止计时器
            victoryTimer.stop();

            // 设置标志位，防止重复弹出
            victoryDialogShowing = true;

            // 立即显示胜利消息
            showVictoryMessage();

            // 禁用移动
            boardPanel.setEnabled(false);
            surrenderButton.setEnabled(false);
            drawButton.setEnabled(false);

            // 强制刷新界面
            revalidate();
            repaint();
        } else if (state == ChessBoardModel.GameState.PLAYING) {
            // 如果游戏还在进行中，检查将军状态并更新显示
            boardPanel.updateCheckStatus();
        }
    }

    /**
     * 显示胜利消息
     */
    private void showVictoryMessage() {
        ChessBoardModel.GameState state = model.getGameState();
        String message = model.getVictoryMessage();

        // 添加调试信息
        System.out.println("显示胜利消息: " + state + " - " + message);

        // 创建胜利对话框
        JDialog victoryDialog = new JDialog(this, "游戏结束", true);
        victoryDialog.setLayout(new BorderLayout());
        victoryDialog.setSize(400, 300);
        victoryDialog.setLocationRelativeTo(this);

        // 设置背景颜色
        Color bgColor;
        String title;

        switch (state) {
            case RED_WIN:
                bgColor = new Color(220, 20, 60, 230);
                title = "红方胜利！";
                break;
            case BLACK_WIN:
                bgColor = new Color(0, 0, 0, 230);
                title = "黑方胜利！";
                break;
            case SURRENDER_RED:
                bgColor = new Color(255, 140, 0, 230);
                title = "红方投降！";
                break;
            case SURRENDER_BLACK:
                bgColor = new Color(255, 140, 0, 230);
                title = "黑方投降！";
                break;
            case DRAW:
                bgColor = new Color(30, 144, 255, 230);
                title = "和棋！";
                break;
            default:
                bgColor = Color.WHITE;
                title = "游戏结束";
        }

        victoryDialog.getContentPane().setBackground(bgColor);

        // 标题标签
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("楷体", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // 消息标签
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("宋体", Font.PLAIN, 18));
        messageLabel.setForeground(Color.YELLOW);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);

        JButton restartButton = new JButton("重新开始");
        JButton exitButton = new JButton("退出游戏");

        restartButton.setFont(new Font("宋体", Font.BOLD, 14));
        exitButton.setFont(new Font("宋体", Font.BOLD, 14));

        restartButton.setBackground(new Color(60, 179, 113));
        restartButton.setForeground(Color.WHITE);
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);

        restartButton.addActionListener(e -> {
            victoryDialog.dispose();
            victoryDialogShowing = false; // 重置标志位
            handleRestart();
        });

        exitButton.addActionListener(e -> {
            System.exit(0);
        });

        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);

        // 添加组件
        victoryDialog.add(titleLabel, BorderLayout.NORTH);
        victoryDialog.add(messageLabel, BorderLayout.CENTER);
        victoryDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 对话框关闭时的处理
        victoryDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                victoryDialogShowing = false;
            }
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                victoryDialogShowing = false;
            }
        });

        victoryDialog.setVisible(true);
    }

    /**
     * 修改重启方法
     */
    private void handleRestart() {
        model.resetGame();
        label.setText("红方回合");
        boardPanel.repaint();
        notationPanel.updateNotation();

        // 重新启用按钮和面板
        boardPanel.setEnabled(true);
        surrenderButton.setEnabled(true);
        drawButton.setEnabled(true);

        victoryDialogShowing = false;

        // 重启计时器
        victoryTimer.start();
    }
}