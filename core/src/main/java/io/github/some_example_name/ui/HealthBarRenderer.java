package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Class quản lý việc vẽ thanh máu sử dụng texture từ thư mục heal
 */
public class HealthBarRenderer {
    private static Texture healthBarBackground;  // Texture nền thanh máu
    private static Texture healthBarForeground;  // Texture máu hiện tại
    
    // Kích thước mặc định của thanh máu
    private static final float DEFAULT_WIDTH = 100f;
    private static final float DEFAULT_HEIGHT = 10f;
    
    /**
     * Khởi tạo health bar renderer
     */
    public static void initialize() {
        healthBarBackground = new Texture("heal/health_bar-04.png");  // Nền đỏ
        healthBarForeground = new Texture("heal/health_bar-05.png");  // Máu xanh
    }
    
    /**
     * Vẽ thanh máu tại vị trí xác định
     * @param batch SpriteBatch để vẽ
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param width Chiều rộng thanh máu
     * @param height Chiều cao thanh máu
     * @param healthRatio Tỷ lệ máu (0.0 - 1.0)
     */
    public static void renderHealthBar(SpriteBatch batch, float x, float y, 
                                     float width, float height, float healthRatio) {
        if (healthBarBackground == null || healthBarForeground == null) {
            initialize();
        }
        
        // Đảm bảo healthRatio trong khoảng [0, 1]
        healthRatio = Math.max(0f, Math.min(1f, healthRatio));
        
        // Vẽ nền thanh máu (màu đỏ)
        batch.draw(healthBarBackground, x, y, width, height);
        
        // Vẽ máu hiện tại (màu xanh)
        if (healthRatio > 0) {
            batch.draw(healthBarForeground, x, y, width * healthRatio, height);
        }
    }
    
    /**
     * Vẽ thanh máu với kích thước mặc định
     * @param batch SpriteBatch để vẽ
     * @param x Tọa độ X
     * @param y Tọa độ Y
     * @param healthRatio Tỷ lệ máu (0.0 - 1.0)
     */
    public static void renderHealthBar(SpriteBatch batch, float x, float y, float healthRatio) {
        renderHealthBar(batch, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, healthRatio);
    }
    
    /**
     * Giải phóng tài nguyên
     */
    public static void dispose() {
        if (healthBarBackground != null) {
            healthBarBackground.dispose();
            healthBarBackground = null;
        }
        if (healthBarForeground != null) {
            healthBarForeground.dispose();
            healthBarForeground = null;
        }
    }
}
