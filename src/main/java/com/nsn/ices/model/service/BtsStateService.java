package com.nsn.ices.model.service;

import java.util.List;

import com.nsn.ices.model.entity.BtsState;

public interface BtsStateService {
	void add(BtsState btsState);
	void edit(BtsState btsState);
	void delete(int btsStateId);
	BtsState search(int btsStateId);
	List<BtsState> getAllBtsState();
}
