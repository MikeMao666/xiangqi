package edu.sustech.xiangqi.ui;
import edu.sustech.xiangqi.model.ChessBoardModel;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame(String title) {
        this.setTitle(title);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChessBoardModel model = new ChessBoardModel( );
        ChessBoardPanel boardPanel = new ChessBoardPanel(model);

        boardPanel.setSize(600, 700);
        boardPanel.setLocation(0, 0);

        JLabel label = new JLabel("状态");
        boardPanel.setLabel(label);
        label.setSize(100, 100);
        label.setLocation(600, 100);

        JButton button = new JButton("按钮");
        button.setLocation(600, 200);
        button.setSize(100, 50);

        this.add(boardPanel);
        this.add(label);
        this.add(button);
        this.setSize(800, 700);
        this.setLocationRelativeTo(null);

        button.addActionListener(i -> {
            label.setText("按按钮啦");
        });

    }
}
