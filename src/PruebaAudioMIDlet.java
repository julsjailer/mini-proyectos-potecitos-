import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.*;
import java.util.*;

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

    public void pauseApp() {}
    public void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == salirCmd) {
            destroyApp(true);
            notifyDestroyed();
        }
    }

    // ---------------------------------------------------------------
    // Canvas principal
    // ---------------------------------------------------------------
    class AudioCanvas extends Canvas {
        private String linea1 = "1 Grabar";
        private String linea2 = "2 Reproducir";
        private String estado = "";
        private byte[] audioData = null;
        private String mimeCapturado = null; // "audio/x-wav" o "audio/amr"

        private Font fuente;

        AudioCanvas() {
            fuente = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            g.setColor(0x000000);
            g.fillRect(0, 0, w, h);
            g.setColor(0xFFFFFF);

            int y = 2;
            int salto = fuente.getHeight() + 2;

            g.setFont(fuente);
            g.drawString(linea1, 2, y, Graphics.TOP | Graphics.LEFT);
            y += salto;
            g.drawString(linea2, 2, y, Graphics.TOP | Graphics.LEFT);
            y += salto * 2;

            // Estado en varias líneas si es largo
            if (estado != null && estado.length() > 0) {
                int maxAncho = w - 4;
                String resto = estado;
                while (resto.length() > 0 && y < h - salto) {
                    int corte = resto.length();
                    while (corte > 0 && fuente.stringWidth(resto.substring(0, corte)) > maxAncho) {
                        corte--;
                    }
                    if (corte == 0) corte = 1;
                    g.drawString(resto.substring(0, corte), 2, y, Graphics.TOP | Graphics.LEFT);
                    resto = resto.substring(corte);
                    y += salto;
                }
            }
        }

        protected void keyPressed(int keyCode) {
            if (keyCode == Canvas.KEY_NUM1) {
                grabar();
            } else if (keyCode == Canvas.KEY_NUM2) {
                reproducir();
            }
        }

        // -----------------------------------------------------------
        // GRABACIÓN (5 s con TimerTask de corte)
        // -----------------------------------------------------------
        private void grabar() {
            estado = "Grabando...";
            audioData = null;
            mimeCapturado = null;
            repaint();

            new Thread(new Runnable() {
                public void run() {
                    grabarHilo();
                }
            }).start();
        }

        private void grabarHilo() {
            Player p = null;
            RecordControl rc = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Timer timer = new Timer();
            final boolean[] cancelado = new boolean[]{false};

            try {
                // Intento 1: capture://audio genérico
                try {
                    p = Manager.createPlayer("capture://audio");
                    p.realize();
                } catch (Exception e1) {
                    // Intento 2: AMR explícito
                    if (p != null) { try { p.close(); } catch (Exception ex) {} }
                    p = Manager.createPlayer("capture://audio?encoding=amr");
                    p.realize();
                }

                rc = (RecordControl) p.getControl("RecordControl");
                if (rc == null) {
                    throw new MediaException("RecordControl no disponible");
                }

                rc.setRecordStream(baos);
                rc.startRecord();
                p.start();

                // Corte a los 5 s
                timer.schedule(new TimerTask() {
                    public void run() {
                        cancelado[0] = true;
                    }
                }, 5000);

                while (!cancelado[0]) {
                    try { Thread.sleep(100); } catch (InterruptedException ie) {}
                }

            } catch (Exception e) {
                final String msg = "Error grab: " + e.getClass().getName() + " - " + e.getMessage();
                estado = msg;
                repaint();
                return;
            } finally {
                timer.cancel();
                if (rc != null) {
                    try { rc.stopRecord(); } catch (Exception e) {}
                }
                if (p != null) {
                    try { p.stop(); } catch (Exception e) {}
                    try { p.close(); } catch (Exception e) {}
                }
            }

            // Determinar formato probable según el locator que funcionó
            // (si createPlayer no lanzó excepción con el genérico, asumimos WAV/PCM;
            //  si cayó al AMR, asumimos AMR).
            // Para no complicar: guardamos el MIME que usaremos al reproducir.
            // Una heurística: si los primeros bytes son 'RIFF' es WAV.
            byte[] datos = baos.toByteArray();
            if (datos.length >= 4 && datos[0] == 'R' && datos[1] == 'I' && datos[2] == 'F' && datos[3] == 'F') {
                mimeCapturado = "audio/x-wav";
            } else {
                // Si no es RIFF, lo más seguro es que el firmware haya entregado AMR
                mimeCapturado = "audio/amr";
            }

            audioData = datos;

            final String msgFinal = "Grabado: " + datos.length + " bytes (" + mimeCapturado + ")";
            estado = msgFinal;
            repaint();
        }

        // -----------------------------------------------------------
        // REPRODUCCIÓN
        // -----------------------------------------------------------
        private void reproducir() {
            if (audioData == null || audioData.length == 0) {
                estado = "No hay audio grabado";
                repaint();
                return;
            }
            estado = "Reproduciendo...";
            repaint();

            new Thread(new Runnable() {
                public void run() {
                    reproducirHilo();
                }
            }).start();
        }

        private void reproducirHilo() {
            Player p = null;
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                // Usamos el MIME detectado en la grabación.
                // Si por alguna razón mimeCapturado es null, intentamos WAV como pide el enunciado.
                String mime = (mimeCapturado != null) ? mimeCapturado : "audio/x-wav";
                p = Manager.createPlayer(bais, mime);
                p.realize();
                p.start();
                // Esperar a que termine (máximo 10 s por seguridad)
                long inicio = System.currentTimeMillis();
                while (p.getState() == Player.STARTED && (System.currentTimeMillis() - inicio) < 10000) {
                    try { Thread.sleep(100); } catch (InterruptedException ie) {}
                }
                estado = "Reproducido";
            } catch (Exception e) {
                estado = "Error reprod: " + e.getClass().getName() + " - " + e.getMessage();
            } finally {
                if (p != null) {
                    try { p.stop(); } catch (Exception e) {}
                    try { p.close(); } catch (Exception e) {}
                }
                repaint();
            }
        }
    }
}
