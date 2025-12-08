SistemaECG
===========

Proyecto Java Swing: monitor ECG con soporte MQTT (Mosquitto) y control de motor ESP8266.

Contenido
- `SistemaECG.java`: aplicación principal (UI + lógica)

Quick start
1. Compilar:

```powershell
cd C:\Users\jacki
javac SistemaECG.java
```

2. Ejecutar:

```powershell
java SistemaECG
```

3. Conectar al broker MQTT (ejemplo local):
- Broker: `localhost:1883`
- Topic de datos ECG: `ecg/datos`
- Topic del motor (encender/apagar): `esp8266/motor`

Ejemplo para publicar valores con mosquitto (desde otra terminal):

```bash
mosquitto_pub -h localhost -t ecg/datos -m "120.5"
mosquitto_pub -h localhost -t esp8266/motor -m "on"
mosquitto_pub -h localhost -t esp8266/motor -m "off"
```

Subir al repositorio remoto (GitHub)
1. Crea un repositorio vacío en GitHub.
2. En tu máquina, añade el remote y sube:

```bash
cd C:\Users\jacki
git remote add origin https://github.com/USER/REPO.git
git branch -M main
git push -u origin main
```

Notas
- Este proyecto usa una implementación MQTT simulada en `SistemaECG.java`. Para producción, integra una librería MQTT (p. ej. Eclipse Paho) y reemplaza `MQTTDataReader`.
- Ajusta `user.name` y `user.email` en git si es necesario.
