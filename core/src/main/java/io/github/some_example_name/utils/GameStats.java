package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;

/**
 * Class quản lý thống kê game như số quái đã tiêu diệt và thời gian chơi
 */
public class GameStats {
    private static int enemiesKilled = 0;
    private static int totalEnemies = 0;
    private static float gameStartTime = 0;
    private static float currentPlayTime = 0;
    private static boolean gameStarted = false;

    /**
     * Khởi tạo thống kê game
     */
    public static void initialize() {
        reset();
    }

    /**
     * Reset tất cả thống kê về 0
     */
    public static void reset() {
        enemiesKilled = 0;
        totalEnemies = 0;
        gameStartTime = 0;
        currentPlayTime = 0;
        gameStarted = false;
    }

    /**
     * Bắt đầu game và ghi lại thời gian bắt đầu
     */
    public static void startGame() {
        gameStartTime = Gdx.app.getGraphics().getDeltaTime() > 0 ? 
            System.currentTimeMillis() / 1000f : 0;
        gameStarted = true;
    }

    /**
     * Cập nhật thời gian chơi hiện tại
     */
    public static void updatePlayTime() {
        if (gameStarted) {
            currentPlayTime = (System.currentTimeMillis() / 1000f) - gameStartTime;
        }
    }

    /**
     * Tăng số quái đã tiêu diệt
     */
    public static void incrementEnemiesKilled() {
        enemiesKilled++;
    }

    /**
     * Thiết lập tổng số quái trong game
     */
    public static void setTotalEnemies(int total) {
        totalEnemies = total;
    }

    /**
     * Lấy số quái đã tiêu diệt
     */
    public static int getEnemiesKilled() {
        return enemiesKilled;
    }

    /**
     * Lấy tổng số quái
     */
    public static int getTotalEnemies() {
        return totalEnemies;
    }

    /**
     * Lấy thời gian chơi hiện tại (giây)
     */
    public static float getCurrentPlayTime() {
        return currentPlayTime;
    }

    /**
     * Format thời gian thành chuỗi MM:SS
     */
    public static String formatTime(float seconds) {
        int total = Math.max(0, (int)seconds);
        int minutes = total / 60;
        int secs = total % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Lấy chuỗi thống kê quái đã tiêu diệt
     */
    public static String getEnemyStatsString() {
        if (totalEnemies <= 0) {
            return String.format("Total Creep (%d/?)", enemiesKilled);
        }
        return String.format("Total Creep (%d/%d)", enemiesKilled, totalEnemies);
    }

    /**
     * Lấy chuỗi thời gian chơi
     */
    public static String getTimeString() {
        return "Time: " + formatTime(currentPlayTime);
    }
}
