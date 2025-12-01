import requests
from bs4 import BeautifulSoup
import json
import os
from collections import defaultdict

URL = "https://hotwheels.fandom.com/wiki/2012_Treasure_Hunts_Series"
YEAR = 2012

response = requests.get(URL)
soup = BeautifulSoup(response.content, "html.parser")

# Encuentra la tabla principal (wikitable más relevante)
tables = soup.find_all("table", class_="wikitable")

# Puedes revisar si hay más de una tabla; asumiremos la primera
table = tables[0]

# Dictionary to group cars by name
cars_by_name = defaultdict(list)

# Filas de la tabla (omitimos encabezado)
for row in table.find_all("tr")[1:]:
    cols = row.find_all("td")
    if len(cols) < 13:
        continue

    try:
        series = cols[2].get_text(strip=True)
        model_name = cols[3].get_text(strip=True)
        color = cols[4].get_text(strip=True) if len(cols) > 4 else "Unknown"

        # Imagen
        a_tag = cols[11].find("a", href=True)
        photo_url = a_tag["href"] if a_tag else ""

        # Add this variation to the car's list
        cars_by_name[model_name].append({
            "name": model_name,
            "year": str(YEAR),
            "series": series,
            "color": color,
            "url": photo_url
        })

    except Exception as e:
        print(f"Error procesando fila: {e}")
        continue

# Convert the defaultdict to a regular dict for JSON serialization
all_cars = dict(cars_by_name)

# Save all cars to a single JSON file
output_file = f"treasure_hunts_{YEAR}.json"

with open(output_file, "w", encoding="utf-8") as f:
    json.dump(all_cars, f, indent=2, ensure_ascii=False)

total_variations = sum(len(variations) for variations in all_cars.values())
print(f"Se extrajeron {len(all_cars)} modelos de autos con {total_variations} variaciones en total.")
print(f"Archivo creado: '{output_file}'")

