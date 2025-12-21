package edu.sustech.xiangqi.model.pieces;

import edu.sustech.xiangqi.model.ChessBoardModel;

/**
 * 炮
 */
public class CannonPiece extends AbstractPiece {

    public CannonPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed);
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();
        int currentCol = getCol();

        // 不能原地不动
        if (currentRow == targetRow && currentCol == targetCol) {
            return false;
        }

        // 不能吃自己的棋子
        AbstractPiece targetPiece = model.getPieceAt(targetRow, targetCol);
        if (targetPiece != null && targetPiece.isRed() == this.isRed()) {
            return false;
        }

        // 炮只能直线移动
        if (currentRow != targetRow && currentCol != targetCol) {
            return false;
        }

        int pieceCount = countPiecesBetween(currentRow, currentCol, targetRow, targetCol, model);

        if (targetPiece == null) {
            // 移动时：路径上不能有棋子
            return pieceCount == 0;
        } else {
            // 吃子时：路径上必须恰好有一个棋子（炮架）
            return pieceCount == 1;
        }
    }

    /**
     * 计算起点和终点之间的棋子数量
     */
    private int countPiecesBetween(int startRow, int startCol, int endRow, int endCol, ChessBoardModel model) {
        int count = 0;

        if (startRow == endRow) {
            // 横向移动
            int startColTemp = Math.min(startCol, endCol);
            int endColTemp = Math.max(startCol, endCol);
            for (int col = startColTemp + 1; col < endColTemp; col++) {
                if (model.getPieceAt(startRow, col) != null) {
                    count++;
                }
            }
        } else {
            // 纵向移动
            int startRowTemp = Math.min(startRow, endRow);
            int endRowTemp = Math.max(startRow, endRow);
            for (int row = startRowTemp + 1; row < endRowTemp; row++) {
                if (model.getPieceAt(row, startCol) != null) {
                    count++;
                }
            }
        }

        return count;
    }
}