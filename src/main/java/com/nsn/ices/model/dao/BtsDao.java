package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.Bts;

public interface BtsDao {
	void add(Bts bts);
	void edit(Bts bts);
	void delete(int btsId);
	Bts search(int btsId);
	List<Bts> getAllBts();
}
