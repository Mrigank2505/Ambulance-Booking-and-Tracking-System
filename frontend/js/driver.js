/**
 * Driver Cockpit & Mission Control
 */
const DriverApp = {
  driverProfile: null,
  pollTimer: null,

  async init() {
    UI.renderNavbar('driver-dashboard');

    if (!Auth.requireAuth(['DRIVER'])) {
      return;
    }

    await this.loadDriverProfile();
    await this.loadPendingRequests();
    await this.loadActiveTrip();
    await this.loadCompletedTrips();

    // Start auto polling for dispatch queue
    this.pollTimer = setInterval(() => {
      this.loadPendingRequests();
      this.loadActiveTrip();
    }, 5000);

    const toggle = document.getElementById('driver-online-toggle');
    if (toggle) {
      toggle.addEventListener('change', (e) => this.toggleOnlineStatus(e.target.checked));
    }
  },

  async loadDriverProfile() {
    try {
      // Driver profile is at /driver/me on backend, with fallback
      try {
        this.driverProfile = await api.get('/driver/me');
      } catch (err) {
        this.driverProfile = await api.get('/driver/profile');
      }

      const nameEl = document.getElementById('driver-name');
      const licEl = document.getElementById('driver-license');
      const ambEl = document.getElementById('driver-ambulance');
      const toggle = document.getElementById('driver-online-toggle');
      const statusBadge = document.getElementById('driver-status-text');

      if (nameEl) nameEl.textContent = this.driverProfile.fullName || 'Driver';
      if (licEl) licEl.textContent = `License: ${this.driverProfile.licenseNumber || 'Verified EMT'}`;
      if (ambEl) {
        const ambNum = this.driverProfile.assignedAmbulanceNumber || this.driverProfile.ambulanceNumber;
        ambEl.textContent = ambNum 
          ? `Assigned Unit: ${ambNum}`
          : 'No ambulance assigned yet';
      }
      
      const isOnline = this.driverProfile.availabilityStatus === 'AVAILABLE' || this.driverProfile.availabilityStatus === 'BUSY' || this.driverProfile.status === 'AVAILABLE';
      if (toggle) {
        toggle.checked = isOnline;
      }
      if (statusBadge) {
        statusBadge.innerHTML = `<span class="badge ${isOnline ? 'badge-online' : 'badge-offline'}">${this.driverProfile.availabilityStatus || 'ONLINE'}</span>`;
      }
    } catch (e) {
      console.warn('Could not load driver profile:', e);
      // Graceful display fallback from cached auth user
      const user = Auth.getUser() || {};
      const nameEl = document.getElementById('driver-name');
      if (nameEl && user.fullName) nameEl.textContent = user.fullName;
    }
  },

  async toggleOnlineStatus(isOnline) {
    try {
      const newStatus = isOnline ? 'AVAILABLE' : 'OFFLINE';
      try {
        await api.put('/driver/availability', { availabilityStatus: newStatus });
      } catch (err) {
        await api.put(`/driver/status?status=${newStatus}`);
      }
      UI.showToast(`Status updated to ${newStatus}`, 'success');
      const badge = document.getElementById('driver-status-text');
      if (badge) {
        badge.innerHTML = `<span class="badge ${isOnline ? 'badge-online' : 'badge-offline'}">${newStatus}</span>`;
      }
    } catch (e) {
      UI.showToast(e.message || 'Failed to update online status', 'error');
    }
  },

  async loadPendingRequests() {
    const container = document.getElementById('pending-requests-container');
    const countEl = document.getElementById('pending-count');
    if (!container) return;

    try {
      const requests = await api.get('/driver/bookings/pending');
      if (countEl) countEl.textContent = requests ? requests.length : 0;

      if (!requests || requests.length === 0) {
        container.innerHTML = `
          <div class="card" style="text-align: center; padding: 2rem; color: #64748b;">
            <p>No pending emergency dispatches in your queue right now.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = requests.map(r => `
        <div class="card" style="border-left: 5px solid #dc2626; margin-bottom: 1rem;">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
            <div>
              <span class="badge badge-critical" style="margin-right: 0.5rem;">PRIORITY DISPATCH</span>
              <strong>${r.bookingCode}</strong>
            </div>
            ${UI.formatEmergencyBadge(r.emergencyType)}
          </div>

          <div class="form-row" style="margin-bottom: 1rem;">
            <div>
              <span style="font-size: 0.75rem; color: #64748b;">PATIENT</span>
              <p style="font-weight: 700;">${r.patientName} (${r.patientPhone})</p>
            </div>
            <div>
              <span style="font-size: 0.75rem; color: #64748b;">PICKUP POINT</span>
              <p style="font-weight: 600; color: #0f172a;">${r.pickupAddress}</p>
            </div>
            <div>
              <span style="font-size: 0.75rem; color: #64748b;">DESTINATION</span>
              <p style="font-weight: 600; color: #1e3a8a;">${r.destinationAddress || r.hospitalName || 'Emergency Center'}</p>
            </div>
          </div>

          <div style="display: flex; gap: 0.75rem; justify-content: flex-end;">
            <button onclick="DriverApp.acceptTrip(${r.id})" class="btn btn-primary btn-sm">Accept and Dispatch</button>
            <button onclick="DriverApp.rejectTrip(${r.id})" class="btn btn-outline btn-sm" style="color: #dc2626;">Decline</button>
          </div>
        </div>
      `).join('');
    } catch (e) {
      console.warn('Could not load pending dispatches:', e);
    }
  },

  async loadActiveTrip() {
    const container = document.getElementById('active-trip-container');
    if (!container) return;

    try {
      const activeList = await api.get('/driver/bookings/active');

      if (!activeList || activeList.length === 0) {
        container.innerHTML = `
          <div style="text-align: center; padding: 2.5rem; color: #64748b;">
            <h4 style="color: #0f172a; margin-bottom: 0.25rem;">No active trips</h4>
            <p>You currently do not have an accepted trip in progress.</p>
          </div>
        `;
        return;
      }

      const trip = activeList[0];
      container.innerHTML = `
        <div class="card" style="border: 2px solid #1e3a8a;">
          <div class="card-header" style="background: #eff6ff; margin: -1.5rem -1.5rem 1.5rem -1.5rem; padding: 1rem 1.5rem;">
            <div>
              <span class="badge badge-ontrip" style="margin-right: 0.5rem;">ACTIVE MISSION</span>
              <strong>${trip.bookingCode}</strong>
            </div>
            ${UI.formatStatusBadge(trip.bookingStatus)}
          </div>

          <div class="form-row" style="margin-bottom: 1.5rem;">
            <div>
              <span style="font-size: 0.8rem; color: #64748b;">PATIENT DETAILS</span>
              <p style="font-weight: 700; font-size: 1.1rem; color: #0f172a;">${trip.patientName}</p>
              <p style="color: #1e3a8a; font-weight: 600;">Phone: <a href="tel:${trip.patientPhone}">${trip.patientPhone}</a></p>
            </div>
            <div>
              <span style="font-size: 0.8rem; color: #64748b;">PICKUP POINT</span>
              <p style="font-weight: 600; color: #0f172a;">${trip.pickupAddress}</p>
            </div>
            <div>
              <span style="font-size: 0.8rem; color: #64748b;">DESTINATION HOSPITAL</span>
              <p style="font-weight: 600; color: #0f172a;">${trip.destinationAddress || trip.hospitalName}</p>
            </div>
          </div>

          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem; margin-bottom: 1.5rem;">
            <h5 style="margin-bottom: 0.75rem; font-weight: 700; color: #0f172a;">Trip Progression Controls:</h5>
            <div style="display: flex; flex-wrap: wrap; gap: 0.75rem;">
              <button onclick="DriverApp.markArrived(${trip.id})" class="btn btn-primary btn-sm" ${trip.bookingStatus === 'ARRIVED' || trip.bookingStatus === 'PATIENT_PICKED_UP' ? 'disabled' : ''}>
                Mark Arrived at Patient
              </button>
              <button onclick="DriverApp.markPickedUp(${trip.id})" class="btn btn-warning btn-sm" ${trip.bookingStatus === 'PATIENT_PICKED_UP' ? 'disabled' : ''}>
                Mark Patient Picked Up
              </button>
              <button onclick="DriverApp.markCompleted(${trip.id})" class="btn btn-success btn-sm">
                Complete Trip at Hospital
              </button>
              <a href="tracking.html?id=${trip.id}" target="_blank" class="btn btn-outline btn-sm">
                Track Location
              </a>
            </div>
          </div>
        </div>
      `;
    } catch (e) {
      console.warn('Could not load active trip:', e);
    }
  },

  async acceptTrip(id) {
    try {
      UI.showLoading(true, 'Accepting dispatch...');
      await api.put(`/driver/bookings/${id}/accept`);
      UI.showLoading(false);
      UI.showToast('Dispatch accepted! Vehicle locked to mission.', 'success');
      this.loadPendingRequests();
      this.loadActiveTrip();
    } catch (e) {
      UI.showLoading(false);
      UI.showToast(e.message || 'Could not accept trip', 'error');
    }
  },

  async rejectTrip(id) {
    try {
      await api.put(`/driver/bookings/${id}/reject`, { reason: 'Traffic impediment / Refusal' });
      UI.showToast('Dispatch declined and rerouted', 'info');
      this.loadPendingRequests();
    } catch (e) {
      UI.showToast(e.message || 'Could not decline trip', 'error');
    }
  },

  async markArrived(id) {
    try {
      UI.showLoading(true, 'Updating arrival...');
      await api.put(`/driver/bookings/${id}/arrived`);
      UI.showLoading(false);
      UI.showToast('Marked arrived at patient pickup', 'success');
      this.loadActiveTrip();
    } catch (e) {
      UI.showLoading(false);
      UI.showToast(e.message, 'error');
    }
  },

  async markPickedUp(id) {
    try {
      UI.showLoading(true, 'Updating patient handover...');
      await api.put(`/driver/bookings/${id}/picked-up`);
      UI.showLoading(false);
      UI.showToast('Patient loaded. En route to destination hospital.', 'success');
      this.loadActiveTrip();
    } catch (e) {
      UI.showLoading(false);
      UI.showToast(e.message, 'error');
    }
  },

  async markCompleted(id) {
    UI.showConfirm(
      'Complete Emergency Mission',
      'Confirm patient handover to hospital trauma care team and mark this mission complete?',
      async () => {
        try {
          UI.showLoading(true, 'Finalizing mission...');
          await api.put(`/driver/bookings/${id}/completed`);
          UI.showLoading(false);
          UI.showToast('Mission completed successfully. Ready for next call.', 'success');
          this.loadActiveTrip();
          this.loadCompletedTrips();
        } catch (e) {
          UI.showLoading(false);
          UI.showToast(e.message, 'error');
        }
      }
    );
  },

  async loadCompletedTrips() {
    const tbody = document.getElementById('driver-history-tbody');
    if (!tbody) return;

    try {
      let list = [];
      try {
        list = await api.get('/bookings');
      } catch (e) {
        list = await api.get('/driver/bookings/history');
      }
      
      const completedList = (list || []).filter(t => t.bookingStatus === 'COMPLETED');
      if (completedList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b; padding: 2rem;">No past trips recorded.</td></tr>`;
        return;
      }

      tbody.innerHTML = completedList.map(t => `
        <tr>
          <td>${UI.formatDate(t.createdAt)}</td>
          <td><strong>${t.bookingCode}</strong></td>
          <td>${t.patientName}</td>
          <td>${t.destinationAddress || t.hospitalName || 'Emergency Center'}</td>
          <td>${UI.formatStatusBadge(t.bookingStatus)}</td>
        </tr>
      `).join('');
    } catch (e) {
      console.warn('Could not load completed trips:', e);
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b; padding: 2rem;">No past trips recorded.</td></tr>`;
    }
  }
};
