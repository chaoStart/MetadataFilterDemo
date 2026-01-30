package tokenizer;

import filterdocid.DocumentSimpleInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static dto.GetDefaultSqlDictionary.extractMetricTerms;

public class RetrieverDocInfo {

    public static void main(String[] args) throws Exception {
        // 1. 创建 Es 实例
        ESConnection es = new ESConnection();

        //2. 初始化实例
        Dealer  init_search_module = new Dealer(es);

        //3. 定义测试字符串
//        String question = "科远table智慧";
        String question = "sc231Ew和sc231aw产品手册的电源要求是什么？";
//        String question = "科远智慧和天龙集团是两个专业公司的名称";
        List<DocumentSimpleInfo> matchedDocInfo = extractMetricTerms(question.toLowerCase());
        List<String> docIds = new ArrayList<>();
        for (DocumentSimpleInfo matchedDocInfo1 : matchedDocInfo){
            docIds.add(matchedDocInfo1.getDocId());
        }
        Object embdMdl = new Object();
//        List<String> kbIds= Arrays.asList("569543200684343300,569543200684343331,569543200684343346,569599181288210433,569599181288210452,570302834370510851,571776832787972098,573980554738204672,574132862868881503,579910246719520778,580850286211989631,592546126405304321,601015020229427204,xiaokedoc,601678309210619904,602517898009444389,622500070151684096,633497471353716737,637881825510490114,643016622123057152,643168629605728256,654164630652387330,654164630652387331,654873987451650048,657864083554271232,662278992109010944,662564616024522752,663113530025050112,664778147681501184,667733660709126144,669201508733616128,670600431122055168,673746752365953026,674914906161774592,675106934150299648,675139515772272640,676533722286718983,678944246913531905,685512162448900098,685598654500634625,686249264737878016,686249264737878017,688494759540817920,689347001313165313,689347001313165316");
        List<String> kbIds = Arrays.asList(
                "569543200684343300",
                "569543200684343331",
                "569543200684343346",
                "784072908687638551"
        );
        int page = 1;
        int size = 10;
        double similarityThreshold = 0.2;
        double vectorSimilarityWeight = 0.3;
        int top = 10;
        boolean aggs =false ;
        Object rerankMdl = new Object();
        boolean highlight = false;
        // 4. 调用 tokenize 方法
        Map<String, Object> result = init_search_module.retriever(question,embdMdl, kbIds,page,size,similarityThreshold,
                vectorSimilarityWeight,top,docIds,aggs,rerankMdl,highlight);

        // 5. 打印结果`
        System.out.println("准备开始打印检索结果:******* \n");

        ArrayList<Map<String, Object>> chunks = (ArrayList<Map<String, Object>>) result.get("chunks");
        for (Map<String, Object> chunk : chunks) {
            System.out.println("chunk: " + chunk.get("content_with_weight")+"\n");
        }
    }
}