import { describe, expect, it } from 'vitest';

import { parseCurrency } from './currency';

describe('parseCurrency', () => {
  it.each([
    ['1.234,56', 1234.56],
    ['1234.56', 1234.56],
    ['R$ 42,50', 42.5],
    ['', 0],
    ['inválido', 0]
  ])('parses %s as %s', (input, expected) => {
    expect(parseCurrency(input)).toBe(expected);
  });
});
