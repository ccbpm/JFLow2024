package bp.wf.template;

import bp.da.*;
import bp.difference.SystemConfig;
import bp.web.*;
import bp.sys.*;
import bp.wf.*;

/** 
 找人规则
*/
public class FindWorker
{

		///变量
	public WorkNode town = null;
	public WorkNode currWn = null;
	public Flow fl = null;
	private String dbStr = SystemConfig.getAppCenterDBVarStr();
	public Paras ps = null;
	private String JumpToEmp = null;
	private int JumpToNode = 0;
	private long WorkID = 0;

		/// 变量

	/** 
	 找人
	 
	 @param fl
	 @param currWn
	 @param toWn
	*/
	public FindWorker()
	{
	}
	public final DataTable FindByWorkFlowModel() throws Exception
	{
		this.town = town;

		DataTable dt = new DataTable();
		dt.Columns.Add("No", String.class);
		String sql;
		String FK_Emp;

		// 如果执行了两次发送，那前一次的轨迹就需要被删除,这里是为了避免错误。
		ps = new Paras();
		ps.Add("WorkID", this.WorkID);
		ps.Add("FK_Node", town.getHisNode().getNodeID());
		ps.SQL="DELETE FROM WF_GenerWorkerlist WHERE WorkID=" + dbStr + "WorkID AND FK_Node =" + dbStr + "FK_Node";
		DBAccess.RunSQL(ps);

		// 如果指定特定的人员处理。
		if (DataType.IsNullOrEmpty(JumpToEmp) == false)
		{
			String[] emps = JumpToEmp.split("[,]", -1);
			for (String emp : emps)
			{
				if (DataType.IsNullOrEmpty(emp))
				{
					continue;
				}
				DataRow dr = dt.NewRow();
				dr.setValue(0, emp);
				dt.Rows.add(dr);
			}
			return dt;
		}

		// 按上一节点发送人处理。
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByPreviousNodeEmp)
		{
			DataRow dr = dt.NewRow();
			dr.setValue(0, WebUser.getNo());
			dt.Rows.add(dr);
			return dt;
		}

		//首先判断是否配置了获取下一步接受人员的sql.
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySQL || town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySQLTemplate || town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySQLAsSubThreadEmpsAndData)
		{

			if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySQLTemplate)
			{
				SQLTemplate st = new SQLTemplate(town.getHisNode().getDeliveryParas());
				sql = st.getDocs();
			}
			else
			{
				if (town.getHisNode().getDeliveryParas().length() < 4)
				{
					throw new RuntimeException("@您设置的当前节点按照SQL，决定下一步的接受人员，但是你没有设置SQL.");
				}
				sql = town.getHisNode().getDeliveryParas();
			}


			//特殊的变量.
			sql = sql.replace("@FK_Node", String.valueOf(this.town.getHisNode().getNodeID()));
			sql = sql.replace("@NodeID", String.valueOf(this.town.getHisNode().getNodeID()));

			sql = sql.replace("@WorkID", String.valueOf(this.currWn.getHisWork().getOID()));
			sql = sql.replace("@FID", String.valueOf(this.currWn.getHisWork().getFID()));

		   if(this.town.getHisNode().getFormType()== NodeFormType.RefOneFrmTree)
           {
              GEEntity en = new GEEntity(this.town.getHisNode().getNodeFrmID(), this.currWn.getHisWork().getOID());
              sql = bp.wf.Glo.DealExp(sql, en, null);
           }
           else
              sql = bp.wf.Glo.DealExp(sql, this.currWn.rptGe, null);

	
			if (sql.contains("@"))
			{
				if (bp.wf.Glo.getSendHTOfTemp() != null)
				{
					for (Object key : bp.wf.Glo.getSendHTOfTemp().keySet())
					{
						sql = sql.replace("@" + key, bp.wf.Glo.getSendHTOfTemp().get(key).toString());
					}
				}
			}

			if (sql.contains("@GuestUser.No"))
			{
				sql = sql.replace("@GuestUser.No", GuestUser.getNo());
			}

			if (sql.contains("@GuestUser.Name"))
			{
				sql = sql.replace("@GuestUser.Name", GuestUser.getName());
			}

			dt = DBAccess.RunSQLReturnTable(sql);
			if (dt.Rows.size() == 0 && town.getHisNode().getHisWhenNoWorker() == false)
			{
				throw new RuntimeException("@没有找到可接受的工作人员。@技术信息：执行的SQL没有发现人员:" + sql);
			}
			return dt;
		}


			///按绑定部门计算,该部门一人处理标识该工作结束(子线程)..
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySetDeptAsSubthread)
		{
			if (this.town.getHisNode().getHisRunModel() != RunModel.SubThread)
			{
				throw new RuntimeException("@您设置的节点接收人方式为：按绑定部门计算,该部门一人处理标识该工作结束(子线程)，但是当前节点非子线程节点。");
			}

			sql = "SELECT No, Name,FK_Dept AS GroupMark FROM Port_Emp WHERE FK_Dept IN (SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node=" + town.getHisNode().getNodeID() + ")";
			dt = DBAccess.RunSQLReturnTable(sql);
			if (dt.Rows.size() == 0 && town.getHisNode().getHisWhenNoWorker() == false)
			{
				throw new RuntimeException("@没有找到可接受的工作人员,接受人方式为, ‘按绑定部门计算,该部门一人处理标识该工作结束(子线程)’ @技术信息：执行的SQL没有发现人员:" + sql);
			}
			return dt;
		}

			/// 按绑定部门计算,该部门一人处理标识该工作结束(子线程)..


			///按照明细表,作为子线程的接收人.
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByDtlAsSubThreadEmps)
		{
			if (this.town.getHisNode().getHisRunModel() != RunModel.SubThread)
			{
				throw new RuntimeException("@您设置的节点接收人方式为：以分流点表单的明细表数据源确定子线程的接收人，但是当前节点非子线程节点。");
			}

			this.currWn.getHisNode().WorkID=this.WorkID; //为获取表单ID ( NodeFrmID )提供参数.
			MapDtls dtls = new MapDtls(this.currWn.getHisNode().getNodeFrmID());
			String msg = null;
			for (MapDtl dtl : dtls.ToJavaList())
			{
				try
				{
					String empFild = town.getHisNode().getDeliveryParas();
					if (DataType.IsNullOrEmpty(empFild))
					{
						empFild = " UserNo ";
					}

					ps = new Paras();
					ps.SQL="SELECT " + empFild + ", * FROM " + dtl.getPTable() + " WHERE RefPK=" + dbStr + "OID ORDER BY OID";
					ps.Add("OID", this.WorkID);
					dt = DBAccess.RunSQLReturnTable(ps);
					if (dt.Rows.size() == 0 && town.getHisNode().getHisWhenNoWorker() == false)
					{
						throw new RuntimeException("@流程设计错误，到达的节点（" + town.getHisNode().getName() + "）在指定的节点中没有数据，无法找到子线程的工作人员。");
					}
					return dt;
				}
				catch (RuntimeException ex)
				{
					msg += ex.getMessage();
					//if (dtls.size() == 1)
					//    throw new Exception("@估计是流程设计错误,没有在分流节点的明细表中设置");
				}
			}
			throw new RuntimeException("@没有找到分流节点的明细表作为子线程的发起的数据源，流程设计错误，请确认分流节点表单中的明细表是否有UserNo约定的系统字段。" + msg);
		}

			/// 按照明细表,作为子线程的接收人.


			///按节点绑定的人员处理.
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByBindEmp)
		{
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.SQL="SELECT FK_Emp FROM WF_NodeEmp WHERE FK_Node=" + dbStr + "FK_Node ORDER BY FK_Emp";
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() == 0)
			{
				throw new RuntimeException("@流程设计错误:下一个节点(" + town.getHisNode().getName() + ")没有绑定工作人员 . ");
			}
			return dt;
		}

			/// 按节点绑定的人员处理.



			///按照部门负责人计算. @gaoxin 翻译过去.
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByDeptLeader)
		{
			ps = new Paras();
			ps.Add("No", WebUser.getFK_Dept());
			ps.SQL="SELECT Leader FROM Port_Dept WHERE No=" + dbStr + "No";
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() == 0)
			{
				throw new RuntimeException("@流程设计错误:下一个节点(" + town.getHisNode().getName() + ")设置的按照部门负责人计算，当前您的部门(" + WebUser.getFK_Dept() + "," + WebUser.getFK_DeptName() + ")没有维护负责人 . ");
			}
			return dt;
		}

			/// .按照部门负责人计算



			///按照选择的人员处理。
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySelected || town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySelectedForPrj
				|| town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByFEE
				|| town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByDeptAndEmpField)
		{
			ps = new Paras();
			ps.Add("FK_Node", this.town.getHisNode().getNodeID());
			ps.Add("WorkID", this.currWn.getHisWork().getOID());
			ps.SQL="SELECT FK_Emp FROM WF_SelectAccper WHERE FK_Node=" + dbStr + "FK_Node AND WorkID=" + dbStr + "WorkID AND AccType=0 ORDER BY IDX";
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() == 0)
			{
				/*从上次发送设置的地方查询. */
				SelectAccpers sas = new SelectAccpers();
				int i = sas.QueryAccepterPriSetting(this.town.getHisNode().getNodeID());
				if (i == 0)
				{
					if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySelected || town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySelectedForPrj)
					{
						Node toNode = this.town.getHisNode();
						Selector select = new Selector(toNode.getNodeID());
						if (select.getSelectorModel() == SelectorModel.GenerUserSelecter)
						{
							throw new RuntimeException("url@./WorkOpt/AccepterOfGener.htm?FK_Flow=" + toNode.getFK_Flow() + "&FK_Node=" + this.currWn.getHisNode().getNodeID() + "&ToNode=" + toNode.getNodeID() + "&WorkID=" + this.WorkID);
						}
						else
						{
							throw new RuntimeException("url@./WorkOpt/Accepter.htm?FK_Flow=" + toNode.getFK_Flow() + "&FK_Node=" + this.currWn.getHisNode().getNodeID() + "&ToNode=" + toNode.getNodeID() + "&WorkID=" + this.WorkID);
						}
					} else if(town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByDeptAndEmpField){
						throw new RuntimeException("url@./WorkOpt/AccepterOfDept.htm?FK_Flow=" + town.getHisNode().getFK_Flow() + "&FK_Node=" + this.currWn.getHisNode().getNodeID() + "&ToNode=" + town.getHisNode().getNodeID() + "&WorkID=" + this.WorkID);
					}
					else
					{
						throw new RuntimeException("@流程设计错误，请重写FEE，然后为节点(" + town.getHisNode().getName() + ")设置接受人员，详细请参考cc流程设计手册。");
					}
				}

				//插入里面.
				for (SelectAccper item : sas.ToJavaList())
				{
					DataRow dr = dt.NewRow();
					dr.setValue(0, item.getFK_Emp());
					dt.Rows.add(dr);
				}
				return dt;
			}
			return dt;
		}

			/// 按照选择的人员处理。


			///按照指定节点的处理人计算。
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySpecNodeEmp || town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByStarter)
		{
			/* 按指定节点的人员计算 */
			String strs = town.getHisNode().getDeliveryParas();
			if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByStarter)
			{
				long myworkid = this.currWn.getWorkID();
				if (this.currWn.getHisWork().getFID() != 0)
				{
					myworkid = this.currWn.getHisWork().getFID();
				}
				dt = DBAccess.RunSQLReturnTable("SELECT Starter as No, StarterName as Name FROM WF_GenerWorkFlow WHERE WorkID=" + myworkid);
				if (dt.Rows.size() == 1)
				{
					return dt;
				}

				/* 有可能当前节点就是第一个节点，那个时间还没有初始化数据，就返回当前人. */
				if (this.currWn.getHisNode().getIsStartNode())
				{
					DataRow dr = dt.NewRow();
					dr.setValue(0, WebUser.getNo());
					dt.Rows.add(dr);
					return dt;
				}

				if (dt.Rows.size() == 0)
				{
					throw new RuntimeException("@流程设计错误，到达的节点（" + town.getHisNode().getName() + "）无法找到开始节点的工作人员。");
				}
				else
				{
					return dt;
				}

			}

			// 首先从本流程里去找。
			strs = strs.replace(";", ",");
			String[] nds = strs.split("[,]", -1);
			for (String nd : nds)
			{
				if (DataType.IsNullOrEmpty(nd))
				{
					continue;
				}

				if (DataType.IsNumStr(nd) == false)
				{
					throw new RuntimeException("流程设计错误:您设置的节点(" + town.getHisNode().getName() + ")的接收方式为按指定的节点岗位投递，但是您没有在访问规则设置中设置节点编号。");
				}

				ps = new Paras();
				String workSQL = "";
				//获取指定节点的信息
				Node specNode = new Node(nd);
				//指定节点是子线程
				if (specNode.getHisRunModel() == RunModel.SubThread)
				{
					if (this.currWn.getHisNode().getHisRunModel() == RunModel.SubThread)
					{
						workSQL = "FID=" + this.currWn.getHisWork().getFID();
					}
					else
					{
						workSQL = "FID=" + this.WorkID;
					}
				}
				else
				{
					if (this.currWn.getHisNode().getHisRunModel() == RunModel.SubThread)
					{
						workSQL = "WorkID=" + this.currWn.getHisWork().getFID();
					}
					else
					{
						workSQL = "WorkID=" + this.WorkID;
					}

				}

				ps.SQL="SELECT DISTINCT(FK_Emp) FROM WF_GenerWorkerList WHERE " + workSQL + " AND FK_Node=" + dbStr + "FK_Node AND IsEnable=1 ";
				ps.Add("FK_Node", Integer.parseInt(nd));

				DataTable dt_ND = DBAccess.RunSQLReturnTable(ps);
				//添加到结果表
				if (dt_ND.Rows.size() != 0)
				{
					for (DataRow row : dt_ND.Rows)
					{
						DataRow dr = dt.NewRow();
						dr.setValue(0, row.getValue(0).toString());
						dt.Rows.add(dr);
					}
					//此节点已找到数据则不向下找，继续下个节点
					continue;
				}

				//就要到轨迹表里查,因为有可能是跳过的节点.
				ps = new Paras();
				ps.SQL="SELECT DISTINCT(" + TrackAttr.EmpFrom + ") FROM ND" + Integer.parseInt(fl.getNo()) + "Track WHERE" + " (ActionType=" + dbStr + "ActionType1 OR ActionType=" + dbStr + "ActionType2 OR ActionType=" + dbStr + "ActionType3" + "  OR ActionType=" + dbStr + "ActionType4 OR ActionType=" + dbStr + "ActionType5 OR ActionType=" + dbStr + "ActionType6)" + "   AND NDFrom=" + dbStr + "NDFrom AND " + workSQL;
				ps.Add("ActionType1", ActionType.Skip.getValue());
				ps.Add("ActionType2", ActionType.Forward.getValue());
				ps.Add("ActionType3", ActionType.ForwardFL.getValue());
				ps.Add("ActionType4", ActionType.ForwardHL.getValue());
				ps.Add("ActionType5", ActionType.SubThreadForward.getValue());
				ps.Add("ActionType6", ActionType.Start.getValue());
				ps.Add("NDFrom", Integer.parseInt(nd));

				dt_ND = DBAccess.RunSQLReturnTable(ps);
				if (dt_ND.Rows.size() != 0)
				{
					for (DataRow row : dt_ND.Rows)
					{
						DataRow dr = dt.NewRow();
						dr.setValue(0, row.getValue(0).toString());
						dt.Rows.add(dr);
					}
					continue;
				}

				//从Selector中查找
				ps = new Paras();
				ps.SQL="SELECT DISTINCT(FK_Emp) FROM WF_SelectAccper WHERE FK_Node=" + dbStr + "FK_Node AND " + workSQL;
				ps.Add("FK_Node", Integer.parseInt(nd));


				dt_ND = DBAccess.RunSQLReturnTable(ps);
				//添加到结果表
				if (dt_ND.Rows.size() != 0)
				{
					for (DataRow row : dt_ND.Rows)
					{
						DataRow dr = dt.NewRow();
						dr.setValue(0, row.getValue(0).toString());
						dt.Rows.add(dr);
					}
					//此节点已找到数据则不向下找，继续下个节点
					continue;
				}


			}

			//本流程里没有有可能该节点是配置的父流程节点,也就是说子流程的一个节点与父流程指定的节点的工作人员一致.
			GenerWorkFlow gwf = new GenerWorkFlow(this.WorkID);
			if (gwf.getPWorkID() != 0)
			{
				for (String pnodeiD : nds)
				{
					if (DataType.IsNullOrEmpty(pnodeiD))
					{
						continue;
					}

					Node nd = new Node(Integer.parseInt(pnodeiD));
					if (!nd.getFK_Flow().equals(gwf.getPFlowNo()))
					{
						continue; // 如果不是父流程的节点，就不执行.
					}

					ps = new Paras();
					ps.SQL="SELECT FK_Emp FROM WF_GenerWorkerList WHERE WorkID=" + dbStr + "OID AND FK_Node=" + dbStr + "FK_Node AND IsPass=1 AND IsEnable=1 ";
					ps.Add("FK_Node", nd.getNodeID());
					if (this.currWn.getHisNode().getHisRunModel() == RunModel.SubThread)
					{
						ps.Add("OID", gwf.getPFID());
					}
					else
					{
						ps.Add("OID", gwf.getPWorkID());
					}

					DataTable dt_PWork = DBAccess.RunSQLReturnTable(ps);
					if (dt_PWork.Rows.size() != 0)
					{
						for (DataRow row : dt_PWork.Rows)
						{
							DataRow dr = dt.NewRow();
							dr.setValue(0, row.getValue(0).toString());
							dt.Rows.add(dr);
						}
						//此节点已找到数据则不向下找，继续下个节点
						continue;
					}

					//就要到轨迹表里查,因为有可能是跳过的节点.
					ps = new Paras();
					ps.SQL="SELECT " + TrackAttr.EmpFrom + " FROM ND" + Integer.parseInt(fl.getNo()) + "Track WHERE (ActionType=" + dbStr + "ActionType1 OR ActionType=" + dbStr + "ActionType2 OR ActionType=" + dbStr + "ActionType3 OR ActionType=" + dbStr + "ActionType4 OR ActionType=" + dbStr + "ActionType5) AND NDFrom=" + dbStr + "NDFrom AND WorkID=" + dbStr + "WorkID";
					ps.Add("ActionType1", ActionType.Start.getValue());
					ps.Add("ActionType2", ActionType.Forward.getValue());
					ps.Add("ActionType3", ActionType.ForwardFL.getValue());
					ps.Add("ActionType4", ActionType.ForwardHL.getValue());
					ps.Add("ActionType5", ActionType.Skip.getValue());

					ps.Add("NDFrom", nd.getNodeID());

					if (this.currWn.getHisNode().getHisRunModel() == RunModel.SubThread)
					{
						ps.Add("WorkID", gwf.getPFID());
					}
					else
					{
						ps.Add("WorkID", gwf.getPWorkID());
					}

					dt_PWork = DBAccess.RunSQLReturnTable(ps);
					if (dt_PWork.Rows.size() != 0)
					{
						for (DataRow row : dt_PWork.Rows)
						{
							DataRow dr = dt.NewRow();
							dr.setValue(0, row.getValue(0).toString());
							dt.Rows.add(dr);
						}
					}
				}
			}
			//返回指定节点的处理人
			if (dt.Rows.size() != 0)
			{
				return dt;
			}

			throw new RuntimeException("@流程设计错误，到达的节点（" + town.getHisNode().getName() + "）在指定的节点(" + strs + ")中没有数据，无法找到工作的人员。 @技术信息如下: 投递方式:BySpecNodeEmp sql=" + ps.getSQLNoPara());
		}

			/// 按照节点绑定的人员处理。


			///按照上一个节点表单指定字段的人员处理。
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByPreviousNodeFormEmpsField)
		{
			// 检查接受人员规则,是否符合设计要求.
			String specEmpFields = town.getHisNode().getDeliveryParas();
			if (DataType.IsNullOrEmpty(specEmpFields))
			{
				specEmpFields = "SysSendEmps";
			}

			if (this.currWn.rptGe.getEnMap().getAttrs().Contains(specEmpFields) == false)
			{
				throw new RuntimeException("@您设置的接受人规则是按照表单指定的字段，决定下一步的接受人员，该字段{" + specEmpFields + "}已经删除或者丢失。");
			}

			//获取接受人并格式化接受人, 
			String emps = this.currWn.rptGe.GetValStringByKey(specEmpFields);
			emps = emps.replace(" ", ""); //去掉空格.

			if (emps.contains(",") && emps.contains(";"))
			{
				/*如果包含,; 例如 zhangsan,张三;lisi,李四;*/
				String[] myemps1 = emps.split("[;]", -1);
				for (String str : myemps1)
				{
					if (DataType.IsNullOrEmpty(str))
					{
						continue;
					}

					String[] ss = str.split("[,]", -1);
					DataRow dr = dt.NewRow();
					dr.setValue(0, ss[0]);
					dt.Rows.add(dr);
				}
				if (dt.Rows.size() == 0)
				{
					throw new RuntimeException("@输入的接受人员信息错误;[" + emps + "]。");
				}
				else
				{
					return dt;
				}
			}

			emps = emps.replace(";", ",");
			emps = emps.replace("；", ",");
			emps = emps.replace("，", ",");
			emps = emps.replace("、", ",");
			emps = emps.replace("@", ",");

			if (DataType.IsNullOrEmpty(emps))
			{
				throw new RuntimeException("@没有在字段[" + this.currWn.getHisWork().getEnMap().getAttrs().GetAttrByKey(specEmpFields).getDesc() + "]中指定接受人，工作无法向下发送。");
			}

			// 把它加入接受人员列表中.
			String[] myemps = emps.split("[,]", -1);
			for (String s : myemps)
			{
				if (DataType.IsNullOrEmpty(s))
				{
					continue;
				}

				//if (DBAccess.RunSQLReturnValInt("SELECT COUNT(NO) AS NUM FROM Port_Emp WHERE NO='" + s + "' or name='"+s+"'", 0) == 0)
				//    continue;

				DataRow dr = dt.NewRow();
				dr.setValue(0, s);
				dt.Rows.add(dr);
			}
			return dt;
		}

			/// 按照上一个节点表单指定字段的人员处理。

		String prjNo = "";
		FlowAppType flowAppType = this.currWn.getHisNode().getHisFlow().getHisFlowAppType();
		sql = "";
		if (this.currWn.getHisNode().getHisFlow().getHisFlowAppType() == FlowAppType.PRJ)
		{
			prjNo = "";
			try
			{
				prjNo = this.currWn.rptGe.GetValStrByKey("PrjNo");
			}
			catch (RuntimeException ex)
			{
				if (this.currWn.rptGe.getEnMap().getAttrs().Contains("PrjNo") == false)
				{
					throw new RuntimeException("@当前流程是工程类流程，但是在节点表单中没有PrjNo字段(注意区分大小写)，请确认。@异常信息:" + ex.getMessage());
				}
				else
				{
					throw ex;
				}
			}

			if (DataType.IsNullOrEmpty(prjNo) == true)
			{
				throw new RuntimeException("err@没有找到项目编号PrjNo.");
			}
		}



			///为省立医院增加，按照指定的部门范围内的岗位计算..
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.FindSpecDeptEmpsInStationlist)
		{
			//sql = "SELECT pdes.FK_Emp AS No"
			//      + " FROM   Port_DeptEmpStation pdes"
			//      + " INNER JOIN WF_NodeDept wnd ON wnd.FK_Dept = pdes.FK_Dept"
			//      + " AND wnd.FK_Node = " + town.HisNode.NodeID
			//      + " INNER JOIN WF_NodeStation wns ON  wns.FK_Station = pdes.FK_Station"
			//      + " AND wns.FK_Node =" + town.HisNode.NodeID
			//      + " ORDER BY pdes.FK_Emp";

			sql = "SELECT A.FK_Emp FROM Port_DeptEmpStation A, WF_NodeDept B, WF_NodeStation C ";
			sql += "WHERE  A.FK_Dept=B.FK_Dept AND B.FK_Node=C.FK_Node AND C.FK_Node=" + town.getHisNode().getNodeID() + " AND A.FK_Dept='" + WebUser.getFK_Dept() + "'";

			dt = DBAccess.RunSQLReturnTable(sql);

			if (dt.Rows.size() > 0)
			{
				return dt;
			}
			else
			{
				if (this.town.getHisNode().getHisWhenNoWorker() == false)
				{
					throw new RuntimeException("@节点访问规则(" + town.getHisNode().getHisDeliveryWay().toString() + ")错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 按照岗位与部门的交集确定接受人的范围错误，没有找到人员:SQL=" + sql);
				}
				else
				{
					return dt;
				}
			}
		}

			/// 按部门与岗位的交集计算.


			///按部门与岗位的交集计算.
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByDeptAndStation)
		{
			//added by liuxc,2015.6.29.
			//区别集成与BPM模式

			sql = "SELECT pdes.FK_Emp AS No" + " FROM   Port_DeptEmpStation pdes" + " INNER JOIN WF_NodeDept wnd ON wnd.FK_Dept = pdes.FK_Dept" + " AND wnd.FK_Node = " + town.getHisNode().getNodeID() + " INNER JOIN WF_NodeStation wns ON  wns.FK_Station = pdes.FK_Station" + " AND wns.FK_Node =" + town.getHisNode().getNodeID() + " ORDER BY pdes.FK_Emp";

			dt = DBAccess.RunSQLReturnTable(sql);

			if (dt.Rows.size() > 0)
			{
				return dt;
			}
			else
			{
				if (this.town.getHisNode().getHisWhenNoWorker() == false)
				{
					throw new RuntimeException("@节点访问规则(" + town.getHisNode().getHisDeliveryWay().toString() + ")错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 按照岗位与部门的交集确定接受人的范围错误，没有找到人员:SQL=" + sql);
				}
				else
				{
					return dt;
				}
			}
		}

			/// 按部门与岗位的交集计算.


			///判断节点部门里面是否设置了部门，如果设置了就按照它的部门处理。
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByDept)
		{
			ps = new Paras();
			ps.Add("FK_Node", this.town.getHisNode().getNodeID());
			ps.Add("WorkID", this.currWn.getHisWork().getOID());
			ps.SQL="SELECT FK_Emp FROM WF_SelectAccper WHERE FK_Node=" + dbStr + "FK_Node AND WorkID=" + dbStr + "WorkID AND AccType=0 ORDER BY IDX";
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() > 0)
			{
				return dt;
			}

			if (flowAppType == FlowAppType.Normal)
			{
				ps = new Paras();

				String mysql = "SELECT  A.No, A.Name  FROM Port_Emp A, WF_NodeDept B, Port_DeptEmp C  WHERE  A.No=C.FK_Emp AND C.FK_Dept=B.FK_Dept AND B.FK_Node=" + dbStr + "FK_Node";
				mysql += " UNION ";
				mysql += "SELECT  A.No, A.Name  FROM Port_Emp A, WF_NodeDept B WHERE A.FK_Dept=B.FK_Dept AND B.FK_Node=" + dbStr + "FK_Node";
				ps.SQL=mysql;

				ps.Add("FK_Node", town.getHisNode().getNodeID());
				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() > 0 && town.getHisNode().getHisWhenNoWorker() == false)
				{
					return dt;
				}
				else
				{
					throw new RuntimeException("@按部门确定接受人的范围,没有找到人员.");
				}
			}

			if (flowAppType == FlowAppType.PRJ)
			{
				sql = " SELECT A.No,A.Name FROM Port_Emp A, WF_NodeDept B, Prj_EmpPrjStation C, WF_NodeStation D ";
				sql += "  WHERE A.FK_Dept=B.FK_Dept AND A.No=C.FK_Emp AND C.FK_Station=D.FK_Station AND B.FK_Node=D.FK_Node ";
				sql += "  AND C.FK_Prj=" + dbStr + "FK_Prj  AND D.FK_Node=" + dbStr + "FK_Node";

				ps = new Paras();
				ps.Add("FK_Prj", prjNo);
				ps.Add("FK_Node", town.getHisNode().getNodeID());
				ps.SQL=sql;

				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() == 0)
				{
					/* 如果项目组里没有工作人员就提交到公共部门里去找。*/
					sql = "SELECT NO FROM Port_Emp WHERE NO IN ";


					sql += "(SELECT FK_Emp FROM Port_DeptEmp WHERE FK_Dept IN ";

					sql += "( SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node=" + dbStr + "FK_Node1)";
					sql += ")";
					sql += "AND NO IN ";
					sql += "(";
					sql += "SELECT FK_Emp FROM " + bp.wf.Glo.getEmpStation() + " WHERE FK_Station IN ";
					sql += "( SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + dbStr + "FK_Node2)";
					sql += ")";
					sql += " ORDER BY No";

					ps = new Paras();
					ps.Add("FK_Node1", town.getHisNode().getNodeID());
					ps.Add("FK_Node2", town.getHisNode().getNodeID());
					ps.SQL=sql;
				}
				else
				{
					return dt;
				}

				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() > 0)
				{
					return dt;
				}
			}
		}

			/// 判断节点部门里面是否设置了部门，如果设置了，就按照它的部门处理。


			///按照岗位计算，项目类.
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByStationForPrj)
		{
			sql = "SELECT A.FK_Emp FROM " + bp.wf.Glo.getEmpStation() + " A, WF_NodeStation B, WF_PrjEmp C WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node AND A.FK_Emp=C.FK_Emp AND C.FK_Prj='" + prjNo + "' ORDER BY A.FK_Emp";
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.SQL=sql;
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() > 0)
			{
				return dt;
			}
			else
			{
				if (this.town.getHisNode().getHisWhenNoWorker() == false)
				{
					throw new RuntimeException("@节点访问规则错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 仅按岗位计算，没有找到人员:SQL=" + ps.getSQLNoPara());
				}
				else
				{
					return dt; //可能处理跳转,在没有处理人的情况下.
				}
			}
		}

			/// 按照岗位计算，项目类.


			///用户组 计算 @lizhen
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByTeamOnly)
		{
			sql = "SELECT A.FK_Emp FROM Port_TeamEmp A, WF_NodeTeam B WHERE A.FK_Team=B.FK_Team AND B.FK_Node=" + dbStr + "FK_Node ORDER BY A.FK_Emp";
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.SQL=sql;
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() > 0)
			{
				return dt;
			}
			else
			{
				if (this.town.getHisNode().getHisWhenNoWorker() == false)
				{
					throw new RuntimeException("@节点访问规则错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 仅按用户组计算，没有找到人员:SQL=" + ps.getSQLNoPara());
				}
				else
				{
					return dt; //可能处理跳转,在没有处理人的情况下.
				}
			}
		}
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByTeamOrgOnly)
		{
			sql = "SELECT DISTINCT A.FK_Emp FROM Port_TeamEmp A, WF_NodeTeam B, Port_Emp C WHERE A.FK_Emp=C.No AND A.FK_Team=B.FK_Team AND B.FK_Node=" + dbStr + "FK_Node AND C.OrgNo=" + dbStr + "OrgNo  ORDER BY A.FK_Emp";
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("OrgNo", WebUser.getOrgNo());

			ps.SQL=sql;
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() > 0)
			{
				return dt;
			}

			if (this.town.getHisNode().getHisWhenNoWorker() == false)
			{
				throw new RuntimeException("@节点访问规则错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 仅按用户组计算，没有找到人员:SQL=" + ps.getSQLNoPara());
			}

			return dt; //可能处理跳转,在没有处理人的情况下.
		}

		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByTeamDeptOnly)
		{
			sql = "SELECT DISTINCT A.FK_Emp FROM Port_TeamEmp A, WF_NodeTeam B, Port_Emp C WHERE A.FK_Emp=C.No AND A.FK_Team=B.FK_Team AND B.FK_Node=" + dbStr + "FK_Node AND C.FK_Dept=" + dbStr + "FK_Dept  ORDER BY A.FK_Emp";
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", WebUser.getFK_Dept());

			ps.SQL=sql;
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() > 0)
			{
				return dt;
			}

			if (this.town.getHisNode().getHisWhenNoWorker() == false)
			{
				throw new RuntimeException("@节点访问规则错误 ByTeamDeptOnly :节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 仅按用户组计算，没有找到人员:SQL=" + ps.getSQLNoPara());
			}

			return dt; //可能处理跳转,在没有处理人的情况下.
		}

			///


			///仅按岗位计算
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByStationOnly)
		{
			//2020-4-25 按照岗位倒序排序 修改原因队列模式时，下级岗位处理后发给上级岗位， 岗位越高数值越小
			sql = "SELECT A.FK_Emp FROM " + bp.wf.Glo.getEmpStation() + " A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node ORDER BY A.FK_Station desc";
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.SQL=sql;
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() > 0)
			{
				return dt;
			}

			if (this.town.getHisNode().getHisWhenNoWorker() == false)
			{
				throw new RuntimeException("@节点访问规则错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 仅按岗位计算，没有找到人员:SQL=" + ps.getSQLNoPara());
			}

			return dt; //可能处理跳转,在没有处理人的情况下.
		}

			///


			///按配置的人员路由表计算
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByFromEmpToEmp)
		{
			String[] fromto = town.getHisNode().getDeliveryParas().split("[@]", -1);

			String defUser = "";

			for (String str : fromto)
			{
				String[] kv = str.split("[,]", -1);

				if (kv[0].equals("Defalut") == true)
				{
					defUser = kv[1];
					continue;
				}

				if (kv[0].equals(WebUser.getNo()))
				{
					String empTo = kv[1];
					//bp.port.Emp emp = new bp.port.Emp(empTo);
					DataRow dr = dt.NewRow();
					dr.setValue(0, empTo);
					//  dr[1] = emp.getName();
					dt.Rows.add(dr);
					return dt;
				}
			}

			if (DataType.IsNullOrEmpty(defUser) == false)
			{
				String empTo = defUser;
				DataRow dr = dt.NewRow();
				dr.setValue(0, empTo);
				dt.Rows.add(dr);
				return dt;
			}

			throw new RuntimeException("@接收人规则是按照人员路由表设置的，但是系统管理员没有为您配置路由,当前节点;" + town.getHisNode().getName());
		}

			///


			///按岗位计算(以部门集合为纬度).
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.ByStationAndEmpDept)
		{
			/* 考虑当前操作人员的部门, 如果本部门没有这个岗位就不向上寻找. */
			ps = new Paras();
			sql = "SELECT No,Name FROM Port_Emp WHERE No=" + dbStr + "FK_Emp ";
			ps.Add("FK_Emp", WebUser.getNo());
			dt = DBAccess.RunSQLReturnTable(ps);

			if (dt.Rows.size() > 0)
			{
				return dt;
			}
			else
			{
				if (this.town.getHisNode().getHisWhenNoWorker() == false)
				{
					throw new RuntimeException("@节点访问规则(" + town.getHisNode().getHisDeliveryWay().toString() + ")错误:节点(" + town.getHisNode().getNodeID() + "," + town.getHisNode().getName() + "), 按岗位计算(以部门集合为纬度)。技术信息,执行的SQL=" + ps.getSQLNoPara());
				}
				else
				{
					return dt; //可能处理跳转,在没有处理人的情况下.
				}
			}
		}

			///

		String empNo = WebUser.getNo();
		String empDept = WebUser.getFK_Dept();


			///按指定的节点的人员岗位，做为下一步骤的流程接受人。
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySpecNodeEmpStation)
		{
			/* 按指定的节点的人员岗位 */
			String para = town.getHisNode().getDeliveryParas();
			para = para.replace("@", "");
			para = para.replace("@", "");

			if (DataType.IsNullOrEmpty(para) == false)
			{
				String[] strs = para.split("[,]", -1);

				for (String str : strs)
				{
					if (DataType.IsNullOrEmpty(str) == true)
					{
						continue;
					}

					ps = new Paras();
					ps.SQL="SELECT FK_Emp,FK_Dept FROM WF_GenerWorkerList WHERE WorkID=" + dbStr + "OID AND FK_Node=" + dbStr + "FK_Node ";
					ps.Add("OID", this.WorkID);
					ps.Add("FK_Node", Integer.parseInt(str));

					dt = DBAccess.RunSQLReturnTable(ps);
					if (dt.Rows.size() != 1)
					{
						continue;
					}

					empNo = dt.Rows.get(0).getValue(0).toString();
					empDept = dt.Rows.get(0).getValue(1).toString();
				}

				//  throw new Exception("@流程设计错误，到达的节点（" + town.HisNode.Name + "）在指定的节点中没有数据，无法找到工作的人员，指定的节点是:"+para);
			}
			else
			{
				if (this.currWn.rptGe.getRow().containsKey(para) == false)
				{
					throw new RuntimeException("@在找人接收人的时候错误@字段{" + para + "}不包含在rpt里，流程设计错误。");
				}

				empNo = this.currWn.rptGe.GetValStrByKey(para);
				if (DataType.IsNullOrEmpty(empNo))
				{
					throw new RuntimeException("@字段{" + para + "}不能为空，没有取出来处理人员。");
				}

				bp.port.Emp em = new bp.port.Emp(empNo);
				empDept = em.getFK_Dept();
			}
		}

			/// 按指定的节点人员，做为下一步骤的流程接受人。


			///按照自定义的URL来计算
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySelfUrl)
		{
			ps = new Paras();
			ps.Add("FK_Node", this.town.getHisNode().getNodeID());
			ps.Add("WorkID", this.currWn.getHisWork().getOID());
			ps.SQL="SELECT FK_Emp FROM WF_SelectAccper WHERE FK_Node=" + dbStr + "FK_Node AND WorkID=" + dbStr + "WorkID AND AccType=0 ORDER BY IDX";
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() == 0)
			{
				/*从上次发送设置的地方查询. */
				SelectAccpers sas = new SelectAccpers();
				int i = sas.QueryAccepterPriSetting(this.town.getHisNode().getNodeID());
				if (i == 0)
				{
				   Node toNode = this.town.getHisNode();
					GenerWorkFlow gwf = new GenerWorkFlow(this.WorkID);
					if (DataType.IsNullOrEmpty(toNode.getDeliveryParas()) == true)
					{
						throw new RuntimeException("节点" + toNode.getNodeID() + "_" + toNode.getName() + "设置的接收人规则是自定义的URL,现在未获取到设置的信息");
					}
					else
					{
						throw new RuntimeException("BySelfUrl@" + toNode.getDeliveryParas() + "?FK_Flow=" + toNode.getFK_Flow() + "&FK_Node=" + this.currWn.getHisNode().getNodeID() + "&ToNode=" + toNode.getNodeID() + "&WorkID=" + this.WorkID + "&PWorkID=" + gwf.getPWorkID() + "&FID=" + gwf.getFID());
					}
				}

				//插入里面.
				for (SelectAccper item : sas.ToJavaList())
				{
					DataRow dr = dt.NewRow();
					dr.setValue(0, item.getFK_Emp());
					dt.Rows.add(dr);
				}
				return dt;
			}
			return dt;
		}

			/// 按照自定义的URL来计算


			///按照组织模式人员选择器
		if (town.getHisNode().getHisDeliveryWay() == DeliveryWay.BySelectedEmpsOrgModel)
		{
			ps = new Paras();
			ps.Add("FK_Node", this.town.getHisNode().getNodeID());
			ps.Add("WorkID", this.currWn.getHisWork().getOID());
			ps.SQL="SELECT FK_Emp FROM WF_SelectAccper WHERE FK_Node=" + dbStr + "FK_Node AND WorkID=" + dbStr + "WorkID AND AccType=0 ORDER BY IDX";
			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() == 0)
			{
				/*从上次发送设置的地方查询. */
				SelectAccpers sas = new SelectAccpers();
				int i = sas.QueryAccepterPriSetting(this.town.getHisNode().getNodeID());
				if (i == 0)
				{
					Node toNode = this.town.getHisNode();
					throw new RuntimeException("url@./WorkOpt/AccepterOfOrg.htm?FK_Flow=" + toNode.getFK_Flow() + "&FK_Node=" + this.currWn.getHisNode().getNodeID() + "&ToNode=" + toNode.getNodeID() + "&WorkID=" + this.WorkID);
				}

				//插入里面.
				for (SelectAccper item : sas.ToJavaList())
				{
					DataRow dr = dt.NewRow();
					dr.setValue(0, item.getFK_Emp());
					dt.Rows.add(dr);
				}
				return dt;
			}
			return dt;
		}

			/// 按照自定义的URL来计算


			///最后判断 - 按照岗位来执行。
		if (this.currWn.getHisNode().getIsStartNode() == false)
		{
			ps = new Paras();
			if (flowAppType == FlowAppType.Normal || flowAppType == FlowAppType.DocFlow)
			{
				// 如果当前的节点不是开始节点， 从轨迹里面查询。
				sql = "SELECT DISTINCT FK_Emp  FROM " + bp.wf.Glo.getEmpStation() + " WHERE FK_Station IN " + "(SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + town.getHisNode().getNodeID() + ") " + "AND FK_Emp IN (SELECT FK_Emp FROM WF_GenerWorkerlist WHERE WorkID=" + dbStr + "WorkID AND FK_Node IN (" + DataType.PraseAtToInSql(town.getHisNode().getGroupStaNDs(), true) + ") )";

				sql += " ORDER BY FK_Emp ";

				ps.SQL=sql;
				ps.Add("WorkID", this.WorkID);
			}

			if (flowAppType == FlowAppType.PRJ)
			{
				// 如果当前的节点不是开始节点， 从轨迹里面查询。
				sql = "SELECT DISTINCT FK_Emp  FROM Prj_EmpPrjStation WHERE FK_Station IN " + "(SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + dbStr + "FK_Node ) AND FK_Prj=" + dbStr + "FK_Prj " + "AND FK_Emp IN (SELECT FK_Emp FROM WF_GenerWorkerlist WHERE WorkID=" + dbStr + "WorkID AND FK_Node IN (" + DataType.PraseAtToInSql(town.getHisNode().getGroupStaNDs(), true) + ") )";
				sql += " ORDER BY FK_Emp ";

				ps = new Paras();
				ps.SQL=sql;
				ps.Add("FK_Node", town.getHisNode().getNodeID());
				ps.Add("FK_Prj", prjNo);
				ps.Add("WorkID", this.WorkID);

				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() == 0)
				{
					/* 如果项目组里没有工作人员就提交到公共部门里去找。*/
					sql = "SELECT DISTINCT FK_Emp  FROM " + bp.wf.Glo.getEmpStation() + " WHERE FK_Station IN " + "(SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + dbStr + "FK_Node ) " + "AND FK_Emp IN (SELECT FK_Emp FROM WF_GenerWorkerlist WHERE WorkID=" + dbStr + "WorkID AND FK_Node IN (" + DataType.PraseAtToInSql(town.getHisNode().getGroupStaNDs(), true) + ") )";
					sql += " ORDER BY FK_Emp ";

					ps = new Paras();
					ps.SQL=sql;
					ps.Add("FK_Node", town.getHisNode().getNodeID());
					ps.Add("WorkID", this.WorkID);
				}
				else
				{
					return dt;
				}
			}

			dt = DBAccess.RunSQLReturnTable(ps);
			// 如果能够找到.
			if (dt.Rows.size() >= 1)
			{
				if (dt.Rows.size() == 1)
				{
					/*如果人员只有一个的情况，说明他可能要 */
				}
				return dt;
			}
		}

		/* 如果执行节点 与 接受节点岗位集合一致 */
		String currGroupStaNDs = this.currWn.getHisNode().getGroupStaNDs();
		String toNodeTeamStaNDs = town.getHisNode().getGroupStaNDs();

		if (DataType.IsNullOrEmpty(currGroupStaNDs) == false && currGroupStaNDs.equals(toNodeTeamStaNDs) == true)
		{
			/* 说明，就把当前人员做为下一个节点处理人。*/
			DataRow dr = dt.NewRow();
			dr.setValue(0, WebUser.getNo());
			dt.Rows.add(dr);
			return dt;
		}

		/* 如果执行节点 与 接受节点岗位集合不一致 */
		if ((DataType.IsNullOrEmpty(toNodeTeamStaNDs) == true && DataType.IsNullOrEmpty(currGroupStaNDs) == true) || currGroupStaNDs.equals(toNodeTeamStaNDs) == false)
		{
			/* 没有查询到的情况下, 先按照本部门计算。*/
			if (flowAppType == FlowAppType.Normal)
			{

				sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B         WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node AND A.FK_Dept=" + dbStr + "FK_Dept";
				ps = new Paras();
				ps.SQL=sql;
				ps.Add("FK_Node", town.getHisNode().getNodeID());
				ps.Add("FK_Dept", empDept);

			}

			if (flowAppType == FlowAppType.PRJ)
			{
				sql = "SELECT  FK_Emp  FROM Prj_EmpPrjStation WHERE FK_Prj=" + dbStr + "FK_Prj1 AND FK_Station IN (SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + dbStr + "FK_Node)" + " AND  FK_Prj=" + dbStr + "FK_Prj2 ";
				sql += " ORDER BY FK_Emp ";

				ps = new Paras();
				ps.SQL=sql;
				ps.Add("FK_Prj1", prjNo);
				ps.Add("FK_Node", town.getHisNode().getNodeID());
				ps.Add("FK_Prj2", prjNo);
				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() == 0)
				{
					/* 如果项目组里没有工作人员就提交到公共部门里去找。 */


					sql = "SELECT No FROM Port_Emp WHERE NO IN " + "(SELECT  FK_Emp  FROM " + bp.wf.Glo.getEmpStation() + " WHERE FK_Station IN (SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + dbStr + "FK_Node))" + " AND  NO IN " + "(SELECT FK_Emp FROM Port_DeptEmp WHERE FK_Dept =" + dbStr + "FK_Dept)";
					sql += " ORDER BY No ";


					ps = new Paras();
					ps.SQL=sql;
					ps.Add("FK_Node", town.getHisNode().getNodeID());
					ps.Add("FK_Dept", empDept);
					//  dt = DBAccess.RunSQLReturnTable(ps);
				}
				else
				{
					return dt;
				}
			}

			dt = DBAccess.RunSQLReturnTable(ps);
			if (dt.Rows.size() == 0)
			{
				NodeStations nextStations = town.getHisNode().getNodeStations();
				if (nextStations.size() == 0)
				{
					throw new RuntimeException("@节点没有岗位:" + town.getHisNode().getNodeID() + "  " + town.getHisNode().getName());
				}
			}
			else
			{
				boolean isInit = false;
				for (DataRow dr : dt.Rows)
				{
					if (dr.getValue(0).toString().equals(WebUser.getNo()))
					{
						/* 如果岗位分组不一样，并且结果集合里还有当前的人员，就说明了出现了当前操作员，拥有本节点上的岗位也拥有下一个节点的工作岗位
						 导致：节点的分组不同，传递到同一个人身上。 */
						isInit = true;
					}
				}

				return dt;
			}
		}

		//@Add 开创的判断， 直线查询父级，和下级
		if(SystemConfig.getCustomerNo().equals("KaiChuang")){
			Object tempVar = empDept;
			String nowDeptID = (String) ((tempVar instanceof String) ? tempVar : null);
			while (true) {
				bp.port.Dept myDept = new bp.port.Dept(nowDeptID);
				nowDeptID = myDept.getParentNo();
				if (nowDeptID.equals("-1") || nowDeptID.toString().equals("0")) {
					break; // 一直找到了最高级仍然没有发现，就跳出来循环从当前操作员人部门向下找。
				}

				// 检查指定的部门下面是否有该人员.
				DataTable mydtTemp = this.Func_GenerWorkerList_DiGui(nowDeptID, empNo);
				if (mydtTemp == null) {
					continue; //不找父级的平级
				} else {
					return mydtTemp;
				}
			}

			// 如果向上找没有找到，就考虑从本级部门上向下找。
			Object tempVar2 = empDept;
			nowDeptID = (String) ((tempVar2 instanceof String) ? tempVar2 : null);
			bp.port.Depts subDepts = new bp.port.Depts(nowDeptID);

			// 递归出来子部门下有该岗位的人员
			DataTable mydt = Func_GenerWorkerList_DiGui_ByDepts(subDepts, empNo);
			if(mydt == null || mydt.Rows.size()  == 0){
				sql = "SELECT A.FK_Emp FROM " + bp.wf.Glo.getEmpStation() + " A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node ORDER BY A.FK_Emp";
				ps = new Paras();
				ps.Add("FK_Node", town.getHisNode().getNodeID());
				ps.SQL = sql;
				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() > 0)
					return dt;
				if (this.town.getHisNode().getHisWhenNoWorker() == false)
					throw new RuntimeException("@按岗位智能计算没有找到(" + town.getHisNode().getName() + ")接受人 @当前工作人员:" + WebUser.getNo()
							+ ",名称:" + WebUser.getName() + " , 部门编号:" + WebUser.getFK_Dept() + " 部门名称："
							+ WebUser.getFK_DeptName());

				if (dt.Rows.size() == 0){
					mydt = new DataTable();
					mydt.Columns.Add(new DataColumn("No", String.class));
					mydt.Columns.Add(new DataColumn("Name", String.class));
				}
			}


			return mydt;
		}

		/*这里去掉了向下级别寻找的算法. */


		/* 没有查询到的情况下, 按照最大匹配数 提高一个级别计算，递归算法未完成。
		 * 因为:以上已经做的岗位的判断，就没有必要在判断其它类型的节点处理了。
		 * */
		Object tempVar = empDept;
		String nowDeptID = tempVar instanceof String ? (String)tempVar : null;

		//第1步:直线父级寻找.
		while (true)
		{
			bp.port.Dept myDept = new bp.port.Dept(nowDeptID);
			nowDeptID = myDept.getParentNo();
			if (nowDeptID.equals("-1") || nowDeptID.toString().equals("0"))
			{
				break; //一直找到了最高级仍然没有发现，就跳出来循环从当前操作员人部门向下找。
				//throw new RuntimeException("@按岗位计算没有找到(" + town.getHisNode().getName() + ")接受人.");
			}

			//检查指定的父部门下面是否有该人员.
			DataTable mydtTemp = this.Func_GenerWorkerList_SpecDept(nowDeptID, empNo);
			if (mydtTemp.Rows.size() != 0)
			{
				return mydtTemp;
			}

			continue;
		}

		//第2步：父级的子级.
		Object tempVar2 = empDept;
		nowDeptID = tempVar2 instanceof String ? (String)tempVar2 : null;
		while (true)
		{
			bp.port.Dept myDept = new bp.port.Dept(nowDeptID);
			nowDeptID = myDept.getParentNo();
			if (nowDeptID.equals("-1") || nowDeptID.toString().equals("0"))
			{
				break; //一直找到了最高级仍然没有发现，就跳出来循环从当前操作员人部门向下找。
				//throw new RuntimeException("@按岗位计算没有找到(" + town.getHisNode().getName() + ")接受人.");
			}

			//该部门下的所有子部门是否有人员.
			DataTable mydtTemp = Func_GenerWorkerList_SpecDept_SameLevel(nowDeptID, empNo);
			if (mydtTemp.Rows.size() != 0)
			{
				return mydtTemp;
			}
			continue;
		}

		/*如果向上找没有找到，就考虑从本级部门上向下找。只找一级下级的平级 */
		Object tempVar3 = empDept;
		nowDeptID = tempVar3 instanceof String ? (String)tempVar3 : null;

		//递归出来子部门下有该岗位的人员
		DataTable mydt = Func_GenerWorkerList_SpecDept_SameLevel(nowDeptID, empNo);

		if ((mydt == null || mydt.Rows.size() == 0) && this.town.getHisNode().getHisWhenNoWorker() == false)
		{
			//如果递归没有找到人,就全局搜索岗位.
			sql = "SELECT A.FK_Emp FROM  Port_DeptEmpStation A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node ORDER BY A.FK_Emp";
			ps = new Paras();
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.SQL=sql;
			dt = DBAccess.RunSQLReturnTable(ps);

			if (dt.Rows.size() > 0)
			{
				return dt;
			}
			if (this.town.getHisNode().getHisWhenNoWorker() == false)
			{
				throw new RuntimeException("@按岗位智能计算没有找到(" + town.getHisNode().getName() + ")接受人 @当前工作人员:" + WebUser.getNo() + ",名称:" + WebUser.getName() + " , 部门编号:" + WebUser.getFK_Dept() + " 部门名称：" + WebUser.getFK_DeptName());
			}

			if (dt.Rows.size() == 0)
			{
				mydt = new DataTable();
				mydt.Columns.Add(new DataColumn("No", String.class));
				mydt.Columns.Add(new DataColumn("Name", String.class));
			}
		}

		return mydt;

			///  按照岗位来执行。
	}

	/**
	 * 递归出来子部门下有该岗位的人员
	 *
	 * @param subDepts
	 * @param empNo
	 * @return
	 * @throws Exception
	 */
	public final DataTable Func_GenerWorkerList_DiGui_ByDepts(bp.port.Depts subDepts, String empNo) throws Exception {
		for (bp.port.Dept item : subDepts.ToJavaList()) {
			DataTable dt = Func_GenerWorkerList_DiGui(item.getNo(), empNo);
			if (dt != null) {
				return dt;
			}

			dt = Func_GenerWorkerList_DiGui_ByDepts(item.getHisSubDepts(), empNo);
			if (dt != null) {
				return dt;
			}
		}
		return null;
	}

	/**
	 * 根据部门获取下一步的操作员
	 *
	 * @param deptNo
	 * @param empNo
	 * @return
	 * @throws Exception
	 */
	public final DataTable Func_GenerWorkerList_DiGui(String deptNo, String empNo) throws Exception {
		String sql;

		Paras ps = new Paras();

		if (this.town.getHisNode().getIsExpSender() == true) {

			sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node="
					+ dbStr + "FK_Node AND A.FK_Dept=" + dbStr + "FK_Dept AND A.FK_Emp!=" + dbStr + "FK_Emp";

			ps.SQL = sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", deptNo);
			ps.Add("FK_Emp", empNo);

		} else {

			sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node="
					+ dbStr + "FK_Node AND A.FK_Dept=" + dbStr + "FK_Dept ";

			ps.SQL = sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", deptNo);

		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (dt.Rows.size() == 0) {
			NodeStations nextStations = town.getHisNode().getNodeStations();
			if (nextStations.size() == 0) {
				throw new RuntimeException(
						"@节点没有岗位:" + town.getHisNode().getNodeID() + "  " + town.getHisNode().getName());
			}

			sql = "SELECT No FROM Port_Emp WHERE No IN ";
			sql += "(SELECT  FK_Emp  FROM " + bp.wf.Glo.getEmpStation()
					+ " WHERE FK_Station IN (SELECT FK_Station FROM WF_NodeStation WHERE FK_Node=" + dbStr
					+ "FK_Node ) )";
			sql += " AND No IN ";

			if (deptNo.equals("1")) {
				sql += "(SELECT No as FK_Emp FROM Port_Emp WHERE No!=" + dbStr + "FK_Emp ) ";
			} else {
				bp.port.Dept deptP = new bp.port.Dept(deptNo);
				sql += "(SELECT No as FK_Emp FROM Port_Emp WHERE No!=" + dbStr + "FK_Emp AND FK_Dept = '"
						+ deptP.getParentNo() + "')";
			}

			ps = new Paras();
			ps.SQL = sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Emp", empNo);
			dt = DBAccess.RunSQLReturnTable(ps);

			if (dt.Rows.size() == 0) {
				return null;
			}
			return dt;
		} else {
			return dt;
		}
	}

	/** 
	 获得指定部门下是否有该岗位的人员.
	 
	 @param deptNo 部门编号
	 @param empNo 人员编号
	 @return 
	 * @throws Exception 
	*/
	public final DataTable Func_GenerWorkerList_SpecDept(String deptNo, String empNo) throws Exception
	{
		String sql;

		Paras ps = new Paras();
		if (this.town.getHisNode().getIsExpSender() == true)
		{
			/* 不允许包含当前处理人. */
			sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node AND A.FK_Dept=" + dbStr + "FK_Dept AND A.FK_Emp!=" + dbStr + "FK_Emp";

			ps.SQL=sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", deptNo);
			ps.Add("FK_Emp", empNo);
		}
		else
		{
			sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B WHERE A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node AND A.FK_Dept=" + dbStr + "FK_Dept";

			ps.SQL=sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", deptNo);
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		return dt;
	}
	/** 
	 获得本部门的人员
	 
	 @param deptNo
	 @param emp1
	 @return 
	 * @throws Exception 
	*/
	public final DataTable Func_GenerWorkerList_SpecDept_SameLevel(String deptNo, String empNo) throws Exception
	{
		String sql;

		Paras ps = new Paras();
		if (this.town.getHisNode().getIsExpSender() == true)
		{
			/* 不允许包含当前处理人. */
			sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B, Port_Dept C WHERE A.FK_Dept=C.No AND A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node AND C.ParentNo=" + dbStr + "FK_Dept AND A.FK_Emp!=" + dbStr + "FK_Emp";

			ps.SQL=sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", deptNo);
			ps.Add("FK_Emp", empNo);
		}
		else
		{
			sql = "SELECT FK_Emp as No FROM Port_DeptEmpStation A, WF_NodeStation B, Port_Dept C  WHERE A.FK_Dept=C.No AND A.FK_Station=B.FK_Station AND B.FK_Node=" + dbStr + "FK_Node AND C.ParentNo=" + dbStr + "FK_Dept";
			ps.SQL=sql;
			ps.Add("FK_Node", town.getHisNode().getNodeID());
			ps.Add("FK_Dept", deptNo);
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		return dt;
	}
	/** 
	 执行找人
	 
	 @return 
	 * @throws Exception 
	*/
	public final DataTable DoIt(Flow fl, WorkNode currWn, WorkNode toWn) throws Exception
	{
		// 给变量赋值.
		this.fl = fl;
		this.currWn = currWn;
		this.town = toWn;
		this.WorkID=currWn.getWorkID();

		if (this.town.getHisNode().getIsGuestNode())
		{
			/*到达的节点是客户参与的节点. add by zhoupeng 2016.5.11*/
			DataTable mydt = new DataTable();
			mydt.Columns.Add("No", String.class);
			mydt.Columns.Add("Name", String.class);

			DataRow dr = mydt.NewRow();
			dr.setValue("No", "Guest");
			dr.setValue("Name", "外部用户");
			mydt.Rows.add(dr);
			return mydt;
		}


		//如果到达的节点是按照workflow的模式。
		if (toWn.getHisNode().getHisDeliveryWay() != DeliveryWay.ByCCFlowBPM)
		{
			DataTable re_dt = this.FindByWorkFlowModel();
			if (re_dt.Rows.size() == 1)
			{
				return re_dt; //如果只有一个人，就直接返回，就不处理了。
			}


				///根据配置追加接收人 by dgq 2015.5.18

			String paras = this.town.getHisNode().getDeliveryParas();
			if (paras.contains("@Spec"))
			{
				//如果返回null ,则创建表
				if (re_dt == null)
				{
					re_dt = new DataTable();
					re_dt.Columns.Add("No", String.class);
				}

				//获取配置规则
				String[] reWays = this.town.getHisNode().getDeliveryParas().split("[@]", -1);
				for (String reWay : reWays)
				{
					if (DataType.IsNullOrEmpty(reWay))
					{
						continue;
					}
					String[] specItems = reWay.split("[=]", -1);
					//配置规则错误
					if (specItems.length != 2)
					{
						continue;
					}
					//规则名称，SpecStations、SpecEmps
					String specName = specItems[0];
					//规则内容
					String specContent = specItems[1];
					switch (specName)
					{
						case "SpecStations": //按岗位
							String[] stations = specContent.split("[,]", -1);
							for (String station : stations)
							{
								if (DataType.IsNullOrEmpty(station))
								{
									continue;
								}

								//获取岗位下的人员
								DataTable dt_Emps = DBAccess.RunSQLReturnTable("SELECT FK_Emp FROM " + bp.wf.Glo.getEmpStation() + " WHERE FK_Station='" + station + "'");
								for (DataRow empRow : dt_Emps.Rows)
								{
									//排除为空编号
									if (empRow.getValue(0) == null || DataType.IsNullOrEmpty(empRow.getValue(0).toString()))
									{
										continue;
									}

									DataRow dr = re_dt.NewRow();
									dr.setValue(0, empRow.getValue(0));
									re_dt.Rows.add(dr);
								}
							}
							break;
						case "SpecEmps": //按人员编号
							String[] emps = specContent.split("[,]", -1);
							for (String emp : emps)
							{
								//排除为空编号
								if (DataType.IsNullOrEmpty(emp))
								{
									continue;
								}

								DataRow dr = re_dt.NewRow();
								dr.setValue(0, emp);
								re_dt.Rows.add(dr);
							}
							break;
					}
				}
			}

				///

			//本节点接收人不允许包含上一步发送人 。
			if (this.town.getHisNode().getIsExpSender() == true && re_dt.Rows.size() >= 2)
			{
				/*
				 * 排除了接受人分组的情况, 因为如果有了分组，就破坏了分组的结构了.
				 * 
				 */
				//复制表结构
				DataTable dt = re_dt.clone();
				for (DataRow row : re_dt.Rows)
				{
					//排除当前登录人
					if (row.getValue(0).toString().equals(WebUser.getNo()))
					{
						continue;
					}

					DataRow dr = dt.NewRow();
					dr.setValue(0, row.getValue(0));
					dt.Rows.add(dr);
				}
				return dt;
			}
			return re_dt;
		}

		// 规则集合.
		FindWorkerRoles ens = new FindWorkerRoles(town.getHisNode().getNodeID());
		for (FindWorkerRole en : ens.ToJavaList())
		{
			en.fl = this.fl;
			en.town = toWn;
			en.currWn = currWn;
			en.HisNode = currWn.getHisNode();
			en.WorkID=this.WorkID;

			DataTable dt = en.GenerWorkerOfDataTable();
			if (dt == null || dt.Rows.size() == 0)
			{
				continue;
			}

			//本节点接收人不允许包含上一步发送人
			if (this.town.getHisNode().getIsExpSender() == true)
			{
				DataTable re_dt = dt.clone();
				for (DataRow row : dt.Rows)
				{
					if (row.getValue(0).toString().equals(WebUser.getNo()))
					{
						continue;
					}
					DataRow dr = re_dt.NewRow();
					dr.setValue(0, row.getValue(0));
					re_dt.Rows.add(dr);
				}
				return re_dt;
			}
			return dt;
		}

		//没有找到人的情况，就返回空.
		return null;
	}


}