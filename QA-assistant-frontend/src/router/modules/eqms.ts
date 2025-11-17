import type { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { SettingOutlined, ToolOutlined, ExclamationCircleOutlined, BugOutlined, SearchOutlined, FileTextOutlined } from '@vicons/antd';
import { renderIcon } from '@/utils/index';

const eqmsRoutes: RouteRecordRaw = {
  path: '/eqms',
  name: 'EQMS',
  component: Layout,
  redirect: '/eqms/maintenance',
  meta: {
    title: '设备管理',
    icon: renderIcon(SettingOutlined),
    order: 3,
    isRoot: true,
  },
  children: [
    {
      path: 'index',
      name: 'EQMSIndex',
      component: () => import('@/views/eqms/index.vue'),
      meta: {
        title: '设备管理',
        icon: renderIcon(SettingOutlined),
      },
    },
    {
      path: 'maintenance',
      name: 'EQMSMaintenance',
      redirect: '/eqms/maintenance/index',
      meta: {
        title: '设备维护',
        icon: renderIcon(ToolOutlined),
      },
      children: [
        {
          path: 'index',
          name: 'EQMSMaintenanceIndex',
          component: () => import('@/views/eqms/maintenance/index.vue'),
          meta: {
            title: '设备维护',
            icon: renderIcon(ToolOutlined),
          },
        },
        {
          path: 'daily',
          name: 'EQMSMaintenanceDaily',
          component: () => import('@/views/eqms/maintenance/daily/index.vue'),
          meta: {
            title: '日常维护',
            icon: renderIcon(ToolOutlined),
          },
        },
        {
          path: 'preventive',
          name: 'EQMSMaintenancePreventive',
          component: () => import('@/views/eqms/maintenance/preventive/index.vue'),
          meta: {
            title: '预防性维护',
            icon: renderIcon(ToolOutlined),
          },
        },
        {
          path: 'repair',
          name: 'EQMSMaintenanceRepair',
          component: () => import('@/views/eqms/maintenance/repair/index.vue'),
          meta: {
            title: '维修记录',
            icon: renderIcon(FileTextOutlined),
          },
        },
      ],
    },
    {
      path: 'diagnosis',
      name: 'EQMSDiagnosis',
      redirect: '/eqms/diagnosis/index',
      meta: {
        title: '故障诊断',
        icon: renderIcon(ExclamationCircleOutlined),
      },
      children: [
        {
          path: 'index',
          name: 'EQMSDiagnosisIndex',
          component: () => import('@/views/eqms/diagnosis/index.vue'),
          meta: {
            title: '故障诊断',
            icon: renderIcon(ExclamationCircleOutlined),
          },
        },
        {
          path: 'detection',
          name: 'EQMSDiagnosisDetection',
          component: () => import('@/views/eqms/diagnosis/detection/index.vue'),
          meta: {
            title: '故障检测',
            icon: renderIcon(SearchOutlined),
          },
        },
        {
          path: 'analysis',
          name: 'EQMSDiagnosisAnalysis',
          component: () => import('@/views/eqms/diagnosis/analysis/index.vue'),
          meta: {
            title: '故障分析',
            icon: renderIcon(BugOutlined),
          },
        },
        {
          path: 'handling',
          name: 'EQMSDiagnosisHandling',
          component: () => import('@/views/eqms/diagnosis/handling/index.vue'),
          meta: {
            title: '故障处理',
            icon: renderIcon(ToolOutlined),
          },
        },
      ],
    },
  ],
};

export default eqmsRoutes;
