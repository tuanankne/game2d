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
    private Array<EnemyType> enemies;        // Danh sách các loại quái trong wave
    private float spawnInterval;             // Thời gian giữa mỗi lần sinh quái

    // Constructor khởi tạo cấu hình wave
    public WaveConfig(float spawnInterval) {
        this.spawnInterval = spawnInterval;  // Lưu thời gian sinh quái
        this.enemies = new Array<>();        // Khởi tạo danh sách quái
    }

    // Thêm một số lượng quái cùng loại vào wave
    public void addEnemy(EnemyType type, int count) {
        // Thêm quái vào danh sách theo số lượng chỉ định
        for (int i = 0; i < count; i++) {
            enemies.add(type);
        }
    }

    // Tạo một wave mới từ cấu hình
    public Wave createWave() {
        return new Wave(enemies, spawnInterval);  // Tạo wave với danh sách quái và thời gian sinh
    }
}
