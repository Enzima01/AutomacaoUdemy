package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import database.DB;
import database.DB.PresetData;

public class PresetManager extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color PURPLE = new Color(164, 53, 240);
	private static final Color PURPLE_DIM = new Color(130, 30, 200);


	private static final String[] COLS = { "Nome", "Centro (X,Y)", "Final (X,Y)", "Próximo (X,Y)", "Segundos", "Vídeos" };

	private DefaultTableModel model;
	private JTable table;

	private final java.util.function.Consumer<PresetData> onLoad;
	
	private final Runnable onChanged;

	public PresetManager(Frame owner, java.util.function.Consumer<PresetData> onLoad, Runnable onChanged) {
		super("Gerenciar Presets");
		this.onLoad = onLoad;
		this.onChanged = onChanged;
		setIconImage(new ImageIcon(getClass().getResource("/imgs/preset.png")).getImage());
		buildUI();
		refresh();
		setSize(740, 420);
		setResizable(false);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void buildUI() {
		JPanel root = new JPanel(new BorderLayout(0, 0));
		root.setBorder(new EmptyBorder(14, 14, 14, 14));
		setContentPane(root);

		JLabel header = new JLabel("Presets Salvos");
		header.setFont(new Font("SansSerif", Font.BOLD, 18));
		header.setBorder(new EmptyBorder(0, 0, 12, 0));
		root.add(header, BorderLayout.NORTH);

		model = new DefaultTableModel(COLS, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		table = new JTable(model);
		table.setRowHeight(30);
		table.setFont(new Font("SansSerif", Font.PLAIN, 13));
		table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);

		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus,
					int row, int col) {

				Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);

				if (isSelected) {
					c.setBackground(PURPLE);
					c.setForeground(Color.WHITE);
				} else {
					Color base = t.getBackground();

					if (isDarkTheme()) {
						c.setBackground(row % 2 == 0 ? base : base.darker());
					} else {
						c.setBackground(row % 2 == 0 ? base : base.brighter());
					}
					c.setForeground(t.getForeground());
				}

				return c;
			}
		});

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
		root.add(scroll, BorderLayout.CENTER);

		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		bar.setBorder(new EmptyBorder(12, 0, 0, 0));

		JButton btnDelete = new JButton("Excluir");
		btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnDelete.addActionListener(e -> excluir());

		JButton btnEdit = new JButton("Editar");
		btnEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnEdit.addActionListener(e -> editar());

		JButton btnLoad = new JButton("Carregar Preset");
		btnLoad.setBackground(PURPLE);
		btnLoad.setForeground(Color.WHITE);
		btnLoad.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnLoad.setFocusPainted(false);
		btnLoad.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnLoad.setBackground(PURPLE_DIM);
			}

			public void mouseExited(MouseEvent e) {
				btnLoad.setBackground(PURPLE);
			}
		});
		btnLoad.addActionListener(e -> carregar());

		bar.add(btnDelete);
		bar.add(btnEdit);
		bar.add(btnLoad);
		root.add(bar, BorderLayout.SOUTH);
	}

	public void refresh() {
		model.setRowCount(0);
		List<PresetData> list = DB.loadPresets();
		for (PresetData p : list) {
			model.addRow(new Object[] { p.name, p.centroX + ", " + p.centroY, p.finalX + ", " + p.finalY, p.proxX + ", " + p.proxY, p.segundos, p.quantidade });
		}
	}

	private PresetData selectedPreset() {
		int row = table.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Selecione um preset na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		String name = (String) model.getValueAt(row, 0);
		return DB.loadPresets().stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
	}

	private void carregar() {
		PresetData p = selectedPreset();
		if (p == null)
			return;
		if (onLoad != null)
			onLoad.accept(p);
		dispose();
	}

	private void excluir() {
		PresetData p = selectedPreset();
		if (p == null)
			return;
		int ans = JOptionPane.showConfirmDialog(this, "Excluir o preset \"" + p.name + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (ans != JOptionPane.YES_OPTION)
			return;
		try {
			DB.deletePreset(p.name);
			refresh();
			if (onChanged != null)
				onChanged.run();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void editar() {
		PresetData p = selectedPreset();
		if (p == null)
			return;
		new EditPresetDialog(this, p, () -> {
			refresh();
			if (onChanged != null)
				onChanged.run();
		}).setVisible(true);
	}

	private boolean isDarkTheme() {
		Color bg = UIManager.getColor("Table.background");
		int brightness = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
		return brightness < 128;
	}

	private static class EditPresetDialog extends JDialog {

		private static final long serialVersionUID = 1L;

		private final PresetData original;
		private final Runnable onDone;

		private JTextField tfNome, tfCentroX, tfCentroY, tfFinalX, tfFinalY, tfProxX, tfProxY, tfSeg, tfQtd;

		EditPresetDialog(JFrame owner, PresetData p, Runnable onDone) {
			super(owner, "Editar Preset", true);
			this.original = p;
			this.onDone = onDone;

			buildUI();
			pack();
			setResizable(false);
			setLocationRelativeTo(owner);
		}

		private JTextField row(JPanel p, GridBagConstraints g, String label, String value, int y) {
			g.gridx = 0;
			g.gridy = y;
			g.weightx = 0;
			p.add(new JLabel(label), g);
			JTextField tf = new JTextField(value, 12);
			g.gridx = 1;
			g.weightx = 1;
			p.add(tf, g);
			return tf;
		}

		private void buildUI() {
			JPanel root = new JPanel(new BorderLayout(0, 0));
			root.setBorder(new EmptyBorder(20, 24, 16, 24));
			setContentPane(root);

			JLabel title = new JLabel("Editar: " + original.name);
			title.setFont(new Font("SansSerif", Font.BOLD, 15));
			title.setBorder(new EmptyBorder(0, 0, 12, 0));
			root.add(title, BorderLayout.NORTH);

			JPanel form = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(4, 4, 4, 4);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			tfNome = row(form, gbc, "Nome:", original.name, 0);
			tfCentroX = row(form, gbc, "Centro X:", String.valueOf(original.centroX), 1);
			tfCentroY = row(form, gbc, "Centro Y:", String.valueOf(original.centroY), 2);
			tfFinalX = row(form, gbc, "Final X:", String.valueOf(original.finalX), 3);
			tfFinalY = row(form, gbc, "Final Y:", String.valueOf(original.finalY), 4);
			tfProxX = row(form, gbc, "Próximo X:", String.valueOf(original.proxX), 5);
			tfProxY = row(form, gbc, "Próximo Y:", String.valueOf(original.proxY), 6);
			tfSeg = row(form, gbc, "Segundos:", String.valueOf(original.segundos), 7);
			tfQtd = row(form, gbc, "Qtd. de Vídeos:", String.valueOf(original.quantidade), 8);

			root.add(form, BorderLayout.CENTER);

			JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
			bar.setBorder(new EmptyBorder(12, 0, 0, 0));

			JButton btnCancel = new JButton("Cancelar");
			btnCancel.addActionListener(e -> dispose());

			JButton btnSave = new JButton("Salvar Alterações");
			btnSave.setBackground(PURPLE);
			btnSave.setForeground(Color.WHITE);
			btnSave.setFont(new Font("SansSerif", Font.BOLD, 12));
			btnSave.setFocusPainted(false);
			btnSave.addActionListener(e -> salvar());
			getRootPane().setDefaultButton(btnSave);

			bar.add(btnCancel);
			bar.add(btnSave);
			root.add(bar, BorderLayout.SOUTH);
		}

		private void salvar() {
			try {
				String nome = tfNome.getText().trim();
				int cx = Integer.parseInt(tfCentroX.getText().trim());
				int cy = Integer.parseInt(tfCentroY.getText().trim());
				int fx = Integer.parseInt(tfFinalX.getText().trim());
				int fy = Integer.parseInt(tfFinalY.getText().trim());
				int px = Integer.parseInt(tfProxX.getText().trim());
				int py = Integer.parseInt(tfProxY.getText().trim());
				int seg = Integer.parseInt(tfSeg.getText().trim());
				int qtd = Integer.parseInt(tfQtd.getText().trim());

				if (nome.isEmpty())
					throw new Exception("Nome não pode ser vazio.");

				if (!nome.equalsIgnoreCase(original.name)) {
					DB.deletePreset(original.name);
					DB.savePreset(new PresetData(nome, cx, cy, fx, fy, px, py, seg, qtd));
				} else {
					DB.updatePreset(new PresetData(nome, cx, cy, fx, fy, px, py, seg, qtd));
				}

				dispose();
				if (onDone != null)
					onDone.run();

			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Todos os campos de coordenadas devem ser números inteiros.", "Erro", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
