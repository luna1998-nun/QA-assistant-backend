<template>
  <div>
    <div class="n-layout-page-header">
      <n-card :bordered="false" title="关于">
        {{ name }} 是一个基于 Vue3、Vite7、TypeScript 的生产运营平台，专为企业级生产运营管理而设计。平台集成了生产运行、安全环保、大监控、设备管理等核心功能模块，为企业提供全方位的生产运营解决方案。
      </n-card>
    </div>
    <n-card
      :bordered="false"
      title="项目信息"
      class="mt-4 proCard"
      size="small"
      :segmented="{ content: true }"
    >
      <n-descriptions bordered label-placement="left" class="py-2">
        <n-descriptions-item label="版本">
          <n-tag type="info"> {{ version }} </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="最后编译时间">
          <n-tag type="info"> {{ lastBuildTime }} </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="平台类型">
          <n-tag type="success">生产运营管理平台</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="核心模块">
          <div class="flex items-center gap-2">
            <n-tag type="info" size="small">生产运行</n-tag>
            <n-tag type="info" size="small">安全环保</n-tag>
            <n-tag type="info" size="small">大监控</n-tag>
            <n-tag type="info" size="small">设备管理</n-tag>
          </div>
        </n-descriptions-item>
        <n-descriptions-item label="技术栈">
          <div class="flex items-center gap-2">
            <n-tag type="success" size="small">Vue3</n-tag>
            <n-tag type="success" size="small">TypeScript</n-tag>
            <n-tag type="success" size="small">Vite7</n-tag>
            <n-tag type="success" size="small">Naive UI</n-tag>
          </div>
        </n-descriptions-item>
        <n-descriptions-item label="系统状态">
          <n-tag type="success">运行中</n-tag>
        </n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card
      :bordered="false"
      title="开发环境依赖"
      class="mt-4 proCard"
      size="small"
      :segmented="{ content: true }"
    >
      <n-descriptions bordered label-placement="left" class="py-2">
        <n-descriptions-item v-for="item in devSchema" :key="item.field" :label="item.field">
          {{ item.label }}
        </n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card
      :bordered="false"
      title="生产环境依赖"
      class="mt-4 proCard"
      size="small"
      :segmented="{ content: true }"
    >
      <n-descriptions bordered label-placement="left" class="py-2">
        <n-descriptions-item v-for="item in schema" :key="item.field" :label="item.field">
          {{ item.label }}
        </n-descriptions-item>
      </n-descriptions>
    </n-card>
  </div>
</template>

<script lang="ts" setup>
  export interface schemaItem {
    field: string;
    label: string;
  }

  const { pkg, lastBuildTime } = __APP_INFO__;
  const { dependencies, devDependencies, name, version } = pkg;

  const schema: schemaItem[] = [];
  const devSchema: schemaItem[] = [];

  Object.keys(dependencies).forEach((key) => {
    schema.push({ field: key, label: dependencies[key] });
  });

  Object.keys(devDependencies).forEach((key) => {
    devSchema.push({ field: key, label: devDependencies[key] });
  });
</script>

<style lang="less" scoped></style>
