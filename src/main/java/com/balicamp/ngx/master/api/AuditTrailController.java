/**
 * 
 */
package com.balicamp.ngx.master.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.balicamp.ngx.master.entity.AuditTrailEntity;
import com.balicamp.ngx.master.repository.EventRepo;
import com.balicamp.ngx.master.util.CommonUtil;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id:
 */
@RestController
@RequestMapping("/api")
public class AuditTrailController extends CommonUtil {

	@Autowired
	private EventRepo repo;

	@RequestMapping(value = "/sendAudit", produces = "application/json")
	public @ResponseBody AuditTrailEntity sendRequest(@RequestBody String request) {
		return repo.create(request);
	}
}
