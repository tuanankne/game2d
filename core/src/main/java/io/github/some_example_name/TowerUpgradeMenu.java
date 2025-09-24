package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class TowerUpgradeMenu {
    private static final float BUTTON_SIZE = 48f;  // Kích thước nút nhỏ hơn
    private static final float ICON_SIZE = 24f;   // Kích thước icon coin
    private static final float SPACING = 150f;    // Khoảng cách xa hơn so với tháp
    private static final float COIN_OFFSET_Y = -30f;   // Độ lệch của icon coin xuống dưới

    private Vector2 position;
    private boolean visible;
    private Tower tower;
    private Texture upgradeTexture;
    private Texture sellTexture;
    private Texture coinTexture;
    private BitmapFont font;

    public TowerUpgradeMenu(BitmapFont font) {
        this.font = font;
        this.position = new Vector2();
        this.visible = false;
        
        // Load textures
        upgradeTexture = new Texture("map1/towerDefense_tile016.png"); // Upgrade icon
        sellTexture = new Texture("map1/towerDefense_tile017.png");    // Sell icon
        coinTexture = Currency.getCoinTexture();
    }

    public void show(float x, float y, Tower tower) {
        this.position.set(x, y);
        this.tower = tower;
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public void render(SpriteBatch batch) {
        if (!visible || tower == null) return;

        // Vẽ nút bán bên trái
        float sellX = position.x - SPACING;
        float buttonY = position.y - BUTTON_SIZE/2; // Căn giữa theo chiều dọc
        batch.draw(sellTexture, sellX, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        
        // Vẽ giá bán
        int sellValue = tower.getSellValue();
        batch.draw(coinTexture, sellX, buttonY + COIN_OFFSET_Y, ICON_SIZE, ICON_SIZE);
        NumberRenderer.drawNumber(batch, sellValue, sellX + ICON_SIZE + 5, buttonY + COIN_OFFSET_Y, ICON_SIZE);

        // Chỉ vẽ nút nâng cấp nếu tháp chưa đạt cấp tối đa
        if (tower.canUpgrade()) {
            // Vẽ nút nâng cấp bên phải
            float upgradeX = position.x + SPACING - BUTTON_SIZE;
            batch.draw(upgradeTexture, upgradeX, buttonY, BUTTON_SIZE, BUTTON_SIZE);
            
            // Vẽ giá nâng cấp
            int upgradeCost = tower.getUpgradeCost();
            batch.draw(coinTexture, upgradeX, buttonY + COIN_OFFSET_Y, ICON_SIZE, ICON_SIZE);
            NumberRenderer.drawNumber(batch, upgradeCost, upgradeX + ICON_SIZE + 5, buttonY + COIN_OFFSET_Y, ICON_SIZE);
        }
    }

    public int checkClick(float x, float y) {
        if (!visible || tower == null) return -1;

        float buttonY = position.y - BUTTON_SIZE/2;
        
        // Kiểm tra click vào nút bán (bên trái)
        float sellX = position.x - SPACING;
        if (x >= sellX && x <= sellX + BUTTON_SIZE &&
            y >= buttonY && y <= buttonY + BUTTON_SIZE) {
            return 1; // Sell button
        }

        // Kiểm tra click vào nút nâng cấp (bên phải) nếu tháp chưa đạt cấp tối đa
        if (tower.canUpgrade()) {
            float upgradeX = position.x + SPACING - BUTTON_SIZE;
            if (x >= upgradeX && x <= upgradeX + BUTTON_SIZE &&
                y >= buttonY && y <= buttonY + BUTTON_SIZE) {
                return 0; // Upgrade button
            }
        }

        return -1;
    }

    public boolean isVisible() {
        return visible;
    }

    public void dispose() {
        if (upgradeTexture != null) upgradeTexture.dispose();
        if (sellTexture != null) sellTexture.dispose();
    }
}
