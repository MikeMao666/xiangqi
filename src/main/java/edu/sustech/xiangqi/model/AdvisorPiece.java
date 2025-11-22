package edu.sustech.xiangqi.model;

/**
 * 士/仕
 */
public class AdvisorPiece extends AbstractPiece {

    public AdvisorPiece(String name, int row, int col, boolean isRed) {
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

        // 士只能斜走一格
        if (rowDiff != 1 || colDiff != 1) {
            return false;
        }

        // 检查是否在九宫格内
        return isInPalace(targetRow, targetCol);
    }

    /**
     * 检查目标位置是否在九宫格内
     */
    private boolean isInPalace(int row, int col) {
        if (isRed()) {
            // 红方九宫格：行7-9，列3-5
            return row >= 7 && row <= 9 && col >= 3 && col <= 5;
        } else {
            // 黑方九宫格：行0-2，列3-5
            return row >= 0 && row <= 2 && col >= 3 && col <= 5;
        }
    }
}