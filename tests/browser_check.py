"""Functional desktop/mobile smoke tests against the generated static explorer."""
import functools
import http.server
import json
import threading
from pathlib import Path
from playwright.sync_api import sync_playwright
ROOT=Path(__file__).resolve().parents[1]
handler=functools.partial(http.server.SimpleHTTPRequestHandler,directory=str(ROOT/"build/site"))
server=http.server.ThreadingHTTPServer(("127.0.0.1",0),handler)
thread=threading.Thread(target=server.serve_forever,daemon=True);thread.start()
errors=[]
try:
    with sync_playwright() as p:
        browser=p.chromium.launch()
        page=browser.new_page(viewport={"width":1440,"height":1000})
        page.on("pageerror",lambda error:errors.append(str(error)))
        page.goto(f"http://127.0.0.1:{server.server_port}",wait_until="networkidle")
        page.locator("#query").fill("743474")
        page.locator(".result").wait_for()
        assert page.locator(".result").count()==1
        page.locator(".result").click()
        page.locator(".chart").wait_for()
        assert "missing" in page.locator(".warning-text").inner_text().lower()
        assert "Obama" in page.locator("#detail h2").inner_text()
        with page.expect_download() as download:
            page.get_by_text("Download original contract files",exact=True).click()
        assert download.value.suggested_filename=="743474.zip"
        page.locator("#active-only").check()
        assert "rows shown" in page.locator("#detail .chart-note").inner_text()
        page.locator("#query").fill("no_such_contract_zzzz")
        assert page.locator(".result").count()==0
        page.locator("#reset").click()
        assert page.locator(".result").count()>1
        page.locator("#next").click()
        assert page.locator("#page").inner_text().startswith("2 /")
        page.locator(".result").first.focus()
        page.keyboard.press("Enter")
        page.wait_for_timeout(200)
        assert page.locator(".result").first.get_attribute("aria-current")=="true"
        page.set_viewport_size({"width":390,"height":844})
        page.wait_for_timeout(200)
        assert page.evaluate("document.documentElement.scrollWidth <= window.innerWidth + 2")
        page.screenshot(path=str(ROOT/"build/mobile.png"),full_page=True)
        page.set_viewport_size({"width":1440,"height":1000})
        page.screenshot(path=str(ROOT/"build/desktop.png"),full_page=True)
        assert not errors,errors
        browser.close()
    print("Browser checks passed: search, selection, missingness, chart filter, download, reset, pagination, keyboard, mobile overflow.")
finally:
    server.shutdown();server.server_close()
