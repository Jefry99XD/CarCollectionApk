import os
import json

def cargar_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def unir_json_en_carpeta(carpeta):
    archivos = [f for f in os.listdir(carpeta) if f.endswith('.json')]
    todos_los_datos = []

    for archivo in archivos:
        ruta_completa = os.path.join(carpeta, archivo)
        try:
            data = cargar_json(ruta_completa)
            if isinstance(data, list):
                todos_los_datos.extend(data)
            else:
                todos_los_datos.append(data)
        except Exception as e:
            print(f"Error al leer {archivo}: {e}")

    return todos_los_datos

def main():
    carpeta = "json"
    if not os.path.isdir(carpeta):
        print("La ruta no es válida o no es una carpeta.")
        return

    datos_unidos = unir_json_en_carpeta(carpeta)

    output_path = os.path.join(carpeta, 'json_unido.json')
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(datos_unidos, f, ensure_ascii=False, indent=2)

    print(f"\nArchivos combinados guardados en: {output_path}")

if __name__ == "__main__":
    main()
