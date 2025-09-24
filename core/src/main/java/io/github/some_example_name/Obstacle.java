package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

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
        BUSH_CLUSTER(30, 20, "map1/towerDefense_tile130.png"),     // Bụi cây nhỏ: máu thấp, thưởng ít
        BUSH_SMALL(50, 35, "map1/towerDefense_tile131.png"),    // Bụi cây vừa: máu trung bình, thưởng vừa
        BUSH_MEDIUM(70, 50, "map1/towerDefense_tile132.png"),     // Bụi cây to: máu cao, thưởng khá
        BUSH_LARGE(100, 60, "map1/towerDefense_tile134.png"),   // Đá vừa: máu cao, thưởng khá
        ROCK_SMALL(150, 100, "map1/towerDefense_tile135.png"),   // Đá to: máu rất cao, thưởng cao
        ROCK_HUGE(200, 150, "map1/towerDefense_tile136.png"),    // Tảng đá lớn: máu cực cao, thưởng rất cao
        ROCK_LARGE(90, 70, "map1/towerDefense_tile137.png");   // Cụm bụi cây: máu cao, thưởng cao

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
