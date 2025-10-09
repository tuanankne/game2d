package io.github.some_example_name.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Class xử lý nền cuộn liên tục từ phải sang trái, khi hết sẽ lật ngược lại và chạy tiếp.
 * Dựa trên logic từ thuchanh.java
 */
public class ScrollingBackground {
    private Texture backgroundTexture;
    private float drawWidth;
    private float drawHeight;
    
    // Vị trí x của hai tile nền để cuộn liên tục
    private float bgX1;
    private float bgX2;
    
    // Mỗi tile có trạng thái lật riêng
    private boolean tileFlipped1 = false; // tile đầu bình thường
    private boolean tileFlipped2 = true;  // tile thứ hai lật ngay từ đầu
    
    // Tốc độ cuộn (pixel/giây)
    private float scrollSpeed = 100f; // chậm hơn một chút cho menu
    
    // Luôn cuộn sang trái
    private static final int SCROLL_LEFT = -1;

    public ScrollingBackground(String backgroundFile) {
        // Tải ảnh nền
        backgroundTexture = new Texture(Gdx.files.internal(backgroundFile));
        
        // Lấy kích thước màn hình
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        // Tính kích thước vẽ để nền luôn phủ kín màn hình, giữ tỉ lệ ảnh
        computeDrawSize(screenW, screenH);
        
        // Đặt hai tile liền nhau theo hướng cuộn ban đầu
        bgX1 = 0f;
        bgX2 = bgX1 + drawWidth;
    }

    private void computeDrawSize(float screenW, float screenH) {
        float texW = backgroundTexture.getWidth();
        float texH = backgroundTexture.getHeight();
        float scale = Math.max(screenW / texW, screenH / texH);
        drawWidth = texW * scale;
        drawHeight = texH * scale;
    }

    public void update(float delta) {
        // Cập nhật vị trí cuộn
        float moveDistance = scrollSpeed * delta * SCROLL_LEFT;
        bgX1 += moveDistance;
        bgX2 += moveDistance;
        
        // Kiểm tra nếu tile đầu đã cuộn hết ra ngoài màn hình
        if (bgX1 + drawWidth <= 0) {
            // Đặt tile đầu ở cuối tile thứ hai và lật ngược
            bgX1 = bgX2 + drawWidth;
            tileFlipped1 = !tileFlipped1;
        }
        
        // Kiểm tra nếu tile thứ hai đã cuộn hết ra ngoài màn hình
        if (bgX2 + drawWidth <= 0) {
            // Đặt tile thứ hai ở cuối tile đầu và lật ngược
            bgX2 = bgX1 + drawWidth;
            tileFlipped2 = !tileFlipped2;
        }
    }

    public void render(SpriteBatch batch) {
        // Vẽ tile đầu tiên
        if (tileFlipped1) {
            // Vẽ lật ngược theo chiều ngang
            batch.draw(backgroundTexture, 
                bgX1 + drawWidth, 0, -drawWidth, drawHeight);
        } else {
            // Vẽ bình thường
            batch.draw(backgroundTexture, bgX1, 0, drawWidth, drawHeight);
        }
        
        // Vẽ tile thứ hai
        if (tileFlipped2) {
            // Vẽ lật ngược theo chiều ngang
            batch.draw(backgroundTexture, 
                bgX2 + drawWidth, 0, -drawWidth, drawHeight);
        } else {
            // Vẽ bình thường
            batch.draw(backgroundTexture, bgX2, 0, drawWidth, drawHeight);
        }
    }

    public void resize(float width, float height) {
        // Tính lại kích thước vẽ khi màn hình thay đổi
        computeDrawSize(width, height);
        
        // Đặt lại vị trí hai tile
        bgX1 = 0f;
        bgX2 = bgX1 + drawWidth;
    }

    public void dispose() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
