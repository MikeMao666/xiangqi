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
        new File(SAVE_DIR).mkdirs();
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

    public List<String> getUserSaves(String username) {
        File saveDir = new File(SAVE_DIR);
        File[] files = saveDir.listFiles((dir, name) -> name.startsWith(username + "_") && name.endsWith(".json"));

        if (files == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(files)
                .map(file -> {
                    String name = file.getName();
                    return name.substring((username + "_").length(), name.length() - 5);
                })
                .collect(Collectors.toList());
    }

    public boolean deleteSave(String saveName, String username) {
        File saveFile = new File(SAVE_DIR, username + "_" + saveName + ".json");
        return saveFile.exists() && saveFile.delete();
    }
}