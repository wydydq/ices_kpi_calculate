package com.nsn.ices.model.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nsn.ices.model.dao.CellDao;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.service.CellService;

@Service(value="cellService")
public class CellServiceImpl implements CellService {

	@Autowired
	private CellDao cellDao;
	
	@Transactional
	public void add(Cell cell) {
		cellDao.add(cell);
	}

	@Transactional
	public void edit(Cell cell) {
		cellDao.edit(cell);
	}

	@Transactional
	public void delete(int cellId) {
		cellDao.delete(cellId);
	}

	@Transactional
	public Cell search(int cellId) {
		return cellDao.search(cellId);
	}

	@Transactional
	public List<Cell> getAllCell() {
		return cellDao.getAllCell();
	}
	
	@Transactional
	public List<Cell> getAllActiveCell() {
		return cellDao.getAllActiveCell();
	}
}
