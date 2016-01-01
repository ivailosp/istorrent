package com.istorrent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.*;
import java.beans.*;
import java.io.*;
import javax.swing.filechooser.*;
import com.istorrent.lib.*;

public class ISFrame extends JFrame {

	private ISMenuBar menuBar;

	private SideBarList sideBar;
	private JSplitPane jspMain;
	private JSplitPane rghTorrentPanel;
	private InfoPane rghTorrentStatusPanel;

	private TorrentTable mainTorrentList;
	private JPopupMenu menuRTable;
	private JMenuItem menuRItem;

	private Preferences prefs;
	private DividerLocationFieldListener dividerListener;
	private SideBarListener sideBarListener;
	private final TorrentManager torrentManager = TorrentManager.getInstance();

	public ISFrame() {
		initComponents();
	}

	private void clearWindow() {
		try {
			prefs.clear();
		} catch (BackingStoreException ex) {
			//ex.printStackTrace();
		}
		setLocation(0, 0);
		pack();
		rghTorrentPanel.setDividerLocation(-1);
		jspMain.setDividerLocation(-1);
	}

	private class SideBarListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			mainTorrentList.setTorrentMode((int) e.getNewValue());
		}
	}

	private class DividerLocationFieldListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			switch (propertyName) {
				case "dividerLocation":
					if (e.getSource() == jspMain) {
						prefs.putInt("jspMain dividerLocation", (Integer) e.getNewValue());
					} else if (e.getSource() == rghTorrentPanel) {
						prefs.putInt("rghTorrentPanel dividerLocation", (Integer) e.getNewValue());
					}
					break;
				case TorrentTable.SELECTED:
					rghTorrentStatusPanel.setHash((String) e.getNewValue());
					break;
				case ISMenuBar.RESET_WINDOW:
					clearWindow();
					break;
				case ISMenuBar.ADD_TORRENT:
					try {
						JFileChooser chooser = new JFileChooser(new File("."));
						FileNameExtensionFilter filter = new FileNameExtensionFilter("Torrents(*.torrent)", "torrent");
						chooser.setFileFilter(filter);
						int returnVal = chooser.showOpenDialog(ISFrame.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								NewFileDialog ntd = new NewFileDialog(ISFrame.this, "Add torrent...", chooser.getSelectedFile());
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(ISFrame.this, "Bad torrent file!");
								//ex.printStackTrace();
							}
						}

					} catch (Exception ex) {
						//ex.printStackTrace();
					}
					break;

				case ISMenuBar.ABOUT:
					JOptionPane.showMessageDialog(ISFrame.this,
							"istorrent by Ivaylo Spasov",
							"About",
							JOptionPane.PLAIN_MESSAGE);
					break;
				case ISMenuBar.PREFERENCES:
				case ISMenuBar.ADD_TORRENT_URL:
					JOptionPane.showMessageDialog(ISFrame.this,
							"Not implemented yet",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
					break;
			}
		}
	}

	private void initComponents() {
		setTitle("istorrent");
		prefs = Preferences.userNodeForPackage(this.getClass());
		dividerListener = new DividerLocationFieldListener();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		torrentManager.setCallback(SqlManager.getInstance());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				prefs.putInt("TJFrame width", getWidth());
				prefs.putInt("TJFrame height", getHeight());
				prefs.putInt("TJFrame location x", getLocationOnScreen().x);
				prefs.putInt("TJFrame location y", getLocationOnScreen().y);
				torrentManager.shutdown();
			}
		});

		menuBar = new ISMenuBar();
		menuBar.addPropertyChangeListener(dividerListener);
		setJMenuBar(menuBar);

		rghTorrentStatusPanel = new InfoPane();

		mainTorrentList = new TorrentTable();
		mainTorrentList.addPropertyChangeListener(dividerListener);

		sideBar = new SideBarList();
		sideBarListener = new SideBarListener();
		sideBar.addPropertyChangeListener(sideBarListener);

		rghTorrentPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, mainTorrentList, rghTorrentStatusPanel);
		rghTorrentPanel.setMinimumSize(new Dimension(500, 400));
		rghTorrentPanel.setPreferredSize(new Dimension(800, 600));
		rghTorrentPanel.setResizeWeight(1.);
		rghTorrentPanel.addPropertyChangeListener(dividerListener);
		rghTorrentPanel.setDividerLocation(prefs.getInt("rghTorrentPanel dividerLocation", rghTorrentPanel.getDividerLocation()));

		jspMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, sideBar, rghTorrentPanel);
		jspMain.setDividerLocation(prefs.getInt("jspMain dividerLocation", jspMain.getDividerLocation()));
		jspMain.addPropertyChangeListener(dividerListener);
		add(jspMain);

		setMinimumSize(new Dimension(700, 500));
		pack();
		setSize(prefs.getInt("TJFrame width", getWidth()), prefs.getInt("TJFrame height", getHeight()));
		setLocation(prefs.getInt("TJFrame location x", 0), prefs.getInt("TJFrame location y", 0));
	}
}
