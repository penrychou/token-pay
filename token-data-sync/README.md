# 数据同步工具

该模块用于将一个数据库的表数据同步到另一个数据库，支持不同字段名称的映射转换。

## 功能特点

- 支持将源数据库表数据同步到目标数据库
- 支持字段名称映射，可处理两个库字段不完全相同的情况
- 支持批量处理，减少内存占用
- 支持自动同步和手动触发同步
- 提供RESTful API接口，方便集成

## 配置说明

在 `application.yml` 文件中配置数据源和同步规则：

### 数据源配置

```yaml
spring:
  # 源数据库配置（其他系统的数据库）
  source-datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/source_db?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: root

  # 目标数据库配置（当前系统的数据库）
  target-datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/target_db?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: root
```

### 同步规则配置

```yaml
sync:
  # 数据同步批次大小
  batch-size: 1000
  # 启动时是否执行同步
  auto-sync: false
  # 同步表配置
  tables:
    # 货币表
    currency:
      source-table: source_currency  # 源表名
      target-table: pay_currency     # 目标表名
      id-field: id                   # 主键字段
      mapping:
        # 源字段: 目标字段
        id: id
        currency_name: currencyName
        currency_logo: currencyLogo
        currency_type: currencyType
        # 更多字段映射...
```

## 使用方法

### 1. 配置文件

修改 `application.yml` 配置文件，设置正确的数据源和同步规则。

### 2. 启动应用

有两种启动方式：

1. **自动同步模式**：
   设置 `sync.auto-sync=true`，应用启动后会自动执行同步操作。

2. **手动触发模式**：
   设置 `sync.auto-sync=false`，然后通过API手动触发同步操作。

### 3. API接口

#### 同步所有表
```
POST /sync/all
```

#### 同步指定表
```
POST /sync/{tableName}
```

#### 获取同步状态
```
GET /sync/status
```

## 示例

### 配置映射示例

假设源数据库的字段是下划线风格，目标数据库是驼峰风格：

```yaml
sync:
  tables:
    currency:
      source-table: source_currency
      target-table: pay_currency
      id-field: id
      mapping:
        id: id
        name: currencyName
        logo_url: currencyLogo
        currency_type: currencyType
        decimal_num: currencyDecimalsNum
        contract_address: contractAddress
        cold_address: coldAddress
        created_at: createdAt
        updated_at: updatedAt
```

## 注意事项

1. 确保两个数据库都可以连接且有足够的权限进行读写操作
2. 配置中的字段映射必须准确，错误的映射可能导致数据错误
3. 对于大量数据，调整 `batch-size` 以平衡性能和内存使用
4. 首次使用时建议将 `auto-sync` 设置为 `false`，测试通过后再打开自动同步 