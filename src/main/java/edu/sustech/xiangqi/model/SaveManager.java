package edu.sustech.xiangqi.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                Save save = objectMapper.readValue(file, Save.class);

                if (save != null && save.isValid()) {
                    try {
                        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                        if (save.getSaveTime() == null) throw new NullPointerException();
                        LocalDateTime.parse(save.getSaveTime(), formatter);//解析

                        // 只有时间格式正确才加入列表
                        saves.add(save);

                    } catch (Exception e) {
                        // 捕获时间解析错误，跳过这个存档
                        System.err.println("跳过损坏存档 (时间格式错误): " + file.getName());
                    }
                }
            } catch (IOException e) {
                System.err.println("跳过损坏存档 (JSON损坏): " + file.getName());
            }
        }

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    /**
     * 导出
     */
    public boolean exportSaveToText(Save save, File targetFile) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            List<String> moves = save.getMoveNotations();

            for (int i = 0; i < moves.size(); i += 2) {
                int round = (i / 2) + 1;
                String redMove = moves.get(i);
                String blackMove = (i + 1 < moves.size()) ? moves.get(i + 1) : "";

                if (blackMove.isEmpty()) {
                    writer.printf("%d. %s%n", round, redMove);
                } else {
                    writer.printf("%d. %s %s%n", round, redMove, blackMove);
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 导入
     */
    public Save importSaveFromText(File sourceFile, String username) {
        List<String> moveNotations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 去掉序号 数字 + 点 + 可能的空格
                line = line.replaceAll("\\d+\\.", " ");

                // 替换掉可能存在的全角空格或其他分隔符，统一变为空格
                line = line.replaceAll("[\\t\\u3000]", " ");

                // 按空格分割
                String[] parts = line.split("\\s+");
                for (String part : parts) {
                        moveNotations.add(part);
                }
            }

            if (moveNotations.isEmpty()) {
                return null;
            }

            String timeStr = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            String saveName = "Imported_" + timeStr;

            boolean isRedTurn = (moveNotations.size() % 2 == 0);

            return new Save(saveName, username, isRedTurn, moveNotations);

        } catch (IOException e) {
            return null;
        }
    }
}