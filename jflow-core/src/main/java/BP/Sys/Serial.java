package BP.Sys;

import BP.DA.*;
import BP.Difference.SystemConfig;
import BP.En.*;
import BP.En.Map;

import java.util.*;

/** 
 序列号
*/
public class Serial extends Entity
{

		///#region 基本属性
	/** 
	 序列号
	 * @throws Exception 
	*/
	public final String getIntVal() throws Exception
	{
		return this.GetValStringByKey(SerialAttr.IntVal);
	}
	public final void setIntVal(String value) throws Exception
	{
		this.SetValByKey(SerialAttr.IntVal, value);
	}
	/** 
	 操作员ID
	 * @throws Exception 
	*/
	public final String getCfgKey() throws Exception
	{
		return this.GetValStringByKey(SerialAttr.CfgKey);
	}
	public final void setCfgKey(String value) throws Exception
	{
		this.SetValByKey(SerialAttr.CfgKey, value);
	}

		///#endregion


		///#region 构造方法

	/** 
	 序列号
	*/
	public Serial()
	{
	}
	/** 
	 map
	*/
	@Override
	public Map getEnMap()
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}
		Map map = new Map("Sys_Serial", "序列号");
		map.setEnType(EnType.Sys);
		map.setDepositaryOfEntity(Depositary.None);
		map.AddTBStringPK(SerialAttr.CfgKey, "OID", "CfgKey", false, true, 1, 100, 10);
		map.AddTBInt(SerialAttr.IntVal, 0, "属性", true, false);
		this.set_enMap(map);
		return this.get_enMap();
	}

		///#endregion

	public final int Gener(String CfgKey) throws Exception
	{
		Paras ps = new Paras();
		ps.Add("p", CfgKey);

		String sql = "SELECT IntVal Sys_Serial WHERE CfgKey=" + SystemConfig.getAppCenterDBVarStr() + "p";
		int val = DBAccess.RunSQLReturnValInt(sql, 0, ps);
		if (val == 0)
		{
			sql = "INSERT INTO Sys_Serial VALUES(" + SystemConfig.getAppCenterDBVarStr() + "p,1)";
			DBAccess.RunSQLReturnVal(sql, ps);
			return 1;
		}
		else
		{
			val++;
			ps.Add("intV", val);
			sql = "UPDATE  Sys_Serial SET IntVal=" + SystemConfig.getAppCenterDBVarStr() + "intV WHERE  CfgKey=" + SystemConfig.getAppCenterDBVarStr() + "p";
			DBAccess.RunSQLReturnVal(sql);
			return val;
		}
	}
}