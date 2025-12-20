package edu.sustech.xiangqi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Save {
    private String saveName;
    private String username;

    private String saveTime;

    private boolean isRedTurn;
    private List<String> moveNotations;

    // 定义一个静态的时间格式化器
    @JsonIgnore
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Save() {}

    public Save(String saveName, String username, boolean isRedTurn, List<String> moveNotations) {
        this.saveName = saveName;
        this.username = username;
        //使用 LocalDateTime 获取当前时间并格式化
        this.saveTime = LocalDateTime.now().format(FORMATTER);
        this.isRedTurn = isRedTurn;
        this.moveNotations = moveNotations != null ? moveNotations : new ArrayList<>();
    }

    @JsonIgnore
    public boolean isValid() {
        return saveName != null && username != null && moveNotations != null;
    }

    // Getters and Setters
    public String getSaveName() { return saveName; }
    public void setSaveName(String saveName) { this.saveName = saveName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSaveTime() { return saveTime; }
    public void setSaveTime(String saveTime) { this.saveTime = saveTime; }

    public boolean isRedTurn() { return isRedTurn; }
    public void setRedTurn(boolean redTurn) { isRedTurn = redTurn; }

    public List<String> getMoveNotations() { return moveNotations; }
    public void setMoveNotations(List<String> moveNotations) { this.moveNotations = moveNotations; }

}
