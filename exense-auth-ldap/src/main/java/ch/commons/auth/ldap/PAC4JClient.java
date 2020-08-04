package ch.commons.auth.ldap;

import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapUtils;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.ldap.profile.service.LdapProfileService;

public class PAC4JClient {
	
	private LdapProfileService service;

	public PAC4JClient() {
		super();

		FormatDnResolver dnResolver = new FormatDnResolver();
		dnResolver.setFormat("dc=example,dc=com" + "=%s," + "ou=people,dc=example,dc=com");
		
		ConnectionConfig connectionConfig = new ConnectionConfig();
		connectionConfig.setLdapUrl("ldap://ldap.exense.ch:389");
		DefaultConnectionFactory connectionFactory = new DefaultConnectionFactory();
		connectionFactory.setConnectionConfig(connectionConfig);
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMinPoolSize(1);
		poolConfig.setMaxPoolSize(2);
		poolConfig.setValidateOnCheckOut(true);
		poolConfig.setValidateOnCheckIn(true);
		poolConfig.setValidatePeriodically(false);
		SearchValidator searchValidator = new SearchValidator();
		IdlePruneStrategy pruneStrategy = new IdlePruneStrategy();
		BlockingConnectionPool connectionPool = new BlockingConnectionPool();
		connectionPool.setPoolConfig(poolConfig);
		connectionPool.setValidator(searchValidator);
		connectionPool.setPruneStrategy(pruneStrategy);
		connectionPool.setConnectionFactory(connectionFactory);
		connectionPool.initialize();
		PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
		pooledConnectionFactory.setConnectionPool(connectionPool);
		PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler();
		handler.setConnectionFactory(pooledConnectionFactory);
		Authenticator ldaptiveAuthenticator = new Authenticator();
		ldaptiveAuthenticator.setDnResolver(dnResolver);
		ldaptiveAuthenticator.setAuthenticationHandler(handler);
		// pac4j:
		service  = new LdapProfileService(connectionFactory, ldaptiveAuthenticator, "cn=admin,dc=exense,dc=ch");
	}

	public String getDn() {
		return service.getUsersDn();
	}

	public void validate() {
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("testpwdmd5", "testpwdmd5");
		service.validate(credentials, null);
	}
	
}
