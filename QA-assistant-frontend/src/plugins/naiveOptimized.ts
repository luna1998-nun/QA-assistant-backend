import type { App } from 'vue';
import { create } from 'naive-ui';

/**
 * 优化的 Naive UI 配置
 * 使用动态导入和按需加载策略
 */

// 核心组件 - 必须导入
const coreComponents = [
  'NMessageProvider',
  'NDialogProvider', 
  'NConfigProvider',
  'NLoadingBarProvider',
  'NNotificationProvider',
];

// 常用组件 - 按需导入
const commonComponents = [
  'NButton',
  'NInput',
  'NIcon',
  'NCard',
  'NLayout',
  'NLayoutHeader',
  'NLayoutContent',
  'NLayoutFooter',
  'NLayoutSider',
  'NMenu',
  'NTooltip',
  'NAvatar',
  'NSpace',
  'NDivider',
  'NTag',
  'NBadge',
  'NAlert',
];

// 表单组件 - 按需导入
const formComponents = [
  'NForm',
  'NFormItem',
  'NSelect',
  'NInputNumber',
  'NCheckbox',
  'NCheckboxGroup',
  'NRadio',
  'NRadioGroup',
  'NSwitch',
];

// 数据展示组件 - 按需导入
const dataComponents = [
  'NTable',
  'NDataTable',
  'NPagination',
  'NList',
  'NListItem',
  'NThing',
  'NDescriptions',
  'NDescriptionsItem',
];

// 导航组件 - 按需导入
const navigationComponents = [
  'NBreadcrumb',
  'NBreadcrumbItem',
  'NDropdown',
  'NTabs',
  'NTabPane',
  'NSteps',
  'NStep',
];

// 反馈组件 - 按需导入
const feedbackComponents = [
  'NModal',
  'NPopover',
  'NProgress',
  'NSpin',
  'NSkeleton',
  'NBackTop',
];

// 其他组件 - 按需导入
const otherComponents = [
  'NGrid',
  'NGridItem',
  'NRow',
  'NCol',
  'NInputGroup',
  'NResult',
  'NUpload',
  'NTree',
  'NCascader',
  'NDatePicker',
  'NTimePicker',
  'NElement',
  'NDrawer',
  'NDrawerContent',
];

/**
 * 动态导入组件
 */
async function loadComponents(componentNames: string[]) {
  const { create } = await import('naive-ui');
  const components = [];
  
  for (const name of componentNames) {
    try {
      const component = await import('naive-ui').then(module => module[name]);
      if (component) {
        components.push(component);
      }
    } catch (error) {
      console.warn(`Failed to load component ${name}:`, error);
    }
  }
  
  return create({ components });
}

/**
 * 创建优化的 Naive UI 实例
 */
export async function createOptimizedNaive() {
  // 首先加载核心组件
  const coreNaive = await loadComponents(coreComponents);
  
  // 然后加载常用组件
  const commonNaive = await loadComponents(commonComponents);
  
  return {
    core: coreNaive,
    common: commonNaive,
    // 按需加载其他组件
    async loadFormComponents() {
      return await loadComponents(formComponents);
    },
    async loadDataComponents() {
      return await loadComponents(dataComponents);
    },
    async loadNavigationComponents() {
      return await loadComponents(navigationComponents);
    },
    async loadFeedbackComponents() {
      return await loadComponents(feedbackComponents);
    },
    async loadOtherComponents() {
      return await loadComponents(otherComponents);
    }
  };
}

/**
 * 设置优化的 Naive UI
 */
export async function setupOptimizedNaive(app: App<Element>) {
  try {
    const naive = await createOptimizedNaive();
    
    // 注册核心组件
    app.use(naive.core);
    
    // 注册常用组件
    app.use(naive.common);
    
    // 将按需加载方法挂载到全局
    app.config.globalProperties.$naiveLoaders = {
      form: naive.loadFormComponents,
      data: naive.loadDataComponents,
      navigation: naive.loadNavigationComponents,
      feedback: naive.loadFeedbackComponents,
      other: naive.loadOtherComponents,
    };
    
    console.log('✅ Naive UI 优化加载完成');
  } catch (error) {
    console.error('❌ Naive UI 加载失败:', error);
    // 降级到基础配置
    const { create } = await import('naive-ui');
    const basicComponents = await loadComponents(coreComponents);
    app.use(basicComponents);
  }
}

/**
 * 组件懒加载 Hook
 */
export function useNaiveComponent(componentName: string) {
  return async () => {
    try {
      const module = await import('naive-ui');
      return module[componentName];
    } catch (error) {
      console.error(`Failed to load component ${componentName}:`, error);
      return null;
    }
  };
}
