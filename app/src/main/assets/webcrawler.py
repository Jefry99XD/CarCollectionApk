import requests
from bs4 import BeautifulSoup
import json

URL = "https://hotwheels.fandom.com/wiki/2012_Treasure_Hunts_Series"
YEAR = 2012

response = requests.get(URL)
soup = BeautifulSoup(response.content, "html.parser")

# Encuentra la tabla principal (wikitable más relevante)
tables = soup.find_all("table", class_="wikitable")

# Puedes revisar si hay más de una tabla; asumiremos la primera
table = tables[0]

treasure_hunts = []

# Filas de la tabla (omitimos encabezado)
for row in table.find_all("tr")[1:]:
    cols = row.find_all("td")
    if len(cols) < 13:
        continue

    try:
        series = cols[2].get_text(strip=True)
        model_name = cols[3].get_text(strip=True)
        case = cols[12].get_text(strip=True)

        # Armar el nombre como se pidió
        name = f"{model_name} (Caja {case})"

        # Imagen
        a_tag = cols[11].find("a", href=True)
        photo_url = a_tag["href"] if a_tag else ""


        treasure_hunts.append({
            "name": name,
            "year": YEAR,
            "series": series,
            "regularPhotoUrl": photo_url
        })

    except Exception as e:
        print(f"Error procesando fila: {e}")
        continue

# Guardar en archivo JSON
with open("treasure_hunts_2024.json", "w", encoding="utf-8") as f:
    json.dump(treasure_hunts, f, indent=2, ensure_ascii=False)

print(f"Se extrajeron {len(treasure_hunts)} treasure hunts.")
