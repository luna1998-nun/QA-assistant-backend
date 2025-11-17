import { h, unref } from 'vue';
import type { App, Plugin, Component } from 'vue';
import { NIcon, NTag } from 'naive-ui';
import { PageEnum } from '@/enums/pageEnum';
import { isObject } from './is/index';
import { cloneDeep } from 'lodash-es';
/**
 * render 图标
 * */
export function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) });
}
/**
 * font 图标(Font class)
 * */
export function renderFontClassIcon(icon: string, iconName = 'iconfont') {
  return () => h('span', { class: [iconName, icon] });
}
/**
 * font 图标(Unicode)
 * */
export function renderUnicodeIcon(icon: string, iconName = 'iconfont') {
  return () => h('span', { class: [iconName], innerHTML: icon });
}
/**
 * font svg 图标
 * */
export function renderfontsvg(icon) {
  return () =>
    h(NIcon, null, {
      default: () =>
        h('svg', { class: `icon`, 'aria-hidden': 'true' }, h('use', { 'xlink:href': `#${icon}` })),
    });
}

/**
 * render new Tag
 * */
const newTagColors = { color: '#f90', textColor: '#fff', borderColor: '#f90' };
export function renderNew(type = 'warning', text = 'New', color: object = newTagColors) {
  return () =>
    h(
      NTag as any,
      {
        type,
        round: true,
        size: 'small',
        color,
      },
      { default: () => text }
    );
}

/**
 * 递归组装菜单格式
 */
export function generatorMenu(routerMap: Array<any>) {
  return filterRouter(routerMap).map((item) => {
    const isRoot = isRootRouter(item);
    const info = isRoot ? item.children[0] : item;
    const currentMenu = {
      ...info,
      ...info.meta,
      label: info.meta?.title,
      key: info.name,
      icon: isRoot ? item.meta?.icon : info.meta?.icon,
    };
    // 是否有子菜单，并递归处理
    if (info.children && info.children.length > 0) {
      // Recursion
      currentMenu.children = generatorMenu(info.children);
    }
    return currentMenu;
  });
}

/**
 * 混合菜单
 * */
export function generatorMenuMix(routerMap: Array<any>, routerName: string, location: string) {
  const cloneRouterMap = cloneDeep(routerMap);
  const newRouter = filterRouter(cloneRouterMap);
  if (location === 'header') {
    const firstRouter: any[] = [];
    newRouter.forEach((item) => {
      const isRoot = isRootRouter(item);
      const info = isRoot ? item.children[0] : item;
      info.children = undefined;
      const currentMenu = {
        ...info,
        ...info.meta,
        label: info.meta?.title,
        key: info.name,
      };
      firstRouter.push(currentMenu);
    });
    return firstRouter;
  } else {
    return getChildrenRouter(newRouter.filter((item) => item.name === routerName));
  }
}

/**
 * 递归组装子菜单
 * */
export function getChildrenRouter(routerMap: Array<any>) {
  return filterRouter(routerMap).map((item) => {
    const isRoot = isRootRouter(item);
    const info = isRoot ? item.children[0] : item;
    const currentMenu = {
      ...info,
      ...info.meta,
      label: info.meta?.title,
      key: info.name,
    };
    // 是否有子菜单，并递归处理
    if (info.children && info.children.length > 0) {
      // Recursion
      currentMenu.children = getChildrenRouter(info.children);
    }
    return currentMenu;
  });
}

/**
 * 判断根路由 Router
 * */
export function isRootRouter(item) {
  return (
    item.meta?.alwaysShow != true &&
    item?.children?.filter((item) => !Boolean(item?.meta?.hidden))?.length === 1
  );
}

/**
 * 排除Router
 * */
export function filterRouter(routerMap: Array<any>) {
  return routerMap.filter((item) => {
    return (
      (item.meta?.hidden || false) != true &&
      !['/:path(.*)*', '/', PageEnum.REDIRECT, PageEnum.BASE_LOGIN].includes(item.path)
    );
  });
}

export const withInstall = <T extends Component>(component: T, alias?: string) => {
  const comp = component as any;
  comp.install = (app: App) => {
    app.component(comp.name || comp.displayName, component);
    if (alias) {
      app.config.globalProperties[alias] = component;
    }
  };
  return component as T & Plugin;
};

/**
 * 生成设备管理专用菜单
 * */
export function generatorEqmsMenu(routerMap: Array<any>) {
  const eqmsRoute = routerMap.find(item => item.name === 'EQMS');
  if (!eqmsRoute || !eqmsRoute.children) {
    return [];
  }
  
  // 处理设备管理模块的子菜单，包括三级菜单
  return eqmsRoute.children.map((item) => {
    const menuItem = {
      ...item,
      ...item.meta,
      label: item.meta?.title,
      key: item.name,
      icon: item.meta?.icon,
    };
    
    // 如果有子菜单，递归处理
    if (item.children && item.children.length > 0) {
      menuItem.children = item.children.map((child) => ({
        ...child,
        ...child.meta,
        label: child.meta?.title,
        key: child.name,
        icon: child.meta?.icon,
      }));
    }
    
    return menuItem;
  });
}

/**
 * 生成大监控管理专用菜单
 * */
export function generatorMonitorMenu(routerMap: Array<any>) {
  const monitorRoute = routerMap.find(item => item.name === 'MONITOR');
  if (!monitorRoute || !monitorRoute.children) {
    return [];
  }
  
  return monitorRoute.children.map((item) => {
    return {
      ...item,
      ...item.meta,
      label: item.meta?.title,
      key: item.name,
      icon: item.meta?.icon,
    };
  });
}

/**
 * 生成应急协调专用菜单
 * */
export function generatorEmergencyMenu(routerMap: Array<any>) {
  const emergencyRoute = routerMap.find(item => item.name === 'EMERGENCY');
  if (!emergencyRoute || !emergencyRoute.children) {
    return [];
  }
  
  return emergencyRoute.children.map((item) => {
    return {
      ...item,
      ...item.meta,
      label: item.meta?.title,
      key: item.name,
      icon: item.meta?.icon,
    };
  });
}

/**
 * 生成辅助保障专用菜单
 * */
export function generatorSupportMenu(routerMap: Array<any>) {
  const supportRoute = routerMap.find(item => item.name === 'SUPPORT');
  if (!supportRoute || !supportRoute.children) {
    return [];
  }
  
  return supportRoute.children.map((item) => {
    return {
      ...item,
      ...item.meta,
      label: item.meta?.title,
      key: item.name,
      icon: item.meta?.icon,
    };
  });
}

/**
 * 生成油气流态势监测专用菜单
 * */
export function generatorSafetyMenu(routerMap: Array<any>) {
  const safetyRoute = routerMap.find(item => item.name === 'SAFETY');
  if (!safetyRoute || !safetyRoute.children) {
    return [];
  }
  
  return safetyRoute.children.map((item) => {
    return {
      ...item,
      ...item.meta,
      label: item.meta?.title,
      key: item.name,
      icon: item.meta?.icon,
    };
  });
}

/**
 * 生成开发规范专用菜单
 * */
export function generatorDevStandardsMenu(routerMap: Array<any>) {
  const devStandardsRoute = routerMap.find(item => item.name === 'DevStandards');
  if (!devStandardsRoute || !devStandardsRoute.children) {
    return [];
  }
  
  return devStandardsRoute.children.map((item) => {
    return {
      ...item,
      ...item.meta,
      label: item.meta?.title,
      key: item.name,
      icon: item.meta?.icon,
    };
  });
}

/**
 *  找到对应的节点
 * */
let result = null;
export function getTreeItem(data: any[], key?: string | number): any {
  data.map((item) => {
    if (item.key === key) {
      result = item;
    } else {
      if (item.children && item.children.length) {
        getTreeItem(item.children, key);
      }
    }
  });
  return result;
}

/**
 *  找到所有节点
 * */
const treeAll: any[] = [];
export function getTreeAll(data: any[]): any[] {
  data.map((item) => {
    treeAll.push(item.key);
    if (item.children && item.children.length) {
      getTreeAll(item.children);
    }
  });
  return treeAll;
}

// dynamic use hook props
export function getDynamicProps<T extends {}, U>(props: T): Partial<U> {
  const ret: Recordable = {};

  Object.keys(props).map((key) => {
    ret[key] = unref((props as Recordable)[key]);
  });

  return ret as Partial<U>;
}

export function deepMerge<T = any>(src: any = {}, target: any = {}): T {
  let key: string;
  for (key in target) {
    src[key] = isObject(src[key]) ? deepMerge(src[key], target[key]) : (src[key] = target[key]);
  }
  return src;
}

/**
 * Sums the passed percentage to the R, G or B of a HEX color
 * @param {string} color The color to change
 * @param {number} amount The amount to change the color by
 * @returns {string} The processed part of the color
 */
function addLight(color: string, amount: number) {
  const cc = parseInt(color, 16) + amount;
  const c = cc > 255 ? 255 : cc;
  return c.toString(16).length > 1 ? c.toString(16) : `0${c.toString(16)}`;
}

/**
 * Lightens a 6 char HEX color according to the passed percentage
 * @param {string} color The color to change
 * @param {number} amount The amount to change the color by
 * @returns {string} The processed color represented as HEX
 */
export function lighten(color: string, amount: number) {
  color = color.indexOf('#') >= 0 ? color.substring(1, color.length) : color;
  amount = Math.trunc((255 * amount) / 100);
  return `#${addLight(color.substring(0, 2), amount)}${addLight(
    color.substring(2, 4),
    amount
  )}${addLight(color.substring(4, 6), amount)}`;
}

/**
 * 判断是否 url
 * */
export function isUrl(url: string) {
  return /^(http|https):\/\//g.test(url);
}
