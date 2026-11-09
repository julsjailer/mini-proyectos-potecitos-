import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.*;
import java.util.Vector;

public class PruebaAudioMIDlet extends MIDlet
        implements CommandListener {

    private Display display;
    private AudioCanvas canvas;
    private Command salirCmd;

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new AudioCanvas();
        salirCmd = new Command("Salir", Command.EXIT, 1);
        canvas.addCommand(salirCmd);
        canvas.setCommandListener(this);
        display.setCurrent(canvas);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) {
        if (c == salirCmd) {
            if (canvas != null) {
                canvas.detenerTodo();
            }
            notifyDestroyed();
        }
    }

    class AudioCanvas extends Canvas {

        private Vector segmentos = new Vector();
        private Vector formatos = new Vector();

        private String estado = "Listo para grabar";
        private String diagnostico = "";
        private int[] duraciones = {5, 10, 15, 30, 60};
        private int indiceDuracion = 0;
        private int segundosRestantes = 0;
        private int volumen = 70;

        private boolean ocupado = false;
        private boolean grabando = false;
        private boolean pausada = false;
        private boolean finalizar = false;

        private Player reproductor;
        private VolumeControl controlVolumen;

        private Font titulo;
        private Font normal;
        private Font pequeno;

        AudioCanvas() {
            titulo = Font.getFont(Font.FACE_SYSTEM,
                    Font.STYLE_BOLD, Font.SIZE_MEDIUM);
            normal = Font.getFont(Font.FACE_SYSTEM,
                    Font.STYLE_PLAIN, Font.SIZE_SMALL);
            pequeno = Font.getFont(Font.FACE_SYSTEM,
                    Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();

            g.setColor(0x071629);
            g.fillRect(0, 0, w, h);

            g.setColor(0x12385A);
            g.fillRect(0, 0, w, 30);

            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("PRUEBA DE AUDIO", 7, 6,
                    Graphics.TOP | Graphics.LEFT);

            int margen = 7;
            int ancho = w - margen * 2;
            int y = 38;
            int tarjetaH = 37;

            dibujarTarjeta(g, margen, y, ancho, tarjetaH,
                    0x145078);
            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("1  GRABAR", margen + 9, y + 3,
                    Graphics.TOP | Graphics.LEFT);
            g.setFont(pequeno);
            g.setColor(0xD5E8F5);
            g.drawString("Duracion: " + duraciones[indiceDuracion]
                    + " segundos", margen + 9, y + 21,
                    Graphics.TOP | Graphics.LEFT);

            y += tarjetaH + 5;

            dibujarTarjeta(g, margen, y, ancho, tarjetaH,
                    0x245B49);
            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("2  REPRODUCIR", margen + 9, y + 8,
                    Graphics.TOP | Graphics.LEFT);

            y += tarjetaH + 5;

            dibujarTarjeta(g, margen, y, ancho, tarjetaH,
                    0x594A25);
            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("3  PAUSAR / CONTINUAR", margen + 9,
                    y + 8, Graphics.TOP | Graphics.LEFT);

            y += tarjetaH + 5;

            dibujarTarjeta(g, margen, y, ancho, tarjetaH,
                    0x633D42);
            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("4  FINALIZAR", margen + 9, y + 8,
                    Graphics.TOP | Graphics.LEFT);

            y += tarjetaH + 5;

            dibujarTarjeta(g, margen, y, ancho, tarjetaH,
                    0x344B68);
            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("5  CAMBIAR DURACION", margen + 9,
                    y + 8, Graphics.TOP | Graphics.LEFT);

            y += tarjetaH + 7;

            g.setColor(0x9BC7E5);
            g.setFont(normal);
            g.drawString("ESTADO", margen + 2, y,
                    Graphics.TOP | Graphics.LEFT);

            y += normal.getHeight() + 3;

            int panelH = h - y - 22;
            if (panelH < 35) {
                panelH = 35;
            }

            dibujarTarjeta(g, margen, y, ancho, panelH,
                    0x102A43);

            g.setColor(0xFFFFFF);
            g.setFont(normal);

            if (segundosRestantes > 0 && grabando
                    && !pausada) {
                g.drawString("Restante: " + segundosRestantes
                        + " s", margen + 8, y + 5,
                        Graphics.TOP | Graphics.LEFT);
                dibujarTextoEnvuelto(g, estado,
                        margen + 8, y + 22,
                        ancho - 16, panelH - 25);
            } else {
                dibujarTextoEnvuelto(g, estado,
                        margen + 8, y + 5,
                        ancho - 16, panelH - 10);
            }

            g.setColor(0x9AB4C9);
            g.setFont(pequeno);
            g.drawString("7 - Volumen | 9 + Volumen",
                    margen, h - pequeno.getHeight() - 3,
                    Graphics.BOTTOM | Graphics.LEFT);
        }

        private void dibujarTarjeta(Graphics g, int x, int y,
                int w, int h, int color) {
            g.setColor(color);
            g.fillRoundRect(x, y, w, h, 8, 8);
            g.setColor(0x47718F);
            g.drawRoundRect(x, y, w, h, 8, 8);
        }

        private void dibujarTextoEnvuelto(Graphics g,
                String texto, int x, int y,
                int maxAncho, int maxAlto) {

            if (texto == null) {
                return;
            }

            int inicioY = y;
            int altoLinea = normal.getHeight() + 2;
            String resto = texto;

            while (resto.length() > 0
                    && y < inicioY + maxAlto) {

                int corte = resto.length();

                while (corte > 1
                        && normal.stringWidth(
                        resto.substring(0, corte)) > maxAncho) {
                    corte--;
                }

                if (corte < resto.length()) {
                    int espacio = resto.lastIndexOf(' ', corte);
                    if (espacio > 0) {
                        corte = espacio;
                    }
                }

                if (corte <= 0) {
                    corte = 1;
                }

                g.drawString(resto.substring(0, corte),
                        x, y, Graphics.TOP | Graphics.LEFT);

                resto = resto.substring(corte);

                while (resto.length() > 0
                        && resto.charAt(0) == ' ') {
                    resto = resto.substring(1);
                }

                y += altoLinea;
            }
        }

        protected void keyPressed(int keyCode) {
            if (keyCode == Canvas.KEY_NUM1) {
                iniciarGrabacion();
            } else if (keyCode == Canvas.KEY_NUM2) {
                reproducir();
            } else if (keyCode == Canvas.KEY_NUM3) {
                cambiarPausa();
            } else if (keyCode == Canvas.KEY_NUM4) {
                finalizarGrabacion();
            } else if (keyCode == Canvas.KEY_NUM5) {
                cambiarDuracion();
            } else if (keyCode == Canvas.KEY_NUM7) {
                cambiarVolumen(-10);
            } else if (keyCode == Canvas.KEY_NUM9) {
                cambiarVolumen(10);
            }
        }

        private void cambiarDuracion() {
            if (ocupado) {
                estado = "No se puede cambiar ahora.";
            } else {
                indiceDuracion++;
                if (indiceDuracion >= duraciones.length) {
                    indiceDuracion = 0;
                }
                estado = "Duracion seleccionada: "
                        + duraciones[indiceDuracion] + " segundos.";
            }
            repaint();
        }

        private void iniciarGrabacion() {
            if (ocupado) {
                estado = "Hay una operacion en curso.";
                repaint();
                return;
            }

            segmentos.removeAllElements();
            formatos.removeAllElements();

            finalizar = false;
            pausada = false;
            grabando = true;
            ocupado = true;
            segundosRestantes = duraciones[indiceDuracion];
            estado = "Preparando microfono...";
            diagnostico = "";
            repaint();

            Thread hilo = new Thread(new Runnable() {
                public void run() {
                    grabarHilo();
                }
            });
            hilo.start();
        }

        private void cambiarPausa() {
            if (!grabando) {
                estado = "No hay grabacion activa.";
            } else {
                pausada = !pausada;
                if (pausada) {
                    estado = "Pausando grabacion...";
                } else {
                    estado = "Continuando grabacion...";
                }
            }
            repaint();
        }

        private void finalizarGrabacion() {
            if (grabando) {
                finalizar = true;
                pausada = false;
                estado = "Finalizando grabacion...";
            } else {
                estado = "No hay grabacion activa.";
            }
            repaint();
        }

        private void grabarHilo() {
            int duracion = duraciones[indiceDuracion];
            int tiempoActivo = 0;
            String error = null;

            while (tiempoActivo < duracion && !finalizar) {

                if (pausada) {
                    estado = "Grabacion pausada. Pulsa 3 para seguir.";
                    segundosRestantes = duracion - tiempoActivo;
                    repaint();

                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }

                try {
                    estado = "Grabando segmento...";
                    segundosRestantes = duracion - tiempoActivo;
                    repaint();

                    Object[] resultado =
                            grabarSegmento(duracion - tiempoActivo);

                    byte[] datos = (byte[]) resultado[0];
                    String mime = (String) resultado[1];
                    int segundos = ((Integer) resultado[2]).intValue();

                    if (datos != null && datos.length > 0) {
                        segmentos.addElement(datos);
                        formatos.addElement(mime);
                    }

                    tiempoActivo += segundos;

                    if (segundos == 0 && !finalizar) {
                        error = "No se pudo completar un segmento.";
                        break;
                    }

                } catch (Exception e) {
                    error = e.getClass().getName()
                            + ": " + e.getMessage();
                    break;
                }
            }

            grabando = false;
            pausada = false;
            ocupado = false;
            segundosRestantes = 0;

            if (segmentos.size() > 0) {
                estado = "Grabacion lista. Segmentos: "
                        + segmentos.size();
                diagnostico = "Tiempo grabado: "
                        + tiempoActivo + " s";
            } else {
                estado = "No se obtuvo audio.";
                if (error != null) {
                    diagnostico = error;
                }
            }

            repaint();
        }

        private Object[] grabarSegmento(int maxSegundos)
                throws Exception {

            String[] locators = {
                "capture://audio",
                "capture://audio?encoding=amr"
            };

            Exception ultimoError = null;

            for (int i = 0; i < locators.length; i++) {
                try {
                    Object[] r = grabarConLocator(
                            locators[i], maxSegundos);
                    byte[] datos = (byte[]) r[0];

                    if (datos != null && datos.length > 0) {
                        return r;
                    }
                } catch (Exception e) {
                    ultimoError = e;
                }

                if (finalizar || pausada) {
                    break;
                }
            }

            if (ultimoError != null) {
                throw ultimoError;
            }

            return new Object[] {
                new byte[0], "unknown", new Integer(0)
            };
        }

        private Object[] grabarConLocator(String locator,
                int maxSegundos) throws Exception {

            Player p = null;
            RecordControl rc = null;
            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            boolean iniciada = false;
            long inicio = System.currentTimeMillis();

            try {
                p = Manager.createPlayer(locator);
                p.realize();

                rc = (RecordControl)
                        p.getControl("RecordControl");

                if (rc == null) {
                    throw new MediaException(
                            "RecordControl no disponible");
                }

                rc.setRecordStream(salida);
                rc.startRecord();
                p.start();
                iniciada = true;

                while (!finalizar && !pausada) {
                    long transcurrido =
                            (System.currentTimeMillis() - inicio)
                            / 1000;

                    if (transcurrido >= maxSegundos) {
                        break;
                    }

                    segundosRestantes =
                            maxSegundos - (int) transcurrido;
                    repaint();

                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                    }
                }

                rc.stopRecord();
                rc.commit();
                iniciada = false;

                byte[] datos = salida.toByteArray();
                String mime = detectarFormato(datos);

                int segundos = (int)
                        ((System.currentTimeMillis() - inicio)
                        / 1000);

                if (segundos < 0) {
                    segundos = 0;
                }
                if (segundos > maxSegundos) {
                    segundos = maxSegundos;
                }

                return new Object[] {
                    datos, mime, new Integer(segundos)
                };

            } finally {
                if (iniciada && rc != null) {
                    try {
                        rc.stopRecord();
                        rc.commit();
                    } catch (Exception e) {
                    }
                }

                if (p != null) {
                    try {
                        p.stop();
                    } catch (Exception e) {
                    }
                    try {
                        p.close();
                    } catch (Exception e) {
                    }
                }

                try {
                    salida.close();
                } catch (Exception e) {
                }
            }
        }

        private String detectarFormato(byte[] d) {
            if (d == null) {
                return "unknown";
            }

            if (d.length >= 12
                    && d[0] == 'R' && d[1] == 'I'
                    && d[2] == 'F' && d[3] == 'F') {
                return "audio/x-wav";
            }

            if (d.length >= 6
                    && d[0] == '#' && d[1] == '!'
                    && d[2] == 'A' && d[3] == 'M'
                    && d[4] == 'R') {
                return "audio/amr";
            }

            if (d.length >= 9
                    && d[0] == '#' && d[1] == '!'
                    && d[2] == 'A' && d[3] == 'M'
                    && d[4] == 'R' && d[5] == '-'
                    && d[6] == 'W' && d[7] == 'B') {
                return "audio/amr-wb";
            }

            return "unknown";
        }

        private void reproducir() {
            if (ocupado || grabando) {
                estado = "Espera a que termine la grabacion.";
                repaint();
                return;
            }

            if (segmentos.size() == 0) {
                estado = "Todavia no hay audio grabado.";
                repaint();
                return;
            }

            ocupado = true;
            estado = "Preparando reproduccion...";
            repaint();

            Thread hilo = new Thread(new Runnable() {
                public void run() {
                    reproducirHilo();
                }
            });
            hilo.start();
        }

        private void reproducirHilo() {
            int reproducidos = 0;
            String errores = "";

            for (int i = 0; i < segmentos.size(); i++) {
                byte[] datos = (byte[]) segmentos.elementAt(i);
                String mime = (String) formatos.elementAt(i);

                try {
                    estado = "Reproduciendo parte "
                            + (i + 1) + " de " + segmentos.size();
                    repaint();

                    reproducirSegmento(datos, mime);
                    reproducidos++;

                } catch (Exception e) {
                    errores = e.getClass().getName()
                            + ": " + e.getMessage();
                    break;
                }
            }

            if (reproducidos == segmentos.size()) {
                estado = "Reproduccion finalizada.";
                diagnostico = "Partes reproducidas: "
                        + reproducidos;
            } else {
                estado = "No se pudo reproducir todo el audio.";
                diagnostico = errores;
            }

            ocupado = false;
            repaint();
        }

        private void reproducirSegmento(byte[] datos,
                String mime) throws Exception {

            String[] tipos;

            if ("audio/x-wav".equals(mime)) {
                tipos = new String[] {
                    "audio/x-wav", "audio/wav"
                };
            } else if ("audio/amr".equals(mime)) {
                tipos = new String[] {
                    "audio/amr"
                };
            } else if ("audio/amr-wb".equals(mime)) {
                tipos = new String[] {
                    "audio/amr-wb", "audio/amr"
                };
            } else {
                tipos = new String[] {
                    "audio/amr", "audio/x-wav",
                    "audio/wav", "audio/amr-wb"
                };
            }

            Exception ultimoError = null;

            for (int i = 0; i < tipos.length; i++) {
                Player p = null;

                try {
                    ByteArrayInputStream entrada =
                            new ByteArrayInputStream(datos);

                    p = Manager.createPlayer(entrada, tipos[i]);
                    p.realize();

                    Control c = p.getControl("VolumeControl");
                    if (c != null) {
                        controlVolumen = (VolumeControl) c;
                        controlVolumen.setLevel(volumen);
                    } else {
                        controlVolumen = null;
                    }

                    reproductor = p;
                    p.start();

                    long inicio = System.currentTimeMillis();
                    long limite = inicio + 70000;

                    while (p.getState() == Player.STARTED
                            && System.currentTimeMillis() < limite) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                        }
                    }

                    reproductor = null;
                    controlVolumen = null;
                    p.close();
                    return;

                } catch (Exception e) {
                    ultimoError = e;
                    reproductor = null;
                    controlVolumen = null;

                    if (p != null) {
                        try {
                            p.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }

            if (ultimoError != null) {
                throw ultimoError;
            }

            throw new MediaException(
                    "Formato de audio no compatible");
        }

        private void cambiarVolumen(int cambio) {
            volumen += cambio;

            if (volumen > 100) {
                volumen = 100;
            }
            if (volumen < 0) {
                volumen = 0;
            }

            if (controlVolumen != null) {
                try {
                    controlVolumen.setLevel(volumen);
                    estado = "Volumen: " + volumen + "%";
                } catch (Exception e) {
                    estado = "El telefono no permite cambiar volumen.";
                }
            } else {
                estado = "Volumen solicitado: " + volumen
                        + "%. Control no disponible.";
            }

            repaint();
        }

        void detenerTodo() {
            finalizar = true;
            pausada = false;

            if (reproductor != null) {
                try {
                    reproductor.stop();
                } catch (Exception e) {
                }
                try {
                    reproductor.close();
                } catch (Exception e) {
                }
            }
        }
    }
}