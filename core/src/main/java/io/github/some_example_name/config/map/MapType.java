package io.github.some_example_name.config.map;

public enum MapType {
    MAP1("map1/map1.tmx", "path"),
    MAP2("map1/map2.tmx", "path"),
    MAP3("map1/map3.tmx", "path"),
    MAP4("map1/map4.tmx", "path"),
    MAP5("map1/map5.tmx", "path"),
    MAP6("map1/map6.tmx", "path"),
    MAP7("map1/map7.tmx", "path"),
    MAP8("map1/map8.tmx", "path");

    private final String mapPath;
    private final String pathLayerName;

    MapType(String mapPath, String pathLayerName) {
        this.mapPath = mapPath;
        this.pathLayerName = pathLayerName;
    }

    public String getMapPath() {
        return mapPath;
    }

    public String getPathLayerName() {
        return pathLayerName;
    }
}
