package dto;

import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;

import java.util.List;
import lombok.Data;
@Data
public class Keymapper {
    private final ViterbiSegment segment;          // 已经开启自定义词典的分词器
    private final List<String> FileNames;        // 从数据库加载的指标名称列表
    public Keymapper(ViterbiSegment segment, List<String> FileNames) {
        this.segment = segment;
        this.FileNames = FileNames;
    }

    public ViterbiSegment getSegment() { return segment; }
    public List<String> getFileNames() { return FileNames; }

}
