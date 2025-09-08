package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class Wave {
    private Array<EnemyType> enemies;
    private float spawnInterval; // Thời gian giữa mỗi lần spawn quái
    private float timeSinceLastSpawn;
    private int enemiesSpawned;
    private boolean isComplete;

    public Wave(Array<EnemyType> enemies, float spawnInterval) {
        this.enemies = enemies;
        this.spawnInterval = spawnInterval;
        this.timeSinceLastSpawn = 0;
        this.enemiesSpawned = 0;
        this.isComplete = false;
    }

    public boolean shouldSpawnEnemy(float delta) {
        if (isComplete) {
            Gdx.app.log("Wave", "Wave is complete, no more spawning");
            return false;
        }

        timeSinceLastSpawn += delta;
        if (timeSinceLastSpawn >= spawnInterval && enemiesSpawned < enemies.size) {
            timeSinceLastSpawn = 0;
            Gdx.app.log("Wave", String.format("Should spawn enemy: %d/%d spawned",
                enemiesSpawned + 1, enemies.size));
            return true;
        }
        return false;
    }

    public EnemyType getNextEnemy() {
        if (enemiesSpawned < enemies.size) {
            EnemyType type = enemies.get(enemiesSpawned);
            enemiesSpawned++;
            if (enemiesSpawned == enemies.size) {
                isComplete = true;
            }
            return type;
        }
        return null;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public int getTotalEnemies() {
        return enemies.size;
    }
}
