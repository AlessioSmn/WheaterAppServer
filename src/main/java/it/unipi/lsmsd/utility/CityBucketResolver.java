package it.unipi.lsmsd.utility;

import java.util.*;

public class CityBucketResolver {

    private static final Map<String, String> regionCodeToName = Map.ofEntries(
            Map.entry("lom", "Lombardy"),
            Map.entry("emi", "Emilia-Romagna"),
            Map.entry("cal", "Calabria"),
            Map.entry("sar", "Sardinia"),
            Map.entry("umb", "Umbria"),
            Map.entry("aos", "Aosta Valley"),
            Map.entry("lat", "Lazio"),
            Map.entry("cam", "Campania"),
            Map.entry("apu", "Apulia"),
            Map.entry("lig", "Liguria"),
            Map.entry("tre", "Trentino-Alto Adige"),
            Map.entry("mol", "Molise"),
            Map.entry("sic", "Sicily"),
            Map.entry("ven", "Veneto"),
            Map.entry("pie", "Piedmont"),
            Map.entry("tus", "Tuscany"),
            Map.entry("the", "The Marches"),
            Map.entry("abr", "Abruzzo"),
            Map.entry("bas", "Basilicata"),
            Map.entry("fri", "Friuli-Venezia Giulia")
    );

    private static final Set<String> bucket1 = Set.of(
            "Lombardy", "Trentino-Alto Adige", "Veneto", "Friuli-Venezia Giulia", "Piedmont", "Aosta Valley"
    );

    private static final Set<String> bucket2 = Set.of(
            "Liguria", "Tuscany", "Emilia-Romagna", "Umbria", "The Marches", "Lazio", "Abruzzo"
    );

    private static final Set<String> bucket3 = Set.of(
            "Sicily", "Sardinia", "Molise", "Apulia", "Calabria", "Campania", "Basilicata"
    );

    public static String getBucket(String cityId) {
        String[] parts = cityId.split("-");
        if (parts.length < 2) {
            return "Invalid ID format.";
        }

        String regionCode = parts[0].toLowerCase(); // e.g., "tus"
        String region = regionCodeToName.get(regionCode);

        if (region == null) {
            return "Unknown region code: " + regionCode;
        }

        if (bucket1.contains(region)) return "bucket1";
        if (bucket2.contains(region)) return "bucket2";
        if (bucket3.contains(region)) return "bucket3";

        return "No bucket defined for region: " + region;
    }
}