package com.nsn.ices.model.service;

import java.util.List;

import com.nsn.ices.model.entity.Cell;

public interface CellService {
	void add(Cell cell);
	void edit(Cell cell);
	void delete(int cellId);
	Cell search(int cellId);
	List<Cell> getAllCell();
	List<Cell> getAllActiveCell();
}
