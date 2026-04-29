#!/usr/bin/env python3
"""
Script para crear el logro ultra exclusivo "Car of the Day" en Firebase
"""

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import os
import json

# Ruta al archivo de credenciales
# Obtener la ruta del directorio actual del script
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
SERVICE_ACCOUNT_KEY_PATH = os.path.join(SCRIPT_DIR, "keys.json")

def initialize_firebase():
    """Inicializar Firebase Admin SDK"""
    try:
        app = firebase_admin.get_app()
    except ValueError:
        if not os.path.exists(SERVICE_ACCOUNT_KEY_PATH):
            raise FileNotFoundError(f"No se encontró keys.json en {SERVICE_ACCOUNT_KEY_PATH}")

        cred = credentials.Certificate(SERVICE_ACCOUNT_KEY_PATH)
        app = firebase_admin.initialize_app(cred)

    return firestore.client()

def create_car_of_the_day_achievement(db):
    """Crear el logro 'Car of the Day'"""

    achievement_data = {
        "id": "car_of_the_day",
        "title": "Carro del Día",
        "description": "Posee el carro destacado del día. Este logro se otorga automáticamente cuando tienes el carro del día.",
        "iconUrl": "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Fcar_of_the_day.png?alt=media",
        "category": "SPECIAL",
        "rarity": "SPECIAL",
        "conditions": [],
        "goal": 1,
        "rules": {
            "conditionLogic": "AND",
            "timeWindow": None,
            "uniquePerCar": True
        },
        "hidden": False,
        "active": True,
        "createdAt": firestore.SERVER_TIMESTAMP,
        "isExclusive": False,
        "exclusiveUserIds": []
    }

    try:
        # Crear o actualizar el documento
        db.collection("achievements").document("car_of_the_day").set(achievement_data)
        print("✅ Logro 'Car of the Day' creado exitosamente!")
        print(f"   ID: car_of_the_day")
        print(f"   Título: {achievement_data['title']}")
        print(f"   Rareza: {achievement_data['rarity']}")
        return True
    except Exception as e:
        print(f"❌ Error al crear el logro: {e}")
        return False

def main():
    print("=" * 60)
    print("🎯 CREAR LOGRO ULTRA EXCLUSIVO: CAR OF THE DAY")
    print("=" * 60)
    print()

    # Verificar que el archivo de credenciales existe
    if not os.path.exists(SERVICE_ACCOUNT_KEY_PATH):
        print(f"❌ Error: No se encontró el archivo de credenciales en:")
        print(f"   {SERVICE_ACCOUNT_KEY_PATH}")
        print()
        print("Por favor, asegúrate de que keys.json esté en la raíz del proyecto")
        return

    # Inicializar Firebase
    print("🔐 Inicializando Firebase Admin SDK...")
    try:
        db = initialize_firebase()
        print("✅ Firebase inicializado correctamente")
    except Exception as e:
        print(f"❌ Error al inicializar Firebase: {e}")
        return

    print()

    # Crear el logro
    print("📝 Creando logro 'Car of the Day'...")
    print()

    success = create_car_of_the_day_achievement(db)

    print()
    print("=" * 60)
    if success:
        print("🎉 ¡Logro creado exitosamente!")
        print()
        print("📌 DETALLES DEL LOGRO:")
        print("   • ID: car_of_the_day")
        print("   • Nombre: Carro del Día")
        print("   • Rareza: SPECIAL (1200 XP)")
        print("   • Evaluación: Automática al iniciar sesión")
        print()
        print("🎯 FUNCIONAMIENTO:")
        print("   • Se verifica si el usuario tiene el carro del día")
        print("   • El contador (progress) aumenta cada vez que coincide")
        print("   • Muestra: '🎯 Obtenido X veces'")
    else:
        print("❌ No se pudo crear el logro")
    print("=" * 60)

if __name__ == "__main__":
    main()

