package com.istorrent;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.beans.*;
import java.util.prefs.*;

public class SideBarList extends JComponent {

	private JList<String> list;
	private final PropertyChangeSupport pcs;
	private Preferences prefs;
	public static final String SELECTED = "side.bar.selected";

	private final int MIN_SIZE_X = 100;
	private final int MIN_SIZE_Y = 500;
	private final int PREF_SIZE_X = 150;
	private final int PREF_SIZE_Y = 600;

	public SideBarList() {
		pcs = new PropertyChangeSupport(this);
		prefs = Preferences.userNodeForPackage(this.getClass());
		DefaultListModel<String> model = new DefaultListModel<>();
		list = new JList<>(model);
		model.addElement("Torrents");
		model.addElement("Downloading");
		model.addElement("Seeding");
		model.addElement("Completed");
		model.addElement("Active");
		model.addElement("Inactive");
		setMinimumSize(new Dimension(MIN_SIZE_X, MIN_SIZE_Y));
		setPreferredSize(new Dimension(PREF_SIZE_X, PREF_SIZE_Y));
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					pcs.firePropertyChange(SELECTED, null, list.getSelectedIndex());
					prefs.putInt(SELECTED, list.getSelectedIndex());
				}
			}
		});
		list.setSelectedIndex(prefs.getInt(SELECTED, 0));
		setLayout(new BorderLayout());
		add(list, BorderLayout.CENTER);
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
