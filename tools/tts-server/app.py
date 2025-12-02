from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from melo.api import TTS
import os
from fastapi.responses import Response

app = FastAPI()

# 全局变量，用于存储模型和speaker_ids
model = None
speaker_ids = None

# 请求体模型：用于生成语音
class SpeechRequest(BaseModel):
    input: str
    speed: float = 1.0  # 调整语音的速度
    language: str = 'ZH'  # 语言代码，默认'ZH'表示中文

# 请求体模型：用于获取模型信息
class ModelRequest(BaseModel):
    model_uid: str  # 模型的唯一标识符

# 在服务启动时初始化模型和speaker_ids
@app.on_event("startup")
async def startup():
    global model, speaker_ids
    # 初始化TTS模型，使用默认设备'cuda:0'
    device = 'cuda:0'  # 默认设备
    model = TTS(language='ZH', device=device)  # 初始化模型，默认语言为中文
    speaker_ids = model.hps.data.spk2id  # 获取模型的speaker_ids


@app.get("/get_model")
async def get_model(request: ModelRequest):
    try:
        # 检查model_uid是否有效（假设MeloTTS模型的model_uid为"melotts"）
        if request.model_uid != "melotts":
            raise HTTPException(status_code=404, detail="模型未找到")

        # 返回MeloTTS模型的信息
        return {
            "model_uid": request.model_uid,
            "model_name": "MeloTTS",
            "description": "MeloTTS 文本转语音模型",
            "supported_languages": ["ZH", "EN"],  # 根据实际模型支持的语言进行调整
            "speaker_ids": list(speaker_ids.values()),  # 从初始化的模型获取speaker_ids
            "input_format": "text",
            "output_format": "audio"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取模型信息时出错: {str(e)}")

@app.post("/speech")
async def speech(request: SpeechRequest):
    try:
        # 检查请求的语言是否在speaker_ids中
        if request.language not in speaker_ids:
            raise HTTPException(status_code=400, detail="无效的语言或speaker ID")

        # 生成语音并保存为文件
        output_path = f'{request.language}_output.wav'
        model.tts_to_file(request.input, speaker_ids[request.language], output_path, speed=request.speed)

        # 检查语音文件是否成功生成
        if not os.path.exists(output_path):
            raise HTTPException(status_code=500, detail="语音生成失败")

        # 读取生成的音频文件并返回
        with open(output_path, 'rb') as f:
            audio_binary = f.read()

        # 返回音频文件作为响应
        return Response(content=audio_binary, media_type="audio/wav")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"生成语音时出错: {str(e)}")