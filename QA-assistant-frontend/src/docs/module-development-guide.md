# 模块开发流程指南

## 概述

本文档详细介绍了在系统中新增一个完整模块的开发流程，包括 API 创建、代码结构、权限控制等。

## 开发流程

### 1. 需求分析

#### 1.1 功能需求
- 确定模块功能范围
- 定义数据模型
- 确定权限要求
- 确定页面交互流程

#### 1.2 技术需求
- 确定 API 接口规范
- 确定数据库表结构
- 确定权限控制粒度
- 确定页面组件结构

### 2. 数据库设计

#### 2.1 表结构设计
```sql
-- 示例：设备管理模块
  略...
```

#### 2.2 权限数据设计
```sql
-- 权限表
INSERT INTO permissions (code, name, type, parent_id) VALUES
('eqms:equipment:add', '新增设备', 'button', 'eqms:equipment'),
('eqms:equipment:edit', '编辑设备', 'button', 'eqms:equipment'),
('eqms:equipment:delete', '删除设备', 'button', 'eqms:equipment'),
('eqms:equipment:view', '查看设备', 'button', 'eqms:equipment');
```

### 3. 后端 API 开发

#### 3.1 API 接口规范
```typescript
// 设备管理 API 接口
GET    /api/v1/po/eqms/equipment/list     // 分页查询设备列表
GET    /api/v1/po/eqms/equipment/{id}     // 查询单个设备
POST   /api/v1/po/eqms/equipment         // 新增设备
PUT    /api/v1/po/eqms/equipment/{id}     // 更新设备
DELETE /api/v1/po/eqms/equipment/{id}     // 删除设备
DELETE /api/v1/po/eqms/equipment/batch    // 批量删除设备
```

#### 3.2 响应数据格式
```typescript
// 统一响应格式
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 分页响应格式
interface PageResponse<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}
```

### 4. 前端代码结构

#### 4.1 目录结构
```
src/
├── api/eqms/equipment.ts          # API 服务
├── views/eqms/equipment/          # 页面组件
│   ├── index.vue                  # 主页面
│   ├── components/                # 子组件
│   │   ├── EquipmentForm.vue      # 表单组件
│   │   └── EquipmentDetail.vue    # 详情组件
├── types/equipment.ts             # 类型定义
└── router/modules/eqms.ts         # 路由配置
```

#### 4.2 文件创建顺序
1. 类型定义文件
2. API 服务文件
3. 页面组件文件
4. 路由配置
5. 权限配置

### 5. 具体实现步骤

#### 5.1 创建类型定义（也可以放在api中定义，如果内容多的话，放到types中）
```typescript
// src/types/equipment.ts
export interface Equipment {
  id?: number;
  name: string;
  type: string;
  status: 'active' | 'inactive' | 'maintenance';
  location: string;
  createTime?: string;
  updateTime?: string;
}

export interface EquipmentListParams {
  page: number;
  size: number;
  name?: string;
  type?: string;
  status?: string;
}

export interface EquipmentListResponse {
  records: Equipment[];
  total: number;
  current: number;
  size: number;
  pages: number;
}
```

#### 5.2 创建 API 服务
```typescript
// src/api/eqms/equipment.ts
import { AlovaAxios } from '@/utils/http/alova';
import { apiConfig } from '@/config/api.config';
import type { 
  Equipment, 
  EquipmentListParams, 
  EquipmentListResponse 
} from '@/types/equipment';

// 分页查询设备列表
export const getEquipmentList = (params: EquipmentListParams) => {
  return AlovaAxios.Get(apiConfig.baseURL_eqms + '/equipment/list', {
    params,
    meta: {
      isReturnNativeResponse: true,
    },
  });
};

// 查询单个设备
export const getEquipment = (id: number) => {
  return AlovaAxios.Get(apiConfig.baseURL_eqms + `/equipment/${id}`);
};

// 新增设备
export const addEquipment = (data: Omit<Equipment, 'id'>) => {
  return AlovaAxios.Post(apiConfig.baseURL_eqms + '/equipment', data);
};

// 更新设备
export const updateEquipment = (id: number, data: Omit<Equipment, 'id'>) => {
  return AlovaAxios.Put(apiConfig.baseURL_eqms + `/equipment/${id}`, data);
};

// 删除设备
export const deleteEquipment = (id: number) => {
  return AlovaAxios.Delete(apiConfig.baseURL_eqms + `/equipment/${id}`);
};

// 批量删除设备
export const batchDeleteEquipment = (ids: number[]) => {
  return AlovaAxios.Delete(apiConfig.baseURL_eqms + '/equipment/batch', { ids });
};
```

#### 5.3 创建页面组件
```vue
<!-- src/views/eqms/equipment/index.vue -->
<template>
  <n-card title="设备管理">
    <!-- 搜索表单 -->
    <div class="search-section">
      <n-form inline>
        <n-form-item label="设备名称">
          <n-input v-model:value="searchForm.name" placeholder="请输入设备名称" />
        </n-form-item>
        <n-form-item label="设备类型">
          <n-select v-model:value="searchForm.type" :options="typeOptions" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" @click="handleSearch">
            <template #icon><n-icon><SearchOutlined /></n-icon></template>
            搜索
          </n-button>
          <n-button @click="handleReset">
            <template #icon><n-icon><ReloadOutlined /></n-icon></template>
            重置
          </n-button>
        </n-form-item>
      </n-form>
    </div>

    <!-- 操作按钮 -->
    <div class="action-section">
      <n-space>
        <n-button 
          v-if="permissions.equipment.add"
          type="primary" 
          @click="handleAdd"
        >
          <template #icon><n-icon><PlusOutlined /></n-icon></template>
          新增设备
        </n-button>
        <n-button 
          v-if="permissions.equipment.batchDelete"
          type="error" 
          :disabled="!selectedRowKeys.length"
          @click="handleBatchDelete"
        >
          <template #icon><n-icon><DeleteOutlined /></n-icon></template>
          批量删除
        </n-button>
      </n-space>
    </div>

    <!-- 数据表格 -->
    <n-data-table
      :columns="columns"
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :row-key="(row) => row.id"
      @update:checked-row-keys="handleCheckedRowKeysChange"
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
    />
  </n-card>

  <!-- 新增/编辑弹窗 -->
  <n-modal
    v-model:show="modalVisible"
    :title="isEdit ? '编辑设备' : '新增设备'"
    preset="dialog"
    style="width: 600px"
  >
    <EquipmentForm
      :form-data="formData"
      :is-edit="isEdit"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </n-modal>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue';
import { useMessage, useDialog } from 'naive-ui';
import { usePermission } from '@/hooks/usePermission';
import { 
  getEquipmentList, 
  addEquipment, 
  updateEquipment, 
  deleteEquipment, 
  batchDeleteEquipment 
} from '@/api/eqms/equipment';
import type { Equipment, EquipmentListParams } from '@/types/equipment';
import EquipmentForm from './components/EquipmentForm.vue';

// 权限控制
const { permissions } = usePermission();

// 响应式数据
const message = useMessage();
const dialog = useDialog();
const loading = ref(false);
const modalVisible = ref(false);
const isEdit = ref(false);
const tableData = ref<Equipment[]>([]);
const selectedRowKeys = ref<number[]>([]);

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  status: ''
});

// 表单数据
const formData = ref<Equipment>({
  name: '',
  type: '',
  status: 'active',
  location: ''
});

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

// 选项数据
const typeOptions = [
  { label: '生产设备', value: 'production' },
  { label: '检测设备', value: 'testing' },
  { label: '安全设备', value: 'safety' }
];

// 表格列配置
const columns = [
  {
    type: 'selection'
  },
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '设备名称',
    key: 'name',
    width: 150
  },
  {
    title: '设备类型',
    key: 'type',
    width: 120
  },
  {
    title: '设备状态',
    key: 'status',
    width: 100,
    render: (row: Equipment) => {
      const statusMap = {
        active: { type: 'success', text: '正常' },
        inactive: { type: 'error', text: '停用' },
        maintenance: { type: 'warning', text: '维护中' }
      };
      const status = statusMap[row.status];
      return h(NTag, { type: status.type }, () => status.text);
    }
  },
  {
    title: '设备位置',
    key: 'location',
    ellipsis: { tooltip: true }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row: Equipment) => {
      const buttons = [];
      
      if (permissions.value.equipment.edit) {
        buttons.push(
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleEdit(row)
          }, () => '编辑')
        );
      }
      
      if (permissions.value.equipment.delete) {
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

// 获取数据列表
const fetchData = async () => {
  loading.value = true;
  try {
    const params: EquipmentListParams = {
      page: pagination.page,
      size: pagination.pageSize,
      ...(searchForm.name && { name: searchForm.name }),
      ...(searchForm.type && { type: searchForm.type }),
      ...(searchForm.status && { status: searchForm.status })
    };
    
    const response = await getEquipmentList(params);
    tableData.value = response.records || [];
    pagination.total = response.total || 0;
  } catch (error) {
    console.error('获取数据失败:', error);
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchData();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: '',
    status: ''
  });
  pagination.page = 1;
  fetchData();
};

// 新增
const handleAdd = () => {
  isEdit.value = false;
  formData.value = {
    name: '',
    type: '',
    status: 'active',
    location: ''
  };
  modalVisible.value = true;
};

// 编辑
const handleEdit = (row: Equipment) => {
  isEdit.value = true;
  formData.value = { ...row };
  modalVisible.value = true;
};

// 删除
const handleDelete = (id: number) => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该设备吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteEquipment(id);
        message.success('删除成功');
        fetchData();
      } catch (error) {
        console.error('删除失败:', error);
        message.error('删除失败');
      }
    }
  });
};

// 批量删除
const handleBatchDelete = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请选择要删除的设备');
    return;
  }
  
  dialog.warning({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个设备吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchDeleteEquipment(selectedRowKeys.value);
        message.success('批量删除成功');
        selectedRowKeys.value = [];
        fetchData();
      } catch (error) {
        console.error('批量删除失败:', error);
        message.error('批量删除失败');
      }
    }
  });
};

// 提交表单
const handleSubmit = async (data: Equipment) => {
  try {
    if (isEdit.value) {
      await updateEquipment(data.id!, data);
      message.success('更新成功');
    } else {
      await addEquipment(data);
      message.success('新增成功');
    }
    modalVisible.value = false;
    fetchData();
  } catch (error) {
    console.error('提交失败:', error);
    message.error('提交失败');
  }
};

// 取消
const handleCancel = () => {
  modalVisible.value = false;
};

// 选择行变化
const handleCheckedRowKeysChange = (keys: number[]) => {
  selectedRowKeys.value = keys;
};

// 分页变化
const handlePageChange = (page: number) => {
  pagination.page = page;
  fetchData();
};

const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize;
  pagination.page = 1;
  fetchData();
};

// 初始化
onMounted(() => {
  fetchData();
});
</script>
```

#### 5.4 创建表单组件
```vue
<!-- src/views/eqms/equipment/components/EquipmentForm.vue -->
<template>
  <n-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-placement="left"
    label-width="80px"
  >
    <n-form-item label="设备名称" path="name">
      <n-input v-model:value="formData.name" placeholder="请输入设备名称" />
    </n-form-item>
    
    <n-form-item label="设备类型" path="type">
      <n-select v-model:value="formData.type" :options="typeOptions" placeholder="请选择设备类型" />
    </n-form-item>
    
    <n-form-item label="设备状态" path="status">
      <n-select v-model:value="formData.status" :options="statusOptions" placeholder="请选择设备状态" />
    </n-form-item>
    
    <n-form-item label="设备位置" path="location">
      <n-input v-model:value="formData.location" placeholder="请输入设备位置" />
    </n-form-item>
    
    <n-form-item>
      <n-space>
        <n-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </n-button>
        <n-button @click="handleCancel">
          取消
        </n-button>
      </n-space>
    </n-form-item>
  </n-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import type { Equipment } from '@/types/equipment';

interface Props {
  formData: Equipment;
  isEdit: boolean;
}

interface Emits {
  (e: 'submit', data: Equipment): void;
  (e: 'cancel'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const formRef = ref();
const submitLoading = ref(false);

// 表单数据
const formData = reactive({ ...props.formData });

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择设备状态', trigger: 'change' }
  ]
};

// 选项数据
const typeOptions = [
  { label: '生产设备', value: 'production' },
  { label: '检测设备', value: 'testing' },
  { label: '安全设备', value: 'safety' }
];

const statusOptions = [
  { label: '正常', value: 'active' },
  { label: '停用', value: 'inactive' },
  { label: '维护中', value: 'maintenance' }
];

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitLoading.value = true;
    emit('submit', { ...formData });
  } catch (error) {
    console.error('表单验证失败:', error);
  } finally {
    submitLoading.value = false;
  }
};

// 取消
const handleCancel = () => {
  emit('cancel');
};
</script>
```

#### 5.5 配置路由
```typescript
// src/router/modules/eqms.ts
import { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/eqms',
    name: 'Eqms',
    redirect: '/eqms/equipment',
    component: Layout,
    meta: {
      title: '设备管理',
      icon: 'equipment',
      sort: 1,
    },
    children: [
      {
        path: 'equipment',
        name: 'eqms_equipment',
        meta: {
          title: '设备管理',
          permissions: ['eqms:equipment:view']
        },
        component: () => import('@/views/eqms/equipment/index.vue'),
      },
      // ... 其他子路由
    ],
  },
];
```

#### 5.6 配置权限
```typescript
// src/utils/permission.ts
// 在权限配置中添加新模块权限
export const mockUserPermissions: Permission[] = [
  // ... 现有权限
  
  // 设备管理权限
  { id: '9', name: '设备管理', code: 'eqms:equipment', type: 'menu' },
  { id: '10', name: '新增设备', code: 'eqms:equipment:add', type: 'button', parentId: '9' },
  { id: '11', name: '编辑设备', code: 'eqms:equipment:edit', type: 'button', parentId: '9' },
  { id: '12', name: '删除设备', code: 'eqms:equipment:delete', type: 'button', parentId: '9' },
  { id: '13', name: '查看设备', code: 'eqms:equipment:view', type: 'button', parentId: '9' },
  { id: '14', name: '批量删除', code: 'eqms:equipment:batchDelete', type: 'button', parentId: '9' },
];
```

### 6. 测试验证

#### 6.1 功能测试
- [ ] API 接口测试
- [ ] 页面功能测试
- [ ] 权限控制测试
- [ ] 数据验证测试

#### 6.2 性能测试
- [ ] 大数据量加载测试
- [ ] 分页性能测试
- [ ] 搜索性能测试

#### 6.3 兼容性测试
- [ ] 浏览器兼容性
- [ ] 响应式布局测试
- [ ] 移动端适配测试

### 7. 部署上线

#### 7.1 代码审查
- [ ] 代码规范检查
- [ ] 安全漏洞检查
- [ ] 性能优化检查

#### 7.2 部署准备
- [ ] 数据库脚本准备
- [ ] 配置文件更新
- [ ] 权限数据初始化

#### 7.3 上线部署
- [ ] 测试环境部署
- [ ] 生产环境部署
- [ ] 功能验证
- [ ] 监控配置

## 最佳实践

### 1. 代码规范
- 使用 TypeScript 严格模式
- 遵循 ESLint 规范
- 统一的命名规范
- 完善的注释文档

### 2. 性能优化
- 合理使用缓存
- 避免不必要的重渲染
- 优化大数据量处理
- 使用虚拟滚动

### 3. 安全考虑
- 输入验证
- XSS 防护
- CSRF 防护
- 权限验证

### 4. 用户体验
- 友好的错误提示
- 加载状态显示
- 操作确认机制
- 响应式设计

## 总结

通过以上流程，可以快速开发一个完整的模块功能。关键是要遵循统一的开发规范，确保代码质量和可维护性。
