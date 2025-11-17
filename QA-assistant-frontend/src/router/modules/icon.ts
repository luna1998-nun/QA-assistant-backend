import { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { ColorPaletteOutline } from '@vicons/ionicons5';
import { renderIcon } from '@/utils/index';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/icon',
    name: 'Icon',
    component: Layout,
    redirect: '/icon/index',
    meta: {
      title: '图标库',
      sort: 9,
      icon: renderIcon(ColorPaletteOutline),
    },
    children: [
      {
        path: 'index',
        name: 'icon-index',
        meta: {
          title: '图标展示',
        },
        component: () => import('@/views/icon/index.vue'),
      },
    ],
  },
];

export default routes;
