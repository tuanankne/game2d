package io.github.some_example_name.entities.enemy;

/**
 * Lớp lưu trữ thông tin phân bổ từng loại quái cho một đường
 */
public class EnemyDistribution {
    // Số lượng từng loại quái
    private int normalCount;  // Số lượng quái thường
    private int fastCount;    // Số lượng quái nhanh
    private int tankCount;    // Số lượng quái tank

    // Thông số cho quái thường
    private float normalHealth;  // Máu của quái thường
    private float normalSpeed;   // Tốc độ của quái thường

    // Thông số cho quái nhanh
    private float fastHealth;    // Máu của quái nhanh
    private float fastSpeed;     // Tốc độ của quái nhanh

    // Thông số cho quái tank
    private float tankHealth;    // Máu của quái tank
    private float tankSpeed;     // Tốc độ của quái tank

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
     */
    public EnemyDistribution(
        int normalCount, float normalHealth, float normalSpeed,
        int fastCount, float fastHealth, float fastSpeed,
        int tankCount, float tankHealth, float tankSpeed
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
    }

    /**
     * Lấy tổng số quái trên đường này
     */
    public int getTotalCount() {
        return normalCount + fastCount + tankCount;
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
            default:
                return new float[]{0, 0};
        }
    }

    // Các phương thức getter
    public int getNormalCount() { return normalCount; }
    public int getFastCount() { return fastCount; }
    public int getTankCount() { return tankCount; }
}
