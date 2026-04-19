package application;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import automation.RobotController;
import database.DB;
import database.DB.PresetData;
import utils.CoordenadasMouse;
import utils.PresetConfig;
import utils.PresetManager;
import utils.Tutorial;

public class Program extends JFrame {

	private Thread automacaoThread;

	private static final long serialVersionUID = 1L;

	private static final Color PURPLE = new Color(164, 53, 240);

	private JPanel contentPane;

	private int centroX, centroY;
	private int finalX, finalY;
	private int proxX, proxY;

	private boolean temFinal = false;
	private boolean temProx = false;

	private Integer presetSegundos = null;
	private Integer presetQuantidade = null;

	private long tempoMs;

	private volatile boolean rodando = false;

	private JButton btn1, btn2, btn3, btnIniciar;
	private JMenu mnPreset;

	private JLabel lblPreset;
	private JButton btnLimparPreset;
	private String nomePresetAtual = null;

	public static void main(String[] args) {
		try {
			FlatDarkLaf.setup();
		} 
		catch (Exception ex) {
		}
		EventQueue.invokeLater(() -> {
			try {
				new Program().setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Program() {
		setResizable(false);
		setTitle("Automação Udemy");
		setSize(550, 360);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		java.awt.Image iconImg = loadImage("/imgs/icon.png");
		if (iconImg != null)setIconImage(iconImg);

		buildMenuBar();
		buildContent();
		refreshPresetMenu();
	}

	private void buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnUtils = new JMenu("Utils");
		mnUtils.setFont(new Font("SansSerif", Font.PLAIN, 12));
		menuBar.add(mnUtils);

		JMenuItem itemMouse = new JMenuItem("Coordenada Mouse");
		itemMouse.setFont(new Font("SansSerif", Font.PLAIN, 12));
		itemMouse.setIcon(safeIcon("/imgs/xymouseicon16px.png"));
		itemMouse.addActionListener(e -> new CoordenadasMouse().setVisible(true));
		mnUtils.add(itemMouse);

		JMenuItem itemTutorial = new JMenuItem("Tutorial");
		itemTutorial.setFont(new Font("SansSerif", Font.PLAIN, 12));
		itemTutorial.setIcon(safeIcon("/imgs/tutorial16.png"));
		itemTutorial.addActionListener(e -> new Tutorial().setVisible(true));
		mnUtils.add(itemTutorial);

		JMenu mnTema = new JMenu("Tema");
		mnTema.setFont(new Font("SansSerif", Font.PLAIN, 12));
		menuBar.add(mnTema);

		JMenuItem modoClaro = new JMenuItem("Modo Claro");
		modoClaro.setFont(new Font("SansSerif", Font.PLAIN, 12));
		modoClaro.setIcon(safeIcon("/imgs/sun.png"));
		modoClaro.addActionListener(e -> trocarTema(false));
		mnTema.add(modoClaro);

		JMenuItem modoEscuro = new JMenuItem("Modo Escuro");
		modoEscuro.setFont(new Font("SansSerif", Font.PLAIN, 12));
		modoEscuro.setIcon(safeIcon("/imgs/moon.png"));
		modoEscuro.addActionListener(e -> trocarTema(true));
		mnTema.add(modoEscuro);

		mnPreset = new JMenu("Preset");
		mnPreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
		menuBar.add(mnPreset);
	}

	public void refreshPresetMenu() {
		mnPreset.removeAll();

		JMenuItem itemGerenciar = new JMenuItem("Gerenciar Presets");
		itemGerenciar.setIcon(new ImageIcon(Program.class.getResource("/imgs/preset.png")));
		itemGerenciar.setFont(new Font("SansSerif", Font.PLAIN, 12));
		itemGerenciar.addActionListener(e -> new PresetManager(this, this::carregarPreset, this::refreshPresetMenu).setVisible(true));
		mnPreset.add(itemGerenciar);

		JMenuItem itemConfig = new JMenuItem("Configurações");
		itemConfig.setIcon(new ImageIcon(Program.class.getResource("/imgs/config.png")));
		itemConfig.setFont(new Font("SansSerif", Font.PLAIN, 12));
		itemConfig.addActionListener(e -> new PresetConfig(this, this::refreshPresetMenu).setVisible(true));
		mnPreset.add(itemConfig);

		List<PresetData> presets = DB.loadPresets();
		if (!presets.isEmpty()) {
			mnPreset.addSeparator();
			for (PresetData p : presets) {
				JMenuItem item = new JMenuItem(p.name);
				item.setFont(new Font("SansSerif", Font.PLAIN, 12));
				final PresetData capture = p;
				item.addActionListener(e -> carregarPreset(capture));
				mnPreset.add(item);
			}
		}
	}

	private void buildContent() {
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		btn1 = new JButton("<html><center>1) Centro do Vídeo</center></html>");
		btn1.setBackground(PURPLE);
		btn1.setForeground(Color.WHITE);
		btn1.setFont(new Font("SansSerif", Font.BOLD, 15));
		btn1.setBounds(150, 20, 220, 60);
		contentPane.add(btn1);

		btn2 = new JButton("<html><center>2) Final do Vídeo</center></html>");
		btn2.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btn2.setBounds(150, 100, 220, 60);
		btn2.setEnabled(false);
		contentPane.add(btn2);

		btn3 = new JButton("<html><center>3) Próximo Vídeo</center></html>");
		btn3.setBackground(PURPLE);
		btn3.setForeground(Color.WHITE);
		btn3.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btn3.setBounds(150, 180, 220, 60);
		btn3.setEnabled(false);
		contentPane.add(btn3);

		btnIniciar = new JButton("Iniciar");
		btnIniciar.setBackground(PURPLE);
		btnIniciar.setForeground(Color.WHITE);
		btnIniciar.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnIniciar.setBounds(200, 263, 120, 40);
		btnIniciar.setEnabled(false);
		contentPane.add(btnIniciar);

		lblPreset = new JLabel("");
		lblPreset.setFont(new Font("SansSerif", Font.BOLD, 11));
		lblPreset.setBounds(10, 275, 200, 14);
		lblPreset.setVisible(false);
		contentPane.add(lblPreset);

		btnLimparPreset = new JButton();
		btnLimparPreset.setToolTipText("Limpar Tudo");
		btnLimparPreset.setIcon(new ImageIcon(Program.class.getResource("/imgs/erase.png")));
		btnLimparPreset.setBounds(63, 100, 24, 11);
		btnLimparPreset.setVisible(false);
		contentPane.add(btnLimparPreset);

		btnLimparPreset.addActionListener(e -> limparPreset());
		btnLimparPreset.setIcon(new ImageIcon(Program.class.getResource("/imgs/erase.png")));
		btnLimparPreset.setBounds(107, 267, 50, 33);
		contentPane.add(btnLimparPreset);

		JLabel lblNewLabel = new JLabel("Made by Enzima01");
		lblNewLabel.setIcon(new ImageIcon(Program.class.getResource("/imgs/01_16x16.png")));
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel.setBounds(394, 290, 140, 14);
		contentPane.add(lblNewLabel);

		btn1.addActionListener(e -> {
			Integer[] v = pedirCoordenadas("Centro do Vídeo");
			if (v != null) {
				centroX = v[0];
				centroY = v[1];
				btn1.setText("<html><center>1) Centro do Vídeo<br>(" + centroX + ", " + centroY + ")</center></html>");
				btn2.setFont(new Font("SansSerif", Font.BOLD, 15));
				btn2.setBackground(PURPLE);
				btn2.setForeground(Color.WHITE);
				btn1.setFont(new Font("SansSerif", Font.PLAIN, 12));
				btn2.setEnabled(true);
			}
		});

		btn2.addActionListener(e -> {
			Integer[] v = pedirCoordenadas("Final do Vídeo");
			if (v != null) {
				finalX = v[0];
				finalY = v[1];
				temFinal = true;
				btn2.setText("<html><center>2) Final do Vídeo<br>(" + finalX + ", " + finalY + ")</center></html>");
				btn3.setFont(new Font("SansSerif", Font.BOLD, 15));
				btn2.setFont(new Font("SansSerif", Font.PLAIN, 12));
				btn3.setEnabled(true);
			}
		});

		btn3.addActionListener(e -> {
			Integer[] v = pedirCoordenadas("Próximo Vídeo");
			if (v != null) {
				proxX = v[0];
				proxY = v[1];
				temProx = true;
				btn3.setText("<html><center>3) Próximo Vídeo<br>(" + proxX + ", " + proxY + ")</center></html>");
				btnIniciar.setFont(new Font("SansSerif", Font.BOLD, 15));
				btn3.setFont(new Font("SansSerif", Font.PLAIN, 12));
				btnIniciar.setEnabled(true);
				btnLimparPreset.setVisible(true);
			}
		});

		btnIniciar.addActionListener(e -> {
			if (!rodando)
				iniciar();
			else
				encerrar();
		});
	}

	private void encerrar() {
		rodando = false;
		setEstadoParado();

		if (automacaoThread != null && automacaoThread.isAlive()) {
			automacaoThread.interrupt();
		}

		JOptionPane.showMessageDialog(null, "Automação encerrada!", "Aviso", JOptionPane.WARNING_MESSAGE);
	}

	private void setEstadoRodando() {
		rodando = true;
		btn1.setEnabled(false);
		btn2.setEnabled(false);
		btn3.setEnabled(false);
		btnIniciar.setText("Encerrar");
		btnLimparPreset.setEnabled(false);
	}

	private void setEstadoParado() {
		rodando = false;
		btn1.setEnabled(true);
		btn2.setEnabled(temFinal);
		btn3.setEnabled(temProx);
		btnIniciar.setText("Iniciar");
		btnLimparPreset.setEnabled(true);
	}

	private void iniciar() {
		String initialTempo = presetSegundos != null ? presetSegundos.toString() : "";
		String initialQtd = presetQuantidade != null ? presetQuantidade.toString() : "";

		String tempoInput = (String) JOptionPane.showInputDialog(this,"Abra o Udemy no navegador e deixe o vídeo pronto!\n\n" + "Tempo para iniciar (segundos):\nRecomendado: 5", "Tempo", JOptionPane.QUESTION_MESSAGE, null, null, initialTempo);
		if (tempoInput == null)
			return;
		
		String qtdInput = (String) JOptionPane.showInputDialog(this, "Quantos vídeos deseja passar?", "Quantidade", JOptionPane.QUESTION_MESSAGE, null, null, initialQtd);
		if (qtdInput == null)
			return;

		int segundos, quantidade;
		try {
			segundos = Integer.parseInt(tempoInput.trim());
			quantidade = Integer.parseInt(qtdInput.trim());
			tempoMs = segundos * 1000L;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Digite apenas números!");
			return;
		}

		boolean veioDePreset = presetSegundos != null && presetQuantidade != null;

		presetSegundos = segundos;
		presetQuantidade = quantidade;

		if (!veioDePreset) {
			int resp = JOptionPane.showConfirmDialog(this, "Deseja salvar essas configurações como preset?", "Salvar Preset", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (resp == JOptionPane.YES_OPTION) {
				new utils.Preset(this, centroX, centroY, finalX, finalY, proxX, proxY, segundos, quantidade, this::refreshPresetMenu).setVisible(true);
			}
		}

		setEstadoRodando();

		automacaoThread = new Thread(() -> {
			try {
				RobotController robo = new RobotController();
				Thread.sleep(tempoMs);

				for (int i = 0; i < quantidade && rodando; i++) {
					if (Thread.currentThread().isInterrupted())
						break;

					robo.clicar(centroX, centroY);
					Thread.sleep(2000);

					if (!rodando || Thread.currentThread().isInterrupted())
						break;

					robo.clicar(finalX, finalY);
					Thread.sleep(1000);

					if (!rodando || Thread.currentThread().isInterrupted())
						break;

					robo.clicar(proxX, proxY);
					Thread.sleep(3000);
				}

				SwingUtilities.invokeLater(() -> {
					if (rodando)
						JOptionPane.showMessageDialog(null, "Automação finalizada!");
					else
						JOptionPane.showMessageDialog(null, "Automação interrompida!");
				});

			} catch (Exception e) {
			}

			SwingUtilities.invokeLater(this::setEstadoParado);
		});

		automacaoThread.start();
	}

	private void carregarPreset(PresetData p) {
		centroX = p.centroX;
		centroY = p.centroY;
		finalX = p.finalX;
		finalY = p.finalY;
		proxX = p.proxX;
		proxY = p.proxY;

		presetSegundos = p.segundos;
		presetQuantidade = p.quantidade;

		temFinal = true;
		temProx = true;

		btn1.setText("<html><center>1) Centro do Vídeo<br>(" + centroX + ", " + centroY + ")</center></html>");
		btn1.setFont(new Font("SansSerif", Font.PLAIN, 12));

		btn2.setText("<html><center>2) Final do Vídeo<br>(" + finalX + ", " + finalY + ")</center></html>");
		btn2.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btn2.setBackground(PURPLE);
		btn2.setForeground(Color.WHITE);
		btn2.setEnabled(true);

		btn3.setText("<html><center>3) Próximo Vídeo<br>(" + proxX + ", " + proxY + ")</center></html>");
		btn3.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btn3.setEnabled(true);

		btnIniciar.setFont(new Font("SansSerif", Font.BOLD, 15));
		btnIniciar.setEnabled(true);

		nomePresetAtual = p.name;

		String nomeExibicao = nomePresetAtual.length() > 7 ? nomePresetAtual.substring(0, 7) + "..." : nomePresetAtual;
		lblPreset.setText("Preset: " + nomeExibicao);
		lblPreset.setVisible(true);
		btnLimparPreset.setVisible(true);
	}

	private void limparPreset() {
		nomePresetAtual = null;
		presetSegundos = null;
		presetQuantidade = null;

		centroX = centroY = 0;
		finalX = finalY = 0;
		proxX = proxY = 0;

		temFinal = false;
		temProx = false;

		btn1.setText("<html><center>1) Centro do Vídeo</center></html>");
		btn1.setFont(new Font("SansSerif", Font.BOLD, 15));

		btn2.setText("<html><center>2) Final do Vídeo</center></html>");
		btn2.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btn2.setEnabled(false);

		btn3.setText("<html><center>3) Próximo Vídeo</center></html>");
		btn3.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btn3.setEnabled(false);

		btnIniciar.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnIniciar.setEnabled(false);

		lblPreset.setVisible(false);
		btnLimparPreset.setVisible(false);
	}

	private void trocarTema(boolean escuro) {
		try {
			UIManager.setLookAndFeel(escuro ? new FlatDarkLaf() : new FlatLightLaf());
			for (Window w : Window.getWindows()) {
				SwingUtilities.updateComponentTreeUI(w);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Integer[] pedirCoordenadas(String etapa) {
		try {
			int x = Integer.parseInt(JOptionPane.showInputDialog(null, "X", etapa, JOptionPane.QUESTION_MESSAGE));
			int y = Integer.parseInt(JOptionPane.showInputDialog(null, "Y", etapa, JOptionPane.QUESTION_MESSAGE));
			return new Integer[] { x, y };
		} catch (Exception e) {
			return null;
		}
	}

	private static java.awt.Image loadImage(String path) {
		try {
			java.net.URL url = Program.class.getResource(path);
			return url != null ? Toolkit.getDefaultToolkit().getImage(url) : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static ImageIcon safeIcon(String path) {
		try {
			java.net.URL url = Program.class.getResource(path);
			return url != null ? new ImageIcon(url) : null;
		} catch (Exception e) {
			return null;
		}
	}
}