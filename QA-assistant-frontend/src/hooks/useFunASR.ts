import { ref } from 'vue';
// @ts-ignore
import Recorder from 'recorder-core';
import 'recorder-core/src/engine/pcm.js';

export function useFunASR() {
  const isRecording = ref(false);
  const resultText = ref('');
  
  // 新增：内部维护的两个文本缓冲
  let offline_text = ''; // 存已经确定的句子
  let online_text = '';  // 存正在说的句子
  
  let socket: WebSocket | null = null;
  let rec: any = null;
  let sampleBuf = new Int16Array(); 

  const WS_URL = 'ws://127.0.0.1:10095'; 

  const startRecording = () => {
    // 重置状态
    resultText.value = '';
    offline_text = '';
    online_text = '';
    sampleBuf = new Int16Array();

    socket = new WebSocket(WS_URL);
    socket.binaryType = 'arraybuffer';

    socket.onopen = () => {
      console.log('FunASR Connected');
      const config = {
        mode: "2pass",
        chunk_size: [5, 10, 5],
        chunk_interval: 10,
        encoder_chunk_look_back: 4,
        decoder_chunk_look_back: 1,
        wav_name: "microphone",
        is_speaking: true
      };
      socket?.send(JSON.stringify(config));
      startRecorder();
    };

    // ✅ 核心修改：处理消息拼接逻辑
    socket.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data);
        const text = data.text;
        const mode = data.mode; // '2pass-online' 或 '2pass-offline'

        if (!text) return;

        if (mode === '2pass-offline') {
          // 1. 如果是离线修正结果（一句话结束），追加到 offline_text
          // 并给句子加个标点或空格（可选，视模型输出而定）
          offline_text += text;
          online_text = ''; // 清空当前正在说的缓存
        } else {
          // 2. 如果是实时流结果，更新 online_text
          online_text += text;
        }

        // 3. 拼接展示：已确认的历史 + 正在说的当前句
        resultText.value = offline_text + online_text;

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
    rec = Recorder({
      type: "pcm",
      bitRate: 16,
      sampleRate: 16000,
      onProcess: (buffers: any, powerLevel: any, bufferDuration: any, bufferSampleRate: any, newBufferIdx: any, asyncEnd: any) => {
        if (socket && socket.readyState === WebSocket.OPEN) {
          const data_raw = buffers[buffers.length - 1];
          const array_raw = new Int16Array(data_raw); 
          const data_16k = Recorder.SampleData([array_raw], bufferSampleRate, 16000).data;

          const newBuf = new Int16Array(sampleBuf.length + data_16k.length);
          newBuf.set(sampleBuf);
          newBuf.set(data_16k, sampleBuf.length);
          sampleBuf = newBuf;

          const chunk_size = 960; 
          while (sampleBuf.length >= chunk_size) {
            const sendBuf = sampleBuf.slice(0, chunk_size);
            sampleBuf = sampleBuf.slice(chunk_size, sampleBuf.length);
            socket.send(sendBuf);
          }
        }
      }
    });

    rec.open(() => {
      rec.start();
      isRecording.value = true;
    }, (msg: string) => {
      console.error("Recorder open failed:", msg);
    });
  };

  const stopRecording = () => {
    if (rec) {
      rec.stop(() => {
        rec.close();
        rec = null;
      });
    }
    if (socket) {
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