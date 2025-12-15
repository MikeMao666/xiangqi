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

    public enum GameState {
        PLAYING,        // 游戏中
        RED_WIN,        // 红方胜
        BLACK_WIN,      // 黑方胜
        DRAW,           // 和棋
        SURRENDER_RED,  // 红方投降
        SURRENDER_BLACK // 黑方投降
    }

    private GameState gameState = GameState.PLAYING;
    private String victoryMessage = "";
    private boolean isCheck = false;
    // 当前是否将军

    /**
     * 获取当前游戏状态
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * 获取胜利消息
     */
    public String getVictoryMessage() {
        return victoryMessage;
    }

    /**
     * 是否在将军状态
     */
    public boolean isInCheck() {
        // 1. 找到当前回合方的将/帅
        GeneralPiece currentGeneral = null;
        for (AbstractPiece piece : pieces) {
            if (piece instanceof GeneralPiece && piece.isRed() == isRedTurn) { // 注意：是当前回合方
                currentGeneral = (GeneralPiece) piece;
                break;
            }
        }
        if (currentGeneral == null) return false;

        // 2. 检查对方棋子是否能攻击到将/帅
        for (AbstractPiece enemyPiece : pieces) {
            if (enemyPiece.isRed() != isRedTurn) { // 对方棋子
                if (enemyPiece.canMoveTo(currentGeneral.getRow(), currentGeneral.getCol(), this)) {
                    return true; // 能攻击到，说明被将军
                }
            }
        }
        return false;
    }


    /**
     * 检查是否将死
     */
    public boolean checkForCheckmate(boolean forRed) {
        // 首先检查是否被将军
        if (!isInCheckForPlayer(forRed)) {
            return false; // 没有被将军，不是将死
        }

        // 尝试所有可能的走法，看是否能解除将军
        for (AbstractPiece piece : new ArrayList<>(pieces)) { // 使用副本避免并发修改
            if (piece.isRed() == forRed) { // 己方棋子
                // 记录原始位置
                int originalRow = piece.getRow();
                int originalCol = piece.getCol();

                // 尝试所有可能的移动
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (piece.canMoveTo(row, col, this)) {
                            // 检查移动是否会导致将帅对面（规则不允许）
                            // 临时模拟移动
                            AbstractPiece captured = getPieceAt(row, col);
                            piece.moveTo(row, col);
                            if (captured != null) {
                                pieces.remove(captured);
                            }

                            // 检查移动后是否仍被将军
                            boolean stillInCheck = isInCheckForPlayer(forRed);

                            // 检查是否会导致将帅对面
                            boolean facingGenerals = willCauseFacingGeneralsAfterMove();

                            // 回退
                            piece.moveTo(originalRow, originalCol);
                            if (captured != null) {
                                pieces.add(captured);
                            }

                            // 如果移动后不被将军且不导致将帅对面，则不是将死
                            if (!stillInCheck && !facingGenerals) {
                                return false; // 有解将的方法
                            }
                        }
                    }
                }
            }
        }

        return true; // 无解，将死
    }

    /**
     * 检查指定玩家是否被将军 - 专用方法
     */
    private boolean isInCheckForPlayer(boolean forRed) {
        // 1. 找到指定玩家的将/帅
        GeneralPiece playerGeneral = null;
        for (AbstractPiece piece : pieces) {
            if (piece instanceof GeneralPiece && piece.isRed() == forRed) {
                playerGeneral = (GeneralPiece) piece;
                break;
            }
        }
        if (playerGeneral == null) return false;

        // 2. 检查对方棋子是否能攻击到将/帅
        for (AbstractPiece enemyPiece : pieces) {
            if (enemyPiece.isRed() != forRed) { // 对方棋子
                if (enemyPiece.canMoveTo(playerGeneral.getRow(), playerGeneral.getCol(), this)) {
                    return true; // 能攻击到，说明被将军
                }
            }
        }
        return false;
    }


    /**
     * 检查是否困毙（无子可动且不被将军）
     */
    public boolean checkForStalemate(boolean forRed) {
        // 首先检查是否被将军
        if (isInCheckForPlayer(forRed)) {
            return false; // 被将军不是困毙
        }

        // 检查是否有任何合法走法
        for (AbstractPiece piece : new ArrayList<>(pieces)) { // 使用副本
            if (piece.isRed() == forRed) { // 己方棋子
                int originalRow = piece.getRow();
                int originalCol = piece.getCol();

                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (piece.canMoveTo(row, col, this)) {
                            // 模拟移动以检查是否会导致将帅对面
                            AbstractPiece captured = getPieceAt(row, col);
                            piece.moveTo(row, col);
                            if (captured != null) {
                                pieces.remove(captured);
                            }

                            // 检查移动是否会导致将帅对面
                            boolean facingGenerals = willCauseFacingGeneralsAfterMove();

                            // 回退
                            piece.moveTo(originalRow, originalCol);
                            if (captured != null) {
                                pieces.add(captured);
                            }

                            // 如果有合法移动且不导致将帅对面，则不是困毙
                            if (!facingGenerals) {
                                return false; // 有合法走法
                            }
                        }
                    }
                }
            }
        }

        return true; // 无子可动
    }

    /**
     * 更新游戏状态
     */
    private void updateGameState() {
        // 首先检查将/帅是否还存在
        checkGeneralsExistence();

        // 如果游戏已经结束（比如将/帅被吃），直接返回
        if (gameState != GameState.PLAYING) {
            return;
        }

        // 然后检查是否将军
        boolean currentPlayerInCheck = isInCheck();

        if (currentPlayerInCheck) {
            // 当前玩家被将军，检查是否将死
            if (checkForCheckmate(isRedTurn)) {
                // 当前玩家被将死，对方胜利
                if (isRedTurn) {
                    gameState = GameState.BLACK_WIN;
                    victoryMessage = "红方被将死！黑方胜利！";
                } else {
                    gameState = GameState.RED_WIN;
                    victoryMessage = "黑方被将死！红方胜利！";
                }
                return;
            }
        } else {
            // 没有被将军，检查是否困毙
            if (checkForStalemate(isRedTurn)) {
                gameState = GameState.DRAW;
                victoryMessage = "和棋！困毙！";
                return;
            }
        }

        // 检查将帅对面
        if (willCauseFacingGeneralsAfterMove()) {
            // 将帅对面的规则：违规的一方输
            if (isRedTurn) {
                gameState = GameState.BLACK_WIN;
                victoryMessage = "红方违规！将帅对面！黑方胜利！";
            } else {
                gameState = GameState.RED_WIN;
                victoryMessage = "黑方违规！将帅对面！红方胜利！";
            }
        }
    }

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

        if (gameState != GameState.PLAYING) {
            return false; // 游戏已结束
        }

        if (!piece.canMoveTo(newRow, newCol, this)) {
            return false;
        }

        if(piece.isRed() != isRedTurn){
            return false;
        }

        // 保存原始位置
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();

        // 获取目标位置的棋子（可能被吃掉）
        AbstractPiece targetPiece = this.getPieceAt(newRow, newCol);

        // 临时移动棋子检查是否会导致将帅对面
        if (targetPiece != null && targetPiece.isRed() != piece.isRed()) {
            // 临时移除被吃的棋子
            pieces.remove(targetPiece);
        }

        piece.moveTo(newRow, newCol);

        // 检查移动后是否会导致将帅对面
        if (willCauseFacingGeneralsAfterMove()) {
            // 如果会导致将帅对面，撤回
            piece.moveTo(originalRow, originalCol);
            if (targetPiece != null) {
                pieces.add(targetPiece);
            }
            return false;
        }

        // 如果目标位置有对方棋子，吃掉
        if (targetPiece != null && targetPiece.isRed() != piece.isRed()) {
            // 关键：检查是否吃掉的是对方的将/帅
            if (targetPiece instanceof GeneralPiece) {
                // 吃掉了对方的将/帅，游戏立即结束
                if (piece.isRed()) {
                    gameState = GameState.RED_WIN;
                    victoryMessage = "红方吃掉黑将！红方胜利！";
                } else {
                    gameState = GameState.BLACK_WIN;
                    victoryMessage = "黑方吃掉红帅！黑方胜利！";
                }

                // 从棋盘上移除被吃的将/帅
                pieces.remove(targetPiece);

                // 切换回合（虽然游戏结束了，但为了记录可能还是要切换）
                isRedTurn = !isRedTurn;

                // 记录移动历史
                moveHistory.push(new MoveRecord(piece, originalRow, originalCol, newRow, newCol, targetPiece, this));

                // 返回true，移动成功（虽然游戏结束了）
                return true;
            }

            pieces.remove(targetPiece);
        }

        // 切换回合
        isRedTurn = !isRedTurn;

        // 记录移动历史
        moveHistory.push(new MoveRecord(piece, originalRow, originalCol, newRow, newCol, targetPiece, this));

        // 关键：立即更新游戏状态
        updateGameState();

        return true;
    }

    /**
     * 检查游戏是否应该结束（将/帅是否都存在）
     */
    private void checkGeneralsExistence() {
        boolean redGeneralExists = false;
        boolean blackGeneralExists = false;

        for (AbstractPiece piece : pieces) {
            if (piece instanceof GeneralPiece) {
                if (piece.isRed()) {
                    redGeneralExists = true;
                } else {
                    blackGeneralExists = true;
                }
            }
        }

        // 如果红方将/帅不存在，黑方胜利
        if (!redGeneralExists && gameState == GameState.PLAYING) {
            gameState = GameState.BLACK_WIN;
            victoryMessage = "红方将/帅被吃！黑方胜利！";
        }

        // 如果黑方将/帅不存在，红方胜利
        if (!blackGeneralExists && gameState == GameState.PLAYING) {
            gameState = GameState.RED_WIN;
            victoryMessage = "黑方将/帅被吃！红方胜利！";
        }
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

    /**
     * 投降方法
     */
    public boolean surrender(boolean isRedSurrendering) {
        if (gameState != GameState.PLAYING) {
            return false;
        }

        if (isRedSurrendering) {
            gameState = GameState.SURRENDER_RED;
            victoryMessage = "红方投降！黑方胜利！";
        } else {
            gameState = GameState.SURRENDER_BLACK;
            victoryMessage = "黑方投降！红方胜利！";
        }

        return true;
    }

    /**
     * 提议和棋
     */
    public boolean proposeDraw() {
        if (gameState != GameState.PLAYING) {
            return false;
        }

        // 实际实现需要双方同意
        // 这里简化为直接判和
        gameState = GameState.DRAW;
        victoryMessage = "双方同意和棋！";
        return true;
    }

    /**
     * 重置游戏状态
     */
    public void resetGame() {
        pieces.clear();
        initializePieces();
        isRedTurn = true;
        moveHistory.clear();
        gameState = GameState.PLAYING;
        victoryMessage = "";
        isCheck = false;
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
