package dto;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import filterdocid.DocumentSimpleInfo;
import lombok.Data;
@Data
public class Keymapper {
    private final Map<String, DocumentSimpleInfo> fileNameMap;  // ✅ 新增：快速映射表
    private final CustomDictionary customDictionary;          // 已经开启自定义词典的分词器
    private final Set<String> FileNames;        // 从数据库加载的指标名称列表
    private final List<DocumentSimpleInfo> docList;

    public Keymapper(CustomDictionary customDictionary, Set<String> FileNames, List<DocumentSimpleInfo> docList) {
        this.customDictionary = customDictionary;
        this.FileNames = FileNames;
        this.docList = docList;
        // 构建 fileName -> DocumentSimpleInfo 映射
        this.fileNameMap = new HashMap<>();
        for (DocumentSimpleInfo d : docList) {
            if (d.getFileName() != null) {
                fileNameMap.put(d.getFileName().toLowerCase(), d);
            }
        }
    }
}
