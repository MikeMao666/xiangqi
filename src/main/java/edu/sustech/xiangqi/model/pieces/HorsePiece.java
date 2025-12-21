package edu.sustech.xiangqi.model.pieces;

import edu.sustech.xiangqi.model.ChessBoardModel;

/**
 * 马
 */
public class HorsePiece extends AbstractPiece {

    public HorsePiece(String name, int row, int col, boolean isRed) {
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

        int rowDiff = Math.abs(targetRow - currentRow);
        int colDiff = Math.abs(targetCol - currentCol);

        // 马走"日"字：行差2列差1，或者行差1列差2
        if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
            return false;
        }

        // 检查蹩马腿
        if (rowDiff == 2) {
            // 竖向走"日"字，检查中间的行
            int middleRow = currentRow + (targetRow - currentRow) / 2;
            if (model.getPieceAt(middleRow, currentCol) != null) {
                return false;
            }
        } else {
            // 横向走"日"字，检查中间的列
            int middleCol = currentCol + (targetCol - currentCol) / 2;
            if (model.getPieceAt(currentRow, middleCol) != null) {
                return false;
            }
        }

        return true;
    }
}