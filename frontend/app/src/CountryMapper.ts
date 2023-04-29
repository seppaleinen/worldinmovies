// Old countries that have been split up into new countries or changed countrycode
const countryMapper = {
    "SU": ["AM", "AZ", "EE", "GE", "KZ", "KG", "LV", "LT", "MD", "RU", "TJ", "TM", "UZ"],
    "AN": ["BQ", "CW", "SX"],
    "AQ": ["AQ"],
    "BU": ["MM"],
    "CS": ["RS", "SK"],
    "TP": ["TL"],
    "UM": ["UM-DQ", "UM-FQ", "UM-HQ", "UM-JQ", "UM-MQ", "UM-WQ"],
    "XC": ["IC"],
    "XG": ["DE"],
    "XI": ["IM"],
    "ZR": ["CD"],
    "YU": ["BA", "HR", "MK", "CS", "SI"]
} as Record<string, string[]>;

const mapCountry = (countryCode: string): string[] => {
    let result = Object.entries(countryMapper)
        .find(entry => {
            return entry[1].includes(countryCode);
        });
    return result ? [result[0], countryCode] : [countryCode];
}

export default mapCountry;