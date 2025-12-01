package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;


public class ChessBoardModel {
    // 储存棋盘上所有的棋子，要实现吃子的话，直接通过pieces.remove(被吃掉的棋子)删除就可以
    private final List<AbstractPiece> pieces;
    private static final int ROWS = 10;
    private static final int COLS = 9;
    private boolean isRedTurn = true;

    private Stack<MoveRecord> moveHistory = new Stack<>( );
    public ChessBoardModel() {
        pieces = new ArrayList<>();
        initializePieces();
    }

    public boolean isRedTurn( ){
        return this.isRedTurn;
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
        pieces.add(new CannonPiece("砲", 2, 1, false));
        pieces.add(new CannonPiece("砲", 2, 7, false));
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

        if(piece.isRed() != isRedTurn()){
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
        isRedTurn = !isRedTurn;

        moveHistory.push(new MoveRecord(piece, originalRow, originalCol, newRow, newCol, targetPiece, this));

        return true;
    }

    /**
     * 悔棋方法
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false; // 没有可悔棋的步骤
        }

        MoveRecord lastMove = moveHistory.pop();

        // 将棋子移回原位置
        lastMove.piece.moveTo(lastMove.fromRow, lastMove.fromCol);

        // 如果这一步吃了棋子，恢复被吃的棋子
        if (lastMove.capturedPiece != null) {
            pieces.add(lastMove.capturedPiece);
        }

        // 切换回合（回到上一步的玩家回合）
        isRedTurn = !isRedTurn;

        return true;
    }

    /**
     * 检查是否可以悔棋
     */
    public boolean canUndo() {
        return !moveHistory.isEmpty();
    }

    public void setRedTurn(boolean redTurn) {
        isRedTurn = redTurn;
    }

    /*
    * 读写移动记录
    * */
    public Stack<MoveRecord> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(Stack<MoveRecord> moveHistory) {
        this.moveHistory = moveHistory;
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

    public void resetGame() {
        pieces.clear();
        initializePieces();
        isRedTurn = true;
        moveHistory.clear();
    }
    public static int getRows() {
        return ROWS;
    }

    public static int getCols() {
        return COLS;
    }

    /**
     * 从棋谱存档加载游戏
     */
    public boolean loadFromNotationSave(Save save) {
        if (save == null) return false;

        try {
            // 重置棋盘到初始状态
            resetGame();

            // 重新执行所有走法
            List<String> notations = save.getMoveNotations();
            for (String notation : notations) {
                NotationAnalyzer analyzer = new NotationAnalyzer(notation, this);

                int fromRow = analyzer.getFromRow();
                int fromCol = analyzer.getFromCol();
                int toRow = analyzer.getToRow();
                int toCol = analyzer.getToCol();

                AbstractPiece piece = getPieceAt(fromRow, fromCol);

                boolean isValidMove = this.movePiece(piece, toRow, toCol);
                if (!isValidMove)
                    return false;
            }

            // 设置正确的回合状态
            this.isRedTurn = save.isRedTurn();

            return true;

        } catch (Exception e) {
            System.err.println("从棋谱加载游戏时发生错误: " + e.getMessage());
            // 出错时重置游戏
            resetGame();
            return false;
        }
    }

    /**
     * 获取当前棋谱记录（用于显示）
     */
    public List<String> getMoveNotations() {
        return moveHistory.stream()
                .map(MoveRecord::getNotation)
                .collect(Collectors.toList());
    }

    /**
     * 获取格式化的棋谱（带序号）
     */
    public List<String> getFormattedNotations() {
        List<String> notations = getMoveNotations();
        List<String> formattedNotation = new ArrayList<>();

        for (int i = 0; i < notations.size(); i += 2) {
            int moveNumber = (i / 2) + 1;
            String redMove = notations.get(i);
            String blackMove = (i + 1 < notations.size()) ? notations.get(i + 1) : "";

            formattedNotation.add(String.format("%-6d%-10s%-10s", moveNumber, redMove, blackMove));
        }

        return formattedNotation;
    }
}
