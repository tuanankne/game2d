package io.github.some_example_name;

import com.badlogic.gdx.utils.Array;

/**
 * Lớp cấu hình cho một map
 */
public class MapConfig {
    private String mapPath;
    private String pathLayerName;
    private float timeBetweenWaves;
    private final Array<WaveConfig> waveConfigs;

    public MapConfig(String mapPath, String pathLayerName, float timeBetweenWaves) {
        this.mapPath = mapPath;
        this.pathLayerName = pathLayerName;
        this.timeBetweenWaves = timeBetweenWaves;
        this.waveConfigs = new Array<>();
    }

    public void addWaveConfig(WaveConfig waveConfig) {
        waveConfigs.add(waveConfig);
    }

    public String getMapPath() {
        return mapPath;
    }

    public String getPathLayerName() {
        return pathLayerName;
    }

    public float getTimeBetweenWaves() {
        return timeBetweenWaves;
    }

    public Array<WaveConfig> getWaveConfigs() {
        return waveConfigs;
    }
}
