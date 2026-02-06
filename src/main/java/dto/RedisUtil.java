package dto;

import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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
}
