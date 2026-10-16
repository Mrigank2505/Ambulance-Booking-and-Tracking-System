/**
 * Ambulance Booking and Tracking System - Login Controller
 * Handles user, driver, and admin authentication, client-side validation,
 * server role verification, and authorized dashboard routing.
 */

let isSubmitting = false;
let currentRoleTab = 'user';

/**
 * Switch active role tab (User, Driver, Admin)
 */
function switchLoginRole(role) {
  currentRoleTab = role;

  const tabUser = document.getElementById('tab-btn-user');
  const tabDriver = document.getElementById('tab-btn-driver');
  const tabAdmin = document.getElementById('tab-btn-admin');

  const heading = document.getElementById('login-role-heading');
  const subheading = document.getElementById('login-role-subheading');
  const labelId = document.getElementById('label-identifier');
  const inputId = document.getElementById('login-identifier');
  const submitBtn = document.getElementById('btn-login');

  hideAlert();
  clearAllErrors();

  // Reset tab active states
  [tabUser, tabDriver, tabAdmin].forEach(btn => {
    if (btn) {
      btn.classList.remove('active');
      btn.setAttribute('aria-selected', 'false');
    }
  });

  if (role === 'admin') {
    if (tabAdmin) {
      tabAdmin.classList.add('active');
      tabAdmin.setAttribute('aria-selected', 'true');
    }
    if (heading) heading.textContent = 'Admin Login';
    if (subheading) subheading.textContent = 'Sign in to access fleet dispatch, hospital triage, and system oversight';
    if (labelId) labelId.textContent = 'Admin Email or Username';
    if (inputId) inputId.placeholder = 'admin@ambulance.com or admin';
    if (submitBtn) submitBtn.textContent = 'Sign In as Administrator';
  } else if (role === 'driver') {
    if (tabDriver) {
      tabDriver.classList.add('active');
      tabDriver.setAttribute('aria-selected', 'true');
    }
    if (heading) heading.textContent = 'Driver Login';
    if (subheading) subheading.textContent = 'Sign in to access your cockpit, active trip assignments, and route tracking';
    if (labelId) labelId.textContent = 'Driver ID, License, or Username';
    if (inputId) inputId.placeholder = 'driver.mike, DL-2024-8841, or email';
    if (submitBtn) submitBtn.textContent = 'Sign In to Driver Cockpit';
  } else {
    // Default: User
    currentRoleTab = 'user';
    if (tabUser) {
      tabUser.classList.add('active');
      tabUser.setAttribute('aria-selected', 'true');
    }
    if (heading) heading.textContent = 'User Sign-In';
    if (subheading) subheading.textContent = 'Sign in to book ambulances, track emergency rides, and view history';
    if (labelId) labelId.textContent = 'Email or Username';
    if (inputId) inputId.placeholder = 'e.g. john@example.com or john';
    if (submitBtn) submitBtn.textContent = 'Sign In to Account';
  }

  const regLink = document.getElementById('link-register-page');
  if (regLink) {
    regLink.href = `register.html?role=${role}`;
  }

  if (inputId) inputId.focus();
}

/**
 * Toggle password input visibility between password and text
 */
function togglePasswordVisibility() {
  const input = document.getElementById('login-password');
  const btn = document.getElementById('password-toggle-btn');
  if (!input || !btn) return;

  const isPassword = input.getAttribute('type') === 'password';
  if (isPassword) {
    input.setAttribute('type', 'text');
    btn.textContent = 'Hide';
    btn.setAttribute('aria-label', 'Hide password');
  } else {
    input.setAttribute('type', 'password');
    btn.textContent = 'Show';
    btn.setAttribute('aria-label', 'Show password');
  }
}

/**
 * Show field error
 */
function showFieldError(inputId, errorId, message) {
  const input = document.getElementById(inputId);
  const errorEl = document.getElementById(errorId);

  if (input) {
    input.classList.add('input-error');
    input.focus();
  }
  if (errorEl) {
    errorEl.textContent = message;
    errorEl.style.display = 'block';
  }
}

/**
 * Clear single field error
 */
function clearFieldError(inputId, errorId) {
  const input = document.getElementById(inputId);
  const errorEl = document.getElementById(errorId);

  if (input) input.classList.remove('input-error');
  if (errorEl) {
    errorEl.textContent = '';
    errorEl.style.display = 'none';
  }
}

/**
 * Clear all field errors
 */
function clearAllErrors() {
  clearFieldError('login-identifier', 'identifier-error');
  clearFieldError('login-password', 'password-error');
}

/**
 * Show alert banner
 */
function showAlert(message, type = 'danger') {
  const alertEl = document.getElementById('login-alert');
  if (!alertEl) return;
  alertEl.className = `login-alert alert-${type}`;
  alertEl.textContent = message;
  alertEl.style.display = 'block';
}

/**
 * Hide alert banner
 */
function hideAlert() {
  const alertEl = document.getElementById('login-alert');
  if (!alertEl) return;
  alertEl.style.display = 'none';
  alertEl.textContent = '';
}

/**
 * Toggle button loading state
 */
function setButtonLoading(loading, loadingText = 'Authenticating...') {
  const btn = document.getElementById('btn-login');
  if (!btn) return;

  if (loading) {
    btn.dataset.originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="login-spinner" aria-hidden="true"></span> ${loadingText}`;
  } else {
    btn.disabled = false;
    if (btn.dataset.originalText) {
      btn.innerHTML = btn.dataset.originalText;
    } else {
      btn.innerHTML = 'Sign In';
    }
  }
}

/**
 * Redirect user based on server-verified role
 */
function redirectByServerRole(role) {
  const params = new URLSearchParams(window.location.search);
  const redirectParam = params.get('redirect');

  if (role === 'ADMIN') {
    window.location.replace('admin-dashboard.html');
  } else if (role === 'DRIVER') {
    window.location.replace('driver-dashboard.html');
  } else {
    if (redirectParam && !redirectParam.toLowerCase().includes('login.html')) {
      window.location.replace(redirectParam);
    } else {
      window.location.replace('dashboard.html');
    }
  }
}

// Attach event listeners on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  // If user is already authenticated, route them to their authorized page
  if (typeof Auth !== 'undefined' && Auth.isAuthenticated()) {
    const user = Auth.getUser();
    if (user && user.role) {
      redirectByServerRole(user.role);
      return;
    }
  }

  // Check URL query parameters to auto-select tab (e.g. ?tab=driver or ?tab=admin)
  const params = new URLSearchParams(window.location.search);
  const requestedTab = params.get('tab');
  if (requestedTab === 'driver') {
    switchLoginRole('driver');
  } else if (requestedTab === 'admin') {
    switchLoginRole('admin');
  } else {
    switchLoginRole('user');
  }

  // Clear errors when user types
  const inputId = document.getElementById('login-identifier');
  const inputPass = document.getElementById('login-password');

  if (inputId) {
    inputId.addEventListener('input', () => clearFieldError('login-identifier', 'identifier-error'));
  }
  if (inputPass) {
    inputPass.addEventListener('input', () => clearFieldError('login-password', 'password-error'));
  }

  // Form submit listener
  const form = document.getElementById('login-form');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (isSubmitting) return;

      hideAlert();
      clearAllErrors();

      const rawIdentifier = inputId ? inputId.value : '';
      const cleanIdentifier = rawIdentifier.trim();
      const password = inputPass ? inputPass.value : '';

      let hasError = false;
      if (!cleanIdentifier) {
        showFieldError('login-identifier', 'identifier-error', 'Please enter your email or username.');
        hasError = true;
      }
      if (!password) {
        showFieldError('login-password', 'password-error', 'Please enter your password.');
        hasError = true;
      }
      if (hasError) return;

      isSubmitting = true;
      setButtonLoading(true, 'Verifying Credentials...');

      try {
        const user = await Auth.login(cleanIdentifier, password);

        // Server-side role check if user explicitly selected a restricted tab
        if (currentRoleTab === 'admin' && user.role !== 'ADMIN') {
          Auth.clearSession();
          isSubmitting = false;
          setButtonLoading(false);
          showAlert('Access Denied: Administrator role required for Command Center access.', 'danger');
          return;
        }

        if (currentRoleTab === 'driver' && user.role !== 'DRIVER' && user.role !== 'ADMIN') {
          Auth.clearSession();
          isSubmitting = false;
          setButtonLoading(false);
          showAlert('Access Denied: Certified Emergency Driver credentials required.', 'danger');
          return;
        }

        showAlert(`Welcome back, ${user.fullName || 'User'}! Redirecting to authorized portal...`, 'success');
        setTimeout(() => {
          redirectByServerRole(user.role);
        }, 350);
      } catch (err) {
        isSubmitting = false;
        setButtonLoading(false);
        const errorMsg = err.message || 'Invalid email/username or password. Please verify your credentials.';
        showAlert(errorMsg, 'danger');
      }
    });
  }
});
