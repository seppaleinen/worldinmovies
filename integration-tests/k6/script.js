import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

export const options = {
    scenarios: {
        shared_iter_scenario: {
            executor: "shared-iterations",
            vus: 10,
            iterations: 100,
            startTime: "0s",
        },
        per_vu_scenario: {
            executor: "per-vu-iterations",
            vus: 30,
            iterations: 10,
            startTime: "20s",
        },
    },
};

const durationStatus = new Trend('duration_status');
const durationHealth = new Trend('duration_health');
const durationBestOfAllCountries = new Trend('duration_best_of_all_countries');
const durationBestOfCountry = new Trend('duration_best_of_country');
const countrycodes = ["US", "RU", "FR", "GR", "KR", "SE", "FI", "CZ", "PL"];

export default function () {
    const res = http.get('http://localhost:81/backend/status');
    check(res, { 'status was 200': (r) => r.status === 200 });
    durationStatus.add(res.timings.duration);

    const res2 = http.get('http://localhost:81/backend/health');
    check(res2, { 'status was 200': (r) => r.status === 200 });
    durationHealth.add(res2.timings.duration);

    const res3 = http.get('http://localhost:81/backend/view/best');
    check(res3, { 'status was 200': (r) => r.status === 200 });
    durationBestOfAllCountries.add(res3.timings.duration);

    const randomCountryCode = countrycodes[Math.floor(Math.random() * countrycodes.length)]
    const res4 = http.get('http://localhost:81/backend/view/best/' + randomCountryCode);
    check(res4, { 'status was 200': (r) => r.status === 200 });
    durationBestOfCountry.add(res4.timings.duration);
}
