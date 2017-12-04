/**
 * 
 */
package com.balicamp.ngx.master.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import org.springframework.data.annotation.Id;

import lombok.Data;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
@SuppressWarnings("serial")
@Entity
@Data
public class AuditTrailEntity implements Serializable {

	@Id
	@javax.persistence.Id
	private String logId;
	private String activity;
	private String ipMacAddress;
	private String modifiedBy;
	private Date modifiedOn;
	private String newValue;
	private String oldValue;
	private String rowId;
	private String tableName;
	private String fieldIdName;
	private String entityClass;
	private String typeId; // 0:single id, 1:EmbeddedId
}
