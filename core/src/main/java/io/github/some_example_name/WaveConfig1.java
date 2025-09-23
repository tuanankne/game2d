//package io.github.some_example_name;
//
//import com.badlogic.gdx.utils.Array;
//
///**
// * Lớp cấu hình cho một wave
// */
//public class WaveConfig {
//    // Loại quái và số lượng tương ứng
//    public static class EnemyGroup {
//        public Enemy.Type type;
//        public int count;
//        public float health;
//        public float speed;
//
//        public EnemyGroup(Enemy.Type type, int count, float health, float speed) {
//            this.type = type;
//            this.count = count;
//            this.health = health;
//            this.speed = speed;
//        }
//    }
//
//    private Array<EnemyGroup> enemyGroups;
//    private float spawnInterval;
//    private Array<Integer> spawnPoints; // Index của các điểm spawn có thể sử dụng
//
//    public WaveConfig(float spawnInterval) {
//        this.enemyGroups = new Array<>();
//        this.spawnPoints = new Array<>();
//        this.spawnInterval = spawnInterval;
//    }
//
//    public void addEnemyGroup(Enemy.Type type, int count, float health, float speed) {
//        enemyGroups.add(new EnemyGroup(type, count, health, speed));
//    }
//
//    public void addSpawnPoint(int index) {
//        spawnPoints.add(index);
//    }
//
//    public Array<EnemyGroup> getEnemyGroups() {
//        return enemyGroups;
//    }
//
//    public float getSpawnInterval() {
//        return spawnInterval;
//    }
//
//    public Array<Integer> getSpawnPoints() {
//        return spawnPoints;
//    }
//}
