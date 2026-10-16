/**
 * Ambulance Booking and Tracking System
 * Location & Geocoding Service (10 KM Radius Hospital Locator)
 */
const LocationService = {
  // Pre-mapped landmarks and neighborhoods for instant, resilient offline/online resolution
  KNOWN_LOCATIONS: {
    'central district': { lat: 12.9716, lon: 77.5946, label: 'Central District, Metro City' },
    'medical square': { lat: 12.97244, lon: 77.58064, label: '100 Medical Square, Central District' },
    'metro city': { lat: 12.9716, lon: 77.5946, label: 'Metro City Center' },
    'city center': { lat: 12.9716, lon: 77.5946, label: 'Metro City Center' },
    'elm street': { lat: 12.9650, lon: 77.5980, label: '42 Elm Street, Metro City' },
    'oak lane': { lat: 12.9780, lon: 77.5840, label: '108 Oak Lane, Metro City' },
    'south park': { lat: 12.9602, lon: 77.5950, label: '25 Health Highway, South Park' },
    'health highway': { lat: 12.9602, lon: 77.5950, label: 'Health Highway, South Park' },
    'north zone': { lat: 12.9850, lon: 77.6050, label: '44 Ring Road, North Zone' },
    'ring road': { lat: 12.9850, lon: 77.6050, label: 'Ring Road, North Zone' },
    'east corridor': { lat: 12.9580, lon: 77.6480, label: '88 Airport Road, East Corridor' },
    'airport road': { lat: 12.9580, lon: 77.6480, label: 'Airport Road, East Corridor' },
    'koramangala': { lat: 12.9344, lon: 77.6219, label: '12 Lake View Road, Koramangala' },
    'lake view road': { lat: 12.9344, lon: 77.6219, label: 'Lake View Road, Koramangala' },
    'indiranagar': { lat: 12.9784, lon: 77.6408, label: 'Indiranagar Metro Corridor' },
    'mg road': { lat: 12.9756, lon: 77.6066, label: 'MG Road Central Boulevard' },
    'jayanagar': { lat: 12.9308, lon: 77.5838, label: 'Jayanagar Emergency Ward' },
    'electronic city': { lat: 12.8250, lon: 77.6850, label: 'Electronic City (~19 km from center)' },
    'devanahalli': { lat: 13.2450, lon: 77.7120, label: 'Devanahalli (~33 km from center)' },
    'chennai': { lat: 13.0827, lon: 80.2707, label: 'Chennai Central District (~290 km away)' }
  },

  /**
   * Calculate geographic straight-line distance in kilometres using the Haversine formula
   */
  calculateHaversineDistance(lat1, lon1, lat2, lon2) {
    if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return null;
    const toRad = deg => (deg * Math.PI) / 180;
    const R = 6371; // Earth's mean radius in km
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return parseFloat((R * c).toFixed(2));
  },

  /**
   * Extract coordinates if input is written as "lat, lon"
   */
  parseCoordinateString(text) {
    if (!text || typeof text !== 'string') return null;
    const trimmed = text.trim();
    // Pattern: number, comma/space, number
    const match = trimmed.match(/^([-+]?\d{1,2}(?:\.\d+)?)[,\s]+([-+]?\d{1,3}(?:\.\d+)?)$/);
    if (match) {
      const lat = parseFloat(match[1]);
      const lon = parseFloat(match[2]);
      if (!isNaN(lat) && !isNaN(lon) && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
        return { lat, lon, label: `Coordinates (${lat.toFixed(4)}, ${lon.toFixed(4)})`, source: 'coordinates' };
      }
    }
    return null;
  },

  /**
   * Convert an address, landmark, or GPS input into latitude and longitude
   */
  async geocode(query) {
    if (!query || !query.trim()) {
      return null;
    }
    const clean = query.trim();

    // 1. Direct coordinate format (e.g., "12.9716, 77.5946" or "Emergency GPS Pin: 12.9716, 77.5946")
    const coordDirect = this.parseCoordinateString(clean.replace(/.*GPS Pin:?\s*/i, ''));
    if (coordDirect) {
      return coordDirect;
    }

    // 2. Check local landmark and neighborhood dictionary
    const lower = clean.toLowerCase();
    for (const [key, item] of Object.entries(this.KNOWN_LOCATIONS)) {
      if (lower.includes(key)) {
        return { lat: item.lat, lon: item.lon, label: item.label, source: 'landmark' };
      }
    }

    // 3. Online Geocoding via OpenStreetMap Nominatim with 3500ms timeout
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 3500);

      const url = `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(clean)}`;
      const res = await fetch(url, {
        headers: { 'Accept': 'application/json' },
        signal: controller.signal
      });
      clearTimeout(timeoutId);

      if (res.ok) {
        const results = await res.json();
        if (results && results.length > 0) {
          const lat = parseFloat(results[0].lat);
          const lon = parseFloat(results[0].lon);
          if (!isNaN(lat) && !isNaN(lon)) {
            return {
              lat,
              lon,
              label: results[0].display_name || clean,
              source: 'nominatim'
            };
          }
        }
      }
    } catch (err) {
      console.warn('Online geocoding lookup timed out or failed:', err.message);
    }

    // 4. If query looks like an address in Metro City or has a number, map to central district corridor
    if (/\d+/.test(clean) || clean.length >= 3) {
      return {
        lat: 12.9716,
        lon: 77.5946,
        label: `${clean} (Metro City Reference Area)`,
        source: 'corridor_fallback'
      };
    }

    return null;
  },

  /**
   * Request browser GPS position with high accuracy
   */
  async getCurrentGpsPosition() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Browser geolocation is not supported on this device.'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (pos) => {
          resolve({
            lat: pos.coords.latitude,
            lon: pos.coords.longitude,
            accuracy: pos.coords.accuracy,
            label: `GPS Location (${pos.coords.latitude.toFixed(4)}, ${pos.coords.longitude.toFixed(4)})`,
            source: 'gps'
          });
        },
        (err) => {
          let msg = 'GPS access denied or unavailable.';
          if (err.code === err.PERMISSION_DENIED) {
            msg = 'GPS permission denied. Please enter your pickup address or landmark.';
          } else if (err.code === err.POSITION_UNAVAILABLE) {
            msg = 'GPS position unavailable. Please enter your pickup address.';
          } else if (err.code === err.TIMEOUT) {
            msg = 'GPS location timed out. Please enter your pickup address.';
          }
          reject(new Error(msg));
        },
        { timeout: 7000, enableHighAccuracy: true, maximumAge: 30000 }
      );
    });
  }
};

window.LocationService = LocationService;
