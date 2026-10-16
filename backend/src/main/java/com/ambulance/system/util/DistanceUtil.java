package com.ambulance.system.util;

import org.springframework.stereotype.Component;

@Component
public class DistanceUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_SPEED_KMH = 40.0; // average ambulance urban emergency speed

    /**
     * Calculates distance between two latitude/longitude points in kilometers using Haversine formula.
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;
        return Math.round(distance * 100.0) / 100.0; // round to 2 decimal places
    }

    /**
     * Estimates arrival time in minutes given distance in kilometers.
     */
    public int estimateEtaMinutes(double distanceKm) {
        if (distanceKm <= 0.05) return 1;
        double hours = distanceKm / AVERAGE_SPEED_KMH;
        int minutes = (int) Math.ceil(hours * 60) + 2; // +2 mins base dispatch buffer
        return Math.max(1, minutes);
    }

    /**
     * Formats estimated arrival time into a user-friendly string (e.g. "6 mins" or "1 min").
     */
    public String formatEta(int minutes) {
        if (minutes <= 1) {
            return "1 min (Arriving shortly)";
        }
        return minutes + " mins";
    }
}
