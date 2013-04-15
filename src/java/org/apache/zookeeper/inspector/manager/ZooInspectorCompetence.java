package org.apache.zookeeper.inspector.manager;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester3.Digester;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.xml.sax.SAXException;

public class ZooInspectorCompetence {

	private static final File configFile = new File("./config/config.xml");

	private static final String AUTHENTICATION_TYPE = "digest";

	private static Zookeeper zk = null;

	private static Map<String, List<ACL>> aclsMap = null;

	private ZooInspectorCompetence() {
	}

	public synchronized static void init() {
		if (zk == null) {
			try {
				zk = parse(configFile);
				aclsMap = initACLs(zk);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 权限检查
	 * 
	 * @param name
	 * @param password
	 * @return
	 * @author tianmu
	 * @created 2012-11-9 下午1:46:26
	 */
	public static boolean check(String host, String name, String password) {
		List<Server> servers = zk.getServers();
		for (Server server : servers) {
			if (server.getHost().equals(host)) {
				List<User> users = server.getUsers();
				for (User user : users) {
					if (user.getName().trim().equals(name.trim()) && user.getPassword().trim().equals(password.trim())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * 取得主机列表
	 * 
	 * @param prop
	 * @return
	 * @author tianmu
	 * @created 2012-11-9 上午9:26:09
	 */
	public static List<String> getHosts() {
		List<String> hosts = new ArrayList<String>();
		List<Server> servers = zk.getServers();
		for (Server server : servers) {
			hosts.add(server.getHost());
		}
		return hosts;
	}

	/**
	 * 取得权限列表
	 * 
	 * @return
	 * @author tianmu
	 * @created 2012-11-9 上午10:04:16
	 */
	public static List<ACL> getAcls(String host) {
		return aclsMap.get(host);
	}

	/**
	 * 取得权限类型
	 * 
	 * @param role
	 * @return
	 * @author tianmu
	 * @created 2012-11-12 下午2:48:41
	 */
	private static int getPerm(String role) {
		if (role != null && role.equals("admin")) {
			return ZooDefs.Perms.ALL;
		} else {
			return ZooDefs.Perms.READ;
		}
	}

	/**
	 * 取得认证类型
	 * 
	 * @return
	 * @author tianmu
	 * @created 2012-11-9 上午10:24:50
	 */
	public static String getAuthenticationType() {
		return AUTHENTICATION_TYPE;
	}
	
	/**
	 * 初始化ACL
	 *
	 * @return
	 * @author tianmu
	 * @created 2013-4-15 上午10:13:35
	 */
	private static Map<String, List<ACL>> initACLs(Zookeeper zkServer) {
		Map<String, List<ACL>> aclsMap = new HashMap<String, List<ACL>>();
		List<Server> servers = zkServer.getServers();
		for (Server server : servers) {
			List<ACL> acls = new ArrayList<ACL>();
			List<User> users = server.getUsers();
			for (User user : users) {
				Id id = null;
				try {
					id = new Id(AUTHENTICATION_TYPE, DigestAuthenticationProvider.generateDigest(user.getName()
							+ ":" + user.getPassword()));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				ACL acl = new ACL(getPerm(user.getRole()), id);
				acls.add(acl);
			}
			aclsMap.put(server.getHost(), acls);
		}
		
		return aclsMap;
	}

	/**
	 * 解析config.xml配置文件
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @author tianmu
	 * @created 2012-11-12 下午2:30:20
	 */
	private static Zookeeper parse(File file) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("zookeeper", "org.apache.zookeeper.inspector.manager.Zookeeper");
		digester.addObjectCreate("zookeeper/server", "org.apache.zookeeper.inspector.manager.Server");
		digester.addSetProperties("zookeeper/server");
		digester.addObjectCreate("zookeeper/server/user", "org.apache.zookeeper.inspector.manager.User");
		digester.addSetProperties("zookeeper/server/user");
		digester.addSetNext("zookeeper/server", "addServer", "org.apache.zookeeper.inspector.manager.Server");
		digester.addSetNext("zookeeper/server/user", "addUser", "org.apache.zookeeper.inspector.manager.User");

		return (Zookeeper) digester.parse(file);
	}
}
