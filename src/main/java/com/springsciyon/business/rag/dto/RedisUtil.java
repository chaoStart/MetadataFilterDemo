package com.springsciyon.business.rag.dto;

import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class RedisUtil {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Jedis getJedis() {
        return new Jedis(REDIS_HOST, REDIS_PORT);
    }

    public static void set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, value);
        }
    }

    /**
     * 存储 Set 类型数据到 Redis
     * @param key Redis键
     * @param set 要存储的Set集合
     */
    public static <T> void setSet(String key, Set<T> set) throws Exception {
        String json = mapper.writeValueAsString(set);
        set(key, json);
    }

    public static String get(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        }
    }

    public static <T> void setObject(String key, T obj) throws Exception {
        set(key, mapper.writeValueAsString(obj));
    }

    public static <T> T getObject(String key, Class<T> clazz) throws Exception {
        String json = get(key);
        if (json == null) return null;
        return mapper.readValue(json, clazz);
    }

    public static <T> List<T> getList(String key, Class<T> clazz) throws Exception {
        String json = get(key);
        if (json == null) return null;
        return mapper.readValue(json,
                mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    /**
     * 从 Redis 获取 Set 类型数据
     * @param key Redis键
     * @param clazz 元素类型
     * @return Set<T>
     */
    public static <T> Set<T> getSet(String key, Class<T> clazz) throws Exception {
        String json = get(key);
        if (json == null) return null;
        return mapper.readValue(json,
                mapper.getTypeFactory().constructCollectionType(Set.class, clazz));  // ✅ List改为Set
    }
    /**
     * 存储 Map 类型数据到 Redis
     * @param key Redis键
     * @param map 要存储的Map
     */
    public static <K, V> void setMap(String key, Map<K, V> map) throws Exception {
        String json = mapper.writeValueAsString(map);
        set(key, json);
    }

    /**
     * 从 Redis 获取 Map 类型数据
     * @param key Redis键
     * @param keyClass key类型
     * @param valueClass value类型
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> getMap(String key, Class<K> keyClass, Class<V> valueClass) throws Exception {
        String json = get(key);
        if (json == null) return null;
        return mapper.readValue(json,
                mapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
    }

    // ==================== Redis Hash 操作方法 ====================

    /**
     * 使用 Redis Hash 存储 filename -> DocumentSimpleInfo 映射
     * @param hashKey Redis Hash 的 key
     * @param field Hash 中的 field（即 filename）
     * @param value 要存储的对象
     */
    public static <T> void hset(String hashKey, String field, T value) throws Exception {
        try (Jedis jedis = getJedis()) {
            String json = mapper.writeValueAsString(value);
            jedis.hset(hashKey, field, json);
        }
    }

    /**
     * 批量存储到 Redis Hash
     * @param hashKey Redis Hash 的 key
     * @param map 要存储的 Map<String, T>
     */
    public static <T> void hmset(String hashKey, Map<String, T> map) throws Exception {
        try (Jedis jedis = getJedis()) {
            Map<String, String> jsonMap = new HashMap<>();
            for (Map.Entry<String, T> entry : map.entrySet()) {
                jsonMap.put(entry.getKey(), mapper.writeValueAsString(entry.getValue()));
            }
            if (!jsonMap.isEmpty()) {
                jedis.hset(hashKey, jsonMap);
            }
        }
    }

    /**
     * 从 Redis Hash 获取单个字段
     * @param hashKey Redis Hash 的 key
     * @param field Hash 中的 field
     * @param clazz 返回对象类型
     * @return 反序列化后的对象
     */
    public static <T> T hget(String hashKey, String field, Class<T> clazz) throws Exception {
        try (Jedis jedis = getJedis()) {
            String json = jedis.hget(hashKey, field);
            if (json == null) return null;
            return mapper.readValue(json, clazz);
        }
    }

    /**
     * 从 Redis Hash 批量获取多个字段（使用 HMGET 命令）
     * @param hashKey Redis Hash 的 key
     * @param fields 要查询的 field 列表
     * @param clazz 返回对象类型
     * @return 对应的对象列表（顺序与 fields 一致，不存在的返回 null）
     */
    public static <T> List<T> hmget(String hashKey, List<String> fields, Class<T> clazz) throws Exception {
        if (fields == null || fields.isEmpty()) {
            return new ArrayList<>();
        }
        try (Jedis jedis = getJedis()) {
            List<String> jsonList = jedis.hmget(hashKey, fields.toArray(new String[0]));
            List<T> result = new ArrayList<>();
            for (String json : jsonList) {
                if (json != null) {
                    result.add(mapper.readValue(json, clazz));
                }
            }
            return result;
        }
    }

    /**
     * 删除 Redis Hash
     * @param hashKey Redis Hash 的 key
     */
    public static void delHash(String hashKey) {
        try (Jedis jedis = getJedis()) {
            jedis.del(hashKey);
        }
    }

    /**
     * 检查 Redis Hash 是否存在
     * @param hashKey Redis Hash 的 key
     * @return 是否存在
     */
    public static boolean hexists(String hashKey) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(hashKey);
        }
    }

    /**
     * 批量存储到 Redis Hash，值为 List<T> 类型
     * 适用于一个术语对应多个文档的场景
     * @param hashKey Redis Hash 的 key
     * @param map Map<String, List<T>>，field -> 文档列表
     */
    public static <T> void hmsetList(String hashKey, Map<String, List<T>> map) throws Exception {
        try (Jedis jedis = getJedis()) {
            Map<String, String> jsonMap = new HashMap<>();
            for (Map.Entry<String, List<T>> entry : map.entrySet()) {
                jsonMap.put(entry.getKey(), mapper.writeValueAsString(entry.getValue()));
            }
            if (!jsonMap.isEmpty()) {
                jedis.hset(hashKey, jsonMap);
            }
        }
    }

    /**
     * 从 Redis Hash 批量获取多个字段，每个字段的值是 List<T> 类型
     * 适用于一个术语对应多个文档的场景
     * @param hashKey Redis Hash 的 key
     * @param fields 要查询的 field 列表（术语列表）
     * @param elementClass 列表元素类型（如 DocumentSimpleInfo.class）
     * @return Map<String, List<T>>，field -> 对应的文档列表
     */
    public static <T> Map<String, List<T>> hmgetList(String hashKey, List<String> fields, Class<T> elementClass) throws Exception {
        Map<String, List<T>> result = new HashMap<>();
        if (fields == null || fields.isEmpty()) {
            return result;
        }
        try (Jedis jedis = getJedis()) {
            List<String> jsonList = jedis.hmget(hashKey, fields.toArray(new String[0]));
            for (int i = 0; i < fields.size(); i++) {
                String json = jsonList.get(i);
                if (json != null && !json.trim().isEmpty()) {
                    List<T> list = mapper.readValue(json,
                        mapper.getTypeFactory().constructCollectionType(List.class, elementClass));
                    result.put(fields.get(i), list);
                }
            }
        }
        return result;
    }

}
