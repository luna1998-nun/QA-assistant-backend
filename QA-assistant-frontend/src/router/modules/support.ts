import type { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { ToolOutlined, CarOutlined, DollarOutlined, CheckCircleOutlined } from '@vicons/antd';
import { renderIcon } from '@/utils/index';

const supportRoutes: RouteRecordRaw = {
  path: '/support',
  name: 'SUPPORT',
  component: Layout,
  redirect: '/support/logistics',
  meta: {
    title: '辅助保障',
    icon: renderIcon(ToolOutlined),
    order: 6,
    isRoot: true,
  },
  children: [
    {
      path: 'index',
      name: 'SUPPORTIndex',
      component: () => import('@/views/support/index.vue'),
      meta: {
        title: '辅助保障',
        icon: renderIcon(ToolOutlined),
      },
    },
    {
      path: 'logistics',
      name: 'SUPPORTLogistics',
      component: () => import('@/views/support/logistics/index.vue'),
      meta: {
        title: '后勤保障',
        icon: renderIcon(CarOutlined),
      },
    },
    {
      path: 'finance',
      name: 'SUPPORTFinance',
      component: () => import('@/views/support/finance/index.vue'),
      meta: {
        title: '财务管理',
        icon: renderIcon(DollarOutlined),
      },
    },
    {
      path: 'quality',
      name: 'SUPPORTQuality',
      component: () => import('@/views/support/quality/index.vue'),
      meta: {
        title: '质量监督',
        icon: renderIcon(CheckCircleOutlined),
      },
    },
  ],
};

export default supportRoutes;
