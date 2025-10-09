package io.github.some_example_name.entities.obstacle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.utils.Currency;

public class Obstacle {
    private Vector2 position;
    private float width;
    private float height;
    private int maxHealth;
    private int currentHealth;
    private int reward;  // Số tiền nhận được khi phá hủy
    private Texture texture;
    private boolean isTargeted;
    private float targetCircleRadius = 15f;
    private Type type;

    public enum Type {
        ROCK_01(350, 30, "map1/Rock01.png"),   // Cụm đá nhỏ: máu rất cao, thưởng rất cao
        ROCK_02(300, 25, "map1/Rock02.png"),   // Cụm đá lớn: máu cực cao, thưởng cực cao
        ROCK_03(250, 20, "map1/Rock03.png"),   // Tảng đá khổng lồ: máu tối đa, thưởng tối đa
        ROCK_04(180, 10, "map1/Rock04.png"),   // Tảng đá khổng lồ: máu tối đa, thưởng tối đa
        ROCK_05(120, 10, "map1/Rock05.png"),  // Tảng đá khổng lồ: máu tối đa, thưởng tối đa

        TENT(400, 15, "map1/Tent.png"),   // Lều trại: máu cực cao, thưởng cực cao
        TREE_LARGE(600, 15, "map1/TreeLarge.png"),   // Cây lớn: máu tối đa, thưởng tối đa
        TREE_MEDIUM(450, 20, "map1/TreeMedium.png"),
        TREE_SMALL(300, 25, "map1/TreeSmall.png"),
        TREE_STUMP_SHORT(200, 15, "map1/TreeStumpShort.png"),
        TREE_STUMP_TALL(250, 20, "map1/TreeStumpTall.png"),
        WATCH_TOWER_SHORT(800, 20, "map1/WatchtowerShort.png"),
        WATCH_TOWER_TALL(1000, 25, "map1/WatchtowerTall.png"),
        WELL(350, 20, "map1/Well.png"),
        WIND_MILL(1200, 75, "map1/WindMill.png"),
        WOODEN_BARREL(150, 10, "map1/WoodenBarrel.png"),
        WOODEN_CART(400, 30, "map1/WoodenCart.png"),
        BLUE_BANNER(100, 10, "map1/BlueBanner.png"),
        BUSHES_LARGE(300, 20, "map1/BushesLarge.png"),
        BUSHES_MEDIUM(200, 15, "map1/BushesMedium.png"),
        BUSHES_SMALL(100, 10, "map1/BushesSmall.png"),
        CAMP_FIRE(250, 20, "map1/Campfire.png"),
        FLAG(150, 10, "map1/Flag.png"),
        WOODEN_FENCE_HORIZONTAL(200, 15, "map1/WoodenFenceHorizontal.png"),
        WOODEN_FENCE_VERTICAL(200, 15, "map1/WoodenFenceVertical.png"),
        RED_BANNER(100, 10, "map1/RedBanner.png");


        private final int health;
        private final int reward;
        private final String texturePath;

        Type(int health, int reward, String texturePath) {
            this.health = health;
            this.reward = reward;
            this.texturePath = texturePath;
        }
    }

    public Obstacle(float x, float y, float width, float height, Type type) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.type = type;
        this.maxHealth = type.health;
        this.currentHealth = maxHealth;
        this.reward = type.reward;
        this.texture = new Texture(type.texturePath);
    }

    public void hit(float damage) {
        currentHealth -= (int)damage;
        if (currentHealth <= 0) {
            Currency.addMoney(reward);
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, width, height);

        // Vẽ thanh máu nếu bị damage
        if (currentHealth < maxHealth) {
            float healthBarWidth = width * 0.8f;
            float healthBarHeight = 4f;
            float healthBarY = position.y + height + 5f;

            // Vẽ nền đỏ
            batch.setColor(1, 0, 0, 0.8f);
            batch.draw(texture, position.x + (width - healthBarWidth)/2, healthBarY,
                      healthBarWidth, healthBarHeight);

            // Vẽ máu xanh
            batch.setColor(0, 1, 0, 0.8f);
            float healthRatio = (float)currentHealth / maxHealth;
            batch.draw(texture, position.x + (width - healthBarWidth)/2, healthBarY,
                      healthBarWidth * healthRatio, healthBarHeight);

            batch.setColor(1, 1, 1, 1);
        }
    }

    public void renderDebug(ShapeRenderer shapeRenderer) {
        // Vẽ vòng tròn target nếu được nhắm
        if (isTargeted) {
            shapeRenderer.setColor(1, 0, 0, 0.8f);
            shapeRenderer.circle(position.x + width/2, position.y + height/2, targetCircleRadius);
        }
    }

    public void setTargeted(boolean targeted) {
        if (this.isTargeted != targeted) {
            this.isTargeted = targeted;
        }
    }

    public boolean isTargeted() {
        return isTargeted;
    }

    public boolean isDestroyed() {
        return currentHealth <= 0;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Type getType() {
        return type;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
