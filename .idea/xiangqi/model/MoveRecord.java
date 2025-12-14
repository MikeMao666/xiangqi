package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.AbstractPiece;

public class MoveRecord {
    AbstractPiece piece;
    int fromRow;
    int fromCol;
    int toRow;
    int toCol;
    AbstractPiece capturedPiece;
    String notation;

    // 新增：保存棋子移动前的信息
    private String pieceName;
    private boolean isRed;

    // 修改构造函数
    public MoveRecord(AbstractPiece piece, int fromRow, int fromCol,
                      int toRow, int toCol, AbstractPiece capturedPiece,
                      ChessBoardModel model) {
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = capturedPiece;

        // 保存棋子移动前的信息
        this.pieceName = piece.getName();
        this.isRed = piece.isRed();

        // 使用修改后的方法生成棋谱
        this.notation = NotationGenerator.generateNotation(
                pieceName, isRed, fromRow, fromCol, toRow, toCol, model);
    }

    public String getNotation() {
        return notation;
    }
}