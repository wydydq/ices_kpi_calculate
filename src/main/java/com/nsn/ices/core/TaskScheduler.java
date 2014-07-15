package com.nsn.ices.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.nsn.ices.model.entity.Bts;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.entity.Kpi;
import com.nsn.ices.model.entity.KpiResult;
import com.nsn.ices.model.entity.Oss;
import com.nsn.ices.model.service.BtsService;
import com.nsn.ices.model.service.CellService;
import com.nsn.ices.model.service.KpiService;
import com.nsn.ices.model.service.OmcService;
import com.nsn.ices.model.service.OssService;

/**
 * Schedule tasks.
 * 
 */
@EnableScheduling
public class TaskScheduler {
	
	@Autowired
	private KpiService kpiService;
	@Autowired
	private CellService cellService;
	@Autowired
	private BtsService btsService;
	@Autowired
	private OssService ossService;
	@Autowired
	private OmcService omcService;
	@Autowired
	private TaskExecutor taskExecutor;

	private static final Logger logger = Logger.getLogger(TaskScheduler.class);
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	
	@Scheduled(fixedDelay=50000)
	public void getKpi(){
		List<Kpi> list = kpiService.getAllKpi();
		for(Kpi kpi : list){
			Application.kpiMap.put(kpi.getId(), kpi);
			Application.kpiNameMap.put(kpi.getKpi_name_en(), kpi);
		}
	}
	@Scheduled(fixedDelay=50000,initialDelay=1000)
	public void getCell(){
		List<Cell> list = cellService.getAllActiveCell();
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		Map<Integer, Map<Integer, Cell>> btsCellMap = Application.btsCellMap;
		Map<Integer, KpiResult> kpiResultMap = Application.kpiResultMap;
		for(Cell cell : list){
			Application.cellMap.put(cell.getCell_obj_gid(), cell);
			int oss_id = Application.btsMap.get(cell.getNe_code()).getOss_id();
			int ne_code = cell.getNe_code();
			if(ossCellMap.containsKey(oss_id)){
				ossCellMap.get(oss_id).put(cell.getCell_obj_gid(),cell);
			}else {
				Map<Integer, Cell> btsMap = new HashMap<Integer, Cell>();
				btsMap.put(cell.getCell_obj_gid(),cell);
				ossCellMap.put(oss_id, btsMap);
			}
			if(btsCellMap.containsKey(ne_code)){
				btsCellMap.get(ne_code).put(cell.getCell_obj_gid(),cell);
			}else {
				Map<Integer, Cell> cellMap = new HashMap<Integer, Cell>();
				cellMap.put(cell.getCell_obj_gid(),cell);
				btsCellMap.put(ne_code, cellMap);
			}
			kpiResultMap.put(cell.getCell_obj_gid(), new KpiResult());
		}
	}
	@Scheduled(fixedDelay=50000)
	public void getBts(){
		List<Bts> list = btsService.getAllBts();
		Map<Integer, List<Bts>> ossBtsMap = Application.ossBtsMap;
		for(Bts bts : list){
			Application.btsMap.put(bts.getNe_code(), bts);
			if(ossBtsMap.containsKey(bts.getOss_id())){
				ossBtsMap.get(bts.getOss_id()).add(bts);
			}else {
				List<Bts> btsList = new ArrayList<Bts>();
				btsList.add(bts);
				ossBtsMap.put(bts.getOss_id(), btsList);
			}
		}
		
		
	}
	
	@Scheduled(fixedDelay=50000)
	public void getOss(){
		List<Oss> ossList = ossService.getAllOss();
		for(Oss oss : ossList){
			Application.ossMap.put(oss.getId(), oss);
		}
	}
	
//	@Scheduled(cron="0 0/2 * * * *")
	@Scheduled(fixedDelay=150000,initialDelay=2000)
	public void calculate(){
		logger.info("Schedule task begin at " + dateFormat.format(new Date()));
		Application.count = 0;
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		logger.info(ossCellMap.size());
		Map<Integer, Kpi> kpiMap = Application.kpiMap;
		Set<Integer> ossSet = ossCellMap.keySet();
		for(final int oss_id : ossSet){
			final String cycle = omcService.getCycle(oss_id);
			for(final int kpi_id: kpiMap.keySet()){
				if (!kpiMap.get(kpi_id).getKpi_name_en().endsWith("1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_016_1")) {
					continue;
				}
//				if (!kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_006_1")) {
//					continue;
//				}
				taskExecutor.execute(new Runnable() {
					public void run() {
						omcService.syncing(oss_id, kpi_id, cycle);
					}
				});
			}
		}
		logger.info("Schedule task end at " + dateFormat.format(new Date()));
	}
	
}

