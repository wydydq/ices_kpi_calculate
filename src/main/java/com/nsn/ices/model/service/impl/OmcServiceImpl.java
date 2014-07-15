package com.nsn.ices.model.service.impl;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nsn.ices.common.annotation.ResourceValue;
import com.nsn.ices.common.utils.KpiUtil;
import com.nsn.ices.core.Application;
import com.nsn.ices.model.dao.KpiDao;
import com.nsn.ices.model.dao.OmcDao;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.entity.KpiResult;
import com.nsn.ices.model.service.OmcService;

@Service(value="omcService")
public class OmcServiceImpl implements OmcService {
	
	@Autowired
	private OmcDao omcDao;
	
	@Autowired
	private KpiDao kpiDao;
	
	@ResourceValue(value="loadDataLocalFileDirectory")
	private String loadDataLocalFileDirectory;
	
	Logger log = Logger.getLogger(OmcServiceImpl.class);
	
	private final static String LOAD_FORMAT_RESULT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (ne_code,cel_id,cdatetime,result,is_ok)";
	private final static String LOAD_FORMAT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (period,kpi_id,cel_obj_gid,result)";
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Transactional
	public void syncing(int omc_id, int kpi_id, String period){
		log.info("Syncing data at "+period+", omc_id: "+omc_id+", kpi_id: "+kpi_id);
		try {
			String kpi_name_en = Application.kpiMap.get(kpi_id).getKpi_name_en();
			String kpi_name = kpi_name_en.substring(0, kpi_name_en.lastIndexOf("_"));
			List<Object[]> resultList = omcDao.find(omc_id, kpi_id, period);
			log.info("KPI:"+kpi_name_en+", cycle:"+period+", result list size: "+resultList.size());
			if(resultList.size()==0){
				log.info("Can not fetch data,return.");
				return;
			}
			String filename = saveToFile(resultList, kpi_name);
			String loadsql = String.format(LOAD_FORMAT, filename,"ices_kpi_result_detail");
			log.info("loadsql: "+loadsql);
			kpiDao.loadData(loadsql);
			new File(filename).delete();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			Application.count ++;
			if(Application.count == 22){
				Map<Integer, KpiResult> kpiResultMap = Application.kpiResultMap;
				String filename = loadDataLocalFileDirectory+"ices_kpi_result_" + dateFormat.format(new Date()) + ".txt";
				log.info("Temporary filename: "+filename);
				StringBuffer sb = new StringBuffer();
				int i=1;
				File file = new File(filename);
				FileWriter fw = null;
				BufferedWriter bw =null;
				try {
					if(file.exists()){
						if(!file.delete()){
							log.error("The file already exists and get exception when delete it, please close it if it is opened.");

						}
					}
					file.createNewFile();
					fw = new FileWriter(file, true);
					bw = new BufferedWriter(fw);
					for(int cell_id: kpiResultMap.keySet()){
						sb.append("`");
						sb.append(Application.cellMap.get(cell_id).getNe_code());
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(period);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpiResultMap.get(cell_id).getResultMap());
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpiResultMap.get(cell_id).getIsOkJson());
						sb.append("`");
						sb.append("@");
						sb.append("\n");
						i++;
						if(i % 1000 == 0){
							bw.append(sb);
							bw.flush();
							sb = new StringBuffer();
							i=1;
						}
						kpiResultMap.get(cell_id).getResultMap().clear();
						kpiResultMap.get(cell_id).getIsOkSet().clear();
					}
					bw.append(sb);
					bw.flush();
					String loadsql = String.format(LOAD_FORMAT_RESULT, filename,"ices_kpi_result");
					log.info("loadsql: "+loadsql);
					kpiDao.loadData(loadsql);
					file.delete();
				} catch (IOException e1) {
					e1.printStackTrace();
				}finally{
					if(bw != null){
						try {
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(fw != null){
						try {
							fw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(file!=null && file.exists()){
						file.delete();
					}
				}
			}
		}
		log.info("Synced data at "+period+", omc_id: "+omc_id+", kpi_id: "+kpi_id);
		
	}
	
	/**
	 * Save the given result list to a temporary file,which the filename is generated using kpi name,date time, if that file is just in there,delete it.
	 * Note: When you finish load data from the file,remember to delete it in order not to hold the limited space.We use different method organize the data through kpi name or the returned columns count,cause 
	 * 	     different kpi has different  return value,however the difference is small.The following kpi must take more care:KPI_006_1,KPI_013_1,KPI_016_1.
	 * @param resultList
	 * @param kpi_name
	 * @return
	 */
	private String saveToFile(List<Object[]> resultList, String kpi_name){
		log.info("Attention please,saving data...");
		String filename = loadDataLocalFileDirectory+"ices_kpi_result_detail_" + kpi_name + "_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		Map<Integer, KpiResult> kpiResultMap = Application.kpiResultMap;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
					return filename;
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			int columnCount = resultList.get(0).length;
			if("KPI_006".equals(kpi_name)){
				Map<Integer, Map<Integer, Cell>> btsCellMap = Application.btsCellMap;
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_2 = Application.kpiNameMap.get(kpi_name+"_2").getId();
				int kpi_id_3 = Application.kpiNameMap.get(kpi_name+"_3").getId();
				boolean hasThreshold_kpi_id_1 = hasThreshold(kpi_id_1);
				boolean hasThreshold_kpi_id_2 = hasThreshold(kpi_id_2);
				boolean hasThreshold_kpi_id_3 = hasThreshold(kpi_id_3);
				double threshold_kpi_id_1 = getThreshold(kpi_id_1);
				double threshold_kpi_id_2 = getThreshold(kpi_id_2);
				double threshold_kpi_id_3 = getThreshold(kpi_id_3);
				String relation_kpi_id_1 = getRelation(kpi_id_1);
				String relation_kpi_id_2 = getRelation(kpi_id_2);
				String relation_kpi_id_3 = getRelation(kpi_id_3);
				for(Object[] objects : resultList){
					int ne_code = Integer.parseInt(objects[1].toString());
					Map<Integer, Cell> cellMap = btsCellMap.get(ne_code);
					if(cellMap == null || cellMap.size()==0){
						continue;
					}
					for(int cell_id: cellMap.keySet()){
						kpiResultMap.get(cell_id).getResultMap().put(kpi_id_1, objects[4]+"");
						kpiResultMap.get(cell_id).getResultMap().put(kpi_id_2, objects[2]+"");
						kpiResultMap.get(cell_id).getResultMap().put(kpi_id_3, objects[3]+"");
						if (hasThreshold_kpi_id_1) {
							if(objects[4] == null){
								log.error("Get null value: " + objects[0] + objects[1] + objects[2] + objects[3] + objects[4] + ",ignore cell "+cell_id);
								continue;
							}
							double realValue = Double.parseDouble(objects[4]+"");
							if(compareTo(realValue, threshold_kpi_id_1, relation_kpi_id_1)){
								kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_1);
							}
						}
						if (hasThreshold_kpi_id_2) {
							double realValue = Double.parseDouble(objects[2]+"");
							if(compareTo(realValue, threshold_kpi_id_2, relation_kpi_id_2)){
								kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_2);
							}
						}
						if (hasThreshold_kpi_id_3) {
							double realValue = Double.parseDouble(objects[3]+"");
							if(compareTo(realValue, threshold_kpi_id_3, relation_kpi_id_3)){
								kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_3);
							}
						}
						sb.append("`");
						sb.append(objects[0]);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpi_id_2);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(objects[2]);
						sb.append("`");
						sb.append("@");
						sb.append("\n");
						
						sb.append("`");
						sb.append(objects[0]);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpi_id_3);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(objects[3]);
						sb.append("`");
						sb.append("@");
						sb.append("\n");
						
						sb.append("`");
						sb.append(objects[0]);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpi_id_1);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(objects[4]);
						sb.append("`");
						sb.append("@");
						sb.append("\n");
					}
					i++;
					if(i % 1000 == 0){
						bw.append(sb);
						bw.flush();
						sb = new StringBuffer();
						i=1;
					}
				}
			}else if("KPI_013".equals(kpi_name)){
				int kpi_id_13 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_16 = Application.kpiNameMap.get("KPI_016_1").getId();
				for(Object[] objects : resultList){
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_13, objects[2]+"");
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_16, objects[3]+"");
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_13);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_16);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[3]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					i++;
					if(i % 1000 == 0){
						bw.append(sb);
						bw.flush();
						sb = new StringBuffer();
						i=1;
					}
				}
			}else if(columnCount==5){
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_2 = Application.kpiNameMap.get(kpi_name+"_2").getId();
				int kpi_id_3 = Application.kpiNameMap.get(kpi_name+"_3").getId();
				boolean hasThreshold_kpi_id_1 = hasThreshold(kpi_id_1);
				boolean hasThreshold_kpi_id_2 = hasThreshold(kpi_id_2);
				boolean hasThreshold_kpi_id_3 = hasThreshold(kpi_id_3);
				double threshold_kpi_id_1 = getThreshold(kpi_id_1);
				double threshold_kpi_id_2 = getThreshold(kpi_id_2);
				double threshold_kpi_id_3 = getThreshold(kpi_id_3);
				String relation_kpi_id_1 = getRelation(kpi_id_1);
				String relation_kpi_id_2 = getRelation(kpi_id_2);
				String relation_kpi_id_3 = getRelation(kpi_id_3);
				for(Object[] objects : resultList){
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_1, objects[4]+"");
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_2, objects[2]+"");
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_3, objects[3]+"");
					if (hasThreshold_kpi_id_1) {
						double realValue = Double.parseDouble(objects[4]+"");
						if(compareTo(realValue, threshold_kpi_id_1, relation_kpi_id_1)){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
						}
					}
					if (hasThreshold_kpi_id_2) {
						double realValue = Double.parseDouble(objects[2]+"");
						if(compareTo(realValue, threshold_kpi_id_2, relation_kpi_id_2)){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_2);
						}
					}
					if (hasThreshold_kpi_id_3) {
						double realValue = Double.parseDouble(objects[3]+"");
						if(compareTo(realValue, threshold_kpi_id_3, relation_kpi_id_3)){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_3);
						}
					}
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_2);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_3);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[3]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_1);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[4]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					i++;
					if(i % 1000 == 0){
						bw.append(sb);
						bw.flush();
						sb = new StringBuffer();
						i=1;
					}
				}
			}else if(columnCount == 3){
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				boolean hasThreshold_kpi_id_1 = hasThreshold(kpi_id_1);
				double threshold_kpi_id_1 = getThreshold(kpi_id_1);
				String relation_kpi_id_1 = getRelation(kpi_id_1);
				for(Object[] objects : resultList){
					if(kpiResultMap.containsKey(Integer.parseInt(objects[1]+"")))
						kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_1, objects[2]+"");
					else {
						log.error("kpiResultMap does't contain cell: "+kpi_name +", "+objects[0]+","+objects[1]+","+objects[2]);
						continue;
					}
					if (hasThreshold_kpi_id_1) {
						double realValue = Double.parseDouble(objects[2]+"");
						if(compareTo(realValue, threshold_kpi_id_1, relation_kpi_id_1)){
							try {
								kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_1);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					i++;
					if(i % 1000 == 0){
						bw.append(sb);
						bw.flush();
						sb = new StringBuffer();
						i=1;
					}
				}
			}
			bw.append(sb);
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("Wow, save data successfully.");
		return filename;
	}
	
	/**
	 * 
	 */
	public String getCycle(int omc_id){
		String latest15MinutesCycle = KpiUtil.latest15MinutesCycle();
		log.info("The current cycle on lte server is: " + latest15MinutesCycle);
		omcDao.maxCycle(omc_id, "NOKLTE_PS_LUEST_MNC1_RAW");
		log.info("Comparing max cycle in different tables on omc.");
		Set<String> set = new HashSet<String>();
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LS1AP_LNBTS_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LCELLD_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LEPSB_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LRRC_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LIANBHO_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LCELLR_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LCELLT_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LUEST_MNC1_RAW"));
		set.add(omcDao.maxCycle(omc_id, "NOKLTE_PS_LIENBHO_MNC1_RAW"));
		if(set.size()>1){
			log.error("The max cycles in 9 tables is different,please check.");
			return "0000-00-00 00:00:00";
		}else {
			String maxCycle = set.iterator().next();
			if (!latest15MinutesCycle.equals(maxCycle)) {
				log.info("The latest 15 minutes cycle is different from the max cycle of omc database,please synchronize the local time with OMC server.");
			}
			return maxCycle;
		}
	}
	
	/**
	 * 是否有阈值(0:有,1:没有)
	 * @param kpi_id
	 * @return
	 */
	private boolean hasThreshold(int kpi_id){
		return Application.kpiMap.get(kpi_id).getHas_threshold() == 0; 
	}
	
	/**
	 * 获取指标的阈值
	 * @param kpi_id
	 * @return
	 */
	private double getThreshold(int kpi_id){
		return Application.kpiMap.get(kpi_id).getThreshold();
	}
	
	private String getRelation(int kpi_id){
		return Application.kpiMap.get(kpi_id).getRelation();
	}
	
	/**
	 * return the value of [v1 relation v2] 
	 * @param v1 double
	 * @param v2 double
	 * @param relation 关系运算符(<,<=,>,>=,==,!=)
	 * @return
	 */
	private boolean compareTo(double v1, double v2, String relation){
		boolean result = false;
		switch (relation) {
		case ">":
			if (v1 > v2) {
				result = true;
			}
			break;
		case ">=":
			if (v1 >= v2) {
				result = true;
			}
			break;
		case "<":
			if (v1 < v2) {
				result = true;
			}
			break;
		case "<=":
			if (v1 <= v2) {
				result = true;
			}
			break;
		case "==":
			if (v1 == v2) {
				result = true;
			}
			break;
		case "!=":
			if (v1 != v2) {
				result = true;
			}
			break;
		default:
			break;
		}
		
		return result;
	}
}
