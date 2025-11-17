<template>
  <div style="background-color: #f5f5f5; min-height: 100vh; padding: 20px;">
    <h1 style="color: #424242;">设备管理</h1>
    <p style="color: #212121;">设备管理主页 - 设备全生命周期管理</p>
    
    <div style="margin-top: 30px;">
      <h2 style="color: #424242; margin-bottom: 20px;">功能模块</h2>
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px;">
        <div style="background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
          <h3 style="color: #3f51b5; margin-bottom: 10px;">🔧 设备维护</h3>
          <p style="color: #666; margin-bottom: 15px;">设备日常维护、预防性维护、维修记录管理</p>
          <ul style="color: #666; margin: 0; padding-left: 20px;">
            <li>日常维护</li>
            <li>预防性维护</li>
            <li>维修记录</li>
          </ul>
        </div>
        
        <div style="background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
          <h3 style="color: #d32f2f; margin-bottom: 10px;">⚠️ 故障诊断</h3>
          <p style="color: #666; margin-bottom: 15px;">设备故障检测、分析、处理全流程管理</p>
          <ul style="color: #666; margin: 0; padding-left: 20px;">
            <li>故障检测</li>
            <li>故障分析</li>
            <li>故障处理</li>
          </ul>
        </div>
        
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
</script>
