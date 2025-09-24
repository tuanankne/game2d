package io.github.some_example_name;

import com.badlogic.gdx.utils.Array;

public class MapConfigFactory {
    // Thông số cơ bản cho từng loại quái
    private static final float BASE_NORMAL_HEALTH = 100;  // Máu cơ bản của quái thường
    private static final float BASE_NORMAL_SPEED = 100;   // Tốc độ cơ bản của quái thường
    private static final float BASE_FAST_HEALTH = 80;     // Máu cơ bản của quái nhanh
    private static final float BASE_FAST_SPEED = 150;     // Tốc độ cơ bản của quái nhanh
    private static final float BASE_TANK_HEALTH = 200;    // Máu cơ bản của quái tank
    private static final float BASE_TANK_SPEED = 50;      // Tốc độ cơ bản của quái tank

    public static MapConfig createConfig(MapType mapType) {
        switch (mapType) {
            case MAP1:
                return createMap1Config();
            case MAP2:
                return createMap2Config();
            default:
                throw new IllegalArgumentException("Unknown map type: " + mapType);
        }
    }

    private static MapConfig createMap1Config() {
        Currency.initialize(500);
        PlayerHealth.initialize();

        MapConfig config = new MapConfig(MapType.MAP1.getMapPath(), MapType.MAP1.getPathLayerName(), 5f);

        // Wave 1: Chỉ có quái thường
        WaveConfig wave1 = new WaveConfig(1.5f);
        Array<EnemyDistribution> wave1Dist = new Array<>();
        // Đường 1: 5 quái thường
        wave1Dist.add(new EnemyDistribution(
            5, BASE_NORMAL_HEALTH, BASE_NORMAL_SPEED,     // Quái thường: 5 con, máu 100, tốc độ 100
            0, 0, 0,                                      // Không có quái nhanh
            0, 0, 0                                       // Không có quái tank
        ));
        // Đường 2: 3 quái thường
        wave1Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH, BASE_NORMAL_SPEED,     // Quái thường: 3 con, máu 100, tốc độ 100
            0, 0, 0,                                      // Không có quái nhanh
            0, 0, 0                                       // Không có quái tank
        ));
        wave1.setEnemyDistributions(wave1Dist);
        config.addWaveConfig(wave1);

        // Wave 2: Quái thường và quái nhanh
        WaveConfig wave2 = new WaveConfig(1.2f);
        Array<EnemyDistribution> wave2Dist = new Array<>();
        // Đường 1: 4 quái thường, 2 quái nhanh
        wave2Dist.add(new EnemyDistribution(
            4, BASE_NORMAL_HEALTH * 1.2f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 20%
            2, BASE_FAST_HEALTH, BASE_FAST_SPEED,                 // Quái nhanh: thông số cơ bản
            0, 0, 0                                               // Không có quái tank
        ));
        // Đường 2: 2 quái thường, 3 quái nhanh
        wave2Dist.add(new EnemyDistribution(
            2, BASE_NORMAL_HEALTH * 1.2f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 20%
            3, BASE_FAST_HEALTH, BASE_FAST_SPEED * 1.1f,         // Quái nhanh: tốc độ tăng 10%
            0, 0, 0                                               // Không có quái tank
        ));
        wave2.setEnemyDistributions(wave2Dist);
        config.addWaveConfig(wave2);

        // Wave 3: Thêm quái tank
        WaveConfig wave3 = new WaveConfig(1.0f);
        Array<EnemyDistribution> wave3Dist = new Array<>();
        // Đường 1: 3 quái thường, 2 quái nhanh, 2 quái tank
        wave3Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.4f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 40%
            2, BASE_FAST_HEALTH * 1.2f, BASE_FAST_SPEED,         // Quái nhanh: máu tăng 20%
            2, BASE_TANK_HEALTH, BASE_TANK_SPEED                  // Quái tank: thông số cơ bản
        ));
        // Đường 2: 2 quái thường, 2 quái nhanh, 1 quái tank
        wave3Dist.add(new EnemyDistribution(
            2, BASE_NORMAL_HEALTH * 1.4f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 40%
            2, BASE_FAST_HEALTH * 1.2f, BASE_FAST_SPEED * 1.1f,  // Quái nhanh: máu +20%, tốc độ +10%
            1, BASE_TANK_HEALTH * 1.1f, BASE_TANK_SPEED          // Quái tank: máu tăng 10%
        ));
        wave3.setEnemyDistributions(wave3Dist);
        config.addWaveConfig(wave3);

        // Wave 4: Đợt tấn công lớn
        WaveConfig wave4 = new WaveConfig(0.8f);
        Array<EnemyDistribution> wave4Dist = new Array<>();
        // Đường 1: 5 quái thường, 3 quái nhanh, 2 quái tank
        wave4Dist.add(new EnemyDistribution(
            5, BASE_NORMAL_HEALTH * 1.6f, BASE_NORMAL_SPEED * 1.1f,  // Quái thường: máu +60%, tốc độ +10%
            3, BASE_FAST_HEALTH * 1.4f, BASE_FAST_SPEED * 1.1f,     // Quái nhanh: máu +40%, tốc độ +10%
            2, BASE_TANK_HEALTH * 1.2f, BASE_TANK_SPEED             // Quái tank: máu tăng 20%
        ));
        // Đường 2: 3 quái thường, 3 quái nhanh, 2 quái tank
        wave4Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.6f, BASE_NORMAL_SPEED * 1.1f,  // Quái thường: máu +60%, tốc độ +10%
            3, BASE_FAST_HEALTH * 1.4f, BASE_FAST_SPEED * 1.2f,     // Quái nhanh: máu +40%, tốc độ +20%
            2, BASE_TANK_HEALTH * 1.3f, BASE_TANK_SPEED             // Quái tank: máu tăng 30%
        ));
        wave4.setEnemyDistributions(wave4Dist);
        config.addWaveConfig(wave4);

        // Wave 5: Boss wave
        WaveConfig wave5 = new WaveConfig(0.5f);
        Array<EnemyDistribution> wave5Dist = new Array<>();
        // Đường 1: 6 quái thường, 5 quái nhanh, 3 quái tank
        wave5Dist.add(new EnemyDistribution(
            6, BASE_NORMAL_HEALTH * 1.8f, BASE_NORMAL_SPEED * 1.2f,  // Quái thường: máu +80%, tốc độ +20%
            5, BASE_FAST_HEALTH * 1.6f, BASE_FAST_SPEED * 1.2f,     // Quái nhanh: máu +60%, tốc độ +20%
            3, BASE_TANK_HEALTH * 1.5f, BASE_TANK_SPEED * 1.1f      // Quái tank: máu +50%, tốc độ +10%
        ));
        // Đường 2: 4 quái thường, 3 quái nhanh, 3 quái tank
        wave5Dist.add(new EnemyDistribution(
            4, BASE_NORMAL_HEALTH * 1.8f, BASE_NORMAL_SPEED * 1.2f,  // Quái thường: máu +80%, tốc độ +20%
            3, BASE_FAST_HEALTH * 1.6f, BASE_FAST_SPEED * 1.3f,     // Quái nhanh: máu +60%, tốc độ +30%
            3, BASE_TANK_HEALTH * 1.5f, BASE_TANK_SPEED * 1.1f      // Quái tank: máu +50%, tốc độ +10%
        ));
        wave5.setEnemyDistributions(wave5Dist);
        config.addWaveConfig(wave5);

        return config;
    }

    private static MapConfig createMap2Config() {
        Currency.initialize(600);
        PlayerHealth.initialize();

        MapConfig config = new MapConfig(MapType.MAP2.getMapPath(), MapType.MAP2.getPathLayerName(), 5f);

        // Wave 1: Chỉ có quái thường
        WaveConfig wave1 = new WaveConfig(1.5f);
        Array<EnemyDistribution> wave1Dist = new Array<>();
        // Đường 1: 5 quái thường
        wave1Dist.add(new EnemyDistribution(
            5, BASE_NORMAL_HEALTH, BASE_NORMAL_SPEED,     // Quái thường: thông số cơ bản
            0, 0, 0,                                      // Không có quái nhanh
            0, 0, 0                                       // Không có quái tank
        ));
        // Đường 2: 4 quái thường
        wave1Dist.add(new EnemyDistribution(
            4, BASE_NORMAL_HEALTH, BASE_NORMAL_SPEED,     // Quái thường: thông số cơ bản
            0, 0, 0,                                      // Không có quái nhanh
            0, 0, 0                                       // Không có quái tank
        ));
        // Đường 3: 3 quái thường
        wave1Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH, BASE_NORMAL_SPEED,     // Quái thường: thông số cơ bản
            0, 0, 0,                                      // Không có quái nhanh
            0, 0, 0                                       // Không có quái tank
        ));
        wave1.setEnemyDistributions(wave1Dist);
        config.addWaveConfig(wave1);

        // Wave 2: Quái thường và quái nhanh
        WaveConfig wave2 = new WaveConfig(1.2f);
        Array<EnemyDistribution> wave2Dist = new Array<>();
        // Đường 1: 3 quái thường, 3 quái nhanh
        wave2Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.2f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 20%
            3, BASE_FAST_HEALTH, BASE_FAST_SPEED,                 // Quái nhanh: thông số cơ bản
            0, 0, 0                                               // Không có quái tank
        ));
        // Đường 2: 3 quái thường, 2 quái nhanh
        wave2Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.2f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 20%
            2, BASE_FAST_HEALTH, BASE_FAST_SPEED * 1.1f,         // Quái nhanh: tốc độ tăng 10%
            0, 0, 0                                               // Không có quái tank
        ));
        // Đường 3: 2 quái thường, 2 quái nhanh
        wave2Dist.add(new EnemyDistribution(
            2, BASE_NORMAL_HEALTH * 1.2f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 20%
            2, BASE_FAST_HEALTH * 1.1f, BASE_FAST_SPEED * 1.1f,  // Quái nhanh: máu +10%, tốc độ +10%
            0, 0, 0                                               // Không có quái tank
        ));
        wave2.setEnemyDistributions(wave2Dist);
        config.addWaveConfig(wave2);

        // Wave 3: Thêm quái tank
        WaveConfig wave3 = new WaveConfig(1.0f);
        Array<EnemyDistribution> wave3Dist = new Array<>();
        // Đường 1: 3 quái thường, 2 quái nhanh, 2 quái tank
        wave3Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.4f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 40%
            2, BASE_FAST_HEALTH * 1.2f, BASE_FAST_SPEED,         // Quái nhanh: máu tăng 20%
            2, BASE_TANK_HEALTH, BASE_TANK_SPEED                  // Quái tank: thông số cơ bản
        ));
        // Đường 2: 2 quái thường, 2 quái nhanh, 2 quái tank
        wave3Dist.add(new EnemyDistribution(
            2, BASE_NORMAL_HEALTH * 1.4f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 40%
            2, BASE_FAST_HEALTH * 1.2f, BASE_FAST_SPEED * 1.1f,  // Quái nhanh: máu +20%, tốc độ +10%
            2, BASE_TANK_HEALTH * 1.1f, BASE_TANK_SPEED          // Quái tank: máu tăng 10%
        ));
        // Đường 3: 2 quái thường, 2 quái nhanh
        wave3Dist.add(new EnemyDistribution(
            2, BASE_NORMAL_HEALTH * 1.4f, BASE_NORMAL_SPEED,      // Quái thường: máu tăng 40%
            2, BASE_FAST_HEALTH * 1.2f, BASE_FAST_SPEED * 1.2f,  // Quái nhanh: máu +20%, tốc độ +20%
            0, 0, 0                                               // Không có quái tank
        ));
        wave3.setEnemyDistributions(wave3Dist);
        config.addWaveConfig(wave3);

        // Wave 4: Đợt tấn công lớn
        WaveConfig wave4 = new WaveConfig(0.8f);
        Array<EnemyDistribution> wave4Dist = new Array<>();
        // Đường 1: 4 quái thường, 3 quái nhanh, 2 quái tank
        wave4Dist.add(new EnemyDistribution(
            4, BASE_NORMAL_HEALTH * 1.6f, BASE_NORMAL_SPEED * 1.1f,  // Quái thường: máu +60%, tốc độ +10%
            3, BASE_FAST_HEALTH * 1.4f, BASE_FAST_SPEED * 1.1f,     // Quái nhanh: máu +40%, tốc độ +10%
            2, BASE_TANK_HEALTH * 1.2f, BASE_TANK_SPEED             // Quái tank: máu tăng 20%
        ));
        // Đường 2: 3 quái thường, 3 quái nhanh, 2 quái tank
        wave4Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.6f, BASE_NORMAL_SPEED * 1.1f,  // Quái thường: máu +60%, tốc độ +10%
            3, BASE_FAST_HEALTH * 1.4f, BASE_FAST_SPEED * 1.2f,     // Quái nhanh: máu +40%, tốc độ +20%
            2, BASE_TANK_HEALTH * 1.3f, BASE_TANK_SPEED             // Quái tank: máu tăng 30%
        ));
        // Đường 3: 3 quái thường, 2 quái nhanh, 1 quái tank
        wave4Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.6f, BASE_NORMAL_SPEED * 1.1f,  // Quái thường: máu +60%, tốc độ +10%
            2, BASE_FAST_HEALTH * 1.4f, BASE_FAST_SPEED * 1.2f,     // Quái nhanh: máu +40%, tốc độ +20%
            1, BASE_TANK_HEALTH * 1.3f, BASE_TANK_SPEED             // Quái tank: máu tăng 30%
        ));
        wave4.setEnemyDistributions(wave4Dist);
        config.addWaveConfig(wave4);

        // Wave 5: Boss wave
        WaveConfig wave5 = new WaveConfig(0.5f);
        Array<EnemyDistribution> wave5Dist = new Array<>();
        // Đường 1: 5 quái thường, 4 quái nhanh, 3 quái tank
        wave5Dist.add(new EnemyDistribution(
            5, BASE_NORMAL_HEALTH * 1.8f, BASE_NORMAL_SPEED * 1.2f,  // Quái thường: máu +80%, tốc độ +20%
            4, BASE_FAST_HEALTH * 1.6f, BASE_FAST_SPEED * 1.2f,     // Quái nhanh: máu +60%, tốc độ +20%
            3, BASE_TANK_HEALTH * 1.5f, BASE_TANK_SPEED * 1.1f      // Quái tank: máu +50%, tốc độ +10%
        ));
        // Đường 2: 4 quái thường, 3 quái nhanh, 3 quái tank
        wave5Dist.add(new EnemyDistribution(
            4, BASE_NORMAL_HEALTH * 1.8f, BASE_NORMAL_SPEED * 1.2f,  // Quái thường: máu +80%, tốc độ +20%
            3, BASE_FAST_HEALTH * 1.6f, BASE_FAST_SPEED * 1.3f,     // Quái nhanh: máu +60%, tốc độ +30%
            3, BASE_TANK_HEALTH * 1.5f, BASE_TANK_SPEED * 1.1f      // Quái tank: máu +50%, tốc độ +10%
        ));
        // Đường 3: 3 quái thường, 3 quái nhanh, 2 quái tank
        wave5Dist.add(new EnemyDistribution(
            3, BASE_NORMAL_HEALTH * 1.8f, BASE_NORMAL_SPEED * 1.2f,  // Quái thường: máu +80%, tốc độ +20%
            3, BASE_FAST_HEALTH * 1.6f, BASE_FAST_SPEED * 1.3f,     // Quái nhanh: máu +60%, tốc độ +30%
            2, BASE_TANK_HEALTH * 1.5f, BASE_TANK_SPEED * 1.1f      // Quái tank: máu +50%, tốc độ +10%
        ));
        wave5.setEnemyDistributions(wave5Dist);
        config.addWaveConfig(wave5);

        return config;
    }
}