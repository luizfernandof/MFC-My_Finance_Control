export function parseCurrency(value) {
  const normalized = String(value ?? '').trim().replace(/\s|R\$/gi, '');
  const decimal = normalized.includes(',')
    ? normalized.replace(/\./g, '').replace(',', '.')
    : normalized;
  const parsed = Number(decimal);
  return Number.isFinite(parsed) ? parsed : 0;
}
