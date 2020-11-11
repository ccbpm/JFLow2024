package bp.gpm;

import bp.da.*;
import bp.en.*;
import bp.en.Map;
import bp.port.Stations;
/** 
 权限组岗位
*/
public class GroupStation extends EntityMM
{
	private static final long serialVersionUID = 1L;
	///属性
	public final String getFK_Station()throws Exception
	{
		return this.GetValStringByKey(GroupStationAttr.FK_Station);
	}
	public final void setFK_Station(String value) throws Exception
	{
		this.SetValByKey(GroupStationAttr.FK_Station, value);
	}
	public final String getFK_Group()throws Exception
	{
		return this.GetValStringByKey(GroupStationAttr.FK_Group);
	}
	public final void setFK_Group(String value) throws Exception
	{
		this.SetValByKey(GroupStationAttr.FK_Group, value);
	}

		///


		///构造方法
	/** 
	 权限组岗位
	*/
	public GroupStation()
	{
	}
	/** 
	 权限组岗位
	 
	 @param mypk
	 * @throws Exception 
	*/
	public GroupStation(String no) throws Exception
	{
		this.Retrieve();
	}
	/** 
	 权限组岗位
	*/
	@Override
	public Map getEnMap() throws Exception
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}
		Map map = new Map("GPM_GroupStation");
		map.setDepositaryOfEntity( Depositary.None);
		map.setDepositaryOfMap(Depositary.Application);
		map.setEnDesc("权限组岗位");
		map.setEnType( EnType.Sys);

		map.AddTBStringPK(GroupStationAttr.FK_Group, null, "权限组", false, false, 0, 50, 20);
		map.AddDDLEntitiesPK(GroupStationAttr.FK_Station, null, "岗位", new Stations(), true);

		this.set_enMap(map);
		return this.get_enMap();
	}

		///
}