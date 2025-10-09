package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import io.github.some_example_name.screen.ui.SoundManager;

public class GameSoundManager {
    // Sound effects
    private static Sound oneDanSound;
    private static Sound oneVienSound;
    private static Sound lienThanhSound;
    private static Sound buildSound;
    private static Sound chetSound;
    private static Sound startWaveSound;
    private static Sound congratulationSound;
    private static Sound snowSound;
    private static Sound setSound;
    private static Sound healSound;

    // Background music
    private static Music nhacNenMusic;

    // Settings
    private static float soundVolume = 0.5f;
    private static float musicVolume = 0.3f;

    public static void initialize() {
        try {
            // Load sound effects
            oneDanSound = Gdx.audio.newSound(Gdx.files.internal("sound effect/1dan.mp3"));
            oneVienSound = Gdx.audio.newSound(Gdx.files.internal("sound effect/1vien.mp3"));
            lienThanhSound = Gdx.audio.newSound(Gdx.files.internal("sound effect/lienthanh.mp3"));
            buildSound = Gdx.audio.newSound(Gdx.files.internal("sound effect/build.mp3"));
            chetSound = Gdx.audio.newSound(Gdx.files.internal("sound effect/chet.mp3"));
            startWaveSound = Gdx.audio.newSound(Gdx.files.internal("sound effect/startwave.mp3"));
            congratulationSound = Gdx.audio.newSound(Gdx.files.internal("Music/congratulation.wav"));
            snowSound = Gdx.audio.newSound(Gdx.files.internal("item/snow.mp3"));
            setSound = Gdx.audio.newSound(Gdx.files.internal("item/set.mp3"));
            healSound = Gdx.audio.newSound(Gdx.files.internal("item/hoimau.mp3"));

            // Load background music
            nhacNenMusic = Gdx.audio.newMusic(Gdx.files.internal("sound effect/nhac.mp3"));
            nhacNenMusic.setLooping(true);
            nhacNenMusic.setVolume(musicVolume);

            Gdx.app.log("GameSoundManager", "All sounds loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error loading sounds: " + e.getMessage());
        }
    }

    // Play tower shooting sounds
    public static void playTowerShootSound(String towerType) {
        if (!SoundManager.getInstance().isSoundEffectEnabled()) return;

        try {
            switch (towerType) {
                case "STONE_TOWER":
                case "LAND_TOWER":
                    if (oneDanSound != null) {
                        oneDanSound.play(soundVolume);
                    }
                    break;
                case "FIRE_TOWER":
                    if (oneVienSound != null) {
                        oneVienSound.play(soundVolume);
                    }
                    break;
                case "BIGLAND_TOWER":
                    if (lienThanhSound != null) {
                        lienThanhSound.play(soundVolume);
                    }
                    break;
            }
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing tower shoot sound: " + e.getMessage());
        }
    }

    // Play build sound
    public static void playBuildSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || buildSound == null) return;
        try {
            buildSound.play(soundVolume);
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing build sound: " + e.getMessage());
        }
    }

    // Play enemy death sound
    public static void playEnemyDeathSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || chetSound == null) return;
        try {
            chetSound.play(soundVolume * 0.6f); // Nhỏ hơn để không quá ồn
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing death sound: " + e.getMessage());
        }
    }

    // Play wave start sound
    public static void playWaveStartSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || startWaveSound == null) return;
        try {
            startWaveSound.play(soundVolume);
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing wave start sound: " + e.getMessage());
        }
    }
    
    // Play congratulation sound
    public static void playCongratulationSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || congratulationSound == null) return;
        try {
            congratulationSound.play(soundVolume);
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing congratulation sound: " + e.getMessage());
        }
    }
    
    // Play snow sound
    public static void playSnowSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || snowSound == null) return;
        try {
            snowSound.play(soundVolume);
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing snow sound: " + e.getMessage());
        }
    }
    
    // Play lightning/set sound
    public static void setSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || setSound == null) return;
        try {
            setSound.play(soundVolume);
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing lightning sound: " + e.getMessage());
        }
    }
    
    // Play heal sound
    public static void playHealSound() {
        if (!SoundManager.getInstance().isSoundEffectEnabled() || healSound == null) return;
        try {
            healSound.play(soundVolume);
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing heal sound: " + e.getMessage());
        }
    }

    // Background music controls
    public static void playBackgroundMusic() {
        if (!SoundManager.getInstance().isMusicEnabled() || nhacNenMusic == null) return;
        try {
            if (!nhacNenMusic.isPlaying()) {
                nhacNenMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.error("GameSoundManager", "Error playing background music: " + e.getMessage());
        }
    }

    public static void pauseBackgroundMusic() {
        if (nhacNenMusic != null && nhacNenMusic.isPlaying()) {
            nhacNenMusic.pause();
        }
    }

    public static void stopBackgroundMusic() {
        if (nhacNenMusic != null) {
            nhacNenMusic.stop();
        }
    }

    public static void resumeBackgroundMusic() {
        if (SoundManager.getInstance().isMusicEnabled() && nhacNenMusic != null && !nhacNenMusic.isPlaying()) {
            nhacNenMusic.play();
        }
    }

    // Volume controls
    public static void setSoundVolume(float volume) {
        soundVolume = Math.max(0f, Math.min(1f, volume));
    }

    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(1f, volume));
        if (nhacNenMusic != null) {
            nhacNenMusic.setVolume(musicVolume);
        }
    }

    // Dispose
    public static void dispose() {
        if (oneDanSound != null) oneDanSound.dispose();
        if (oneVienSound != null) oneVienSound.dispose();
        if (lienThanhSound != null) lienThanhSound.dispose();
        if (buildSound != null) buildSound.dispose();
        if (chetSound != null) chetSound.dispose();
        if (startWaveSound != null) startWaveSound.dispose();
        if (congratulationSound != null) congratulationSound.dispose();
        if (snowSound != null) snowSound.dispose();
        if (setSound != null) setSound.dispose();
        if (healSound != null) healSound.dispose();
        if (nhacNenMusic != null) nhacNenMusic.dispose();
    }
}

