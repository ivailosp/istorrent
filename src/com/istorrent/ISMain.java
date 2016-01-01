package com.istorrent;

public class ISMain {

	public static void main(String[] args) throws Exception {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new com.istorrent.ISFrame().setVisible(true);
			}
		});
	}
}
