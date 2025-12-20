package edu.sustech.xiangqi.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SaveManager {
    private static final String SAVE_DIR = "resources/saves";
    private ObjectMapper objectMapper;

    public SaveManager() {
        this.objectMapper = new ObjectMapper();
    }

    public boolean saveGame(String saveName, String username, ChessBoardModel model) {
        try {
            // 生成棋谱记录列表
            List<String> moveNotations = model.getMoveNotations();

            Save save = new Save(saveName, username, model.isRedTurn(), moveNotations);

            File saveFile = new File(SAVE_DIR, username + "_" + saveName + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(saveFile, save);
            return true;

        } catch (IOException e) {
            System.err.println("保存游戏失败: " + e.getMessage());
            return false;
        }
    }

    public Save loadGame(String saveName, String username) {
        try {
            File saveFile = new File(SAVE_DIR, username + "_" + saveName + ".json");
            if (!saveFile.exists()) {
                return null;
            }

            Save save = objectMapper.readValue(saveFile, Save.class);

            if (!save.isValid()) {
                System.err.println("存档文件已损坏");
                return null;
            }

            return save;

        } catch (IOException e) {
            System.err.println("加载游戏失败，存档可能已损坏: " + e.getMessage());
            return null;
        }
    }

    public List<Save> getUserSaves(String username) {
        File saveDir = new File(SAVE_DIR);
        File[] files = saveDir.listFiles((dir, name) -> name.startsWith(username + "_") && name.endsWith(".json"));

        List<Save> saves = new ArrayList<>();
        if (files == null) {
            return saves;
        }

        for (File file : files) {
            try {
                // 读取存档对象
                Save save = objectMapper.readValue(file, Save.class);
                if (save != null && save.isValid()) {
                    saves.add(save);
                }
            } catch (IOException e) {
                System.err.println("读取存档列表失败: " + file.getName());
            }
        }

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 按时间倒序排序
        saves.sort((s1, s2) -> {
            // 解析字符串回 LocalDateTime 进行比较
            java.time.LocalDateTime t1 = java.time.LocalDateTime.parse(s1.getSaveTime(), formatter);
            java.time.LocalDateTime t2 = java.time.LocalDateTime.parse(s2.getSaveTime(), formatter);
            return t2.compareTo(t1); // 倒序
        });

        return saves;
    }

    public boolean deleteSave(String saveName, String username) {
        File saveFile = new File(SAVE_DIR, username + "_" + saveName + ".json");
        return saveFile.exists() && saveFile.delete();
    }
}