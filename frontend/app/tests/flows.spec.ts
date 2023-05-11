
module.exports = { scenario, mainPage, map, country, details };

const url = 'https://localhost';

async function scenario(page) {
    await page.goto(`${url}`);
    await page.getByRole('link', { name: 'World Map' }).click();
    await page.locator('path[data-code="SE"]').click();
    await page.getByRole('link', { name: 'The Seventh Seal Det sjunde inseglet (1957) \'The Seventh Seal\' 8.2' }).click();
    await page.getByText('I\'ve seen it').click();
    await page.getByText('Cast', { exact: true }).click();
    await page.getByText('Crew').click();
    await page.getByText('Details').click();
    await page.getByRole('link', { name: 'Sweden' }).click();
}

async function mainPage(page) {
    await page.goto(`${url}`);
}

async function map(page) {
    await page.goto(`${url}/map`);
}

async function country(page) {
    await page.goto(`${url}/country/SE`);
}

async function details(page) {
    await page.goto(`${url}/movie/475557`);
}
