package io.github.some_example_name;

// Import thư viện Array từ libGDX
import com.badlogic.gdx.utils.Array;

// Lớp chứa cấu hình cho bản đồ và các wave
public class MapConfig {
    private String mapPath;                  // Đường dẫn đến file bản đồ
    private String pathLayerName;            // Tên layer chứa đường đi của quái
    private Array<WaveConfig> waveConfigs;   // Danh sách cấu hình các wave
    private float timeBetweenWaves;          // Thời gian giữa các wave

    // Constructor khởi tạo cấu hình bản đồ
    public MapConfig(String mapPath, String pathLayerName, float timeBetweenWaves) {
        this.mapPath = mapPath;                // Lưu đường dẫn bản đồ
        this.pathLayerName = pathLayerName;    // Lưu tên layer đường đi
        this.timeBetweenWaves = timeBetweenWaves;  // Lưu thời gian giữa các wave
        this.waveConfigs = new Array<>();      // Khởi tạo danh sách wave
    }

    // Thêm cấu hình wave mới vào danh sách
    public void addWaveConfig(WaveConfig config) {
        waveConfigs.add(config);
    }

    // Các phương thức getter
    public String getMapPath() { return mapPath; }               // Lấy đường dẫn bản đồ
    public String getPathLayerName() { return pathLayerName; }   // Lấy tên layer đường đi
    public Array<WaveConfig> getWaveConfigs() { return waveConfigs; }  // Lấy danh sách wave
    public float getTimeBetweenWaves() { return timeBetweenWaves; }   // Lấy thời gian giữa các wave
}

// Lớp chứa cấu hình cho một wave cụ thể
class WaveConfig {
    private Array<Enemy.Type> enemies;        // Danh sách các loại quái trong wave
    private float spawnInterval;             // Thời gian giữa mỗi lần sinh quái
    private Array<Float> healths;           // Máu của từng loại quái
    private Array<Float> speeds;            // Tốc độ của từng loại quái
    private Array<Integer> spawnPoints;     // Các điểm spawn có thể sử dụng

    public WaveConfig(float spawnInterval) {
        this.spawnInterval = spawnInterval;
        this.enemies = new Array<>();
        this.healths = new Array<>();
        this.speeds = new Array<>();
        this.spawnPoints = new Array<>();
    }

    public void addEnemy(Enemy.Type type, int count, float health, float speed) {
        for (int i = 0; i < count; i++) {
            enemies.add(type);
            healths.add(health);
            speeds.add(speed);
        }
    }

    public void addSpawnPoint(int index) {
        spawnPoints.add(index);
    }

    public float getSpawnInterval() {
        return spawnInterval;
    }

    public Array<Enemy.Type> getEnemies() {
        return enemies;
    }

    public Array<Float> getHealths() {
        return healths;
    }

    public Array<Float> getSpeeds() {
        return speeds;
    }

    public Array<Integer> getSpawnPoints() {
        return spawnPoints;
    }

    public Wave createWave() {
        return new Wave(this);  // Tạo wave với cấu hình hiện tại
    }
}

