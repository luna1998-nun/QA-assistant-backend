import type { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { MonitorOutlined, EyeOutlined, BarChartOutlined, BellOutlined } from '@vicons/antd';
import { renderIcon } from '@/utils/index';

const monitorRoutes: RouteRecordRaw = {
  path: '/monitor',
  name: 'MONITOR',
  component: Layout,
  redirect: '/monitor/realtime',
  meta: {
    title: '大监控管理',
    icon: renderIcon(MonitorOutlined),
    order: 4,
    isRoot: true,
  },
  children: [
    {
      path: 'index',
      name: 'MONITORIndex',
      component: () => import('@/views/monitor/index.vue'),
      meta: {
        title: '大监控管理',
        icon: renderIcon(MonitorOutlined),
      },
    },
    {
      path: 'realtime',
      name: 'MONITORRealtime',
      component: () => import('@/views/monitor/realtime/index.vue'),
      meta: {
        title: '实时监控',
        icon: renderIcon(EyeOutlined),
      },
    },
    {
      path: 'analysis',
      name: 'MONITORAnalysis',
      component: () => import('@/views/monitor/analysis/index.vue'),
      meta: {
        title: '数据分析',
        icon: renderIcon(BarChartOutlined),
      },
    },
    {
      path: 'alarm',
      name: 'MONITORAlarm',
      component: () => import('@/views/monitor/alarm/index.vue'),
      meta: {
        title: '告警管理',
        icon: renderIcon(BellOutlined),
      },
    },
  ],
};

export default monitorRoutes;
