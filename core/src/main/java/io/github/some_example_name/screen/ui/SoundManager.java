package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Quản lý âm thanh toàn cục cho game
 */
public class SoundManager {
    private static SoundManager instance;
    private final Preferences prefs;
    
    private static final String MUSIC_ENABLED_KEY = "music_enabled";
    private static final String SOUND_EFFECT_ENABLED_KEY = "sound_effect_enabled";
    
    private boolean musicEnabled;
    private boolean soundEffectEnabled;

    private SoundManager() {
        prefs = Gdx.app.getPreferences("game_settings");
        loadSettings();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSettings() {
        musicEnabled = prefs.getBoolean(MUSIC_ENABLED_KEY, true); // Mặc định bật
        soundEffectEnabled = prefs.getBoolean(SOUND_EFFECT_ENABLED_KEY, true); // Mặc định bật
    }

    private void saveSettings() {
        prefs.putBoolean(MUSIC_ENABLED_KEY, musicEnabled);
        prefs.putBoolean(SOUND_EFFECT_ENABLED_KEY, soundEffectEnabled);
        prefs.flush();
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSoundEffectEnabled() {
        return soundEffectEnabled;
    }

    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        saveSettings();
    }

    public void toggleSoundEffect() {
        soundEffectEnabled = !soundEffectEnabled;
        saveSettings();
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        saveSettings();
    }

    public void setSoundEffectEnabled(boolean enabled) {
        soundEffectEnabled = enabled;
        saveSettings();
    }
}
