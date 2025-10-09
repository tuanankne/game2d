package io.github.some_example_name.mechanics.wave;

import com.badlogic.gdx.Gdx;
import io.github.some_example_name.config.wave.WaveConfig;
import io.github.some_example_name.mechanics.path.PathDistribution;

/**
 * Lớp quản lý một đợt tấn công của quái (wave)
 */
public class Wave {
    private float spawnInterval; // Thời gian giữa các lần spawn quái
    private float timeSinceLastSpawn; // Thời gian đã trôi qua từ lần spawn trước
    private boolean isComplete; // Trạng thái hoàn thành của wave
    private int enemiesAlive; // Số lượng quái còn sống
    private int enemiesKilled; // Số lượng quái đã bị tiêu diệt
    private PathDistribution pathDistribution; // Phân bổ quái cho các đường

    /**
     * Khởi tạo wave mới
     */
    public Wave(WaveConfig config) {
        this.spawnInterval = config.getSpawnInterval();
        this.timeSinceLastSpawn = 0;
        this.isComplete = false;
        this.enemiesAlive = 0;
        this.enemiesKilled = 0;
    }

    /**
     * Thiết lập phân bổ quái cho các đường
     */
    public void setPathDistribution(PathDistribution distribution) {
        this.pathDistribution = distribution;
    }

    /**
     * Kiểm tra xem có nên spawn quái mới không
     */
    public boolean shouldSpawnEnemy(float delta) {
        if (isComplete) {
            return false;
        }

        timeSinceLastSpawn += delta;
        if (timeSinceLastSpawn >= spawnInterval) {
            timeSinceLastSpawn = 0;
            return true;
        }
        return false;
    }

    /**
     * Lấy thông tin quái tiếp theo cần spawn
     * @return Mảng gồm [chỉ số đường, loại quái, máu, tốc độ], hoặc null nếu đã spawn đủ
     */
    public Object[] getNextEnemy() {
        if (pathDistribution == null) {
            return null;
        }

        Object[] nextSpawn = pathDistribution.getNextSpawn();
        if (nextSpawn != null) {
            enemiesAlive++;
        } else {
            // Không còn enemies để spawn, đánh dấu wave đã hoàn thành việc spawn
            isComplete = true;
        }
        return nextSpawn;
    }

    /**
     * Xử lý khi một quái chết
     */
    public void onEnemyKilled() {
        enemiesAlive--;
        enemiesKilled++;
        
        if (isComplete && enemiesAlive == 0) {
            Gdx.app.log("Wave", "Wave hoàn thành! Tất cả quái đã bị tiêu diệt.");
        }
    }

    /**
     * Xử lý khi một quái đến đích (không bị tiêu diệt)
     */
    public void onEnemyReachedEnd() {
        enemiesAlive--;
        
        if (isComplete && enemiesAlive == 0) {
            Gdx.app.log("Wave", "Wave hoàn thành! Tất cả quái đã đến đích hoặc bị tiêu diệt.");
        }
    }

    // Các phương thức getter
    public boolean isComplete() { return isComplete && enemiesAlive == 0; }
    public int getEnemiesAlive() { return enemiesAlive; }
    public int getEnemiesKilled() { return enemiesKilled; }
    public int getTotalEnemies() { 
        if (pathDistribution == null) return 0;
        return pathDistribution.getTotalEnemyCount(); 
    }
    public PathDistribution getPathDistribution() { return pathDistribution; }
}
