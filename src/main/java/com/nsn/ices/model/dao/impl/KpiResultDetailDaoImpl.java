package com.nsn.ices.model.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.common.utils.KpiCalculateUtil;
import com.nsn.ices.model.dao.KpiResultDetailDao;
import com.nsn.ices.model.entity.KpiBusyTime;
import com.nsn.ices.model.entity.StartCycleWithEnd;

@Repository(value="kpiResultDetailDao")
public class KpiResultDetailDaoImpl implements KpiResultDetailDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	/**
	 * notice: if add new kpi to table,this method must be rewrite
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getKpiResultDetail(String priod){
		String sql = "SELECT period,kpi_id,region_id, "+
				"CASE "+
				"WHEN kpi_id IN (2,3,5,6,8,9,11,12,14,15,17,18,20,21,23,24,26,27,29,30,48,49,51,52,54,55,57,58,60,61,62,63,64,65,66,72"
				+ ",79,80,81,83,85,86,87,101,102,103,104,105,106) THEN SUM(result) "+
				"WHEN kpi_id IN (32,35,38,39,40,41,42,43,73,74) THEN ROUND(SUM(result),4) " +
				"WHEN kpi_id IN (31,34,44,46,67,68,69,70,71,75,76,78"
				+ ",82,84,88,89,90,91,92,93,94,95,96,97,98,99,100,107,108,109,110,111,112) THEN ROUND(AVG(result),4) "+
				"WHEN kpi_id IN (33,36) THEN result "+
				"WHEN kpi_id = 45 THEN MAX(result) "+
				"END AS result "+
				"FROM "+
				"ices_cell  b,"+
				"(SELECT * FROM ices_kpi_result_detail_60 WHERE period='" + priod + "') a "+
				"WHERE a.cel_obj_gid = b.cell_obj_gid "+
				"GROUP BY region_id,kpi_id ";
		List<Object[]> list = sessionFactory.getCurrentSession().createSQLQuery(sql).list();
		Map<String, Object[]> resultMap = new HashMap<String, Object[]>();
		for(Object[] objects : list){
			String kpi_id = objects[1].toString();
			String region_id = (String)objects[2].toString();
			resultMap.put(region_id+kpi_id, objects);
		}
		for(Object[] objects : list){
			int kpi_id = (Integer) objects[1];
			String region_id = objects[2].toString();
			int numerator_kpi_id = kpi_id + 1, denominator_kpi_id = kpi_id+2;
			Double numerator, denominator;
			switch (kpi_id) {
			case 1:
			case 4:
			case 7:
			case 10:
			case 13:
			case 16:
			case 19:
			case 22:
			case 25:
			case 28:
			case 37:
			case 47:
			case 50:
			case 53:
			case 56:
			case 59:
				if (resultMap.get(region_id+numerator_kpi_id) == null) {
					numerator = 0d;
				}else {
					numerator = Double.parseDouble(resultMap.get(region_id+numerator_kpi_id)[3]+"");
				}
				if (resultMap.get(region_id+denominator_kpi_id) == null) {
					denominator = 0d;
				}else {
					denominator = Double.parseDouble(resultMap.get(region_id+denominator_kpi_id)[3]+"");
				}
				if(denominator == 0)
					objects[3] = 0 ;
				else {
					objects[3] = String.format("%.4f", 100*numerator/denominator);
				}
				break;
			default:
				break;
			}
		}
		
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getKpiResultDetail(String period, int granularity){
		String icesKpiResultDetailTable = KpiCalculateUtil.getIcesKpiResultDetailTableByGranularity(granularity);
		String sql = "SELECT period,kpi_id,group_id, "+
				"CASE "+
				"WHEN kpi_id IN (2,3,5,6,8,9,11,12,14,15,17,18,20,21,23,24,26,27,29,30,48,49,51,52,54,55,57,58,60,61,62,63,64,65,66,72" +
				",79,80,81,83,85,86,87,101,102,103,104,105,106) THEN SUM(result) "+
				"WHEN kpi_id IN (32,35,38,39,40,41,42,43,73,74) THEN ROUND(SUM(result),4) " +
				"WHEN kpi_id IN (31,34,44,46,67,68,69,70,71,75,76,78"+
				",82,84,88,89,90,91,92,93,94,95,96,97,98,99,100,107,108,109,110,111,112) THEN ROUND(AVG(result),4) "+
				"WHEN kpi_id IN (33,36) THEN result "+
				"WHEN kpi_id = 45 THEN MAX(result) "+
				"END AS result "+
				"FROM "+
				"(SELECT t2.cell_obj_gid, group_id FROM ices_bts_group_data t1, ices_cell t2, ices_bts_group t3 WHERE t1.group_id = t3.id AND t1.ne_code = t2.ne_code AND t3.type = 1) b,"+
				"(SELECT * FROM " + icesKpiResultDetailTable + " WHERE period='" + period + "') a "+
				"WHERE a.cel_obj_gid = b.cell_obj_gid "+
				"GROUP BY group_id,kpi_id ";
		List<Object[]> list = sessionFactory.getCurrentSession().createSQLQuery(sql).list();
		Map<String, Object[]> resultMap = new HashMap<String, Object[]>();
		for(Object[] objects : list){
			String kpi_id = objects[1].toString();
			String group_id = (String)objects[2].toString();
			resultMap.put(group_id+kpi_id, objects);
		}
		for(Object[] objects : list){
			int kpi_id = (Integer) objects[1];
			String group_id = objects[2].toString();
			int numerator_kpi_id = kpi_id + 1, denominator_kpi_id = kpi_id+2;
			Double numerator, denominator;
			switch (kpi_id) {
			case 1:
			case 4:
			case 7:
			case 10:
			case 13:
			case 16:
			case 19:
			case 22:
			case 25:
			case 28:
			case 37:
			case 47:
			case 50:
			case 53:
			case 56:
			case 59:
				if (resultMap.get(group_id+numerator_kpi_id) == null) {
					numerator = 0d;
				}else {
					numerator = Double.parseDouble(resultMap.get(group_id+numerator_kpi_id)[3]+"");
				}
				if (resultMap.get(group_id+denominator_kpi_id) == null) {
					denominator = 0d;
				}else {
					denominator = Double.parseDouble(resultMap.get(group_id+denominator_kpi_id)[3]+"");
				}
				if(denominator == 0)
					objects[3] = 0 ;
				else {
					objects[3] = String.format("%.4f", 100*numerator/denominator);
				}
				break;
			default:
				break;
			}
		}
		
		return list;
	}
	
	/**
	 * notice: if add new kpi to table,this method must be rewrite
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getKpiResultDetailCityWeekly(String period, List<StartCycleWithEnd> periods){
		String periodConditions = "";
		for(StartCycleWithEnd startCycleWithEnd: periods){
			if (periodConditions.equals("")) {
				periodConditions = "WHERE ((period>='" + startCycleWithEnd.getStartCycle() + "' AND period<'" + startCycleWithEnd.getEndCycle() + "') "; 
			}else{
				periodConditions += " OR (period>='" + startCycleWithEnd.getStartCycle() + "' AND period<'" + startCycleWithEnd.getEndCycle() + "')) ";
			}
			
		}
		String sql = "SELECT '"+period+"' period,7 as granularity,mode,kpi_id, "+
				"ROUND(AVG(result),4) AS result "+
				"FROM ices_kpi_result_detail_city "+
				periodConditions +
				"AND granularity = 1 " +
				"GROUP BY mode,kpi_id ";
		List<Object[]> list = sessionFactory.getCurrentSession().createSQLQuery(sql).list();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<KpiBusyTime> getKpiBusyTime(String period){
		return sessionFactory.getCurrentSession()
				.createQuery("from KpiBusyTime where id>1 and :period between start_date and end_date")
				.setParameter("period", period)
				.list();
	}
	
	@Override
	public KpiBusyTime getKpiBusyTimeDefault(){
		return (KpiBusyTime)sessionFactory.getCurrentSession().createQuery("from KpiBusyTime where id=1").list().get(0);
	}
}
