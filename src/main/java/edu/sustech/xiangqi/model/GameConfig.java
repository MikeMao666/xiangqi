package edu.sustech.xiangqi.model;

public class GameConfig {
    public TimeControl getTimeControl() {
        return timeControl;
    }

    public enum Mode {
        NORMAL,     // 普通模式
        TIMED,      // 计时赛
        AI,         // 人机 (开发中)
        ONLINE      // 联网 (开发中)
    }

    public enum TimeControl {
        SUDDEN_DEATH, // 包干制 (如10分钟包干)
        INCREMENT     // 加秒制 (如10分钟+5秒)
    }

    private Mode mode;
    private int initialTimeSeconds; // 初始时间
    private int incrementSeconds;   // 每步加秒
    private TimeControl timeControl;

    // 普通模式
    public GameConfig() {
        this.mode = Mode.NORMAL;
    }

    // 计时模式
    public GameConfig(int initialTimeMinutes, int incrementSeconds) {
        this.mode = Mode.TIMED;
        this.initialTimeSeconds = initialTimeMinutes * 60;
        this.incrementSeconds = incrementSeconds;
        this.timeControl = (incrementSeconds > 0) ? TimeControl.INCREMENT : TimeControl.SUDDEN_DEATH;
    }

    public Mode getMode() { return mode; }
    public int getInitialTimeSeconds() { return initialTimeSeconds; }
    public int getIncrementSeconds() { return incrementSeconds; }
}