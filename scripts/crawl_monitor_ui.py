#!/usr/bin/env python3
"""
Playwright crawler for OpenECPDS Monitor UI.
Logs in as admin and monitor, captures screenshots of all key pages,
and generates Markdown documentation.
"""

import asyncio
import os
import re
from pathlib import Path
from playwright.async_api import async_playwright

BASE          = os.environ.get("MONITOR_UI_BASE",     "https://ecpds-mover:8443")
ADMIN_USER    = os.environ.get("MONITOR_ADMIN_USER", "admin")
ADMIN_PASS    = os.environ.get("MONITOR_ADMIN_PASS", "admin2021")
MONITOR_USER  = os.environ.get("MONITOR_MON_USER",   "monitor")
MONITOR_PASS  = os.environ.get("MONITOR_MON_PASS",   "monitor2021")
OUT_DIR = Path("/workspaces/open-ecpds/docs/monitor-ui")
IMG_DIR = OUT_DIR / "img"
IMG_DIR.mkdir(parents=True, exist_ok=True)

# ──────────────────────────────────────────────────────────────────────────────
# Page catalogue: (slug, url_path, title, description)
# ──────────────────────────────────────────────────────────────────────────────
PAGES = [
    ("dashboard",        "/do/monitoring",                "Dashboard",
     "The top-level monitoring dashboard shows the current state of all destinations, "
     "active transfers, and system health at a glance."),

    ("monitoring-summary", "/do/monitoring/summary",      "Monitoring Summary",
     "A summary view grouping destinations by product and showing transfer status "
     "counts (queued, running, done, failed) per destination."),

    ("destinations",     "/do/transfer/destination",      "Destinations",
     "Lists all configured destinations. Each destination represents a target site "
     "to which OpenECPDS disseminates data, or from which it acquires data."),

    ("hosts",            "/do/transfer/host",             "Transfer Hosts",
     "Lists all transfer hosts. A host pairs a remote server address with a "
     "transfer method (FTP, SFTP, S3, etc.) and is associated with one or more "
     "destinations."),

    ("transfer-history", "/do/transfer/history?mode=&date=2026-07-24&destinationName=hourly_aq", "Transfer History",
     "Shows the history of completed and failed transfer requests across all "
     "destinations, with filtering by date range, destination, status, and target "
     "filename."),

    ("datafiles",        "/do/datafile/datafile?mode=&date=2026-07-24", "Data Files",
     "Lists all data files known to the system, with their size, checksum, "
     "storage location, and associated transfer requests."),

    ("transfer-groups",  "/do/datafile/transfergroup",    "Transfer Groups",
     "Transfer groups define pools of Data Movers. Destinations are assigned to "
     "a transfer group, and the scheduler selects an available mover from the pool."),

    ("transfer-servers", "/do/datafile/transferserver",   "Data Movers",
     "Lists all registered Data Mover servers, their current status, disk usage, "
     "and active connections."),

    ("datarates",        "/do/datafile/datarates",        "Data Rates",
     "Shows historical throughput charts for each Data Mover and destination, "
     "useful for identifying bottlenecks and capacity trends."),

    ("portal-traffic",   "/do/datafile/portaltraffic",    "Portal Traffic",
     "Displays Data Portal upload and download activity over time, broken down "
     "by user and destination."),

    ("mover-downloads",  "/do/datafile/moverdownloads",   "Mover Downloads",
     "Shows currently active download sessions on each Data Mover, including "
     "the remote client address and bytes transferred so far."),

    ("transfer-methods", "/do/transfer/method",           "Transfer Methods",
     "Transfer methods define the connection type and transfer module used by "
     "hosts (e.g. FTP, SFTP, S3). Each method links to a specific ECtrans module."),

    ("transfer-modules", "/do/transfer/module",           "Transfer Modules",
     "Modules are the Java implementations of each protocol adapter. This page "
     "lists installed modules and their configuration class names."),

    ("incoming-users",   "/do/user/incoming",             "Data Users",
     "Lists all Data Portal users (IncomingUsers). Each user has credentials, "
     "associated destinations, and a portal service mode (Standard, Open Access, "
     "or Self-Service)."),

    ("web-users",        "/do/user/user",                 "Web Users",
     "Lists all monitoring interface users. Users are assigned to categories "
     "which control which destinations and features they can access."),

    ("categories",       "/do/user/category",             "Categories",
     "Categories group Web Users and define their access rights: which "
     "destinations they can view, and which management operations they can perform."),

    ("policies",         "/do/user/policy",               "Policies",
     "Policies define sets of destinations that can be associated with Data Portal "
     "users, controlling which datasets a portal user can access."),

    ("resources",        "/do/user/resource",             "Resources",
     "Resources represent URL paths in the monitoring interface. They are assigned "
     "to categories to grant or restrict access to specific pages."),

    ("admin",            "/do/admin",                     "Administration",
     "The Administration section provides tools for system-wide operations: "
     "metadata field management, transfer requeue, file upload, and audit log."),
]

async def login(page, user, password):
    await page.goto(f"{BASE}/do/login", wait_until="networkidle")
    await page.fill('input[name="user"]', user)
    await page.fill('input[name="password"]', password)
    await page.click('button[type="submit"], input[type="submit"]')
    await page.wait_for_load_state("networkidle")

async def screenshot(page, url, slug, extra_wait=1500, max_height=4000):
    try:
        await page.goto(f"{BASE}{url}", wait_until="networkidle", timeout=25000)
        await page.wait_for_timeout(extra_wait)
        # Measure full document height and resize viewport so the sticky footer
        # renders at the true bottom and all content is visible in one shot.
        scroll_h = await page.evaluate("document.documentElement.scrollHeight")
        fit_h = min(scroll_h, max_height)
        await page.set_viewport_size({"width": 1400, "height": fit_h})
        await page.wait_for_timeout(300)  # allow reflow
        path = IMG_DIR / f"{slug}.png"
        await page.screenshot(path=str(path), full_page=False)
        # Reset for next page
        await page.set_viewport_size({"width": 1400, "height": 900})
        print(f"  ✓ {slug} ({fit_h}px tall)")
        return True
    except Exception as e:
        print(f"  ✗ {slug} ({url}): {e}")
        return False

async def get_first_id(page, list_url, link_pattern):
    """Visit a list page and extract the first entity ID from links."""
    try:
        await page.goto(f"{BASE}{list_url}", wait_until="networkidle", timeout=15000)
        links = await page.eval_on_selector_all(
            f'a[href*="{link_pattern}"]',
            "els => els.map(e => e.href)"
        )
        for href in links:
            m = re.search(rf'{re.escape(link_pattern)}/?(\d+)', href)
            if m:
                return m.group(1)
    except Exception:
        pass
    return None

async def main():
    async with async_playwright() as pw:
        browser = await pw.chromium.launch(headless=True)
        ctx = await browser.new_context(ignore_https_errors=True,
                                        viewport={"width": 1400, "height": 900})
        page = await ctx.new_page()

        print("=== Logging in as admin ===")
        await login(page, ADMIN_USER, ADMIN_PASS)

        print("\n=== Capturing list/dashboard pages (admin view) ===")
        for slug, url, title, desc in PAGES:
            await screenshot(page, url, slug)

        print("\n=== Discovering entity IDs for detail pages ===")
        # Destinations use names, not numeric IDs
        dest_links = await page.eval_on_selector_all(
            'a[href*="/do/transfer/destination/"]',
            "els => els.map(e => e.getAttribute('href'))"
        )
        await page.goto(f"{BASE}/do/transfer/destination", wait_until="networkidle")
        dest_links = await page.eval_on_selector_all(
            'a[href*="/do/transfer/destination/"]',
            "els => els.map(e => e.getAttribute('href'))"
        )
        dest_name = next((h.split("/do/transfer/destination/")[1]
                          for h in dest_links
                          if "/do/transfer/destination/" in h
                          and not any(x in h for x in ["edit", "insert", "delete", "update"])), None)

        host_id = await get_first_id(page, "/do/transfer/host", "/do/transfer/host/")

        await page.goto(f"{BASE}/do/user/incoming", wait_until="networkidle")
        user_links = await page.eval_on_selector_all(
            'a[href*="/do/user/incoming/"]',
            "els => els.map(e => e.getAttribute('href'))"
        )
        user_name = next((h.split("/do/user/incoming/")[1]
                          for h in user_links
                          if "/do/user/incoming/" in h
                          and not any(x in h for x in ["edit", "insert", "delete", "update"])), None)

        if dest_name:
            print(f"  destination name: {dest_name}")
            await screenshot(page, f"/do/transfer/destination/{dest_name}", "destination-detail")
        if host_id:
            print(f"  host id: {host_id}")
            await screenshot(page, f"/do/transfer/host/{host_id}", "host-detail")
            await screenshot(page, f"/do/transfer/host/edit/getReport/{host_id}", "host-report", extra_wait=3000)
        if user_name:
            print(f"  incoming user: {user_name}")
            await screenshot(page, f"/do/user/incoming/{user_name}", "incoming-detail")

        print("\n=== Capturing monitor-role view ===")
        await login(page, MONITOR_USER, MONITOR_PASS)
        await screenshot(page, "/do/monitoring", "dashboard-monitor")
        await screenshot(page, "/do/transfer/destination", "destinations-monitor")

        await browser.close()
        print(f"\n=== Done. Screenshots in {IMG_DIR} ===")
        print(f"  Total: {len(list(IMG_DIR.glob('*.png')))} images")

asyncio.run(main())
