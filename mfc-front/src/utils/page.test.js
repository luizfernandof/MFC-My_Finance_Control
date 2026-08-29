import { describe, expect, it } from 'vitest';
import { parsePageResponse } from './page';

describe('parsePageResponse', () => {
  it('normalizes the Spring paged model response', () => {
    expect(parsePageResponse({
      content: [{ id: 1 }],
      page: { totalElements: 12, totalPages: 2 }
    })).toEqual({
      content: [{ id: 1 }],
      totalElements: 12,
      totalPages: 2
    });
  });

  it('returns an empty page for an invalid response', () => {
    expect(parsePageResponse(null)).toEqual({ content: [], totalElements: 0, totalPages: 0 });
  });
});
