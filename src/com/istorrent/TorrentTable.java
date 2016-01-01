package com.istorrent;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.*;
import java.beans.*;
import java.util.*;
import com.istorrent.lib.*;
import java.sql.Timestamp;

public class TorrentTable extends JComponent {

	private final JTable torrentTable;
	private final JScrollPane scrollPane;
	private final Preferences prefs;
	private TorrentManager torrentManager;
	private final PropertyChangeSupport pcs;

	public static final String SELECTED = "torrent.row.selected";

	private final String NAME = "Name";
	private final String SIZE = "Size";
	private final String STATUS = "Status";
	private final String DOWN_SPEED = "Down Speed";
	private final String UP_SPEED = "Up Speed";
	private final String ADDED = "Added";

	private final int MIN_SIZE_X = 600;
	private final int MIN_SIZE_Y = 200;
	private final int PREF_SIZE_X = 600;
	private final int PREF_SIZE_Y = 200;

	private final int NAME_MIN_WIDTH = 20;
	private final int NAME_PREF_WIDTH = 286;
	private final int SIZE_MIN_WIDTH = 20;
	private final int SIZE_PREF_WIDTH = 76;
	private final int STATUS_MIN_WIDTH = 20;
	private final int STATUS_PREF_WIDTH = 130;
	private final int DOWN_SPEED_MIN_WIDTH = 20;
	private final int DOWN_SPEED_PREF_WIDTH = 78;
	private final int UP_SPEED_MIN_WIDTH = 20;
	private final int UP_SPEED_PREF_WIDTH = 78;
	private final int ADDED_MIN_WIDTH = 20;
	private final int ADDED_PREF_WIDTH = 144;

	private JTable intTable() {
		torrentManager = TorrentManager.getInstance();
		TorrentTableModel tableModel = new TorrentTableModel();
		tableModel.addColumn(NAME);
		tableModel.addColumn(SIZE);
		tableModel.addColumn(STATUS);
		tableModel.addColumn(DOWN_SPEED);
		tableModel.addColumn(UP_SPEED);
		tableModel.addColumn(ADDED);

		final JTable table;
		table = new JTable(tableModel);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFillsViewportHeight(true);
		table.setFocusable(false);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		ProgressBarRenderer pbr = new ProgressBarRenderer(0, 100);
		pbr.setStringPainted(true);
		pbr.setBorderPainted(false);
		table.setDefaultRenderer(TorrentData.StateType.class, pbr);

		PreferredWidthFieldListener preferreListener = new PreferredWidthFieldListener();
		TableColumn tc;
		tc = table.getColumn(NAME);
		tc.addPropertyChangeListener(preferreListener);
		tc = table.getColumn(SIZE);
		tc.addPropertyChangeListener(preferreListener);
		tc.setCellRenderer(centerRenderer);
		tc = table.getColumn(STATUS);
		tc.addPropertyChangeListener(preferreListener);
		tc.setCellRenderer(pbr);
		tc = table.getColumn(DOWN_SPEED);
		tc.addPropertyChangeListener(preferreListener);
		tc.setCellRenderer(centerRenderer);
		tc = table.getColumn(UP_SPEED);
		tc.addPropertyChangeListener(preferreListener);
		tc.setCellRenderer(centerRenderer);
		tc = table.getColumn(ADDED);
		tc.addPropertyChangeListener(preferreListener);
		tc.setCellRenderer(centerRenderer);

		table.getSelectionModel().addListSelectionListener(new TableSelection());

		return table;
	}

	private class TableSelection implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {
			TorrentTableModel model = (TorrentTableModel) torrentTable.getModel();
			String hashNew = null;
			if (torrentTable.getSelectedRow() != -1) {
				hashNew = model.getTorrentHash(torrentTable.getSelectedRow());
				pcs.firePropertyChange(SELECTED, null, hashNew);
			} else {
				pcs.firePropertyChange(SELECTED, "", hashNew);
			}
		}
	}

	private class PreferredWidthFieldListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if ("preferredWidth".equals(propertyName)) {
				TableColumn tc = (TableColumn) e.getSource();
				if (!prefs.getBoolean(tc.getHeaderValue() + " hide", false)) {
					prefs.putInt((String) tc.getHeaderValue(), (Integer) e.getNewValue());
				}
			}
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public TorrentTable() {
		pcs = new PropertyChangeSupport(this);
		prefs = Preferences.userNodeForPackage(this.getClass());
		setLayout(new BorderLayout());
		torrentTable = intTable();
		scrollPane = new JScrollPane(torrentTable);
		setMinimumSize(new Dimension(MIN_SIZE_X, MIN_SIZE_Y));
		setPreferredSize(new Dimension(PREF_SIZE_X, PREF_SIZE_Y));
		add(scrollPane, BorderLayout.CENTER);

		initSettings();
		initColumnMenu();
	}

	private class MenuItemAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			TableColumn tc;
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
			tc = torrentTable.getColumn(item.getName());
			prefs.putBoolean(item.getName() + " hide", !item.getState());
			initSettings();
		}
	}

	private class MenuTorrentItemAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JMenuItem item = (JMenuItem) event.getSource();
			int[] selection = torrentTable.getSelectedRows();
			ArrayList<String> hashList = new ArrayList<>();
			TorrentTableModel model = (TorrentTableModel) torrentTable.getModel();
			for (int i = 0; i < selection.length; i++) {
				String hash = model.getTorrentHash(selection[i]);
				if (hash != null) {
					hashList.add(hash);
				}
			}
			switch (item.getActionCommand()) {
				case "Start":
					for (String hash : hashList)
						torrentManager.startTorrent(hash);
					break;
				case "Stop":
					for (String hash : hashList)
						torrentManager.stopTorrent(hash);
					break;
				case "Remove":
					for (String hash : hashList)
						torrentManager.removeTorrent(hash);
					break;
			}
		}
	}

	private JMenuItem makeColumnItem(String name) {
		TableColumn tc;
		JMenuItem item;
		tc = torrentTable.getColumn(name);
		boolean hide = prefs.getBoolean(tc.getHeaderValue() + " hide", false);
		item = new JCheckBoxMenuItem((String) tc.getHeaderValue(), !hide);
		item.setName((String) tc.getHeaderValue());
		return item;
	}

	private void addItemMenu(String name, ActionListener action, JPopupMenu menu) {
		JMenuItem item;
		item = makeColumnItem(name);
		item.addActionListener(action);
		menu.add(item);
	}

	private void addTorrentItemMenu(String name, ActionListener action, JPopupMenu menu) {
		JMenuItem item;
		item = new JMenuItem(name);
		item.addActionListener(action);
		menu.add(item);
	}

	private void initColumnMenu() {
		final JPopupMenu menuColumn;
		JMenuItem item;
		ActionListener itemAction = new MenuItemAction();
		menuColumn = new JPopupMenu();

		addItemMenu(NAME, itemAction, menuColumn);
		addItemMenu(SIZE, itemAction, menuColumn);
		addItemMenu(STATUS, itemAction, menuColumn);
		addItemMenu(DOWN_SPEED, itemAction, menuColumn);
		addItemMenu(UP_SPEED, itemAction, menuColumn);
		addItemMenu(ADDED, itemAction, menuColumn);

		menuColumn.addSeparator();

		item = new JMenuItem("Reset");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				resetSettings();
			}
		});
		menuColumn.add(item);

		torrentTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					menuColumn.show(e.getComponent(), e.getX(), e.getY());
				} else {
					JTableHeader jth = (JTableHeader) e.getSource();
					Cursor cur = jth.getCursor();
					if (cur.getType() == Cursor.DEFAULT_CURSOR) {
						int col = torrentTable.columnAtPoint(e.getPoint());
						String name = torrentTable.getColumnName(col);
						TorrentTableModel model = (TorrentTableModel) torrentTable.getModel();
						model.setSort(name);
					}
					//System.out.println("Column index selected " + col + " " + name);
				}
			}
		});

		final JPopupMenu menuTorrent = new JPopupMenu();
		itemAction = new MenuTorrentItemAction();

		addTorrentItemMenu("Start", itemAction, menuTorrent);
		addTorrentItemMenu("Stop", itemAction, menuTorrent);
		addTorrentItemMenu("Remove", itemAction, menuTorrent);
		torrentTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int[] selection = torrentTable.getSelectedRows();
					int selectedRow = torrentTable.rowAtPoint(e.getPoint());
					for (int i = 0; i < selection.length; ++i) {
						if (selectedRow == selection[i]) {
							menuTorrent.show(e.getComponent(), e.getX(), e.getY());
							return;
						}
					}
					if (selectedRow >= 0) {
						torrentTable.setRowSelectionInterval(selectedRow, selectedRow);
					}
					menuTorrent.show(e.getComponent(), e.getX(), e.getY());
				} else if (SwingUtilities.isLeftMouseButton(e)) {
					int row = torrentTable.rowAtPoint(e.getPoint());
					if (row < 0) {
						row = torrentTable.getRowCount();
						if (row > 0) {
							torrentTable.setRowSelectionInterval(row - 1, row - 1);
						}
						torrentTable.clearSelection();
					}
				}
			}
		});
	}

	private void resetSettings() {
		try {
			prefs.clear();
		} catch (BackingStoreException ex) {
			//ex.printStackTrace();
		}
		initSettings();
	}

	private void setColumnSetting(String name, int minWidth, int prefWidth) {
		TableColumn tc;
		tc = torrentTable.getColumn(name);
		if (prefs.getBoolean(tc.getHeaderValue() + " hide", false)) {
			tc.setMinWidth(0);
			tc.setPreferredWidth(0);
		} else {
			tc.setPreferredWidth(prefs.getInt((String) tc.getHeaderValue(), prefWidth));
			tc.setMinWidth(prefs.getInt(tc.getHeaderValue() + " min", minWidth));
		}
	}

	private void initSettings() {
		setColumnSetting(NAME, NAME_MIN_WIDTH, NAME_PREF_WIDTH);
		setColumnSetting(SIZE, SIZE_MIN_WIDTH, SIZE_PREF_WIDTH);
		setColumnSetting(STATUS, STATUS_MIN_WIDTH, STATUS_PREF_WIDTH);
		setColumnSetting(DOWN_SPEED, DOWN_SPEED_MIN_WIDTH, DOWN_SPEED_PREF_WIDTH);
		setColumnSetting(UP_SPEED, UP_SPEED_MIN_WIDTH, UP_SPEED_PREF_WIDTH);
		setColumnSetting(ADDED, ADDED_MIN_WIDTH, ADDED_PREF_WIDTH);
	}

	public void setTorrentMode(int mode) {
		TorrentTableModel model = (TorrentTableModel) torrentTable.getModel();
		model.setTorrentMode(mode);
	}
}

class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

	public ProgressBarRenderer(int min, int max) {
		super(min, max);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setValue((int) ((TorrentData.StateType) value).pr.value);
		setString(value.toString());
		return this;
	}
}

class TorrentTableModel extends AbstractTableModel {

	private final java.util.List<Object> columnList = new ArrayList<>();
	private java.util.List<Object[]> torrentRows = null;
	private final Preferences prefs = Preferences.userNodeForPackage(SideBarList.class);
	private final TorrentManager torrentManager = TorrentManager.getInstance();
	private int mode = 0;
	private int sortBy = -1;
	boolean reverseOrder = false;

	public TorrentTableModel() {
		mode = prefs.getInt(SideBarList.SELECTED, 0);
		sortBy = prefs.getInt("sortBy", -1);
		reverseOrder = prefs.getBoolean("reverseOrder", false);
		updateTorrentList(false);
		torrentManager.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				updateTorrentList(false);
			}
		});
	}

	@Override
	public synchronized Object getValueAt(int rowIndex, int columnIndex) {
		if (torrentRows == null || torrentRows.size() < rowIndex - 1) {
			return null;
		}
		Object[] obj = torrentRows.get(rowIndex);
		if (obj.length - 1 <= columnIndex) {
			return null;
		}
		return obj[columnIndex + 1];
	}

	@Override
	public synchronized int getColumnCount() {
		return columnList.size();
	}

	@Override
	public synchronized int getRowCount() {
		if (torrentRows == null) {
			return 0;
		}
		return torrentRows.size();
	}

	@Override
	public synchronized String getColumnName(int column) {
		return columnList.get(column).toString();
	}

	public synchronized void addColumn(Object columnName) {
		columnList.add(columnName);
		updateTorrentList(false);
	}

	public synchronized String getTorrentHash(int index) {
		if (index < torrentRows.size() && torrentRows.get(index).length >= 0) {
			return (String) torrentRows.get(index)[0];
		} else {
			return null;
		}
	}

	public synchronized void setTorrentMode(int mode) {
		this.mode = mode;
		updateTorrentList(true);
	}

	public synchronized void setSort(String tableName) {

		for (int i = 0; i < columnList.size(); ++i) {
			if (tableName.equals(columnList.get(i))) {
				if (i == sortBy) {
					reverseOrder = !reverseOrder;
				} else {
					sortBy = i;
					reverseOrder = false;
				}
				prefs.putInt("sortBy", sortBy);
				prefs.putBoolean("reverseOrder", reverseOrder);
				updateTorrentList(true);
				return;
			}
		}
		sortBy = -1;
		reverseOrder = false;
		prefs.putInt("sortBy", sortBy);
		prefs.putBoolean("reverseOrder", reverseOrder);
		updateTorrentList(true);
	}

	private synchronized void updateTorrentList(boolean forced) {
		int oldSize = 0;
		int newSize = 0;
		if (torrentRows != null) {
			oldSize = torrentRows.size();
		}

		torrentRows = torrentManager.getTorrentInfo(columnList.toArray(), mode);

		if (torrentRows != null && torrentRows.size() > 0) {
			if (sortBy != -1 && sortBy + 1 < torrentRows.get(0).length) {
				try {
					Collections.sort(torrentRows, new Comparator<Object[]>() {
						@Override
						public int compare(final Object[] entry1, final Object[] entry2) {
							if (entry1[sortBy + 1] instanceof Timestamp) {
								final Timestamp time1 = (Timestamp) entry1[sortBy + 1];
								final Timestamp time2 = (Timestamp) entry2[sortBy + 1];
								if (reverseOrder == false) {
									return time1.compareTo(time2);
								} else {
									return time2.compareTo(time1);
								}
							} else if (entry1[sortBy + 1] instanceof TorrentData.SpeedType) {
								final TorrentData.SpeedType speed1 = (TorrentData.SpeedType) entry1[sortBy + 1];
								final TorrentData.SpeedType speed2 = (TorrentData.SpeedType) entry2[sortBy + 1];
								if (reverseOrder == false) {
									return speed1.compareTo(speed2);
								} else {
									return speed2.compareTo(speed1);
								}
							} else if (entry1[sortBy + 1] instanceof TorrentData.SizeType) {
								final TorrentData.SizeType size1 = (TorrentData.SizeType) entry1[sortBy + 1];
								final TorrentData.SizeType size2 = (TorrentData.SizeType) entry2[sortBy + 1];
								if (reverseOrder == false) {
									return size1.compareTo(size2);
								} else {
									return size2.compareTo(size1);
								}
							} else if (entry1[sortBy + 1] instanceof TorrentData.StateType) {
								final TorrentData.StateType state1 = (TorrentData.StateType) entry1[sortBy + 1];
								final TorrentData.StateType state2 = (TorrentData.StateType) entry2[sortBy + 1];
								if (reverseOrder == false) {
									return state1.compareTo(state2);
								} else {
									return state2.compareTo(state1);
								}
							} else if (entry1[sortBy + 1] instanceof String) {
								final String str1 = (String) entry1[sortBy + 1];
								final String str2 = (String) entry2[sortBy + 1];
								if (reverseOrder == false) {
									return str1.compareTo(str2);
								} else {
									return str2.compareTo(str1);
								}
							}
							return 0;
						}
					});
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			newSize = torrentRows.size();
		}
		if (oldSize != newSize || forced == true) {
			fireTableDataChanged();
		} else if (newSize > 0) {
			fireTableRowsUpdated(0, newSize - 1);
		}
	}
}
