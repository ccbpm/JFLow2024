package BP.WF;

import BP.WF.Template.*;
import BP.WF.Data.*;
import BP.DA.*;
import BP.Difference.SystemConfig;
import BP.En.*;
import BP.Sys.*;

/** 
 流程检查类
 1. 创建修复数据表.
 2. 检查流程设计的合法性.
*/
public class FlowCheckError
{

		///#region 构造方法与属性.
	public DataTable dt = null;
	/** 
	 流程
	*/
	public Flow flow = null;
	/** 
	 节点s
	*/
	public Nodes nds = null;
	/** 
	 通用的
	 * @throws Exception 
	*/
	public final GERpt getHisGERpt() throws Exception
	{
		return this.flow.getHisGERpt();
	}
	/** 
	 流程检查
	 
	 @param fl 流程实体
	 * @throws Exception 
	*/
	public FlowCheckError(Flow fl) throws Exception
	{
		this.flow = fl;
		this.nds = new Nodes(fl.getNo());
		//构造消息存储.
		dt = new DataTable();
		dt.Columns.Add("InfoType");
		dt.Columns.Add("Msg");
		dt.Columns.Add("NodeID");
		dt.Columns.Add("NodeName");
	}
	/** 
	 流程检查
	 
	 @param flNo 流程编号
	 * @throws Exception 
	*/
	public FlowCheckError(String flNo) throws Exception
	{
		this.flow = new Flow(flNo);
		this.nds = new Nodes(this.flow.getNo());

		//构造消息存储.
		dt = new DataTable();
		dt.Columns.Add("InfoType");
		dt.Columns.Add("Msg");
		dt.Columns.Add("NodeID");
		dt.Columns.Add("NodeName");

	}
	/** 
	 信息
	 
	 @param info
	 @param nd
	 * @throws Exception 
	*/

	private void AddMsgInfo(String info) throws Exception
	{
		AddMsgInfo(info, null);
	}

	private void AddMsgInfo(String info, Node nd) throws Exception
	{
		AddMsg("信息", info, nd);
	}
	/** 
	 警告
	 
	 @param info
	 @param nd
	 * @throws Exception 
	*/

	private void AddMsgWarning(String info) throws Exception
	{
		AddMsgWarning(info, null);
	}

	private void AddMsgWarning(String info, Node nd) throws Exception
	{
		AddMsg("警告", info, nd);
	}

	private void AddMsgError(String info) throws Exception
	{
		AddMsgError(info, null);
	}

	private void AddMsgError(String info, Node nd) throws Exception
	{
		AddMsg("错误", info, nd);
	}
	/** 
	 增加审核信息
	 
	 @param type 类型
	 @param info 消息
	 @param nd 节点
	 @return 
	 * @throws Exception 
	*/

	private void AddMsg(String type, String info) throws Exception
	{
		AddMsg(type, info, null);
	}

	private void AddMsg(String type, String info, Node nd) throws Exception
	{
		DataRow dr = this.dt.NewRow();
		dr.setValue(0, type);
		dr.setValue(1, info);

		if (nd != null)
		{
			dr.setValue(2, nd.getNodeID());
			dr.setValue(3, nd.getName());
		}
		this.dt.Rows.add(dr);
	}

		///#endregion 构造方法与属性.

	/** 
	 校验流程
	 
	 @return 
	 * @throws Exception 
	*/
	public final void DoCheck() throws Exception
	{
		BP.DA.Cash.ClearCash();
		try
		{
			//设置自动计算.
			CheckMode_Auto();

			/**检查独立表单的完整性.
			*/
			CheckMode_Frms();

			//通用检查.
			CheckMode_Gener();

			//检查数据合并模式.
			CheckMode_SpecTable();

			//节点表单字段数据类型检查 
			CheckModel_FormFields();

			//检查越轨流程,子流程发起.
			CheckModel_SubFlowYanXus();



			//检查报表.
			this.DoCheck_CheckRpt(this.nds);

			//检查焦点字段设置是否还有效.
			CheckMode_FocusField();

			//检查质量考核点.
			CheckMode_EvalModel();

			//检查如果是合流节点必须不能是由上一个节点指定接受人员.
			CheckMode_HeliuAccpterRole();

			Node.CheckFlow(this.flow);

			//创建track.
			Track.CreateOrRepairTrackTable(this.flow.getNo());

			//如果是引用的表单库的表单，就要检查该表单是否有FID字段，没有就自动增加.
			CheckMode_Ref();
		}
		catch (RuntimeException ex)
		{
			this.AddMsgError("err@" + ex.getMessage() + " " + ex.getStackTrace());
		}
	}
	/** 
	 通用的检查.
	 * @throws Exception 
	*/
	public final void CheckMode_Gener() throws Exception
	{
		//条件集合.
		Conds conds = new Conds(this.flow.getNo());

		for (Node nd : nds.ToJavaList())
		{
			//设置它的位置类型.
			nd.SetValByKey(NodeAttr.NodePosType, nd.GetHisNodePosType().getValue());

			this.AddMsgInfo("修复&检查节点信息", nd);
			nd.RepareMap(this.flow);

			// 从表检查。
			MapDtls dtls = new BP.Sys.MapDtls("ND" + nd.getNodeID());
			for (MapDtl dtl : dtls.ToJavaList())
			{
				this.AddMsgInfo("检查明细表" + dtl.getName(), nd);
				dtl.getHisGEDtl().CheckPhysicsTable();
			}

			MapAttrs mattrs = new MapAttrs("ND" + nd.getNodeID());


				///#region 对节点的访问规则进行检查

			this.AddMsgInfo("开始对节点的访问规则进行检查", nd);

			switch (nd.getHisDeliveryWay())
			{
				case ByStation:
				case FindSpecDeptEmpsInStationlist:
					if (nd.getNodeStations().size() == 0)
					{
						this.AddMsgInfo("错误:您设置了该节点的访问规则是按岗位，但是您没有为节点绑定岗位。", nd);
					}
					break;
				case ByDept:
					if (nd.getNodeDepts().size() == 0)
					{
						this.AddMsgInfo("设置了该节点的访问规则是按部门，但是您没有为节点绑定部门", nd);
					}

					break;
				case ByBindEmp:
					if (nd.getNodeEmps().size() == 0)
					{
						this.AddMsgInfo("您设置了该节点的访问规则是按人员，但是您没有为节点绑定人员。", nd);
					}

					break;
				case BySpecNodeEmp: //按指定的岗位计算.
				case BySpecNodeEmpStation: //按指定的岗位计算.
					if (nd.getDeliveryParas().trim().length() == 0)
					{
						this.AddMsgInfo("您设置了该节点的访问规则是按指定的岗位计算，但是您没有设置节点编号。", nd);
					}
					else
					{
						if (DataType.IsNumStr(nd.getDeliveryParas()) == false)
						{
							this.AddMsgInfo("您没有设置指定岗位的节点编号，目前设置的为{" + nd.getDeliveryParas() + "}", nd);
						}
					}
					break;
				case ByDeptAndStation: //按部门与岗位的交集计算.
					String mysql = "";
					//added by liuxc,2015.6.30.
					//区别集成与BPM模式
					if (BP.WF.Glo.getOSModel() == BP.Sys.OSModel.OneOne)
					{
						mysql = "SELECT No FROM Port_Emp WHERE No IN (SELECT No FK_Emp FROM Port_Emp WHERE FK_Dept IN ( SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node=" + nd.getNodeID() + "))AND No IN (SELECT FK_Emp FROM " + BP.WF.Glo.getEmpStation() +
							" WHERE FK_Station IN ( SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" +
							nd.getNodeID() + " )) ORDER BY No ";
					}
					else
					{
						mysql = "SELECT pdes.FK_Emp AS No"
								+ " FROM   Port_DeptEmpStation pdes"
								+ "        INNER JOIN WF_NodeDept wnd"
								+ "             ON  wnd.FK_Dept = pdes.FK_Dept"
								+ "             AND wnd.FK_Node = " + nd.getNodeID() + "        INNER JOIN WF_NodeStation wns"
								+ "             ON  wns.FK_Station = pdes.FK_Station"
								+ "             AND wnd.FK_Node =" + nd.getNodeID() + " ORDER BY"
								+ "        pdes.FK_Emp";
					}

					DataTable mydt = DBAccess.RunSQLReturnTable(mysql);
					if (mydt.Rows.size() == 0)
					{
						this.AddMsgInfo("按照岗位与部门的交集计算错误，没有人员集合{" + mysql + "}", nd);
					}
					break;
				case BySQL:
				case BySQLAsSubThreadEmpsAndData:
					if (nd.getDeliveryParas().trim().length() <= 5)
					{
						this.AddMsgInfo("您设置了该节点的访问规则是按SQL查询，但是您没有在节点属性里设置查询sql，此sql的要求是查询必须包含No,Name两个列，sql表达式里支持@+字段变量，详细参考开发手册.", nd);
						continue;
					}

					String sql = nd.getDeliveryParas();
					for (MapAttr item : mattrs.ToJavaList())
					{
						if (item.getIsNum())
						{
							sql = sql.replace("@" + item.getKeyOfEn(), "0");
						}
						else
						{
							sql = sql.replace("@" + item.getKeyOfEn(), "'0'");
						}
					}

					sql = sql.replace("@WebUser.No", "'ss'");
					sql = sql.replace("@WebUser.Name", "'ss'");
					sql = sql.replace("@WebUser.FK_DeptName", "'ss'");
					sql = sql.replace("@WebUser.FK_Dept", "'ss'");


					sql = sql.replace("''''", "''"); //出现双引号的问题.

					if (sql.contains("@"))
					{
						this.AddMsgError("您编写的sql变量填写不正确，实际执行中，没有被完全替换下来" + sql, nd);
						continue;
					}

					DataTable testDB = null;
					try
					{
						testDB = DBAccess.RunSQLReturnTable(sql);
					}
					catch (RuntimeException ex)
					{
						this.AddMsgError("您设置了该节点的访问规则是按SQL查询,执行此语句错误." + sql + " err:" + ex.getMessage(), nd);
						break;
					}

					if (testDB.Columns.contains("no") == false || testDB.Columns.contains("name") == false)
					{
						this.AddMsgError("您设置了该节点的访问规则是按SQL查询，设置的sql不符合规则，此sql的要求是查询必须包含No,Name两个列，sql表达式里支持@+字段变量，详细参考开发手册.", nd);
					}

					break;
				case ByPreviousNodeFormEmpsField:

					//去rpt表中，查询是否有这个字段
					String str = String.valueOf(nd.getNodeID()).substring(0, String.valueOf(nd.getNodeID()).length() - 2);
					MapAttrs rptAttrs = new BP.Sys.MapAttrs();
					rptAttrs.Retrieve(MapAttrAttr.FK_MapData, "ND" + str + "Rpt", MapAttrAttr.KeyOfEn);

					if (rptAttrs.Contains(BP.Sys.MapAttrAttr.KeyOfEn, nd.getDeliveryParas()) == false)
					{
						/*检查节点字段是否有FK_Emp字段*/
						this.AddMsgError("您设置了该节点的访问规则是[06.按上一节点表单指定的字段值作为本步骤的接受人]，但是您没有在节点属性的[访问规则设置内容]里设置指定的表单字段，详细参考开发手册.", nd);
					}

					break;
				case BySelected: // 由上一步发送人员选择
					break;
				case ByPreviousNodeEmp: // 与上一个节点人员相同.
					break;
				default:
					break;
			}

				///#endregion


				///#region 检查节点完成条件，方向条件的定义.
			//设置它没有流程完成条件.
			nd.setIsCCFlow(false);

			if (conds.size() != 0)
			{
				this.AddMsgInfo("开始检查(" + nd.getName() + ")方向条件:", nd);

				for (Cond cond : conds.ToJavaList())
				{
					if (cond.getFK_Node() == nd.getNodeID() && cond.getHisCondType() == CondType.Flow)
					{
						nd.setIsCCFlow(true);
						nd.Update();
					}

					Node ndOfCond = new Node();
					ndOfCond.setNodeID(ndOfCond.getNodeID());
					if (ndOfCond.RetrieveFromDBSources() == 0)
					{
						continue;
					}

					if (cond.getAttrKey().length() < 2)
					{
						continue;
					}
					if (ndOfCond.getHisWork().getEnMap().getAttrs().Contains(cond.getAttrKey()) == false)
					{
						this.AddMsgError("@错误:属性:" + cond.getAttrKey() + " , " + cond.getAttrName() + " 不存在。", nd);
						continue;
					}
				}
			}

				///#endregion 检查节点完成条件的定义.
		}
	}

	/** 
	 流程属性的预先计算与基础的更新
	 * @throws Exception 
	*/
	public final void CheckMode_Auto() throws Exception
	{
		// 设置流程名称.
		DBAccess.RunSQL("UPDATE WF_Node SET FlowName = (SELECT Name FROM WF_Flow WHERE NO=WF_Node.FK_Flow)");

		//设置单据编号只读格式.
		DBAccess.RunSQL("UPDATE Sys_MapAttr SET UIIsEnable=0 WHERE KeyOfEn='BillNo' AND UIIsEnable=1");

		//开始节点不能有会签.
		DBAccess.RunSQL("UPDATE WF_Node SET HuiQianRole=0 WHERE NodePosType=0 AND HuiQianRole !=0");

		//开始节点不能有退回.
		DBAccess.RunSQL("UPDATE WF_Node SET ReturnRole=0 WHERE NodePosType=0 AND ReturnRole !=0");

		//删除垃圾,非法数据.
		String sqls = "DELETE FROM Sys_FrmSln WHERE FK_MapData NOT IN (SELECT No from Sys_MapData)";
		sqls += "@ DELETE FROM WF_Direction WHERE Node=ToNode";
		DBAccess.RunSQLs(sqls);

		//更新计算数据.
		this.flow.setNumOfBill(DBAccess.RunSQLReturnValInt("SELECT count(*) FROM WF_BillTemplate WHERE NodeID IN (SELECT NodeID FROM WF_Flow WHERE No='" + this.flow.getNo() + "')"));
		this.flow.setNumOfDtl(DBAccess.RunSQLReturnValInt("SELECT count(*) FROM Sys_MapDtl WHERE FK_MapData='ND" + Integer.parseInt(this.flow.getNo()) + "Rpt'"));
		this.flow.DirectUpdate();

		//一直没有找到设置3列，自动回到四列的情况.
		DBAccess.RunSQL("UPDATE Sys_MapAttr SET ColSpan=3 WHERE  UIHeight<=23 AND ColSpan=4");
	}
	/** 
	 检查独立表单的完整性.
	 * @throws Exception 
	*/
	public final void CheckMode_Frms() throws Exception
	{
		FrmNodes fns = new FrmNodes();
		fns.Retrieve(FrmNodeAttr.FK_Flow, this.flow.getNo());
		String frms = "";
		String err = "";
		for (FrmNode item : fns.ToJavaList())
		{
			if (frms.contains(item.getFK_Frm() + ","))
			{
				continue;
			}
			frms += item.getFK_Frm() + ",";

			MapData md = new MapData();
			md.setNo(item.getFK_Frm());
			if (md.RetrieveFromDBSources() == 0)
			{
				this.AddMsgError("节点绑定的表单ID=" + item.getFK_Frm() + "，但该表单已经不存在.", new Node(item.getFK_Node()));
				continue;
			}
		}
	}
	/** 
	 如果是引用的表单库的表单，就要检查该表单是否有FID字段，没有就自动增加.
	 * @throws Exception 
	*/
	public final void CheckMode_Ref() throws Exception
	{
		for (Node nd : nds.ToJavaList())
		{
			if (nd.getHisFormType() == NodeFormType.RefOneFrmTree)
			{
				MapAttr mattr = new MapAttr();
				mattr.setMyPK( nd.getNodeFrmID() + "_FID");
				if (mattr.RetrieveFromDBSources() == 0)
				{
					mattr.setKeyOfEn ("FID");
					mattr.setFK_MapData( nd.getNodeFrmID());
					mattr.setMyDataType (DataType.AppInt);
					mattr.setUIVisible(false);
					mattr.setName("FID(自动增加)");
					mattr.Insert();

					GEEntity en = new GEEntity(nd.getNodeFrmID());
					en.CheckPhysicsTable();
				}
			}
		}
	}
	/** 
	 检查是否是数据合并模式
	 * @throws Exception 
	*/
	public final void CheckMode_SpecTable() throws Exception
	{
		if (this.flow.getHisDataStoreModel() != DataStoreModel.SpecTable)
		{
			return;
		}

		for (Node nd : nds.ToJavaList())
		{
			MapData md = new MapData();
			md.setNo("ND" + nd.getNodeID());
			if (md.RetrieveFromDBSources() == 1)
			{
				if (!this.flow.getPTable().equals(md.getPTable()))
				{
					md.setPTable(this.flow.getPTable());
					md.Update();
				}
			}
		}
	}
	/** 
	 检查越轨流程,子流程发起.
	 * @throws Exception 
	*/
	public final void CheckModel_SubFlowYanXus() throws Exception
	{
		String msg = "";
		SubFlowYanXus yanxuFlows = new SubFlowYanXus();
		yanxuFlows.Retrieve(SubFlowYanXuAttr.SubFlowNo, this.flow.getNo());

		for (SubFlowYanXu flow : yanxuFlows.ToJavaList())
		{
			Flow fl = new Flow(flow.getSubFlowNo());

			/* 如果当前为子流程的时候，允许节点自动运行下一步骤，就要确定下一步骤的节点，必须有确定的可以计算的接收人. */
			if (fl.getSubFlowOver() == SubFlowOver.SendParentFlowToNextStep)
			{
				Node nd = new Node(flow.getFK_Node());
				if (nd.getHisToNodes().size() > 1)
				{
					this.AddMsgError("@当前节点[" + nd.getName() + "]的可以启动子流程或者延续流程.被启动的子流程设置了当子流程结束时让父流程自动运行到下一个节点，但是当前节点有分支，导致流程无法运行到下一个节点.", nd);
				}

				if (nd.getHisToNodes().size() == 1)
				{
					Node toNode = nd.getHisToNodes().get(0) instanceof Node ? (Node)nd.getHisToNodes().get(0) : null;
					if (nd.getHisDeliveryWay() == DeliveryWay.BySelected)
					{
						msg = "@当前节点[" + nd.getName() + "]的可以启动子流程或者延续流程.被启动的子流程设置了当子流程结束时让父流程自动运行到下一个节点，但是当前节点有分支，导致流程无法运行到下一个节点.";
						this.AddMsgError(msg, nd);
					}
				}

			}
		}
	}

	/** 
	 检查焦点字段设置是否还有效
	 * @throws Exception 
	*/
	public final void CheckMode_FocusField() throws Exception
	{
		String msg = "";
		//获得gerpt字段.
		GERpt rpt = this.flow.getHisGERpt();
		for (Node nd : nds.ToJavaList())
		{
			if (nd.getFocusField().trim().equals(""))
			{
				Work wk = nd.getHisWork();
				String attrKey = "";
				for (Attr attr : wk.getEnMap().getAttrs())
				{
					if (attr.getUIVisible() == true && attr.getUIIsDoc() && attr.getUIIsReadonly() == false)
					{
						attrKey = attr.getDesc() + ":@" + attr.getKey();
					}
				}

				if (attrKey.equals(""))
				{
					msg = "@警告:节点ID:" + nd.getNodeID() + " 名称:" + nd.getName() + "属性里没有设置焦点字段，会导致信息写入轨迹表空白，为了能够保证流程轨迹是可读的请设置焦点字段.";
					this.AddMsgWarning(msg, nd);
				}
				else
				{
					msg = "@信息:节点ID:" + nd.getNodeID() + " 名称:" + nd.getName() + "属性里没有设置焦点字段，会导致信息写入轨迹表空白，为了能够保证流程轨迹是可读的系统自动设置了焦点字段为" + attrKey + ".";
					this.AddMsgInfo(msg, nd);

					nd.setFocusField(attrKey);
					nd.DirectUpdate();
				}
				continue;
			}

			Object tempVar = nd.getFocusField();
			String strs = tempVar instanceof String ? (String)tempVar : null;
			strs = Glo.DealExp(strs, rpt, "err");
			if (strs.contains("@") == true)
			{
				//@shilianyu. 
				msg = "@警告:焦点字段（" + nd.getFocusField() + "）在节点(step:" + nd.getStep() + " 名称:" + nd.getName() + ")属性里的设置已无效，表单里不存在该字段.";
				this.AddMsgError(msg, nd);
			}

			if (this.flow.getIsMD5())
			{
				if (nd.getHisWork().getEnMap().getAttrs().Contains(WorkAttr.MD5) == false)
				{
					nd.RepareMap(this.flow);
				}
			}
		}
	}
	/** 
	 检查质量考核点
	 * @throws Exception 
	*/
	public final void CheckMode_EvalModel() throws Exception
	{
		String msg = "";
		for (Node nd : nds.ToJavaList())
		{
			if (nd.getIsEval())
			{
				/*如果是质量考核点，检查节点表单是否具别质量考核的特别字段？*/
				String sql = "SELECT COUNT(*) FROM Sys_MapAttr WHERE FK_MapData='ND" + nd.getNodeID() + "' AND KeyOfEn IN ('EvalEmpNo','EvalEmpName','EvalEmpCent')";
				if (DBAccess.RunSQLReturnValInt(sql) != 3)
				{
					msg = "@信息:您设置了节点(" + nd.getNodeID() + "," + nd.getName() + ")为质量考核节点，但是您没有在该节点表单中设置必要的节点考核字段.";
					this.AddMsgError(msg, nd);
				}
			}
		}
	}
	/** 
	 检查如果是合流节点必须不能是由上一个节点指定接受人员.
	 
	 @return 
	 * @throws Exception 
	*/
	public final void CheckMode_HeliuAccpterRole() throws Exception
	{
		String msg = "";
		for (Node nd : nds.ToJavaList())
		{
			//如果是合流节点.
			if (nd.getHisNodeWorkType() == NodeWorkType.WorkHL || nd.getHisNodeWorkType() == NodeWorkType.WorkFHL)
			{
				if (nd.getHisDeliveryWay() == DeliveryWay.BySelected)
				{
					msg = "@错误:节点ID:" + nd.getNodeID() + " 名称:" + nd.getName() + "是合流或者分合流节点，但是该节点设置的接收人规则为由上一步指定，这是错误的，应该为自动计算而非每个子线程人为的选择.";
					this.AddMsgError(msg, nd);
				}
			}

			//子线程节点
			if (nd.getHisNodeWorkType() == NodeWorkType.SubThreadWork)
			{
				if (nd.getCondModel() == CondModel.ByUserSelected)
				{
					Nodes toNodes = nd.getHisToNodes();
					if (toNodes.size() == 1)
					{
						//msg += "@错误:节点ID:" + nd.NodeID + " 名称:" + nd.Name + " 错误当前节点为子线程，但是该节点的到达.";
					}
				}
			}
		}
	}

	/** 
	 节点表单字段数据类型检查，名字相同的字段出现类型不同的处理方法：依照不同于NDxxRpt表中同名字段类型为基准
	 
	 @return 检查结果
	 * @throws Exception 
	*/
	private String CheckModel_FormFields() throws Exception
	{
		StringBuilder errorAppend = new StringBuilder();
		errorAppend.append("@信息: -------- 流程节点表单的字段类型检查: ------ ");
		try
		{
			Nodes nds = new Nodes(this.flow.getNo());
			String fk_mapdatas = "'ND" + Integer.parseInt(this.flow.getNo()) + "Rpt'";
			for (Node nd : nds.ToJavaList())
			{
				fk_mapdatas += ",'ND" + nd.getNodeID() + "'";
			}

			//筛选出类型不同的字段
			String checkSQL = "SELECT   AA.KEYOFEN, COUNT(*) AS MYNUM FROM ("
								+ "  SELECT A.KEYOFEN,  MYDATATYPE,  COUNT(*) AS MYNUM"
								+ "  FROM SYS_MAPATTR A WHERE FK_MAPDATA IN (" + fk_mapdatas + ") GROUP BY KEYOFEN, MYDATATYPE"
								+ ")  AA GROUP BY  AA.KEYOFEN HAVING COUNT(*) > 1";
			DataTable dt_Fields = DBAccess.RunSQLReturnTable(checkSQL);
			for (DataRow row : dt_Fields.Rows)
			{
				String keyOfEn = row.getValue("KEYOFEN").toString();
				String myNum = row.getValue("MYNUM").toString();
				int iMyNum = Integer.parseInt(myNum);
				

				//存在2种以上数据类型，有手动进行调整
				if (iMyNum > 2)
				{
					errorAppend.append("@错误：字段名" + keyOfEn + "在此流程表(" + fk_mapdatas + ")中存在2种以上数据类型(如：int，float,varchar,datetime)，请手动修改。");
					return errorAppend.toString();
				}

				//存在2种数据类型，以不同于NDxxRpt字段类型为主
				MapAttr baseMapAttr = new MapAttr();
				MapAttr rptMapAttr = new MapAttr("ND" + Integer.parseInt(this.flow.getNo()) + "Rpt", keyOfEn);

				//Rpt表中不存在此字段
				if (rptMapAttr == null || rptMapAttr.getMyPK().equals(""))
				{
					this.DoCheck_CheckRpt(this.nds);
					rptMapAttr = new MapAttr("ND" + Integer.parseInt(this.flow.getNo()) + "Rpt", keyOfEn);
					this.getHisGERpt().CheckPhysicsTable();
				}

				//Rpt表中不存在此字段,直接结束
				if (rptMapAttr == null || rptMapAttr.getMyPK().equals(""))
				{
					continue;
				}

				for (Node nd : nds.ToJavaList())
				{
					MapAttr ndMapAttr = new MapAttr("ND" + nd.getNodeID(), keyOfEn);
					if (ndMapAttr == null || ndMapAttr.getMyPK().equals(""))
					{
						continue;
					}

					//找出与NDxxRpt表中字段数据类型不同的表单
					if (rptMapAttr.getMyDataType() != ndMapAttr.getMyDataType())
					{
						baseMapAttr = ndMapAttr;
						break;
					}
				}
				errorAppend.append("@基础表" + baseMapAttr.getFK_MapData() + "，字段" + keyOfEn + "数据类型为：" + baseMapAttr.getMyDataTypeStr());
				//根据基础属性类修改数据类型不同的表单
				for (Node nd : nds.ToJavaList())
				{
					MapAttr ndMapAttr = new MapAttr("ND" + nd.getNodeID(), keyOfEn);
					//不包含此字段的进行返回,类型相同的进行返回
					if (ndMapAttr == null || ndMapAttr.getMyPK().equals("") || baseMapAttr.getMyPK().equals(ndMapAttr.getMyPK()) || baseMapAttr.getMyDataType() == ndMapAttr.getMyDataType())
					{
						continue;
					}

					ndMapAttr.setName(baseMapAttr.getName());
					ndMapAttr.setMyDataType (baseMapAttr.getMyDataType());
					ndMapAttr.setUIWidth(baseMapAttr.getUIWidth());
					ndMapAttr.setUIHeight(baseMapAttr.getUIHeight());
					ndMapAttr.setMinLen(baseMapAttr.getMinLen());
					ndMapAttr.setMaxLen(baseMapAttr.getMaxLen());
					if (ndMapAttr.Update() > 0)
					{
						errorAppend.append("@修改了" + "ND" + nd.getNodeID() + " 表，字段" + keyOfEn + "修改为：" + baseMapAttr.getMyDataTypeStr());
					}
					else
					{
						errorAppend.append("@错误:修改" + "ND" + nd.getNodeID() + " 表，字段" + keyOfEn + "修改为：" + baseMapAttr.getMyDataTypeStr() + "失败。");
					}
				}
				//修改NDxxRpt
				rptMapAttr.setName(baseMapAttr.getName());
				rptMapAttr.setMyDataType (baseMapAttr.getMyDataType());
				rptMapAttr.setUIWidth(baseMapAttr.getUIWidth());
				rptMapAttr.setUIHeight(baseMapAttr.getUIHeight());
				rptMapAttr.setMinLen(baseMapAttr.getMinLen());
				rptMapAttr.setMaxLen(baseMapAttr.getMaxLen());
				if (rptMapAttr.Update() > 0)
				{
					errorAppend.append("@修改了" + "ND" + Integer.parseInt(this.flow.getNo()) + "Rpt 表，字段" + keyOfEn + "修改为：" + baseMapAttr.getMyDataTypeStr());
				}
				else
				{
					errorAppend.append("@错误:修改" + "ND" + Integer.parseInt(this.flow.getNo()) + "Rpt 表，字段" + keyOfEn + "修改为：" + baseMapAttr.getMyDataTypeStr() + "失败。");
				}
			}
		}
		catch (RuntimeException ex)
		{
			errorAppend.append("@错误:" + ex.getMessage());
		}
		return errorAppend.toString();
	}
	/** 
	 检查数据报表.
	 
	 @param nds
	 * @throws Exception 
	*/
	private void DoCheck_CheckRpt(Nodes nds) throws Exception
	{
		String fk_mapData = "ND" + Integer.parseInt(this.flow.getNo()) + "Rpt";
		String flowId = String.valueOf(Integer.parseInt(this.flow.getNo()));

		//生成该节点的 nds 比如  "'ND101','ND102','ND103'"
		String ndsstrs = "";
		for (BP.WF.Node nd : nds.ToJavaList())
		{
			ndsstrs += "'ND" + nd.getNodeID() + "',";
		}
		ndsstrs = ndsstrs.substring(0, ndsstrs.length() - 1);


			///#region 插入字段。
		String sql = "SELECT distinct KeyOfEn FROM Sys_MapAttr WHERE FK_MapData IN (" + ndsstrs + ")";
		if (SystemConfig.getAppCenterDBType() == DBType.MySQL)
		{
			sql = "SELECT A.* FROM (" + sql + ") AS A ";
			String sql3 = "DELETE FROM Sys_MapAttr WHERE KeyOfEn NOT IN (" + sql + ") AND FK_MapData='" + fk_mapData + "' ";
			DBAccess.RunSQL(sql3); // 删除不存在的字段.
		}
		else
		{
			String sql2 = "DELETE FROM Sys_MapAttr WHERE KeyOfEn NOT IN (" + sql + ") AND FK_MapData='" + fk_mapData + "' ";
			DBAccess.RunSQL(sql2); // 删除不存在的字段.
		}

		//所有节点表单字段的合集.
		sql = "SELECT MyPK, KeyOfEn FROM Sys_MapAttr WHERE FK_MapData IN (" + ndsstrs + ")";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);

		//求已经存在的字段集合。
		sql = "SELECT KeyOfEn FROM Sys_MapAttr WHERE FK_MapData='ND" + flowId + "Rpt'";
		DataTable dtExits = DBAccess.RunSQLReturnTable(sql);
		String pks = "@";
		for (DataRow dr : dtExits.Rows)
		{
			pks += dr.getValue(0) + "@";
		}

		//遍历 - 所有节点表单字段的合集
		for (DataRow dr : dt.Rows)
		{
			if (pks.contains("@" + dr.getValue("KeyOfEn").toString() + "@") == true)
			{
				continue;
			}

			String mypk = dr.getValue("MyPK").toString();

			pks += dr.getValue("KeyOfEn").toString() + "@";

			//找到这个属性.
			BP.Sys.MapAttr ma = new BP.Sys.MapAttr(mypk);

			ma.setMyPK( "ND" + flowId + "Rpt_" + ma.getKeyOfEn());
			ma.setFK_MapData( "ND" + flowId + "Rpt");
			ma.setUIIsEnable(false);

			if (ma.getDefValReal().contains("@"))
			{
				/*如果是一个有变量的参数.*/
				ma.setDefVal( "");
			}

			// 如果不存在.
			if (ma.getIsExits() == false)
			{
				ma.Insert();
			}
		}

		MapAttrs attrs = new MapAttrs(fk_mapData);

		// 创建mapData.
		BP.Sys.MapData md = new BP.Sys.MapData();
		md.setNo("ND" + flowId + "Rpt");
		if (md.RetrieveFromDBSources() == 0)
		{
			md.setName(this.flow.getName());
			md.setPTable(this.flow.getPTable());
			md.Insert();
		}
		else
		{
			md.setName(this.flow.getName());
			md.setPTable(this.flow.getPTable());
			md.Update();
		}

			///#endregion 插入字段。


			///#region 补充上流程字段到NDxxxRpt.
		int groupID = 0;
		for (MapAttr attr : attrs.ToJavaList())
		{
			switch (attr.getKeyOfEn())
			{
				case StartWorkAttr.FK_Dept:
					attr.setUIContralType(UIContralType.TB);
					attr.setLGType(FieldTypeS.Normal);
					attr.setUIVisible(true);
					attr.setGroupID(groupID); // gfs[0].GetValIntByKey("OID");
					attr.setUIIsEnable(false);
					attr.setDefVal("");
					attr.setMaxLen(100);
					attr.Update();
					break;
				case "FK_NY":
					attr.setUIContralType(UIContralType.TB);
					attr.setLGType(FieldTypeS.Normal);
					attr.setUIVisible(true);
					attr.setUIIsEnable(false);
					attr.setGroupID(groupID);
					attr.Update();
					break;
				case "FK_Emp":
					break;
				default:
					break;
			}
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.Title) == false)
		{
			/* 标题 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.Title); // "FlowEmps";
			attr.setName("标题");
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(true);
			attr.setMinLen(0);
			attr.setMaxLen(400);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.OID) == false)
		{
			/* WorkID */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setKeyOfEn("OID");
			attr.setName("WorkID");
			attr.setMyDataType(DataType.AppInt);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(false);
			attr.setUIIsEnable(false);
			attr.setDefVal("0");
			attr.setHisEditType(EditType.Readonly);
			attr.Insert();
		}


		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FID) == false)
		{
			/* FID */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setKeyOfEn ("FID");
			attr.setName("FID");
			attr.setMyDataType(BP.DA.DataType.AppInt);
			attr.setUIContralType (UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(false);
			attr.setUIIsEnable(false);
			attr.setDefVal("0");
			attr.setHisEditType(BP.En.EditType.Readonly);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.WFState) == false)
		{
			/* 流程状态 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.WFState);
			attr.setName("流程状态");
			attr.setMyDataType(DataType.AppInt);
			attr.setUIBindKey(GERptAttr.WFState);
			attr.setUIContralType(UIContralType.DDL);
			attr.setLGType(FieldTypeS.Enum);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setMinLen(0);
			attr.setMaxLen(1000);
			attr.setIdx(-1);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.WFSta) == false)
		{
			/* 流程状态Ext */
			MapAttr attr = new MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.WFSta);
			attr.setName("状态");
			attr.setMyDataType(DataType.AppInt);
			attr.setUIBindKey(GERptAttr.WFSta);
			attr.setUIContralType(UIContralType.DDL);
			attr.setLGType(FieldTypeS.Enum);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setMinLen(0);
			attr.setMaxLen(1000);
			attr.setIdx(-1);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowEmps) == false)
		{
			/* 参与人 */
			MapAttr attr = new MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowEmps); // "FlowEmps";
			attr.setName("参与人");
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(true);
			attr.setMinLen(0);
			attr.setMaxLen(1000);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowStarter) == false)
		{
			/* 发起人 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowStarter);
			attr.setName("发起人");
			attr.setMyDataType(DataType.AppString);

			attr.setUIBindKey("BP.Port.Emps");
			attr.setUIContralType(UIContralType.DDL);
			attr.setLGType(FieldTypeS.FK);

			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setMinLen(0);
			attr.setMaxLen(20);
			attr.setIdx(-1);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowStartRDT) == false)
		{
			/* MyNum */
			MapAttr attr = new MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowStartRDT); // "FlowStartRDT";
			attr.setName("发起时间");
			attr.setMyDataType(DataType.AppDateTime);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowEnder) == false)
		{
			/* 结束人 */
			MapAttr attr = new MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowEnder);
			attr.setName("结束人");
			attr.setMyDataType(DataType.AppString);
			attr.setUIBindKey("BP.Port.Emps");
			attr.setUIContralType(UIContralType.DDL);
			attr.setLGType(FieldTypeS.FK);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setMinLen(0);
			attr.setMaxLen(20);
			attr.setIdx(-1);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowEnderRDT) == false)
		{
			/* 结束 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowEnderRDT); // "FlowStartRDT";
			attr.setName("结束时间");
			attr.setMyDataType(DataType.AppDateTime);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowEndNode) == false)
		{
			/* 结束节点 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowEndNode);
			attr.setName("结束节点");
			attr.setMyDataType(DataType.AppInt);
			attr.setDefVal("0");
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setHisEditType(EditType.UnDel);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowDaySpan) == false)
		{
			/* FlowDaySpan */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowDaySpan); // "FlowStartRDT";
			attr.setName("跨度(天)");
			attr.setMyDataType(DataType.AppMoney);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(true);
			attr.setUIIsLine(false);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.PFlowNo) == false)
		{
			/* 父流程 流程编号 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.PFlowNo);
			attr.setName("父流程流程编号"); // 父流程流程编号
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(true);
			attr.setMinLen(0);
			attr.setMaxLen(3);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.PNodeID) == false)
		{
			/* 父流程PNodeID */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.PNodeID);
			attr.setName("父流程启动的节点");
			attr.setMyDataType(DataType.AppInt);
			attr.setDefVal("0");
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setHisEditType(EditType.UnDel);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.PWorkID) == false)
		{
			/* 父流程WorkID */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.PWorkID);
			attr.setName("父流程WorkID");
			attr.setMyDataType(DataType.AppInt);
			attr.setDefVal("0");
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setHisEditType(EditType.UnDel);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.PEmp) == false)
		{
			/* 调起子流程的人员 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.PEmp);
			attr.setName("调起子流程的人员");
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(true);
			attr.setMinLen(0);
			attr.setMaxLen(32);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.BillNo) == false)
		{
			/* BillNo */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.BillNo);
			attr.setName("单据编号"); // 单据编号
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setMinLen(0);
			attr.setMaxLen(100);
			attr.setIdx(-100);
			attr.Insert();
		}


		if (attrs.Contains(md.getNo() + "_MyNum") == false)
		{
			/* MyNum */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn("MyNum");
			attr.setName("条");
			attr.setMyDataType(DataType.AppInt);
			attr.setDefVal("1");
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setHisEditType(EditType.UnDel);
			attr.setIdx(-101);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.AtPara) == false)
		{
			/* AtPara */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.AtPara);
			attr.setName("参数"); // 单据编号
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(false);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setMinLen(0);
			attr.setMaxLen(4000);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.GUID) == false)
		{
			/* GUID */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.GUID);
			attr.setName("GUID"); // 单据编号
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(false);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setMinLen(0);
			attr.setMaxLen(32);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.PrjNo) == false)
		{
			/* 项目编号 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.PrjNo);
			attr.setName("项目编号"); // 项目编号
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setMinLen(0);
			attr.setMaxLen(100);
			attr.setIdx(-100);
			attr.Insert();
		}
		if (attrs.Contains(md.getNo() + "_" + GERptAttr.PrjName) == false)
		{
			/* 项目名称 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.PrjName);
			attr.setName("项目名称");
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(false);
			attr.setMinLen(0);
			attr.setMaxLen(100);
			attr.setIdx(-100);
			attr.Insert();
		}

		if (attrs.Contains(md.getNo() + "_" + GERptAttr.FlowNote) == false)
		{
			/* 流程信息 */
			MapAttr attr = new BP.Sys.MapAttr();
			attr.setFK_MapData(md.getNo());
			attr.setHisEditType(EditType.UnDel);
			attr.setKeyOfEn(GERptAttr.FlowNote);
			attr.setName("流程信息"); // 父流程流程编号
			attr.setMyDataType(DataType.AppString);
			attr.setUIContralType(UIContralType.TB);
			attr.setLGType(FieldTypeS.Normal);
			attr.setUIVisible(true);
			attr.setUIIsEnable(false);
			attr.setUIIsLine(true);
			attr.setMinLen(0);
			attr.setMaxLen(500);
			attr.setIdx(-100);
			attr.Insert();
		}

			///#endregion 补充上流程字段。


			///#region 为流程字段设置分组。
		try
		{
			String flowInfo = "流程信息";
			GroupField flowGF = new GroupField();
			int num = flowGF.Retrieve(GroupFieldAttr.FrmID, fk_mapData, GroupFieldAttr.Lab, "流程信息");
			if (num == 0)
			{
				flowGF = new GroupField();
				flowGF.setLab(flowInfo);
				flowGF.setFrmID(fk_mapData);
				flowGF.setIdx(-1);
				flowGF.Insert();
			}
			sql = "UPDATE Sys_MapAttr SET GroupID='" + flowGF.getOID() + "' WHERE  FK_MapData='" + fk_mapData + "'  AND KeyOfEn IN('" + GERptAttr.PFlowNo + "','" + GERptAttr.PWorkID + "','" + GERptAttr.MyNum + "','" + GERptAttr.FK_Dept + "','" + GERptAttr.FK_NY + "','" + GERptAttr.FlowDaySpan + "','" + GERptAttr.FlowEmps + "','" + GERptAttr.FlowEnder + "','" + GERptAttr.FlowEnderRDT + "','" + GERptAttr.FlowEndNode + "','" + GERptAttr.FlowStarter + "','" + GERptAttr.FlowStartRDT + "','" + GERptAttr.WFState + "')";
			DBAccess.RunSQL(sql);
		}
		catch (RuntimeException ex)
		{
			Log.DefaultLogWriteLineError(ex.getMessage());
		}

			///#endregion 为流程字段设置分组


			///#region 尾后处理.
		GERpt gerpt = this.getHisGERpt();
		gerpt.CheckPhysicsTable(); //让报表重新生成.

		DBAccess.RunSQL("DELETE FROM Sys_GroupField WHERE FrmID='" + fk_mapData + "' AND OID NOT IN (SELECT GroupID FROM Sys_MapAttr WHERE FK_MapData = '" + fk_mapData + "')");

		DBAccess.RunSQL("UPDATE Sys_MapAttr SET Name='活动时间' WHERE FK_MapData='ND" + flowId + "Rpt' AND KeyOfEn='CDT'");
		DBAccess.RunSQL("UPDATE Sys_MapAttr SET Name='参与者' WHERE FK_MapData='ND" + flowId + "Rpt' AND KeyOfEn='Emps'");

			///#endregion 尾后处理.
	}
}