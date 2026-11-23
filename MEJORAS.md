# Primera mejora de la grabadora

## Cambios

- Tecla 3 para detener la grabacion o reproduccion.
- Tecla 4 para elegir 5, 10 o 30 segundos antes de grabar.
- Contador de segundos durante la captura.
- Bloqueo de operaciones simultaneas.
- Se conserva el audio anterior hasta completar una nueva captura valida.
- Se invoca RecordControl.commit() antes de leer los bytes.
- Se comprueban las firmas WAV, AMR y AMR-WB; para otros casos se consulta el tipo declarado por el reproductor.
- Se solicita detener el audio al pausar o cerrar, y el hilo libera el reproductor en finally. La liberacion no es sincrona y depende de que las llamadas multimedia retornen.
- Reanudar la aplicacion ya no crea una pantalla nueva ni descarta la grabacion.

## Limites y estado de validacion

No se ha compilado ni ejecutado esta version en un entorno Java ME. Son cambios pendientes de verificar en dispositivo o emulador. El README inicial describe la version anterior; los controles y cambios actuales se documentan aqui.

Las grabaciones siguen siendo temporales y no se guardan al salir. No hay exportacion ni envio por red. La duracion limita el tiempo de captura, pero no garantiza un limite estricto de memoria: depende del formato del dispositivo.

## Pruebas manuales pendientes

1. Abrir y pulsar 2 sin grabacion: debe mostrar un aviso.
2. Grabar 5 segundos y reproducir; comprobar que hay audio audible.
3. Pulsar 3 durante una grabacion y comprobar que puede escucharse el fragmento.
4. Probar las duraciones de 10 y 30 segundos.
5. Pulsar repetidamente 1 y 2 durante una operacion: no deben iniciarse operaciones simultaneas.
6. Denegar el permiso de microfono: comprobar el mensaje y que se pueda reintentar.
7. Tras una captura correcta, provocar un fallo de grabacion: comprobar que sigue disponible el audio anterior.
8. Pausar y reanudar durante captura y reproduccion: comprobar liberacion de recursos y conservacion del audio.
9. Cerrar durante una operacion: verificar que termina el uso del microfono o altavoz.
10. Probar WAV y AMR en los entornos disponibles; registrar dispositivo, formato, bytes y resultado.
