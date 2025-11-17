import { RouteRecordRaw } from 'vue-router';
import { Layout } from '@/router/constant';
import { BookOutlined } from '@vicons/antd';
import { renderIcon } from '@/utils/index';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/dev-standards',
    name: 'DevStandards',
    redirect: '/dev-standards/api-development-guide',
    component: Layout,
    meta: {
      title: '开发规范',
      icon: renderIcon(BookOutlined),
      sort: 1.5, // 放在系统设置之后，测试模块之前
    },
    children: [
      {
        path: 'api-development-guide',
        name: 'dev_standards_api_development_guide',
        meta: {
          title: 'API开发指南',
        },
        component: () => import('@/views/dev-standards/api-development-guide.vue'),
      },
      {
        path: 'module-development-guide',
        name: 'dev_standards_module_development_guide',
        meta: {
          title: '模块开发指南',
        },
        component: () => import('@/views/dev-standards/module-development-guide.vue'),
      },
      {
        path: 'permission-control-guide',
        name: 'dev_standards_permission_control_guide',
        meta: {
          title: '权限控制指南',
        },
        component: () => import('@/views/dev-standards/permission-control-guide.vue'),
      },
    ],
  },
];

export default routes;
