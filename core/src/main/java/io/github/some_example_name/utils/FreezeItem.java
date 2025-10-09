package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class FreezeItem {
    private static Texture itemTexture;
    private static Texture snowTexture;
    private static Texture frozenTexture;
    
    private static final float ITEM_SIZE = 80f;
    private static final float COOLDOWN_TIME = 10f; // 10 giây hồi
    private static final float FREEZE_DURATION = 3f; // 3 giây đóng băng
    
    private static float cooldownTimer = 0f;
    private static float freezeTimer = 0f;
    private static boolean isActive = false;
    private static boolean canUse = true;
    
    public static void initialize() {
        itemTexture = new Texture(Gdx.files.internal("item/item1.png"));
        snowTexture = new Texture(Gdx.files.internal("item/snow.png"));
        frozenTexture = new Texture(Gdx.files.internal("item/hoada.png"));
        cooldownTimer = 0f;
        freezeTimer = 0f;
        isActive = false;
        canUse = true;
        Gdx.app.log("FreezeItem", "Freeze item initialized");
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
        
        // Cập nhật freeze effect
        if (isActive) {
            freezeTimer += delta;
            if (freezeTimer >= FREEZE_DURATION) {
                freezeTimer = 0f;
                isActive = false;
            }
        }
    }
    
    public static void render(SpriteBatch batch, BitmapFont font) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Vẽ item button ở góc dưới trái
        float itemX = 20f;
        float itemY = 20f;
        
        // Vẽ button với alpha dựa trên trạng thái
        if (canUse) {
            batch.setColor(1, 1, 1, 1); // Màu bình thường
        } else {
            batch.setColor(0.5f, 0.5f, 0.5f, 0.7f); // Màu xám khi cooldown
        }
        batch.draw(itemTexture, itemX, itemY, ITEM_SIZE, ITEM_SIZE);
        batch.setColor(1, 1, 1, 1); // Reset color
        
        // Vẽ cooldown timer
        if (!canUse) {
            font.getData().setScale(1.5f);
            font.setColor(Color.WHITE);
            String cooldownText = String.format("%.1f", COOLDOWN_TIME - cooldownTimer);
            font.draw(batch, cooldownText, itemX + ITEM_SIZE/2 - 15, itemY + ITEM_SIZE/2 + 10);
            font.getData().setScale(1.0f);
        }
        
        // Vẽ hiệu ứng snow khi active
        if (isActive) {
            // Vẽ snow overlay toàn màn hình với alpha
            float alpha = 0.7f;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(snowTexture, 0, 0, screenWidth, screenHeight);
            batch.setColor(1, 1, 1, 1); // Reset color
        }
    }
    
    public static void renderFrozenEffect(SpriteBatch batch, float x, float y, float size) {
        if (isActive && frozenTexture != null) {
            batch.draw(frozenTexture, x, y, size, size);
        }
    }
    
    public static boolean checkClick(float x, float y) {
        float itemX = 20f;
        float itemY = 20f;
        
        if (canUse && x >= itemX && x <= itemX + ITEM_SIZE && 
            y >= itemY && y <= itemY + ITEM_SIZE) {
            activate();
            return true;
        }
        return false;
    }
    
    public static void activate() {
        if (canUse) {
            isActive = true;
            freezeTimer = 0f;
            canUse = false;
            cooldownTimer = 0f;
            
            // Phát âm thanh snow
            GameSoundManager.playSnowSound();
            
            Gdx.app.log("FreezeItem", "Freeze item activated!");
        }
    }
    
    public static boolean isEnemiesFrozen() {
        return isActive;
    }
    
    public static void reset() {
        cooldownTimer = 0f;
        freezeTimer = 0f;
        isActive = false;
        canUse = true;
    }
    
    public static void dispose() {
        if (itemTexture != null) itemTexture.dispose();
        if (snowTexture != null) snowTexture.dispose();
        if (frozenTexture != null) frozenTexture.dispose();
    }
}

