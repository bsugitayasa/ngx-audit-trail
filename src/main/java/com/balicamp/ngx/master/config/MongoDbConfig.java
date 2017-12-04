/**
 * 
 */
package com.balicamp.ngx.master.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
@Configuration
public class MongoDbConfig extends AbstractMongoConfiguration {

	@Value("${mongo.db.databaseName:ngxaudittrail}")
	private String DB_NAME;

	@Value("${mongo.db.host:localhost}")
	private String HOST;

	@Value("${mongo.db.port:27017}")
	private Integer PORT;

//	@Autowired
//	private ProjectConfig config;

	@Override
	protected String getDatabaseName() {
		return DB_NAME;
	}

	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient(HOST, PORT);
	}
}