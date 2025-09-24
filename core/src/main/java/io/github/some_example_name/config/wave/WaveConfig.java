package io.github.some_example_name.config.wave;

import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.entities.enemy.EnemyDistribution;
import io.github.some_example_name.mechanics.path.PathDistribution;
import io.github.some_example_name.mechanics.wave.Wave;

/**
 * Lớp cấu hình cho một wave
 */
public class WaveConfig {
    private float spawnInterval; // Thời gian giữa các lần spawn quái
    private PathDistribution pathDistribution; // Phân bổ quái cho các đường

    /**
     * Khởi tạo cấu hình wave mới
     * @param spawnInterval Thời gian giữa các lần spawn quái (giây)
     */
    public WaveConfig(float spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    /**
     * Thiết lập phân bổ quái cho từng đường
     * @param distributions Mảng chứa thông tin phân bổ quái cho từng đường
     */
    public void setEnemyDistributions(Array<EnemyDistribution> distributions) {
        this.pathDistribution = new PathDistribution(distributions);
    }

    /**
     * Tạo wave mới từ cấu hình này
     */
    public Wave createWave() {
        if (pathDistribution == null) {
            throw new IllegalStateException("Phải gọi setEnemyDistributions trước khi tạo wave");
        }
        Wave wave = new Wave(this);
        wave.setPathDistribution(pathDistribution);
        return wave;
    }

    /**
     * Lấy thời gian giữa các lần spawn quái
     */
    public float getSpawnInterval() {
        return spawnInterval;
    }

    /**
     * Lấy phân bổ quái cho các đường
     */
    public PathDistribution getPathDistribution() {
        return pathDistribution;
    }
}
