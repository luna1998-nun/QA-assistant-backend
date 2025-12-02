这里有一份根据你的特定环境（macOS + CPU + mise）和需求（无Java后端验证）定制的**MeloTTS前端直连验证开发计划**。

这份计划的核心策略是利用 **Vite 的 Proxy 代理功能**，将前端对 `/api/ai/tts` 的请求直接转发到本地运行的 Python 服务，从而绕过 Java 后端，实现纯前端+Python模型的调试。

-----

# 开发任务清单：MeloTTS 前端直连集成 (macOS/CPU/Mise)

## 1\. 环境准备 (Python Service)

**目标**：使用 `mise` 配置 Python 环境，并运行一个强制使用 CPU 的 MeloTTS 轻量级服务。

### 1.1 初始化 Python 环境

在项目根目录下执行以下fish命令：
```bash
# 创建独立工具目录
mkdir -p tools/tts-server
cd tools/tts-server

# 使用 mise 指定 Python 版本 (推荐 3.9 或 3.10)
mise use python@3.10
mise exec -- python -m venv .venv

# [关键修改] Fish shell 使用 activate.fish
source .venv/bin/activate.fish

# 安装依赖 (macOS CPU环境)
# 注意：MeloTTS 依赖 mecab，macOS 可能需要先 brew install mecab
pip install fastapi "uvicorn[standard]" melotts
python -m unidic download
```




### 1.2 创建 TTS 服务脚本

在 `tools/tts-server/main.py` 创建以下内容。
*注意：已配置 `device='cpu'` 且添加了 CORS 支持以防万一，同时模拟了 Java 后端的接口路径结构以免去 Vite 重写路径的麻烦。*

```python
import os
import uvicorn
from fastapi import FastAPI, HTTPException, Response
from fastapi.middleware.cors import CORSMiddleware
from melo.api import TTS

app = FastAPI()

# 允许跨域，方便本地调试
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 强制使用 CPU
device = 'cpu'
print(f"正在初始化 MeloTTS (Device: {device})...")
# 这里以中文为例，如需英文请改为 language='EN'
model = TTS(language='ZH', device=device)
speaker_ids = model.hps.data.spk2id
print("模型加载完毕")

# 模拟 Java 后端的路径结构: /api/ai/tts/speak
# 这样前端代码写好后，上线对接 Java 后端时无需修改 URL
@app.get("/api/ai/tts/speak")
async def tts_generate(text: str):
    print(f"收到请求: {text}")
    try:
        speaker_id = speaker_ids['ZH']
        temp_path = "temp_output.wav"
        
        # 生成音频 (CPU 速度较慢，请耐心等待)
        model.tts_to_file(text, speaker_id, temp_path, speed=1.0)
        
        with open(temp_path, "rb") as f:
            audio_data = f.read()
            
        if os.path.exists(temp_path):
            os.remove(temp_path)
            
        return Response(content=audio_data, media_type="audio/wav")
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    # 运行在 8001 端口
    uvicorn.run(app, host="0.0.0.0", port=8001)
```

## 2\. 前端配置 (Vite Proxy)

**目标**：修改 Vue 项目配置，拦截 TTS 请求并转发给 Python 服务，其他请求保持不变（或失败，因为没有 Java 后端）。

### 2.1 修改 `vite.config.ts`

文件路径：`QA-assistant-frontend/vite.config.ts`

在 `server.proxy` 配置块中添加针对 TTS 的特殊规则。**注意：这条规则必须放在通用的 `/api` 规则之前**。

```typescript
// ... 原有代码 ...
server: {
  // ...
  proxy: {
    // [新增] 优先拦截 TTS 请求转发给 Python 服务 (8001)
    '/api/ai/tts': {
      target: 'http://localhost:8001',
      changeOrigin: true,
      // Python脚本里已经写了完整路径 /api/ai/tts/speak，所以这里不需要 rewrite
    },
    // [原有] 其他 API 仍然指向 Java 后端 (8123) - 此时 Java 后端未启动，这些会报错，但不影响 TTS 测试
    '/api': {
      target: 'http://localhost:8123',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
},
// ... 原有代码 ...
```

## 3\. 前端功能实现 (Vue)

**目标**：在聊天窗口实现播放按钮和音频流播放逻辑。

### 3.1 修改 `ChatWindow.vue`

文件路径：`QA-assistant-frontend/src/views/assistant/components/ChatWindow.vue`

**Step A: 引入图标与 Message**

```typescript
// 在 script setup 顶部
import { SoundOutlined, LoadingOutlined } from '@vicons/antd';
import { useMessage } from 'naive-ui';

const message = useMessage();
```

**Step B: 扩展消息接口**

```typescript
interface Msg { 
  id: number; 
  role: 'user' | 'assistant'; 
  text: string;
  // ... 其他原有字段 ...
  isAudioLoading?: boolean; // 新增
  isPlaying?: boolean;      // 新增
}
```

**Step C: 添加播放逻辑**

```typescript
// 在 script setup 中添加
const audioPlayer = new Audio();
const currentPlayingMsgId = ref<number | null>(null);

const playAudio = (msg: Msg) => {
  // 1. 停止当前播放
  if (currentPlayingMsgId.value === msg.id) {
    audioPlayer.pause();
    audioPlayer.currentTime = 0;
    currentPlayingMsgId.value = null;
    msg.isPlaying = false;
    return;
  }
  
  // 停止其他正在播放的
  audioPlayer.pause();
  // 重置所有消息的播放状态 (如果需要更严谨的状态管理，可以遍历重置)
  if (currentPlayingMsgId.value !== null) {
     // 简单处理：实际项目中可能需要遍历 messages 重置 isPlaying
  }

  // 2. 开始新请求
  msg.isAudioLoading = true;
  
  // 清理 Markdown 符号，避免朗读干扰
  const cleanText = msg.text.replace(/[#*`\-]/g, '');
  
  // 这里的 URL 会被 vite.config.ts 代理到 localhost:8001
  const url = `/api/ai/tts/speak?text=${encodeURIComponent(cleanText)}`;
  
  audioPlayer.src = url;
  
  audioPlayer.oncanplay = () => {
    msg.isAudioLoading = false;
    msg.isPlaying = true;
    currentPlayingMsgId.value = msg.id;
    audioPlayer.play();
  };
  
  audioPlayer.onended = () => {
    msg.isPlaying = false;
    currentPlayingMsgId.value = null;
  };
  
  audioPlayer.onerror = () => {
    msg.isAudioLoading = false;
    msg.isPlaying = false;
    currentPlayingMsgId.value = null;
    console.error('Audio Error:', audioPlayer.error);
    message.error('语音生成失败，请检查 Python 服务是否启动');
  };
};

// 组件销毁时清理
import { onUnmounted } from 'vue';
onUnmounted(() => {
  audioPlayer.pause();
  audioPlayer.src = '';
});
```

**Step D: 修改 Template 添加按钮**
找到 `.message-header` 部分，修改如下：

```html
<div class="message-header">
  <span class="message-role">{{ msg.role === 'user' ? '用户' : 'OCEM AI助手' }}</span>
  
  <n-button 
    v-if="msg.role === 'assistant' && !msg.isTyping" 
    text 
    size="tiny" 
    @click="playAudio(msg)"
    style="margin-left: 8px; vertical-align: middle;"
  >
    <template #icon>
      <n-icon>
        <LoadingOutlined v-if="msg.isAudioLoading" />
        <SoundOutlined v-else :class="{'text-[#66c2ff]': msg.isPlaying}" />
      </n-icon>
    </template>
    {{ msg.isPlaying ? '停止' : (msg.isAudioLoading ? '生成中...' : '朗读') }}
  </n-button>

  </div>
```

## 4\. 验证与运行流程

1.  **启动 Python 服务** (Terminal 1):

    ```bash
    cd tools/tts-server
    # Fish 下激活虚拟环境
    source .venv/bin/activate.fish
    python main.py
    # 确保看到 "Uvicorn running on http://0.0.0.0:8001"
    ```

2.  **启动前端** (Terminal 2):

    ```bash
    cd QA-assistant-frontend
    pnpm dev
    ```

3.  **测试**：

      * 打开浏览器访问前端页面。
      * 由于 Java 后端没启动，页面初始加载可能会报 API 错误（获取历史记录等失败），请忽略。
      * **关键点**：如果页面没有历史消息，你可能需要手动在前端代码里 hardcode 一条测试消息到 `messages` 数组中，或者如果你有本地存储的历史记录。
      * 点击那条 AI 消息旁边的“朗读”按钮。
      * 观察 Python 终端是否有日志输出 (`收到请求: ...`)。
      * 等待几秒（macOS CPU 推理需要时间），听到声音即成功。

5. 后续对接 (Java 后端启动后)
当你验证完毕，准备正式集成 Java 后端时：

保留 Python 服务，可以将其 Docker 化或后台运行。

恢复 vite.config.ts：删除 /api/ai/tts 的代理配置。

启用 Java 端代码：按照我之前提供的 TtsController.java 方案，在 Java 端实现 /ai/tts/speak 接口来转发请求。

前端代码：无需任何修改，因为 Java 接口路径和我们模拟的 Python 路径是一致的。