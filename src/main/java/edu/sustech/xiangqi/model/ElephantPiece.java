package edu.sustech.xiangqi.model;

/**
 * 象/相
 */
public class ElephantPiece extends AbstractPiece {

    public ElephantPiece(String name, int row, int col, boolean isRed) {
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

        // 象走"田"字：行差2列差2
        if (rowDiff != 2 || colDiff != 2) {
            return false;
        }

        // 检查塞象眼（田字中心）
        int middleRow = currentRow + (targetRow - currentRow) / 2;
        int middleCol = currentCol + (targetCol - currentCol) / 2;
        if (model.getPieceAt(middleRow, middleCol) != null) {
            return false;
        }

        // 检查是否过河
        if (isRed()) {
            // 红方象不能过河（行5-9）
            return targetRow >= 5;
        } else {
            // 黑方象不能过河（行0-4）
            return targetRow <= 4;
        }
    }
}