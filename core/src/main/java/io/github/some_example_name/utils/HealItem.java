package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class HealItem {
    private static Texture itemTexture;
    private static Texture healEffectTexture;
    
    private static final float ITEM_SIZE = 80f;
    private static final float COOLDOWN_TIME = 90f; // 1 phút 30 giây = 90 giây
    private static final float HEAL_AMOUNT = 20f; // Hồi 20 máu
    private static final float EFFECT_DURATION = 1f; // Hiệu ứng hiển thị 1 giây
    
    private static float cooldownTimer = 0f;
    private static float effectTimer = 0f;
    private static boolean canUse = true;
    private static boolean isActive = false;
    
    public static void initialize() {
        itemTexture = new Texture(Gdx.files.internal("item/item3.png"));
        healEffectTexture = new Texture(Gdx.files.internal("item/hoimau.png"));
        cooldownTimer = 0f;
        effectTimer = 0f;
        canUse = true;
        isActive = false;
        Gdx.app.log("HealItem", "Heal item initialized");
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
        
        // Cập nhật heal effect
        if (isActive) {
            effectTimer += delta;
            if (effectTimer >= EFFECT_DURATION) {
                effectTimer = 0f;
                isActive = false;
            }
        }
    }
    
    public static void render(SpriteBatch batch, BitmapFont font) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Vẽ item button ở góc dưới trái, bên cạnh lightning item
        float itemX = 20f + (ITEM_SIZE + 20f) * 2; // Cách lightning item 20px
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
            font.getData().setScale(1.2f);
            font.setColor(Color.WHITE);
            String cooldownText = String.format("%.0f", COOLDOWN_TIME - cooldownTimer);
            font.draw(batch, cooldownText, itemX + ITEM_SIZE/2 - 15, itemY + ITEM_SIZE/2 + 10);
            font.getData().setScale(1.0f);
        }
    }
    
    public static void renderHealEffect(SpriteBatch batch) {
        if (isActive) {
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            
            // Vẽ hiệu ứng hồi máu toàn màn hình với alpha
            float alpha = 0.6f;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(healEffectTexture, 0, 0, screenWidth, screenHeight);
            batch.setColor(1, 1, 1, 1); // Reset color
        }
    }
    
    public static boolean checkClick(float x, float y) {
        float itemX = 20f + (ITEM_SIZE + 20f) * 2;
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
            // Hồi máu cho nhà chính
            PlayerHealth.heal(HEAL_AMOUNT);
            
            // Kích hoạt hiệu ứng
            isActive = true;
            effectTimer = 0f;
            canUse = false;
            cooldownTimer = 0f;
            
            // Phát âm thanh hồi máu
            GameSoundManager.playHealSound();
            
            Gdx.app.log("HealItem", "Heal item activated! Healed " + HEAL_AMOUNT + " HP");
        }
    }
    
    public static boolean isHealActive() {
        return isActive;
    }
    
    public static void reset() {
        cooldownTimer = 0f;
        effectTimer = 0f;
        canUse = true;
        isActive = false;
    }
    
    public static void dispose() {
        if (itemTexture != null) itemTexture.dispose();
        if (healEffectTexture != null) healEffectTexture.dispose();
    }
}
