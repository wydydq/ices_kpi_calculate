package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.Oss;

public interface OssDao {
	void add(Oss oss);
	void edit(Oss oss);
	void delete(int ossId);
	Oss search(int ossId);
	List<Oss> getAllOss();
}
