package io.github.some_example_name.entities.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.utils.GameControls;
import io.github.some_example_name.utils.Currency;
import io.github.some_example_name.ui.HealthBarRenderer;
import io.github.some_example_name.utils.PlayerHealth;

@SuppressWarnings("DefaultLocale")
public class Enemy {
    public enum Type {
        NORMAL,
        FAST,
        TANK,
        BOSS
    }

    public enum DirectionState {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    private Vector2 position;           // Vị trí hiện tại
    private Vector2 velocity;          // Vector vận tốc hiện tại
    private float speed;               // Tốc độ di chuyển
    private Array<Vector2> path;       // Đường đi
    private int currentPathIndex;      // Vị trí hiện tại trên đường đi
    private Texture texture;           // Texture của quái
    private int health;                // Máu hiện tại của quái
    private int maxHealth;             // Máu tối đa của quái
    private boolean alive;             // Trạng thái sống/chết
    private float lookAheadDistance;   // Khoảng cách nhìn trước để dự đoán cua
    private Vector2 targetPoint;       // Điểm đích đến hiện tại
    private Vector2 nextPoint;         // Điểm tiếp theo để dự đoán cua
    private static final float HEALTHBAR_WIDTH = 40f;  // Chiều rộng thanh máu
    private static final float HEALTHBAR_HEIGHT = 4f;  // Chiều cao thanh máu
    private static final float HEALTHBAR_Y_OFFSET = 30f; // Khoảng cách từ quái đến thanh máu

    public Enemy(float x, float y, Type type, float health, float speed) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        targetPoint = new Vector2();
        nextPoint = new Vector2();
        currentPathIndex = 0;
        alive = true;
        this.speed = speed;
        this.maxHealth = (int)health;
        this.health = maxHealth;
        this.type = type;  // Lưu loại quái

        // Thiết lập khoảng cách nhìn trước dựa vào loại quái
        switch (type) {
            case FAST:
                lookAheadDistance = 100f; // Quái nhanh nhìn xa hơn để cua mượt
                break;
            case TANK:
                lookAheadDistance = 50f;  // Quái tank quay chậm nên nhìn gần
                break;
            case BOSS:
                lookAheadDistance = 60f;  // Boss có kích thước lớn nên nhìn vừa phải
                break;
            default:
                lookAheadDistance = 80f;  // Quái thường nhìn trước vừa phải
                break;
        }

        // Khởi tạo animation variables
        animationTimer = 0f;
        currentFrameIndex = 0;
        currentDirection = DirectionState.RIGHT; // Mặc định di chuyển sang phải
        lastDirectionChangeTime = 0f;
        totalTime = 0f;

        // Khởi tạo texture arrays cho mỗi loại quái
        textures = new Array<Texture>();
        deathTextures = new Array<Texture>();

        // Khởi tạo death animation variables
        isPlayingDeathAnimation = false;
        deathAnimationTimer = 0f;
        deathFrameIndex = 0;
        deathAnimationCompleted = false;

        // Thiết lập texture dựa vào loại quái
        switch (type) {
            case NORMAL:
                // Texture cho enemy normal
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/normal/run/2_enemies_1_run_0%02d.png", i);
                    textures.add(new Texture(fileName));
                }
                // Death textures cho enemy normal
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/normal/die/2_enemies_1_die_0%02d.png", i);
                    deathTextures.add(new Texture(fileName));
                }
                texture = textures.get(0); // Texture mặc định
                break;

            case FAST:
                // Texture cho enemy fast
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/fast/run/7_enemies_1_run_0%02d.png", i);
                    textures.add(new Texture(fileName));
                }
                // Death textures cho enemy fast
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/fast/die/7_enemies_1_die_0%02d.png", i);
                    deathTextures.add(new Texture(fileName));
                }
                texture = textures.get(0); // Texture mặc định
                break;

            case TANK:
                // Texture cho enemy tank
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/tank/run/3_enemies_1_run_0%02d.png", i);
                    textures.add(new Texture(fileName));
                }
                // Death textures cho enemy tank
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/tank/die/3_enemies_1_die_0%02d.png", i);
                    deathTextures.add(new Texture(fileName));
                }
                texture = textures.get(0); // Texture mặc định
                break;

            case BOSS:
                // Texture cho enemy boss
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/boss/run/10_enemies_1_run_0%02d.png", i);
                    textures.add(new Texture(fileName));
                }
                // Death textures cho enemy tank
                for (int i = 0; i <= 19; i++) {
                    String fileName = String.format("enemy/boss/die/10_enemies_1_die_0%02d.png", i);
                    deathTextures.add(new Texture(fileName));
                }
                texture = textures.get(0); // Texture mặc định
                break;
        }
    }

    public void setPath(Array<Vector2> path) {
        this.path = path;
    }

    // Cập nhật death animation
    private void updateDeathAnimation(float delta) {
        deathAnimationTimer += delta;

        if (deathAnimationTimer >= deathAnimationSpeed) {
            deathAnimationTimer = 0f;

            // Chuyển sang frame tiếp theo
            if (deathTextures != null && deathTextures.size > 0) {
                deathFrameIndex++;

                // Kiểm tra xem đã hoàn thành animation chưa
                if (deathFrameIndex >= deathTextures.size) {
                    deathFrameIndex = deathTextures.size - 1; // Giữ ở frame cuối
                    deathAnimationCompleted = true;
                    isPlayingDeathAnimation = false;
                } else {
                    // Cập nhật texture cho frame hiện tại
                    texture = deathTextures.get(deathFrameIndex);
                }
            }
        }
    }

    private float rotation = 0;  // Góc xoay của enemy

    public void update(float delta) {
        if (currentPathIndex >= path.size) return;

        // Áp dụng tốc độ game
        float adjustedDelta = delta * GameControls.getGameSpeed();

        // Nếu đang chơi death animation
        if (isPlayingDeathAnimation && !deathAnimationCompleted) {
            updateDeathAnimation(adjustedDelta);
            return; // Không cập nhật movement khi đang chết
        }

        // Nếu enemy đã chết và hoàn thành death animation
        if (!alive && deathAnimationCompleted) {
            return; // Không cập nhật gì thêm
        }

        // Cập nhật animation bình thường khi enemy còn sống
        totalTime += adjustedDelta;
        animationTimer += adjustedDelta;
        if (animationTimer >= animationSpeed) {
            animationTimer = 0f;

            // Sử dụng mảng texture duy nhất cho tất cả hướng di chuyển
            if (textures != null && textures.size > 0) {
                currentFrameIndex = (currentFrameIndex + 1) % textures.size;
                texture = textures.get(currentFrameIndex);
            }
        }


        // Cập nhật điểm đích và điểm tiếp theo
        updateTargetPoints();

        // Tính toán vector hướng di chuyển mới
        Vector2 desiredVelocity = calculateDesiredVelocity();

        // Áp dụng lực lái để di chuyển mượt mà
        float steeringStrength = getSteeringStrength();
        velocity.x += (desiredVelocity.x - velocity.x) * steeringStrength * adjustedDelta;
        velocity.y += (desiredVelocity.y - velocity.y) * steeringStrength * adjustedDelta;

        // Giới hạn tốc độ tối đa
        float currentSpeed = (float)Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y);
        if (currentSpeed > speed) {
            velocity.x = (velocity.x / currentSpeed) * speed;
            velocity.y = (velocity.y / currentSpeed) * speed;
        }

        // Cập nhật vị trí
        position.x += velocity.x * adjustedDelta;
        position.y += velocity.y * adjustedDelta;

        // Cập nhật góc xoay mượt mà
        float targetRotation;
        if (currentPathIndex >= path.size - 2) {
            // Ở gần cuối, tính góc trực tiếp đến điểm cuối cùng để tránh quay không cần thiết
            Vector2 finalPoint = path.get(path.size - 1);
            targetRotation = (float)Math.toDegrees(Math.atan2(
                finalPoint.y - position.y,
                finalPoint.x - position.x
            ));
        } else {
            // Các điểm khác thì dùng velocity để tính góc
            targetRotation = (float)Math.toDegrees(Math.atan2(velocity.y, velocity.x));
        }

        float angleDiff = targetRotation - rotation;

        // Chuẩn hóa góc về khoảng [-180, 180] và chọn hướng quay ngắn nhất
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        // Áp dụng xoay mượt mà với tốc độ phù hợp
        float rotationSpeed = getRotationSpeed();
        // Giảm tốc độ xoay khi góc nhỏ để tránh rung lắc
        if (Math.abs(angleDiff) < 10) {
            rotationSpeed *= 0.5f;
        }
        rotation += angleDiff * rotationSpeed * adjustedDelta;

        // Kiểm tra đến điểm tiếp theo
        float distanceToTarget = Vector2.dst(position.x, position.y, targetPoint.x, targetPoint.y);
        float arrivalThreshold = (currentPathIndex >= path.size - 1) ? 2f : 1f; // Tăng ngưỡng cho điểm cuối

        if (distanceToTarget < arrivalThreshold) {
            currentPathIndex++;
            if (currentPathIndex >= path.size) {
                PlayerHealth.takeDamage(type);
                alive = false;
                return;
            }
        }
    }

    public void render(SpriteBatch batch) {
        // Render nếu enemy còn sống hoặc đang chơi death animation
        if (!alive && !isPlayingDeathAnimation) return;

        // Đặc biệt cho boss - có thể thêm hiệu ứng scale theo animation
        float scale = 0.3f;
        if (type == Type.BOSS) {
            // Tạo hiệu ứng "pulse" cho boss dựa trên frame hiện tại
//            float pulseEffect = 0.4f + 0.1f * (float)Math.sin(animationTimer * 10f);
            scale = 0.35f;
        }

        // Vẽ quái với góc xoay
        batch.draw(
            texture,
            position.x - texture.getWidth()/2,
            position.y - texture.getHeight()/2,
            texture.getWidth()/2,  // Điểm xoay ở giữa texture
            texture.getHeight()/2,
            texture.getWidth(),
            texture.getHeight(),
            scale, scale,          // Scale (có thể khác nhau cho boss)
            rotation,              // Góc xoay
            0, 0,                 // Source position
            texture.getWidth(),
            texture.getHeight(),
            false, false          // Flip
        );

        // Vẽ thanh máu sử dụng HealthBarRenderer
        float healthRatio = (float)health / maxHealth;
        HealthBarRenderer.renderHealthBar(
            batch,
            position.x - HEALTHBAR_WIDTH/2,
            position.y + HEALTHBAR_Y_OFFSET,
            HEALTHBAR_WIDTH,
            HEALTHBAR_HEIGHT,
            healthRatio
        );
    }

    public void renderDebug(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer) {
        if (!alive || currentPathIndex >= path.size) return;

        // Chỉ vẽ vòng tròn target nếu được nhắm
        if (isTargeted) {
            shapeRenderer.setColor(1, 0, 0, 0.8f);
            shapeRenderer.circle(position.x, position.y, targetCircleRadius);
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

    public boolean hasReachedEnd() {
        return currentPathIndex >= path.size;
    }

    private Type type;  // Thêm biến type
    private boolean isTargeted; // Trạng thái được nhắm
    private float targetCircleRadius = 15f; // Bán kính vòng tròn target

    // Animation variables for all enemy types
    private Array<Texture> textures;        // Mảng texture duy nhất cho enemy
    private Array<Texture> deathTextures;   // Mảng texture cho animation chết
    private float animationTimer;           // Timer cho animation
    private int currentFrameIndex;          // Index của frame hiện tại
    private float animationSpeed = 0.2f;    // Tốc độ animation (giây/frame)
    private DirectionState currentDirection; // Trạng thái hướng di chuyển hiện tại
    private float lastDirectionChangeTime;  // Thời gian lần cuối thay đổi hướng
    private float totalTime;                // Tổng thời gian đã trôi qua

    // Death animation variables
    private boolean isPlayingDeathAnimation; // Trạng thái đang chạy animation chết
    private float deathAnimationTimer;       // Timer cho animation chết
    private int deathFrameIndex;             // Index của frame chết hiện tại
    private float deathAnimationSpeed = 0.15f; // Tốc độ animation chết
    private boolean deathAnimationCompleted;   // Đã hoàn thành animation chết chưa

    // Legacy variables for backward compatibility (will be removed)
    private boolean isMovingVertically;  // Trạng thái di chuyển dọc/ngang

    public void hit(float damage) {
        health -= (int)damage;  // Chuyển damage từ float sang int
        if (health <= 0 && alive) {
            alive = false;
            // Bắt đầu death animation
            isPlayingDeathAnimation = true;
            deathAnimationTimer = 0f;
            deathFrameIndex = 0;
            deathAnimationCompleted = false;
            Currency.addReward(type);  // Thêm tiền thưởng khi quái chết
        }
    }

    public boolean isAlive() {
        return alive;
    }

    // Kiểm tra xem enemy có thể được xóa khỏi danh sách không
    public boolean canBeRemoved() {
        return !alive && deathAnimationCompleted;
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

    // Cập nhật điểm đích cho enemy
    private void updateTargetPoints() {
        if (currentPathIndex >= path.size - 1) {
            // Nếu là điểm cuối, đi thẳng đến đích
            targetPoint.set(path.get(currentPathIndex));
            nextPoint.set(targetPoint);
            return;
        }

        // Lấy điểm hiện tại và điểm tiếp theo
        Vector2 currentWaypoint = path.get(currentPathIndex);
        Vector2 nextWaypoint = path.get(currentPathIndex + 1);

        // Tính khoảng cách đến điểm tiếp theo
        float distanceToNext = Vector2.dst(position.x, position.y, currentWaypoint.x, currentWaypoint.y);
        // Điều chỉnh khoảng cách chuyển điểm dựa vào vị trí trên đường đi
        float switchDistance = (currentPathIndex >= path.size - 2) ? 5f : 25f; // Giảm khoảng cách ở gần cuối

        // Kiểm tra xem có phải điểm cuối không
        boolean isSecondToLastPoint = currentPathIndex >= path.size - 2; // Điểm gần cuối trong path

        // Nếu đủ gần điểm hiện tại và không phải điểm gần cuối, chuyển sang điểm tiếp theo
        if (distanceToNext < switchDistance && !isSecondToLastPoint) {
            currentPathIndex++;
            currentWaypoint = path.get(currentPathIndex);
            // Chỉ lấy điểm tiếp theo nếu không phải điểm cuối
            if (currentPathIndex < path.size - 1) {
                nextWaypoint = path.get(currentPathIndex + 1);
            } else {
                nextWaypoint = currentWaypoint; // Điểm cuối thì giữ nguyên
            }
        }

        // Cập nhật điểm đích
        targetPoint.set(currentWaypoint);
        nextPoint.set(nextWaypoint);
    }

    // Tính khoảng cách điểm điều khiển dựa vào góc giữa các đoạn
    // Tính độ dài điểm điều khiển dựa vào góc
    private float calculateControlLength(float angle) {
        // Chuẩn hóa góc về khoảng [-PI, PI]
        float PI = (float)Math.PI;
        while (angle > PI) angle -= 2 * PI;
        while (angle < -PI) angle += 2 * PI;
        angle = Math.abs(angle);

        // Điều chỉnh độ dài dựa vào góc
        if (angle < PI / 6) { // Góc nhỏ (< 30 độ)
            return 0.1f; // Gần như đi thẳng
        } else if (angle > PI * 2/3) { // Góc lớn (> 120 độ)
            return 0.4f; // Cua rộng
        } else {
            // Nội suy tuyến tính cho các góc trung gian
            return 0.1f + (angle - PI/6) * 0.3f / (PI*2/3 - PI/6);
        }
    }

    // Tính tiến độ trên đường cong (0-1)
    private float calculatePathProgress() {
        if (currentPathIndex >= path.size - 1) return 1;

        Vector2 currentTarget = path.get(currentPathIndex);
        Vector2 nextTarget = path.get(currentPathIndex + 1);

        float totalDist = Vector2.dst(currentTarget.x, currentTarget.y, nextTarget.x, nextTarget.y);
        float currentDist = Vector2.dst(position.x, position.y, currentTarget.x, currentTarget.y);

        return Math.max(0, Math.min(1, currentDist / totalDist));
    }

    // Tính điểm trên đường cong Bézier bậc 3
    private Vector2 evaluateBezierCurve(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float t) {
        float mt = 1 - t;
        float mt2 = mt * mt;
        float mt3 = mt2 * mt;
        float t2 = t * t;
        float t3 = t2 * t;

        return new Vector2(
            mt3 * p0.x + 3 * mt2 * t * p1.x + 3 * mt * t2 * p2.x + t3 * p3.x,
            mt3 * p0.y + 3 * mt2 * t * p1.y + 3 * mt * t2 * p2.y + t3 * p3.y
        );
    }

    // Tính toán vector vận tốc mong muốn
    private Vector2 calculateDesiredVelocity() {
        Vector2 desiredVelocity = new Vector2();

        // Tính vector hướng đến điểm đích
        desiredVelocity.x = targetPoint.x - position.x;
        desiredVelocity.y = targetPoint.y - position.y;

        // Chuẩn hóa vector và nhân với tốc độ
        float len = (float)Math.sqrt(desiredVelocity.x * desiredVelocity.x + desiredVelocity.y * desiredVelocity.y);
        if (len > 0) {
            desiredVelocity.x = (desiredVelocity.x / len) * speed;
            desiredVelocity.y = (desiredVelocity.y / len) * speed;
        }

        return desiredVelocity;
    }

    // Lấy độ mạnh của lực lái dựa vào loại quái
    private float getSteeringStrength() {
        switch (type) {
            case FAST:
                return 4.0f;  // Quái nhanh phản ứng nhanh
            case TANK:
                return 2.0f;  // Quái tank phản ứng chậm
            case BOSS:
                return 2.5f;  // Boss phản ứng vừa phải (lớn và mạnh mẽ)
            default:
                return 3.0f;  // Quái thường phản ứng vừa phải
        }
    }

    // Lấy tốc độ xoay dựa vào loại quái
    private float getRotationSpeed() {
        switch (type) {
            case FAST:
                return 10.0f;  // Quái nhanh xoay nhanh
            case TANK:
                return 3.0f;   // Quái tank xoay chậm
            case BOSS:
                return 4.0f;   // Boss xoay vừa phải (lớn và mạnh mẽ)
            default:
                return 5.0f;   // Quái thường xoay vừa phải
        }
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    // Phương thức phát hiện hướng di chuyển dựa trên velocity
    private DirectionState detectDirection() {
        float absX = Math.abs(velocity.x);
        float absY = Math.abs(velocity.y);

        // Ngưỡng để xác định hướng chính
        float threshold = 0.1f;

        if (absX < threshold && absY < threshold) {
            // Nếu velocity quá nhỏ, giữ nguyên hướng hiện tại
            return currentDirection;
        }

        // Xác định hướng chính dựa trên thành phần lớn hơn
        if (absY > absX) {
            // Di chuyển theo chiều dọc
            return velocity.y > 0 ? DirectionState.UP : DirectionState.DOWN;
        } else {
            // Di chuyển theo chiều ngang
            return velocity.x > 0 ? DirectionState.RIGHT : DirectionState.LEFT;
        }
    }


    // Phương thức để debug trạng thái boss
    public String getBossDirectionInfo() {
        if (type != Type.BOSS) return "Not a boss";

        String direction = isMovingVertically ? "Vertical" : "Horizontal";
        String velocityInfo = String.format("Velocity: (%.1f, %.1f)", velocity.x, velocity.y);
        String frameInfo = String.format("Frame: %d", currentFrameIndex);

        return String.format("Boss Direction: %s | %s | %s", direction, velocityInfo, frameInfo);
    }

    // Phương thức debug cho tất cả loại quái
    public String getDirectionInfo() {
        String direction = currentDirection.toString();
        String velocityInfo = String.format("Velocity: (%.1f, %.1f)", velocity.x, velocity.y);
        String frameInfo = String.format("Frame: %d", currentFrameIndex);

        return String.format("Enemy Direction: %s | %s | %s", direction, velocityInfo, frameInfo);
    }

    public void dispose() {
        // Giải phóng texture arrays
        disposeTextureArray(textures);
        disposeTextureArray(deathTextures);
    }

    // Helper method để dispose texture array
    private void disposeTextureArray(Array<Texture> textureArray) {
        if (textureArray != null) {
            for (Texture tex : textureArray) {
                if (tex != null) {
                    tex.dispose();
                }
            }
            textureArray.clear();
        }
    }
}
