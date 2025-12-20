package edu.sustech.xiangqi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Save {
    @JsonIgnore
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String saveName;
    private String username;
    private String saveTime;
    private boolean isRedTurn;
    private List<String> moveNotations;
    //用于记录赛制和剩余时间 ---
    private int initialTime;   // 初始时间(秒)
    private int incrementTime; // 加秒(秒)
    private int redTimeLeft;   // 红方剩余(秒)
    private int blackTimeLeft; // 黑方剩余(秒)

    public Save() {
    }

    public Save(String saveName, String username, boolean isRedTurn, List<String> moveNotations) {
        this(saveName, username, isRedTurn, moveNotations, 0, 0, 0, 0);
    }

    // 全参数构造函数
    public Save(String saveName, String username, boolean isRedTurn, List<String> moveNotations,
                int initialTime, int incrementTime, int redTimeLeft, int blackTimeLeft) {
        this.saveName = saveName;
        this.username = username;
        this.saveTime = LocalDateTime.now().format(FORMATTER);
        this.isRedTurn = isRedTurn;
        this.moveNotations = moveNotations != null ? moveNotations : new ArrayList<>();

        this.initialTime = initialTime;
        this.incrementTime = incrementTime;
        this.redTimeLeft = redTimeLeft;
        this.blackTimeLeft = blackTimeLeft;
    }

    @JsonIgnore
    public boolean isValid() {
        return saveName != null && username != null && moveNotations != null;
    }

    // 判断是否是计时赛存档
    @JsonIgnore
    public boolean isTimedGame() {
        return initialTime > 0;
    }

    // Getters and Setters for new fields
    public int getInitialTime() {
        return initialTime;
    }

    public void setInitialTime(int initialTime) {
        this.initialTime = initialTime;
    }

    public int getIncrementTime() {
        return incrementTime;
    }

    public void setIncrementTime(int incrementTime) {
        this.incrementTime = incrementTime;
    }

    public int getRedTimeLeft() {
        return redTimeLeft;
    }

    public void setRedTimeLeft(int redTimeLeft) {
        this.redTimeLeft = redTimeLeft;
    }

    public int getBlackTimeLeft() {
        return blackTimeLeft;
    }

    public void setBlackTimeLeft(int blackTimeLeft) {
        this.blackTimeLeft = blackTimeLeft;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }

    public boolean isRedTurn() {
        return isRedTurn;
    }

    public void setRedTurn(boolean redTurn) {
        isRedTurn = redTurn;
    }

    public List<String> getMoveNotations() {
        return moveNotations;
    }

    public void setMoveNotations(List<String> moveNotations) {
        this.moveNotations = moveNotations;
    }
}