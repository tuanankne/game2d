package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

public class MapProgress {
    private static final String PREF_NAME = "map_progress";
    private static final String MAP_DATA_KEY = "map_data";
    private static MapProgress instance;
    private final Preferences prefs;
    private final Map<String, MapData> mapData;
    
    private static class MapData {
        public boolean unlocked;
        public int stars;
        
        public MapData() {
            this.unlocked = false;
            this.stars = 0;
        }
        
        public MapData(boolean unlocked, int stars) {
            this.unlocked = unlocked;
            this.stars = stars;
        }
    }
    
    private MapProgress() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        mapData = new HashMap<>();
        loadData();
        
        // Mặc định map đầu tiên luôn mở
        if (!isMapUnlocked(MapType.MAP1)) {
            setMapProgress(MapType.MAP1, true, 0);
        }
    }
    
    public static MapProgress getInstance() {
        if (instance == null) {
            instance = new MapProgress();
        }
        return instance;
    }
    
    private void loadData() {
        String jsonData = prefs.getString(MAP_DATA_KEY, "");
        if (!jsonData.isEmpty()) {
            Json json = new Json();
            @SuppressWarnings("unchecked")
            HashMap<String, MapData> loaded = json.fromJson(HashMap.class, jsonData);
            if (loaded != null) {
                mapData.putAll(loaded);
            }
        }
    }
    
    private void saveData() {
        Json json = new Json();
        String jsonData = json.toJson(mapData);
        prefs.putString(MAP_DATA_KEY, jsonData);
        prefs.flush();
    }
    
    public boolean isMapUnlocked(MapType mapType) {
        MapData data = mapData.get(mapType.name());
        return data != null && data.unlocked;
    }
    
    public int getMapStars(MapType mapType) {
        MapData data = mapData.get(mapType.name());
        return data != null ? data.stars : 0;
    }
    
    public void setMapProgress(MapType mapType, boolean unlocked, int stars) {
        mapData.put(mapType.name(), new MapData(unlocked, stars));
        saveData();
    }
    
    public void updateMapStars(MapType mapType, int stars) {
        if (stars >= 0 && stars <= 3) {
            MapData data = mapData.get(mapType.name());
            if (data == null) {
                data = new MapData(true, stars);
            } else {
                data.stars = stars;
            }
            mapData.put(mapType.name(), data);
            saveData();
            
            // Mở khóa map tiếp theo nếu đạt được sao
            if (stars > 0) {
                unlockNextMap(mapType);
            }
        }
    }
    
    private void unlockNextMap(MapType currentMap) {
        MapType[] maps = MapType.values();
        for (int i = 0; i < maps.length - 1; i++) {
            if (maps[i] == currentMap) {
                setMapProgress(maps[i + 1], true, 0);
                break;
            }
        }
    }
}
