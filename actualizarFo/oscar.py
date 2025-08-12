import requests
import json
import os

# Reemplaza con tus valores
OWNER = "polarismkr"
REPO = "diecastimghoster"
PATH = "diecast"

API_URL = f"https://api.github.com/repos/{OWNER}/{REPO}/contents/{PATH}"

def fetch_all_files():
    results = []
    stack = [API_URL]
    while stack:
        url = stack.pop()
        resp = requests.get(url)
        resp.raise_for_status()
        items = resp.json()
        for item in items:
            if item['type'] == 'file':
                name = os.path.splitext(item['name'])[0]
                url = item['download_url']
                results.append({"name": name, "url": url})
            elif item['type'] == 'dir':
                stack.append(item['url'])
    return results

if __name__ == "__main__":
    files = fetch_all_files()
    with open("diecast_images.json", "w", encoding="utf-8") as f:
        json.dump(files, f, ensure_ascii=False, indent=2)
    print(f"Exported {len(files)} entries to diecast_images.json")
