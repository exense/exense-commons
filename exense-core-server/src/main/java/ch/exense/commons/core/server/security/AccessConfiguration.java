package ch.exense.commons.core.server.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessConfiguration {
	
	boolean authentication;
	
	boolean demo;
	
	boolean debug;
	
	List<String> roles;
	
	Map<String,String> miscParams;
	
	String defaultUrl;
	
	String title;

	public AccessConfiguration() {
		super();
		this.miscParams = new HashMap<>();
	}

	public boolean isDemo() {
		return demo;
	}

	public void setDemo(boolean demo) {
		this.demo = demo;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isAuthentication() {
		return authentication;
	}

	public void setAuthentication(boolean authentication) {
		this.authentication = authentication;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public Map<String, String> getMiscParams() {
		return miscParams;
	}

	public void setMiscParams(Map<String, String> miscParams) {
		this.miscParams = miscParams;
	}

	public String getDefaultUrl() {
		return defaultUrl;
	}

	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
