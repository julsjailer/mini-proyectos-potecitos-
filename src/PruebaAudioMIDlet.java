import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.*;

public class PruebaAudioMIDlet extends MIDlet implements CommandListener {
    private AudioCanvas canvas;
    private final Command salir = new Command("Salir", Command.EXIT, 1);

    public void startApp() {
        if (canvas == null) {
            canvas = new AudioCanvas();
            canvas.addCommand(salir);
            canvas.setCommandListener(this);
        }
        Display.getDisplay(this).setCurrent(canvas);
    }
    public void pauseApp() { if (canvas != null) canvas.detener(); }
    public void destroyApp(boolean unconditional) {
        if (canvas != null) canvas.detener();
    }
    public void commandAction(Command c, Displayable d) {
        if (c == salir) { destroyApp(true); notifyDestroyed(); }
    }

    class AudioCanvas extends Canvas implements Runnable {
        private final Object lock = new Object();
        private volatile boolean parar;
        private boolean ocupado;
        private boolean grabando;
        private int segundos = 5;
        private byte[] audio;
        private String mime;
        private String estado = "Listo";
        private final Font fuente = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

        protected void paint(Graphics g) {
            String texto;
            int duracion;
            synchronized (lock) { texto = estado; duracion = segundos; }
            int w = getWidth();
            int salto = fuente.getHeight() + 3;
            g.setColor(0x101820);
            g.fillRect(0, 0, w, getHeight());
            g.setFont(fuente);
            g.setColor(0x58D6C7);
            g.drawString("POTECITOS AUDIO", 3, 3, Graphics.TOP | Graphics.LEFT);
            g.setColor(0xFFFFFF);
            String[] lineas = {"1 Grabar | 2 Escuchar", "3 Detener",
                "4 Duracion: " + duracion + " s", "Solo memoria; no se guarda al salir"};
            int y = salto + 3;
            for (int i = 0; i < lineas.length; i++) {
                y = dibujar(g, lineas[i], y, salto, w);
            }
            dibujar(g, texto, y + salto, salto, w);
        }
        private int dibujar(Graphics g, String texto, int y, int salto, int w) {
            while (texto.length() > 0 && y < getHeight()) {
                int n = texto.length();
                while (n > 1 && fuente.stringWidth(texto.substring(0, n)) > w - 6) n--;
                g.drawString(texto.substring(0, n), 3, y, Graphics.TOP | Graphics.LEFT);
                texto = texto.substring(n);
                y += salto;
            }
            return y;
        }
        private void mensaje(String texto) {
            synchronized (lock) { estado = texto; }
            repaint();
        }
        void detener() { parar = true; }
        protected void keyPressed(int key) {
            if (key == KEY_NUM3) { detener(); return; }
            synchronized (lock) {
                if (ocupado) return;
                if (key == KEY_NUM4) {
                    segundos = segundos == 5 ? 10 : segundos == 10 ? 30 : 5;
                    repaint();
                    return;
                }
                if (key != KEY_NUM1 && key != KEY_NUM2) return;
                if (key == KEY_NUM2 && audio == null) {
                    estado = "Primero graba un audio";
                    repaint();
                    return;
                }
                grabando = key == KEY_NUM1;
                ocupado = true;
                parar = false;
            }
            try { new Thread(this).start(); }
            catch (RuntimeException e) {
                synchronized (lock) { ocupado = false; }
                mensaje("No se pudo iniciar: " + e.toString());
            }
        }
        public void run() {
            try {
                if (grabando) grabar(); else reproducir();
            } catch (SecurityException e) {
                mensaje("Permiso de audio denegado");
            } catch (Exception e) {
                mensaje("Error: " + e.toString());
            } finally {
                synchronized (lock) { ocupado = false; }
                repaint();
            }
        }
        private void cerrar(Player p) {
            if (p == null) return;
            try { p.stop(); } catch (Exception e) {}
            try { p.close(); } catch (Exception e) {}
        }
        private void grabar() throws Exception {
            Player p = null;
            RecordControl rc = null;
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            try {
                mensaje("Preparando microfono...");
                try {
                    p = Manager.createPlayer("capture://audio");
                    p.realize();
                } catch (SecurityException e) { throw e; }
                catch (Exception e) {
                    cerrar(p);
                    p = Manager.createPlayer("capture://audio?encoding=amr");
                    p.realize();
                }
                rc = (RecordControl) p.getControl("RecordControl");
                if (rc == null) throw new MediaException("Grabacion no disponible");
                if (parar) { mensaje("Cancelado; audio anterior conservado"); return; }
                rc.setRecordStream(salida);
                rc.startRecord();
                p.start();
                long inicio = System.currentTimeMillis();
                int limite;
                synchronized (lock) { limite = segundos; }
                int anterior = -1;
                while (!parar) {
                    int transcurrido = (int) ((System.currentTimeMillis() - inicio) / 1000);
                    if (transcurrido >= limite) break;
                    if (transcurrido != anterior) {
                        mensaje("Grabando " + transcurrido + "/" + limite + " s | 3 Detener");
                        anterior = transcurrido;
                    }
                    Thread.sleep(100);
                }
                rc.stopRecord();
                rc.commit();
                byte[] datos = salida.toByteArray();
                if (datos.length == 0) throw new IOException("Grabacion vacia; audio anterior conservado");
                String tipo = detectar(datos, p.getContentType());
                synchronized (lock) { audio = datos; mime = tipo; }
                mensaje("Grabado: " + datos.length + " bytes | " + tipo);
            } finally {
                if (rc != null) { try { rc.stopRecord(); } catch (Exception e) {} }
                cerrar(p);
                salida.close();
            }
        }
        private boolean empieza(byte[] datos, String firma, int offset) {
            if (datos.length < offset + firma.length()) return false;
            for (int i = 0; i < firma.length(); i++)
                if ((datos[offset + i] & 255) != firma.charAt(i)) return false;
            return true;
        }
        private String detectar(byte[] datos, String declarado) throws MediaException {
            if (empieza(datos, "RIFF", 0) && empieza(datos, "WAVE", 8)) return "audio/x-wav";
            if (empieza(datos, "#!AMR-WB\n", 0)) return "audio/amr-wb";
            if (empieza(datos, "#!AMR\n", 0)) return "audio/amr";
            if (declarado != null && declarado.startsWith("audio/")) return declarado;
            throw new MediaException("Formato desconocido; audio anterior conservado");
        }
        private void reproducir() throws Exception {
            Player p = null;
            try {
                byte[] datos;
                String tipo;
                synchronized (lock) { datos = audio; tipo = mime; }
                mensaje("Preparando reproduccion...");
                p = Manager.createPlayer(new ByteArrayInputStream(datos), tipo);
                p.realize();
                if (parar) { mensaje("Reproduccion cancelada"); return; }
                p.start();
                mensaje("Reproduciendo | 3 Detener");
                long inicio = System.currentTimeMillis();
                while (!parar && p.getState() == Player.STARTED
                        && System.currentTimeMillis() - inicio < 45000) Thread.sleep(100);
                mensaje(parar ? "Reproduccion detenida" :
                    p.getState() == Player.STARTED ? "Limite de reproduccion alcanzado" : "Reproduccion finalizada");
            } finally { cerrar(p); }
        }
    }
}
