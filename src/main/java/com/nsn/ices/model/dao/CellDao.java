package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.Cell;

public interface CellDao {
	void add(Cell cell);
	void edit(Cell cell);
	void delete(int cellId);
	Cell search(int cellId);
	List<Cell> getAllCell();
	List<Cell> getAllActiveCell();
}
