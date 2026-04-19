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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import database.DB;
import database.DB.PresetData;

public class Preset extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Color PURPLE = new Color(164, 53, 240);
	private static final Color PURPLE_DIM = new Color(130, 30, 200);

	private final int centroX, centroY;
	private final int finalX, finalY;
	private final int proxX, proxY;
	private final int segundos;
	private final int quantidade;
	private final Runnable onSaved;

	private JTextField tfNome;

	public Preset(Frame owner, int centroX, int centroY, int finalX, int finalY, int proxX, int proxY, int segundos, int quantidade, Runnable onSaved) {
		super(owner, "Salvar Preset", true);
		this.centroX = centroX;
		this.centroY = centroY;
		this.finalX = finalX;
		this.finalY = finalY;
		this.proxX = proxX;
		this.proxY = proxY;
		this.segundos = segundos;
		this.quantidade = quantidade;
		this.onSaved = onSaved;

		buildUI();
		pack();
		setResizable(false);
		setResizable(false);
		setLocationRelativeTo(owner);
	}

	private void buildUI() {
		JPanel root = new JPanel(new BorderLayout(0, 0));
		setResizable(false);
		root.setBorder(new EmptyBorder(22, 26, 18, 26));
		setContentPane(root);

		JLabel title = new JLabel("Salvar Preset");
		title.setFont(new Font("SansSerif", Font.BOLD, 17));
		title.setBorder(new EmptyBorder(0, 0, 14, 0));
		root.add(title, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 8);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		JLabel lblNome = new JLabel("Nome do preset:");
		lblNome.setFont(new Font("SansSerif", Font.BOLD, 13));
		form.add(lblNome, gbc);

		tfNome = new JTextField(22);
		tfNome.setFont(new Font("SansSerif", Font.PLAIN, 13));
		gbc.gridx = 1;
		gbc.weightx = 1;
		form.add(tfNome, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 4, 6, 4);
		JLabel lblResumo = new JLabel("Resumo das configurações:");
		lblResumo.setFont(new Font("SansSerif", Font.BOLD, 11));
		lblResumo.setForeground(new Color(120, 120, 120));
		form.add(lblResumo, gbc);
		gbc.gridwidth = 1;
		gbc.insets = new Insets(3, 4, 3, 8);

		Object[][] rows = { { "Centro do Vídeo:", centroX + ", " + centroY },
				{ "Final do Vídeo:", finalX + ", " + finalY }, { "Próximo Vídeo:", proxX + ", " + proxY },
				{ "Tempo (seg):", String.valueOf(segundos) }, { "Qtd. de Vídeos:", String.valueOf(quantidade) }, };
		for (int i = 0; i < rows.length; i++) {
			gbc.gridx = 0;
			gbc.gridy = i + 2;
			gbc.weightx = 0;
			JLabel lbl = new JLabel((String) rows[i][0]);
			lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
			lbl.setForeground(new Color(140, 140, 140));
			form.add(lbl, gbc);

			gbc.gridx = 1;
			gbc.weightx = 1;
			JLabel val = new JLabel((String) rows[i][1]);
			val.setFont(new Font("SansSerif", Font.BOLD, 12));
			form.add(val, gbc);
		}

		root.add(form, BorderLayout.CENTER);

		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		bar.setBorder(new EmptyBorder(14, 0, 0, 0));

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

	private void salvar() {
		String nome = tfNome.getText().trim();
		if (nome.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Informe um nome para o preset.", "Aviso", JOptionPane.WARNING_MESSAGE);
			tfNome.requestFocus();
			return;
		}
		PresetData p = new PresetData(nome, centroX, centroY, finalX, finalY, proxX, proxY, segundos, quantidade);
		try {
			DB.savePreset(p);
			JOptionPane.showMessageDialog(this, "Preset \"" + nome + "\" salvo com sucesso!", "Salvo",
					JOptionPane.INFORMATION_MESSAGE);
			dispose();
			if (onSaved != null)
				onSaved.run();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
}
