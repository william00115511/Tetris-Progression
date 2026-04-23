package tetris;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Phase 12 / 12.5: 獨立音效引擎 (非同步、自動格式轉換、BGM 迴圈、SFX 重疊播放)
 * 
 * 設計原則：
 * - 所有播放操作均在獨立 Thread 執行，絕不阻塞遊戲主迴圈。
 * - SFX 每次呼叫都嘗試開新 Clip，天然支援重疊播放 (Polyphony)。
 * - BGM 使用單一 Clip 搭配 LOOP_CONTINUOUSLY。
 * - 自動將 24-bit 等不支援格式轉換為 16-bit PCM_SIGNED。
 * - 若音訊檔案不存在，僅在 Console 印出事件名稱，程式不崩潰。
 */
public class SoundManager {

    private static final String SOUND_DIR = "sounds/";
    private Clip bgmClip;
    private int bgmVolume = 80;
    private int sfxVolume = 80;
    private long bgmPausePosition = 0;

    // ==========================================
    // 音量控制 (Volume Control)
    // ==========================================

    public void setBgmVolume(int volume) {
        this.bgmVolume = volume;
        if (bgmClip != null) {
            applyVolume(bgmClip, volume);
        }
    }

    public void setSfxVolume(int volume) {
        this.sfxVolume = volume;
    }

    private void applyVolume(Clip clip, int volume) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (volume == 0) {
                gainControl.setValue(-80.0f); // 靜音
            } else {
                // dB 轉換公式
                float db = 20f * (float) Math.log10(volume / 100f);
                gainControl.setValue(db);
            }
        } catch (IllegalArgumentException e) {
            // 例外防護：某些作業系統環境如果抓不到 Master Gain，就忽略設定
        }
    }

    // ==========================================
    // 核心：安全取得 AudioInputStream（自動降轉 24-bit → 16-bit）
    // ==========================================

    private AudioInputStream getSafeStream(File file) throws Exception {
        AudioInputStream originalStream = AudioSystem.getAudioInputStream(file);
        AudioFormat originalFormat = originalStream.getFormat();

        // 如果是 16-bit 或 8-bit，直接回傳
        if (originalFormat.getSampleSizeInBits() <= 16) {
            return originalStream;
        }

        // 24-bit / 32-bit → 強制轉換為 16-bit PCM_SIGNED
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            originalFormat.getSampleRate(),
            16,                                 // 目標 16-bit
            originalFormat.getChannels(),
            originalFormat.getChannels() * 2,   // 每 frame = channels × 2 bytes
            originalFormat.getSampleRate(),
            false                               // little-endian
        );

        return AudioSystem.getAudioInputStream(targetFormat, originalStream);
    }

    // ==========================================
    // BGM (背景音樂 - 單軌迴圈)
    // ==========================================

    public void playBGM(String filename) {
        // Stage 3: Restore Audio
        stopBGM();
        new Thread(() -> {
            try {
                File file = new File(SOUND_DIR + filename);
                if (!file.exists()) {
                    System.out.println("Play BGM: " + filename + " (file not found)");
                    return;
                }
                AudioInputStream ais = getSafeStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                applyVolume(clip, bgmVolume); // 套用音量
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                bgmClip = clip; // 成功後才賦值，避免競態問題
                bgmPausePosition = 0;
            } catch (Exception e) {
                System.out.println("Play BGM: " + filename + " (error: " + e.getMessage() + ")");
            }
        }, "BGM-Thread").start();
    }

    public void stopBGM() {
        // Stage 3: Restore Audio
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.close();
            bgmClip = null;
        }
    }

    public void pauseBGM() {
        // Stage 3: Restore Audio
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmPausePosition = bgmClip.getMicrosecondPosition();
            bgmClip.stop();
        }
    }

    public void resumeBGM() {
        // Stage 3: Restore Audio
        if (bgmClip != null && !bgmClip.isRunning()) {
            bgmClip.setMicrosecondPosition(bgmPausePosition);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        }
    }

    // ==========================================
    // SFX (音效 - 支援多重重疊播放)
    // ==========================================

    public void playSFX(String eventName) {
        // Stage 3: Restore Audio
        new Thread(() -> {
            try {
                String filename = eventName.toLowerCase() + ".wav";
                File file = new File(SOUND_DIR + filename);
                if (!file.exists()) {
                    System.out.println("Play Sound: " + eventName);
                    return;
                }
                AudioInputStream ais = getSafeStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                applyVolume(clip, sfxVolume); // 套用音量
                // 播放完畢後自動釋放資源
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception e) {
                System.out.println("Play Sound: " + eventName + " (error: " + e.getMessage() + ")");
            }
        }, "SFX-" + eventName).start();
    }
}
