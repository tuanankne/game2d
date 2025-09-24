package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Vector2 position;    // Vị trí hiện tại
    private Vector2 velocity;    // Vận tốc
    private float rotation;      // Góc xoay
    private Texture texture;     // Texture của đạn
    private Texture effectTexture; // Texture cho hiệu ứng (lửa, khói)
    private boolean active;      // Trạng thái hoạt động
    private float speed;        // Tốc độ di chuyển
    private Enemy targetEnemy;       // Mục tiêu Enemy đang nhắm
    private Obstacle targetObstacle;    // Mục tiêu Obstacle đang nhắm
    private Tower.Type projectileType; // Loại đạn (để xác định hiệu ứng)
    private float turnSpeed; // Tốc độ xoay của đạn (độ/giây)
    private float maxTurnSpeed = 180f; // Tốc độ xoay tối đa (độ/giây)
    private float acceleration = 200f; // Gia tốc khi đuổi theo mục tiêu
    private float effectRotation; // Góc xoay của hiệu ứng
    private float effectScale = 1f; // Tỷ lệ kích thước hiệu ứng
    private float effectAlpha = 1f; // Độ trong suốt của hiệu ứng
    private float effectTimer = 0f; // Thời gian hiệu ứng
    private float damage; // Sát thương của đạn

    public Projectile(Tower.Type towerType, float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        active = false;
        projectileType = towerType;

        // Thiết lập các thông số dựa vào loại tháp
        switch (towerType) {
            case CANNON:
                texture = new Texture("map1/towerDefense_tile272.png"); // Đạn pháo
                effectTexture = new Texture("map1/towerDefense_tile295.png"); // Hiệu ứng nổ
                speed = 300f;
                effectScale = 0.7f;
                turnSpeed = 360f; // Xoay nhanh
                break;
            case MISSILE:
                texture = new Texture("map1/towerDefense_tile251.png"); // Tên lửa
                effectTexture = new Texture("map1/towerDefense_tile294.png"); // Hiệu ứng lửa
                speed = 200f;
                effectScale = 0.5f;
                turnSpeed = 120f; // Xoay chậm hơn để tạo quỹ đạo cong
                break;
            case LASER:
                texture = new Texture("map1/towerDefense_tile296.png"); // Tia laser
                effectTexture = new Texture("map1/towerDefense_tile297.png"); // Hiệu ứng laser
                speed = 400f;
                effectScale = 0.4f;
                turnSpeed = 540f; // Xoay rất nhanh
                break;
        }

        // Tạm thời bỏ qua hiệu ứng
        //if (trailEffect != null) {
        //    trailEffect.start(); // Bắt đầu hiệu ứng
        //}
    }

    // Bắn đạn về phía mục tiêu
    public void fire(Enemy target, float rotation, float damage) {
        this.targetEnemy = target;
        this.targetObstacle = null;
        this.rotation = rotation;
        this.active = true;
        this.damage = damage;

        // Khởi tạo vận tốc ban đầu theo hướng bắn
        float radians = rotation * MathUtils.degreesToRadians;
        velocity.x = MathUtils.cos(radians) * speed;
        velocity.y = MathUtils.sin(radians) * speed;
    }

    public void fireAtObstacle(Obstacle obstacle, float rotation, float damage) {
        this.targetObstacle = obstacle;
        this.targetEnemy = null;
        this.rotation = rotation;
        this.active = true;
        this.damage = damage;

        // Khởi tạo vận tốc ban đầu theo hướng bắn
        float radians = rotation * MathUtils.degreesToRadians;
        velocity.x = MathUtils.cos(radians) * speed;
        velocity.y = MathUtils.sin(radians) * speed;
    }

    // Cập nhật vị trí đạn
    public void update(float delta) {
        if (!active) return;

        // Áp dụng tốc độ game
        float adjustedDelta = delta * GameControls.getGameSpeed();

        float targetX = 0, targetY = 0;
        boolean hasTarget = false;
        float predictedTime = 0.2f; // Thời gian dự đoán trước (giây)

        // Xác định mục tiêu và vị trí, tính toán điểm chặn
        if (targetEnemy != null && targetEnemy.isAlive()) {
            // Tính toán vị trí dự đoán của enemy
            Vector2 predictedPos = predictTargetPosition(targetEnemy, predictedTime);
            targetX = predictedPos.x;
            targetY = predictedPos.y;
            hasTarget = true;
        } else if (targetObstacle != null && !targetObstacle.isDestroyed()) {
            targetX = targetObstacle.getX() + targetObstacle.getWidth()/2;
            targetY = targetObstacle.getY() + targetObstacle.getHeight()/2;
            hasTarget = true;
        }

        if (hasTarget) {
            // Tính toán vector hướng đến mục tiêu
            float dx = targetX - position.x;
            float dy = targetY - position.y;
            float distToTarget = (float)Math.sqrt(dx * dx + dy * dy);

            // Tính góc đến mục tiêu
            float targetAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            
            // Chuẩn hóa góc về khoảng [-180, 180]
            targetAngle = ((targetAngle % 360) + 360) % 360;
            if (targetAngle > 180) targetAngle -= 360;
            
            float currentRotation = ((rotation % 360) + 360) % 360;
            if (currentRotation > 180) currentRotation -= 360;

            // Tính góc chênh lệch ngắn nhất
            float angleDiff = targetAngle - currentRotation;
            if (angleDiff > 180) angleDiff -= 360;
            if (angleDiff < -180) angleDiff += 360;

            // Điều chỉnh tốc độ xoay dựa vào khoảng cách
            float adjustedTurnSpeed = turnSpeed;
            if (distToTarget < 100) {
                // Giảm tốc độ xoay khi gần mục tiêu để tránh xoay vòng
                adjustedTurnSpeed *= (distToTarget / 100);
            }
            adjustedTurnSpeed *= adjustedDelta;

            // Xoay đạn
            float turnAmount = Math.min(Math.abs(angleDiff), adjustedTurnSpeed) * Math.signum(angleDiff);
            rotation += turnAmount;

            // Tính toán vận tốc mới
            float radians = rotation * MathUtils.degreesToRadians;
            float currentSpeed = speed;

            // Điều chỉnh tốc độ dựa vào góc lệch và khoảng cách
            if (Math.abs(angleDiff) < 30) {
                if (distToTarget > 200) {
                    currentSpeed += acceleration * adjustedDelta;
                } else {
                    // Giảm tốc khi gần mục tiêu
                    currentSpeed = Math.max(speed * 0.5f, currentSpeed - acceleration * adjustedDelta);
                }
            } else {
                // Giảm tốc khi góc lệch lớn
                currentSpeed = Math.max(speed * 0.3f, speed - Math.abs(angleDiff) / 180 * speed);
            }

            // Cập nhật vận tốc và vị trí
            velocity.x = MathUtils.cos(radians) * currentSpeed;
            velocity.y = MathUtils.sin(radians) * currentSpeed;
            position.x += velocity.x * adjustedDelta;
            position.y += velocity.y * adjustedDelta;

            // Cập nhật hiệu ứng
            effectTimer += adjustedDelta;
            updateEffects(adjustedDelta);

            // Kiểm tra va chạm với bán kính thay đổi theo tốc độ
            float collisionRadius = 20 + (currentSpeed / speed) * 10;
            if (distToTarget < collisionRadius) {
                if (targetEnemy != null) {
                    targetEnemy.hit(damage);
                } else if (targetObstacle != null) {
                    targetObstacle.hit(damage);
                }
                active = false;
            }

            // Hủy đạn nếu đi quá xa mục tiêu
            if (distToTarget > 1000) {
                active = false;
            }
        } else {
            active = false;
        }
    }

    // Dự đoán vị trí mục tiêu sau một khoảng thời gian
    private Vector2 predictTargetPosition(Enemy target, float time) {
        Vector2 predictedPos = new Vector2(target.getX(), target.getY());
        
        // Tính toán vị trí dự đoán dựa trên hướng di chuyển hiện tại của enemy
        // (Giả sử enemy có phương thức getVelocity() trả về vector vận tốc)
        // predictedPos.add(target.getVelocity().x * time, target.getVelocity().y * time);
        
        return predictedPos;
    }

    // Cập nhật hiệu ứng của đạn
    private void updateEffects(float adjustedDelta) {
        switch (projectileType) {
            case MISSILE:
                effectRotation = rotation + 180;
                effectAlpha = 0.8f + MathUtils.sin(effectTimer * 10) * 0.2f;
                break;
            case CANNON:
                effectRotation += adjustedDelta * 360;
                effectAlpha = 0.6f + MathUtils.cos(effectTimer * 5) * 0.4f;
                break;
            case LASER:
                effectRotation = rotation;
                effectAlpha = 0.5f + MathUtils.sin(effectTimer * 15) * 0.5f;
                break;
        }
    }

    // Vẽ đạn
    public void render(SpriteBatch batch) {
        if (!active) return;

        // Vẽ hiệu ứng trước (để nó ở phía sau đạn)
        batch.setColor(1, 1, 1, effectAlpha);
        batch.draw(
            effectTexture,
            position.x - (float) effectTexture.getWidth() /2, position.y - (float) effectTexture.getHeight() /2,
            (float) effectTexture.getWidth() /2, (float) effectTexture.getHeight() /2,
            effectTexture.getWidth(), effectTexture.getHeight(),
            effectScale, effectScale,
            effectRotation,
            0, 0,
            effectTexture.getWidth(), effectTexture.getHeight(),
            false, false
        );

        // Vẽ đạn với góc xoay
        batch.setColor(1, 1, 1, 1);
        batch.draw(
            texture,
            position.x - (float) texture.getWidth() /2, position.y - (float) texture.getHeight() /2,
            (float) texture.getWidth() /2, (float) texture.getHeight() /2,
            texture.getWidth(), texture.getHeight(),
            1, 1,
            rotation - 90, // Trừ 90 độ để đạn hướng theo chiều bay
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public boolean isActive() {
        return active;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
        if (effectTexture != null) {
            effectTexture.dispose();
        }
    }
}
