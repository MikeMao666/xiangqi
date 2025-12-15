package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.ChessBoardModel;
import edu.sustech.xiangqi.model.pieces.AbstractPiece;
import edu.sustech.xiangqi.model.pieces.GeneralPiece;
import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.util.List;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ChessBoardPanel extends JPanel {
    private final ChessBoardModel model;
    private JLabel label;
    private NotationPanel notationPanel;
    private JLabel checkLabel;
    private List<Point> validMoves = new ArrayList<>();// 用于显示将军状态

    public void setNotationPanel(NotationPanel notationPanel) {
        this.notationPanel = notationPanel;
    }
    public void setLabel(JLabel label){this.label = label;}

    private static final int MARGIN = 20;
    private static final int CELL_SIZE = 60;
    private static final int PIECE_RADIUS = 25;

    private AbstractPiece selectedPiece = null;

    public ChessBoardPanel(ChessBoardModel model) {
        this.model = model;
        setPreferredSize(new Dimension(
                CELL_SIZE * (ChessBoardModel.getCols() - 1) + MARGIN * 2,
                CELL_SIZE * (ChessBoardModel.getRows() - 1) + MARGIN * 2
        ));
        setBackground(new Color(220, 179, 92));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        // 创建将军状态标签
        checkLabel = new JLabel("", SwingConstants.CENTER);
        checkLabel.setFont(new Font("楷体", Font.BOLD, 16));
        checkLabel.setForeground(Color.RED);
        checkLabel.setVisible(false);

        // 添加到面板（需要调整布局）
        setLayout(new BorderLayout());
        add(checkLabel, BorderLayout.SOUTH);
    }

    private void handleMouseClick(int x, int y) {
        int col = Math.round((float)(x - MARGIN) / CELL_SIZE);
        int row = Math.round((float)(y - MARGIN) / CELL_SIZE);

        if (model.getGameState() != ChessBoardModel.GameState.PLAYING) {
            label.setText("游戏已结束，请重新开始");
            return;
        }

        if (!model.isValidPosition(row, col)) {
            return;
        }

        AbstractPiece clickedPiece = model.getPieceAt(row, col);
        if (selectedPiece == null) {
            // 选中棋子：仅允许选当前回合的棋子
            if (clickedPiece != null && clickedPiece.isRed() == model.isRedTurn()) {
                selectedPiece = clickedPiece;
                calculateValidMoves(); // 计算高亮路径
                if (label != null) {
                    label.setText("选中" + (clickedPiece.isRed() ? "红" : "黑") + clickedPiece.getName());
                }
            }
        } else {
            // 移动棋子
            boolean moveSuccess = model.movePiece(selectedPiece, row, col);
            if (moveSuccess) {
                // 立即检查游戏状态
                ChessBoardModel.GameState state = model.getGameState();

                updateCheckStatus();
                if (label != null) {
                    label.setText((model.isRedTurn() ? "红方" : "黑方") + "回合");
                }

                // 更新棋谱记录
                if (notationPanel != null) {
                    notationPanel.updateNotation();
                }

                // 如果游戏结束，立即显示消息
                if (state != ChessBoardModel.GameState.PLAYING) {
                    // 强制UI更新
                    SwingUtilities.invokeLater(() -> {
                        // 触发胜利对话框
                        GameFrame parentFrame = (GameFrame) SwingUtilities.getWindowAncestor(ChessBoardPanel.this);
                        if (parentFrame != null) {
                            parentFrame.checkGameState();
                        }
                    });
                }
            } else {
                if (label != null) {
                    label.setText("非法移动！" + (model.isRedTurn() ? "红方" : "黑方") + "回合");
                }
            }
            selectedPiece = null; // 清空选中状态
            validMoves.clear(); // 清空高亮路径
        }
        repaint(); // 强制刷新面板，显示选中框/高亮路径
    }

    private void calculateValidMoves() {
        validMoves.clear();
        if (selectedPiece == null) return;

        // 遍历棋盘所有位置，检查是否可以移动
        for (int row = 0; row < ChessBoardModel.getRows(); row++) {
            for (int col = 0; col < ChessBoardModel.getCols(); col++) {
                if (selectedPiece.canMoveTo(row, col, model)) {
                    validMoves.add(new Point(row, col));
                }
            }
        }
    }

    /**
     * 更新将军状态显示
     */
    public void updateCheckStatus() {
        if (model.isInCheck()) {
            boolean isRedInCheck = model.isRedTurn(); // 当前回合方=被将军方
            String checkMsg = isRedInCheck ? "红方被将军！" : "黑方被将军！";

            // 1. 更新将军提示标签
            if (checkLabel != null) {
                checkLabel.setText(checkMsg);
                checkLabel.setForeground(isRedInCheck ? Color.RED : Color.BLACK);
                checkLabel.setVisible(true);
                checkLabel.repaint(); // 强制刷新
            }

            // 2. 更新右上方文字栏（核心）
            if (label != null) {
                String fullMsg = checkMsg + " 请移动" + (isRedInCheck ? "红方" : "黑方") + "棋子";
                label.setText(fullMsg);
                label.setForeground(isRedInCheck ? Color.RED : Color.BLACK);
                label.repaint(); // 强制刷新
            }
        } else {
            if (checkLabel != null) checkLabel.setVisible(false);
            if (label != null) {
                label.setText((model.isRedTurn() ? "红方" : "黑方") + "回合");
                label.setForeground(Color.BLACK);
            }
        }
    }

    /**
     * 重写paintComponent，添加游戏结束遮罩
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制棋盘和棋子
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2d);
        drawValidMoves(g2d);
        drawPieces(g2d);

        // 如果游戏结束，绘制半透明遮罩
        if (model.getGameState() != ChessBoardModel.GameState.PLAYING) {
            g2d.setColor(new Color(0, 0, 0, 150)); // 半透明黑色
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // 绘制游戏结束文字
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("楷体", Font.BOLD, 36));
            String gameOverText = "游戏结束";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(gameOverText);
            g2d.drawString(gameOverText, (getWidth() - textWidth) / 2, getHeight() / 2);
        }
    }

    // 覆盖原有drawValidMoves方法
    private void drawValidMoves(Graphics2D g2d) {
        if (validMoves.isEmpty() || selectedPiece == null) return;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 高亮路径：半透明绿渐变+圆角
        for (Point p : validMoves) {
            int centerX = MARGIN + p.y * CELL_SIZE;
            int centerY = MARGIN + p.x * CELL_SIZE;
            int diameter = PIECE_RADIUS * 2 - 4; // 比棋子略小，不抢焦点

            // 渐变绿色（浅绿→深绿，透明度80%）
            GradientPaint moveGradient = new GradientPaint(centerX - PIECE_RADIUS, centerY - PIECE_RADIUS,
                    new Color(0, 255, 0, 80),
                    centerX + PIECE_RADIUS, centerY + PIECE_RADIUS,
                    new Color(0, 200, 0, 80));
            g2d.setPaint(moveGradient);
            // 圆角椭圆填充（更柔和）
            g2d.fillOval(centerX - PIECE_RADIUS + 2, centerY - PIECE_RADIUS + 2,
                    diameter, diameter);
            // 细边框（增加轮廓）
            g2d.setColor(new Color(0, 180, 0, 120));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawOval(centerX - PIECE_RADIUS + 2, centerY - PIECE_RADIUS + 2,
                    diameter, diameter);
        }
    }

    /**
     * 绘制棋盘
     */
    private void drawBoard(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));

        // 绘制横线
        for (int i = 0; i < ChessBoardModel.getRows(); i++) {
            int y = MARGIN + i * CELL_SIZE;
            g.drawLine(MARGIN, y, MARGIN + (ChessBoardModel.getCols() - 1) * CELL_SIZE, y);
        }

        // 绘制竖线
        for (int i = 0; i < ChessBoardModel.getCols(); i++) {
            int x = MARGIN + i * CELL_SIZE;
            if (i == 0 || i == ChessBoardModel.getCols() - 1) {
                // 两边的竖线贯通整个棋盘
                g.drawLine(x, MARGIN, x, MARGIN + (ChessBoardModel.getRows() - 1) * CELL_SIZE);
            } else {
                // 中间的竖线分为上下两段（楚河汉界断开）
                g.drawLine(x, MARGIN, x, MARGIN + 4 * CELL_SIZE);
                g.drawLine(x, MARGIN + 5 * CELL_SIZE, x, MARGIN + (ChessBoardModel.getRows() - 1) * CELL_SIZE);
            }
        }

        // 绘制"楚河"和"汉界"这两个文字
        g.setColor(Color.BLACK);
        g.setFont(new Font("楷体", Font.BOLD, 24));

        int riverY = MARGIN + 4 * CELL_SIZE + CELL_SIZE / 2;

        String chuHeText = "楚河";
        FontMetrics fm = g.getFontMetrics();
        int chuHeWidth = fm.stringWidth(chuHeText);
        g.drawString(chuHeText, MARGIN + CELL_SIZE * 2 - chuHeWidth / 2, riverY + 8);

        String hanJieText = "汉界";
        int hanJieWidth = fm.stringWidth(hanJieText);
        g.drawString(hanJieText, MARGIN + CELL_SIZE * 6 - hanJieWidth / 2, riverY + 8);
    }

    /**
     * 绘制棋子
     */
    private void drawPieces(Graphics2D g) {
        // 确保字体可用（兜底方案，避免字体不存在导致文字不显示）
        Font pieceFont = new Font("楷体", Font.BOLD, 24);
        if (pieceFont == null) {
            pieceFont = new Font("SimHei", Font.BOLD, 24); // 黑体兜底
        }
        g.setFont(pieceFont);

        for (AbstractPiece piece : model.getPieces()) {
            int x = MARGIN + piece.getCol() * CELL_SIZE; // 棋子中心X
            int y = MARGIN + piece.getRow() * CELL_SIZE; // 棋子中心Y

            // 1. 绘制棋子底色（木色）
            g.setColor(new Color(230, 150, 0));
            g.fillOval(x - PIECE_RADIUS, y - PIECE_RADIUS,
                    PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            // 2. 绘制棋子白色边框
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - PIECE_RADIUS, y - PIECE_RADIUS,
                    PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            // 3. 绘制棋子文字（核心：修复文字消失问题）
            String pieceName = piece.getName(); // 如"帅"、"将"、"车"等
            g.setColor(piece.isRed()?Color.RED : Color.BLACK); // 白字在红/黑底上更清晰
            // 文字居中计算（关键：避免文字偏移出棋子）
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(pieceName);
            int textHeight = fm.getAscent() - fm.getDescent();
            int textX = x - textWidth / 2;
            int textY = y + textHeight / 2;
            g.drawString(pieceName, textX, textY); // 绘制文字

            // 4. 选中棋子的黄色方框标记
            if (piece == selectedPiece) {
                g.setColor(Color.YELLOW);
                g.setStroke(new BasicStroke(3));
                g.drawRect(x - PIECE_RADIUS - 4, y - PIECE_RADIUS - 4,
                        (PIECE_RADIUS + 4) * 2, (PIECE_RADIUS + 4) * 2);
            }

            // 5. 将军时将/帅的红色边框
            if (piece instanceof GeneralPiece && model.isInCheck()) {
                boolean isCheckedGeneralRed = ((piece.isRed() == model.isRedTurn()));
                boolean isCheckedGeneralBlack = ((!piece.isRed() == !model.isRedTurn()));
                if (isCheckedGeneralRed || isCheckedGeneralBlack) {
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(4));
                    g.drawOval(x - PIECE_RADIUS - 2, y - PIECE_RADIUS - 2,
                            (PIECE_RADIUS + 2) * 2, (PIECE_RADIUS + 2) * 2);
                }
            }
        }
    }
}