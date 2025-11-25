import type { AppRouteModule } from '@/router/types';

const ttsTestRoute: AppRouteModule = {
  path: '/tts-test',
  name: 'TtsTest',
  component: () => import('@/views/tts-test/index.vue'),
  meta: {
    title: 'TTS测试',
    hideTab: true,
    hideMenu: true,
  },
};

export default ttsTestRoute;