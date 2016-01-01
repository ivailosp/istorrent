package com.istorrent;

import java.sql.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import com.istorrent.lib.TorrentData;
import com.istorrent.lib.TorrentManager.TorrentManagerCallback;

public class SqlManager implements TorrentManagerCallback {

	private static SqlManager instance = null;
	private Connection c = null;
	public final static String table = "TORRENTS";

	public static SqlManager getInstance() {
		if (instance == null) {
			instance = new SqlManager();
		}
		return instance;
	}

	protected SqlManager() {
		try {
			Class.forName("org.sqlite.JDBC");
			Statement stmt;
			c = DriverManager.getConnection("jdbc:sqlite:torrents.db");

			stmt = c.createStatement();

			String sql = "create table if not exists " + table
					+ "(SHA TEXT PRIMARY KEY        NOT NULL, "
					+ "STATUS            INTEGER    NOT NULL, "
					+ "PERCENT           REAL       NOT NULL, "
					+ "ADDED             DATETIME   NOT NULL, "
					+ "RAWDATA           BLOB	     NOT NULL, "
					+ "SAVEDIR           TEXT       NOT NULL);";

			stmt.executeUpdate(sql);
		} catch (ClassNotFoundException | SQLException e) {
			//e.printStackTrace();
		}
	}

	@Override
	public void addTorrent(byte[] data, String sha, String savedir, Timestamp added) {
		if (c == null) {
			return;
		}
		try {
			PreparedStatement preStmt;
			TorrentSqlObject obj = new TorrentSqlObject(sha, 0, 0.f, added, data, savedir);
			preStmt = c.prepareStatement(obj.getPrepareStatementStr(table));
			obj.setPreparedStatement(preStmt);
			preStmt.executeUpdate();
			preStmt.close();
		} catch (SQLException | IOException e) {
			//e.printStackTrace();
		}
	}

	@Override
	public void removeTorrent(String sha) {
		if (c == null) {
			return;
		}
		try {
			Statement stmt;
			stmt = c.createStatement();
			String sql = "DELETE from " + table + " where SHA='" + sha + "';";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	@Override
	public void updateTorrent(String sha, int status, Float percent) {
		if (c == null) {
			return;
		}
		try {
			Statement stmt;
			stmt = c.createStatement();
			String sql = "UPDATE " + table + " SET STATUS = " + status
					+ ", PERCENT = " + percent + " where SHA='" + sha + "';";
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	@Override
	public Map<String, TorrentData> getAllTorrents() {
		Map<String, TorrentData> ret = new HashMap<>();
		if (c == null) {
			return ret;
		}
		try {
			Statement stmt;
			ResultSet rs;
			stmt = c.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + table);
			TorrentSqlObject obj = new TorrentSqlObject();
			while (rs.next()) {
				obj.updateData(rs);
				TorrentData td = new TorrentData(InetAddress.getLocalHost(),
						obj.getRowdatabyte(), new File(obj.getSaveDir()), obj.getAdded());
				ret.put(obj.getSha(), td);
				td.setLolcalState(obj.getStatus());
				td.setLocalCompletion(obj.getPercent());
				if (obj.getStatus() > 0 && obj.getStatus() < 4) {
					td.start();
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException | IOException e) {
			//e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void close() {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		c = null;
	}
}

class TorrentSqlObject {

	private String sha;
	private int status;
	private float percent;
	private Timestamp added;
	private byte[] rowdata;
	private String savedir;

	public TorrentSqlObject() {
	}

	public TorrentSqlObject(String sha, int status, float percent, Timestamp added, byte[] rowdata, String savedir) {
		this.sha = sha;
		this.status = status;
		this.percent = percent;
		this.added = added;
		this.rowdata = rowdata;
		this.savedir = savedir;
	}

	public String getPrepareStatementStr(String table) {
		return "INSERT INTO " + table
				+ " (SHA, STATUS, PERCENT, ADDED, RAWDATA, SAVEDIR) VALUES (?, ?, ?, ?, ?, ?)";
	}

	public void setPreparedStatement(PreparedStatement preStmt) throws SQLException, IOException {
		preStmt.setString(1, sha);
		preStmt.setInt(2, status);
		preStmt.setFloat(3, percent);
		preStmt.setTimestamp(4, added);
		preStmt.setBytes(5, rowdata);
		preStmt.setString(6, savedir);
	}

	public void updateData(ResultSet rs) throws SQLException {
		sha = rs.getString("SHA");
		status = rs.getInt("STATUS");
		percent = rs.getFloat("PERCENT");
		added = rs.getTimestamp("ADDED");
		rowdata = rs.getBytes("RAWDATA");
		savedir = rs.getString("SAVEDIR");
	}

	public String getSha() {
		return sha;
	}

	public int getStatus() {
		return status;
	}

	public float getPercent() {
		return percent;
	}

	public Timestamp getAdded() {
		return added;
	}

	public String getSaveDir() {
		return savedir;
	}

	public byte[] getRowdatabyte() {
		return rowdata;
	}
}
