# API 开发指南

## 概述

本文档详细介绍了系统中 API 的开发规范、最佳实践和具体实现方法。

## API 设计规范

### 1. RESTful API 设计

#### 1.1 URL 设计规范
```
# 资源命名
GET    /api/v1/po/eqms/equipment          # 获取设备列表
GET    /api/v1/po/eqms/equipment/{id}     # 获取单个设备
POST   /api/v1/po/eqms/equipment          # 创建设备
PUT    /api/v1/po/eqms/equipment/{id}     # 更新设备
DELETE /api/v1/po/eqms/equipment/{id}     # 删除设备
DELETE /api/v1/po/eqms/equipment/batch    # 批量删除设备
```

#### 1.2 HTTP 状态码规范
```typescript
// 成功状态码
200 OK                    // 请求成功

// 客户端错误
400 Bad Request          // 请求参数错误
401 Unauthorized         // 未授权
403 Forbidden            // 禁止访问
404 Not Found            // 资源不存在
409 Conflict             // 资源冲突

// 服务器错误
500 Internal Server Error // 服务器内部错误
```

### 2. 请求/响应格式

#### 2.1 统一响应格式
```typescript
interface ApiResponse<T> {
  code: number;          // 状态码
  message: string;       // 消息
  data: T;              // 数据
  timestamp: number;     // 时间戳
}

// 成功响应示例
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "设备A",
    "type": "production"
  },
  "timestamp": 1640995200000
}

// 错误响应示例
{
  "code": 500,
  "message": "参数错误",
  "data": null,
  "timestamp": 1640995200000
}
```

#### 2.2 分页响应格式
```typescript
interface PageResponse<T> {
  records: T[];          // 数据列表
  total: number;         // 总记录数
  current: number;       // 当前页
  size: number;         // 每页大小
  pages: number;        // 总页数
}

// 分页响应示例
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      { "id": 1, "name": "设备A" },
      { "id": 2, "name": "设备B" }
    ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  },
  "timestamp": 1640995200000
}
```

## 前端 API 服务开发

### 1. 创建 API 服务文件

#### 1.1 基础结构
```typescript
// src/api/eqms/equipment.ts
import { AlovaAxios } from '@/utils/http/alova';
import { apiConfig } from '@/config/api.config';
import type { Equipment, EquipmentListParams } from '@/types/equipment';

/**
 * 设备管理 API 服务
 */

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

#### 1.2 高级 API 服务
```typescript
// src/api/eqms/equipment-advanced.ts
import { AlovaAxios } from '@/utils/http/alova';
import { apiConfig } from '@/config/api.config';
import type { Equipment, EquipmentListParams } from '@/types/equipment';

/**
 * 设备管理高级 API 服务
 */

// 带缓存的查询
export const getEquipmentListWithCache = (params: EquipmentListParams) => {
  return AlovaAxios.Get(apiConfig.baseURL_eqms + '/equipment/list', {
    params,
    meta: {
      isReturnNativeResponse: true,
      cache: true, // 启用缓存
      cacheExpire: 5 * 60 * 1000, // 5分钟缓存
    },
  });
};

// 带重试的请求
export const getEquipmentWithRetry = (id: number) => {
  return AlovaAxios.Get(apiConfig.baseURL_eqms + `/equipment/${id}`, {
    meta: {
      retry: 3, // 重试3次
      retryDelay: 1000, // 重试延迟1秒
    },
  });
};

// 带超时的请求
export const addEquipmentWithTimeout = (data: Omit<Equipment, 'id'>) => {
  return AlovaAxios.Post(apiConfig.baseURL_eqms + '/equipment', data, {
    meta: {
      timeout: 10000, // 10秒超时
    },
  });
};

// 带进度回调的请求
export const uploadEquipmentFile = (file: File, onProgress?: (progress: number) => void) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return AlovaAxios.Post(apiConfig.baseURL_eqms + '/equipment/upload', formData, {
    meta: {
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    },
  });
};
```

### 2. 类型定义

#### 2.1 基础类型定义
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
  startTime?: string;
  endTime?: string;
}

export interface EquipmentListResponse {
  records: Equipment[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

export interface EquipmentFormData {
  name: string;
  type: string;
  status: string;
  location: string;
}

export interface EquipmentSearchForm {
  name: string;
  type: string;
  status: string;
  dateRange: [string, string] | null;
}
```

#### 2.2 高级类型定义
```typescript
// src/types/api.ts
// 通用 API 响应类型
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 分页参数类型
export interface PageParams {
  page: number;
  size: number;
}

// 分页响应类型
export interface PageResponse<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 搜索参数类型
export interface SearchParams extends PageParams {
  keyword?: string;
  startTime?: string;
  endTime?: string;
}

// 批量操作参数类型
export interface BatchOperationParams {
  ids: number[];
}

// 文件上传参数类型
export interface FileUploadParams {
  file: File;
  onProgress?: (progress: number) => void;
}
```

### 3. API 服务组合

#### 3.1 组合式 API 服务
```typescript
// src/composables/useEquipment.ts
import { ref, reactive, computed } from 'vue';
import { useMessage, useDialog } from 'naive-ui';
import { 
  getEquipmentList, 
  addEquipment, 
  updateEquipment, 
  deleteEquipment, 
  batchDeleteEquipment 
} from '@/api/eqms/equipment';
import type { Equipment, EquipmentListParams } from '@/types/equipment';

export function useEquipment() {
  const message = useMessage();
  const dialog = useDialog();
  
  // 响应式数据
  const loading = ref(false);
  const tableData = ref<Equipment[]>([]);
  const selectedRowKeys = ref<number[]>([]);
  
  // 分页配置
  const pagination = reactive({
    page: 1,
    pageSize: 10,
    total: 0
  });
  
  // 搜索表单
  const searchForm = reactive({
    name: '',
    type: '',
    status: ''
  });
  
  // 计算属性
  const hasSelection = computed(() => selectedRowKeys.value.length > 0);
  
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
  const handleAdd = async (data: Omit<Equipment, 'id'>) => {
    try {
      await addEquipment(data);
      message.success('新增成功');
      fetchData();
    } catch (error) {
      console.error('新增失败:', error);
      message.error('新增失败');
    }
  };
  
  // 编辑
  const handleEdit = async (id: number, data: Omit<Equipment, 'id'>) => {
    try {
      await updateEquipment(id, data);
      message.success('更新成功');
      fetchData();
    } catch (error) {
      console.error('更新失败:', error);
      message.error('更新失败');
    }
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
  
  return {
    // 响应式数据
    loading,
    tableData,
    selectedRowKeys,
    pagination,
    searchForm,
    hasSelection,
    
    // 方法
    fetchData,
    handleSearch,
    handleReset,
    handleAdd,
    handleEdit,
    handleDelete,
    handleBatchDelete,
    handleCheckedRowKeysChange,
    handlePageChange,
    handlePageSizeChange
  };
}
```

### 4. 错误处理

#### 4.1 全局错误处理
```typescript
// src/utils/error-handler.ts
export class ApiError extends Error {
  constructor(
    public code: number,
    public message: string,
    public data?: any
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export const handleApiError = (error: any) => {
  if (error instanceof ApiError) {
    // 处理 API 错误
    console.error('API Error:', error.code, error.message);
    return error.message;
  } else if (error.response) {
    // 处理 HTTP 错误
    const { status, data } = error.response;
    console.error('HTTP Error:', status, data);
    return data?.message || '请求失败';
  } else {
    // 处理网络错误
    console.error('Network Error:', error.message);
    return '网络错误，请检查网络连接';
  }
};
```

#### 4.2 请求拦截器
```typescript
// src/utils/http/interceptors.ts
export const requestInterceptor = (config: any) => {
  // 添加认证头
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  // 添加请求ID
  config.headers['X-Request-ID'] = generateRequestId();
  
  return config;
};

export const responseInterceptor = (response: any) => {
  // 处理响应数据
  if (response.data.code !== 200) {
    throw new ApiError(response.data.code, response.data.message, response.data.data);
  }
  
  return response.data;
};

export const errorInterceptor = (error: any) => {
  // 处理错误
  const message = handleApiError(error);
  throw new Error(message);
};
```

### 5. 测试

#### 5.1 单元测试
```typescript
// src/api/__tests__/equipment.test.ts
import { describe, it, expect, vi } from 'vitest';
import { getEquipmentList, addEquipment } from '@/api/eqms/equipment';

describe('Equipment API', () => {
  it('should fetch equipment list', async () => {
    const mockData = {
      records: [
        { id: 1, name: '设备A', type: 'production' }
      ],
      total: 1,
      current: 1,
      size: 10,
      pages: 1
    };
    
    vi.mocked(getEquipmentList).mockResolvedValue(mockData);
    
    const result = await getEquipmentList({ page: 1, size: 10 });
    
    expect(result).toEqual(mockData);
  });
  
  it('should add equipment', async () => {
    const equipmentData = {
      name: '设备B',
      type: 'testing',
      status: 'active',
      location: '车间A'
    };
    
    vi.mocked(addEquipment).mockResolvedValue({ id: 2, ...equipmentData });
    
    const result = await addEquipment(equipmentData);
    
    expect(result.id).toBe(2);
    expect(result.name).toBe('设备B');
  });
});
```

#### 5.2 集成测试
```typescript
// src/views/__tests__/equipment.test.ts
import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import EquipmentIndex from '@/views/eqms/equipment/index.vue';

describe('Equipment Page', () => {
  it('should render equipment list', async () => {
    const wrapper = mount(EquipmentIndex);
    
    // 等待数据加载
    await wrapper.vm.$nextTick();
    
    expect(wrapper.find('.equipment-list').exists()).toBe(true);
  });
  
  it('should handle search', async () => {
    const wrapper = mount(EquipmentIndex);
    
    // 模拟搜索
    await wrapper.find('.search-input').setValue('设备A');
    await wrapper.find('.search-button').trigger('click');
    
    expect(wrapper.vm.searchForm.name).toBe('设备A');
  });
});
```

## 最佳实践

### 1. 命名规范
- API 函数使用动词开头：`get`, `add`, `update`, `delete`
- 类型定义使用 PascalCase：`Equipment`, `EquipmentListParams`
- 接口使用描述性名称：`getEquipmentList`, `addEquipment`

### 2. 错误处理
- 统一错误处理机制
- 友好的错误提示
- 详细的错误日志

### 3. 性能优化
- 合理使用缓存
- 避免重复请求
- 使用分页加载

### 4. 安全考虑
- 输入验证
- XSS 防护
- CSRF 防护

### 5. 可维护性
- 清晰的代码结构
- 完善的注释文档
- 统一的编码规范

## 总结

通过遵循以上规范和最佳实践，可以开发出高质量、可维护的 API 服务。关键是要保持一致性，注重错误处理和用户体验。
