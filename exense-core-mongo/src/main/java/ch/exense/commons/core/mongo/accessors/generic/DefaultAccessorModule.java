package ch.exense.commons.core.mongo.accessors.generic;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Default Jackson module used for the serialization in the persistence layer (Jongo)
 * This module isn't used in the REST layer (Jersey) and can therefore be used to define serializers that only 
 * have to be used when persisting objects
 * 
 */
public class DefaultAccessorModule extends SimpleModule {

	private static final long serialVersionUID = 5544301456563146100L;

	public DefaultAccessorModule() {
		super();
		
		/* TODO
		 * Excluding custom serialization stuff for now
		 * Will fix once we integrate with step's actual accessors
		 * 
		 * */
		
		//addSerializer(DottedKeyMap.class, new DottedMapKeySerializer());
		//addDeserializer(DottedKeyMap.class, new DottedMapKeyDeserializer());
		
	}

}
