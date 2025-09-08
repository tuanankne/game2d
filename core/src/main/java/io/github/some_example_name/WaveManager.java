package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;

public class WaveManager {
    private Array<Wave> waves;
    private int currentWaveIndex;
    private float timeBetweenWaves;
    private float waveTimer;
    private BitmapFont font;
    private boolean showingWaveMessage;
    private float messageTimer;
    private static final float MESSAGE_DURATION = 2f; // Thời gian hiển thị thông báo

    public WaveManager(float timeBetweenWaves) {
        this.waves = new Array<>();
        this.currentWaveIndex = 0;
        this.timeBetweenWaves = timeBetweenWaves;
        this.waveTimer = timeBetweenWaves;
        this.font = new BitmapFont();
        font.getData().setScale(2); // Tăng kích thước font
        showingWaveMessage = false;
        messageTimer = 0;
    }

    public void addWave(Wave wave) {
        waves.add(wave);
        Gdx.app.log("WaveManager", "Added wave " + waves.size + " with " + wave.getTotalEnemies() + " enemies");
    }

    public void update(float delta) {
        if (showingWaveMessage) {
            messageTimer -= delta;
            if (messageTimer <= 0) {
                showingWaveMessage = false;
            }
        } else if (waveTimer > 0) {
            waveTimer -= delta;
            if (waveTimer <= 0 && currentWaveIndex < waves.size) {
                showWaveMessage();
                Gdx.app.log("WaveManager", "Wave timer expired, starting wave " + (currentWaveIndex + 1));
            }
        }
        
        // Debug log
        if (currentWaveIndex < waves.size) {
            Wave currentWave = waves.get(currentWaveIndex);
            if (currentWave.isComplete()) {
                Gdx.app.log("WaveManager", "Current wave status: Complete, Timer: " + waveTimer);
            }
        }
    }

    private void showWaveMessage() {
        showingWaveMessage = true;
        messageTimer = MESSAGE_DURATION;
        Gdx.app.log("WaveManager", "Starting Wave " + (currentWaveIndex + 1));
    }

    public void render(SpriteBatch batch) {
        if (showingWaveMessage && currentWaveIndex < waves.size) {
            String message = "Wave " + (currentWaveIndex + 1);
            float messageWidth = font.draw(batch, message, 0, 0).width;
            float x = (Gdx.graphics.getWidth() - messageWidth) / 2;
            float y = Gdx.graphics.getHeight() / 2;
            font.draw(batch, message, x, y);
        }
    }

    public Wave getCurrentWave() {
        return currentWaveIndex < waves.size ? waves.get(currentWaveIndex) : null;
    }

    public boolean shouldSpawnEnemy(float delta) {
        Wave currentWave = getCurrentWave();
        return currentWave != null && !showingWaveMessage && waveTimer <= 0 && currentWave.shouldSpawnEnemy(delta);
    }

    public void waveCompleted() {
        Gdx.app.log("WaveManager", "Wave " + (currentWaveIndex + 1) + " completed");
        currentWaveIndex++;
        if (currentWaveIndex < waves.size) {
            waveTimer = timeBetweenWaves;
            Gdx.app.log("WaveManager", "Next wave starts in " + timeBetweenWaves + " seconds");
        } else {
            Gdx.app.log("WaveManager", "All waves completed!");
        }
    }

    public boolean isFinished() {
        return currentWaveIndex >= waves.size;
    }

    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }
}
