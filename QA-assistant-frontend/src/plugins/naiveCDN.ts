/**
 * Naive UI CDN 优化方案
 * 将 Naive UI 通过 CDN 加载，减少打包大小
 */

import type { App } from 'vue';

interface CDNConfig {
  name: string;
  url: string;
  version: string;
  files: string[];
}

// CDN 配置
const naiveCDNConfig: CDNConfig = {
  name: 'naive-ui',
  url: 'https://unpkg.com/naive-ui@2.38.1',
  version: '2.38.1',
  files: [
    'dist/index.css',
    'dist/index.js'
  ]
};

/**
 * 动态加载 CDN 资源
 */
async function loadCDNResource(url: string, type: 'css' | 'js'): Promise<void> {
  return new Promise((resolve, reject) => {
    if (type === 'css') {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = url;
      link.onload = () => resolve();
      link.onerror = () => reject(new Error(`Failed to load CSS: ${url}`));
      document.head.appendChild(link);
    } else {
      const script = document.createElement('script');
      script.src = url;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error(`Failed to load JS: ${url}`));
      document.head.appendChild(script);
    }
  });
}

/**
 * 检查 CDN 资源是否已加载
 */
function isCDNLoaded(): boolean {
  return !!(window as any).naive;
}

/**
 * 等待 CDN 资源加载完成
 */
async function waitForCDN(): Promise<any> {
  return new Promise((resolve, reject) => {
    const maxAttempts = 50; // 最多等待 5 秒
    let attempts = 0;
    
    const checkCDN = () => {
      if (isCDNLoaded()) {
        resolve((window as any).naive);
      } else if (attempts < maxAttempts) {
        attempts++;
        setTimeout(checkCDN, 100);
      } else {
        reject(new Error('CDN loading timeout'));
      }
    };
    
    checkCDN();
  });
}

/**
 * 设置 Naive UI CDN
 */
export async function setupNaiveCDN(app: App<Element>) {
  try {
    console.log('🌐 开始加载 Naive UI CDN...');
    
    // 检查是否已经加载
    if (isCDNLoaded()) {
      console.log('✅ Naive UI CDN 已加载');
      const naive = (window as any).naive;
      app.use(naive);
      return;
    }

    // 加载 CSS
    await loadCDNResource(`${naiveCDNConfig.url}/${naiveCDNConfig.files[0]}`, 'css');
    console.log('✅ Naive UI CSS 加载完成');

    // 加载 JS
    await loadCDNResource(`${naiveCDNConfig.url}/${naiveCDNConfig.files[1]}`, 'js');
    console.log('✅ Naive UI JS 加载完成');

    // 等待全局对象可用
    const naive = await waitForCDN();
    app.use(naive);
    
    console.log('🎉 Naive UI CDN 设置完成');
  } catch (error) {
    console.error('❌ Naive UI CDN 加载失败:', error);
    
    // 降级到本地加载
    console.log('🔄 降级到本地加载...');
    const { setupNaive } = await import('./naive');
    setupNaive(app);
  }
}

/**
 * 预加载 CDN 资源
 */
export function preloadNaiveCDN() {
  if (typeof window !== 'undefined' && !isCDNLoaded()) {
    // 预加载 CSS
    const cssLink = document.createElement('link');
    cssLink.rel = 'preload';
    cssLink.as = 'style';
    cssLink.href = `${naiveCDNConfig.url}/${naiveCDNConfig.files[0]}`;
    document.head.appendChild(cssLink);

    // 预加载 JS
    const jsLink = document.createElement('link');
    jsLink.rel = 'preload';
    jsLink.as = 'script';
    jsLink.href = `${naiveCDNConfig.url}/${naiveCDNConfig.files[1]}`;
    document.head.appendChild(jsLink);
  }
}

/**
 * 检查网络连接质量
 */
function checkNetworkQuality(): 'fast' | 'slow' | 'offline' {
  if (!navigator.onLine) return 'offline';
  
  // 简单的网络质量检测
  const connection = (navigator as any).connection;
  if (connection) {
    if (connection.effectiveType === '4g' || connection.effectiveType === '3g') {
      return 'fast';
    }
    return 'slow';
  }
  
  return 'fast'; // 默认假设网络良好
}

/**
 * 智能选择加载策略
 */
export async function setupSmartNaive(app: App<Element>) {
  const networkQuality = checkNetworkQuality();
  
  if (networkQuality === 'offline') {
    console.log('📴 离线模式，使用本地资源');
    const { setupNaive } = await import('./naive');
    setupNaive(app);
    return;
  }
  
  if (networkQuality === 'fast') {
    console.log('🚀 网络良好，使用 CDN 加载');
    await setupNaiveCDN(app);
  } else {
    console.log('🐌 网络较慢，使用本地资源');
    const { setupNaive } = await import('./naive');
    setupNaive(app);
  }
}
