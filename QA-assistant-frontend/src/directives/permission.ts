/**
 * 权限控制指令
 */
import type { Directive } from 'vue';
import { hasPermission, hasAnyPermission } from '@/utils/permission';

export const permission: Directive = {
  mounted(el: HTMLElement, binding) {
    const { value } = binding;
    if (value) {
      const hasAuth = Array.isArray(value) ? hasAnyPermission(value) : hasPermission(value);
      if (!hasAuth) {
        el.style.display = 'none';
      }
    }
  },
  updated(el: HTMLElement, binding) {
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

// 默认导出
export default permission;