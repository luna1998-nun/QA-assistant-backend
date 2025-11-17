<template>
  <div class="api-development-guide">
    <n-card title="API开发指南" :bordered="false">
      <n-space vertical size="large">
        <n-alert type="info">
          <template #header>
            📖 API开发完整指南
          </template>
          本页面展示API开发的最佳实践和规范。
        </n-alert>
        
        <n-card title="文档内容" size="small">
          <div class="markdown-content">
            <vue3-markdown-it :source="markdownContent" />
          </div>
        </n-card>
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Vue3MarkdownIt from 'vue3-markdown-it';

const markdownContent = ref('');

onMounted(async () => {
  try {
    // 动态导入markdown文件
    const module = await import('@/docs/api-development-guide.md?raw');
    markdownContent.value = module.default;
  } catch (error) {
    console.error('加载markdown文件失败:', error);
    markdownContent.value = '文档加载失败，请检查文件路径。';
  }
});
</script>

<style scoped>
.api-development-guide {
  padding: 20px;
}

.markdown-content {
  line-height: 1.6;
  font-size: 14px;
}

/* Markdown 样式 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: bold;
  color: #333;
}

.markdown-content :deep(h1) {
  font-size: 1.8em;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 0.3em;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 0.2em;
}

.markdown-content :deep(h3) {
  font-size: 1.3em;
}

.markdown-content :deep(p) {
  margin-bottom: 1em;
  color: #555;
}

.markdown-content :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.markdown-content :deep(pre) {
  background-color: #f8f9fa;
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  margin: 1em 0;
  border: 1px solid #e9ecef;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  font-size: 0.9em;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-bottom: 1em;
  padding-left: 2em;
}

.markdown-content :deep(li) {
  margin-bottom: 0.5em;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #ddd;
  margin: 1em 0;
  padding-left: 1em;
  color: #666;
  background-color: #f9f9f9;
  padding: 1em;
  border-radius: 0 4px 4px 0;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
  font-size: 0.9em;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  background-color: #f5f5f5;
  font-weight: bold;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #ddd;
  margin: 2em 0;
}
</style>