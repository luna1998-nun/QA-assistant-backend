import { ref, computed, watch } from 'vue'
import { useMessage } from 'naive-ui'

// TTS配置接口
export interface TTSConfig {
  voice: string
  language: string
  format: string
  speed: number
  volume: number
  enabled: boolean
}

// TTS响应接口
export interface TTSResponse {
  success: boolean
  filePath?: string
  message: string
}

// 音色信息接口
export interface VoiceInfo {
  chinese: string[]
  english: string[]
  japanese: string[]
  korean: string[]
  cantonese: string[]
}

// API响应接口
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message: string
  code?: string
}

export function useTTS() {
  const message = useMessage()

  // 响应式状态
  const isLoading = ref(false)
  const isGenerating = ref(false)
  const currentAudioUrl = ref<string>('')
  const currentDownloadUrl = ref<string>('')
  const ttsEnabled = ref(false)
  const error = ref<string>('')

  // Mock模式开关
  const mockMode = ref(true) // 默认启用Mock模式

  // TTS配置
  const config = ref<TTSConfig>({
    voice: 'longwan',           // 默认中文女声
    language: 'chinese',        // 默认中文
    format: 'mp3',             // 默认MP3格式
    speed: 1.0,                // 默认语速
    volume: 1.0,               // 默认音量
    enabled: true              // 默认启用TTS
  })

  // 支持的音色列表
  const supportedVoices = ref<VoiceInfo>({
    chinese: ['longwan', 'xiaoqing', 'xiaomeng', 'xiaoxue', 'xiaoyun'],
    english: ['anna', 'brian', 'cathy', 'david', 'emily'],
    japanese: ['haruka', 'hikari', 'kaori', 'mai', 'nana'],
    korean: ['jihoon', 'sujin', 'yuna', 'minjun', 'hyejin'],
    cantonese: ['xiaomin', 'xiaofen', 'xiaoya', 'xiaoying', 'xiaomei']
  })

  // API基础URL
  const API_BASE_URL = '/api/tts'

  // 计算属性
  const currentVoiceLabel = computed(() => {
    const { voice, language } = config.value
    const voiceList = supportedVoices.value[language as keyof VoiceInfo]
    const index = voiceList?.indexOf(voice)

    const languageLabels = {
      chinese: '中文',
      english: '英文',
      japanese: '日文',
      korean: '韩文',
      cantonese: '粤语'
    }

    const voiceLabels = {
      chinese: ['龙婉', '晓晴', '晓梦', '晓雪', '晓云'],
      english: ['Anna', 'Brian', 'Cathy', 'David', 'Emily'],
      japanese: ['春香', '光', '香织', '舞', '奈奈'],
      korean: ['智勋', '秀珍', '允娜', '民俊', '惠珍'],
      cantonese: ['小敏', '小芬', '小雅', '小莹', '小美']
    }

    const currentLanguage = languageLabels[language as keyof typeof languageLabels]
    const currentVoice = voiceLabels[language as keyof typeof voiceLabels]?.[index] || voice

    return `${currentLanguage} - ${currentVoice}`
  })

  // 生成语音
  const generateSpeech = async (text: string, options?: Partial<TTSConfig>): Promise<TTSResponse> => {
    if (!text || text.trim().length === 0) {
      const errorMsg = '文本内容不能为空'
      error.value = errorMsg
      message.error(errorMsg)
      return { success: false, message: errorMsg }
    }

    if (text.length > 5000) {
      const errorMsg = '文本内容过长，请控制在5000字符以内'
      error.value = errorMsg
      message.error(errorMsg)
      return { success: false, message: errorMsg }
    }

    try {
      isGenerating.value = true
      isLoading.value = true
      error.value = ''

      // 合并配置
      const finalConfig = { ...config.value, ...options }

      // Mock模式处理
      if (mockMode.value) {
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 生成模拟文件路径
        const timestamp = Date.now()
        const filename = `tts_${finalConfig.voice}_${timestamp}.mp3`
        const filePath = `/tmp/tts/${filename}`

        // 构建音频URL（使用Mock数据）
        const mockAudioBase64 = 'SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWGluZwAAAA8AAAACAAAEaAAzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMz//////////////////////////////////////////////////////////////////8AAAAATGF2YzU4LjEzAAAAAAAAAAAAAAAAJAQKAAAAAAAADrABXFU3AAAAAAgAA0AAAAgAA0AAAAgAA0AAAAgAA0AAAQAAAAgAA0AAAAgAA0AAAAgAA0AAAQAA'
        const audioBlob = new Blob([Uint8Array.from(atob(mockAudioBase64), c => c.charCodeAt(0))], { type: 'audio/mpeg' })
        const audioUrl = URL.createObjectURL(audioBlob)

        currentAudioUrl.value = audioUrl
        currentDownloadUrl.value = audioUrl

        message.success('语音生成成功 (Mock模式)')
        return {
          success: true,
          filePath: filePath,
          message: '语音生成成功 (Mock模式)'
        }
      }

      // 实际API调用
      const requestBody = {
        text: text.trim(),
        voice: finalConfig.voice,
        language: finalConfig.language,
        format: finalConfig.format
      }

      console.log('TTS Request:', requestBody)

      const response = await fetch(`${API_BASE_URL}/convert/file`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const result: TTSResponse = await response.json()

      if (result.success && result.filePath) {
        // 构建音频URL和下载URL
        const baseUrl = window.location.origin
        currentAudioUrl.value = `${API_BASE_URL}/download?filePath=${encodeURIComponent(result.filePath)}`
        currentDownloadUrl.value = currentAudioUrl.value

        message.success('语音生成成功')
        return result
      } else {
        throw new Error(result.message || '语音生成失败')
      }

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '语音生成失败'
      error.value = errorMessage
      message.error(errorMessage)
      return { success: false, message: errorMessage }
    } finally {
      isGenerating.value = false
      isLoading.value = false
    }
  }

  // 生成语音流（直接返回音频流）
  const generateSpeechStream = async (text: string, options?: Partial<TTSConfig>): Promise<string | null> => {
    if (!text || text.trim().length === 0) {
      const errorMsg = '文本内容不能为空'
      error.value = errorMsg
      message.error(errorMsg)
      return null
    }

    try {
      isGenerating.value = true
      error.value = ''

      // 合并配置
      const finalConfig = { ...config.value, ...options }

      const requestBody = {
        text: text.trim(),
        voice: finalConfig.voice,
        language: finalConfig.language,
        format: finalConfig.format
      }

      const response = await fetch(`${API_BASE_URL}/convert/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      // 创建Blob URL
      const blob = await response.blob()
      const audioUrl = URL.createObjectURL(blob)
      currentAudioUrl.value = audioUrl
      currentDownloadUrl.value = audioUrl

      message.success('语音生成成功')
      return audioUrl

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '语音生成失败'
      error.value = errorMessage
      message.error(errorMessage)
      return null
    } finally {
      isGenerating.value = false
    }
  }

  // 获取支持的音色
  const fetchSupportedVoices = async (): Promise<VoiceInfo | null> => {
    try {
      // Mock模式处理
      if (mockMode.value) {
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 500))

        // 返回预设的音色数据
        const voices: VoiceInfo = {
          chinese: ['longwan', 'xiaoqing', 'xiaomeng', 'xiaoxue', 'xiaoyun'],
          english: ['anna', 'brian', 'cathy', 'david', 'emily'],
          japanese: ['haruka', 'hikari', 'kaori', 'mai', 'nana'],
          korean: ['jihoon', 'sujin', 'yuna', 'minjun', 'hyejin'],
          cantonese: ['xiaomin', 'xiaofen', 'xiaoya', 'xiaoying', 'xiaomei']
        }
        supportedVoices.value = voices
        message.success('获取音色成功 (Mock模式)')
        return voices
      }

      const response = await fetch(`${API_BASE_URL}/voices`)

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const voices = await response.json()
      supportedVoices.value = voices
      return voices

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '获取音色失败'
      console.error('Failed to fetch voices:', errorMessage)
      return null
    }
  }

  // 检查TTS服务状态
  const checkServiceStatus = async () => {
    try {
      // Mock模式处理
      if (mockMode.value) {
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 300))

        const status = {
          success: true,
          data: {
            status: 'running',
            version: '1.0.0',
            supportedFormats: ['mp3', 'wav', 'ogg'],
            maxTextLength: 5000
          },
          message: 'TTS服务运行正常 (Mock模式)'
        }
        console.log('TTS Service Status:', status)
        message.success('服务状态检查成功 (Mock模式)')
        return status
      }

      const response = await fetch(`${API_BASE_URL}/status`)

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const status = await response.json()
      console.log('TTS Service Status:', status)
      return status

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '检查服务状态失败'
      console.error('Failed to check TTS service status:', errorMessage)
      return null
    }
  }

  // 清理临时文件
  const cleanupTempFiles = async (hoursOld: number = 24) => {
    try {
      const response = await fetch(`${API_BASE_URL}/cleanup?hoursOld=${hoursOld}`, {
        method: 'POST'
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const result = await response.json()
      if (result.success) {
        message.success(`清理完成，删除了${hoursOld}小时前的临时文件`)
      } else {
        message.error(result.message || '清理失败')
      }

      return result

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '清理临时文件失败'
      message.error(errorMessage)
      return null
    }
  }

  // 下载音频文件
  const downloadAudio = (url?: string, filename?: string) => {
    const downloadUrl = url || currentDownloadUrl.value
    if (!downloadUrl) {
      message.error('没有可下载的音频文件')
      return
    }

    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename || `tts-audio-${Date.now()}.mp3`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  // 更新配置
  const updateConfig = (newConfig: Partial<TTSConfig>) => {
    config.value = { ...config.value, ...newConfig }

    // 保存到localStorage
    localStorage.setItem('tts-config', JSON.stringify(config.value))
  }

  // 切换TTS开关
  const toggleTTS = () => {
    config.value.enabled = !config.value.enabled
    ttsEnabled.value = config.value.enabled
    localStorage.setItem('tts-config', JSON.stringify(config.value))
  }

  // 重置配置
  const resetConfig = () => {
    config.value = {
      voice: 'longwan',
      language: 'chinese',
      format: 'mp3',
      speed: 1.0,
      volume: 1.0,
      enabled: true
    }
    localStorage.setItem('tts-config', JSON.stringify(config.value))
    message.success('配置已重置')
  }

  // 初始化
  const init = async () => {
    try {
      // 从localStorage加载配置
      const savedConfig = localStorage.getItem('tts-config')
      if (savedConfig) {
        try {
          const parsed = JSON.parse(savedConfig)
          config.value = { ...config.value, ...parsed }
          ttsEnabled.value = config.value.enabled
        } catch (e) {
          console.error('Failed to parse saved TTS config:', e)
        }
      }

      // 获取支持的音色
      await fetchSupportedVoices()

      // 检查服务状态
      await checkServiceStatus()

    } catch (err) {
      console.error('Failed to initialize TTS:', err)
    }
  }

  // 清理资源
  const cleanup = () => {
    if (currentAudioUrl.value && currentAudioUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(currentAudioUrl.value)
      currentAudioUrl.value = ''
    }
    if (currentDownloadUrl.value && currentDownloadUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(currentDownloadUrl.value)
      currentDownloadUrl.value = ''
    }
  }

  // 监听配置变化
  watch(config, (newConfig) => {
    localStorage.setItem('tts-config', JSON.stringify(newConfig))
  }, { deep: true })

  return {
    // 状态
    isLoading,
    isGenerating,
    currentAudioUrl,
    currentDownloadUrl,
    ttsEnabled,
    error,
    config,
    supportedVoices,
    currentVoiceLabel,
    mockMode,

    // 方法
    generateSpeech,
    generateSpeechStream,
    fetchSupportedVoices,
    checkServiceStatus,
    cleanupTempFiles,
    downloadAudio,
    updateConfig,
    toggleTTS,
    resetConfig,
    init,
    cleanup
  }
}