import requests
from bs4 import BeautifulSoup
import json

URL = "https://hotwheels.fandom.com/wiki/2012_Treasure_Hunts_Series"
YEAR = 2012  # Cambia si scrapeas otro año

response = requests.get(URL)
soup = BeautifulSoup(response.content, "html.parser")

# Encuentra todas las tablas, normalmente la segunda o tercera es la de STH
tables = soup.find_all("table", class_="wikitable")

# ⚠️ Puedes ajustar el índice si en el futuro la posición cambia
table = tables[1]  # ← posiblemente esta sea la tabla de STH

super_treasure_hunts = []

for row in table.find_all("tr")[1:]:  # saltar encabezado
    cols = row.find_all("td")
    if len(cols) < 13:
        continue

    try:
        series_raw = cols[2].get_text(strip=True)
        series = series_raw.split("\n")[0]
        model_name = cols[3].get_text(strip=True)
        case = cols[12].get_text(strip=True)
        name = f"{model_name}(Caja {case})"

        # Obtener href de la imagen grande
        a_tag = cols[11].find("a", href=True)
        sth_photo_url = a_tag["href"] if a_tag else ""

        # Ignorar si no hay imagen o nombre
        if not model_name or not sth_photo_url:
            continue

        super_treasure_hunts.append({
            "name": name,
            "year": YEAR,
            "series": "Treasure Hunts",
            "regularPhotoUrl": "",
            "sthPhotoUrl": sth_photo_url
        })

    except Exception as e:
        print(f"Error procesando fila: {e}")
        continue

# Guardar JSON
with open("super_treasure_hunts_2025.json", "w", encoding="utf-8") as f:
    json.dump(super_treasure_hunts, f, indent=2, ensure_ascii=False)

print(f"✅ Se extrajeron {len(super_treasure_hunts)} Super Treasure Hunts.")
