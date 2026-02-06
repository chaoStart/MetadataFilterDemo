package dto;

import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import filterdocid.DocumentSimpleInfo;
import lombok.Data;
@Data
public class Keymapper {
    // ✅ 新增：快速映射表
    private final Map<String, DocumentSimpleInfo> fileNameMap;
    private final ViterbiSegment segment;          // 已经开启自定义词典的分词器
    private final List<String> FileNames;        // 从数据库加载的指标名称列表
    private final List<DocumentSimpleInfo> docList;

    public Keymapper(ViterbiSegment segment, List<String> FileNames, List<DocumentSimpleInfo> docList) {
        this.segment = segment;
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

    public ViterbiSegment getSegment() { return segment; }
    public List<String> getFileNames() { return FileNames; }
    public List<DocumentSimpleInfo> getDocList() {
        return docList;
    }
    public Map<String, DocumentSimpleInfo> getFileNameMap() {
        return fileNameMap;
    }
}
