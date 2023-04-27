package se.worldinmovies.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CountryMapper {
    private static final Map<String, List<String>> countryMap = new HashMap<>();

    static {
        countryMap.put("AN", List.of("BQ", "CW", "SX"));  //# The Netherlands Antilles was divided into
        // Bonaire, Saint Eustatius and Saba (BQ)
        // Curaçao (CW)
        // and Sint Maarten (SX)
        countryMap.put("AQ", List.of("AQ"));  //# Antarctica is not even on the map
        countryMap.put("BU", List.of("MM"));  //# Burma is now Myanmar
        countryMap.put("CS", List.of("RS", "SK"));  //# Czechoslovakia was divided into Czechia (CZ), and Slovakia (SK)
        countryMap.put("SU", List.of("AM", "AZ", "EE", "GE", "KZ", "KG", "LV", "LT", "MD", "RU", "TJ", "TM", "UZ"));  //# USSR was divided into,
        //# Armenia (AM),
        //# Azerbaijan (AZ),
        //# Estonia (EE),
        //# Georgia (GE),
        //# Kazakstan (KZ),
        //# Kyrgyzstan (KG),
        //# Latvia (LV),
        //# Lithuania (LT),
        //# Republic of Moldova (MD),
        //# Russian Federation (RU),
        //# Tajikistan (TJ),
        //# Turkmenistan (TM),
        //# Uzbekistan (UZ).
        countryMap.put("TP", List.of("TL"));  //# Name changed from East Timor (TP) to Timor-Leste (TL)
        countryMap.put("UM", List.of("UM-DQ", "UM-FQ", "UM-HQ", "UM-JQ", "UM-MQ", "UM-WQ"));  //# United States Minor Outlying Islands is
        //# Jarvis Island   (UM-DQ)
        //# Baker Island    (UM-FQ)
        //# Howland Island  (UM-HQ)
        //# Johnston Atoll  (UM-JQ)
        //# Midway Islands  (UM-MQ)
        //# Wake Island     (UM-WQ)
        countryMap.put("XC", List.of("IC"));  //# Czechoslovakia was divided into Czechia (CZ), and Slovakia (SK)
        countryMap.put("XG", List.of("DE"));  //# East Germany is now germany (DE)
        countryMap.put("XI", List.of("IM"));  //# Northern Ireland is kind of Isle of man
        countryMap.put("ZR", List.of("CD")); // Name changed from Zaire to the Democratic Republic of the Congo (CD)
        countryMap.put("YU", List.of("BA", "HR", "MK", "CS", "SI")); //# Former Yugoslavia was divided into
        //# Bosnia and Herzegovina (BA),
        //# Croatia (HR),
        //# The former Yugoslav Republic of Macedonia (MK),
        //# Serbia and Montenegro (CS),
        //# Slovenia (SI)
    }

    public static List<String> getOldFromNew(String countryCode) {
        return countryMap.entrySet()
                .stream()
                .filter(a -> a.getValue().contains(countryCode))
                .findAny()
                .map(a -> List.of(countryCode, a.getKey()))
                .orElse(List.of(countryCode));
    }
}
