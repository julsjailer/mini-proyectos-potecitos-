# Mini proyectos potecitos

Prueba de grabación y reproducción de audio para teléfonos compatibles con Java ME (J2ME).

El programa principal, `PruebaAudioMIDlet`, permite capturar aproximadamente 5 segundos de audio con el micrófono y reproducir la última grabación desde una interfaz sencilla con teclado numérico.

## Funciones

- **Tecla 1 — Grabar:** inicia una grabación y la detiene automáticamente tras aproximadamente 5 segundos.
- **Tecla 2 — Reproducir:** reproduce el audio capturado. Si no hay una grabación disponible, muestra un aviso.
- **Salir:** cierra la aplicación.
- Muestra mensajes de estado y errores en pantalla.

## Plataforma y requisitos

El código utiliza Java ME, las API MIDlet y LCDUI, y las API multimedia `javax.microedition.media` y `RecordControl`.

Para utilizarlo se necesita un teléfono o emulador compatible con estas API y con captura de audio. La disponibilidad de grabación, los formatos admitidos y los permisos dependen del entorno.

No es una aplicación Android nativa ni una aplicación web.

## Uso

1. Compila y empaqueta el proyecto con herramientas compatibles con Java ME y las API multimedia utilizadas.
2. Instala o carga la aplicación en un dispositivo o emulador compatible.
3. Abre la aplicación y pulsa `1` para grabar.
4. Espera a que aparezca el mensaje de grabación terminada.
5. Pulsa `2` para escucharla o utiliza `Salir` para cerrar.

Estas indicaciones describen el flujo previsto; no constituyen un procedimiento de compilación verificado.

## Cómo funciona

La aplicación intenta abrir la captura con `capture://audio`. Si falla, vuelve a intentarlo con `capture://audio?encoding=amr`.

El audio se mantiene en memoria. Para reproducirlo, el programa interpreta una cabecera `RIFF` como WAV (`audio/x-wav`); si no la encuentra, supone que es AMR (`audio/amr`). Esta detección es una heurística, no una validación completa del formato.

La captura y la reproducción se ejecutan en hilos separados de la interacción con el usuario.

## Estructura

```text
.
├── .github/
├── MANIFEST.MF
├── src/
│   └── PruebaAudioMIDlet.java
└── stubs/
```

La lógica de la interfaz, la grabación y la reproducción está en `src/PruebaAudioMIDlet.java`.

## Limitaciones

- Solo se mantiene una grabación en memoria; iniciar otra descarta la anterior.
- El código no implementa guardado permanente ni envío del audio por Internet.
- No incluye una opción para detener manualmente la grabación antes del corte automático.
- La compatibilidad real depende del dispositivo o emulador y de su implementación de las API multimedia.
- La descripción se basa en la lectura del código; no se ha verificado su ejecución ni su compilación.
