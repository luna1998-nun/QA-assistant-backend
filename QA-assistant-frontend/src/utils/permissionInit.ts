/**
 * 权限初始化工具
 */
import { initUserPermissions, clearUserPermissions } from './permission';

/**
 * 应用启动时初始化权限
 */
export const initAppPermissions = async (): Promise<void> => {
  try {
    console.log('正在初始化用户权限...');
    await initUserPermissions();
    console.log('用户权限初始化完成');
  } catch (error) {
    console.error('用户权限初始化失败:', error);
    throw error;
  }
};

/**
 * 用户登录后初始化权限
 */
export const initUserLoginPermissions = async (): Promise<void> => {
  try {
    console.log('用户登录，正在加载权限...');
    await initUserPermissions();
    console.log('用户权限加载完成');
  } catch (error) {
    console.error('用户权限加载失败:', error);
    throw error;
  }
};

/**
 * 用户退出登录时清除权限
 */
export const clearUserLoginPermissions = (): void => {
  console.log('用户退出登录，清除权限数据');
  clearUserPermissions();
};

/**
 * 权限刷新
 */
export const refreshPermissions = async (): Promise<void> => {
  try {
    console.log('正在刷新用户权限...');
    await initUserPermissions();
    console.log('用户权限刷新完成');
  } catch (error) {
    console.error('用户权限刷新失败:', error);
    throw error;
  }
};

