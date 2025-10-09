package io.github.some_example_name.entities.tower;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.utils.GameControls;
import io.github.some_example_name.entities.obstacle.Obstacle;
import io.github.some_example_name.entities.enemy.Enemy;

public class Projectile {
    private Vector2 position;    // Vị trí hiện tại
    private Vector2 velocity;    // Vận tốc
    private float rotation;      // Góc xoay
    private Texture texture;     // Texture của đạn
    private Animation<TextureRegion> effectAnimation; // Animation hiệu ứng nổ
    private TextureRegion[] effectFrames; // Các frame hiệu ứng nổ
    private float effectFrameDuration = 0.05f; // Thời gian mỗi frame
    private boolean active;      // Trạng thái hoạt động
    private float speed;        // Tốc độ di chuyển
    private Enemy targetEnemy;       // Mục tiêu Enemy đang nhắm
    private Obstacle targetObstacle;    // Mục tiêu Obstacle đang nhắm
    private TowerType projectileType; // Loại đạn (để xác định hiệu ứng)
    private float turnSpeed; // Tốc độ xoay của đạn (độ/giây)
    private float maxTurnSpeed = 180f; // Tốc độ xoay tối đa (độ/giây)
    private float acceleration = 200f; // Gia tốc khi đuổi theo mục tiêu
    private float effectRotation; // Góc xoay của hiệu ứng
    private float effectScale = 1f; // Tỷ lệ kích thước hiệu ứng
    private float effectAlpha = 1f; // Độ trong suốt của hiệu ứng
    private float effectTimer = 0f; // Thời gian hiệu ứng
    private float damage; // Sát thương của đạn
    private float flightTime = 0f; // Thời gian bay từ khi bắn
    private Vector2 initialPosition = new Vector2(); // Vị trí ban đầu khi bắn
    private boolean isEffectActive = false; // Hiệu ứng nổ đang hoạt động
    private Vector2 effectPosition = new Vector2(); // Vị trí hiệu ứng nổ

    public Projectile(TowerType towerType, float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        active = false;
        projectileType = towerType;

        // Thiết lập các thông số dựa vào loại tháp
        switch (towerType) {
            case STONE_TOWER:
                texture = new Texture("towers/stone/level1/40.png"); // Đạn pháo
                effectFrames = loadEffectFrames("towers/stone/effect/", 5, 40); // 5 frame nổ
                effectAnimation = new Animation<>(effectFrameDuration, effectFrames);
                speed = 300f;
                effectScale = 1.2f; // Tăng kích thước hiệu ứng nổ
                effectAlpha = 1f;   // Đảm bảo hiệu ứng nổ rõ ràng
                turnSpeed = 360f; // Xoay nhanh
                break;
            case FIRE_TOWER:
                texture = new Texture("towers/fire/level1/35.png"); // Tên lửa
                effectFrames = loadEffectFrames("towers/fire/effect/", 5, 35); // 5 frame lửa
                effectAnimation = new Animation<>(effectFrameDuration, effectFrames);
                speed = 250f;
                effectScale = 1.2f;
                effectAlpha = 1f;
                turnSpeed = 120f; // Xoay chậm hơn để tạo quỹ đạo cong
                break;
            case BIGLAND_TOWER:
                texture = new Texture("towers/bigLand/level1/45.png"); // Tia laser
                effectFrames = loadEffectFrames("towers/bigLand/effect/", 4, 45); // 4 frame laser
                effectAnimation = new Animation<>(effectFrameDuration, effectFrames);
                speed = 200f;
                effectScale = 1.2f;
                effectAlpha = 1f;
                turnSpeed = 540f; // Xoay rất nhanh
                break;
            case LAND_TOWER:
                texture = new Texture("towers/land/level1/29.png"); // Đạn pháo
                effectFrames = loadEffectFrames("towers/land/effect/", 6, 29); // 6 frame nổ
                effectAnimation = new Animation<>(effectFrameDuration, effectFrames);
                speed = 350f;
                effectScale = 1.2f;
                effectAlpha = 1f;
                turnSpeed = 300f; // Xoay vừa phải
                break;
        }
    }

    // Hàm nạp các frame hiệu ứng nổ
    private TextureRegion[] loadEffectFrames(String basePath, int frameCount, int startIndex) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int j = 0; j < frameCount; j++) {
            Texture texture = new Texture(basePath + (startIndex + j) + ".png");
            frames[j] = new TextureRegion(texture);
        }
        return frames;
    }

    // Bắn đạn về phía mục tiêu
    public void fire(Enemy target, float rotation, float damage) {
        this.targetEnemy = target;
        this.targetObstacle = null;
        this.rotation = rotation;
        this.active = true;
        this.damage = damage;
        this.flightTime = 0f;
        this.initialPosition.set(position);

        // Khởi tạo vận tốc ban đầu theo hướng thẳng lên (90 độ)
        velocity.x = 0;
        velocity.y = speed; // Bay thẳng lên
    }

    public void fireAtObstacle(Obstacle obstacle, float rotation, float damage) {
        this.targetObstacle = obstacle;
        this.targetEnemy = null;
        this.rotation = rotation;
        this.active = true;
        this.damage = damage;
        this.flightTime = 0f;
        this.initialPosition.set(position);

        // Khởi tạo vận tốc ban đầu theo hướng thẳng lên (90 độ)
        velocity.x = 0;
        velocity.y = speed; // Bay thẳng lên
    }

    // Cập nhật vị trí đạn
    public void update(float delta) {
        if (!active && !isEffectActive) return;

        float adjustedDelta = delta * GameControls.getGameSpeed();

        if (active) {
            flightTime += adjustedDelta;
            
            // Giai đoạn 1: Bay thẳng lên trong 0.2 giây đầu
            if (flightTime < 0.2f) {
                // Bay thẳng lên với tốc độ không đổi
                velocity.x = 0;
                velocity.y = speed;
                position.x += velocity.x * adjustedDelta;
                position.y += velocity.y * adjustedDelta;
            } else {
                // Giai đoạn 2: Sau 0.2s, bắt đầu hướng về mục tiêu với quỹ đạo cong
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
                    updateEffects(adjustedDelta);

                    // Kiểm tra va chạm với bán kính thay đổi theo tốc độ
                    float collisionRadius = 20 + (currentSpeed / speed) * 10;
                    if (distToTarget < collisionRadius) {
                        if (targetEnemy != null) {
                            targetEnemy.hit(damage);
                        } else if (targetObstacle != null) {
                            targetObstacle.hit(damage);
                        }
                        // Kích hoạt hiệu ứng nổ
                        isEffectActive = true;
                        effectTimer = 0f;
                        effectPosition.set(position.x, position.y);
                        active = false;
                    }

                    // Hủy đạn nếu đi quá xa mục tiêu
                    if (distToTarget > 1000) {
                        active = false;
                    }
                } else {
                    // Không có mục tiêu, bay thẳng tiếp
                    position.x += velocity.x * adjustedDelta;
                    position.y += velocity.y * adjustedDelta;
                }
            }
        }
        // Nếu hiệu ứng nổ đang hoạt động, cập nhật thời gian
        if (isEffectActive) {
            effectTimer += adjustedDelta;
            if (effectAnimation.isAnimationFinished(effectTimer)) {
                isEffectActive = false;
            }
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
            case FIRE_TOWER:
                effectRotation = rotation + 180;
                effectAlpha = 0.8f + MathUtils.sin(effectTimer * 10) * 0.2f;
                break;
            case STONE_TOWER:
                effectRotation += adjustedDelta * 360;
                effectAlpha = 0.6f + MathUtils.cos(effectTimer * 5) * 0.4f;
                break;
            case BIGLAND_TOWER:
                effectRotation = rotation;
                effectAlpha = 0.5f + MathUtils.sin(effectTimer * 15) * 0.5f;
                break;
            case LAND_TOWER:
                effectRotation = rotation + 90;
                effectAlpha = 0.7f + MathUtils.cos(effectTimer * 8) * 0.3f;
                break;
        }
    }

    // Vẽ đạn
    public void render(SpriteBatch batch) {
        // Vẽ hiệu ứng nổ nếu đang hoạt động
        if (isEffectActive) {
            batch.setColor(1, 1, 1, effectAlpha);
            TextureRegion currentFrame = effectAnimation.getKeyFrame(effectTimer, false);
            batch.draw(
                currentFrame,
                effectPosition.x - (float) currentFrame.getRegionWidth() /2, effectPosition.y - (float) currentFrame.getRegionHeight() /2,
                (float) currentFrame.getRegionWidth() /2, (float) currentFrame.getRegionHeight() /2,
                currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                effectScale, effectScale,
                effectRotation
            );
        }
        // Vẽ đạn khi đang bay
        if (active) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(
                texture,
                position.x - (float) texture.getWidth() /2, position.y - (float) texture.getHeight() /2,
                (float) texture.getWidth() /2, (float) texture.getHeight() /2,
                texture.getWidth(), texture.getHeight(),
                1, 1,
                rotation - 90,
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false
            );
        }
    }

    public boolean isActive() {
        return active;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
        if (effectFrames != null) {
            for (TextureRegion region : effectFrames) {
                if (region.getTexture() != null) {
                    region.getTexture().dispose();
                }
            }
        }
    }
}
