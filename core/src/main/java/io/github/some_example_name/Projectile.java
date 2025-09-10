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
    private Enemy target;       // Mục tiêu đang nhắm
    private Tower.Type projectileType; // Loại đạn (để xác định hiệu ứng)
    private float turnSpeed; // Tốc độ xoay của đạn (độ/giây)
    private float maxTurnSpeed = 180f; // Tốc độ xoay tối đa (độ/giây)
    private float acceleration = 200f; // Gia tốc khi đuổi theo mục tiêu
    private float effectRotation; // Góc xoay của hiệu ứng
    private float effectScale = 1f; // Tỷ lệ kích thước hiệu ứng
    private float effectAlpha = 1f; // Độ trong suốt của hiệu ứng
    private float effectTimer = 0f; // Thời gian hiệu ứng

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
    public void fire(Enemy target, float rotation) {
        this.target = target;
        this.rotation = rotation;
        this.active = true;

        // Khởi tạo vận tốc ban đầu theo hướng bắn
        float radians = rotation * MathUtils.degreesToRadians;
        velocity.x = MathUtils.cos(radians) * speed;
        velocity.y = MathUtils.sin(radians) * speed;
    }

    // Cập nhật vị trí đạn
    public void update(float delta) {
        if (!active) return;

        if (target != null && target.isAlive()) {
            // Tính toán hướng đến mục tiêu
            float targetX = target.getX();
            float targetY = target.getY();
            float dx = targetX - position.x;
            float dy = targetY - position.y;

            // Tính góc đến mục tiêu
            float targetAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            // Điều chỉnh góc xoay của đạn
            float angleDiff = ((targetAngle - rotation + 540) % 360) - 180; // Chuẩn hóa góc
            float turnAmount = Math.min(Math.abs(angleDiff), turnSpeed * delta) * Math.signum(angleDiff);
            rotation = (rotation + turnAmount) % 360;

            // Cập nhật vector vận tốc dựa trên góc xoay mới
            float radians = rotation * MathUtils.degreesToRadians;
            float targetSpeed = speed;

            // Tăng tốc nếu đang hướng về mục tiêu
            if (Math.abs(angleDiff) < 45) {
                targetSpeed += acceleration * delta;
            }

            // Cập nhật vận tốc
            velocity.x = MathUtils.cos(radians) * targetSpeed;
            velocity.y = MathUtils.sin(radians) * targetSpeed;

            // Di chuyển đạn
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;

            // Cập nhật hiệu ứng
            effectTimer += delta;

            // Cập nhật hiệu ứng dựa vào loại đạn
            switch (projectileType) {
                case MISSILE:
                    // Hiệu ứng lửa phía sau tên lửa
                    effectRotation = rotation + 180; // Ngược hướng với tên lửa
                    effectAlpha = 0.8f + MathUtils.sin(effectTimer * 10) * 0.2f; // Nhấp nháy
                    break;
                case CANNON:
                    // Hiệu ứng nổ xoay tròn
                    effectRotation += delta * 360; // Xoay 360 độ/giây
                    effectAlpha = 0.6f + MathUtils.cos(effectTimer * 5) * 0.4f;
                    break;
                case LASER:
                    // Hiệu ứng laser nhấp nháy
                    effectRotation = rotation;
                    effectAlpha = 0.5f + MathUtils.sin(effectTimer * 15) * 0.5f;
                    break;
            }

            // Kiểm tra va chạm
            float distSqr = dx * dx + dy * dy;
            if (distSqr < 20 * 20) { // Bán kính va chạm 20 pixel
                target.hit(); // Gây sát thương cho quái
                active = false;
            }
        } else {
            active = false;
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
