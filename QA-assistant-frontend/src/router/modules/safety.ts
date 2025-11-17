import type { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { SafetyOutlined, EyeOutlined, BarChartOutlined, FileTextOutlined } from '@vicons/antd';
import { renderIcon } from '@/utils/index';

const safetyRoutes: RouteRecordRaw = {
  path: '/safety',
  name: 'SAFETY',
  component: Layout,
  redirect: '/safety/monitoring',
  meta: {
    title: '油气流态势监测',
    icon: renderIcon(SafetyOutlined),
    order: 7,
    isRoot: true,
  },
  children: [
    {
      path: 'index',
      name: 'SAFETYIndex',
      component: () => import('@/views/safety/index.vue'),
      meta: {
        title: '油气流态势监测',
        icon: renderIcon(SafetyOutlined),
      },
    },
    {
      path: 'monitoring',
      name: 'SAFETYMonitoring',
      component: () => import('@/views/safety/monitoring/index.vue'),
      meta: {
        title: '实时监测',
        icon: renderIcon(EyeOutlined),
      },
    },
    {
      path: 'analysis',
      name: 'SAFETYAnalysis',
      component: () => import('@/views/safety/analysis/index.vue'),
      meta: {
        title: '态势分析',
        icon: renderIcon(BarChartOutlined),
      },
    },
    {
      path: 'report',
      name: 'SAFETYReport',
      component: () => import('@/views/safety/report/index.vue'),
      meta: {
        title: '监测报告',
        icon: renderIcon(FileTextOutlined),
      },
    },
  ],
};

export default safetyRoutes;
