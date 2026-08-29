export function getApiErrorMessage(error, fallback = 'Ocorreu um erro inesperado.') {
  return error?.response?.data?.detail
    || error?.response?.data?.message
    || fallback;
}

export async function getBlobApiErrorMessage(error, fallback = 'Ocorreu um erro inesperado.') {
  const data = error?.response?.data;

  if (data && typeof data.text === 'function' && data.type?.includes('json')) {
    try {
      const parsed = JSON.parse(await data.text());
      return parsed.detail || parsed.message || fallback;
    } catch {
      return fallback;
    }
  }

  return getApiErrorMessage(error, fallback);
}
