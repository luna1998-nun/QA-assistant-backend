
  <template>
  <div class="history-panel">
    <div class="header">
      <div class="logo">OCEM AI助手</div>
      <n-button type="primary" block class="new-chat-btn" @click="handleNewChat">
        <template #icon>
          <n-icon><PlusOutlined /></n-icon>
        </template>
        开启新对话
      </n-button>
    </div>
    
    <!-- 搜索框 -->
    <div class="search-section">
      <n-input
        v-model:value="searchQuery"
        placeholder="搜索历史对话"
        clearable
        class="search-input"
      >
        <template #prefix>
          <n-icon><SearchOutlined /></n-icon>
        </template>
      </n-input>
      <div class="filter-tabs">
        <n-button 
          v-for="filter in filters" 
          :key="filter.key"
          :type="activeFilter === filter.key ? 'primary' : 'default'"
          size="small"
          class="filter-button"
          @click="setFilter(filter.key)"
        >
          {{ filter.label }}
        </n-button>
      </div>
    </div>
    
    <div class="history-section">
      <div class="section-title">{{ getSectionTitle() }}</div>
      <div class="conversation-list">
        <div 
          v-for="item in filteredConversations" 
          :key="item.id"
          class="conversation-card"
          :class="{ active: item.id === currentChatId }"
          @mouseenter="hoveredId = item.id"
          @mouseleave="hoveredId = null"
          @click="selectConversation(item.id)"
        >
          <div class="card-content">
            <div class="conversation-title">{{ item.title }}</div>
            <div class="conversation-meta">
              <span class="conversation-type">{{ item.type }}</span>
              <span class="conversation-time">{{ item.time }}</span>
            </div>
          </div>
          <div class="delete-btn" v-if="hoveredId === item.id" @click.stop="deleteConversation($event, item.id)">
            <n-icon><DeleteOutlined /></n-icon>
          </div>
        </div>
      </div>
    </div>
    
    <div class="user-info">
      <div class="user-avatar">Q</div>
      <div class="user-details">
        <div class="user-email">281*****63@qq.com</div>
      </div>
      <n-button text class="settings-btn">
        <n-icon><EllipsisOutlined /></n-icon>
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { PlusOutlined, EllipsisOutlined, SearchOutlined, DeleteOutlined } from '@vicons/antd';
import type { ChatMetadata } from '@/api/assistant/history';

interface Props {
  conversations: ChatMetadata[];
  currentChatId: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{ (e: 'new-chat'): void; (e: 'select-chat', chatId: string): void; (e: 'delete-chat', chatId: string): void }>();

const searchQuery = ref('');
const activeFilter = ref('all');
const hoveredId = ref<string | null>(null);

const filters = [
  { key: 'all', label: '全部' },
  { key: 'today', label: '今天' },
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' }
];

const filteredConversations = computed(() => {
  let filtered = props.conversations;
  
  // 按搜索关键词过滤
  if (searchQuery.value) {
    filtered = filtered.filter(item => 
      item.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.type.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }
  
  // 按时间过滤
  if (activeFilter.value !== 'all') {
    const now = new Date();
    filtered = filtered.filter(item => {
      const itemDate = new Date(item.time);
      switch (activeFilter.value) {
        case 'today':
          return itemDate.toDateString() === now.toDateString();
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
          return itemDate >= weekAgo;
        case 'month':
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
          return itemDate >= monthAgo;
        default:
          return true;
      }
    });
  }
  
  return filtered;
});

function selectConversation(chatId: string) {
  emit('select-chat', chatId);
}

function deleteConversation(event: MouseEvent, chatId: string) {
  event.stopPropagation(); // 阻止事件冒泡
  if (confirm('确定要删除这个对话吗？')) {
    emit('delete-chat', chatId);
  }
}

function setFilter(filterKey: string) {
  activeFilter.value = filterKey;
}

function getSectionTitle() {
  const count = filteredConversations.value.length;
  if (activeFilter.value === 'all') {
    return `全部对话 (${count})`;
  }
  return `${filters.find(f => f.key === activeFilter.value)?.label} (${count})`;
}

function handleNewChat() {
  emit('new-chat');
}
</script>

<style scoped>
.history-panel { 
  height: 100%; 
  display: flex; 
  flex-direction: column; 
  background: linear-gradient(180deg, #f5fbff 0%, #e6f7ff 100%); 
  color: #333333;
}
.header {
  padding: 20px 16px;
  border-bottom: 1px solid #e6f7ff;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}
.logo {
  font-size: 18px;
  font-weight: 600;
  color: #0066cc;
  margin-bottom: 16px;
  font-family: 'Inter', 'Roboto', '思源黑体', sans-serif;
}
.new-chat-btn {
  background: linear-gradient(135deg, #66c2ff 0%, #0066cc 100%);
  border: none;
  height: 40px;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 2px 8px rgba(102, 194, 255, 0.3) !important;
  
  &:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 4px 16px rgba(102, 194, 255, 0.4) !important;
    background: linear-gradient(135deg, #4db8ff 0%, #0052a3 100%) !important;
  }
  
  &:active {
    transform: translateY(0) !important;
    box-shadow: 0 2px 8px rgba(102, 194, 255, 0.3) !important;
  }
}

.search-section {
  padding: 16px;
  border-bottom: 1px solid #e6f7ff;
  background: rgba(255, 255, 255, 0.6);
}

.search-input {
  margin-bottom: 12px;
  
  :deep(.n-input__input-el) {
    background: #ffffff;
    border: 1px solid #99d6ff;
    border-radius: 8px;
    color: #333333;
    
    &::placeholder {
      color: #999999;
    }
    
    &:focus {
      border-color: #0066cc;
      box-shadow: 0 0 0 2px rgba(0, 102, 204, 0.2);
      background: #ffffff;
    }
  }
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  
  :deep(.n-button) {
    color: #333333 !important;
    border-color: #e0e0e0 !important;
    background: #ffffff !important;
    font-weight: 500 !important;
    
    &:hover {
      color: #0066cc !important;
      background: #e6f7ff !important;
      border-color: #99d6ff !important;
    }
    
    &.n-button--primary-type {
      color: #ffffff !important;
      background: linear-gradient(135deg, #66c2ff 0%, #0066cc 100%) !important;
      border-color: #66c2ff !important;
    }
    
    /* 针对不同按钮状态的强制样式 */
    &.n-button--default-type {
      color: #333333 !important;
    }
    
    .n-button__content {
      color: inherit !important;
    }
  }
}

/* 针对特定按钮class的强制样式 */
.filter-button {
  color: #333333 !important;
  
  :deep(.n-button__content) {
    color: inherit !important;
  }
  
  &:hover {
    color: #0066cc !important;
  }
}
.history-section {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  
  /* 隐藏默认滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  
  /* Webkit浏览器滚动条样式 - 默认隐藏 */
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 2px;
    transition: all 0.3s ease;
  }
  
  /* 悬停时显示滚动条 */
  &:hover::-webkit-scrollbar-thumb {
    background: rgba(102, 194, 255, 0.3);
  }
  
  /* 滚动时显示滚动条 */
  &:hover::-webkit-scrollbar-thumb:hover {
    background: rgba(102, 194, 255, 0.5);
  }
  
  /* 滚动条轨道在悬停时显示 */
  &:hover::-webkit-scrollbar-track {
    background: rgba(102, 194, 255, 0.1);
  }
}
.section-title {
  font-size: 12px;
  color: #666666;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.conversation-card {
  margin-bottom: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e6f7ff;
  background: #ffffff;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(102, 194, 255, 0.1);
}

.conversation-card:hover {
  background: #f0f8ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 194, 255, 0.2);
  border-color: #99d6ff;
}

.conversation-card.active {
  background: linear-gradient(135deg, #e6f7ff 0%, #b3e0ff 100%);
  border-color: #66c2ff;
  box-shadow: 0 4px 16px rgba(102, 194, 255, 0.3);
}

.card-content {
  padding: 16px;
}

.conversation-title {
  font-size: 14px;
  color: #333333;
  margin-bottom: 8px;
  line-height: 1.4;
  font-weight: 500;
}

.conversation-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-type {
  font-size: 11px;
  color: #0066cc;
  background: rgba(102, 194, 255, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.conversation-time {
  font-size: 11px;
  color: #666666;
  font-weight: 400;
}
.user-info {
  padding: 16px;
  border-top: 1px solid #e6f7ff;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8f9fa;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #66c2ff 0%, #0066cc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}
.user-details {
  flex: 1;
}
.user-email {
  font-size: 14px;
  color: #333333;
}
.settings-btn {
  color: #666666;
}

.conversation-card {
  position: relative;
}

.delete-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #ff4d4f;
  transition: all 0.2s ease;
  z-index: 1;
}

.delete-btn:hover {
  color: #ff7875;
  transform: translateY(-50%) scale(1.1);
}
</style>