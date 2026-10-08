const DEFAULT_API_BASE_URL = 'http://localhost:8080/api';

declare global {
  interface Window {
    __APP_CONFIG__?: {
      apiBaseUrl?: string;
    };
  }
}

const nodeRuntime = globalThis as typeof globalThis & {
  process?: {
    env?: Record<string, string | undefined>;
  };
};

export function apiBaseUrl(): string {
  const configuredUrl =
    typeof window === 'undefined'
      ? nodeRuntime.process?.env?.['API_BASE_URL']
      : window.__APP_CONFIG__?.apiBaseUrl;

  return (configuredUrl || DEFAULT_API_BASE_URL).replace(/\/+$/, '');
}
