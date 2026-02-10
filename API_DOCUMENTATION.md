# Tag API 接口文档

## 概述
本文档描述了标签管理系统的 RESTful API 接口，包括标签的增删改查功能。

## 基础信息
- **Base URL**: `http://localhost:8080/api/tags`
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

## 接口列表

### 1. 添加单个标签
**接口地址**: `POST /api/tags/add`

**请求参数**:
```json
{
  "doc_id": "756063966790811660",
  "metadataList": ["sc235aw", "235aw产品手册", "NT6000文档"],
  "filename": "235aw.pdf",
  "author": "研发管理部门",
  "dateTime": "2025-01-10 10:30:00"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| doc_id | String | 是 | 文档ID |
| metadataList | Array[String] | 否 | 元数据列表 |
| filename | String | 否 | 文件名 |
| author | String | 否 | 作者 |
| dateTime | String | 否 | 日期时间（格式：yyyy-MM-dd HH:mm:ss） |
| id | String | 否 | ID（预留字段） |

**响应示例**:
```json
{
  "code": 200,
  "message": "标签添加成功",
  "data": "标签添加成功"
}
```

**cURL 示例**:
```bash
curl -X POST http://localhost:8080/api/tags/add \
  -H "Content-Type: application/json" \
  -d '{
    "doc_id": "756063966790811660",
    "metadataList": ["sc235aw", "235aw产品手册", "NT6000文档"],
    "filename": "235aw.pdf",
    "author": "研发管理部门",
    "dateTime": "2025-01-10 10:30:00"
  }'
```

---

### 2. 批量添加标签
**接口地址**: `POST /api/tags/batch-add`

**请求参数**:
```json
[
  {
    "doc_id": "756063966790811660",
    "metadataList": ["sc235aw", "235aw产品手册"],
    "filename": "235aw.pdf",
    "author": "研发部",
    "dateTime": "2025-01-10 10:30:00"
  },
  {
    "doc_id": "756063966790811661",
    "metadataList": ["sc236aw", "236aw产品手册"],
    "filename": "236aw.pdf",
    "author": "研发部",
    "dateTime": "2025-01-11 10:30:00"
  }
]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量添加标签成功，共 2 条",
  "data": "批量添加标签成功，共 2 条"
}
```

**cURL 示例**:
```bash
curl -X POST http://localhost:8080/api/tags/batch-add \
  -H "Content-Type: application/json" \
  -d '[
    {
      "doc_id": "756063966790811660",
      "metadataList": ["sc235aw", "235aw产品手册"],
      "filename": "235aw.pdf",
      "author": "研发部",
      "dateTime": "2025-01-10 10:30:00"
    }
  ]'
```

---

### 3. 删除单个标签（路径参数）
**接口地址**: `DELETE /api/tags/delete/{docId}`

**路径参数**:
- `docId`: 文档ID

**响应示例**:
```json
{
  "code": 200,
  "message": "标签删除成功",
  "data": "标签删除成功"
}
```

**cURL 示例**:
```bash
curl -X DELETE http://localhost:8080/api/tags/delete/756063966790811660
```

---

### 4. 删除单个标签（请求体）
**接口地址**: `POST /api/tags/delete`

**请求参数**:
```json
{
  "doc_id": "756063966790811660"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "标签删除成功",
  "data": "标签删除成功"
}
```

**cURL 示例**:
```bash
curl -X POST http://localhost:8080/api/tags/delete \
  -H "Content-Type: application/json" \
  -d '{"doc_id": "756063966790811660"}'
```

---

### 5. 批量删除标签
**接口地址**: `POST /api/tags/batch-delete`

**请求参数**:
```json
["756063966790811660", "756063966790811661"]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量删除标签成功，共 2 条",
  "data": "批量删除标签成功，共 2 条"
}
```

**cURL 示例**:
```bash
curl -X POST http://localhost:8080/api/tags/batch-delete \
  -H "Content-Type: application/json" \
  -d '["756063966790811660", "756063966790811661"]'
```

---

### 6. 查询标签（路径参数）
**接口地址**: `GET /api/tags/get/{docId}`

**路径参数**:
- `docId`: 文档ID

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "docId": "756063966790811660",
    "fileName": "235aw.pdf",
    "author": "研发管理部门",
    "dateTime": "2025-01-10 10:30:00",
    "metadataList": ["sc235aw", "235aw产品手册", "NT6000文档"]
  }
}
```

**cURL 示例**:
```bash
curl -X GET http://localhost:8080/api/tags/get/756063966790811660
```

---

### 7. 查询标签（请求体）
**接口地址**: `POST /api/tags/get`

**请求参数**:
```json
{
  "doc_id": "756063966790811660"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "docId": "756063966790811660",
    "fileName": "235aw.pdf",
    "author": "研发管理部门",
    "dateTime": "2025-01-10 10:30:00",
    "metadataList": ["sc235aw", "235aw产品手册", "NT6000文档"]
  }
}
```

**cURL 示例**:
```bash
curl -X POST http://localhost:8080/api/tags/get \
  -H "Content-Type: application/json" \
  -d '{"doc_id": "756063966790811660"}'
```

---

### 8. 更新标签
**接口地址**: `PUT /api/tags/update`

**请求参数**:
```json
{
  "doc_id": "756063966790811660",
  "metadataList": ["sc235Aw", "231AW产品手册", "NT6000文档"],
  "filename": "sc235AW产品手册.pdf",
  "author": "研发管理部门",
  "dateTime": "2025-02-01 00:00:00"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "标签更新成功",
  "data": "标签更新成功"
}
```

**cURL 示例**:
```bash
curl -X PUT http://localhost:8080/api/tags/update \
  -H "Content-Type: application/json" \
  -d '{
    "doc_id": "756063966790811660",
    "metadataList": ["sc235Aw", "231AW产品手册", "NT6000文档"],
    "filename": "sc235AW产品手册.pdf",
    "author": "研发管理部门",
    "dateTime": "2025-02-01 00:00:00"
  }'
```

---

### 9. 健康检查
**接口地址**: `GET /api/tags/health`

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "Tag API 服务正常运行"
}
```

**cURL 示例**:
```bash
curl -X GET http://localhost:8080/api/tags/health
```

---

## 响应状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 客户端请求错误（参数错误） |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |

## 统一响应格式

所有接口都返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**字段说明**:
- `code`: 响应状态码
- `message`: 响应消息
- `data`: 响应数据（可能为 null）

## 错误响应示例

```json
{
  "code": 400,
  "message": "doc_id 不能为空",
  "data": null
}
```

```json
{
  "code": 404,
  "message": "未找到该文档的标签信息",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "服务器错误: 数据库连接失败",
  "data": null
}
```

## 使用 Postman 测试

### 1. 导入接口
可以使用以下 JSON 导入到 Postman：

```json
{
  "info": {
    "name": "Tag API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "添加标签",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"doc_id\": \"756063966790811660\",\n  \"metadataList\": [\"sc235aw\", \"235aw产品手册\"],\n  \"filename\": \"235aw.pdf\",\n  \"author\": \"研发部\",\n  \"dateTime\": \"2025-01-10 10:30:00\"\n}"
        },
        "url": {"raw": "http://localhost:8080/api/tags/add"}
      }
    },
    {
      "name": "查询标签",
      "request": {
        "method": "GET",
        "url": {"raw": "http://localhost:8080/api/tags/get/756063966790811660"}
      }
    },
    {
      "name": "删除标签",
      "request": {
        "method": "DELETE",
        "url": {"raw": "http://localhost:8080/api/tags/delete/756063966790811660"}
      }
    }
  ]
}
```

## 启动应用

1. 确保数据库配置正确（在 `application.properties` 中）
2. 运行 Spring Boot 应用：
   ```bash
   mvn spring-boot:run
   ```
   或者在 IDE 中运行 `rag.Application.java`

3. 访问健康检查接口验证服务是否正常：
   ```bash
   curl http://localhost:8080/api/tags/health
   ```

## 注意事项

1. **必填字段**: `doc_id` 是必填字段，其他字段可选
2. **日期格式**: `dateTime` 字段格式为 `yyyy-MM-dd HH:mm:ss`
3. **元数据列表**: `metadataList` 是一个字符串数组，可以为空
4. **跨域支持**: API 已配置 CORS，支持跨域访问
5. **数据库**: 确保 `DocumentTag` 表已创建，包含以下字段：
   - `doc_id` (VARCHAR, PRIMARY KEY)
   - `file_name` (VARCHAR)
   - `author` (VARCHAR)
   - `date_time` (TIMESTAMP)
   - `metadata_list` (JSON)
