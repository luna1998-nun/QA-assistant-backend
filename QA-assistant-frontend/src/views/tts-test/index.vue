<template>
  <div class="tts-test-container">
    <div class="test-header">
      <h1>TTS模块测试页面</h1>
      <p>这是一个独立的TTS功能测试页面，可以在没有后端服务的情况下测试前端TTS功能</p>
      <div class="mode-toggle">
        <n-tag :type="mockMode ? 'warning' : 'success'" size="large">
          {{ mockMode ? 'Mock模式 (无需后端)' : '真实API模式' }}
        </n-tag>
        <n-switch
          v-model:value="mockMode"
          size="large"
          style="margin-left: 15px"
        >
          <template #checked>Mock模式</template>
          <template #unchecked>API模式</template>
        </n-switch>
      </div>
    </div>

    <div class="test-content">
      <!-- 基础配置 -->
      <n-card title="基础配置" class="test-card">
        <div class="config-item">
          <label>测试文本：</label>
          <n-input
            v-model:value="testText"
            type="textarea"
            placeholder="请输入要测试的文本内容"
            :rows="4"
            maxlength="5000"
            show-count
          />
        </div>

        <div class="config-item">
          <label>语言选择：</label>
          <n-select
            v-model:value="config.language"
            :options="languageOptions"
            placeholder="选择语言"
            @update:value="onLanguageChange"
          />
        </div>

        <div class="config-item">
          <label>音色选择：</label>
          <n-select
            v-model:value="config.voice"
            :options="voiceOptions"
            placeholder="选择音色"
            :disabled="!config.language"
          />
        </div>

        <div class="config-item">
          <label>音频格式：</label>
          <n-select
            v-model:value="config.format"
            :options="formatOptions"
            placeholder="选择音频格式"
          />
        </div>
      </n-card>

      <!-- 功能测试 -->
      <n-card title="功能测试" class="test-card">
        <div class="test-buttons">
          <n-button
            type="primary"
            :loading="isLoading"
            @click="testGenerateSpeech"
          >
            测试语音生成（文件模式）
          </n-button>

          <n-button
            type="success"
            :loading="isLoading"
            @click="testGenerateSpeechStream"
          >
            测试语音生成（流模式）
          </n-button>

          <n-button
            type="info"
            @click="testFetchVoices"
          >
            测试获取音色
          </n-button>

          <n-button
            type="warning"
            @click="testServiceStatus"
          >
            测试服务状态
          </n-button>
        </div>
      </n-card>

      <!-- 结果显示 -->
      <n-card title="测试结果" class="test-card">
        <div class="result-display">
          <div class="result-item">
            <label>当前状态：</label>
            <n-tag :type="isGenerating ? 'warning' : 'success'">
              {{ isGenerating ? '生成中...' : '就绪' }}
            </n-tag>
          </div>

          <div v-if="currentAudioUrl" class="result-item">
            <label>音频URL：</label>
            <n-input :value="currentAudioUrl" readonly />
          </div>

          <div v-if="errorMessage" class="result-item">
            <label>错误信息：</label>
            <n-alert type="error" :title="errorMessage" />
          </div>
        </div>
      </n-card>

      <!-- 音频播放器 -->
      <n-card v-if="currentAudioUrl" title="音频播放" class="test-card">
        <audio
          ref="audioPlayer"
          :src="currentAudioUrl"
          controls
          style="width: 100%"
          @error="onAudioError"
          @loadedmetadata="onAudioLoaded"
        >
          您的浏览器不支持音频播放
        </audio>

        <div class="audio-controls">
          <n-button @click="downloadAudio">下载音频</n-button>
          <n-button @click="clearAudio">清除音频</n-button>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useTTS, type TTSConfig } from '@/hooks/useTTS'

const message = useMessage()

// 使用TTS Hook
const {
  isLoading,
  isGenerating,
  currentAudioUrl,
  currentDownloadUrl,
  error: ttsError,
  config,
  supportedVoices,
  mockMode,
  generateSpeech,
  generateSpeechStream,
  fetchSupportedVoices,
  checkServiceStatus,
  downloadAudio: downloadAudioFromTTS,
  cleanup
} = useTTS()

// 本地状态
const testText = ref('你好，这是一个TTS语音合成测试。Hello, this is a TTS speech synthesis test.')
const errorMessage = ref('')
const audioPlayer = ref<HTMLAudioElement>()

// 选项数据
const languageOptions = [
  { label: '中文', value: 'chinese' },
  { label: '英文', value: 'english' },
  { label: '日文', value: 'japanese' },
  { label: '韩文', value: 'korean' },
  { label: '粤语', value: 'cantonese' }
]

const formatOptions = [
  { label: 'MP3', value: 'mp3' },
  { label: 'WAV', value: 'wav' },
  { label: 'OGG', value: 'ogg' }
]

// 动态音色选项
const voiceOptions = computed(() => {
  if (!config.value.language) return []
  const voices = supportedVoices.value[config.value.language as keyof typeof supportedVoices.value] || []

  const voiceLabels = {
    chinese: ['龙婉', '晓晴', '晓梦', '晓雪', '晓云'],
    english: ['Anna', 'Brian', 'Cathy', 'David', 'Emily'],
    japanese: ['春香', '光', '香织', '舞', '奈奈'],
    korean: ['智勋', '秀珍', '允娜', '民俊', '惠珍'],
    cantonese: ['小敏', '小芬', '小雅', '小莹', '小美']
  }

  const labels = voiceLabels[config.value.language as keyof typeof voiceLabels] || []

  return voices.map((voice, index) => ({
    label: labels[index] || voice,
    value: voice
  }))
})

// 方法
const onLanguageChange = () => {
  config.value.voice = supportedVoices.value[config.value.language as keyof typeof supportedVoices.value]?.[0] || ''
}

const testGenerateSpeech = async () => {
  errorMessage.value = ''
  const result = await generateSpeech(testText.value)

  if (!result.success) {
    errorMessage.value = result.message
  }
}

const testGenerateSpeechStream = async () => {
  errorMessage.value = ''
  const result = await generateSpeechStream(testText.value)

  if (!result) {
    errorMessage.value = '流模式语音生成失败'
  }
}

const testFetchVoices = async () => {
  const voices = await fetchSupportedVoices()
  if (voices) {
    message.success('获取音色成功')
  } else {
    message.error('获取音色失败')
  }
}

const testServiceStatus = async () => {
  const status = await checkServiceStatus()
  if (status) {
    message.success('服务状态检查成功')
  } else {
    message.error('服务状态检查失败')
  }
}

const downloadAudio = () => {
  downloadAudioFromTTS()
}

const clearAudio = () => {
  cleanup()
  currentAudioUrl.value = ''
  message.success('音频已清除')
}

const onAudioError = () => {
  message.error('音频加载失败')
}

const onAudioLoaded = () => {
  message.success('音频加载成功')
}

// 生命周期
onMounted(() => {
  // 初始化TTS配置
  config.value = {
    voice: 'longwan',
    language: 'chinese',
    format: 'mp3',
    speed: 1.0,
    volume: 1.0,
    enabled: true
  }
})
</script>

<style scoped>
.tts-test-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-header {
  text-align: center;
  margin-bottom: 30px;
}

.test-header h1 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.test-header p {
  color: #666;
  font-size: 16px;
}

.mode-toggle {
  margin-top: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.test-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.test-card {
  width: 100%;
}

.config-item {
  margin-bottom: 20px;
}

.config-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.test-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.result-display {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-item label {
  font-weight: bold;
  color: #333;
}

.audio-controls {
  margin-top: 15px;
  display: flex;
  gap: 15px;
}

@media (max-width: 768px) {
  .test-buttons {
    flex-direction: column;
  }

  .audio-controls {
    flex-direction: column;
  }
}
</style>