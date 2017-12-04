/**
 * 
 */
package com.balicamp.ngx.master.aop;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.balicamp.ngx.master.entity.AuditTrailEntity;
import com.balicamp.ngx.master.util.CommonUtil;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
@Aspect
@Component
public class AuditTrailAspect extends CommonUtil {

	private Logger auditLogger = null;

	static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

	private ObjectMapper mapper = new ObjectMapper();

	/* TODO : Update dari konfigurasi / system properties */
	@Value("${audittrail.log.buffer.size:2}")
	private Integer logBufferSize;

	@Value("${audittrail.log.enabled:false}")
	private Boolean auditTrailAnabled;

	private static final String SINGLE_ID = "1";
	private static final String EMBEDDED_ID = "2";
	/* END OF TODO */

	@Autowired
	private MongoOperations mongoOperations;

	@SuppressWarnings("unchecked")
	@AfterReturning("execution(public * org.springframework.data.repository.Repository+.save*(..))")
	public void testAfter(JoinPoint jp) {
		try {
			Object o = jp.getArgs()[0];
			if (o instanceof Iterable) {
				Iterable<Object> iter = (Iterable<Object>) o;
				for (Object i : iter) {
					createAudit(i);
				}
			} else {
				createAudit(o);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void createAudit(Object o) {
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
		try {
			Boolean isEntity = o.getClass().isAnnotationPresent(Entity.class);
			String idFieldName = "";
			String typeId = SINGLE_ID;
			if (isEntity) {
				Field[] fields = o.getClass().getDeclaredFields();
				ArrayList<String> listField = new ArrayList<String>();
				for (Field field : fields) {
					if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
						idFieldName = field.getName();
						typeId = field.isAnnotationPresent(EmbeddedId.class) ? EMBEDDED_ID : SINGLE_ID;
					}
					if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(OneToOne.class)
							|| field.isAnnotationPresent(ManyToMany.class)
							|| field.isAnnotationPresent(ManyToOne.class)) {
						listField.add(field.getName());
					}
				}

				FilterProvider filter = null;

				if (listField.size() > 0) {
					filter = new SimpleFilterProvider().addFilter("filter", SimpleBeanPropertyFilter
							.filterOutAllExcept(listField.toArray(new String[listField.size()])));
				}

				String tableName = o.getClass().getSimpleName();
				if (o.getClass().isAnnotationPresent(Table.class)) {
					Table table = o.getClass().getAnnotation(Table.class);
					tableName = table.name();
				}

				String json = "{}";

				if (filter == null) {
					json = mapper.writeValueAsString(o);
				} else {
					json = mapper.writer(filter).writeValueAsString(o);
				}

				HashMap<String, Object> dataMap = mapper.readValue(json, new TypeReference<HashMap<String, Object>>() {
				});

				RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
				String ipAddress = "0.0.0.0"; // local address
				if (attrs != null) {
					HttpServletRequest request = (HttpServletRequest) attrs
							.resolveReference(RequestAttributes.REFERENCE_REQUEST);
					ipAddress = request.getRemoteAddr();
				}

				String modifiedBy = setStringValue("", (String) dataMap.get("modifiedBy"));
				modifiedBy = setStringValue(modifiedBy, (String) dataMap.get("approvedBy"));
				modifiedBy = setStringValue(modifiedBy, (String) dataMap.get("checkedBy"));
				modifiedBy = setStringValue(modifiedBy, (String) dataMap.get("createdBy"));

				StringBuffer msg = new StringBuffer();
				msg.append("{").append("\"entityClass\": \"").append(o.getClass().getName()).append("\",")
						.append("\"fieldIdName\": \"").append(idFieldName).append("\",").append("\"typeId\": \"")
						.append(typeId).append("\",").append("\"newValue\": \"")
						.append(Base64Utils.encodeToString(json.getBytes("UTF-8"))).append("\",")
						.append("\"ipMacAddress\": \"").append(ipAddress).append("\",").append("\"tableName\": \"")
						.append(tableName).append("\",").append("\"modifiedBy\": \"").append(modifiedBy).append("\",")
						.append("\"modifiedOn\": ")
						.append(Calendar.getInstance()
								.getTimeInMillis()/* SDF.format(Calendar.getInstance().getTime()) */)
						.append(",").append("\"rowId\": \"").append(getEntityId(dataMap.get(idFieldName))).append("\",")
						.append("\"logId\": \"")
						.append((o.getClass().getSimpleName() + "_" + getEntityId(dataMap.get(idFieldName))))
						.append("\"").append("}");

				log.info("AuditTrail: " + msg.toString());
				if (auditLogger != null && auditTrailAnabled) {
					auditLogger.info(msg.toString());
				}

			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String setStringValue(String src, String dest) {
		if (src == null || src.trim().length() == 0)
			return dest;
		return src;
	}

	@PostConstruct
	public void postConstruc() {
		log.info("post construct: " + mongoOperations);

		if (!auditTrailAnabled)
			return;

		// check mongo connection
		if (mongoOperations != null) {
			try {
				mongoOperations.findById("TEST123", AuditTrailEntity.class, "AuditTrailLog");
			} catch (Exception e) {
				log.warn(e.getMessage());
				return;
			}
		}

		try {

			auditLogger = LoggerFactory.getLogger("MongoAuditLogger");
			LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) auditLogger).getLoggerContext();
			((ch.qos.logback.classic.Logger) auditLogger).detachAndStopAllAppenders();

			AuditTrailMongoDbAppender appender = new AuditTrailMongoDbAppender();
			appender.setContext(loggerContext);
			appender.setMongoTemplate(mongoOperations);
			appender.setBufferSize(logBufferSize);
			appender.start();

			ch.qos.logback.classic.AsyncAppender aAppender = new ch.qos.logback.classic.AsyncAppender();
			aAppender.addAppender(appender);
			aAppender.start();

			((ch.qos.logback.classic.Logger) auditLogger).addAppender(aAppender);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
