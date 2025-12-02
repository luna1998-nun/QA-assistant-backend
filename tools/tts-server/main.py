import os
import sys


# [关键修复] 1. 在导入任何 HuggingFace 相关库之前设置镜像源
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"

# [关键修复] 强制禁用 MPS，避免 Apple Silicon GPU 问题
os.environ["PYTORCH_ENABLE_MPS_FALLBACK"] = "1"
os.environ["CUDA_VISIBLE_DEVICES"] = ""
os.environ["FORCE_CPU"] = "1"

# [关键修复] 2. 尝试禁用 SSL 验证以解决某些网络下的证书问题
import ssl
try:
    _create_unverified_https_context = ssl._create_unverified_context
except AttributeError:
    pass
else:
    ssl._create_default_https_context = _create_unverified_https_context

# [关键修复] 3. 预下载 NLTK 资源，避免运行时缺失错误
import nltk
try:
    nltk.download('averaged_perceptron_tagger_eng', quiet=True)
    nltk.download('cmudict', quiet=True)
except Exception as e:
    print(f"NLTK 数据预下载警告: {e}")


import uvicorn
from fastapi import FastAPI, HTTPException, Response
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# 允许跨域，方便本地调试
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化 TTS 模型和相关变量
model = None
speaker_ids = None

def initialize_tts():
    """安全地初始化 TTS 模型"""
    global model, speaker_ids

    # 禁用 MeCab 以避免 unidic 字典问题
    os.environ['MECab_PATH'] = '/dev/null'

    try:
        from melo.api import TTS

        # 强制使用 CPU
        device = 'cpu'
        print(f"正在初始化 MeloTTS (Device: {device})...")
        model = TTS(language='ZH', device=device)
        speaker_ids = model.hps.data.spk2id
        print("模型加载完毕")
        return True

    except Exception as e:
        print(f"TTS 初始化失败: {e}")
        print("服务器将在降级模式下运行")
        return False

# 在启动时尝试初始化 TTS
tts_available = initialize_tts()

# 如果 TTS 初始化失败，启用降级模式
if not tts_available:
    print("⚠️  TTS 服务运行在降级模式，将返回静音音频")

# 模拟 Java 后端的路径结构: /api/ai/tts/speak
# 这样前端代码写好后，上线对接 Java 后端时无需修改 URL
@app.get("/api/ai/tts/speak")
async def tts_generate(text: str):
    print(f"收到请求: {text}")

    # 检查 TTS 模型是否可用
    if model is None or speaker_ids is None:
        # 返回静音音频作为降级响应
        wav_header = bytes([
            0x52, 0x49, 0x46, 0x46,  # "RIFF"
            0x24, 0x08, 0x00, 0x00,  # 文件大小 - 8
            0x57, 0x41, 0x56, 0x45,  # "WAVE"
            0x66, 0x6D, 0x74, 0x20,  # "fmt "
            0x10, 0x00, 0x00, 0x00,  # fmt chunk size
            0x01, 0x00,              # PCM format
            0x01, 0x00,              # mono
            0x40, 0x1F, 0x00, 0x00,  # sample rate 8000
            0x40, 0x1F, 0x00, 0x00,  # byte rate
            0x01, 0x00,              # block align
            0x08, 0x00,              # bits per sample
            0x64, 0x61, 0x74, 0x61,  # "data"
            0x00, 0x08, 0x00, 0x00   # data size
        ])
        # 生成静音音频数据
        audio_data = wav_header + bytes([0] * 8000)  # 1秒的静音
        return Response(content=audio_data, media_type="audio/wav")

    try:
        # 检查中文说话人是否可用
        if 'ZH' not in speaker_ids:
            raise HTTPException(status_code=500, detail="中文说话人不可用")

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
        raise HTTPException(status_code=500, detail=f"TTS 生成失败: {str(e)}")

if __name__ == "__main__":
    # 运行在 8001 端口
    uvicorn.run(app, host="0.0.0.0", port=8001)