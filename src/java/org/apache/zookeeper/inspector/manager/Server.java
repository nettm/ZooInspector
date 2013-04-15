/*
 * StoreFiles 1.0. Copyright 2012 Xikang, Co.ltd. All rights reserved.
 * 
 * FileName: Server.java
 * 
 */

package org.apache.zookeeper.inspector.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {该处请说明该class的含义和作用}
 * 
 * @author tianmu
 * @version $Revision$$Date$
 * @created 2012-11-12 上午11:09:55
 * @history
 * @see
 */
public class Server implements Serializable {

	/**
	 * {该处请说明该field的含义和作用}
	 */
	private static final long serialVersionUID = 3236075755328230987L;

	private String host;

	private List<User> users = new ArrayList<User>();

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<User> getUsers() {
		return users;
	}

	public void addUser(User user) {
		users.add(user);
	}
}
