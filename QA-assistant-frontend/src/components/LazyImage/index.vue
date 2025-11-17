<template>
  <div class="lazy-image-container" :style="{ width, height }">
    <img
      v-if="loaded"
      :src="src"
      :alt="alt"
      :class="['lazy-image', { 'fade-in': loaded }]"
      @load="handleLoad"
      @error="handleError"
    />
    <div
      v-else
      class="lazy-image-placeholder"
      :class="{ 'loading': isLoading }"
    >
      <div v-if="isLoading" class="loading-spinner"></div>
      <div v-else-if="hasError" class="error-icon">⚠️</div>
      <div v-else class="placeholder-content">
        <slot name="placeholder">
          <div class="default-placeholder">图片加载中...</div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';

interface Props {
  src: string;
  alt?: string;
  width?: string;
  height?: string;
  threshold?: number;
  rootMargin?: string;
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  width: '100%',
  height: 'auto',
  threshold: 0.1,
  rootMargin: '50px'
});

const emit = defineEmits<{
  load: [event: Event];
  error: [event: Event];
}>();

const loaded = ref(false);
const isLoading = ref(false);
const hasError = ref(false);
const observer = ref<IntersectionObserver | null>(null);

const handleLoad = (event: Event) => {
  loaded.value = true;
  isLoading.value = false;
  emit('load', event);
};

const handleError = (event: Event) => {
  hasError.value = true;
  isLoading.value = false;
  emit('error', event);
};

const startLoading = () => {
  if (loaded.value || isLoading.value) return;
  
  isLoading.value = true;
  hasError.value = false;
  
  // 创建图片对象进行预加载
  const img = new Image();
  img.onload = () => {
    loaded.value = true;
    isLoading.value = false;
  };
  img.onerror = () => {
    hasError.value = true;
    isLoading.value = false;
  };
  img.src = props.src;
};

const setupIntersectionObserver = () => {
  if (!('IntersectionObserver' in window)) {
    // 不支持 IntersectionObserver 时直接加载
    startLoading();
    return;
  }

  observer.value = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          startLoading();
          observer.value?.unobserve(entry.target);
        }
      });
    },
    {
      threshold: props.threshold,
      rootMargin: props.rootMargin
    }
  );

  // 观察容器元素
  const container = document.querySelector('.lazy-image-container');
  if (container) {
    observer.value.observe(container);
  }
};

onMounted(() => {
  setupIntersectionObserver();
});

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect();
  }
});

// 监听 src 变化
watch(() => props.src, () => {
  loaded.value = false;
  hasError.value = false;
  isLoading.value = false;
  setupIntersectionObserver();
});
</script>

<style lang="less" scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.lazy-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
  
  &.fade-in {
    opacity: 1;
  }
}

.lazy-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #999;
  font-size: 14px;
  
  &.loading {
    background-color: #f0f0f0;
  }
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e0e0e0;
  border-top: 2px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-icon {
  font-size: 24px;
  color: #ff4d4f;
}

.default-placeholder {
  text-align: center;
  color: #999;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
