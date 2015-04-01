package com.nsn.ices.model.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.nsn.ices.core.Application;
import com.nsn.ices.model.dao.OmcDao;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.entity.StartCycleWithEnd;

@Repository(value="omcDao")
public class OmcDaoImpl implements OmcDao {
	Logger log = Logger.getLogger(OmcDaoImpl.class);

	/**
	 * Note: In order to calculate conveniently, we suppose that kpi_name end with '_2' is numerator and is denominator when end with '_3';The others only end with '_1'.
	 * It's a important feature you should follow when add new kpi.
	 * We has five different sqls which depends on kpi_name, such as follows:
	 * (1). KPI_001,KPI_002,KPI_003,KPI_005,KPI_007,KPI_008,KPI_009,KPI_010,KPI_017  Get numerator and denominator from same table
	 * (2). KPI_011,KPI_012,KPI_014,KPI_015,KPI_018,KPI_019,KPI_020,KPI_021,KPI_022,KPI_023  Get single result from one table
	 * (3). KPI_004             Get numerator from table NOKLTE_PS_LRRC_MNC1_RAW when get denominator from table NOKLTE_PS_LUEST_MNC1_RAW
	 * (4). KPI_006             We should get data from table NOKLTE_PS_LEPSB_MNC1_RAW,but there is no LNCEL_ID column,so we have to use LNBTS_ID 
	 * 							and save the same data for every cell in detail table.
	 * (5). KPI_013,KPI_016     Get common values like 15 DL PRBS,25 DL PRBS etc.
	 * ------------------------------Group KPI as follows------------------------------
	 * This time we may use different methods to calculate kpi value, for example,we'll query all values at one time if they are from same table.
	 * (1). KPI_025,KPI_027,KPI_028		count:3
	 * (2). KPI_030,KPI_031,KPI_032,KPI_033,KPI_034,KPI_035,KPI_036,KPI_037,KPI_038,KPI_039,
	 *      KPI_040,KPI_041,KPI_042,KPI_043,KPI_044,KPI_046		count:16
	 * (3). KPI_026 			Get numerator from NOKLTE_PS_LRRC_MNC1_RAW when get denominator from NOKLTE_PS_LUEST_MNC1_RAW		count:1
	 * (4).
	 * (5).
	 * (6). KPI_024				Get single result from different more than one table		count:1
	 * (7). KPI_029				Get numerator and denominator from more than one table		count:1
	 * ------------------------------NEW Common KPI-------------------------------------
	 * (8). KPI_047-KPI_055,KPI_067-KPI_080 count:23, type value must be 4
	 * (9). KPI_056 			Get value from three tables   count:1
	 * (10).KPI_057-KPI_066 	Get single result from two tables. count:10 and type value should be 5
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> find(int omc_id, int kpi_id, StartCycleWithEnd periods){
		log.info("Begin to fetch data,omc_id="+omc_id +", kpi_id="+kpi_id +", period="+periods);
		SessionFactory ossSessionFactory = getSessionFactory(Application.ossMap.get(omc_id).getOss_name());
		List<Object[]> resultList = new ArrayList<Object[]>();
		String kpi_name_en = Application.kpiMap.get(kpi_id).getKpi_name_en();
		String kpi_name = kpi_name_en.substring(0, kpi_name_en.lastIndexOf("_"));
		int kpi_type = Application.kpiMap.get(kpi_id).getType();
		String sql = "";
		String numerator="", denominator="",result="",table_name="";
		table_name= Application.kpiNameMap.get(kpi_name+"_1").getOss_table_name();
		if(kpi_name.equals("KPI_001") || kpi_name.equals("KPI_002") || kpi_name.equals("KPI_003") || kpi_name.equals("KPI_005") 
				 || kpi_name.equals("KPI_007") || kpi_name.equals("KPI_008") || kpi_name.equals("KPI_009") || kpi_name.equals("KPI_010")
				 || kpi_name.equals("KPI_017") || kpi_name.equals("KPI_025") || kpi_name.equals("KPI_027") || kpi_name.equals("KPI_028")){
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();//"sum(SIGN_CONN_ESTAB_ATT_MO_S +SIGN_CONN_ESTAB_ATT_MT +SIGN_CONN_ESTAB_ATT_MO_D +SIGN_CONN_ESTAB_ATT_OTHERS +SIGN_CONN_ESTAB_ATT_EMG)";
			result = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,LNCEL_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ result +" "
					+ "end as \"result\" "
					+ "from "+ table_name
					+ " where PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ "and  PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') ";
			sql +="group by LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_type == 4/*kpi_name.equals("KPI_011") || kpi_name.equals("KPI_012") || kpi_name.equals("KPI_014") || kpi_name.equals("KPI_015") 
			 || kpi_name.equals("KPI_018") || kpi_name.equals("KPI_019") || kpi_name.equals("KPI_020") || kpi_name.equals("KPI_021")
			 || kpi_name.equals("KPI_022") || kpi_name.equals("KPI_023") || kpi_name.equals("KPI_030")
			 || kpi_name.equals("KPI_031") || kpi_name.equals("KPI_032") || kpi_name.equals("KPI_033") || kpi_name.equals("KPI_034")
			 || kpi_name.equals("KPI_035") || kpi_name.equals("KPI_036") || kpi_name.equals("KPI_037") || kpi_name.equals("KPI_038")
			 || kpi_name.equals("KPI_039") || kpi_name.equals("KPI_040") || kpi_name.equals("KPI_041") || kpi_name.equals("KPI_042")
			 || kpi_name.equals("KPI_043") || kpi_name.equals("KPI_044") || kpi_name.equals("KPI_046")*/){
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,LNCEL_ID, "
					+ numerator + " as \"result\" "
					+ "from "+ table_name
					+ " where PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ "and  PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') ";
			sql +="group by LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_004") || kpi_name.equals("KPI_026") || kpi_name.equals("KPI_029")){
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			String numerator_table = Application.kpiNameMap.get(kpi_name+"_2").getOss_table_name();
			String denominator_table = Application.kpiNameMap.get(kpi_name+"_3").getOss_table_name();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();
			result = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,m.LNCEL_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ result +" "
					+ "end as \"result\" "
					+ "from "+ numerator_table +" m, "+denominator_table +" n "
					+ " where m.PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ " and  m.PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ " and m.PERIOD_START_TIME=n.PERIOD_START_TIME "
					+ " and m.LNCEL_ID = n.LNCEL_ID ";
			sql +="group by m.LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			
		}else if(kpi_name.equals("KPI_006")){
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();
			result = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,b.CO_OBJECT_INSTANCE LNBTS_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ result +" "
					+ "end as \"result\" "
					+ "from "+ table_name +" a, CTP_COMMON_OBJECTS b"
					+ " where PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ " and  PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ "and b.CO_GID=a.LNBTS_ID "
					+ "and b.CO_OC_ID = 2853 ";
			sql +="group by b.CO_OBJECT_INSTANCE";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_013") || kpi_name.equals("KPI_016")){
			sql = "SELECT '"+ periods.getStartCycle() +"' PERIOD_START_TIME, OBJ_GID LNCEL_ID, "
				+ "case LNCEL_DL_CH_BW  "
				+ "when 14 then '6' "
				+ "when 30 then '15' "
				+ "when 50 then '25' "
				+ "when 100 then '50' "
				+ "when 150 then '75' "
				+ "when 200 then '100' "
				+ "else '100'"
				+ "end " 
				+ "as \"LNCEL_DL_CH_BW\", " 
				+ "case LNCEL_UL_CH_BW  "
				+ "when 14 then '6' "
				+ "when 30 then '15' "
				+ "when 50 then '25' "
				+ "when 100 then '50' "
				+ "when 150 then '75' "
				+ "when 200 then '100' "
				+ "else '100'"
				+ "end "
				+ "as \"LNCEL_UL_CH_BW\" "
				+ "from C_LTE_LNCEL "
				+ "where CONF_ID = 1 ";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_024")){
			table_name = "NOKLTE_PS_LUEST_MNC1_RAW a,NOKLTE_PS_LEPSB_MNC1_RAW c ";
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,a.LNCEL_ID,"
					+"case SUM(A.SIGN_CONN_ESTAB_COMP)*SUM(A.SIGN_CONN_ESTAB_ATT_MO_S + A.SIGN_CONN_ESTAB_ATT_MT + A.SIGN_CONN_ESTAB_ATT_MO_D + A.SIGN_CONN_ESTAB_ATT_OTHERS + A.SIGN_CONN_ESTAB_ATT_EMG)*SUM(C.EPS_BEARER_STP_COM_INI_QCI1 + C.EPS_BEARER_STP_COM_INI_QCI_2 + C.EPS_BEARER_STP_COM_INI_QCI_3 + C.EPS_BEARER_STP_COM_INI_QCI_4 + C.EPS_BEAR_STP_COM_INI_NON_GBR)*SUM(C.EPS_BEARER_STP_ATT_INI_QCI_1 + C.EPS_BEARER_STP_ATT_INI_QCI_2 + C.EPS_BEARER_STP_ATT_INI_QCI_3 + C.EPS_BEARER_STP_ATT_INI_QCI_4 + C.EPS_BEAR_STP_ATT_INI_NON_GBR)*SUM(C.EPS_BEARER_SETUP_COMPLETIONS - C.EPS_BEARER_STP_COM_INI_QCI1 - C.EPS_BEARER_STP_COM_INI_QCI_2 - C.EPS_BEARER_STP_COM_INI_QCI_3 - C.EPS_BEARER_STP_COM_INI_QCI_4 - C.EPS_BEAR_STP_COM_INI_NON_GBR)*SUM(C.EPS_BEARER_SETUP_ATTEMPTS - C.EPS_BEARER_STP_ATT_INI_QCI_1 - C.EPS_BEAR_STP_ATT_INI_NON_GBR - C.EPS_BEARER_STP_ATT_INI_QCI_2 - C.EPS_BEARER_STP_ATT_INI_QCI_3 - C.EPS_BEARER_STP_ATT_INI_QCI_4)" 
					+" when 0 then 0 else "
					+ numerator + " end as \"result\" "
					+ "from "+ table_name
					+ " where a.PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ " and a.PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ " and a.PERIOD_START_TIME=c.PERIOD_START_TIME"
					+ " and a.LNCEL_ID = c.LNCEL_ID";
			sql +=" group by a.LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if("KPI_057,KPI_058,KPI_059,KPI_060,KPI_061,KPI_062,KPI_063,KPI_064,KPI_065,KPI_066".contains(kpi_name)){
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,a.LNCEL_ID,"
					+ numerator + " as \"result\" "
					+ "from "+ table_name
					+ " where a.PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ " and a.PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ " and a.PERIOD_START_TIME=b.PERIOD_START_TIME"
					+ " and a.LNCEL_ID = b.LNCEL_ID";
			sql +=" group by a.LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if("KPI_056".contains(kpi_name)){
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ periods.getStartCycle() +"' PERIOD_START_TIME,a.LNCEL_ID,"
					+ numerator + " as \"result\" "
					+ "from "+ table_name
					+ " where a.PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ " and a.PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ " and a.PERIOD_START_TIME=b.PERIOD_START_TIME"
					+ " and a.PERIOD_START_TIME=c.PERIOD_START_TIME"
					+ " and a.LNCEL_ID = b.LNCEL_ID"
					+ " and a.LNCEL_ID = c.LNCEL_ID";
			sql +=" group by a.LNCEL_ID";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}
	
		log.info("  End to fetch data,omc_id="+omc_id +", kpi_id="+kpi_id +", period="+periods);
		return resultList;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findByCity(int omc_id, int kpi_id,String PERIOD_START_TIME, List<StartCycleWithEnd> periods){
		log.info("Begin to fetch data by city,omc_id="+omc_id +", kpi_id="+kpi_id +", period="+periods);
		SessionFactory ossSessionFactory = getSessionFactory(Application.ossMap.get(omc_id).getOss_name());
		List<Object[]> resultList = new ArrayList<Object[]>();
		String kpi_name_en = Application.kpiMap.get(kpi_id).getKpi_name_en();
		String kpi_name = kpi_name_en.substring(0, kpi_name_en.lastIndexOf("_"));
		String sql = "";
		String numerator="", denominator="",result="",table_name="";
		table_name= Application.kpiNameMap.get(kpi_name+"_1").getOss_table_name();
		StringBuffer periodConditionBuffer = new StringBuffer();
		for (StartCycleWithEnd startCycleWithEnd: periods) {
			if (periodConditionBuffer.length()>0) {
				periodConditionBuffer.append(" OR");
			}else {
				periodConditionBuffer.append(" where (");
			}
			periodConditionBuffer.append(" PERIOD_START_TIME >= to_date('"+ startCycleWithEnd.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') and PERIOD_START_TIME < to_date('"+ startCycleWithEnd.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') ");
		}
		periodConditionBuffer.append(") ");
		String periodCondition = periodConditionBuffer.toString();
		if(kpi_name.equals("KPI_001") || kpi_name.equals("KPI_002") || kpi_name.equals("KPI_003") || kpi_name.equals("KPI_005") 
				 || kpi_name.equals("KPI_007") || kpi_name.equals("KPI_008") || kpi_name.equals("KPI_009") || kpi_name.equals("KPI_010")
				 || kpi_name.equals("KPI_017") || kpi_name.equals("KPI_025") || kpi_name.equals("KPI_027") || kpi_name.equals("KPI_028")){
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();//"sum(SIGN_CONN_ESTAB_ATT_MO_S +SIGN_CONN_ESTAB_ATT_MT +SIGN_CONN_ESTAB_ATT_MO_D +SIGN_CONN_ESTAB_ATT_OTHERS +SIGN_CONN_ESTAB_ATT_EMG)";
			result = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ PERIOD_START_TIME +"' PERIOD_START_TIME, \"MODE\", "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ result +" "
					+ "end as \"result\" "
					+ "from "+ table_name
					+" a,(SELECT OBJ_GID,case when (LNCEL_TDD_FRAME_CONF is not null and LNCEL_TSSC_296 is not null) then 'TDD' else 'FDD' end as \"MODE\" FROM C_LTE_LNCEL WHERE CONF_ID = 1) b"
//					+ " where PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
//					+ "and PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ periodCondition
					+ "and a.LNCEL_ID = b.OBJ_GID ";
			sql +="group by \"MODE\"";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_011") || kpi_name.equals("KPI_012") || kpi_name.equals("KPI_014") || kpi_name.equals("KPI_015") 
			 || kpi_name.equals("KPI_018") || kpi_name.equals("KPI_019") || kpi_name.equals("KPI_020") || kpi_name.equals("KPI_021")
			 || kpi_name.equals("KPI_022") || kpi_name.equals("KPI_023") || kpi_name.equals("KPI_030")
			 || kpi_name.equals("KPI_031") || kpi_name.equals("KPI_032") || kpi_name.equals("KPI_033") || kpi_name.equals("KPI_034")
			 || kpi_name.equals("KPI_035") || kpi_name.equals("KPI_036") || kpi_name.equals("KPI_037") || kpi_name.equals("KPI_038")
			 || kpi_name.equals("KPI_039") || kpi_name.equals("KPI_040") || kpi_name.equals("KPI_041") || kpi_name.equals("KPI_042")
			 || kpi_name.equals("KPI_043") || kpi_name.equals("KPI_044") || kpi_name.equals("KPI_046")){
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ PERIOD_START_TIME +"' PERIOD_START_TIME, \"MODE\", "
					+ numerator + " as \"result\" "
					+ "from "+ table_name
					+" a,(SELECT OBJ_GID,case when (LNCEL_TDD_FRAME_CONF is not null and LNCEL_TSSC_296 is not null) then 'TDD' else 'FDD' end as \"MODE\" FROM C_LTE_LNCEL WHERE CONF_ID = 1) b"
//					+ " where PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
//					+ "and PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ periodCondition
					+ "and a.LNCEL_ID = b.OBJ_GID ";
			sql +="group by \"MODE\"";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_004") || kpi_name.equals("KPI_026") || kpi_name.equals("KPI_029")){
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			String numerator_table = Application.kpiNameMap.get(kpi_name+"_2").getOss_table_name();
			String denominator_table = Application.kpiNameMap.get(kpi_name+"_3").getOss_table_name();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();
			result = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ PERIOD_START_TIME +"' PERIOD_START_TIME,\"MODE\", "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ result +" "
					+ "end as \"result\" "
					+ "from "+ numerator_table +" m, "+denominator_table +" n "
					+" ,(SELECT OBJ_GID,case when (LNCEL_TDD_FRAME_CONF is not null and LNCEL_TSSC_296 is not null) then 'TDD' else 'FDD' end as \"MODE\" FROM C_LTE_LNCEL WHERE CONF_ID = 1) b"
//					+ " where m.PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
//					+ " and  m.PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ periodCondition.replaceAll("PERIOD_START_TIME", "m.PERIOD_START_TIME")
					+ " and m.PERIOD_START_TIME=n.PERIOD_START_TIME "
					+ " and m.LNCEL_ID = n.LNCEL_ID "
					+ " and m.LNCEL_ID = b.OBJ_GID ";
			sql +="group by \"MODE\"";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
			
		}else if(kpi_name.equals("KPI_006")){
			numerator = Application.kpiNameMap.get(kpi_name+"_2").getFormula();
			denominator = Application.kpiNameMap.get(kpi_name+"_3").getFormula();
			result = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ PERIOD_START_TIME +"' PERIOD_START_TIME,b.CO_OBJECT_INSTANCE LNBTS_ID, "
					+ numerator + " as \"numerator\","
					+ denominator + " as \"denominator\","
					+ "case "+ denominator +" "
					+ "when 0 then 0 "
					+ "else "+ result +" "
					+ "end as \"result\" "
					+ "from "+ table_name +" a, CTP_COMMON_OBJECTS b"
//					+ " where PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS') "
//					+ " and  PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS') "
					+ periodCondition
					+ "and b.CO_GID=a.LNBTS_ID "
					+ "and b.CO_OC_ID = 2853 ";
			sql +="group by b.CO_OBJECT_INSTANCE";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_013") || kpi_name.equals("KPI_016")){
			sql = "SELECT '"+ PERIOD_START_TIME +"' PERIOD_START_TIME, OBJ_GID LNCEL_ID, "
				+ "case LNCEL_DL_CH_BW  "
				+ "when 14 then '6' "
				+ "when 30 then '15' "
				+ "when 50 then '25' "
				+ "when 100 then '50' "
				+ "when 150 then '75' "
				+ "when 200 then '100' "
				+ "else '100'"
				+ "end " 
				+ "as \"LNCEL_DL_CH_BW\", " 
				+ "case LNCEL_UL_CH_BW  "
				+ "when 14 then '6' "
				+ "when 30 then '15' "
				+ "when 50 then '25' "
				+ "when 100 then '50' "
				+ "when 150 then '75' "
				+ "when 200 then '100' "
				+ "else '100'"
				+ "end "
				+ "as \"LNCEL_UL_CH_BW\" "
				+ "from C_LTE_LNCEL "
				+ "where CONF_ID = 1 ";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
//			log.error("The kpi "+kpi_name_en+" is new added, please tell the administrator.");
		}else if(kpi_name.equals("KPI_024")){
			table_name = "NOKLTE_PS_LUEST_MNC1_RAW a,NOKLTE_PS_LS1AP_LNBTS_RAW b,NOKLTE_PS_LEPSB_MNC1_RAW c ";
			numerator = Application.kpiNameMap.get(kpi_name+"_1").getFormula();
			sql = "select '"+ PERIOD_START_TIME +"' PERIOD_START_TIME,\"MODE\","
					+"case SUM(A.SIGN_CONN_ESTAB_COMP)*SUM(A.SIGN_CONN_ESTAB_ATT_MO_S + A.SIGN_CONN_ESTAB_ATT_MT + A.SIGN_CONN_ESTAB_ATT_MO_D + A.SIGN_CONN_ESTAB_ATT_OTHERS + A.SIGN_CONN_ESTAB_ATT_EMG)*SUM(B.S1_SETUP_SUCC)*SUM(B.S1_SETUP_ATT)*SUM(C.EPS_BEARER_STP_COM_INI_QCI1 + C.EPS_BEARER_STP_COM_INI_QCI_2 + C.EPS_BEARER_STP_COM_INI_QCI_3 + C.EPS_BEARER_STP_COM_INI_QCI_4 + C.EPS_BEAR_STP_COM_INI_NON_GBR)*SUM(C.EPS_BEARER_STP_ATT_INI_QCI_1 + C.EPS_BEARER_STP_ATT_INI_QCI_2 + C.EPS_BEARER_STP_ATT_INI_QCI_3 + C.EPS_BEARER_STP_ATT_INI_QCI_4 + C.EPS_BEAR_STP_ATT_INI_NON_GBR)*SUM(C.EPS_BEARER_SETUP_COMPLETIONS - C.EPS_BEARER_STP_COM_INI_QCI1 - C.EPS_BEARER_STP_COM_INI_QCI_2 - C.EPS_BEARER_STP_COM_INI_QCI_3 - C.EPS_BEARER_STP_COM_INI_QCI_4 - C.EPS_BEAR_STP_COM_INI_NON_GBR)*SUM(C.EPS_BEARER_SETUP_ATTEMPTS - C.EPS_BEARER_STP_ATT_INI_QCI_1 - C.EPS_BEAR_STP_ATT_INI_NON_GBR - C.EPS_BEARER_STP_ATT_INI_QCI_2 - C.EPS_BEARER_STP_ATT_INI_QCI_3 - C.EPS_BEARER_STP_ATT_INI_QCI_4)" 
					+" when 0 then 0 else "
					+ numerator + " end as \"result\" "
					+ "from "+ table_name
					+" ,(SELECT OBJ_GID,case when (LNCEL_TDD_FRAME_CONF is not null and LNCEL_TSSC_296 is not null) then 'TDD' else 'FDD' end as \"MODE\" FROM C_LTE_LNCEL WHERE CONF_ID = 1) d"
//					+ " where a.PERIOD_START_TIME >= to_date('"+ periods.getStartCycle() +"','YYYY-MM-DD HH24:MI:SS')"
//					+ " and a.PERIOD_START_TIME < to_date('"+ periods.getEndCycle() +"','YYYY-MM-DD HH24:MI:SS')"
					+ periodCondition.replaceAll("PERIOD_START_TIME", "a.PERIOD_START_TIME")
					+ " and a.PERIOD_START_TIME=b.PERIOD_START_TIME"
					+ " and a.PERIOD_START_TIME=c.PERIOD_START_TIME"
					+ " and a.LNBTS_ID = b.LNBTS_ID"
					+ " and a.LNCEL_ID = c.LNCEL_ID"
					+ " and a.LNCEL_ID = d.OBJ_GID ";
			sql +=" group by \"MODE\"";
			resultList = ossSessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}else if(kpi_name.equals("KPI_045")){
			StringBuffer periodConditionBuffer4KPI045 = new StringBuffer();
			for (StartCycleWithEnd startCycleWithEnd: periods) {
				if (periodConditionBuffer4KPI045.length()>0) {
					periodConditionBuffer4KPI045.append(" OR");
				}else {
					periodConditionBuffer4KPI045.append(" where cdatetime =(SELECT MAX(cdatetime) FROM ices_bts_statistics where (");
				}
				periodConditionBuffer4KPI045.append(" cdatetime >= STR_TO_DATE('"+ startCycleWithEnd.getStartCycle() +"','%Y-%m-%d %H:%i:%s') and cdatetime < STR_TO_DATE('"+ startCycleWithEnd.getEndCycle() +"','%Y-%m-%d %H:%i:%s') ");
			}
			periodConditionBuffer4KPI045.append(")) ");
			table_name = "ices_bts_statistics";
			sql = "select '"+ PERIOD_START_TIME +"' PERIOD_START_TIME, key_name \"MODE\","
					+ "key_value \"result\" "
					+ "from "+ table_name
					+ periodConditionBuffer4KPI045
					+ " and key_type=2 ";
			resultList = Application.getInstance().getSessionFactoryByName("sessionFactory").getCurrentSession()
					.createSQLQuery(sql)
					.list();
		}
	
		log.info("  End to fetch data by city,omc_id="+omc_id +", kpi_id="+kpi_id +", period="+periods);
		return resultList;
		
	}

	/**
	 * 通过omc id获取该omc下所有基站的所有的cell,并对cell分组放入set中,每组以逗号分隔,数目不能超过一千个.
	 * Cacheable注解把相应的omc_id下的所有cell都缓存起来,方便后边使用.
	 * @param omc_id
	 * @return Set<String> 每一个值都是由不超过1千个cell_obj_gid拼接而成的.
	 */
//	@Cacheable("omcCell")
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
//	private String addCellConditions(Set<String> cells, String sql) {
//		String cellConditions = "";
//		for(String cell :cells){
//			cellConditions += "or LNCEL_ID in("+ cell +") ";
//		}
//		cellConditions = cellConditions.replaceFirst("or", "");
//		if(cells.size()>0){
//			sql += "and ("+ cellConditions +")";
//		}
//		return sql;
//	}
	
	private SessionFactory getSessionFactory(String omcName){
		return Application.getInstance().getSessionFactoryByName(
				"sessionFactory_" + omcName);
	}

	@Override
	public String sysdate(int omc_id){
		SessionFactory ossSessionFactory = getSessionFactory(Application.ossMap.get(omc_id).getOss_name());
		String sql = "select to_char(sysdate,'YYYY-MM-DD HH24:MI:SS')  FROM dual";
		return (String)ossSessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0);
	}
	
}
