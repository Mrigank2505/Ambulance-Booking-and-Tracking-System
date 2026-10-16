/**
 * Ambulance Booking and Tracking System - Emergency Booking Engine
 * Features:
 * - Automatically retrieves registered user details from MySQL via GET /api/auth/me
 * - Pre-fills patient name, email, phone, and saved pickup address
 * - Handles missing profile data and informs user to complete required fields
 * - 10-second visible countdown started immediately on page load
 * - Displays: "Please enter your booking details within 10 seconds."
 * - Default Ambulance Type: Critical Ambulance (ICU) preselected
 * - Automatically stops countdown if user begins typing or entering details
 * - Auto-booking trigger if 10 seconds elapse without user interaction
 * - Validates required details before dispatch; avoids creating invalid bookings
 * - Provides 1-click continuation if missing details need to be completed
 * - Associates booking with authenticated user ID in MySQL
 * - Prevents duplicate requests and handles server errors cleanly
 */
const BookingEngine = {
  currentLat: CONFIG.DEFAULT_LATITUDE,
  currentLon: CONFIG.DEFAULT_LONGITUDE,
  countdownSeconds: 10,
  timerInterval: null,
  timerStarted: false,
  countdownStopped: false,
  userStartedEntering: false,
  isSubmitting: false,
  selectedType: 'ICU', // Default: Critical Ambulance
  currentUserProfile: null,
  lastSubmittedPayload: null,
  hospitalRadiusKm: 10.0,
  pickupGeocodeDebounceTimer: null,

  async initBookingPage() {
    if (!Auth.requireAuth()) return;
    UI.renderNavbar('booking');

    // 1. Set Critical Ambulance as default preselected option right away
    this.selectAmbulanceType('ICU', true);

    // 2. Load hospitals and fleet preview
    await this.loadHospitalsDropdown();
    this.updateCoordinatesDisplay(this.currentLat, this.currentLon);
    this.checkAvailableFleet();

    // 3. Attach input listeners to STOP countdown as soon as the user enters any details
    this.attachFormListeners();

    // 4. Automatically retrieve registered user profile from backend & MySQL
    await this.loadRegisteredUserProfile();

    // 5. Check URL parameters for explicit ambulance type or hospital override
    const queryParams = new URLSearchParams(window.location.search);
    const preselectedType = queryParams.get('type');
    if (preselectedType && ['BASIC', 'ALS', 'ICU'].includes(preselectedType.toUpperCase())) {
      this.selectAmbulanceType(preselectedType.toUpperCase(), false);
    }

    // 6. Start the visible 10-second countdown immediately on page open
    this.startCountdown();

    // 7. Clean up timer on page leave / unload
    window.addEventListener('beforeunload', () => this.clearTimer());
  },

  /**
   * Retrieves the authenticated user's profile from MySQL and pre-fills the form
   */
  async loadRegisteredUserProfile() {
    const loadingIndicator = document.getElementById('profile-loading-indicator');
    const statusBadge = document.getElementById('profile-status-badge');
    const userInfoSpan = document.getElementById('profile-user-info');
    const missingAlert = document.getElementById('profile-missing-alert');
    const missingMsg = document.getElementById('profile-missing-msg');

    if (loadingIndicator) loadingIndicator.style.display = 'flex';
    if (statusBadge) statusBadge.style.display = 'none';
    if (missingAlert) missingAlert.style.display = 'none';

    try {
      // Fetch fresh profile from MySQL using verified JWT token
      const profile = await api.get('/auth/me');
      this.currentUserProfile = profile;

      // Update cached session
      if (typeof Auth !== 'undefined' && typeof Auth.setUser === 'function') {
        Auth.setUser(profile);
      }

      const nameInput = document.getElementById('patientName');
      const emailInput = document.getElementById('patientEmail');
      const phoneInput = document.getElementById('patientPhone');
      const addressInput = document.getElementById('pickupAddress');

      // Pre-fill form fields
      if (nameInput && profile.fullName) {
        nameInput.value = profile.fullName;
      }
      if (emailInput && profile.email) {
        emailInput.value = profile.email;
      }
      if (phoneInput && profile.phone) {
        phoneInput.value = profile.phone;
      }
      if (addressInput && profile.address && !addressInput.value) {
        addressInput.value = profile.address;
      }

      if (loadingIndicator) loadingIndicator.style.display = 'none';

      if (statusBadge && userInfoSpan) {
        userInfoSpan.textContent = `${profile.fullName || 'User'} (ID #${profile.id}, ${profile.email})`;
        statusBadge.style.display = 'block';
      }

      // Check if required fields are missing in registered profile
      const missingFields = [];
      if (!profile.phone || profile.phone.trim().length < 7) {
        missingFields.push('contact phone number');
      }
      if (!profile.address || profile.address.trim().length === 0) {
        missingFields.push('saved pickup address');
      }

      if (missingFields.length > 0 && missingAlert && missingMsg) {
        missingMsg.textContent = `Your registered profile is missing your ${missingFields.join(' and ')}. Please enter these required details below or click "Use Current Location".`;
        missingAlert.style.display = 'block';
      }
    } catch (err) {
      console.warn('Could not load profile from /api/auth/me:', err);
      if (loadingIndicator) loadingIndicator.style.display = 'none';

      // Fallback to local session if available
      const localUser = Auth.getUser();
      if (localUser) {
        this.currentUserProfile = localUser;
        const nameInput = document.getElementById('patientName');
        const emailInput = document.getElementById('patientEmail');
        const phoneInput = document.getElementById('patientPhone');
        const addressInput = document.getElementById('pickupAddress');

        if (nameInput && localUser.fullName) nameInput.value = localUser.fullName;
        if (emailInput && localUser.email) emailInput.value = localUser.email;
        if (phoneInput && localUser.phone) phoneInput.value = localUser.phone;
        if (addressInput && localUser.address && !addressInput.value) addressInput.value = localUser.address;

        if (statusBadge && userInfoSpan) {
          userInfoSpan.textContent = `${localUser.fullName || 'User'} (${localUser.email})`;
          statusBadge.style.display = 'block';
        }
      } else {
        if (missingAlert && missingMsg) {
          missingMsg.textContent = 'Could not load profile from database. Please fill in your booking details manually.';
          missingAlert.style.display = 'block';
        }
      }
    }
  },

  /**
   * Attach listeners across form fields to stop countdown upon user interaction
   */
  attachFormListeners() {
    const form = document.getElementById('booking-form');
    if (form) {
      form.addEventListener('submit', (e) => this.handleSubmit(e));
    }

    const inputFields = ['patientName', 'patientPhone', 'pickupAddress', 'destinationHospital', 'emergencyType', 'notes'];
    inputFields.forEach(id => {
      const el = document.getElementById(id);
      if (el) {
        el.addEventListener('input', () => this.stopCountdownOnUserInteraction(`input_${id}`));
        el.addEventListener('change', () => this.stopCountdownOnUserInteraction(`change_${id}`));
        el.addEventListener('keydown', () => this.stopCountdownOnUserInteraction(`keydown_${id}`));
      }
    });

    const pickupInput = document.getElementById('pickupAddress');
    if (pickupInput) {
      pickupInput.addEventListener('input', (e) => this.handlePickupAddressInput(e.target.value));
    }
  },

  /**
   * Starts the 10-second countdown timer immediately
   */
  startCountdown() {
    if (this.timerStarted || this.countdownStopped) return;
    this.timerStarted = true;
    this.countdownSeconds = 10;

    this.updateCountdownUI(this.countdownSeconds);

    this.timerInterval = setInterval(() => {
      // If user started entering details in the meantime, stop immediately
      if (this.userStartedEntering || this.countdownStopped) {
        this.clearTimer();
        return;
      }

      this.countdownSeconds--;

      if (this.countdownSeconds > 0) {
        this.updateCountdownUI(this.countdownSeconds);
      } else {
        // Reached 0 seconds without interaction
        this.clearTimer();
        this.updateCountdownUI(0);
        this.handleAutoBookingTimeout();
      }
    }, 1000);
  },

  /**
   * Updates timer display, progress bar, and badge
   */
  updateCountdownUI(seconds) {
    const timerDisplay = document.getElementById('countdown-timer-display');
    const progressBar = document.getElementById('countdown-progress-bar');
    const msgEl = document.getElementById('auto-booking-message');

    if (timerDisplay) {
      timerDisplay.textContent = `${seconds}s`;
      if (seconds <= 3 && seconds > 0) {
        timerDisplay.style.color = '#b91c1c';
        if (timerDisplay.parentElement) {
          timerDisplay.parentElement.style.borderColor = '#b91c1c';
          timerDisplay.parentElement.style.boxShadow = '0 0 10px rgba(185, 28, 28, 0.35)';
        }
      } else {
        timerDisplay.style.color = '#dc2626';
        if (timerDisplay.parentElement) {
          timerDisplay.parentElement.style.borderColor = '#dc2626';
          timerDisplay.parentElement.style.boxShadow = '0 3px 8px rgba(220, 38, 38, 0.15)';
        }
      }
    }

    if (progressBar) {
      const percentage = Math.max(0, Math.min(100, (seconds / 10) * 100));
      progressBar.style.width = `${percentage}%`;
    }

    if (msgEl && !this.userStartedEntering) {
      msgEl.textContent = 'Please enter your booking details within 10 seconds.';
    }
  },

  /**
   * Stops the countdown when the user starts entering details or clicks pause
   */
  stopCountdownOnUserInteraction(source = 'input') {
    if (this.countdownStopped) return;

    this.clearTimer();
    this.countdownStopped = true;
    this.userStartedEntering = true;

    const timerDisplay = document.getElementById('countdown-timer-display');
    const timerBox = document.getElementById('timer-box');
    const timerLabel = document.getElementById('timer-label');
    const statusDot = document.getElementById('countdown-status-dot');
    const titleEl = document.getElementById('auto-booking-title');
    const msgEl = document.getElementById('auto-booking-message');
    const subtextEl = document.getElementById('auto-booking-subtext');
    const badgeEl = document.getElementById('auto-booking-badge');
    const container = document.getElementById('auto-booking-container');
    const cancelBtn = document.getElementById('btn-cancel-countdown');
    const progressBar = document.getElementById('countdown-progress-bar');

    if (timerDisplay) {
      timerDisplay.textContent = 'PAUSED';
      timerDisplay.style.fontSize = '0.95rem';
      timerDisplay.style.color = '#059669';
    }
    if (timerLabel) {
      timerLabel.textContent = 'MANUAL MODE';
      timerLabel.style.color = '#059669';
    }
    if (timerBox) {
      timerBox.style.borderColor = '#10b981';
      timerBox.style.boxShadow = 'none';
      timerBox.style.minWidth = '80px';
    }
    if (statusDot) {
      statusDot.style.background = '#10b981';
      statusDot.style.boxShadow = '0 0 0 3px rgba(16, 185, 129, 0.2)';
    }
    if (titleEl) {
      titleEl.textContent = 'Manual Booking Active';
      titleEl.style.color = '#065f46';
    }
    if (msgEl) {
      msgEl.textContent = 'Countdown stopped. Please complete your booking details at your own pace.';
      msgEl.style.color = '#065f46';
    }
    if (subtextEl) {
      const typeLabel = this.selectedType === 'ICU' ? 'Critical Care Ambulance' : (this.selectedType === 'ALS' ? 'Advanced Life Support' : 'Basic Ambulance');
      subtextEl.textContent = `Selected type: ${typeLabel}. Click "Confirm and Dispatch Ambulance" when ready.`;
      subtextEl.style.color = '#047857';
    }
    if (badgeEl) {
      badgeEl.className = 'badge badge-available';
      badgeEl.textContent = 'TIMER STOPPED';
    }
    if (cancelBtn) {
      cancelBtn.style.display = 'none';
    }
    if (container) {
      container.style.background = '#f0fdf4';
      container.style.borderColor = '#bbf7d0';
      container.style.borderLeftColor = '#10b981';
      container.style.boxShadow = 'none';
    }
    if (progressBar) {
      progressBar.style.background = '#10b981';
      progressBar.style.width = '100%';
    }
  },

  /**
   * Clears the timer interval safely
   */
  clearTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  },

  /**
   * Selects ambulance type (Basic, ALS, ICU). Respects manual choice and cancels countdown.
   */
  selectAmbulanceType(type, isDefault = false) {
    this.selectedType = type;
    const typeInput = document.getElementById('ambulanceType');
    if (typeInput) typeInput.value = type;

    // Card styling
    const cards = {
      'BASIC': document.getElementById('card-amb-BASIC'),
      'ALS': document.getElementById('card-amb-ALS'),
      'ICU': document.getElementById('card-amb-ICU')
    };

    const badge = document.getElementById('selected-type-badge');

    Object.keys(cards).forEach(key => {
      const card = cards[key];
      if (!card) return;
      if (key === type) {
        card.style.borderColor = key === 'ICU' ? '#dc2626' : (key === 'ALS' ? '#d97706' : '#2563eb');
        card.style.background = key === 'ICU' ? '#fff5f5' : (key === 'ALS' ? '#fffbeb' : '#eff6ff');
        card.style.boxShadow = '0 0 0 2px rgba(220, 38, 38, 0.2)';
      } else {
        card.style.borderColor = '#e2e8f0';
        card.style.background = '#ffffff';
        card.style.boxShadow = 'none';
      }
    });

    if (badge) {
      if (type === 'ICU') {
        badge.className = 'badge badge-critical';
        badge.textContent = isDefault ? 'DEFAULT: CRITICAL CARE / ICU' : 'SELECTED: CRITICAL CARE / ICU';
      } else if (type === 'ALS') {
        badge.className = 'badge badge-serious';
        badge.textContent = 'SELECTED: ADVANCED LIFE SUPPORT (ALS)';
      } else {
        badge.className = 'badge badge-normal';
        badge.textContent = 'SELECTED: BASIC AMBULANCE';
      }
    }

    // If user clicked manually, stop countdown
    if (!isDefault) {
      this.stopCountdownOnUserInteraction('manual_ambulance_choice');
    }

    this.checkAvailableFleet();
  },

  /**
   * Validates if required patient details and pickup location are present
   */
  hasRequiredDetails() {
    const name = document.getElementById('patientName')?.value.trim();
    const phone = document.getElementById('patientPhone')?.value.trim();
    const pickup = document.getElementById('pickupAddress')?.value.trim();
    return !!(name && phone && phone.length >= 7 && pickup);
  },

  /**
   * Triggered when 10 seconds elapse without the user entering details
   */
  async handleAutoBookingTimeout() {
    if (this.isSubmitting || this.userStartedEntering) return;

    // Automatically enforce Critical Ambulance as target
    this.selectAmbulanceType('ICU', true);

    const msgEl = document.getElementById('auto-booking-message');
    const subtextEl = document.getElementById('auto-booking-subtext');
    const missingPrompt = document.getElementById('missing-info-prompt');

    // Check if required patient details and pickup location are available
    if (this.hasRequiredDetails()) {
      if (msgEl) {
        msgEl.textContent = '10 seconds elapsed: Initiating Critical Ambulance auto-booking...';
      }
      if (subtextEl) {
        subtextEl.textContent = 'Registered details and pickup location verified. Dispatching emergency unit...';
      }
      await this.executeBookingSubmission(true);
    } else {
      // Required information is missing: DO NOT create an invalid booking!
      if (msgEl) {
        msgEl.textContent = '10 seconds elapsed: Critical Ambulance preselected.';
      }
      if (subtextEl) {
        subtextEl.textContent = 'Pickup location or contact phone number is required before dispatch.';
      }

      if (missingPrompt) {
        missingPrompt.style.display = 'block';
      }

      // Highlight missing fields
      const name = document.getElementById('patientName')?.value.trim();
      const phone = document.getElementById('patientPhone')?.value.trim();
      const pickup = document.getElementById('pickupAddress')?.value.trim();

      if (!name) Validation.showFieldError(document.getElementById('patientName'), 'Please enter patient name');
      if (!phone) Validation.showFieldError(document.getElementById('patientPhone'), 'Please enter contact phone');
      if (!pickup) Validation.showFieldError(document.getElementById('pickupAddress'), 'Please provide pickup location or click "Use Current GPS Location"');

      // Scroll smoothly to missing information prompt
      missingPrompt.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  },

  /**
   * One-click option to continue with Critical Ambulance
   */
  async quickBookCritical() {
    this.selectAmbulanceType('ICU', false);

    // If pickup address is empty, attempt to fill with current GPS coordinates
    const pickupEl = document.getElementById('pickupAddress');
    if (pickupEl && !pickupEl.value.trim()) {
      pickupEl.value = `Emergency GPS Pin: ${this.currentLat.toFixed(4)}, ${this.currentLon.toFixed(4)}`;
    }

    const form = document.getElementById('booking-form');
    if (form) {
      form.requestSubmit();
    }
  },

  /**
   * Executes backend booking submission with duplicate prevention
   */
  async executeBookingSubmission(isAuto = false) {
    if (this.isSubmitting) {
      console.warn('Booking submission in progress. Preventing duplicate request.');
      return;
    }

    this.isSubmitting = true;
    this.clearTimer();

    const submitBtn = document.getElementById('btn-submit-booking');
    const loadingText = isAuto ? 'Auto-Dispatching Critical Ambulance...' : 'Allocating Emergency Unit...';
    UI.setButtonLoading(submitBtn, true, loadingText);

    const errorContainer = document.getElementById('booking-error-container');
    if (errorContainer) errorContainer.style.display = 'none';

    const patientName = document.getElementById('patientName').value.trim();
    const patientPhone = document.getElementById('patientPhone').value.trim();
    const pickupAddress = document.getElementById('pickupAddress').value.trim();
    const emergencyType = document.getElementById('emergencyType').value || 'CRITICAL';
    const ambulanceType = document.getElementById('ambulanceType').value || 'ICU';
    const notes = document.getElementById('notes').value.trim();

    const hospSelect = document.getElementById('destinationHospital');
    let hospitalId = null;
    let destName = null;
    let destLat = null;
    let destLon = null;

    if (hospSelect && hospSelect.value) {
      hospitalId = hospSelect.value;
      const opt = hospSelect.options[hospSelect.selectedIndex];
      destName = opt.getAttribute('data-name');
      destLat = parseFloat(opt.getAttribute('data-lat'));
      destLon = parseFloat(opt.getAttribute('data-lon'));
    }

    const payload = {
      patientName,
      patientPhone,
      pickupAddress,
      pickupLatitude: this.currentLat,
      pickupLongitude: this.currentLon,
      destinationAddress: destName,
      destinationLatitude: destLat,
      destinationLongitude: destLon,
      hospitalId: hospitalId ? parseInt(hospitalId) : null,
      emergencyType,
      ambulanceType,
      notes
    };

    this.lastSubmittedPayload = payload;

    try {
      const booking = await api.post('/bookings', payload);
      UI.showToast('Critical Ambulance booking successfully created!', 'success');

      // Navigate to confirmation page
      setTimeout(() => {
        window.location.href = `booking-confirmation.html?id=${booking.id}&code=${booking.bookingCode}`;
      }, 500);
    } catch (err) {
      this.isSubmitting = false;
      UI.setButtonLoading(submitBtn, false);
      const errorMsg = err.message || 'Unable to allocate an emergency ambulance. Please retry or contact dispatch.';
      this.showBookingError('Emergency Booking Request Failed', errorMsg);
    }
  },

  showBookingError(title, message) {
    const errorContainer = document.getElementById('booking-error-container');
    const errorTitle = document.getElementById('booking-error-title');
    const errorMsg = document.getElementById('booking-error-msg');

    if (errorTitle) errorTitle.textContent = title;
    if (errorMsg) errorMsg.textContent = message;
    if (errorContainer) {
      errorContainer.style.display = 'block';
      errorContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    UI.showToast(message, 'error');
  },

  async retryBooking() {
    const errorContainer = document.getElementById('booking-error-container');
    if (errorContainer) errorContainer.style.display = 'none';

    if (this.lastSubmittedPayload) {
      await this.executeBookingSubmission();
    } else {
      const form = document.getElementById('booking-form');
      if (form) form.requestSubmit();
    }
  },

  handlePickupAddressInput(addressText) {
    clearTimeout(this.pickupGeocodeDebounceTimer);
    const trimmed = (addressText || '').trim();
    if (!trimmed) {
      this.renderBookingHospitals([], this.hospitalRadiusKm || 10.0);
      return;
    }

    this.pickupGeocodeDebounceTimer = setTimeout(async () => {
      try {
        const geoResult = typeof LocationService !== 'undefined'
          ? await LocationService.geocode(trimmed)
          : null;
        if (geoResult && geoResult.lat != null && geoResult.lon != null) {
          this.currentLat = geoResult.lat;
          this.currentLon = geoResult.lon;
          this.updateCoordinatesDisplay(this.currentLat, this.currentLon);
          await this.loadHospitalsDropdown(this.hospitalRadiusKm || 10.0);
          this.checkAvailableFleet();
        }
      } catch (err) {
        console.warn('Geocoding pickup address error:', err);
      }
    }, 450);
  },

  async loadHospitalsDropdown(radiusKm = 10.0) {
    this.hospitalRadiusKm = radiusKm;
    const select = document.getElementById('destinationHospital');
    const bannerEl = document.getElementById('booking-hospitals-banner');
    const countEl = document.getElementById('booking-hospital-count');
    const listEl = document.getElementById('booking-hospitals-list');

    if (bannerEl) {
      if (radiusKm === 10.0) {
        bannerEl.textContent = 'Showing hospitals within 10 km of your pickup location.';
      } else {
        bannerEl.textContent = `Showing hospitals within ${radiusKm} km of your pickup location.`;
      }
    }

    if (countEl) countEl.textContent = 'Searching nearby facilities...';

    try {
      const data = await api.get('/hospitals/nearby', {
        lat: this.currentLat,
        lon: this.currentLon,
        radiusKm: radiusKm
      });

      const rawList = Array.isArray(data) ? data : [];
      // Enforce <= radiusKm straight-line distance
      const hospitals = rawList.filter(h => h.distanceKm !== null && h.distanceKm !== undefined && h.distanceKm <= radiusKm);
      hospitals.sort((a, b) => (a.distanceKm || 0) - (b.distanceKm || 0));

      if (select) {
        const currentSelectedVal = select.value;
        select.innerHTML = '<option value="">-- Nearest Emergency Hospital (&le; 10 km, Automatic) --</option>';
        hospitals.forEach(h => {
          const distText = h.distanceKm != null ? ` (${h.distanceKm.toFixed(2)} km away)` : '';
          const icuText = h.icuAvailable ? ' [ICU Ready]' : '';
          select.innerHTML += `<option value="${h.id}" data-lat="${h.latitude}" data-lon="${h.longitude}" data-name="${h.name}">${h.name}${distText}${icuText}</option>`;
        });

        const urlParams = new URLSearchParams(window.location.search);
        const preselectedId = urlParams.get('hospitalId');
        if (preselectedId && hospitals.some(h => String(h.id) === String(preselectedId))) {
          select.value = preselectedId;
        } else if (currentSelectedVal && hospitals.some(h => String(h.id) === String(currentSelectedVal))) {
          select.value = currentSelectedVal;
        }
      }

      this.renderBookingHospitals(hospitals, radiusKm);
    } catch (e) {
      console.warn('Could not load hospitals list:', e);
      if (countEl) countEl.textContent = 'Service unavailable';
      if (listEl) {
        listEl.innerHTML = `
          <div style="font-size: 0.8rem; color: #b91c1c; padding: 0.75rem; background: #fef2f2; border: 1px solid #fecaca; border-radius: 6px;">
            Hospital directory service temporarily unavailable. Emergency dispatch will assign nearest unit automatically.
          </div>
        `;
      }
    }
  },

  renderBookingHospitals(hospitals, radiusKm = 10.0) {
    const listEl = document.getElementById('booking-hospitals-list');
    const countEl = document.getElementById('booking-hospital-count');
    if (!listEl) return;

    if (!hospitals || hospitals.length === 0) {
      if (countEl) countEl.textContent = `0 within ${radiusKm} km`;
      listEl.innerHTML = `
        <div style="padding: 0.85rem; background: #fffaf0; border: 1px solid #fed7aa; border-radius: 6px; text-align: center;">
          <strong style="color: #9a3412; font-size: 0.85rem; display: block; margin-bottom: 0.25rem;">
            No hospitals found within 10 km of your pickup location.
          </strong>
          <p style="color: #78350f; font-size: 0.775rem; margin-bottom: 0.65rem;">
            No hospitals found within 10 km of your pickup location. You can change your pickup location or expand your search radius.
          </p>
          <div style="display: flex; gap: 0.5rem; justify-content: center; flex-wrap: wrap;">
            <button type="button" onclick="BookingEngine.expandHospitalRadius(20.0)" class="btn btn-outline btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.6rem; color: #1e3a8a; border-color: #93c5fd;">
              Expand to 20 km
            </button>
            <button type="button" onclick="document.getElementById('pickupAddress').focus()" class="btn btn-outline btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.6rem;">
              Change Pickup Location
            </button>
          </div>
        </div>
      `;
      return;
    }

    if (countEl) {
      countEl.textContent = `${hospitals.length} hospital(s) within ${radiusKm} km`;
    }

    const select = document.getElementById('destinationHospital');
    const currentVal = select ? select.value : '';

    listEl.innerHTML = hospitals.map(h => {
      const isSelected = String(currentVal) === String(h.id);
      const distKm = h.distanceKm != null ? h.distanceKm.toFixed(2) : 'N/A';
      const mapUrl = `https://www.google.com/maps?q=${h.latitude},${h.longitude}`;
      const directionsUrl = `https://www.google.com/maps/dir/?api=1&origin=${this.currentLat},${this.currentLon}&destination=${h.latitude},${h.longitude}`;

      return `
        <div style="background: ${isSelected ? '#eff6ff' : '#ffffff'}; border: 1px solid ${isSelected ? '#3b82f6' : '#e2e8f0'}; border-radius: 6px; padding: 0.75rem; display: flex; justify-content: space-between; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
          <div style="flex: 1; min-width: 200px;">
            <div style="display: flex; align-items: center; gap: 0.4rem; margin-bottom: 0.2rem;">
              <strong style="color: #0f172a; font-size: 0.85rem;">${escapeHtml(h.name)}</strong>
              <span class="badge badge-normal" style="font-size: 0.7rem; font-weight: 700; background: #e0f2fe; color: #0369a1;">${distKm} km (Straight-line)</span>
            </div>
            <div style="font-size: 0.775rem; color: #64748b; margin-bottom: 0.35rem;">
              ${escapeHtml(h.address)}
            </div>
            <div style="display: flex; gap: 0.35rem; align-items: center; flex-wrap: wrap; font-size: 0.725rem;">
              ${h.phone ? `<a href="tel:${escapeHtml(h.phone)}" style="color: #1e3a8a; font-weight: 700; text-decoration: none;">&#128222; ${escapeHtml(h.phone)}</a>` : ''}
              ${h.emergencyAvailable ? '<span class="badge badge-available" style="font-size: 0.65rem;">24/7 Trauma</span>' : ''}
              ${h.icuAvailable ? '<span class="badge badge-assigned" style="font-size: 0.65rem;">ICU Ready</span>' : ''}
            </div>
          </div>
          <div style="display: flex; gap: 0.35rem; align-items: center;">
            <a href="${mapUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm" style="font-size: 0.725rem; padding: 0.25rem 0.5rem;" title="View hospital on Google Maps">
              View on Map
            </a>
            <a href="${directionsUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm" style="font-size: 0.725rem; padding: 0.25rem 0.5rem; color: #1e3a8a; border-color: #93c5fd;" title="Directions from pickup point">
              Directions
            </a>
            <button type="button" onclick="BookingEngine.selectHospitalDestination('${h.id}')" class="btn ${isSelected ? 'btn-primary' : 'btn-outline'} btn-sm" style="font-size: 0.725rem; padding: 0.25rem 0.6rem; font-weight: 600;">
              ${isSelected ? '&#10003; Selected' : 'Select'}
            </button>
          </div>
        </div>
      `;
    }).join('');
  },

  selectHospitalDestination(hospitalId) {
    const select = document.getElementById('destinationHospital');
    if (select) {
      select.value = String(hospitalId);
      this.onHospitalSelectChange(hospitalId);
    }
  },

  onHospitalSelectChange(hospitalId) {
    this.stopCountdownOnUserInteraction('select_hospital');
    this.loadHospitalsDropdown(this.hospitalRadiusKm || 10.0);
  },

  expandHospitalRadius(newRadius) {
    this.hospitalRadiusKm = newRadius;
    const badge = document.getElementById('hospital-radius-badge');
    if (badge) {
      badge.textContent = `${newRadius} KM RADIUS ACTIVE`;
    }
    this.loadHospitalsDropdown(newRadius);
  },

  useCurrentLocation() {
    const btn = document.getElementById('btn-use-location');
    UI.setButtonLoading(btn, true, 'Detecting GPS...');

    if (typeof LocationService !== 'undefined') {
      LocationService.getCurrentGpsPosition()
        .then(pos => {
          this.currentLat = pos.lat;
          this.currentLon = pos.lon;
          this.updateCoordinatesDisplay(this.currentLat, this.currentLon);

          const addrInput = document.getElementById('pickupAddress');
          if (addrInput) {
            addrInput.value = pos.label || `GPS Pin: ${this.currentLat.toFixed(4)}, ${this.currentLon.toFixed(4)}`;
          }

          UI.setButtonLoading(btn, false);
          UI.showToast('GPS coordinates updated successfully!', 'success');
          this.loadHospitalsDropdown(this.hospitalRadiusKm || 10.0);
          this.checkAvailableFleet();
          this.stopCountdownOnUserInteraction('use_current_location');
        })
        .catch(err => {
          console.warn('Geolocation error:', err.message);
          UI.showToast(err.message || 'Could not fetch GPS. Defaulting to Central District.', 'info');
          this.currentLat = CONFIG.DEFAULT_LATITUDE;
          this.currentLon = CONFIG.DEFAULT_LONGITUDE;
          this.updateCoordinatesDisplay(this.currentLat, this.currentLon);
          UI.setButtonLoading(btn, false);
          this.loadHospitalsDropdown(this.hospitalRadiusKm || 10.0);
        });
    } else {
      if (btn) UI.setButtonLoading(btn, false);
      this.loadHospitalsDropdown(this.hospitalRadiusKm || 10.0);
    }
  },

  updateCoordinatesDisplay(lat, lon) {
    const latInput = document.getElementById('pickupLat');
    const lonInput = document.getElementById('pickupLon');
    const badge = document.getElementById('coords-badge');
    if (latInput) latInput.value = lat;
    if (lonInput) lonInput.value = lon;
    if (badge) badge.textContent = `${lat.toFixed(4)}, ${lon.toFixed(4)}`;
  },

  async checkAvailableFleet() {
    const previewEl = document.getElementById('fleet-preview');
    if (!previewEl) return;

    const type = document.getElementById('ambulanceType')?.value || this.selectedType || 'ICU';
    try {
      const units = await api.get('/ambulances/available', {
        type: type,
        lat: this.currentLat,
        lon: this.currentLon
      });

      if (units && units.length > 0) {
        const nearest = units[0];
        previewEl.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: space-between; background: #ecfdf5; border: 1px solid #a7f3d0; padding: 0.75rem 1rem; border-radius: 8px;">
            <div>
              <strong style="color: #065f46;">Nearest Unit Available:</strong> ${nearest.ambulanceNumber} (${nearest.ambulanceType})
              <div style="font-size: 0.8rem; color: #047857;">Est. Arrival: <strong>${nearest.etaMinutes ? nearest.etaMinutes + ' mins' : 'Immediate'}</strong> | Distance: ${nearest.distanceKm ? nearest.distanceKm + ' km' : 'Nearby'}</div>
            </div>
            <span class="badge badge-available">DISPATCH READY</span>
          </div>
        `;
      } else {
        previewEl.innerHTML = `
          <div style="background: #fffbeb; border: 1px solid #fde68a; padding: 0.75rem 1rem; border-radius: 8px; color: #92400e;">
            <strong>High Demand:</strong> Dedicated ${type} units busy; system will auto-allocate closest emergency backup fleet immediately upon submission.
          </div>
        `;
      }
    } catch (e) {
      console.warn('Could not preview fleet availability:', e);
    }
  },

  async handleSubmit(e) {
    e.preventDefault();

    if (this.isSubmitting) return;

    if (!Auth.isAuthenticated()) {
      UI.showToast('Please sign in or register to confirm emergency booking', 'warning');
      window.location.href = 'login.html?redirect=booking.html';
      return;
    }

    const form = e.target;
    Validation.clearAllErrors(form);

    const patientName = document.getElementById('patientName').value.trim();
    const patientPhone = document.getElementById('patientPhone').value.trim();
    const pickupAddress = document.getElementById('pickupAddress').value.trim();

    let valid = true;
    if (!patientName) {
      Validation.showFieldError(document.getElementById('patientName'), 'Patient name is required');
      valid = false;
    }
    if (!Validation.isValidPhone(patientPhone)) {
      Validation.showFieldError(document.getElementById('patientPhone'), 'Please provide a valid contact phone number');
      valid = false;
    }
    if (!pickupAddress) {
      Validation.showFieldError(document.getElementById('pickupAddress'), 'Pickup address or landmark is required');
      valid = false;
    }

    if (!valid) return;

    // User is manually submitting, stop countdown
    this.stopCountdownOnUserInteraction('manual_submit');
    await this.executeBookingSubmission(false);
  }
};
