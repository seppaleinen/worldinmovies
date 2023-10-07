import {test, expect} from '@playwright/test';

test.describe("World map", () => {
  test('world map link should work', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: 'World Map' }).click();
    await expect(page).toHaveURL(/.*map/);
    await expect(page.locator('path[data-code="US"]')).toBeVisible();
    await expect(page.locator('path[data-code="SE"]')).toBeVisible();
    await expect(page.locator('path[data-code="FR"]')).toBeVisible();
  });

  test('world map click on country', async ({ page }) => {
    await page.goto('/map');
    let sweden = page.locator('path[data-code="SE"]');
    await expect(sweden).toBeVisible();
    await sweden.click();
    await expect(page).toHaveURL(/.*\/country\/SE/);
    await expect(page.locator("h1")).toHaveText("Sweden");
  });

  test('Can access world map link in header', async ({ page }) => {
    await page.goto('/map');
    await page.getByRole('link', { name: 'World Map' }).click();
    await expect(page).toHaveURL(/.*map/);
  });

  test('Can access home link in header', async ({ page }) => {
    await page.goto('/map');
    await page.getByRole('link', { name: 'The World in Movies' }).click();
    await expect(page).toHaveURL(/\//);
    await expect(page).toHaveTitle(/World in Movies/);
  });

  test('Can access menu links in header', async ({ page }) => {
    await page.goto('/map');
    await page.locator('#menu label').first().click();

    let importMoviesLink = page.getByRole('link', {name: "Import Movies"});
    await expect(importMoviesLink).toBeVisible();
    await importMoviesLink.click();
    await expect(page).toHaveURL(/.*import/);
  });

  test('Can access menu links to admin in header', async ({ page }) => {
    await page.goto('/map');
    await page.locator('#menu label').first().click();

    let adminLink = page.getByRole('link', {name: "Admin"});
    await expect(adminLink).toBeVisible();
    await adminLink.click();
    await expect(page).toHaveURL(/.*admin/);
  });
})

test('has title', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveTitle(/World in Movies/);
});

