<template>
  <n-modal v-model:show="visible" preset="card" title="数据可视化" size="large" class="chart-modal">
    <div class="chart-container">
      <div class="chart-header">
        <h3>{{ chartTitle }}</h3>
        <div class="chart-tabs">
          <n-button 
            v-for="tab in chartTabs" 
            :key="tab.key"
            :type="activeTab === tab.key ? 'primary' : 'default'"
            size="small"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </n-button>
        </div>
      </div>
      
      <div class="chart-content">
        <!-- 趋势图 -->
        <div v-if="activeTab === 'trend'" class="chart-item">
          <div class="chart-placeholder trend-chart">
            <div class="chart-title">📈 趋势分析图</div>
            <div class="chart-data">
              <div class="data-point" v-for="(point, index) in trendData" :key="index">
                <div class="point-value">{{ point.value }}</div>
                <div class="point-label">{{ point.label }}</div>
              </div>
            </div>
            <div class="chart-description">
              数据显示持续上升趋势，增长率达到 15.2%
            </div>
          </div>
        </div>
        
        <!-- 分布图 -->
        <div v-if="activeTab === 'distribution'" class="chart-item">
          <div class="chart-placeholder distribution-chart">
            <div class="chart-title">📊 数据分布图</div>
            <div class="distribution-bars">
              <div class="bar" v-for="(bar, index) in distributionData" :key="index" 
                   :style="{ height: bar.percentage + '%' }">
                <span class="bar-label">{{ bar.label }}</span>
                <span class="bar-value">{{ bar.value }}</span>
              </div>
            </div>
            <div class="chart-description">
              数据分布相对均匀，主要集中在中高区间
            </div>
          </div>
        </div>
        
        <!-- 对比图 -->
        <div v-if="activeTab === 'comparison'" class="chart-item">
          <div class="chart-placeholder comparison-chart">
            <div class="chart-title">📋 多维度对比图</div>
            <div class="comparison-grid">
              <div class="comparison-item" v-for="(item, index) in comparisonData" :key="index">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-value">{{ item.value }}</div>
                <div class="item-trend" :class="item.trend">
                  {{ item.trend === 'up' ? '↗' : '↘' }} {{ item.change }}
                </div>
              </div>
            </div>
            <div class="chart-description">
              各维度表现差异明显，建议重点关注优势领域
            </div>
          </div>
        </div>
      </div>
      
      <div class="chart-footer">
        <n-button @click="exportChart">导出图表</n-button>
        <n-button type="primary" @click="visible = false">关闭</n-button>
      </div>
    </div>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

const visible = ref(false);
const activeTab = ref('trend');
const chartTitle = ref('数据分析报告');

const chartTabs = [
  { key: 'trend', label: '趋势图' },
  { key: 'distribution', label: '分布图' },
  { key: 'comparison', label: '对比图' }
];

const trendData = ref([
  { label: 'Q1', value: 85 },
  { label: 'Q2', value: 92 },
  { label: 'Q3', value: 88 },
  { label: 'Q4', value: 95 }
]);

const distributionData = ref([
  { label: 'A区', value: 45, percentage: 75 },
  { label: 'B区', value: 38, percentage: 63 },
  { label: 'C区', value: 52, percentage: 87 },
  { label: 'D区', value: 41, percentage: 68 }
]);

const comparisonData = ref([
  { name: '生产效率', value: '92%', trend: 'up', change: '+5%' },
  { name: '质量指标', value: '88%', trend: 'up', change: '+3%' },
  { name: '成本控制', value: '85%', trend: 'down', change: '-2%' },
  { name: '安全记录', value: '96%', trend: 'up', change: '+1%' }
]);

function exportChart() {
  // 模拟导出功能
  console.log('导出图表:', activeTab.value);
}

// 暴露方法给父组件调用
defineExpose({
  show: (title: string) => {
    chartTitle.value = title;
    visible.value = true;
  }
});
</script>

<style scoped>
.chart-modal {
  :deep(.n-card) {
    background: linear-gradient(135deg, #0f1422 0%, #121827 100%);
    border: 1px solid rgba(0, 119, 187, 0.3);
    color: #ffffff;
  }
}

.chart-container {
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 119, 187, 0.3);
}

.chart-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
}

.chart-tabs {
  display: flex;
  gap: 8px;
}

.chart-content {
  min-height: 400px;
  margin-bottom: 20px;
}

.chart-item {
  height: 100%;
}

.chart-placeholder {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 119, 187, 0.3);
  border-radius: 12px;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #0066ff;
  margin-bottom: 20px;
}

.trend-chart .chart-data {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.data-point {
  text-align: center;
  padding: 12px;
  background: rgba(0, 102, 255, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(0, 102, 255, 0.3);
}

.point-value {
  font-size: 24px;
  font-weight: 600;
  color: #0066ff;
}

.point-label {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.distribution-chart .distribution-bars {
  display: flex;
  gap: 16px;
  align-items: end;
  height: 200px;
  margin-bottom: 20px;
}

.bar {
  width: 60px;
  background: linear-gradient(180deg, #0066ff 0%, #0033cc 100%);
  border-radius: 4px 4px 0 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px 4px;
  position: relative;
}

.bar-label {
  font-size: 10px;
  color: #ffffff;
  text-align: center;
}

.bar-value {
  font-size: 12px;
  color: #ffffff;
  text-align: center;
  font-weight: 600;
}

.comparison-chart .comparison-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  width: 100%;
  max-width: 400px;
}

.comparison-item {
  background: rgba(0, 102, 255, 0.1);
  border: 1px solid rgba(0, 102, 255, 0.3);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.item-name {
  font-size: 14px;
  color: #ffffff;
  margin-bottom: 8px;
}

.item-value {
  font-size: 20px;
  font-weight: 600;
  color: #0066ff;
  margin-bottom: 4px;
}

.item-trend {
  font-size: 12px;
  font-weight: 500;
}

.item-trend.up {
  color: #10b981;
}

.item-trend.down {
  color: #ef4444;
}

.chart-description {
  font-size: 14px;
  color: #9ca3af;
  text-align: center;
  line-height: 1.5;
}

.chart-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 119, 187, 0.3);
}
</style>
