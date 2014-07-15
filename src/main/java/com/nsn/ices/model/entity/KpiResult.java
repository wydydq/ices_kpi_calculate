package com.nsn.ices.model.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONSerializer;

public class KpiResult {

	private Map<Integer, String> resultMap = new HashMap<Integer, String>();
	
	private Set<Integer> isOkSet = new HashSet<Integer>();

	public Map<Integer, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<Integer, String> resultMap) {
		this.resultMap = resultMap;
	}

	public Set<Integer> getIsOkSet() {
		return isOkSet;
	}

	public void setIsOkSet(Set<Integer> isOkSet) {
		this.isOkSet = isOkSet;
	}

	public String getResultJson(){
		return JSONSerializer.toJSON(resultMap).toString();
	}
	
	public String getIsOkJson(){
		return JSONSerializer.toJSON(isOkSet).toString();
	}
}
