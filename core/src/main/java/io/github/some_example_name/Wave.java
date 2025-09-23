package io.github.some_example_name;

// Import các thư viện cần thiết
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

// Lớp quản lý một đợt tấn công của quái (wave)
public class Wave {
    private WaveConfig config;
    private Array<Enemy.Type> enemies; // Danh sách các loại quái
    private Array<Float> healths; // Máu tương ứng cho từng quái
    private Array<Float> speeds; // Tốc độ tương ứng cho từng quái
    private float spawnInterval;
    private float timeSinceLastSpawn;
    private int enemiesSpawned;
    private boolean isComplete;
    private int enemiesAlive;
    private int enemiesKilled;
    private int totalEnemies;

    public Wave(WaveConfig config) {
        this.config = config;
        this.spawnInterval = config.getSpawnInterval();
        this.timeSinceLastSpawn = 0;
        this.enemiesSpawned = 0;
        this.isComplete = false;
        this.enemiesAlive = 0;
        this.enemiesKilled = 0;

        this.enemies = config.getEnemies();
        this.healths = config.getHealths();
        this.speeds = config.getSpeeds();
        this.totalEnemies = enemies.size;
    }

    // Kiểm tra xem có nên sinh quái mới không
    public boolean shouldSpawnEnemy(float delta) {
        // Nếu wave đã hoàn thành, không sinh thêm quái
        if (isComplete) {
            return false;
        }

        // Cộng dồn thời gian đã trôi qua
        timeSinceLastSpawn += delta;
        // Kiểm tra điều kiện sinh quái: đủ thời gian và còn quái để sinh
        if (timeSinceLastSpawn >= spawnInterval && enemiesSpawned < totalEnemies) {
            timeSinceLastSpawn = 0;  // Reset thời gian đếm
            Gdx.app.log("Wave", String.format("Should spawn enemy: %d/%d spawned",
                enemiesSpawned + 1, totalEnemies));
            return true;
        }
        return false;
    }

    // Lấy thông tin quái tiếp theo cần sinh
    public Enemy.Type getNextEnemy() {
        if (enemiesSpawned < totalEnemies) {
            Enemy.Type type = enemies.get(enemiesSpawned);
            enemiesSpawned++;
            if (enemiesSpawned == totalEnemies) {
                isComplete = true;
            }
            return type;
        }
        return null;
    }

    // Lấy máu của quái tiếp theo
    public float getNextEnemyHealth() {
        if (enemiesSpawned > 0 && enemiesSpawned <= totalEnemies) {
            return healths.get(enemiesSpawned - 1);
        }
        return 0;
    }

    // Lấy tốc độ của quái tiếp theo
    public float getNextEnemySpeed() {
        if (enemiesSpawned > 0 && enemiesSpawned <= totalEnemies) {
            return speeds.get(enemiesSpawned - 1);
        }
        return 0;
    }

    // Lấy điểm spawn ngẫu nhiên cho quái tiếp theo
    public int getRandomSpawnPoint() {
        Array<Integer> spawnPoints = config.getSpawnPoints();
        if (spawnPoints.size > 0) {
            int randomIndex = (int)(Math.random() * spawnPoints.size);
            return spawnPoints.get(randomIndex);
        }
        return 0;
    }

    // Kiểm tra wave đã hoàn thành chưa
    public boolean isComplete() {
        return isComplete;
    }

    // Lấy tổng số quái trong wave
    public int getTotalEnemies() {
        return enemies.size;
    }

    // Thêm quái mới vào wave
    public void addEnemy() {
        enemiesAlive++;
        // Gdx.app.log("Wave", String.format("Quái trên màn hình: %d (Còn sống: %d, Đã chết: %d, Tổng: %d)",
        //     enemiesAlive, enemiesAlive, enemiesKilled, totalEnemies));
    }

    // Xử lý khi một quái chết
    public void onEnemyKilled() {
        enemiesAlive--;
        enemiesKilled++;
        
        // Kiểm tra điều kiện hoàn thành wave
        if (enemiesKilled == enemies.size) {
            isComplete = true;
            Gdx.app.log("Wave", "Wave hoàn thành! Tất cả quái đã bị tiêu diệt.");
        }

        // Gdx.app.log("Wave", String.format("Quái trên màn hình: %d (Còn sống: %d, Đã chết: %d, Tổng: %d)",
        //     enemiesAlive, enemiesAlive, enemiesKilled, totalEnemies));
    }

    // Lấy số lượng quái còn sống
    public int getEnemiesAlive() {
        return enemiesAlive;
    }

    // Lấy số lượng quái đã chết
    public int getEnemiesKilled() {
        return enemiesKilled;
    }
}
