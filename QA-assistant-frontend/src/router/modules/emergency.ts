import type { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { AlertOutlined, RocketOutlined, TeamOutlined, PhoneOutlined } from '@vicons/antd';
import { renderIcon } from '@/utils/index';

const emergencyRoutes: RouteRecordRaw = {
  path: '/emergency',
  name: 'EMERGENCY',
  component: Layout,
  redirect: '/emergency/response',
  meta: {
    title: '应急协调',
    icon: renderIcon(AlertOutlined),
    order: 5,
    isRoot: true,
  },
  children: [
    {
      path: 'index',
      name: 'EMERGENCYIndex',
      component: () => import('@/views/emergency/index.vue'),
      meta: {
        title: '应急协调',
        icon: renderIcon(AlertOutlined),
      },
    },
    {
      path: 'response',
      name: 'EMERGENCYResponse',
      component: () => import('@/views/emergency/response/index.vue'),
      meta: {
        title: '应急响应',
        icon: renderIcon(RocketOutlined),
      },
    },
    {
      path: 'coordination',
      name: 'EMERGENCYCoordination',
      component: () => import('@/views/emergency/coordination/index.vue'),
      meta: {
        title: '协调指挥',
        icon: renderIcon(TeamOutlined),
      },
    },
    {
      path: 'communication',
      name: 'EMERGENCYCommunication',
      component: () => import('@/views/emergency/communication/index.vue'),
      meta: {
        title: '通信联络',
        icon: renderIcon(PhoneOutlined),
      },
    },
  ],
};

export default emergencyRoutes;
