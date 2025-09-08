package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class Enemy {
    private Vector2 position;
    private Vector2 target;
    private Vector2 direction;
    private float speed;
    private float health;
    private Texture texture;
    private static final float BASE_SIZE = 64; // Kích thước cơ bản
    private float rotation; // Góc xoay của sprite
    private float scale; // Tỷ lệ phóng to/thu nhỏ

    public Enemy(float x, float y, EnemyType type) {
        position = new Vector2(x, y);
        target = new Vector2();
        direction = new Vector2();
        texture = new Texture(type.getTexturePath());
        speed = type.getSpeed();
        health = type.getHealth();
        scale = type.getScale();
        rotation = 0;
    }

    public void setTarget(float x, float y) {
        target.set(x, y);
    }

    private Vector2 currentWaypoint;
    private int currentWaypointIndex;
    private com.badlogic.gdx.utils.Array<Vector2> path;

    public void setPath(com.badlogic.gdx.utils.Array<Vector2> newPath) {
        this.path = newPath;
        currentWaypointIndex = 0;
        if (path != null && path.size > 0) {
            currentWaypoint = path.get(0);
            target.set(currentWaypoint);
        }
    }

    public void update(float delta) {
        if (path == null || path.size == 0) return;

        // Calculate direction to current waypoint
        direction.set(target).sub(position);
        float distance = direction.len();

        // Move towards target if not already there
        if (distance > 1) {
            direction.nor(); // Normalize để có vector đơn vị
            position.add(direction.x * speed * delta, direction.y * speed * delta);
            
            // Cập nhật góc xoay (đổi sang độ)
            // Tính góc dựa trên hướng di chuyển
            rotation = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
        } else {
            // Đã đến waypoint hiện tại, chuyển sang waypoint tiếp theo
            currentWaypointIndex++;
            if (currentWaypointIndex < path.size) {
                currentWaypoint = path.get(currentWaypointIndex);
                target.set(currentWaypoint);
            }
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean hasReachedEnd() {
        return currentWaypointIndex >= path.size;
    }

    public void render(SpriteBatch batch) {
        float size = BASE_SIZE * scale; // Kích thước thực tế sau khi scale
        batch.draw(texture, 
            position.x - size/2, position.y - size/2, // Vị trí
            size/2, size/2,                          // Điểm xoay (center)
            size, size,                              // Kích thước
            1, 1,                                    // Scale bổ sung nếu cần
            rotation,                                // Góc xoay theo hướng di chuyển
            0, 0,                                    // Source position
            texture.getWidth(), texture.getHeight(), // Source size
            false, false);                          // Flip
    }

    // Debug: vẽ hướng di chuyển
    public void renderDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(1, 0, 0, 1); // Màu đỏ
        shapeRenderer.line(position.x, position.y, 
            position.x + direction.x * 30, 
            position.y + direction.y * 30);
    }

    public void dispose() {
        texture.dispose();
    }
}
