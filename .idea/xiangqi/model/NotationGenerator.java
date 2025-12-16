package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.AbstractPiece;
import edu.sustech.xiangqi.model.pieces.SoldierPiece;

import java.util.ArrayList;
import java.util.List;

public class NotationGenerator {

    private static final String[] COLUMN_NOTATION_RED = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] COLUMN_NOTATION_BLACK = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};

    /**
     * 原有的生成棋谱方法（为了兼容性保留）
     */
    public static String generateNotation(AbstractPiece piece, int fromRow, int fromCol,
                                          int toRow, int toCol, ChessBoardModel model) {
        return generateNotation(piece.getName(), piece.isRed(),
                fromRow, fromCol, toRow, toCol, model);
    }

    /**
     * 生成棋谱记录（使用原始位置信息，而不是棋子的当前位置）
     */
    public static String generateNotation(String pieceName, boolean isRed,
                                          int fromRow, int fromCol,
                                          int toRow, int toCol, ChessBoardModel model) {
        // 获取列坐标表示
        String fromColNotation = getColumnNotation(fromCol, isRed);
        String toColNotation = getColumnNotation(toCol, isRed);

        // 判断移动方向并生成记录
        String direction = getMoveDirection(isRed, pieceName, fromRow, fromCol, toRow, toCol, toColNotation);

        // 处理同一列有多个相同棋子的情况
        String positionPrefix = getPositionPrefix(pieceName, isRed, fromRow, fromCol, model);

        if (positionPrefix.isEmpty())
            return pieceName + fromColNotation + direction;

        return positionPrefix + direction;
    }

    /**
     * 获取移动方向描述
     */
    private static String getMoveDirection(boolean isRed, String pieceName,
                                           int fromRow, int fromCol,
                                           int toRow, int toCol, String toColNotation) {
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        if (rowDiff == 0)
            return "平" + toColNotation;
        else {
            if (isRed) {
                // 红方：向前是行数减小
                return (rowDiff < 0 ? "进" : "退") +
                        (colDiff != 0 ? toColNotation : COLUMN_NOTATION_RED[Math.abs(rowDiff) - 1]);
            } else {
                // 黑方：向前是行数增加
                return (rowDiff > 0 ? "进" : "退") +
                        (colDiff != 0 ? toColNotation : COLUMN_NOTATION_BLACK[Math.abs(rowDiff) - 1]);
            }
        }
    }

    /**
     * 获取位置前缀（处理同一列有多个相同棋子的情况）
     * 关键修复：这个方法不再依赖棋子的当前位置，而是基于传入的原始位置
     */
    private static String getPositionPrefix(String pieceName, boolean isRed,
                                            int fromRow, int fromCol,
                                            ChessBoardModel model) {
        // 统计同一列中相同类型的棋子数量（基于传入的原始位置）
        List<AbstractPiece> sameColumnPieces = new ArrayList<>();
        int[] otherColumnCounts = new int[9]; // 记录其他列的同名棋子数量（用于兵的特殊情况）

        // 遍历棋盘上的所有棋子
        for (AbstractPiece p : model.getPieces()) {
            // 只统计同色、同类型的棋子
            if (p.isRed() == isRed && p.getName().equals(pieceName)) {
                if (p.getCol() == fromCol) {
                    // 在同一列，添加到列表中
                    sameColumnPieces.add(p);
                } else if (pieceName.equals("兵") || pieceName.equals("卒")) {
                    // 对于兵/卒，记录其他列的数量
                    otherColumnCounts[p.getCol()]++;
                }
            }
        }

        // 如果只有一个，不需要前缀
        if (sameColumnPieces.size() <= 1) {
            return "";
        }

        // 按行排序：红方从上到下（行号小到大），黑方从下到上（行号大到小）
        sameColumnPieces.sort((p1, p2) -> {
            if (isRed) {
                return Integer.compare(p1.getRow(), p2.getRow());
            } else {
                return Integer.compare(p2.getRow(), p1.getRow());
            }
        });

        // 关键：我们需要找到哪个棋子是正在移动的棋子
        // 由于棋子可能已经移动了，我们通过位置匹配来找到它
        AbstractPiece movingPiece = null;
        for (AbstractPiece p : sameColumnPieces) {
            if (p.getRow() == fromRow && p.getCol() == fromCol) {
                movingPiece = p;
                break;
            }
        }

        // 如果没有找到（可能是棋子已经移动了），使用第一个
        if (movingPiece == null && !sameColumnPieces.isEmpty()) {
            movingPiece = sameColumnPieces.get(0);
        }

        int index = sameColumnPieces.indexOf(movingPiece);

        // 非兵/卒的棋子
        if (!pieceName.equals("兵") && !pieceName.equals("卒")) {
            return (index == 0 ? "前" : "后") + pieceName;
        }
        // 兵/卒的特殊情况
        else {
            // 检查是否有其他列存在多个同名棋子
            boolean hasMultipleInOtherColumn = false;
            for (int count : otherColumnCounts) {
                if (count > 1) {
                    hasMultipleInOtherColumn = true;
                    break;
                }
            }

            if (!hasMultipleInOtherColumn && sameColumnPieces.size() == 2) {
                return (index == 0 ? "前" : "后") + pieceName;
            } else {
                String indexNotation = isRed ?
                        COLUMN_NOTATION_RED[index] :
                        COLUMN_NOTATION_BLACK[index];
                String colNotation = getColumnNotation(fromCol, isRed);
                return indexNotation + colNotation;
            }
        }
    }

    /**
     * 获取列坐标表示
     */
    private static String getColumnNotation(int col, boolean isRed) {
        if (isRed) {
            // 红方视角：从右到左（8->一, 7->二, ..., 0->九）
            // 注意：这里要确保索引在范围内
            int index = 8 - col;
            if (index >= 0 && index < COLUMN_NOTATION_RED.length) {
                return COLUMN_NOTATION_RED[index];
            }
        } else {
            // 黑方视角：从左到右（0->1, 1->2, ..., 8->9）
            if (col >= 0 && col < COLUMN_NOTATION_BLACK.length) {
                return COLUMN_NOTATION_BLACK[col];
            }
        }
        return ""; // 默认返回空字符串
    }
}