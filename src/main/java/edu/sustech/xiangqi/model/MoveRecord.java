package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.AbstractPiece;

public class MoveRecord {
    AbstractPiece piece;
    int fromRow;
    int fromCol;
    int toRow;
    int toCol;
    AbstractPiece capturedPiece; // 被吃掉的棋子
    String notation;
    //constructor
    public MoveRecord(AbstractPiece piece, int fromRow, int fromCol, int toRow, int toCol, AbstractPiece capturedPiece, ChessBoardModel model) {
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = capturedPiece;
        this.notation = NotationGenerator.generateNotation(piece, fromRow, fromCol, toRow, toCol, model);
    }

    public String getNotation() {
        return notation;
    }

    public AbstractPiece getPiece() {
        return piece;
    }

    public void setPiece(AbstractPiece piece) {
        this.piece = piece;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public AbstractPiece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(AbstractPiece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public int getToCol() {
        return toCol;
    }

    public void setToCol(int toCol) {
        this.toCol = toCol;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }
}