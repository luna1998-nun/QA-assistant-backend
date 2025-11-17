<template>
  <div
    ref="containerRef"
    class="virtual-list-container"
    :style="{ height: containerHeight + 'px' }"
    @scroll="handleScroll"
  >
    <div
      class="virtual-list-content"
      :style="{ height: totalHeight + 'px', transform: `translateY(${offsetY}px)` }"
    >
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item, item.index)"
        class="virtual-list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item.data" :index="item.index">
          {{ item.data }}
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';

interface VirtualListItem {
  data: any;
  index: number;
}

interface Props {
  items: any[];
  itemHeight: number;
  containerHeight: number;
  overscan?: number;
  getItemKey?: (item: any, index: number) => string | number;
}

const props = withDefaults(defineProps<Props>(), {
  overscan: 5,
  getItemKey: (item: any, index: number) => index
});

const containerRef = ref<HTMLElement>();
const scrollTop = ref(0);

// 计算可见范围
const visibleRange = computed(() => {
  const start = Math.floor(scrollTop.value / props.itemHeight);
  const end = Math.min(
    start + Math.ceil(props.containerHeight / props.itemHeight) + props.overscan,
    props.items.length
  );
  return { start, end };
});

// 可见项目
const visibleItems = computed((): VirtualListItem[] => {
  const { start, end } = visibleRange.value;
  return props.items.slice(start, end).map((item, index) => ({
    data: item,
    index: start + index
  }));
});

// 总高度
const totalHeight = computed(() => props.items.length * props.itemHeight);

// 偏移量
const offsetY = computed(() => visibleRange.value.start * props.itemHeight);

// 滚动处理
const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement;
  scrollTop.value = target.scrollTop;
};

// 滚动到指定位置
const scrollTo = (index: number) => {
  if (containerRef.value) {
    const scrollTop = index * props.itemHeight;
    containerRef.value.scrollTop = scrollTop;
  }
};

// 滚动到顶部
const scrollToTop = () => {
  scrollTo(0);
};

// 滚动到底部
const scrollToBottom = () => {
  scrollTo(props.items.length - 1);
};

// 获取当前可见的第一个项目索引
const getVisibleStartIndex = () => {
  return visibleRange.value.start;
};

// 获取当前可见的最后一个项目索引
const getVisibleEndIndex = () => {
  return visibleRange.value.end - 1;
};

// 暴露方法给父组件
defineExpose({
  scrollTo,
  scrollToTop,
  scrollToBottom,
  getVisibleStartIndex,
  getVisibleEndIndex
});

// 监听容器高度变化
const resizeObserver = ref<ResizeObserver | null>(null);

onMounted(() => {
  if (containerRef.value && 'ResizeObserver' in window) {
    resizeObserver.value = new ResizeObserver((entries) => {
      // 容器大小变化时重新计算
      for (const entry of entries) {
        // 可以在这里处理容器大小变化
      }
    });
    resizeObserver.value.observe(containerRef.value);
  }
});

onUnmounted(() => {
  if (resizeObserver.value) {
    resizeObserver.value.disconnect();
  }
});

// 监听 items 变化
watch(() => props.items, () => {
  // 数据变化时重置滚动位置
  scrollTop.value = 0;
  if (containerRef.value) {
    containerRef.value.scrollTop = 0;
  }
});
</script>

<style lang="less" scoped>
.virtual-list-container {
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

.virtual-list-content {
  position: relative;
}

.virtual-list-item {
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  
  &:hover {
    background-color: #f5f5f5;
  }
}

// 滚动条样式优化
.virtual-list-container::-webkit-scrollbar {
  width: 6px;
}

.virtual-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.virtual-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.virtual-list-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
