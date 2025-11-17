/**
 * 权限控制组合式函数
 */
import { computed } from 'vue';
import { hasPermission, hasAnyPermission, hasAllPermissions, getUserPermissions } from '@/utils/permission';

export function usePermission() {
  // 获取用户权限列表
  const userPermissions = computed(() => getUserPermissions());

  // 检查单个权限
  const checkPermission = (permissionCode: string) => {
    return hasPermission(permissionCode);
  };

  // 检查多个权限（任一）
  const checkAnyPermission = (permissionCodes: string[]) => {
    return hasAnyPermission(permissionCodes);
  };

  // 检查多个权限（全部）
  const checkAllPermissions = (permissionCodes: string[]) => {
    return hasAllPermissions(permissionCodes);
  };

  // 权限控制对象
  const permissions = computed(() => ({
    // 日常维护页面权限
    daily: {
      add: checkPermission('eqms:maintenance:daily:add'),
      edit: checkPermission('eqms:maintenance:daily:edit'),
      delete: checkPermission('eqms:maintenance:daily:delete'),
      batchDelete: checkPermission('eqms:maintenance:daily:batchDelete'),
      view: checkPermission('eqms:maintenance:daily:view'),
      export: checkPermission('eqms:maintenance:daily:export'),
    }
  }));

  return {
    userPermissions,
    checkPermission,
    checkAnyPermission,
    checkAllPermissions,
    permissions
  };
}
