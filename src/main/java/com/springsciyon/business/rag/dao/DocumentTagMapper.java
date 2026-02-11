package com.springsciyon.business.rag.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springsciyon.business.rag.entity.DocumentTagEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DocumentTagMapper extends BaseMapper<DocumentTagEntity> {
    // 不用写CRUD方法，BaseMapper 已经包含 CRUD
    // 查询文档数量和最后更新时间
    Map<String, Object> selectCountAndMaxTime();

    // 查询 doc_id、file_name、metadata_list
    List<DocumentTagEntity> selectSimpleInfo();

    // 插入或更新（根据 doc_id 唯一键）
    void insertOrUpdate(DocumentTagEntity entity);
}

