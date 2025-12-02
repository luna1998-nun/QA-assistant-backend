import os
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

# 简单的测试响应
@app.get("/api/ai/tts/speak")
async def tts_generate(text: str):
    """
    简化版 TTS 服务 - 仅返回测试音频
    这是一个占位符实现，真正的 TTS 功能需要解决 MeCab 字典问题
    """
    print(f"收到 TTS 请求: {text}")

    try:
        # 创建一个简单的 WAV 文件头（44字节）+ 简单的音频数据
        # 这是一个 1 秒的静音音频文件
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

        # 生成简单的音频数据（静音）
        audio_data = wav_header + bytes([0] * 8000)  # 1秒的静音

        return Response(content=audio_data, media_type="audio/wav")

    except Exception as e:
        print(f"生成音频时出错: {e}")
        raise HTTPException(status_code=500, detail=f"生成音频失败: {str(e)}")

if __name__ == "__main__":
    print("启动简化版 TTS 服务器...")
    print("注意：这是一个测试服务器，只返回静音音频")
    print("要使用真正的 TTS 功能，需要先解决 MeCab unidic 字典问题")
    # 运行在 8001 端口
    uvicorn.run(app, host="0.0.0.0", port=8001)