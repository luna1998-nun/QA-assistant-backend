/**
 * 性能监控工具
 * 用于监控和优化应用性能
 */

interface PerformanceMetrics {
  loadTime: number;
  renderTime: number;
  memoryUsage?: number;
  networkRequests: number;
  errors: number;
}

class PerformanceMonitor {
  private metrics: PerformanceMetrics = {
    loadTime: 0,
    renderTime: 0,
    networkRequests: 0,
    errors: 0
  };

  private observers: PerformanceObserver[] = [];

  constructor() {
    this.init();
  }

  private init() {
    // 监控页面加载性能
    this.observePageLoad();
    
    // 监控内存使用
    this.observeMemoryUsage();
    
    // 监控网络请求
    this.observeNetworkRequests();
    
    // 监控错误
    this.observeErrors();
  }

  private observePageLoad() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach((entry) => {
          if (entry.entryType === 'navigation') {
            this.metrics.loadTime = entry.duration;
            console.log('页面加载时间:', entry.duration + 'ms');
          }
        });
      });
      
      observer.observe({ entryTypes: ['navigation'] });
      this.observers.push(observer);
    }
  }

  private observeMemoryUsage() {
    if ('memory' in performance) {
      const checkMemory = () => {
        const memory = (performance as any).memory;
        if (memory) {
          this.metrics.memoryUsage = memory.usedJSHeapSize;
          console.log('内存使用:', Math.round(memory.usedJSHeapSize / 1024 / 1024) + 'MB');
        }
      };
      
      // 每30秒检查一次内存使用
      setInterval(checkMemory, 30000);
      checkMemory();
    }
  }

  private observeNetworkRequests() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach((entry) => {
          if (entry.entryType === 'resource') {
            this.metrics.networkRequests++;
          }
        });
      });
      
      observer.observe({ entryTypes: ['resource'] });
      this.observers.push(observer);
    }
  }

  private observeErrors() {
    window.addEventListener('error', (event) => {
      this.metrics.errors++;
      console.error('页面错误:', event.error);
    });

    window.addEventListener('unhandledrejection', (event) => {
      this.metrics.errors++;
      console.error('未处理的Promise拒绝:', event.reason);
    });
  }

  /**
   * 获取性能指标
   */
  getMetrics(): PerformanceMetrics {
    return { ...this.metrics };
  }

  /**
   * 测量函数执行时间
   */
  measureFunction<T>(name: string, fn: () => T): T {
    const start = performance.now();
    const result = fn();
    const end = performance.now();
    console.log(`${name} 执行时间: ${(end - start).toFixed(2)}ms`);
    return result;
  }

  /**
   * 测量异步函数执行时间
   */
  async measureAsyncFunction<T>(name: string, fn: () => Promise<T>): Promise<T> {
    const start = performance.now();
    const result = await fn();
    const end = performance.now();
    console.log(`${name} 执行时间: ${(end - start).toFixed(2)}ms`);
    return result;
  }

  /**
   * 清理观察者
   */
  cleanup() {
    this.observers.forEach(observer => observer.disconnect());
    this.observers = [];
  }
}

// 创建全局性能监控实例
export const performanceMonitor = new PerformanceMonitor();

/**
 * 性能优化工具函数
 */
export const performanceUtils = {
  /**
   * 防抖函数
   */
  debounce<T extends (...args: any[]) => any>(
    func: T,
    wait: number
  ): (...args: Parameters<T>) => void {
    let timeout: NodeJS.Timeout;
    return (...args: Parameters<T>) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => func.apply(this, args), wait);
    };
  },

  /**
   * 节流函数
   */
  throttle<T extends (...args: any[]) => any>(
    func: T,
    limit: number
  ): (...args: Parameters<T>) => void {
    let inThrottle: boolean;
    return (...args: Parameters<T>) => {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => (inThrottle = false), limit);
      }
    };
  },

  /**
   * 批量处理函数
   */
  batch<T>(items: T[], batchSize: number, processor: (batch: T[]) => void) {
    for (let i = 0; i < items.length; i += batchSize) {
      const batch = items.slice(i, i + batchSize);
      processor(batch);
    }
  },

  /**
   * 延迟执行
   */
  delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
};

// 在开发环境输出性能信息
if (import.meta.env.DEV) {
  // 每5秒输出一次性能指标
  setInterval(() => {
    const metrics = performanceMonitor.getMetrics();
    console.log('性能指标:', metrics);
  }, 5000);
}
