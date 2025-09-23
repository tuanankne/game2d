package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

// Lớp quản lý tháp phòng thủ
public class Tower {
    // Các loại tháp
    public enum Type {
        CANNON,     // Pháo (nòng súng xoay)
        MISSILE,    // Tên lửa (bệ phóng xoay)
        LASER       // Laser (đầu laser xoay)
    }

    private Type type;                  // Loại tháp
    private Vector2 position;           // Vị trí tháp
    private float rotation;             // Góc xoay của phần động
    private Texture baseTexture;        // Texture phần đế tháp
    private Texture turretTexture;      // Texture phần động (nòng súng, bệ phóng...)
    private float tileSize;             // Kích thước một ô tile
    private Array<Projectile> projectiles; // Danh sách đạn
    private float shootTimer;           // Thời gian giữa các lần bắn
    private float shootDelay;           // Độ trễ giữa các lần bắn
    private Enemy currentTarget;        // Mục tiêu hiện tại
    private float range;                // Tầm bắn của tháp
    private boolean showRange;           // Hiển thị vùng tầm bắn
    private Enemy manualTarget;          // Mục tiêu được chọn thủ công
    private ShapeRenderer shapeRenderer; // Để vẽ hình tròn tầm bắn

    public Tower(Type type, float tileX, float tileY, float tileSize) {
        this.type = type;
        // Chuyển đổi từ tọa độ tile sang tọa độ pixel
        this.position = new Vector2(tileX * tileSize, tileY * tileSize);
        this.tileSize = tileSize;
        this.rotation = 0;
        this.projectiles = new Array<>();
        this.shootTimer = 0;
        this.showRange = false;
        this.manualTarget = null;
        this.shapeRenderer = new ShapeRenderer();

        // Thiết lập thông số dựa vào loại tháp
        switch (type) {
            case CANNON:
                shootDelay = 0.5f;  // Bắn 2 viên/giây
                range = 450f;       // Tầm bắn 200 pixel
                break;
            case MISSILE:
                shootDelay = 2.0f;  // Bắn 1 viên/2 giây
                range = 500f;       // Tầm bắn 300 pixel
                break;
            case LASER:
                shootDelay = 0.1f;  // Bắn 10 viên/giây
                range = 400f;       // Tầm bắn 150 pixel
                break;
        }

        loadTextures();
    }

    // Tải texture cho từng loại tháp
    private void loadTextures() {
        switch (type) {
            case CANNON:
                baseTexture = new Texture("map1/towerDefense_tile180.png");    // Đế pháo
                turretTexture = new Texture("map1/towerDefense_tile249.png");  // Nòng pháo
                break;
            case MISSILE:
                baseTexture = new Texture("map1/towerDefense_tile181.png");    // Đế tên lửa
                turretTexture = new Texture("map1/towerDefense_tile206.png");  // Bệ phóng
                break;
            case LASER:
                baseTexture = new Texture("map1/towerDefense_tile182.png");    // Đế laser
                turretTexture = new Texture("map1/towerDefense_tile203.png");  // Đầu laser
                break;
        }
    }

    // Kiểm tra xem enemy có trong tầm bắn không
    public boolean isInRange(Enemy enemy) {
        float towerCenterX = position.x + tileSize/2;
        float towerCenterY = position.y + tileSize/2;

        float dx = enemy.getX() - towerCenterX;
        float dy = enemy.getY() - towerCenterY;
        float distSqr = dx * dx + dy * dy;

        return distSqr <= range * range;
    }

    // Tìm mục tiêu gần nhất trong tầm bắn
    private Enemy findNearestTarget(Array<Enemy> enemies) {
        // Nếu có mục tiêu thủ công và nó vẫn trong tầm bắn, ưu tiên bắn nó
        if (manualTarget != null && manualTarget.isAlive() && isInRange(manualTarget)) {
            return manualTarget;
        }

        Enemy nearest = null;
        float minDistSqr = range * range;

        float towerCenterX = position.x + tileSize/2;
        float towerCenterY = position.y + tileSize/2;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - towerCenterX;
            float dy = enemy.getY() - towerCenterY;
            float distSqr = dx * dx + dy * dy;

            if (distSqr < minDistSqr) {
                minDistSqr = distSqr;
                nearest = enemy;
            }
        }

        return nearest;
    }

    // Cập nhật trạng thái tháp
    public void update(float delta, Array<Enemy> enemies) {
        // Áp dụng tốc độ game
        float adjustedDelta = delta * GameControls.getGameSpeed();
        
        // Cập nhật các đạn đang bay
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(adjustedDelta);
            if (!projectile.isActive()) {
                projectiles.removeIndex(i);
            }
        }

        // Tìm mục tiêu mới nếu chưa có hoặc mục tiêu cũ đã chết
        if (currentTarget == null || !currentTarget.isAlive()) {
            currentTarget = findNearestTarget(enemies);
        }

        // Nếu có mục tiêu, xoay tháp và bắn
        if (currentTarget != null) {
            // Tính góc xoay để hướng về mục tiêu
            float dx = currentTarget.getX() - (position.x + tileSize/2);
            float dy = currentTarget.getY() - (position.y + tileSize/2);
            
            // Tính góc mới
            float targetRotation = (float) Math.toDegrees(Math.atan2(dy, dx));
            
            // Xoay tháp với tốc độ được điều chỉnh theo tốc độ game
            float rotationSpeed = 180f * adjustedDelta; // 180 độ/giây
            float angleDiff = ((targetRotation - rotation + 540) % 360) - 180; // Chuẩn hóa góc
            if (Math.abs(angleDiff) > 0.1f) {
                rotation += Math.signum(angleDiff) * Math.min(Math.abs(angleDiff), rotationSpeed);
            }

            // Cập nhật thời gian bắn với tốc độ game
            shootTimer += adjustedDelta;
            if (shootTimer >= shootDelay) {
                shoot();
                shootTimer = 0;
            }
        }
    }

    // Bắn đạn
    private void shoot() {
        if (currentTarget == null) return;

        float radians = rotation * MathUtils.degreesToRadians;
        float spawnX, spawnY;
        float centerX = position.x + tileSize/2;
        float centerY = position.y + tileSize/2;

        switch (type) {
            case CANNON:
                // Đối với pháo: đạn bắn ra từ đầu nòng súng
                float barrelLength = tileSize * 0.8f; // Độ dài nòng súng (80% kích thước ô)
                spawnX = centerX + MathUtils.cos(radians) * barrelLength;
                spawnY = centerY + MathUtils.sin(radians) * barrelLength;
                break;

            case MISSILE:
                // Đối với tên lửa: đạn bắn ra từ giữa bệ phóng
                float launchOffset = tileSize * 0.5f; // Khoảng cách từ tâm (50% kích thước ô)
                spawnX = centerX + MathUtils.cos(radians) * launchOffset;
                spawnY = centerY + MathUtils.sin(radians) * launchOffset;
                break;

            case LASER:
                // Đối với laser: tia bắn ra từ đầu súng
                float laserOffset = tileSize * 0.6f; // Độ dài đầu súng laser (60% kích thước ô)
                spawnX = centerX + MathUtils.cos(radians) * laserOffset;
                spawnY = centerY + MathUtils.sin(radians) * laserOffset;
                break;

            default:
                spawnX = centerX;
                spawnY = centerY;
                break;
        }

        // Tạo đạn mới tại vị trí đã tính
        Projectile projectile = new Projectile(type, spawnX, spawnY);
        projectile.fire(currentTarget, rotation);
        projectiles.add(projectile);
    }

    // Vẽ tháp
    public void render(SpriteBatch batch) {
        // Kết thúc SpriteBatch để vẽ hình tròn
        batch.end();

        // Vẽ vùng tầm bắn nếu được yêu cầu
        if (showRange) {
            // Sử dụng cùng projection matrix với SpriteBatch
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // Vẽ vòng tròn tầm bắn
            float centerX = position.x + tileSize/2;
            float centerY = position.y + tileSize/2;
            int segments = 360; // Số đoạn để vẽ hình tròn
            float angleStep = 360f / segments;

            // Vẽ vòng tròn chính
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 0.3f);
            for (int i = 0; i < segments; i++) {
                float angle1 = i * angleStep;
                float angle2 = (i + 1) * angleStep;

                float x1 = centerX + MathUtils.cosDeg(angle1) * range;
                float y1 = centerY + MathUtils.sinDeg(angle1) * range;
                float x2 = centerX + MathUtils.cosDeg(angle2) * range;
                float y2 = centerY + MathUtils.sinDeg(angle2) * range;

                shapeRenderer.line(x1, y1, x2, y2);
            }

            // Vẽ vòng tròn phụ để tạo hiệu ứng đẹp hơn
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 0.1f);
            for (int i = 0; i < segments; i++) {
                float angle1 = i * angleStep;
                float angle2 = (i + 1) * angleStep;

                float x1 = centerX + MathUtils.cosDeg(angle1) * (range - 5);
                float y1 = centerY + MathUtils.sinDeg(angle1) * (range - 5);
                float x2 = centerX + MathUtils.cosDeg(angle2) * (range - 5);
                float y2 = centerY + MathUtils.sinDeg(angle2) * (range - 5);

                shapeRenderer.line(x1, y1, x2, y2);
            }

            shapeRenderer.end();
        }

        // Bắt đầu lại SpriteBatch để vẽ tháp
        batch.begin();

        // Vẽ phần đế (không xoay)
        batch.draw(
            baseTexture,
            position.x, position.y,
            tileSize, tileSize
        );

        // Vẽ phần động (có xoay)
        batch.draw(
            turretTexture,
            position.x, position.y,           // Vị trí
            tileSize/2, tileSize/2,          // Điểm xoay ở giữa
            tileSize, tileSize,              // Kích thước
            1, 1,                            // Scale
            rotation,                        // Góc xoay
            0, 0,                            // Source position
            turretTexture.getWidth(),        // Source size
            turretTexture.getHeight(),
            false, false                     // Flip
        );

        // Vẽ các đạn
        for (Projectile projectile : projectiles) {
            projectile.render(batch);
        }
    }

    // Thiết lập mục tiêu thủ công
    public void setManualTarget(Enemy target) {
        if (target != null && isInRange(target)) {
            this.manualTarget = target;
        }
    }

    // Bật/tắt hiển thị vùng tầm bắn
    public void toggleRange() {
        this.showRange = !this.showRange;
    }

    // Kiểm tra xem một điểm có nằm trong vùng tầm bắn không
    public boolean isPointInRange(float x, float y) {
        float centerX = position.x + tileSize/2;
        float centerY = position.y + tileSize/2;
        float dx = x - centerX;
        float dy = y - centerY;
        return dx * dx + dy * dy <= range * range;
    }

    // Lấy vị trí của tháp
    public Vector2 getPosition() {
        return position;
    }

    // Lấy kích thước ô
    public float getTileSize() {
        return tileSize;
    }

    // Giải phóng tài nguyên
    public void dispose() {
        if (baseTexture != null) baseTexture.dispose();
        if (turretTexture != null) turretTexture.dispose();
        for (Projectile projectile : projectiles) {
            projectile.dispose();
        }
    }
}
