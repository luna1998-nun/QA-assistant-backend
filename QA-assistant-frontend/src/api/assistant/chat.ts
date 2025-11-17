// 类型定义
export interface ChatMessage {
  id: number;
  role: 'user' | 'assistant' | 'system';
  content: string;
  thinkingContent?: string;
  thinkingTime?: number;
  isTyping?: boolean;
  displayText?: string;
}

/**
 * 生成唯一的会话ID
 */
export function generateChatId(): string {
  return 'chat-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

/**
 * 简单的配置检查（后端接口不需要前端配置）
 */
export function checkApiConfig(): { valid: boolean; error?: string } {
  return { valid: true };
}

/**
 * SSE 流式响应解析器
 */
class SSEParser {
  private buffer = '';
  private eventType = '';
  private eventData = '';

  constructor(
    private onMessageEvent: (data: string) => void,
    private onCompleteEvent: (data: string) => void,
    private onError: (error: Error) => void,
    private onThinkingEvent?: (data: string) => void
  ) {}

  parseChunk(chunk: string) {
    this.buffer += chunk;
    const lines = this.buffer.split('\n');
    
    // 保留最后一个不完整的行
    this.buffer = lines.pop() || '';

    for (const line of lines) {
      if (line.startsWith('event:')) {
        this.eventType = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        this.eventData = line.slice(5).trim();
      } else if (line.trim() === '') {
        // 空行表示一个完整的SSE消息
        this.processSSEMessage();
      }
    }
  }

  flush() {
    if (this.buffer.trim()) {
      // 处理剩余的缓冲区内容
      const lines = this.buffer.split('\n');
      for (const line of lines) {
        if (line.startsWith('event:')) {
          this.eventType = line.slice(6).trim();
        } else if (line.startsWith('data:')) {
          this.eventData = line.slice(5).trim();
        }
      }
      if (this.eventType && this.eventData) {
        this.processSSEMessage();
      }
    }
  }

  private processSSEMessage() {
    if (this.eventType === 'message') {
      this.onMessageEvent(this.eventData);
    } else if (this.eventType === 'thinking' && this.onThinkingEvent) {
      this.onThinkingEvent(this.eventData);
    } else if (this.eventType === 'complete') {
      this.onCompleteEvent(this.eventData);
    }
    
    // 重置
    this.eventType = '';
    this.eventData = '';
  }
}

/**
 * 调用后端 SSE 流式接口（使用 POST 请求，避免 URL 长度限制）
 */
export async function sendMessageStream(
  message: string,
  chatId: string,
  onChunk: (text: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void,
  onThinkingChunk?: (text: string) => void
): Promise<void> {
  try {
    // 使用 POST 请求，将参数放在请求体中，避免 URL 长度限制
    const url = `/api/ai/dispatch_app/chat/sse_emitter`;
    
    // 构建 URLSearchParams（application/x-www-form-urlencoded 格式）
    const params = new URLSearchParams();
    params.append('message', message);
    params.append('chatId', chatId);
    
    console.log('发起 SSE POST 请求:', url, 'message length:', message.length);

    // 使用 fetch 发起 POST 请求
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': 'text/event-stream',
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: params.toString(),
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    // 检查响应类型
    const contentType = response.headers.get('content-type');
    if (!contentType?.includes('text/event-stream')) {
      console.warn('响应类型不是 text/event-stream:', contentType);
    }

    // 创建 SSE 解析器
    const parser = new SSEParser(
      (data) => {
        // 收到 message 事件
        if (data) {
          onChunk(data);
        }
      },
      (data) => {
        // 收到 complete 事件
        console.log('收到 complete 事件:', data);
        onComplete();
      },
      onError,
      onThinkingChunk ? (data) => {
        // 收到 thinking 事件
        if (data) {
          onThinkingChunk(data);
        }
      } : undefined
    );

    // 读取流
    const reader = response.body?.getReader();
    const decoder = new TextDecoder('utf-8');

    if (!reader) {
      throw new Error('无法获取响应流');
    }

    try {
      while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
          console.log('SSE 流读取完成');
          parser.flush();
          break;
        }

        // 解码并解析数据
        const chunk = decoder.decode(value, { stream: true });
        parser.parseChunk(chunk);
      }
    } catch (readError) {
      console.error('读取 SSE 流时出错:', readError);
      onError(new Error(`读取响应流失败: ${readError instanceof Error ? readError.message : String(readError)}`));
    }

  } catch (error) {
    console.error('调用后端接口失败:', error);
    onError(error as Error);
  }
}

/**
 * 非流式调用（备用方案，使用 POST 请求）
 */
export async function sendMessage(
  message: string,
  chatId: string
): Promise<{ content: string; thinkingTime: number }> {
  const startTime = Date.now();

  try {
    // 使用 POST 请求，将参数放在请求体中
    const url = `/api/ai/dispatch_app/chat/sync`;
    
    // 构建 URLSearchParams（application/x-www-form-urlencoded 格式）
    const params = new URLSearchParams();
    params.append('message', message);
    params.append('chatId', chatId);
    
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: params.toString(),
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const content = await response.text();
    const thinkingTime = Date.now() - startTime;

    return {
      content,
      thinkingTime,
    };
  } catch (error) {
    console.error('调用后端同步接口失败:', error);
    throw error;
  }
}

/**
 * 获取当前配置信息（用于调试）
 */
export function getConfigInfo() {
  return {
    provider: 'backend',
    baseURL: '/api/ai',
    endpoint: 'dispatch_app/chat/sse_emitter',
  };
}
