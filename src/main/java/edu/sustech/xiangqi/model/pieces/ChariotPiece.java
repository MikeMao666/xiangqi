package edu.sustech.xiangqi.model.pieces;

import edu.sustech.xiangqi.model.ChessBoardModel;

/**
 * 车
 */
public class ChariotPiece extends AbstractPiece {

    public ChariotPiece(String name, int row, int col, boolean isRed) {
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

        // 车只能直线移动
        if (currentRow != targetRow && currentCol != targetCol) {
            return false;
        }

        // 检查路径上是否有其他棋子
        if (currentRow == targetRow) {
            // 横向移动
            int startCol = Math.min(currentCol, targetCol);
            int endCol = Math.max(currentCol, targetCol);
            for (int col = startCol + 1; col < endCol; col++) {
                if (model.getPieceAt(currentRow, col) != null) {
                    return false;
                }
            }
        } else {
            // 纵向移动
            int startRow = Math.min(currentRow, targetRow);
            int endRow = Math.max(currentRow, targetRow);
            for (int row = startRow + 1; row < endRow; row++) {
                if (model.getPieceAt(row, currentCol) != null) {
                    return false;
                }
            }
        }

        return true;
    }
}