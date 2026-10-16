/**
 * User Dashboard Engine
 */
const DashboardApp = {
  async init() {
    UI.renderNavbar('dashboard');

    if (!Auth.requireAuth(['USER', 'ADMIN'])) {
      return;
    }

    const user = Auth.getUser();
    const nameEl = document.getElementById('dash-user-name');
    const emailEl = document.getElementById('dash-user-email');

    if (nameEl && user) nameEl.textContent = user.fullName || 'User';
    if (emailEl && user) emailEl.textContent = `${user.email} (${user.phone || 'Phone pending'})`;

    await this.loadActiveBookingWidget();
    await this.loadRecentBookings();
  },

  async loadActiveBookingWidget() {
    const container = document.getElementById('active-booking-widget');
    if (!container) return;

    try {
      const bookings = await api.get('/bookings');
      const active = bookings.find(b => 
        b.bookingStatus !== 'COMPLETED' && b.bookingStatus !== 'CANCELLED' && b.bookingStatus !== 'REJECTED'
      );

      if (!active) {
        container.innerHTML = `
          <div class="card" style="background: #ffffff; text-align: center; padding: 2rem;">
            <p style="color: #64748b; margin-bottom: 0.75rem;">No active emergency booking right now.</p>
            <a href="booking.html" class="btn btn-danger btn-sm btn-pulse">Book Emergency Ambulance</a>
          </div>
        `;
        return;
      }

      container.innerHTML = `
        <div class="card" style="border: 2px solid #dc2626; background: #fffdfd;">
          <div class="card-header" style="border-bottom-color: #fecaca;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span class="badge badge-critical">DISPATCHED</span>
              <strong>${active.bookingCode}</strong>
            </div>
            ${UI.formatStatusBadge(active.bookingStatus)}
          </div>
          <div class="form-row" style="margin-bottom: 1rem;">
            <div>
              <span style="font-size: 0.75rem; color: #64748b;">PATIENT</span>
              <p style="font-weight: 700;">${active.patientName}</p>
            </div>
            <div>
              <span style="font-size: 0.75rem; color: #64748b;">ASSIGNED UNIT</span>
              <p style="font-weight: 600; color: #1e3a8a;">${active.ambulanceNumber || 'Locating nearest unit'}</p>
            </div>
            <div>
              <span style="font-size: 0.75rem; color: #64748b;">ESTIMATED ARRIVAL</span>
              <p style="font-weight: 700; color: #dc2626;">${active.estimatedArrivalTime || 'Under 8 mins'}</p>
            </div>
          </div>
          <div style="display: flex; gap: 0.75rem; justify-content: flex-end;">
            <a href="tracking.html?id=${active.id}" class="btn btn-primary btn-sm">Track Location</a>
            ${active.bookingStatus === 'REQUESTED' || active.bookingStatus === 'SEARCHING' || active.bookingStatus === 'ASSIGNED' ? 
              `<button onclick="DashboardApp.cancelBooking(${active.id})" class="btn btn-outline btn-sm" style="color: #dc2626;">Cancel</button>` : ''}
          </div>
        </div>
      `;
    } catch (e) {
      console.warn('Could not load active booking widget:', e);
    }
  },

  async loadRecentBookings() {
    const tableBody = document.getElementById('recent-bookings-tbody');
    const totalCountEl = document.getElementById('total-rides-count');
    const completedCountEl = document.getElementById('completed-rides-count');

    if (!tableBody) return;

    try {
      const bookings = await api.get('/bookings');
      if (totalCountEl) totalCountEl.textContent = bookings.length;
      if (completedCountEl) {
        completedCountEl.textContent = bookings.filter(b => b.bookingStatus === 'COMPLETED').length;
      }

      if (!bookings || bookings.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2rem;">No trip records found.</td></tr>`;
        return;
      }

      tableBody.innerHTML = bookings.slice(0, 5).map(b => `
        <tr>
          <td><strong>${b.bookingCode}</strong></td>
          <td>${b.patientName}</td>
          <td>${b.destinationAddress || b.hospitalName || 'Emergency Center'}</td>
          <td>${UI.formatEmergencyBadge(b.emergencyType)}</td>
          <td>${UI.formatStatusBadge(b.bookingStatus)}</td>
          <td>
            <a href="tracking.html?id=${b.id}" class="btn btn-outline btn-sm">Track Location</a>
          </td>
        </tr>
      `).join('');
    } catch (e) {
      console.warn('Could not load recent bookings:', e);
    }
  },

  async cancelBooking(bookingId) {
    UI.showConfirm(
      'Cancel Emergency Request',
      'Are you sure you want to cancel this emergency ambulance dispatch? An active unit will be released.',
      async () => {
        try {
          UI.showLoading(true, 'Cancelling request...');
          await api.put(`/bookings/${bookingId}/cancel`);
          UI.showLoading(false);
          UI.showToast('Booking cancelled successfully', 'info');
          this.loadActiveBookingWidget();
          this.loadRecentBookings();
        } catch (err) {
          UI.showLoading(false);
          UI.showToast(err.message || 'Could not cancel booking', 'error');
        }
      }
    );
  }
};
