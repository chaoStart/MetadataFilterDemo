// SimpleTrie.java
package tokenizer;

import java.io.*;
import java.util.*;

/**
 * 一个轻量的 Trie 实现（支持序列化到文件与从文件加载）。
 * - key 存储为 String（已按 Python 原始 key_ 生成后的形式）
 * - value 存储为 Object（通常我们放 Value 或 Integer）
 */
public class SimpleTrie implements Serializable {
    private static final long serialVersionUID = 1L;

    // 改用 Map<String, Double> ，明确只存 log(freq)
    private Map<String, TrieValue> map;

    public SimpleTrie() {
        this.map = new HashMap<>();
    }

    public TrieValue get(String key) {
        return map.get(key);
    }

    public void put(String key, TrieValue value) {
        map.put(key, value);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    // 保留你原来的序列化方法（如果以后还想 save）
    public void save(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(new HashMap<>(this.map));  // 深拷贝一下更安全
        }
    }

    public boolean hasKeysWithPrefix(String prefix) {
        if (prefix == null || prefix.length() == 0) return false;
        for (String k : map.keySet()) {
            if (k.startsWith(prefix)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static SimpleTrie load(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object obj = ois.readObject();
            SimpleTrie t = new SimpleTrie();
            if (obj instanceof Map) {
                t.map = new HashMap<>((Map<String, TrieValue>) obj);
            }
            return t;
        }
    }

    /**
     * 从文件加载（反序列化）
     */
    @SuppressWarnings("unchecked")
    public static SimpleTrie loaddatrie(String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        fis.close();
        SimpleTrie t = new SimpleTrie();
        if (obj instanceof Map) {
            t.map = (Map<String, TrieValue>) obj;
        }
        return t;
    }
}
