<template>
  <div class="chat-assistant">
    <n-layout has-sider class="chat-layout">
      <n-layout-sider
        bordered
        collapse-mode="width"
        :collapsed-width="0"
        :width="280"
        show-trigger="arrow-circle"
        class="history-sider"
      >
        <HistoryPanel 
          :conversations="historyList"
          :current-chat-id="currentChatId"
          @new-chat="createNewSession"
          @select-chat="loadConversation"
          @delete-chat="deleteConversation"
        />
      </n-layout-sider>

      <n-layout class="chat-main">
        <div class="chat-content">
          <ChatWindow :messages="messages" :show-thinking="!isViewingHistory" />
          <InputArea @send="handleSend" />
        </div>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import HistoryPanel from './components/HistoryPanel.vue';
import ChatWindow from './components/ChatWindow.vue';
import InputArea from './components/InputArea.vue';
import { sendMessageStream, checkApiConfig, generateChatId } from '@/api/assistant/chat';
import { formatStreamChunk, createFormatContext } from '@/utils/streamFormatter';
import { 
  getAllChatMetadata, 
  saveChatMetadata, 
  removeChatMetadata, 
  syncChatHistoryList,
  type ChatMetadata 
} from '@/api/assistant/history';

// 当前会话ID
const currentChatId = ref(generateChatId());

// 对话消息（初始化时为空数组，不再是硬编码的演示消息）
const messages = ref<Array<{id: number, role: 'user' | 'assistant', text: string, thinkingContent?: string, thinkingTime?: number, isTyping?: boolean, displayText?: string}>>([]);

// 是否在查看历史对话
const isViewingHistory = ref(false);

// 仅历史对话使用：清理消息中的 think/思考 内容，保留最终答案
function sanitizeAssistantHistoryContent(content: string): string {
  if (!content) return '';
  let t = content;
  // 移除常见的思考标签块
  t = t.replace(/<think>[\s\S]*?<\/think>/gi, '');
  t = t.replace(/\[think\][\s\S]*?\[\/think\]/gi, '');
  t = t.replace(/```\s*think[\s\S]*?```/gi, '');
  // 移除明显的思考/推理段落行
  const lines = t.split('\n').filter(l => !/^(\s*(思考|思考过程|推理过程|反思|think|reasoning|thought)\s*[:：])/i.test(l.trim()));
  t = lines.join('\n');
  // 如果存在“最终答案/Final Answer/Answer”等标记，仅保留其后的内容
  const finalMarkers = /(最终答案|最终回答|答案|回答|final\s*answer|final|answer)\s*[:：]/i;
  const match = t.match(finalMarkers);
  if (match) {
    const idx = t.toLowerCase().indexOf(match[0].toLowerCase());
    if (idx !== -1) {
      t = t.slice(idx + match[0].length).trim();
    }
  }
  // 兜底：去除过多空行
  t = t.replace(/\n{3,}/g, '\n\n').trim();
  return t;
}

// 历史对话列表
const historyList = ref<ChatMetadata[]>([]);

// 初始化：加载历史对话列表
onMounted(async () => {
  // 从 localStorage 加载
  const localHistory = getAllChatMetadata();
  
  // 如果本地有历史，先显示本地
  if (localHistory.length > 0) {
    historyList.value = localHistory;
  }
  
  // 同步后端数据
  const backendHistory = await syncChatHistoryList();
  if (backendHistory.length > 0) {
    historyList.value = backendHistory;
  }
});

// 新建会话
function createNewSession() {
  currentChatId.value = generateChatId();
  messages.value = [];
  isViewingHistory.value = false;
  console.log('创建新会话:', currentChatId.value);
}

// 检查API配置（启动时）
const configCheck = checkApiConfig();
if (!configCheck.valid) {
  console.error('API配置错误:', configCheck.error);
}

// 加载历史对话
async function loadConversation(chatId: string) {
  currentChatId.value = chatId;
  isViewingHistory.value = true;
  
  try {
    const response = await fetch(`/api/ai/dispatch_app/chat/history/detail?chatId=${encodeURIComponent(chatId)}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (response.ok) {
      const data = await response.json();
      console.log('后端返回的消息数据:', data);
      
      // 后端现在返回 ChatMessageDto[] 格式: {role: string, content: string}
      messages.value = data.map((msg: any, index: number) => {
        const role = msg.role === 'user' ? 'user' : 'assistant';
        return {
          id: index + 1,
          role: role as 'user' | 'assistant',
          text: role === 'assistant' ? sanitizeAssistantHistoryContent(msg.content || '') : (msg.content || ''),
          isTyping: false,
          displayText: msg.content || ''
        };
      });
      console.log('加载历史对话成功:', chatId, messages.value.length, '条消息');
    } else {
      console.error('加载历史对话失败:', response.status);
      messages.value = [];
    }
  } catch (error) {
    console.error('加载历史对话出错:', error);
    messages.value = [];
  }
}

// 删除对话
async function deleteConversation(chatId: string) {
  try {
    // 从后端删除
    await fetch(`/api/ai/dispatch_app/chat/history/delete?chatId=${encodeURIComponent(chatId)}`, {
      method: 'DELETE',
    });
    
    // 从本地删除
    removeChatMetadata(chatId);
    
    // 更新列表
    historyList.value = historyList.value.filter(item => item.id !== chatId);
    
    // 如果删除的是当前对话，创建新对话
    if (chatId === currentChatId.value) {
      createNewSession();
    }
    
    console.log('删除对话成功:', chatId);
  } catch (error) {
    console.error('删除对话出错:', error);
  }
}

async function handleSend(text: string) {
  if (!text?.trim()) return;
  isViewingHistory.value = false;
  
  // 如果是新对话的第一条消息，生成标题
  if (messages.value.length === 0) {
    const title = text.length > 50 ? text.substring(0, 50) + '...' : text;
    const metadata: ChatMetadata = {
      id: currentChatId.value,
      title: title.replace(/\n/g, ' ').trim(),
      time: new Date().toLocaleString('zh-CN'),
      type: '数据对话',
      messageCount: 0
    };
    saveChatMetadata(metadata);
    historyList.value.unshift(metadata);
  }
  
  // 添加用户消息
  const userMessage = { 
    id: Date.now(), 
    role: 'user' as const, 
    text 
  };
  messages.value.push(userMessage);
  
  // 更新元数据中的消息数量
  const currentMetadata = historyList.value.find(item => item.id === currentChatId.value);
  if (currentMetadata) {
    currentMetadata.messageCount = messages.value.length;
    saveChatMetadata(currentMetadata);
  }
  
  // 准备助手消息
  const assistantMessage = {
    id: Date.now() + 1,
    role: 'assistant' as const,
    text: '',
    thinkingContent: '',
    thinkingTime: 0,
    isTyping: true,
    displayText: ''
  };
  messages.value.push(assistantMessage);
  
  const startTime = Date.now();
  let fullText = '';
  let fullThinkingContent = '';
  
  // 创建格式化上下文（用于前端格式前缀识别）
  const formatContext = createFormatContext();
  
  try {
    // 调用后端流式API（只发送当前消息，后端会维护会话历史）
    await sendMessageStream(
      text, // 当前用户消息
      currentChatId.value, // 会话ID
      // onChunk - 接收到回答内容片段
      (chunk: string) => {
        // 直接使用后端已格式化的chunk（后端已处理格式前缀）
        fullText += chunk;
        const msgIndex = messages.value.findIndex(m => m.id === assistantMessage.id);
        if (msgIndex !== -1) {
          messages.value[msgIndex].text = fullText;
          messages.value[msgIndex].displayText = fullText;
        }
      },
      // onComplete - 完成
      () => {
        const msgIndex = messages.value.findIndex(m => m.id === assistantMessage.id);
        if (msgIndex !== -1) {
          messages.value[msgIndex].isTyping = false;
          messages.value[msgIndex].thinkingTime = Date.now() - startTime;
        }
        console.log('对话完成，用时:', Date.now() - startTime, 'ms');
      },
      // onError - 错误处理
      (error: Error) => {
        console.error('发送消息失败:', error);
        // 可以在这里添加错误提示
        // useMessage()?.error(`发送失败: ${error.message}`);
        
        // 移除失败的助手消息
        const msgIndex = messages.value.findIndex(m => m.id === assistantMessage.id);
        if (msgIndex !== -1) {
          messages.value.splice(msgIndex, 1);
        }
      },
      // onThinkingChunk - 接收到思考内容片段（后端不支持思考过程，留空）
      (chunk: string) => {
        fullThinkingContent += chunk;
        const msgIndex = messages.value.findIndex(m => m.id === assistantMessage.id);
        if (msgIndex !== -1) {
          messages.value[msgIndex].thinkingContent = fullThinkingContent;
        }
      }
    );
  } catch (error) {
    console.error('调用 sendMessageStream 时出错:', error);
    // 移除失败的助手消息
    const msgIndex = messages.value.findIndex(m => m.id === assistantMessage.id);
    if (msgIndex !== -1) {
      messages.value.splice(msgIndex, 1);
    }
  }
}
</script>

<style scoped>
.chat-assistant { 
  width: 100%; 
  height: calc(100vh - 156px); 
  padding: 0; 
  background: #ffffff; 
  color: #333333;
}
.chat-layout { 
  height: 100%; 
  border-radius: 0; 
  overflow: hidden; 
  box-shadow: 0 2px 8px rgba(102, 194, 255, 0.1);
  background: #ffffff;
}
.history-sider { 
  background: linear-gradient(180deg, #f5fbff 0%, #e6f7ff 100%); 
  border-right: 1px solid #e6f7ff;
}
.chat-main { 
  display: flex; 
  flex-direction: column; 
  background: #ffffff; 
}
.chat-content { 
  display: flex; 
  flex-direction: column; 
  height: 100%; 
}
</style>