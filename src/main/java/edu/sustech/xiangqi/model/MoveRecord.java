package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.AbstractPiece;

import java.util.Stack;

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
}