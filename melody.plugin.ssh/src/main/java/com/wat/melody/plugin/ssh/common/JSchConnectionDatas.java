package com.wat.melody.plugin.ssh.common;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

public interface JSchConnectionDatas extends UserInfo, UIKeyboardInteractive {

	public String getLogin();

	public Host getHost();

	public Port getPort();

}
