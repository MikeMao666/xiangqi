package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.AbstractPiece;
import edu.sustech.xiangqi.model.pieces.SoldierPiece;

import java.util.ArrayList;
import java.util.List;

public class NotationGenerator {

    // 数字到中文数字的映射（用于列坐标，从右到左）
    private static final String[] COLUMN_NOTATION_RED = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] COLUMN_NOTATION_BLACK = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};

    /**
     * 生成棋谱记录
     */
    public static String generateNotation(AbstractPiece piece, int fromRow, int fromCol,
                                          int toRow, int toCol, ChessBoardModel model) {
        String pieceName = piece.getName();
        // 获取列坐标表示（红方用中文，黑方用阿拉伯数字）
        String fromColNotation = getColumnNotation(fromCol, piece.isRed());
        String toColNotation = getColumnNotation(toCol, piece.isRed());

        // 判断移动方向并生成记录
        String direction = getMoveDirection(piece, fromRow, fromCol, toRow, toCol, toColNotation);

        // 处理同一列有多个相同棋子的情况
        String positionPrefix = getPositionPrefix(piece, fromCol, model);

        if (positionPrefix.isEmpty())
            return pieceName + fromColNotation + direction;

        return positionPrefix + direction;
    }

    /**
     * 获取列坐标表示
     */
    private static String getColumnNotation(int col, boolean isRed) {
        if (isRed) {
            // 红方视角：从右到左（9->一, 8->二, ..., 1->九, 0->?）
            return COLUMN_NOTATION_RED[8 - col];
        } else {
            // 黑方视角：从左到右（0->1, 1->2, ..., 8->9）
            return COLUMN_NOTATION_BLACK[col];
        }
    }

    /**
     * 获取移动方向描述
     */
    private static String getMoveDirection(AbstractPiece piece, int fromRow, int fromCol,
                                           int toRow, int toCol, String toColNotation) {//没有检测不合法移动！
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        if (rowDiff == 0)
            return "平" + toColNotation;
        else {
            if (piece.isRed()) {
                // 红方：向前是行数减小
                return (rowDiff < 0 ? "进" : "退") + (colDiff != 0 ? toColNotation : COLUMN_NOTATION_RED[Math.abs(rowDiff) - 1]);
            } else {
                // 黑方：向前是行数增加
                return (rowDiff > 0 ? "进" : "退") + (colDiff != 0 ? toColNotation : COLUMN_NOTATION_BLACK[Math.abs(rowDiff) - 1]);
            }
        }
    }

    /**
     * 获取位置前缀（处理同一列有多个相同棋子的情况）
     */
    private static String getPositionPrefix(AbstractPiece piece, int col,
                                            ChessBoardModel model) {//问题在于移走之后piece的坐标变了！
        // 统计同一列中相同类型的棋子数量
        List<AbstractPiece> sameColumnPieces = new ArrayList<>();
        int[] check = new int[9];

        for (AbstractPiece p : model.getPieces()) {
            if (p.isRed() == piece.isRed() &&
                    p.getClass() == piece.getClass() &&
                    p.getCol() == col) {
                sameColumnPieces.add(p);
            }
            if (p.isRed() == piece.isRed() &&
                    p.getClass() == piece.getClass() &&
                    p.getCol() != col &&
                    piece instanceof SoldierPiece &&
                    p != piece) {//不能把自己算进去！
                check[p.getCol()]++;
            }
        }
        //如果自己移走后不在同一column里，就补上
        if (piece.getCol() != col)
            sameColumnPieces.add(piece);

        if (sameColumnPieces.size() <= 1) {
            // 只有一个同类棋子，不需要前缀
            return "";
        } else {
            // 按行排序：红方从上到下（行号小到大），黑方从下到上（行号大到小）
            sameColumnPieces.sort((p1, p2) -> {
                if (piece.isRed()) {
                    return Integer.compare(p1.getRow(), p2.getRow());
                } else {
                    return Integer.compare(p2.getRow(), p1.getRow());
                }
            });

            int index = sameColumnPieces.indexOf(piece);
            if (!(piece instanceof SoldierPiece))
                return (index == 0 ? "前" : "后") + piece.getName();
            else {//兵的特殊情况
                boolean isCheck = false;
                for (int e : check) {
                    if (e > 1) {
                        isCheck = true;
                        break;
                    }
                }
                if (!isCheck && sameColumnPieces.size() == 2) {
                    return (index == 0 ? "前" : "后") + piece.getName();
                } else {
                    return (piece.isRed() ? COLUMN_NOTATION_RED[index] : COLUMN_NOTATION_BLACK[index]) + getColumnNotation(col, piece.isRed());
                }
            }
        }
    }
}