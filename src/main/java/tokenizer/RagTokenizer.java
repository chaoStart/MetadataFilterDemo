package tokenizer;

import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.util.logging.Logger;
import java.lang.Math;

/**
 * RagTokenizer 的 Java 实现（参照你提供的 Python 代码）
 * Java 1.8 兼容
 */
public class RagTokenizer {
    private static final Logger logger = Logger.getLogger(RagTokenizer.class.getName());
    private  ESConnection esConn = new ESConnection(); // 依赖注入
    private boolean DEBUG = false;
    private int DENOMINATOR = 1000000;
    private String DIR_; // base dir + utils/res/huqie
    private String SPLIT_CHAR;
    private SimpleTrie trie_;
    public RagTokenizer() {
        this(false);
    }

    public RagTokenizer(boolean debug) {
        this.DEBUG = debug;
        this.DENOMINATOR = 1000000;
        this.DIR_ = getProjectBaseDirectory() + File.separator + "utils" + File.separator + "res" + File.separator + "huqie";
        // 与 Python 保持一致的 split 正则（注意 Java 字符串中需要双反斜杠）
        this.SPLIT_CHAR = "([ ,\\.<>/?;:'\\[\\]\\\\`!@#$%^&*\\(\\)\\{\\}\\|_+=《》，。？、；‘’：“”【】~！￥%……（）——-]+|[a-zA-Z0-9,\\.-]+)";
        // 构造时初始化（但内部会检查是否已加载）
        initializeTrie();
    }
    /**
     * 初始化 trie_，仅在 trie_ 为 null 时执行加载逻辑。
     * 线程安全：使用 synchronized 防止多线程重复初始化。
     */
    private synchronized void initializeTrie() {
        if (this.trie_ != null) {
            return;
        }

        String trieFileName = this.DIR_ + ".txt.trie";
        File trieFile = new File(trieFileName);

        // 我们不再尝试加载 .trie 文件，直接走 txt 加载
        this.trie_ = new SimpleTrie();

        String dictFileName = this.DIR_ + ".txt";
        File dictFile = new File(dictFileName);

        if (trieFile.exists()) {
            try {
                this.trie_ = trie_.loaddatrie(trieFileName);
                System.out.println("[HUQIE]: 载入 trie 文件成功: " + trieFileName);
                return;
            } catch (Exception ex) {
                logger.warning("[HUQIE]:Fail to load trie file " + trieFileName + ", build the default trie");
                this.trie_ = new SimpleTrie();
            }
        } else {
            loadDict_(dictFileName);
            // 可选：加载成功后保存为 .trie 供下次快速加载（如果你愿意）
            try {
                this.trie_.save(trieFileName);
                logger.info("[HUQIE]: 成功生成并保存 trie 文件: " + trieFileName);
            } catch (Exception e) {
                logger.warning("[HUQIE]: 保存 trie 文件失败: " + e.getMessage());
            }
            logger.warning("[HUQIE]: 未找到词典文件: " + dictFileName + "，tokenizer 将无词频信息！");
        }
    }
    // ---------- 辅助：返回项目根目录（和 Python get_project_base_directory 行为类似） ----------
    private String getProjectBaseDirectory() {
        try {
            // 取得当前类文件运行位置并上跳一级
            String path = new File(RagTokenizer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            File dir = new File(path);
            File parent = dir.getParentFile();
            if (parent != null) return parent.getAbsolutePath();
            return dir.getAbsolutePath();
        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }

    // key_：lower + utf-8 bytes string 表示风格（Python 的 str(...encode))，这里采用简单的 lower()UTF-8 bytes hex 表示以保证唯一
    private String key_(String line) {
        if (line == null) return null;
        try {
            String low = line.toLowerCase();
            byte[] bs = low.getBytes("UTF-8");
            // 用 hex 表示（类似 Python 的字节表示），但更简洁
            StringBuilder sb = new StringBuilder();
            for (byte b : bs) {
                int v = b & 0xFF;
                String hx = Integer.toHexString(v);
                if (hx.length() == 1) sb.append('0');
                sb.append(hx);
            }
            return sb.toString();
        } catch (Exception ex) {
            return line.toLowerCase();
        }
    }

    private String rkey_(String line) {
        if (line == null) return null;
        try {
            String reversed = "DD" + new StringBuilder(line.toLowerCase()).reverse().toString();
            byte[] bs = reversed.getBytes("UTF-8");
            StringBuilder sb = new StringBuilder();
            for (byte b : bs) {
                int v = b & 0xFF;
                String hx = Integer.toHexString(v);
                if (hx.length() == 1) sb.append('0');
                sb.append(hx);
            }
            return sb.toString();
        } catch (Exception ex) {
            return line;
        }
    }

    /**
     * loadDict_：从一个 dict 文件读取并填充 trie_，并保存到 cache 文件 (.trie)
     * Python 里每行格式像： word <space> frequency_as_float <space> tag
     */
    private void loadDict_(String dictPath) {
        File file = new File(dictPath);
        if (!file.exists()) {
            logger.warning("[HUQIE]: 词典文件不存在: " + dictPath);
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int loaded = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // 支持空格或制表符分割
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                String word = parts[0];
                String freqStr = parts[1];
                String tag = parts.length > 2 ? parts[2] : null;
                try {
                    double rawFreq = Double.parseDouble(freqStr);
                    if (rawFreq <= 0) continue;

                    // 关键！完全复现原始 Python 逻辑
                    double score = rawFreq / (double) this.DENOMINATOR;
                    double F = Math.log(score);           // ln(score)
                    int F_int = (int) (F + 0.5);          // 四舍五入到整数

                    String key = key_(word);
                    String rkey = rkey_(word);            // 反向 key

                    // 正向：存 (F_int, 词性)
                    // 我们这里只关心分数，所以只存 F_int（作为 Double 也行）
//                    this.trie_.put(key, (double) F_int);
                    this.trie_.put(key, new TrieValue(F_int, tag));

                    // 反向 key 存 1（用于后缀匹配？原始项目有这个逻辑）
                    this.trie_.put(rkey, new TrieValue(1.0,""));

                    loaded++;
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            logger.info("[HUQIE]: 从词典成功加载 " + loaded + " 条记录，生成对数评分");
        } catch (Exception e) {
            logger.warning("[HUQIE]: 加载词典失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // external API：loadUserDict
    public void loadUserDict(String fnm) {
        try {
            this.trie_ = SimpleTrie.load(fnm + ".trie");
            return;
        } catch (Exception ex) {
            this.trie_ = new SimpleTrie();
        }
        this.loadDict_(fnm);
    }


    public void addUserDict(String fnm) {
        this.loadDict_(fnm);
    }

    // strQ2B: 全角转半角
    public String _strQ2B(String ustring) {
        if (ustring == null) return null;
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < ustring.length(); i++) {
            char uchar = ustring.charAt(i);
            int inside_code = uchar;
            if (inside_code == 0x3000) {
                inside_code = 0x0020;
            } else {
                inside_code -= 0xfee0;
            }
            if (inside_code < 0x0020 || inside_code > 0x7e) {
                r.append(uchar);
            } else {
                r.append((char) inside_code);
            }
        }
        return r.toString();
    }

    // trad -> simp using opencc4j
    public String _tradi2simp(String line) {
        if (line == null) return null;
        try {
            return ZhConverterUtil.toSimple(line);
        } catch (Exception ex) {
            return line;
        }
    }

    // ---------- 深度优先分词搜索 dfs_（尽量保持 Python 行为） ----------
    /**
     * dfs_:
     * chars: char array of the token string (tk)
     * s: start index
     * preTks: list of Pair<String,Object> (token, valueFromTrie)
     * tkslist: output list of possible preTks sequences (List<List<Pair>>)
     */
    public int dfs_(char[] chars, int s, List<Pair<String, Object>> preTks, List<List<Pair<String, Object>>> tkslist) {
        return dfs_(chars, s, preTks, tkslist, 0, new HashMap<String, Integer>());
    }

    private int dfs_(char[] chars, int s, List<Pair<String, Object>> preTks, List<List<Pair<String, Object>>> tkslist, int _depth, Map<String, Integer> _memo) {
        if (_memo == null) _memo = new HashMap<String, Integer>();
        final int MAX_DEPTH = 10;
        if (_depth > MAX_DEPTH) {
            if (s < chars.length) {
                List<Pair<String, Object>> copyPretks = deepCopyPairs(preTks);
                String remaining = new String(chars, s, chars.length - s);
                copyPretks.add(new Pair<String, Object>(remaining, new Value(-12, "")));
                tkslist.add(copyPretks);
            }
            return s;
        }

        String stateKey;
        if (preTks != null && preTks.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Pair<String, Object> p : preTks) {
                sb.append(p.getLeft());
                sb.append("|");
            }
            stateKey = s + ":" + sb.toString();
        } else {
            stateKey = s + ":#";
        }
        if (_memo.containsKey(stateKey)) {
            return _memo.get(stateKey);
        }

        int res = s;
        if (s >= chars.length) {
            tkslist.add(preTks);
            _memo.put(stateKey, s);
            return s;
        }
        if (s < chars.length - 4) {
            boolean isRepetitive = true;
            char charToCheck = chars[s];
            for (int i = 1; i < 5; i++) {
                if (s + i >= chars.length || chars[s + i] != charToCheck) {
                    isRepetitive = false;
                    break;
                }
            }
            if (isRepetitive) {
                int end = s;
                while (end < chars.length && chars[end] == charToCheck) end++;
                int mid = s + Math.min(10, end - s);
                String t = new String(chars, s, mid - s);
                String k = this.key_(t);
                List<Pair<String, Object>> copyPretks = deepCopyPairs(preTks);
                if (this.trie_.containsKey(k)) {
                    copyPretks.add(new Pair<String, Object>(t, this.trie_.get(k)));
                } else {
                    copyPretks.add(new Pair<String, Object>(t, new Value(-12, "")));
                }
                int nextRes = dfs_(chars, mid, copyPretks, tkslist, _depth + 1, _memo);
                res = Math.max(res, nextRes);
                _memo.put(stateKey, res);
                return res;
            }
        }

        int S = s + 1;
        if (s + 2 <= chars.length) {
            String t1 = new String(chars, s, 1);
            String t2 = new String(chars, s, 2);
            if (this.trie_.hasKeysWithPrefix(this.key_(t1)) && !this.trie_.hasKeysWithPrefix(this.key_(t2))) {
                S = s + 2;
            }
        }
        if (preTks.size() > 2 && preTks.get(preTks.size() - 1).getLeft().length() == 1
                && preTks.get(preTks.size() - 2).getLeft().length() == 1
                && preTks.get(preTks.size() - 3).getLeft().length() == 1) {
            String t1 = preTks.get(preTks.size() - 1).getLeft() + new String(chars, s, 1);
            if (this.trie_.hasKeysWithPrefix(this.key_(t1))) {
                S = s + 2;
            }
        }

        for (int e = S; e <= chars.length; e++) {
            String t = new String(chars, s, e - s);
            String k = this.key_(t);
            if (e > s + 1 && !this.trie_.hasKeysWithPrefix(k)) {
                break;
            }
            if (this.trie_.containsKey(k)) {
                List<Pair<String, Object>> pretks = deepCopyPairs(preTks);
                pretks.add(new Pair<String, Object>(t, this.trie_.get(k)));
                res = Math.max(res, dfs_(chars, e, pretks, tkslist, _depth + 1, _memo));
            }
        }

        if (res > s) {
            _memo.put(stateKey, res);
            return res;
        }

        String t = new String(chars, s, 1);
        String k = this.key_(t);
        List<Pair<String, Object>> copyPretks = deepCopyPairs(preTks);
        if (this.trie_.containsKey(k)) {
            copyPretks.add(new Pair<String, Object>(t, this.trie_.get(k)));
        } else {
            copyPretks.add(new Pair<String, Object>(t, new Value(-12, "")));
        }
        int result = dfs_(chars, s + 1, copyPretks, tkslist, _depth + 1, _memo);
        _memo.put(stateKey, result);
        return result;
    }

    // deep copy helpers for Pair lists
    private List<Pair<String, Object>> deepCopyPairs(List<Pair<String, Object>> src) {
        List<Pair<String, Object>> out = new ArrayList<Pair<String, Object>>();
        if (src == null) return out;
        for (Pair<String, Object> p : src) {
            out.add(new Pair<String, Object>(p.getLeft(), p.getRight()));
        }
        return out;
    }

    // score_ : 接受 List<Pair<String,Object>> 每个 Pair 的 right 期望为 Value (freqExp, tag)
    public Pair<List<String>, Double> score_(List<Pair<String, Object>> tfts) {
        double B = 30.0;
        double F = 0.0;
        double L = 0.0;
        List<String> tks = new ArrayList<String>();
        for (Pair<String, Object> p : tfts) {
            String tk = p.getLeft();
            Object val = p.getRight();
            int freq = 0;
            String tag = "";
            if (val instanceof Value) {
                freq = ((Value) val).freqExp;
                tag = ((Value) val).tag;
            } else if (val instanceof Integer) {
                freq = (Integer) val;
            }
            F += freq;
            if (tk.length() >= 2) L += 1.0;
            tks.add(tk);
        }
        if (tks.size() == 0) {
            return new Pair<List<String>, Double>(tks, 0.0);
        }
        L = L / tks.size();
        double score = B / tks.size() + L + F;
        if (this.DEBUG) {
            logger.info("[SC] " + tks.toString() + " " + tks.size() + " " + L + " " + F + " " + score);
        }
        return new Pair<List<String>, Double>(tks, score);
    }

    // sortTks_
    public List<Pair<List<String>, Double>> sortTks_(List<List<Pair<String, Object>>> tkslist) {
        List<Pair<List<String>, Double>> res = new ArrayList<Pair<List<String>, Double>>();
        for (List<Pair<String, Object>> tfts : tkslist) {
            Pair<List<String>, Double> p = score_(tfts);
            res.add(new Pair<List<String>, Double>(p.getLeft(), p.getRight()));
        }
        // sort by score desc
        Collections.sort(res, new Comparator<Pair<List<String>, Double>>() {
            public int compare(Pair<List<String>, Double> a, Pair<List<String>, Double> b) {
                return Double.compare(b.getRight(), a.getRight());
            }
        });
        return res;
    }

    // merge_ : 合并可能被 split char 切开的 token
    public String merge_(String tks) {
        if (tks == null) return null;
        tks = tks.replaceAll("[ ]+", " ");
        String[] parts = tks.trim().split("\\s+");
        List<String> res = new ArrayList<String>();
        int s = 0;
        while (true) {
            if (s >= parts.length) break;
            int E = s + 1;
            for (int e = s + 2; e < Math.min(parts.length + 2, s + 6); e++) {
                String tk = "";
                for (int i = s; i < e && i < parts.length; i++) tk += parts[i];
                if (Pattern.compile(this.SPLIT_CHAR).matcher(tk).find() && this.freq(tk) > 0) {
                    E = e;
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i = s; i < E && i < parts.length; i++) sb.append(parts[i]);
            res.add(sb.toString());
            s = E;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < res.size(); i++) {
            if (i > 0) out.append(" ");
            out.append(res.get(i));
        }
        return out.toString();
    }

    // maxForward_
    public Pair<List<String>, Double> maxForward_(String line) {
        List<Pair<String, Object>> res = new ArrayList<Pair<String, Object>>();
        int s = 0;
        while (s < line.length()) {
            int e = s + 1;
            String t = line.substring(s, e);
            while (e < line.length() && this.trie_.hasKeysWithPrefix(this.key_(t))) {
                e += 1;
                t = line.substring(s, e);
            }
            while (e - 1 > s && !this.trie_.containsKey(this.key_(t))) {
                e -= 1;
                t = line.substring(s, e);
            }
            if (this.trie_.containsKey(this.key_(t))) {
                res.add(new Pair<String, Object>(t, this.trie_.get(this.key_(t))));
            } else {
                res.add(new Pair<String, Object>(t, 0));
            }
            s = e;
        }
        return score_(res);
    }

    // maxBackward_
    public Pair<List<String>, Double> maxBackward_(String line) {
        List<Pair<String, Object>> res = new ArrayList<Pair<String, Object>>();
        int s = line.length() - 1;
        while (s >= 0) {
            int e = s + 1;
            String t = line.substring(s, e);
            while (s > 0 && this.trie_.hasKeysWithPrefix(this.rkey_(t))) {
                s -= 1;
                t = line.substring(s, e);
            }
            while (s + 1 < e && !this.trie_.containsKey(this.key_(t))) {
                s += 1;
                t = line.substring(s, e);
            }
            if (this.trie_.containsKey(this.key_(t))) {
                res.add(new Pair<String, Object>(t, this.trie_.get(this.key_(t))));
            } else {
                res.add(new Pair<String, Object>(t, 0));
            }
            s -= 1;
        }
        // reverse order then score_
        List<Pair<String, Object>> rev = new ArrayList<Pair<String, Object>>();
        for (int i = res.size() - 1; i >= 0; i--) rev.add(res.get(i));
        return score_(rev);
    }

    // _split_by_lang: 返回 List<Pair<String,Boolean>> 表示 text + isChinese
    public List<Pair<String, Boolean>> _split_by_lang(String line) {
        List<Pair<String, Boolean>> out = new ArrayList<>();

        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]+|[a-zA-Z0-9]+");
        Matcher m = p.matcher(line);

        while (m.find()) {
            String token = m.group();
            boolean zh = is_chinese(token.charAt(0));
            out.add(new Pair<>(token, zh));
        }
        return out;
    }

    // tokenize: 使用 ES _analyze（ik_smart），并返回 merge_ 后的结果
    public String tokenize(String line) {
        Pattern p = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);
        String clean_line = p.matcher(line).replaceAll(" ").trim();

        List<String> res = new ArrayList<>();

        try {
            line = this._strQ2B(clean_line).toLowerCase();
            line = this._tradi2simp(line);

            List<Pair<String, Boolean>> arr = this._split_by_lang(line);

            for (Pair<String, Boolean> pair : arr) {
                String part = pair.getLeft();

                try {
                    if (!pair.getRight()) {
                        // ===== 中文分词：ES ik =====
                        List<String> tokens = new ArrayList<>();

                        AnalyzeRequest req = AnalyzeRequest.of(a -> a
                                .analyzer("ik_max_word")
                                .text(part)
                        );

                        AnalyzeResponse resp = this.esConn.getClient()
                                .indices().analyze(req);

                        if (resp.tokens() != null) {
                            for (AnalyzeToken t : resp.tokens()) {
                                tokens.add(t.token());
                            }
                        }

                        res.add(String.join(" ", tokens));

                    } else {
                        // ===== 英文/数字分词：RAGFLOW逻辑 =====
                        if (part.length() < 2 || part.matches("[a-z\\.-]+") || part.matches("[0-9\\.-]+")) {
                            res.add(part);
                            continue;
                        }

                        Pair<List<String>, Double> tks_obj = maxForward_(part);
                        List<String> tks = tks_obj.getLeft();
                        Pair<List<String>, Double> tks1_obj = maxBackward_(part);
                        List<String> tks1 = tks1_obj.getLeft();

                        int i = 0, j = 0;
                        int _i = 0, _j = 0;
                        int same = 0;

                        while (i + same < tks1.size() && j + same < tks.size()
                                && tks1.get(i + same).equals(tks.get(j + same))) {
                            same++;
                        }

                        if (same > 0) {
                            res.add(String.join(" ", tks.subList(j, j + same)));
                        }

                        _i = i + same;
                        _j = j + same;
                        j = _j + 1;
                        i = _i + 1;

                        while (i < tks1.size() && j < tks.size()) {
                            String tk1 = String.join("", tks1.subList(_i, i));
                            String tk = String.join("", tks.subList(_j, j));

                            if (!tk1.equals(tk)) {
                                if (tk1.length() > tk.length()) {
                                    j++;
                                } else {
                                    i++;
                                }
                                continue;
                            }

                            if (!tks1.get(i).equals(tks.get(j))) {
                                i++;
                                j++;
                                continue;
                            }

                            String segment = String.join("", tks.subList(_j, j));

                            List<Pair<String, Object>> preTksTemp = new ArrayList<>();
                            List<List<Pair<String, Object>>> tkslist = new ArrayList<>();
                            this.dfs_(segment.toCharArray(), 0, preTksTemp, tkslist);

                            List<Pair<List<String>, Double>> sortedTks = this.sortTks_(tkslist);
                            res.add(String.join(" ", sortedTks.get(0).getLeft()));

                            same = 1;
                            while (i + same < tks1.size() && j + same < tks.size()
                                    && tks1.get(i + same).equals(tks.get(j + same))) {
                                same++;
                            }

                            if (same > 0) {
                                res.add(String.join(" ", tks.subList(j, j + same)));
                            }

                            _i = i + same;
                            _j = j + same;
                            j = _j + 1;
                            i = _i + 1;
                        }

                        if (_i < tks1.size()) {
                            String tail1 = String.join("", tks1.subList(_i, tks1.size()));
                            String tail = String.join("", tks.subList(_j, tks.size()));

                            if (!tail1.equals(tail)) {
                                throw new IllegalStateException("Tail mismatch: " + tail1 + " vs " + tail);
                            }

                            List<Pair<String, Object>> preTksTail = new ArrayList<>();
                            List<List<Pair<String, Object>>> tkslistTail = new ArrayList<>();
                            this.dfs_(tail.toCharArray(), 0, preTksTail, tkslistTail);

                            List<Pair<List<String>, Double>> sortedTail = this.sortTks_(tkslistTail);
                            if (!sortedTail.isEmpty()) {
                                res.add(String.join(" ", sortedTail.get(0).getLeft()));
                            } else {
                                res.add(tail);
                            }
                        }
                    }

                } catch (Exception e) {
                    // 单段失败兜底
                    logger.severe("[TKS-ERROR] segment=\"" + part + "\" : " + e.getMessage());
                    res.add(part); // fallback：原样加入
                }
            }

        } catch (Exception e) {
            // 整体失败兜底
            logger.severe("[TKS-FATAL] tokenize failed: " + e.getMessage());
            return line == null ? "" : line;
        }

        String result = String.join(" ", res);
        logger.fine("[TKS] " + this.merge_(result));
        return this.merge_(result);
    }

    // fine_grained_tokenize
    public String fine_grained_tokenize(String tks) {
        if (tks == null) return null;
        String[] arr = tks.split("\\s+");
        List<String> tksList = new ArrayList<String>(Arrays.asList(arr));
        int zh_num = 0;
        for (String c : tksList) {
            if (c != null && c.length() > 0 && is_chinese(c.charAt(0))) zh_num++;
        }
        if (zh_num < tksList.size() * 0.2) {
            List<String> res = new ArrayList<String>();
            for (String tk : tksList) {
                String[] pieces = tk.split("/");
                for (String p : pieces) {
                    if (p.length() > 0) res.add(p);
                }
            }
            return String.join(" ", res);
        }

        List<String> res = new ArrayList<String>();
        for (String tk : tksList) {
            if (tk.length() < 3 || tk.matches("[0-9,\\.-]+$")) {
                res.add(tk);
                continue;
            }
            List<List<Pair<String, Object>>> tkslist = new ArrayList<List<Pair<String, Object>>>();
            if (tk.length() > 10) {
                List<Pair<String, Object>> one = new ArrayList<Pair<String, Object>>();
                one.add(new Pair<String, Object>(tk, null));
                tkslist.add(one);
            } else {
                this.dfs_(tk.toCharArray(), 0, new ArrayList<Pair<String, Object>>(), tkslist);
            }
            if (tkslist.size() < 2) {
                res.add(tk);
                continue;
            }
            List<Pair<List<String>, Double>> sorted = this.sortTks_(tkslist);
            // Python 中取第二项 sorted(...)[1][0]，注意越界检查
            List<String> stk = null;
            if (sorted.size() > 1) {
                stk = sorted.get(1).getLeft();
            } else {
                stk = sorted.get(0).getLeft();
            }
            String joined;
            if (stk.size() == tk.length()) {
                joined = tk;
            } else {
                if (tk.matches("[a-z\\.-]+$")) {
                    boolean shortFound = false;
                    for (String t : stk) {
                        if (t.length() < 3) {
                            shortFound = true;
                            break;
                        }
                    }
                    if (shortFound) {
                        joined = tk;
                    } else {
                        joined = String.join(" ", stk);
                    }
                } else {
                    joined = String.join(" ", stk);
                }
            }
            res.add(joined);
        }
        return String.join(" ", res);
    }

    // freq: 从 trie 中取值
    public int freq(String tk) {
        if (tk == null || tk.isEmpty()) return 0;
        String k = key_(tk);
        TrieValue value = this.trie_.get(k);
        if (value == null) return 0;
        return (int) (Math.exp(value.score) * this.DENOMINATOR + 0.5);
    }

    /**
     * 返回词性，和 Python tag() 方法行为完全一致
     * @param tk 输入词
     * @return 词性字符串，不存在返回空字符串 ""
     */
    public String tag(String tk) {
        if (tk == null || tk.trim().isEmpty()) {
            return "";
        }
        String k = key_(tk);
        TrieValue value = this.trie_.get(k);
        if (value == null) {
            return "";
        }
        return value.pos;
    }

    // helper: is Chinese char
    public static boolean is_chinese(char s) {
        return s >= '\u4e00' && s <= '\u9fa5';
    }

    // ----- 内部静态类：Value，Pair （简单泛型 Pair） -----
    public static class Value implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public int freqExp;
        public String tag;

        public Value(int f, String t) {
            this.freqExp = f;
            this.tag = t;
        }
    }

    // 简单 Pair 类（左、右）
    public static class Pair<L, R> {
        private L left;
        private R right;

        public Pair(L l, R r) {
            this.left = l;
            this.right = r;
        }

        public L getLeft() { return left; }
        public R getRight() { return right; }
        public void setLeft(L l) { this.left = l; }
        public void setRight(R r) { this.right = r; }
    }
}
