package edu.sustech.xiangqi.model;

/**
 * 帅/将
 */
public class GeneralPiece extends AbstractPiece {

    public GeneralPiece(String name, int row, int col, boolean isRed) {
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

        // 1. 检查是否在九宫格内移动
        if (!isInPalace(targetRow, targetCol)) {
            return false;
        }

        // 2. 检查移动距离：只能横向或竖向移动一格
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);

//        // 3. 检查其他棋子移动后是否会导致将帅直接对面
//        if (willCauseFacingGenerals(targetRow, targetCol, model)) {
//            return false;
//        }

        //4.检查将棋移动后是否会导致将帅直接对面
//        if (generalsWillCauseFacingGenerals(targetRow,targetCol,model)){
//            return false;
//        }
    }

    /**
     * 检查目标位置是否在九宫格内
     */
    private boolean isInPalace(int row, int col) {
        if (isRed()) {
            // 红方九宫格：行0-2，列3-5
            return row >= 7 && row <= 9 && col >= 3 && col <= 5;
        } else {
            // 黑方九宫格：行7-9，列3-5
            return row >= 0 && row <= 2 && col >= 3 && col <= 5;
        }
    }
    /*
      检查移动后是否会导致将帅直接对面
     */

//    public  boolean generalsWillCauseFacingGenerals(int targetRow, int targetCol, ChessBoardModel model){
//        GeneralPiece oppositeGeneral = findOppositeGeneral(model);
//        if (oppositeGeneral == null) {
//            return false;
//        }
//
//        // 情况1：移动将本身导致将帅在同一列且中间没有棋子
//        if (targetCol == oppositeGeneral.getCol()) {
//            int topRow = Math.min(targetRow, oppositeGeneral.getRow());
//            int bottomRow = Math.max(targetRow, oppositeGeneral.getRow());
//
//            // 检查两个将之间是否有棋子
//            boolean hasPieceBetween = false;
//            for (int row = topRow + 1; row < bottomRow; row++) {
//                if (model.getPieceAt(row, targetCol) != null) {
//                    hasPieceBetween = true;
//                    break;
//                }
//            }
//
//            // 如果中间没有棋子，将帅直接对面
//            if (!hasPieceBetween) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean willCauseFacingGenerals(int targetRow, int targetCol, ChessBoardModel model, AbstractPiece pieceToMove) {
//        // 找到对方将帅
//        GeneralPiece oppositeGeneral = findOppositeGeneral(model);
//        if (oppositeGeneral == null) {
//            return false;
//        }
//
//        // 情况2：移动中间棋子导致将帅在同一列且中间没有棋子
//        if (getCol() == oppositeGeneral.getCol()) {
//            // 计算两个将之间的行范围
//            int topRow = Math.min(getRow(), oppositeGeneral.getRow());
//            int bottomRow = Math.max(getRow(), oppositeGeneral.getRow());
//
//            // 计算两个将之间的棋子数量
//            int pieceCount = 0;
//            //可能有逻辑错误
//            for (int row = topRow + 1; row < bottomRow; row++) {
//                if (model.getPieceAt(row, getCol()) != null) {
//                    pieceCount++;
//                }
//            }
//
//            // 如果两个将之间只有一个棋子，且这个棋子就是当前棋子
//            // 并且移动会使当前棋子离开这一列，则会导致将帅对面
//            if (pieceCount == 1 &&
//                    model.getPieceAt(getRow(), getCol()) == pieceToMove &&
//                    targetCol != getCol()) {
//                return true;
//            }
//        }
//
//        return false;
//
//    }
//

    /*
      找到对方将帅
     */
//    private GeneralPiece findOppositeGeneral(ChessBoardModel model) {
//        for (int row = 0; row < 10; row++) {
//            for (int col = 0; col < 9; col++) {
//                AbstractPiece piece = model.getPieceAt(row, col);
//                if (piece instanceof GeneralPiece && piece.isRed() != this.isRed()) {
//                    return (GeneralPiece) piece;
//                }
//            }
//        }
//        return null;
//    }
}