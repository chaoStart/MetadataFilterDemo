package dto;

import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
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
}
