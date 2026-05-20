## 数据库脚本

所有脚本放在 `scripts/` 目录下，需要在 **devcontainer 内** 或安装了 `psql` + Maven 的环境中执行。

### 创建数据库

```bash
bash scripts/db-create.sh
```

幂等操作，数据库已存在时自动跳过。

### 删除数据库

```bash
bash scripts/db-drop.sh
```

非开发环境需设置 `DISABLE_DATABASE_ENVIRONMENT_CHECK=1` 确认。

### 重置数据库（删 + 建 + 种子数据）

```bash
bash scripts/db-reset.sh
```

依次执行：删除数据库 → 创建数据库 → 启动应用填充种子数据。

### 单独填充种子数据

```bash
bash scripts/db-seed.sh
```