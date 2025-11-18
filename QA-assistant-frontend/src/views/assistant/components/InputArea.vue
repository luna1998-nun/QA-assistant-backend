<template>
  <div class="input-area">
    <div class="input-wrapper">
      <div class="input-container">
        <n-input
          v-model:value="inputText"
          type="textarea"
          size="large"
          :placeholder="isRecording ? '正在聆听中...' : '发消息、输入@选择技能或/选择文件'"
          :autosize="{ minRows: 1, maxRows: 5 }"
          class="chat-input"
          @keyup.enter.exact="send"
          @keyup.ctrl.enter="send"
        />
        <div class="toolbar">
          <div class="toolbar-left">
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-button text><n-icon size="20"><AttachOutline /></n-icon></n-button>
              </template>
              添加附件
            </n-tooltip>
            <n-space align="center">
              <n-switch size="small" />
              <span>深度思考</span>
            </n-space>
          </div>
          <div class="toolbar-right">
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-button text><n-icon size="20"><ScissorOutlined /></n-icon></n-button>
              </template>
              截图
            </n-tooltip>
            
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-button text @click="handleMicClick">
                  <n-icon size="20" :color="isRecording ? '#ff4d4f' : undefined">
                    <MicOutline />
                  </n-icon>
                </n-button>
              </template>
              {{ isRecording ? '点击停止录音' : '语音输入' }}
            </n-tooltip>
            </div>
        </div>
      </div>
      <n-button type="primary" size="large" class="send-button" @click="send">
        发送
        <template #icon>
          <n-icon><SendOutline /></n-icon>
        </template>
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'; // 引入 watch
import { MicOutline, AttachOutline, SendOutline } from '@vicons/ionicons5';
import { ScissorOutlined } from '@vicons/antd';
import { useFunASR } from '@/hooks/useFunASR'; // 引入刚才创建的 Hook

const emit = defineEmits<{ (e: 'send', text: string): void }>();
const inputText = ref('');

// 使用 FunASR Hook
const { isRecording, resultText, startRecording, stopRecording } = useFunASR();

// 处理麦克风点击
function handleMicClick() {
  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
}

// 监听识别结果，追加到输入框
// 注意：FunASR 返回的是整句更新，不是增量字符，所以这里需要一点技巧
// 简单的策略是：当在录音时，用识别结果替换当前输入框的最后一部分，或者直接覆盖
// 这里演示最简单的逻辑：如果正在录音，直接把识别结果显示在输入框
// 进阶逻辑：记录录音前的文本 + 识别文本
let textBeforeRecord = '';

watch(isRecording, (newVal) => {
  if (newVal) {
    // 开始录音时，记录当前已有的文本
    textBeforeRecord = inputText.value;
  } else {
    // 停止录音时，识别结果已经合并进去了，无需操作
  }
});

watch(resultText, (newText) => {
  if (isRecording.value) {
    // 实时更新输入框：原始内容 + 正在识别的内容
    inputText.value = textBeforeRecord + (textBeforeRecord ? ' ' : '') + newText;
  }
});

function send() {
  const text = inputText.value.trim();
  if (!text) return;
  // 发送前如果正在录音，先停止
  if (isRecording.value) {
      stopRecording();
  }
  emit('send', text);
  inputText.value = '';
}
</script>
  
  <style lang="less" scoped>
  .input-area {
    padding: 16px;
    border-top: 1px solid #ffffff;
    background: #ffffff;

  }
  
  .input-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    max-width: 740px; // 与对话框边界一致 (800px + 20px padding * 2)
    margin: 0 auto;
    width: 100%;
    border: 1px solid #e6f7ff;
    border-radius: 16px;
    transition: none;
    
    &:focus-within {
      border-color: #e6f7ff;
      box-shadow: none;
      outline: none;
    }
    
    &:hover {
      border-color: #e6f7ff;
      box-shadow: none;
      outline: none;
    }
  }
  
  .input-container {
    flex-grow: 1;
    position: relative;
    background: #ffffff;
    border: none;
    border-radius: 16px;
    overflow: hidden;
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);
  }
  
  .chat-input {
    width: 100%;
    background: transparent;
    border: none;
    color: #333333;
    font-size: 14px;
    line-height: 1.5;
    resize: none;
    
    :deep(.n-input__input-el) {
      background: transparent;
      color: #333333;
      border: none !important;
      box-shadow: none !important;
      outline: none !important;
      
      &::placeholder {
        color: #999999;
      }
      
      &:focus {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
    }
    
    :deep(.n-input__border) {
      display: none !important;
    }
    
    :deep(.n-input) {
      border: none !important;
      box-shadow: none !important;
      
      &:focus {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
      
      &.n-input--focus {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
      
      &:hover {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
    }
    
    :deep(.n-input__wrapper) {
      border: none !important;
      box-shadow: none !important;
      outline: none !important;
      
      &:focus {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
      
      &:hover {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
    }
    
    :deep(*) {
      &:focus {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
      
      &:hover {
        border: none !important;
        box-shadow: none !important;
        outline: none !important;
      }
    }
    
    :deep(.n-input__state-border) {
      display: none !important;
    }
    
    :deep(.n-input__border) {
      display: none !important;
    }
    
    :deep(.n-input__state-border--focus) {
      display: none !important;
    }
    
    :deep(.n-input__state-border--hover) {
      display: none !important;
    }
  }
  
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    color: #666666;
    font-size: 12px;
    background: #ffffff;
    border-top: none; /* 完全移除分割线 */
    backdrop-filter: blur(5px);
  }
  
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    // 确保深度思考文字为深色
    span {
      color: #333333 !important;
    }
  }
  
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .send-button {
    height: 80px;
    min-width: 80px;
    background: linear-gradient(135deg, #66c2ff 0%, #0066cc 100%);
    border: none;
    border-radius: 12px;
    color: white;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(102, 194, 255, 0.3);
    
    &:hover {
      background: linear-gradient(135deg, #4db8ff 0%, #0052a3 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 194, 255, 0.4);
    }
    
    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 6px rgba(102, 194, 255, 0.3);
    }
  }
  
  :deep(.n-button) {
    &.n-button--text-type {
      color: #666666 !important;
      border-radius: 8px;
      transition: all 0.2s ease;
      font-weight: 500;
      font-size: 12px !important;
      
      &:hover {
        color: #0066cc !important;
        background: rgba(102, 194, 255, 0.1) !important;
        transform: translateY(-1px);
      }
      
      .n-button__content {
        color: inherit !important;
        font-size: 12px !important;
      }
    }
  }
  
  :deep(.n-switch) {
    &.n-switch--active {
      .n-switch__rail {
        background: linear-gradient(135deg, #66c2ff 0%, #0066cc 100%);
      }
    }
  }
  
  :deep(.n-tooltip) {
    .n-tooltip__content {
      background: rgba(255, 255, 255, 0.95);
      color: #333333;
      border: 1px solid rgba(102, 194, 255, 0.3);
      backdrop-filter: blur(10px);
      border-radius: 8px;
    }
  }
  </style>




