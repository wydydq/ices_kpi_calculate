package com.nsn.ices.model.service;

import java.util.List;

import com.nsn.ices.model.entity.Bts;

public interface BtsService {
	void add(Bts bts);
	void edit(Bts bts);
	void delete(int btsId);
	Bts search(int btsId);
	List<Bts> getAllBts();
}
