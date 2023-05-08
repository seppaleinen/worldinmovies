package se.worldinmovies.neo4j.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class LanguageMapper {
    private static final Map<String, List<String>> languagesToCountryMap = new ConcurrentHashMap<>();
    private static final Resource resourceFile = new ClassPathResource("territory-info.xml");

    public LanguageMapper() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TerritoryInfo.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TerritoryInfo territoryInfo = (TerritoryInfo) jaxbUnmarshaller.unmarshal(resourceFile.getInputStream());
            languagesToCountryMap.putAll(territoryInfo.getTerritoryList()
                    .stream()
                    .collect(Collectors.groupingBy(TerritoryInfo.Territory::getType,
                            Collectors.flatMapping(LanguageMapper::streamLanguagesFromTerritory,
                                    Collectors.toList()))));
        } catch (Exception e) {
            log.error("Something terrible has happened: {}", e.getMessage());
        }
    }

    private static Stream<String> streamLanguagesFromTerritory(TerritoryInfo.Territory territory) {
        return Optional.ofNullable(territory.getLanguagePopulations())
                .map(languagePopulations -> {
                    if(languagePopulations.stream().anyMatch(a -> a.getOfficialStatus() != null)) {
                        return languagePopulations.stream()
                                .filter(a -> a.getOfficialStatus() != null)
                                .map(TerritoryInfo.Territory.LanguagePopulation::getType)
                                .map(a -> a.split("_")[0]);
                    } else {
                        return languagePopulations.stream()
                                .map(TerritoryInfo.Territory.LanguagePopulation::getType)
                                .map(a -> a.split("_")[0]);
                    }
                })
                .orElse(Stream.empty());
    }

    public List<String> getLanguagesFromCountryCode(String countryCode) {
        return languagesToCountryMap.getOrDefault(countryCode, List.of());
    }
}
