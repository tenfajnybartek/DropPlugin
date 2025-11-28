package pl.tenfajnybartek.dropplugin.utils;

import java.util.*;

public final class MapUtils {
    private MapUtils() {
    }

    public static String serializeList(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : set) {
            stringBuilder.append(string).append("@");
        }
        return stringBuilder.length() > 0 ? stringBuilder.substring(0, stringBuilder.length() - 1) : "";
    }

    public static Set<String> deserializeList(String serializedData) {
        Set<String> set = new HashSet<>();
        if (serializedData != null && !serializedData.isEmpty()) {
            String[] split = serializedData.split("@");
            Collections.addAll(set, split);
        }
        return set;
    }

    public static String serializeMap(HashMap<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("@");
        }
        return stringBuilder.length() > 0 ? stringBuilder.substring(0, stringBuilder.length() - 1) : "";
    }

    public static HashMap<String, Integer> deserializeMap(String serializedData) {
        HashMap<String, Integer> map = new HashMap<>();
        if (serializedData != null && !serializedData.isEmpty()) {
            String[] split = serializedData.split("@");
            for (String string : split) {
                String[] mapSplit = string.split("=");
                if (mapSplit.length == 2) {
                    try {
                        map.put(mapSplit[0], Integer.parseInt(mapSplit[1]));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return map;
    }
}
