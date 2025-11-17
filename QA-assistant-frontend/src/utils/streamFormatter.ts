/**
 * 前端流式格式化器（逐字实时格式化）
 * 识别格式前缀并自动创建新行
 */

interface FormatContext {
  lastChars: string; // 用于检测模式的前几个字符（最后20个字符）
}

/**
 * 处理新的chunk，逐字实时格式化
 */
export function formatStreamChunk(chunk: string, context: FormatContext): string {
  if (!chunk || chunk.length === 0) {
    return '';
  }

  const formatted: string[] = [];
  
  // 更新上下文窗口
  context.lastChars += chunk;
  if (context.lastChars.length > 20) {
    context.lastChars = context.lastChars.slice(-20);
  }

  // 逐字符处理
  for (let i = 0; i < chunk.length; i++) {
    const c = chunk[i];
    const contextLen = context.lastChars.length - chunk.length + i;
    const currentContext = contextLen > 0 
      ? context.lastChars.slice(Math.max(0, contextLen - 15)) 
      : '';

    // 检测序号前缀（如 "1." 或 "2、"）
    if (isNumberedPrefix(currentContext, c)) {
      formatted.push('\n');
    }

    // 检测标题前缀
    if (isTitlePrefix(currentContext, c)) {
      formatted.push('\n');
    }

    formatted.push(c);
  }

  return formatted.join('');
}

/**
 * 检测序号前缀（如 "1." 或 "2、"）
 */
function isNumberedPrefix(context: string, currentChar: string): boolean {
  // 检测模式：数字 + "." 或 "、"
  if (currentChar === '.' || currentChar === '、') {
    if (context.length >= 1) {
      const prevChar = context[context.length - 1];
      if (/\d/.test(prevChar)) {
        // 检查更前面的字符，如果不是数字，说明是新序号
        if (context.length === 1) {
          return true; // 开头就是 "1." 或 "1、"
        }
        const prevPrevChar = context[context.length - 2];
        if (!/\d/.test(prevPrevChar)) {
          return true;
        }
      }
    }
  }
  
  return false;
}

/**
 * 检测标题前缀
 */
function isTitlePrefix(context: string, currentChar: string): boolean {
  // 检测特定关键字开头
  // 由于是逐字处理，在关键字第一个字符前添加换行

  // 检测 "昨日处理主要工作" - 在"昨"字前添加换行
  if (currentChar === '昨' && (context.length === 0 || context.endsWith('\n') || 
      !context.includes('昨日处理主要工作'))) {
    return true;
  }

  // 检测 "今日关注工作" - 在"今"字前添加换行
  if (currentChar === '今' && (context.length === 0 || context.endsWith('\n') || 
      !context.includes('今日关注工作'))) {
    return true;
  }

  // 检测 "交接班总结" - 在"交"字前添加换行
  if (currentChar === '交' && (context.length === 0 || context.endsWith('\n') || 
      !context.includes('交接班总结'))) {
    return true;
  }

  return false;
}

/**
 * 创建新的格式化上下文
 */
export function createFormatContext(): FormatContext {
  return {
    lastChars: ''
  };
}

/**
 * 重置格式化上下文
 */
export function resetFormatContext(context: FormatContext): void {
  context.lastChars = '';
}

