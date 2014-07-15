package com.nsn.ices.model.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.nsn.ices.core.Application;
import com.nsn.ices.model.dao.OmcDao;
import com.nsn.ices.model.entity.Cell;

@Repository(value="omcDao")
public class OmcDaoImpl implements OmcDao {
	Logger log = Logger.getLogger(OmcDaoImpl.class);

	/**
	 * note: In order to calculate conveniently, we suppose that
	 *  the kpi_name with '_2' is numerator and the kpi_name with '_3' is denominator.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> find(int omc_id, int kpi_id, String period){
		log.info("Begin to fetch data,omc_id="+omc_id +", kpi_id="+kpi_id +", period="+period);
		SessionFactory ossSessionFactory = getSessionFactory(Application.ossMap.get(omc_id).getOss_name());
		List<Object[]> resultList = new ArrayList<Object[]>();
		String kpi_name_en = Application.kpiMap.get(kpi_id).getKpi_name_en();
		String kpi_name = kpi_name_en.substring(0, kpi_name_en.lastIndexOf("_"));
		String sql = "";
		String numerator="", denominator="",table_name="";
		table_name= Application.kpiNameMap.get(kpi_name+"_1").getOss_table_name();
		switch (kpi_name) {
		case "KPI_001":
		case "KPI_002":
		case "KPI_003":
		case "KPI_005":
		case "KPI_007":
		case "KPI_008":
		case "KPI_009":
		case "KPI_010":
		case "KPI_017":
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();//"sum(SIGN_CONN_ESTAB_ATT_MO_S +SIGN_CONN_ESTAB_ATT_MT +SIGN_CONN_ESTAB_ATT_MO_D +SIGN_CONN_ESTAB_ATT_OTHERS +SIGN_CONN_ESTAB_ATT_EMG)";
			sql = "select PERIOD_START_TIME,LNCEL_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ numerator +"/"+ denominator +" "
					+ "end as \"result\" "
					+ "from "+ table_name
					+ " where PERIOD_START_TIME = to_date('"+ period +"','YYYY-MM-DD HH24:MI:SS') ";
			sql = addCellConditions(cells(omc_id), sql);
			sql +="group by PERIOD_START_TIME, LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			break;
		case "KPI_011":
		case "KPI_012":
		case "KPI_014":
		case "KPI_015":
		case "KPI_018":
		case "KPI_019":
		case "KPI_020":
		case "KPI_021":
		case "KPI_022":
		case "KPI_023":
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select PERIOD_START_TIME,LNCEL_ID, "
					+ numerator + " as \"result\" "
					+ "from "+ table_name
					+ " where PERIOD_START_TIME = to_date('"+ period +"','YYYY-MM-DD HH24:MI:SS') ";
			sql = addCellConditions(cells(omc_id), sql);
			sql +="group by PERIOD_START_TIME, LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			break;
		case "KPI_004": //We should fetch data from different tables. This is a special KPI, so need more work
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			String numerator_table = Application.kpiNameMap.get(kpi_name+"_2").getOss_table_name();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();//"sum(SIGN_CONN_ESTAB_ATT_MO_S +SIGN_CONN_ESTAB_ATT_MT +SIGN_CONN_ESTAB_ATT_MO_D +SIGN_CONN_ESTAB_ATT_OTHERS +SIGN_CONN_ESTAB_ATT_EMG)";
			sql = "select m.PERIOD_START_TIME,m.LNCEL_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ numerator +"/"+ denominator +" "
					+ "end as \"result\" "
					+ "from "+ table_name +" m, "+numerator_table +" n "
					+ " where m.PERIOD_START_TIME = to_date('"+ period +"','YYYY-MM-DD HH24:MI:SS') "
					+ " and m.PERIOD_START_TIME=n.PERIOD_START_TIME "
					+ " and m.LNCEL_ID = n.LNCEL_ID ";
			sql = addCellConditions(cells(omc_id), sql);
			sql +="group by m.PERIOD_START_TIME, m.LNCEL_ID";
			sql = sql.replaceAll("LNCEL_ID in", "m.LNCEL_ID in");
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			
			break;
		case "KPI_006": //There is no 'LNBTS_ID' column in table 'NOKLTE_PS_LS1AP_LNBTS_RAW'
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();//"sum(SIGN_CONN_ESTAB_ATT_MO_S +SIGN_CONN_ESTAB_ATT_MT +SIGN_CONN_ESTAB_ATT_MO_D +SIGN_CONN_ESTAB_ATT_OTHERS +SIGN_CONN_ESTAB_ATT_EMG)";
			sql = "select PERIOD_START_TIME,b.CO_OBJECT_INSTANCE LNBTS_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ numerator +"/"+ denominator +" "
					+ "end as \"result\" "
					+ "from "+ table_name +" a, CTP_COMMON_OBJECTS b"
					+ " where PERIOD_START_TIME = to_date('"+ period +"','YYYY-MM-DD HH24:MI:SS') "
					+ "and b.CO_GID=a.LNBTS_ID "
					+ "and b.CO_OC_ID = 2853 ";
			sql +="group by PERIOD_START_TIME,b.CO_OBJECT_INSTANCE";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			break;
		case "KPI_013":
		case "KPI_016":
			sql = "SELECT DISTINCT to_date('"+ period +"','YYYY-MM-DD HH24:MI:SS') PERIOD_START_TIME, OBJ_GID LNCEL_ID, "
				+ "case LNCEL_DL_CH_BW  "
				+ "when 14 then '6 DL PRBS' "
				+ "when 30 then '15 DL PRBS' "
				+ "when 50 then '25 DL PRBS' "
				+ "when 100 then '50 DL PRBS' "
				+ "when 150 then '75 DL PRBS' "
				+ "when 200 then '100 DL PRBS' "
				+ "else 'NULL'"
				+ "end " 
				+ "as \"LNCEL_DL_CH_BW\", " 
				+ "case LNCEL_UL_CH_BW  "
				+ "when 14 then '6 DL PRBS' "
				+ "when 30 then '15 DL PRBS' "
				+ "when 50 then '25 DL PRBS' "
				+ "when 100 then '50 DL PRBS' "
				+ "when 150 then '75 DL PRBS' "
				+ "when 200 then '100 DL PRBS' "
				+ "end "
				+ "as \"LNCEL_UL_CH_BW\" "
				+ "from C_LTE_LNCEL "
				+ "where 1=1 ";
			sql = addCellConditions(cells(omc_id), sql);
			sql = sql.replace("LNCEL_ID in", "OBJ_GID in");
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			break;
		default:
			log.error("The kpi "+kpi_name_en+" is new added, please tell the administrator.");
			break;
		}
	
		log.info("  End to fetch data,omc_id="+omc_id +", kpi_id="+kpi_id +", period="+period);
		return resultList;
		
	}

	/**
	 * 通过omc id获取该omc下所有基站的所有的cell,并对cell分组放入set中,每组以逗号分隔,数目不能超过一千个.
	 * Cacheable注解把相应的omc_id下的所有cell都缓存起来,方便后边使用.
	 * @param omc_id
	 * @return Set<String> 每一个值都是由不超过1千个cell_obj_gid拼接而成的.
	 */
	@Cacheable("omcCell")
	public Set<String> cells(int omc_id) {
		log.info("Preparing a Set<String> cache of cell id for omc_id "+omc_id);
		Map<Integer, Cell> cellMap = Application.ossCellMap.get(omc_id);
		Set<Integer> cellSet = cellMap.keySet();
		StringBuilder sbBuffer = new StringBuilder();
		int i=0;
		Set<String> resultSet = new HashSet<String>();
		for(Integer cell_id: cellSet){
			sbBuffer.append(cell_id);
			if(i<999){
				sbBuffer.append(",");
			}else{
				resultSet.add(sbBuffer.toString());
				sbBuffer = new StringBuilder();
				i=0;
			}
			i++;
		}
		return resultSet;
	}
	/**
	 * 给sql添加cell查询条件,由于in中的项不能超过1000个,cell数量又很多,固对cell分组拼接
	 * @param cells
	 * @param sql
	 * @return
	 */
	private String addCellConditions(Set<String> cells, String sql) {
		String cellConditions = "";
		for(String cell :cells){
			cellConditions += "or LNCEL_ID in("+ cell +") ";
		}
		cellConditions = cellConditions.replaceFirst("or", "");
		if(cells.size()>0){
			sql += "and ("+ cellConditions +")";
		}
		return sql;
	}
	
	private SessionFactory getSessionFactory(String omcName){
		return Application.getInstance().getSessionFactoryByName(
				"sessionFactory_" + omcName);
	}

	@Override
	public String maxCycle(int omc_id, String tableName){
		SessionFactory ossSessionFactory = getSessionFactory(Application.ossMap.get(omc_id).getOss_name());
		String sql = "select to_char(max(PERIOD_START_TIME),'YYYY-MM-DD HH24:MI:SS')  FROM "+tableName;
		return (String)ossSessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0);
	}
	
}
