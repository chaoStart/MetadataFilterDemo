package com.springsciyon.business.rag.tokenizer;

import com.springsciyon.business.rag.dto.GetDocIdInfoByCustomDictionary;
import com.springsciyon.business.rag.filterdocid.DocumentSimpleInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RetrieverDocInfo {

    public static void main(String[] args) throws Exception {
        // 1. 创建 Es 实例
        ESConnection es = new ESConnection();

        //2. 初始化实例
        Dealer  init_search_module = new Dealer(es);

         GetDocIdInfoByCustomDictionary extractMetricTerms = new GetDocIdInfoByCustomDictionary();

        //3. 定义测试字符串
//        String question = "科远table智慧";
        String question = "sc231Ew和SC8000-sc231aw产品手册的电源要求是什么？";
//        String question = "科远智慧和天龙集团是两个专业公司的名称";
        List<DocumentSimpleInfo> matchedDocInfo = extractMetricTerms.extractMetricTerms(question.toLowerCase());
        List<String> docIds = new ArrayList<>();
        for (DocumentSimpleInfo matchedDocInfo1 : matchedDocInfo){
            docIds.add(matchedDocInfo1.getDocId());
        }
        Object embdMdl = new Object();
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