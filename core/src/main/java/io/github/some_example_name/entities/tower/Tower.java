package io.github.some_example_name.entities.tower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.entities.enemy.Enemy;
import io.github.some_example_name.utils.Currency;
import io.github.some_example_name.utils.GameControls;
import io.github.some_example_name.entities.obstacle.Obstacle;

// Lớp quản lý tháp phòng thủ với animation
public class Tower {
    // Sử dụng TowerType enum từ TowerType.java thay vì enum cũ

    private TowerType type;             // Loại tháp (sử dụng TowerType enum)
    private Vector2 position;           // Vị trí tháp
    private float rotation;             // Góc xoay của phần động
    private int level = 1;              // Cấp độ hiện tại của tháp
    private static final int MAX_LEVEL = 3; // Cấp độ tối đa (theo TowerType)
    
    // Animation variables
    private AnimatedTower animatedTower; // Instance của AnimatedTower để xử lý animation
    private float tileSize;             // Kích thước một ô tile
    private Array<Projectile> projectiles; // Danh sách đạn
    private float shootTimer;           // Thời gian giữa các lần bắn
    private float shootDelay;           // Độ trễ giữa các lần bắn
    private float damage;               // Sát thương của tháp
    private Enemy currentTarget;        // Mục tiêu Enemy hiện tại
    private Obstacle currentObstacle;    // Mục tiêu Obstacle hiện tại
    private float range;                // Tầm bắn của tháp
    private boolean showRange;           // Hiển thị vùng tầm bắn
    private Enemy manualTarget;          // Enemy được chọn thủ công
    private Obstacle manualObstacle;     // Obstacle được chọn thủ công
    private ShapeRenderer shapeRenderer; // Để vẽ hình tròn tầm bắn

    public Tower(TowerType type, float tileX, float tileY, float tileSize) {
        this.type = type;
        // Chuyển đổi từ tọa độ tile sang tọa độ pixel
        this.position = new Vector2(tileX * tileSize, tileY * tileSize);
        this.tileSize = tileSize;
        this.rotation = 0;
        
        // Khởi tạo AnimatedTower
        this.animatedTower = new AnimatedTower(this.position.x, this.position.y, type);
        // Set tham chiếu để AnimatedTower có thể gây damage
        this.animatedTower.setTowerRef(this);
        this.projectiles = new Array<>();
        this.shootTimer = 0;
        this.showRange = false;
        this.manualTarget = null;
        this.shapeRenderer = new ShapeRenderer();

        // Thiết lập thông số dựa vào loại tháp mới
        switch (type) {
            case STONE_TOWER:
                shootDelay = 0.8f;  // Bắn 1.25 viên/giây
                range = 400f;       // Tầm bắn 400 pixel
                damage = 40f;       // Sát thương trung bình
                break;
            case FIRE_TOWER:
                shootDelay = 0.5f;  // Bắn 2 viên/giây
                range = 350f;       // Tầm bắn 350 pixel
                damage = 25f;       // Sát thương thấp nhưng bắn nhanh
                break;
            case BIGLAND_TOWER:
                shootDelay = 2.5f;  // Bắn 1 viên/2.5 giây
                range = 550f;       // Tầm bắn 550 pixel
                damage = 120f;      // Sát thương cao
                break;
            case LAND_TOWER:
                shootDelay = 1.0f;  // Bắn 1 viên/giây
                range = 450f;       // Tầm bắn 450 pixel
                damage = 60f;       // Sát thương trung bình cao
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

    public boolean isInRange(Obstacle obstacle) {
        float towerCenterX = position.x + tileSize/2;
        float towerCenterY = position.y + tileSize/2;

        float obstacleX = obstacle.getX() + obstacle.getWidth()/2;
        float obstacleY = obstacle.getY() + obstacle.getHeight()/2;

        float dx = obstacleX - towerCenterX;
        float dy = obstacleY - towerCenterY;
        float distSqr = dx * dx + dy * dy;

        return distSqr <= range * range;
    }

    // Tìm mục tiêu gần nhất trong tầm bắn
    @SuppressWarnings("DefaultLocale")
    private Enemy findNearestTarget(Array<Enemy> enemies) {
        float towerCenterX = position.x + tileSize/2;
        float towerCenterY = position.y + tileSize/2;

        // Nếu có mục tiêu thủ công và nó vẫn trong tầm bắn, ưu tiên bắn nó
        if (manualTarget != null && manualTarget.isAlive() && isInRange(manualTarget)) {
            // Vẫn đếm số enemy trong tầm để thông báo
            int enemiesInRange = countEnemiesInRange(enemies, towerCenterX, towerCenterY);
            Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) has manual target and %d other enemies in range",
                position.x, position.y, enemiesInRange));
            return manualTarget;
        }

        // Nếu có mục tiêu obstacle thủ công, không tìm enemy mới
        if (manualObstacle != null && !manualObstacle.isDestroyed() && isInRange(manualObstacle)) {
            int enemiesInRange = countEnemiesInRange(enemies, towerCenterX, towerCenterY);
            Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) targeting obstacle with %d enemies in range",
                position.x, position.y, enemiesInRange));
            return null;
        }

        // Tạo danh sách enemies trong tầm bắn
        Array<Enemy> inRangeEnemies = new Array<>();
        Array<Enemy> nearbyEnemies = new Array<>(); // Enemies đang tiến gần tầm bắn

//        Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) scanning for targets...",
//            position.x, position.y));

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - towerCenterX;
            float dy = enemy.getY() - towerCenterY;
            float distSqr = dx * dx + dy * dy;
            float dist = (float)Math.sqrt(distSqr);

            if (distSqr <= range * range) {
                inRangeEnemies.add(enemy);
                Gdx.app.log("Tower", String.format("Found enemy in range at (%.1f,%.1f), distance: %.1f",
                    enemy.getX(), enemy.getY(), dist));
            } else if (distSqr <= (range + 100) * (range + 100)) {
                // Theo dõi cả enemies trong phạm vi +100 đơn vị để cảnh báo sớm
                nearbyEnemies.add(enemy);
            }
        }

        // Log thông tin tổng quan về enemies
        if (inRangeEnemies.size > 0 || nearbyEnemies.size > 0) {
            Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) status: %d enemies in range, %d enemies approaching",
                position.x, position.y, inRangeEnemies.size, nearbyEnemies.size));
        }

        // Nếu có enemies trong tầm, chọn ngẫu nhiên một enemy
        if (inRangeEnemies.size > 0) {
            int randomIndex = MathUtils.random(inRangeEnemies.size - 1);
            Enemy target = inRangeEnemies.get(randomIndex);
            // Gdx.app.log("Tower", String.format("Auto targeting enemy at (%.1f,%.1f) [%d/%d in range]",
            //     target.getX(), target.getY(), inRangeEnemies.indexOf(target, true) + 1, inRangeEnemies.size));

            // Reset timer để bắn ngay lập tức
            shootTimer = shootDelay;
            return target;
        }

        return null;
    }

    // Đếm số enemy trong tầm bắn
    private int countEnemiesInRange(Array<Enemy> enemies, float centerX, float centerY) {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            float dx = enemy.getX() - centerX;
            float dy = enemy.getY() - centerY;
            if (dx * dx + dy * dy <= range * range) {
                count++;
            }
        }
        return count;
    }

    // Cập nhật trạng thái tháp
    @SuppressWarnings("DefaultLocale")
    public void update(float delta, Array<Enemy> enemies) {
        // Áp dụng tốc độ game
        float adjustedDelta = delta * GameControls.getGameSpeed();
        
        // Cập nhật animation của tháp (bao gồm cả đạn bay theo vòng cung)
        animatedTower.update(adjustedDelta);

        // Cập nhật projectiles thật
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(adjustedDelta);
            if (!p.isActive()) {
                projectiles.removeIndex(i);
            }
        }

        // Kiểm tra và cập nhật mục tiêu
        Enemy targetEnemy = null;
        Obstacle targetObstacle = null;
        float targetX = 0, targetY = 0;
        boolean hasTarget = false;

        // Ưu tiên manualObstacle nếu còn tồn tại và trong tầm
        if (manualObstacle != null && !manualObstacle.isDestroyed() && isInRange(manualObstacle)) {
            targetObstacle = manualObstacle;
            targetX = manualObstacle.getX() + manualObstacle.getWidth()/2;
            targetY = manualObstacle.getY() + manualObstacle.getHeight()/2;
            hasTarget = true;
            // Gdx.app.log("Tower", String.format("Using manual obstacle target %s", manualObstacle.getType()));
        }
        // Tiếp theo ưu tiên manualTarget nếu còn sống và trong tầm
        else if (manualTarget != null && manualTarget.isAlive() && isInRange(manualTarget)) {
            targetEnemy = manualTarget;
            targetX = manualTarget.getX();
            targetY = manualTarget.getY();
            hasTarget = true;
            Gdx.app.log("Tower", String.format("Using manual enemy target at (%.1f,%.1f)",
                targetX, targetY));
        }
        // Nếu không có manual target hoặc manual target không hợp lệ
        else {
            // Xóa manual targets không hợp lệ
            if (manualObstacle != null) {
                Gdx.app.log("Tower", "Manual obstacle target no longer valid");
                manualObstacle = null;
            }
            if (manualTarget != null) {
                Gdx.app.log("Tower", String.format("Manual enemy target no longer valid at (%.1f,%.1f)",
                    manualTarget.getX(), manualTarget.getY()));
                manualTarget = null;
            }

            // Quét liên tục để phát hiện enemy mới trong tầm
            Array<Enemy> inRangeEnemies = new Array<>();
            float towerCenterX = position.x + tileSize/2;
            float towerCenterY = position.y + tileSize/2;

            // Tìm tất cả enemies trong tầm
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                float dx = enemy.getX() - towerCenterX;
                float dy = enemy.getY() - towerCenterY;
                float distSqr = dx * dx + dy * dy;
                if (distSqr <= range * range) {
                    inRangeEnemies.add(enemy);
                }
            }

            // Nếu có enemies trong tầm
            if (inRangeEnemies.size > 0) {
                // Nếu chưa có target hoặc target hiện tại không hợp lệ
                if (currentTarget == null || !currentTarget.isAlive() || !isInRange(currentTarget)) {
                    // Chọn enemy gần nhất làm mục tiêu mới
                    Enemy nearestEnemy = null;
                    float minDistSqr = range * range;

                    for (Enemy enemy : inRangeEnemies) {
                        float dx = enemy.getX() - towerCenterX;
                        float dy = enemy.getY() - towerCenterY;
                        float distSqr = dx * dx + dy * dy;
                        if (distSqr < minDistSqr) {
                            minDistSqr = distSqr;
                            nearestEnemy = enemy;
                        }
                    }

                    if (nearestEnemy != null) {
                        // if (currentTarget != nearestEnemy) {
                        //     Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) detected new target at (%.1f,%.1f) [%d enemies in range]",
                        //         position.x, position.y, nearestEnemy.getX(), nearestEnemy.getY(), inRangeEnemies.size));
                        // }
                        currentTarget = nearestEnemy;
                        targetEnemy = currentTarget;
                        targetX = currentTarget.getX();
                        targetY = currentTarget.getY();
                        hasTarget = true;
                        shootTimer = shootDelay; // Reset timer để bắn ngay
                    }
                } else {
                    // Tiếp tục theo dõi target hiện tại
                    targetEnemy = currentTarget;
                    targetX = currentTarget.getX();
                    targetY = currentTarget.getY();
                    hasTarget = true;
                }
            } else {
                if (currentTarget != null) {
                    // Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) lost target - no enemies in range",
                    //     position.x, position.y));
                    currentTarget = null;
                }
            }
        }

        // Nếu có mục tiêu hợp lệ, xoay tháp và bắn
        if (hasTarget) {
            // Tính góc xoay để hướng về mục tiêu
            float dx = targetX - (position.x + tileSize/2);
            float dy = targetY - (position.y + tileSize/2);

            // Tính góc mới
            float targetRotation = (float) Math.toDegrees(Math.atan2(dy, dx));

            // Chuẩn hóa góc mục tiêu và góc hiện tại về khoảng [-180, 180]
            targetRotation = ((targetRotation % 360) + 360) % 360;
            if (targetRotation > 180) targetRotation -= 360;

            float currentRotation = ((rotation % 360) + 360) % 360;
            if (currentRotation > 180) currentRotation -= 360;

            // Tính góc chênh lệch ngắn nhất
            float angleDiff = targetRotation - currentRotation;
            if (angleDiff > 180) angleDiff -= 360;
            if (angleDiff < -180) angleDiff += 360;

            // Xoay tháp với tốc độ được điều chỉnh theo tốc độ game
            float baseRotationSpeed = 270f; // Tốc độ xoay cơ bản (độ/giây)
            float rotationSpeed = baseRotationSpeed * adjustedDelta;

            // Giới hạn tốc độ xoay khi gần đến đích
            if (Math.abs(angleDiff) < rotationSpeed) {
                rotation = targetRotation;
            } else {
                rotation += Math.signum(angleDiff) * rotationSpeed;
            }

            // Log thông tin xoay nếu góc chênh lệch đáng kể
            // if (Math.abs(angleDiff) > 1f) {
            //     Gdx.app.log("Tower", String.format("Rotating tower from %.1f to %.1f (diff: %.1f)",
            //         currentRotation, targetRotation, angleDiff));
            // }

            // Chỉ bắn khi đã xoay gần đúng hướng (sai số < 5 độ)
            boolean canShoot = Math.abs(angleDiff) < 5f;

            // Cập nhật thời gian bắn với tốc độ game
            shootTimer += adjustedDelta;
            if (canShoot && shootTimer >= shootDelay) {
                if (targetEnemy != null) {
                    currentTarget = targetEnemy;
                    // Sử dụng hệ thống đạn mới thay vì tạo Projectile cũ
                    shootAtEnemyNew(targetEnemy);
                } else if (targetObstacle != null) {
                    currentObstacle = targetObstacle;
                    // Sử dụng hệ thống đạn mới thay vì tạo Projectile cũ
                    shootAtObstacleNew(targetObstacle);
                }
                // Trigger fire animation
                animatedTower.fire();
                shootTimer = 0;
            }
        }
    }

    // Hệ thống đạn mới - sử dụng AnimatedTower
    private void shootAtEnemyNew(Enemy target) {
        if (target == null) return;
        
        // Dự đoán vị trí enemy sau một khoảng thời gian
        float predictTime = 0.5f; // Dự đoán 0.5 giây
        float predictedX = target.getX() + target.getVelocity().x * predictTime;
        float predictedY = target.getY() + target.getVelocity().y * predictTime;
        
        // Set target position cho projectile (sẽ bắn ra khi lên vị trí cao nhất)
        animatedTower.setTargetPosition(predictedX, predictedY);
    }
    
    private void shootAtObstacleNew(Obstacle obstacle) {
        if (obstacle == null) return;
        
        // Vị trí mục tiêu là center của obstacle
        float targetX = obstacle.getX() + obstacle.getWidth() / 2;
        float targetY = obstacle.getY() + obstacle.getHeight() / 2;
        
        // Set target position cho projectile (sẽ bắn ra khi lên vị trí cao nhất)
        animatedTower.setTargetPosition(targetX, targetY);
    }

    // Bắn đạn (method cũ - giữ lại để tham khảo)
    private void shootAtEnemy(Enemy target) {
        if (target == null) return;

        float radians = rotation * MathUtils.degreesToRadians;
        float spawnX, spawnY;
        float centerX = position.x + tileSize/2;
        float centerY = position.y + tileSize/2;

        switch (type) {
            case STONE_TOWER:
                // Đối với Stone Tower: đạn bắn ra từ đầu súng
                float stoneOffset = tileSize * 0.7f;
                spawnX = centerX + MathUtils.cos(radians) * stoneOffset;
                spawnY = centerY + MathUtils.sin(radians) * stoneOffset;
                break;

            case FIRE_TOWER:
                // Đối với Fire Tower: đạn bắn ra từ giữa tháp
                float fireOffset = tileSize * 0.5f;
                spawnX = centerX + MathUtils.cos(radians) * fireOffset;
                spawnY = centerY + MathUtils.sin(radians) * fireOffset;
                break;

            case BIGLAND_TOWER:
                // Đối với BigLand Tower: đạn bắn ra từ đầu súng
                float bigLandOffset = tileSize * 0.8f;
                spawnX = centerX + MathUtils.cos(radians) * bigLandOffset;
                spawnY = centerY + MathUtils.sin(radians) * bigLandOffset;
                break;

            case LAND_TOWER:
                // Đối với Land Tower: đạn bắn ra từ đầu súng
                float landOffset = tileSize * 0.6f;
                spawnX = centerX + MathUtils.cos(radians) * landOffset;
                spawnY = centerY + MathUtils.sin(radians) * landOffset;
                break;

            default:
                spawnX = centerX;
                spawnY = centerY;
                break;
        }

        // Tạo đạn mới tại vị trí đã tính
        Projectile projectile = new Projectile(type, spawnX, spawnY);
        projectile.fire(target, rotation, damage);
        projectiles.add(projectile);
    }

    private void shootAtObstacle(Obstacle obstacle) {
        if (obstacle == null) return;

        float radians = rotation * MathUtils.degreesToRadians;
        float spawnX, spawnY;
        float centerX = position.x + tileSize/2;
        float centerY = position.y + tileSize/2;

        switch (type) {
            case STONE_TOWER:
                float stoneOffset = tileSize * 0.7f;
                spawnX = centerX + MathUtils.cos(radians) * stoneOffset;
                spawnY = centerY + MathUtils.sin(radians) * stoneOffset;
                break;

            case FIRE_TOWER:
                float fireOffset = tileSize * 0.5f;
                spawnX = centerX + MathUtils.cos(radians) * fireOffset;
                spawnY = centerY + MathUtils.sin(radians) * fireOffset;
                break;

            case BIGLAND_TOWER:
                float bigLandOffset = tileSize * 0.8f;
                spawnX = centerX + MathUtils.cos(radians) * bigLandOffset;
                spawnY = centerY + MathUtils.sin(radians) * bigLandOffset;
                break;

            case LAND_TOWER:
                float landOffset = tileSize * 0.6f;
                spawnX = centerX + MathUtils.cos(radians) * landOffset;
                spawnY = centerY + MathUtils.sin(radians) * landOffset;
                break;

            default:
                spawnX = centerX;
                spawnY = centerY;
                break;
        }

        // Tạo đạn mới tại vị trí đã tính
        Projectile projectile = new Projectile(type, spawnX, spawnY);
        projectile.fireAtObstacle(obstacle, rotation, damage);
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

        // Vẽ tháp sử dụng AnimatedTower (bao gồm cả đạn bay theo vòng cung)
        animatedTower.render(batch);

        // Vẽ projectiles thật
        for (int i = 0; i < projectiles.size; i++) {
            projectiles.get(i).render(batch);
        }
    }

    // Được AnimatedTower gọi khi đạn đạt đỉnh và tách khỏi tháp
    public void onProjectileLaunchedFromApex(float apexX, float apexY) {
        Projectile projectile = new Projectile(type, apexX, apexY);
        if (currentObstacle != null && !currentObstacle.isDestroyed()) {
            projectile.fireAtObstacle(currentObstacle, rotation, damage);
        } else if (currentTarget != null && currentTarget.isAlive()) {
            projectile.fire(currentTarget, rotation, damage);
        } else {
            // Không có mục tiêu hợp lệ, bắn theo hướng hiện tại
            projectile.fire(null, rotation, damage);
        }
        projectiles.add(projectile);
    }

    // Thiết lập mục tiêu thủ công
    @SuppressWarnings("DefaultLocale")
    public void setManualTarget(Enemy target) {
        if (target != null && isInRange(target)) {
            // if (this.manualTarget != target) {
            //     Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) targeting enemy at (%.1f,%.1f)",
            //         position.x, position.y, target.getX(), target.getY()));
            // }
            this.manualTarget = target;
            this.manualObstacle = null; // Clear obstacle target when targeting enemy
        } else if (target == null && this.manualTarget != null) {
            Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) clearing enemy target",
                position.x, position.y));
            this.manualTarget = null;
        }
    }

    @SuppressWarnings("DefaultLocale")
    public void setManualObstacleTarget(Obstacle obstacle) {
        if (obstacle != null && isInRange(obstacle)) {
            if (this.manualObstacle != obstacle) {
                Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) targeting %s obstacle",
                    position.x, position.y, obstacle.getType()));
            }
            this.manualObstacle = obstacle;
            this.manualTarget = null; // Clear enemy target when targeting obstacle
        } else if (obstacle == null && this.manualObstacle != null) {
            Gdx.app.log("Tower", String.format("Tower at (%.1f,%.1f) clearing obstacle target",
                position.x, position.y));
            this.manualObstacle = null;
        }
    }

    // Bật/tắt hiển thị vùng tầm bắn
    public void showRange(boolean show) {
        this.showRange = show;
    }

    public boolean isShowingRange() {
        return showRange;
    }

    public void toggleRange() {
        this.showRange = !this.showRange;
    }

    public int getUpgradeCost() {
        // Giá nâng cấp tùy thuộc vào loại tháp
        switch (type) {
            case STONE_TOWER:
                return 150;
            case FIRE_TOWER:
                return 120;
            case BIGLAND_TOWER:
                return 300;
            case LAND_TOWER:
                return 200;
            default:
                return 100;
        }
    }

    public boolean canUpgrade() {
        return level < MAX_LEVEL;
    }

    public void upgrade() {
        if (!canUpgrade()) return;

        level++;
        // Nâng cấp tháp: tăng sát thương và tầm bắn
        switch (type) {
            case STONE_TOWER:
                range *= 1.2f;      // Tăng tầm bắn 20%
                shootDelay *= 0.8f; // Giảm thời gian chờ 20%
                damage *= 1.5f;     // Tăng sát thương 50%
                break;
            case FIRE_TOWER:
                range *= 1.1f;      // Tăng tầm bắn 10%
                shootDelay *= 0.7f; // Giảm thời gian chờ 30%
                damage *= 1.3f;     // Tăng sát thương 30%
                break;
            case BIGLAND_TOWER:
                range *= 1.3f;      // Tăng tầm bắn 30%
                shootDelay *= 0.8f; // Giảm thời gian chờ 20%
                damage *= 1.8f;     // Tăng sát thương 80%
                break;
            case LAND_TOWER:
                range *= 1.25f;     // Tăng tầm bắn 25%
                shootDelay *= 0.75f; // Giảm thời gian chờ 25%
                damage *= 1.6f;     // Tăng sát thương 60%
                break;
        }
        
        // Nâng cấp AnimatedTower
        animatedTower.upgrade();
    }

    public int getSellValue() {
        // Giá bán bằng 70% giá gốc
        return (int)(Currency.getCost(type) * 0.7f);
    }

    public Texture getBaseTexture() {
        // Trả về texture từ AnimatedTower thay vì baseTexture cũ
        return null; // AnimatedTower sẽ xử lý việc vẽ
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

    // Lấy tầm bắn của tháp
    public float getRange() {
        return range;
    }

    // Giải phóng tài nguyên
    public void dispose() {
        // Dispose AnimatedTower
        if (animatedTower != null) {
            animatedTower.dispose();
        }
        
        // Không cần dispose projectiles cũ nữa vì đã thay thế bằng hệ thống mới
        
        // Dispose shapeRenderer
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}

