# Tag API 接口实现说明

## 📋 项目概述

本项目为 `WriteDocumentsTag` 类实现了完整的 RESTful API 接口，支持标签的增删改查操作。

## 🎯 实现的功能

### 核心接口
1. **addTag** - 添加标签（单个/批量）
2. **deleteTag** - 删除标签（单个/批量）
3. **getTagByDocID** - 根据文档ID查询标签
4. **updateTag** - 更新标签信息

### 接口特性
- ✅ RESTful 风格设计
- ✅ 统一的响应格式
- ✅ 完善的参数验证
- ✅ 支持跨域访问（CORS）
- ✅ 详细的错误处理
- ✅ 支持批量操作

## 📁 项目结构

```
MetadataDemo/
├── src/main/java/
│   ├── rag.Application.java                    # Spring Boot 主类
│   ├── rag.controller/
│   │   └── TagController.java              # REST 控制器
│   ├── rag.service/
│   │   └── TagService.java                 # 业务逻辑层
│   ├── rag.dto/
│   │   ├── TagRequest.java                 # 请求 DTO
│   │   ├── TagResponse.java                # 响应 DTO
│   │   └── ApiResponse.java                # 统一响应格式
│   ├── rag.filterdocid/
│   │   ├── Tag.java                        # 标签实体类
│   │   └── WriteDocumentsTag.java          # 数据访问层
│   └── rag.dto/
│       └── SqlConnect.java                 # 数据库连接
├── src/main/resources/
│   └── application.properties              # 应用配置
├── pom.xml                                 # Maven 配置
├── API_DOCUMENTATION.md                    # API 文档
├── test-api.html                           # 测试页面
└── README_API.md                           # 本文件
```

## 🚀 快速开始

### 1. 环境要求
- Java 8+
- Maven 3.6+
- MySQL 5.7+
- Spring Boot 2.7.18

### 2. 数据库准备

确保数据库中存在 `DocumentTag` 表：

```sql
CREATE TABLE DocumentTag (
    doc_id VARCHAR(50) PRIMARY KEY,
    file_name VARCHAR(255),
    author VARCHAR(100),
    date_time TIMESTAMP,
    metadata_list JSON
);
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.properties`（如果使用 SqlConnect 类，则在该类中配置）：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. 启动应用

#### 方式一：使用 Maven
```bash
cd D:\JavaCache\MetadataDemo
mvn clean install
mvn spring-boot:run
```

#### 方式二：使用 IDE
直接运行 `rag.Application.java` 主类

### 5. 验证服务

访问健康检查接口：
```bash
curl http://localhost:8080/api/tags/health
```

预期响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "Tag API 服务正常运行"
}
```

## 📖 API 使用示例

### 1. 添加标签

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

### 2. 查询标签

```bash
curl http://localhost:8080/api/tags/get/756063966790811660
```

### 3. 更新标签

```bash
curl -X PUT http://localhost:8080/api/tags/update \
  -H "Content-Type: application/json" \
  -d '{
    "doc_id": "756063966790811660",
    "metadataList": ["sc235Aw", "231AW产品手册"],
    "filename": "sc235AW产品手册.pdf",
    "author": "研发管理部门",
    "dateTime": "2025-02-01 00:00:00"
  }'
```

### 4. 删除标签

```bash
curl -X DELETE http://localhost:8080/api/tags/delete/756063966790811660
```

## 🧪 测试工具

### 1. 使用 HTML 测试页面

打开浏览器访问：
```
file:///D:/JavaCache/MetadataDemo/test-api.html
```

这个页面提供了可视化的接口测试界面，可以方便地测试所有 API。

### 2. 使用 Postman

导入 API 文档中的 Postman Collection，或手动创建请求。

### 3. 使用 cURL

参考 `API_DOCUMENTATION.md` 中的 cURL 示例。

## 📝 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/tags/add | 添加单个标签 |
| POST | /api/tags/batch-add | 批量添加标签 |
| DELETE | /api/tags/delete/{docId} | 删除单个标签（路径参数） |
| POST | /api/tags/delete | 删除单个标签（请求体） |
| POST | /api/tags/batch-delete | 批量删除标签 |
| GET | /api/tags/get/{docId} | 查询标签（路径参数） |
| POST | /api/tags/get | 查询标签（请求体） |
| PUT | /api/tags/update | 更新标签 |
| GET | /api/tags/health | 健康检查 |

详细的接口文档请查看 `API_DOCUMENTATION.md`。

## 🔧 配置说明

### application.properties

```properties
# 服务器端口
server.port=8080

# 应用名称
spring.application.name=MetadataDemo

# JSON 配置
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.time-zone=GMT+8
spring.jackson.default-property-inclusion=non_null

# 日志级别
logging.level.root=INFO
logging.level.rag.controller=DEBUG
logging.level.rag.service=DEBUG
```

## 📦 依赖说明

主要依赖：
- Spring Boot Starter Web - Web 框架
- Spring Boot Starter - 核心依赖
- MySQL Connector - 数据库驱动
- Jackson - JSON 处理

## 🎨 请求参数说明

### TagRequest 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| doc_id | String | 是 | 文档ID |
| metadataList | List<String> | 否 | 元数据列表 |
| id | String | 否 | ID（预留字段） |
| filename | String | 否 | 文件名 |
| author | String | 否 | 作者 |
| dateTime | String | 否 | 日期时间（格式：yyyy-MM-dd HH:mm:ss） |

### 响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## ⚠️ 注意事项

1. **必填字段**：`doc_id` 是所有操作的必填字段
2. **日期格式**：`dateTime` 必须使用 `yyyy-MM-dd HH:mm:ss` 格式
3. **元数据列表**：`metadataList` 是一个字符串数组，可以为 null
4. **数据库连接**：确保 `SqlConnect` 类中的数据库配置正确
5. **端口占用**：默认使用 8080 端口，如有冲突请修改配置

## 🐛 常见问题

### 1. 启动失败

**问题**：应用启动时报错
**解决**：
- 检查数据库连接配置
- 确保 8080 端口未被占用
- 检查 Maven 依赖是否正确下载

### 2. 接口调用失败

**问题**：接口返回 500 错误
**解决**：
- 检查数据库表是否存在
- 查看控制台错误日志
- 确认请求参数格式正确

### 3. 跨域问题

**问题**：前端调用接口时出现 CORS 错误
**解决**：
- Controller 已配置 `@CrossOrigin`
- 如仍有问题，检查浏览器控制台具体错误信息

## 📚 相关文档

- [API 详细文档](API_DOCUMENTATION.md)
- [测试页面](test-api.html)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

## 🤝 贡献

如有问题或建议，欢迎提出 Issue 或 Pull Request。

## 📄 许可证

本项目仅供学习和参考使用。

---

**开发完成时间**：2025-01-30
**版本**：1.0.0
