<template>
  <div class="daily-maintenance">
    <n-card title="日常维护管理" :bordered="false">
      <!-- 搜索和操作区域 -->
      <div class="search-section">
        <n-space>
          <n-input
            v-model:value="searchForm.name"
            placeholder="请输入姓名"
            clearable
            style="width: 200px"
          />
          <n-select
            v-model:value="searchForm.sex"
            placeholder="请选择性别"
            clearable
            style="width: 120px"
            :options="sexOptions"
          />
          <n-select
            v-model:value="searchForm.classes"
            placeholder="请选择班级"
            clearable
            style="width: 150px"
            :options="classesOptions"
          />
          <n-button type="primary" @click="handleSearch">
            <template #icon>
              <n-icon><SearchOutlined /></n-icon>
            </template>
            搜索
          </n-button>
          <n-button @click="handleReset">
            <template #icon>
              <n-icon><ReloadOutlined /></n-icon>
            </template>
            重置
          </n-button>
        </n-space>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-section">
        <n-space>
          <n-button 
            v-if="permissions.daily.add"
            type="primary" 
            @click="handleAdd"
          >
            <template #icon>
              <n-icon><PlusOutlined /></n-icon>
            </template>
            新增
          </n-button>
          <n-button 
            v-if="permissions.daily.batchDelete"
            type="error" 
            :disabled="!selectedRowKeys.length"
            @click="handleBatchDelete"
          >
            <template #icon>
              <n-icon><DeleteOutlined /></n-icon>
            </template>
            批量删除
          </n-button>
          <n-button 
            type="info" 
            @click="showPermissionDemo"
          >
            权限演示
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
      :title="isEdit ? '编辑记录' : '新增记录'"
      preset="dialog"
      style="width: 600px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="80px"
      >
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="内容" path="cont">
          <n-input
            v-model:value="formData.cont"
            type="textarea"
            placeholder="请输入内容"
            :rows="3"
          />
        </n-form-item>
        <n-form-item label="性别" path="sex">
          <n-select
            v-model:value="formData.sex"
            placeholder="请选择性别"
            :options="sexOptions"
          />
        </n-form-item>
        <n-form-item label="班级" path="classes">
          <n-select
            v-model:value="formData.classes"
            placeholder="请选择班级"
            :options="classesOptions"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, h } from 'vue';
import { NButton, NSpace, NTag } from 'naive-ui';
import { 
  SearchOutlined, 
  ReloadOutlined, 
  PlusOutlined, 
  DeleteOutlined
} from '@vicons/antd';
import { useMessage, useDialog } from 'naive-ui';
import {
  addDailyRecord,
  getDailyList,
  updateDailyRecord,
  deleteDailyRecord,
  batchDeleteDailyRecords,
  type DailyRecord,
  type DailyListParams
} from '@/api/eqms/daily';
import { usePermission } from '@/hooks/usePermission';

// 响应式数据
const message = useMessage();
const dialog = useDialog();

// 权限控制
const { permissions } = usePermission();

const loading = ref(false);
const submitLoading = ref(false);
const modalVisible = ref(false);
const isEdit = ref(false);
const selectedRowKeys = ref<number[]>([]);

// 搜索表单
const searchForm = reactive({
  name: '',
  sex: '',
  classes: ''
});

// 表格数据
const tableData = ref<DailyRecord[]>([]);
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
});

// 表单数据
const formData = reactive<DailyRecord>({
  name: '',
  cont: '',
  sex: '',
  classes: ''
});

const formRef = ref();

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  cont: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  classes: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ]
};

// 选项数据
const sexOptions = [
  { label: '男', value: '男' },
  { label: '女', value: '女' }
];

const classesOptions = [
  { label: '一班', value: '一班' },
  { label: '二班', value: '二班' },
  { label: '三班', value: '三班' },
  { label: '四班', value: '四班' }
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
    title: '姓名',
    key: 'name',
    width: 120
  },
  {
    title: '内容',
    key: 'cont',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '性别',
    key: 'sex',
    width: 80,
    render: (row: DailyRecord) => {
      return h(NTag, {
        type: row.sex === '男' ? 'info' : 'warning'
      }, { default: () => row.sex });
    }
  },
  {
    title: '班级',
    key: 'classes',
    width: 100
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

// 获取数据列表
const fetchData = async () => {
  loading.value = true;
  try {
    const params: DailyListParams = {
      page: pagination.page,
      size: pagination.pageSize,
      ...(searchForm.name && { name: searchForm.name }),
      ...(searchForm.sex && { sex: searchForm.sex }),
      ...(searchForm.classes && { classes: searchForm.classes })
    };
    
    const response = await getDailyList(params);
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
  searchForm.name = '';
  searchForm.sex = '';
  searchForm.classes = '';
  pagination.page = 1;
  fetchData();
};

// 新增
const handleAdd = () => {
  isEdit.value = false;
  Object.assign(formData, {
    name: '',
    cont: '',
    sex: '',
    classes: ''
  });
  modalVisible.value = true;
};

// 编辑
const handleEdit = (row: DailyRecord) => {
  isEdit.value = true;
  Object.assign(formData, { ...row });
  modalVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitLoading.value = true;
    
    if (isEdit.value) {
      await updateDailyRecord(formData.id!, formData);
      message.success('更新成功');
    } else {
      await addDailyRecord(formData);
      message.success('新增成功');
    }
    
    modalVisible.value = false;
    fetchData();
  } catch (error) {
    console.error('提交失败:', error);
    message.error('操作失败');
  } finally {
    submitLoading.value = false;
  }
};

// 删除
const handleDelete = async (id: number) => {
  try {
    await deleteDailyRecord(id);
    message.success('删除成功');
    fetchData();
  } catch (error) {
    console.error('删除失败:', error);
    message.error('删除失败');
  }
};

// 批量删除
const handleBatchDelete = () => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchDeleteDailyRecords({ ids: selectedRowKeys.value });
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

// 显示权限演示
const showPermissionDemo = () => {
  dialog.info({
    title: '权限控制演示',
    content: '权限控制功能已实现！\n\n当前页面按钮会根据用户权限动态显示/隐藏：\n• 新增按钮：需要 eqms:maintenance:daily:add 权限\n• 编辑按钮：需要 eqms:maintenance:daily:edit 权限\n• 删除按钮：需要 eqms:maintenance:daily:delete 权限\n• 批量删除按钮：需要 eqms:maintenance:daily:batchDelete 权限\n\n您可以通过权限演示页面动态切换权限来测试效果。',
    positiveText: '了解',
    style: 'width: 500px'
  });
};

// 初始化
onMounted(() => {
  fetchData();
});

// TypeScript 误报修复 - 这些图标在模板中使用
void SearchOutlined;
void ReloadOutlined;
void PlusOutlined;
void DeleteOutlined;
</script>

<style lang="less" scoped>
.daily-maintenance {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;

  .search-section {
    margin-bottom: 16px;
    padding: 16px;
    background: white;
    border-radius: 6px;
  }

  .action-section {
    margin-bottom: 16px;
    padding: 16px;
    background: white;
    border-radius: 6px;
  }
}
</style>
