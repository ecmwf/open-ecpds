#!/usr/bin/env python3
"""Capture Monitor UI screenshots from a running, suitably prepared instance."""

import argparse
import asyncio
import getpass
import json
import math
import os
import re
import shutil
import sys
import tempfile
import time
from pathlib import Path
from urllib.parse import quote, urlsplit

from playwright.async_api import async_playwright


ROOT = Path(__file__).resolve().parent.parent
VIEWPORT = {"width": 1440, "height": 1000}
PAGES = [
    ("operations", "/do/operations", "Operations"),
    ("destinations", "/do/transfer/destination", "Destinations"),
    ("hosts", "/do/transfer/host", "Transfer Hosts"),
    ("transfer-history", "/do/transfer/history", "Transfer History"),
    ("datafiles", "/do/datafile/datafile", "Data Files"),
    ("transfer-groups", "/do/datafile/transfergroup", "Transfer Groups"),
    ("transfer-servers", "/do/datafile/transferserver", "Data Movers"),
    ("datarates", "/do/datafile/datarates", "Data Rates"),
    ("portal-traffic", "/do/datafile/portaltraffic", "Data Portal Traffic"),
    ("mover-downloads", "/do/datafile/moverdownloads", "Download Activity"),
    ("transfer-methods", "/do/transfer/method", "Transfer Methods"),
    ("transfer-modules", "/do/transfer/module", "Transfer Modules"),
    ("incoming-users", "/do/user/incoming", "Data Users"),
    ("web-users", "/do/user/user", "Web Users"),
    ("api-clients", "/do/user/api", "API Clients"),
    ("categories", "/do/user/category", "Web Categories"),
    ("policies", "/do/user/policy", "Data Policies"),
    ("resources", "/do/user/resource", "Web Resources"),
    ("admin", "/do/admin", "Administration Tasks"),
    ("topology", "/do/monitoring/topology", "System Topology"),
    ("mqtt-activity", "/do/monitoring/mqtt", "MQTT Activity"),
]


def password(variable, label):
    value = os.environ.get(variable)
    if value is None:
        if not sys.stdin.isatty():
            raise ValueError(f"{variable} must be set when running without a terminal")
        value = getpass.getpass(f"{label} password: ")
    if not value:
        raise ValueError(f"{label} password cannot be empty")
    return value


async def navigate(page, base, path, title):
    response = await page.goto(base + path, wait_until="domcontentloaded", timeout=30000)
    if response is None or response.status >= 400:
        raise RuntimeError(f"{path}: HTTP {response.status if response else 'no response'}")
    await page.wait_for_timeout(1500)
    actual = await page.title()
    if actual != title or urlsplit(page.url).path != urlsplit(path).path:
        raise RuntimeError(f"{path}: expected page {title!r}, got {actual!r} at {page.url}")
    if len((await page.locator("body").inner_text()).strip()) < 80:
        raise RuntimeError(f"{path}: page content did not load")


async def login(page, base, user, secret):
    await page.goto(base + "/do/login", wait_until="domcontentloaded", timeout=30000)
    await page.locator('input[name="user"]').fill(user)
    await page.locator('input[name="password"]').fill(secret)
    await page.locator('button[type="submit"], input[type="submit"]').first.click()
    await page.wait_for_load_state("domcontentloaded")
    if urlsplit(page.url).path in ("/do/login", "/do/login/"):
        raise RuntimeError(f"Login failed for {user!r}")


async def capture(page, base, stage, slug, path, title):
    await navigate(page, base, path, title)
    if slug == "topology":
        await page.locator("#topoFitAllBtn").click()
        await page.wait_for_timeout(1000)
    if slug == "mqtt-activity":
        await page.locator("#mqttRows tr").first.wait_for()
    if slug == "transfer-history":
        await page.locator("#histPageLen").select_option("10")
        await page.wait_for_timeout(400)
        if "through null" in await page.locator("#transferHistoryTable").inner_text():
            raise RuntimeError("History contains incomplete portal events; prepare correctly recorded sample uploads")
    if slug in ("datarates", "portal-traffic"):
        selector = "#chartPeriodSelector" if slug == "datarates" else "#ptPeriodSelector"
        await page.locator(f'{selector} button[data-days="30"]').click()
        await page.wait_for_timeout(400)
    await save_screenshot(page, stage, slug)


async def save_screenshot(page, stage, slug):
    height = await page.evaluate("document.documentElement.scrollHeight")
    try:
        await page.set_viewport_size({"width": VIEWPORT["width"], "height": max(VIEWPORT["height"], height)})
        await page.wait_for_timeout(300)
        await page.screenshot(path=str(stage / f"{slug}.png"), full_page=True)
    finally:
        await page.set_viewport_size(VIEWPORT)
    print(f"Captured {slug}")


async def first_detail(page, base, list_path, title, pattern):
    await navigate(page, base, list_path, title)
    hrefs = await page.locator(f'a[href^="{list_path}/"]').evaluate_all(
        "(links) => links.map(link => link.getAttribute('href'))"
    )
    return next((href for href in hrefs if re.fullmatch(pattern, href)), None)


async def new_context(browser, args, **options):
    context = await browser.new_context(
        ignore_https_errors=args.insecure, viewport=VIEWPORT, color_scheme=args.theme, **options
    )
    await context.add_init_script(
        f"localStorage.setItem('ecpds-theme', {json.dumps(args.theme)});"
    )
    return context


def globe_snapshot(tick):
    now = int(time.time() * 1000)
    routes = [
        ("London", 51.51, -0.13, "GB", "SFTP", "Dissemination"),
        ("Paris", 48.86, 2.35, "FR", "HTTPS", "Dissemination"),
        ("Helsinki", 60.17, 24.94, "FI", "S3", "Dissemination"),
        ("Rome", 41.90, 12.50, "IT", "SFTP", "Dissemination"),
        ("New York", 40.71, -74.01, "US", "HTTPS", "Acquisition"),
        ("Tokyo", 35.68, 139.69, "JP", "S3", "Dissemination"),
        ("Cape Town", -33.92, 18.42, "ZA", "HTTPS", "Acquisition"),
    ]
    transfers = []
    for index, (city, lat, lon, country, protocol, host_type) in enumerate(routes):
        rate = int((45 + index * 11 + 12 * math.sin(tick / 3 + index)) * 1_000_000)
        transfers.append({
            "type": "transfer", "transferId": 900000 + index,
            "eventKey": f"documentation-{index}", "mover": "ecpds-mover",
            "destination": f"forecast_{country.lower()}", "host": f"docs-host-{index}",
            "hostLabel": city, "protocol": protocol, "fileSize": 20_000_000_000,
            "bytesSent": 100_000_000 + int(tick * rate / 8),
            "duration": (tick + 5) * 1000, "rateBitsPerSecond": rate,
            "status": "ACTIVE", "timestamp": now,
            "direction": host_type.upper(), "hostType": host_type,
            "hostLat": lat, "hostLon": lon, "hostCountry": country,
        })
    return {
        "type": "snapshot", "originResolved": True, "originLat": 51.34, "originLon": 12.37,
        "originHost": "ecpds-master", "transfers": transfers, "proxyHosts": [],
        "bytes24h": 842_000_000_000, "bytes24hDissemination": 611_000_000_000,
        "bytes24hAcquisition": 231_000_000_000,
        "bytes24hByType": {"Dissemination": 611_000_000_000, "Acquisition": 231_000_000_000,
                          "Replication": 0, "Source": 0, "Backup": 0, "Proxy": 0},
        "dataPortalSessions": 12, "dataPortalBytesInPerSecond": 2_500_000,
        "dataPortalBytesOutPerSecond": 18_000_000,
        "moverStorageUsedBytes": 438_000_000_000, "moverStorageTotalBytes": 1_000_000_000_000,
    }


async def prepare_globe(page):
    tasks = []

    def connect(socket):
        async def emit():
            tick = 0
            while True:
                socket.send(json.dumps(globe_snapshot(tick)))
                tick += 1
                await asyncio.sleep(1)

        task = asyncio.create_task(emit())
        tasks.append(task)
        socket.on_close(lambda *args: task.cancel())

    await page.route_web_socket("**/ws/globe", connect)
    return tasks


async def record_demo(browser, args, stage, storage):
    context = await new_context(
        browser, args, storage_state=storage,
        record_video_dir=str(stage), record_video_size=VIEWPORT,
    )
    page = await context.new_page()
    video = page.video
    try:
        path = "/do/transfer/destination/" + quote(args.destination, safe="")
        await navigate(page, args.base, path, "Destination " + args.destination)
        search = page.locator('input[placeholder*="expired=no"]').first
        await search.wait_for(state="visible", timeout=10000)
        await page.wait_for_timeout(1500)
        await search.fill("target=forecast*")
        await search.press("Enter")
        await page.wait_for_timeout(2500)
        await search.fill("")
        await search.press("Enter")
        await page.wait_for_timeout(1500)
    finally:
        await context.close()
    await video.save_as(str(stage / "destination-filter.webm"))


async def capture_globe(browser, args, stage, storage):
    options = {"record_video_dir": str(stage), "record_video_size": VIEWPORT} if args.video else {}
    context = await new_context(browser, args, storage_state=storage, **options)
    page = await context.new_page()
    video = page.video
    tasks = await prepare_globe(page) if args.globe_demo else []
    try:
        await navigate(page, args.base, "/do/monitoring/globe", "Live Earth")
        await page.locator(".cesium-widget canvas").wait_for(state="visible", timeout=30000)
        await page.wait_for_timeout(6000)
        await save_screenshot(page, stage, "live-earth")
        await page.locator("#globeMapModeBtn").click()
        await page.wait_for_timeout(2500)
        await save_screenshot(page, stage, "live-earth-map")
        if args.video:
            await page.wait_for_timeout(2000)
            await page.locator("#globeMapModeBtn").click()
            await page.wait_for_timeout(2500)
    finally:
        for task in tasks:
            task.cancel()
        await asyncio.gather(*tasks, return_exceptions=True)
        await context.close()
    if video:
        await video.save_as(str(stage / "live-earth.webm"))


async def capture_mqtt_details(page, args, stage):
    await navigate(page, args.base, "/do/monitoring/mqtt", "MQTT Activity")
    await page.locator("#mqttRows button").first.wait_for(state="visible", timeout=15000)
    await page.locator("#mqttPause").click()
    await page.locator("#mqttRows button").first.click()
    await page.locator("#mqttDetails.show").wait_for(state="visible")
    await page.wait_for_timeout(400)
    await save_screenshot(page, stage, "mqtt-details")


async def record_mqtt(browser, args, stage, storage):
    context = await new_context(
        browser, args, storage_state=storage,
        record_video_dir=str(stage), record_video_size=VIEWPORT,
    )
    page = await context.new_page()
    video = page.video
    try:
        await navigate(page, args.base, "/do/monitoring/mqtt", "MQTT Activity")
        await page.locator("#mqttRows button").first.wait_for(state="visible", timeout=15000)
        await page.locator("#mqttPause").click()
        await page.wait_for_timeout(1500)
        await page.locator("#mqttRows button").first.click()
        await page.locator("#mqttDetails.show").wait_for(state="visible")
        await page.wait_for_timeout(2500)
        await page.locator("#mqttDetails .btn-close").click()
        await page.wait_for_timeout(1500)
    finally:
        await context.close()
    await video.save_as(str(stage / "mqtt-activity.webm"))


async def run(args, admin_password, monitor_password):
    args.output_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix=".screenshots-", dir=args.output_dir) as tmp:
        stage = Path(tmp)
        async with async_playwright() as pw:
            browser = await pw.chromium.launch(
                headless=True, args=["--disable-dev-shm-usage", "--enable-unsafe-swiftshader"]
            )
            try:
                context = await new_context(browser, args)
                page = await context.new_page()
                await login(page, args.base, args.admin_user, admin_password)
                for slug, path, title in PAGES:
                    if slug == "transfer-history":
                        path += "?destinationName=" + quote(args.destination, safe="")
                    await capture(page, args.base, stage, slug, path, title)
                destination_path = "/do/transfer/destination/" + quote(args.destination, safe="")
                await capture(page, args.base, stage, "destination-detail", destination_path,
                              "Destination " + args.destination)
                host = await first_detail(page, args.base, "/do/transfer/host", "Transfer Hosts",
                                          r"/do/transfer/host/\d+")
                incoming = await first_detail(page, args.base, "/do/user/incoming", "Data Users",
                                              r"/do/user/incoming/[^/?#]+")
                if not host or not incoming:
                    raise RuntimeError("The instance must contain at least one host and data user")
                await detail_page(page, args.base, host, "Show Transfer Host")
                await save_screenshot(page, stage, "host-detail")
                await detail_page(page, args.base, incoming, "Show Data User")
                await save_screenshot(page, stage, "incoming-detail")
                web_user = await first_detail(page, args.base, "/do/user/user", "Web Users",
                                              r"/do/user/user/[^/?#]+")
                if not web_user:
                    raise RuntimeError("The instance must contain at least one Web User")
                await detail_page(page, args.base, web_user, "Show Web User")
                await save_screenshot(page, stage, "web-user-detail")
                api_path = "/do/user/api/" + quote(args.api_client, safe="")
                await detail_page(page, args.base, api_path, "API Client")
                await save_screenshot(page, stage, "api-client-detail")
                await capture_mqtt_details(page, args, stage)
                storage = await context.storage_state()
                if args.video:
                    await record_demo(browser, args, stage, storage)
                    await record_mqtt(browser, args, stage, storage)
                await context.close()
                await capture_globe(browser, args, stage, storage)

                context = await new_context(browser, args)
                page = await context.new_page()
                await login(page, args.base, args.monitor_user, monitor_password)
                await capture(page, args.base, stage, "destinations-monitor",
                              "/do/transfer/destination", "Destinations")
                await context.close()
            finally:
                await browser.close()

        for item in stage.iterdir():
            if item.suffix == ".png" or item.name in (
                "destination-filter.webm", "live-earth.webm", "mqtt-activity.webm"
            ):
                shutil.move(str(item), args.output_dir / item.name)
    print(f"Verified screenshots saved to {args.output_dir}")


async def detail_page(page, base, path, title):
    response = await page.goto(base + path, wait_until="domcontentloaded", timeout=30000)
    if response is None or response.status >= 400 or urlsplit(page.url).path != path:
        raise RuntimeError(f"Could not open detail page {path}")
    await page.wait_for_timeout(1500)
    actual = await page.title()
    if not actual.startswith(title):
        raise RuntimeError(f"Invalid detail page {path}: {actual!r}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", default=os.environ.get("MONITOR_UI_BASE", "https://localhost:8443"))
    parser.add_argument("--output-dir", type=Path, default=ROOT / "docs/monitor-ui/img")
    parser.add_argument("--destination", default="aviso_demo",
                        help="destination used for detail screenshots and video (default: aviso_demo)")
    parser.add_argument("--admin-user", default=os.environ.get("MONITOR_ADMIN_USER", "admin"))
    parser.add_argument("--monitor-user", default=os.environ.get("MONITOR_MON_USER", "monitor"))
    parser.add_argument("--api-client", default="api", help="existing API client for detail view (default: api)")
    parser.add_argument("--insecure", action="store_true", help="accept self-signed TLS certificates")
    parser.add_argument("--video", action="store_true", help="also record queue, MQTT and Live Earth interactions")
    parser.add_argument("--theme", choices=("dark", "light"), default="dark",
                        help="UI theme for screenshots and recordings (default: dark)")
    parser.add_argument("--globe-demo", action="store_true",
                        help="use browser-only sample transfer events for Live Earth")
    args = parser.parse_args()
    args.base = args.base.rstrip("/")
    try:
        asyncio.run(run(args, password("MONITOR_ADMIN_PASS", "Admin"),
                        password("MONITOR_MON_PASS", "Monitor")))
    except Exception as exc:
        print(f"Screenshot capture failed: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
