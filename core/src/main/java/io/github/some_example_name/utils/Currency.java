package io.github.some_example_name.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.entities.enemy.Enemy;
import io.github.some_example_name.entities.tower.TowerType;

public class Currency {
    private static int money;
    private static Texture coinTexture;
    private static final int COIN_SIZE = 30;  // Kích thước icon coin

    // Giá tiền cho mỗi loại tháp
    public static final int STONE_TOWER_COST = 100;
    public static final int FIRE_TOWER_COST = 80;
    public static final int BIGLAND_TOWER_COST = 250;
    public static final int LAND_TOWER_COST = 150;

    // Tiền thưởng cho mỗi loại quái
    public static final int NORMAL_REWARD = 20;
    public static final int FAST_REWARD = 30;
    public static final int TANK_REWARD = 50;
    public static final int BOSS_REWARD = 100;

    public static void initialize(int startingMoney) {
        money = startingMoney;
        coinTexture = new Texture("map1/towerDefense_tile287.png");
    }

    public static boolean canAfford(TowerType towerType) {
        switch (towerType) {
            case STONE_TOWER: return money >= STONE_TOWER_COST;
            case FIRE_TOWER: return money >= FIRE_TOWER_COST;
            case BIGLAND_TOWER: return money >= BIGLAND_TOWER_COST;
            case LAND_TOWER: return money >= LAND_TOWER_COST;
            default: return false;
        }
    }

    public static int getCost(TowerType towerType) {
        switch (towerType) {
            case STONE_TOWER: return STONE_TOWER_COST;
            case FIRE_TOWER: return FIRE_TOWER_COST;
            case BIGLAND_TOWER: return BIGLAND_TOWER_COST;
            case LAND_TOWER: return LAND_TOWER_COST;
            default: return 0;
        }
    }

    public static void addReward(Enemy.Type enemyType) {
        switch (enemyType) {
            case NORMAL: money += NORMAL_REWARD; break;
            case FAST: money += FAST_REWARD; break;
            case TANK: money += TANK_REWARD; break;
            case BOSS: money += BOSS_REWARD; break; // Boss reward
        }
    }

    public static boolean spendMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public static int getMoney() {
        return money;
    }

    public static void addMoney(int amount) {
        money += amount;
    }

    public static Texture getCoinTexture() {
        return coinTexture;
    }

    public static void dispose() {
        if (coinTexture != null) {
            coinTexture.dispose();
        }
    }
}
