package com.nsn.ices.common.utils;

public class KpiCalculateUtil {
	public static String getIcesKpiResultTableByGranularity(int granularity) {
		String icesKpiResultTable = "ices_kpi_result";
		switch (granularity) {
		case 15:
			icesKpiResultTable = Constands.ICESKPIRESULT;
			break;
		case 30:
			icesKpiResultTable = Constands.ICESKPIRESULT30;
			;
			break;
		case 60:
			icesKpiResultTable = Constands.ICESKPIRESULT60;
			break;
		case 1:
			icesKpiResultTable = Constands.ICESKPIRESULT1;
			break;
		default:
			break;
		}
		return icesKpiResultTable;
	}

	public static String getIcesKpiResultDetailTableByGranularity(int granularity) {
		String icesKpiResultDetailTable = "ices_kpi_result_detail";
		switch (granularity) {
		case 15:
			icesKpiResultDetailTable = Constands.ICESKPIRESULTDETAIL;
			break;
		case 30:
			icesKpiResultDetailTable = Constands.ICESKPIRESULTDETAIL30;
			;
			break;
		case 60:
			icesKpiResultDetailTable = Constands.ICESKPIRESULTDETAIL60;
			break;
		case 1:
			icesKpiResultDetailTable = Constands.ICESKPIRESULTDETAIL1;
			break;
		default:
			break;
		}
		return icesKpiResultDetailTable;
	}
}
