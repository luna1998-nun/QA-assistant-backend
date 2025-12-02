/**
 * Vite environment configuration types
 */

export interface ViteEnv {
  VITE_PORT: number;
  VITE_USE_MOCK: boolean;
  VITE_USE_PWA: boolean;
  VITE_PUBLIC_PATH: string;
  VITE_PROXY: string[];
  VITE_GLOB_APP_TITLE: string;
  VITE_DROP_CONSOLE: boolean;
  VITE_USE_HTTPS: boolean;
  VITE_USE_CDN: boolean;
  VITE_COMPRESSION: boolean;
  VITE_LEGACY: boolean;
  VITE_USE_IMAGEMIN: boolean;
  VITE_GENERATE_UI: boolean;
  VITE_USE_COMPRESS: boolean;
  VITE_GENERATE_SOURCEMAP: boolean;
  VITE_USE_COMPRESS_TYPE: 'gzip' | 'brotli' | 'none';
}