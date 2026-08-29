export function parsePageResponse(data) {
  const response = data || {};
  const metadata = response.page || response;

  return {
    content: Array.isArray(response.content) ? response.content : [],
    totalElements: Number(metadata.totalElements) || 0,
    totalPages: Number(metadata.totalPages) || 0
  };
}
