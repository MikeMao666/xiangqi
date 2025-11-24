package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.ChessBoardModel;

import javax.swing.*;

public class GameFrame extends JFrame {
    private ChessBoardModel model;
    private ChessBoardPanel boardPanel;
    private JLabel label;

    public GameFrame(String title) {
        this.setTitle(title);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new ChessBoardModel();
        boardPanel = new ChessBoardPanel(model);

        boardPanel.setSize(600, 700);
        boardPanel.setLocation(0, 0);

        label = new JLabel("红方回合");
        boardPanel.setLabel(label);
        label.setSize(200, 100);
        label.setLocation(600, 50);

        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        undoButton.setLocation(600, 150);
        undoButton.setSize(100, 50);
        undoButton.addActionListener(e -> {
            if (model.canUndo()) {
                boolean success = model.undoMove();
                if (success) {
                    label.setText("悔棋成功，" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                    boardPanel.repaint();
                } else {
                    label.setText("悔棋失败");
                }
            } else {
                label.setText("无法悔棋，没有历史记录");
            }
        });

        // 重新开始按钮
        JButton restartButton = new JButton("重新开始");
        restartButton.setLocation(600, 210);
        restartButton.setSize(100, 50);
        restartButton.addActionListener(i -> {
            // 使用公共方法重置游戏
            model.resetGame();
            label.setText("红方回合");
            boardPanel.repaint();
        });

        this.add(boardPanel);
        this.add(label);
        this.add(undoButton);
        this.add(restartButton);
        this.setSize(800, 700);
        this.setLocationRelativeTo(null);
    }
}