package com.springsciyon.business.rag.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springsciyon.business.rag.entity.DocumentTagEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentTagMapper extends BaseMapper<DocumentTagEntity> {
    // 不用写任何方法，BaseMapper 已经包含 CRUD
}

