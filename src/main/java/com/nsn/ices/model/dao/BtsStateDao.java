package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.BtsState;

public interface BtsStateDao {
	void add(BtsState btsState);
	void edit(BtsState btsState);
	void delete(int btsStateId);
	BtsState search(int btsStateId);
	List<BtsState> getAllBtsState();
}
