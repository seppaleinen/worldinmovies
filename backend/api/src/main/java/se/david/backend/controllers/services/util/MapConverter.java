package se.david.backend.controllers.services.util;

import java.util.HashMap;
import java.util.Map;

public final class MapConverter {
    private static final Map<String, String> countryMap = new HashMap<>();

    /**
     * Republic of Macedonia
     * Moldova
     * Nauru
     */
    static {
        countryMap.put("Afghanistan", "AF");
        countryMap.put("Albania", "AL");
        countryMap.put("Algeria", "DZ");
        countryMap.put("American Samoa", "AS");
        countryMap.put("Andorra", "AD");
        countryMap.put("Angola", "AO");
        countryMap.put("Antarctica", "AQ");
        countryMap.put("Antigua and Barbuda", "AG");
        countryMap.put("Argentina", "AR");
        countryMap.put("Armenia", "AM");
        countryMap.put("Aruba", "AW");
        countryMap.put("Australia", "AU");
        countryMap.put("Austria", "AT");
        countryMap.put("Azerbaijan", "AZ");
        countryMap.put("Bahamas", "BS");
        countryMap.put("Bahrain", "BH");
        countryMap.put("Bangladesh", "BD");
        countryMap.put("Barbados", "BB");
        countryMap.put("Belarus", "BY");
        countryMap.put("Belgium", "BE");
        countryMap.put("Belize", "BZ");
        countryMap.put("Benin", "BJ");
        countryMap.put("Bermuda", "BM");
        countryMap.put("Bhutan", "BT");
        countryMap.put("Bolivia", "BO");
        countryMap.put("Bosnia and Herzegovina", "BA");
        countryMap.put("Botswana", "BW");
        countryMap.put("Brazil", "BR");
        countryMap.put("British Indian Ocean Territory", "IO");
        countryMap.put("Brunei Darussalam", "BN");
        countryMap.put("Bulgaria", "BG");
        countryMap.put("Burkina Faso", "BF");
        countryMap.put("Burma", "MM");
        countryMap.put("Burundi", "BI");
        countryMap.put("Cambodia", "KH");
        countryMap.put("Cameroon", "CM");
        countryMap.put("Canada", "CA");
        countryMap.put("Cape Verde", "CV");
        countryMap.put("Cayman Islands", "KY");
        countryMap.put("Central African Republic", "CF");
        countryMap.put("Chad", "TD");
        countryMap.put("Chile", "CL");
        countryMap.put("China", "CN");
        countryMap.put("Colombia", "CO");
        countryMap.put("Comoros", "KM");
        countryMap.put("Congo", "CG");
        countryMap.put("Cook Islands", "CK");
        countryMap.put("Costa Rica", "CR");
        countryMap.put("Croatia", "HR");
        countryMap.put("Cuba", "CU");
        countryMap.put("Cyprus", "CY");
        countryMap.put("Czechoslovakia", "CZ");
        countryMap.put("Czech Republic", "CZ");
        countryMap.put("Democratic Republic of the Congo", "CD");
        countryMap.put("Denmark", "DK");
        countryMap.put("Djibouti", "DJ");
        countryMap.put("Dominica", "DM");
        countryMap.put("Dominican Republic", "DO");
        countryMap.put("East Germany", "GE");
        countryMap.put("Ecuador", "EC");
        countryMap.put("Egypt", "EG");
        countryMap.put("El Salvador", "SV");
        countryMap.put("Equatorial Guinea", "GQ");
        countryMap.put("Eritrea", "ER");
        countryMap.put("Estonia", "EE");
        countryMap.put("Ethiopia", "ET");
        countryMap.put("Falkland Islands", "FK");
        countryMap.put("Faroe Islands", "FO");
        countryMap.put("Federal Republic of Yugoslavia", "RS");
        countryMap.put("Federated States of Micronesia", "FM");
        countryMap.put("Fiji", "FJ");
        countryMap.put("Finland", "FI");
        countryMap.put("France", "FR");
        countryMap.put("French Guiana", "GF");
        countryMap.put("French Polynesia", "PF");
        countryMap.put("Gabon", "GA");
        countryMap.put("Gambia", "GM");
        countryMap.put("Georgia", "GE");
        countryMap.put("Germany", "DE");
        countryMap.put("Ghana", "GH");
        countryMap.put("Gibraltar", "GI");
        countryMap.put("Greece", "GR");
        countryMap.put("Greenland", "GL");
        countryMap.put("Grenada", "GD");
        countryMap.put("Guadeloupe", "GP");
        countryMap.put("Guam", "GU");
        countryMap.put("Guatemala", "GT");
        countryMap.put("Guinea", "GN");
        countryMap.put("Guinea-Bissau", "GW");
        countryMap.put("Guyana", "GY");
        countryMap.put("Haiti", "HT");
        countryMap.put("Honduras", "HN");
        countryMap.put("Hong Kong", "HK");
        countryMap.put("Hungary", "HU");
        countryMap.put("Iceland", "IS");
        countryMap.put("India", "IN");
        countryMap.put("Indonesia", "ID");
        countryMap.put("Iran", "IR");
        countryMap.put("Iraq", "IQ");
        countryMap.put("Ireland", "IE");
        countryMap.put("Isle of Man", "IM");
        countryMap.put("Israel", "IL");
        countryMap.put("Italy", "IT");
        countryMap.put("Ivory Coast", "CI");
        countryMap.put("Jamaica", "JM");
        countryMap.put("Japan", "JP");
        countryMap.put("Jordan", "JO");
        countryMap.put("Kazakhstan", "KZ");
        countryMap.put("Kenya", "KE");
        countryMap.put("Kiribati", "KI");
        countryMap.put("Korea", "KR");
        countryMap.put("Kosovo", "RS");
        countryMap.put("Kuwait", "KW");
        countryMap.put("Kyrgyzstan", "KG");
        countryMap.put("Laos", "LA");
        countryMap.put("Latvia", "LV");
        countryMap.put("Lebanon", "LB");
        countryMap.put("Lesotho", "LS");
        countryMap.put("Liberia", "LR");
        countryMap.put("Libya", "LY");
        countryMap.put("Liechtenstein", "LI");
        countryMap.put("Lithuania", "LT");
        countryMap.put("Luxembourg", "LU");
        countryMap.put("Macao", "MO");
        countryMap.put("Madagascar", "MG");
        countryMap.put("Malawi", "MW");
        countryMap.put("Malaysia", "MY");
        countryMap.put("Maldives", "MV");
        countryMap.put("Mali", "ML");
        countryMap.put("Malta", "MT");
        countryMap.put("Marshall Islands", "MH");
        countryMap.put("Martinique", "MQ");
        countryMap.put("Mauritania", "MR");
        countryMap.put("Mauritius", "MU");
        countryMap.put("Mexico", "MX");
        countryMap.put("Moldova", "MD");
        countryMap.put("Monaco", "MC");
        countryMap.put("Mongolia", "MN");
        countryMap.put("Montenegro", "ME");
        countryMap.put("Montserrat", "MS");
        countryMap.put("Morocco", "MA");
        countryMap.put("Mozambique", "MZ");
        countryMap.put("Myanmar", "MM");
        countryMap.put("Namibia", "NA");
        countryMap.put("Nepal", "NP");
        countryMap.put("Netherlands", "NL");
        countryMap.put("Netherlands Antilles", "NL");
        countryMap.put("New Caledonia", "NC");
        countryMap.put("New Zealand", "NZ");
        countryMap.put("Nicaragua", "NI");
        countryMap.put("Niger", "NE");
        countryMap.put("Nigeria", "NG");
        countryMap.put("Niue", "NU");
        countryMap.put("North Korea", "KR");
        countryMap.put("North Vietnam", "VN");
        countryMap.put("Norway", "NO");
        countryMap.put("Occupied Palestinian Territory", "PS");
        countryMap.put("Oman", "OM");
        countryMap.put("Pakistan", "PK");
        countryMap.put("Palau", "PW");
        countryMap.put("Palestine", "PS");
        countryMap.put("Panama", "PA");
        countryMap.put("Papua New Guinea", "PG");
        countryMap.put("Paraguay", "PY");
        countryMap.put("Peru", "PE");
        countryMap.put("Philippines", "PH");
        countryMap.put("Poland", "PL");
        countryMap.put("Portugal", "PT");
        countryMap.put("Puerto Rico", "PR");
        countryMap.put("Qatar", "QA");
        countryMap.put("Republic of Macedonia", "MK");
        countryMap.put("Reunion", "RE");
        countryMap.put("Romania", "RO");
        countryMap.put("Russia", "RU");
        countryMap.put("Rwanda", "RW");
        countryMap.put("Saint Helena", "SH");
        countryMap.put("Saint Kitts and Nevis", "KN");
        countryMap.put("Saint Lucia", "LC");
        countryMap.put("Saint Vincent and the Grenadines", "VC");
        countryMap.put("Samoa", "WS");
        countryMap.put("San Marino", "SM");
        countryMap.put("Sao Tome and Principe", "ST");
        countryMap.put("Saudi Arabia", "SA");
        countryMap.put("Senegal", "SN");
        countryMap.put("Serbia", "RS");
        countryMap.put("Serbia and Montenegro", "RS");
        countryMap.put("Seychelles", "SC");
        countryMap.put("Siam", "TH");
        countryMap.put("Sierra Leone", "SL");
        countryMap.put("Singapore", "SG");
        countryMap.put("Slovakia", "SK");
        countryMap.put("Slovenia", "SI");
        countryMap.put("Solomon Islands", "SB");
        countryMap.put("Somalia", "SO");
        countryMap.put("South Africa", "ZA");
        countryMap.put("South Georgia and the South Sandwich Islands", "GS");
        countryMap.put("South Korea", "KR");
        countryMap.put("Soviet Union", "RU");
        countryMap.put("Spain", "ES");
        countryMap.put("Sri Lanka", "LK");
        countryMap.put("Sudan", "SD");
        countryMap.put("Suriname", "SR");
        countryMap.put("Svalbard and Jan Mayen", "SJ");
        countryMap.put("Swaziland", "SZ");
        countryMap.put("Sweden", "SE");
        countryMap.put("Switzerland", "CH");
        countryMap.put("Syria", "SY");
        countryMap.put("Taiwan", "CN");
        countryMap.put("Tajikistan", "TJ");
        countryMap.put("Tanzania", "TZ");
        countryMap.put("Thailand", "TH");
        countryMap.put("Timor-Leste", "TL");
        countryMap.put("Togo", "TG");
        countryMap.put("Tonga", "TO");
        countryMap.put("Trinidad and Tobago", "TT");
        countryMap.put("Tunisia", "TN");
        countryMap.put("Turkey", "TR");
        countryMap.put("Turkmenistan", "TM");
        countryMap.put("Turks and Caicos Islands", "TC");
        countryMap.put("Tuvalu", "TV");
        countryMap.put("Uganda", "UG");
        countryMap.put("UK", "GB");
        countryMap.put("Ukraine", "UA");
        countryMap.put("United Arab Emirates", "AE");
        countryMap.put("Uruguay", "UY");
        countryMap.put("USA", "US");
        countryMap.put("U.S. Virgin Islands", "US2");
        countryMap.put("Uzbekistan", "UZ");
        countryMap.put("Vanuatu", "VU");
        countryMap.put("Venezuela", "VE");
        countryMap.put("Vietnam", "VN");
        countryMap.put("Western Sahara", "EH");
        countryMap.put("West Germany", "GE");
        countryMap.put("Yemen", "YE");
        countryMap.put("Yugoslavia", "RS");
        countryMap.put("Zaire", "CD");
        countryMap.put("Zambia", "ZM");
        countryMap.put("Zimbabwe", "ZW");
    }

    static String countryCode(String country) {
        return countryMap.get(country);
    }

    private MapConverter(){}
}
