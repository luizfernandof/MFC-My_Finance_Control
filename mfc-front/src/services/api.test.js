import axios from 'axios';
import AxiosMockAdapter from 'axios-mock-adapter';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import api, { clearAuthTokens } from './api';

describe('authentication interceptor', () => {
  let apiMock;
  let axiosMock;

  beforeEach(() => {
    clearAuthTokens();
    apiMock = new AxiosMockAdapter(api);
    axiosMock = new AxiosMockAdapter(axios);
  });

  afterEach(() => {
    apiMock.restore();
    axiosMock.restore();
    clearAuthTokens();
  });

  it('stores both rotated tokens and retries the original request', async () => {
    localStorage.setItem('accessToken', 'expired-access');
    localStorage.setItem('refreshToken', 'old-refresh');
    apiMock.onGet('/protected').replyOnce(401).onGet('/protected').reply(200, { ok: true });
    axiosMock.onPost('/api/auth/refresh', { refreshToken: 'old-refresh' }).reply(200, {
      accessToken: 'new-access',
      refreshToken: 'new-refresh'
    });

    const response = await api.get('/protected');

    expect(response.data).toEqual({ ok: true });
    expect(localStorage.getItem('accessToken')).toBe('new-access');
    expect(localStorage.getItem('refreshToken')).toBe('new-refresh');
    expect(apiMock.history.get).toHaveLength(2);
    expect(apiMock.history.get[1].headers.Authorization).toBe('Bearer new-access');
  });
});
