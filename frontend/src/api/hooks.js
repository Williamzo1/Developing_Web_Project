import { useEffect, useRef, useState } from 'react';
import { api } from './client';

export function useApi(path, token) {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState(null);
  useEffect(() => {
    if (!path) return;
    const controller = new AbortController();
    const request = { path, token, attempt };
    api(path, { token, signal: controller.signal }).then(data => {
      if (!controller.signal.aborted) setResult({ ...request, data });
    }).catch(error => {
      if (!controller.signal.aborted) setResult({ ...request, error, missing: error.status === 404 });
    });
    return () => controller.abort();
  }, [path, token, attempt]);

  return {
    current: result && result.path === path && result.token === token && result.attempt === attempt ? result : null,
    retry: () => setAttempt(value => value + 1),
  };
}

export function useMutation(token, onSuccess) {
  const locked = useRef(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState(null);

  async function submit(path, method, body) {
    if (locked.current) return false;
    locked.current = true;
    setPending(true);
    setError(null);
    try {
      await api(path, { token, method, body });
      onSuccess?.();
      return true;
    } catch (failure) {
      setError(failure);
      return false;
    } finally {
      locked.current = false;
      setPending(false);
    }
  }

  return { submit, pending, error };
}
