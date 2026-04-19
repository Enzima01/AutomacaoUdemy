package utils;

import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class CoordenadasMouse extends JFrame {

	private static final long serialVersionUID = 1L;

	private JLabel label;

	public CoordenadasMouse() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(CoordenadasMouse.class.getResource("/imgs/xymouseicon.png")));
		setResizable(false);
		setTitle("Coordenadas do Mouse | Enzima01");
		setSize(363, 120);

		setLocationRelativeTo(null);

		label = new JLabel("X: 0 | Y: 0", SwingConstants.CENTER);
		label.setFont(new Font("Arial", Font.BOLD, 18));
		getContentPane().add(label);

		// Atualiza as coordenadas a cada 20ms
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Point p = MouseInfo.getPointerInfo().getLocation();
				label.setText("X: " + p.x + " | Y: " + p.y);
			}
		}, 0, 20);

		setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		new CoordenadasMouse();
	}
}