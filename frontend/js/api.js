/**
 * Centralized API Client with Fetch Wrapper and Interceptor
 */
const api = {
  /**
   * Helper to perform HTTP requests
   */
  async request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${CONFIG.API_BASE_URL}${endpoint}`;
    
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...options.headers
    };

    let token = null;
    try {
      if (typeof Auth !== 'undefined' && typeof Auth.getToken === 'function') {
        token = Auth.getToken();
      } else {
        token = localStorage.getItem('amb_auth_token');
      }
    } catch (e) {}

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);

      // Handle 401 Unauthorized
      if (response.status === 401) {
        const errorData = await response.json().catch(() => null);
        const errorMsg = errorData && (errorData.message || errorData.error) 
          ? (errorData.message || errorData.error) 
          : 'Invalid email or password';

        // Only treat as expired session if this is NOT a login attempt
        if (!endpoint.includes('/auth/login')) {
          console.warn('Session expired or unauthorized request to:', endpoint);
          if (typeof Auth !== 'undefined' && Auth.isAuthenticated()) {
            if (typeof UI !== 'undefined' && typeof UI.showToast === 'function') {
              UI.showToast('Your session has expired. Please sign in again.', 'warning');
            }
            Auth.logout();
          }
        }
        throw new Error(errorMsg);
      }

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const errorMsg = data && (data.message || data.error) ? (data.message || data.error) : `Request failed with status ${response.status}`;
        throw new Error(errorMsg);
      }

      // If wrapped in standard ApiResponse envelope
      if (data && typeof data === 'object' && 'success' in data && 'data' in data) {
        return data.data;
      }

      return data;
    } catch (err) {
      console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, err);
      throw err;
    }
  },

  get(endpoint, params = null) {
    let url = endpoint;
    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== '') {
          searchParams.append(key, val);
        }
      });
      const qs = searchParams.toString();
      if (qs) {
        url += (url.includes('?') ? '&' : '?') + qs;
      }
    }
    return this.request(url, { method: 'GET' });
  },

  post(endpoint, data = {}) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  put(endpoint, data = {}) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
};
