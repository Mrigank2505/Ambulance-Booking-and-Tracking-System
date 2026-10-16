/**
 * Form Validation and Input Sanity Checking
 */
const Validation = {
  isValidEmail(email) {
    if (!email) return false;
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
  },

  isValidPhone(phone) {
    if (!phone) return false;
    // Allow international format, plus sign, hyphens, parentheses, minimum 7 digits
    const digits = phone.replace(/\D/g, '');
    return digits.length >= 7 && digits.length <= 15;
  },

  isValidPassword(password) {
    return !!password && password.length >= 6;
  },

  showFieldError(inputElement, errorMessage) {
    if (!inputElement) return;
    this.clearFieldError(inputElement);

    inputElement.style.borderColor = '#dc2626';
    const err = document.createElement('div');
    err.className = 'form-error';
    err.style.color = '#dc2626';
    err.style.fontSize = '0.75rem';
    err.style.marginTop = '0.3rem';
    err.textContent = errorMessage;

    // Attach one-time listener to remove error as user types
    const clearOnInput = () => {
      this.clearFieldError(inputElement);
      inputElement.removeEventListener('input', clearOnInput);
    };
    inputElement.addEventListener('input', clearOnInput);

    // If input is wrapped in .password-field-container, insert error after the container
    const passContainer = inputElement.closest('.password-field-container');
    if (passContainer && passContainer.parentNode) {
      passContainer.parentNode.insertBefore(err, passContainer.nextSibling);
    } else if (inputElement.parentNode) {
      inputElement.parentNode.appendChild(err);
    }
  },

  clearFieldError(inputElement) {
    if (!inputElement) return;
    inputElement.style.borderColor = '';
    const formGroup = inputElement.closest('.form-group') || inputElement.parentNode;
    if (formGroup) {
      const existing = formGroup.querySelectorAll('.form-error');
      existing.forEach(el => el.remove());
    }
  },

  clearAllErrors(formElement) {
    if (!formElement) return;
    formElement.querySelectorAll('.form-error').forEach(e => e.remove());
    formElement.querySelectorAll('.form-control, input, select, textarea').forEach(input => {
      input.style.borderColor = '';
    });
  }
};
