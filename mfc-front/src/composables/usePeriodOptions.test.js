import { describe, expect, it } from 'vitest';

import { months, years } from './usePeriodOptions';

describe('period options', () => {
  it('contains every month and a moving 16-year range', () => {
    const currentYear = new Date().getFullYear();
    expect(months).toHaveLength(12);
    expect(months[0]).toEqual({ value: 1, label: 'Janeiro' });
    expect(months[11]).toEqual({ value: 12, label: 'Dezembro' });
    expect(years).toHaveLength(16);
    expect(years[0]).toBe(currentYear + 10);
    expect(years.at(-1)).toBe(currentYear - 5);
  });
});
