/**
 * 
 */
package com.balicamp.ngx.master.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author <a href="mailto:bagus.sugitayasa@sigma.co.id">GusdeGita</a>
 * @version Id: 
 */
public class CommonUtil {

	protected Logger log = LoggerFactory.getLogger(this.getClass().getName());

	protected <T> List<T> jsonToListOfObj(Class<?> typeKey, String json) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		List<T> listo = null;
		try {
			listo = mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, typeKey));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return listo;
	}

	protected Map<String, Object> jsonToMapOfObject(String json) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> mapto = new HashMap<String, Object>();
		try {
			mapto = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return mapto;
	}

	protected String objToJson(Object obj) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = "";
		try {
			json = ow.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return json;
	}

	@SuppressWarnings("rawtypes")
	protected <T> List<T> castCollection(List srcList, Class<T> clas) {
		List<T> list = new ArrayList<T>();
		for (Object obj : srcList) {
			if (obj != null && clas.isAssignableFrom(obj.getClass()))
				list.add(clas.cast(obj));
		}
		return list;
	}
	
	protected Object getEntityId(Object id) {
		ObjectMapper mapper = new ObjectMapper();
		if (id instanceof HashMap) {
			try {
				String sId = mapper.writeValueAsString(id);
				return Base64Utils.encodeToString(sId.getBytes("UTF-8"));
			} catch (Exception e) {
				try {
					return Base64Utils.encodeToString(id.toString().getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					return id.hashCode();
				}
			}
		}
		return id;
	}
}
