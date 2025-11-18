import { ref } from 'vue';
// @ts-ignore
import Recorder from 'recorder-core';
// 引入 wav 格式支持 (必须)
import 'recorder-core/src/engine/wav.js';

export function useFunASR() {
  const isRecording = ref(false);
  const resultText = ref('');
  let socket: WebSocket | null = null;
  let rec: any = null;

  // FunASR 服务配置
  const WS_URL = 'ws://127.0.0.1:10095'; 
  
  // 启动录音和识别
  const startRecording = () => {
    resultText.value = '';
    
    // 1. 初始化 WebSocket
    socket = new WebSocket(WS_URL);
    socket.binaryType = 'arraybuffer';

    socket.onopen = () => {
      console.log('FunASR Connected');
      // 发送配置帧 (握手)
      const config = {
        mode: "2pass", // 实时流式 + 句尾修正
        chunk_size: [5, 10, 5],
        encoder_chunk_look_back: 4,
        decoder_chunk_look_back: 1,
        wav_name: "microphone",
        is_speaking: true
      };
      socket?.send(JSON.stringify(config));
      
      // 2. WebSocket 连接成功后，启动录音机
      startRecorder();
    };

    socket.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data);
        // 实时更新识别结果
        if (data.text) {
          resultText.value = data.text;
        }
      } catch (error) {
        console.error('Parse error:', error);
      }
    };

    socket.onerror = (e) => {
      console.error('WebSocket Error:', e);
      stopRecording();
    };
  };

  const startRecorder = () => {
    // 配置录音机：16000hz, 16位, 单声道 (FunASR 要求)
    rec = Recorder({
      type: "wav",
      sampleRate: 16000,
      bitRate: 16,
      onProcess: (buffers: any, powerLevel: any, bufferDuration: any, bufferSampleRate: any, newBufferIdx: any, asyncEnd: any) => {
        // 实时处理音频片段
        if (socket && socket.readyState === WebSocket.OPEN) {
          // 获取新增的 PCM 数据
          const chunk = Recorder.SampleData(buffers, bufferSampleRate, 16000, rec.mock(newBufferIdx));
          // 发送二进制数据
          socket.send(chunk.data);
        }
      }
    });

    rec.open(() => {
      rec.start();
      isRecording.value = true;
      console.log("Recording started");
    }, (msg: string, isUserNotAllow: boolean) => {
      console.error((isUserNotAllow ? "User denied permission" : "Recorder open failed") + ":" + msg);
    });
  };

  // 停止录音
  const stopRecording = () => {
    if (rec) {
      rec.stop((blob: Blob, duration: number) => {
        console.log("Recorder stopped", duration);
        rec.close();
        rec = null;
      });
    }
    
    if (socket) {
      // 发送结束标志 (可选，视服务端逻辑而定，FunASR Python demo 中通常直接断开或发 is_speaking: false)
      if(socket.readyState === WebSocket.OPEN) {
         socket.send(JSON.stringify({ is_speaking: false }));
      }
      socket.close();
      socket = null;
    }
    isRecording.value = false;
  };

  return {
    isRecording,
    resultText,
    startRecording,
    stopRecording
  };
}