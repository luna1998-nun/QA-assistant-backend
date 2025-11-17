/**
 * 日常维护 API 服务
 */

import { AlovaAxios } from '@/utils/http/alova';
 
import { apiConfig } from '@/config/api.config';

// 数据模型接口
export interface DailyRecord {
  id?: number;
  name: string;
  cont: string;
  sex: string;
  classes: string;
  createTime?: string;
  updateTime?: string;
}

// 分页查询参数
export interface DailyListParams {
  page: number;
  size: number;
  name?: string;
  sex?: string;
  classes?: string;
}

// 分页响应数据
export interface DailyListResponse {
  records: DailyRecord[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 批量删除参数
export interface BatchDeleteParams {
  ids: number[];
}

/**
 * 新增记录
 */
export const addDailyRecord = (data: Omit<DailyRecord, 'id'>) => {
  return AlovaAxios.Post(apiConfig.baseURL_eqms+'/test/add', data);
};

/**
 * 查询单条记录
 */
export const getDailyRecord = (id: number) => {
  return AlovaAxios.Get(apiConfig.baseURL_eqms+`/test/get/${id}`);
};

/**
 * 分页查询记录
 */
export const getDailyList = (params: DailyListParams) => {
  return AlovaAxios.Get(apiConfig.baseURL_eqms+'/test/list', {
    params: {
      page: params.page,
      size: params.size,
      ...(params.name && { name: params.name }),
      ...(params.sex && { sex: params.sex }),
      ...(params.classes && { classes: params.classes }),
    },
    meta: {
      isReturnNativeResponse: true,
    },
  });
};

/**
 * 更新记录
 */
export const updateDailyRecord = (id: number, data: Omit<DailyRecord, 'id'>) => {
  return AlovaAxios.Put(apiConfig.baseURL_eqms+`/test/update/${id}`, data);
};

/**
 * 删除单条记录
 */
export const deleteDailyRecord = (id: number) => {
  return AlovaAxios.Delete(apiConfig.baseURL_eqms+`/test/delete/${id}`);
};

/**
 * 批量删除记录
 */
export const batchDeleteDailyRecords = (data: BatchDeleteParams) => {
  return AlovaAxios.Delete(apiConfig.baseURL_eqms+'/test/batchDelete', data);
};
