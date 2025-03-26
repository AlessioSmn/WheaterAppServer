package it.unipi.lsmsd.utility;

import java.text.DecimalFormat;

public final class CityUtility {
    
    // Private constructor to prevent instantiation
    private CityUtility(){}

    // Custom cityId generation logic
    // Pisa,Tuscany, (43.690685, 10.452489) --> pis-tus-43.6907-10.4525 
    // 23 characters long
    // Static function for reusuabilty
    public static String generateCityId(String name, String region, Double latitude, Double longitude) {
        DecimalFormat df = new DecimalFormat("#.####");  // Format coordinates to 4 decimal places
        // Generate code based on each inputs
        String latCode = df.format(latitude);
        String lonCode = df.format(longitude);
        String nameCode = name.substring(0, 3);
        String regionCode = (region.length() >= 3) ? region.substring(0, 3):region;
        return (nameCode + "-" + regionCode + "-" + latCode + "-" + lonCode).toLowerCase();
    }
}
