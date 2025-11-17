/**
 * 权限控制工具函数
 */

// 模拟权限数据
export interface Permission {
  id: string;
  name: string;
  code: string;
  type: 'menu' | 'button';
  parentId?: string;
}

// 模拟用户权限数据
export const mockUserPermissions: Permission[] = [
  // 菜单权限
  { id: '1', name: '设备管理', code: 'eqms', type: 'menu' },
  { id: '2', name: '日常维护', code: 'eqms:maintenance:daily', type: 'menu', parentId: '1' },
  
  // 按钮权限
  { id: '3', name: '新增记录', code: 'eqms:maintenance:daily:add', type: 'button', parentId: '2' },
  { id: '4', name: '编辑记录', code: 'eqms:maintenance:daily:edit', type: 'button', parentId: '2' },
  { id: '5', name: '删除记录', code: 'eqms:maintenance:daily:delete', type: 'button', parentId: '2' },
  { id: '6', name: '批量删除', code: 'eqms:maintenance:daily:batchDelete', type: 'button', parentId: '2' },
  { id: '7', name: '查看详情', code: 'eqms:maintenance:daily:view', type: 'button', parentId: '2' },
  { id: '8', name: '导出数据', code: 'eqms:maintenance:daily:export', type: 'button', parentId: '2' },
];

// 动态权限存储
let dynamicPermissions: Permission[] = [...mockUserPermissions];

// 获取用户权限列表
export const getUserPermissions = (): Permission[] => {
  // 这里可以从 store 或 API 获取用户权限
  // 现在使用动态权限数据
  return dynamicPermissions;
};

// 设置用户权限
export const setUserPermissions = (permissions: Permission[]) => {
  dynamicPermissions = permissions;
};

// 添加权限
export const addPermission = (permission: Permission) => {
  if (!dynamicPermissions.find(p => p.code === permission.code)) {
    dynamicPermissions.push(permission);
  }
};

// 移除权限
export const removePermission = (permissionCode: string) => {
  dynamicPermissions = dynamicPermissions.filter(p => p.code !== permissionCode);
};

// 检查是否有指定权限
export const hasPermission = (permissionCode: string): boolean => {
  const userPermissions = getUserPermissions();
  return userPermissions.some(permission => permission.code === permissionCode);
};

// 检查是否有任一权限
export const hasAnyPermission = (permissionCodes: string[]): boolean => {
  return permissionCodes.some(code => hasPermission(code));
};

// 检查是否有所有权限
export const hasAllPermissions = (permissionCodes: string[]): boolean => {
  return permissionCodes.every(code => hasPermission(code));
};

// 权限控制指令
export const permissionDirective = {
  mounted(el: HTMLElement, binding: any) {
    const { value } = binding;
    if (value) {
      const hasAuth = Array.isArray(value) ? hasAnyPermission(value) : hasPermission(value);
      if (!hasAuth) {
        el.style.display = 'none';
      }
    }
  },
  updated(el: HTMLElement, binding: any) {
    const { value } = binding;
    if (value) {
      const hasAuth = Array.isArray(value) ? hasAnyPermission(value) : hasPermission(value);
      if (!hasAuth) {
        el.style.display = 'none';
      } else {
        el.style.display = '';
      }
    }
  }
};
