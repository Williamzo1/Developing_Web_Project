export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || 'Không thể kết nối với dịch vụ. Vui lòng thử lại.');
    this.status = status;
    this.code = body?.code;
    this.fieldErrors = body?.fieldErrors || {};
  }
}

export async function api(path, { token, method = 'GET', body, signal } = {}) {
  let response;
  try {
    response = await fetch(path, {
      method,
      headers: {
        Accept: 'application/json',
        ...(token && { Authorization: 'Bearer ' + token }),
        ...(body !== undefined && { 'Content-Type': 'application/json' }),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError(0);
  }
  if (!response.ok) throw new ApiError(response.status, await response.json().catch(() => null));
  if (response.status !== 204) return response.json();
}
