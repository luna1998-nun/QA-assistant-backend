# 权限控制系统

## 概述

本系统实现了完整的按钮权限控制功能，支持动态权限切换和实时权限验证。

## 功能特性

### 1. 权限数据结构
```typescript
interface Permission {
  id: string;
  name: string;
  code: string;
  type: 'menu' | 'button';
  parentId?: string;
}
```

### 2. 权限控制方式

#### 2.1 组合式函数方式
```vue
<script setup>
import { usePermission } from '@/hooks/usePermission';

const { permissions } = usePermission();

// 在模板中使用
// v-if="permissions.daily.add"
</script>
```

#### 2.2 指令方式
```vue
<template>
  <n-button v-permission="'eqms:maintenance:daily:add'">
    新增
  </n-button>
</template>
```

#### 2.3 函数方式
```typescript
import { hasPermission } from '@/utils/permission';

if (hasPermission('eqms:maintenance:daily:add')) {
  // 显示按钮
}
```

### 3. 权限代码规范

#### 3.1 命名规范
- 格式：`模块:功能:操作`
- 示例：`eqms:maintenance:daily:add`

#### 3.2 权限代码列表
- `eqms:maintenance:daily:add` - 新增记录
- `eqms:maintenance:daily:edit` - 编辑记录
- `eqms:maintenance:daily:delete` - 删除记录
- `eqms:maintenance:daily:batchDelete` - 批量删除
- `eqms:maintenance:daily:view` - 查看详情
- `eqms:maintenance:daily:export` - 导出数据

### 4. 实现示例

#### 4.1 页面级权限控制
```vue
<template>
  <div>
    <!-- 新增按钮 -->
    <n-button 
      v-if="permissions.daily.add"
      type="primary" 
      @click="handleAdd"
    >
      新增
    </n-button>
    
    <!-- 批量删除按钮 -->
    <n-button 
      v-if="permissions.daily.batchDelete"
      type="error" 
      @click="handleBatchDelete"
    >
      批量删除
    </n-button>
  </div>
</template>
```

#### 4.2 表格操作列权限控制
```typescript
const columns = [
  // ... 其他列
  {
    title: '操作',
    key: 'actions',
    render: (row: DailyRecord) => {
      const buttons = [];
      
      // 编辑按钮
      if (permissions.value.daily.edit) {
        buttons.push(
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleEdit(row)
          }, () => '编辑')
        );
      }
      
      // 删除按钮
      if (permissions.value.daily.delete) {
        buttons.push(
          h(NButton, {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id!)
          }, () => '删除')
        );
      }
      
      return h(NSpace, {}, buttons);
    }
  }
];
```

### 5. 权限管理

#### 5.1 动态权限切换
```typescript
import { 
  setUserPermissions, 
  addPermission, 
  removePermission 
} from '@/utils/permission';

// 设置权限
setUserPermissions(permissions);

// 添加权限
addPermission(permission);

// 移除权限
removePermission(permissionCode);
```

#### 5.2 权限验证
```typescript
import { 
  hasPermission, 
  hasAnyPermission, 
  hasAllPermissions 
} from '@/utils/permission';

// 检查单个权限
hasPermission('eqms:maintenance:daily:add')

// 检查任一权限
hasAnyPermission(['eqms:maintenance:daily:add', 'eqms:maintenance:daily:edit'])

// 检查所有权限
hasAllPermissions(['eqms:maintenance:daily:add', 'eqms:maintenance:daily:edit'])
```

### 6. 权限演示

访问 `/system/permission-demo` 页面可以：
- 查看当前用户权限
- 动态切换权限
- 测试权限控制效果

### 7. 最佳实践

1. **权限粒度**：建议按功能模块划分权限，避免过细或过粗
2. **权限命名**：使用统一的命名规范，便于维护
3. **权限缓存**：合理使用权限缓存，提高性能
4. **权限验证**：在关键操作前进行权限验证
5. **权限日志**：记录权限变更日志，便于审计

## 使用说明

1. 在页面中导入 `usePermission` 组合式函数
2. 使用 `permissions` 对象控制按钮显示
3. 通过权限演示页面测试权限控制效果
4. 根据实际需求调整权限配置
