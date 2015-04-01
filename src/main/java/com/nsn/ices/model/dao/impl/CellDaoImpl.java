package com.nsn.ices.model.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.CellDao;
import com.nsn.ices.model.entity.Cell;

@Repository(value="cellDao")
public class CellDaoImpl implements CellDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void add(Cell cell) {
		sessionFactory.getCurrentSession().save(cell);

	}

	@Override
	public void edit(Cell cell) {
		sessionFactory.getCurrentSession().update(cell);

	}

	@Override
	public void delete(int cellId) {
		sessionFactory.getCurrentSession().delete(search(cellId));

	}

	@Override
	public Cell search(int cellId) {
		return (Cell)sessionFactory.getCurrentSession().get(Cell.class, cellId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Cell> getAllCell() {
		return sessionFactory.getCurrentSession().createQuery("from Cell").list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Cell> getAllActiveCell() {
		return sessionFactory.getCurrentSession().createQuery("from Cell as c where c.ne_code in(select b.ne_code from Bts as b where b.is_active=0) and c.cell_obj_gid is not null and c.cell_obj_gid!=0 ").list();
	}

}
