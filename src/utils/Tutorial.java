package utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Tutorial extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public Tutorial() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Tutorial.class.getResource("/imgs/tutorial16.png")));
		setResizable(false);
		setTitle("Tutorial de Uso");
		setSize(600, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel texto = new JLabel("<html><div style='text-align: center;'>" + "<b>COMO USAR O PROGRAMA:</b><br><br>"
				+ "1) Abra o Udemy no navegador e deixe o vídeo pronto.<br><br>"
				+ "2) Vá em Utils → Coordenada Mouse.<br>" + "Passe o mouse sobre os botões desejados.<br>"
				+ "(Indicado como 'X' nas imagens de exemplo)<br>" + "Pegue os valores X e Y.<br><br>"
				+ "3) Preencha no programa:<br>" + "- Centro do vídeo<br>" + "- Final do vídeo<br>"
				+ "- Próximo vídeo<br><br>" + "4) Clique em iniciar:<br>" + "- Informe o tempo<br>"
				+ "- Informe a quantidade de vídeos que deseja assitir e pular<br><br>"
				+ "<b>⚠ Não mexa no mouse durante a automação!</b>" + "</div></html>");

		texto.setHorizontalAlignment(SwingConstants.CENTER);
		texto.setFont(new Font("SansSerif", Font.BOLD, 15));

		texto.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(texto);
		contentPane.add(Box.createRigidArea(new Dimension(0, 15)));

		adicionarImagem("/imgs/1.png", "Centro do vídeo");
		adicionarImagem("/imgs/2.png", "Final do vídeo");
		adicionarImagem("/imgs/3.png", "Próximo vídeo");

		JScrollPane scroll = new JScrollPane(contentPane);
		setContentPane(scroll);
	}

	private void adicionarImagem(String caminho, String descricao) {

		JLabel titulo = new JLabel(descricao);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
		titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(titulo);

		try {
			ImageIcon icon = new ImageIcon(getClass().getResource(caminho));
			Image img = icon.getImage().getScaledInstance(350, 200, Image.SCALE_SMOOTH);

			JLabel labelImg = new JLabel(new ImageIcon(img));
			labelImg.setAlignmentX(Component.CENTER_ALIGNMENT);

			contentPane.add(labelImg);

		} catch (Exception e) {
			JLabel erro = new JLabel("Imagem não encontrada: " + caminho);
			erro.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(erro);
		}

		contentPane.add(Box.createRigidArea(new Dimension(0, 20)));
	}
}