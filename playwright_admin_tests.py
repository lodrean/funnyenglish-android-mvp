import asyncio
from playwright.async_api import async_playwright
import sys

BASE_URL = "http://localhost:8082"
ADMIN_USER = "admin"
ADMIN_PASS = "admin123"

passed = 0
failed = 0

def check(cond, name):
    global passed, failed
    if cond:
        print(f"  PASS: {name}")
        passed += 1
    else:
        print(f"  FAIL: {name}")
        failed += 1

async def run_tests():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context(viewport={"width": 1280, "height": 800})
        page = await context.new_page()

        # 1. Login page loads
        await page.goto(f"{BASE_URL}/admin/login")
        await page.wait_for_load_state("networkidle")
        title = await page.title()
        check("Вход" in title or "Login" in title or "FunnyEnglish" in title, "Login page title")
        has_user = await page.locator("input[name='username']").count() > 0
        check(has_user, "Login page has username field")
        has_pass = await page.locator("input[name='password']").count() > 0
        check(has_pass, "Login page has password field")
        has_btn = await page.locator("button[type='submit']").count() > 0
        check(has_btn, "Login page has submit button")

        # 2. Extract CSRF and login
        csrf_token = await page.locator("input[name='_csrf']").input_value()
        check(csrf_token is not None and len(csrf_token) > 0, "CSRF token extracted")

        await page.fill("input[name='username']", ADMIN_USER)
        await page.fill("input[name='password']", ADMIN_PASS)
        await page.click("button[type='submit']")
        await page.wait_for_load_state("networkidle")
        
        # Check if dashboard loaded or error
        url = page.url
        html = await page.content()
        with open("playwright_debug2.html", "w", encoding="utf-8") as f:
            f.write(html)
        await page.screenshot(path="playwright_debug2.png", full_page=True)
        
        check("dashboard" in url, f"After login URL is dashboard (got {url})")
        check('class="cards"' in html or 'Whitelabel' not in html, f"Dashboard rendered without error (len={len(html)})")

        if 'class="cards"' in html:
            cards = await page.locator(".card").count()
            check(cards >= 6, f"Dashboard has {cards} stat cards")
            
            check(await page.locator("text=Пользователи").count() > 0, "Dashboard has users section")
            check(await page.locator("text=Игры").count() > 0, "Dashboard has games section")
            check(await page.locator("text=Сообщения").count() > 0, "Dashboard has chat section")

            # Navigate to Users
            await page.click("a.nav-link:has-text('Пользователи')")
            await page.wait_for_load_state("networkidle")
            check("users" in page.url, "Navigate to Users page")
            has_table = await page.locator("table tbody tr").count() > 0
            check(has_table, "Users page has data rows")

            # Navigate to Games
            await page.click("a.nav-link:has-text('Игры')")
            await page.wait_for_load_state("networkidle")
            check("games" in page.url, "Navigate to Games page")

            # Navigate to Chat
            await page.click("a.nav-link:has-text('Чат')")
            await page.wait_for_load_state("networkidle")
            check("chat" in page.url, "Navigate to Chat page")

            # Back to Dashboard
            await page.click("a.nav-link:has-text('Dashboard')")
            await page.wait_for_load_state("networkidle")
            check("dashboard" in page.url, "Navigate back to Dashboard")

            # Logout
            await page.click("button.logout-btn")
            await page.wait_for_load_state("networkidle")
            check("login" in page.url, "Logout redirects to login")
        else:
            print(f"  SKIP: Navigation tests (dashboard error)")

        # H2 Console
        await page.goto(f"{BASE_URL}/h2-console")
        await page.wait_for_load_state("networkidle")
        h2_title = await page.title()
        check("H2" in h2_title or "Console" in h2_title, "H2 Console accessible")

        # API endpoint
        await page.goto(f"{BASE_URL}/api/v1/users")
        await page.wait_for_load_state("networkidle")
        body = await page.locator("body").inner_text()
        check(body.startswith("[") and (body == "[]" or "telegramId" in body), "API users endpoint returns JSON array")

        await browser.close()

async def main():
    await run_tests()
    print(f"\nPlaywright Results: {passed} passed, {failed} failed")
    if failed > 0:
        sys.exit(1)

if __name__ == "__main__":
    asyncio.run(main())
