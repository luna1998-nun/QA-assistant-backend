import os
import time
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

# 模拟 Java 后端的路径结构: /api/ai/tts/speak
@app.get("/api/ai/tts/speak")
async def tts_generate(text: str):
    print(f"收到请求: {text}")
    try:
        # 模拟处理时间
        time.sleep(1)

        # 返回一个简单的音频文件（这里是模拟，实际应该返回真实的音频数据）
        # 为了测试，我们返回一个最小的WAV文件头
        # 这是一个44字节的WAV文件头，表示1秒的静音
        wav_header = bytes([
            0x52, 0x49, 0x46, 0x46,  # "RIFF"
            0x24, 0x08, 0x00, 0x00,  # 文件大小 - 8
            0x57, 0x41, 0x56, 0x45,  # "WAVE"
            0x66, 0x6d, 0x74, 0x20,  # "fmt "
            0x10, 0x00, 0x00, 0x00,  # fmt chunk size
            0x01, 0x00,              # PCM format
            0x01, 0x00,              # mono
            0x44, 0xac, 0x00, 0x00,  # sample rate 44100
            0x88, 0x58, 0x01, 0x00,  # byte rate
            0x02, 0x00,              # block align
            0x10, 0x00,              # bits per sample
            0x64, 0x61, 0x74, 0x61,  # "data"
            0x00, 0x08, 0x00, 0x00   # data size
        ])

        # 添加静音数据
        silence_data = bytes([0] * (44100 * 1))  # 1秒的静音
        audio_data = wav_header + silence_data

        return Response(content=audio_data, media_type="audio/wav")
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    return {"status": "ok", "message": "TTS service is running"}

if __name__ == "__main__":
    # 运行在 8001 端口
    uvicorn.run(app, host="0.0.0.0", port=8001)