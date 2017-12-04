/**
 * 
 */
package com.balicamp.ngx.master.config;


import lombok.Data;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
//@Configuration
//@ConfigurationProperties(prefix = "spring.data.mongodb")
@Data
public class ProjectConfig {
	private String host;
	private String database;
	private Integer port;
}