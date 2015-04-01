package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.BtsGroupData;

public interface BtsGroupDataDao {
	void add(BtsGroupData btsGroupData);
	void edit(BtsGroupData btsGroupData);
	void delete(int btsGroupDataId);
	BtsGroupData search(int btsGroupDataId);
	List<BtsGroupData> getAllBtsGroupData();
}
