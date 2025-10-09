package io.github.some_example_name.entities.enemy;

/**
 * Lớp lưu trữ thông tin phân bổ từng loại quái cho một đường
 */
public class EnemyDistribution {
    // Số lượng từng loại quái
    private int normalCount;  // Số lượng quái thường
    private int fastCount;    // Số lượng quái nhanh
    private int tankCount;    // Số lượng quái tank
    private int bossCount;    // Số lượng boss

    // Thông số cho quái thường
    private float normalHealth;  // Máu của quái thường
    private float normalSpeed;   // Tốc độ của quái thường

    // Thông số cho quái nhanh
    private float fastHealth;    // Máu của quái nhanh
    private float fastSpeed;     // Tốc độ của quái nhanh

    // Thông số cho quái tank
    private float tankHealth;    // Máu của quái tank
    private float tankSpeed;     // Tốc độ của quái tank

    // Thông số cho boss
    private float bossHealth;    // Máu của boss
    private float bossSpeed;     // Tốc độ của boss

    /**
     * Khởi tạo phân bổ quái cho một đường với thông số riêng cho từng loại
     * @param normalCount Số lượng quái thường
     * @param normalHealth Máu của quái thường
     * @param normalSpeed Tốc độ của quái thường
     * @param fastCount Số lượng quái nhanh
     * @param fastHealth Máu của quái nhanh
     * @param fastSpeed Tốc độ của quái nhanh
     * @param tankCount Số lượng quái tank
     * @param tankHealth Máu của quái tank
     * @param tankSpeed Tốc độ của quái tank
     * @param bossCount Số lượng boss
     * @param bossHealth Máu của boss
     * @param bossSpeed Tốc độ của boss
     */
    public EnemyDistribution(
        int normalCount, float normalHealth, float normalSpeed,
        int fastCount, float fastHealth, float fastSpeed,
        int tankCount, float tankHealth, float tankSpeed,
        int bossCount, float bossHealth, float bossSpeed
    ) {
        this.normalCount = normalCount;
        this.normalHealth = normalHealth;
        this.normalSpeed = normalSpeed;

        this.fastCount = fastCount;
        this.fastHealth = fastHealth;
        this.fastSpeed = fastSpeed;

        this.tankCount = tankCount;
        this.tankHealth = tankHealth;
        this.tankSpeed = tankSpeed;

        this.bossCount = bossCount;
        this.bossHealth = bossHealth;
        this.bossSpeed = bossSpeed;
    }

    /**
     * Lấy tổng số quái trên đường này
     */
    public int getTotalCount() {
        return normalCount + fastCount + tankCount + bossCount;
    }

    /**
     * Lấy thông số của một loại quái cụ thể
     * @param type Loại quái cần lấy thông số
     * @return Mảng gồm [máu, tốc độ] của loại quái đó
     */
    public float[] getStats(Enemy.Type type) {
        switch (type) {
            case NORMAL:
                return new float[]{normalHealth, normalSpeed};
            case FAST:
                return new float[]{fastHealth, fastSpeed};
            case TANK:
                return new float[]{tankHealth, tankSpeed};
            case BOSS:
                return new float[]{bossHealth, bossSpeed};
            default:
                return new float[]{0, 0};
        }
    }

    // Các phương thức getter
    public int getNormalCount() { return normalCount; }
    public int getFastCount() { return fastCount; }
    public int getTankCount() { return tankCount; }
    public int getBossCount() { return bossCount; }
}
