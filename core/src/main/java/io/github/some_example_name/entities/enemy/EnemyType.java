package io.github.some_example_name.entities.enemy;

// Enum định nghĩa các loại quái vật trong game
public enum EnemyType {
    // Quái thường: tốc độ và máu trung bình, kích thước chuẩn
    NORMAL("map1/towerDefense_tile245.png", 150, 80, 2.5f),
    // Quái nhanh: tốc độ cao, ít máu, kích thước nhỏ
    FAST("map1/towerDefense_tile270.png", 100, 150, 2.2f),
    // Quái tank: tốc độ chậm, nhiều máu, kích thước lớn
    TANK("map1/towerDefense_tile271.png", 70, 200, 3.0f);

    // Đường dẫn đến file ảnh texture của quái
    private final String texturePath;
    // Tốc độ di chuyển của quái
    private final float speed;
    // Lượng máu của quái
    private final float health;
    // Kích thước của quái (tỉ lệ so với kích thước gốc)
    private final float scale;

    // Constructor khởi tạo thuộc tính cho mỗi loại quái
    EnemyType(String texturePath, float speed, float health, float scale) {
        this.texturePath = texturePath;
        this.speed = speed;
        this.health = health;
        this.scale = scale;
    }

    // Các phương thức getter để lấy thông tin của quái
    public String getTexturePath() { return texturePath; }  // Lấy đường dẫn texture
    public float getSpeed() { return speed; }               // Lấy tốc độ
    public float getHealth() { return health; }             // Lấy lượng máu
    public float getScale() { return scale; }               // Lấy tỉ lệ kích thước
}
