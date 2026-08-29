import { describe, expect, it } from 'vitest';

import { getApiErrorMessage, getBlobApiErrorMessage } from './apiError';

describe('getApiErrorMessage', () => {
  it('prefers the Problem Detail message returned by the API', () => {
    expect(getApiErrorMessage({ response: { data: { detail: 'Categoria duplicada' } } }))
      .toBe('Categoria duplicada');
  });

  it('uses the fallback when the response has no public message', () => {
    expect(getApiErrorMessage(new Error('database password leaked'), 'Falha segura'))
      .toBe('Falha segura');
  });

  it('reads Problem Details returned as a blob by a download request', async () => {
    const problemBlob = {
      type: 'application/problem+json',
      text: async () => JSON.stringify({ detail: 'Não foi possível gerar o relatório.' })
    };

    await expect(getBlobApiErrorMessage({ response: { data: problemBlob } }, 'Falha segura'))
      .resolves.toBe('Não foi possível gerar o relatório.');
  });
});
