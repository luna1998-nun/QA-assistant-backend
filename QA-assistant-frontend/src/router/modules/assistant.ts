// src/router/modules/assistant.ts

import { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { ChatbubblesOutline } from '@vicons/ionicons5';
import { renderIcon } from '@/utils/index';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/assistant',
    name: 'Assistant',
    component: Layout,
    redirect: '/assistant/index',
    meta: {
      title: '智能问询助手',
      icon: renderIcon(ChatbubblesOutline),
      sort: 0, // 排序，数字越小越靠前
    },
    children: [
      {
        path: 'index',
        name: 'AssistantIndex',
        meta: {
          title: '智能问询',
        },
        component: () => import('@/views/assistant/index.vue'),
      },
    ],
  },
];

export default routes;