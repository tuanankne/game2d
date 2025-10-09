package io.github.some_example_name.entities.tower;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import java.util.Arrays;

public class AnimatedTower implements Disposable {
    // ... Các biến và Constructor giữ nguyên ...
    private enum State { BUILDING, IDLE, FIRING }
    private State currentState;
    private float animationTimer;

    private Texture[] baseTextures, cannonTextures, holderBackTextures, holderFrontTextures,
        projectileTextures, armTextures, clampTopTextures, clampBottomTextures;

    private TowerType type;
    private int currentLevel;
    private Vector2 position;

    // Tham chiếu đến Tower để có thể gây damage
    private Tower towerRef; // Sẽ được set từ Tower

    private float cannonYOffset, projectileYOffset, buildEffectAlpha, armRotation;

    // Cờ để ẩn viên đạn giả khi đã tách ra ở đỉnh
    private boolean projectileFlying = false;
    private Vector2 projectileStartPos = new Vector2();
    private Vector2 projectileTargetPos = new Vector2();
    private boolean targetPositionSet = false; // Để biết target đã được set chưa
    private final Vector2 tmpApexPos = new Vector2();

    public AnimatedTower(float x, float y, TowerType type) {
        this.position = new Vector2(x, y);
        this.type = type;
        this.currentLevel = 1;
        this.currentState = State.BUILDING;
        this.animationTimer = 0f;

        baseTextures = loadTexturesForComponent("base", 3);
        cannonTextures = loadTexturesForComponent("cannon", 3);
        holderBackTextures = loadTexturesForComponent("holderBack", 3);
        holderFrontTextures = loadTexturesForComponent("holderFront", 3);
        projectileTextures = loadTexturesForComponent("projectile", 3);
        armTextures = loadTexturesForComponent("arm", 3);
        clampTopTextures = loadTexturesForComponent("clampTop", 3);
        clampBottomTextures = loadTexturesForComponent("clampBottom", 3);
    }

    private Texture[] loadTexturesForComponent(String component, int maxLevels) {
        if (type.getPathFor(component, 1) == null) return null;
        Texture[] textures = new Texture[maxLevels];
        for (int i = 0; i < maxLevels; i++) {
            String path = type.getPathFor(component, i + 1);
            if (path != null) textures[i] = new Texture(path);
        }
        return textures;
    }

    public void upgrade() { if (currentLevel < 3) currentLevel++; }

    public void fire() {
        if (currentState == State.IDLE) {
            currentState = State.FIRING;
            animationTimer = 0;
            projectileYOffset = 0;
            armRotation = 0;
            projectileFlying = false;
        }
    }

    // Method để set target position cho projectile
    public void setTargetPosition(float targetX, float targetY) {
        // Vị trí bắt đầu (từ tháp)
        projectileStartPos.set(position.x + 64, position.y + 125); // Điều chỉnh theo vị trí tháp

        // Vị trí mục tiêu
        projectileTargetPos.set(targetX, targetY);
        targetPositionSet = true;
    }

    // ==========================================================
    // HÀM UPDATE ĐÃ SỬA LẠI
    // ==========================================================
    public void update(float delta) {
        animationTimer += delta;

        if(currentState == State.IDLE) {
            float bobSpeed = 2.5f;
            float bobHeight = 4f;
            projectileYOffset = (float) Math.sin(animationTimer * bobSpeed) * bobHeight;
        }

        switch (type) {
            // GỘP TẤT CẢ CÁC THÁP VÀO CHUNG MỘT LOGIC ANIMATION
            case STONE_TOWER:
            case FIRE_TOWER:
            case LAND_TOWER:
                if (currentState == State.BUILDING) {
                    currentState = State.IDLE;
                    animationTimer = 0;
                }
                else if (currentState == State.FIRING) {
                    // Animation nâng-hạ khi bắn
                    float liftSpeed = 200f;
                    if (animationTimer < 0.4f) {
                        projectileYOffset += liftSpeed * delta; // Nâng lên
                    } else if (animationTimer < 0.6f) {
                        // Ở vị trí cao nhất - projectile bắn ra ngoài
                        projectileYOffset = 80f; // Giữ ở vị trí cao nhất
                        if (!projectileFlying && targetPositionSet) {
                            // Bắt đầu projectile bay ra ngoài tấn công target
                            projectileFlying = true;
                            // Gọi Tower để spawn Projectile thật tại đúng vị trí viên đạn giả biến mất
                            if (towerRef != null) {
                                Vector2 apex = getHeldProjectileCenter();
                                towerRef.onProjectileLaunchedFromApex(apex.x, apex.y);
                            }
                        }
                    } else {
                        projectileYOffset = 0;
                        currentState = State.IDLE;
                        // Reset trạng thái cho lần bắn tiếp theo
                        projectileFlying = false;
                        targetPositionSet = false;
                    }
                }
                break;

            case BIGLAND_TOWER:
                if (currentState == State.BUILDING) {
                    if (buildEffectAlpha < 1.0f) buildEffectAlpha += delta / 0.7f;
                    else {
                        buildEffectAlpha = 1.0f;
                        currentState = State.IDLE;
                        animationTimer = 0;
                    }
                }
                else if (currentState == State.FIRING) {
                    // Animation nâng-hạ khi bắn
                    float liftSpeed = 200f;
                    if (animationTimer < 0.4f) {
                        projectileYOffset += liftSpeed * delta; // Nâng lên
                    } else if (animationTimer < 0.6f) {
                        // Ở vị trí cao nhất - projectile bắn ra ngoài
                        projectileYOffset = 80f; // Giữ ở vị trí cao nhất
                        if (!projectileFlying && targetPositionSet) {
                            // Bắt đầu projectile bay ra ngoài tấn công target
                            projectileFlying = true;
                            // Gọi Tower để spawn Projectile thật tại đúng vị trí viên đạn giả biến mất
                            if (towerRef != null) {
                                Vector2 apex = getHeldProjectileCenter();
                                towerRef.onProjectileLaunchedFromApex(apex.x, apex.y);
                            }
                        }
                    } else {
                        projectileYOffset = 0;
                        currentState = State.IDLE;
                        // Reset trạng thái cho lần bắn tiếp theo
                        projectileFlying = false;
                        targetPositionSet = false;
                    }
                }
                break;
        }
    }

    public void render(Batch batch) {
        switch (type) {
            case STONE_TOWER:
                renderStoneTower(batch);
                break;
            case FIRE_TOWER:
                renderFireTower(batch);
                break;
            case LAND_TOWER:
                renderLandTower(batch);
                break;
            case BIGLAND_TOWER:
                renderTwoArmLiftTower(batch);
                break;
        }
    }

    // Trong file AnimatedTower.java

    private void renderStoneTower(Batch batch) {
        Texture baseTex = baseTextures[currentLevel - 1];
        Texture holderBackTex = holderBackTextures[currentLevel - 1];   // Phần trên
        Texture holderFrontTex = holderFrontTextures[currentLevel - 1]; // Phần dưới
        Texture projectileTex = projectileTextures[currentLevel - 1];



        // 2. Tính toán vị trí Y cơ sở cho cả khối di chuyển
        float movingBlockY = position.y + projectileYOffset + 40;

        // --- LOGIC XẾP CHỒNG MỚI ---



        // 4. Tính toán vị trí cho holderBack (phần trên)
        // Lấy chiều cao của phần dưới
        float frontHeight = holderFrontTex.getHeight();
        // Vị trí của phần trên = vị trí phần dưới + chiều cao phần dưới
        float backY = movingBlockY + frontHeight - 5; // Trừ đi một chút để xếp chồng khít hơn

        // 5. Vẽ holderBack (phần trên)
        // Lưu ý: Để xếp chồng khít, 2 ảnh của bạn nên có cùng chiều rộng
        batch.draw(holderBackTex, position.x, backY);
        // 1. Vẽ đế (cố định)
        batch.draw(baseTex, position.x, position.y);
        // 3. Vẽ holderFront (phần dưới) trước
        batch.draw(holderFrontTex, position.x, movingBlockY);
        // 6. Vẽ viên đạn giả chỉ khi chưa bắn ra ngoài
        if (!projectileFlying) {
            // Vẽ projectile khi không bắn ra ngoài
            float projectileX = position.x + (baseTex.getWidth() - projectileTex.getWidth()) / 2f;
            float projectileY_relative = 125; // Vị trí đạn so với đế
            batch.draw(projectileTex, projectileX, position.y + projectileY_relative + projectileYOffset);
        }
        // Viên đạn thật sẽ do Tower render
    }
    private void renderFireTower(Batch batch) {
        Texture baseTex = baseTextures[currentLevel - 1];
        Texture holderBackTex = holderBackTextures[currentLevel - 1];   // Phần trên
        Texture holderFrontTex = holderFrontTextures[currentLevel - 1]; // Phần dưới
        Texture projectileTex = projectileTextures[currentLevel - 1];



        // 2. Tính toán vị trí Y cơ sở cho cả khối di chuyển
        float movingBlockY = position.y + projectileYOffset + 35;

        // --- LOGIC XẾP CHỒNG MỚI ---



        // 4. Tính toán vị trí cho holderBack (phần trên)
        // Lấy chiều cao của phần dưới
        float frontHeight = holderFrontTex.getHeight();
        // Vị trí của phần trên = vị trí phần dưới + chiều cao phần dưới
        float backY = movingBlockY + frontHeight - 15; // Trừ đi một chút để xếp chồng khít hơn

        // 5. Vẽ holderBack (phần trên)
        // Lưu ý: Để xếp chồng khít, 2 ảnh của bạn nên có cùng chiều rộng
        batch.draw(holderBackTex, position.x, backY);
        // 1. Vẽ đế (cố định)
        batch.draw(baseTex, position.x, position.y);
        // 3. Vẽ holderFront (phần dưới) trước
        batch.draw(holderFrontTex, position.x, movingBlockY);
        // 6. Vẽ viên đạn giả chỉ khi chưa bắn ra ngoài
        if (!projectileFlying) {
            // Vẽ projectile khi không bắn ra ngoài
            float projectileX = position.x + (baseTex.getWidth() - projectileTex.getWidth()) / 2f;
            float projectileY_relative = 125; // Vị trí đạn so với đế
            batch.draw(projectileTex, projectileX, position.y + projectileY_relative + projectileYOffset);
        }
        // Viên đạn thật sẽ do Tower render
    }
    private void renderLandTower(Batch batch) {
        Texture baseTex = baseTextures[currentLevel - 1];
        Texture holderBackTex = holderBackTextures[currentLevel - 1];   // Phần trên
        Texture holderFrontTex = holderFrontTextures[currentLevel - 1]; // Phần dưới
        Texture projectileTex = projectileTextures[currentLevel - 1];



        // 2. Tính toán vị trí Y cơ sở cho cả khối di chuyển
        float movingBlockY = position.y + projectileYOffset + 40;

        // --- LOGIC XẾP CHỒNG MỚI ---



        // 4. Tính toán vị trí cho holderBack (phần trên)
        // Lấy chiều cao của phần dưới
        float frontHeight = holderFrontTex.getHeight();
        // Vị trí của phần trên = vị trí phần dưới + chiều cao phần dưới
        float backY = movingBlockY + frontHeight - 5; // Trừ đi một chút để xếp chồng khít hơn

        // 5. Vẽ holderBack (phần trên)
        // Lưu ý: Để xếp chồng khít, 2 ảnh của bạn nên có cùng chiều rộng
        batch.draw(holderBackTex, position.x, backY);
        // 1. Vẽ đế (cố định)
        batch.draw(baseTex, position.x, position.y);
        // 3. Vẽ holderFront (phần dưới) trước
        batch.draw(holderFrontTex, position.x, movingBlockY);
        // 6. Vẽ viên đạn giả chỉ khi chưa bắn ra ngoài
        if (!projectileFlying) {
            // Vẽ projectile khi không bắn ra ngoài
            float projectileX = position.x + (baseTex.getWidth() - projectileTex.getWidth()) / 2f;
            float projectileY_relative = 125; // Vị trí đạn so với đế
            batch.draw(projectileTex, projectileX, position.y + projectileY_relative + projectileYOffset);
        }
        // Viên đạn thật sẽ do Tower render
    }

    // ==========================================================
    // HÀM CHO BIGLAND_TOWER (2 CÁNH TAY + ĐẠN CÙNG NÂNG LÊN)
    // ==========================================================
    private void renderTwoArmLiftTower(Batch batch) {
        Texture baseTex = baseTextures[currentLevel - 1];
        Texture armTex = armTextures[currentLevel - 1];
        Texture projectileTex = projectileTextures[currentLevel - 1];
        // 3. Vẽ 2 cánh tay cùng nâng lên
        float leftArmX = position.x + 6;
        float rightArmX = position.x + baseTex.getWidth() - armTex.getWidth() - 20;
        float armsY = position.y + 50;

        // Vẽ 2 cánh tay di chuyển theo chiều dọc
        batch.draw(armTex, leftArmX, armsY + projectileYOffset);
        batch.draw(armTex, rightArmX, armsY + projectileYOffset);

        // 1. Vẽ đế (cố định)
        batch.draw(baseTex, position.x, position.y);

        // 2. Tính toán vị trí Y chung cho 2 CÁNH TAY và ĐẠN di chuyển



        // 4. Vẽ viên đạn giả chỉ khi chưa bắn ra ngoài
        if (!projectileFlying) {
            // Vẽ projectile khi không bắn ra ngoài
            float projectileX = position.x + (baseTex.getWidth() - projectileTex.getWidth()) / 2f;
            float projectileY_relative = 125; // Vị trí đạn so với đế
            batch.draw(projectileTex, projectileX, position.y + projectileY_relative + projectileYOffset);
        }
        // Viên đạn thật sẽ do Tower render
    }


    // Method để set tham chiếu Tower
    public void setTowerRef(Tower tower) {
        this.towerRef = tower;
    }

    // Tính tâm (center) của viên đạn giả đang được giữ trên tháp (theo công thức vẽ hiện tại)
    private Vector2 getHeldProjectileCenter() {
        Texture baseTex = baseTextures[currentLevel - 1];
        Texture projectileTex = projectileTextures[currentLevel - 1];
        // Vị trí vẽ viên đạn giả trong các render*Tower đều dùng cùng công thức X và offset Y 125
        float projectileX = position.x + (baseTex.getWidth() - projectileTex.getWidth()) / 2f;
        float projectileY = position.y + 125 + projectileYOffset;
        // Trả về tâm
        tmpApexPos.set(
            projectileX + projectileTex.getWidth() / 2f,
            projectileY + projectileTex.getHeight() / 2f
        );
        return tmpApexPos;
    }

    @Override
    public void dispose() {
        // ... Hàm dispose giữ nguyên ...
        Arrays.asList(baseTextures, cannonTextures, holderBackTextures, holderFrontTextures,
                projectileTextures, armTextures, clampTopTextures, clampBottomTextures)
            .forEach(textureArray -> {
                if (textureArray != null) {
                    for (Texture texture : textureArray) {
                        if (texture != null) texture.dispose();
                    }
                }
            });
    }
}
