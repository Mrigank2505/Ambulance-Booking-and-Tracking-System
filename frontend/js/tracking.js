/**
 * ====================================================================
 * AMBULANCE TRACKING SYSTEM - CORE TRACKING CONTROLLER
 * File: js/tracking.js
 * Clean Medical Dashboard - Zero Emojis / Decorative Icons
 * ====================================================================
 */

const TrackingApp = {
  // Application State
  currentBookingId: null,
  bookingData: null,
  telemetryData: null,
  isDemoMode: false,
  isSimPaused: false,
  simTimer: null,
  simWaypointIndex: 0,
  pollTimer: null,
  wsSocket: null,

  // Leaflet Map State
  map: null,
  ambulanceMarker: null,
  pickupMarker: null,
  hospitalMarker: null,
  userLocationMarker: null,
  routePolyline: null,
  routePolylineBg: null,
  regionalMarkers: [],

  // Real Tamil Nadu State Emergency Network Locations
  tamilNaduLocations: [
    { name: 'Chennai', coords: [13.0827, 80.2707], hospital: 'Rajiv Gandhi Government General Hospital (RGGGH)', beds: '45 Emergency Beds', type: 'Level 1 Trauma' },
    { name: 'Kanchipuram', coords: [12.8342, 79.7036], hospital: 'Kanchipuram District Headquarters Hospital', beds: '20 Emergency Beds', type: 'District Center' },
    { name: 'Vellore', coords: [12.9165, 79.1325], hospital: 'Christian Medical College (CMC Trauma Unit)', beds: '35 ICU Beds', type: 'Super-Specialty' },
    { name: 'Salem', coords: [11.6643, 78.1460], hospital: 'Salem Govt Mohan Kumaramangalam Medical College', beds: '28 ICU Beds', type: 'Regional Care' },
    { name: 'Erode', coords: [11.3410, 77.7172], hospital: 'Erode Government District Headquarters Hospital', beds: '18 Emergency Beds', type: 'District Node' },
    { name: 'Coimbatore', coords: [11.0168, 76.9558], hospital: 'Coimbatore Medical College Hospital & Trauma Center', beds: '40 ICU Beds', type: 'Apex Trauma' },
    { name: 'Tiruchirappalli', coords: [10.7905, 78.7047], hospital: 'K.A.P. Viswanatham Government Medical College Hospital', beds: '30 ICU Beds', type: 'Central Zone Hub' },
    { name: 'Thanjavur', coords: [10.7870, 79.1378], hospital: 'Thanjavur Medical College Hospital', beds: '25 Emergency Beds', type: 'Delta Care' },
    { name: 'Madurai', coords: [9.9252, 78.1198], hospital: 'Government Rajaji Hospital (GRH Madurai)', beds: '42 Emergency Beds', type: 'South Zone Apex' },
    { name: 'Tirunelveli', coords: [8.7139, 77.7567], hospital: 'Tirunelveli Government Medical College Hospital', beds: '24 Emergency Beds', type: 'Regional Hub' },
    { name: 'Kanniyakumari', coords: [8.0883, 77.5385], hospital: 'Kanyakumari Govt Medical College Hospital, Asaripallam', beds: '20 Emergency Beds', type: 'Coastal Center' }
  ],

  // Real Tamil Nadu Corridors (Demonstration mode data)
  demoBookings: {
    'BK-1001': {
      id: 1001,
      bookingCode: 'BK-1001',
      patientName: 'Sarah Jenkins',
      patientPhone: '+91 94440 12891',
      pickupAddress: 'Anna Nagar 2nd Avenue, Chennai, Tamil Nadu',
      pickupLatitude: 13.0850,
      pickupLongitude: 80.2100,
      destinationAddress: 'EVR Periyar Salai, Park Town, Chennai, Tamil Nadu',
      hospitalName: 'Rajiv Gandhi Government General Hospital (RGGGH)',
      hospitalLatitude: 13.0818,
      hospitalLongitude: 80.2785,
      ambulanceId: 'AMB-ALS-204',
      ambulanceNumber: 'TN-01-AL-204',
      ambulanceType: 'ALS (Advanced Life Support)',
      driverName: 'Michael Ross',
      driverPhone: '+91 98401 22834',
      driverRating: 'Rating: 4.9 (420+ trips)',
      bookingStatus: 'ON_THE_WAY',
      stageIndex: 4,
      distanceRemainingKm: 3.4,
      etaMinutes: 6,
      speedKmH: 48,
      emergencyType: 'CARDIAC EMERGENCY',
      specs: ['Ventilator Ready', 'Defibrillator', 'Oxygen Flow', 'Real-time GPS'],
      waypoints: [
        [13.0850, 80.2100],
        [13.0835, 80.2240],
        [13.0820, 80.2410],
        [13.0810, 80.2580],
        [13.0805, 80.2690],
        [13.0818, 80.2785]
      ]
    },
    'BK-1002': {
      id: 1002,
      bookingCode: 'BK-1002',
      patientName: 'Robert Sterling',
      patientPhone: '+91 97890 44112',
      pickupAddress: 'Peelamedu, Avinashi Road, Coimbatore, Tamil Nadu',
      pickupLatitude: 11.0260,
      pickupLongitude: 77.0020,
      destinationAddress: 'Avinashi Road, Coimbatore, Tamil Nadu',
      hospitalName: 'Coimbatore Medical College Hospital',
      hospitalLatitude: 11.0015,
      hospitalLongitude: 76.9660,
      ambulanceId: 'AMB-ICU-108',
      ambulanceNumber: 'TN-38-IC-108',
      ambulanceType: 'ICU Mobile Critical Care',
      driverName: 'Elena Vance',
      driverPhone: '+91 94432 99182',
      driverRating: 'Rating: 4.95 (580+ trips)',
      bookingStatus: 'ASSIGNED',
      stageIndex: 2,
      distanceRemainingKm: 5.2,
      etaMinutes: 10,
      speedKmH: 44,
      emergencyType: 'TRAUMA / ACCIDENT',
      specs: ['ECMO Portable', 'Bi-Phasic Shock', 'Syringe Pumps', 'Cryo Box'],
      waypoints: [
        [11.0260, 77.0020],
        [11.0200, 76.9930],
        [11.0150, 76.9840],
        [11.0090, 76.9750],
        [11.0015, 76.9660]
      ]
    },
    'BK-1003': {
      id: 1003,
      bookingCode: 'BK-1003',
      patientName: 'David Miller',
      patientPhone: '+91 98940 71234',
      pickupAddress: 'KK Nagar East 4th Street, Madurai, Tamil Nadu',
      pickupLatitude: 9.9270,
      pickupLongitude: 78.1450,
      destinationAddress: 'Alagar Kovil Road, Goripalayam, Madurai, Tamil Nadu',
      hospitalName: 'Government Rajaji Hospital (GRH), Madurai',
      hospitalLatitude: 9.9260,
      hospitalLongitude: 78.1250,
      ambulanceId: 'AMB-BLS-301',
      ambulanceNumber: 'TN-58-BL-301',
      ambulanceType: 'BLS (Basic Life Support)',
      driverName: 'Carlos Gomez',
      driverPhone: '+91 97910 88901',
      driverRating: 'Rating: 4.8 (310+ trips)',
      bookingStatus: 'REACHED_HOSPITAL',
      stageIndex: 6,
      distanceRemainingKm: 0.1,
      etaMinutes: 0,
      speedKmH: 0,
      emergencyType: 'GENERAL MEDICAL',
      specs: ['Basic Oxygen', 'First-Aid Trauma', 'Stretcher Trolley'],
      waypoints: [
        [9.9270, 78.1450],
        [9.9265, 78.1380],
        [9.9262, 78.1310],
        [9.9260, 78.1250]
      ]
    }
  },

  /**
   * Initialize module from URL query parameter (?id=... or ?code=...)
   */
  initFromURL() {
    this.setupEventListeners();

    const params = new URLSearchParams(window.location.search);
    const idParam = params.get('id') || params.get('code') || params.get('bookingId');

    if (idParam) {
      const inputEl = document.getElementById('booking-id-input');
      if (inputEl) inputEl.value = idParam.trim();
      this.trackBookingById(idParam.trim());
    } else {
      // Initialize real interactive Tamil Nadu emergency map centered near Chennai
      this.renderTamilNaduOverviewMap();
    }
  },

  /**
   * Set up all UI interaction listeners
   */
  setupEventListeners() {
    // Form submission
    const form = document.getElementById('tracking-search-form');
    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        const input = document.getElementById('booking-id-input');
        const id = input ? input.value.trim() : '';
        this.trackBookingById(id);
      });
    }

    // Geolocation permission button
    const locationBtn = document.getElementById('btn-use-location');
    if (locationBtn) {
      locationBtn.addEventListener('click', () => this.requestUserLocation());
    }

    // Map controls
    const btnCenterAmb = document.getElementById('btn-center-ambulance');
    if (btnCenterAmb) {
      btnCenterAmb.addEventListener('click', () => this.centerOnAmbulance());
    }

    const btnCenterRoute = document.getElementById('btn-center-route');
    if (btnCenterRoute) {
      btnCenterRoute.addEventListener('click', () => this.fitRouteBounds());
    }

    // Demo simulation controls
    const btnDemoToggle = document.getElementById('btn-demo-toggle');
    if (btnDemoToggle) {
      btnDemoToggle.addEventListener('click', () => this.toggleDemoPause());
    }

    const btnDemoStep = document.getElementById('btn-demo-step');
    if (btnDemoStep) {
      btnDemoStep.addEventListener('click', () => this.stepDemoSimulation(true));
    }

    const btnDemoReset = document.getElementById('btn-demo-reset');
    if (btnDemoReset) {
      btnDemoReset.addEventListener('click', () => this.resetDemoSimulation());
    }
  },

  /**
   * Track booking by ID or Code with validation and error states
   */
  async trackBookingById(rawId) {
    this.hideError();
    const input = document.getElementById('booking-id-input');
    if (input) {
      input.classList.remove('input-error');
      input.value = rawId;
    }

    if (!rawId || !rawId.trim()) {
      this.showError('Please enter a valid Booking ID or Reference Code.');
      if (input) input.classList.add('input-error');
      return;
    }

    const id = rawId.trim();
    this.currentBookingId = id;

    const trackBtn = document.getElementById('btn-track-ambulance');
    if (typeof UI !== 'undefined' && UI.setButtonLoading) {
      UI.setButtonLoading(trackBtn, true, 'Locating Unit...');
    }

    // Stop any existing simulation or polling
    this.stopDemoSimulation();
    if (this.pollTimer) clearInterval(this.pollTimer);

    try {
      // 1. First attempt to fetch from backend API
      let booking = null;
      let isBackendConnected = false;

      try {
        const endpoint = id.toString().toUpperCase().startsWith('BK-')
          ? `/bookings/code/${encodeURIComponent(id)}`
          : `/bookings/${encodeURIComponent(id)}`;

        booking = await api.get(endpoint);
        if (booking && booking.id) {
          isBackendConnected = true;
        }
      } catch (backendErr) {
        console.info('Backend request unavailable, checking demo registry:', backendErr.message);
      }

      // 2. If backend found the booking, use real backend data
      if (isBackendConnected && booking) {
        this.isDemoMode = false;
        this.bookingData = booking;
        this.setDemoBannerVisible(false);
        this.setConnectionStatus('Live GPS Backend Connected', '#059669');

        this.renderBookingDetails(this.bookingData);
        this.renderTimeline(this.mapStatusToStage(this.bookingData.bookingStatus));
        this.initOrUpdateMap();

        // Connect real telemetry
        this.connectBackendTelemetry(this.bookingData.id);
      } else {
        // 3. Check Demo Bookings registry or numeric alias
        const demoKey = id.toUpperCase();
        let demoMatch = this.demoBookings[demoKey];

        if (!demoMatch) {
          if (id === '1' || id === '1001') demoMatch = this.demoBookings['BK-1001'];
          else if (id === '2' || id === '1002') demoMatch = this.demoBookings['BK-1002'];
          else if (id === '3' || id === '1003') demoMatch = this.demoBookings['BK-1003'];
        }

        if (demoMatch) {
          this.isDemoMode = true;
          this.bookingData = JSON.parse(JSON.stringify(demoMatch));
          this.setDemoBannerVisible(true);
          this.setConnectionStatus('Standalone Demo Mode (Simulated)', '#d97706');

          this.renderBookingDetails(this.bookingData);
          this.renderTimeline(this.bookingData.stageIndex);
          this.initOrUpdateMap();

          // Start animated simulation along demo route
          this.startDemoSimulation();
        } else {
          this.showError(`No active booking found for "${id}". Please enter a valid booking ID or test with demo codes: BK-1001, BK-1002, or BK-1003.`);
          if (input) input.classList.add('input-error');
          this.hideDashboard();
          return;
        }
      }

      // Show active dashboard
      this.showDashboard();
      if (typeof UI !== 'undefined' && UI.showToast) {
        UI.showToast(`Tracking details loaded for ${this.bookingData.bookingCode}`, 'success');
      }

    } catch (err) {
      this.showError(err.message || 'Error occurred while loading tracking information.');
      this.hideDashboard();
    } finally {
      if (typeof UI !== 'undefined' && UI.setButtonLoading) {
        UI.setButtonLoading(trackBtn, false);
      }
    }
  },

  /**
   * Map Booking Status String to 6-stage number
   * 1. Booking Confirmed
   * 2. Ambulance Assigned
   * 3. Driver Accepted
   * 4. Ambulance On the Way
   * 5. Patient Picked Up
   * 6. Reached Hospital
   */
  mapStatusToStage(status) {
    if (!status) return 1;
    const s = status.toUpperCase();

    if (s === 'REQUESTED' || s === 'SEARCHING' || s === 'CONFIRMED') return 1;
    if (s === 'ASSIGNED') return 2;
    if (s === 'DRIVER_ACCEPTED' || s === 'ACCEPTED') return 3;
    if (s === 'ON_THE_WAY' || s === 'EN_ROUTE' || s === 'ARRIVED') return 4;
    if (s === 'PATIENT_PICKED_UP' || s === 'PICKED_UP') return 5;
    if (s === 'REACHED_HOSPITAL' || s === 'COMPLETED' || s === 'DELIVERED') return 6;
    return 1;
  },

  /**
   * Render all text details, HUD metrics, and driver info
   */
  renderBookingDetails(b) {
    // HUD Metrics
    const etaEl = document.getElementById('hud-eta');
    const distEl = document.getElementById('hud-distance');
    const speedEl = document.getElementById('hud-speed');
    const statusBadgeEl = document.getElementById('hud-status-badge');
    const availTextEl = document.getElementById('hud-availability-text');

    if (etaEl) {
      etaEl.textContent = b.etaMinutes !== undefined && b.etaMinutes !== null 
        ? (b.etaMinutes === 0 ? 'Arrived' : `${b.etaMinutes} mins`)
        : (b.estimatedArrivalTime || '-- mins');
    }

    if (distEl) {
      distEl.textContent = b.distanceRemainingKm !== undefined && b.distanceRemainingKm !== null
        ? `${Number(b.distanceRemainingKm).toFixed(1)} km`
        : (b.distanceKm ? `${b.distanceKm} km` : '-- km');
    }

    if (speedEl) {
      speedEl.textContent = b.speedKmH !== undefined 
        ? `${Math.round(b.speedKmH)} km/h` 
        : (b.speed ? `${Math.round(b.speed)} km/h` : '42 km/h');
    }

    const currentStatus = (b.bookingStatus || 'DISPATCHED').replace(/_/g, ' ');
    if (statusBadgeEl) {
      statusBadgeEl.innerHTML = `<span class="badge badge-ontrip">${currentStatus}</span>`;
    }

    if (availTextEl) {
      availTextEl.textContent = `Unit Status: ${currentStatus}`;
    }

    // Detail Column Cards
    const codeEl = document.getElementById('detail-booking-code');
    const pickupAddrEl = document.getElementById('detail-pickup-address');
    const patientMetaEl = document.getElementById('detail-patient-meta');
    const hospNameEl = document.getElementById('detail-hospital-name');
    const destAddrEl = document.getElementById('detail-destination-address');
    const emergTypeEl = document.getElementById('detail-emergency-type');

    if (codeEl) codeEl.textContent = b.bookingCode || `BK-${b.id}`;
    if (pickupAddrEl) pickupAddrEl.textContent = b.pickupAddress || 'Verified Emergency Coordinate';
    if (patientMetaEl) patientMetaEl.textContent = `Patient: ${b.patientName || 'Anonymous'} (${b.patientPhone || 'Direct Dispatch'})`;
    if (hospNameEl) hospNameEl.textContent = b.hospitalName || 'Emergency Trauma Center';
    if (destAddrEl) destAddrEl.textContent = b.destinationAddress || b.hospitalName || 'Designated Regional Hospital';
    if (emergTypeEl) emergTypeEl.textContent = b.emergencyType || 'HIGH PRIORITY';

    // Vehicle Card
    const ambIdEl = document.getElementById('detail-ambulance-id');
    const ambTypeEl = document.getElementById('detail-ambulance-type');
    const ambStatusEl = document.getElementById('detail-ambulance-status');
    const specsContainer = document.getElementById('detail-specs-container');

    if (ambIdEl) ambIdEl.textContent = b.ambulanceId || b.ambulanceNumber || 'AMB-DISPATCH-01';
    if (ambTypeEl) ambTypeEl.textContent = b.ambulanceType || 'ADVANCED LIFE SUPPORT';
    if (ambStatusEl) ambStatusEl.textContent = currentStatus;

    if (specsContainer && b.specs) {
      specsContainer.innerHTML = b.specs.map(s => `<span class="spec-pill">${s}</span>`).join('');
    }

    // Driver Card
    const driverNameEl = document.getElementById('detail-driver-name');
    const driverAvatarEl = document.getElementById('detail-driver-avatar');
    const driverRatingEl = document.getElementById('detail-driver-rating');
    const driverPhoneEl = document.getElementById('detail-driver-phone-display');
    const callBtn = document.getElementById('btn-call-driver');

    const dName = b.driverName || 'Central EMT Pilot';
    const dPhone = b.driverPhone || '+1 (555) 019-2834';

    if (driverNameEl) driverNameEl.textContent = dName;
    if (driverAvatarEl) {
      const initials = dName.split(' ').map(n => n[0]).join('').substring(0, 2);
      driverAvatarEl.textContent = initials || 'DR';
    }
    if (driverRatingEl) driverRatingEl.textContent = b.driverRating || 'Rating: 4.9 (400+ trips)';
    if (driverPhoneEl) driverPhoneEl.textContent = `Direct Phone: ${dPhone}`;
    if (callBtn) callBtn.href = `tel:${dPhone.replace(/[^0-9+]/g, '')}`;
  },

  /**
   * Render the 6-stage booking progress timeline
   */
  renderTimeline(activeStageNumber) {
    const stageNum = Math.min(6, Math.max(1, activeStageNumber));

    const progressBar = document.getElementById('stepper-progress-bar');
    if (progressBar) {
      const percentage = ((stageNum - 1) / 5) * 100;
      progressBar.style.width = `${percentage}%`;
    }

    const stageNames = [
      'Booking Confirmed',
      'Ambulance Assigned',
      'Driver Accepted',
      'Ambulance On the Way',
      'Patient Picked Up',
      'Reached Hospital'
    ];

    for (let i = 1; i <= 6; i++) {
      const nodeEl = document.getElementById(`timeline-step-${i}`);
      const circleEl = document.getElementById(`circle-${i}`);
      const timeEl = document.getElementById(`time-step-${i}`);

      if (!nodeEl || !circleEl) continue;

      nodeEl.classList.remove('completed', 'active');

      if (i < stageNum) {
        nodeEl.classList.add('completed');
        circleEl.textContent = i;
        if (timeEl) timeEl.textContent = 'Completed';
      } else if (i === stageNum) {
        nodeEl.classList.add('active');
        circleEl.textContent = i;
        if (timeEl) timeEl.textContent = 'In Progress';
      } else {
        circleEl.textContent = i;
        if (timeEl) timeEl.textContent = 'Pending';
      }
    }

    const currentBadge = document.getElementById('timeline-current-status-badge');
    if (currentBadge) {
      currentBadge.innerHTML = `<span class="badge ${stageNum >= 5 ? 'badge-available' : 'badge-ontrip'}">${stageNames[stageNum - 1].toUpperCase()}</span>`;
    }
  },

  /**
   * Render initial real Tamil Nadu State Overview Map
   * Centered near Chennai at [13.0827, 80.2707], initial zoom 7.5
   * Displays all 11 Tamil Nadu trauma care and district hubs
   */
  renderTamilNaduOverviewMap() {
    const mapContainer = document.getElementById('tracking-map');
    if (!mapContainer) return;

    if (typeof L === 'undefined') {
      console.warn('Leaflet library loading...');
      setTimeout(() => this.renderTamilNaduOverviewMap(), 250);
      return;
    }

    if (!this.map) {
      this.map = L.map('tracking-map', {
        zoomControl: true,
        attributionControl: true
      }).setView([13.0827, 80.2707], 7.5);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors | Tamil Nadu Emergency Medical Services'
      }).addTo(this.map);
    } else {
      this.map.setView([13.0827, 80.2707], 7.5);
    }

    // Clear previous dynamic layers
    this.clearMapLayers();

    // Render interactive nodes for all 11 Tamil Nadu locations
    this.regionalMarkers = [];
    this.tamilNaduLocations.forEach(loc => {
      const nodeIcon = L.divIcon({
        className: 'custom-leaflet-marker',
        html: `
          <div class="marker-inner hospital" style="width: 32px; height: 32px; font-size: 0.725rem; background: #1e3a8a; border: 2px solid #ffffff; box-shadow: 0 2px 6px rgba(0,0,0,0.3); color: #ffffff;">TN</div>
        `,
        iconSize: [32, 32],
        iconAnchor: [16, 16]
      });

      const marker = L.marker(loc.coords, { icon: nodeIcon })
        .addTo(this.map)
        .bindPopup(`
          <div style="font-size: 0.8125rem;">
            <strong style="color: #0f172a; font-size: 0.9rem;">${loc.name} Emergency Trauma Center</strong><br>
            <span style="color: #475569;">${loc.hospital}</span><br>
            <div style="margin-top: 0.35rem; display: flex; justify-content: space-between; align-items: center;">
              <span class="badge badge-available" style="font-size: 0.675rem;">${loc.beds}</span>
              <span style="font-size: 0.7rem; color: #1e3a8a; font-weight: 700;">${loc.type}</span>
            </div>
          </div>
        `);
      this.regionalMarkers.push(marker);
    });

    // Update HUD metrics to State Standby Mode
    const etaEl = document.getElementById('hud-eta');
    const distEl = document.getElementById('hud-distance');
    const speedEl = document.getElementById('hud-speed');
    const statusBadgeEl = document.getElementById('hud-status-badge');
    const availTextEl = document.getElementById('hud-availability-text');
    const connectionText = document.getElementById('connection-status-text');

    if (etaEl) etaEl.textContent = 'Standby';
    if (distEl) distEl.textContent = '11 Hubs';
    if (speedEl) speedEl.textContent = '100% Monitored';
    if (statusBadgeEl) statusBadgeEl.innerHTML = '<span class="badge badge-available">TAMIL NADU GRID</span>';
    if (availTextEl) availTextEl.textContent = 'All Regional Corridors Linked';
    if (connectionText) connectionText.textContent = 'Tamil Nadu Network Active';

    setTimeout(() => {
      if (this.map) this.map.invalidateSize();
    }, 250);
  },

  clearMapLayers() {
    if (this.ambulanceMarker && this.map) this.map.removeLayer(this.ambulanceMarker);
    if (this.pickupMarker && this.map) this.map.removeLayer(this.pickupMarker);
    if (this.hospitalMarker && this.map) this.map.removeLayer(this.hospitalMarker);
    if (this.routePolyline && this.map) this.map.removeLayer(this.routePolyline);
    if (this.routePolylineBg && this.map) this.map.removeLayer(this.routePolylineBg);
    if (this.regionalMarkers && this.map) {
      this.regionalMarkers.forEach(m => this.map.removeLayer(m));
      this.regionalMarkers = [];
    }
  },

  /**
   * Initialize or update Leaflet Map for specific active booking
   * Valid Tamil Nadu road routing, actual GPS coordinates & OpenStreetMap integration
   */
  initOrUpdateMap() {
    const b = this.bookingData;
    if (!b) return;

    // Use Tamil Nadu coordinates (default near Chennai if not specified)
    const pLat = b.pickupLatitude || 13.0827;
    const pLon = b.pickupLongitude || 80.2707;
    const hLat = b.hospitalLatitude || 13.0818;
    const hLon = b.hospitalLongitude || 80.2785;

    let ambLat = pLat;
    let ambLon = pLon;

    if (b.waypoints && b.waypoints.length > 0) {
      const wp = b.waypoints[this.simWaypointIndex || 0];
      ambLat = wp[0];
      ambLon = wp[1];
    }

    const mapContainer = document.getElementById('tracking-map');
    if (!mapContainer) return;

    if (typeof L === 'undefined') {
      console.warn('Leaflet library loading...');
      setTimeout(() => this.initOrUpdateMap(), 250);
      return;
    }

    if (!this.map) {
      this.map = L.map('tracking-map', {
        zoomControl: true,
        attributionControl: true
      }).setView([pLat, pLon], 12);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors | Tamil Nadu Emergency Response'
      }).addTo(this.map);
    }

    this.clearMapLayers();

    // 1. Patient Pickup Marker - Clean text "P"
    const patientIcon = L.divIcon({
      className: 'custom-leaflet-marker',
      html: `
        <div class="marker-inner patient">P</div>
        <div class="marker-radar" style="border-color: #dc2626; background: rgba(220, 38, 38, 0.25);"></div>
      `,
      iconSize: [34, 34],
      iconAnchor: [17, 17]
    });

    this.pickupMarker = L.marker([pLat, pLon], { icon: patientIcon })
      .addTo(this.map)
      .bindPopup(`<strong>Patient Pickup Location</strong><br>${b.pickupAddress || 'Pickup Point'}<br>Patient: ${b.patientName || 'Emergency Call'}`);

    // 2. Hospital Marker - Clean text "H"
    const hospitalIcon = L.divIcon({
      className: 'custom-leaflet-marker',
      html: `<div class="marker-inner hospital">H</div>`,
      iconSize: [34, 34],
      iconAnchor: [17, 17]
    });

    this.hospitalMarker = L.marker([hLat, hLon], { icon: hospitalIcon })
      .addTo(this.map)
      .bindPopup(`<strong>${b.hospitalName || 'Destination Hospital'}</strong><br>${b.destinationAddress || 'Trauma Center'}`);

    // 3. Ambulance Moving Marker - Clean text "AMB"
    const ambulanceIcon = L.divIcon({
      className: 'custom-leaflet-marker',
      html: `
        <div class="marker-inner ambulance">AMB</div>
        <div class="marker-radar"></div>
      `,
      iconSize: [38, 38],
      iconAnchor: [19, 19]
    });

    this.ambulanceMarker = L.marker([ambLat, ambLon], { icon: ambulanceIcon })
      .addTo(this.map)
      .bindPopup(`<strong>Assigned Unit: ${b.ambulanceNumber || b.ambulanceId || 'AMB-TN'}</strong><br>Driver: ${b.driverName || 'EMT Crew'}<br>Speed: ${b.speedKmH || 44} km/h`);

    // 4. Draw Valid Road Route Polyline
    let routeCoords = [];
    if (b.waypoints && b.waypoints.length > 0) {
      routeCoords = b.waypoints;
    } else {
      routeCoords = [
        [ambLat, ambLon],
        [pLat, pLon],
        [hLat, hLon]
      ];
    }

    this.routePolylineBg = L.polyline(routeCoords, {
      color: '#1e3a8a',
      weight: 6,
      opacity: 0.25,
      lineCap: 'round'
    }).addTo(this.map);

    this.routePolyline = L.polyline(routeCoords, {
      color: '#dc2626',
      weight: 3.5,
      opacity: 0.9,
      dashArray: '6, 6',
      lineCap: 'round'
    }).addTo(this.map);

    this.fitRouteBounds();

    setTimeout(() => {
      if (this.map) this.map.invalidateSize();
    }, 250);
  },

  fitRouteBounds() {
    if (!this.map || !this.routePolyline) return;
    try {
      this.map.fitBounds(this.routePolyline.getBounds(), {
        padding: [50, 50],
        maxZoom: 15
      });
    } catch (e) {
      console.warn('Could not fit route bounds:', e);
    }
  },

  centerOnAmbulance() {
    if (!this.map || !this.ambulanceMarker) return;
    const latlng = this.ambulanceMarker.getLatLng();
    this.map.setView(latlng, 15, { animate: true });
    this.ambulanceMarker.openPopup();
  },

  requestUserLocation() {
    const btn = document.getElementById('btn-use-location');
    if (btn) btn.disabled = true;

    if (!navigator.geolocation) {
      if (typeof UI !== 'undefined' && UI.showToast) {
        UI.showToast('Geolocation is not supported by your browser.', 'error');
      }
      if (btn) btn.disabled = false;
      return;
    }

    if (typeof UI !== 'undefined' && UI.showToast) {
      UI.showToast('Requesting GPS location permission...', 'info');
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        if (btn) btn.disabled = false;
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;

        if (this.map) {
          if (this.userLocationMarker) this.map.removeLayer(this.userLocationMarker);

          const userIcon = L.divIcon({
            className: 'custom-leaflet-marker',
            html: `<div class="marker-inner user-location">YOU</div>`,
            iconSize: [36, 36],
            iconAnchor: [18, 18]
          });

          this.userLocationMarker = L.marker([lat, lon], { icon: userIcon })
            .addTo(this.map)
            .bindPopup(`<strong>Your Current Location</strong><br>Coordinates: ${lat.toFixed(4)}, ${lon.toFixed(4)}`)
            .openPopup();

          this.map.setView([lat, lon], 14, { animate: true });
        }

        if (typeof UI !== 'undefined' && UI.showToast) {
          UI.showToast(`Your location acquired (${lat.toFixed(4)}, ${lon.toFixed(4)})`, 'success');
        }
      },
      (err) => {
        if (btn) btn.disabled = false;
        console.warn('Geolocation denied or failed:', err.message);
        if (typeof UI !== 'undefined' && UI.showToast) {
          UI.showToast('Location permission denied or unavailable. Using default map center.', 'warning');
        }
      },
      { timeout: 8000, enableHighAccuracy: true }
    );
  },

  // ====================================================================
  // DEMO SIMULATION LOOP
  // ====================================================================
  startDemoSimulation() {
    this.stopDemoSimulation();
    this.isSimPaused = false;
    this.simWaypointIndex = 0;

    const toggleBtn = document.getElementById('btn-demo-toggle');
    if (toggleBtn) {
      toggleBtn.textContent = 'Pause Simulation';
      toggleBtn.className = 'btn btn-sm btn-primary';
      toggleBtn.style.background = '#d97706';
      toggleBtn.style.borderColor = '#d97706';
    }

    this.simTimer = setInterval(() => {
      if (!this.isSimPaused) {
        this.stepDemoSimulation(false);
      }
    }, (typeof CONFIG !== 'undefined' && CONFIG.DEMO_STEP_INTERVAL_MS) || 3000);
  },

  stopDemoSimulation() {
    if (this.simTimer) {
      clearInterval(this.simTimer);
      this.simTimer = null;
    }
  },

  toggleDemoPause() {
    this.isSimPaused = !this.isSimPaused;
    const toggleBtn = document.getElementById('btn-demo-toggle');

    if (toggleBtn) {
      if (this.isSimPaused) {
        toggleBtn.textContent = 'Resume Simulation';
        toggleBtn.style.background = '#059669';
        toggleBtn.style.borderColor = '#059669';
        if (typeof UI !== 'undefined' && UI.showToast) {
          UI.showToast('Demo simulation paused', 'info');
        }
      } else {
        toggleBtn.textContent = 'Pause Simulation';
        toggleBtn.style.background = '#d97706';
        toggleBtn.style.borderColor = '#d97706';
        if (typeof UI !== 'undefined' && UI.showToast) {
          UI.showToast('Demo simulation resumed', 'info');
        }
      }
    }
  },

  stepDemoSimulation(manual = false) {
    if (!this.bookingData || !this.bookingData.waypoints) return;
    const waypoints = this.bookingData.waypoints;

    this.simWaypointIndex++;
    if (this.simWaypointIndex >= waypoints.length) {
      this.simWaypointIndex = waypoints.length - 1;
      this.bookingData.bookingStatus = 'REACHED_HOSPITAL';
      this.bookingData.stageIndex = 6;
      this.bookingData.etaMinutes = 0;
      this.bookingData.distanceRemainingKm = 0;
      this.bookingData.speedKmH = 0;

      this.renderBookingDetails(this.bookingData);
      this.renderTimeline(6);

      if (manual && typeof UI !== 'undefined' && UI.showToast) {
        UI.showToast('Ambulance has safely arrived at Destination Hospital.', 'success');
      }
      return;
    }

    const currentCoords = waypoints[this.simWaypointIndex];
    const totalWaypoints = waypoints.length;
    const progress = this.simWaypointIndex / (totalWaypoints - 1);

    if (this.ambulanceMarker) {
      this.ambulanceMarker.setLatLng(currentCoords);
    }

    if (this.simWaypointIndex < Math.floor(totalWaypoints * 0.4)) {
      this.bookingData.bookingStatus = 'ON_THE_WAY';
      this.bookingData.stageIndex = 4;
    } else if (this.simWaypointIndex < totalWaypoints - 1) {
      this.bookingData.bookingStatus = 'PATIENT_PICKED_UP';
      this.bookingData.stageIndex = 5;
    } else {
      this.bookingData.bookingStatus = 'REACHED_HOSPITAL';
      this.bookingData.stageIndex = 6;
    }

    const origDist = 5.0;
    this.bookingData.distanceRemainingKm = Math.max(0.1, (origDist * (1 - progress)));
    this.bookingData.etaMinutes = Math.max(1, Math.round(this.bookingData.distanceRemainingKm * 1.8));
    this.bookingData.speedKmH = 40 + Math.floor(Math.random() * 15);

    this.renderBookingDetails(this.bookingData);
    this.renderTimeline(this.bookingData.stageIndex);

    if (manual && typeof UI !== 'undefined' && UI.showToast) {
      UI.showToast(`Advanced to waypoint ${this.simWaypointIndex + 1}/${totalWaypoints}`, 'info');
    }
  },

  resetDemoSimulation() {
    this.simWaypointIndex = 0;
    if (this.bookingData && this.bookingData.waypoints) {
      const start = this.bookingData.waypoints[0];
      if (this.ambulanceMarker) this.ambulanceMarker.setLatLng(start);

      this.bookingData.bookingStatus = 'ON_THE_WAY';
      this.bookingData.stageIndex = 4;
      this.bookingData.distanceRemainingKm = 4.2;
      this.bookingData.etaMinutes = 7;
      this.bookingData.speedKmH = 45;

      this.renderBookingDetails(this.bookingData);
      this.renderTimeline(4);
      this.fitRouteBounds();

      if (typeof UI !== 'undefined' && UI.showToast) {
        UI.showToast('Demo route reset to starting position', 'info');
      }
    }
  },

  // ====================================================================
  // BACKEND REAL TELEMETRY
  // ====================================================================
  connectBackendTelemetry(bookingId) {
    if (!bookingId) return;

    const pollInterval = (typeof CONFIG !== 'undefined' && CONFIG.POLLING_INTERVAL_MS) || 3500;
    this.pollTimer = setInterval(async () => {
      try {
        const telemetry = await api.get(`/tracking/${bookingId}`);
        if (telemetry) {
          this.applyBackendTelemetry(telemetry);
        }
      } catch (err) {}
    }, pollInterval);

    try {
      const wsBase = (typeof CONFIG !== 'undefined' && CONFIG.WS_BASE_URL) || 'http://localhost:8080/ws';
      const wsUrl = `${wsBase.replace(/^http/, 'ws')}/websocket`;
      this.wsSocket = new WebSocket(wsUrl);

      this.wsSocket.onopen = () => {
        this.setConnectionStatus('Real-Time Telemetry Connected', '#059669');
      };

      this.wsSocket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data && (data.bookingId == bookingId || data.ambulanceId == this.bookingData?.ambulanceId)) {
            this.applyBackendTelemetry(data);
          }
        } catch (e) {}
      };

      this.wsSocket.onerror = () => {
        this.setConnectionStatus('HTTP Polling Active', '#d97706');
      };
    } catch (e) {
      this.setConnectionStatus('HTTP Polling Active', '#d97706');
    }
  },

  applyBackendTelemetry(t) {
    if (!t) return;
    this.telemetryData = t;

    if (t.latitude && t.longitude && this.ambulanceMarker) {
      this.ambulanceMarker.setLatLng([t.latitude, t.longitude]);
    }

    if (t.eta) {
      const el = document.getElementById('hud-eta');
      if (el) el.textContent = t.eta;
    }

    if (t.distanceRemainingKm !== undefined) {
      const el = document.getElementById('hud-distance');
      if (el) el.textContent = `${Number(t.distanceRemainingKm).toFixed(1)} km`;
    }

    if (t.speed !== undefined) {
      const el = document.getElementById('hud-speed');
      if (el) el.textContent = `${Math.round(t.speed)} km/h`;
    }

    if (t.status) {
      const stage = this.mapStatusToStage(t.status);
      this.renderTimeline(stage);
      const bEl = document.getElementById('hud-status-badge');
      if (bEl) bEl.innerHTML = `<span class="badge badge-ontrip">${t.status.replace(/_/g, ' ')}</span>`;
    }
  },

  // ====================================================================
  // UI HELPERS
  // ====================================================================
  showDashboard() {
    const emptyEl = document.getElementById('empty-state-card');
    const dashEl = document.getElementById('active-tracking-dashboard');
    if (emptyEl) emptyEl.style.display = 'none';
    if (dashEl) dashEl.style.display = 'block';
  },

  hideDashboard() {
    const emptyEl = document.getElementById('empty-state-card');
    const dashEl = document.getElementById('active-tracking-dashboard');
    if (emptyEl) emptyEl.style.display = 'block';
    if (dashEl) dashEl.style.display = 'none';
  },

  showError(message) {
    const box = document.getElementById('tracking-error-box');
    const msgEl = document.getElementById('tracking-error-msg');
    if (msgEl) msgEl.textContent = message;
    if (box) box.style.display = 'flex';
  },

  hideError() {
    const box = document.getElementById('tracking-error-box');
    if (box) box.style.display = 'none';
  },

  setDemoBannerVisible(visible) {
    const banner = document.getElementById('demo-simulation-banner');
    if (banner) banner.style.display = visible ? 'block' : 'none';
  },

  setConnectionStatus(text, color) {
    const textEl = document.getElementById('connection-status-text');
    if (textEl) {
      textEl.textContent = text;
      if (color) textEl.style.color = color;
    }
  },

  shareTrackingLink() {
    const code = this.bookingData?.bookingCode || this.currentBookingId;
    const url = `${window.location.origin}${window.location.pathname}?code=${encodeURIComponent(code)}`;

    if (navigator.clipboard) {
      navigator.clipboard.writeText(url).then(() => {
        if (typeof UI !== 'undefined' && UI.showToast) {
          UI.showToast('Direct tracking link copied to clipboard.', 'success');
        }
      });
    } else {
      prompt('Direct tracking link:', url);
    }
  }
};
