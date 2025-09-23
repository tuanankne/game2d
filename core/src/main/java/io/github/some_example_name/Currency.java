package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Currency {
    private static int money;
    private static Texture coinTexture;
    private static final int COIN_SIZE = 30;  // Kích thước icon coin

    // Giá tiền cho mỗi loại tháp
    public static final int CANNON_COST = 100;
    public static final int MISSILE_COST = 200;
    public static final int LASER_COST = 150;

    // Tiền thưởng cho mỗi loại quái
    public static final int NORMAL_REWARD = 20;
    public static final int FAST_REWARD = 30;
    public static final int TANK_REWARD = 50;

    public static void initialize(int startingMoney) {
        money = startingMoney;
        coinTexture = new Texture("map1/towerDefense_tile287.png");
    }

    public static boolean canAfford(Tower.Type towerType) {
        switch (towerType) {
            case CANNON: return money >= CANNON_COST;
            case MISSILE: return money >= MISSILE_COST;
            case LASER: return money >= LASER_COST;
            default: return false;
        }
    }

    public static int getCost(Tower.Type towerType) {
        switch (towerType) {
            case CANNON: return CANNON_COST;
            case MISSILE: return MISSILE_COST;
            case LASER: return LASER_COST;
            default: return 0;
        }
    }

    public static void addReward(Enemy.Type enemyType) {
        switch (enemyType) {
            case NORMAL: money += NORMAL_REWARD; break;
            case FAST: money += FAST_REWARD; break;
            case TANK: money += TANK_REWARD; break;
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

    public static Texture getCoinTexture() {
        return coinTexture;
    }

    public static void dispose() {
        if (coinTexture != null) {
            coinTexture.dispose();
        }
    }
}
