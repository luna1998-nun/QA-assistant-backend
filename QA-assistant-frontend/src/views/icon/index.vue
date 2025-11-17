<template>
  <div class="icon-showcase">
    <!-- 页面标题和搜索 -->
    <div class="page-header">
      <h2>SVG 图标库</h2>
      <p class="page-description">点击图标查看配置选项并复制代码</p>
      
      <div class="search-section">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索图标名称..."
          clearable
          size="large"
          class="search-input"
          @input="handleSearchChange"
        >
          <template #prefix>
            <n-icon>
              <SearchOutlined />
            </n-icon>
          </template>
        </n-input>
        
        <div class="filter-tabs">
          <n-tabs v-model:value="activeTab" type="line" size="small" @update:value="handleTabChange">
            <n-tab-pane name="all" tab="全部" />
            <n-tab-pane name="frontend" tab="前端图标" />
            <n-tab-pane name="fonticon" tab="字体图标" />
            <n-tab-pane name="icon" tab="基础图标" />
            <n-tab-pane name="menu" tab="菜单图标" />
            <n-tab-pane name="file" tab="文件图标" />
            <n-tab-pane name="system" tab="系统图标" />
          </n-tabs>
        </div>
      </div>
    </div>

    <!-- 图标网格 -->
    <div class="icon-grid">
      <div 
        v-for="icon in displayedIcons" 
        :key="icon.name"
        class="icon-card"
        @click="openIconConfig(icon)"
      >
        <div class="icon-preview">
          <GiSvgIcon :name="icon.name" :size="32" />
        </div>
        <div class="icon-name">{{ icon.name }}</div>
        <div class="icon-category">{{ icon.category }}</div>
      </div>
    </div>

    <!-- 加载更多按钮 -->
    <div class="load-more-section" v-if="hasMoreIcons">
      <n-button 
        type="primary" 
        :loading="isLoading"
        @click="loadMoreIcons"
        class="load-more-btn"
      >
        {{ isLoading ? '加载中...' : '加载更多图标' }}
      </n-button>
      <div class="load-more-info">
        已显示 {{ displayedIcons.length }} / {{ filteredIcons.length }} 个图标
      </div>
    </div>

    <!-- 加载完成提示 -->
    <div class="load-complete" v-else-if="filteredIcons.length > 0">
      <n-alert type="info" show-icon>
        已显示全部 {{ filteredIcons.length }} 个图标
      </n-alert>
    </div>

    <!-- 图标配置弹窗 -->
    <n-modal
      v-model:show="showConfigModal"
      preset="card"
      style="width: 800px"
      title="图标配置"
      size="huge"
      :bordered="false"
      :segmented="false"
      class="icon-config-modal"
      :mask-closable="false"
    >
     

      <div class="config-content">
        <!-- 图标预览 -->
        <div class="preview-section">
          <div class="preview-header">
            <h4>预览效果</h4>
            <div class="preview-size-info">{{ previewSize }}px</div>
          </div>
          <div class="icon-preview-large">
            <div class="preview-background">
              <GiSvgIcon 
                :name="selectedIcon?.name" 
                :size="previewSize" 
                :color="previewColor"
              />
            </div>
          </div>
          <div class="preview-variants">
            <div class="variant-item" @click="previewSize = 16">
              <GiSvgIcon :name="selectedIcon?.name" :size="16" :color="previewColor" />
              <span>16px</span>
            </div>
            <div class="variant-item" @click="previewSize = 24">
              <GiSvgIcon :name="selectedIcon?.name" :size="24" :color="previewColor" />
              <span>24px</span>
            </div>
            <div class="variant-item" @click="previewSize = 32">
              <GiSvgIcon :name="selectedIcon?.name" :size="32" :color="previewColor" />
              <span>32px</span>
            </div>
            <div class="variant-item" @click="previewSize = 48">
              <GiSvgIcon :name="selectedIcon?.name" :size="48" :color="previewColor" />
              <span>48px</span>
            </div>
          </div>
        </div>

        <!-- 配置选项 -->
        <div class="config-section">
          <div class="config-header">
            <h4>配置选项</h4>
          </div>
          
          <div class="config-form">
            <div class="form-group">
              <label class="form-label">图标名称</label>
              <n-input 
                :value="selectedIcon?.name" 
                readonly 
                class="readonly-input"
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">尺寸大小</label>
              <div class="slider-container">
                <n-slider
                  v-model:value="previewSize"
                  :min="16"
                  :max="128"
                  :step="4"
                  :marks="{ 16: '16', 32: '32', 48: '48', 64: '64', 96: '96', 128: '128' }"
                  class="size-slider"
                />
                <div class="size-display">{{ previewSize }}px</div>
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">颜色设置</label>
              <div class="color-options">
                <div class="color-presets">
                  <div 
                    v-for="color in colorPresets" 
                    :key="color.name"
                    class="color-item"
                    :class="{ active: previewColor === color.value }"
                    :style="{ backgroundColor: color.value }"
                    @click="previewColor = color.value"
                    :title="color.name"
                  >
                    <span class="color-name">{{ color.name }}</span>
                  </div>
                </div>
                <div class="color-picker-container">
                  <n-color-picker 
                    v-model:value="previewColor" 
                    :show-alpha="false"
                    size="small"
                    class="custom-color-picker"
                  />
                  <span class="picker-label">自定义颜色</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 代码生成 -->
        <div class="code-section">
          <div class="code-header">
            <h4>生成的代码</h4>
            <div class="code-actions">
              <n-button 
                type="primary" 
                size="small"
                @click="copyCode"
                :loading="copyLoading"
                class="copy-btn"
              >
                <template #icon>
                  <n-icon>
                    <CopyOutlined />
                  </n-icon>
                </template>
                {{ copyLoading ? '复制中...' : '复制代码' }}
              </n-button>
            </div>
          </div>
          
          <div class="code-block-container">
            <n-code 
              :code="generatedCode" 
              language="vue"
              :show-line-numbers="false"
              class="code-block"
            />
          </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useThemeVars, useMessage } from 'naive-ui'
import { SearchOutlined, CopyOutlined } from '@vicons/antd'

// 主题变量
const themeVars = useThemeVars()
const message = useMessage()

// 响应式数据
const searchKeyword = ref('')
const activeTab = ref('all')
const showConfigModal = ref(false)
const selectedIcon = ref<IconItem | null>(null)
const previewSize = ref(32)
const previewColor = ref('#1890ff')
const copyLoading = ref(false)

// 分页和懒加载
const currentPage = ref(1)
const pageSize = ref(50) // 每页显示50个图标
const isLoading = ref(false)

// 图标数据
interface IconItem {
  name: string
  category: string
  displayName: string
}

const allIcons = ref<IconItem[]>([])

// 颜色预设
const colorPresets = [
  { name: '主题色', value: '#1890ff' },
  { name: '成功', value: '#52c41a' },
  { name: '警告', value: '#faad14' },
  { name: '错误', value: '#ff4d4f' },
  { name: '信息', value: '#13c2c2' },
  { name: '默认', value: '#666666' },
  { name: '黑色', value: '#000000' },
  { name: '白色', value: '#ffffff' }
]

// 生成所有图标数据
const generateIconData = () => {
  const icons: IconItem[] = []
  
  // 字体图标 (fonticon-001 到 fonticon-230)
  for (let i = 1; i <= 230; i++) {
    const num = i.toString().padStart(3, '0')
    icons.push({
      name: `fonticon-${num}`,
      category: 'fonticon',
      displayName: `字体图标 ${num}`
    })
  }
  
  // 基础图标 (icon-001 到 icon-066)
  for (let i = 1; i <= 66; i++) {
    const num = i.toString().padStart(3, '0')
    icons.push({
      name: `icon-${num}`,
      category: 'icon',
      displayName: `基础图标 ${num}`
    })
  }
  
  // 特殊图标
  const specialIcons = [
    { name: 'icon-msg', category: 'icon', displayName: '消息图标' },
    { name: 'icon-notice', category: 'icon', displayName: '通知图标' },
    { name: 'icon-num', category: 'icon', displayName: '数字图标' },
    { name: 'icon-user', category: 'icon', displayName: '用户图标' },
    { name: 'icon-wait', category: 'icon', displayName: '等待图标' },
  ]
  
  // 菜单图标
  const menuIcons = [
    'menu-about', 'menu-chart', 'menu-data', 'menu-detail', 'menu-document',
    'menu-error', 'menu-example', 'menu-file', 'menu-form', 'menu-gitee',
    'menu-home', 'menu-layout', 'menu-multi', 'menu-nav', 'menu-result',
    'menu-system', 'menu-table', 'menu-test'
  ]
  
  // 文件图标
  const fileIcons = [
    'file', 'file-close', 'file-css', 'file-dir', 'file-excel', 'file-exe',
    'file-html', 'file-image', 'file-js', 'file-json', 'file-music',
    'file-open', 'file-other', 'file-pdf', 'file-ppt', 'file-rar',
    'file-txt', 'file-video', 'file-wps', 'file-zip'
  ]
  
  // 系统图标
  const systemIcons = [
    'sys1', 'sys2', 'sys3', 'sys4', 'sys5', 'sys6', 'sys7',
    'backtop', 'time', 'upload-file', 'upload-folder'
  ]
  
  // 前端常用图标
  const frontendIcons = [
    'frontend-home', 'frontend-user', 'frontend-search', 'frontend-settings',
    'frontend-heart', 'frontend-star', 'frontend-mail', 'frontend-phone',
    'frontend-camera', 'frontend-share', 'frontend-download', 'frontend-upload',
    'frontend-edit', 'frontend-delete', 'frontend-plus', 'frontend-minus',
    'frontend-check', 'frontend-close', 'frontend-refresh', 'frontend-lock',
    'frontend-unlock', 'frontend-eye', 'frontend-eye-off', 'frontend-bell',
    'frontend-calendar', 'frontend-clock', 'frontend-tag', 'frontend-link',
    'frontend-external-link', 'frontend-menu', 'frontend-grid', 'frontend-list',
    'frontend-arrow-left', 'frontend-arrow-right', 'frontend-arrow-up', 'frontend-arrow-down'
  ]
  
  // 添加所有图标
  icons.push(...specialIcons)
  
  menuIcons.forEach(name => {
    icons.push({
      name,
      category: 'menu',
      displayName: name.replace('menu-', '菜单-')
    })
  })
  
  fileIcons.forEach(name => {
    icons.push({
      name,
      category: 'file',
      displayName: name.replace('file-', '文件-')
    })
  })
  
  systemIcons.forEach(name => {
    icons.push({
      name,
      category: 'system',
      displayName: name.replace('sys', '系统')
    })
  })
  
  frontendIcons.forEach(name => {
    icons.push({
      name,
      category: 'frontend',
      displayName: name.replace('frontend-', '前端-')
    })
  })
  
  return icons
}

// 过滤后的图标
const filteredIcons = computed(() => {
  let filtered = allIcons.value
  
  // 按分类过滤
  if (activeTab.value !== 'all') {
    filtered = filtered.filter(icon => icon.category === activeTab.value)
  }
  
  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(icon => 
      icon.name.toLowerCase().includes(keyword) ||
      icon.displayName.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})

// 分页显示的图标
const displayedIcons = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredIcons.value.slice(0, end)
})

// 是否还有更多图标
const hasMoreIcons = computed(() => {
  return displayedIcons.value.length < filteredIcons.value.length
})

// 生成的代码
const generatedCode = computed(() => {
  if (!selectedIcon.value) return ''
  
  return `<GiSvgIcon 
  name="${selectedIcon.value.name}" 
  :size="${previewSize.value}" 
  color="${previewColor.value}" 
/>`
})

// 打开图标配置
const openIconConfig = (icon: IconItem) => {
  selectedIcon.value = icon
  previewSize.value = 32
  previewColor.value = '#1890ff'
  showConfigModal.value = true
}

// 复制代码
const copyCode = async () => {
  try {
    copyLoading.value = true
    await navigator.clipboard.writeText(generatedCode.value)
    message.success('代码已复制到剪贴板')
  } catch (err) {
    message.error('复制失败，请手动复制')
  } finally {
    copyLoading.value = false
  }
}


// 加载更多图标
const loadMoreIcons = async () => {
  if (isLoading.value || !hasMoreIcons.value) return
  
  isLoading.value = true
  
  // 模拟加载延迟，实际项目中可以移除
  await new Promise(resolve => setTimeout(resolve, 100))
  
  currentPage.value += 1
  isLoading.value = false
}

// 重置分页
const resetPagination = () => {
  currentPage.value = 1
}

// 监听搜索和分类变化，重置分页
const handleSearchChange = () => {
  resetPagination()
}

const handleTabChange = () => {
  resetPagination()
}

// 初始化
onMounted(() => {
  allIcons.value = generateIconData()
})
</script>

<style scoped>
.icon-showcase {
  padding: 24px;
  background: v-bind('themeVars.cardColor');
  border-radius: 8px;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: v-bind('themeVars.textColor1');
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0 0 24px 0;
  color: v-bind('themeVars.textColor2');
  font-size: 14px;
}

.search-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-input {
  max-width: 400px;
}

.filter-tabs {
  margin-top: 8px;
}

/* 图标网格 */
.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.icon-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  border: 1px solid v-bind('themeVars.borderColor');
  border-radius: 8px;
  background: v-bind('themeVars.cardColor');
  transition: all 0.3s ease;
  cursor: pointer;
  text-align: center;
}

.icon-card:hover {
  border-color: v-bind('themeVars.primaryColor');
  box-shadow: 0 4px 12px v-bind('themeVars.boxShadow1');
  transform: translateY(-2px);
}

.icon-preview {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
}

.icon-name {
  font-size: 12px;
  color: v-bind('themeVars.textColor1');
  font-weight: 500;
  margin-bottom: 4px;
  word-break: break-all;
}

.icon-category {
  font-size: 10px;
  color: v-bind('themeVars.textColor3');
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

/* 配置弹窗 */
.icon-config-modal {
  max-width: 800px;
  width: 90vw;
  max-height: 700px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* 弹窗头部 */
.modal-header {
  background: linear-gradient(135deg, v-bind('themeVars.primaryColor') 0%, v-bind('themeVars.primaryColorHover') 100%);
  color: white;
  padding: 20px 24px;
  margin: -24px -24px 0 -24px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.icon-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-preview-small {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.icon-details {
  flex: 1;
}

.icon-title {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.icon-subtitle {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.close-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

/* 配置内容 */
.config-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 16px;
  max-height: 600px;
  overflow-y: auto;
}

/* 预览区域 */
.preview-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  border: 1px solid v-bind('themeVars.borderColor');
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.preview-header h4 {
  margin: 0;
  color: v-bind('themeVars.textColor1');
  font-size: 13px;
  font-weight: 600;
}

.preview-size-info {
  background: v-bind('themeVars.primaryColor');
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  display: inline-block;
  width: fit-content;
}

.icon-preview-large {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  margin: 0 auto;
  position: relative;
}

.preview-background {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: white;
  border: 2px solid v-bind('themeVars.borderColor');
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.preview-background:hover {
  border-color: v-bind('themeVars.primaryColor');
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.preview-variants {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.variant-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px;
  border: 1px solid v-bind('themeVars.borderColor');
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 40px;
}

.variant-item:hover {
  border-color: v-bind('themeVars.primaryColor');
  background: #f5f5f5;
  transform: translateY(-1px);
}

.variant-item span {
  font-size: 9px;
  color: v-bind('themeVars.textColor3');
  font-weight: 500;
}

/* 配置区域 */
.config-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-header h4 {
  margin: 0 0 12px 0;
  color: v-bind('themeVars.textColor1');
  font-size: 14px;
  font-weight: 600;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 12px;
  font-weight: 500;
  color: v-bind('themeVars.textColor1');
  margin-bottom: 2px;
}

.readonly-input {
  background: #f5f5f5;
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-slider {
  flex: 1;
}

.size-display {
  background: v-bind('themeVars.primaryColor');
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  min-width: 35px;
  text-align: center;
}

.color-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.color-presets {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.color-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.color-item:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.color-item.active {
  border-color: v-bind('themeVars.primaryColor');
  box-shadow: 0 0 0 2px v-bind('themeVars.primaryColorSuppl');
}

.color-name {
  font-size: 9px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  font-weight: 500;
  z-index: 1;
}

.color-picker-container {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px;
  background: #f5f5f5;
  border-radius: 4px;
}

.picker-label {
  font-size: 11px;
  color: v-bind('themeVars.textColor2');
  font-weight: 500;
}

/* 代码部分 */
.code-section {
  grid-column: 1 / -1;
  margin-top: 8px;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.code-header h4 {
  margin: 0;
  color: v-bind('themeVars.textColor1');
  font-size: 14px;
  font-weight: 600;
}

.code-actions {
  display: flex;
  gap: 6px;
}

.copy-btn {
  font-size: 12px;
  padding: 4px 8px;
}

.close-btn-bottom {
  background: v-bind('themeVars.errorColor');
  border-color: v-bind('themeVars.errorColor');
  color: white;
  font-size: 12px;
  padding: 4px 8px;
}

.close-btn-bottom:hover {
  background: v-bind('themeVars.errorColorHover');
  border-color: v-bind('themeVars.errorColorHover');
}

.code-block-container {
  background: #f5f5f5;
  border: 1px solid v-bind('themeVars.borderColor');
  border-radius: 6px;
  overflow: hidden;
}

.code-block {
  margin: 0;
  border-radius: 0;
  background: transparent;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .icon-showcase {
    padding: 16px;
  }
  
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 12px;
  }
  
  .icon-card {
    padding: 16px 8px;
  }
  
  .icon-config-modal {
    max-width: 95vw;
    margin: 20px;
  }
  
  .modal-header {
    padding: 12px 16px;
    margin: -16px -16px 0 -16px;
  }
  
  .config-content {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }
  
  .preview-variants {
    gap: 8px;
  }
  
  .variant-item {
    min-width: 40px;
    padding: 6px;
  }
  
  .color-presets {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .code-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .code-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
  
  .icon-card {
    padding: 12px 6px;
  }
  
  .icon-name {
    font-size: 10px;
  }
  
  .icon-category {
    font-size: 8px;
  }
  
  .icon-config-modal {
    max-width: 100vw;
    margin: 10px;
    border-radius: 8px;
  }
  
  .modal-header {
    padding: 10px 12px;
    margin: -12px -12px 0 -12px;
  }
  
  .icon-title {
    font-size: 14px;
  }
  
  .icon-subtitle {
    font-size: 9px;
  }
  
  .config-content {
    grid-template-columns: 1fr;
    padding: 12px;
    gap: 12px;
  }
  
  .icon-preview-large {
    width: 120px;
    height: 120px;
  }
  
  .preview-variants {
    gap: 6px;
  }
  
  .variant-item {
    min-width: 35px;
    padding: 4px;
  }
  
  .variant-item span {
    font-size: 9px;
  }
  
  .color-presets {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }
  
  .color-item {
    height: 32px;
  }
  
  .color-name {
    font-size: 9px;
  }
}

/* 加载更多区域 */
.load-more-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 32px;
  padding: 24px;
}

.load-more-btn {
  min-width: 160px;
}

.load-more-info {
  color: v-bind('themeVars.textColor3');
  font-size: 12px;
  text-align: center;
}

.load-complete {
  margin-top: 32px;
  padding: 0 24px;
}

.load-complete .n-alert {
  text-align: center;
}
</style>
