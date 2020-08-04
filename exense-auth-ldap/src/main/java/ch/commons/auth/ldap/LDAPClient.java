package ch.commons.auth.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.PasswordDirectory;

public class LDAPClient implements PasswordDirectory{

	protected Logger logger = LoggerFactory.getLogger(LDAPClient.class);

	private LdapContext ctx;
	private String ldapBaseDn;

	public LDAPClient(String server, String baseDn, String username, String password) throws NamingException {

		this.ldapBaseDn = baseDn;

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, server);

		this.ctx = new InitialLdapContext(env, null);
	}
	
	public LDAPClient(String server, String baseDn, String username, String password, String pathToJks, String jksPassword) throws NamingException {

		this.ldapBaseDn = baseDn;

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, server);
		// Specify SSL
		env.put(Context.SECURITY_PROTOCOL, "ssl");

		System.setProperty("javax.net.ssl.trustStore", pathToJks);
		System.setProperty("javax.net.ssl.trustStorePassword", jksPassword);
		
		System.setProperty("javax.net.ssl.keyStore", pathToJks);
		System.setProperty("javax.net.ssl.keyStorePassword", jksPassword);
		
		this.ctx = new InitialLdapContext(env, null);
	}

	public SearchResult findAccountByAccountName(String accountName) throws NamingException {

		String searchFilter = "(cn=" + accountName + ")";

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

	private SSLContext setSSLContext(String jksPath, String password) throws KeyStoreException, NoSuchAlgorithmException,
	CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		FileInputStream instream = new FileInputStream(new File(jksPath));
		KeyStore keyStore = KeyStore.getInstance("jks");
		keyStore.load((InputStream) instream, password.toCharArray());

		KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyFactory.init(keyStore, password.toCharArray());
		KeyManager[] keyManagers = keyFactory.getKeyManagers();

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(keyManagers, trustAllCerts, new SecureRandom());

		return sc;
		//this.client = HttpClients.custom().setSSLContext(sc).build();
	}

}