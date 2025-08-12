import json

def remove_duplicates_by_image(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error al leer el archivo: {e}")
        return

    seen_urls = set()
    unique_entries = []

    for entry in data:
        url = entry.get("url", "").strip()
        if url and url not in seen_urls:
            seen_urls.add(url)
            unique_entries.append(entry)

    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(unique_entries, f, indent=2, ensure_ascii=False)
        print(f"Archivo actualizado. Se guardaron {len(unique_entries)} entradas únicas por imagen.")
    except Exception as e:
        print(f"Error al escribir el archivo: {e}")

# Ejemplo de uso
remove_duplicates_by_image("diecast_images.json")
