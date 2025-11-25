export default [
  // TTS服务状态检查
  {
    url: '/api/tts/status',
    method: 'get',
    response: () => {
      return {
        success: true,
        data: {
          status: 'running',
          version: '1.0.0',
          supportedFormats: ['mp3', 'wav', 'ogg'],
          maxTextLength: 5000
        },
        message: 'TTS服务运行正常'
      }
    }
  },

  // 获取支持的音色
  {
    url: '/api/tts/voices',
    method: 'get',
    response: () => {
      return {
        success: true,
        data: {
          chinese: ['longwan', 'xiaoqing', 'xiaomeng', 'xiaoxue', 'xiaoyun'],
          english: ['anna', 'brian', 'cathy', 'david', 'emily'],
          japanese: ['haruka', 'hikari', 'kaori', 'mai', 'nana'],
          korean: ['jihoon', 'sujin', 'yuna', 'minjun', 'hyejin'],
          cantonese: ['xiaomin', 'xiaofen', 'xiaoya', 'xiaoying', 'xiaomei']
        },
        message: '获取音色成功'
      }
    }
  },

  // 文本转语音 - 文件模式
  {
    url: '/api/tts/convert/file',
    method: 'post',
    timeout: 1000, // 模拟网络延迟
    response: ({ body }) => {
      const { text, voice, language } = body as any

      if (!text || text.trim().length === 0) {
        return {
          success: false,
          message: '文本内容不能为空'
        }
      }

      if (text.length > 5000) {
        return {
          success: false,
          message: '文本内容过长，请控制在5000字符以内'
        }
      }

      // 生成模拟文件路径
      const timestamp = Date.now()
      const filename = `tts_${voice}_${timestamp}.mp3`
      const filePath = `/tmp/tts/${filename}`

      return {
        success: true,
        filePath: filePath, // 直接返回filePath，符合TTSResponse接口
        message: '语音生成成功'
      }
    }
  },

  // 文本转语音 - 流模式 (简化返回base64音频数据)
  {
    url: '/api/tts/convert/stream',
    method: 'post',
    timeout: 1000, // 模拟网络延迟
    response: ({ body }) => {
      const { text, voice, language } = body as any

      if (!text || text.trim().length === 0) {
        return {
          success: false,
          message: '文本内容不能为空'
        }
      }

      // 返回模拟的base64音频数据 (非常短的静音MP3)
      const mockAudioBase64 = 'SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWGluZwAAAA8AAAACAAAEaAAzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMz//////////////////////////////////////////////////////////////////8AAAAATGF2YzU4LjEzAAAAAAAAAAAAAAAAJAQKAAAAAAAADrABXFU3AAAAAAgAA0AAAAgAA0AAAAgAA0AAAAgAA0AAAQAAAAgAA0AAAAgAA0AAAAgAA0AAAQAA'

      return {
        success: true,
        audioData: mockAudioBase64,
        message: '语音生成成功'
      }
    }
  },

  // 下载音频文件
  {
    url: '/api/tts/download',
    method: 'get',
    response: ({ query }) => {
      const { filePath } = query as any

      if (!filePath) {
        return {
          success: false,
          message: '文件路径不能为空'
        }
      }

      // 返回模拟的base64音频数据
      const mockAudioBase64 = 'SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWGluZwAAAA8AAAACAAAEaAAzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMz//////////////////////////////////////////////////////////////////8AAAAATGF2YzU4LjEzAAAAAAAAAAAAAAAAJAQKAAAAAAAADrABXFU3AAAAAAgAA0AAAAgAA0AAAAgAA0AAAAgAA0AAAQAAAAgAA0AAAAgAA0AAAAgAA0AAAQAA'

      return new Response(Uint8Array.from(atob(mockAudioBase64), c => c.charCodeAt(0)), {
        status: 200,
        headers: {
          'Content-Type': 'audio/mpeg',
          'Content-Disposition': `attachment; filename="${filePath.split('/').pop() || 'tts_audio.mp3'}"`,
        }
      })
    }
  },

  // 清理临时文件
  {
    url: '/api/tts/cleanup',
    method: 'post',
    response: ({ query }) => {
      const { hoursOld } = query as any

      const hours = parseInt(hoursOld) || 24
      const deletedCount = Math.floor(Math.random() * 10) + 1 // 模拟删除了1-10个文件

      return {
        success: true,
        data: {
          deletedCount: deletedCount,
          totalSize: deletedCount * 1024 // 模拟文件大小
        },
        message: `清理完成，删除了${hours}小时前的${deletedCount}个临时文件`
      }
    }
  }
]