<template>
  <div class="tts-settings">
    <!-- 设置标题 -->
    <div class="settings-header">
      <h4>语音设置</h4>
      <n-switch
        v-model:value="localConfig.enabled"
        @update:value="onEnabledChange"
        size="small"
      >
        <template #checked>启用</template>
        <template #unchecked>禁用</template>
      </n-switch>
    </div>

    <!-- 设置内容 -->
    <n-collapse-transition :show="localConfig.enabled">
      <div class="settings-content">
        <!-- 音色选择 -->
        <div class="setting-item">
          <label class="setting-label">
            <n-icon size="16"><SoundOutlined /></n-icon>
            音色选择
          </label>
          <div class="setting-controls">
            <n-select
              v-model:value="localConfig.language"
              :options="languageOptions"
              placeholder="选择语言"
              size="small"
              @update:value="onLanguageChange"
            />
            <n-select
              v-model:value="localConfig.voice"
              :options="voiceOptions"
              placeholder="选择音色"
              size="small"
              :disabled="!localConfig.language"
              @update:value="onVoiceChange"
            />
          </div>
        </div>

        <!-- 音频格式 -->
        <div class="setting-item">
          <label class="setting-label">
            <n-icon size="16"><FileAudioOutlined /></n-icon>
            音频格式
          </label>
          <div class="setting-controls">
            <n-select
              v-model:value="localConfig.format"
              :options="formatOptions"
              size="small"
              @update:value="onFormatChange"
            />
          </div>
        </div>

        <!-- 语速调节 -->
        <div class="setting-item">
          <label class="setting-label">
            <n-icon size="16"><ThunderboltOutlined /></n-icon>
            语速调节
          </label>
          <div class="setting-controls">
            <n-slider
              v-model:value="localConfig.speed"
              :min="0.5"
              :max="2.0"
              :step="0.1"
              :marks="speedMarks"
              :tooltip="false"
              size="small"
              @update:value="onSpeedChange"
            />
            <span class="value-display">{{ localConfig.speed }}x</span>
          </div>
        </div>

        <!-- 音量调节 -->
        <div class="setting-item">
          <label class="setting-label">
            <n-icon size="16"><VolumeUpOutlined /></n-icon>
            音量调节
          </label>
          <div class="setting-controls">
            <n-slider
              v-model:value="localConfig.volume"
              :min="0.1"
              :max="1.0"
              :step="0.1"
              :marks="volumeMarks"
              :tooltip="false"
              size="small"
              @update:value="onVolumeChange"
            />
            <span class="value-display">{{ Math.round(localConfig.volume * 100) }}%</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="setting-actions">
          <n-button size="small" @click="testTTS" :loading="isTesting">
            <template #icon>
              <n-icon><PlayCircleOutlined /></n-icon>
            </template>
            测试语音
          </n-button>
          <n-button size="small" @click="resetSettings" quaternary>
            <template #icon>
              <n-icon><ReloadOutlined /></n-icon>
            </template>
            重置
          </n-button>
        </div>
      </div>
    </n-collapse-transition>

    <!-- 测试用的音频播放器 -->
    <n-collapse-transition :show="!!testAudioUrl">
      <div class="test-audio">
        <AudioPlayer
          :audio-url="testAudioUrl"
          :title="'语音测试'"
          :download-url="testAudioUrl"
        />
      </div>
    </n-collapse-transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import {
  NSwitch,
  NCollapseTransition,
  NSelect,
  NSlider,
  NButton,
  NIcon
} from 'naive-ui'
import {
  SoundOutlined,
  FileAudioOutlined,
  ThunderboltOutlined,
  VolumeUpOutlined,
  PlayCircleOutlined,
  ReloadOutlined
} from '@vicons/antd'
import AudioPlayer from '../AudioPlayer/AudioPlayer.vue'
import { useTTS, type TTSConfig } from '@/hooks/useTTS'

const emit = defineEmits<{
  configChange: [config: TTSConfig]
}>()

const message = useMessage()
const { supportedVoices, generateSpeech, updateConfig } = useTTS()

// 本地配置状态
const localConfig = ref<TTSConfig>({
  voice: 'longwan',
  language: 'chinese',
  format: 'mp3',
  speed: 1.0,
  volume: 1.0,
  enabled: true
})

// 测试相关状态
const isTesting = ref(false)
const testAudioUrl = ref('')

// 语言选项
const languageOptions = computed(() => [
  { label: '中文', value: 'chinese' },
  { label: '英文', value: 'english' },
  { label: '日文', value: 'japanese' },
  { label: '韩文', value: 'korean' },
  { label: '粤语', value: 'cantonese' }
])

// 音色选项（基于选中的语言）
const voiceOptions = computed(() => {
  if (!localConfig.value.language || !supportedVoices.value) {
    return []
  }

  const voices = supportedVoices.value[localConfig.value.language as keyof typeof supportedVoices.value]
  const voiceLabels = {
    chinese: ['龙婉', '晓晴', '晓梦', '晓雪', '晓云'],
    english: ['Anna', 'Brian', 'Cathy', 'David', 'Emily'],
    japanese: ['春香', '光', '香织', '舞', '奈奈'],
    korean: ['智勋', '秀珍', '允娜', '民俊', '惠珍'],
    cantonese: ['小敏', '小芬', '小雅', '小莹', '小美']
  }

  return voices?.map((voice, index) => ({
    label: voiceLabels[localConfig.value.language as keyof typeof voiceLabels]?.[index] || voice,
    value: voice
  })) || []
})

// 格式选项
const formatOptions = [
  { label: 'MP3 (推荐)', value: 'mp3' },
  { label: 'WAV', value: 'wav' },
  { label: 'PCM', value: 'pcm' }
]

// 语速标记
const speedMarks = {
  0.5: '慢速',
  1.0: '正常',
  2.0: '快速'
}

// 音量标记
const volumeMarks = {
  0.1: '10%',
  0.5: '50%',
  1.0: '100%'
}

// 配置变化处理函数
function onEnabledChange(enabled: boolean) {
  localConfig.value.enabled = enabled
  updateConfigAndEmit()
}

function onLanguageChange(language: string) {
  localConfig.value.language = language
  // 重置音色为该语言的第一个音色
  if (voiceOptions.value.length > 0) {
    localConfig.value.voice = voiceOptions.value[0].value
  }
  updateConfigAndEmit()
}

function onVoiceChange(voice: string) {
  localConfig.value.voice = voice
  updateConfigAndEmit()
}

function onFormatChange(format: string) {
  localConfig.value.format = format
  updateConfigAndEmit()
}

function onSpeedChange(speed: number) {
  localConfig.value.speed = speed
  updateConfigAndEmit()
}

function onVolumeChange(volume: number) {
  localConfig.value.volume = volume
  updateConfigAndEmit()
}

// 更新配置并发送事件
function updateConfigAndEmit() {
  updateConfig(localConfig.value)
  emit('configChange', localConfig.value)
}

// 测试TTS
async function testTTS() {
  if (isTesting.value) return

  const testText = '这是语音合成测试，欢迎使用文字转语音功能。'

  try {
    isTesting.value = true
    testAudioUrl.value = '' // 清除之前的测试音频

    const result = await generateSpeech(testText, {
      voice: localConfig.value.voice,
      language: localConfig.value.language,
      format: localConfig.value.format,
      speed: localConfig.value.speed,
      volume: localConfig.value.volume
    })

    if (result.success && result.filePath) {
      testAudioUrl.value = `/api/tts/download?filePath=${encodeURIComponent(result.filePath)}`
      message.success('语音测试生成成功')
    } else {
      message.error(result.message || '语音测试失败')
    }
  } catch (error) {
    console.error('TTS test error:', error)
    message.error('语音测试失败')
  } finally {
    isTesting.value = false
  }
}

// 重置设置
function resetSettings() {
  localConfig.value = {
    voice: 'longwan',
    language: 'chinese',
    format: 'mp3',
    speed: 1.0,
    volume: 1.0,
    enabled: true
  }
  testAudioUrl.value = ''
  updateConfigAndEmit()
  message.success('设置已重置')
}

// 初始化
onMounted(() => {
  // 从localStorage加载配置
  const savedConfig = localStorage.getItem('tts-config')
  if (savedConfig) {
    try {
      const parsed = JSON.parse(savedConfig)
      localConfig.value = { ...localConfig.value, ...parsed }
    } catch (e) {
      console.error('Failed to parse saved TTS config:', e)
    }
  }
})
</script>

<style scoped lang="less">
.tts-settings {
  .settings-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    h4 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #333333;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .settings-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .setting-item {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .setting-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      font-weight: 500;
      color: #666666;
    }

    .setting-controls {
      display: flex;
      align-items: center;
      gap: 12px;

      .n-select,
      .n-slider {
        flex: 1;
      }

      .value-display {
        min-width: 50px;
        text-align: right;
        font-size: 12px;
        color: #333333;
        font-weight: 500;
      }
    }
  }

  .setting-actions {
    display: flex;
    gap: 8px;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
  }

  .test-audio {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .tts-settings {
    .setting-item {
      .setting-controls {
        flex-direction: column;
        align-items: stretch;
        gap: 8px;

        .value-display {
          text-align: left;
          align-self: flex-end;
        }
      }
    }

    .setting-actions {
      flex-direction: column;
    }
  }
}
</style>