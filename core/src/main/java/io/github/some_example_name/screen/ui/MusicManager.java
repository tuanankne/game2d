package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Quản lý nhạc nền toàn cục cho game
 */
public class MusicManager {
    private static MusicManager instance;
    private Music menuMusic;
    private final SoundManager soundManager;

    private MusicManager() {
        soundManager = SoundManager.getInstance();
        loadMusic();
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    private void loadMusic() {
        if (Gdx.files.internal("Music/Menu.mp3").exists()) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Menu.mp3"));
            menuMusic.setLooping(true);
            updateVolume();
        }
    }

    public void playMusic() {
        if (menuMusic != null) {
            if (soundManager.isMusicEnabled()) {
                menuMusic.setVolume(0.5f);
                if (!menuMusic.isPlaying()) {
                    menuMusic.play();
                    Gdx.app.log("MusicManager", "Music started playing");
                }
            } else {
                menuMusic.setVolume(0f);
                menuMusic.pause();
                Gdx.app.log("MusicManager", "Music paused - disabled in settings");
            }
        } else {
            Gdx.app.log("MusicManager", "Menu music is null - file not found");
        }
    }

    public void pauseMusic() {
        if (menuMusic != null) {
            menuMusic.pause();
        }
    }

    public void stopMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
        }
    }

    public void updateVolume() {
        if (menuMusic != null) {
            if (soundManager.isMusicEnabled()) {
                menuMusic.setVolume(0.5f);
                if (!menuMusic.isPlaying()) {
                    menuMusic.play();
                    Gdx.app.log("MusicManager", "Music resumed after enabling");
                }
            } else {
                menuMusic.setVolume(0f);
                menuMusic.pause();
                Gdx.app.log("MusicManager", "Music paused after disabling");
            }
        }
    }

    public void dispose() {
        if (menuMusic != null) {
            menuMusic.dispose();
            menuMusic = null;
        }
    }
}
