package com.springsciyon.business.rag.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springsciyon.business.rag.entity.DocumentTagEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DocumentTagMapper extends BaseMapper<DocumentTagEntity> {
    // 不用写CRUD方法，BaseMapper 已经包含 CRUD
    // 查询文档数量和最后更新时间
//    @Select("SELECT COUNT(*) AS c, IFNULL(MAX(update_time),0) AS t FROM DocumentTag")
//    Map<String, Object> selectCountAndMaxTime();
//    // 查询文档doc_id、filename和metadata_list的信息
//    @Select("SELECT doc_id, file_name, metadata_list FROM DocumentTag")
//    List<DocumentTagEntity> selectSimpleInfo();
//    // 批量插入数据
//    @Insert("INSERT INTO DocumentTag (doc_id, file_name, author, date_time, metadata_list)"+
//           "VALUES (#{docId}, #{fileName}, #{author}, #{dateTime}, #{metadataList, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler})"+
//            "ON DUPLICATE KEY UPDATE"+
//            "file_name = VALUES(file_name),"+
//            "author = VALUES(author),"+
//            "date_time = VALUES(date_time),"+
//            "metadata_list = VALUES(metadata_list)")
//    void insertOrUpdate(DocumentTagEntity entity);
    // 查询文档数量和最后更新时间
    Map<String, Object> selectCountAndMaxTime();

    // 查询 doc_id、file_name、metadata_list
    List<DocumentTagEntity> selectSimpleInfo();

    // 插入或更新（根据 doc_id 唯一键）
    void insertOrUpdate(DocumentTagEntity entity);
}

