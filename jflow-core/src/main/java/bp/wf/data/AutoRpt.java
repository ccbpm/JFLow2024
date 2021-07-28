package bp.wf.data;

import bp.dts.Auto_Rpt_Dtl_DTS;
import bp.en.EntityMyPK;
import bp.en.Map;
import bp.en.RefMethod;
import bp.en.RefMethodType;
import bp.wf.Dev2Interface;

public class AutoRpt  extends EntityMyPK {

	//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
   ///#region 属性
	/** 
	 发起时间点
	 * @throws Exception 
	*/
	public final String getStartDT() throws Exception
	{
		return this.GetValStringByKey(AutoRptAttr.StartDT);
	}
	public final void setStartDT(String value) throws Exception
	{
		this.SetValByKey(AutoRptAttr.StartDT, value);
	}
	/** 
	 执行的时间点.
	 * @throws Exception 
	*/
	public final String getDots() throws Exception
	{
		return this.GetValStringByKey(AutoRptAttr.Dots);
	}
	public final void setDots(String value) throws Exception
	{
		this.SetValByKey(AutoRptAttr.Dots, value);
	}
	/** 
	 执行时间
	 * @throws Exception 
	*/
	public final String getDTOfExe() throws Exception
	{
		return this.GetValStringByKey(AutoRptAttr.DTOfExe);
	}
	public final void setDTOfExe(String value) throws Exception
	{
		this.SetValByKey(AutoRptAttr.DTOfExe, value);
	}
	/** 
	 到达的人员
	 * @throws Exception 
	*/
	public final String getToEmps() throws Exception
	{
		return this.GetValStringByKey(AutoRptAttr.ToEmps);
	}
	public final void setToEmps(String value) throws Exception
	{
		this.SetValByKey(AutoRptAttr.ToEmps, value);
	}
	public final String getToEmpOfSQLs() throws Exception
	{
		return this.GetValStringByKey(AutoRptAttr.ToEmpOfSQLs);
	}
	public final void setToEmpOfSQLs(String value) throws Exception
	{
		this.SetValByKey(AutoRptAttr.ToEmpOfSQLs, value);
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#region 构造函数
	/** 
	 AutoRpt
	*/
//C# TO JAVA CONVERTER WARNING: The following constructor is declared outside of its associated class:
//ORIGINAL LINE: public AutoRpt()
	public AutoRpt()
	{

	}
	/** 
	 重写基类方法
	 * @throws Exception 
	*/
	@Override
	public Map getEnMap() throws Exception
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}

		Map map = new Map("WF_AutoRpt", "自动报表");
		map.setCodeStruct("2");

		map.AddTBStringPK(AutoRptAttr.No, null, "编号", true, true, 2, 2, 2);
		map.AddTBDateTime(AutoRptAttr.DTOfExe, null, "最近执行时间", true, true);

		map.AddTBString(AutoRptAttr.Name, null, "标题", true, false, 0, 200, 200, true);
		map.AddTBString(AutoRptAttr.StartDT, null, "发起时间点", true, false, 0, 200, 200, true);
		map.SetHelperAlert(AutoRptAttr.StartDT, "比如:08:03,20:15   多个时间点用逗号分开，一定是HH:mm格式的时间点.");

			// map.AddTBString(AutoRptAttr.ToEmps, null, "通知的人员ID", true, false, 0, 200, 10);
		map.AddTBString(AutoRptAttr.ToEmpOfSQLs, null, "通知的人员SQL", true, false, 0, 500, 10,true);
		map.SetHelperAlert(AutoRptAttr.ToEmpOfSQLs,"查询出来要通知的人员，返回列必须是No,Name 比如:SELECT top 100 No,Name FROM Port_Emp ");

			//map.AddTBString(AutoRptAttr.ToStations, null, "通知的岗位", true, false, 0, 200, 10);
			//map.AddTBString(AutoRptAttr.ToDepts, null, "通知的部门", true, false, 0, 200, 10);


		map.AddTBStringDoc(AutoRptAttr.Dots, null, "执行的时间点(系统写入)", true, true, true);
		map.SetHelperAlert(AutoRptAttr.Dots, "系统的日志，曾经发起的时间点记录.格式为:2020-09-10 20:22,2020-09-10 22:22");

			//
		map.AddDtl(new AutoRptDtls(), AutoRptDtlAttr.AutoRptNo);

		RefMethod rm = new RefMethod();
		rm.Title = "手工执行";
		rm.ClassMethodName = this.toString() + ".DoIt";
		rm.refMethodType=RefMethodType.Func;
		map.AddRefMethod(rm);

		this.set_enMap(map);
		return this.get_enMap();
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#endregion

	public final String DoIt() throws Exception
	{
		Auto_Rpt_Dtl_DTS dts = new Auto_Rpt_Dtl_DTS();
		Object tempVar = dts.Do();
		String str = tempVar instanceof String ? (String)tempVar : null;

		Dev2Interface.Port_Login("admin");

		return str;
	}

}
