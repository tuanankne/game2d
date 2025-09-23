package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class NumberRenderer {
    private static Array<Texture> numberTextures;
    private static final float NUMBER_SPACING = -8f; // Khoảng cách âm để các số chồng lên nhau một chút

    public static void initialize() {
        numberTextures = new Array<>();
        // Load textures từ 0-9 (276-285)
        for (int i = 0; i <= 9; i++) {
            try {
                numberTextures.add(new Texture("map1/towerDefense_tile" + (276 + i) + ".png"));
            } catch (Exception e) {
                Gdx.app.error("NumberRenderer", "Failed to load texture: " + e.getMessage());
            }
        }
        if (numberTextures.size == 0) {
            // Fallback texture nếu load thất bại
            numberTextures.add(new Texture("map1/towerDefense_tile276.png"));
        }
    }

    public static void drawNumber(SpriteBatch batch, int number, float x, float y, float size) {
        String numStr = String.valueOf(number);
        float currentX = x;

        // com.badlogic.gdx.Gdx.app.debug("NumberRenderer",
        //     String.format("Drawing number %d at (%.1f, %.1f) with size %.2f", number, x, y, size));

        if (numberTextures.size > 0) {
            for (char digit : numStr.toCharArray()) {
                int index = digit - '0';
                if (index >= 0 && index < numberTextures.size) {
                    batch.draw(numberTextures.get(index), currentX, y, size, size);
                } else {
                    batch.draw(numberTextures.get(0), currentX, y, size, size);
                }
                currentX += size + NUMBER_SPACING;
            }
        }
    }

    public static void dispose() {
        if (numberTextures != null) {
            for (Texture texture : numberTextures) {
                texture.dispose();
            }
            numberTextures.clear();
        }
    }
}
