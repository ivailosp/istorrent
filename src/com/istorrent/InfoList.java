package com.istorrent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import java.awt.Dimension;
import com.istorrent.lib.TorrentData;
import com.istorrent.lib.TorrentManager;

public class InfoList extends JComponent {

	private final JLabel labelSaveAsLabel;
	private final JLabel totalSizeLabel;
	private final JLabel creationDateLabel;
	private final JLabel hashLabel;
	private final JLabel commentLabel;
	private final TorrentManager torrentManager;

	private class makeConstraint {

		private JComponent lftComp = null;
		private JComponent rghComp = null;
		private final SpringLayout layout;
		private final JComponent parent;
		private final int offset;

		public makeConstraint(SpringLayout layout, JComponent parent, int offset) {
			this.parent = parent;
			this.offset = offset;
			this.layout = layout;
		}

		public void updateConstraint(JComponent left, JComponent right) {
			parent.add(left);
			parent.add(right);
			if (lftComp == null && rghComp == null) {
				layout.putConstraint(SpringLayout.WEST, left, offset,
						SpringLayout.WEST, parent);
				layout.putConstraint(SpringLayout.NORTH, left, offset,
						SpringLayout.NORTH, parent);
				layout.putConstraint(SpringLayout.WEST, right, offset,
						SpringLayout.EAST, left);
				layout.putConstraint(SpringLayout.NORTH, right, offset,
						SpringLayout.NORTH, parent);
			} else {
				layout.putConstraint(SpringLayout.WEST, left, offset,
						SpringLayout.WEST, parent);
				layout.putConstraint(SpringLayout.NORTH, left, offset,
						SpringLayout.SOUTH, lftComp);
				layout.putConstraint(SpringLayout.WEST, right, offset,
						SpringLayout.EAST, left);
				layout.putConstraint(SpringLayout.NORTH, right, offset,
						SpringLayout.SOUTH, rghComp);
			}
			lftComp = left;
			rghComp = right;
		}
	}

	public InfoList() {
		this(false);
	}

	public InfoList(boolean shortInfo) {
		torrentManager = TorrentManager.getInstance();
		SpringLayout layout = new SpringLayout();
		makeConstraint mkconst = new makeConstraint(layout, this, 5);

		JLabel label;
		labelSaveAsLabel = new JLabel();
		if (shortInfo == false) {
			label = new JLabel("Save as:");
			mkconst.updateConstraint(label, labelSaveAsLabel);
		}
		label = new JLabel("Total size:");
		totalSizeLabel = new JLabel();
		mkconst.updateConstraint(label, totalSizeLabel);

		label = new JLabel("Creation date:");
		creationDateLabel = new JLabel();
		mkconst.updateConstraint(label, creationDateLabel);

		label = new JLabel("Hash:");
		hashLabel = new JLabel();
		mkconst.updateConstraint(label, hashLabel);

		label = new JLabel("Comment:");
		commentLabel = new JLabel();
		mkconst.updateConstraint(label, commentLabel);

		setLayout(layout);
		setPreferredSize(new Dimension(400, 120));
	}

	public void setHash(String hash) {
		if (hash != null) {
			labelSaveAsLabel.setText(torrentManager.getTorrentInfo(TorrentData.FULL_PATH, hash).toString());
			totalSizeLabel.setText(torrentManager.getTorrentInfo(TorrentData.SIZE, hash).toString());
			creationDateLabel.setText(torrentManager.getTorrentInfo(TorrentData.CREATION_DATE, hash).toString());
			hashLabel.setText(torrentManager.getTorrentInfo(TorrentData.HASH, hash).toString());
			try{
				commentLabel.setText(torrentManager.getTorrentInfo(TorrentData.COMMENT, hash).toString());
			} catch(Exception e) {
				commentLabel.setText(null);
			}
		} else {
			labelSaveAsLabel.setText(null);
			totalSizeLabel.setText(null);
			creationDateLabel.setText(null);
			hashLabel.setText(null);
			commentLabel.setText(null);
		}
	}

	public void setTorrentData(TorrentData data) {
		if (data != null) {
			labelSaveAsLabel.setText(data.getInfo(TorrentData.FULL_PATH).toString());
			totalSizeLabel.setText(data.getInfo(TorrentData.SIZE).toString());
			creationDateLabel.setText(data.getInfo(TorrentData.CREATION_DATE).toString());
			hashLabel.setText(data.getInfo(TorrentData.HASH).toString());
			try {
				commentLabel.setText(data.getInfo(TorrentData.COMMENT).toString());
			}catch(Exception e) {
				commentLabel.setText(null);
			}
		} else {
			labelSaveAsLabel.setText(null);
			totalSizeLabel.setText(null);
			creationDateLabel.setText(null);
			hashLabel.setText(null);
			commentLabel.setText(null);
		}
	}
}
