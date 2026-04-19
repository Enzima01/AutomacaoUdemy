package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DB {

	private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".udemy_automation";
	private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.properties";
	private static final String DEFAULT_DB_DIR = CONFIG_DIR;

	public static String getDbDir() {
		try {
			Properties props = loadConfig();
			return props.getProperty("db.dir", DEFAULT_DB_DIR);
		} catch (Exception e) {
			return DEFAULT_DB_DIR;
		}
	}

	public static void setDbDir(String newDir) throws Exception {
		String oldPath = getDbDir() + File.separator + "presets.db";
		String newPath = newDir + File.separator + "presets.db";

		new File(newDir).mkdirs();

		File oldFile = new File(oldPath);
		if (oldFile.exists() && !oldPath.equals(newPath)) {
			Files.move(oldFile.toPath(), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
		}

		Properties props = loadConfig();
		props.setProperty("db.dir", newDir);
		saveConfig(props);

		initTable();
	}

	private static String getUrl() {
		return "jdbc:sqlite:" + getDbDir() + File.separator + "presets.db";
	}

	static {
		try {
			new File(CONFIG_DIR).mkdirs();
			new File(getDbDir()).mkdirs();
			initTable();
		} catch (Exception e) {
			throw new RuntimeException("Falha ao inicializar banco: " + e.getMessage(), e);
		}
	}

	private static void initTable() throws Exception {
		new File(getDbDir()).mkdirs();
		try (Connection conn = DriverManager.getConnection(getUrl()); Statement stmt = conn.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS presets (" + "  name       TEXT PRIMARY KEY,"
					+ "  centroX    INTEGER NOT NULL," + "  centroY    INTEGER NOT NULL,"
					+ "  finalX     INTEGER NOT NULL," + "  finalY     INTEGER NOT NULL,"
					+ "  proxX      INTEGER NOT NULL," + "  proxY      INTEGER NOT NULL,"
					+ "  segundos   INTEGER NOT NULL DEFAULT 5," + "  quantidade INTEGER NOT NULL DEFAULT 1" + ")");
		}
	}

	private static Connection connect() throws SQLException {
		return DriverManager.getConnection(getUrl());
	}

	private static Properties loadConfig() throws IOException {
		Properties props = new Properties();
		File f = new File(CONFIG_FILE);
		if (f.exists()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				props.load(fis);
			}
		}
		return props;
	}

	private static void saveConfig(Properties props) throws IOException {
		new File(CONFIG_DIR).mkdirs();
		try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
			props.store(fos, "Udemy Automation Config");
		}
	}

	public static class PresetData {
		public String name;
		public int centroX, centroY;
		public int finalX, finalY;
		public int proxX, proxY;
		public int segundos;
		public int quantidade;

		public PresetData() {
		}

		public PresetData(String name, int centroX, int centroY, int finalX, int finalY, int proxX, int proxY,
				int segundos, int quantidade) {
			this.name = name;
			this.centroX = centroX;
			this.centroY = centroY;
			this.finalX = finalX;
			this.finalY = finalY;
			this.proxX = proxX;
			this.proxY = proxY;
			this.segundos = segundos;
			this.quantidade = quantidade;
		}
	}

	public static List<PresetData> loadPresets() {
		List<PresetData> list = new ArrayList<>();
		try (Connection conn = connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM presets ORDER BY name COLLATE NOCASE")) {
			while (rs.next())
				list.add(fromRow(rs));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void savePreset(PresetData p) throws Exception {
		String sql = "INSERT INTO presets " + "(name,centroX,centroY,finalX,finalY,proxX,proxY,segundos,quantidade) "
				+ "VALUES (?,?,?,?,?,?,?,?,?)";
		try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			fill(ps, p);
			ps.executeUpdate();
		} catch (SQLException e) {
			if (e.getMessage().toLowerCase().contains("unique")) {
				throw new Exception("Já existe um preset com o nome \"" + p.name + "\".");
			}
			throw e;
		}
	}

	public static void updatePreset(PresetData p) throws Exception {
		String sql = "UPDATE presets SET centroX=?,centroY=?,finalX=?,finalY=?,"
				+ "proxX=?,proxY=?,segundos=?,quantidade=? WHERE name=?";
		try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, p.centroX);
			ps.setInt(2, p.centroY);
			ps.setInt(3, p.finalX);
			ps.setInt(4, p.finalY);
			ps.setInt(5, p.proxX);
			ps.setInt(6, p.proxY);
			ps.setInt(7, p.segundos);
			ps.setInt(8, p.quantidade);
			ps.setString(9, p.name);
			if (ps.executeUpdate() == 0)
				throw new Exception("Preset \"" + p.name + "\" não encontrado.");
		}
	}

	public static void deletePreset(String name) throws Exception {
		try (Connection conn = connect();
				PreparedStatement ps = conn.prepareStatement("DELETE FROM presets WHERE name=?")) {
			ps.setString(1, name);
			ps.executeUpdate();
		}
	}

	private static PresetData fromRow(ResultSet rs) throws SQLException {
		return new PresetData(rs.getString("name"), rs.getInt("centroX"), rs.getInt("centroY"), rs.getInt("finalX"),
				rs.getInt("finalY"), rs.getInt("proxX"), rs.getInt("proxY"), rs.getInt("segundos"),
				rs.getInt("quantidade"));
	}

	private static void fill(PreparedStatement ps, PresetData p) throws SQLException {
		ps.setString(1, p.name);
		ps.setInt(2, p.centroX);
		ps.setInt(3, p.centroY);
		ps.setInt(4, p.finalX);
		ps.setInt(5, p.finalY);
		ps.setInt(6, p.proxX);
		ps.setInt(7, p.proxY);
		ps.setInt(8, p.segundos);
		ps.setInt(9, p.quantidade);
	}
}
