package BP.GPM;

import BP.DA.*;
import BP.En.*;

/** 
 控制方式
 
*/
public enum CtrlWay
{
	/** 
	 游客
	 
	*/
	Guest,
	/** 
	 任何人
	 
	*/
	AnyOne,
	/** 
	 按岗位
	 
	*/
	ByStation,
	/** 
	 按部门
	 
	*/
	ByDept,
	/** 
	 按人员
	 
	*/
	ByEmp,
	/** 
	 按sql
	 
	*/
	BySQL;

	public int getValue()
	{
		return this.ordinal();
	}

	public static CtrlWay forValue(int value)
	{
		return values()[value];
	}
}