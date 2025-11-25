<template>
  <div class="audio-player" :class="{ 'is-playing': isPlaying, 'is-loading': isLoading }">
    <!-- 音频播放器主体 -->
    <div class="player-controls">
      <!-- 播放/暂停按钮 -->
      <button
        class="play-button"
        :disabled="isLoading || !audioUrl"
        @click="togglePlay"
        :title="isPlaying ? '暂停' : '播放'"
      >
        <n-icon size="20">
          <PauseOutlined v-if="isPlaying" />
          <PlayCircleOutlined v-else-if="!isLoading" />
          <LoadingOutlined v-else />
        </n-icon>
      </button>

      <!-- 音频信息显示 -->
      <div class="audio-info" v-if="audioUrl">
        <div class="audio-title">{{ title || '语音回复' }}</div>
        <div class="audio-duration">
          {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
        </div>
      </div>

      <!-- 进度条 -->
      <div class="progress-container" v-if="audioUrl">
        <div
          class="progress-bar"
          @click="seekTo"
          ref="progressBar"
        >
          <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
          <div class="progress-handle" :style="{ left: progressPercentage + '%' }"></div>
        </div>
      </div>

      <!-- 音量控制 -->
      <div class="volume-control">
        <button
          class="volume-button"
          @click="toggleMute"
          :title="isMuted ? '取消静音' : '静音'"
        >
          <n-icon size="16">
            <SoundOutlined v-if="!isMuted && volume > 0" />
            <MutedOutlined v-else-if="isMuted || volume === 0" />
          </n-icon>
        </button>
        <div class="volume-slider">
          <input
            type="range"
            min="0"
            max="100"
            v-model="volumeValue"
            @input="updateVolume"
            class="volume-input"
          />
        </div>
      </div>

      <!-- 下载按钮 -->
      <button
        class="download-button"
        v-if="audioUrl && downloadUrl"
        @click="downloadAudio"
        title="下载音频"
      >
        <n-icon size="16">
          <DownloadOutlined />
        </n-icon>
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <n-icon size="14"><ExclamationCircleOutlined /></n-icon>
      <span>{{ error }}</span>
    </div>

    <!-- 隐藏的音频元素 -->
    <audio
      ref="audioRef"
      :src="audioUrl"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @ended="onEnded"
      @error="onError"
      @canplay="onCanPlay"
      preload="metadata"
    ></audio>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  PlayCircleOutlined,
  PauseOutlined,
  LoadingOutlined,
  SoundOutlined,
  MutedOutlined,
  DownloadOutlined,
  ExclamationCircleOutlined
} from '@vicons/antd'

interface Props {
  audioUrl?: string
  title?: string
  downloadUrl?: string
  autoPlay?: boolean
  showDownload?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoPlay: false,
  showDownload: true
})

const emit = defineEmits<{
  play: []
  pause: []
  ended: []
  error: [error: string]
  timeupdate: [currentTime: number, duration: number]
}>()

// 音频相关状态
const audioRef = ref<HTMLAudioElement>()
const progressBar = ref<HTMLElement>()
const isPlaying = ref(false)
const isLoading = ref(false)
const isMuted = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(1)
const volumeValue = ref(100)
const error = ref('')

// 计算属性
const progressPercentage = computed(() => {
  if (duration.value === 0) return 0
  return (currentTime.value / duration.value) * 100
})

// 播放/暂停切换
function togglePlay() {
  if (!audioRef.value || !props.audioUrl) return

  if (isPlaying.value) {
    audioRef.value.pause()
  } else {
    isLoading.value = true
    audioRef.value.play().catch(err => {
      error.value = '播放失败: ' + err.message
      isLoading.value = false
    })
  }
}

// 静音切换
function toggleMute() {
  if (!audioRef.value) return

  isMuted.value = !isMuted.value
  audioRef.value.muted = isMuted.value
}

// 更新音量
function updateVolume() {
  if (!audioRef.value) return

  const newVolume = volumeValue.value / 100
  volume.value = newVolume
  audioRef.value.volume = newVolume

  // 如果音量大于0且处于静音状态，取消静音
  if (newVolume > 0 && isMuted.value) {
    isMuted.value = false
    audioRef.value.muted = false
  }
}

// 跳转到指定位置
function seekTo(event: MouseEvent) {
  if (!audioRef.value || !progressBar.value) return

  const rect = progressBar.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const percentage = x / rect.width
  const targetTime = percentage * duration.value

  audioRef.value.currentTime = targetTime
  currentTime.value = targetTime
}

// 下载音频
function downloadAudio() {
  if (!props.downloadUrl) return

  const link = document.createElement('a')
  link.href = props.downloadUrl
  link.download = props.title || 'tts-audio.mp3'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 格式化时间
function formatTime(seconds: number): string {
  if (isNaN(seconds)) return '00:00'

  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 音频事件处理
function onTimeUpdate() {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
    emit('timeupdate', currentTime.value, duration.value)
  }
}

function onLoadedMetadata() {
  if (audioRef.value) {
    duration.value = audioRef.value.duration
  }
}

function onCanPlay() {
  isLoading.value = false
}

function onEnded() {
  isPlaying.value = false
  currentTime.value = 0
  emit('ended')
}

function onError(event: Event) {
  const audioElement = event.target as HTMLAudioElement
  error.value = `音频加载失败: ${audioElement.error?.message || '未知错误'}`
  isLoading.value = false
  isPlaying.value = false
  emit('error', error.value)
}

// 监听音频URL变化
watch(() => props.audioUrl, (newUrl) => {
  if (newUrl && audioRef.value) {
    // 重置状态
    isPlaying.value = false
    isLoading.value = false
    currentTime.value = 0
    duration.value = 0
    error.value = ''

    nextTick(() => {
      if (props.autoPlay) {
        togglePlay()
      }
    })
  }
})

// 监听播放状态
watch(isPlaying, (playing) => {
  if (playing) {
    emit('play')
  } else {
    emit('pause')
  }
})

// 组件挂载时初始化音量
onMounted(() => {
  if (audioRef.value) {
    audioRef.value.volume = volume.value
  }
})

// 组件卸载时清理
onBeforeUnmount(() => {
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value.src = ''
  }
})

// 暴露给父组件的方法
defineExpose({
  play: () => togglePlay(),
  pause: () => {
    if (audioRef.value) {
      audioRef.value.pause()
    }
  },
  stop: () => {
    if (audioRef.value) {
      audioRef.value.pause()
      audioRef.value.currentTime = 0
    }
  },
  setVolume: (vol: number) => {
    volumeValue.value = vol * 100
    updateVolume()
  },
  getCurrentTime: () => currentTime.value,
  getDuration: () => duration.value
})
</script>

<style scoped lang="less">
.audio-player {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  font-size: 12px;

  &.is-playing {
    border-color: #66c2ff;
    background: #f0f8ff;
  }

  &.is-loading {
    opacity: 0.8;
  }
}

.player-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.play-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #66c2ff;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    background: #0066cc;
    transform: scale(1.05);
  }

  &:disabled {
    background: #cccccc;
    cursor: not-allowed;
    transform: none;
  }
}

.audio-info {
  flex: 1;
  min-width: 0;
}

.audio-title {
  font-weight: 500;
  color: #333333;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audio-duration {
  color: #666666;
  font-size: 11px;
}

.progress-container {
  flex: 2;
  min-width: 100px;
}

.progress-bar {
  position: relative;
  height: 6px;
  background: #e5e5e5;
  border-radius: 3px;
  cursor: pointer;
  overflow: hidden;
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #66c2ff, #0066cc);
  border-radius: 3px;
  transition: width 0.1s ease;
}

.progress-handle {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: white;
  border: 2px solid #66c2ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 6px;
}

.volume-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #666666;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #e5e5e5;
    color: #333333;
  }
}

.volume-slider {
  width: 60px;
}

.volume-input {
  width: 100%;
  height: 4px;
  background: #e5e5e5;
  border-radius: 2px;
  outline: none;
  cursor: pointer;
  -webkit-appearance: none;

  &::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 12px;
    height: 12px;
    background: #66c2ff;
    border-radius: 50%;
    cursor: pointer;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.2);
    }
  }

  &::-moz-range-thumb {
    width: 12px;
    height: 12px;
    background: #66c2ff;
    border-radius: 50%;
    cursor: pointer;
    border: none;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.2);
    }
  }
}

.download-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #666666;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #e5e5e5;
    color: #333333;
  }
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  color: #ff4d4f;
  font-size: 11px;
}

// 响应式设计
@media (max-width: 768px) {
  .player-controls {
    flex-wrap: wrap;
    gap: 8px;
  }

  .progress-container {
    order: 3;
    flex-basis: 100%;
  }

  .audio-info {
    min-width: 80px;
  }

  .volume-slider {
    width: 40px;
  }
}
</style>