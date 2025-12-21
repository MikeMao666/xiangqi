package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.*;
import edu.sustech.xiangqi.model.pieces.AbstractPiece;
import edu.sustech.xiangqi.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class GameFrame extends JFrame {
    private ChessBoardModel model;
    private ChessBoardPanel boardPanel;
    private NotationPanel notationPanel;
    private JLabel label;
    private User currentUser;
    private SaveManager saveManager;

    private JButton undoButton;
    private JButton restartButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton surrenderButton;
    private JButton drawButton;

    private Timer victoryTimer;
    private boolean victoryDialogShowing = false;

    private String currentLoadedSaveName = null;

    private GameConfig config;
    private Timer gameClock;
    private int redTimeRemaining;
    private int blackTimeRemaining;
    private JLabel redTimerLabel;
    private JLabel blackTimerLabel;
    private JPanel rightPanel;

    public GameFrame(String title, User user, GameConfig config) {
        this.currentUser = user;
        this.saveManager = new SaveManager();
        this.config = config != null ? config : new GameConfig();

        this.setTitle(title + " - " + user.getUsername() +
                (this.config.getMode() == GameConfig.Mode.TIMED ? String.format("[计时 %d分+%d秒]", config.getInitialTimeSeconds() / 60, config.getIncrementSeconds()) : ""));

        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        model = new ChessBoardModel();
        boardPanel = new ChessBoardPanel(model);
        notationPanel = new NotationPanel(model);
        boardPanel.setNotationPanel(notationPanel);
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // 右侧面板
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(220, 600));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 计时模式，先添加计时器到右侧面板顶部
        if (this.config.getMode() == GameConfig.Mode.TIMED) {
            initTimers(); // 初始化并添加到 rightPanel
        }

        // 状态标签
        label = new JLabel("红方回合");
        label.setFont(new Font("宋体", Font.BOLD, 16));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        boardPanel.setLabel(label);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(label);
        rightPanel.add(Box.createVerticalStrut(10));

        // 棋谱
        rightPanel.add(notationPanel);
        rightPanel.add(Box.createVerticalStrut(10));

        // 按钮
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        undoButton = new JButton("悔棋");
        restartButton = new JButton("重新开始");
        saveButton = new JButton("存档");
        loadButton = new JButton("读档");
        surrenderButton = new JButton("投降");
        drawButton = new JButton("和棋");

        saveButton.setEnabled(!currentUser.isGuest);
        loadButton.setEnabled(!currentUser.isGuest);
        surrenderButton.setBackground(new Color(220, 20, 60));
        surrenderButton.setForeground(Color.WHITE);
        drawButton.setBackground(new Color(30, 144, 255));
        drawButton.setForeground(Color.WHITE);

        undoButton.addActionListener(e -> handleUndo());
        restartButton.addActionListener(e -> handleRestart());
        saveButton.addActionListener(e -> handleSave());
        loadButton.addActionListener(e -> handleLoad());
        surrenderButton.addActionListener(e -> handleSurrender());
        drawButton.addActionListener(e -> handleDraw());

        buttonPanel.add(undoButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(surrenderButton);
        buttonPanel.add(drawButton);

        // 设置按钮最大高度，防止拉伸的太难看
        buttonPanel.setMaximumSize(new Dimension(200, 300));
        rightPanel.add(buttonPanel);

        mainPanel.add(rightPanel, BorderLayout.EAST);
        this.add(mainPanel);
        this.setSize(775, 650);
        this.setLocationRelativeTo(null);

        // 计时器
        victoryTimer = new Timer(100, e -> checkGameState());
        victoryTimer.start();
    }

    private void handleUndo() {
        if (model.canUndo()) {
            String message = (model.isRedTurn() ? "黑方" : "红方") + "请求悔棋，" + (model.isRedTurn() ? "红方" : "黑方") + "同意吗？";

            int response = JOptionPane.showConfirmDialog(this, message, "悔棋请求", JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                boolean success = model.undoMove();
                if (success) {
                    label.setText("悔棋成功，" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                    boardPanel.repaint();
                    notationPanel.updateNotation();

                    boardPanel.updateCheckStatus();
                } else {
                    label.setText("悔棋失败");
                }
            } else {
                label.setText("对方拒绝了悔棋请求");
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
        boolean wasRunning = (gameClock != null && gameClock.isRunning());
        if (wasRunning) gameClock.stop();
        String saveName;

        // 确定存档名称 (覆盖/新建)
        if (currentLoadedSaveName != null) {
            // 如果是读档进来的，询问是否覆盖
            Object[] options = {"覆盖原存档: " + currentLoadedSaveName, "另存为新存档", "取消"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "当前游戏读取自: " + currentLoadedSaveName + "\n您希望如何保存？",
                    "保存选项",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) { // 覆盖
                saveName = currentLoadedSaveName;
            } else if (choice == 1) { // 另存为
                String inputName = JOptionPane.showInputDialog(this, "请输入新存档名称:");
                if (inputName != null && !inputName.trim().isEmpty()) {
                    saveName = inputName.trim();
                } else {
                    return;
                }
            } else { // 取消
                return;
            }
        } else {
            // 如果是新开局，直接新建
            String inputName = JOptionPane.showInputDialog(this, "请输入存档名称:");
            if (inputName != null && !inputName.trim().isEmpty()) {
                saveName = inputName.trim();
            } else {
                return;
            }
        }

        // 保存
        if (saveName != null) {
            try {
                List<String> moves = model.getMoveNotations();
                Save save;

                if (config.getMode() == GameConfig.Mode.TIMED) {
                    // 计时模式保存时间信息
                    save = new Save(
                            saveName,
                            currentUser.getUsername(),
                            model.isRedTurn(),
                            moves,
                            config.getInitialTimeSeconds(),
                            config.getIncrementSeconds(),
                            redTimeRemaining,
                            blackTimeRemaining
                    );
                } else {
                    // 普通模式时间字段设为 0
                    save = new Save(
                            saveName,
                            currentUser.getUsername(),
                            model.isRedTurn(),
                            moves
                    );
                }

                //直接写入文件
                java.io.File saveDir = new java.io.File("resources/saves");

                java.io.File saveFile = new java.io.File(saveDir, currentUser.getUsername() + "_" + saveName + ".json");

                new com.fasterxml.jackson.databind.ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(saveFile, save);

                // 更新当前存档名记录
                this.currentLoadedSaveName = saveName;
                JOptionPane.showMessageDialog(this, "存档成功！");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage());
            } finally {
                if (wasRunning && model.getGameState() == ChessBoardModel.GameState.PLAYING) {
                    gameClock.start();
                }
            }
        }
    }

    private void handleLoad() {
        // 游客检查
        if (currentUser.isGuest) {
            JOptionPane.showMessageDialog(this, "游客模式无法读档");
            return;
        }
        boolean wasRunning = (gameClock != null && gameClock.isRunning());
        if (wasRunning) gameClock.stop();
        // 获取所有存档
        List<Save> allSaves = saveManager.getUserSaves(currentUser.getUsername());
        if (allSaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有找到存档");
            return;
        }

        // 计时模式只能看计时存档，普通模式只能看普通存档
        boolean isCurrentTimed = (config.getMode() == GameConfig.Mode.TIMED);

        List<Save> filteredSaves = new ArrayList<>();
        for (Save s : allSaves) {
            if (isCurrentTimed) {
                // 计时模式只保留带有时间信息的存档
                if (s.isTimedGame()) {
                    if (s.getInitialTime() == config.getInitialTimeSeconds() &&
                            s.getIncrementTime() == config.getIncrementSeconds())//只能打开对应模式的存档
                        filteredSaves.add(s);
                }
            } else {
                // 普通模式只保留普通存档
                if (!s.isTimedGame()) {
                    filteredSaves.add(s);
                }
            }
        }

        if (filteredSaves.isEmpty()) {
            String message = isCurrentTimed ? "没有找到符合当前【计时模式】的存档" : "没有找到符合当前【普通模式】的存档";
            JOptionPane.showMessageDialog(this, message);
            return;
        }

        // 构建显示列表 (HTML格式化)
        Object[] selectionValues = new Object[filteredSaves.size()];
        for (int i = 0; i < filteredSaves.size(); i++) {
            Save s = filteredSaves.get(i);

            // 额外显示赛制信息
            String modeInfo = s.isTimedGame()
                    ? String.format("[计时 %d分+%d秒]", s.getInitialTime() / 60, s.getIncrementTime())
                    : "[普通模式]";

            String displayHtml = String.format(
                    "<html><body style='width: 250px'>" +
                            "<b>%s</b> <span style='color: blue'>%s</span><br>" +
                            "<span style='color: gray; font-size: 9px'>%s</span>" +
                            "</body></html>",
                    s.getSaveName(),
                    modeInfo,
                    s.getSaveTime()
            );
            selectionValues[i] = displayHtml;
        }

        //弹出选择框
        String selectedHtml = (String) JOptionPane.showInputDialog(
                this,
                "选择要加载的存档 (已过滤不兼容模式):",
                "读档",
                JOptionPane.PLAIN_MESSAGE,
                null,
                selectionValues,
                selectionValues[0]);

        if (selectedHtml != null) {
            // 找回选中的 Save 对象
            int selectedIndex = -1;
            for (int i = 0; i < selectionValues.length; i++) {
                if (selectionValues[i].equals(selectedHtml)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex != -1) {
                Save selectedSave = filteredSaves.get(selectedIndex);

                //  执行加载
                if (model.loadFromNotationSave(selectedSave)) {
                    this.currentLoadedSaveName = selectedSave.getSaveName();

                    //恢复时间
                    if (isCurrentTimed) {
                        this.redTimeRemaining = selectedSave.getRedTimeLeft();
                        this.blackTimeRemaining = selectedSave.getBlackTimeLeft();

                        // 立即刷新界面显示
                        updateTimerLabels();

                        // 确保时钟正在运行 (如果之前暂停了)
                        if (gameClock != null && !gameClock.isRunning()) {
                            gameClock.start();
                        }
                    }

                    // 刷新通用界面
                    label.setText("读档成功，" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                    boardPanel.repaint();
                    notationPanel.updateNotation();

                    // 重启胜利检测
                    victoryDialogShowing = false;
                    if (victoryTimer != null) victoryTimer.start();

                    JOptionPane.showMessageDialog(this, "读档成功");
                } else {
                    JOptionPane.showMessageDialog(this, "读档失败，存档可能已损坏");
                }
            }
        }
        if (wasRunning && model.getGameState() == ChessBoardModel.GameState.PLAYING) {
            gameClock.start();
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
        int response = JOptionPane.showConfirmDialog(
                this,
                ((model.isRedTurn()) ? "红方" : "黑方") + "提议和棋？",
                "和棋提议",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            boolean success = model.proposeDraw();
            if (success) {
                showVictoryMessage();
            }
        }
    }

    /**
     * 检查游戏状态并显示胜利消息
     */
    void checkGameState() {
        ChessBoardModel.GameState state = model.getGameState();

        if (state != ChessBoardModel.GameState.PLAYING && !victoryDialogShowing) {
            // 停止计时器
            victoryTimer.stop();

            victoryDialogShowing = true;

            //锁住按钮
            disableControlButtons();

            // 立即显示胜利消息
            showVictoryMessage();

            // 强制刷新界面
            revalidate();
            repaint();

            // 稍为延迟，让用户看清最后一步棋
            SwingUtilities.invokeLater(this::showVictoryMessage);
        }
    }

    /**
     * 显示胜利消息
     */
    private void showVictoryMessage() {
        ChessBoardModel.GameState state = model.getGameState();
        String message = model.getVictoryMessage();

        // 创建胜利对话框
        JDialog victoryDialog = new JDialog(this, "游戏结束", true);
        victoryDialog.setLayout(new BorderLayout());
        victoryDialog.setSize(400, 350);
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

        JButton replayButton = new JButton("对局回放");
        JButton restartButton = new JButton("重新开始");
        JButton exitButton = new JButton("退出游戏");

        replayButton.setFont(new Font("宋体", Font.BOLD, 14));
        restartButton.setFont(new Font("宋体", Font.BOLD, 14));
        exitButton.setFont(new Font("宋体", Font.BOLD, 14));


        replayButton.setBackground(new Color(100, 149, 237));
        replayButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(60, 179, 113));
        restartButton.setForeground(Color.WHITE);
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);

        if (currentUser.isGuest) {
            replayButton.setEnabled(false);
            replayButton.setToolTipText("注册用户专享功能");
        }


        replayButton.addActionListener(e -> {
            victoryDialog.dispose();
            List<MoveRecord> history = model.getMoveHistoryList();
            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(victoryDialog, "没有可回放的步数");
                return;
            }

            // 打开回放窗口
            ReplayDialog replayDialog = new ReplayDialog(this, history);
            replayDialog.setVisible(true);
        });

        restartButton.addActionListener(e -> {
            victoryDialog.dispose();
            victoryDialogShowing = false; // 重置标志位
            handleRestart();
        });

        exitButton.addActionListener(e -> {
            if (checkAndSaveOnExit())//退出前也问要不要保存
                victoryDialog.dispose();
            this.dispose();

            new MainMenuFrame(currentUser, new edu.sustech.xiangqi.model.user.UserManager()).setVisible(true);//回到主界面
        });
        buttonPanel.add(replayButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);

        // 添加组件
        victoryDialog.add(titleLabel, BorderLayout.NORTH);
        victoryDialog.add(messageLabel, BorderLayout.CENTER);
        victoryDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 对话框关闭时的处理
        victoryDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                victoryDialogShowing = false;
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                victoryDialogShowing = false;
            }
        });

        victoryDialog.setVisible(victoryDialogShowing);//!!!
    }

    /**
     * 修改重启方法
     */
    private void handleRestart() {
        int response = JOptionPane.showConfirmDialog(this, "确定要重启游戏吗？", "重启游戏", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            model.resetGame();
            if (config.getMode() == GameConfig.Mode.TIMED) {
                redTimeRemaining = config.getInitialTimeSeconds();
                blackTimeRemaining = config.getInitialTimeSeconds();
                updateTimerLabels();
                if (gameClock != null) gameClock.restart();
            }
            this.currentLoadedSaveName = null;
            label.setText("红方回合");

            enableControlButtons();

            List<Point> points = boardPanel.getValidMoves();
            AbstractPiece selectedPiece = boardPanel.getSelectedPiece();
            points.clear();
            selectedPiece = null;

            boardPanel.revalidate();
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

//        model.loadStalemateTest();
//
//        label.setText("测试模式：红方困毙局面");
//        boardPanel.repaint();
//        notationPanel.updateNotation();
//
//        // 重新启用按钮
//        boardPanel.setEnabled(true);
//        surrenderButton.setEnabled(true);
//        drawButton.setEnabled(true);
//        victoryDialogShowing = false;
//
//        if (victoryTimer != null) victoryTimer.start();
//
//        // 强制检查一次状态
//        checkGameState();
    }

    /**
     * 退出前检查保存
     *
     * @return true=可以退出, false=用户取消了操作，不要退出
     */
    public boolean checkAndSaveOnExit() {
        // 游客直接退出，不保存
        if (currentUser.isGuest) {
            return true;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "正在退出游戏，是否保存当前棋谱？",
                "退出保存",
                JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            String saveName;

            // 如果是读档的，询问覆盖还是另存为
            if (currentLoadedSaveName != null) {
                Object[] options = {"覆盖原存档: " + currentLoadedSaveName, "另存为新存档"};
                int saveChoice = JOptionPane.showOptionDialog(
                        this,
                        "当前游戏读取自: " + currentLoadedSaveName + "\n您希望如何保存？",
                        "保存选项",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (saveChoice == 0) {
                    saveName = currentLoadedSaveName;
                } else if (saveChoice == 1) {
                    // 另存为
                    String timeStr = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    saveName = "AutoSave_" + timeStr;
                } else {
                    return false;
                }
            } else {
                String timeStr = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                saveName = "AutoSave_" + timeStr;
            }

            boolean success = saveManager.saveGame(saveName, currentUser.getUsername(), model);
            if (success) {
                JOptionPane.showMessageDialog(this, "游戏已保存为: " + saveName);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "保存失败！");
                return true;
            }
        } else return option == JOptionPane.NO_OPTION;
    }

    /**
     * 禁用控制按钮
     */
    private void disableControlButtons() {
        undoButton.setEnabled(false);
        saveButton.setEnabled(false);
        loadButton.setEnabled(false); // 读档通常允许，因为这相当于重新开始，看你需求
        restartButton.setEnabled(true); // 重新开始永远允许
        surrenderButton.setEnabled(false);
        drawButton.setEnabled(false);
        boardPanel.setEnabled(false);
    }

    /**
     * 启用控制按钮
     */
    private void enableControlButtons() {
        undoButton.setEnabled(true);
        saveButton.setEnabled(!currentUser.isGuest);
        loadButton.setEnabled(!currentUser.isGuest);
        surrenderButton.setEnabled(true);
        drawButton.setEnabled(true);
        boardPanel.setEnabled(true);
    }

    private void initTimers() {

        this.redTimeRemaining = config.getInitialTimeSeconds();
        this.blackTimeRemaining = config.getInitialTimeSeconds();

        JPanel timerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        timerPanel.setBorder(BorderFactory.createTitledBorder("比赛时间"));
        timerPanel.setMaximumSize(new Dimension(200, 100)); // 限制高度

        redTimerLabel = new JLabel(formatTime(redTimeRemaining), SwingConstants.CENTER);
        blackTimerLabel = new JLabel(formatTime(blackTimeRemaining), SwingConstants.CENTER);

        redTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        blackTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));

        redTimerLabel.setForeground(Color.RED);
        redTimerLabel.setBorder(BorderFactory.createTitledBorder("红方"));

        blackTimerLabel.setForeground(Color.BLACK);
        blackTimerLabel.setBorder(BorderFactory.createTitledBorder("黑方"));

        timerPanel.add(blackTimerLabel);
        timerPanel.add(redTimerLabel);   // 红方在下面

        // 添加到右侧面板
        rightPanel.add(timerPanel);

        // 启动倒计时
        gameClock = new Timer(1000, e -> {
            if (model.getGameState() != ChessBoardModel.GameState.PLAYING) {
                return;
            }

            if (model.isRedTurn()) {
                redTimeRemaining--;
                if (redTimeRemaining <= 0) handleTimeout(true);
            } else {
                blackTimeRemaining--;
                if (blackTimeRemaining <= 0) handleTimeout(false);
            }
            updateTimerLabels();
        });
        gameClock.start();
    }

    private void updateTimerLabels() {
        if (redTimerLabel == null || blackTimerLabel == null) return;//如果不是计时模式，直接返回
        redTimerLabel.setText(formatTime(redTimeRemaining));
        blackTimerLabel.setText(formatTime(blackTimeRemaining));
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void handleTimeout(boolean isRed) {
        if (gameClock != null) gameClock.stop(); // 停止计时器

        // 更新 Model 状态
        model.triggerTimeout(isRed);
        // 触发胜利弹窗
        checkGameState();
    }

    /**
     * 加缪
     *
     */
    public void onMoveMade() {
        if (config.getMode() == GameConfig.Mode.TIMED && redTimerLabel != null) {
            // 给刚下完的那一方加秒
            if (!model.isRedTurn()) {
                redTimeRemaining += config.getIncrementSeconds();
            } else {
                blackTimeRemaining += config.getIncrementSeconds();
            }
            updateTimerLabels();
        }
    }
}