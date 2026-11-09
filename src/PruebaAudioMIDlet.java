import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.*;

public class PruebaAudioMIDlet extends MIDlet implements CommandListener {

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
            destroyApp(true);
            notifyDestroyed();
        }
    }

    class AudioCanvas extends Canvas {

        private byte[] audioData;
        private String mimeCapturado;
        private String estado = "Listo para grabar";
        private int segundosRestantes = 0;
        private boolean ocupado = false;

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
            int ancho = w - (margen * 2);
            int tarjetaY = 39;
            int tarjetaH = 42;

            dibujarTarjeta(g, margen, tarjetaY,
                    ancho, tarjetaH, 0x145078);

            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("1  GRABAR", margen + 10,
                    tarjetaY + 5, Graphics.TOP | Graphics.LEFT);

            g.setFont(pequeno);
            g.setColor(0xD5E8F5);
            g.drawString("Captura de audio durante 5 segundos",
                    margen + 10, tarjetaY + 25,
                    Graphics.TOP | Graphics.LEFT);

            tarjetaY += tarjetaH + 7;

            dibujarTarjeta(g, margen, tarjetaY,
                    ancho, tarjetaH, 0x245B49);

            g.setColor(0xFFFFFF);
            g.setFont(titulo);
            g.drawString("2  REPRODUCIR", margen + 10,
                    tarjetaY + 5, Graphics.TOP | Graphics.LEFT);

            g.setFont(pequeno);
            g.setColor(0xD8F1E5);
            g.drawString("Escucha la ultima grabacion",
                    margen + 10, tarjetaY + 25,
                    Graphics.TOP | Graphics.LEFT);

            tarjetaY += tarjetaH + 10;

            g.setColor(0x9BC7E5);
            g.setFont(normal);
            g.drawString("ESTADO", margen + 2, tarjetaY,
                    Graphics.TOP | Graphics.LEFT);

            tarjetaY += normal.getHeight() + 3;

            int panelH = h - tarjetaY - 18;
            if (panelH < 38) {
                panelH = 38;
            }

            dibujarTarjeta(g, margen, tarjetaY,
                    ancho, panelH, 0x102A43);

            g.setColor(0xFFFFFF);
            g.setFont(normal);

            if (segundosRestantes > 0) {
                g.drawString("Grabando: " + segundosRestantes + " s",
                        margen + 9, tarjetaY + 7,
                        Graphics.TOP | Graphics.LEFT);
            } else {
                dibujarTextoEnvuelto(g, estado,
                        margen + 9, tarjetaY + 7,
                        ancho - 18, panelH - 14);
            }

            g.setColor(0x9AB4C9);
            g.setFont(pequeno);
            g.drawString("Teclas 1 y 2  |  Opcion Salir",
                    margen, h - pequeno.getHeight() - 3,
                    Graphics.BOTTOM | Graphics.LEFT);
        }

        private void dibujarTarjeta(Graphics g,
                int x, int y, int w, int h, int color) {

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

            while (resto.length() > 0 &&
                    y < inicioY + maxAlto) {

                int corte = resto.length();

                while (corte > 1 &&
                        normal.stringWidth(
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

                while (resto.length() > 0 &&
                        resto.charAt(0) == ' ') {
                    resto = resto.substring(1);
                }

                y += altoLinea;
            }
        }

        protected void keyPressed(int keyCode) {
            if (ocupado) {
                return;
            }

            if (keyCode == Canvas.KEY_NUM1) {
                grabar();
            } else if (keyCode == Canvas.KEY_NUM2) {
                reproducir();
            }
        }

        private void grabar() {
            if (ocupado) {
                return;
            }

            ocupado = true;
            audioData = null;
            mimeCapturado = null;
            estado = "Preparando grabacion...";
            segundosRestantes = 5;
            repaint();

            Thread hilo = new Thread(new Runnable() {
                public void run() {
                    grabarHilo();
                }
            });

            hilo.start();
        }

        private void grabarHilo() {
            String error1 = null;
            String error2 = null;
            byte[] datos = null;
            String mime = null;

            String[] locators = new String[] {
                "capture://audio",
                "capture://audio?encoding=amr"
            };

            for (int intento = 0;
                    intento < locators.length; intento++) {

                try {
                    estado = "Intento " + (intento + 1) + " de 2...";
                    segundosRestantes = 5;
                    repaint();

                    datos = grabarConLocator(locators[intento]);

                    if (datos != null && datos.length > 0) {

                        if (datos.length >= 4 &&
                                datos[0] == 'R' &&
                                datos[1] == 'I' &&
                                datos[2] == 'F' &&
                                datos[3] == 'F') {

                            mime = "audio/x-wav";

                        } else {
                            mime = "audio/amr";
                        }

                        break;
                    }

                } catch (Exception e) {

                    if (intento == 0) {
                        error1 = e.getClass().getName()
                                + ": " + e.getMessage();
                    } else {
                        error2 = e.getClass().getName()
                                + ": " + e.getMessage();
                    }
                }
            }

            if (datos != null && datos.length > 0) {

                audioData = datos;
                mimeCapturado = mime;

                estado = "Grabacion completada: "
                        + datos.length + " bytes (" + mime + ")";

            } else {

                String detalle = "";

                if (error1 != null) {
                    detalle = " Intento 1: " + error1;
                }

                if (error2 != null) {
                    detalle += " Intento 2: " + error2;
                }

                estado = "No se recibieron datos de audio."
                        + detalle;
            }

            segundosRestantes = 0;
            ocupado = false;
            repaint();
        }

        private byte[] grabarConLocator(String locator)
                throws Exception {

            Player p = null;
            RecordControl rc = null;
            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            boolean iniciada = false;

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

                for (int i = 5; i > 0; i--) {

                    segundosRestantes = i;
                    repaint();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }
                }

                rc.stopRecord();
                rc.commit();

                iniciada = false;

                return salida.toByteArray();

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

        private void reproducir() {

            if (ocupado) {
                return;
            }

            if (audioData == null || audioData.length == 0) {
                estado = "Todavia no hay audio grabado.";
                repaint();
                return;
            }

            ocupado = true;
            estado = "Reproduciendo...";
            repaint();

            Thread hilo = new Thread(new Runnable() {
                public void run() {
                    reproducirHilo();
                }
            });

            hilo.start();
        }

        private void reproducirHilo() {

            Player p = null;

            try {

                ByteArrayInputStream entrada =
                        new ByteArrayInputStream(audioData);

                String mime = mimeCapturado;

                if (mime == null) {
                    mime = "audio/amr";
                }

                p = Manager.createPlayer(entrada, mime);
                p.realize();
                p.start();

                long inicio = System.currentTimeMillis();

                while (p.getState() == Player.STARTED &&
                        (System.currentTimeMillis() - inicio)
                        < 10000) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }

                estado = "Reproduccion finalizada.";

            } catch (Exception e) {

                estado = "Error al reproducir: "
                        + e.getClass().getName();

            } finally {

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

                ocupado = false;
                repaint();
            }
        }
    }
}
