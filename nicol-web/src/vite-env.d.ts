declare module 'sse.js';

interface ImportMetaEnv {
  readonly VITE_BACKEND_API: string;
  readonly VITE_BACKEND_PROXY_URL: string | null;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
