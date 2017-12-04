/**
 * 
 */
package com.balicamp.ngx.master.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.balicamp.ngx.master.config.MongoDbConfig;
import com.balicamp.ngx.master.entity.AuditTrailEntity;
import com.balicamp.ngx.master.util.CommonUtil;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
@Repository
public class EventRepo extends CommonUtil {

	@Autowired
	private MongoDbConfig config;

	public AuditTrailEntity create(String event) {
		AuditTrailEntity entity = new AuditTrailEntity();
		try {
			entity.setActivity(event);
			config.mongoTemplate().insert(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity;
	}
}
