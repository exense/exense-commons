package ch.commons.auth.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.PasswordDirectory;

public class LDAPClient implements PasswordDirectory{

	private static final String DEFAULT_FILTER = "(cn=%name%)";

	protected Logger logger = LoggerFactory.getLogger(LDAPClient.class);

	private LdapContext ctx;
	private String ldapBaseDn;
	private String filter;
	private Hashtable<String, Object> env = new Hashtable<String, Object>();

	public LDAPClient(String server, String baseDn, String username, String password) throws NamingException {
		buildClient(server, baseDn, null, username, password);
	}

	public LDAPClient(String server, String baseDn, String filter, String username, String password) throws NamingException {
		buildClient(server, baseDn, filter, username, password);
	}
	
	public LDAPClient(String server, String baseDn, String filter, String username, String password, String pathToJks, String jksPassword) throws NamingException {

		if(server != null && server.toLowerCase().contains("ldaps")) { // the "s" is only real proof that we want SSL
			env.put(Context.SECURITY_PROTOCOL, "ssl");

			if(pathToJks != null && jksPassword != null && !pathToJks.isEmpty() && !jksPassword.isEmpty()){ // Use custom truststore
				System.setProperty("javax.net.ssl.trustStore", pathToJks);
				System.setProperty("javax.net.ssl.trustStorePassword", jksPassword);

				System.setProperty("javax.net.ssl.keyStore", pathToJks);
				System.setProperty("javax.net.ssl.keyStorePassword", jksPassword);
			}
		}

		buildClient(server, baseDn, filter, username, password);
	}

	private void buildClient(String server, String baseDn, String filter, String username, String password) throws NamingException {
		this.ldapBaseDn = baseDn;
		
		this.filter = (filter==null)? DEFAULT_FILTER : filter;

		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, server);

		this.ctx = new InitialLdapContext(env, null);
	}

	public SearchResult findAccountByAccountName(String accountName) throws NamingException {

		String searchFilter = filter.replaceAll("%name%", accountName);

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<SearchResult> results = this.ctx.newInstance(null).search(ldapBaseDn, searchFilter, searchControls);

		SearchResult searchResult = null;
		if(results.hasMoreElements()) {
			searchResult = (SearchResult) results.nextElement();

			if(results.hasMoreElements()) {
				logger.error("Multiple users are present for cn: " + accountName);
				throw new NamingException("Multiple users are present for cn: " + accountName);
			}
		}

		return searchResult;
	}

	@Override
	public String getUserPassword(String username) throws Exception{
		SearchResult result = findAccountByAccountName(username);
		if(result == null || result.getAttributes() == null || result.getAttributes().get("uid") == null || result.getAttributes().get("userPassword") == null) {
			throw new Exception("Incorrect ldap configuration for user '"+username+"'. Result was:" + result);
		}
		logger.debug("Retrieving hashed password for uid '"+result.getAttributes().get("uid").get()+"', originating from cn '"+username+"'");
		return new String((byte[]) result.getAttributes().get("userPassword").get());
	}
}