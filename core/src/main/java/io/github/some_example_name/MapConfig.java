package io.github.some_example_name;

import com.badlogic.gdx.utils.Array;

public class MapConfig {
    private String mapPath;
    private String pathLayerName;
    private Array<WaveConfig> waveConfigs;
    private float timeBetweenWaves;

    public MapConfig(String mapPath, String pathLayerName, float timeBetweenWaves) {
        this.mapPath = mapPath;
        this.pathLayerName = pathLayerName;
        this.timeBetweenWaves = timeBetweenWaves;
        this.waveConfigs = new Array<>();
    }

    public void addWaveConfig(WaveConfig config) {
        waveConfigs.add(config);
    }

    public String getMapPath() { return mapPath; }
    public String getPathLayerName() { return pathLayerName; }
    public Array<WaveConfig> getWaveConfigs() { return waveConfigs; }
    public float getTimeBetweenWaves() { return timeBetweenWaves; }
}

class WaveConfig {
    private Array<EnemyType> enemies;
    private float spawnInterval;

    public WaveConfig(float spawnInterval) {
        this.spawnInterval = spawnInterval;
        this.enemies = new Array<>();
    }

    public void addEnemy(EnemyType type, int count) {
        for (int i = 0; i < count; i++) {
            enemies.add(type);
        }
    }

    public Wave createWave() {
        return new Wave(enemies, spawnInterval);
    }
}
