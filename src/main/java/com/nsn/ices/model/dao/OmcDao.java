package com.nsn.ices.model.dao;

import java.util.List;


public interface OmcDao {

	List<Object[]> find(int omc_id, int kpi_id, String period);
	
	String maxCycle(int omc_id, String tableName);
}
