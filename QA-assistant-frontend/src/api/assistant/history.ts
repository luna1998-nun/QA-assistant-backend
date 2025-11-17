import { storage } from '@/utils/Storage';

/**
 * 对话元数据接口
 */
export interface ChatMetadata {
  id: string;
  title: string;
  time: string;
  type: string;
  messageCount: number;
}

/**
 * 将 Message 转换为前端消息格式
 */
export interface FrontendMessage {
  id: number;
  role: 'user' | 'assistant';
  text: string;
  thinkingContent?: string;
  thinkingTime?: number;
  isTyping?: boolean;
  displayText?: string;
}

/**
 * 获取所有历史对话列表
 */
export async function getChatHistoryList(): Promise<ChatMetadata[]> {
  try {
    const response = await fetch('/api/ai/dispatch_app/chat/history/list', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('获取历史对话列表失败:', error);
    return [];
  }
}

/**
 * 从 localStorage 获取所有对话元数据
 */
export function getAllChatMetadata(): ChatMetadata[] {
  const metadataList = storage.get('chat-history-list', []);
  return Array.isArray(metadataList) ? metadataList : [];
}

/**
 * 保存对话元数据到 localStorage
 */
export function saveChatMetadata(metadata: ChatMetadata): void {
  const list = getAllChatMetadata();
  const index = list.findIndex(item => item.id === metadata.id);
  
  if (index !== -1) {
    // 更新已存在的记录
    list[index] = metadata;
  } else {
    // 添加新记录
    list.unshift(metadata);
  }
  
  storage.set('chat-history-list', list);
}

/**
 * 从 localStorage 删除对话元数据
 */
export function removeChatMetadata(chatId: string): void {
  const list = getAllChatMetadata();
  const filtered = list.filter(item => item.id !== chatId);
  storage.set('chat-history-list', filtered);
}

/**
 * 同步本地和后端的对话列表
 */
export async function syncChatHistoryList(): Promise<ChatMetadata[]> {
  // 从后端获取最新列表
  const backendList = await getChatHistoryList();
  
  // 更新 localStorage
  if (backendList.length > 0) {
    storage.set('chat-history-list', backendList);
  }
  
  return backendList;
}

/**
 * 清除所有对话历史（仅本地）
 */
export function clearLocalChatHistory(): void {
  storage.remove('chat-history-list');
}

