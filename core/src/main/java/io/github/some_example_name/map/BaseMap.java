package io.github.some_example_name.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.Enemy;
import io.github.some_example_name.Tower;
import io.github.some_example_name.Wave;

/**
 * Lớp abstract cung cấp các chức năng cơ bản cho một map
 */
public abstract class BaseMap implements GameMap {
    // Danh sách các điểm spawn quái
    protected Array<Vector2> spawnPoints;
    // Danh sách các đường đi cho mỗi điểm spawn
    protected Array<Array<Vector2>> paths;
    // Danh sách quái đang có trên map
    protected Array<Enemy> enemies;
    // Danh sách tháp đang có trên map
    protected Array<Tower> towers;
    // Wave hiện tại
    protected Wave currentWave;
    // Danh sách tất cả các wave
    protected Array<Wave> waves;
    // Index của wave hiện tại
    protected int currentWaveIndex;

    public BaseMap() {
        spawnPoints = new Array<>();
        paths = new Array<>();
        enemies = new Array<>();
        towers = new Array<>();
        waves = new Array<>();
        currentWaveIndex = -1;
    }

    @Override
    public void update(float delta) {
        // Cập nhật wave hiện tại
        if (currentWave != null) {
            // Kiểm tra điều kiện để sinh quái mới
            if (currentWave.shouldSpawnEnemy(delta)) {
                spawnEnemy();
            }

            // Cập nhật trạng thái các quái
            for (Enemy enemy : enemies) {
                enemy.update(delta);
                if (!enemy.isAlive()) {
                    currentWave.onEnemyKilled();
                }
            }

            // Xóa các quái đã chết
            for (int i = enemies.size - 1; i >= 0; i--) {
                if (!enemies.get(i).isAlive()) {
                    enemies.removeIndex(i);
                }
            }

            // Cập nhật trạng thái các tháp
            for (Tower tower : towers) {
                tower.update(delta, enemies);
            }

            // Kiểm tra điều kiện để chuyển wave
            if (currentWave.isComplete() && enemies.size == 0) {
                nextWave();
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // Vẽ các đối tượng trong map
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
        for (Tower tower : towers) {
            tower.render(batch);
        }
    }

    @Override
    public Array<Vector2> getSpawnPoints() {
        return spawnPoints;
    }

    @Override
    public Array<Vector2> getPath(int spawnPointIndex) {
        if (spawnPointIndex >= 0 && spawnPointIndex < paths.size) {
            return paths.get(spawnPointIndex);
        }
        return null;
    }

    @Override
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
        if (currentWave != null) {
            currentWave.addEnemy();
        }
    }

    @Override
    public Array<Enemy> getEnemies() {
        return enemies;
    }

    @Override
    public Array<Tower> getTowers() {
        return towers;
    }

    @Override
    public Wave getCurrentWave() {
        return currentWave;
    }

    @Override
    public boolean nextWave() {
        currentWaveIndex++;
        if (currentWaveIndex < waves.size) {
            currentWave = waves.get(currentWaveIndex);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        // Giải phóng tài nguyên của các đối tượng
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        for (Tower tower : towers) {
            tower.dispose();
        }
    }

    /**
     * Phương thức hỗ trợ để sinh quái mới
     */
    protected void spawnEnemy() {
        if (currentWave != null) {
            // Lấy loại quái tiếp theo từ wave
            Enemy.Type enemyType = currentWave.getNextEnemy();
            if (enemyType != null) {
                // Lấy thông số của quái
                float health = currentWave.getNextEnemyHealth();
                float speed = currentWave.getNextEnemySpeed();
                int spawnIndex = currentWave.getRandomSpawnPoint();

                // Lấy điểm spawn và đường đi
                Vector2 spawnPoint = spawnPoints.get(spawnIndex);
                Array<Vector2> path = paths.get(spawnIndex);

                // Tạo quái mới và thêm vào map
                Enemy enemy = new Enemy(spawnPoint.x, spawnPoint.y, enemyType, health, speed);
                enemy.setPath(path);
                addEnemy(enemy);
            }
        }
    }
}
