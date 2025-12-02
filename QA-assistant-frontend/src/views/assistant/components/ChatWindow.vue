<template>
  <div class="chat-window" ref="chatWindowRef">
    <div class="message-list" ref="messageListRef">
      <div v-for="msg in messages" :key="msg.id" class="message-item" :class="`message-${msg.role}`">
        <div class="message-avatar">
          <div class="avatar" :class="`avatar-${msg.role}`">
            <n-icon v-if="msg.role === 'assistant'" size="18">
              <RobotOutlined />
            </n-icon>
            <span v-else>我</span>
          </div>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-role">{{ msg.role === 'user' ? '用户' : 'OCEM AI助手' }}</span>

            <n-button
              v-if="msg.role === 'assistant' && !msg.isTyping"
              text
              size="tiny"
              @click="playAudio(msg)"
              style="margin-left: 8px; vertical-align: middle;"
            >
              <template #icon>
                <n-icon>
                  <LoadingOutlined v-if="msg.isAudioLoading" />
                  <SoundOutlined v-else :class="{'text-[#66c2ff]': msg.isPlaying}" />
                </n-icon>
              </template>
              {{ msg.isPlaying ? '停止' : (msg.isAudioLoading ? '生成中...' : '朗读') }}
            </n-button>

            <div v-if="showThinking && msg.thinkingTime" class="thinking-time">
              <div class="thinking-info">
                <n-icon><ClockCircleOutlined /></n-icon>
                <span>思考时间: {{ msg.thinkingTime }}ms</span>
              </div>
              <div class="thinking-progress">
                <div class="progress-bar" :style="{ width: '100%' }"></div>
              </div>
            </div>
          </div>
          <!-- 思考过程区域（仅助手消息且有思考内容时显示） -->
          <div 
            v-if="showThinking && hasThinkingContent(msg)" 
            class="thinking-section"
          >
            <div 
              class="thinking-toggle"
              @click="toggleThinking(msg.id)"
            >
              <n-icon size="14">
                <DownOutlined v-if="!expandedThinking[msg.id]" />
                <UpOutlined v-else />
              </n-icon>
              <span class="thinking-label">
                <n-icon size="12"><ClockCircleOutlined /></n-icon>
                思考过程 ({{ msg.thinkingContent.length }}字)
              </span>
            </div>
            <n-collapse-transition :show="expandedThinking[msg.id]">
              <div class="thinking-content">
                <div 
                  class="thinking-text"
                  v-html="renderMarkdown(msg.thinkingContent || '')"
                ></div>
              </div>
            </n-collapse-transition>
          </div>
          
          <div class="message-bubble" :class="`bubble-${msg.role}`">
            <div 
              v-if="msg.role === 'assistant' && msg.isTyping" 
              class="typing-content"
              v-html="renderMarkdown(msg.displayText || '')"
            ></div>
            <div 
              v-else 
              class="message-text"
              v-html="renderMarkdown(msg.text)"
              @click="handleTextClick"
            ></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 图表弹窗 -->
  <ChartModal ref="chartModalRef" />
</template>

<script setup lang="ts">
import { onUpdated, ref, nextTick, watch } from 'vue';
import { ClockCircleOutlined, RobotOutlined, DownOutlined, UpOutlined, SoundOutlined, LoadingOutlined } from '@vicons/antd';
import ChartModal from './ChartModal.vue';
import { renderMarkdown } from '@/utils/markdown';
import { useMessage } from 'naive-ui';

interface Msg {
  id: number;
  role: 'user' | 'assistant';
  text: string;
  thinkingContent?: string; // 思考过程内容
  displayText?: string;
  isTyping?: boolean;
  thinkingTime?: number;
  isAudioLoading?: boolean; // 新增
  isPlaying?: boolean;      // 新增
}

const props = defineProps<{ messages: any, showThinking?: boolean }>();

const chatWindowRef = ref();
const messageListRef = ref();
const chartModalRef = ref();
const $message = useMessage();

// 思考过程展开状态管理 (默认折叠)
const expandedThinking = ref<Record<number, boolean>>({});

// 切换思考过程展开状态
function toggleThinking(messageId: number) {
  expandedThinking.value[messageId] = !expandedThinking.value[messageId];
}

// 检查是否有思考内容
function hasThinkingContent(msg: Msg): boolean {
  return !!(msg.role === 'assistant' && msg.thinkingContent && msg.thinkingContent.trim().length > 0);
}

// TTS 播放逻辑
const audioPlayer = new Audio();
const currentPlayingMsgId = ref<number | null>(null);

const playAudio = (msg: Msg) => {
  // 1. 停止当前播放
  if (currentPlayingMsgId.value === msg.id) {
    audioPlayer.pause();
    audioPlayer.currentTime = 0;
    currentPlayingMsgId.value = null;
    msg.isPlaying = false;
    return;
  }

  // 停止其他正在播放的
  audioPlayer.pause();
  // 重置所有消息的播放状态 (如果需要更严谨的状态管理，可以遍历重置)
  if (currentPlayingMsgId.value !== null) {
     // 简单处理：实际项目中可能需要遍历 messages 重置 isPlaying
  }

  // 2. 开始新请求
  msg.isAudioLoading = true;

  // 清理 Markdown 符号，避免朗读干扰
  const cleanText = msg.text.replace(/[#*`\-]/g, '');

  // 这里的 URL 会被 vite.config.ts 代理到 localhost:8001
  const url = `/api/ai/tts/speak?text=${encodeURIComponent(cleanText)}`;

  audioPlayer.src = url;

  audioPlayer.oncanplay = () => {
    msg.isAudioLoading = false;
    msg.isPlaying = true;
    currentPlayingMsgId.value = msg.id;
    audioPlayer.play();
  };

  audioPlayer.onended = () => {
    msg.isPlaying = false;
    currentPlayingMsgId.value = null;
  };

  audioPlayer.onerror = () => {
    msg.isAudioLoading = false;
    msg.isPlaying = false;
    currentPlayingMsgId.value = null;
    console.error('Audio Error:', audioPlayer.error);
    $message.error('语音生成失败，请检查 Python 服务是否启动');
  };
};

// 组件销毁时清理
import { onUnmounted } from 'vue';
onUnmounted(() => {
  audioPlayer.pause();
  audioPlayer.src = '';
});

// 打字机效果
async function typewriterEffect(message: Msg) {
  if (message.role !== 'assistant' || !message.isTyping) return;
  
  const text = message.text;
  let currentText = '';
  const speed = 30; // 打字速度（毫秒）
  
  for (let i = 0; i <= text.length; i++) {
    currentText = text.slice(0, i);
    // 更新显示文本
    if (props.messages.value) {
      const msgIndex = props.messages.value.findIndex((m: Msg) => m.id === message.id);
      if (msgIndex !== -1) {
        props.messages.value[msgIndex].displayText = currentText;
      }
    }
    
    // 自动滚动到底部
    await nextTick();
    scrollToBottom();
    
    if (i < text.length) {
      await new Promise(resolve => setTimeout(resolve, speed));
    }
  }
  
  // 打字完成
  if (props.messages.value) {
    const msgIndex = props.messages.value.findIndex((m: Msg) => m.id === message.id);
    if (msgIndex !== -1) {
      props.messages.value[msgIndex].isTyping = false;
      props.messages.value[msgIndex].displayText = text;
    }
  }
}

// 滚动到底部
function scrollToBottom() {
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight;
  }
}

// 监听消息变化，启动打字机效果
watch(() => props.messages, (newMessages) => {
  if (newMessages && newMessages.value) {
    const lastMessage = newMessages.value[newMessages.value.length - 1];
    if (lastMessage && lastMessage.role === 'assistant' && lastMessage.isTyping) {
      typewriterEffect(lastMessage);
    }
  }
}, { deep: true });

// 处理文本点击事件
function handleTextClick(event: MouseEvent) {
  const target = event.target as HTMLElement;
  
  // 检查是否点击了可点击的元素
  if (target.tagName === 'EM' || target.closest('em')) {
    const text = target.textContent || '';
    
    // 根据文本内容决定显示哪种图表
    if (text.includes('图表') || text.includes('可视化')) {
      chartModalRef.value?.show('数据可视化分析');
    } else if (text.includes('代码') || text.includes('实现')) {
      chartModalRef.value?.show('代码实现详情');
    } else {
      chartModalRef.value?.show('详细分析报告');
    }
  }
}

onUpdated(() => {
  scrollToBottom();
});
</script>

<style scoped lang="less">
.chat-window { 
  flex-grow: 1; 
  background: #ffffff; 
  overflow-y: auto;
  padding: 20px;
  color: #333333;
}

.message-list {
  max-width: 800px;
  margin: 0 auto;
}

.message-item { 
  display: flex; 
  margin-bottom: 24px;
  gap: 12px;
  
  &.message-user { 
    flex-direction: row-reverse;
    .message-content {
      align-items: flex-end;
    }
  }
  
  &.message-assistant {
    .message-content {
      align-items: flex-start;
    }
  }
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  
  &.avatar-user {
    background: linear-gradient(135deg, #99d6ff 0%, #66c2ff 100%);
    color: white;
  }
  
  &.avatar-assistant {
    background: linear-gradient(135deg, #66c2ff 0%, #0066cc 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #666666;
}

.message-role {
  font-weight: 500;
}

.thinking-time {
  margin-top: 4px;
}

.thinking-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999999;
  font-size: 11px;
  margin-bottom: 4px;
}

.thinking-progress {
  width: 100%;
  height: 2px;
  background: rgba(102, 194, 255, 0.2);
  border-radius: 1px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #66c2ff 0%, #0066cc 100%);
  border-radius: 1px;
  animation: progressAnimation 2s ease-in-out infinite;
}

@keyframes progressAnimation {
  0% { transform: translateX(-100%); }
  50% { transform: translateX(0%); }
  100% { transform: translateX(100%); }
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(102, 194, 255, 0.1);
  
  &.bubble-user {
    background: #f8f9fa;
    color: #333333;
    border-bottom-right-radius: 6px;
    border-right: 3px solid #99d6ff;
    box-shadow: 0 4px 12px rgba(153, 214, 255, 0.2);
  }
  
  &.bubble-assistant {
    background: #e6f7ff;
    color: #333333;
    border: 1px solid #b3e0ff;
    border-bottom-left-radius: 6px;
    border-left: 3px solid #66c2ff;
    box-shadow: 0 4px 12px rgba(102, 194, 255, 0.15);
  }
}

.message-text, .typing-content {
  font-size: 14px;
  
  :deep(pre) {
    background: #f0f8ff;
    border: 1px solid #b3e0ff;
    border-radius: 8px;
    padding: 12px;
    margin: 8px 0;
    overflow-x: auto;
    backdrop-filter: blur(5px);
  }
  
  :deep(code) {
    background: rgba(102, 194, 255, 0.2);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    color: #0066cc;
  }
  
  :deep(blockquote) {
    border-left: 4px solid #66c2ff;
    padding-left: 12px;
    margin: 8px 0;
    color: #333333;
    background: rgba(102, 194, 255, 0.1);
    border-radius: 0 6px 6px 0;
  }
  :deep(hr) {
    border: none;
    height: 1px;
    background: linear-gradient(90deg, transparent, #66c2ff, transparent);
    margin: 16px 0;
  }
  :deep(strong) {
    font-weight: 600;
    color: #333333;
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 12px 0 8px 0;
    font-size: 15px;
  }
  :deep(li) {
    margin: 6px 0;
    padding-left: 4px;
    position: relative;
    
    &::before {
      content: '•';
      color: #66c2ff;
      font-weight: bold;
      position: absolute;
      left: -12px;
    }
  }
  :deep(em) {
    font-style: italic;
    color: #666666;
    cursor: pointer;
    text-decoration: underline;
    text-decoration-color: #66c2ff;
    transition: color 0.2s ease;
    
    &:hover {
      color: #0066cc;
    }
  }
  :deep(ul), :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }
  :deep(p) {
    margin: 0 !important;
    padding: 0 !important;
    
    &:first-child {
      margin-top: 0 !important;
      padding-top: 0 !important;
    }
    
    &:last-child {
      margin-bottom: 0 !important;
      padding-bottom: 0 !important;
    }
  }
  .message-text {
    margin: 0 !important;
    padding: 0 !important;
    
    > *:first-child {
      margin-top: 0 !important;
      padding-top: 0 !important;
    }
    
    > *:last-child {
      margin-bottom: 0 !important;
      padding-bottom: 0 !important;
    }
  }
}

.typing-content {
  position: relative;
  
  &::after {
    content: '|';
    animation: blink 1s infinite;
    color: #66c2ff;
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.thinking-section {
  margin-bottom: 8px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #e5e5e5;
  overflow: hidden;
}

.thinking-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  background: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;
  transition: all 0.2s ease;
  font-size: 12px;
  color: #666666;
  user-select: none;
  
  &:hover {
    background: #e9e9e9;
  }
  
  .n-icon {
    transition: transform 0.2s ease;
  }
}

.thinking-label {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999999;
}

.thinking-content {
  padding: 12px;
  background: #ffffff;
}

.thinking-text {
  font-size: 13px;
  line-height: 1.6;
  color: #666666;
  font-style: italic;
  white-space: pre-wrap;
  word-wrap: break-word;
  :deep(p) {
    font-style: italic;
    color: #777777;
  }
  
  :deep(strong) {
    font-weight: 600;
    color: #333333;
    font-style: normal;
  }
  
  :deep(code) {
    background: rgba(102, 194, 255, 0.1);
    padding: 2px 4px;
    border-radius: 3px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    color: #0066cc;
    font-style: normal;
  }
  
  :deep(ul), :deep(ol) {
    padding-left: 16px;
    margin: 4px 0;
  }
  
  :deep(li) {
    margin: 2px 0;
    font-style: italic;
  }
}
</style>