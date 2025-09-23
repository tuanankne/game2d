package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Enemy {
    public enum Type {
        NORMAL,
        FAST,
        TANK
    }
    private Vector2 position;           // Vị trí hiện tại
    private float speed;               // Tốc độ di chuyển
    private Array<Vector2> path;       // Đường đi
    private int currentPathIndex;      // Vị trí hiện tại trên đường đi
    private Texture texture;           // Texture của quái
    private int health;                // Máu hiện tại của quái
    private int maxHealth;             // Máu tối đa của quái
    private boolean alive;             // Trạng thái sống/chết
    private static final float HEALTHBAR_WIDTH = 40f;  // Chiều rộng thanh máu
    private static final float HEALTHBAR_HEIGHT = 4f;  // Chiều cao thanh máu
    private static final float HEALTHBAR_Y_OFFSET = 30f; // Khoảng cách từ quái đến thanh máu
    
    public Enemy(float x, float y, Type type, float health, float speed) {
        position = new Vector2(x, y);
        currentPathIndex = 0;
        alive = true;
        this.speed = speed;
        this.maxHealth = (int)health;
        this.health = maxHealth;
        this.type = type;  // Lưu loại quái
        
        // Thiết lập texture dựa vào loại quái
        switch (type) {
            case NORMAL:
                texture = new Texture("map1/towerDefense_tile245.png");
                break;
            case FAST:
                texture = new Texture("map1/towerDefense_tile246.png");
                break;
            case TANK:
                texture = new Texture("map1/towerDefense_tile247.png");
                break;
        }
    }
    
    public void setPath(Array<Vector2> path) {
        this.path = path;
    }
    
    private float rotation = 0;  // Góc xoay của enemy

    public void update(float delta) {
        if (!alive || currentPathIndex >= path.size) return;
        
        // Áp dụng tốc độ game
        float adjustedDelta = delta * GameControls.getGameSpeed();
        
        // Lấy điểm tiếp theo trên đường đi
        Vector2 target = path.get(currentPathIndex);
        
        // Tính vector hướng đến điểm tiếp theo
        float dx = target.x - position.x;
        float dy = target.y - position.y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1) {
            // Đã đến điểm tiếp theo, chuyển sang điểm kế
            currentPathIndex++;
            if (currentPathIndex >= path.size) {
                // Đã đến đích, gây sát thương cho người chơi
                PlayerHealth.takeDamage(type);
                alive = false;
                return;
            }
        } else {
            // Tính góc xoay dựa trên hướng di chuyển
            rotation = (float)Math.toDegrees(Math.atan2(dy, dx));
            
            // Di chuyển về phía điểm tiếp theo với tốc độ game được áp dụng
            position.x += (dx / distance) * speed * adjustedDelta;
            position.y += (dy / distance) * speed * adjustedDelta;
        }
    }
    
    public void render(SpriteBatch batch) {
        if (!alive) return;
        
        // Vẽ quái với góc xoay
        batch.draw(
            texture,
            position.x - texture.getWidth()/2,
            position.y - texture.getHeight()/2,
            texture.getWidth()/2,  // Điểm xoay ở giữa texture
            texture.getHeight()/2,
            texture.getWidth(),
            texture.getHeight(),
            1, 1,                  // Scale
            rotation,              // Góc xoay
            0, 0,                 // Source position
            texture.getWidth(),
            texture.getHeight(),
            false, false          // Flip
        );
        
        // Vẽ thanh máu nền (màu đỏ)
        batch.setColor(1, 0, 0, 0.8f);
        batch.draw(
            texture, // Sử dụng texture hiện có làm hình chữ nhật
            position.x - HEALTHBAR_WIDTH/2,
            position.y + HEALTHBAR_Y_OFFSET,
            HEALTHBAR_WIDTH,
            HEALTHBAR_HEIGHT
        );
        
        // Vẽ thanh máu hiện tại (màu xanh)
        batch.setColor(0, 1, 0, 0.8f);
        float healthRatio = (float)health / maxHealth;
        batch.draw(
            texture, // Sử dụng texture hiện có làm hình chữ nhật
            position.x - HEALTHBAR_WIDTH/2,
            position.y + HEALTHBAR_Y_OFFSET,
            HEALTHBAR_WIDTH * healthRatio,
            HEALTHBAR_HEIGHT
        );
        
        // Reset màu về mặc định
        batch.setColor(1, 1, 1, 1);
    }
    
    public void renderDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer) {
        if (!alive || currentPathIndex >= path.size) return;
        
        // Vẽ đường đi còn lại
        shapeRenderer.setColor(1, 0, 0, 1);
        Vector2 start = position;
        for (int i = currentPathIndex; i < path.size; i++) {
            Vector2 end = path.get(i);
            shapeRenderer.line(start.x, start.y, end.x, end.y);
            start = end;
        }
    }
    
    public boolean hasReachedEnd() {
        return currentPathIndex >= path.size;
    }
    
    private Type type;  // Thêm biến type

    public void hit(float damage) {
        health -= (int)damage;  // Chuyển damage từ float sang int
        if (health <= 0 && alive) {
            alive = false;
            Currency.addReward(type);  // Thêm tiền thưởng khi quái chết
        }
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public float getX() {
        return position.x;
    }
    
    public float getY() {
        return position.y;
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