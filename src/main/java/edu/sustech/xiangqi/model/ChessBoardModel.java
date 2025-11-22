package edu.sustech.xiangqi.model;

import java.util.ArrayList;
import java.util.List;

public class ChessBoardModel {
    // 储存棋盘上所有的棋子，要实现吃子的话，直接通过pieces.remove(被吃掉的棋子)删除就可以
    private final List<AbstractPiece> pieces;
    private static final int ROWS = 10;
    private static final int COLS = 9;

    public ChessBoardModel() {
        pieces = new ArrayList<>();
        initializePieces();
    }

    private void initializePieces() {
        // 黑方棋子
        pieces.add(new GeneralPiece("將", 0, 4, false));
        pieces.add(new AdvisorPiece("士", 0, 3, false));
        pieces.add(new AdvisorPiece("士", 0, 5, false));
        pieces.add(new ElephantPiece("象", 0, 2, false));
        pieces.add(new ElephantPiece("象", 0, 6, false));
        pieces.add(new HorsePiece("馬", 0, 1, false));
        pieces.add(new HorsePiece("馬", 0, 7, false));
        pieces.add(new ChariotPiece("車", 0, 0, false));
        pieces.add(new ChariotPiece("車", 0, 8, false));
        pieces.add(new CannonPiece("炮", 2, 1, false));
        pieces.add(new CannonPiece("炮", 2, 7, false));
        pieces.add(new SoldierPiece("卒", 3, 0, false));
        pieces.add(new SoldierPiece("卒", 3, 2, false));
        pieces.add(new SoldierPiece("卒", 3, 4, false));
        pieces.add(new SoldierPiece("卒", 3, 6, false));
        pieces.add(new SoldierPiece("卒", 3, 8, false));

        // 红方棋子
        pieces.add(new GeneralPiece("帅", 9, 4, true));
        pieces.add(new AdvisorPiece("仕", 9, 3, true));
        pieces.add(new AdvisorPiece("仕", 9, 5, true));
        pieces.add(new ElephantPiece("相", 9, 2, true));
        pieces.add(new ElephantPiece("相", 9, 6, true));
        pieces.add(new HorsePiece("马", 9, 1, true));
        pieces.add(new HorsePiece("马", 9, 7, true));
        pieces.add(new ChariotPiece("车", 9, 0, true));
        pieces.add(new ChariotPiece("车", 9, 8, true));
        pieces.add(new CannonPiece("炮", 7, 1, true));
        pieces.add(new CannonPiece("炮", 7, 7, true));
        pieces.add(new SoldierPiece("兵", 6, 0, true));
        pieces.add(new SoldierPiece("兵", 6, 2, true));
        pieces.add(new SoldierPiece("兵", 6, 4, true));
        pieces.add(new SoldierPiece("兵", 6, 6, true));
        pieces.add(new SoldierPiece("兵", 6, 8, true));
    }

    public List<AbstractPiece> getPieces() {
        return pieces;
    }

    public AbstractPiece getPieceAt(int row, int col) {
        for (AbstractPiece piece : pieces) {
            if (piece.getRow() == row && piece.getCol() == col) {
                return piece;
            }
        }
        return null;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public boolean movePiece(AbstractPiece piece, int newRow, int newCol) {
        if (!isValidPosition(newRow, newCol)) {
            return false;
        }

        if (!piece.canMoveTo(newRow, newCol, this)) {
            return false;
        }

        // 保存原始位置
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();

        // 临时移动棋子检查是否会导致将帅对面
        AbstractPiece targetPiece = getPieceAt(newRow, newCol);
        if (targetPiece != null && targetPiece.isRed() != piece.isRed()) {
            // 临时移除被吃的棋子
            pieces.remove(targetPiece);
        }

        piece.moveTo(newRow, newCol);

        // 检查移动后是否会导致将帅对面
        if (willCauseFacingGeneralsAfterMove()) {
            // 如果会导致将帅对面，z撤回
            piece.moveTo(originalRow, originalCol);
            if (targetPiece != null) {
                pieces.add(targetPiece);
            }
            return false;
        }

        // 如果目标位置有对方棋子，吃
        if (targetPiece != null && targetPiece.isRed() != piece.isRed()) {
            pieces.remove(targetPiece);
        }

        return true;
    }

    /**
     * 检查移动后是否会导致将帅直接对面
     */
    private boolean willCauseFacingGeneralsAfterMove() {
        GeneralPiece redGeneral = null;
        GeneralPiece blackGeneral = null;

        // 找到两个将帅
        for (AbstractPiece piece : pieces) {
            if (piece instanceof GeneralPiece) {
                if (piece.isRed()) {
                    redGeneral = (GeneralPiece) piece;
                } else {
                    blackGeneral = (GeneralPiece) piece;
                }
            }
        }

        if (redGeneral == null || blackGeneral == null) {
            return false;
        }

        // 检查是否在同一列
        if (redGeneral.getCol() != blackGeneral.getCol()) {
            return false;
        }

        // 检查中间是否有其他棋子
        int topRow = Math.min(redGeneral.getRow(), blackGeneral.getRow());
        int bottomRow = Math.max(redGeneral.getRow(), blackGeneral.getRow());

        for (int row = topRow + 1; row < bottomRow; row++) {
            // 中间有棋子，不会对面
            if (getPieceAt(row, redGeneral.getCol()) != null) {
                return false;
            }
        }

        return true;
    }
    public static int getRows() {
        return ROWS;
    }

    public static int getCols() {
        return COLS;
    }

}
