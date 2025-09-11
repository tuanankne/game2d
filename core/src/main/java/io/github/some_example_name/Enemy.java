package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Enemy {
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
    
    public Enemy(float x, float y, EnemyType type) {
        position = new Vector2(x, y);
        currentPathIndex = 0;
        alive = true;
        
        // Thiết lập thông số dựa vào loại quái
        switch (type) {
            case NORMAL:
                texture = new Texture("map1/towerDefense_tile245.png");
                speed = 100f;
                maxHealth = 100;
                health = maxHealth;
                break;
            case FAST:
                texture = new Texture("map1/towerDefense_tile246.png");
                speed = 150f;
                maxHealth = 50;
                health = maxHealth;
                break;
            case TANK:
                texture = new Texture("map1/towerDefense_tile247.png");
                speed = 50f;
                maxHealth = 200;
                health = maxHealth;
                break;
        }
    }
    
    public void setPath(Array<Vector2> path) {
        this.path = path;
    }
    
    public void update(float delta) {
        if (!alive || currentPathIndex >= path.size) return;
        
        // Lấy điểm tiếp theo trên đường đi
        Vector2 target = path.get(currentPathIndex);
        
        // Tính vector hướng đến điểm tiếp theo
        float dx = target.x - position.x;
        float dy = target.y - position.y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1) {
            // Đã đến điểm tiếp theo, chuyển sang điểm kế
            currentPathIndex++;
        } else {
            // Di chuyển về phía điểm tiếp theo
            position.x += (dx / distance) * speed * delta;
            position.y += (dy / distance) * speed * delta;
        }
    }
    
    public void render(SpriteBatch batch) {
        if (!alive) return;
        
        // Vẽ quái
        batch.draw(
            texture,
            position.x - texture.getWidth()/2,
            position.y - texture.getHeight()/2
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
    
    public void hit() {
        health -= 25;  // Mỗi đạn gây 25 sát thương
        if (health <= 0) {
            alive = false;
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
    
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}