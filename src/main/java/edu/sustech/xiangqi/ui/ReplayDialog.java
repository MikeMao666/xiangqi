package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.ChessBoardModel;
import edu.sustech.xiangqi.model.MoveRecord;
import edu.sustech.xiangqi.model.pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.util.List;

public class ReplayDialog extends JDialog {
    private ChessBoardModel replayModel;
    private ChessBoardPanel replayPanel;
    private List<MoveRecord> moveHistory;
    private int currentStepIndex = 0;
    private JLabel statusLabel;

    public ReplayDialog(JFrame parent, List<MoveRecord> moveHistory) {
        super(parent, "对局回放", true); // 模态对话框
        this.moveHistory = moveHistory;

        // 新的 Model，状态为初始棋盘
        this.replayModel = new ChessBoardModel();

        initializeUI();
        setSize(600, 700);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 顶部状态栏
        statusLabel = new JLabel("开局", SwingConstants.CENTER);
        statusLabel.setFont(new Font("楷体", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.NORTH);

        // 中间棋盘 （不让点）
        replayPanel = new ChessBoardPanel(replayModel);

        for (MouseListener ml : replayPanel.getMouseListeners()) {
            replayPanel.removeMouseListener(ml);
        }
        add(replayPanel, BorderLayout.CENTER);

        // 底部控制按钮
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton prevButton = new JButton("上一步");
        JButton nextButton = new JButton("下一步");
        JButton closeButton = new JButton("关闭");

        prevButton.addActionListener(e -> stepBackward());
        nextButton.addActionListener(e -> stepForward());
        closeButton.addActionListener(e -> dispose());

        controlPanel.add(prevButton);
        controlPanel.add(nextButton);
        controlPanel.add(closeButton);
        add(controlPanel, BorderLayout.SOUTH);

        updateStatus();
    }

    private void stepForward() {
        if (currentStepIndex >= moveHistory.size()) {
            JOptionPane.showMessageDialog(this, "已经是最后一步了");
            return;
        }

        MoveRecord record = moveHistory.get(currentStepIndex);

        // 在 replayModel 中找到对应的棋子并移动
        AbstractPiece pieceToMove = replayModel.getPieceAt(record.getFromRow(), record.getFromCol());

        if (pieceToMove != null) {
            // 模拟吃子
            AbstractPiece targetPiece = replayModel.getPieceAt(record.getToRow(), record.getToCol());
            if (targetPiece != null) {
                replayModel.getPieces().remove(targetPiece);
            }

            // 移动棋子
            pieceToMove.moveTo(record.getToRow(), record.getToCol());

            // 更新索引
            currentStepIndex++;
            updateStatus();
            replayPanel.repaint();
        }
    }

    private void stepBackward() {
        if (currentStepIndex <= 0) {
            JOptionPane.showMessageDialog(this, "已经是开局了");
            return;
        }

        currentStepIndex--;
        MoveRecord record = moveHistory.get(currentStepIndex);

        // 撤销这一步：把棋子从 to 移回 from
        AbstractPiece pieceMoved = replayModel.getPieceAt(record.getToRow(), record.getToCol());

        if (pieceMoved != null) {
            pieceMoved.moveTo(record.getFromRow(), record.getFromCol());

            // 如果这一步吃过子，需要把被吃的子恢复
            if (record.getCapturedPiece() != null) {
                restoreCapturedPiece(record.getCapturedPiece(), record.getToRow(), record.getToCol());
            }

            updateStatus();
            replayPanel.repaint();
        }
    }

    /**
     * 恢复被吃掉的棋子
     * 复制一个新的在replayModel里
     */
    private void restoreCapturedPiece(AbstractPiece original, int row, int col) {
        AbstractPiece newPiece = null;
        String name = original.getName();
        boolean isRed = original.isRed();

        if (original instanceof ChariotPiece) {
            newPiece = new ChariotPiece(name, row, col, isRed);
        }
        else if (original instanceof HorsePiece) {
            newPiece = new HorsePiece(name, row, col, isRed);
        }
        else if (original instanceof CannonPiece) {
            newPiece = new CannonPiece(name, row, col, isRed);
        }
        else if (original instanceof SoldierPiece) {
            newPiece = new SoldierPiece(name, row, col, isRed);
        }
        else if (original instanceof ElephantPiece) {
            newPiece = new ElephantPiece(name, row, col, isRed);
        }
        else if (original instanceof AdvisorPiece) {
            newPiece = new AdvisorPiece(name, row, col, isRed);
        }
        else if (original instanceof GeneralPiece) {
            newPiece = new GeneralPiece(name, row, col, isRed);
        }

        if (newPiece != null) {
            replayModel.getPieces().add(newPiece);
        }
    }

    private void updateStatus() {
        statusLabel.setText(String.format("当前步数: %d / %d", currentStepIndex, moveHistory.size()));
    }
}