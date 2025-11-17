/**
 * API 配置文件
 * 用于管理不同环境的API地址
 */

// 开发环境配置
const devConfig = {
  baseURL_eqms: '/api/v1/po/eqms',
  baseURL_plan: '/api/v1/po/plan',
  timeout: 10000,
  withCredentials: false,
};

// 生产环境配置
const prodConfig = {
  baseURL_eqms: import.meta.env.VITE_GLOB_API_URL + '/api/v1/po/eqms',
  baseURL_plan: import.meta.env.VITE_GLOB_API_URL + '/api/v1/po/plan',
  timeout: 10000,
  withCredentials: false,
};

// 根据环境选择配置
export const apiConfig = import.meta.env.DEV ? devConfig : prodConfig;

