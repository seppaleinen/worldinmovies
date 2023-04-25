package se.worldinmovies.neo4j.xml;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TerritoryInfo {
    @XmlElement(name = "territory")
    private List<Territory> territoryList;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Territory {
        @XmlAttribute(name = "type")
        private String type;
        @XmlElement(name = "languagePopulation")
        private List<LanguagePopulation> languagePopulations;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class LanguagePopulation {
            @XmlAttribute(name = "type")
            private String type;
            @XmlAttribute(name = "officialStatus")
            private String officialStatus;
        }
    }
}
