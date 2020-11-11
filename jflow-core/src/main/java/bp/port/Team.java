package bp.port;

import bp.da.*;
import bp.en.*;
import bp.en.Map;

/**
 * 用户组
 */
public class Team extends EntityNoName {

	private static final long serialVersionUID = 1L;

	/// 构造方法
	/**
	 * 用户组
	 */
	public Team() {
	}

	/**
	 * 用户组
	 * 
	 * @param mypk
	 * @throws Exception 
	 */
	public Team(String no) throws Exception {
		this.setNo(no);
		this.Retrieve();
	}

	/**
	 * EnMap
	 */
	@Override
	public Map getEnMap() throws Exception {
		if (this.get_enMap() != null) {
			return this.get_enMap();
		}

		Map map = new Map("Port_Team");
		map.setDepositaryOfEntity(Depositary.None);
		map.setEnDesc("用户组");
		map.setEnType(EnType.Sys);
		map.setIsAutoGenerNo(true);

		map.AddTBStringPK(TeamAttr.No, null, "编号", true, true, 3, 3, 3);
		map.AddTBString(TeamAttr.Name, null, "名称", true, false, 0, 300, 20);
		map.AddDDLEntities(TeamAttr.FK_TeamType, null, "类型", new TeamTypes(), true);

		// map.AddTBString(TeamAttr.ParentNo, null, "父亲节编号", true, true, 0, 100,
		// 20);
		map.AddTBInt(TeamAttr.Idx, 0, "显示顺序", true, false);

		map.AddSearchAttr(TeamAttr.FK_TeamType);

		map.getAttrsOfOneVSM().Add(new bp.port.TeamEmps(), new Emps(), TeamEmpAttr.FK_Team, TeamEmpAttr.FK_Emp,
				EmpAttr.Name, EmpAttr.No, "人员(简单)");

		// 节点绑定人员. 使用树杆与叶子的模式绑定.
		map.getAttrsOfOneVSM().AddBranchesAndLeaf(new bp.port.TeamEmps(), new bp.port.Emps(), TeamEmpAttr.FK_Team,
				TeamEmpAttr.FK_Emp, "人员(树)", EmpAttr.FK_Dept, EmpAttr.Name, EmpAttr.No, "@WebUser.FK_Dept");

		// map.getAttrsOfOneVSM().Add(new TeamEmps(), new Emps(),
		// TeamEmpAttr.FK_Team, TeamEmpAttr.FK_Emp, EmpAttr.Name, EmpAttr.No,
		// "人员(简单)");

		// map.getAttrsOfOneVSM().Add(new TeamStations(), new Stations(),
		// TeamEmpAttr.FK_Team, TeamStationAttr.FK_Station, EmpAttr.Name,
		// EmpAttr.No, "岗位(简单)");

		// map.getAttrsOfOneVSM().AddTeamListModel(new TeamStations(), new
		// bp.port.Stations(),
		// TeamStationAttr.FK_Team,
		// TeamStationAttr.FK_Station, "岗位(平铺)", StationAttr.FK_StationType);

		this.set_enMap(map);
		return this.get_enMap();
	}

	///
}