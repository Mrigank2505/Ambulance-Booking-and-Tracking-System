/**
 * Authentication & Session Management Module
 */
const Auth = {
  TOKEN_KEY: 'amb_auth_token',
  USER_KEY: 'amb_auth_user',

  getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  getUser() {
    try {
      const data = localStorage.getItem(this.USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  },

  isAuthenticated() {
    return !!this.getToken() && !!this.getUser();
  },

  hasRole(role) {
    const user = this.getUser();
    return user && user.role === role;
  },

  setUser(user) {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  },

  setSession(token, user) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  },

  clearSession() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    try {
      sessionStorage.clear();
    } catch (e) {}
  },

  async login(identifier, password) {
    try {
      const cleanIdentifier = identifier ? identifier.trim() : '';
      const res = await api.post('/auth/login', { 
        email: cleanIdentifier, 
        username: cleanIdentifier, 
        password: password 
      });
      this.setSession(res.token, res.user);
      return res.user;
    } catch (err) {
      if (!err.message || err.message.includes('Failed to fetch') || err.message.includes('NetworkError')) {
        throw new Error('Unable to connect to the authentication server. Please check backend connection.');
      }
      throw err;
    }
  },

  async register(registrationData) {
    const res = await api.post('/auth/register', registrationData);
    return res;
  },

  logout() {
    this.clearSession();
    window.location.replace('login.html');
  },

  redirectAfterLogin(defaultUrl = null) {
    const params = new URLSearchParams(window.location.search);
    const redirect = params.get('redirect');
    if (redirect && !redirect.toLowerCase().includes('login.html')) {
      window.location.replace(redirect);
      return;
    }

    const user = this.getUser();
    if (user && user.role === 'ADMIN') {
      window.location.replace('admin-dashboard.html');
    } else if (user && user.role === 'DRIVER') {
      window.location.replace('driver-dashboard.html');
    } else {
      window.location.replace(defaultUrl || 'dashboard.html');
    }
  },

  redirectBasedOnRole() {
    const user = this.getUser();
    if (!user) {
      window.location.replace('login.html');
      return;
    }
    if (user.role === 'ADMIN') {
      window.location.replace('admin-dashboard.html');
    } else if (user.role === 'DRIVER') {
      window.location.replace('driver-dashboard.html');
    } else {
      window.location.replace('dashboard.html');
    }
  },

  requireAuth(allowedRoles = null) {
    if (!this.isAuthenticated()) {
      try {
        if (document.body) {
          document.body.style.display = 'none';
        }
      } catch (e) {}
      const pageName = window.location.pathname.split('/').pop() || 'index.html';
      const redirectTarget = pageName.startsWith('login') ? 'index.html' : pageName;
      window.location.replace('login.html?redirect=' + encodeURIComponent(redirectTarget + window.location.search));
      return false;
    }

    if (allowedRoles && allowedRoles.length > 0) {
      const user = this.getUser();
      const hasPermission = allowedRoles.includes(user.role);
      if (!hasPermission) {
        UI.showToast('Access denied for your role: ' + (user ? user.role : 'Unknown'), 'error');
        this.redirectBasedOnRole();
        return false;
      }
    }

    return true;
  }
};
