package com.istorrent;

import com.istorrent.lib.TorrentData;
import com.istorrent.lib.TorrentManager;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;

class PeerInfo extends JComponent {

	private final JTable peerTable;
	private final JScrollPane scrollPane;

	public PeerInfo() {
		setLayout(new BorderLayout());
		PeerTableModel tableModel = new PeerTableModel();
		tableModel.addColumn("IP");
		tableModel.addColumn("Client");
		tableModel.addColumn("%");
		tableModel.addColumn("Down Speed");
		tableModel.addColumn("Up Speed");
		peerTable = new JTable(tableModel);

		peerTable.getTableHeader().setReorderingAllowed(false);
		peerTable.setFillsViewportHeight(true);
		peerTable.setFocusable(false);

		scrollPane = new JScrollPane(peerTable);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void setHash(String hash) {
		PeerTableModel model = (PeerTableModel) peerTable.getModel();
		model.setHash(hash);
	}
}

public class InfoPane extends JTabbedPane {

	private final PeerInfo peerInfo = new PeerInfo();
	private final InfoList infoTab = new InfoList(false);
	private final FileInfoTable fileInfo = new FileInfoTable();

	public InfoPane() {
		addTab("Info", infoTab);
		addTab("Files", fileInfo);
		addTab("Peers", peerInfo);
		setMinimumSize(new Dimension(600, 150));
		setPreferredSize(new Dimension(600, 200));
	}

	public void setHash(String hash) {
		peerInfo.setHash(hash);
		fileInfo.setHash(hash);
		infoTab.setHash(hash);
	}
}

class PeerTableModel extends AbstractTableModel {

	private final List<Object> columnList = new ArrayList<>();
	private List<Object[]> peerRows = null;
	private final TorrentManager torrentManager = TorrentManager.getInstance();
	private String hash = null;

	public PeerTableModel() {
		torrentManager.addObserver(new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				updateTorrentList();
			}
		});
	}

	@Override
	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
		if (peerRows == null || peerRows.size() < rowIndex - 1) {
			return null;
		}
		Object[] obj = peerRows.get(rowIndex);
		if (obj.length < columnIndex) {
			return null;
		}
		return obj[columnIndex];
	}

	@Override
	public synchronized int getColumnCount() {
		return columnList.size();
	}

	@Override
	public synchronized int getRowCount() {
		if (peerRows == null) {
			return 0;
		}
		return peerRows.size();
	}

	@Override
	public synchronized String getColumnName(int column) {
		return columnList.get(column).toString();
	}

	public synchronized void addColumn(Object columnName) {
		columnList.add(columnName);
	}

	public synchronized void setHash(String hash) {
		this.hash = hash;
		updateTorrentList();
	}

	private synchronized void updateTorrentList() {
		int oldSize = 0;
		int newSize = 0;
		if (peerRows != null) {
			oldSize = peerRows.size();
		}

		if (hash != null) {
			peerRows = null;
			TorrentData.PeerDataFlags pdf = new TorrentData.PeerDataFlags(columnList.toArray());
			TorrentData.PeerData data = (TorrentData.PeerData) torrentManager.getTorrentInfo(pdf, hash);
			peerRows = data.getPeerData();
		} else {
			peerRows = null;
		}

		if (peerRows != null) {
			newSize = peerRows.size();
		}

		if (oldSize != newSize) {
			fireTableDataChanged();
		} else if (newSize > 0) {
			fireTableRowsUpdated(0, newSize - 1);
		}
	}
}
