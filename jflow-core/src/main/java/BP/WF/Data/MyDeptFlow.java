package BP.WF.Data;

import BP.DA.*;
import BP.WF.*;
import BP.Web.WebUser;
import BP.Port.*;
import BP.Sys.*;
import BP.En.*;
import BP.En.Map;
import BP.WF.*;
import java.util.*;

/** 
 我部门的流程
*/
public class MyDeptFlow extends Entity
{

		///#region 基本属性
	@Override
	public UAC getHisUAC()
	{
		UAC uac = new UAC();
		uac.Readonly();
		return uac;
	}
	/** 
	 主键
	*/
	@Override
	public String getPK()
	{
		return MyDeptFlowAttr.WorkID;
	}
	/** 
	 备注
	*/
	public final String getFlowNote() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.FlowNote);
	}
	public final void setFlowNote(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FlowNote, value);
	}
	/** 
	 工作流程编号
	*/
	public final String getFK_Flow() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.FK_Flow);
	}
	public final void setFK_Flow(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FK_Flow,value);
	}
	/** 
	 BillNo
	*/
	public final String getBillNo() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.BillNo);
	}
	public final void setBillNo(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.BillNo, value);
	}
	/** 
	 流程名称
	*/
	public final String getFlowName() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.FlowName);
	}
	public final void setFlowName(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FlowName, value);
	}
	/** 
	 优先级
	*/
	public final int getPRI() throws Exception
	{
		return this.GetValIntByKey(MyDeptFlowAttr.PRI);
	}
	public final void setPRI(int value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.PRI, value);
	}
	/** 
	 待办人员数量
	*/
	public final int getTodoEmpsNum() throws Exception
	{
		return this.GetValIntByKey(MyDeptFlowAttr.TodoEmpsNum);
	}
	public final void setTodoEmpsNum(int value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.TodoEmpsNum, value);
	}
	/** 
	 待办人员列表
	*/
	public final String getTodoEmps() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.TodoEmps);
	}
	public final void setTodoEmps(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.TodoEmps, value);
	}
	/** 
	 参与人
	*/
	public final String getEmps() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.Emps);
	}
	public final void setEmps(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.Emps, value);
	}
	/** 
	 状态
	*/
	public final TaskSta getTaskSta() throws Exception
	{
		return TaskSta.forValue(this.GetValIntByKey(MyDeptFlowAttr.TaskSta));
	}
	public final void setTaskSta(TaskSta value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.TaskSta, value.getValue());
	}
	/** 
	 类别编号
	*/
	public final String getFK_FlowSort() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.FK_FlowSort);
	}
	public final void setFK_FlowSort(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FK_FlowSort, value);
	}
	/** 
	 部门编号
	*/
	public final String getFK_Dept() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.FK_Dept);
	}
	public final void setFK_Dept(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FK_Dept,value);
	}
	/** 
	 标题
	*/
	public final String getTitle() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.Title);
	}
	public final void setTitle(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.Title,value);
	}
	/** 
	 客户编号
	*/
	public final String getGuestNo() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.GuestNo);
	}
	public final void setGuestNo(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.GuestNo, value);
	}
	/** 
	 客户名称
	*/
	public final String getGuestName() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.GuestName);
	}
	public final void setGuestName(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.GuestName, value);
	}
	/** 
	 产生时间
	*/
	public final String getRDT() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.RDT);
	}
	public final void setRDT(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.RDT,value);
	}
	/** 
	 节点应完成时间
	*/
	public final String getSDTOfNode() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.SDTOfNode);
	}
	public final void setSDTOfNode(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.SDTOfNode, value);
	}
	/** 
	 流程应完成时间
	*/
	public final String getSDTOfFlow() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.SDTOfFlow);
	}
	public final void setSDTOfFlow(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.SDTOfFlow, value);
	}
	/** 
	 流程ID
	*/
	public final long getWorkID() throws Exception
	{
		return this.GetValInt64ByKey(MyDeptFlowAttr.WorkID);
	}
	public final void setWorkID(long value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.WorkID,value);
	}
	/** 
	 主线程ID
	*/
	public final long getFID() throws Exception
	{
		return this.GetValInt64ByKey(MyDeptFlowAttr.FID);
	}
	public final void setFID(long value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FID, value);
	}
	/** 
	 父节点流程编号.
	*/
	public final long getPWorkID() throws Exception
	{
		return this.GetValInt64ByKey(MyDeptFlowAttr.PWorkID);
	}
	public final void setPWorkID(long value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.PWorkID, value);
	}
	/** 
	 父流程调用的节点
	*/
	public final int getPNodeID() throws Exception
	{
		return this.GetValIntByKey(MyDeptFlowAttr.PNodeID);
	}
	public final void setPNodeID(int value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.PNodeID, value);
	}
	/** 
	 PFlowNo
	*/
	public final String getPFlowNo() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.PFlowNo);
	}
	public final void setPFlowNo(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.PFlowNo, value);
	}
	/** 
	 吊起子流程的人员
	*/
	public final String getPEmp() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.PEmp);
	}
	public final void setPEmp(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.PEmp, value);
	}
	/** 
	 发起人
	*/
	public final String getStarter() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.Starter);
	}
	public final void setStarter(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.Starter, value);
	}
	/** 
	 发起人名称
	*/
	public final String getStarterName() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.StarterName);
	}
	public final void setStarterName(String value) throws Exception
	{
		this.SetValByKey(MyDeptFlowAttr.StarterName, value);
	}
	/** 
	 发起人部门名称
	 * @throws Exception 
	*/
	public final String getDeptName() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.DeptName);
	}
	public final void setDeptName(String value) throws Exception
	{
		this.SetValByKey(MyDeptFlowAttr.DeptName, value);
	}
	/** 
	 当前节点名称
	 * @throws Exception 
	*/
	public final String getNodeName() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.NodeName);
	}
	public final void setNodeName(String value) throws Exception
	{
		this.SetValByKey(MyDeptFlowAttr.NodeName, value);
	}
	/** 
	 当前工作到的节点
	 * @throws Exception 
	*/
	public final int getFK_Node() throws Exception
	{
		return this.GetValIntByKey(MyDeptFlowAttr.FK_Node);
	}
	public final void setFK_Node(int value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.FK_Node, value);
	}
	/** 
	 工作流程状态
	 * @throws Exception 
	*/
	public final WFState getWFState() throws Exception
	{
		return WFState.forValue(this.GetValIntByKey(MyDeptFlowAttr.WFState));
	}
	public final void setWFState(WFState value) throws Exception
	{
		if (value == WFState.Complete)
		{
			SetValByKey(MyDeptFlowAttr.WFSta, getWFSta().Complete.getValue());
		}
		else if (value == WFState.Delete)
		{
			SetValByKey(MyDeptFlowAttr.WFSta, getWFSta().Etc.getValue());
		}
		else
		{
			SetValByKey(MyDeptFlowAttr.WFSta, getWFSta().Runing.getValue());
		}

		SetValByKey(MyDeptFlowAttr.WFState, value.getValue());
	}
	/** 
	 状态(简单)
	 * @throws Exception 
	*/
	public final WFSta getWFSta() throws Exception
	{
		return WFSta.forValue(this.GetValIntByKey(MyDeptFlowAttr.WFSta));
	}
	public final void setWFSta(WFSta value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.WFSta, value.getValue());
	}
	public final String getWFStateText() throws Exception
	{
		BP.WF.WFState ws = (WFState)this.getWFState();
		switch (ws)
		{
			case Complete:
				return "已完成";
			case Runing:
				return "在运行";
			case HungUp:
				return "挂起";
			case Askfor:
				return "加签";
			default:
				return "未判断";
		}
	}
	/** 
	 GUID
	 * @throws Exception 
	*/
	public final String getGUID() throws Exception
	{
		return this.GetValStrByKey(MyDeptFlowAttr.GUID);
	}
	public final void setGUID(String value) throws Exception
	{
		SetValByKey(MyDeptFlowAttr.GUID, value);
	}

		///#endregion


		///#region 参数属性.

	public final String getParas_ToNodes() throws Exception
	{
		return this.GetParaString("ToNodes");
	}

	public final void setParas_ToNodes(String value) throws Exception
	{
		this.SetPara("ToNodes", value);
	}
	/** 
	 加签信息
	 * @throws Exception 
	*/

	public final String getParas_AskForReply() throws Exception
	{
		return this.GetParaString("AskForReply");
	}

	public final void setParas_AskForReply(String value) throws Exception
	{
		this.SetPara("AskForReply", value);
	}

		///#endregion 参数属性.


		///#region 构造函数
	/** 
	 产生的工作流程
	*/
	public MyDeptFlow()
	{
	}
	public MyDeptFlow(long workId) throws Exception
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(MyDeptFlowAttr.WorkID, workId);
		if (qo.DoQuery() == 0)
		{
			throw new RuntimeException("工作 MyDeptFlow [" + workId + "]不存在。");
		}
	}
	/** 
	 执行修复
	*/
	public final void DoRepair()
	{
	}
	/** 
	 重写基类方法
	*/
	@Override
	public Map getEnMap()
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}

		Map map = new Map("WF_GenerWorkFlow", "我部门的流程");
		map.Java_SetEnType(EnType.View);

		map.AddTBString(MyDeptFlowAttr.Title, null, "标题", true, false, 0, 100, 150, true);
		map.AddDDLEntities(MyDeptFlowAttr.FK_Flow, null, "流程", new Flows(), false);
		map.AddTBString(MyDeptFlowAttr.BillNo, null, "单据编号", true, false, 0, 100, 50);

		map.AddTBString(MyDeptFlowAttr.StarterName, null, "发起人", true, false, 0, 30, 40);
		map.AddTBDateTime(MyDeptFlowAttr.RDT, "发起日期", true, true);

		map.AddTBString(MyDeptFlowAttr.NodeName, null, "当前节点", true, false, 0, 100, 80);
		map.AddTBString(MyDeptFlowAttr.TodoEmps, null, "当前处理人", true, false, 0, 100, 80);

		map.AddDDLSysEnum(MyDeptFlowAttr.WFSta, 0, "状态", true, false, MyDeptFlowAttr.WFSta);
		map.AddDDLSysEnum(MyFlowAttr.TSpan, 0, "时间段", true, false, MyFlowAttr.TSpan, "@0=本周@1=上周@2=两周以前@3=三周以前@4=更早");

		map.AddTBStringDoc(MyDeptFlowAttr.FlowNote, null, "备注", true, false,true);
		map.AddTBMyNum();

			//工作ID
		map.AddTBIntPK(MyDeptFlowAttr.WorkID, 0, "工作ID", true, true);

			//隐藏字段.
		map.AddTBInt(MyDeptFlowAttr.FID, 0, "FID", false, false);
		map.AddTBString(MyDeptFlowAttr.FK_Dept, null, "部门", false, false, 0, 30, 10);


		map.AddSearchAttr(MyDeptFlowAttr.FK_Flow);
		map.AddSearchAttr(MyDeptFlowAttr.WFSta);
		map.AddSearchAttr(MyDeptFlowAttr.TSpan);
		map.AddHidden(MyStartFlowAttr.FID, "=", "0");


			//增加隐藏的查询条件.
		AttrOfSearch search = new AttrOfSearch(MyDeptFlowAttr.FK_Dept, "部门", MyDeptFlowAttr.FK_Dept, "=", WebUser.getFK_Dept(), 0, true);

		map.getAttrsOfSearch().Add(search);

		RefMethod rm = new RefMethod();
		rm.Title = "流程轨迹";
		rm.ClassMethodName = this.toString() + ".DoTrack";
		rm.Icon = "../../WF/Img/FileType/doc.gif";
		map.AddRefMethod(rm);

		this.set_enMap(map);
		return this.get_enMap();
	}

		///#endregion


		///#region 执行诊断
	public final String DoTrack() throws Exception
	{
		return "../../WFRpt.htm?WorkID=" + this.getWorkID() + "&FID=" + this.getFID() + "&FK_Flow=" + this.getFK_Flow() + "&FK_Node=" + this.getFK_Node();
	}

		///#endregion
}