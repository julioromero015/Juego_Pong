/*
Aplicación del juego pong clásico, implementado en Java con uso de 
librerías JFrame
@author: Julio Cesar Gonzalez Romero
Version 1.0
*/
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class JuegoPong extends JPanel implements Runnable, KeyListener {

    // Dimensiones de la pantalla de juego
    private static final int ANCHO = 800;
    private static final int ALTO = 500;

    // Propiedades de la Pelota
    private int pelotaX = ANCHO / 2;
    private int pelotaY = ALTO / 2;
    private int pelotaDiametro = 20;
    private int velocidadPelotaX = 4;
    private int velocidadPelotaY = 4;

    // Propiedades de las Paletas (Jugadores)
    private final int PALETA_ANCHO = 15;
    private final int PALETA_ALTO = 80;
    
    private int jugador1Y = ALTO / 2 - PALETA_ALTO / 2; // Izquierda
    private int jugador2Y = ALTO / 2 - PALETA_ALTO / 2; // Derecha
    private final int VELOCIDAD_PALETA = 7;

    // Puntuaciones
    private int puntajeJugador1 = 0;
    private int puntajeJugador2 = 0;

    // Control de teclas presionadas
    private boolean j1Arriba = false, j1Abajo = false;
    private boolean j2Arriba = false, j2Abajo = false;

    // Hilo para el Game Loop
    private Thread hiloJuego;
    private boolean enEjecucion = true;

    // Constructor: Configura la ventana y los controles
    public JuegoPong() {
        this.setPreferredSize(new Dimension(ANCHO, ALTO));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        // Iniciamos el hilo del juego (el bucle en segundo plano)
        hiloJuego = new Thread(this);
        hiloJuego.start();
    }

    // EL CORAZÓN DEL JUEGO: El Game Loop (Bucle de juego)
    @Override
    public void run() {
        while (enEjecucion) {
            actualizarLogica(); // 1. Mueve las cosas en la memoria
            repaint();          // 2. Borra la pantalla y vuelve a dibujar todo (llama a paintComponent)

            try {
                // Pausa de ~16 milisegundos para correr a 60 fotogramas por segundo (FPS)
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 1. ACTUALIZAR LÓGICA (Movimientos y Colisiones)
    private void actualizarLogica() {
        // Movimiento de la pelota
        pelotaX += velocidadPelotaX;
        pelotaY += velocidadPelotaY;

        // Colisión con techo y suelo (rebote)
        if (pelotaY <= 0 || pelotaY >= ALTO - pelotaDiametro) {
            velocidadPelotaY = -velocidadPelotaY;
        }

        // Movimiento del Jugador 1 (Teclas W y S)
        if (j1Arriba && jugador1Y > 0) jugador1Y -= VELOCIDAD_PALETA;
        if (j1Abajo && jugador1Y < ALTO - PALETA_ALTO) jugador1Y += VELOCIDAD_PALETA;

        // Movimiento del Jugador 2 (Flechas Arriba y Abajo)
        if (j2Arriba && jugador2Y > 0) jugador2Y -= VELOCIDAD_PALETA;
        if (j2Abajo && jugador2Y < ALTO - PALETA_ALTO) jugador2Y += VELOCIDAD_PALETA;

        // COLISIÓN: ¿La pelota toca la paleta del Jugador 1?
        if (pelotaX <= 30 && pelotaX >= 15) { // Rango X de la paleta izquierda
            if (pelotaY + pelotaDiametro >= jugador1Y && pelotaY <= jugador1Y + PALETA_ALTO) {
                velocidadPelotaX = -velocidadPelotaX;
                pelotaX = 31; // Evita que la pelota se quede "atrapada" dentro de la paleta
            }
        }

        // COLISIÓN: ¿La pelota toca la paleta del Jugador 2?
        if (pelotaX >= ANCHO - 30 - pelotaDiametro && pelotaX <= ANCHO - 15 - pelotaDiametro) { // Rango X de la paleta derecha
            if (pelotaY + pelotaDiametro >= jugador2Y && pelotaY <= jugador2Y + PALETA_ALTO) {
                velocidadPelotaX = -velocidadPelotaX;
                pelotaX = ANCHO - 31 - pelotaDiametro;
            }
        }

        // ANOTACIÓN DE PUNTOS
        if (pelotaX < 0) { // Punto para Jugador 2 (Derecho)
            puntajeJugador2++;
            reiniciarPelota();
        } else if (pelotaX > ANCHO) { // Punto para Jugador 1 (Izquierdo)
            puntajeJugador1++;
            reiniciarPelota();
        }
    }

    // Reinicia la pelota en el centro al anotar un punto
    private void reiniciarPelota() {
        pelotaX = ANCHO / 2;
        pelotaY = ALTO / 2;
        velocidadPelotaX = -velocidadPelotaX; // Cambia de dirección para el siguiente saque
    }

    // 2. RENDERIZADO: Dibuja los gráficos en la ventana
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dibujar línea del centro (Red)
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < ALTO; i += 30) {
            g.fillRect(ANCHO / 2 - 2, i, 4, 15);
        }

        // Dibujar Pelota (Blanca)
        g.setColor(Color.WHITE);
        g.fillOval(pelotaX, pelotaY, pelotaDiametro, pelotaDiametro);

        // Dibujar Jugador 1 (Izquierda - Azul)
        g.setColor(Color.CYAN);
        g.fillRect(15, jugador1Y, PALETA_ANCHO, PALETA_ALTO);

        // Dibujar Jugador 2 (Derecha - Roja)
        g.setColor(Color.RED);
        g.fillRect(ANCHO - 15 - PALETA_ANCHO, jugador2Y, PALETA_ANCHO, PALETA_ALTO);

        // Dibujar Marcador de Puntos
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(30.0f)); // Tamaño de letra grande
        g.drawString(String.valueOf(puntajeJugador1), ANCHO / 4, 50);
        g.drawString(String.valueOf(puntajeJugador2), (ANCHO / 4) * 3, 50);
    }

    // --- CAPTURA DEL TECLADO (KeyListener) ---
    @Override
    public void keyPressed(KeyEvent e) {
        // Al presionar la tecla, activamos el movimiento
        if (e.getKeyCode() == KeyEvent.VK_W) j1Arriba = true;
        if (e.getKeyCode() == KeyEvent.VK_S) j1Abajo = true;
        
        if (e.getKeyCode() == KeyEvent.VK_UP) j2Arriba = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) j2Abajo = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Al soltar la tecla, detenemos el movimiento
        if (e.getKeyCode() == KeyEvent.VK_W) j1Arriba = false;
        if (e.getKeyCode() == KeyEvent.VK_S) j1Abajo = false;
        
        if (e.getKeyCode() == KeyEvent.VK_UP) j2Arriba = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) j2Abajo = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {} // No lo necesitamos para este juego

    // MÉTODO MAIN: Crea la ventana contenedora de Windows/Mac
    public static void main(String[] args) {
        JFrame ventana = new JFrame("Juego de Ping Pong - Primera version");
        JuegoPong panelJuego = new JuegoPong();

        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.add(panelJuego);
        ventana.pack(); // Ajusta la ventana al tamaño del panel (800x500)
        ventana.setLocationRelativeTo(null); // Centra la ventana en la pantalla
        ventana.setResizable(false); // Evita que se cambie el tamaño del juego
        ventana.setVisible(true); // Muestra la ventana
    }
}