package com.baojie.sched;

import java.io.IOException;

public class Start {

	public static void main(String[] args) throws IOException {
		final ServerManager sm = ServerManager.instance();
		sm.registerNotify(new ServerManager.Notification() {
			@Override
			public void showdown() {
				System.out.println("********* sched server stopped *********");
			}
		});
		sm.startServer();
	}
}
