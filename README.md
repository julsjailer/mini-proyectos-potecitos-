# Prueba de Audio — Java ME

Aplicación MIDlet para grabar y reproducir audio en teléfonos con Java ME / J2ME.
Está diseñada para dispositivos antiguos, con interfaz sencilla, teclas numéricas
y grabación por segmentos.

## Funciones actuales

- Grabar audio desde el micrófono.
- Seleccionar duración: 5, 10, 15, 30 o 60 segundos.
- Pausar y continuar una grabación.
- Finalizar la grabación manualmente.
- Reproducir el audio grabado.
- Ajustar el volumen con las teclas 7 y 9.
- Detectar formatos WAV, AMR y AMR-WB.
- Mostrar estado y diagnóstico cuando ocurre un error.

## Controles

| Tecla | Acción |
|---:|---|
| 1 | Iniciar grabación |
| 2 | Reproducir audio |
| 3 | Pausar / continuar |
| 4 | Finalizar grabación |
| 5 | Cambiar duración |
| 7 | Bajar volumen |
| 9 | Subir volumen |

## Requisitos

- Teléfono con Java ME / MIDP 2.0.
- Soporte de `javax.microedition.media`.
- Micrófono disponible para la aplicación.
- Para instalar desde archivo: habilitar instalación de aplicaciones no firmadas,
  si el teléfono lo permite.

## Instalación

1. Descarga el archivo `.jad` y `.jar` desde la sección **Releases**.
2. Copia ambos archivos al teléfono, o abre el `.jad` desde el navegador del equipo.
3. Instala la aplicación y concede permisos de micrófono si el sistema los solicita.

## Compilación

El proyecto está preparado para compilarse mediante GitHub Actions.

- Rama de desarrollo: `fix/grabacion-audio-interfaz`
- Rama estable: `main`
- Versiones: [SemVer](https://semver.org/), por ejemplo `v1.0.0`
- Cada versión publicada creará un **GitHub Release** con los archivos instalables.

## Roadmap

- Guardar grabaciones en la memoria del teléfono.
- Lista de grabaciones con reproducción y borrado.
- Renombrar grabaciones.
- Exportar audio por Bluetooth, correo o tarjeta de memoria, según el equipo.
- Indicador visual de nivel de micrófono.
- Mejor manejo de errores por modelo de teléfono.
- Pruebas automáticas de empaquetado.
- Publicación automática de Releases con `JAR` y `JAD`.

## Solución de problemas

| Problema | Posible causa | Acción |
|---|---|---|
| No graba | El teléfono no permite acceso al micrófono | Revisa permisos y prueba en otra aplicación de grabación |
| “RecordControl no disponible” | El firmware no expone la API de grabación | El dispositivo puede no ser compatible |
| No reproduce | Formato no soportado por el reproductor | Prueba grabar en otro formato si la versión futura lo permite |
| La app se cierra | Falta de memoria | Borra archivos innecesarios y reinicia el teléfono |

## Licencia

Pendiente de definir.
