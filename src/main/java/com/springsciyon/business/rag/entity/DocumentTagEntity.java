package com.springsciyon.business.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "DocumentTag", autoResultMap = true)
public class DocumentTagEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("doc_id")
    private String docId;

    @TableField("file_name")
    private String fileName;

    @TableField("author")
    private String author;

    @TableField("date_time")
    private LocalDateTime dateTime;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * JSON字段：metadata_list
     */
    @TableField(value = "metadata_list", typeHandler = JacksonTypeHandler.class)
    private List<String> metadataList;
}
