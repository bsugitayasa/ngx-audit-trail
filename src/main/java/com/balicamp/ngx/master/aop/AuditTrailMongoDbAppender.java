/**
 * 
 */
package com.balicamp.ngx.master.aop;

import java.util.ArrayList;

import org.codehaus.jackson.map.ObjectMapper;
import org.apache.log4j.helpers.LogLog;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import com.balicamp.ngx.master.entity.AuditTrailEntity;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
public class AuditTrailMongoDbAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	@Autowired
	private MongoOperations mongoTemplate;

	private int bufferSize;

	private ObjectMapper mapper = new ObjectMapper();

	protected ArrayList<ILoggingEvent> removes = new ArrayList<ILoggingEvent>();
	protected ArrayList<ILoggingEvent> buffer = new ArrayList<ILoggingEvent>();

	public AuditTrailMongoDbAppender() {
		mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		this.bufferSize = 20;
	}

	public void setMongoTemplate(MongoOperations mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	protected void flushBuffer() {
		removes.ensureCapacity(buffer.size());
		for (ILoggingEvent logEvent : buffer) {
			try {
				String json = logEvent.getMessage();
				AuditTrailEntity audit = mapper.readValue(json, new TypeReference<AuditTrailEntity>() {
				});
				AuditTrailEntity oAudit = mongoTemplate.findById(audit.getLogId(), AuditTrailEntity.class,
						"AuditTrailLog");
				audit.setActivity("ADD");
				if (oAudit != null) {
					audit.setOldValue(oAudit.getNewValue());
					audit.setActivity("UPDATE");
				}
				mongoTemplate.save(audit, "AuditTrailLog");
			} catch (Exception e) {
				LogLog.warn(e.getMessage());
			} finally {
				removes.add(logEvent);
			}
		}

		buffer.removeAll(removes);

		removes.clear();
	}

	@Override
	protected void append(ILoggingEvent event) {
		buffer.add(event);
		if (buffer.size() >= bufferSize) {
			flushBuffer();
		}
	}

}
