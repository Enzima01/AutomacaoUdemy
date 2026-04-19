package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import database.DB;

public class PresetConfig extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Color PURPLE = new Color(164, 53, 240);
	private static final Color PURPLE_DIM = new Color(130, 30, 200);

	private JTextField tfCaminho;
	private final Runnable onSaved;

	public PresetConfig(Frame owner, Runnable onSaved) {
		super(owner, "Configurações de Preset", true);
		this.onSaved = onSaved;
		setIconImage(new ImageIcon(getClass().getResource("/imgs/config.png")).getImage());
		buildUI();
		pack();
		setResizable(false);
		setResizable(false);
		setLocationRelativeTo(owner);
	}

	private void buildUI() {
		JPanel root = new JPanel(new BorderLayout(0, 12));
		root.setBorder(new EmptyBorder(20, 24, 18, 24));
		setContentPane(root);

		JLabel title = new JLabel("Configurações");
		title.setFont(new Font("SansSerif", Font.BOLD, 17));
		root.add(title, BorderLayout.NORTH);

		JPanel center = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 0, 6, 8);
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		JLabel desc = new JLabel("<html><div style='width:340px'>" + "Escolha a pasta onde os presets serão salvos. "
				+ "O banco de dados (<b>presets.db</b>) será movido automaticamente " + "para o novo local."
				+ "</div></html>");
		desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
		center.add(desc, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		center.add(new JLabel("Pasta:"), gbc);

		tfCaminho = new JTextField(DB.getDbDir(), 28);
		tfCaminho.setFont(new Font("SansSerif", Font.PLAIN, 12));
		tfCaminho.setEditable(false);
		tfCaminho.setBackground(UIManager.getColor("TextField.background"));
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		center.add(tfCaminho, gbc);

		JButton btnBrowse = new JButton("...");
		btnBrowse.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnBrowse.setMargin(new Insets(2, 8, 2, 8));
		btnBrowse.addActionListener(e -> escolherPasta());
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		center.add(btnBrowse, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.NONE;
		JLabel lblAtual = new JLabel("Caminho atual: " + DB.getDbDir());
		lblAtual.setFont(new Font("SansSerif", Font.ITALIC, 11));
		lblAtual.setForeground(new Color(130, 130, 130));
		center.add(lblAtual, gbc);

		root.add(center, BorderLayout.CENTER);

		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnCancelar.addActionListener(e -> dispose());

		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.setBackground(PURPLE);
		btnSalvar.setForeground(Color.WHITE);
		btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnSalvar.setFocusPainted(false);
		btnSalvar.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnSalvar.setBackground(PURPLE_DIM);
			}

			public void mouseExited(MouseEvent e) {
				btnSalvar.setBackground(PURPLE);
			}
		});
		btnSalvar.addActionListener(e -> salvar());
		getRootPane().setDefaultButton(btnSalvar);

		bar.add(btnCancelar);
		bar.add(btnSalvar);
		root.add(bar, BorderLayout.SOUTH);
	}

	private void escolherPasta() {
		JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		chooser.setDialogTitle("Escolher pasta para salvar presets");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(DB.getDbDir()));
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			tfCaminho.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void salvar() {
		String novoCaminho = tfCaminho.getText().trim();
		if (novoCaminho.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Informe um caminho válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			DB.setDbDir(novoCaminho);
			JOptionPane.showMessageDialog(this, "Configuração salva!\nPresets agora serão salvos em:\n" + novoCaminho, "Salvo", JOptionPane.INFORMATION_MESSAGE);
			dispose();
			if (onSaved != null)
				onSaved.run();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Erro ao salvar configuração:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
}
