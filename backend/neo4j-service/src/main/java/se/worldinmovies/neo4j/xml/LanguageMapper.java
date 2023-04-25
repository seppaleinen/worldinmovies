package se.worldinmovies.neo4j.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageMapper {
    private static final Map<String, List<String>> languagesToCountryMap = new ConcurrentHashMap<>();

    public LanguageMapper() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TerritoryInfo.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            File territoryXml = Paths.get(LanguageMapper.class.getClassLoader().getResource("territory-info.xml").getPath()).toFile();
            TerritoryInfo territoryInfo = (TerritoryInfo) jaxbUnmarshaller.unmarshal(territoryXml);
            languagesToCountryMap.putAll(territoryInfo.getTerritoryList()
                    .stream()
                    .collect(Collectors.groupingBy(TerritoryInfo.Territory::getType,
                            Collectors.flatMapping(LanguageMapper::streamLanguagesFromTerritory,
                                    Collectors.toList()))));
        } catch (Exception e) {
            //Ignore
        }
    }

    private static Stream<String> streamLanguagesFromTerritory(TerritoryInfo.Territory territory) {
        return Optional.ofNullable(territory.getLanguagePopulations())
                .map(languagePopulations -> {
                    if(languagePopulations.stream().anyMatch(a -> a.getOfficialStatus() != null)) {
                        return languagePopulations.stream()
                                .filter(a -> a.getOfficialStatus() != null)
                                .map(TerritoryInfo.Territory.LanguagePopulation::getType);
                    } else {
                        return languagePopulations.stream()
                                .map(TerritoryInfo.Territory.LanguagePopulation::getType);
                    }
                })
                .orElse(Stream.empty());
    }

    public List<String> getLanguagesFromCountryCode(String countryCode) {
        return languagesToCountryMap.getOrDefault(countryCode, List.of());
    }
}
