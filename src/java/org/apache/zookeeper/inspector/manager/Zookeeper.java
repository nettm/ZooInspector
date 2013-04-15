/*
 * StoreFiles 1.0. Copyright 2012 Xikang, Co.ltd. All rights reserved.
 * 
 * FileName: ZookeeperParser.java
 * 
 */

package org.apache.zookeeper.inspector.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * {该处请说明该class的含义和作用}
 * 
 * @author tianmu
 * @version $Revision$$Date$
 * @created 2012-11-12 下午1:40:03
 * @history
 * @see
 */
public class Zookeeper {

	List<Server> servers = new ArrayList<Server>();

	public List<Server> getServers() {
		return servers;
	}

	public void addServer(Server server) {
		servers.add(server);
	}

}
