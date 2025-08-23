import requests
from bs4 import BeautifulSoup
import json
import re
import time  # ← IMPORTANTE: para usar sleep

BASE_URL = "https://hotwheels.fandom.com"
HEADERS = {"User-Agent": "Mozilla/5.0 (compatible; HotWheelsScraper/1.0)"}
DELAY_SECONDS = 1.5  # ← Ajusta el tiempo entre solicitudes

def expand_rowspan_table(table):
    rows = []
    for row in table.find_all("tr"):
        rows.append(row.find_all(["td", "th"]))
    return rows

def scrape_model_images(model_url, model_name, seen):
    full = BASE_URL + model_url if not model_url.startswith("http") else model_url
    time.sleep(DELAY_SECONDS)  # ← Espera antes de hacer request
    resp = requests.get(full, headers=HEADERS)
    if resp.status_code != 200:
        print("   Error al acceder:", resp.status_code)
        return []

    soup = BeautifulSoup(resp.text, "html.parser")
    output = []

    for table in soup.find_all("table", class_="wikitable"):
        header_tag = table.find_previous(["h1", "h2", "h3", "h4", "h5", "h6"])
        fallback_year = None
        if header_tag:
            match = re.search(r"\b(19|20)\d{2}\b", header_tag.get_text())
            if match:
                fallback_year = match.group(0)

        hdr = table.find("tr")
        if not hdr:
            continue

        headers = [cell.get_text(strip=True) for cell in hdr.find_all(["th", "td"])]
        if "Photo" not in headers:
            continue

        photo_idx = headers.index("Photo")
        year_idx = headers.index("Year") if "Year" in headers else None
        series_idx = headers.index("Series") if "Series" in headers else None
        color_idx = headers.index("Color") if "Color" in headers else None

        rows = expand_rowspan_table(table)[1:]
        for cells in rows:
            if len(cells) <= photo_idx:
                continue

            row_year = cells[year_idx].get_text(strip=True) if year_idx is not None else fallback_year
            row_year = re.search(r"\b(19|20)\d{2}\b", row_year or "")
            row_year = row_year.group(0) if row_year else fallback_year or ""

            series = cells[series_idx].get_text(" ", strip=True) if series_idx is not None else ""
            color = cells[color_idx].get_text(" ", strip=True) if color_idx is not None else ""
            photo_cell = cells[photo_idx]

            for link in photo_cell.find_all("a", href=True):
                href = link["href"]
                href = href if href.startswith("http") else BASE_URL + href

                entry = {
                    "name": model_name,
                    "year": row_year,
                    "series": series,
                    "color": color,
                    "url": href
                }

                key = (model_name, row_year, series, color, href)
                if key not in seen:
                    seen.add(key)
                    output.append(entry)
    return output

def scrape_single_model(full_url):
    if not full_url.startswith("http"):
        print("❌ URL inválida.")
        return

    time.sleep(DELAY_SECONDS)  # ← Espera antes de acceder a la página principal
    resp = requests.get(full_url, headers=HEADERS)
    if resp.status_code != 200:
        print("❌ Error al acceder a la página del modelo:", resp.status_code)
        return

    soup = BeautifulSoup(resp.text, "html.parser")
    title = soup.find("h1")
    model_name = title.get_text(strip=True) if title else "unknown"

    model_path = full_url.replace(BASE_URL, "")
    seen = set()
    data = scrape_model_images(model_path, model_name, seen)

    if data:
        filename = f"{model_name.replace(' ', '_')}.json"
        with open(filename, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"✅ {len(data)} imágenes guardadas en {filename}")
    else:
        print("⚠️ No se encontró información.")

# ---- Configura aquí tu link de modelo ----
if __name__ == "__main__":
    url_del_carro = "https://hotwheels.fandom.com/wiki/%2770_Toyota_Celica"
    scrape_single_model(url_del_carro)
