package ch.commons.auth.cyphers;

public enum SupportedCypher {
    MD5(new MD5Encoder()),
    SSHA(new SSHAEncoder());
 
    public final CypherEncoder encoder;
 
    private SupportedCypher(CypherEncoder encoder) {
        this.encoder = encoder;
    }
    
    public static SupportedCypher forName(String cypher) throws Exception {
    	switch(cypher){
    	case "MD5":
    		return SupportedCypher.MD5;
    	case "SSHA":
    		return SupportedCypher.SSHA;
    	default:
    		throw new Exception("Unsupported cypher type: " +cypher);
    	}
    }
}