package com.nsn.ices.model.service;

import java.util.List;

import com.nsn.ices.model.entity.BtsGroupData;

public interface BtsGroupDataService {
	void add(BtsGroupData btsGroupData);
	void edit(BtsGroupData btsGroupData);
	void delete(int btsGroupDataId);
	BtsGroupData search(int btsGroupDataId);
	List<BtsGroupData> getAllBtsGroupData();
}
