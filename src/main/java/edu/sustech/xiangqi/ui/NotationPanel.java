package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.ChessBoardModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotationPanel extends JPanel {
    private ChessBoardModel model;
    private JTextArea notationArea;

    public NotationPanel(ChessBoardModel model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 600));

        JLabel titleLabel = new JLabel("棋谱记录", JLabel.CENTER);
        titleLabel.setFont(new Font("楷体", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        notationArea = new JTextArea();
        notationArea.setEditable(false);
        notationArea.setFont(new Font("楷体", Font.PLAIN, 14));
        notationArea.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(notationArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        updateNotation();
    }

    public void updateNotation() {
        List<String> notations = model.getFormattedNotations();
        StringBuilder sb = new StringBuilder();
        sb.append("回合  红方          黑方\n");
        sb.append("—————————————\n");

        for (String notation : notations) {
            sb.append(notation).append("\n");
        }

        notationArea.setText(sb.toString());

        // 滚动到底部
        notationArea.setCaretPosition(notationArea.getDocument().getLength());
    }
}