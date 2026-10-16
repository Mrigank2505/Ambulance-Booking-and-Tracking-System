/**
 * UI Utilities, Components, and Renderers
 */
const UI = {
  /**
   * Display a floating toast alert
   */
  showToast(message, type = 'info', duration = 4000) {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const typeLabel = type === 'success' ? 'Success' : type === 'error' ? 'Notice' : type === 'warning' ? 'Alert' : 'Info';
    toast.innerHTML = `
      <div style="flex: 1; word-break: break-word;">
        <strong style="display: block; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 2px;">${typeLabel}</strong>
        ${message}
      </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  },

  /**
   * Show / hide full-screen loading spinner
   */
  showLoading(show = true, message = 'Processing request...') {
    let overlay = document.getElementById('spinner-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'spinner-overlay';
      overlay.className = 'spinner-overlay';
      overlay.innerHTML = `
        <div class="spinner"></div>
        <p id="spinner-msg" style="font-weight: 600; color: #1e3a8a;"></p>
      `;
      document.body.appendChild(overlay);
    }
    const msgEl = document.getElementById('spinner-msg');
    if (msgEl) msgEl.textContent = message;

    if (show) {
      overlay.classList.add('show');
    } else {
      overlay.classList.remove('show');
    }
  },

  /**
   * Toggle button loading state
   */
  setButtonLoading(button, isLoading, loadingText = 'Please wait...') {
    if (!button) return;
    if (isLoading) {
      button.dataset.originalText = button.innerHTML;
      button.disabled = true;
      button.innerHTML = `<span class="spinner" style="width: 16px; height: 16px; border-width: 2px; display: inline-block; vertical-align: middle; margin-right: 6px;"></span> ${loadingText}`;
    } else {
      button.disabled = false;
      if (button.dataset.originalText) {
        button.innerHTML = button.dataset.originalText;
      }
    }
  },

  /**
   * Confirmation modal
   */
  showConfirm(title, message, onConfirm) {
    let modal = document.getElementById('confirm-modal');
    if (!modal) {
      modal = document.createElement('div');
      modal.id = 'confirm-modal';
      modal.className = 'modal-backdrop';
      modal.innerHTML = `
        <div class="modal">
          <div class="modal-header">
            <h3 class="card-title" id="confirm-title">Confirm Action</h3>
            <button class="close-btn" onclick="document.getElementById('confirm-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body" id="confirm-body">Are you sure?</div>
          <div class="modal-footer">
            <button class="btn btn-outline btn-sm" onclick="document.getElementById('confirm-modal').classList.remove('open')">Cancel</button>
            <button class="btn btn-danger btn-sm" id="confirm-ok-btn">Confirm</button>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }

    document.getElementById('confirm-title').textContent = title;
    document.getElementById('confirm-body').textContent = message;
    const okBtn = document.getElementById('confirm-ok-btn');

    okBtn.onclick = () => {
      modal.classList.remove('open');
      if (typeof onConfirm === 'function') onConfirm();
    };

    modal.classList.add('open');
  },

  /**
   * Format status badge HTML
   */
  formatStatusBadge(status) {
    if (!status) return `<span class="badge">N/A</span>`;
    const cleanStatus = status.toLowerCase().replace(/_/g, ' ');
    const cls = status.toLowerCase();
    return `<span class="badge badge-${cls}">${cleanStatus}</span>`;
  },

  /**
   * Format emergency severity badge
   */
  formatEmergencyBadge(severity) {
    if (!severity) return `<span class="badge">NORMAL</span>`;
    const cls = severity.toLowerCase();
    return `<span class="badge badge-${cls}">${severity}</span>`;
  },

  /**
   * Format ISO Date
   */
  formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString(undefined, {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  },

  /**
   * Render universal Navigation Bar across pages
   * Navigation order:
   * 1. Home
   * 2. Book Ambulance
   * 3. Track Location
   * 4. Hospitals
   * 5. Booking History
   * 6. Contact
   * 7. Login / Register
   */
  /**
   * Universal Dynamic Navigation Bar
   * Required navigation items:
   * 1. Home
   * 2. Track Location
   * 3. Booking History
   * 4. Alerts
   */
  renderNavbar(activePage = '') {
    const navPlaceholder = document.getElementById('navbar-container');
    if (!navPlaceholder) return;

    const user = Auth.getUser();
    const isAuth = Auth.isAuthenticated();

    let roleLinkHtml = '';
    let authNavHtml = '';
    let actionsHtml = '';

    if (isAuth && user) {
      if (user.role === 'ADMIN') {
        roleLinkHtml = `<li><a href="admin-dashboard.html" class="nav-link ${activePage.startsWith('admin') ? 'active' : ''}">Admin Control</a></li>`;
      } else if (user.role === 'DRIVER') {
        roleLinkHtml = `<li><a href="driver-dashboard.html" class="nav-link ${activePage === 'driver-dashboard' ? 'active' : ''}">Driver Cockpit</a></li>`;
      }
      authNavHtml = `
        ${roleLinkHtml}
      `;
      actionsHtml = `
        <button type="button" id="btn-nav-logout" onclick="Auth.logout()" class="btn btn-sm btn-outline btn-logout" style="color: #dc2626; border-color: #fca5a5; font-weight: 600;">Logout</button>
      `;
    } else {
      authNavHtml = `
        <li><a href="login.html" class="nav-link ${activePage === 'login' || activePage === 'admin-login' || activePage === 'driver-login' ? 'active' : ''}">Login</a></li>
      `;
      actionsHtml = `
        <a href="login.html" class="btn btn-primary btn-sm ${activePage === 'login' ? 'active' : ''}">Sign In</a>
      `;
    }

    const navLinksHtml = `
      <li><a href="index.html" class="nav-link ${activePage === 'home' ? 'active' : ''}">Home</a></li>
      <li><a href="tracking.html" class="nav-link ${activePage === 'tracking' ? 'active' : ''}">Track Location</a></li>
      <li><a href="history.html" class="nav-link ${activePage === 'history' ? 'active' : ''}">Booking History</a></li>
      <li><a href="notifications.html" class="nav-link ${activePage === 'notifications' ? 'active' : ''}">Alerts <span id="nav-unread-badge" style="display: none; background: #dc2626; color: white; border-radius: 99px; font-size: 0.65rem; padding: 1px 5px; margin-left: 2px;">0</span></a></li>
      ${authNavHtml}
    `;

    navPlaceholder.innerHTML = `
      <nav class="navbar">
        <div class="container nav-container">
          <a href="index.html" class="brand" aria-label="RESQCARE Home">
            <span class="brand-text">RESQ<span>CARE</span></span>
          </a>

          <ul class="nav-links" id="nav-menu">
            ${navLinksHtml}
          </ul>

          <div class="nav-actions">
            <span style="font-size: 0.775rem; color: #dc2626; font-weight: 700; display: inline-flex; align-items: center; gap: 0.25rem; margin-right: 0.25rem;">
              <span style="display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #dc2626;"></span>
              108
            </span>
            ${actionsHtml}
            <button class="mobile-toggle" id="mobile-menu-toggle" aria-label="Toggle navigation menu" onclick="UI.toggleMobileMenu()">Menu</button>
          </div>
        </div>
      </nav>
    `;

    NotificationsManager.updateUnreadCount();
  },

  /**
   * Mobile navigation drawer toggle (Text-based, no icon)
   */
  toggleMobileMenu() {
    const menu = document.getElementById('nav-menu');
    const toggleBtn = document.getElementById('mobile-menu-toggle');
    if (!menu) return;
    menu.classList.toggle('open');
    if (toggleBtn) {
      toggleBtn.textContent = menu.classList.contains('open') ? 'Close' : 'Menu';
    }
  }
};
