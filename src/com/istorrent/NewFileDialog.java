package com.istorrent;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.net.InetAddress;
import com.istorrent.lib.TorrentData;
import com.istorrent.lib.TorrentManager;

class SaveAs extends JPanel {

	public JTextField textField;
	public JButton button;

	public SaveAs() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		File currDir = new File(".");
		try {
			textField = new JTextField(currDir.getCanonicalPath(), 30);
		} catch (IOException e) {
			textField = new JTextField("", 30);
		}
		button = new JButton("...");
		add(textField);
		add(button);
		setBorder(BorderFactory.createTitledBorder("Save As"));
	}

	public void addActionListener(ActionListener l) {
		button.addActionListener(l);
	}
}

class StartOnAdd extends JPanel {

	public JCheckBox checkStart;
	private final JLabel startLabel;

	public StartOnAdd() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		checkStart = new JCheckBox();
		checkStart.setSelected(true);
		add(checkStart);
		startLabel = new JLabel("Start torrent");
		add(startLabel);
	}
}

class SaveButtons extends JPanel {

	public JButton okButton;
	public JButton canselButton;

	public SaveButtons() {
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		okButton = new JButton("Ok");
		add(okButton);
		canselButton = new JButton("Cancel");
		add(canselButton);
	}

	public void addActionListener(ActionListener l) {
		okButton.addActionListener(l);
		canselButton.addActionListener(l);
	}
}

public class NewFileDialog extends JDialog {

	private SaveAs saveAs;
	private StartOnAdd startOn;
	private InfoList info;
	private FileInfoTable fileInfo;
	private SaveButtons buttons;
	private File torrent;

	public NewFileDialog(JFrame frame, String title, File torrent) throws IllegalArgumentException {
		super(frame, title);
		NewFileAction al = new NewFileAction();
		JPanel panel = new JPanel();
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		saveAs = new SaveAs();
		saveAs.addActionListener(al);
		add(saveAs);
		startOn = new StartOnAdd();
		add(startOn);
		info = new InfoList(true);
		panel.add(info);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createTitledBorder("Torrent Contents"));
		add(panel);
		fileInfo = new FileInfoTable();
		add(fileInfo);
		buttons = new SaveButtons();
		buttons.addActionListener(al);
		add(buttons);
		pack();
		this.torrent = torrent;
		try {
			TorrentData td = new TorrentData(InetAddress.getLocalHost(),
					Files.readAllBytes(torrent.toPath()), new File("."), null);
			setTorrentData(td);
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	private void setTorrentData(TorrentData data) {
		fileInfo.setTorrentData(data);
		info.setTorrentData(data);
	}

	private class NewFileAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == buttons.okButton) {
				try {
					String hash = TorrentManager.getInstance().addNewTorrent(torrent, new File(saveAs.textField.getText()));
					if (startOn.checkStart.isSelected() == true) {
						TorrentManager.getInstance().startTorrent(hash);
					}
				} catch (IllegalArgumentException ex) {
					JOptionPane.showMessageDialog(NewFileDialog.this, "Torrent already exists!");
					//ex.printStackTrace();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(NewFileDialog.this, "Bad torrent file!");
					//ex.printStackTrace();
				}
				dispatchEvent(new WindowEvent(NewFileDialog.this, WindowEvent.WINDOW_CLOSING));
			} else if (e.getSource() == buttons.canselButton) {
				dispatchEvent(new WindowEvent(NewFileDialog.this, WindowEvent.WINDOW_CLOSING));
			} else if (e.getSource() == saveAs.button) {
				JFileChooser chooser_dir = new JFileChooser(new File(saveAs.textField.getText()));
				chooser_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal;
				returnVal = chooser_dir.showOpenDialog(NewFileDialog.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						saveAs.textField.setText(chooser_dir.getSelectedFile().getCanonicalPath());
					} catch (IOException ex) {
						saveAs.textField.setText("");
					}
				}
			}
		}
	}
}
