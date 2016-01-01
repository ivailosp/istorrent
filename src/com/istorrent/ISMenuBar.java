package com.istorrent;

import javax.swing.*;
import java.awt.event.*;
import java.beans.*;

public class ISMenuBar extends JMenuBar {

	private final JMenu menuFile;
	private final JMenu menuOptions;
	private final JMenu menuHelp;
	private final JMenuItem menuItemAddTorrent;
	//private JMenuItem menuItemAddTorrentUrl;
	private final JMenuItem menuItemResetWindows;
	//private JMenuItem menuItemPreferences;
	private final JMenuItem menuItemExit;
	private final JMenuItem menuItemAbout;
	private final ActionMenu itemAction;

	public final static String ADD_TORRENT = "Add Torrent...";
	public final static String ADD_TORRENT_URL = "Add Torrent from URL...";
	public final static String RESET_WINDOW = "Reset Window";
	public final static String PREFERENCES = "Preferences";
	public final static String ABOUT = "About";

	private final PropertyChangeSupport pcs;

	private class ActionMenu implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			ISFrame frame = (ISFrame) SwingUtilities.getRoot(ISMenuBar.this);
			JMenuItem item = (JMenuItem) event.getSource();
			pcs.firePropertyChange(item.getActionCommand(), null, true);
		}
	}

	public ISMenuBar() {
		pcs = new PropertyChangeSupport(this);
		itemAction = new ActionMenu();
		menuFile = new JMenu("File");
		menuItemAddTorrent = new JMenuItem(ADD_TORRENT);
		menuItemAddTorrent.addActionListener(itemAction);
		menuFile.add(menuItemAddTorrent);

		//menuItemAddTorrentUrl = new JMenuItem(ADD_TORRENT_URL);
		//menuItemAddTorrentUrl.addActionListener(itemAction);
		//menuFile.add(menuItemAddTorrentUrl);
		menuFile.addSeparator();
		menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JFrame frame = (JFrame) SwingUtilities.getRoot(ISMenuBar.this);
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		menuFile.add(menuItemExit);
		add(menuFile);

		menuOptions = new JMenu("Options");
		menuItemResetWindows = new JMenuItem(RESET_WINDOW);
		menuItemResetWindows.addActionListener(itemAction);
		menuOptions.add(menuItemResetWindows);
		//menuOptions.addSeparator();
		//menuItemPreferences = new JMenuItem(PREFERENCES);
		//menuItemPreferences.addActionListener(itemAction);
		//menuOptions.add(menuItemPreferences);
		add(menuOptions);

		menuHelp = new JMenu("Help");
		menuItemAbout = new JMenuItem("About");
		menuItemAbout.addActionListener(itemAction);
		menuHelp.add(menuItemAbout);
		add(menuHelp);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
}
