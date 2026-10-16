/**
 * Admin Command Center & Management Controller
 */
const AdminApp = {
  async initDashboard() {
    UI.renderNavbar('admin-dashboard');
    if (!Auth.requireAuth(['ADMIN'])) return;
    await this.loadStats();
  },

  async loadStats() {
    try {
      const stats = await api.get('/admin/dashboard');

      document.getElementById('stat-users').textContent = stats.totalUsers || 0;
      document.getElementById('stat-drivers').textContent = stats.totalDrivers || 0;
      document.getElementById('stat-online-drivers').textContent = stats.onlineDrivers || 0;
      document.getElementById('stat-ambulances').textContent = stats.totalAmbulances || 0;
      document.getElementById('stat-avail-ambulances').textContent = stats.availableAmbulances || 0;
      document.getElementById('stat-active-trips').textContent = stats.activeBookings || 0;
      document.getElementById('stat-completed-trips').textContent = stats.completedBookings || 0;
      document.getElementById('stat-emergency-trips').textContent = stats.emergencyBookings || 0;

      this.renderCanvasChart(stats);
      this.renderRecentLogs(stats.recentBookings || []);
    } catch (e) {
      console.warn('Dashboard stats error:', e);
      UI.showToast(e.message, 'error');
    }
  },

  renderCanvasChart(stats) {
    const canvas = document.getElementById('analytics-chart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.parentElement.clientWidth;
    canvas.height = 240;

    const w = canvas.width;
    const h = canvas.height;

    // Background
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, w, h);

    const categories = [
      { label: 'Active', val: stats.activeBookings || 1, color: '#3b82f6' },
      { label: 'Completed', val: stats.completedBookings || 2, color: '#10b981' },
      { label: 'Critical', val: stats.emergencyBookings || 1, color: '#dc2626' },
      { label: 'Avail Fleet', val: stats.availableAmbulances || 4, color: '#059669' },
      { label: 'Online Crew', val: stats.onlineDrivers || 4, color: '#d97706' }
    ];

    const maxVal = Math.max(...categories.map(c => c.val), 5);
    const barWidth = Math.min(60, (w - 100) / categories.length);
    const spacing = (w - (barWidth * categories.length)) / (categories.length + 1);

    categories.forEach((cat, idx) => {
      const x = spacing + idx * (barWidth + spacing);
      const barHeight = (cat.val / maxVal) * (h - 70);
      const y = h - 40 - barHeight;

      // Draw Bar
      ctx.fillStyle = cat.color;
      ctx.beginPath();
      ctx.roundRect(x, y, barWidth, barHeight, [6, 6, 0, 0]);
      ctx.fill();

      // Draw value on top of bar
      ctx.fillStyle = '#0f172a';
      ctx.font = 'bold 12px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(cat.val, x + barWidth / 2, y - 6);

      // Draw label below bar
      ctx.fillStyle = '#64748b';
      ctx.font = '11px sans-serif';
      ctx.fillText(cat.label, x + barWidth / 2, h - 18);
    });
  },

  renderRecentLogs(list) {
    const tbody = document.getElementById('admin-recent-tbody');
    if (!tbody) return;

    if (!list || list.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2rem;">No recent dispatch records.</td></tr>`;
      return;
    }

    tbody.innerHTML = list.slice(0, 8).map(b => `
      <tr>
        <td><strong>${b.bookingCode}</strong></td>
        <td>${b.patientName}</td>
        <td>${b.ambulanceNumber || 'Pending'}</td>
        <td>${UI.formatEmergencyBadge(b.emergencyType)}</td>
        <td>${UI.formatStatusBadge(b.bookingStatus)}</td>
        <td>
          <a href="tracking.html?id=${b.id}" class="btn btn-outline btn-sm">Track Location</a>
        </td>
      </tr>
    `).join('');
  },

  // --- Users Management Page ---
  async initUsers() {
    UI.renderNavbar('admin-users');
    if (!Auth.requireAuth(['ADMIN'])) return;

    try {
      const users = await api.get('/admin/users');
      const tbody = document.getElementById('users-tbody');
      if (!tbody) return;

      tbody.innerHTML = users.map(u => `
        <tr>
          <td>#${u.id}</td>
          <td><strong>${u.fullName}</strong></td>
          <td>${u.email}</td>
          <td>${u.phone}</td>
          <td><span class="badge ${u.role === 'ADMIN' ? 'badge-critical' : (u.role === 'DRIVER' ? 'badge-ontrip' : 'badge-normal')}">${u.role}</span></td>
          <td>${UI.formatStatusBadge(u.status)}</td>
          <td>
            ${u.role !== 'ADMIN' ? `
              <button onclick="AdminApp.toggleUserStatus(${u.id}, '${u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'}')" class="btn btn-outline btn-sm" style="${u.status === 'PENDING_APPROVAL' ? 'background: #059669; color: white; border-color: #059669;' : ''}">
                ${u.status === 'PENDING_APPROVAL' ? 'Approve' : (u.status === 'ACTIVE' ? 'Suspend' : 'Activate')}
              </button>
            ` : '<span style="color: #94a3b8; font-size: 0.8rem;">Protected</span>'}
          </td>
        </tr>
      `).join('');
    } catch (e) {
      UI.showToast(e.message, 'error');
    }
  },

  async toggleUserStatus(id, newStatus) {
    try {
      await api.put(`/admin/users/${id}/status?status=${newStatus}`);
      UI.showToast(`User status set to ${newStatus}`, 'success');
      this.initUsers();
    } catch (e) {
      UI.showToast(e.message, 'error');
    }
  },

  // --- Ambulances Management Page ---
  async initAmbulances() {
    UI.renderNavbar('admin-ambulances');
    if (!Auth.requireAuth(['ADMIN'])) return;

    try {
      const fleet = await api.get('/admin/ambulances');
      const tbody = document.getElementById('ambulances-tbody');
      if (!tbody) return;

      if (!fleet || fleet.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2.5rem;">No ambulances in fleet yet. Click "+ Add New Ambulance" above to register a vehicle.</td></tr>`;
        return;
      }

      tbody.innerHTML = fleet.map(a => `
        <tr>
          <td><strong>${a.ambulanceNumber}</strong></td>
          <td><span class="badge ${a.ambulanceType === 'ICU' ? 'badge-critical' : (a.ambulanceType === 'ALS' ? 'badge-serious' : 'badge-normal')}">${a.ambulanceType}</span></td>
          <td>${a.driverName ? `${a.driverName} (${a.driverPhone || ''})` : '<span style="color: #94a3b8;">Unassigned</span>'}</td>
          <td>Capacity: ${a.capacity} Bed</td>
          <td>${UI.formatStatusBadge(a.availabilityStatus)}</td>
          <td>
            <button onclick="AdminApp.deleteAmbulance(${a.id})" class="btn btn-outline btn-sm" style="color: #dc2626;">Decommission</button>
          </td>
        </tr>
      `).join('');
    } catch (e) {
      const tbody = document.getElementById('ambulances-tbody');
      if (tbody) tbody.innerHTML = `<tr><td colspan="6" class="alert alert-danger">${e.message}</td></tr>`;
      UI.showToast(e.message, 'error');
    }
  },

  async handleAddAmbulance(e) {
    e.preventDefault();
    const number = document.getElementById('ambNumber').value.trim();
    const type = document.getElementById('ambType').value;
    const capacity = parseInt(document.getElementById('ambCapacity').value) || 1;
    const equipment = document.getElementById('ambEquipment').value.trim();

    try {
      await api.post('/ambulances', {
        ambulanceNumber: number,
        ambulanceType: type,
        capacity,
        equipmentDetails: equipment,
        currentLatitude: CONFIG.DEFAULT_LATITUDE,
        currentLongitude: CONFIG.DEFAULT_LONGITUDE
      });
      UI.showToast('New ambulance added to fleet!', 'success');
      document.getElementById('add-ambulance-modal').classList.remove('open');
      this.initAmbulances();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  },

  async deleteAmbulance(id) {
    UI.showConfirm('Decommission Unit', 'Are you sure you want to remove this ambulance from service?', async () => {
      try {
        await api.delete(`/ambulances/${id}`);
        UI.showToast('Ambulance removed', 'success');
        this.initAmbulances();
      } catch (e) {
        UI.showToast(e.message, 'error');
      }
    });
  },

  // --- Hospitals Management Page ---
  async initHospitals() {
    UI.renderNavbar('admin-hospitals');
    if (!Auth.requireAuth(['ADMIN'])) return;

    try {
      const list = await api.get('/admin/hospitals');
      const tbody = document.getElementById('hospitals-tbody');
      if (!tbody) return;

      if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2.5rem;">No hospitals registered yet. Click "+ Add New Hospital" to register a trauma center.</td></tr>`;
        return;
      }

      tbody.innerHTML = list.map(h => `
        <tr>
          <td><strong>${h.name}</strong></td>
          <td>${h.address}</td>
          <td><a href="tel:${h.phone}" style="color: #1e3a8a; text-decoration: none;">${h.phone}</a></td>
          <td>${h.emergencyAvailable ? '<span class="badge badge-available">24/7 Trauma</span>' : '<span class="badge badge-maintenance">Limited</span>'}</td>
          <td>${h.icuAvailable ? '<span class="badge badge-assigned">ICU Ready</span>' : '<span class="badge badge-offline">No ICU</span>'}</td>
          <td>
            <button onclick="AdminApp.deleteHospital(${h.id})" class="btn btn-outline btn-sm" style="color: #dc2626;">Delete</button>
          </td>
        </tr>
      `).join('');
    } catch (e) {
      const tbody = document.getElementById('hospitals-tbody');
      if (tbody) tbody.innerHTML = `<tr><td colspan="6" class="alert alert-danger">${e.message}</td></tr>`;
      UI.showToast(e.message, 'error');
    }
  },

  async handleAddHospital(e) {
    e.preventDefault();
    const name = document.getElementById('hospName').value.trim();
    const address = document.getElementById('hospAddress').value.trim();
    const phone = document.getElementById('hospPhone').value.trim();
    const lat = parseFloat(document.getElementById('hospLat').value) || CONFIG.DEFAULT_LATITUDE;
    const lon = parseFloat(document.getElementById('hospLon').value) || CONFIG.DEFAULT_LONGITUDE;
    const icu = document.getElementById('hospIcu').checked;

    try {
      await api.post('/hospitals', {
        name,
        address,
        phone,
        latitude: lat,
        longitude: lon,
        emergencyAvailable: true,
        icuAvailable: icu,
        description: 'Multi-specialty emergency facility'
      });
      UI.showToast('Hospital added successfully!', 'success');
      document.getElementById('add-hospital-modal').classList.remove('open');
      this.initHospitals();
    } catch (err) {
      UI.showToast(err.message, 'error');
    }
  },

  async deleteHospital(id) {
    UI.showConfirm('Delete Hospital', 'Are you sure you want to delete this hospital record?', async () => {
      try {
        await api.delete(`/hospitals/${id}`);
        UI.showToast('Hospital deleted', 'success');
        this.initHospitals();
      } catch (e) {
        UI.showToast(e.message, 'error');
      }
    });
  },

  // --- Drivers Management Page ---
  async initDrivers() {
    UI.renderNavbar('admin-drivers');
    if (!Auth.requireAuth(['ADMIN'])) return;

    try {
      const drivers = await api.get('/admin/drivers');
      const tbody = document.getElementById('drivers-tbody');
      if (!tbody) return;

      if (!drivers || drivers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2.5rem;">No drivers registered in system yet.</td></tr>`;
        return;
      }

      tbody.innerHTML = drivers.map(d => `
        <tr>
          <td><strong>${d.fullName}</strong></td>
          <td>${d.email}</td>
          <td><a href="tel:${d.phone}" style="color: #1e3a8a; text-decoration: none;">${d.phone}</a></td>
          <td><code>${d.licenseNumber}</code></td>
          <td><strong>${d.assignedAmbulanceNumber || '<span style="color: #94a3b8;">Unassigned</span>'}</strong></td>
          <td>${UI.formatStatusBadge(d.availabilityStatus)}</td>
        </tr>
      `).join('');
    } catch (e) {
      const tbody = document.getElementById('drivers-tbody');
      if (tbody) tbody.innerHTML = `<tr><td colspan="6" class="alert alert-danger">${e.message}</td></tr>`;
      UI.showToast(e.message, 'error');
    }
  },

  // --- Bookings Management Page ---
  async initBookings() {
    UI.renderNavbar('admin-bookings');
    if (!Auth.requireAuth(['ADMIN'])) return;

    try {
      const list = await api.get('/admin/bookings');
      const tbody = document.getElementById('bookings-tbody');
      if (!tbody) return;

      if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #64748b; padding: 2.5rem;">No emergency dispatches recorded yet.</td></tr>`;
        return;
      }

      tbody.innerHTML = list.map(b => `
        <tr>
          <td><strong>${b.bookingCode}</strong></td>
          <td>${b.patientName} (${b.patientPhone})</td>
          <td>${b.pickupAddress}</td>
          <td>${b.destinationAddress || b.hospitalName || 'Central Trauma Facility'}</td>
          <td>${UI.formatEmergencyBadge(b.emergencyType)}</td>
          <td>${UI.formatStatusBadge(b.bookingStatus)}</td>
          <td>
            <a href="tracking.html?id=${b.id}" class="btn btn-outline btn-sm">Track Location</a>
          </td>
        </tr>
      `).join('');
    } catch (e) {
      const tbody = document.getElementById('bookings-tbody');
      if (tbody) tbody.innerHTML = `<tr><td colspan="7" class="alert alert-danger">${e.message}</td></tr>`;
      UI.showToast(e.message, 'error');
    }
  }
};
