package com.istorrent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;
import java.awt.Dimension;
import com.istorrent.lib.TorrentData;
import com.istorrent.lib.TorrentManager;

public class FileInfoTable extends JComponent {

	private final JScrollPane scrollPane;
	private final JTable filesTable;
	private TorrentManager torrentManager;

	public FileInfoTable() {
		torrentManager = TorrentManager.getInstance();
		setLayout(new BorderLayout());
		DefaultTableModel dtm;
		dtm = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		filesTable = new JTable(dtm);
		dtm.addColumn("Files");
		scrollPane = new JScrollPane(filesTable);
		filesTable.setFillsViewportHeight(true);
		filesTable.setFocusable(false);
		add(scrollPane, BorderLayout.CENTER);
		setPreferredSize(new Dimension(400, 120));
	}

	public void setHash(String hash) {
		DefaultTableModel dtm = (DefaultTableModel) filesTable.getModel();
		dtm.setRowCount(0);
		if (hash != null) {
			List<String> files = torrentManager.getFilenamesFor(hash);
			for(String file : files) {
				dtm.addRow(new Object[]{file});
			}
		}
	}

	public void setTorrentData(TorrentData data) {
		DefaultTableModel dtm = (DefaultTableModel) filesTable.getModel();
		if (data != null) {
			List<String> files = data.getFilenames();
			for(String file : files) {
				dtm.addRow(new Object[]{file});
			}
		}
	}
}
