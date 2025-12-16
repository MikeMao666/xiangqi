package edu.sustech.xiangqi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Save {
    private String saveName;
    private String username;
    private Date saveTime;
    private boolean isRedTurn;
    private List<String> moveNotations; // 存储棋谱记录
    public Save() {}

    public Save(String saveName, String username, boolean isRedTurn, List<String> moveNotations) {
        this.saveName = saveName;
        this.username = username;
        this.saveTime = new Date();
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

    public Date getSaveTime() { return saveTime; }
    public void setSaveTime(Date saveTime) { this.saveTime = saveTime; }

    public boolean isRedTurn() { return isRedTurn; }
    public void setRedTurn(boolean redTurn) { isRedTurn = redTurn; }

    public List<String> getMoveNotations() { return moveNotations; }
    public void setMoveNotations(List<String> moveNotations) { this.moveNotations = moveNotations; }

//    public String getInitialFen() { return initialFen; }
//    public void setInitialFen(String initialFen) { this.initialFen = initialFen; }
}
