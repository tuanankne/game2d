package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import io.github.some_example_name.entities.enemy.Enemy;

public class PlayerHealth {
    private static int maxHealth = 100;
    private static int currentHealth;
    private static ShapeRenderer shapeRenderer;
    private static final float BAR_WIDTH = 800;  // Thanh máu dài hơn
    private static final float BAR_HEIGHT = 30;  // Chiều cao vừa phải
    private static final Color BORDER_COLOR = new Color(0.2f, 0.2f, 0.2f, 1);
    private static final Color BACKGROUND_COLOR = new Color(0.3f, 0.3f, 0.3f, 0.8f);
    private static final Color HEALTH_COLOR_1 = new Color(0.2f, 0.8f, 0.2f, 1);
    private static final Color HEALTH_COLOR_2 = new Color(0.1f, 0.6f, 0.1f, 1);
    private static final Color DAMAGE_COLOR_1 = new Color(0.8f, 0.2f, 0.2f, 0.6f);
    private static final Color DAMAGE_COLOR_2 = new Color(0.6f, 0.1f, 0.1f, 0.6f);

    public static void initialize() {
        currentHealth = maxHealth;
        shapeRenderer = new ShapeRenderer();
    }

    public static void takeDamage(Enemy.Type enemyType) {
        int damage;
        switch (enemyType) {
            case NORMAL:
                damage = 5;  // Quái thường gây 5 sát thương
                break;
            case FAST:
                damage = 3;  // Quái nhanh gây 3 sát thương
                break;
            case TANK:
                damage = 10; // Quái tank gây 10 sát thương
                break;
            default:
                damage = 0;
                break;
        }
        int oldHealth = currentHealth;
        currentHealth = Math.max(0, currentHealth - damage);
    }

    public static boolean isGameOver() {
        return currentHealth <= 0;
    }

    public static int getCurrentHealth() {
        return currentHealth;
    }

    public static int getMaxHealth() {
        return maxHealth;
    }

    public static void render(SpriteBatch batch, BitmapFont font, float screenWidth) {
        float x = (screenWidth - BAR_WIDTH) / 2;  // Căn giữa thanh máu
        float y = Gdx.graphics.getHeight() - BAR_HEIGHT - 5;  // Sát lề trên, chỉ cách 5px

        float borderThickness = 2;  // Độ dày viền
        float innerWidth = BAR_WIDTH - 2 * borderThickness;  // Chiều rộng phần trong
        float innerHeight = BAR_HEIGHT - 2 * borderThickness;  // Chiều cao phần trong
        float innerX = x + borderThickness;  // Vị trí X phần trong
        float innerY = y + borderThickness;  // Vị trí Y phần trong
        float healthRatio = (float)currentHealth / maxHealth;
        float currentBarWidth = innerWidth * healthRatio;

        // Tạm dừng SpriteBatch để vẽ shapes
        batch.end();

        // Bật blend để có hiệu ứng trong suốt
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        // Thiết lập projection matrix cho shapeRenderer
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Vẽ viền
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.end();

        // Vẽ background
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(BACKGROUND_COLOR);
        shapeRenderer.rect(innerX, innerY, innerWidth, innerHeight);
        shapeRenderer.end();

        // Vẽ phần máu đã mất với gradient
        shapeRenderer.begin(ShapeType.Filled);
        for (float i = 0; i < innerWidth - currentBarWidth; i++) {
            float alpha = i / (innerWidth - currentBarWidth);
            shapeRenderer.setColor(DAMAGE_COLOR_1.cpy().lerp(DAMAGE_COLOR_2, alpha));
            shapeRenderer.rect(innerX + currentBarWidth + i, innerY, 1, innerHeight);
        }
        shapeRenderer.end();

        // Vẽ phần máu còn lại với gradient
        shapeRenderer.begin(ShapeType.Filled);
        for (float i = 0; i < currentBarWidth; i++) {
            float alpha = i / currentBarWidth;
            shapeRenderer.setColor(HEALTH_COLOR_1.cpy().lerp(HEALTH_COLOR_2, alpha));
            shapeRenderer.rect(innerX + i, innerY, 1, innerHeight);
        }
        shapeRenderer.end();

        // Tắt blend
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Bật lại SpriteBatch để vẽ text
        batch.begin();
        batch.setColor(1, 1, 1, 1);
        String healthText = currentHealth + "/" + maxHealth;
        font.getData().setScale(1.5f);  // Kích thước font vừa phải
        GlyphLayout layout = new GlyphLayout(font, healthText);
        float textX = x + BAR_WIDTH/2 - layout.width/2;
        float textY = y + BAR_HEIGHT/2 + layout.height/2;

        // Vẽ viền đen cho text
        batch.setColor(0, 0, 0, 1);
        font.draw(batch, healthText, textX - 1, textY - 1);
        font.draw(batch, healthText, textX + 1, textY - 1);
        font.draw(batch, healthText, textX - 1, textY + 1);
        font.draw(batch, healthText, textX + 1, textY + 1);

        // Vẽ text chính màu trắng
        batch.setColor(1, 1, 1, 1);
        font.draw(batch, healthText, textX, textY);
        font.getData().setScale(1.0f);  // Reset kích thước font về mặc định
    }

    public static void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
