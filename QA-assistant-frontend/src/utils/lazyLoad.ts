/**
 * 懒加载工具函数
 * 用于优化组件和路由的加载性能
 */

import { defineAsyncComponent, type AsyncComponentLoader } from 'vue';
import { Loading } from '@/components/Loading';

/**
 * 创建懒加载组件
 * @param loader 组件加载器
 * @param options 配置选项
 */
export function createLazyComponent(
  loader: AsyncComponentLoader,
  options: {
    delay?: number;
    timeout?: number;
    loadingComponent?: any;
    errorComponent?: any;
    onError?: (error: Error, retry: () => void, fail: () => void, attempts: number) => void;
  } = {}
) {
  const {
    delay = 200,
    timeout = 10000,
    loadingComponent = Loading,
    errorComponent,
    onError
  } = options;

  return defineAsyncComponent({
    loader,
    delay,
    timeout,
    loadingComponent,
    errorComponent,
    onError: onError || ((error, retry, fail, attempts) => {
      console.error('组件加载失败:', error);
      if (attempts <= 3) {
        console.log(`重试加载组件，第 ${attempts} 次`);
        retry();
      } else {
        console.error('组件加载失败次数过多，停止重试');
        fail();
      }
    })
  });
}

/**
 * 路由懒加载
 * @param viewPath 视图路径
 */
export function lazyLoadRoute(viewPath: string) {
  return () => import(/* @vite-ignore */ `../views/${viewPath}.vue`);
}

/**
 * 预加载组件
 * @param loader 组件加载器
 */
export function preloadComponent(loader: AsyncComponentLoader) {
  // 在空闲时间预加载组件
  if ('requestIdleCallback' in window) {
    requestIdleCallback(() => {
      loader();
    });
  } else {
    setTimeout(() => {
      loader();
    }, 100);
  }
}

/**
 * 批量预加载组件
 * @param loaders 组件加载器数组
 */
export function preloadComponents(loaders: AsyncComponentLoader[]) {
  loaders.forEach(loader => {
    preloadComponent(loader);
  });
}
