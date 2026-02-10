package com.springsciyon.business.rag.dto;

import com.hankcs.hanlp.dictionary.CustomDictionary;

import java.util.List;

import com.springsciyon.business.rag.filterdocid.DocumentSimpleInfo;
import lombok.Data;
@Data
public class Keymapper {
    private final CustomDictionary customDictionary;          // 已经开启自定义词典的分词器
    private final List<DocumentSimpleInfo> docInfoList;

    public Keymapper(CustomDictionary customDictionary, List<DocumentSimpleInfo> docInfoList) {
        this.customDictionary = customDictionary;
        this.docInfoList = docInfoList;

    }
}
