package org.apache.zookeeper.inspector.manager;

import java.io.Serializable;

public class User implements Serializable {

	/**
	 * {该处请说明该field的含义和作用}
	 */
	private static final long serialVersionUID = -7372935231128729632L;

	private String name;
	
	private String password;
	
	private String role;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
