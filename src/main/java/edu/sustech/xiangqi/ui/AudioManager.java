package edu.sustech.xiangqi.ui;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private Clip backgroundMusic;
    private float volume = 0.5f;
    private boolean isMuted = false;
    private Map<String, Clip> soundEffects;

    private AudioManager() {
        soundEffects = new HashMap<>();
        loadSoundEffects();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void playBackgroundMusic(String musicFile) {
        if (isMuted) return;

        stopBackgroundMusic();

        try {
            InputStream audioSrc = getClass().getResourceAsStream(musicFile);
            if (audioSrc == null) {
                audioSrc = new FileInputStream("resources/music/" + musicFile);
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(audioSrc));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);

            // 设置音量
            FloatControl gainControl =
                    (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(volume));

            // 循环播放
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();

        } catch (Exception e) {
            System.err.println("无法播放背景音乐: " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));

        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            FloatControl gainControl =
                    (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(this.volume));
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            stopBackgroundMusic();
        } else if (volume > 0) {
            // 重新播放音乐（需要重新选择音乐）
        }
    }

    private void loadSoundEffects() {
        // 加载音效文件
        String[] effects = {"move.wav", "capture.wav", "check.wav", "win.wav"};

        for (String effect : effects) {
            try {
                InputStream audioSrc = getClass().getResourceAsStream("/music/" + effect);
                if (audioSrc == null) {
                    audioSrc = new FileInputStream("resources/music/" + effect);
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(audioSrc));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundEffects.put(effect, clip);
            } catch (Exception e) {
                System.err.println("无法加载音效: " + effect);
            }
        }
    }

    public void playSoundEffect(String effectName) {
        if (isMuted || !soundEffects.containsKey(effectName)) return;

        Clip clip = soundEffects.get(effectName);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }
}