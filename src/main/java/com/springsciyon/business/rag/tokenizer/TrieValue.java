package com.springsciyon.business.rag.tokenizer;

import java.io.Serializable;

// 新增一个内部类，专门存分数 + 词性
public  class TrieValue implements Serializable {
    private static final long serialVersionUID = 1L;

    final double score;    // 存储的是 int(log(freq/1e6)+0.5) 的 double 形式
    final String pos;      // 词性：nr, d, v, etc.

    TrieValue(double score, String pos) {
        this.score = score;
        this.pos = pos != null ? pos : "";
    }
}
