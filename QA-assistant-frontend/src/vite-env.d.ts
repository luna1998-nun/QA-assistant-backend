/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_AI_PROVIDER: string
  readonly VITE_DEEPSEEK_API_KEY: string
  readonly VITE_DEEPSEEK_BASE_URL: string
  readonly VITE_DEEPSEEK_MODEL: string
  readonly VITE_DEEPSEEK_TEMPERATURE: string
  readonly VITE_DEEPSEEK_MAX_TOKENS: string
  readonly VITE_OLLAMA_BASE_URL: string
  readonly VITE_OLLAMA_MODEL: string
  readonly VITE_OLLAMA_TEMPERATURE: string
  readonly VITE_OLLAMA_MAX_TOKENS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

