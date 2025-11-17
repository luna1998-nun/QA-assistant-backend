<template>
  <div class="virtual-icon-grid" ref="containerRef" @scroll="handleScroll">
    <div class="virtual-scroll-container" :style="{ height: totalHeight + 'px' }">
      <div 
        class="virtual-scroll-content" 
        :style="{ transform: `translateY(${offsetY}px)` }"
      >
        <div 
          v-for="icon in visibleIcons" 
          :key="icon.name"
          class="icon-card"
          @click="$emit('iconClick', icon)"
        >
          <div class="icon-preview">
            <GiSvgIcon :name="icon.name" :size="32" />
          </div>
          <div class="icon-name">{{ icon.name }}</div>
          <div class="icon-category">{{ icon.category }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface IconItem {
  name: string
  category: string
  displayName: string
}

interface Props {
  icons: IconItem[]
  itemHeight?: number
  containerHeight?: number
  overscan?: number
}

const props = withDefaults(defineProps<Props>(), {
  itemHeight: 120,
  containerHeight: 600,
  overscan: 5
})

const emit = defineEmits<{
  iconClick: [icon: IconItem]
}>()

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

// 计算可见区域
const visibleRange = computed(() => {
  const start = Math.floor(scrollTop.value / props.itemHeight)
  const end = Math.min(
    start + Math.ceil(props.containerHeight / props.itemHeight) + props.overscan,
    props.icons.length
  )
  return { start, end }
})

// 可见的图标
const visibleIcons = computed(() => {
  return props.icons.slice(visibleRange.value.start, visibleRange.value.end)
})

// 总高度
const totalHeight = computed(() => props.icons.length * props.itemHeight)

// 偏移量
const offsetY = computed(() => visibleRange.value.start * props.itemHeight)

// 滚动处理
const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
}

// 滚动到顶部
const scrollToTop = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
}

defineExpose({
  scrollToTop
})
</script>

<style scoped>
.virtual-icon-grid {
  height: 600px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.virtual-scroll-container {
  position: relative;
}

.virtual-scroll-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
  padding: 16px;
}

.icon-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--card-color);
  transition: all 0.3s ease;
  cursor: pointer;
  text-align: center;
  height: 120px;
  box-sizing: border-box;
}

.icon-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px var(--box-shadow-1);
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
  color: var(--text-color-1);
  font-weight: 500;
  margin-bottom: 4px;
  word-break: break-all;
}

.icon-category {
  font-size: 10px;
  color: var(--text-color-3);
  background: var(--color-fill-quaternary);
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}
</style>
