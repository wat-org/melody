package com.wat.melody.common.telnet.test;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.telnet.ITelnetConnectionDatas;
import com.wat.melody.common.telnet.ITelnetUserDatas;
import com.wat.melody.common.telnet.exception.TelnetSessionException;
import com.wat.melody.common.telnet.impl.TelnetConnectionDatas;
import com.wat.melody.common.telnet.impl.TelnetSession;
import com.wat.melody.common.telnet.impl.TelnetUserDatas;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;

public class TelnetTest {

	public static void main(String arg[]) throws Exception {

		startInterruptedThread(Thread.currentThread().getThreadGroup());

		Timeout<Long> timeout = GenericTimeout.parseLong(60000);

		ITelnetUserDatas ud = new TelnetUserDatas();
		ud.setLogin("Administrator");
		ud.setPassword("Rf%9cRgf7");
		ITelnetConnectionDatas cd = new TelnetConnectionDatas();
		cd.setHost(Host.parseString("192.168.122.3"));

		TelnetSession session = new TelnetSession(ud, cd);
		try {
			session.connect();
			session.execRemoteCommand("c:\\test.bat", System.out, timeout);
			session.execRemoteCommand("FOR %A IN (1 2) DO echo %A", System.out,
					timeout);
			session.execRemoteCommand("c:\\test.bat\rc:\\test.bat", System.out,
					timeout);
			session.execRemoteCommand("FOR %A IN (1 2) DO ^", System.out,
					timeout);
			session.execRemoteCommand("FOR %B IN (A B) DO ECHO %A%B",
					System.out, timeout);
			session.execRemoteCommand("c:\\test.bat", System.out, timeout);
			session.disconnect();
			session.connect();
			session.execRemoteCommand("c:\\test.bat", System.out, timeout);
		} catch (TelnetSessionException Ex) {
			System.err.println(Ex.getUserFriendlyStackTrace());
			System.err.println(Ex.getFullStackTrace());
		} finally {
			session.disconnect();
		}
	}

	public static void startInterruptedThread(
			final ThreadGroup threadGroupToInterrupt) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				threadGroupToInterrupt.interrupt();
			}

		};
		Thread t = new Thread(r);
		t.start();
	}

}