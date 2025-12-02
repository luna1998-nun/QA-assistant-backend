import type { UserConfig, ConfigEnv } from 'vite';
import { loadEnv } from 'vite';
import { resolve } from 'path';
import { wrapperEnv } from './build/utils';
import { createVitePlugins } from './build/vite/plugin';
import { OUTPUT_DIR } from './build/constant';
import { createProxy } from './build/vite/proxy';
import pkg from './package.json';
import { format } from 'date-fns';

/**
 * Vite 配置文件
 * 
 * 关于 "Generated an empty chunk: 'vue'" 警告的解决方案：
 * 
 * 问题原因：
 * - Vue 被单独分包，但使用量不足以生成独立的 chunk
 * - 导致生成了空的 Vue chunk
 * 
 * 解决方案：
 * 1. 将 Vue 相关库合并到 vendor chunk 中
 * 2. 避免同一个库被分配到多个 chunk
 * 
 * 使用方法：
 * - 如果遇到空 chunk 警告，取消注释 vendor 配置
 * - 如果不需要特殊分包，保持当前配置即可
 */
const { dependencies, devDependencies, name, version } = pkg;

const __APP_INFO__ = {
  pkg: { dependencies, devDependencies, name, version },
  lastBuildTime: format(new Date(), 'yyyy-MM-dd HH:mm:ss'),
};

function pathResolve(dir: string) {
  return resolve(process.cwd(), '.', dir);
}

export default ({ command, mode }: ConfigEnv): UserConfig => {
  const root = process.cwd();
  const env = loadEnv(mode, root);
  const viteEnv = wrapperEnv(env);
  const { VITE_PUBLIC_PATH, VITE_PORT, VITE_PROXY } = viteEnv;
  const isBuild = command === 'build';
  return {
    base: VITE_PUBLIC_PATH,
    esbuild: {},
    resolve: {
      alias: [
        {
          find: /\/#\//,
          replacement: pathResolve('types') + '/',
        },
        {
          find: '@',
          replacement: pathResolve('src') + '/',
        },
      ],
      dedupe: ['vue'],
    },
    plugins: createVitePlugins(viteEnv, isBuild),
    define: {
      __APP_ENV__: JSON.stringify(env.APP_ENV),
      __APP_INFO__: JSON.stringify(__APP_INFO__),
      __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false,
    },
    server: {
      host: true,
      port: VITE_PORT,
      // proxy: createProxy(VITE_PROXY),
      proxy: {
        // [修复] 优先拦截 TTS 请求转发给 Python 服务 (8001)
        // 必须放在 /api/ai 前面，否则会被通用代理拦截
        '/api/ai/tts': {
          target: 'http://localhost:8001',
          changeOrigin: true,
          // Python脚本里已经写了完整路径 /api/ai/tts/speak，所以这里不需要 rewrite
        },
        // AI Assistant 后端接口代理（非 TTS 请求）
        '/api/ai/dispatch_app/chat': {
          target: 'http://10.59.51.36:8123/',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => {
            // 保持 /api 前缀
            return path;
          }
        },
        '/api/ai/dispatch_app/chat/history': {
          target: 'http://10.59.51.36:8123/',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => {
            // 保持 /api 前缀
            return path;
          }
        },
        '/api/v1/po/eqms': {
          target: 'http://127.0.0.1:9000/',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => {
            // 保持 /api 前缀，不进行重写
            return path;
          }
        },
        '/api/v1/po/plan': {
          target: 'http://127.0.0.1:9000/',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => {
            // 保持 /api 前缀，不进行重写
            return path;
          }
        },
        // Ollama 代理配置
        '/ollama': {
          target: 'http://10.78.183.178:11434',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => {
            // 将 /ollama 重写为 /api
            return path.replace(/^\/ollama/, '/api');
          }
        },
        // [移除] 通用 /api/ai 代理，避免与 TTS 代理冲突
      },
    },
    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'naive-ui',
        '@vueuse/core',
        'lodash-es'
      ],
      exclude: ['vue-demi'],
    },
    build: {
      target: 'es2020',
      cssTarget: 'chrome80',
      outDir: OUTPUT_DIR,
      reportCompressedSize: false,
      chunkSizeWarningLimit: 2000,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: true,
          drop_debugger: true,
        },
      },
      rollupOptions: {
        external: (id) => {
          return id.includes('app.config.js');
        },
        output: {
          manualChunks: (id) => {
            // Naive UI 精细分包
            if (id.includes('naive-ui')) {
              if (id.includes('naive-ui/es/components')) {
                return 'naive-ui-components';
              }
              if (id.includes('naive-ui/es/composables')) {
                return 'naive-ui-composables';
              }
              if (id.includes('naive-ui/es/theme')) {
                return 'naive-ui-theme';
              }
              return 'naive-ui-core';
            }
            
            // 其他库分包
            if (id.includes('lodash-es')) return 'lodash-es';
            if (id.includes('vue-router')) return 'vue-router';
            if (id.includes('@vueup/vue-quill')) return 'vue-quill';
            if (id.includes('@vicons/antd')) return 'vicons-antd';
            if (id.includes('@vicons/ionicons5')) return 'vicons-ionicons5';
            if (id.includes('vuedraggable')) return 'vuedraggable';
            if (id.includes('echarts')) return 'echarts';
            if (id.includes('@vueuse/core')) return 'vueuse';
            if (id.includes('vue')) return 'vue';
            if (id.includes('pinia')) return 'pinia';
            
            // 第三方库
            if (id.includes('node_modules')) {
              return 'vendor';
            }
          },
        },
      },
    },
  };
};
