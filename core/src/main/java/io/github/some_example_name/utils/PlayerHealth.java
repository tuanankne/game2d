package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.entities.enemy.Enemy;
import io.github.some_example_name.ui.HealthBarRenderer;

public class PlayerHealth {
    private static int maxHealth = 100;
    private static int currentHealth;
    private static ShapeRenderer shapeRenderer;
    private static final float BAR_WIDTH = 800;  // Thanh máu dài hơn
    private static final float BAR_HEIGHT = 30;  // Chiều cao vừa phải

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
        float healthRatio = (float)currentHealth / maxHealth;

        // Vẽ thanh máu sử dụng HealthBarRenderer
        HealthBarRenderer.renderHealthBar(batch, x, y, BAR_WIDTH, BAR_HEIGHT, healthRatio);

        // Vẽ text hiển thị số máu
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
        HealthBarRenderer.dispose();
    }
}
