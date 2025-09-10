package io.github.some_example_name;

// Import các thư viện cần thiết
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

// Lớp quản lý một đợt tấn công của quái (wave)
public class Wave {
    // Danh sách các loại quái trong wave
    private Array<EnemyType> enemies;
    // Thời gian giữa mỗi lần sinh quái (giây)
    private float spawnInterval;
    // Thời gian đã trôi qua kể từ lần sinh quái cuối
    private float timeSinceLastSpawn;
    // Số lượng quái đã được sinh ra
    private int enemiesSpawned;
    // Trạng thái hoàn thành của wave
    private boolean isComplete;

    // Constructor khởi tạo wave với danh sách quái và thời gian sinh
    public Wave(Array<EnemyType> enemies, float spawnInterval) {
        this.enemies = enemies;              // Danh sách quái
        this.spawnInterval = spawnInterval;  // Thời gian giữa các lần sinh
        this.timeSinceLastSpawn = 0;        // Reset thời gian
        this.enemiesSpawned = 0;            // Chưa sinh quái nào
        this.isComplete = false;            // Wave chưa hoàn thành
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
        if (timeSinceLastSpawn >= spawnInterval && enemiesSpawned < enemies.size) {
            timeSinceLastSpawn = 0;  // Reset thời gian đếm
            Gdx.app.log("Wave", String.format("Should spawn enemy: %d/%d spawned",
                enemiesSpawned + 1, enemies.size));
            return true;
        }
        return false;
    }

    // Lấy loại quái tiếp theo cần sinh
    public EnemyType getNextEnemy() {
        // Kiểm tra còn quái để sinh không
        if (enemiesSpawned < enemies.size) {
            // Lấy loại quái tiếp theo
            EnemyType type = enemies.get(enemiesSpawned);
            enemiesSpawned++;
            // Nếu đã sinh hết quái, đánh dấu wave hoàn thành
            if (enemiesSpawned == enemies.size) {
                isComplete = true;
            }
            return type;
        }
        return null;  // Không còn quái để sinh
    }

    // Kiểm tra wave đã hoàn thành chưa
    public boolean isComplete() {
        return isComplete;
    }

    // Lấy tổng số quái trong wave
    public int getTotalEnemies() {
        return enemies.size;
    }
}
