package ud.prog3.pr01;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

/** Ventana principal de reproductor de vídeo
 * Utiliza la librería VLCj que debe estar instalada y configurada
 *     (http://www.capricasoftware.co.uk/projects/vlcj/index.html)
 * @author Andoni Eguíluz Morán
 * Facultad de Ingeniería - Universidad de Deusto
 */
public class VideoPlayer extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// Varible de ventana principal de la clase
	private static VideoPlayer miVentana;

	// Atributo de VLCj
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	// Atributos manipulables de swing
	private JList<String> lCanciones = null;  // Lista vertical de vídeos del player
	private JProgressBar pbVideo = null;      // Barra de progreso del vídeo en curso
	private JCheckBox cbAleatorio = null;     // Checkbox de reproducción aleatoria
	private JLabel lMensaje = null;           // Label para mensaje de reproducción
	// Datos asociados a la ventana
	private ListaDeReproduccion listaRepVideos;  // Modelo para la lista de vídeos
	private DateFormat formato = DateFormat.getDateInstance(3, Locale.getDefault());

	public VideoPlayer() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		// Creación de datos asociados a la ventana (lista de reproducción)
		listaRepVideos = new ListaDeReproduccion();
		
		// Creación de componentes/contenedores de swing
		lCanciones = new JList<String>( listaRepVideos );
		pbVideo = new JProgressBar( 0, 10000 );
		cbAleatorio = new JCheckBox("Rep. aleatoria");
		lMensaje = new JLabel( "" );
		JPanel pBotonera = new JPanel();
		JButton bAnyadir = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Add.png")) );
		JButton bAtras = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Rewind.png")) );
		JButton bPausaPlay = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Play Pause.png")) );
		JButton bAdelante = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Fast Forward.png")) );
		JButton bMaximizar = new JButton( new ImageIcon( VideoPlayer.class.getResource("img/Button Maximize.png")) );
		
		// Componente de VCLj
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

		// Configuración de componentes/contenedores
        setTitle("Video Player - Deusto Ingeniería");
		setSize( 800, 600 );
		setLocationRelativeTo( null );  // Centra la ventana en la pantalla
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		lCanciones.setPreferredSize( new Dimension( 200,  500 ) );
		pBotonera.setLayout( new FlowLayout( FlowLayout.LEFT ));
		
		// Enlace de componentes y contenedores
		pBotonera.add( bAnyadir );
		pBotonera.add( bAtras );
		pBotonera.add( bPausaPlay );
		pBotonera.add( bAdelante );
		pBotonera.add( bMaximizar );
		pBotonera.add( cbAleatorio );
		pBotonera.add( lMensaje );
		getContentPane().add( mediaPlayerComponent, BorderLayout.CENTER );
		getContentPane().add( pBotonera, BorderLayout.NORTH );
		getContentPane().add( pbVideo, BorderLayout.SOUTH );
		getContentPane().add( new JScrollPane( lCanciones ), BorderLayout.WEST );
		
		// Escuchadores
		bAnyadir.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File fPath = pedirCarpeta();
				if (fPath==null) return;
				path = fPath.getAbsolutePath();
				// TODO: pedir ficheros por ventana de entrada (JOptionPane)
				// ficheros = ...
				listaRepVideos.add( path, ficheros );
				lCanciones.repaint();
			}
		});
		bAtras.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paraVideo();
				if(cbAleatorio.isSelected()) {
					listaRepVideos.irARandom();
				}else {
					listaRepVideos.irAAnterior();
				}
				lanzaVideo();
			}
		});
		bAdelante.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paraVideo();
				if(cbAleatorio.isSelected()) {
					listaRepVideos.irARandom();
				}else {
					listaRepVideos.irASiguiente();
				}
				lanzaVideo();
			}
		});
		bPausaPlay.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaPlayerComponent.mediaPlayer().status().isPlayable()) {
					if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
						// TODO: hacer pausa
						mediaPlayerComponent.mediaPlayer().controls().pause();
					} else {
						// TODO: hacer play
						mediaPlayerComponent.mediaPlayer().controls().play();
					}
				} else {
					lanzaVideo();
				}
			}
		});
		bMaximizar.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaPlayerComponent.mediaPlayer().fullScreen().isFullScreen())
			        mediaPlayerComponent.mediaPlayer().fullScreen().set(false);
				else
					mediaPlayerComponent.mediaPlayer().fullScreen().set(true);
			}
		});
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mediaPlayerComponent.mediaPlayer().controls().stop();
				mediaPlayerComponent.mediaPlayer().release();
			}
		});
		mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener( 
			new MediaPlayerEventAdapter() {
				@Override
				public void finished(MediaPlayer mediaPlayer) {
					listaRepVideos.irASiguiente();
					lanzaVideo();
				}
				@Override
				public void error(MediaPlayer mediaPlayer) {
					listaRepVideos.irASiguiente();
					lanzaVideo();
					lCanciones.repaint();
				}
			    @Override
			    public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
					pbVideo.setValue( (int) ( 10000.0 * 
							mediaPlayerComponent.mediaPlayer().status().time() /
							mediaPlayerComponent.mediaPlayer().status().length() ) );
					pbVideo.repaint();
			    }
		});
	}

	//
	// Métodos sobre el player de vídeo
	//
	
	// Para la reproducción del vídeo en curso
	private void paraVideo() {
		if (mediaPlayerComponent.mediaPlayer()!=null)
			mediaPlayerComponent.mediaPlayer().controls().stop();
	}
	
	
	// Empieza a reproducir el vídeo en curso de la lista de reproducción
	private void lanzaVideo() {
		if (mediaPlayerComponent.mediaPlayer()!=null &&
			listaRepVideos.getFicSeleccionado()!=-1) {
			File ficVideo = listaRepVideos.getFic(listaRepVideos.getFicSeleccionado());
			mediaPlayerComponent.mediaPlayer().media().play(  
				ficVideo.getAbsolutePath() );
			Date fecha = new Date(ficVideo.lastModified());
			lMensaje.setText("Fecha: " + formato.format(fecha));
			lMensaje.repaint();
			lCanciones.setSelectedIndex( listaRepVideos.getFicSeleccionado() );
		} else {
			lCanciones.setSelectedIndices( new int[] {} );
		}
	}
	
	// Pide interactivamente una carpeta para coger vídeos
	// (null si no se selecciona)
	private static File pedirCarpeta() {
		JFileChooser chooser = new JFileChooser(new File("test/"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int ret = chooser.showOpenDialog(null);
		if(ret == 0) {
			return chooser.getSelectedFile();
		}else {
			return null;
		}
	}

		private static String ficheros;
		private static String path;
	/** Ejecuta una ventana de VideoPlayer.
	 * El path de VLC debe estar en la variable de entorno "vlc".
	 * Comprobar que la versión de 32/64 bits de Java y de VLC es compatible.
	 * @param args	Un array de dos strings. El primero es el nombre (con comodines) de los ficheros,
	 * 				el segundo el path donde encontrarlos.  Si no se suministran, se piden de forma interactiva. 
	 */
	public static void main(String[] args) {
		if(args == null || args.length == 0) {
			args = new String[] { "*Pentatonix*.mp4", "test/res/" };
		}
		args = new String[] {};
		if (args.length < 2) {
			File fPath = pedirCarpeta();
			if (fPath==null) return;
			path = fPath.getAbsolutePath();
			String msg = JOptionPane.showInputDialog(null, "Escoge ficheros (entre *)");
			ficheros = msg;
		} else {
			ficheros = args[0];
			path = args[1];
		}
		
		// Inicializar VLC.
		// Probar con el buscador nativo...
		boolean found = new NativeDiscovery().discover();
    	//System.out.println( LibVlc.libvlc_get_version() );  // Visualiza versión de VLC encontrada
    	// Si no se encuentra probar otras opciones:
    	if (!found) {
			// Buscar vlc como variable de entorno
    		String vlcPath = System.getenv().get( "vlc" );
			if (vlcPath==null) {  // Poner VLC a mano
	        	System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe");
			} else {  // Poner VLC desde la variable de entorno
				System.setProperty( "jna.library.path", vlcPath );
			}
		}
    	
    	// Lanzar ventana
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				miVentana = new VideoPlayer();
				miVentana.setVisible( true );
				miVentana.listaRepVideos.add( path, ficheros );
				miVentana.listaRepVideos.irAPrimero();
				miVentana.lanzaVideo();
			}
		});
	}
	
}
