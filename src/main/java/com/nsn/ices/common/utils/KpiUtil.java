package com.nsn.ices.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class KpiUtil {

	public static Set<String> getStringSetFromMap(Set<Integer> set, int limit) {
		StringBuffer sbBuffer = new StringBuffer();
		int i=0;
		Set<String> resultSet = new HashSet<String>();
		for(Integer cell_id: set){
			sbBuffer.append(cell_id);
			if(i<999){
				sbBuffer.append(",");
			}else{
				resultSet.add(sbBuffer.toString());
				sbBuffer = new StringBuffer();
				i=0;
			}
			i++;
		}
		return resultSet;
	}
	
	/**
	 * get the latest 15 minutes cycle. e.g:2014-07-11 14:00:00
	 * @return
	 * @author: yudq
	 */
	public static String latest15MinutesCycle(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
		String currentTime = dateFormat.format(new Date());
		int minutes = Calendar.getInstance().get(Calendar.MINUTE);
		if(minutes > 0 && minutes <=15){
			currentTime+=":00:00";
		}else if(minutes > 15 && minutes <=30){
			currentTime+=":15:00";
		}else if(minutes > 30 && minutes <=45){
			currentTime+=":30:00";
		}else if(minutes > 45 && minutes <60){
			currentTime+=":45:00";
		}
		return currentTime;
	}
}
