package edu.sustech.xiangqi.model;

import edu.sustech.xiangqi.model.pieces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotationAnalyzer {
    private static final Map<String, Integer> dict = new HashMap<>();

    static {
        dict.put("一", 8);
        dict.put("二", 7);
        dict.put("三", 6);
        dict.put("四", 5);
        dict.put("五", 4);
        dict.put("六", 3);
        dict.put("七", 2);
        dict.put("八", 1);
        dict.put("九", 0);
    }

    private boolean isRed;
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

    public NotationAnalyzer(String notation, ChessBoardModel model) {
        String char1 = String.valueOf(notation.charAt(0));
        String char2 = String.valueOf(notation.charAt(1));
        String moveDirection = String.valueOf(notation.charAt(2));
        String char4 = String.valueOf(notation.charAt(3));

        //找到fromRow 和 fromCol
        String pieceName = "Mamba";//out
        int index;
        switch (char1) {
            case "前", "后" -> {
                switch (char2) {
                    case "兵", "炮", "车", "马", "相", "仕", "帅" -> {
                        isRed = true;
                        pieceName = char2;

                        List<AbstractPiece> sameColumnPieces = new ArrayList<>();

                        for (AbstractPiece p : model.getPieces()) {
                            if (p.getName().equals(pieceName) &&
                                    p.getCol() == fromCol) {
                                sameColumnPieces.add(p);
                            }
                        }
                        sameColumnPieces.sort((p1, p2) -> {
                            if (isRed) {
                                return Integer.compare(p1.getRow(), p2.getRow());
                            } else {
                                return Integer.compare(p2.getRow(), p1.getRow());
                            }
                        });

                        index = char1.equals("前") ? 0 : 1;
                        fromRow = sameColumnPieces.get(index).getRow();
                        fromCol = sameColumnPieces.get(index).getCol();
                    }
                    case "卒", "砲", "車", "馬", "象", "士", "將" -> {
                        isRed = false;
                        pieceName = char2;

                        List<AbstractPiece> sameColumnPieces = new ArrayList<>();

                        for (AbstractPiece p : model.getPieces()) {
                            if (p.getName().equals(pieceName) &&
                                    p.getCol() == fromCol) {
                                sameColumnPieces.add(p);
                            }
                        }
                        sameColumnPieces.sort((p1, p2) -> {
                            if (isRed) {
                                return Integer.compare(p1.getRow(), p2.getRow());
                            } else {
                                return Integer.compare(p2.getRow(), p1.getRow());
                            }
                        });

                        index = char1.equals("前") ? 0 : 1;
                        fromRow = sameColumnPieces.get(index).getRow();
                        fromCol = sameColumnPieces.get(index).getCol();
                    }
                }
            }
            //兵的特殊情况
            case "一", "二", "三", "四", "五" -> {
                isRed = true;
                pieceName = "兵";
                index = switch (char1) {
                    case "一" -> 0;
                    case "二" -> 1;
                    case "三" -> 2;
                    case "四" -> 3;
                    case "五" -> 4;
                    default -> 666;
                };//TODO 检测是否合法？感觉不用？
                fromCol = dict.get(char2);

                //获取fromRow
                List<AbstractPiece> sameColumnPieces = new ArrayList<>();

                for (AbstractPiece p : model.getPieces()) {
                    if (p.getName().equals(pieceName) &&
                            p.getCol() == fromCol) {
                        sameColumnPieces.add(p);
                    }
                }
                sameColumnPieces.sort((p1, p2) -> {
                    if (isRed) {
                        return Integer.compare(p1.getRow(), p2.getRow());
                    } else {
                        return Integer.compare(p2.getRow(), p1.getRow());
                    }
                });
                fromRow = sameColumnPieces.get(index).getRow();
            }
            case "1", "2", "3", "4", "5" -> {
                isRed = false;
                pieceName = "卒";
                index = Integer.parseInt(char1) - 1;
                fromCol = Integer.parseInt(char2) - 1;

                //获取fromRow
                List<AbstractPiece> sameColumnPieces = new ArrayList<>();

                for (AbstractPiece p : model.getPieces()) {
                    if (p.getName().equals(pieceName) &&
                            p.getCol() == fromCol) {
                        sameColumnPieces.add(p);
                    }
                }
                sameColumnPieces.sort((p1, p2) -> {
                    if (isRed) {
                        return Integer.compare(p1.getRow(), p2.getRow());
                    } else {
                        return Integer.compare(p2.getRow(), p1.getRow());
                    }
                });
                fromRow = sameColumnPieces.get(index).getRow();
            }
            //一般情况
            case "兵", "炮", "车", "马", "相", "仕", "帅" -> {
                isRed = true;
                pieceName = char1;
                fromCol = dict.get(char2);

                for (AbstractPiece p : model.getPieces()) {
                    if (p.getName().equals(pieceName) &&
                            p.getCol() == fromCol) {
                        fromRow = p.getRow();
                        break;
                    }
                }
            }
            case "卒", "砲", "車", "馬", "象", "士", "將" -> {
                isRed = false;
                pieceName = char1;
                fromCol = Integer.parseInt(char2) - 1;

                for (AbstractPiece p : model.getPieces()) {
                    if (p.getName().equals(pieceName) &&
                            p.getCol() == fromCol) {
                        fromRow = p.getRow();
                        break;
                    }
                }
            }

            default -> {
                fromCol = 666;
                fromRow = 666;
            }
        }

        //找到toRow 和 toCol
        switch (moveDirection) {
            case "平" -> {
                toCol = (isRed) ? dict.get(char4) : Integer.parseInt(char4) - 1;
                toRow = fromRow;
            }
            case "进", "退" -> {
                switch (pieceName) {
                    //红
                    case "兵", "炮", "车", "帅" -> {
                        toCol = fromCol;
                        toRow = fromRow + (-dict.get(char4) + 9) * ((moveDirection.equals("进")) ? -1 : 1);
                    }
                    case "马", "相", "仕" -> {
                        toCol = dict.get(char4);
                        toRow = fromRow + ((moveDirection.equals("进")) ? -1 : 1) * switch (pieceName) {
                            case "相" -> 2;
                            case "仕" -> 1;
                            case "马" -> (Math.abs(toCol - fromCol) == 2) ? 1 : 2;
                            default -> 0;
                        };
                    }

                    //黑
                    case "卒", "砲", "車", "將" -> {
                        toCol = fromCol;
                        toRow = fromRow + Integer.parseInt(char4) * ((moveDirection.equals("进")) ? 1 : -1);
                    }
                    case "馬", "象", "士" -> {
                        toCol = Integer.parseInt(char4) - 1;
                        toRow = fromRow + ((moveDirection.equals("进")) ? 1 : -1) * switch (pieceName) {
                            case "象" -> 2;
                            case "士" -> 1;
                            case "馬" -> (Math.abs(toCol - fromCol) == 2) ? 1 : 2;
                            default -> 0;
                        };
                    }
                    default -> {
                        toCol = 666;
                        toRow = 666;
                    }
                }
            }
        }
    }

    public int getFromRow() {
        return fromRow;
    }
    public int getFromCol(){
        return fromCol;
    }
    public int getToRow(){
        return  toRow;
    }
    public int getToCol(){
        return toCol;
    }
}
