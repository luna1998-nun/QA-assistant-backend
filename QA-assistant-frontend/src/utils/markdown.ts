import MarkdownIt from 'markdown-it';

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: false,        // 禁止原始 HTML 输入，防止 XSS 攻击
  linkify: true,      // 自动识别链接
  typographer: true,  // 优化排版（引号、破折号等）
  breaks: true,       // 支持软换行
  xhtmlOut: false,    // 输出 HTML 而不是 XHTML
});

// 自定义链接渲染，过滤危险协议
const originalLinkOpen = md.renderer.rules.link_open || function(tokens, idx, options, env, renderer) {
  return renderer.renderToken(tokens, idx, options);
};

md.renderer.rules.link_open = function(tokens, idx, options, env, renderer) {
  const token = tokens[idx];
  const href = token.attrGet('href');
  
  // 过滤危险的链接协议
  if (href && (href.startsWith('javascript:') || href.startsWith('data:') || href.startsWith('vbscript:'))) {
    // 将危险链接转换为纯文本
    token.tag = 'span';
    token.attrSet('href', '');
    token.attrSet('style', 'color: #666; text-decoration: none;');
  }
  
  return originalLinkOpen(tokens, idx, options, env, renderer);
};

// 自定义图片渲染，添加安全属性
const originalImage = md.renderer.rules.image || function(tokens, idx, options, env, renderer) {
  return renderer.renderToken(tokens, idx, options);
};

md.renderer.rules.image = function(tokens, idx, options, env, renderer) {
  const token = tokens[idx];
  
  // 添加安全属性
  token.attrSet('loading', 'lazy');
  token.attrSet('referrerpolicy', 'no-referrer');
  
  return originalImage(tokens, idx, options, env, renderer);
};

/**
 * 渲染 Markdown 文本为 HTML
 * @param text Markdown 文本
 * @returns 渲染后的 HTML 字符串
 */
export function renderMarkdown(text: string): string {
  if (!text) return '';
  
  try {
    // 使用 markdown-it 渲染
    const html = md.render(text);
    
    // 额外的安全清理
    return html
      .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '') // 移除 script 标签
      .replace(/on\w+="[^"]*"/gi, '') // 移除事件处理器
      .replace(/javascript:/gi, '') // 移除 javascript: 协议
      .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, ''); // 移除 iframe 标签
  } catch (error) {
    console.warn('Markdown 渲染失败:', error);
    // 如果渲染失败，返回转义的纯文本
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
}

/**
 * 简约渲染：只进行最基本的文本处理，保持简洁
 * @param text 待渲染的文本
 * @returns 渲染后的 HTML 字符串
 */
export function renderMarkdownSmart(text: string): string {
  if (!text) return '';
  
  // 只进行最基本的文本处理，保持简约
  return renderPlainText(text);
}

/**
 * 检查文本是否包含明显的 Markdown 语法
 * @param text 待检查的文本
 * @returns 是否包含明显的 Markdown 语法
 */
export function hasMarkdownSyntax(text: string): boolean {
  if (!text) return false;
  
  // 更保守的 Markdown 检测，只检测明确的格式化需求
  const strongMarkdownPatterns = [
    /^#{1,6}\s+/m,           // 标题 (# ## ###) - 明确的标题格式
    /\*\*.*?\*\*/,           // 粗体 (**text**) - 明确的粗体格式
    /`.*?`/,                 // 行内代码 (`code`) - 明确的代码格式
    /```[\s\S]*?```/,        // 代码块 (```code```) - 明确的代码块
    /^\s*>\s+/m,             // 引用 (> text) - 明确的引用格式
    /^\s*---+\s*$/m,         // 分隔线 (---) - 明确的分隔线
    /^\s*\|.*\|.*\|/m,       // 表格 (| col1 | col2 |) - 明确的表格格式
    /\[.*?\]\(.*?\)/,        // 链接 [text](url) - 明确的链接格式
    /!\[.*?\]\(.*?\)/,       // 图片 ![alt](url) - 明确的图片格式
  ];
  
  // 检查是否包含明确的 Markdown 格式
  const hasStrongMarkdown = strongMarkdownPatterns.some(pattern => pattern.test(text));
  
  if (hasStrongMarkdown) {
    return true;
  }
  
  // 对于列表，进行更严格的检查
  const lines = text.trim().split('\n').filter(line => line.trim() !== '');
  
  // 检查是否包含无序列表
  const hasUnorderedList = lines.some(line => /^\s*[-*+]\s+/.test(line));
  
  // 检查是否包含有序列表
  const hasOrderedList = lines.some(line => /^\s*\d+\.\s+/.test(line));
  
  // 只有在以下情况下才认为是 Markdown：
  // 1. 包含明确的格式化元素（标题、粗体、代码等）
  // 2. 或者列表项包含复杂内容（不仅仅是简单文本）
  if (hasUnorderedList || hasOrderedList) {
    // 检查列表项是否包含复杂内容
    const listLines = lines.filter(line => 
      /^\s*[-*+]\s+/.test(line) || /^\s*\d+\.\s+/.test(line)
    );
    
    const hasComplexListItems = listLines.some(line => {
      const content = line.replace(/^\s*[-*+\d.]+\s+/, '').trim();
      // 如果列表项内容包含特殊字符或很长，认为是复杂内容
      return content.length > 50 || 
             /[#*`_\[\](){}]/.test(content) ||
             content.includes('**') ||
             content.includes('__');
    });
    
    // 如果列表项很简单且数量不多，不使用 Markdown 渲染
    if (!hasComplexListItems && listLines.length <= 5) {
      return false;
    }
  }
  
  // 检查斜体（更保守的检测）
  const hasItalic = /\*[^*\n]+\*/.test(text) && !/\*\*.*?\*\*/.test(text);
  
  return hasUnorderedList || hasOrderedList || hasItalic;
}

/**
 * 渲染纯文本（简约模式）
 * @param text 纯文本
 * @returns 安全的 HTML 字符串
 */
export function renderPlainText(text: string): string {
  if (!text) return '';
  
  // 最基本的文本处理：只保留换行和基本转义
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
    .replace(/\n/g, '<br>'); // 保留换行
}
