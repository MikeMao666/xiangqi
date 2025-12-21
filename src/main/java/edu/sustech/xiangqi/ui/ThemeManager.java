package edu.sustech.xiangqi.ui;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private String currentTheme = "classic";
    private Map<String, Theme> themes;

    private ThemeManager() {
        themes = new HashMap<>();
        initializeThemes();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void initializeThemes() {
        // 经典主题（默认）
        Theme classic = new Theme();
        classic.boardColor = new Color(220, 179, 92);
        classic.gridColor = Color.BLACK;
        classic.redPieceColor = Color.RED;
        classic.blackPieceColor = Color.BLACK;
        classic.pieceBgColor = new Color(230, 150, 0);
        classic.highlightColor = new Color(0, 200, 0, 120);
        classic.selectedColor = Color.YELLOW;
        classic.checkColor = Color.RED;
        classic.validMoveColor = new Color(0, 200, 0, 80);
        classic.uiBgColor = new Color(240, 240, 240);
        classic.uiTextColor = Color.BLACK;
        themes.put("classic", classic);

        // 深色主题
        Theme dark = new Theme();
        dark.boardColor = new Color(60, 63, 65);
        dark.gridColor = new Color(187, 187, 187);
        dark.redPieceColor = new Color(255, 100, 100);
        dark.blackPieceColor = new Color(180, 180, 180);
        dark.pieceBgColor = new Color(80, 80, 80);
        dark.highlightColor = new Color(100, 200, 100, 120);
        dark.selectedColor = new Color(255, 255, 100);
        dark.checkColor = new Color(255, 50, 50);
        dark.validMoveColor = new Color(100, 200, 100, 80);
        dark.uiBgColor = new Color(45, 45, 48);
        dark.uiTextColor = new Color(240, 240, 240);
        themes.put("dark", dark);

        // 木质主题
        Theme wooden = new Theme();
        wooden.boardColor = new Color(139, 90, 43);
        wooden.gridColor = new Color(101, 67, 33);
        wooden.redPieceColor = Color.RED;
        wooden.blackPieceColor = Color.BLACK;
        wooden.pieceBgColor = new Color(222, 184, 135);
        wooden.highlightColor = new Color(255, 215, 0, 120);
        wooden.selectedColor = new Color(255, 255, 100);
        wooden.checkColor = Color.RED;
        wooden.validMoveColor = new Color(255, 215, 0, 80);
        wooden.uiBgColor = new Color(245, 222, 179);
        wooden.uiTextColor = Color.BLACK;
        themes.put("wooden", wooden);

        // 现代主题
        Theme modern = new Theme();
        modern.boardColor = new Color(240, 240, 240);
        modern.gridColor = new Color(100, 100, 100);
        modern.redPieceColor = new Color(220, 20, 60);
        modern.blackPieceColor = new Color(30, 30, 30);
        modern.pieceBgColor = Color.WHITE;
        modern.highlightColor = new Color(30, 144, 255, 120);
        modern.selectedColor = new Color(255, 165, 0);
        modern.checkColor = new Color(220, 20, 60);
        modern.validMoveColor = new Color(30, 144, 255, 80);
        modern.uiBgColor = Color.WHITE;
        modern.uiTextColor = Color.BLACK;
        themes.put("modern", modern);
    }

    public void setTheme(String themeName) {
        if (themes.containsKey(themeName)) {
            currentTheme = themeName;
        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public Theme getTheme(String themeName) {
        return themes.getOrDefault(themeName, themes.get("classic"));
    }

    public Theme getCurrentThemeObject() {
        return themes.get(currentTheme);
    }

    public String[] getAvailableThemes() {
        return themes.keySet().toArray(new String[0]);
    }

    public static class Theme {
        public Color boardColor;
        public Color gridColor;
        public Color redPieceColor;
        public Color blackPieceColor;
        public Color pieceBgColor;
        public Color highlightColor;
        public Color selectedColor;
        public Color checkColor;
        public Color validMoveColor;
        public Color uiBgColor;
        public Color uiTextColor;
    }
}