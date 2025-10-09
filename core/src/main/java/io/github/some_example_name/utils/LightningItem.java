package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class LightningItem {
    private static Texture itemTexture;
    private static Texture lightningTexture;
    
    private static final float ITEM_SIZE = 80f;
    private static final float COOLDOWN_TIME = 30f; // 30 giây hồi
    private static final float LIGHTNING_DURATION = 0.5f; // Hiệu ứng sét hiển thị 0.5s
    private static final int LIGHTNING_RANGE = 1; // Phạm vi 1 ô = 3x3 grid
    
    private static float cooldownTimer = 0f;
    private static float lightningTimer = 0f;
    private static boolean canUse = true;
    private static boolean waitingForTarget = false;
    private static boolean isActive = false;
    
    // Vị trí các ô sét
    private static Array<LightningCell> lightningCells = new Array<>();
    
    public static class LightningCell {
        public int tileX;
        public int tileY;
        public float worldX;
        public float worldY;
        public float size;
        
        public LightningCell(int tileX, int tileY, float worldX, float worldY, float size) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.worldX = worldX;
            this.worldY = worldY;
            this.size = size;
        }
    }
    
    public static void initialize() {
        itemTexture = new Texture(Gdx.files.internal("item/item2.png"));
        lightningTexture = new Texture(Gdx.files.internal("item/set.png"));
        cooldownTimer = 0f;
        lightningTimer = 0f;
        canUse = true;
        waitingForTarget = false;
        isActive = false;
        lightningCells.clear();
        Gdx.app.log("LightningItem", "Lightning item initialized");
    }
    
    public static void update(float delta) {
        // Cập nhật cooldown
        if (!canUse) {
            cooldownTimer += delta;
            if (cooldownTimer >= COOLDOWN_TIME) {
                cooldownTimer = 0f;
                canUse = true;
            }
        }
        
        // Cập nhật lightning effect
        if (isActive) {
            lightningTimer += delta;
            if (lightningTimer >= LIGHTNING_DURATION) {
                lightningTimer = 0f;
                isActive = false;
                lightningCells.clear();
            }
        }
    }
    
    public static void render(SpriteBatch batch, BitmapFont font) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Vẽ item button ở góc dưới trái, bên cạnh freeze item
        float itemX = 20f + ITEM_SIZE + 20f; // Cách freeze item 20px
        float itemY = 20f;
        
        // Vẽ button với alpha dựa trên trạng thái
        if (canUse || waitingForTarget) {
            if (waitingForTarget) {
                // Hiệu ứng nhấp nháy khi đang chờ chọn vị trí
                float alpha = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() / 200.0);
                batch.setColor(1, 1, alpha, 1);
            } else {
                batch.setColor(1, 1, 1, 1); // Màu bình thường
            }
        } else {
            batch.setColor(0.5f, 0.5f, 0.5f, 0.7f); // Màu xám khi cooldown
        }
        batch.draw(itemTexture, itemX, itemY, ITEM_SIZE, ITEM_SIZE);
        batch.setColor(1, 1, 1, 1); // Reset color
        
        // Vẽ cooldown timer
        if (!canUse && !waitingForTarget) {
            font.getData().setScale(1.5f);
            font.setColor(Color.WHITE);
            String cooldownText = String.format("%.0f", COOLDOWN_TIME - cooldownTimer);
            font.draw(batch, cooldownText, itemX + ITEM_SIZE/2 - 15, itemY + ITEM_SIZE/2 + 10);
            font.getData().setScale(1.0f);
        }
    }
    
    public static void renderLightning(SpriteBatch batch) {
        if (isActive) {
            for (LightningCell cell : lightningCells) {
                batch.draw(lightningTexture, cell.worldX, cell.worldY, cell.size, cell.size);
            }
        }
    }
    
    public static boolean checkClick(float x, float y) {
        float itemX = 20f + ITEM_SIZE + 20f;
        float itemY = 20f;
        
        if (canUse && x >= itemX && x <= itemX + ITEM_SIZE && 
            y >= itemY && y <= itemY + ITEM_SIZE) {
            activateSelection();
            return true;
        }
        return false;
    }
    
    public static void activateSelection() {
        if (canUse) {
            waitingForTarget = true;
            Gdx.app.log("LightningItem", "Waiting for target selection...");
        }
    }
    
    public static void activateLightning(int centerTileX, int centerTileY, float tileWidth, float tileHeight) {
        if (waitingForTarget) {
            lightningCells.clear();
            
            // Tạo 9 ô sét (3x3 grid với ô chọn là trung tâm)
            for (int dx = -LIGHTNING_RANGE; dx <= LIGHTNING_RANGE; dx++) {
                for (int dy = -LIGHTNING_RANGE; dy <= LIGHTNING_RANGE; dy++) {
                    int tileX = centerTileX + dx;
                    int tileY = centerTileY + dy;
                    float worldX = tileX * tileWidth;
                    float worldY = tileY * tileHeight;
                    
                    lightningCells.add(new LightningCell(tileX, tileY, worldX, worldY, tileWidth));
                }
            }
            
            isActive = true;
            lightningTimer = 0f;
            waitingForTarget = false;
            canUse = false;
            cooldownTimer = 0f;
            
            // Phát âm thanh sét
            GameSoundManager.setSound();
            
            Gdx.app.log("LightningItem", "Lightning activated at tile (" + centerTileX + ", " + centerTileY + ")");
        }
    }
    
    public static boolean isWaitingForTarget() {
        return waitingForTarget;
    }
    
    public static void cancelSelection() {
        waitingForTarget = false;
    }
    
    public static Array<LightningCell> getLightningCells() {
        return lightningCells;
    }
    
    public static boolean isLightningActive() {
        return isActive;
    }
    
    public static void reset() {
        cooldownTimer = 0f;
        lightningTimer = 0f;
        canUse = true;
        waitingForTarget = false;
        isActive = false;
        lightningCells.clear();
    }
    
    public static void dispose() {
        if (itemTexture != null) itemTexture.dispose();
        if (lightningTexture != null) lightningTexture.dispose();
    }
}

