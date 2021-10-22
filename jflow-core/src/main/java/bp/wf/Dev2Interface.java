package bp.wf;

import bp.da.*;
import bp.difference.SystemConfig;
import bp.en.*;
import bp.port.*;
import bp.sys.*;
import bp.tools.AesEncodeUtil;
import bp.tools.DateUtils;
import bp.tools.FtpUtil;
import bp.tools.SftpUtil;
import bp.web.*;
import bp.wf.data.*;
import bp.wf.port.WFEmp;
import bp.wf.port.WFEmpAttr;
import bp.wf.port.admin2.Org;
import bp.wf.template.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.*;

/**
 * 此接口为程序员二次开发使用,在阅读代码前请注意如下事项. 1, CCFlow的对外的接口都是以静态方法来实现的. 2, 以 DB_
 * 开头的是需要返回结果集合的接口. 3, 以 Flow_ 是流程接口. 4, 以 Node_ 是节点接口. 5, 以 Port_ 是组织架构接口. 6, 以
 * DTS_ 是调度. data tranr system. 8, 以 WorkOpt_ 用工作处理器相关的接口。
 */
public class Dev2Interface {

	/// 写入消息表.
	/**
	 * 写入消息 用途可以处理提醒.
	 * 
	 * @param sendToUserNo
	 *            发送给的操作员ID
	 * @param sendDT
	 *            发送时间，如果null 则表示立刻发送。
	 * @param title
	 *            标题
	 * @param doc
	 *            内容
	 * @param msgFlag
	 *            消息标记
	 * @return 写入成功或者失败.
	 * @throws Exception
	 */
	public static boolean WriteToSMS(String sendToUserNo, String sendDT, String title, String doc, String msgFlag,
			long workid) throws Exception {
		SMS.SendMsg(sendToUserNo, title, doc, msgFlag, "Info", "", workid);
		return true;
	}

	///
	   /// <summary>
    /// 按照token登录 2021.07.01 采用新方式.
    /// @hongyan 新增的方法.
    /// </summary>
    /// <param name="token"></param>
    public static void Port_LoginByToken(String token) throws Exception
    {
        if (DataType.IsNullOrEmpty(token))
            throw new Exception("err@SID不能为空.");

        token = token.trim();

        if (DataType.IsNullOrEmpty(token) == true)
            throw new Exception("err@非法的Token.");

        WFEmp emp = new WFEmp();
        int i = emp.Retrieve(WFEmpAttr.Token, token);
        if (i == 0)
            throw new Exception("err@非法或者失效的token:" + token);

        //执行登录.
        Dev2Interface.Port_Login(emp.getNo());
    }
	/**
	 * 待办工作数量
	 * 
	 * @throws Exception
	 */
	public static int getTodolistEmpWorks() throws Exception {
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();

		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE FK_Emp=" + dbstr
						+ "FK_Emp AND TaskSta!=1 ";
			} else {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp ";
			}

			ps.Add("FK_Emp", WebUser.getNo());

			// bp.da.Log.DebugWriteInfo(ps.SQL);
			return DBAccess.RunSQLReturnValInt(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr
						+ "FK_Emp AND TaskSta!=1  ";
			} else {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp ";
			}

			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case SpecFlows:
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE FK_Emp=" + dbstr
						+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows() + " AND TaskSta!=0   ";
			} else {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr
						+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows();
			}

			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case None:
			/* 不是授权状态 */
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr
						+ "FK_Emp AND TaskSta!=1 ";
			} else {
				ps.SQL = "SELECT count(WorkID) as Num FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp ";
			}

			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnValInt(ps);
		default:
			throw new RuntimeException("no such way...");
		}
		return DBAccess.RunSQLReturnValInt(ps);
	}

	/**
	 * 抄送数量
	 * 
	 * @throws Exception
	 */
	public static int getTodolistCCWorks() throws Exception {
		Paras ps = new Paras();
		ps.SQL = "SELECT count(MyPK) as Num FROM WF_CCList WHERE CCTo=" + SystemConfig.getAppCenterDBVarStr()
				+ "FK_Emp AND Sta=0";
		ps.Add("FK_Emp", WebUser.getNo());
		return DBAccess.RunSQLReturnValInt(ps, 0);
	}

	/**
	 * 返回挂起流程数量
	 * 
	 * @throws Exception
	 */
	public static int getTodolistHungUpNum() throws Exception {
		String sql = "SELECT  COUNT(WorkID) AS Num from WF_GenerWorkFlow where WFState=4 and  WorkID in (SELECT distinct WorkID FROM WF_HungUp WHERE Rec='"
				+ WebUser.getNo() + "')";
		return DBAccess.RunSQLReturnValInt(sql);
	}

	/**
	 * 在途的工作数量
	 * 
	 * @throws Exception
	 */
	public static int getTodolistRuning() throws Exception {
		String sql;
		int state = WFState.Runing.getValue();
		if (WebUser.getIsAuthorize()) {
			/* 如果是授权状态. */
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
			sql = "SELECT count( distinct A.WorkID ) as Num FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE (A.TodoEmps  not like '%" + WebUser.getNo() + ",%' OR A.TodoEmps  not like '%" + WebUser.getNo() + ";%') AND A.WorkID=B.WorkID AND B.FK_Emp='"
					+ WebUser.getNo() + "' AND B.IsEnable=1 AND (B.IsPass=1 OR B.IsPass<-1) AND A.FK_Flow IN "
					+ emp.getAuthorFlows();
			return DBAccess.RunSQLReturnValInt(sql);
		} else {
			Paras ps = new Paras();
			ps.SQL = "SELECT count( distinct A.WorkID ) as Num FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  (A.TodoEmps  not like '%" + WebUser.getNo() + ",%' OR A.TodoEmps  not like '%" + WebUser.getNo() + ";%') AND  A.WorkID=B.WorkID AND B.FK_Emp="
					+ SystemConfig.getAppCenterDBVarStr() + "FK_Emp AND B.IsEnable=1 AND (B.IsPass=1 OR B.IsPass<-1) ";
			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnValInt(ps);
		}
	}

	/**
	 * 获取草稿箱流程数量
	 *
	 * @throws Exception
	 */
	public static int getTodolistDraft() throws Exception {
		/* 获取数据. */
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		bp.da.Paras ps = new bp.da.Paras();
		ps.SQL = "SELECT count(a.WorkID ) as Num FROM WF_GenerWorkFlow A WHERE WFState=1 AND Starter=" + dbStr
				+ "Starter";
		ps.Add(GenerWorkFlowAttr.Starter, WebUser.getNo());
		return DBAccess.RunSQLReturnValInt(ps);
	}

	/**
	 * 会签的数量
	 *
	 * @throws Exception
	 */
	public static int getTodolistHuiQian() throws Exception {
		/* 获取数据. */
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		bp.da.Paras ps = new bp.da.Paras();
		String sql = "SELECT count(*)";
		sql += " FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B ";
		sql += " WHERE A.WorkID=B.WorkID and a.FK_Node=b.FK_Node ";
		sql += " AND (B.IsPass=90 OR A.AtPara LIKE '%HuiQianZhuChiRen=" + WebUser.getNo() + "%') ";
		sql += " AND B.FK_Emp=" + dbStr + "FK_Emp";
		ps.SQL = sql;
		ps.Add(GenerWorkerListAttr.FK_Emp, WebUser.getNo());
		return DBAccess.RunSQLReturnValInt(ps);
	}

	/**
	 * 获取已经完成流程数量
	 *
	 * @return
	 * @throws Exception
	 */
	public static int getTodolistComplete() throws Exception {

		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL = "SELECT count(WorkID) Num FROM WF_GenerWorkFlow WHERE (Emps LIKE '%@" + WebUser.getNo()
				+ "@%' OR Emps LIKE '%@" + WebUser.getNo() + ",%') AND WFState=" + WFState.Complete.getValue();
		return DBAccess.RunSQLReturnValInt(ps, 0);

	}

	/**
	 * 共享任务个数
	 *
	 * @throws Exception
	 */
	public static int getTodolistSharing() throws Exception {
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String wfSql = "  (WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
				+ " OR WFState=" + WFState.Shift.getValue() + " OR WFState=" + WFState.ReturnSta.getValue()
				+ ") AND TaskSta=" + TaskSta.Sharing.getValue();
		String sql;
		String realSql = null;
		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			ps.SQL = "SELECT COUNT(WorkID) FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp ";
			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnValInt(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			ps.SQL = "SELECT COUNT(WorkID) FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
					+ "FK_Emp AND TaskSta=0";
			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case SpecFlows:
			ps.SQL = "SELECT COUNT(WorkID) FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
					+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows() + " ";
			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case None:
			// WebUser.getIsAuthorize() = false;
			/* 不是授权状态 */
			ps.SQL = "SELECT COUNT(WorkID) FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp ";
			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnValInt(ps);
		// throw new Exception("对方(" + WebUser.getNo() + ")已经取消了授权.");
		default:
			throw new RuntimeException("no such way...");
		}
		return DBAccess.RunSQLReturnValInt(ps);
	}

	/**
	 * 申请下来的工作个数
	 *
	 * @throws Exception
	 */
	public static int getTodolistApply() throws Exception {
		if (bp.wf.Glo.getIsEnableTaskPool() == false) {
			return 0;
		}


		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String wfSql = "  (WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
				+ " OR WFState=" + WFState.Shift.getValue() + " OR WFState=" + WFState.ReturnSta.getValue()
				+ ") AND TaskSta=" + TaskSta.Takeback.getValue();		String sql;
		String realSql;
		//if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			ps.SQL = "SELECT COUNT(WorkID) FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp";

			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnValInt(ps);
		//}


	}

	/// 自动执行
	/**
	 * 处理延期的任务.根据节点属性的设置
	 *
	 * @return 返回处理的消息
	 */
	public static String DTS_DealDeferredWork() throws Exception {
		bp.wf.dts.DTS_DealDeferredWork en = new bp.wf.dts.DTS_DealDeferredWork();
		en.Do();

		return "执行成功..";
	}

	/**
	 * 自动执行开始节点数据 说明:根据自动执行的流程设置，自动启动发起的流程。
	 * 比如：您根据ccflow的自动启动流程的设置，自动启动该流程，不使用ccflow的提供的服务程序，您需要按如下步骤去做。 1,
	 * 写一个自动调度的程序。 2，根据自己的时间需要调用这个接口。
	 *
	 * @param fl
	 *            流程实体,您可以 new Flow(flowNo); 来传入.
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static void DTS_AutoStarterFlow(Flow fl) throws NumberFormatException, Exception {

		/// 读取数据.
		MapExt me = new MapExt();
		int i = me.Retrieve(MapExtAttr.FK_MapData, "ND" + Integer.parseInt(fl.getNo()) + "01", MapExtAttr.ExtType,
				"PageLoadFull");
		if (i == 0) {
			bp.da.Log.DefaultLogWriteLineError("没有为流程(" + fl.getName() + ")的开始节点设置发起数据,请参考说明书解决.");
			return;
		}

		// 获取从表数据.
		DataSet ds = new DataSet();
		String[] dtlSQLs = me.getTag1().split("[*]", -1);
		for (String sql : dtlSQLs) {
			if (DataType.IsNullOrEmpty(sql)) {
				continue;
			}

			String[] tempStrs = sql.split("[=]", -1);
			String dtlName = tempStrs[0];
			DataTable dtlTable = DBAccess.RunSQLReturnTable(sql.replace(dtlName + "=", ""));
			dtlTable.TableName = dtlName;
			ds.Tables.add(dtlTable);
		}

		/// 读取数据.

		/// 检查数据源是否正确.
		String errMsg = "";
		// 获取主表数据.
		DataTable dtMain = DBAccess.RunSQLReturnTable(me.getTag());
		if (dtMain.Columns.contains("Starter") == false) {
			errMsg += "@配值的主表中没有Starter列.";
		}

		if (dtMain.Columns.contains("MainPK") == false) {
			errMsg += "@配值的主表中没有MainPK列.";
		}

		if (errMsg.length() > 2) {
			bp.da.Log.DefaultLogWriteLineError("流程(" + fl.getName() + ")的开始节点设置发起数据,不完整." + errMsg);
			return;
		}

		/// 检查数据源是否正确.

		/// 处理流程发起.

		String nodeTable = "ND" + Integer.parseInt(fl.getNo()) + "01";
		MapData md = new MapData(nodeTable);

		for (DataRow dr : dtMain.Rows) {
			String mainPK = dr.getValue("MainPK").toString();
			String sql = "SELECT OID FROM " + md.getPTable() + " WHERE MainPK='" + mainPK + "'";
			if (DBAccess.RunSQLReturnTable(sql).Rows.size() != 0) {
				continue; // 说明已经调度过了
			}

			String starter = dr.getValue("Starter").toString();
			if (!starter.equals(WebUser.getNo())) {
				WebUser.Exit();
				bp.port.Emp emp = new bp.port.Emp();
				emp.setNo(starter);
				if (emp.RetrieveFromDBSources() == 0) {
					bp.da.Log.DefaultLogWriteLineInfo(
							"@数据驱动方式发起流程(" + fl.getName() + ")设置的发起人员:" + emp.getNo() + "不存在。");
					continue;
				}

				WebUser.SignInOfGener(emp);
			}

			/// 给值.
			Work wk = fl.NewWork();
			for (DataColumn dc : dtMain.Columns) {
				wk.SetValByKey(dc.ColumnName, dr.getValue(dc.ColumnName).toString());
			}

			if (ds.Tables.size() != 0) {
				String refPK = dr.getValue("MainPK").toString();
				MapDtls dtls = wk.getHisNode().getMapData().getMapDtls(); // new
																			// MapDtls(nodeTable);
				for (MapDtl dtl : dtls.ToJavaList()) {
					for (DataTable dt : ds.Tables) {
						if (!dt.TableName.equals(dtl.getNo())) {
							continue;
						}

						// 删除原来的数据。
						GEDtl dtlEn = dtl.getHisGEDtl();
						dtlEn.Delete(GEDtlAttr.RefPK, String.valueOf(wk.getOID()));

						// 执行数据插入。
						for (DataRow drDtl : dt.Rows) {
							if (!drDtl.getValue("RefMainPK").toString().equals(refPK)) {
								continue;
							}

							dtlEn = dtl.getHisGEDtl();

							for (DataColumn dc : dt.Columns) {
								dtlEn.SetValByKey(dc.ColumnName, drDtl.getValue(dc.ColumnName).toString());
							}

							dtlEn.setRefPK(String.valueOf(wk.getOID()));
							dtlEn.Insert();
						}
					}
				}
			}

			/// 给值.

			// 处理发送信息.
			Node nd = fl.getHisStartNode();
			try {
				WorkNode wn = new WorkNode(wk, nd);
				String msg = wn.NodeSend().ToMsgOfHtml();
				// bp.da.Log.DefaultLogWriteLineInfo(msg);
			} catch (RuntimeException ex) {
				bp.da.Log.DefaultLogWriteLineWarning(ex.getMessage());
			}
		}

		/// 处理流程发起.

	}

	///

	/// 数据集合接口(如果您想获取一个结果集合的接口，都是以DB_开头的.)
	/**
	 * 获取能发起流程的人员
	 *
	 * @param fk_flow
	 *            流程编号
	 * @return
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static String GetFlowStarters(String fk_flow) throws NumberFormatException, Exception {
		bp.wf.Node nd = new Node(Integer.parseInt(fk_flow + "01"));
		String sql = "";
		switch (nd.getHisDeliveryWay()) {
		case ByBindEmp: // 按人员
			sql = "SELECT * FROM Port_Emp WHERE No IN (SELECT FK_Emp FROM WF_NodeEmp WHERE FK_Node=" + nd.getNodeID()
					+ ")";
			break;
		case ByDept: // 按部门
			sql = "SELECT * FROM Port_Emp WHERE FK_Dept IN (SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node="
					+ nd.getNodeID() + ")";
			break;
		case ByStation: // 按岗位
		case FindSpecDeptEmpsInStationlist: // 按岗位
			sql = "SELECT * FROM Port_Emp WHERE No IN (SELECT FK_Emp FROM " + bp.wf.Glo.getEmpStation()
					+ " WHERE FK_Station IN ( SELECT FK_Station from WF_nodeStation where FK_Node=" + nd.getNodeID()
					+ ")) ";
			break;
		default:
			throw new RuntimeException("@开始节点的人员访问规则错误,不允许在开始节点设置此访问类型:" + nd.getHisDeliveryWay());
		}
		return sql;
	}

	public static String GetFlowStarters(String fk_flow, String fk_dept) throws NumberFormatException, Exception {
		bp.wf.Node nd = new Node(Integer.parseInt(fk_flow + "01"));
		String sql = "";
		switch (nd.getHisDeliveryWay()) {
		case ByBindEmp: // 按人员
			sql = "SELECT * FROM Port_Emp WHERE No IN (SELECT FK_Emp FROM WF_NodeEmp WHERE FK_Node=" + nd.getNodeID()
					+ ") and fk_dept='" + fk_dept + "'";
			break;
		case ByDept: // 按部门
			sql = "SELECT * FROM Port_Emp WHERE FK_Dept IN (SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node="
					+ nd.getNodeID() + ") and fk_dept='" + fk_dept + "' ";
			break;
		case ByStation: // 按岗位
			sql = "SELECT * FROM Port_Emp WHERE No IN (SELECT FK_Emp FROM " + bp.wf.Glo.getEmpStation()
					+ " WHERE FK_Station IN ( SELECT FK_Station from WF_nodeStation where FK_Node=" + nd.getNodeID()
					+ ")) and fk_dept='" + fk_dept + "' ";
			break;
		default:
			throw new RuntimeException("@开始节点的人员访问规则错误,不允许在开始节点设置此访问类型:" + nd.getHisDeliveryWay());
		}
		return sql;
	}

	/// 与子流程相关.
	/**
	 * 获取流程事例的运行轨迹数据. 说明：使用这些数据可以生成流程的操作日志.
	 *
	 * @param workid
	 *            工作ID
	 * @return GenerWorkFlows
	 * @throws Exception
	 */
	public static GenerWorkFlows DB_SubFlows(long workid) throws Exception {
		GenerWorkFlows gwf = new GenerWorkFlows();
		gwf.Retrieve(GenerWorkFlowAttr.PWorkID, workid);
		return gwf;
	}

	/// 获取流程事例的轨迹图

	/// 获取流程事例的轨迹图

	public static DataTable DB_GenerTrackTable(String fk_flow, long workid, long fid) throws Exception {

		/// 获取track数据.
		String sqlOfWhere2 = "";
		String sqlOfWhere1 = "";
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		if (fid == 0) {
			sqlOfWhere1 = " WHERE (FID=" + dbStr + "WorkID11 OR WorkID=" + dbStr + "WorkID12 )  ";
			ps.Add("WorkID11", workid);
			ps.Add("WorkID12", workid);
		} else {
			sqlOfWhere1 = " WHERE (FID=" + dbStr + "FID11 OR WorkID=" + dbStr + "FID12 ) ";
			ps.Add("FID11", fid);
			ps.Add("FID12", fid);
		}

		String sql = "";
		sql = "SELECT MyPK,ActionType,ActionTypeText,FID,WorkID,NDFrom,NDFromT,NDTo,NDToT,EmpFrom,EmpFromT,EmpTo,EmpToT,RDT,WorkTimeSpan,Msg,NodeData,Exer,Tag FROM ND"
				+ Integer.parseInt(fk_flow) + "Track " + sqlOfWhere1 + " ORDER BY RDT ASC ";
		ps.SQL = sql;
		DataTable dt = null;

		try {
			dt = DBAccess.RunSQLReturnTable(ps);
		} catch (java.lang.Exception e) {
			// 处理track表.
			Track.CreateOrRepairTrackTable(fk_flow);
			dt = DBAccess.RunSQLReturnTable(ps);
		}

		// 把列名转化成区分大小写.
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("MYPK").ColumnName = "MyPK";
			dt.Columns.get("ACTIONTYPE").ColumnName = "ActionType";
			dt.Columns.get("ACTIONTYPETEXT").ColumnName = "ActionTypeText";
			dt.Columns.get("FID").ColumnName = "FID";
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("NDFROM").ColumnName = "NDFrom";
			dt.Columns.get("NDFROMT").ColumnName = "NDFromT";
			dt.Columns.get("NDTO").ColumnName = "NDTo";
			dt.Columns.get("NDTOT").ColumnName = "NDToT";
			dt.Columns.get("EMPFROM").ColumnName = "EmpFrom";
			dt.Columns.get("EMPFROMT").ColumnName = "EmpFromT";
			dt.Columns.get("EMPTO").ColumnName = "EmpTo";
			dt.Columns.get("EMPTOT").ColumnName = "EmpToT";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("WORKTIMESPAN").ColumnName = "WorkTimeSpan";
			dt.Columns.get("MSG").ColumnName = "Msg";
			dt.Columns.get("NODEDATA").ColumnName = "NodeData";
			dt.Columns.get("EXER").ColumnName = "Exer";
			dt.Columns.get("TAG").ColumnName = "Tag";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("mypk").ColumnName = "MyPK";
			dt.Columns.get("actiontype").ColumnName = "ActionType";
			dt.Columns.get("actiontypetext").ColumnName = "ActionTypeText";
			dt.Columns.get("fid").ColumnName = "FID";
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("ndfrom").ColumnName = "NDFrom";
			dt.Columns.get("ndfromt").ColumnName = "NDFromT";
			dt.Columns.get("ndto").ColumnName = "NDTo";
			dt.Columns.get("ndtot").ColumnName = "NDToT";
			dt.Columns.get("empfrom").ColumnName = "EmpFrom";
			dt.Columns.get("empfromt").ColumnName = "EmpFromT";
			dt.Columns.get("empto").ColumnName = "EmpTo";
			dt.Columns.get("emptot").ColumnName = "EmpToT";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("worktimespan").ColumnName = "WorkTimeSpan";
			dt.Columns.get("msg").ColumnName = "Msg";
			dt.Columns.get("nodedata").ColumnName = "NodeData";
			dt.Columns.get("exer").ColumnName = "Exer";
			dt.Columns.get("tag").ColumnName = "Tag";
		}

		// 把track加入里面去.
		dt.TableName = "Track";
		return dt;

		/// 获取track数据.
	}

	/**
	 * 获取一个流程
	 *
	 * @param fk_flow
	 *            流程编号
	 * @param userNo
	 *            操作员编号
	 * @return
	 */
	public static DataTable DB_GenerNDxxxRpt(String fk_flow, String userNo) {
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "SELECT * FROM ND" + Integer.parseInt(fk_flow) + "Rpt WHERE FlowStarter=" + dbstr
				+ "FlowStarter  ORDER BY RDT";
		ps.Add(GERptAttr.FlowStarter, userNo);
		return DBAccess.RunSQLReturnTable(ps);
	}

	/// 获取流程事例的轨迹图

	/// 获取能够发送或者抄送人员的列表.
	/**
	 * 获取可以执行指定节点人的列表
	 *
	 * @param fk_node
	 *            节点编号
	 * @param workid
	 *            工作ID
	 * @return
	 */
	public static DataSet DB_CanExecSpecNodeEmps(int fk_node, long workid) {
		DataSet ds = new DataSet();
		Paras ps = new Paras();
		ps.SQL = "SELECT No,Name,FK_Dept FROM Port_Emp ";
		DataTable dtEmp = DBAccess.RunSQLReturnTable(ps);
		dtEmp.TableName = "Emps";
		ds.Tables.add(dtEmp);

		ps = new Paras();
		ps.SQL = "SELECT No,Name FROM Port_Dept ";
		DataTable dtDept = DBAccess.RunSQLReturnTable(ps);
		dtDept.TableName = "Depts";
		ds.Tables.add(dtDept);
		return ds;
	}

	/**
	 * 获得可以抄送的人员列表
	 *
	 * @param fk_node
	 *            节点编号
	 * @param workid
	 *            工作ID
	 * @return
	 */
	public static DataSet DB_CanCCSpecNodeEmps(int fk_node, long workid) {
		DataSet ds = new DataSet();
		Paras ps = new Paras();
		ps.SQL = "SELECT No,Name,FK_Dept FROM Port_Emp ";
		DataTable dtEmp = DBAccess.RunSQLReturnTable(ps);
		dtEmp.TableName = "Emps";
		ds.Tables.add(dtEmp);

		ps = new Paras();
		ps.SQL = "SELECT No,Name FROM Port_Dept ";
		DataTable dtDept = DBAccess.RunSQLReturnTable(ps);
		dtDept.TableName = "Depts";
		ds.Tables.add(dtDept);

		return ds;
	}

	/// 获取能够发送或者抄送人员的列表.

	/// 获取操送列表
	/**
	 * 获取指定人员的抄送列表 说明:可以根据这个列表生成指定用户的抄送数据.
	 *
	 * @param domain
	 *            域.
	 * @return 返回该人员的所有抄送列表,结构同表WF_CCList.
	 */
	public static DataTable DB_CCList(String domain) throws Exception {
		Paras ps = new Paras();
		if (DataType.IsNullOrEmpty(domain) == true) {
			ps.SQL = "SELECT a.MyPK,A.Title,A.FK_Flow,A.FlowName,A.WorkID,A.Doc,A.Rec,A.RDT,A.FID,B.FK_Node,B.NodeName,B.WFSta,A.Sta FROM WF_CCList A, WF_GenerWorkFlow B WHERE A.CCTo="
					+ SystemConfig.getAppCenterDBVarStr() + "CCTo AND B.WorkID=A.WorkID ";
			ps.Add("CCTo", WebUser.getNo());
		} else {
			ps.SQL = "SELECT a.MyPK,A.Title,A.FK_Flow,A.FlowName,A.WorkID,A.Doc,A.Rec,A.RDT,A.FID,B.FK_Node,B.NodeName,B.WFSta,A.Sta FROM WF_CCList A, WF_GenerWorkFlow B WHERE A.CCTo="
					+ SystemConfig.getAppCenterDBVarStr() + "CCTo AND B.WorkID=A.WorkID AND B.Domain="
					+ SystemConfig.getAppCenterDBVarStr() + "Domain ";
			ps.Add("CCTo", WebUser.getNo());
			ps.Add("Domain", domain);
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("MYPK").ColumnName = "MyPK";
			dt.Columns.get("TITLE").ColumnName = "Title";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("DOC").ColumnName = "DOC";
			dt.Columns.get("REC").ColumnName = "REC";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("FID").ColumnName = "FID";
			dt.Columns.get("WFSTA").ColumnName = "WFSta";
			dt.Columns.get("STA").ColumnName = "Sta"; // @yuanlina
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("mypk").ColumnName = "MyPK";
			dt.Columns.get("title").ColumnName = "Title";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("nodename").ColumnName = "NodeName";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("doc").ColumnName = "DOC";
			dt.Columns.get("rec").ColumnName = "REC";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("fid").ColumnName = "FID";
			dt.Columns.get("wfsta").ColumnName = "WFSta";
			dt.Columns.get("sta").ColumnName = "Sta";
		}
		return dt;
	}

	public static DataTable DB_CCList(CCSta sta) throws Exception {
		return DB_CCList(sta, null);
	}

	public static DataTable DB_CCList(CCSta sta, String domain) throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();

		Paras ps = new Paras();
		if (domain == null) {
			ps.SQL = "SELECT * FROM WF_CCList WHERE Sta=" + dbStr + "Sta AND CCTo=" + dbStr + "CCTo ";
			ps.Add("Sta", sta.getValue());
			ps.Add("CCTo", WebUser.getNo());
		} else {
			ps.SQL = "SELECT * FROM WF_CCList WHERE Sta=" + dbStr + "Sta AND CCTo=" + dbStr + "CCTo AND Domain=" + dbStr
					+ "Domain";
			ps.Add("Sta", sta.getValue());
			ps.Add("CCTo", WebUser.getNo());
			ps.Add("Domain", domain);
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("MYPK").ColumnName = "MyPK";
			dt.Columns.get("TITLE").ColumnName = "Title";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("DOC").ColumnName = "DOC";
			dt.Columns.get("REC").ColumnName = "REC";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("FID").ColumnName = "FID";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("mypk").ColumnName = "MyPK";
			dt.Columns.get("title").ColumnName = "Title";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("nodename").ColumnName = "NodeName";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("doc").ColumnName = "DOC";
			dt.Columns.get("rec").ColumnName = "REC";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("fid").ColumnName = "FID";
		}
		return dt;
	}

	/**
	 * 获取指定人员的抄送列表(未读)
	 *
	 * @param FK_Emp
	 *            人员编号,如果是null,则返回所有的.
	 * @return 返回该人员的未读的抄送列表
	 * @throws Exception
	 */
	public static DataTable DB_CCList_UnRead(String FK_Emp) throws Exception {
		return DB_CCList(CCSta.UnRead);
	}

	/**
	 * 获取指定人员的抄送列表(已读)
	 *
	 * @param FK_Emp
	 *            人员编号
	 * @return 返回该人员的已读的抄送列表
	 * @throws Exception
	 */

	public static DataTable DB_CCList_Read() throws Exception {
		return DB_CCList_Read(null);
	}

	public static DataTable DB_CCList_Read(String domain) throws Exception {
		return DB_CCList(CCSta.Read, domain);
	}

	/**
	 * 获取指定人员的抄送列表(已删除)
	 *
	 * @param FK_Emp
	 *            人员编号
	 * @return 返回该人员的已删除的抄送列表
	 * @throws Exception
	 */
	public static DataTable DB_CCList_Delete(String domain) throws Exception {
		return DB_CCList(CCSta.Del, domain);
	}

	/**
	 * 获取指定人员的抄送列表(已回复)
	 *
	 * @param FK_Emp
	 *            人员编号
	 * @return 返回该人员的已删除的抄送列表
	 * @throws Exception
	 */
	public static DataTable DB_CCList_CheckOver(String domain) throws Exception {
		return DB_CCList(CCSta.CheckOver, domain);
	}

	///

	/// 获取当前操作员可以发起的流程集合
	/**
	 * 获取指定人员能够发起流程的集合. 说明:利用此接口可以生成用户的发起的流程列表.
	 *
	 * @param userNo
	 *            操作员编号
	 * @return BP.WF.Flows 可发起的流程对象集合,如何使用该方法形成发起工作列表,请参考:\WF\UC\Start.ascx
	 * @throws Exception
	 */
	public static Flows DB_GenerCanStartFlowsOfEntities(String userNo) throws Exception {

		String sql = "";
		// 采用新算法.
		sql = "SELECT FK_Flow FROM V_FlowStarterBPM WHERE FK_Emp='" + userNo + "'";

		Flows fls = new Flows();
		bp.en.QueryObject qo = new bp.en.QueryObject(fls);
		qo.AddWhereInSQL("No", sql);
		qo.addAnd();
		qo.AddWhere(FlowAttr.IsCanStart, true);
		if (WebUser.getIsAuthorize()) {
			/* 如果是授权状态 */
			qo.addAnd();
			bp.wf.port.WFEmp wfEmp = new bp.wf.port.WFEmp(userNo);
			qo.AddWhereIn("No", wfEmp.getAuthorFlows());
		}
		qo.addOrderBy("FK_FlowSort", FlowAttr.Idx);
		qo.DoQuery();
		return fls;

	}

	/**
	 * 获得指定人的流程发起列表
	 *
	 * @param userNo
	 *            发起人编号
	 * @return
	 * @throws Exception
	 */

	public static DataTable DB_StarFlows(String userNo) throws Exception {
		return DB_StarFlows(userNo, null);
	}

	public static DataTable DB_StarFlows(String userNo, String domain) throws Exception {
		DataTable dt = DB_GenerCanStartFlowsOfDataTable(userNo);
		return dt;
	}

	public static DataTable DB_GenerCanStartFlowsOfDataTable(String userNo) throws Exception {
		String sql = "SELECT A.No,A.Name,a.IsBatchStart,a.FK_FlowSort,C.Name AS FK_FlowSortText,C.Domain,A.IsStartInMobile, A.Idx";
		sql += " FROM WF_Flow A, V_FlowStarterBPM B, WF_FlowSort C  ";
		sql += " WHERE A.No=B.FK_Flow AND A.IsCanStart=1 AND A.FK_FlowSort=C.No  AND B.FK_Emp='" + WebUser.getNo()
				+ "' ";

		if (Glo.getCCBPMRunModel() == CCBPMRunModel.GroupInc) {
			sql += " AND ( B.OrgNo='" + WebUser.getOrgNo() + "' ) ";
		}

		sql += " ORDER BY C.Idx, A.Idx";

		DataTable dt = DBAccess.RunSQLReturnTable(sql);

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("NO").ColumnName = "No";
			dt.Columns.get("NAME").ColumnName = "Name";
			dt.Columns.get("ISBATCHSTART").ColumnName = "IsBatchStart";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";
			dt.Columns.get("FK_FLOWSORTTEXT").ColumnName = "FK_FlowSortText";
			dt.Columns.get("ISSTARTINMOBILE").ColumnName = "IsStartInMobile";
			dt.Columns.get("IDX").ColumnName = "Idx";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("no").ColumnName = "No";
			dt.Columns.get("name").ColumnName = "Name";
			dt.Columns.get("isbatchstart").ColumnName = "IsBatchStart";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";
			dt.Columns.get("fk_flowsorttext").ColumnName = "FK_FlowSortText";
			dt.Columns.get("isstartinmobile").ColumnName = "IsStartInMobile";
			dt.Columns.get("idx").ColumnName = "Idx";
		}
		return dt;
	}

	public static DataTable DB_GenerCanStartFlowsTree(String userNo) throws Exception {
		// 发起.
		DataTable table = DB_GenerCanStartFlowsOfDataTable(userNo);
		table.Columns.Add("ParentNo");
		table.Columns.Add("ICON");
		String flowSort = String.format("select No,Name,ParentNo from WF_FlowSort");

		DataTable sortTable = DBAccess.RunSQLReturnTable(flowSort);
		for (DataRow row : sortTable.Rows) {
			DataRow newRow = table.NewRow();
			newRow.setValue("No", row.getValue("No"));
			newRow.setValue("Name", row.getValue("Name"));
			newRow.setValue("ParentNo", row.getValue("ParentNo"));
			newRow.setValue("ICON", "icon-tree_folder");
			table.Rows.add(newRow);
		}

		for (DataRow row : table.Rows) {
			if (DataType.IsNullOrEmpty(row.getValue("ParentNo").toString())) {
				row.setValue("ParentNo", row.getValue("FK_FlowSort"));
			}
			if (DataType.IsNullOrEmpty(row.getValue("ICON").toString())) {
				row.setValue("ICON", "icon-4");
			}
		}
		return table;
	}

	/**
	 * 获取(同表单)合流点上的子线程 说明:如果您要想在合流点看到所有的子线程运行的状态.
	 *
	 * @param nodeIDOfHL
	 *            合流点ID
	 * @param workid
	 *            工作ID
	 * @return 与表WF_GenerWorkerList结构类同的datatable.
	 * @throws Exception
	 */
	public static DataTable DB_GenerHLSubFlowDtl_TB(int nodeIDOfHL, long workid) throws Exception {
		Node nd = new Node(nodeIDOfHL);
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.Retrieve();

		GenerWorkerLists wls = new GenerWorkerLists();
		QueryObject qo = new QueryObject(wls);
		qo.AddWhere(GenerWorkerListAttr.FID, wk.getOID());
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.IsEnable, 1);
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.FK_Node, nd.getFromNodes().get(0).GetValByKey(NodeAttr.NodeID));

		DataTable dt = qo.DoQueryToTable();
		if (dt.Rows.size() == 1) {
			qo.clear();
			qo.AddWhere(GenerWorkerListAttr.FID, wk.getOID());
			qo.addAnd();
			qo.AddWhere(GenerWorkerListAttr.IsEnable, 1);
			return qo.DoQueryToTable();
		}
		return dt;
	}

	/**
	 * 获取(异表单)合流点上的子线程
	 *
	 * @param nodeIDOfHL
	 *            合流点ID
	 * @param workid
	 *            工作ID
	 * @return 与表WF_GenerWorkerList结构类同的datatable.
	 * @throws Exception
	 */
	public static DataTable DB_GenerHLSubFlowDtl_YB(int nodeIDOfHL, long workid) throws Exception {
		Node nd = new Node(nodeIDOfHL);
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.Retrieve();

		GenerWorkerLists wls = new GenerWorkerLists();
		QueryObject qo = new QueryObject(wls);
		qo.AddWhere(GenerWorkerListAttr.FID, wk.getOID());
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.IsEnable, 1);
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.IsPass, 0);
		return qo.DoQueryToTable();
	}

	/// 获取当前操作员可以发起的流程集合

	/// 流程草稿
	/**
	 * 获取当前操作员的指定流程的流程草稿数据
	 *
	 * @param fk_flow
	 *            流程编号
	 * @return 返回草稿数据集合,列信息. OID=工作ID,Title=标题,RDT=记录日期,FK_Flow=流程编号,FID=流程ID,
	 *         FK_Node=节点ID
	 * @throws Exception
	 */

	public static DataTable DB_GenerDraftDataTable(String flowNo) throws Exception {
		return DB_GenerDraftDataTable(flowNo, null);
	}

	public static DataTable DB_GenerDraftDataTable() throws Exception {
		return DB_GenerDraftDataTable(null, null);
	}

	public static DataTable DB_GenerDraftDataTable(String flowNo, String domain) throws Exception {
		/* 获取数据. */
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		bp.da.Paras ps = new bp.da.Paras();
		if (DataType.IsNullOrEmpty(domain) == true) {
			if (flowNo == null) {
				ps.SQL = "SELECT WorkID,Title,FK_Flow,FlowName,RDT,FlowNote,AtPara FROM WF_GenerWorkFlow A WHERE WFState=1 AND Starter="
						+ dbStr + "Starter ORDER BY RDT";
				ps.Add(GenerWorkFlowAttr.Starter, WebUser.getNo());
			} else {
				ps.SQL = "SELECT WorkID,Title,FK_Flow,FlowName,RDT,FlowNote,AtPara FROM WF_GenerWorkFlow A WHERE WFState=1 AND Starter="
						+ dbStr + "Starter AND FK_Flow=" + dbStr + "FK_Flow ORDER BY RDT";
				ps.Add(GenerWorkFlowAttr.FK_Flow, flowNo);
				ps.Add(GenerWorkFlowAttr.Starter, WebUser.getNo());
			}

		} else {
			if (flowNo == null) {
				ps.SQL = "SELECT WorkID,Title,FK_Flow,FlowName,RDT,FlowNote,AtPara FROM WF_GenerWorkFlow A WHERE WFState=1 AND Starter="
						+ dbStr + "Starter AND Domain=" + dbStr + "Domain ORDER BY RDT";
				ps.Add(GenerWorkFlowAttr.Starter, WebUser.getNo());
				ps.Add(GenerWorkFlowAttr.Domain, domain);
			} else {
				ps.SQL = "SELECT WorkID,Title,FK_Flow,FlowName,RDT,FlowNote,AtPara FROM WF_GenerWorkFlow A WHERE WFState=1 AND Starter="
						+ dbStr + "Starter AND FK_Flow=" + dbStr + "FK_Flow AND Domain=" + dbStr
						+ "Domain ORDER BY RDT";
				ps.Add(GenerWorkFlowAttr.FK_Flow, flowNo);
				ps.Add(GenerWorkFlowAttr.Starter, WebUser.getNo());
				ps.Add(GenerWorkFlowAttr.Domain, domain);
			}
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("TITLE").ColumnName = "Title";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("title").ColumnName = "Title";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("flownote").ColumnName = "FlowNote";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("atpara").ColumnName = "AtPara";
		}
		return dt;
	}

	/// 流程草稿

	/// 我关注的流程
	/**
	 * 获得我关注的流程列表
	 *
	 * @param flowNo
	 *            流程编号
	 * @param userNo
	 *            操作员编号
	 * @param domain
	 *            域
	 * @return 返回当前关注的流程列表.
	 * @throws Exception
	 */

	public static DataTable DB_Focus(String flowNo, String userNo) throws Exception {
		return DB_Focus(flowNo, userNo, null);
	}

	public static DataTable DB_Focus(String flowNo) throws Exception {
		return DB_Focus(flowNo, null, null);
	}

	public static DataTable DB_Focus() throws Exception {
		return DB_Focus(null, null, null);
	}

	public static DataTable DB_Focus(String flowNo, String userNo, String domain) throws Exception {
		if (flowNo != null && flowNo.equals("")) {
			flowNo = null;
		}

		if (userNo == null) {
			userNo = WebUser.getNo();
		}

		// if (domain == null)
		// domain = "";

		// 执行sql.
		Paras ps = new Paras();
		ps.SQL = "SELECT * FROM WF_GenerWorkFlow WHERE AtPara LIKE  '%F_" + userNo + "=1%'";
		if (flowNo != null) {
			ps.SQL = "SELECT * FROM WF_GenerWorkFlow WHERE AtPara LIKE  '%F_" + userNo + "=1%' AND FK_Flow="
					+ SystemConfig.getAppCenterDBVarStr() + "FK_Flow";
			ps.Add("FK_Flow", flowNo);
		}
		if (DataType.IsNullOrEmpty(domain) == false && DataType.IsNullOrEmpty(flowNo) == false) {
			ps.SQL = "SELECT * FROM WF_GenerWorkFlow WHERE AtPara LIKE  '%F_" + userNo + "=1%' AND FK_Flow="
					+ SystemConfig.getAppCenterDBVarStr() + "FK_Flow AND  Domain=" + SystemConfig.getAppCenterDBVarStr()
					+ "Domain";
			ps.Add("FK_Flow", flowNo);
			ps.Add("Domain", domain);
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		// 添加oracle的处理
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("TITLE").ColumnName = "Title";
			dt.Columns.get("WFSTA").ColumnName = "WFSta";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("BILLNO").ColumnName = "BillNo";
			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FK_DEPT").ColumnName = "FK_Dept";
			dt.Columns.get("FID").ColumnName = "FID";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("WFSTATE").ColumnName = "WFState";
			dt.Columns.get("FK_NY").ColumnName = "FK_NY";
			//dt.Columns.get("MYNUM").ColumnName = "MyNum";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("STARTER").ColumnName = "Starter";
			dt.Columns.get("SENDER").ColumnName = "Sender";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("PRI").ColumnName = "PRI";
			dt.Columns.get("SDTOFNODE").ColumnName = "SDTOfNode";
			dt.Columns.get("SDTOFFLOW").ColumnName = "SDTOfFlow";
			dt.Columns.get("PFLOWNO").ColumnName = "PFlowNo";
			dt.Columns.get("PWORKID").ColumnName = "PWorkID";
			dt.Columns.get("PNODEID").ColumnName = "PNodeID";
			dt.Columns.get("PFID").ColumnName = "PFID";
			dt.Columns.get("PEMP").ColumnName = "PEmp";
			dt.Columns.get("GUESTNO").ColumnName = "GuestNo";
			dt.Columns.get("GUESTNAME").ColumnName = "GuestName";
			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("TODOEMPSNUM").ColumnName = "TodoEmpsNum";
			dt.Columns.get("TASKSTA").ColumnName = "TaskSta";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
			dt.Columns.get("EMPS").ColumnName = "Emps";
			dt.Columns.get("GUID").ColumnName = "GUID";
			dt.Columns.get("WEEKNUM").ColumnName = "WeekNum";
			dt.Columns.get("TSPAN").ColumnName = "TSpan";
			dt.Columns.get("TODOSTA").ColumnName = "TodoSta";
			dt.Columns.get("SYSTYPE").ColumnName = "SysType";

			// dt.Columns.get("CFLOWNO").ColumnName = "CFlowNo";
			// dt.Columns.get("CWORKID").ColumnName = "CWorkID";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("title").ColumnName = "Title";
			dt.Columns.get("wfsta").ColumnName = "WFSta";
			dt.Columns.get("nodename").ColumnName = "NodeName";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("billno").ColumnName = "BillNo";
			dt.Columns.get("flownote").ColumnName = "FlowNote";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("fk_dept").ColumnName = "FK_Dept";
			dt.Columns.get("fid").ColumnName = "FID";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("wfstate").ColumnName = "WFState";
			dt.Columns.get("fk_ny").ColumnName = "FK_NY";
			//dt.Columns.get("mynum").ColumnName = "MyNum";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("starter").ColumnName = "Starter";
			dt.Columns.get("sender").ColumnName = "Sender";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("pri").ColumnName = "PRI";
			dt.Columns.get("sdtofnode").ColumnName = "SDTOfNode";
			dt.Columns.get("sdtofflow").ColumnName = "SDTOfFlow";
			dt.Columns.get("pflowno").ColumnName = "PFlowNo";
			dt.Columns.get("pworkid").ColumnName = "PWorkID";
			dt.Columns.get("pnodeid").ColumnName = "PNodeID";
			dt.Columns.get("pfid").ColumnName = "PFID";
			dt.Columns.get("pemp").ColumnName = "PEmp";
			dt.Columns.get("guestno").ColumnName = "GuestNo";
			dt.Columns.get("guestname").ColumnName = "GuestName";
			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("todoempsnum").ColumnName = "TodoEmpsNum";
			dt.Columns.get("tasksta").ColumnName = "TaskSta";
			dt.Columns.get("atpara").ColumnName = "AtPara";
			dt.Columns.get("emps").ColumnName = "Emps";
			dt.Columns.get("guid").ColumnName = "GUID";
			dt.Columns.get("weeknum").ColumnName = "WeekNum";
			dt.Columns.get("tspan").ColumnName = "TSpan";
			dt.Columns.get("todosta").ColumnName = "TodoSta";
			dt.Columns.get("systype").ColumnName = "SysType";

			// dt.Columns.get("CFLOWNO").ColumnName = "CFlowNo";
			// dt.Columns.get("CWORKID").ColumnName = "CWorkID";
		}
		return dt;
	}

	/// 我关注的流程

	/// 获取当前操作员的共享工作

	public static DataTable DB_Todolist(String userNo, int fk_node) {
		String sql = "";
		sql = "SELECT A.* FROM WF_GenerWorkFlow A, WF_FlowSort B, WF_Flow C, WF_GENERWORKERLIST D ";
		sql += " WHERE (WFState=2 OR WFState=5 OR WFState=8)";
		sql += " AND A.FK_FlowSort=B.No ";
		sql += " AND A.FK_Flow=C.No ";
		sql += " AND A.FK_Node=D.FK_Node ";
		sql += " AND A.WorkID=D.WorkID ";
		sql += " AND D.IsPass=0  "; // = 90 是会签主持人.
		sql += " AND D.FK_Emp='" + userNo + "'";

		if (fk_node != 0) {
			sql += " AND A.FK_Node=" + fk_node;
		}

		sql += "  ORDER BY  B.Idx, C.Idx, A.RDT DESC ";

		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		// 添加oracle的处理
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("PRI").ColumnName = "PRI";
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("TITLE").ColumnName = "Title";
			// dt.Columns.get("ISREAD").ColumnName = "IsRead";
			dt.Columns.get("STARTER").ColumnName = "Starter";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("WFSTATE").ColumnName = "WFState";
			dt.Columns.get("FK_DEPT").ColumnName = "FK_Dept";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("PWORKID").ColumnName = "PWorkID";
			dt.Columns.get("PFLOWNO").ColumnName = "PFlowNo";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";
			dt.Columns.get("FID").ColumnName = "FID";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";
			dt.Columns.get("SYSTYPE").ColumnName = "SysType";
			dt.Columns.get("SDTOFNODE").ColumnName = "SDTOfNode";
			dt.Columns.get("GUESTNO").ColumnName = "GuestNo";
			dt.Columns.get("GUESTNAME").ColumnName = "GuestName";
			dt.Columns.get("BILLNO").ColumnName = "BillNo";
			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";
			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("TODOEMPSNUM").ColumnName = "TodoEmpsNum";
			dt.Columns.get("TODOSTA").ColumnName = "TodoSta";
			dt.Columns.get("TASKSTA").ColumnName = "TaskSta";
			dt.Columns.get("SENDER").ColumnName = "Sender";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
			//dt.Columns.get("MYNUM").ColumnName = "MyNum";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("pri").ColumnName = "PRI";
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("title").ColumnName = "Title";
			// dt.Columns.get("ISREAD").ColumnName = "IsRead";
			dt.Columns.get("starter").ColumnName = "Starter";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("wfstate").ColumnName = "WFState";
			dt.Columns.get("fk_dept").ColumnName = "FK_Dept";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("pworkid").ColumnName = "PWorkID";
			dt.Columns.get("pflowno").ColumnName = "PFlowNo";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("nodename").ColumnName = "NodeName";
			dt.Columns.get("fid").ColumnName = "FID";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";
			dt.Columns.get("systype").ColumnName = "SysType";
			dt.Columns.get("sdtofnode").ColumnName = "SDTOfNode";
			dt.Columns.get("guestno").ColumnName = "GuestNo";
			dt.Columns.get("guestname").ColumnName = "GuestName";
			dt.Columns.get("billno").ColumnName = "BillNo";
			dt.Columns.get("flownote").ColumnName = "FlowNote";
			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("todoempsnum").ColumnName = "TodoEmpsNum";
			dt.Columns.get("todosta").ColumnName = "TodoSta";
			dt.Columns.get("tasksta").ColumnName = "TaskSta";
			dt.Columns.get("sender").ColumnName = "Sender";
			dt.Columns.get("atpara").ColumnName = "AtPara";
			//dt.Columns.get("mynum").ColumnName = "MyNum";
		}

		return dt;
	}

	/**
	 * 获得会签列表
	 *
	 * @param userNo
	 *            人员编号
	 * @return
	 */
	public static DataTable DB_HuiQian(String userNo) {

		return null;
		// WebUser.SignInOfGener2017
	}
	///// <summary>
	/////
	///// </summary>
	///// <param name="fk_node">节点编号</param>
	///// <returns>共享工作列表</returns>
	/////

	/**
	 * 获取当前人员待处理的工作
	 *
	 * @param userNo
	 *            用户编号
	 * @param fk_node
	 *            指定的节点，如果为0就是所有的节点.
	 * @param showWhat
	 *            WFState状态，=5退回的，=2是正在运行的.
	 * @param domain
	 *            域名
	 * @return 返回待办WF_EmpWorks的视图待办.
	 * @throws Exception
	 */

	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo, int fk_node, String showWhat) throws Exception {
		return DB_GenerEmpWorksOfDataTable(userNo, fk_node, showWhat, null);
	}

	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo, int fk_node) throws Exception {
		return DB_GenerEmpWorksOfDataTable(userNo, fk_node, null, null);
	}

	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo) throws Exception {
		return DB_GenerEmpWorksOfDataTable(userNo, 0, null, null);
	}

	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo, int nodeID, String wfstate, String domain) throws Exception {
		return DB_GenerEmpWorksOfDataTable(userNo, nodeID, wfstate, domain,null);
	}

	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo, int nodeID, String wfstate, String domain,
														String flowNo) throws Exception {
		if (DataType.IsNullOrEmpty(userNo) == true) {
			throw new RuntimeException("err@登录信息丢失.");
		}
		// 获得授权信息.
		Auths aths = new Auths();
		aths.Retrieve(AuthAttr.AutherToEmpNo, userNo);

		String whereSQL = " ";

		if (DataType.IsNullOrEmpty(domain) == false) {
			whereSQL = " AND A.Domain='" + domain + "'";
		}

		if (DataType.IsNullOrEmpty(wfstate) == false) {
			whereSQL = " AND A.WFState='" + wfstate + "'";
		}

		if (nodeID != 0) {
			whereSQL = " AND A.FK_Node='" + nodeID + "'";
		}

		if (flowNo != null) {
			whereSQL = " AND A.FK_Flow='" + flowNo + "'";
		}

		String sql;

		if (bp.wf.Glo.getIsEnableTaskPool() == true)
			sql = "SELECT A.*, null as Auther FROM WF_EmpWorks A WHERE  TaskSta=0 AND A.FK_Emp='" + userNo + "' " + whereSQL + "";
		else
		 	sql = "SELECT A.*, null as Auther FROM WF_EmpWorks A WHERE  A.FK_Emp='" + userNo + "' " + whereSQL + "";

		for (Auth ath : aths.ToJavaList()) {
			String todata = ath.getTakeBackDT().replace("-", "");
			if (DataType.IsNullOrEmpty(ath.getTakeBackDT()) == false) {
				int mydt = Integer.parseInt(todata);

				Date dt = new Date();
				SimpleDateFormat matter = new SimpleDateFormat("yyyyMMdd");
				String date = matter.format(dt);

				int nodt = Integer.parseInt(date);
				if (mydt < nodt) {
					continue;
				}
			}
			sql += " UNION ";
			if(ath.getAuthType() == AuthorWay.SpecFlows)
				sql += "SELECT  *,'" + ath.getAuther() + "' as Auther FROM WF_EmpWorks WHERE  FK_Emp='" + ath.getAuther() + "' AND FK_Flow='"+ath.getFlowNo()+"' " + whereSQL + "";
			else
				sql += "SELECT *,'" + ath.getAuther() + "' as Auther FROM WF_EmpWorks WHERE  FK_Emp='" + ath.getAuther() + "' " + whereSQL + "";
		}
		sql += " ORDER BY ADT DESC";
		// 获得待办.
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("PRI").ColumnName = "PRI";
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("ISREAD").ColumnName = "IsRead";
			dt.Columns.get("STARTER").ColumnName = "Starter";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("WFSTATE").ColumnName = "WFState";
			dt.Columns.get("FK_DEPT").ColumnName = "FK_Dept";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("PWORKID").ColumnName = "PWorkID";
			dt.Columns.get("PFLOWNO").ColumnName = "PFlowNo";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";
			dt.Columns.get("TITLE").ColumnName = "Title";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("ADT").ColumnName = "ADT";
			dt.Columns.get("SDT").ColumnName = "SDT";
			dt.Columns.get("FK_EMP").ColumnName = "FK_Emp";
			dt.Columns.get("FID").ColumnName = "FID";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";
			dt.Columns.get("SYSTYPE").ColumnName = "SysType";
			dt.Columns.get("SDTOFNODE").ColumnName = "SDTOfNode";
			dt.Columns.get("PRESSTIMES").ColumnName = "PressTimes";
			dt.Columns.get("GUESTNO").ColumnName = "GuestNo";
			dt.Columns.get("GUESTNAME").ColumnName = "GuestName";
			dt.Columns.get("BILLNO").ColumnName = "BillNo";
			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";
			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("TODOEMPSNUM").ColumnName = "TodoEmpsNum";
			dt.Columns.get("TODOSTA").ColumnName = "TodoSta";
			dt.Columns.get("TASKSTA").ColumnName = "TaskSta";
			dt.Columns.get("LISTTYPE").ColumnName = "ListType";
			dt.Columns.get("SENDER").ColumnName = "Sender";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
			// dt.Columns.get("MYNUM").ColumnName = "MyNum";
		}

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("pri").ColumnName = "PRI";
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("isread").ColumnName = "IsRead";
			dt.Columns.get("starter").ColumnName = "Starter";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("wfstate").ColumnName = "WFState";
			dt.Columns.get("fk_dept").ColumnName = "FK_Dept";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("pworkid").ColumnName = "PWorkID";
			dt.Columns.get("pflowno").ColumnName = "PFlowNo";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("nodename").ColumnName = "NodeName";
			dt.Columns.get("title").ColumnName = "Title";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("adt").ColumnName = "ADT";
			dt.Columns.get("sdt").ColumnName = "SDT";
			dt.Columns.get("fk_emp").ColumnName = "FK_Emp";
			dt.Columns.get("fid").ColumnName = "FID";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";
			dt.Columns.get("systype").ColumnName = "SysType";
			dt.Columns.get("sdtofnode").ColumnName = "SDTOfNode";
			dt.Columns.get("presstimes").ColumnName = "PressTimes";
			dt.Columns.get("guestno").ColumnName = "GuestNo";
			dt.Columns.get("guestname").ColumnName = "GuestName";
			dt.Columns.get("billno").ColumnName = "BillNo";
			dt.Columns.get("flownote").ColumnName = "FlowNote";
			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("todoempsnum").ColumnName = "TodoEmpsNum";
			dt.Columns.get("todosta").ColumnName = "TodoSta";
			dt.Columns.get("tasksta").ColumnName = "TaskSta";
			dt.Columns.get("listtype").ColumnName = "ListType";
			dt.Columns.get("sender").ColumnName = "Sender";
			dt.Columns.get("atpara").ColumnName = "AtPara";
		}
		return dt;
	}



	/**
	 * 获取当前人员待处理的工作
	 *
	 * @param fk_flow
	 *            流程编号
	 * @param fk_node
	 *            节点编号
	 * @return 共享工作列表
	 * @throws Exception
	 */
	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo, String fk_flow) throws Exception {

		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String sql;
		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE FK_Emp=" + dbstr
							+ "FK_Emp AND TaskSta=0  ORDER BY FK_Flow,ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE FK_Emp=" + dbstr + "FK_Emp  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("FK_Emp", userNo);
			} else {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE FK_Emp=" + dbstr + "FK_Emp AND TaskSta=0 AND FK_Flow="
							+ dbstr + "FK_Flow   ORDER BY  ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE FK_Emp=" + dbstr + "FK_Emp AND FK_Flow=" + dbstr
							+ "FK_Flow ORDER BY  ADT DESC ";
				}

				ps.Add("FK_Flow", fk_flow);
				ps.Add("FK_Emp", userNo);
			}
			return DBAccess.RunSQLReturnTable(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr
							+ "FK_Emp  AND TaskSta=0 ORDER BY FK_Flow,ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("FK_Emp", userNo);
			} else {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp AND FK_Flow" + dbstr
							+ "FK_Flow AND TaskSta=0 ORDER BY FK_Flow,ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp AND FK_Flow" + dbstr
							+ "FK_Flow ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("FK_Emp", userNo);
				ps.Add("FK_Flow", fk_flow);
			}
			break;
		case SpecFlows:
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					sql = "SELECT * FROM WF_EmpWorks WHERE FK_Emp=" + dbstr + "FK_Emp AND  FK_Flow IN "
							+ emp.getAuthorFlows() + " AND TaskSta=0 ORDER BY FK_Flow,ADT DESC ";
				} else {
					sql = "SELECT * FROM WF_EmpWorks WHERE FK_Emp=" + dbstr + "FK_Emp AND  FK_Flow IN "
							+ emp.getAuthorFlows() + "  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("FK_Emp", userNo);
			} else {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					sql = "SELECT * FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp  AND FK_Flow" + dbstr
							+ "FK_Flow AND FK_Flow IN " + emp.getAuthorFlows()
							+ " AND TaskSta=0  ORDER BY FK_Flow,ADT DESC ";
				} else {
					sql = "SELECT * FROM WF_EmpWorks WHERE  FK_Emp=" + dbstr + "FK_Emp  AND FK_Flow" + dbstr
							+ "FK_Flow AND FK_Flow IN " + emp.getAuthorFlows() + "  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("FK_Emp", userNo);
				ps.Add("FK_Flow", fk_flow);
			}
			break;
		case None:
			throw new RuntimeException("对方(" + WebUser.getNo() + ")已经取消了授权.");
		default:
			throw new RuntimeException("no such way...");
		}
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 根据状态获取当前操作员的共享工作
	 *
	 * @param wfState
	 *            流程状态
	 * @param fk_flow
	 *            流程编号
	 * @return 表结构与视图WF_EmpWorks一致
	 * @throws Exception
	 */
	public static DataTable DB_GenerEmpWorksOfDataTable(String userNo, WFState wfState, String fk_flow)
			throws Exception {

		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String sql;
		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND TaskSta=0   ORDER BY FK_Flow,ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("WFState", wfState.getValue());
				ps.Add("FK_Emp", userNo);
			} else {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND FK_Flow=" + dbstr + "FK_Flow AND TaskSta=0  ORDER BY  ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND FK_Flow=" + dbstr + "FK_Flow ORDER BY  ADT DESC ";
				}

				ps.Add("WFState", wfState.getValue());
				ps.Add("FK_Flow", fk_flow);
				ps.Add("FK_Emp", userNo);
			}
			return DBAccess.RunSQLReturnTable(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp  AND TaskSta=0  ORDER BY FK_Flow,ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("WFState", wfState.getValue());
				ps.Add("FK_Emp", WebUser.getNo());
			} else {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND FK_Flow" + dbstr + "FK_Flow AND TaskSta=0  ORDER BY FK_Flow,ADT DESC ";
				} else {
					ps.SQL = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND FK_Flow" + dbstr + "FK_Flow ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("WFState", wfState.getValue());
				ps.Add("FK_Emp", WebUser.getNo());
				ps.Add("FK_Flow", fk_flow);
			}
			break;
		case SpecFlows:
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					sql = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows()
							+ " AND TaskSta=0   ORDER BY FK_Flow,ADT DESC ";
				} else {
					sql = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows() + "  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("WFState", wfState.getValue());
				ps.Add("FK_Emp", WebUser.getNo());
			} else {
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					sql = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp  AND FK_Flow" + dbstr + "FK_Flow AND FK_Flow IN " + emp.getAuthorFlows()
							+ " AND TaskSta=0   ORDER BY FK_Flow,ADT DESC ";
				} else {
					sql = "SELECT * FROM WF_EmpWorks WHERE WFState=" + dbstr + "WFState AND FK_Emp=" + dbstr
							+ "FK_Emp  AND FK_Flow" + dbstr + "FK_Flow AND FK_Flow IN " + emp.getAuthorFlows()
							+ "  ORDER BY FK_Flow,ADT DESC ";
				}

				ps.Add("WFState", wfState.getValue());
				ps.Add("FK_Emp", WebUser.getNo());
				ps.Add("FK_Flow", fk_flow);
			}
			break;
		case None:
			throw new RuntimeException("对方(" + WebUser.getNo() + ")已经取消了授权.");
		default:
			throw new RuntimeException("no such way...");
		}
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 获得待办(包括被授权的待办) 区分是自己的待办，还是被授权的待办通过数据源的 FK_Emp 字段来区分。
	 *
	 * @return
	 * @throws Exception
	 */

	public static DataTable DB_Todolist() throws Exception {
		return DB_Todolist(null);
	}

	public static DataTable DB_Todolist(String userNo) throws Exception {
		if (userNo == null) {
			userNo = WebUser.getNo();
			if (WebUser.getIsAuthorize() == false) {
				throw new RuntimeException("@授权登录的模式下不能调用此接口.");
			}
		}

		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String wfSql = "  WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
				+ "  OR WFState=" + WFState.AskForReplay.getValue() + " OR WFState=" + WFState.Shift.getValue()
				+ " OR WFState=" + WFState.ReturnSta.getValue() + " OR WFState=" + WFState.Fix.getValue();
		/* 不是授权状态 */
		if (bp.wf.Glo.getIsEnableTaskPool() == true) {
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp AND TaskSta!=1 ";
		} else {
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp ";
		}

		ps.Add("FK_Emp", userNo);

		// 获取授权给他的人员列表.
		bp.wf.port.WFEmps emps = new bp.wf.port.WFEmps();
		emps.Retrieve(bp.wf.port.WFEmpAttr.Author, userNo);
		for (bp.wf.port.WFEmp emp : emps.ToJavaList()) {
			switch (emp.getHisAuthorWay()) {
			case All:
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL += " UNION  SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' AND TaskSta!=1  ";
				} else {
					ps.SQL += " UNION  SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' ";
				}

				break;
			case SpecFlows:
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					ps.SQL += " UNION SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' AND  FK_Flow IN " + emp.getAuthorFlows() + " AND TaskSta!=0 ";
				} else {
					ps.SQL += " UNION SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' AND  FK_Flow IN " + emp.getAuthorFlows() + "  ";
				}

				break;
			case None: // 非授权状态下.
				continue;
			default:
				throw new RuntimeException("no such way...");
			}
		}
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 获取当前操作人员的待办信息 数据内容请参考图:WF_EmpWorks
	 *
	 * @return 返回从视图WF_EmpWorks查询出来的数据.
	 * @throws Exception
	 */
	public static DataTable DB_GenerEmpWorksOfDataTable() throws Exception {
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String wfSql = "  WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
				+ "  OR WFState=" + WFState.AskForReplay.getValue() + " OR WFState=" + WFState.Shift.getValue()
				+ " OR WFState=" + WFState.ReturnSta.getValue() + " OR WFState=" + WFState.Fix.getValue();
		String sql;

		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp ORDER BY ADT DESC";
			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnTable(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
						+ "FK_Emp AND TaskSta!=1  ORDER BY ADT DESC";
			} else {
				ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
						+ "FK_Emp ORDER BY ADT DESC";
			}

			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case SpecFlows:
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
						+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows() + " AND TaskSta!=0    ORDER BY ADT DESC";
			} else {
				ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
						+ "FK_Emp AND  FK_Flow IN " + emp.getAuthorFlows() + "   ORDER BY ADT DESC";
			}

			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case None:
			/* 不是授权状态 */
			if (bp.wf.Glo.getIsEnableTaskPool() == true) {
				ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
						+ "FK_Emp AND TaskSta!=1  ORDER BY ADT DESC";
			} else {
				ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr
						+ "FK_Emp ORDER BY ADT DESC";
			}

			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnTable(ps);

		default:
			throw new RuntimeException("no such way...");
		}
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 获得已完成数据统计列表
	 *
	 * @param userNo
	 *            操作员编号
	 * @return 具有FlowNo,FlowName,Num三个列的指定人员的待办列表
	 */
	public static DataTable DB_FlowCompleteGroup(String userNo) {
		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL = "SELECT FK_Flow as No,FlowName,COUNT(*) Num FROM WF_GenerWorkFlow WHERE Emps LIKE '%@" + userNo
				+ "@%' AND FID=0 AND WFState=" + WFState.Complete.getValue() + " GROUP BY FK_Flow,FlowName";
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 获取指定页面已经完成流程
	 *
	 * @param userNo
	 *            用户编号
	 * @param flowNo
	 *            流程编号
	 * @param pageSize
	 *            每页的数量
	 * @param pageIdx
	 *            第几页
	 * @return 用户编号
	 * @throws Exception
	 */
	public static DataTable DB_FlowComplete(String userNo, String flowNo, int pageSize, int pageIdx) throws Exception {
		/* 如果不是删除流程注册表. */
		GenerWorkFlows ens = new GenerWorkFlows();
		QueryObject qo = new QueryObject(ens);
		if (flowNo != null) {
			qo.AddWhere(GenerWorkFlowAttr.FK_Flow, flowNo);
			qo.addAnd();
		}
		qo.AddWhere(GenerWorkFlowAttr.FID, 0);
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.WFState, WFState.Complete.getValue());
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.Emps, " LIKE ", " '%@" + userNo + "@%'");
		/** 小周鹏修改-----------------------------START **/
		// qo.DoQuery(GenerWorkFlowAttr.WorkID,pageSize, pageIdx);
		qo.DoQuery(GenerWorkFlowAttr.WorkID, pageSize, pageIdx, GenerWorkFlowAttr.RDT, true);
		/** 小周鹏修改-----------------------------END **/
		return ens.ToDataTableField();

	}

	/**
	 * 查询指定流程中已完成的流程
	 *
	 * @param userNo
	 * @param pageCount
	 * @param pageSize
	 * @param pageIdx
	 * @param strFlow
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_FlowComplete(String userNo, int pageCount, int pageSize, int pageIdx, String strFlow)
			throws Exception {

		/* 如果不是删除流程注册表. */
		GenerWorkFlows ens = new GenerWorkFlows();
		QueryObject qo = new QueryObject(ens);
		qo.AddWhere(GenerWorkFlowAttr.FID, 0);
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.WFState, WFState.Complete.getValue());
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.Emps, " LIKE ", " '%@" + userNo + "@%'");
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.FK_Flow, strFlow);
		qo.DoQuery(GenerWorkFlowAttr.WorkID, pageSize, pageIdx);
		return ens.ToDataTableField();

	}

	/**
	 * 查询指定流程中已完成的公告流程
	 *
	 * @param pageCount
	 *            页数
	 * @param pageSize
	 *            每页条数
	 * @param pageIdx
	 *            页码
	 * @param strFlow
	 *            流程编号
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_FlowComplete(String strFlow, int pageSize, int pageIdx) throws Exception {

		/* 如果不是删除流程注册表. */
		GenerWorkFlows ens = new GenerWorkFlows();
		QueryObject qo = new QueryObject(ens);
		qo.AddWhere(GenerWorkFlowAttr.FID, 0);
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.WFState, WFState.Complete.getValue());
		qo.addAnd();
		qo.AddWhere(GenerWorkFlowAttr.FK_Flow, strFlow);
		qo.DoQuery(GenerWorkFlowAttr.WorkID, pageSize, pageIdx);
		return ens.ToDataTableField();

	}

	/**
	 * 获取已经完成流程
	 *
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_TongJi_FlowComplete() throws Exception {

		DataTable dt = null;

		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL = "SELECT T.FK_Flow, T.FlowName, COUNT(T.WorkID) as Num FROM WF_GenerWorkFlow T WHERE (T.Emps LIKE '%@"
				+ WebUser.getNo() + "@%' OR  T.Emps LIKE '%@" + WebUser.getNo() + ",%') AND T.FID=0 AND T.WFSta="
				+ WFSta.Complete.getValue() + " GROUP BY T.FK_Flow,T.FlowName";
		dt = DBAccess.RunSQLReturnTable(ps);

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("NUM").ColumnName = "Num";
		}

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("num").ColumnName = "Num";
		}

		return dt;

	}

	/**
	 * 获取已经完成流程
	 *
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_FlowComplete() throws Exception {
		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL = "SELECT 'RUNNING' AS Type, T.* FROM WF_GenerWorkFlow T WHERE (T.Emps LIKE '%@" + WebUser.getNo()
				+ "@%' OR T.Emps LIKE '%@" + WebUser.getNo() + ",%') AND T.FID=0 AND T.WFState="
				+ WFState.Complete.getValue() + " ORDER BY  RDT DESC";
		DataTable dt = DBAccess.RunSQLReturnTable(ps);

		// 需要翻译.
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("TYPE").ColumnName = "Type";
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";
			dt.Columns.get("SYSTYPE").ColumnName = "SysType";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("TITLE").ColumnName = "Title";

			dt.Columns.get("WFSTA").ColumnName = "WFSta";
			dt.Columns.get("WFSTATE").ColumnName = "WFState";
			dt.Columns.get("STARTER").ColumnName = "Starter";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("SENDER").ColumnName = "Sender";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";

			dt.Columns.get("FK_DEPT").ColumnName = "FK_Dept";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("SDTOFNODE").ColumnName = "SDTOfNode";
			dt.Columns.get("SDTOFFLOW").ColumnName = "SDTOfFlow";
			dt.Columns.get("PFLOWNO").ColumnName = "PflowNo";
			dt.Columns.get("PWORKID").ColumnName = "PWorkID";

			dt.Columns.get("PNODEID").ColumnName = "PNodeID";
			dt.Columns.get("PEMP").ColumnName = "PEmp";
			dt.Columns.get("GUESTNO").ColumnName = "GuestNo";
			dt.Columns.get("GUESTNAME").ColumnName = "GuestName";
			dt.Columns.get("BILLNO").ColumnName = "BillNo";
			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";

			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("TODOEMPSNUM").ColumnName = "TodoEmpsNum";
			dt.Columns.get("TASKSTA").ColumnName = "TaskSta";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
			dt.Columns.get("EMPS").ColumnName = "Emps";
			dt.Columns.get("DOMAIN").ColumnName = "Domain";
			dt.Columns.get("SENDDT").ColumnName = "SendDT";
			dt.Columns.get("WEEKNUM").ColumnName = "WeekNum";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("type").ColumnName = "Type";
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";
			dt.Columns.get("systype").ColumnName = "SysType";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("title").ColumnName = "Title";

			dt.Columns.get("wfsta").ColumnName = "WFSta";
			dt.Columns.get("wfstate").ColumnName = "WFState";
			dt.Columns.get("starter").ColumnName = "Starter";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("sender").ColumnName = "Sender";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("nodename").ColumnName = "NodeName";

			dt.Columns.get("fk_dept").ColumnName = "FK_Dept";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("sdtofnode").ColumnName = "SDTOfNode";
			dt.Columns.get("sdtofflow").ColumnName = "SDTOfFlow";
			dt.Columns.get("pflowno").ColumnName = "PflowNo";
			dt.Columns.get("pworkid").ColumnName = "PWorkID";

			dt.Columns.get("pnodeid").ColumnName = "PNodeID";
			dt.Columns.get("pemp").ColumnName = "PEmp";
			dt.Columns.get("guestno").ColumnName = "GuestNo";
			dt.Columns.get("guestname").ColumnName = "GuestName";
			dt.Columns.get("billno").ColumnName = "BillNo";
			dt.Columns.get("flownote").ColumnName = "FlowNote";

			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("todoempsnum").ColumnName = "TodoEmpsNum";
			dt.Columns.get("tasksta").ColumnName = "TaskSta";
			dt.Columns.get("atpara").ColumnName = "AtPara";
			dt.Columns.get("emps").ColumnName = "Emps";
			dt.Columns.get("domain").ColumnName = "Domain";
			dt.Columns.get("senddt").ColumnName = "SendDT";
			dt.Columns.get("weeknum").ColumnName = "WeekNum";
		}
		return dt;
	}

	/**
	 * 获取某一个人已完成的工作
	 *
	 * @param userNo
	 * @return
	 */
	public static DataTable DB_FlowComplete(String userNo) {

		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL = "SELECT 'RUNNING' AS Type, T.* FROM WF_GenerWorkFlow T WHERE (T.Emps LIKE '%@" + userNo
				+ "@%' OR  T.Emps LIKE '%@" + userNo + ",%') AND T.FID=0 AND T.WFState=" + WFState.Complete.getValue()
				+ " ORDER BY  RDT DESC";
		return DBAccess.RunSQLReturnTable(ps);

	}

	/**
	 * 获取某一个人某个流程已完成的工作
	 *
	 * @param userNo
	 * @return
	 */
	public static DataTable DB_FlowComplete(String userNo, String flowNo) {

		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL = "SELECT 'RUNNING' AS Type, T.* FROM WF_GenerWorkFlow T WHERE (T.Emps LIKE '%@" + userNo
				+ "@%' OR  T.Emps LIKE '%@" + userNo + ",%') AND T.FK_Flow='" + flowNo + "' AND T.FID=0 AND T.WFState="
				+ WFState.Complete.getValue() + " ORDER BY  RDT DESC";
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 获取已经完成
	 *
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_FlowCompleteAndCC() throws Exception {
		DataTable dt = DB_FlowComplete();
		DataTable ccDT = DB_CCList_CheckOver(WebUser.getNo());

		try {
			dt.Columns.Add("MyPK");
			dt.Columns.Add("Sta");
		} catch (RuntimeException e) {

		}

		for (DataRow row : ccDT.Rows) {
			DataRow newRow = dt.NewRow();

			for (DataColumn column : ccDT.Columns) {
				for (DataColumn dtColumn : dt.Columns) {
					if (column.ColumnName.equals(dtColumn.ColumnName)) {
						newRow.setValue(column.ColumnName, row.getValue(dtColumn.ColumnName));
					}

				}

			}
			newRow.setValue("Type", "CC");
			dt.Rows.add(newRow);
		}

		return dt;
	}

	public static DataTable DB_FlowComplete2(String fk_flow, String title) throws Exception {

		/* 如果不是删除流程注册表. */
		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		if (DataType.IsNullOrEmpty(fk_flow)) {
			if (DataType.IsNullOrEmpty(title)) {
				ps.SQL = "SELECT 'RUNNING' AS Type,* FROM WF_GenerWorkFlow WHERE Emps LIKE '%@" + WebUser.getNo()
						+ "@%' AND FID=0 AND WFState=" + WFState.Complete.getValue()
						+ " and FK_Flow!='010' order by RDT desc";
			} else {
				ps.SQL = "SELECT 'RUNNING' AS Type,* FROM WF_GenerWorkFlow WHERE Emps LIKE '%@" + WebUser.getNo()
						+ "@%' and Title Like '%" + title + "%' AND FID=0 AND WFState=" + WFState.Complete.getValue()
						+ " and FK_Flow!='010' order by RDT desc";
			}
		} else {
			if (DataType.IsNullOrEmpty(title)) {
				ps.SQL = "SELECT 'RUNNING' AS Type,* FROM WF_GenerWorkFlow WHERE Emps LIKE '%@" + WebUser.getNo()
						+ "@%' AND FID=0 AND WFState=" + WFState.Complete.getValue() + " and FK_Flow='" + fk_flow
						+ "' order by RDT desc";
			} else {
				ps.SQL = "SELECT 'RUNNING' AS Type,* FROM WF_GenerWorkFlow WHERE Emps LIKE '%@" + WebUser.getNo()
						+ "@%' and Title Like '%" + title + "%' AND FID=0 AND WFState=" + WFState.Complete.getValue()
						+ " and FK_Flow='" + fk_flow + "' order by RDT desc";
			}
		}
		return DBAccess.RunSQLReturnTable(ps);

	}

	public static DataTable DB_FlowCompleteAndCC2(String fk_flow, String title) throws Exception {
		DataTable dt = DB_FlowComplete2(fk_flow, title);
		DataTable ccDT = DB_CCList_CheckOver(WebUser.getNo());
		try {
			dt.Columns.Add("MyPK");
			dt.Columns.Add("Sta");
		} catch (RuntimeException e) {

		}

		for (DataRow row : ccDT.Rows) {
			DataRow newRow = dt.NewRow();

			for (DataColumn column : ccDT.Columns) {
				for (DataColumn dtColumn : dt.Columns) {
					if (column.ColumnName.equals(dtColumn.ColumnName)) {
						newRow.setValue(column.ColumnName, row.getValue(dtColumn.ColumnName));
					}

				}

			}
			newRow.setValue("Type", "CC");
			dt.Rows.add(newRow);
		}

		return dt;
	}

	/**
	 * 获得任务池的工作列表
	 *
	 * @return 任务池的工作列表
	 * @throws Exception
	 */
	public static DataTable DB_TaskPool() throws Exception {
		if (bp.wf.Glo.getIsEnableTaskPool() == false) {
			throw new RuntimeException("@你必须在Web.config中启用IsEnableTaskPool才可以执行此操作。");
		}

		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String wfSql = "  (WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
				+ " OR WFState=" + WFState.Shift.getValue() + " OR WFState=" + WFState.ReturnSta.getValue()
				+ ") AND TaskSta=" + TaskSta.Sharing.getValue();
		String sql;
		String realSql = null;
		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp ";
			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnTable(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp AND TaskSta=0";
			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case SpecFlows:
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp AND  FK_Flow IN "
					+ emp.getAuthorFlows() + " ";
			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case None:
			throw new RuntimeException("对方(" + WebUser.getNo() + ")已经取消了授权.");
		default:
			throw new RuntimeException("no such way...");
		}
		// @杜. 这里需要翻译.
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("ISREAD").ColumnName = "IsRead";
			dt.Columns.get("STARTER").ColumnName = "Starter";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("WFSTATE").ColumnName = "WFState";
			dt.Columns.get("FK_DEPT").ColumnName = "FK_Dept";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("PWORKID").ColumnName = "PWorkID";

			dt.Columns.get("PFLOWNO").ColumnName = "PFlowNo";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("WORKERDEPT").ColumnName = "WorkerDept";
			dt.Columns.get("FK_EMP").ColumnName = "FK_Emp";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";

			dt.Columns.get("SYSTYPE").ColumnName = "SysType";
			dt.Columns.get("SDTOFNODE").ColumnName = "SDTOfNode";
			dt.Columns.get("GUESTNO").ColumnName = "GuestNo";
			dt.Columns.get("GUESTNAME").ColumnName = "GuestName";
			dt.Columns.get("BILLNO").ColumnName = "BillNo";

			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";
			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("TODOEMPSNUM").ColumnName = "TodoEmpsNum";
			dt.Columns.get("TODOSTA").ColumnName = "TodoSta";
			dt.Columns.get("TASKSTA").ColumnName = "TaskSta";

			dt.Columns.get("LISTTYPE").ColumnName = "ListType";
			dt.Columns.get("SENDER").ColumnName = "Sender";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
			//dt.Columns.get("MYNUM").ColumnName = "MyNum";
		}

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("isread").ColumnName = "IsRead";
			dt.Columns.get("starter").ColumnName = "Starter";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("wfstate").ColumnName = "WFState";
			dt.Columns.get("fk_dept").ColumnName = "FK_Dept";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("pworkid").ColumnName = "PWorkID";

			dt.Columns.get("pflowno").ColumnName = "PFlowNo";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("workerdept").ColumnName = "WorkerDept";
			dt.Columns.get("fk_emp").ColumnName = "FK_Emp";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";

			dt.Columns.get("systype").ColumnName = "SysType";
			dt.Columns.get("sdtofnode").ColumnName = "SDTOfNode";
			dt.Columns.get("guestno").ColumnName = "GuestNo";
			dt.Columns.get("guestname").ColumnName = "GuestName";
			dt.Columns.get("billno").ColumnName = "BillNo";

			dt.Columns.get("flownote").ColumnName = "FlowNote";
			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("todoempsnum").ColumnName = "TodoEmpsNum";
			dt.Columns.get("todosta").ColumnName = "TodoSta";
			dt.Columns.get("tasksta").ColumnName = "TaskSta";

			dt.Columns.get("listtype").ColumnName = "ListType";
			dt.Columns.get("sender").ColumnName = "Sender";
			dt.Columns.get("atpara").ColumnName = "AtPara";
			//dt.Columns.get("mynum").ColumnName = "MyNum";
		}

		return dt;
	}

	/**
	 * 获得我从任务池里申请下来的工作列表
	 *
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_TaskPoolOfMyApply() throws Exception {
		if (bp.wf.Glo.getIsEnableTaskPool() == false) {
			throw new RuntimeException("@你必须在Web.config中启用IsEnableTaskPool才可以执行此操作。");
		}

		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String wfSql = "  (WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
				+ " OR WFState=" + WFState.Shift.getValue() + " OR WFState=" + WFState.ReturnSta.getValue()
				+ ") AND TaskSta=" + TaskSta.Takeback.getValue();
		String sql;
		String realSql;
		if (WebUser.getIsAuthorize() == false) {
			/* 不是授权状态 */
			// ps.SQL="SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND
			// FK_Emp=" + dbstr + "FK_Emp ORDER BY FK_Flow,ADT DESC ";
			// ps.SQL="SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND
			// FK_Emp=" + dbstr + "FK_Emp ORDER BY ADT DESC ";
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp";

			// ps.SQL="select v1.*,v2.name,v3.name as ParentName from (" +
			// realSql + ") as v1 left join JXW_Inc v2 on v1.WorkID=v2.OID left
			// join Jxw_Inc V3 on v1.PWorkID = v3.OID ORDER BY v1.ADT DESC";

			ps.Add("FK_Emp", WebUser.getNo());
			return DBAccess.RunSQLReturnTable(ps);
		}

		/* 如果是授权状态, 获取当前委托人的信息. */
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		switch (emp.getHisAuthorWay()) {
		case All:
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp AND TaskSta=0";
			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case SpecFlows:
			ps.SQL = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp=" + dbstr + "FK_Emp AND  FK_Flow IN "
					+ emp.getAuthorFlows() + "";
			ps.Add("FK_Emp", WebUser.getNo());
			break;
		case None:
			throw new RuntimeException("对方(" + WebUser.getNo() + ")已经取消了授权.");
		default:
			throw new RuntimeException("no such way...");
		}

		// @杜. 这里需要翻译.
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("ISREAD").ColumnName = "IsRead";
			dt.Columns.get("STARTER").ColumnName = "Starter";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("WFSTATE").ColumnName = "WFState";
			dt.Columns.get("FK_DEPT").ColumnName = "FK_Dept";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("PWORKID").ColumnName = "PWorkID";

			dt.Columns.get("PFLOWNO").ColumnName = "PFlowNo";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("WORKERDEPT").ColumnName = "WorkerDept";
			dt.Columns.get("FK_EMP").ColumnName = "FK_Emp";
			dt.Columns.get("FK_FLOWSORT").ColumnName = "FK_FlowSort";

			dt.Columns.get("SYSTYPE").ColumnName = "SysType";
			dt.Columns.get("SDTOFNODE").ColumnName = "SDTOfNode";
			dt.Columns.get("GUESTNO").ColumnName = "GuestNo";
			dt.Columns.get("GUESTNAME").ColumnName = "GuestName";
			dt.Columns.get("BILLNO").ColumnName = "BillNo";

			dt.Columns.get("FLOWNOTE").ColumnName = "FlowNote";
			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("TODOEMPSNUM").ColumnName = "TodoEmpsNum";
			dt.Columns.get("TODOSTA").ColumnName = "TodoSta";
			dt.Columns.get("TASKSTA").ColumnName = "TaskSta";

			dt.Columns.get("LISTTYPE").ColumnName = "ListType";
			dt.Columns.get("SENDER").ColumnName = "Sender";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
			//dt.Columns.get("MYNUM").ColumnName = "MyNum";
		}

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("isread").ColumnName = "IsRead";
			dt.Columns.get("starter").ColumnName = "Starter";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("wfstate").ColumnName = "WFState";
			dt.Columns.get("fk_dept").ColumnName = "FK_Dept";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("pworkid").ColumnName = "PWorkID";

			dt.Columns.get("pflowno").ColumnName = "PFlowNo";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("workerdept").ColumnName = "WorkerDept";
			dt.Columns.get("fk_emp").ColumnName = "FK_Emp";
			dt.Columns.get("fk_flowsort").ColumnName = "FK_FlowSort";

			dt.Columns.get("systype").ColumnName = "SysType";
			dt.Columns.get("sdtofnode").ColumnName = "SDTOfNode";
			dt.Columns.get("guestno").ColumnName = "GuestNo";
			dt.Columns.get("guestname").ColumnName = "GuestName";
			dt.Columns.get("billno").ColumnName = "BillNo";

			dt.Columns.get("flownote").ColumnName = "FlowNote";
			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("todoempsnum").ColumnName = "TodoEmpsNum";
			dt.Columns.get("todosta").ColumnName = "TodoSta";
			dt.Columns.get("tasksta").ColumnName = "TaskSta";

			dt.Columns.get("listtype").ColumnName = "ListType";
			dt.Columns.get("sender").ColumnName = "Sender";
			dt.Columns.get("atpara").ColumnName = "AtPara";
			//dt.Columns.get("mynum").ColumnName = "MyNum";
		}

		return dt;
	}

	/**
	 * 获得所有的流程挂起工作列表
	 *
	 * @return 返回从视图WF_EmpWorks查询出来的数据.
	 * @throws Exception
	 */
	public static DataTable DB_GenerHungUpList() throws Exception {
		return DB_GenerHungUpList(null);
	}

	/**
	 * 将要执行的工作
	 *
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_FutureTodolist() throws Exception {
		String sql = "SELECT A.* FROM WF_GenerWorkFlow A, WF_SelectAccper B WHERE A.WorkID=B.WorkID AND B.FK_Emp='"
				+ WebUser.getNo() + "'";

		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		return dt;
	}

	/**
	 * 获得指定流程挂起工作列表
	 *
	 * @param fk_flow
	 *            流程编号,如果编号为空则返回所有的流程挂起工作列表.
	 * @return 返回从视图WF_EmpWorks查询出来的数据.
	 * @throws Exception
	 */
	public static DataTable DB_GenerHungUpList(String fk_flow) throws Exception {
		String sql;
		int state = WFState.HungUp.getValue();
		if (WebUser.getIsAuthorize()) {
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
			if (DataType.IsNullOrEmpty(fk_flow)) {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.WFState=" + state
						+ " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND B.IsEnable=1 AND A.FK_Flow IN " + emp.getAuthorFlows();
			} else {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.FK_Flow='" + fk_flow
						+ "' AND A.WFState=" + state + " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND  B.IsPass=1 AND A.FK_Flow IN " + emp.getAuthorFlows();
			}
		} else {
			if (DataType.IsNullOrEmpty(fk_flow)) {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.WFState=" + state
						+ " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo() + "' AND B.IsEnable=1   ";
			} else {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WFState=" + state + " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND B.IsEnable=1 ";
			}
		}
		GenerWorkFlows gwfs = new GenerWorkFlows();
		gwfs.RetrieveInSQL(GenerWorkFlowAttr.WorkID, "(" + sql + ")");
		return gwfs.ToDataTableField();
	}

	/**
	 * 获得逻辑删除的流程
	 *
	 * @return 返回从视图WF_EmpWorks查询出来的数据.
	 * @throws Exception
	 */
	public static DataTable DB_GenerDeleteWorkList() throws Exception {
		return DB_GenerDeleteWorkList(WebUser.getNo(), null);
	}

	/**
	 * 获得逻辑删除的流程:根据流程编号
	 *
	 * @param userNo
	 *            操作员编号
	 * @param fk_flow
	 *            流程编号(可以为空)
	 * @return WF_GenerWorkFlow数据结构的集合
	 * @throws Exception
	 */
	public static DataTable DB_GenerDeleteWorkList(String userNo, String fk_flow) throws Exception {
		String sql;
		int state = WFState.Delete.getValue();
		if (WebUser.getIsAuthorize()) {
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
			if (DataType.IsNullOrEmpty(fk_flow)) {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.WFState=" + state
						+ " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND B.IsEnable=1 AND A.FK_Flow IN " + emp.getAuthorFlows();
			} else {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WFState=" + state + " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND  B.IsPass=1 AND A.FK_Flow IN " + emp.getAuthorFlows();
			}
		} else {
			if (DataType.IsNullOrEmpty(fk_flow)) {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.WFState=" + state
						+ " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo() + "' AND B.IsEnable=1   ";
			} else {
				sql = "SELECT A.WorkID FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WFState=" + state + " AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND B.IsEnable=1 ";
			}
		}
		GenerWorkFlows gwfs = new GenerWorkFlows();
		gwfs.RetrieveInSQL(GenerWorkFlowAttr.WorkID, "(" + sql + ")");
		return gwfs.ToDataTableField();
	}

	/// 获取当前操作员的共享工作

	/// 获取流程数据
	/**
	 * 根据流程状态获取指定流程数据
	 *
	 * @param fk_flow
	 *            流程编号
	 * @param sta
	 *            流程状态
	 * @return 数据表OID,Title,RDT,FID
	 * @throws Exception
	 */
	public static DataTable DB_NDxxRpt(String fk_flow, WFState sta) throws Exception {
		Flow fl = new Flow(fk_flow);
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		String sql = "SELECT OID,Title,RDT,FID FROM " + fl.getPTable() + " WHERE WFState=" + sta.getValue()
				+ " AND Rec=" + dbstr + "Rec";
		bp.da.Paras ps = new bp.da.Paras();
		ps.SQL = sql;
		ps.Add("Rec", WebUser.getNo());
		return DBAccess.RunSQLReturnTable(ps);
	}

	///

	/// 工作部件的数据源获取。
	/**
	 * 获取当前节点可以退回的节点
	 *
	 * @param fk_node
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            FID
	 * @return No节点编号,Name节点名称,Rec记录人,RecName记录人名称
	 * @throws Exception
	 */
	public static DataTable DB_GenerWillReturnNodes(int fk_node, long workid, long fid) throws Exception {
		DataTable dt = new DataTable("obt");
		dt.Columns.Add("No", String.class); // 节点ID
		dt.Columns.Add("Name", String.class); // 节点名称.
		dt.Columns.Add("Rec", String.class); // 被退回节点上的操作员编号.
		dt.Columns.Add("RecName", String.class); // 被退回节点上的操作员名称.
		dt.Columns.Add("IsBackTracking", String.class); // 该节点是否可以退回并原路返回？ 0否,
														// 1是.
		dt.Columns.Add("AtPara", String.class); // 该节点是否可以退回并原路返回？ 0否, 1是.

		Node nd = new Node(fk_node);
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		// 增加退回到父流程节点的设计.
		if (nd.getIsStartNode() == true) {
			/* 如果是开始的节点有可能退回到子流程上去. */
			
			if (gwf.getPWorkID() == 0) {
				throw new RuntimeException("@当前节点是开始节点并且不是子流程，您不能执行退回。");
			}

			GenerWorkerLists gwls = new GenerWorkerLists();
			int i = gwls.Retrieve(GenerWorkerListAttr.WorkID, gwf.getPWorkID());
			String nodes = "";
			for (GenerWorkerList gwl : gwls.ToJavaList()) {
				DataRow dr = dt.NewRow();
				dr.setValue("No", String.valueOf(gwl.getFK_Node()));

				if (nodes.contains(String.valueOf(gwl.getFK_Node()) + ",") == true) {
					continue;
				}

				nodes += String.valueOf(gwl.getFK_Node()) + ",";

				dr.setValue("Name", gwl.getFK_NodeText());
				dr.setValue("Rec", gwl.getFK_Emp());
				dr.setValue("RecName", gwl.getFK_EmpText());
				dr.setValue("IsBackTracking", "0");
				dt.Rows.add(dr);
			}
			return dt;
		}

		if (nd.getHisRunModel() == RunModel.SubThread) {
			/* 如果是子线程，它只能退回它的上一个节点，现在写死了，其它的设置不起作用了。 */
			Nodes nds = nd.getFromNodes();
			for (Node ndFrom : nds.ToJavaList()) {
				Work wk;
				switch (ndFrom.getHisRunModel()) {
				case FL:
				case FHL:
					wk = ndFrom.getHisWork();
					wk.setOID(fid);
					if (wk.RetrieveFromDBSources() == 0) {
						continue;
					}

					break;
				case SubThread:
					wk = ndFrom.getHisWork();
					wk.setOID(workid);
					if (wk.RetrieveFromDBSources() == 0) {
						continue;
					}

					break;
				case Ordinary:
				default:
					throw new RuntimeException("流程设计异常，子线程的上一个节点不能是普通节点。");
				}
				if (ndFrom.getNodeID() == fk_node) {
					continue;
				}

				String mysql = "SELECT  a.FK_Emp as Rec, a.FK_EmpText as RecName FROM WF_GenerWorkerlist a WHERE a.FK_Node=" + ndFrom.getNodeID() + " AND  (a.WorkID=" + workid + " AND a.FID=" + fid + " )  ORDER BY RDT DESC ";
				DataTable mydt = DBAccess.RunSQLReturnTable(mysql);
				if (mydt.Rows.size() == 0)
					continue;

				DataRow dr = dt.NewRow();
				dr.setValue("No", String.valueOf(ndFrom.getNodeID()));
				dr.setValue("Name", ndFrom.getName());

				dr.setValue("Rec", mydt.Rows.get(0).getValue(0));
				dr.setValue("RecName", mydt.Rows.get(0).getValue(1));

				if (ndFrom.getIsBackTracking()) {
					dr.setValue("IsBackTracking", "1");
				} else {
					dr.setValue("IsBackTracking", "0");
				}

				dt.Rows.add(dr);
			}
			if (dt.Rows.size() == 0) {
				throw new RuntimeException("err@没有获取到应该退回的节点列表.");
			}

			return dt;
		}

		String sql = "";

		WorkNode wn = new WorkNode(workid, fk_node);
		WorkNodes wns = new WorkNodes();
		switch (nd.getHisReturnRole()) {
		case CanNotReturn:
			return dt;
		case ReturnAnyNodes:
			if (nd.getIsHL() || nd.getIsFLHL()) {
				/* 如果当前点是分流，或者是分合流，就不按退回规则计算了。 */
				sql = "SELECT A.FK_Node AS No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking FROM WF_GenerWorkerlist a, WF_Node b WHERE a.FK_Node=b.NodeID AND a.FID="
						+ fid + " AND a.WorkID=" + workid + " AND a.FK_Node!=" + fk_node
						+ " AND a.IsPass=1 ORDER BY RDT DESC ";
				dt = DBAccess.RunSQLReturnTable(sql);
				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
					dt.Columns.get("NO").ColumnName = "No";
					dt.Columns.get("NAME").ColumnName = "Name";
					dt.Columns.get("REC").ColumnName = "Rec";
					dt.Columns.get("RECNAME").ColumnName = "RecName";
					dt.Columns.get("ISBACKTRACKING").ColumnName = "IsBackTracking";
				}
				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
					dt.Columns.get("no").ColumnName = "No";
					dt.Columns.get("name").ColumnName = "Name";
					dt.Columns.get("rec").ColumnName = "Rec";
					dt.Columns.get("recname").ColumnName = "RecName";
					dt.Columns.get("isbacktracking").ColumnName = "IsBackTracking";
				}

				return dt;
			}

			if (nd.getTodolistModel() == TodolistModel.Order) {
				sql = "SELECT A.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking, a.AtPara FROM WF_GenerWorkerlist a, WF_Node b WHERE a.FK_Node=b.NodeID AND (a.WorkID="
						+ workid + " AND a.IsEnable=1 AND a.IsPass=1 AND a.FK_Node!=" + fk_node + ") OR (a.FK_Node="
						+ fk_node + " AND a.IsPass <0)  ORDER BY a.RDT DESC";
			} else {
				/*sql = "SELECT A.NDFrom AS No, A.NDFromT AS Name, A.EmpFrom AS Rec, A.EmpFromT AS RecName, B.IsBackTracking, A.Msg FROM ND"
						+ Integer.parseInt(nd.getFK_Flow()) + "Track A, WF_Node B WHERE A.NDFrom=B.NodeID AND A.WorkID="
						+ workid + " AND A.ActionType in(" + ActionType.Start.getValue() + ","
						+ ActionType.Forward.getValue() + "," + ActionType.ForwardFL.getValue() + ","
						+ ActionType.ForwardHL.getValue() + ") AND A.NDFrom != " + fk_node + " ORDER BY A.RDT DESC";
				*/
				sql = "SELECT a.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking, a.AtPara FROM WF_GenerWorkerlist a,WF_Node b WHERE a.FK_Node=b.NodeID AND a.WorkID=" + workid + " AND a.IsEnable=1 AND a.IsPass=1 AND a.FK_Node!=" + fk_node + " AND a.AtPara NOT LIKE '%@IsHuiQian=1%' ORDER BY a.RDT DESC";

			}


			dt = DBAccess.RunSQLReturnTable(sql);

			if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
				dt.Columns.get("NO").ColumnName = "No";
				dt.Columns.get("NAME").ColumnName = "Name";
				dt.Columns.get("REC").ColumnName = "Rec";
				dt.Columns.get("RECNAME").ColumnName = "RecName";
				dt.Columns.get("ISBACKTRACKING").ColumnName = "IsBackTracking";
				dt.Columns.get("ATPARA").ColumnName = "AtPara"; // 参数.
			}
			if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
				dt.Columns.get("no").ColumnName = "No";
				dt.Columns.get("name").ColumnName = "Name";
				dt.Columns.get("rec").ColumnName = "Rec";
				dt.Columns.get("recname").ColumnName = "RecName";
				dt.Columns.get("isbacktracking").ColumnName = "IsBackTracking";
				dt.Columns.get("atpara").ColumnName = "AtPara"; // 参数.
			}
			
			if (gwf.getPWorkID()!= 0)
            {
				/* 满足省联社的需求.
                 * 1. 判断是否有延续子流程，延续到当前节点上来？，如果有则把父流程的节点也显示出来。
                 * 2. 
                 */
				sql = "SELECT FK_Flow,FK_Node FROM WF_NodeSubFlow WHERE YanXuToNode LIKE '%" + fk_node + "%'";
                DataTable mydt = DBAccess.RunSQLReturnTable(sql);
                
                for (DataRow drSubFlow : mydt.Rows)
                {
                    String flowNo =  String.valueOf(drSubFlow);
                    String nodeID = String.valueOf(drSubFlow);

                    GenerWorkerLists gwls = new GenerWorkerLists();
                    int i = gwls.Retrieve(GenerWorkerListAttr.WorkID, gwf.getPWorkID());
                    String nodes = "";
                    for (GenerWorkerList gwl : gwls.ToJavaList())
                    {
                        DataRow dr = dt.NewRow();
                        dr.setValue("No", String.valueOf(gwl.getFK_Node()));
                        if (nodes.contains(String.valueOf(gwl.getFK_Node()) + ",") == true)
                            continue;

                        nodes += String.valueOf(gwl.getFK_Node()) + ",";

                        bp.wf.Flow fl = new Flow(gwl.getFK_Flow());

                        
                        dr.setValue("Name", fl.getName() + ":" +gwl.getFK_NodeText());
        				dr.setValue("Rec", gwl.getFK_Emp());
        				dr.setValue("RecName", gwl.getFK_EmpText());
        				dr.setValue("IsBackTracking", "0");
        				dt.Rows.add(dr);
                    }
                }
            }
			
			return dt;
		case ReturnPreviousNode:
			WorkNode mywnP = wn.GetPreviousWorkNode();

			if (nd.getIsHL() || nd.getIsFLHL()) {
				/* 如果当前点是分流，或者是分合流，就不按退回规则计算了。 */
				sql = "SELECT A.FK_Node AS No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking, a.AtPara FROM WF_GenerWorkerlist a, WF_Node b WHERE a.FK_Node=b.NodeID AND a.FID="
						+ fid + " AND a.WorkID=" + workid + " AND a.FK_Node=" + mywnP.getHisNode().getNodeID()
						+ " AND a.IsPass=1 ORDER BY RDT DESC ";
				dt = DBAccess.RunSQLReturnTable(sql);
				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
					dt.Columns.get("NO").ColumnName = "No";
					dt.Columns.get("NAME").ColumnName = "Name";
					dt.Columns.get("REC").ColumnName = "Rec";
					dt.Columns.get("RECNAME").ColumnName = "RecName";
					dt.Columns.get("ISBACKTRACKING").ColumnName = "IsBackTracking";
					dt.Columns.get("ATPARA").ColumnName = "AtPara"; // 参数.
				}
				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
					dt.Columns.get("no").ColumnName = "No";
					dt.Columns.get("name").ColumnName = "Name";
					dt.Columns.get("rec").ColumnName = "Rec";
					dt.Columns.get("recname").ColumnName = "RecName";
					dt.Columns.get("isbacktracking").ColumnName = "IsBackTracking";
					dt.Columns.get("atpara").ColumnName = "AtPara"; // 参数.
				}
				return dt;
			}

			if (nd.getTodolistModel() == TodolistModel.Order) {
				sql = "SELECT A.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking,a.AtPara FROM WF_GenerWorkerlist a, WF_Node b WHERE a.FK_Node=b.NodeID AND (a.WorkID="
						+ workid + " AND a.IsEnable=1 AND a.IsPass=1 AND a.FK_Node=" + mywnP.getHisNode().getNodeID()
						+ ") OR (a.FK_Node=" + mywnP.getHisNode().getNodeID()
						+ " AND a.IsPass <0)  ORDER BY a.RDT DESC";
				dt = DBAccess.RunSQLReturnTable(sql);
			} else {
				sql = "SELECT A.FK_Node as \"No\",a.FK_NodeText as \"Name\", a.FK_Emp as \"Rec\", a.FK_EmpText as \"RecName\", b.IsBackTracking as \"IsBackTracking\", a.AtPara as \"AtPara\"  FROM WF_GenerWorkerlist a,WF_Node b WHERE a.FK_Node=b.NodeID AND a.WorkID="
						+ workid + " AND a.IsEnable=1 AND a.IsPass=1 AND a.FK_Node=" + mywnP.getHisNode().getNodeID()
						+ "  AND ( A.AtPara NOT LIKE '%@IsHuiQian=1%' OR a.AtPara IS NULL) ORDER BY a.RDT DESC ";
				DataTable mydt = DBAccess.RunSQLReturnTable(sql);

				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
					dt.Columns.get("NO").ColumnName = "No";
					dt.Columns.get("NAME").ColumnName = "Name";
					dt.Columns.get("REC").ColumnName = "Rec";
					dt.Columns.get("RECNAME").ColumnName = "RecName";
					dt.Columns.get("ISBACKTRACKING").ColumnName = "IsBackTracking";
					dt.Columns.get("ATPARA").ColumnName = "AtPara";
				}
				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
					dt.Columns.get("no").ColumnName = "No";
					dt.Columns.get("name").ColumnName = "Name";
					dt.Columns.get("rec").ColumnName = "Rec";
					dt.Columns.get("recname").ColumnName = "RecName";
					dt.Columns.get("isbacktracking").ColumnName = "IsBackTracking";
					dt.Columns.get("atpara").ColumnName = "AtPara";
				}

				if (mydt.Rows.size() != 0) {
					return mydt;
				}

				// 有可能是跳转过来的节点.//edited by
				// liuxc,2017-05-26,改RDT排序为CDT排序，更准确，以避免有时找错上一步节点的情况发生
				if (SystemConfig.getAppCenterDBType() == DBType.MSSQL) {
					sql = "SELECT top 1 A.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking,a.AtPara FROM WF_GenerWorkerlist a,WF_Node b WHERE a.FK_Node=b.NodeID AND a.WorkID="
							+ workid + " AND a.IsEnable=1 AND a.IsPass=1 ORDER BY a.CDT DESC ";
				} else if (SystemConfig.getAppCenterDBType() == DBType.Oracle
						|| SystemConfig.getAppCenterDBType() == DBType.KingBaseR3
						|| SystemConfig.getAppCenterDBType() == DBType.KingBaseR6) {
					sql = "SELECT a.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking,a.AtPara FROM WF_GenerWorkerlist a,WF_Node b WHERE a.FK_Node=b.NodeID AND a.WorkID="
							+ workid + " AND a.IsEnable=1 AND a.IsPass=1 AND rownum =1  ORDER BY a.CDT DESC ";
				} else if (SystemConfig.getAppCenterDBType() == DBType.MySQL) {
					sql = "SELECT a.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking,a.AtPara FROM WF_GenerWorkerlist a,WF_Node b WHERE a.FK_Node=b.NodeID AND a.WorkID="
							+ workid + " AND a.IsEnable=1 AND a.IsPass=1 ORDER BY a.CDT DESC LIMIT 1";
				} else if (SystemConfig.getAppCenterDBType() == DBType.PostgreSQL) {
					sql = "SELECT a.FK_Node as No,a.FK_NodeText as Name, a.FK_Emp as Rec, a.FK_EmpText as RecName, b.IsBackTracking,a.AtPara FROM WF_GenerWorkerlist a,WF_Node b WHERE a.FK_Node=b.NodeID AND a.WorkID="
							+ workid + " AND a.IsEnable=1 AND a.IsPass=1 ORDER BY a.CDT DESC LIMIT 1";
				} else {
					throw new RuntimeException("获取上一步节点，未涉及的数据库类型");
				}

				dt = DBAccess.RunSQLReturnTable(sql);

				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
					dt.Columns.get("NO").ColumnName = "No";
					dt.Columns.get("NAME").ColumnName = "Name";
					dt.Columns.get("REC").ColumnName = "Rec";
					dt.Columns.get("RECNAME").ColumnName = "RecName";
					dt.Columns.get("ISBACKTRACKING").ColumnName = "IsBackTracking";
					dt.Columns.get("ATPARA").ColumnName = "AtPara";
				}
				if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
					dt.Columns.get("no").ColumnName = "No";
					dt.Columns.get("name").ColumnName = "Name";
					dt.Columns.get("rec").ColumnName = "Rec";
					dt.Columns.get("recname").ColumnName = "RecName";
					dt.Columns.get("isbacktracking").ColumnName = "IsBackTracking";
					dt.Columns.get("atpara").ColumnName = "AtPara";
				}
				return dt;
			}
			break;
		case ReturnSpecifiedNodes: // 退回指定的节点。
			if (wns.size() == 0) {
				wns.GenerByWorkID(wn.getHisNode().getHisFlow(), workid);
			}

			NodeReturns rnds = new NodeReturns();
			rnds.Retrieve(NodeReturnAttr.FK_Node, fk_node);
			if (rnds.size() == 0) {
				throw new RuntimeException("@流程设计错误，您设置该节点可以退回指定的节点，但是指定的节点集合为空，请在节点属性设置它的制订节点。");
			}

			for (NodeReturn item : rnds.ToJavaList()) {
				GenerWorkerLists gwls = new GenerWorkerLists();
				int i = gwls.Retrieve(GenerWorkerListAttr.FK_Node, item.getReturnTo(), GenerWorkerListAttr.WorkID,
						workid);
				if (i == 0) {
					continue;
				}

				for (GenerWorkerList gwl : gwls.ToJavaList()) {
					DataRow dr = dt.NewRow();
					dr.setValue("No", String.valueOf(gwl.getFK_Node()));
					dr.setValue("Name", gwl.getFK_NodeText());
					dr.setValue("Rec", gwl.getFK_Emp());
					dr.setValue("RecName", gwl.getFK_EmpText());
					Node mynd = new Node(item.getFK_Node());
					if (mynd.getIsBackTracking()) // 是否可以原路返回.
					{
						dr.setValue("IsBackTracking", "1");
					} else {
						dr.setValue("IsBackTracking", "0");
					}

					dt.Rows.add(dr);
				}
			}
			break;
		case ByReturnLine: // 按照流程图画的退回线执行退回.
			Directions dirs = new Directions();
			dirs.Retrieve(DirectionAttr.Node, fk_node);
			if (dirs.size() == 0) {
				throw new RuntimeException("@流程设计错误:当前节点没有画向后退回的退回线,更多的信息请参考退回规则.");
			}

			for (Direction dir : dirs.ToJavaList()) {
				Node toNode = new Node(dir.getToNode());
				sql = "SELECT a.FK_Emp,a.FK_EmpText FROM WF_GenerWorkerlist a, WF_Node b WHERE   a.FK_Node="
						+ toNode.getNodeID() + " AND a.WorkID=" + workid + " AND a.IsEnable=1 AND a.IsPass=1";
				DataTable dt1 = DBAccess.RunSQLReturnTable(sql);
				if (dt1.Rows.size() == 0) {
					continue;
				}

				DataRow dr = dt.NewRow();
				dr.setValue("No", String.valueOf(toNode.getNodeID()));
				dr.setValue("Name", toNode.getName());
				dr.setValue("Rec", dt1.Rows.get(0).getValue(0));
				dr.setValue("RecName", dt1.Rows.get(0).getValue(1));
				if (toNode.getIsBackTracking() == true) {
					dr.setValue("IsBackTracking", "1");
				} else {
					dr.setValue("IsBackTracking", "0");
				}

				dt.Rows.add(dr);
			}
			break;
		default:
			throw new RuntimeException("@没有判断的退回类型。");
		}

		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("NO").ColumnName = "No";
			dt.Columns.get("NAME").ColumnName = "Name";
			dt.Columns.get("REC").ColumnName = "Rec";
			dt.Columns.get("RECNAME").ColumnName = "RecName";
			dt.Columns.get("ISBACKTRACKING").ColumnName = "IsBackTracking";
			dt.Columns.get("ATPARA").ColumnName = "AtPara";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("no").ColumnName = "No";
			dt.Columns.get("name").ColumnName = "Name";
			dt.Columns.get("rec").ColumnName = "Rec";
			dt.Columns.get("recname").ColumnName = "RecName";
			dt.Columns.get("isbacktracking").ColumnName = "IsBackTracking";
			dt.Columns.get("atpara").ColumnName = "AtPara";
		}

		if (dt.Rows.size() == 0) {
			throw new RuntimeException("@没有计算出来要退回的节点，请管理员确认节点退回规则是否合理？当前节点名称:" + nd.getName() + ",退回规则:"
					+ nd.getHisReturnRole().toString());
		}

		return dt;
	}

	/// 工作部件的数据源获取

	/// 获取当前操作员的在途工作

	/**
	 * 获取未完成的流程(也称为在途流程:我参与的但是此流程未完成) 该接口为在途菜单提供数据,在在途工作中，可以执行撤销发送。
	 *
	 * @param userNo
	 *            操作员
	 * @param fk_flow
	 *            流程编号
	 * @param isMyStarter
	 *            是否仅仅查询我发起的在途流程
	 * @return 返回从数据视图WF_GenerWorkflow查询出来的数据.
	 * @throws Exception
	 */

	public static DataTable DB_GenerRuning(String userNo, String fk_flow, boolean isMyStarter, String domain)
			throws Exception {
		return DB_GenerRuning(userNo, fk_flow, isMyStarter, domain, false);
	}

	public static DataTable DB_GenerRuning(String userNo, String fk_flow, boolean isMyStarter) throws Exception {
		return DB_GenerRuning(userNo, fk_flow, isMyStarter, null, false);
	}

	public static DataTable DB_GenerRuning(String userNo, String fk_flow) throws Exception {
		return DB_GenerRuning(userNo, fk_flow, false, null, false);
	}

	public static DataTable DB_GenerRuning(String userNo, String fk_flow, boolean isMyStarter, String domain,
			boolean isContainFuture) throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();

		String domainSQL = "";
		if (domain == null) {
			domainSQL = " AND Domain='" + domain + "' ";
		}

		// 获取用户当前所在的节点
		String currNode = "";
		switch (DBAccess.getAppCenterDBType()) {
		case Oracle:
		case KingBaseR3:
		case KingBaseR6:
			currNode = "(SELECT FK_Node FROM (SELECT FK_Node FROM WF_GenerWorkerlist G WHERE G.WorkID = A.WorkID AND FK_Emp='" + WebUser.getNo()
					+ "' Order by RDT DESC ) WHERE RowNum=1)";
			break;
		case MySQL:
		case PostgreSQL:
			currNode = "(SELECT  FK_Node FROM WF_GenerWorkerlist G WHERE G.WorkID = A.WorkID AND FK_Emp='" + WebUser.getNo()
					+ "' Order by RDT DESC LIMIT 1)";
			break;
		case MSSQL:
			currNode = "(SELECT TOP 1 FK_Node FROM WF_GenerWorkerlist G WHERE G.WorkID = A.WorkID AND FK_Emp='" + WebUser.getNo()
					+ "' Order by RDT DESC)";
			break;
		default:
			break;
		}

		// 授权模式.
		String sql = "";
		String futureSQL = "";
		if (isContainFuture == true) {
			switch (DBAccess.getAppCenterDBType()) {
			case MySQL:
				futureSQL = " UNION SELECT A.WorkID,A.StarterName,A.Title,A.DeptName,D.Name AS NodeName,A.RDT,B.FK_Node,A.FK_Flow,A.FID,A.FlowName,C.EmpName AS TodoEmps,"
						+ currNode + " AS CurrNode ,1 AS RunType FROM WF_GenerWorkFlow A, WF_SelectAccper B,"
						+ "(SELECT GROUP_CONCAT(B.EmpName SEPARATOR ';') AS EmpName, B.WorkID,B.FK_Node FROM WF_GenerWorkFlow A, WF_SelectAccper B WHERE A.WorkID=B.WorkID  group By B.FK_Node,B.WorkID) C,WF_Node D"
						+ " WHERE A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND A.WorkID=B.WorkID AND B.WorkID=C.WorkID AND B.FK_Node = C.FK_Node AND A.FK_Node = D.NodeID AND B.FK_Emp = '"
						+ WebUser.getNo() + "'"
						+ " AND B.FK_Node Not in(Select DISTINCT FK_Node From WF_GenerWorkerlist G where G.WorkID=B.WorkID)AND A.WFState != 3";
				break;
			case MSSQL:
				futureSQL = " UNION SELECT A.WorkID,A.StarterName,A.Title,A.DeptName,D.Name AS NodeName,A.RDT,B.FK_Node,A.FK_Flow,A.FID,A.FlowName,C.EmpName AS TodoEmps ,"
						+ currNode + " AS CurrNode ,1 AS RunType FROM WF_GenerWorkFlow A, WF_SelectAccper B,"
						+ "(SELECT EmpName=STUFF((Select ';'+FK_Emp+','+EmpName From WF_SelectAccper t Where t.FK_Node=B.FK_Node FOR xml path('')) , 1 , 1 , '') , B.WorkID,B.FK_Node FROM WF_GenerWorkFlow A, WF_SelectAccper B WHERE A.WorkID=B.WorkID  group By B.FK_Node,B.WorkID) C,WF_Node D"
						+ " WHERE A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND A.WorkID=B.WorkID AND B.WorkID=C.WorkID AND B.FK_Node = C.FK_Node AND A.FK_Node = D.NodeID AND B.FK_Emp = '"
						+ WebUser.getNo() + "'"
						+ " AND B.FK_Node Not in(Select DISTINCT FK_Node From WF_GenerWorkerlist G where G.WorkID=B.WorkID)AND A.WFState != 3";
				break;
			default:
				break;

			}
		}


		if (WebUser.getIsAuthorize() == true) {
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(userNo);
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (isMyStarter == true) {
					sql = "SELECT DISTINCT a.WorkID,a.StarterName,a.Title,a.DeptName,a.NodeName,a.RDT,a.FK_Node,a.FK_Flow,a.FID,a.FlowName,a.TodoEmps,"
							+ currNode
							+ " AS CurrNode ,0 AS RunType  FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND  A.WorkID=B.WorkID AND A.Starter="
							+ dbStr + "Starter  AND B.FK_Emp=" + dbStr
							+ "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < -1) AND A.FK_Flow IN "
							+ emp.getAuthorFlows();
					if (isContainFuture == true) {

						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("Starter", userNo);
					ps.Add("FK_Emp", userNo);
				} else {
					sql = "SELECT DISTINCT a.WorkID,a.StarterName,a.Title,a.DeptName,a.NodeName,a.RDT,a.FK_Node,a.FK_Flow,a.FID ,a.FlowName,a.TodoEmps,"
							+ currNode
							+ " AS CurrNode ,0 AS RunType FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND  A.WorkID=B.WorkID AND B.FK_Emp="
							+ dbStr + "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < -1) AND A.FK_Flow IN "
							+ emp.getAuthorFlows();
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Emp", userNo);
				}
			} else {
				if (isMyStarter == true) {
					sql = "SELECT DISTINCT a.WorkID,0 AS RunType FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND A.FK_Flow="
							+ dbStr + "FK_Flow  AND A.WorkID=B.WorkID AND B.FK_Emp=" + dbStr
							+ "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < -1) AND  A.Starter=" + dbStr
							+ "Starter AND A.FK_Flow IN " + emp.getAuthorFlows();
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Flow", fk_flow);
					ps.Add("FK_Emp", userNo);
					ps.Add("Starter", userNo);
				} else {
					sql = "SELECT DISTINCT a.WorkID ,0 AS RunType FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND  A.FK_Flow="
							+ dbStr + "FK_Flow  AND A.WorkID=B.WorkID AND B.FK_Emp=" + dbStr
							+ "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < -1) AND A.FK_Flow IN "
							+ emp.getAuthorFlows();
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Flow", fk_flow);
					ps.Add("FK_Emp", userNo);
				}
			}
		}

		// 非授权模式，
		if (WebUser.getIsAuthorize() == false) {
			if (DataType.IsNullOrEmpty(fk_flow)) {
				if (isMyStarter == true) {
					sql = "SELECT DISTINCT a.WorkID,a.StarterName,a.Title,a.DeptName,a.NodeName,a.RDT,a.FK_Node,a.FK_Flow,a.FID ,a.FlowName,a.TodoEmps,"
							+ currNode
							+ " AS CurrNode,0 AS RunType FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND  A.WorkID=B.WorkID AND B.FK_Emp="
							+ dbStr + "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < -1) AND  A.Starter="
							+ dbStr + "Starter ";
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Emp", userNo);
					ps.Add("Starter", userNo);
				} else {
					sql = "SELECT DISTINCT a.WorkID,a.StarterName,a.Title,a.DeptName,a.NodeName,a.RDT,a.FK_Node,a.FK_Flow,a.FID ,a.FlowName,a.TodoEmps ,"
							+ currNode
							+ " AS CurrNode,0 AS RunType FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND  A.WorkID=B.WorkID AND B.FK_Emp="
							+ dbStr + "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < -1)";
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Emp", userNo);
				}
			} else {
				if (isMyStarter == true) {
					sql = "SELECT DISTINCT a.WorkID,a.StarterName,a.Title,a.DeptName,a.NodeName,a.RDT,a.FK_Node,a.FK_Flow,a.FID ,a.FlowName,a.TodoEmps ,"
							+ currNode
							+ " AS CurrNode,0 AS RunType FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%" + WebUser.getNo() + ",%' AND  A.FK_Flow="
							+ dbStr + "FK_Flow  AND A.WorkID=B.WorkID AND B.FK_Emp=" + dbStr
							+ "FK_Emp AND B.IsEnable=1 AND (B.IsPass=1 or B.IsPass < -1 ) AND  A.Starter=" + dbStr
							+ "Starter ";
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Flow", fk_flow);
					ps.Add("FK_Emp", userNo);
					ps.Add("Starter", userNo);
				} else {
					sql = "SELECT DISTINCT a.WorkID,a.StarterName,a.Title,a.DeptName,a.NodeName,a.RDT,a.FK_Node,a.FK_Flow,a.FID ,a.FlowName,a.TodoEmps ,"
							+ currNode
							+ " AS CurrNode,0 AS RunType  FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE  A.TodoEmps  not like '%\" + WebUser.getNo() + \",%'  AND  A.FK_Flow="
							+ dbStr + "FK_Flow  AND A.WorkID=B.WorkID AND B.FK_Emp=" + dbStr
							+ "FK_Emp AND B.IsEnable=1 AND (B.IsPass=1 or B.IsPass < -1 ) ";
					if (isContainFuture == true) {
						sql += futureSQL;
					}
					ps.SQL = sql;
					ps.Add("FK_Flow", fk_flow);
					ps.Add("FK_Emp", userNo);
				}
			}
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("WORKID").ColumnName = "WorkID";
			dt.Columns.get("STARTERNAME").ColumnName = "StarterName";
			dt.Columns.get("TITLE").ColumnName = "Title";
			dt.Columns.get("NODENAME").ColumnName = "NodeName";
			dt.Columns.get("RDT").ColumnName = "RDT";
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FID").ColumnName = "FID";
			dt.Columns.get("FK_NODE").ColumnName = "FK_Node";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dt.Columns.get("TODOEMPS").ColumnName = "TodoEmps";
			dt.Columns.get("CURRNODE").ColumnName = "CurrNode";
			dt.Columns.get("RUNTYPE").ColumnName = "RunType";


		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("workid").ColumnName = "WorkID";
			dt.Columns.get("startername").ColumnName = "StarterName";
			dt.Columns.get("title").ColumnName = "Title";
			dt.Columns.get("nodename").ColumnName = "NodeName";
			dt.Columns.get("rdt").ColumnName = "RDT";
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("fid").ColumnName = "FID";
			dt.Columns.get("fk_node").ColumnName = "FK_Node";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("deptname").ColumnName = "DeptName";
			dt.Columns.get("todoemps").ColumnName = "TodoEmps";
			dt.Columns.get("currnode").ColumnName = "CurrNode";
			dt.Columns.get("runtype").ColumnName = "RunType";


		}
		return dt;
	}

	/**
	 * 在途统计:用于流程查询
	 * 
	 * @return 返回 FK_Flow,FlowName,Num 三个列.
	 * @throws Exception
	 */
	public static DataTable DB_TongJi_Runing() throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		if (WebUser.getIsAuthorize()) {
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
			ps.SQL = "SELECT a.FK_Flow,a.FlowName, Count(a.WorkID) as Num FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp="
					+ dbStr + "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < 0) AND A.FK_Flow IN "
					+ emp.getAuthorFlows() + " GROUP BY A.FK_Flow, A.FlowName";
			ps.Add("FK_Emp", WebUser.getNo());
		} else {
			ps.SQL = "SELECT a.FK_Flow,a.FlowName, Count(a.WorkID) as Num FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp="
					+ dbStr
					+ "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < 0)  GROUP BY A.FK_Flow, A.FlowName";
			ps.Add("FK_Emp", WebUser.getNo());
		}

		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dt.Columns.get("FK_FLOW").ColumnName = "FK_Flow";
			dt.Columns.get("FLOWNAME").ColumnName = "FlowName";
			dt.Columns.get("NUM").ColumnName = "Num";
		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dt.Columns.get("fk_flow").ColumnName = "FK_Flow";
			dt.Columns.get("flowname").ColumnName = "FlowName";
			dt.Columns.get("num").ColumnName = "Num";
		}
		return dt;
	}

	/**
	 * 统计流程状态
	 * 
	 * @return 返回：流程类别编号，名称，流程编号，流程名称，TodoSta0代办中,TodoSta1预警中,TodoSta2预期中,
	 *         TodoSta3已办结.
	 * @throws Exception
	 */
	public static DataTable DB_TongJi_TodoSta() throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		if (WebUser.getIsAuthorize()) {
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
			ps.SQL = "SELECT a.FK_Flow,a.FlowName, Count(a.WorkID) as Num FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp="
					+ dbStr + "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < 0) AND A.FK_Flow IN "
					+ emp.getAuthorFlows() + " GROUP BY A.FK_Flow, A.FlowName";
			ps.Add("FK_Emp", WebUser.getNo());
		} else {
			ps.SQL = "SELECT a.FK_Flow,a.FlowName, Count(a.WorkID) as Num FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp="
					+ dbStr
					+ "FK_Emp AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < 0)  GROUP BY A.FK_Flow, A.FlowName";
			ps.Add("FK_Emp", WebUser.getNo());
		}
		return DBAccess.RunSQLReturnTable(ps);
	}

	public static DataTable DB_GenerRuning2(String userNo, String fk_flow, String titleKey) {
		String sql;
		int state = WFState.Runing.getValue();
		if (DataType.IsNullOrEmpty(fk_flow)) {
			if (DataType.IsNullOrEmpty(titleKey)) {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp='"
						+ userNo + "' AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < 0) and A.FK_Flow!='010'";
			} else {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp='"
						+ userNo
						+ "' AND B.IsEnable=1 AND  (B.IsPass=1 or B.IsPass < 0) and A.FK_Flow!='010' and A.Title Like '%"
						+ titleKey + "%'";
			}
		} else {
			if (DataType.IsNullOrEmpty(titleKey)) {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WorkID=B.WorkID AND B.FK_Emp='" + userNo
						+ "' AND B.IsEnable=1 AND (B.IsPass=1 or B.IsPass < 0 )";
			} else {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WorkID=B.WorkID AND B.FK_Emp='" + userNo
						+ "' AND B.IsEnable=1 AND (B.IsPass=1 or B.IsPass < 0 ) and A.Title Like '%" + titleKey + "%' ";
			}
		}

		return DBAccess.RunSQLReturnTable(sql);
	}

	/**
	 * 在途工作
	 * 
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_GenerRuningV2() throws Exception {
		String userNo = WebUser.getNo();
		String fk_flow = null;

		String sql;
		int state = WFState.Runing.getValue();
		if (WebUser.getIsAuthorize()) {
			bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(userNo);
			if (DataType.IsNullOrEmpty(fk_flow)) {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp='"
						+ userNo + "' AND B.IsEnable=1 AND B.IsPass=1 AND A.FK_Flow IN " + emp.getAuthorFlows();
			} else {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WorkID=B.WorkID AND B.FK_Emp='" + WebUser.getNo()
						+ "' AND B.IsEnable=1 AND B.IsPass=1 AND A.FK_Flow IN " + emp.getAuthorFlows();
			}
		} else {
			if (DataType.IsNullOrEmpty(fk_flow)) {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.WorkID=B.WorkID AND B.FK_Emp='"
						+ userNo + "' AND B.IsEnable=1 AND B.IsPass=1 ";
			} else {
				sql = "SELECT a.* FROM WF_GenerWorkFlow A, WF_GenerWorkerlist B WHERE A.FK_Flow='" + fk_flow
						+ "'  AND A.WorkID=B.WorkID AND B.FK_Emp='" + userNo + "' AND B.IsEnable=1 AND B.IsPass=1 ";
			}
		}
		return DBAccess.RunSQLReturnTable(sql);
	}

	/**
	 * 获取内部系统消息
	 * 
	 * @param myPK
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_GenerPopAlert(String type) throws Exception {
		String sql = "";
		if (type.equals("unRead")) {
			sql = "SELECT LEFT(CONVERT(VARCHAR(20),RDT,120),10) AS SortRDT,Datepart(WEEKDAY, CONVERT(DATETIME,RDT)  + @@DateFirst - 1) AS WeekRDT,"
					+ "* FROM Sys_SMS WHERE SendTo ='" + WebUser.getNo()
					+ "' AND (IsRead = 0 OR IsRead IS NULL)  ORDER BY RDT DESC";
		} else {
			sql = "SELECT LEFT(CONVERT(VARCHAR(20),RDT,120),10) AS SortRDT,Datepart(WEEKDAY, CONVERT(DATETIME,RDT)  + @@DateFirst - 1) AS WeekRDT,"
					+ "* FROM Sys_SMS WHERE SendTo ='" + WebUser.getNo() + "'  ORDER BY RDT DESC";
		}
		return DBAccess.RunSQLReturnTable(sql);
	}

	/**
	 * 获取外部系统消息
	 * 
	 * @param type
	 * @param No
	 * @return
	 */
	public static DataTable DB_GenerPopAlert(String type, String No) {
		String sql = "";
		if (type.equals("unRead")) {
			sql = "SELECT LEFT(CONVERT(VARCHAR(20),RDT,120),10) AS SortRDT,Datepart(WEEKDAY, CONVERT(DATETIME,RDT)  + @@DateFirst - 1) AS WeekRDT,"
					+ "* FROM Sys_SMS WHERE SendTo ='" + No + "' AND (IsRead = 0 OR IsRead IS NULL)  ORDER BY RDT DESC";
		} else {
			sql = "SELECT LEFT(CONVERT(VARCHAR(20),RDT,120),10) AS SortRDT,Datepart(WEEKDAY, CONVERT(DATETIME,RDT)  + @@DateFirst - 1) AS WeekRDT,"
					+ "* FROM Sys_SMS WHERE SendTo ='" + No + "'  ORDER BY RDT DESC";
		}
		return DBAccess.RunSQLReturnTable(sql);
	}

	/**
	 * 更新消息状态
	 * 
	 * @param myPK
	 */
	public static DataTable DB_GenerUpdateMsgSta(String myPK) {
		String sql = "";
		sql = " UPDATE Sys_SMS SET IsRead=1 WHERE MyPK='" + myPK + "'";
		return DBAccess.RunSQLReturnTable(sql);
	}

	/**
	 * 获取未完成的流程(也称为在途流程:我参与的但是此流程未完成)
	 * 
	 * @return 返回从数据视图WF_GenerWorkflow查询出来的数据.
	 * @throws Exception
	 */

	public static DataTable DB_GenerRuning(String userNo, boolean isContainFuture) throws Exception {
		return DB_GenerRuning(userNo, isContainFuture, null);
	}

	public static DataTable DB_GenerRuning() throws Exception {
		return DB_GenerRuning(null, false, null);
	}

	public static DataTable DB_GenerRuning(String userNo, boolean isContainFuture, String domain) throws Exception {
		if (userNo == null) {
			userNo = WebUser.getNo();
		}

		DataTable dt = DB_GenerRuning(userNo, null, false, null, isContainFuture);

		return dt;
	}

	/**
	 * 获取某一个人的在途（参与、未完成的工作）
	 * 
	 * @param userNo
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_GenerRuning(String userNo) throws Exception {
		DataTable dt = DB_GenerRuning(userNo, null);
		dt.Columns.Add("Type");

		for (DataRow row : dt.Rows) {
			row.setValue("Type", "RUNNING");
		}

		return dt;
	}

	/**
	 * 把抄送的信息也发送
	 * 
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_GenerRuningAndCC() throws Exception {
		DataTable dt = DB_GenerRuning();
		DataTable ccDT = DB_CCList_CheckOver(WebUser.getNo());
		try {
			dt.Columns.Add("MyPK");
			dt.Columns.Add("Sta");
		} catch (RuntimeException e) {

		}

		for (DataRow row : ccDT.Rows) {
			DataRow newRow = dt.NewRow();

			for (DataColumn column : ccDT.Columns) {
				for (DataColumn dtColumn : dt.Columns) {
					if (column.ColumnName.equals(dtColumn.ColumnName)) {
						newRow.setValue(column.ColumnName, row.getValue(dtColumn.ColumnName));
					}
				}
			}
			newRow.setValue("Type", "CC");
			dt.Rows.add(newRow);
		}

		return dt;
	}

	/**
	 * 为什么需要这个接口？
	 * 
	 * @param name
	 * @param fk_flow
	 * @param title
	 * @return
	 */
	public static DataTable DB_GenerRuning3(String name, String fk_flow, String title) {
		DataTable dt = DB_GenerRuning2(name, fk_flow, title);

		dt.Columns.Add("Type");

		for (DataRow row : dt.Rows) {
			row.setValue("Type", "RUNNING");
		}

		return dt;
	}

	public static DataTable DB_GenerRuningAndCC2(String name, String fk_flow, String title) throws Exception {
		DataTable dt = DB_GenerRuning3(name, fk_flow, title);
		DataTable ccDT = DB_CCList_CheckOver(WebUser.getNo());
		try {
			dt.Columns.Add("MyPK");
			dt.Columns.Add("Sta");
		} catch (RuntimeException e) {

		}

		for (DataRow row : ccDT.Rows) {
			DataRow newRow = dt.NewRow();

			for (DataColumn column : ccDT.Columns) {
				for (DataColumn dtColumn : dt.Columns) {
					if (column.ColumnName.equals(dtColumn.ColumnName)) {
						newRow.setValue(column.ColumnName, row.getValue(dtColumn.ColumnName));
					}

				}

			}
			newRow.setValue("Type", "CC");
			dt.Rows.add(newRow);
		}

		return dt;
	}

	/// 获取当前操作员的共享工作

	/// 获取当前的批处理工作
	/**
	 * 获取当前节点的批处理工作
	 * 
	 * @param FK_Node
	 * @return
	 * @throws Exception
	 */
	public static DataTable GetBatch(int FK_Node) throws Exception {

		Node nd = new Node(FK_Node);
		Flow fl = nd.getHisFlow();
		String fromTable = "";

		if (fl.getHisDataStoreModel() == DataStoreModel.ByCCFlow) {
			fromTable = nd.getPTable();
		} else {
			fromTable = fl.getPTable();
		}

		String sql = "SELECT a.*, b.Starter,b.Title as STitle,b.ADT,b.WorkID FROM " + fromTable
				+ " a , WF_EmpWorks b WHERE a.OID=B.WorkID AND b.WFState Not IN (7) AND b.FK_Node=" + nd.getNodeID()
				+ " AND b.FK_Emp='" + WebUser.getNo() + "'";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		return dt;
	}

	/// 获取当前的批处理工作

	///

	/// 登陆接口
	/**
	 * 用户登陆,此方法是在开发者校验好用户名与密码后执行
	 * 
	 * @param userNo
	 *            用户名
	 * @param SID
	 *            安全ID,请参考流程设计器操作手册
	 * @throws Exception
	 */
	public static void Port_LoginBySID(String userNo, String sid) throws Exception {
		if (userNo.equals(WebUser.getNo())) {
			return;
		}
		Port_LoginBySID(sid);
	}

	/**
	 * 用户登陆,此方法是在开发者校验好用户名与密码后执行
	 * 
	 * @param SID
	 *            安全ID,请参考流程设计器操作手册
	 * @throws Exception
	 */
	public static void Port_LoginBySID(String sid) throws Exception {
		if (DataType.IsNullOrEmpty(sid)) {
			throw new RuntimeException("err@SID不能为空.");
		}

		sid = sid.trim();

		if (DataType.IsNullOrEmpty(sid) == true) {
			throw new RuntimeException("err@非法的SID.");
		}

		bp.port.Emp myEmp = new bp.port.Emp();
		int i = myEmp.Retrieve("SID", sid);
		if (i == 0) {
			throw new RuntimeException("err@非法的SID:" + sid);
		}

		WebUser.SignInOfGener(myEmp);
		return;
	}

	/**
	 * 登录
	 * 
	 * @param userNo
	 *            人员编号
	 * @param sid
	 *            sid
	 * @return
	 * @throws Exception
	 */

	public static void Port_Login(String userNo) throws Exception {
		Port_Login(userNo, null);
	}

	public static void Port_Login(String userNo, String sid) throws Exception {
		/* 仅仅传递了人员编号，就按照人员来取. */
		bp.port.Emp emp = new bp.port.Emp();
		emp.setNo(userNo);
		emp.RetrieveFromDBSources();
		if (sid != null) {
			if (emp.getSID().equals(sid) == false) {
				throw new RuntimeException("err@SID错误.");
			}
		}

		WebUser.SignInOfGener(emp);
	}


	public static void Port_Login(String userID, String sid, String orgNo)  throws Exception
	{
		/* 仅仅传递了人员编号，就按照人员来取.*/
		bp.port.Emp emp = new bp.port.Emp();
		if (SystemConfig.getCCBPMRunModel() == CCBPMRunModel.SAAS)
		{
			if (orgNo == null)
				throw new RuntimeException("err@缺少OrgNo参数.");
			emp.setNo(orgNo + "_" + userID);
			emp.setOrgNo(orgNo);
		}
		else
		{
			emp.setNo(userID);
		}

		int i = emp.RetrieveFromDBSources();
		if (i == 0)
			throw new Exception("err@用户名:" + emp.GetValByKey("No") + "不存在.");

		if (sid != null)
			if (emp.getSID().equals(sid) == false)
				throw new Exception("err@SID错误.");

		WebUser.SignInOfGener(emp);
		if (orgNo != null)
		{
			bp.web.WebUser.setOrgNo(orgNo);
			bp.web.WebUser.setOrgName(DBAccess.RunSQLReturnStringIsNull("SELECT Name FROM Port_Org WHERE No='" + orgNo + "'", " "));
		}

	}

	/**
	 * 注销当前登录
	 */
	public static void Port_SigOut() {
		WebUser.Exit();
	}

	/**
	 * 获取未读的消息 用于消息提醒.
	 * 
	 * @param userNo
	 *            用户ID
	 */
	public static String Port_SMSInfo(String userNo) {
		Paras ps = new Paras();
		ps.SQL = "SELECT MyPK, EmailTitle  FROM sys_sms where SendTo=" + SystemConfig.getAppCenterDBVarStr()
				+ "SendTo AND IsAlert=0";
		ps.Add("SendTo", userNo);
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		String strs = "";
		for (DataRow dr : dt.Rows) {
			strs += "@" + dr.getValue(0) + "=" + dr.getValue(1).toString();
		}
		ps = new Paras();
		ps.SQL = "UPDATE  sys_sms SET IsAlert=1 WHERE  SendTo=" + SystemConfig.getAppCenterDBVarStr()
				+ "SendTo AND IsAlert=0";
		ps.Add("SendTo", userNo);
		DBAccess.RunSQL(ps);
		return strs;
	}

	/**
	 * 发送消息
	 * 
	 * @param userNo
	 *            信息接收人
	 * @param msgTitle
	 *            标题
	 * @param msgDoc
	 *            内容
	 * @throws Exception
	 */
	public static void Port_SendMsg(String userNo, String msgTitle, String msgDoc, String msgFlag) throws Exception {
		Port_SendMsg(userNo, msgTitle, msgDoc, msgFlag, bp.wf.SMSMsgType.Self, null, 0, 0, 0);
	}

	/**
	 * 获取有效的SID
	 * 
	 * @param userNo
	 *            用户编号
	 * @param logDev
	 *            设备编号
	 * @param activeMinutes
	 *            登录有效时间
	 * @return 返回一个新的SID
	 * @throws Exception
	 */

	public static String Port_GenerSID(String userNo, String logDev) throws Exception {
		return Port_GenerSID(userNo, logDev, 0);
	}

	public static String Port_GenerSID(String userNo) throws Exception {
		return Port_GenerSID(userNo, "PC", 0);
	}

	public static String Port_GenerSID(String userNo, String logDev, int activeMinutes) throws Exception {
		if (logDev == null) {
			logDev = "PC";
		}

		if (activeMinutes == 0) {
			activeMinutes = 300; // 默认为300分钟.
		}

		String key = "SID_" + logDev;

		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(userNo);

		// 如果第一次登录.
		String myGuid = emp.GetParaString(key);
		String guidOID_Dt = emp.GetParaString(key + "_DT");

		if (DataType.IsNullOrEmpty(myGuid) == true || DataType.IsNullOrEmpty(guidOID_Dt) == true) {
			String guid = DBAccess.GenerGUID();
			emp.SetPara(key, guid);

			Date dt = new Date();
			dt = DateUtils.addMinutes(dt, activeMinutes);

			emp.SetPara(key + "_DT", DateUtils.format(dt, "yyyy-MM-dd HH:mm:ss"));
			emp.Update();
			return guid;
		}

		Date dtTo = DataType.ParseSysDateTime2DateTime(guidOID_Dt);
		if (dtTo.compareTo(new Date()) < 0) {
			Date dtUpdate = new Date();
			dtUpdate = DateUtils.addMinutes(dtUpdate, activeMinutes);

			emp.SetPara(key + "_DT", DateUtils.format(dtUpdate, "yyyy-MM-dd HH:mm:ss"));
			emp.Update();
			return myGuid;
		}

		String guidNew = DBAccess.GenerGUID();
		emp.SetPara(key, guidNew);

		Date dtNew = new Date();
		dtNew = DateUtils.addMinutes(dtNew, activeMinutes);

		emp.SetPara(key + "_DT", DateUtils.format(dtNew, "yyyy-MM-dd HH:mm:ss"));
		emp.Update();
		return guidNew;
	}

	/**
	 * 验证用户的合法性
	 * 
	 * @param userNo
	 *            用户编号
	 * @param SID
	 *            密钥
	 * @return 是否匹配
	 */
	public static boolean Port_CheckUserLogin(String userNo, String SID) {
		return true;

	}

	/**
	 * 设置SID
	 * 
	 * @param userNo
	 *            用户编号
	 * @param sid
	 *            SID信息
	 * @return SID
	 */
	public static boolean Port_SetSID(String userNo, String sid) {
		// 判断是否更新的是用户表中的SID
		if (Glo.getUpdataSID().contains("UPDATE Port_Emp SET SID=") == true) {
			// 判断是否视图，如果为视图则不进行修改 需要翻译
			if (DBAccess.IsView("Port_Emp", SystemConfig.getAppCenterDBType()) == true) {
				return false;
			}
		}

		try {
			// 替换变量的值
			Paras ps = new Paras();
			ps.SQL = Glo.getUpdataSID();
			ps.Add("SID", sid);
			ps.Add("No", userNo);
			if (DBAccess.RunSQL(ps) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (RuntimeException ex) {
			if (DBAccess.IsView("Port_Emp", SystemConfig.getAppCenterDBType()) == true) {
				throw new RuntimeException(
						"@执行更新SID失败,您在组织结构集成的时候需要配置一个更新SID的SQL, 比如: update MyUserTable SET SID=@SID WHERE BH='@No'");
			}

			throw ex;
		}
	}

	/**
	 * 发送邮件与消息(如果传入4大流程参数将会增加一个工作链接)
	 * 
	 * @param userNo
	 *            信息接收人
	 * @param title
	 *            标题
	 * @param msgDoc
	 *            内容
	 * @param msgFlag
	 *            消息标志
	 * @param flowNo
	 *            流程编号
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @param fid
	 *            FID
	 * @throws Exception
	 */

	public static void Port_SendMsg(String userNo, String title, String msgDoc, String msgFlag, String msgType,
			String flowNo, long nodeID, long workID, long fid) throws Exception {
		Port_SendMsg(userNo, title, msgDoc, msgFlag, msgType, flowNo, nodeID, workID, fid, null);
	}

	public static void Port_SendMsg(String userNo, String title, String msgDoc, String msgFlag, String msgType,
			String flowNo, long nodeID, long workID, long fid, String pushModel) throws Exception {
		String url = "";
		if (workID != 0) {
			url = Glo.getHostURL() + "WF/Do.htm?SID=" + userNo + "_" + workID + "_" + nodeID;
			url = url.replace("//", "/");
			url = url.replace("//", "/");
			if (bp.wf.SMSMsgType.DoPress.equals(msgType)) {
				url = url + "&DoType=OF";
			}
			if (bp.wf.SMSMsgType.CC.equals(msgType)) {
				url = url + "&DoType=DoOpenCC";
			}

			msgDoc += " <hr>打开工作: " + url;
		}
		String atParas = "@FK_Flow=" + flowNo + "@WorkID=" + workID + "@NodeID=" + nodeID + "@FK_Node=" + nodeID;
		bp.wf.SMS.SendMsg(userNo, title, msgDoc, msgFlag, msgType, atParas, workID, pushModel, url);
	}

	/**
	 * 发送消息
	 * 
	 * @param sendToEmpNo
	 *            接收人
	 * @param smsDoc
	 *            消息内容
	 * @param emailTitle
	 *            邮件标题
	 * @param msgType
	 *            消息类型(例如工作到达后、发送成功后)
	 * @param msgGroupFlag
	 *            消息分组（与消息类型有关联）
	 * @param sendEmpNo
	 *            发送人
	 * @param openUrl
	 *            连接URL
	 * @param pushModel
	 *            可以接受消息的类型(如邮件、短信、丁丁、微信等)
	 * @param msgPK
	 *            唯一标志,防止发送重复.
	 * @param atParas
	 *            参数.
	 * @throws Exception
	 */

	public static void Port_SendMessage(String sendToEmpNo, String smsDoc, String emailTitle, String msgType,
			String msgGroupFlag, String sendEmpNo, String openUrl, String pushModel, long workID, String msgPK)
					throws Exception {
		Port_SendMessage(sendToEmpNo, smsDoc, emailTitle, msgType, msgGroupFlag, sendEmpNo, openUrl, pushModel, workID,
				msgPK, null);
	}

	public static void Port_SendMessage(String sendToEmpNo, String smsDoc, String emailTitle, String msgType,
			String msgGroupFlag, String sendEmpNo, String openUrl, String pushModel, long workID) throws Exception {
		Port_SendMessage(sendToEmpNo, smsDoc, emailTitle, msgType, msgGroupFlag, sendEmpNo, openUrl, pushModel, workID,
				null, null);
	}

	public static void Port_SendMessage(String sendToEmpNo, String smsDoc, String emailTitle, String msgType,
			String msgGroupFlag, String sendEmpNo, String openUrl, String pushModel, long workID, String msgPK,
			String atParas) throws Exception {
		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(sendToEmpNo);
		SMS sms = new SMS();
		if (DataType.IsNullOrEmpty(msgPK) == false) {
			/* 如果有唯一标志,就判断是否有该数据，没有该数据就允许插入. */
			if (sms.IsExit(SMSAttr.MyPK, msgPK) == true) {
				return;
			}
			sms.setMyPK(msgPK);
		} else {
			sms.setMyPK(DBAccess.GenerGUID());
		}

		sms.setHisEmailSta(MsgSta.UnRun);
		sms.setHisMobileSta(MsgSta.UnRun);

		if (sendEmpNo == null) {
			sms.setSender(WebUser.getNo());
		} else {
			sms.setSender(sendEmpNo);
		}

		// 发送给人员ID , 有可能这个人员空的.
		sms.setSendToEmpNo(sendToEmpNo);

		/// 邮件信息
		// 邮件地址.
		sms.setEmail(emp.getEmail());
		// 邮件标题.
		sms.setTitle(emailTitle);
		sms.setDocOfEmail(smsDoc);

		/// 邮件信息

		/// 短消息信息
		sms.setMobile(emp.getTel());
		sms.setMobileInfo(smsDoc);
		sms.setTitle(emailTitle);

		/// 短消息信息

		// 其他属性.
		sms.setRDT(DataType.getCurrentDataTime());

		sms.setMsgType(msgType); // 消息类型.

		sms.setMsgFlag(msgGroupFlag); // 消息分组标志,用于批量删除.

		sms.setAtPara(atParas);

		sms.setWorkID(workID);

		sms.SetPara("OpenUrl", openUrl);
		sms.SetPara("PushModel", pushModel);

		// 先保留本机一份.
		sms.Insert();
	}

	/**
	 * 获取最新的消息
	 * 
	 * @param userNo
	 *            用户编号
	 * @param dateLastTime
	 *            上次获取的时间
	 * @return 返回消息：返回两个列的数据源MsgType,Num.
	 */
	public static DataTable Port_GetNewMsg(String userNo, String dateLastTime) {
		Paras ps = new Paras();
		ps.SQL = "SELECT MsgType , Count(*) as Num FROM Sys_SMS WHERE SendTo=" + SystemConfig.getAppCenterDBVarStr()
				+ "SendTo AND RDT >" + SystemConfig.getAppCenterDBVarStr() + "RDT Group By MsgType";
		ps.Add(bp.wf.SMSAttr.SendTo, userNo);
		ps.Add(bp.wf.SMSAttr.RDT, dateLastTime);
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		return dt;
	}

	/**
	 * 获取最新的消息
	 * 
	 * @param userNo
	 *            用户编号
	 * @return
	 */
	public static DataTable Port_GetNewMsg(String userNo) {
		Paras ps = new Paras();
		ps.SQL = "SELECT MsgType , Count(*) as Num FROM Sys_SMS WHERE SendTo=" + SystemConfig.getAppCenterDBVarStr()
				+ "SendTo  Group By MsgType";
		ps.Add(bp.wf.SMSAttr.SendTo, userNo);
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		return dt;
	}

	/// 登陆接口

	/// 与流程有关的接口
	/**
	 * 写入日志
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeFrom
	 *            节点从
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            FID
	 * @param msg
	 *            信息
	 * @param at
	 *            活动类型
	 * @param tag
	 *            参数:用@符号隔开比如, @PWorkID=101@PFlowNo=003
	 * @param cFlowInfo
	 *            子流程信息
	 * @throws Exception
	 */

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg, String empNoTo, String empNameTo,
			String empNoFrom, String empNameFrom, String rdt) throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,writeImg, optionMsg, empNoTo,
				empNameTo, empNoFrom, empNameFrom, rdt, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg, String empNoTo, String empNameTo,
			String empNoFrom, String empNameFrom) throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,writeImg, optionMsg, empNoTo,
				empNameTo, empNoFrom, empNameFrom, null, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg, String empNoTo, String empNameTo,
			String empNoFrom) throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,writeImg, optionMsg, empNoTo,
				empNameTo, empNoFrom, null, null, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg, String empNoTo, String empNameTo)
					throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,writeImg, optionMsg, empNoTo,
				empNameTo, null, null, null, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg, String empNoTo) throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,writeImg, optionMsg, empNoTo, null,
				null, null, null, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg) throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,writeImg, optionMsg, null, null, null,
				null, null, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo) throws Exception {
		WriteTrack(flowNo, nodeFromID, nodeFromName, workid, fid, msg, at, tag, cFlowInfo,null, null, null, null, null, null,
				null, null);
	}

	public static void WriteTrack(String flowNo, int nodeFromID, String nodeFromName, long workid, long fid, String msg,
			ActionType at, String tag, String cFlowInfo,String writeImg, String optionMsg, String empNoTo, String empNameTo,
			String empNoFrom, String empNameFrom, String rdt, String fwcView) throws Exception {
		if (at == ActionType.CallChildenFlow) {
			if (DataType.IsNullOrEmpty(cFlowInfo) == true) {
				throw new RuntimeException("@必须输入信息cFlowInfo信息,在 CallChildenFlow 模式下.");
			}
		}

		if (DataType.IsNullOrEmpty(optionMsg)) {
			optionMsg = Track.GetActionTypeT(at);
		}

		Track t = new Track();
		t.setWorkID(workid);
		t.setFID(fid);

		// 记录日期.
		if (DataType.IsNullOrEmpty(rdt)) {
			t.setRDT(DataType.getCurrentDataTimess());
		} else {
			t.setRDT(rdt);
		}

		t.setHisActionType(at);
		t.setActionTypeText(optionMsg);

		t.setNDFrom(nodeFromID);
		t.setNDFromT(nodeFromName);

		if (empNoFrom == null) {
			t.setEmpFrom(WebUser.getNo());
		} else {
			t.setEmpFrom(empNoFrom);
		}

		if (empNameFrom == null) {
			t.setEmpFromT(WebUser.getName());
		} else {
			t.setEmpFromT(empNameFrom);
		}

		t.FK_Flow = flowNo;

		t.setNDTo(nodeFromID);
		t.setNDToT(nodeFromName);

		if (empNoTo == null) {
			t.setEmpTo(WebUser.getNo());
			t.setEmpToT(WebUser.getName());
		} else {
			t.setEmpTo(empNoTo);
			t.setEmpToT(empNameTo);
		}
		t.WriteDB=writeImg;
		t.setMsg(msg);
		t.setNodeData("@DeptNo=" + WebUser.getFK_Dept() + "@DeptName=" + WebUser.getFK_DeptName());

		if (tag != null) {
			t.setTag(tag);
		}
		if (fwcView != null) {
			t.setTag(t.getTag() + fwcView);
		}

		try {
			t.Insert();
		} catch (java.lang.Exception e) {
			t.CheckPhysicsTable();
			t.Insert();
		}

		/// 特殊判断.
		if (at == ActionType.CallChildenFlow) {
			/* 如果是吊起子流程，就要向它父流程信息里写数据，让父流程可以看到能够发起那些流程数据。 */
			AtPara ap = new AtPara(tag);
			bp.wf.GenerWorkFlow gwf = new GenerWorkFlow(ap.GetValInt64ByKey(GenerWorkFlowAttr.PWorkID));
			t.setWorkID(gwf.getWorkID());

			t.setNDFrom(gwf.getFK_Node());
			t.setNDFromT(gwf.getNodeName());

			t.setNDTo(t.getNDFrom());
			t.setNDToT(t.getNDFromT());

			t.FK_Flow = gwf.getFK_Flow();

			t.setHisActionType(ActionType.StartChildenFlow);
			t.setTag("@CWorkID=" + workid + "@CFlowNo=" + flowNo);
			t.setMsg(cFlowInfo);
			t.Insert();
		}

		/// 特殊判断.
	}

	/**
	 * 写入日志
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeFrom
	 *            节点从
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            fID
	 * @param msg
	 *            信息
	 * @throws Exception
	 */
	public static void WriteTrackInfo(String flowNo, int nodeFrom, String ndFromName, long workid, long fid, String msg,
			String optionMsg) throws Exception {
		WriteTrack(flowNo, nodeFrom, ndFromName, workid, fid, msg, ActionType.Info, null, null,null, optionMsg);
	}

	/**
	 * 写入工作审核日志:
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param currNodeID
	 *            当前节点ID
	 * @param workid
	 *            工作ID
	 * @param FID
	 *            FID
	 * @param msg
	 *            审核信息
	 * @param optionName
	 *            操作名称(比如:科长审核、部门经理审批),如果为空就是"审核".
	 * @throws Exception
	 */

	public static void WriteTrackWorkCheck(String flowNo, int currNodeID, long workid, long fid, String msg,
			String optionName) throws Exception {
		WriteTrackWorkCheck(flowNo, currNodeID, workid, fid, msg, optionName, null);
	}

	public static void WriteTrackWorkCheck(String flowNo, int currNodeID, long workid, long fid, String msg,
										   String optionName,String writeImg ) throws Exception {
		WriteTrackWorkCheck(flowNo, currNodeID, workid, fid, msg, optionName, writeImg,null);
	}


	public static void WriteTrackWorkCheck(String flowNo, int currNodeID, long workid, long fid, String msg,
			String optionName,String writeImg, String fwcView) throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();

		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		gwf.RetrieveFromDBSources();

		// 求主键 2017.10.6以前的逻辑.
		String tag = currNodeID + "_" + workid + "_" + fid + "_" + WebUser.getNo();

		// 求当前是否是会签. zhangsan,张三;李四;王五;
		String nodeName = gwf.getNodeName();
		Node nd = new Node(currNodeID);
		if (nd.getIsStartNode() == false) {
			if (gwf.getHuiQianTaskSta() == HuiQianTaskSta.HuiQianing
					&& !gwf.getHuiQianZhuChiRen().equals(WebUser.getNo())) {
				nodeName = nd.getName() + "(会签)";
			}
		}

		// 待办抢办模式，一个节点只能有一条记录.
		Paras ps = new Paras();
		if (nd.getTodolistModel() == TodolistModel.QiangBan || nd.getTodolistModel() == TodolistModel.Sharing) {
			// 先删除其他人员写入的数据. 此脚本是2016.11.30号的,为了解决柳州的问题，需要扩展.
			ps.SQL = "DELETE FROM ND" + Integer.parseInt(flowNo) + "Track WHERE  WorkID=" + dbStr
					+ "WorkID  AND NDFrom=" + dbStr + "NDFrom AND ActionType=" + ActionType.WorkCheck.getValue();
			ps.Add(TrackAttr.WorkID, workid);
			ps.Add(TrackAttr.NDFrom, currNodeID);
			DBAccess.RunSQL(ps);

			// 写入日志.
			WriteTrack(flowNo, currNodeID, nodeName, workid, fid, msg, ActionType.WorkCheck, tag, null, writeImg,optionName,
					null, null, null, null, null, fwcView);
			return;
		}

		// @sly
		String trackTable = "ND" + Integer.parseInt(flowNo) + "Track";
		ps.SQL = "UPDATE  " + trackTable + " SET NDFromT=" + dbStr + "NDFromT, Msg=" + dbStr
				+ "Msg, RDT=" + dbStr + "RDT,NodeData=" + dbStr + "NodeData WHERE ActionType="
				+ ActionType.WorkCheck.getValue() + " AND  NDFrom=" + dbStr + "NDFrom AND  NDTo=" + dbStr
				+ "NDTo AND WorkID=" + dbStr + "WorkID AND FID=" + dbStr + "FID AND EmpFrom=" + dbStr + "EmpFrom";
		ps.Add(TrackAttr.NDFromT, nodeName);
		ps.Add(TrackAttr.Msg, msg);
		ps.Add(TrackAttr.NDFrom, currNodeID);
		ps.Add(TrackAttr.NDTo, currNodeID);
		ps.Add(TrackAttr.WorkID, workid);
		ps.Add(TrackAttr.FID, fid);
		ps.Add(TrackAttr.EmpFrom, WebUser.getNo());
		ps.Add(TrackAttr.RDT, DataType.getCurrentDataTimess()); // @sly
		ps.Add(TrackAttr.NodeData, "@DeptNo=" + WebUser.getFK_Dept() + "@DeptName=" + WebUser.getFK_DeptName());

		int num = DBAccess.RunSQL(ps);
		if (num == 1 && DataType.IsNullOrEmpty(writeImg) == false && writeImg.contains("data:image/png;base64,") == true)
		{
			String myPK = DBAccess.RunSQLReturnStringIsNull("SELECT MyPK From " + trackTable + " Where ActionType=" + ActionType.WorkCheck.getValue() + " AND  NDFrom=" + currNodeID + " AND  NDTo=" + currNodeID + " AND WorkID=" + workid + " AND FID=" + fid + " AND EmpFrom='" + WebUser.getNo() + "'", "");
			DBAccess.SaveBigTextToDB(writeImg, trackTable, "MyPK", myPK, "WriteDB");
		}

		if (num > 1) {
			ps.clear();
			ps.SQL = "DELETE FROM ND" + Integer.parseInt(flowNo) + "Track WHERE  NDFrom=" + dbStr + "NDFrom AND  NDTo="
					+ dbStr + "NDTo AND WorkID=" + dbStr + "WorkID AND FID=" + dbStr + "FID AND EmpFrom=" + dbStr
					+ "EmpFrom";
			ps.Add(TrackAttr.NDFrom, currNodeID);
			ps.Add(TrackAttr.NDTo, currNodeID);
			ps.Add(TrackAttr.WorkID, workid);
			ps.Add(TrackAttr.FID, fid);
			ps.Add(TrackAttr.EmpFrom, WebUser.getNo());
			DBAccess.RunSQL(ps);
			num = 0;
		}

		if (num == 0) {
			// 如果没有更新到，就写入.
			WriteTrack(flowNo, currNodeID, nodeName, workid, fid, msg, ActionType.WorkCheck, tag, null,writeImg, optionName,
					null, null, null, null, null, fwcView);
		}
	}

	public static void WriteTrackWorkCheckForTangRenYiYao(String flowNo, int currNodeID, long workid, long fid,
			String msg, String optionName) throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();

		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		gwf.RetrieveFromDBSources();

		// 求主键 2017.10.6以前的逻辑.
		String tag = gwf.getParasLastSendTruckID() + "_" + currNodeID + "_" + workid + "_" + fid + "_"
				+ WebUser.getNo();

		String nodeName = gwf.getNodeName();
		if (gwf.getTodoEmps().contains(WebUser.getNo() + ",") == false) {
			nodeName = nodeName + "(会签)";
		}

		Node nd = new Node(currNodeID);
		// 待办抢办模式，一个节点只能有一条记录.
		Paras ps = new Paras();
		if (nd.getTodolistModel() == TodolistModel.QiangBan || nd.getTodolistModel() == TodolistModel.Sharing) {
			// 先删除其他人员写入的数据. 此脚本是2016.11.30号的,为了解决柳州的问题，需要扩展.
			ps.SQL = "DELETE FROM ND" + Integer.parseInt(flowNo) + "Track WHERE  WorkID=" + dbStr
					+ "WorkID  AND NDFrom=" + dbStr + "NDFrom AND ActionType=" + ActionType.WorkCheck.getValue()
					+ " AND Tag LIKE '" + gwf.getParasLastSendTruckID() + "%'";
			ps.Add(TrackAttr.WorkID, workid);
			ps.Add(TrackAttr.NDFrom, currNodeID);
			DBAccess.RunSQL(ps);

			//// 先删除其他人员写入的数据.
			//// string sql = "DELETE FROM ND" + int.Parse(flowNo) + "Track
			//// WHERE Tag LIKE '" + gwf.Paras_LastSendTruckID + "%' AND
			//// EmpFrom='"+WebUser.getNo()+"' ";
			// string sql = "DELETE FROM ND" + int.Parse(flowNo) + "Track WHERE
			//// Tag LIKE '" + gwf.Paras_LastSendTruckID + "%'";
			// DBAccess.RunSQL(ps);
			// 写入日志
			WriteTrack(flowNo, currNodeID, nodeName, workid, fid, msg, ActionType.WorkCheck, tag, null,null, optionName);
		} else {
			ps.SQL = "UPDATE  ND" + Integer.parseInt(flowNo) + "Track SET NDFromT=" + dbStr + "NDFromT, Msg=" + dbStr
					+ "Msg,RDT=" + dbStr + "RDT WHERE  Tag=" + dbStr + "Tag";
			ps.Add(TrackAttr.NDFromT, nodeName);
			ps.Add(TrackAttr.Msg, msg);
			ps.Add(TrackAttr.Tag, tag);
			ps.Add(TrackAttr.RDT, DataType.getCurrentDataTimess());
			if (DBAccess.RunSQL(ps) == 0) {
				// 如果没有更新到，就写入.
				WriteTrack(flowNo, currNodeID, nodeName, workid, fid, msg, ActionType.WorkCheck, tag, null, optionName,
						null, null);
			}
		}
	}

	/**
	 * 写入日志组件
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeFrom
	 * @param workid
	 * @param fid
	 * @param msg
	 * @param optionName
	 * @throws Exception
	 */
	public static void WriteTrackDailyLog(String flowNo, int nodeFrom, String nodeFromName, long workid, long fid,
			String msg, String optionName) throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		String today = DataType.getCurrentDate();

		Paras ps = new Paras();
		ps.SQL = "UPDATE  ND" + Integer.parseInt(flowNo) + "Track SET Msg=" + dbStr + "Msg WHERE  RDT LIKE '" + today
				+ "%' AND WorkID=" + dbStr + "WorkID  AND NDFrom=" + dbStr + "NDFrom AND EmpFrom=" + dbStr
				+ "EmpFrom AND ActionType=" + ActionType.WorkCheck.getValue();
		ps.Add(TrackAttr.Msg, msg);
		ps.Add(TrackAttr.WorkID, workid);
		ps.Add(TrackAttr.NDFrom, nodeFrom);
		ps.Add(TrackAttr.EmpFrom, WebUser.getNo());
		if (DBAccess.RunSQL(ps) == 0) {
			// 如果没有更新到，就写入.
			WriteTrack(flowNo, nodeFrom, nodeFromName, workid, fid, msg, ActionType.WorkCheck, null, null,null, optionName);
		}
	}

	/**
	 * 写入周报组件 一旦写入数据,只可更新 每周一次 qin
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeFrom
	 * @param workid
	 * @param fid
	 * @param msg
	 * @param optionName
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static void WriteTrackWeekLog(String flowNo, int nodeFrom, String nodeFromName, long workid, long fid,
			String msg, String optionName) throws NumberFormatException, Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();

		Date dTime = new Date();
		Date startWeek = DateUtils.addDay(dTime,
				(1 - Integer.parseInt(String.format("%d", DateUtils.dayForWeek(dTime))))); // 本周第一天

		Hashtable ht = new Hashtable(); // 当前日期所属的week包含哪些日期
		for (int i = 1; i < 7; i++) {
			ht.put(i + 1, DateUtils.parse(DateUtils.addDay(startWeek, i), "yyyy-MM-dd"));
		}
		ht.put(1, DateUtils.parse(startWeek, "yyyy-MM-dd"));

		boolean isExitWeek = false; // 本周是否已经有插入数据
		String insertDate = null;
		DataTable dt;
		String sql = null;

		for (Object de : ht.entrySet()) {
			sql = "SELECT * FROM ND" + Integer.parseInt(flowNo) + "Track  WHERE  RDT LIKE '" + ht.get(de).toString()
					+ "%' AND WorkID=" + workid + "  AND NDFrom='" + nodeFrom + "' AND EmpFrom='" + WebUser.getNo()
					+ "' AND ActionType=" + ActionType.WorkCheck.getValue();

			if (DBAccess.RunSQLReturnCOUNT(sql) != 0) {
				isExitWeek = true;
				insertDate = ht.get(de).toString();
				break;
			}
		}

		// 如果本周已经插入了记录，那么更新
		if (isExitWeek) {
			Paras ps = new Paras();
			ps.SQL = "UPDATE  ND" + Integer.parseInt(flowNo) + "Track SET RDT='"
					+ DateUtils.parse(new Date(), "yyyy-MM-dd HH:mm:ss") + "',Msg=" + dbStr + "Msg WHERE  RDT LIKE '"
					+ insertDate + "%' AND WorkID=" + dbStr + "WorkID  AND NDFrom=" + dbStr + "NDFrom AND EmpFrom="
					+ dbStr + "EmpFrom AND ActionType=" + ActionType.WorkCheck.getValue();
			ps.Add(TrackAttr.Msg, msg);
			ps.Add(TrackAttr.WorkID, workid);
			ps.Add(TrackAttr.NDFrom, nodeFrom);
			ps.Add(TrackAttr.EmpFrom, WebUser.getNo());

			DBAccess.RunSQL(ps);
		} else {
			WriteTrack(flowNo, nodeFrom, nodeFromName, workid, fid, msg, ActionType.WorkCheck, null, null,null, optionName);
		}
	}

	/**
	 * 写入月报组件 同周报一样每月一条记录 qin
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeFrom
	 * @param workid
	 * @param fid
	 * @param msg
	 * @param optionName
	 * @throws Exception
	 */
	public static void WriteTrackMonthLog(String flowNo, int nodeFrom, String nodeFromName, long workid, long fid,
			String msg, String optionName) throws Exception {
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		String today = DataType.getCurrentDate();

		Date dTime = new Date();
		Date startDay = DateUtils.addDay(dTime, 1 - DateUtils.getDayOfMonth(dTime)); // 本月第一天

		int days = DateUtils.getDayOfMonth(startDay);
		Hashtable ht = new Hashtable();

		for (int i = 1; i < days; i++) {
			ht.put(i + 1, DateUtils.parse(DateUtils.addDay(startDay, i), "yyyy-MM-dd"));
		}
		ht.put(1, DateUtils.parse(startDay, "yyyy-MM-dd"));

		boolean isExitMonth = false; // 本月是否已经有插入数据
		String insertDate = null;
		DataTable dt;
		String sql = null;

		for (Object de : ht.entrySet()) {
			sql = "SELECT * FROM ND" + Integer.parseInt(flowNo) + "Track  WHERE  RDT LIKE '" + ht.get(de).toString()
					+ "%' AND WorkID=" + workid + "  AND NDFrom='" + nodeFrom + "' AND EmpFrom='" + WebUser.getNo()
					+ "' AND ActionType=" + ActionType.WorkCheck.getValue();

			if (DBAccess.RunSQLReturnCOUNT(sql) != 0) {
				isExitMonth = true;
				insertDate = ht.get(de).toString();
				break;
			}
		}

		if (isExitMonth) {
			Paras ps = new Paras();
			ps.SQL = "UPDATE  ND" + Integer.parseInt(flowNo) + "Track SET RDT='"
					+ DateUtils.parse(new Date(), "yyyy-MM-dd HH:mm:ss") + "' Msg=" + dbStr + "Msg WHERE  RDT LIKE '"
					+ insertDate + "%' AND WorkID=" + dbStr + "WorkID  AND NDFrom=" + dbStr + "NDFrom AND EmpFrom="
					+ dbStr + "EmpFrom AND ActionType=" + ActionType.WorkCheck.getValue();
			ps.Add(TrackAttr.Msg, msg);
			ps.Add(TrackAttr.WorkID, workid);
			ps.Add(TrackAttr.NDFrom, nodeFrom);
			ps.Add(TrackAttr.EmpFrom, WebUser.getNo());

			DBAccess.RunSQL(ps);
		} else {
			WriteTrack(flowNo, nodeFrom, nodeFromName, workid, fid, msg, ActionType.WorkCheck, null, null,null, optionName);
		}
	}

	/**
	 * 修改审核信息标识 比如：在默认的情况下是"审核"，现在要把ActionTypeText 修改成"组长审核。"。
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param nodeFrom
	 *            节点ID
	 * @param label
	 *            要修改成的标签
	 * @return 是否成功
	 */
	public static boolean WriteTrackWorkCheckLabel(String flowNo, long workid, int nodeFrom, String label) {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "SELECT MyPK FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND WorkID=" + workid
				+ " And NDTo='0' ORDER BY RDT DESC ";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			return false;
		}

		String pk = dt.Rows.get(0).getValue(0).toString();
		sql = "UPDATE " + table + " SET " + TrackAttr.ActionTypeText + "='" + label + "' WHERE MyPK=" + pk;
		DBAccess.RunSQL(sql);
		return true;
	}

	/**
	 * 前进,获取等标签 比如：在默认的情况下是"逻辑删除"，现在要把ActionTypeText 修改成"删除(清场)。"。
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param nodeFrom
	 *            节点ID
	 * @param label
	 *            要修改成的标签
	 * @return 是否成功
	 */
	public static boolean WriteTrackLabel(String flowNo, long workid, int nodeFrom, String label) {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "SELECT MyPK FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND WorkID=" + workid
				+ "  ORDER BY RDT DESC ";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			return false;
		}

		String pk = dt.Rows.get(0).getValue(0).toString();
		sql = "UPDATE " + table + " SET " + TrackAttr.ActionTypeText + "='" + label + "' WHERE MyPK=" + pk;
		DBAccess.RunSQL(sql);
		return true;
	}

	/**
	 * 获取Track 表中的审核的信息
	 * 
	 * @param flowNo
	 * @param workId
	 * @param nodeFrom
	 * @return
	 * @throws Exception
	 */

	public static String GetCheckInfo(String flowNo, long workId, int nodeFrom) throws Exception {
		return GetCheckInfo(flowNo, workId, nodeFrom, null);
	}

	public static String GetCheckInfo(String flowNo, long workId, int nodeFrom, String isNullAsVal) throws Exception {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "SELECT Msg FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND ActionType="
				+ ActionType.WorkCheck.getValue() + " AND EmpFrom='" + WebUser.getNo() + "' AND WorkID=" + workId
				+ " ORDER BY RDT DESC ";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			if (isNullAsVal == null) {
				return bp.wf.Glo.getDefValWFNodeFWCDefInfo();
			} else {
				return isNullAsVal;
			}
		}
		String checkinfo = dt.Rows.get(0).getValue(0).toString();
		if (DataType.IsNullOrEmpty(checkinfo)) {
			if (isNullAsVal == null) {
				return bp.wf.Glo.getDefValWFNodeFWCDefInfo();
			} else {
				return isNullAsVal;
			}
		}

		return checkinfo;
	}

	public static String GetCheckTag(String flowNo, long workId, int nodeFrom, String empFrom) {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "SELECT Tag FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND ActionType="
				+ ActionType.WorkCheck.getValue() + " AND EmpFrom='" + empFrom + "' AND WorkID=" + workId
				+ " ORDER BY RDT DESC ";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			return "";
		}
		String checkinfo = dt.Rows.get(0).getValue(0).toString();
		if (DataType.IsNullOrEmpty(checkinfo)) {
			return "";
		}

		return checkinfo;
	}

	/**
	 * 获取队列节点Track 表中的审核的信息(队列节点中处理人 共享同一处理意见)
	 * 
	 * @param flowNo
	 * @param workId
	 * @param nodeFrom
	 * @return
	 */
	public static String GetOrderCheckInfo(String flowNo, long workId, int nodeFrom) {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "SELECT Msg FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND ActionType="
				+ ActionType.WorkCheck.getValue() + " AND WorkID=" + workId + " ORDER BY RDT DESC ";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			// bp.sys.FrmWorkCheck fwc = new FrmWorkCheck(nodeFrom);
			// return fwc.FWCDefInfo;
			return null;
		}
		String checkinfo = dt.Rows.get(0).getValue(0).toString();
		return checkinfo;
	}

	/**
	 * 删除审核信息,用于退回后.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workId
	 *            工作ID
	 * @param nodeFrom
	 *            节点从
	 * @return 删除自己的审核信息
	 * @throws Exception
	 */
	public static void DeleteCheckInfo(String flowNo, long workId, int nodeFrom) throws Exception {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "DELETE FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND ActionType="
				+ ActionType.WorkCheck.getValue() + " AND EmpFrom='" + WebUser.getNo() + "' AND WorkID=" + workId;
		DBAccess.RunSQL(sql);
	}

	public static String GetAskForHelpReInfo(String flowNo, long workId, int nodeFrom) throws Exception {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";
		String sql = "SELECT Msg FROM " + table + " WHERE NDFrom=" + nodeFrom + " AND ActionType="
				+ ActionType.AskforHelp.getValue() + " AND EmpFrom='" + WebUser.getNo() + "' AND WorkID=" + workId
				+ " ORDER BY RDT DESC ";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			return "";
		}

		String checkinfo = dt.Rows.get(0).getValue(0).toString();
		return checkinfo;
	}

	/**
	 * 更新Track信息
	 * 
	 * @param flowNo
	 * @param workId
	 * @param nodeFrom
	 * @param actionType
	 * @param strMsg
	 * @return
	 */
	public static void SetTrackInfo(String flowNo, long workId, int nodeFrom, int actionType, String strMsg) {
		String table = "ND" + Integer.parseInt(flowNo) + "Track";

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE " + table + " SET Msg=" + dbstr + "Msg  WHERE ActionType=" + dbstr + "ActionType and WorkID="
				+ dbstr + "WorkID and NDFrom=" + dbstr + "NDFrom";
		ps.Add("Msg", strMsg);
		ps.Add("ActionType", actionType);
		ps.Add("WorkID", workId);
		ps.Add("NDFrom", nodeFrom);
		DBAccess.RunSQL(ps);
	}

	/**
	 * 设置BillNo信息
	 * 
	 * @param flowNo
	 * @param workID
	 * @param newBillNo
	 * @throws Exception
	 */
	public static void SetBillNo(String flowNo, long workID, String newBillNo) throws Exception {
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkFlow SET BillNo=" + dbstr + "BillNo  WHERE WorkID=" + dbstr + "WorkID";
		ps.Add("BillNo", newBillNo);
		ps.Add("WorkID", workID);
		DBAccess.RunSQL(ps);

		Flow fl = new Flow(flowNo);
		ps = new Paras();
		ps.SQL = "UPDATE " + fl.getPTable() + " SET BillNo=" + dbstr + "BillNo WHERE OID=" + dbstr + "OID";
		ps.Add("BillNo", newBillNo);
		ps.Add("OID", workID);
		DBAccess.RunSQL(ps);
	}

	/**
	 * 设置父流程信息
	 * 
	 * @param subFlowNo
	 *            子流程编号
	 * @param subFlowWorkID
	 *            子流程workid
	 * @param parentWorkID
	 *            父流程WorkID
	 * @throws Exception
	 */

	public static void SetParentInfo(String subFlowNo, long subFlowWorkID, long parentWorkID, String parentEmpNo)
			throws Exception {
		SetParentInfo(subFlowNo, subFlowWorkID, parentWorkID, parentEmpNo, 0);
	}

	public static void SetParentInfo(String subFlowNo, long subFlowWorkID, long parentWorkID) throws Exception {
		SetParentInfo(subFlowNo, subFlowWorkID, parentWorkID, null, 0);
	}

	public static void SetParentInfo(String subFlowNo, long subFlowWorkID, long parentWorkID, String parentEmpNo,
			int parentNodeID) throws Exception {
		// 创建父流程.
		GenerWorkFlow pgwf = new GenerWorkFlow(parentWorkID);

		if (parentNodeID != 0) {
			pgwf.setFK_Node(parentNodeID);
		}

		if (parentEmpNo == null) {
			parentEmpNo = WebUser.getNo();
		}

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkFlow SET PFlowNo=" + dbstr + "PFlowNo, PWorkID=" + dbstr + "PWorkID,PNodeID="
				+ dbstr + "PNodeID,PEmp=" + dbstr + "PEmp WHERE WorkID=" + dbstr + "WorkID";
		ps.Add(GenerWorkFlowAttr.PFlowNo, pgwf.getFK_Flow());
		ps.Add(GenerWorkFlowAttr.PWorkID, parentWorkID);
		ps.Add(GenerWorkFlowAttr.PNodeID, pgwf.getFK_Node());
		ps.Add(GenerWorkFlowAttr.PEmp, parentEmpNo);
		ps.Add(GenerWorkFlowAttr.WorkID, subFlowWorkID);

		DBAccess.RunSQL(ps);

		Flow fl = new Flow(subFlowNo);
		ps = new Paras();
		ps.SQL = "UPDATE " + fl.getPTable() + " SET PFlowNo=" + dbstr + "PFlowNo, PWorkID=" + dbstr + "PWorkID,PNodeID="
				+ dbstr + "PNodeID, PEmp=" + dbstr + "PEmp WHERE OID=" + dbstr + "OID";
		ps.Add(NDXRptBaseAttr.PFlowNo, pgwf.getFK_Flow());
		ps.Add(NDXRptBaseAttr.PWorkID, pgwf.getWorkID());
		ps.Add(NDXRptBaseAttr.PNodeID, pgwf.getFK_Node());
		ps.Add(NDXRptBaseAttr.PEmp, parentEmpNo);
		ps.Add(NDXRptBaseAttr.OID, subFlowWorkID);

		DBAccess.RunSQL(ps);
	}

	public static GERpt Flow_GenerGERpt(String flowNo, long workID) throws NumberFormatException, Exception {
		GERpt rpt = new GERpt("ND" + Integer.parseInt(flowNo) + "Rpt", workID);
		return rpt;
	}

	/**
	 * 产生一个新的工作
	 * 
	 * @param flowNo
	 *            流程编号
	 * @return 返回当前操作员创建的工作
	 * @throws Exception
	 */
	public static Work Flow_GenerWork(String flowNo) throws Exception {
		Flow fl = new Flow(flowNo);
		Work wk = fl.NewWork();
		wk.ResetDefaultVal();
		return wk;
	}

	/**
	 * 把流程从非正常运行状态恢复到正常运行状态 比如现在的流程的状态是，删除，挂起，现在恢复成正常运行。
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param msg
	 *            原因
	 * @return 执行信息
	 * @throws Exception
	 */
	public static void Flow_DoComeBackWorkFlow(String flowNo, long workID, String msg) throws Exception {
		WorkFlow wf = new WorkFlow(workID);
		wf.DoComeBackWorkFlow(msg);
	}

	/**
	 * 恢复已完成的流程数据到指定的节点，如果节点为0就恢复到最后一个完成的节点上去. 恢复失败抛出异常
	 * 
	 * @param flowNo
	 *            要恢复的流程编号
	 * @param workid
	 *            要恢复的workid
	 * @param backToNodeID
	 *            恢复到的节点编号，如果是0，标示回复到流程最后一个节点上去.
	 * @param note
	 *            恢复的原因，此原因会记录到日志.
	 * @throws Exception
	 */
	public static String Flow_DoRebackWorkFlow(String flowNo, long workid, int backToNodeID, String note)
			throws Exception {
		bp.wf.template.FlowSheet fs = new bp.wf.template.FlowSheet(flowNo);
		return fs.DoRebackFlowData(workid, backToNodeID, note);
	}

	/**
	 * 执行删除流程:彻底的删除流程. 清除的内容如下: 1, 流程引擎中的数据. 2, 节点数据,NDxxRpt数据. 3, 轨迹表数据.
	 * 
	 * @param workID
	 *            工作ID
	 * @param isDelSubFlow
	 *            是否要删除它的子流程
	 * @return 执行信息
	 * @throws Exception
	 */

	public static String Flow_DoDeleteFlowByReal(long workID) throws Exception {
		return Flow_DoDeleteFlowByReal(workID, false);
	}

	public static String Flow_DoDeleteFlowByReal(long workID, boolean isDelSubFlow) throws Exception {
		try {
			WorkFlow.DeleteFlowByReal(workID, isDelSubFlow);
		} catch (RuntimeException ex) {
			throw new RuntimeException("@删除前错误，" + ex.getStackTrace());
		}
		return "删除成功";
	}

	public static String Flow_DoDeleteDraft(String flowNo, long workID, boolean isDelSubFlow) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workID);
		gwf.RetrieveFromDBSources();
		if (!gwf.getStarter().equals(WebUser.getNo()) && WebUser.getIsAdmin() == false) {
			return "err@流程不是您发起的，或者您不是管理员所以您不能删除该草稿。";
		}

		// 删除流程。
		gwf.Delete();

		String dbstr = SystemConfig.getAppCenterDBVarStr();

		Paras ps = new Paras();

		Flow fl = new Flow(flowNo);
		ps = new Paras();
		ps.SQL = "DELETE FROM " + fl.getPTable() + " WHERE OID=" + dbstr + "OID";
		ps.Add("OID", workID);
		DBAccess.RunSQL(ps);

		// 删除开始节点数据.
		Node nd = fl.getHisStartNode();
		Work wk = nd.getHisWork();
		ps = new Paras();
		ps.SQL = "DELETE FROM " + wk.getEnMap().getPhysicsTable() + " WHERE OID=" + dbstr + "OID";
		ps.Add("OID", workID);
		DBAccess.RunSQL(ps);

		bp.da.Log.DefaultLogWriteLineInfo(WebUser.getName() + "删除了FlowNo 为'" + flowNo + "',workID为'" + workID + "'的数据");

		return "草稿删除成功";
	}

	/**
	 * 删除已经完成的流程 注意:它不触发事件.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param isDelSubFlow
	 *            是否删除子流程
	 * @param note
	 *            删除原因
	 * @return 删除过程信息
	 * @throws Exception
	 */
	public static String Flow_DoDeleteWorkFlowAlreadyComplete(String flowNo, long workID, boolean isDelSubFlow,
			String note) throws Exception {
		return WorkFlow.DoDeleteWorkFlowAlreadyComplete(flowNo, workID, isDelSubFlow, note);
	}

	/**
	 * 删除流程并写入日志 清除的内容如下: 1, 流程引擎中的数据. 2, 节点数据,NDxxRpt数据. 并作如下处理: 1, 保留track数据.
	 * 2, 写入流程删除记录表.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param deleteNote
	 *            删除原因
	 * @param isDelSubFlow
	 *            是否要删除它的子流程
	 * @return 执行信息
	 * @throws Exception
	 */
	public static String Flow_DoDeleteFlowByWriteLog(String flowNo, long workID, String deleteNote,
			boolean isDelSubFlow) throws Exception {
		WorkFlow wf = new WorkFlow(workID);
		return wf.DoDeleteWorkFlowByWriteLog(deleteNote, isDelSubFlow);
	}

	/**
	 * 执行逻辑删除流程:此流程并非真正的删除仅做了流程删除标记 比如:逻辑删除工单.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param msg
	 *            逻辑删除的原因
	 * @param isDelSubFlow
	 *            逻辑删除的原因
	 * @return 执行信息,执行不成功抛出异常.
	 * @throws Exception
	 */
	public static String Flow_DoDeleteFlowByFlag(String flowNo, long workID, String msg, boolean isDelSubFlow)
			throws Exception {
		WorkFlow wf = new WorkFlow(workID);
		wf.DoDeleteWorkFlowByFlag(msg);
		if (isDelSubFlow) {
			// 删除子线程
			GenerWorkFlows gwfs = new GenerWorkFlows();
			gwfs.Retrieve(GenerWorkFlowAttr.FID, workID);
			for (GenerWorkFlow item : gwfs.ToJavaList()) {
				Flow_DoDeleteFlowByFlag(item.getFK_Flow(), item.getWorkID(), "删除子流程:" + msg, false);
			}
			// 删除子流程
			gwfs = new GenerWorkFlows();
			gwfs.Retrieve(GenerWorkFlowAttr.PWorkID, workID);
			for (GenerWorkFlow item : gwfs.ToJavaList()) {
				Flow_DoDeleteFlowByFlag(item.getFK_Flow(), item.getWorkID(), "删除子流程:" + msg, false);
			}
		}
		return "删除成功";
	}

	/**
	 * 撤销删除流程 说明:如果一个流程处于逻辑删除状态,要回复正常运行状态,就执行此接口.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作流程ID
	 * @param msg
	 *            撤销删除的原因
	 * @return 执行消息,如果撤销不成功则抛出异常.
	 * @throws Exception
	 */
	public static String Flow_DoUnDeleteFlowByFlag(String flowNo, long workID, String msg) throws Exception {
		WorkFlow wf = new WorkFlow(workID);
		wf.DoUnDeleteWorkFlowByFlag(msg);
		return "撤销删除成功.";
	}

	/**
	 * 执行-撤销发送 说明:如果流程转入了下一个节点,就会执行失败,就会抛出异常.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @return 返回成功执行信息
	 * @throws Exception
	 * @throws NumberFormatException
	 */

	public static String Flow_DoUnSend(String flowNo, long workID, int unSendToNode)
			throws NumberFormatException, Exception {
		return Flow_DoUnSend(flowNo, workID, unSendToNode, 0);
	}

	public static String Flow_DoUnSend(String flowNo, long workID) throws NumberFormatException, Exception {
		return Flow_DoUnSend(flowNo, workID, 0, 0);
	}

	public static String Flow_DoUnSend(String flowNo, long workID, int unSendToNode, long fid)
			throws NumberFormatException, Exception {
		
		 if (unSendToNode==0)
         {
             //获取用户当前所在的节点
             String currNode = "";
             switch (SystemConfig.getAppCenterDBType())
             {
                 case Oracle:
                 case KingBaseR3:
         		 case KingBaseR6:
                     currNode = "SELECT FK_Node FROM (SELECT  FK_Node FROM WF_GenerWorkerlist WHERE FK_Emp='" + WebUser.getNo() + "' Order by RDT DESC ) WHERE rownum=1";
                     break;
                 case MySQL:
                 case PostgreSQL:
                     currNode = "SELECT  FK_Node FROM WF_GenerWorkerlist WHERE FK_Emp='" + WebUser.getNo() + "' Order by RDT DESC LIMIT 1";
                     break;
                 case MSSQL:
                     currNode = "SELECT TOP 1 FK_Node FROM WF_GenerWorkerlist WHERE FK_Emp='" + WebUser.getNo() + "' Order by RDT DESC";
                     break;
                 default:
                     currNode = "SELECT  FK_Node FROM WF_GenerWorkerlist WHERE FK_Emp='" + WebUser.getNo() + "' Order by RDT DESC";
                     break;
             }
             unSendToNode = DBAccess.RunSQLReturnValInt(currNode,0);
         }

		WorkUnSend unSend = new WorkUnSend(flowNo, workID, unSendToNode, fid);
		unSend.UnSendToNode = unSendToNode;

		return unSend.DoUnSend();
	}

	/**
	 * 获得当前节点上一步发送日志记录
	 * 
	 * @param WorkID
	 *            工作流程ID
	 * @param FK_Node
	 *            当前节点编号
	 * @return 上一节点发送记录
	 * @throws Exception
	 */
	public static DataTable Flow_GetPreviousNodeTrack(long WorkID, int FK_Node) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow(WorkID);
		if (gwf.RetrieveFromDBSources() == 0) {
			throw new RuntimeException("没有查询到相关业务实例");
		}

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras pas = new Paras();
		switch (SystemConfig.getAppCenterDBType()) {
		case MSSQL:
			pas.SQL = "SELECT TOP 1 MyPK,ActionType,ActionTypeText,FID,WorkID,NDFrom,NDFromT,NDTo,NDToT,EmpFrom,EmpFromT,EmpTo,EmpToT,RDT,WorkTimeSpan,Msg,NodeData,Tag,Exer FROM ND"
					+ Integer.parseInt(gwf.getFK_Flow()) + "Track WHERE WorkID=" + dbstr + "WorkID  AND NDTo=" + dbstr
					+ "NDTo AND (ActionType=1 OR ActionType=" + ActionType.Skip.getValue() + ") ORDER BY RDT DESC";
			break;
		case Oracle:
		case KingBaseR3:
		case KingBaseR6:
			pas.SQL = "SELECT MyPK,ActionType,ActionTypeText,FID,WorkID,NDFrom,NDFromT,NDTo,NDToT,EmpFrom,EmpFromT,EmpTo,EmpToT,RDT,WorkTimeSpan,Msg,NodeData,Tag,Exer FROM ND"
					+ Integer.parseInt(gwf.getFK_Flow()) + "Track  WHERE WorkID=" + dbstr + "WorkID  AND NDTo=" + dbstr
					+ "NDTo AND (ActionType=1 OR ActionType=" + ActionType.Skip.getValue()
					+ ") AND ROWNUM=1 ORDER BY RDT DESC ";
			break;
		case MySQL:
			pas.SQL = "SELECT MyPK,ActionType,ActionTypeText,FID,WorkID,NDFrom,NDFromT,NDTo,NDToT,EmpFrom,EmpFromT,EmpTo,EmpToT,RDT,WorkTimeSpan,Msg,NodeData,Tag,Exer FROM ND"
					+ Integer.parseInt(gwf.getFK_Flow()) + "Track  WHERE WorkID=" + dbstr + "WorkID AND NDTo=" + dbstr
					+ "NDTo AND (ActionType=1 OR ActionType=" + ActionType.Skip.getValue()
					+ ") ORDER BY RDT DESC limit 0,1 ";
			break;
		case PostgreSQL:
			pas.SQL = "SELECT MyPK,ActionType,ActionTypeText,FID,WorkID,NDFrom,NDFromT,NDTo,NDToT,EmpFrom,EmpFromT,EmpTo,EmpToT,RDT,WorkTimeSpan,Msg,NodeData,Tag,Exer FROM ND"
					+ Integer.parseInt(gwf.getFK_Flow()) + "Track  WHERE WorkID=" + dbstr + "WorkID AND NDTo=" + dbstr
					+ "NDTo AND (ActionType=1 OR ActionType=" + ActionType.Skip.getValue()
					+ ") ORDER BY RDT DESC limit 1 ";
			break;
		default:
			break;
		}
		pas.Add("WorkID", WorkID);
		pas.Add("NDTo", FK_Node);
		return DBAccess.RunSQLReturnTable(pas);
	}

	/**
	 * 执行冻结
	 * 
	 * @param
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param isFixSubFlows
	 *            是否冻结子流程？
	 * @param msg
	 *            冻结原因.
	 * @return 冻结的信息.
	 * @throws Exception
	 */
	public static String Flow_DoFix(long workid, boolean isFixSubFlows, String msg) throws Exception {
		String info = "";
		try {
			// 执行冻结.
			WorkFlow wf = new WorkFlow(workid);
			info = wf.DoFix(msg);
		} catch (RuntimeException ex) {
			info += ex.getMessage();
		}

		if (isFixSubFlows == false) {
			return info;
		}

		GenerWorkFlows gwfs = new GenerWorkFlows();
		gwfs.Retrieve(GenerWorkFlowAttr.PWorkID, workid);

		for (GenerWorkFlow item : gwfs.ToJavaList()) {
			try {
				// 执行冻结.
				WorkFlow wf = new WorkFlow(item.getWorkID());
				info += wf.DoFix(msg);
				GenerWorkFlows subgwfs = new GenerWorkFlows();
				subgwfs.Retrieve(GenerWorkFlowAttr.PWorkID, item.getWorkID());
				for (GenerWorkFlow subitem : subgwfs.ToJavaList()) {
					try {
						// 执行冻结.
						WorkFlow subwf = new WorkFlow(subitem.getWorkID());
						info += subwf.DoFix(msg);
					} catch (RuntimeException ex) {
						info += "err@" + ex.getMessage();
					}
				}
			} catch (RuntimeException ex) {
				info += "err@" + ex.getMessage();
			}
		}

		return info;
	}

	/**
	 * 执行解除冻结 于挂起的区别是,冻结需要有权限的人才可以执行解除冻结， 挂起是自己的工作可以挂起也可以解除挂起。
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            workid
	 * @param msg
	 *            解除原因
	 * @throws Exception
	 */
	public static String Flow_DoUnFix(long workid, String msg) throws Exception {
		// 执行冻结.
		WorkFlow wf = new WorkFlow(workid);
		return wf.DoUnFix(msg);
	}

	/**
	 * 执行流程结束 说明:正常的流程结束.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param msg
	 *            流程结束原因
	 * @return 返回成功执行信息
	 * @throws Exception
	 */

	public static String Flow_DoFlowOver(long workID, String msg) throws Exception {
		return Flow_DoFlowOver(workID, msg, 1);
	}

	public static String Flow_DoFlowOver(long workID, String msg, int stopFlowType) throws Exception {
		WorkFlow wf = new WorkFlow(workID);
		String flowNo = wf.getHisGenerWorkFlow().getFK_Flow();

		Node nd = new Node(wf.getHisGenerWorkFlow().getFK_Node());
		GERpt rpt = new GERpt("ND" + Integer.parseInt(flowNo) + "Rpt");
		rpt.setOID(workID);
		rpt.RetrieveFromDBSources();
		msg = wf.DoFlowOver(ActionType.FlowOver, msg, nd, rpt, stopFlowType);

		Work wk = nd.getHisWork();
		wk.setOID(workID);
		wk.RetrieveFromDBSources();
		WorkNode wn = new WorkNode(wk, nd);
		SendReturnObjs  objs = WorkNodePlus.SubFlowEvent(wn);
		msg += objs == null ? "" : objs.OutMessageHtml;

		return msg;
	}

	/**
	 * 流程运行完成后自动运行/结束父流程或者同级子流程
	 * 
	 * @throws Exception
	 */
	public static String FlowOverAutoSendParentOrSameLevelFlow(GenerWorkFlow gwf, Flow flow,SubFlow subFlow) throws Exception {
		// 判断当前流程是否子流程，是否启用该流程结束后，主流程自动运行到下一节点@yuan
		if (gwf.getPWorkID() != 0) {
			long slWorkID = gwf.GetParaInt("SLWorkID");
			if (slWorkID == 0) // 启动该流程的是父子流程
			{
				GenerWorkFlow pgwf = new GenerWorkFlow(gwf.getPWorkID());
				if (flow.getIsToParentNextNode() == true || subFlow.getIsAutoSendSubFlowOver() == 1) {
					if (pgwf.getFK_Node() != gwf.getPNodeID()) {
						return "";
					}

					if (pgwf.getWFState() == WFState.Complete) {
						return "";
					}

					if (bp.wf.Dev2Interface.Flow_IsCanDoCurrentWork(gwf.getPWorkID(), WebUser.getNo()) == false) {
						return "";
					}

					//判断当前流程是否是最后一个子流程. .
                    String mysql = "SELECT COUNT(WorkID) as Num FROM WF_GenerWorkFlow WHERE PWorkID=" + gwf.getPWorkID() + " AND WFSta != 1 AND WorkID !=" + gwf.getWorkID();
                    if (DBAccess.RunSQLReturnValInt(mysql, 0) != 0)
                        return "";
                    
					// 主流程自动运行到一下节点
					SendReturnObjs returnObjs = bp.wf.Dev2Interface.Node_SendWork(gwf.getPFlowNo(), gwf.getPWorkID());

					return "父流程自动运行到下一个节点," + returnObjs.ToMsgOfHtml();
				}
				// 结束父流程
				if (subFlow.getIsAutoSendSubFlowOver() == 2) {
					if (pgwf.getWFState() == WFState.Complete) {
						return "";
					}
					Flow fl = new Flow(gwf.getPFlowNo());
					String flowOver = bp.wf.Dev2Interface.Flow_DoFlowOver(gwf.getPWorkID(),
							"父流程[" + fl.getName() + "],WorkID为[" + gwf.getPWorkID() + "]成功结束");
					return flowOver;
				}

			} else // 启动的是同级子流程
			{
				String slFlowNo = gwf.GetParaString("SLFlowNo");
				int slNodeID = gwf.GetParaInt("SLNodeID");


				Flow fl = new Flow(slFlowNo);
				GenerWorkFlow subgwf = new GenerWorkFlow(slWorkID);
				if (subFlow.getIsAutoSendSLSubFlowOver() == 1) {
					if (subgwf.getFK_Node() != slNodeID) {
						return "";
					}

					// 主流程自动运行到一下节点
					SendReturnObjs returnObjs = bp.wf.Dev2Interface.Node_SendWork(slFlowNo, slWorkID);
					String sendSuccess = "同级子流程[" + fl.getName() + "]程自动运行到下一个节点，发送过程如下：\n @接收人"
							+ returnObjs.getVarAcceptersName() + "\n @下一步[" + returnObjs.getVarCurrNodeName() + "]启动";
					return sendSuccess;
				}
				// 结束父流程
				if (subFlow.getIsAutoSendSLSubFlowOver() == 2) {
					if (subgwf.getWFState() == WFState.Complete) {
						return "";
					}
					return bp.wf.Dev2Interface.Flow_DoFlowOver(slWorkID,
							"同级子流程流程[" + fl.getName() + "],WorkID为[" + slWorkID + "]成功结束");
				}

			}
		}
		return "";
	}

	/**
	 * 获得执行下一步骤的节点ID，这个功能是在流程未发送前可以预先知道 它就要到达那一个节点上去,以方便在当前节点发送前处理业务逻辑.
	 * 1,首先保证当前人员是可以执行当前节点的工作. 2,其次保证获取下一个节点只有一个.
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @return 下一步骤的所要到达的节点, 如果获取不到就会抛出异常.
	 * @throws Exception
	 */
	public static int Node_GetNextStepNode(String fk_flow, long workid) throws Exception {
		//// 检查当前人员是否可以执行当前工作.
		// if (BP.WF.Dev2Interface.Flow_CheckIsCanDoCurrentWork( workid,
		//// WebUser.getNo()) == false)
		// throw new Exception("@当前人员不能执行此节点上的工作.");

		// 获取当前nodeID.
		int currNodeID = bp.wf.Dev2Interface.Node_GetCurrentNodeID(fk_flow, workid);

		// 获取
		Node nd = new Node(currNodeID);
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.Retrieve();

		WorkNode wn = new WorkNode(wk, nd);
		if(wn.rptGe == null){
			wn.rptGe = nd.getHisFlow().getHisGERpt();
			if (wk.getFID() != 0) {
				wn.rptGe.setOID(wk.getFID());
			} else {
				wn.rptGe.setOID(workid);
			}

			wn.rptGe.RetrieveFromDBSources();
			wk.setRow(wn.rptGe.getRow());
		}
		
		return wn.NodeSend_GenerNextStepNode().getNodeID();
	}

	/**
	 * 获取指定的workid 在运行到的节点编号
	 * 
	 * @param workID
	 *            需要找到的workid
	 * @return 返回节点编号. 如果没有找到，就会抛出异常.
	 */
	public static int Flow_GetCurrentNode(long workID) {
		Paras ps = new Paras();
		ps.SQL = "SELECT FK_Node FROM WF_GenerWorkFlow WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr() + "WorkID";
		ps.Add("WorkID", workID);
		return DBAccess.RunSQLReturnValInt(ps);
	}

	/**
	 * 获取指定节点的Work
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @return 当前工作
	 * @throws Exception
	 */
	public static Work Flow_GetCurrentWork(int nodeID, long workID) throws Exception {
		Node nd = new Node(nodeID);
		Work wk = nd.getHisWork();
		wk.setOID(workID);
		wk.Retrieve();
		return wk;
	}

	/**
	 * 获取当前工作节点的Work
	 * 
	 * @param workID
	 *            工作ID
	 * @return 当前工作节点的Work
	 * @throws Exception
	 */
	public static Work Flow_GetCurrentWork(long workID) throws Exception {
		Node nd = new Node(Flow_GetCurrentNode(workID));
		Work wk = nd.getHisWork();
		wk.setOID(workID);
		wk.Retrieve();
		wk.ResetDefaultVal();
		return wk;
	}

	/**
	 * 指定 workid 当前节点由哪些人可以执行.
	 * 
	 * @param workID
	 *            需要找到的workid
	 * @return 返回当前处理人员列表,数据结构与WF_GenerWorkerList一致.
	 */
	public static DataTable Flow_GetWorkerList(long workID) {
		Paras ps = new Paras();
		ps.SQL = "SELECT * FROM WF_GenerWorkerList WHERE IsEnable=1 AND IsPass=0 AND WorkID="
				+ SystemConfig.getAppCenterDBVarStr() + "WorkID";
		ps.Add("WorkID", workID);
		return DBAccess.RunSQLReturnTable(ps);
	}

	/**
	 * 根据流程标记获得流程编号
	 * 
	 * @param flowMark
	 *            流程属性的流程标记
	 * @return 流程编号
	 */
	public static String Flow_GetFlowNoByFlowMark(String flowMark) {
		String sql = "SELECT No FROM WF_Flow WHERE FlowMark='" + flowMark + "'";
		return DBAccess.RunSQLReturnStringIsNull(sql, null);
	}

	/**
	 * 检查是否可以发起流程
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param userNo
	 *            用户编号
	 * @return 是否可以发起当前流程
	 * @throws Exception
	 */

	public static boolean Flow_IsCanStartThisFlow(String flowNo, String userNo, String pFlowNo, int pNodeID)
			throws Exception {
		return Flow_IsCanStartThisFlow(flowNo, userNo, pFlowNo, pNodeID, 0);
	}

	public static boolean Flow_IsCanStartThisFlow(String flowNo, String userNo, String pFlowNo) throws Exception {
		return Flow_IsCanStartThisFlow(flowNo, userNo, pFlowNo, 0, 0);
	}

	public static boolean Flow_IsCanStartThisFlow(String flowNo, String userNo) throws Exception {
		return Flow_IsCanStartThisFlow(flowNo, userNo, null, 0, 0);
	}

	public static boolean Flow_IsCanStartThisFlow(String flowNo, String userNo, String pFlowNo, int pNodeID,
			long pworkID) throws Exception {

		/// 判断开始节点是否可以发起.
		Node nd = new Node(Integer.parseInt(flowNo + "01"));
		if (nd.getIsGuestNode() == true) {
			if (!WebUser.getNo().equals("Guest")) {
				throw new RuntimeException("@当前节点是来宾处理节点，但是目前您{" + WebUser.getNo() + "}不是来宾帐号。");
			}
			return true;
		}

		Paras ps = new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		int num = 0;

		switch (nd.getHisDeliveryWay()) {
		case ByStation:
		case ByStationOnly:
			ps.SQL = "SELECT COUNT(A.FK_Node) as Num FROM WF_NodeStation A, " + bp.wf.Glo.getEmpStation()
					+ " B WHERE A.FK_Station= B.FK_Station AND  A.FK_Node=" + dbstr + "FK_Node AND B.FK_Emp=" + dbstr
					+ "FK_Emp";
			ps.Add("FK_Node", nd.getNodeID());
			ps.Add("FK_Emp", userNo);
			num = DBAccess.RunSQLReturnValInt(ps);
			break;
		case ByTeamOrgOnly:
		case ByTeamDeptOnly:
			ps.SQL = "SELECT COUNT(A.FK_Node) as Num FROM WF_NodeTeam A, Port_TeamEmp B, Port_Emp C WHERE B.FK_Emp=C.No AND A.FK_Team= B.FK_Team AND  A.FK_Node="
					+ dbstr + "FK_Node AND B.FK_Emp=" + dbstr + "FK_Emp AND C.OrgNo=" + dbstr + "OrgNo";
			ps.Add("FK_Node", nd.getNodeID());
			ps.Add("FK_Emp", userNo);
			ps.Add("OrgNo", WebUser.getOrgNo());
			num = DBAccess.RunSQLReturnValInt(ps);
			break;
		case ByTeamOnly:
			ps.SQL = "SELECT COUNT(A.FK_Node) as Num FROM WF_NodeTeam A, Port_TeamEmp B WHERE A.FK_Group= B.FK_Group AND  A.FK_Node="
					+ dbstr + "FK_Node AND B.FK_Emp=" + dbstr + "FK_Emp";
			ps.Add("FK_Node", nd.getNodeID());
			ps.Add("FK_Emp", userNo);
			num = DBAccess.RunSQLReturnValInt(ps);
			break;
		case ByDept:

			ps.SQL = "SELECT COUNT(A.FK_Node) as Num FROM WF_NodeDept A, Port_DeptEmp B WHERE A.FK_Dept= B.FK_Dept AND  A.FK_Node="
					+ dbstr + "FK_Node AND B.FK_Emp=" + dbstr + "FK_Emp";
			ps.Add("FK_Node", nd.getNodeID());
			ps.Add("FK_Emp", userNo);
			num = DBAccess.RunSQLReturnValInt(ps);

			if (num == 0) {
				ps.clear();
				ps.SQL = "SELECT COUNT(A.FK_Node) as Num FROM WF_NodeDept A, Port_Emp B WHERE A.FK_Dept= B.FK_Dept AND  A.FK_Node="
						+ dbstr + "FK_Node AND B.No=" + dbstr + "FK_Emp";
				ps.Add("FK_Node", nd.getNodeID());
				ps.Add("FK_Emp", userNo);
				num = DBAccess.RunSQLReturnValInt(ps);
			}

			break;

		case ByBindEmp:
			ps.SQL = "SELECT COUNT(*) AS Num FROM WF_NodeEmp WHERE FK_Emp=" + dbstr + "FK_Emp AND FK_Node=" + dbstr
					+ "FK_Node";
			ps.Add("FK_Emp", userNo);
			ps.Add("FK_Node", nd.getNodeID());
			num = DBAccess.RunSQLReturnValInt(ps);
			break;
		case ByDeptAndStation:

			String sql = "SELECT COUNT(A.FK_Node) as Num FROM WF_NodeDept A, Port_DeptEmp B, WF_NodeStation C, "
					+ Glo.getEmpStation() + " D";
			sql += " WHERE A.FK_Dept= B.FK_Dept AND  A.FK_Node=" + dbstr + "FK_Node AND B.FK_Emp=" + dbstr
					+ "FK_Emp AND  A.FK_Node=C.FK_Node AND C.FK_Station=D.FK_Station AND D.FK_Emp=" + dbstr + "FK_Emp";
			ps.SQL = sql;
			ps.Add("FK_Node", nd.getNodeID());
			ps.Add("FK_Emp", userNo);
			num = DBAccess.RunSQLReturnValInt(ps);
			break;
		case BySelected:
			num = 1;
			break;
		case BySelectedOrgs: // 按照绑定的组织计算.
			ps.SQL = "SELECT COUNT(*) AS Num FROM WF_FlowOrg WHERE OrgNo=" + dbstr + "OrgNo ";
			ps.Add("OrgNo", WebUser.getOrgNo());
			num = DBAccess.RunSQLReturnValInt(ps);
			break;
		default:
			throw new RuntimeException("@开始节点不允许设置此访问规则：" + nd.getHisDeliveryWay());
		}

		if (num == 0) {
			return false;
		}

		if (pFlowNo == null) {
			return true;
		}

		/// 判断开始节点是否可以发起.

		/// 检查流程发起限制规则. 为周大福项目增加判断.
		if (pNodeID == 0) {
			return true;
		}

		// 当前节点所有配置的子流程.
		SubFlowHands subflows = new SubFlowHands(pNodeID);

		// 当前的子流程.
		for (SubFlowHand item : subflows.ToJavaList()) {
			if (item.getSubFlowNo().equals(flowNo) == false) {
				continue;
			}

			if (item.getStartOnceOnly() == true) {
				String sql = "SELECT Starter, RDT FROM WF_GenerWorkFlow WHERE PWorkID=" + pworkID + " AND FK_Flow='"
						+ flowNo + "' AND WFState >=2 ";
				DataTable dt = DBAccess.RunSQLReturnTable(sql);
				if (dt.Rows.size() == 0) {
					// return true; //没有人发起，他可以发起。
				} else {
					throw new RuntimeException("该流程只能允许发起一次.");
				}
			}

			if (item.getCompleteReStart() == true) {
				String sql = "SELECT Starter, RDT,WFState FROM WF_GenerWorkFlow WHERE PWorkID=" + pworkID
						+ " AND FK_Flow='" + flowNo + "' AND WFState != 3";
				DataTable dt = DBAccess.RunSQLReturnTable(sql);
				if (dt.Rows.size() != 0) {
					if (dt.Rows.size() == 1 && Integer.parseInt(dt.Rows.get(0).getValue("WFState").toString()) == 0) {

					} else {
						throw new RuntimeException("该流程已经启动还没有运行结束，不能再次启动.");
					}

				}

			}

			if (item.getIsEnableSpecFlowStart() == true) {
				// 指定的流程发起之后，才能启动该流程。
				String[] fls = item.getSpecFlowStart().split("[,]", -1);
				for (String flStr : fls) {
					if (DataType.IsNullOrEmpty(flStr) == true) {
						continue;
					}

					String sql = "SELECT Starter, RDT FROM WF_GenerWorkFlow WHERE PWorkID=" + pworkID + " AND FK_Flow='"
							+ flStr + "' AND WFState >=2 ";
					DataTable dt = DBAccess.RunSQLReturnTable(sql);
					if (dt.Rows.size() == 0) {
						bp.wf.Flow myflow = new Flow(flStr);
						throw new RuntimeException(
								"流程:[" + myflow.getName() + "]没有发起,您不能启动[" + item.getSubFlowName() + "]。");
					}
				}
			}

			if (item.getIsEnableSpecFlowOver() == true) {
				// 指定的流程发起之后，才能启动该流程。
				String[] fls = item.getSpecFlowOver().split("[,]", -1);
				for (String flStr : fls) {
					if (DataType.IsNullOrEmpty(flStr) == true) {
						continue;
					}

					String sql = "SELECT Starter, RDT FROM WF_GenerWorkFlow WHERE PWorkID=" + pworkID + " AND FK_Flow='"
							+ flStr + "' AND WFState =3 ";
					DataTable dt = DBAccess.RunSQLReturnTable(sql);
					if (dt.Rows.size() == 0) {
						bp.wf.Flow myflow = new Flow(flStr);
						throw new RuntimeException(
								"流程:[" + myflow.getName() + "]没有完成,您不能启动[" + item.getSubFlowName() + "]。");
					}
				}
			}
		}

		/// 检查流程发起限制规则.

		/// 判断流程属性的规则.
		Flow fl = new Flow(flowNo);
		if (fl.getStartLimitRole() == StartLimitRole.None) {
			return true;
		}

		// 只有一个子流程,才能发起.
		if (fl.getStartLimitRole() == StartLimitRole.OnlyOneSubFlow) {
			if (pworkID == 0) {
				return true;
			}

			String sql = "SELECT Starter, RDT FROM WF_GenerWorkFlow WHERE PWorkID=" + pworkID + " AND FK_Flow='"
					+ fl.getNo() + "' AND WFState >=2 ";
			DataTable dt = DBAccess.RunSQLReturnTable(sql);
			if (dt.Rows.size() == 0) {
				return true;
			}

			throw new RuntimeException("该流程只能允许发起一个子流程.");
		}

		/// 判断流程属性的规则.

		return true;
	}

	/**
	 * 获得正在运行中的子流程的数量
	 * 
	 * @param workID
	 *            父流程的workid
	 * @return 获得正在运行中的子流程的数量。如果是0，表示所有的流程的子流程都已经结束。
	 */
	public static int Flow_NumOfSubFlowRuning(long pWorkID) {
		String sql = "SELECT COUNT(*) AS num FROM WF_GenerWorkFlow WHERE WFState!=" + WFState.Complete.getValue()
				+ " AND PWorkID=" + pWorkID;
		return DBAccess.RunSQLReturnValInt(sql);
	}

	/**
	 * 获得正在运行中的子流程的数量
	 * 
	 * @param pWorkID
	 *            父流程的workid
	 * @param currWorkID
	 *            不包含当前的工作节点ID
	 * @param workID
	 *            父流程的workid
	 * @return 获得正在运行中的子流程的数量。如果是0，表示所有的流程的子流程都已经结束。
	 */
	public static int Flow_NumOfSubFlowRuning(long pWorkID, long currWorkID) {
		String sql = "SELECT COUNT(*) AS num FROM WF_GenerWorkFlow WHERE WFState!=" + WFState.Complete.getValue()
				+ " AND WorkID!=" + currWorkID + " AND PWorkID=" + pWorkID;
		return DBAccess.RunSQLReturnValInt(sql);
	}

	public static boolean Flow_IsInGenerWork(long workID) {

		if (workID == 0) {
			return false;
		}

		String sql = "select * from WF_Generworkflow where WorkID='" + workID + "'";
		return DBAccess.RunSQLReturnCOUNT(sql) > 0;
	}

	/**
	 * 检查指定节点上的所有子流程是否完成？ For: 深圳熊伟.
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @return 返回该节点上的子流程是否完成？
	 */
	public static boolean Flow_CheckAllSubFlowIsOver(int nodeID, long workID) {
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "SELECT COUNT(WorkID) FROM WF_GenerWorkFlow WHERE  PNodeID=" + dbstr + "PNodeID AND PWorkID=" + dbstr
				+ "PWorkID AND WFState!=" + dbstr + "WFState ";
		ps.Add(GenerWorkFlowAttr.PNodeID, nodeID);
		ps.Add(GenerWorkFlowAttr.PWorkID, workID);
		ps.Add(GenerWorkFlowAttr.WFState, WFState.Complete.getValue());

		if (DBAccess.RunSQLReturnValInt(ps) == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查当前人员是否有权限处理当前的工作
	 * 
	 * @param workID
	 * @param userNo
	 * @return
	 * @throws Exception
	 */
	public static boolean Flow_IsCanDoCurrentWork(long workID, String userNo) throws Exception {
		if (workID == 0) {
			return true;
		}

		// 判断是否有待办.
		GenerWorkerList gwl = new GenerWorkerList();
		int inum = gwl.Retrieve(GenerWorkerListAttr.WorkID, workID, GenerWorkerListAttr.FK_Emp, userNo,
				GenerWorkerListAttr.IsPass, 0);
		if (inum >= 1) {
			return true;
		}

		GenerWorkFlow mygwf = new GenerWorkFlow(workID);

		/// 判断是否是开始节点.
		/* 判断是否是开始节点 . */
		String str = String.valueOf(mygwf.getFK_Node());
		if (str.endsWith("01") == true) {
			String mysql = "SELECT FK_Emp, IsPass FROM WF_GenerWorkerList WHERE WorkID=" + workID + " AND FK_Node="
					+ mygwf.getFK_Node();
			DataTable mydt = DBAccess.RunSQLReturnTable(mysql);
			if (mydt.Rows.size() == 0) {
				return true;
			}

			for (DataRow dr : mydt.Rows) {
				String fk_emp = dr.getValue(0).toString();
				String isPass = dr.getValue(1).toString();
				if (userNo.equals(fk_emp) && (isPass.equals("0") || isPass.equals("80") || isPass.equals("90"))) {
					return true;
				}
			}
			return false;
		}

		/// 判断是否是开始节点.

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "SELECT c.RunModel,c.IsGuestNode, a.GuestNo, a.TaskSta, a.WFState, IsPass FROM WF_GenerWorkFlow a, WF_GenerWorkerlist b, WF_Node c WHERE  b.FK_Node=c.NodeID AND a.WorkID=b.WorkID AND a.FK_Node=b.FK_Node  AND b.FK_Emp="
				+ dbstr + "FK_Emp AND (b.IsEnable=1 OR b.IsPass>=70 OR IsPass=0)   AND a.WorkID=" + dbstr + "WorkID ";
		ps.Add("FK_Emp", userNo);
		ps.Add("WorkID", workID);
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		if (dt.Rows.size() == 0) {
			//#region 判断是否有授权信息？
			Auths aths = new Auths();
			if (DataType.IsNullOrEmpty(bp.web.WebUser.getAuth()) == true)
				aths.Retrieve(AuthAttr.AutherToEmpNo, userNo);
			else
				aths.Retrieve(AuthAttr.AutherToEmpNo, userNo, AuthAttr.Auther, bp.web.WebUser.getAuth());

			if (aths.size() == 0)
				return false;
			for(Auth item : aths.ToJavaList())
			{
				ps = new Paras();
				ps.SQL = "SELECT c.RunModel,c.IsGuestNode, a.GuestNo, a.TaskSta, a.WFState, IsPass FROM WF_GenerWorkFlow a, WF_GenerWorkerlist b, WF_Node c WHERE  b.FK_Node=c.NodeID AND a.WorkID=b.WorkID AND a.FK_Node=b.FK_Node  AND b.FK_Emp=" + dbstr + "FK_Emp AND (b.IsEnable=1 OR b.IsPass>=70 OR IsPass=0)   AND a.WorkID=" + dbstr + "WorkID ";
				ps.Add("FK_Emp", item.getAuther());
				ps.Add("WorkID", workID);
				dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() == 0)
					continue;

				//判断是否是待办.
				int myisPassTemp = Integer.parseInt(dt.Rows.get(0).getValue("IsPass").toString());
				//新增加的标记,=90 就是会签主持人执行会签的状态. 翻译.
				if (myisPassTemp == 90)
					return true;
				if (myisPassTemp == 80)
					return true;
				if (myisPassTemp == 0)
					return true;
			}
            //#endregion 判断是否有授权信息？
			return false;
		}

		// 判断是否是待办.
		int myisPass = Integer.parseInt(dt.Rows.get(0).getValue("IsPass").toString());

		// 新增加的标记,=90 就是会签主持人执行会签的状态. 翻译.
		if (myisPass == 90) {
			return true;
		}

		if (myisPass == 80) {
			return true;
		}

		if (myisPass != 0) {
			return false;
		}

		WFState wfsta = WFState.forValue(Integer.parseInt(dt.Rows.get(0).getValue("WFState").toString()));
		if (wfsta == WFState.Complete) {
			return false;
		}

		if (wfsta == WFState.Delete) {
			return false;
		}

		// 判断是否是客户处理节点.
		int isGuestNode = Integer.parseInt(dt.Rows.get(0).getValue("IsGuestNode").toString());
		if (isGuestNode == 1) {
			if (dt.Rows.get(0).getValue("GuestNo").toString().equals(GuestUser.getNo())) {
				return true;
			} else {
				return false;
			}
		}

		int i = Integer.parseInt(dt.Rows.get(0).getValue(0).toString());

		RunModel rm = RunModel.forValue(i);
		switch (rm) {
		case Ordinary:
			return true;
		case FL:
			return true;
		case HL:
			return true;
		case FHL:
			return true;
		case SubThread:
			return true;
		default:
			break;
		}
		return true;
	}

	/**
	 * 检查当前人员是否有权限处理当前的工作.
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @param userNo
	 *            要判断的操作人员
	 * @return 返回指定的人员是否有操作当前工作的权限
	 * @throws Exception
	 */
	public static boolean Flow_IsCanDoCurrentWorkGuest(int nodeID, long workID, String userNo) throws Exception {
		if (workID == 0) {
			return true;
		}

		if (WebUser.getNo().equals("admin") == true) {
			return true;
		}

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		// ps.SQL="SELECT c.RunModel FROM WF_GenerWorkFlow a ,
		// WF_GenerWorkerlist b, WF_Node c WHERE a.FK_Node=" + dbstr + "FK_Node
		// AND b.FK_Node=c.NodeID AND a.WorkID=b.WorkID AND a.FK_Node=b.FK_Node
		// AND b.FK_Emp=" + dbstr + "FK_Emp AND b.IsEnable=1 AND a.workid=" +
		// dbstr + "WorkID";
		// ps.Add("FK_Node", nodeID);
		// ps.Add("FK_Emp", userNo);
		// ps.Add("WorkID", workID);
		String sql = "SELECT c.RunModel, a.TaskSta FROM WF_GenerWorkFlow a , WF_GenerWorkerlist b, WF_Node c WHERE a.FK_Node='"
				+ nodeID + "'  AND b.FK_Node=c.NodeID AND a.WorkID=b.WorkID AND a.FK_Node=b.FK_Node  AND b.GuestNo='"
				+ userNo + "' AND b.IsEnable=1 AND a.WorkID=" + workID;

		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() == 0) {
			return false;
		}

		int i = Integer.parseInt(dt.Rows.get(0).getValue(0).toString());
		TaskSta TaskStai = TaskSta.forValue(Integer.parseInt(dt.Rows.get(0).getValue(1).toString()));
		if (TaskStai == TaskSta.Sharing) {
			return false;
		}

		RunModel rm = RunModel.forValue(i);
		switch (rm) {
		case Ordinary:
			return true;
		case FL:
			return true;
		case HL:
			return true;
		case FHL:
			return true;
		case SubThread:
			return true;
		default:
			break;
		}

		if (DBAccess.RunSQLReturnValInt(ps) == 0) {
			return false;
		}

		return true;
	}

	/**
	 * 是否可以查看流程数据 用于判断是否可以查看流程轨迹图. edit: stone 2015-03-25
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            FID
	 * @return
	 * @throws Exception
	 */

	public static boolean Flow_IsCanViewTruck(String flowNo, long workid) throws Exception {
		return Flow_IsCanViewTruck(flowNo, workid, null);
	}

	public static boolean Flow_IsCanViewTruck(String flowNo, long workid, String userNo) throws Exception {
		if (userNo == null) {
			userNo = WebUser.getNo();
		}
		if (WebUser.getNo().equals("admin") == true) {
			return true;
		}

		// 先从轨迹里判断.
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "SELECT count(MyPK) as Num FROM ND" + Integer.parseInt(flowNo) + "Track WHERE (WorkID=" + dbStr
				+ "WorkID OR FID=" + dbStr + "FID) AND (EmpFrom=" + dbStr + "Emp1 OR EmpTo=" + dbStr + "Emp2)";
		ps.Add(bp.wf.TrackAttr.WorkID, workid);
		ps.Add(bp.wf.TrackAttr.FID, workid);
		ps.Add("Emp1", WebUser.getNo());
		ps.Add("Emp2", WebUser.getNo());

		if (DBAccess.RunSQLReturnValInt(ps) > 1) {
			return true;
		}

		// 在查看该流程的发起者，与当前人是否在同一个部门，如果是也返回true.
		ps = new Paras();
		ps.SQL = "SELECT FK_Dept FROM WF_GenerWorkFlow WHERE WorkID=" + dbStr + "WorkID OR WorkID=" + dbStr + "FID";
		ps.Add(bp.wf.TrackAttr.WorkID, workid);
		ps.Add(bp.wf.TrackAttr.FID, workid);

		String fk_dept = DBAccess.RunSQLReturnStringIsNull(ps, null);
		if (fk_dept == null) {
			bp.wf.Flow fl = new Flow(flowNo);
			ps.SQL = "SELECT FK_Dept FROM " + fl.getPTable() + " WHERE OID=" + dbStr + "WorkID OR OID=" + dbStr + "FID";
			fk_dept = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (fk_dept == null) {
				throw new RuntimeException("@流程引擎数据被删除.");
			}
		}
		if (fk_dept.equals(WebUser.getFK_Dept())) {
			return true;
		}

		return false;
	}

	/**
	 * 删除子线程
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            子线程的工作ID
	 * @param info
	 *            删除信息
	 * @throws Exception
	 */
	public static String Flow_DeleteSubThread(long workid, String info) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.SetValByKey(GenerWorkFlowAttr.WorkID, workid);
		if (gwf.RetrieveFromDBSources() > 0) {
			WorkFlow wf = new WorkFlow(workid);
			String msg = wf.DoDeleteWorkFlowByReal(false);

			bp.wf.Dev2Interface.WriteTrackInfo(gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getNodeName(), gwf.getFID(), 0,
					info, "删除子线程");
			return msg;
		}
		return null;
	}

	/**
	 * 执行工作催办
	 * 
	 * @param workID
	 *            工作ID
	 * @param msg
	 *            催办消息
	 * @param isPressSubFlow
	 *            是否催办子流程？
	 * @return 返回执行结果
	 * @throws Exception
	 */

	public static String Flow_DoPress(long workID, String msg) throws Exception {
		return Flow_DoPress(workID, msg, false);
	}

	public static String Flow_DoPress(long workID, String msg, boolean isPressSubFlow) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow(workID);

		/* 找到当前待办的工作人员 */
		GenerWorkerLists wls = new GenerWorkerLists(workID, gwf.getFK_Node());
		String toEmp = "", toEmpName = "";
		String mailTitle = "催办:" + gwf.getTitle() + ", 发送人:" + WebUser.getName();
		// 如果子线程找不到流转日志并且父流程编号不为空，在父流程进行查找接收人
		if (wls.size() == 0 && gwf.getFID() != 0) {
			wls = new GenerWorkerLists(gwf.getFID(), gwf.getFK_Node());
		}
		PushMsgs pms = new PushMsgs();
		pms.Retrieve(PushMsgAttr.FK_Node, gwf.getFK_Node(), PushMsgAttr.FK_Event, EventListNode.PressAfter);

		for (GenerWorkerList wl : wls.ToJavaList()) {
			if (wl.getIsEnable() == false) {
				continue;
			}

			toEmp += wl.getFK_Emp() + ",";
			toEmpName += wl.getFK_EmpText() + ",";

			// 发消息.
			for (PushMsg push : pms.ToJavaList()) {
				bp.wf.Dev2Interface.Port_SendMsg(wl.getFK_Emp(), mailTitle, msg, null, bp.wf.SMSMsgType.DoPress,
						gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getWorkID(), gwf.getFID(), push.getSMSPushModel());
			}

			wl.setPressTimes(wl.getPressTimes() + 1);
			wl.Update();

			// wl.Update(GenerWorkerListAttr.PressTimes, wl.PressTimes + 1);
		}

		// 写入日志.
		WorkNode wn = new WorkNode(workID, gwf.getFK_Node());
		wn.AddToTrack(ActionType.Press, toEmp, toEmpName, gwf.getFK_Node(), gwf.getNodeName(), msg);

		// 如果催办子流程.
		if (isPressSubFlow) {
			String subMsg = "";
			GenerWorkFlows gwfs = gwf.getHisSubFlowGenerWorkFlows();
			for (GenerWorkFlow item : gwfs.ToJavaList()) {
				subMsg += "@已经启动对子线程:" + item.getTitle() + "的催办,消息如下:";
				subMsg += Flow_DoPress(item.getWorkID(), msg, false);
			}
			return "系统已经把您的信息通知给:" + toEmpName + subMsg;
		} else {
			return "系统已经把您的信息通知给:" + toEmpName;
		}
	}

	/**
	 * 重新设置流程标题 可以在节点的任何位置调用它,产生新的标题。
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @return 是否设置成功
	 * @throws Exception
	 */
	public static boolean Flow_ReSetFlowTitle(String flowNo, int nodeID, long workid) throws Exception {
		Node nd = new Node(nodeID);
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.RetrieveFromDBSources();
		Flow fl = nd.getHisFlow();
		String title = bp.wf.WorkFlowBuessRole.GenerTitle(fl, wk);
		return Flow_SetFlowTitle(flowNo, workid, title);
	}

	/**
	 * 设置流程参数 该参数，用户可以在流程实例中获得到.
	 * 
	 * @param workid
	 *            工作ID
	 * @param paras
	 *            参数,格式：@GroupMark=xxxx@IsCC=1
	 * @return 是否设置成功
	 * @throws Exception
	 */
	public static boolean Flow_SetFlowParas(String flowNo, long workid, String paras) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		if (gwf.RetrieveFromDBSources() == 0) {
			throw new RuntimeException("创建流程ID不存在.");
		}

		String[] strs = paras.split("[@]", -1);
		for (String item : strs) {
			if (DataType.IsNullOrEmpty(item)) {
				continue;
			}
			// GroupMark=xxxx
			String[] mystr = item.split("[=]", -1);
			gwf.SetPara(mystr[0], mystr[1]);
		}
		gwf.Update();
		return true;
	}

	/**
	 * 设置流程应完成日期.
	 * 
	 * @param workid
	 *            工作ID
	 * @param sdt
	 *            应完成日期
	 * @throws Exception
	 */
	public static void Flow_SetSDTOfFlow(long workid, String sdt) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		gwf.setSDTOfFlow(sdt);
		gwf.Update();
	}

	/**
	 * 设置流程标题
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param title
	 *            标题
	 * @return 是否设置成功
	 * @throws Exception
	 */
	public static boolean Flow_SetFlowTitle(String flowNo, long workid, String title) throws Exception {
		// 替换标题中出现的英文 ""引号，造成在获取数据时，造成异常
		title = title.replace('"', '“');
		title = title.replace('"', '”');

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkFlow SET Title=" + dbstr + "Title WHERE WorkID=" + dbstr + "WorkID";
		ps.Add(GenerWorkFlowAttr.Title, title);
		ps.Add(GenerWorkFlowAttr.WorkID, workid);
		DBAccess.RunSQL(ps);

		Flow fl = new Flow(flowNo);
		ps = new Paras();
		ps.SQL = "UPDATE " + fl.getPTable() + " SET Title=" + dbstr + "Title WHERE OID=" + dbstr + "WorkID";
		ps.Add(GenerWorkFlowAttr.Title, title);
		ps.Add(GenerWorkFlowAttr.WorkID, workid);
		int num = DBAccess.RunSQL(ps);

		if (fl.getHisDataStoreModel() == DataStoreModel.ByCCFlow) {
			// ps = new Paras();
			// ps.SQL="UPDATE ND" + int.Parse(flowNo + "01") + " SET Title=" +
			// dbstr + "Title WHERE OID=" + dbstr + "WorkID";
			// ps.Add(GenerWorkFlowAttr.Title, title);
			// ps.Add(GenerWorkFlowAttr.WorkID, workid);
			// DBAccess.RunSQL(ps);
		}

		if (num == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 调度流程 说明： 1，通常是由admin执行的调度。 2，特殊情况下，需要从一个人的待办调度到另外指定的节点，制定的人员上。
	 * 
	 * @param workid
	 *            工作ID
	 * @param toNodeID
	 *            调度到节点
	 * @param toEmper
	 *            调度到人员
	 * @throws Exception
	 */
	public static String Flow_Schedule(long workid, int toNodeID, String toEmper) throws Exception {
		String dbstr = SystemConfig.getAppCenterDBVarStr();

		Node nd = new Node(toNodeID);
		Emp emp = new Emp(toEmper);

		// 找到GenerWorkFlow,并执行更新.
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		gwf.setWFState(WFState.Runing);
		gwf.setTaskSta(TaskSta.None);
		gwf.setTodoEmps(toEmper + "," + emp.getName() + ";");
		gwf.setFK_Node(toNodeID);
		gwf.setNodeName(nd.getName());
		// gwf.StarterName =emp.getName();
		gwf.Update();

		// 让其都设置完成。
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkerList SET IsPass=1 WHERE WorkID=" + dbstr + "WorkID";
		ps.Add(GenerWorkFlowAttr.WorkID, workid);
		DBAccess.RunSQL(ps);

		// 更新流程数据信息。
		Flow fl = new Flow(gwf.getFK_Flow());
		ps = new Paras();
		ps.SQL = "UPDATE " + fl.getPTable() + " SET FlowEnder=" + dbstr + "FlowEnder,FlowEndNode=" + dbstr
				+ "FlowEndNode WHERE OID=" + dbstr + "OID";
		ps.Add(NDXRptBaseAttr.FlowEnder, toEmper);
		ps.Add(NDXRptBaseAttr.FlowEndNode, toNodeID);
		ps.Add(NDXRptBaseAttr.OID, workid);
		DBAccess.RunSQL(ps);

		// 执行更新.
		GenerWorkerLists gwls = new GenerWorkerLists(workid);
		GenerWorkerList gwl = gwls.get(gwls.size() - 1) instanceof GenerWorkerList
				? (GenerWorkerList) gwls.get(gwls.size() - 1) : null; // 获得最后一个。
		gwl.setWorkID(workid);
		gwl.setFK_Node(toNodeID);
		gwl.setFK_NodeText(nd.getName());
		gwl.setFK_Emp(toEmper);
		gwl.setFK_EmpText(emp.getName());
		gwl.setIsPass(false);
		gwl.setIsEnable(true);
		gwl.setIsRead(false);
		gwl.setWhoExeIt(nd.getWhoExeIt());
		// gwl.Sender = WebUser.getNo();
		gwl.setHungUpTimes(0);
		gwl.setFID(gwf.getFID());
		gwl.setFK_Dept(emp.getFK_Dept());
		gwl.setFK_DeptT(emp.getFK_DeptText());

		if (gwl.Update() == 0) {
			gwl.Insert();
		}

		String sql = "SELECT COUNT(*) FROM WF_EmpWorks where WorkID=" + workid + " and fk_emp='" + toEmper + "'";
		int i = DBAccess.RunSQLReturnValInt(sql);
		if (i == 0) {
			throw new RuntimeException("@调度错误");
		}

		return "该流程(" + gwf.getTitle() + ")，已经调度到(" + nd.getName() + "),分配给(" + emp.getName() + ")";
	}

	/**
	 * 设置流程运行模式 如果是自动流程. 负责人:liuxianchen. 调用地方/WorkOpt/TransferCustom.aspx
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param runType
	 *            是否自动运行？ 如果自动运行，就按照流程设置的规则运行。 非自动运行，就按照用户自己定义的运转顺序计算。
	 * @param paras
	 *            手工运行的参数格式为: @节点ID1`子流程No`处理模式`接受人1,接受人n`抄送人1NO,抄送人nNO`抄送人1Name
	 *            ,抄送人nName@节点ID2`子流程No`处理模式`接受人1,接受人n`抄送人1NO,抄送人nNO`抄送人1Name,
	 *            抄送人nName
	 * @throws Exception
	 */
	public static void Flow_SetFlowTransferCustom(String flowNo, long workid, TransferCustomType runType, String paras)
			throws Exception {
		// 删除以前存储的参数.
		DBAccess.RunSQL("DELETE FROM WF_TransferCustom WHERE WorkID=" + workid);

		// 保存参数.
		// 参数格式为 @104`SubFlow002`1`zhangsan,lisi`wangwu,chenba`王五,陈八@......
		String[] strs = paras.split("[@]", -1);
		int idx = 0, cidx = 0;
		for (String str : strs) {
			if (DataType.IsNullOrEmpty(str)) {
				continue;
			}

			if (str.contains("`") == false) {
				continue;
			}

			// 处理字符串.
			String[] vals = str.split("[`]", -1);
			int nodeid = Integer.parseInt(vals[0]); // 节点ID.
			String subFlow = vals[1]; // 调用的子流程.
			int todomodel = Integer.parseInt(vals[2]); // 处理模式.

			TransferCustom tc = new TransferCustom();
			tc.setIdx(idx); // 顺序.
			tc.setFK_Node(nodeid); // 节点.
			tc.setWorkID(workid); // 工作ID.
			tc.setWorker(vals[3]); // 工作人员.
			tc.setSubFlowNo(subFlow); // 子流程.
			tc.setMyPK(tc.getFK_Node() + "_" + tc.getWorkID() + "_" + idx);
			tc.setTodolistModel(TodolistModel.forValue(todomodel)); // 处理模式.
			tc.Save();
			idx++;

			// 设置抄送
			String[] ccs = vals[4].split("[,]", -1);
			String[] ccNames = vals[5].split("[,]", -1);
			SelectAccper sa = new SelectAccper();
			sa.Delete(SelectAccperAttr.FK_Node, nodeid, SelectAccperAttr.WorkID, workid, SelectAccperAttr.AccType, 1);

			cidx = 0;
			for (int i = 0; i < ccs.length; i++) {
				if (DataType.IsNullOrEmpty(ccs[i]) || ccs[i].equals("0")) {
					continue;
				}

				sa = new SelectAccper();
				sa.setMyPK(nodeid + "_" + workid + "_" + ccs[i]);
				sa.setFK_Emp(ccs[i].trim());
				sa.setEmpName(ccNames[i].trim());
				sa.setFK_Node(nodeid);

				sa.setWorkID(workid);
				sa.setAccType(1);
				sa.setIdx(cidx);
				sa.Insert();
				cidx++;
			}
		}

		// 设置运行模式.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		if (gwf.RetrieveFromDBSources() == 0) {
			// gwf.WFSta = WFSta.Runing;
			gwf.setWFState(WFState.Blank);

			gwf.setStarter(WebUser.getNo());
			gwf.setStarterName(WebUser.getName());

			gwf.setFK_Flow(flowNo);
			bp.wf.Flow fl = new Flow(flowNo);
			gwf.setFK_FlowSort(fl.getFK_FlowSort());

			gwf.setSysType(fl.getSysType());
			gwf.setFK_Dept(WebUser.getFK_Dept());

			gwf.setTransferCustomType(runType);
			gwf.Insert();
			return;
		}
		gwf.setTransferCustomType(runType);
		gwf.Update();
	}

	/**
	 * 设置流程运行模式 启用新的接口原来的接口参数格式太复杂,仍然保留.
	 * 标准格式:@NodeID=节点ID;Worker=操作人员1,操作人员2,操作人员n,TodolistModel=多人处理模式;SubFlowNo
	 * =可发起的子流程编号;SDT=应完成时间;
	 * 标准简洁格式:@NodeID=节点ID;Worker=操作人员1,操作人员2,操作人员n;@NodeID=节点ID2;Worker=操作人员1,
	 * 操作人员2,操作人员n;
	 * 完整格式: @NodeID=101;Worker=zhangsan,lisi;@TodolistModel=1;SubFlowNo=001;SDT
	 * =2015-12-12;@NodeID=102;Worker=zhangsan,lisi;@TodolistModel=1;SubFlowNo=
	 * 001;SDT=2015-12-12;
	 * 简洁格式: @NodeID=101;Worker=zhangsan,lisi;@NodeID=102;Worker=wagnwu,zhaoliu;
	 * 
	 * @param flowNo
	 * @param workid
	 * @param runType
	 * @param paras
	 *            格式为:@节点编号1;处理人员1,处理人员2,处理人员n(可选);应处理时间(可选)
	 * @throws Exception
	 */
	public static void Flow_SetFlowTransferCustomV201605(String flowNo, long workid, TransferCustomType runType,
			String paras) throws Exception {

		/// 更新状态.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		if (gwf.RetrieveFromDBSources() == 0) {
			// gwf.WFSta = WFSta.Runing;
			gwf.setWFState(WFState.Blank);

			gwf.setStarter(WebUser.getNo());
			gwf.setStarterName(WebUser.getName());

			gwf.setFK_Flow(flowNo);
			bp.wf.Flow fl = new Flow(flowNo);
			gwf.setFK_FlowSort(fl.getFK_FlowSort());
			gwf.setSysType(fl.getSysType());

			gwf.setFK_Dept(WebUser.getFK_Dept());

			gwf.setTransferCustomType(runType);
			gwf.Insert();
			return;
		}
		gwf.setTransferCustomType(runType);
		gwf.Update();
		if (runType == TransferCustomType.ByCCBPMDefine) {
			return; // 如果是按照设置的模式运行，就要更改状态后退出它.
		}

		///

		// 删除以前存储的参数.
		DBAccess.RunSQL("DELETE FROM WF_TransferCustom WHERE WorkID=" + workid);

		// 保存参数.
		// 参数格式为 格式为:@节点编号1;处理人员1,处理人员2,处理人员n;应处理时间(可选)
		// 例如1:
		// @101;zhangsan,lisi,wangwu;2016-05-12;@102;liming,xiaohong,xiaozhang;2016-05-12
		// 例如2:
		// @101;zhangsan,lisi,wangwu;@102;liming,xiaohong,xiaozhang;2016-05-12

		String[] strs = paras.split("[@]", -1);
		int idx = 0, cidx = 0;
		for (String str : strs) {
			if (DataType.IsNullOrEmpty(str)) {
				continue;
			}

			if (str.contains(";") == false) {
				continue;
			}

			// 处理字符串.
			String[] vals = str.split("[;]", -1);
			int nodeid = Integer.parseInt(vals[0]); // 节点ID.
			String subFlow = vals[1]; // 调用的子流程.
			int todomodel = Integer.parseInt(vals[2]); // 处理模式.

			TransferCustom tc = new TransferCustom();
			tc.setIdx(idx); // 顺序.
			tc.setFK_Node(nodeid); // 节点.
			tc.setWorkID(workid); // 工作ID.
			tc.setWorker(vals[3]); // 工作人员.
			tc.setSubFlowNo(subFlow); // 子流程.
			tc.setMyPK(tc.getFK_Node() + "_" + tc.getWorkID() + "_" + idx);
			tc.setTodolistModel(TodolistModel.forValue(todomodel)); // 处理模式.
			tc.Save();
			idx++;

			// 设置抄送
			String[] ccs = vals[4].split("[,]", -1);
			String[] ccNames = vals[5].split("[,]", -1);
			SelectAccper sa = new SelectAccper();
			sa.Delete(SelectAccperAttr.FK_Node, nodeid, SelectAccperAttr.WorkID, workid, SelectAccperAttr.AccType, 1);

			cidx = 0;
			for (int i = 0; i < ccs.length; i++) {
				if (DataType.IsNullOrEmpty(ccs[i]) || ccs[i].equals("0")) {
					continue;
				}

				sa = new SelectAccper();
				sa.setMyPK(nodeid + "_" + workid + "_" + ccs[i]);
				sa.setFK_Emp(ccs[i].trim());
				sa.setEmpName(ccNames[i].trim());
				sa.setFK_Node(nodeid);
				sa.setWorkID(workid);
				sa.setAccType(1);
				sa.setIdx(cidx);
				sa.Insert();
				cidx++;
			}
		}
	}

	/**
	 * 是否可以删除该流程？
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @return 是否可以删除该流程
	 * @throws Exception
	 */
	public static boolean Flow_IsCanDeleteFlowInstance(String flowNo, long workid, String userNo) throws Exception {
		if (WebUser.getNo().equals("admin") == true) {
			return true;
		}

		Flow fl = new Flow(flowNo);
		if (fl.getFlowDeleteRole() == FlowDeleteRole.AdminOnly) {
			return false;
		}

		// 是否是用户管理员?
		if (fl.getFlowDeleteRole() == FlowDeleteRole.AdminAppOnly) {
			if (userNo.indexOf("admin") == 0) {
				return true; // 这里判断不严谨,如何判断是否是一个应用管理员使用admin+部门编号来确定的. 比如：
								// admin3701
			} else {
				return false;
			}
		}

		// 是否是发起人.
		if (fl.getFlowDeleteRole() == FlowDeleteRole.ByMyStarter) {
			Paras ps = new Paras();
			ps.SQL = "SELECT WorkID FROM WF_GenerWorkFlow WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr()
					+ "WorkID AND Starter=" + SystemConfig.getAppCenterDBVarStr() + "Starter";
			ps.Add("WorkID", workid);
			ps.Add("Starter", userNo);
			String user = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (user == null) {
				return false;
			}

			return true;
		}

		// 按照节点是否启用删除按钮来计算.
		if (fl.getFlowDeleteRole() == FlowDeleteRole.ByNodeSetting) {
			Paras ps = new Paras();
			ps.SQL = "SELECT WorkID FROM WF_GenerWorkerlist A, WF_Node B  WHERE A.FK_Node=B.NodeID  AND B.DelEnable=1  AND A.WorkID="
					+ SystemConfig.getAppCenterDBVarStr() + "WorkID AND A.FK_Emp=" + SystemConfig.getAppCenterDBVarStr()
					+ "FK_Emp";
			ps.Add("WorkID", workid);
			ps.Add("FK_Emp", userNo);
			String user = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (user == null) {
				return false;
			}

			return true;
		}
		return false;
	}

	/// 与流程有关的接口

	/**
	 * 增加一个评论
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            父工作ID
	 * @param msg
	 *            消息
	 * @param empNo
	 *            评论人编号
	 * @param empName
	 *            评论人名称
	 * @return 插入ID主键
	 * @throws Exception
	 */
	public static String Flow_BBSAdd(String flowNo, long workid, long fid, String msg, String empNo, String empName)
			throws Exception {
		return Glo.AddToTrack(ActionType.FlowBBS, flowNo, workid, fid, 0, null, empNo, empName, 0, null, empNo, empName,
				msg, null);
	}

	/**
	 * 删除一个评论.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param mypk
	 *            主键
	 * @return 返回删除信息.AddToTrack
	 */
	public static String Flow_BBSDelete(String flowNo, String mypk, String username) {
		Paras pss = new Paras();
		pss.SQL = "SELECT EMPFROM FROM ND" + Integer.parseInt(flowNo) + "Track WHERE MyPK="
				+ SystemConfig.getAppCenterDBVarStr() + "MyPK ";
		pss.Add("MyPK", mypk);
		String str = DBAccess.RunSQLReturnString(pss);
		if (str.equals(username) || username.equals(str)) {
			Paras ps = new Paras();
			ps.SQL = "DELETE FROM ND" + Integer.parseInt(flowNo) + "Track WHERE MyPK="
					+ SystemConfig.getAppCenterDBVarStr() + "MyPK ";
			ps.Add("MyPK", mypk);
			DBAccess.RunSQL(ps);
			return "删除成功.";
		} else {
			return "删除失败,仅能删除自己评论!";
		}
	}

	/**
	 * 取消设置关注
	 * 
	 * @param workid
	 *            要取消设置的工作ID
	 * @throws Exception
	 */
	public static void Flow_Focus(long workid) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		int i = gwf.RetrieveFromDBSources();
		if (i == 0) {
			throw new RuntimeException("@ 设置关注错误：没有找到 WorkID= " + workid + " 的实例。");
		}

		String isFocus = gwf.GetParaString("F_" + WebUser.getNo(), "0"); // edit
																			// by
																			// liuxc,2016-10-22,修复关注/取消关注逻辑错误

		if (isFocus.equals("0")) {
			gwf.SetPara("F_" + WebUser.getNo(), "1");
		} else {
			gwf.SetPara("F_" + WebUser.getNo(), "0");
		}

		gwf.DirectUpdate();
	}

	/**
	 * 调整
	 * 
	 * @param workid
	 *            要调整的WorkID
	 * @param toNodeID
	 *            调整到的节点ID
	 * @param toEmpIDs
	 *            人员集合
	 * @param note
	 *            调整原因
	 * @return
	 * @throws Exception
	 */
	public static String Flow_ReSend(long workid, int toNodeID, String toEmpIDs, String note) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		if (gwf.getWFState() == WFState.Complete)
			return "err@该流程已经运行完成您不能执行调整,可以执行回滚.";

		Node nd = new Node(toNodeID);

		Emps emps = new Emps();
		String[] strs = toEmpIDs.split("[,]", -1);

		// String[] strs = toEmpIDs.split(',');

		String todoEmps = "";
		int num = 0;
		for (String empID : strs) {
			if (DataType.IsNullOrEmpty(empID) == true) {
				continue;
			}

			bp.port.Emp emp = new bp.port.Emp(empID);
			todoEmps += emp.getNo() + "," + emp.getName() + ";";
			num++;

			emps.AddEntity(emp);
		}

		// 设置人员.
		gwf.SetValByKey(GenerWorkFlowAttr.TodoEmps, todoEmps);
		gwf.setTodoEmpsNum(num);

		gwf.setHuiQianTaskSta(HuiQianTaskSta.None);
		gwf.setWFState(WFState.Runing);

		// 给当前人员产生待办.
		GenerWorkerList gwl = new GenerWorkerList();

		// 先查询数据
		gwl.Retrieve(GenerWorkerListAttr.WorkID, workid, GenerWorkerListAttr.FK_Node, gwf.getFK_Node());
		// 删除他.
		gwl.Delete(GenerWorkerListAttr.WorkID, workid, GenerWorkerListAttr.FK_Node, gwf.getFK_Node());

		// 删除当前节点.
		gwl.Delete(GenerWorkerListAttr.WorkID, workid, GenerWorkerListAttr.FK_Node, toNodeID);

		String todoEmpsExts = "";
		for (bp.port.Emp item : emps.ToJavaList()) {
			// 插入一条信息，让调整的人员显示待办.
			gwl.setWorkID(workid);
			gwl.setFK_Emp(item.getNo());
			gwl.setFK_EmpText(item.getName());
			gwl.setFK_Node(toNodeID);
			gwl.setIsPassInt(0);
			gwl.setIsRead(false);
			gwl.setWhoExeIt(0);
			gwl.setFK_Dept(item.getFK_Dept());
			gwl.setFK_DeptT(item.getFK_DeptText());
			gwl.setFK_NodeText(nd.getName());
			gwl.setFK_Flow(nd.getFK_Flow());
			gwl.setIsEnable(true);
			gwl.Insert();

			todoEmpsExts += item.getNo() + "," + item.getName() + ";";
		}

		// 更新当前节点状态.
		gwf.setFK_Node(toNodeID);
		gwf.setNodeName(nd.getName());

		// 设置当前的处理人.
		gwf.setTodoEmpsNum(emps.size());
		gwf.setTodoEmps(todoEmpsExts);

		// 发送人.
		gwf.setSender(WebUser.getNo() + "," + WebUser.getName());
		gwf.setSendDT(DataType.getCurrentDataTime());

		gwf.setParasToNodes("");
		gwf.Update();

		return "调整成功,调整到:" + gwf.getNodeName() + " , 调整给:" + todoEmpsExts;
	}

	/**
	 * 取消、确认.
	 * 
	 * @param workid
	 *            要取消设置的工作ID
	 * @throws Exception
	 */
	public static void Flow_Confirm(long workid) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		int i = gwf.RetrieveFromDBSources();
		if (i == 0) {
			throw new RuntimeException("@ 设置关注错误：没有找到 WorkID= " + workid + " 的实例。");
		}

		String isFocus = gwf.GetParaString("C_" + WebUser.getNo(), "0");

		if (isFocus.equals("0")) {
			gwf.SetPara("C_" + WebUser.getNo(), "1");
		} else {
			gwf.SetPara("C_" + WebUser.getNo(), "0");
		}

		gwf.DirectUpdate();
	}

	/**
	 * 获得工作进度-用于展示流程的进度图
	 * 
	 * @param workID
	 *            workID
	 * @return 返回进度数据
	 * @throws Exception
	 */
	public static DataSet DB_JobSchedule(long workID) throws Exception {
		String sql = "";
		DataSet ds = new DataSet();

		/*
		 * 流程控制主表, 可以得到流程状态，停留节点，当前的执行人. 该表里有如下字段是重点: 0. WorkID 流程ID. 1. WFState
		 * 字段用于标识当前流程的状态.. 2. FK_Node 停留节点. 3. NodeName 停留节点名称. 4. TodoEmps
		 * 停留的待办人员.
		 */
		GenerWorkFlow gwf = new GenerWorkFlow(workID);
		ds.Tables.add(gwf.ToDataTableField("WF_GenerWorkFlow"));

		/*
		 * 节点信息: 节点信息表,存储每个环节的节点信息数据. NodeID 节点ID. Name 名称. X,Y
		 * 节点图形位置，如果使用进度图就不需要了.
		 */
		NodeSimples nds = new NodeSimples(gwf.getFK_Flow());
		ds.Tables.add(nds.ToDataTableField("WF_Node"));

		/*
		 * 节点的连接线.
		 */
		Directions dirs = new Directions(gwf.getFK_Flow());
		ds.Tables.add(dirs.ToDataTableField("WF_Direction"));

		/// 运动轨迹
		/*
		 * 运动轨迹： 构造的一个表，用与存储运动轨迹.
		 * 
		 */
		DataTable dtHistory = new DataTable();
		dtHistory.TableName = "Track";
		dtHistory.Columns.Add("FK_Node"); // 节点ID.
		dtHistory.Columns.Add("NodeName"); // 名称.
		dtHistory.Columns.Add("RunModel"); // 节点类型.
		dtHistory.Columns.Add("EmpNo"); // 人员编号.
		dtHistory.Columns.Add("EmpName"); // 名称
		dtHistory.Columns.Add("DeptName"); // 部门名称
		dtHistory.Columns.Add("RDT"); // 记录日期.
		dtHistory.Columns.Add("SDT"); // 应完成日期(可以不用.)
		dtHistory.Columns.Add("IsPass"); // 是否通过?

		// 执行人.

		// 历史执行人.
		sql = "SELECT C.Name AS DeptName,A.MyPK,A.ActionType,A.ActionTypeText,A.FID,A.WorkID,A.NDFrom,A.NDFromT,A.NDTo,A.NDToT,A.EmpFrom,A.EmpFromT,A.EmpTo,A.EmpToT,A.RDT,A.WorkTimeSpan,A.Msg,A.NodeData,A.Tag,A.Exer FROM ND"
				+ Integer.parseInt(gwf.getFK_Flow()) + "Track A, Port_Emp B, Port_Dept C  ";
		sql += " WHERE (A.WorkID=" + workID + " OR A.FID=" + workID
				+ ") AND (A.ActionType=1 OR A.ActionType=0  OR A.ActionType=6  OR A.ActionType=7) AND (A.EmpFrom=B.No) AND (B.FK_Dept=C.No) ";
		sql += " ORDER BY A.RDT ";

		DataTable dtTrack = DBAccess.RunSQLReturnTable(sql);
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.UpperCase) {
			dtTrack.Columns.get("NDFROM").ColumnName = "NDFrom";
			dtTrack.Columns.get("NDFROMT").ColumnName = "NDFromT";
			dtTrack.Columns.get("EMPFROM").ColumnName = "EmpFrom";
			dtTrack.Columns.get("EMPFROMT").ColumnName = "EmpFromT";
			dtTrack.Columns.get("DEPTNAME").ColumnName = "DeptName";
			dtTrack.Columns.get("RDT").ColumnName = "RDT";

		}
		if (SystemConfig.AppCenterDBFieldCaseModel() == FieldCaseModel.Lowercase) {
			dtTrack.Columns.get("ndfrom").ColumnName = "NDFrom";
			dtTrack.Columns.get("ndfromt").ColumnName = "NDFromT";
			dtTrack.Columns.get("empfrom").ColumnName = "EmpFrom";
			dtTrack.Columns.get("empfromt").ColumnName = "EmpFromT";
			dtTrack.Columns.get("deptname").ColumnName = "DeptName";
			dtTrack.Columns.get("rdt").ColumnName = "RDT";

		}

		for (DataRow drTrack : dtTrack.Rows) {

			DataRow dr = dtHistory.NewRow();
			dr.setValue("FK_Node", drTrack.getValue("NDFrom"));
			// dr["ActionType"] = drTrack["NDFrom"];
			dr.setValue("NodeName", drTrack.getValue("NDFromT"));
			dr.setValue("EmpNo", drTrack.getValue("EmpFrom"));
			dr.setValue("EmpName", drTrack.getValue("EmpFromT"));
			dr.setValue("DeptName", drTrack.getValue("DeptName")); // 部门名称.
			dr.setValue("RDT", drTrack.getValue("RDT"));
			dr.setValue("SDT", "");
			dr.setValue("IsPass", 1); // gwl.IsPassInt; //是否通过.
			dtHistory.Rows.add(dr);
		}

		// 如果流程没有完成.
		if (gwf.getWFState() != WFState.Complete && 1 == 2) {
			DataRow dr = dtHistory.NewRow();
			dr.setValue("FK_Node", gwf.getFK_Node());
			// dr["ActionType"] = drTrack["NDFrom"];
			dr.setValue("NodeName", gwf.getNodeName());
			dr.setValue("EmpNo", WebUser.getNo());
			dr.setValue("EmpName", WebUser.getName());
			dr.setValue("DeptName", WebUser.getFK_DeptName()); // 部门名称.
			dr.setValue("RDT", DataType.getCurrentDate());
			dr.setValue("SDT", "");
			dr.setValue("IsPass", 0); // gwl.IsPassInt; //是否通过.
			dtHistory.Rows.add(dr);
		}

		if (dtHistory.Rows.size() == 0) {
			DataRow dr = dtHistory.NewRow();
			dr.setValue("FK_Node", gwf.getFK_Node());
			dr.setValue("NodeName", gwf.getNodeName());
			dr.setValue("EmpNo", gwf.getStarter());
			dr.setValue("EmpName", gwf.getStarterName());
			dr.setValue("RDT", gwf.getRDT());
			dr.setValue("SDT", gwf.getSDTOfNode());
			dtHistory.Rows.add(dr);
		}

		// 给 dtHistory runModel 赋值.
		for (NodeSimple nd : nds.ToJavaList()) {
			int runMode = nd.GetValIntByKey(NodeAttr.RunModel);
			for (DataRow dr : dtHistory.Rows) {
				if (Integer.parseInt(dr.getValue("FK_Node").toString()) == nd.getNodeID()) {
					dr.setValue("RunModel", runMode);
				}
			}
		}
		ds.Tables.add(dtHistory);

		/// 运动轨迹

		/// 游离态
		TransferCustoms tranfs = new TransferCustoms(workID);
		ds.Tables.add(tranfs.ToDataTableField("WF_TransferCustom"));

		/// 游离态

		return ds;
	}

	/**
	 * 设置委托
	 * 
	 * @param Author
	 *            接收委托人账号
	 * @param AuthorWay
	 *            委托方式：0不授权， 1完全授权，2，指定流程范围授权.
	 * @param AuthorFlows
	 *            委托流程编号，格式：001,002,003
	 * @param AuthorDate
	 *            委托开始时间，默认当前时间
	 * @param AuthorToDate
	 *            委托结束时间
	 * @return 设置结果：成功true,失败 false
	 * @throws Exception
	 */

	public static boolean Flow_AuthorSave(String Author, int AuthorWay, String AuthorFlows, String AuthorDate)
			throws Exception {
		return Flow_AuthorSave(Author, AuthorWay, AuthorFlows, AuthorDate, null);
	}

	public static boolean Flow_AuthorSave(String Author, int AuthorWay, String AuthorFlows) throws Exception {
		return Flow_AuthorSave(Author, AuthorWay, AuthorFlows, null, null);
	}

	public static boolean Flow_AuthorSave(String Author, int AuthorWay) throws Exception {
		return Flow_AuthorSave(Author, AuthorWay, null, null, null);
	}

	public static boolean Flow_AuthorSave(String Author, int AuthorWay, String AuthorFlows, String AuthorDate,
			String AuthorToDate) throws Exception {
		if (WebUser.getNo() == null) {
			throw new RuntimeException("@ 非法用户，请执行登录后再试。");
		}

		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(WebUser.getNo());
		emp.setAuthor(Author);
		emp.setAuthorWay(AuthorWay);
		emp.setAuthorDate(bp.da.DataType.getCurrentDate());

		if (!DataType.IsNullOrEmpty(AuthorFlows)) {
			emp.setAuthorFlows(AuthorFlows);
		}

		if (!DataType.IsNullOrEmpty(AuthorDate)) {
			emp.setAuthorFlows(AuthorDate);
		}

		if (!DataType.IsNullOrEmpty(AuthorToDate)) {
			emp.setAuthorToDate(AuthorToDate);
		}

		int i = emp.Save();

		return i >= 0 ? true : false;
	}

	/**
	 * 取消委托当前登录人的委托信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean Flow_AuthorCancel() throws Exception {
		if (WebUser.getNo() == null) {
			throw new RuntimeException("@ 非法用户，请执行登录后再试。");
		}

		bp.wf.port.WFEmp myau = new bp.wf.port.WFEmp(WebUser.getNo());
		bp.da.Log.DefaultLogWriteLineInfo("取消授权:" + WebUser.getNo() + "取消了对(" + myau.getAuthor() + ")的授权。");
		myau.setAuthor("");
		myau.setAuthorWay(0);
		myau.setAuthorDate("");
		myau.setAuthorToDate("");
		int i = myau.Update();
		return i >= 0 ? true : false;
	}

	/**
	 * 获取当前登录人的委托人
	 * 
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_AuthorEmps() throws Exception {
		if (WebUser.getNo() == null) {
			throw new RuntimeException("@ 非法用户，请执行登录后再试。");
		}

		return DBAccess.RunSQLReturnTable("SELECT * FROM WF_EMP WHERE AUTHOR='" + WebUser.getNo() + "'");
	}

	/**
	 * 获取委托给当前登录人的流程待办信息
	 * 
	 * @param empNo
	 *            授权人员编号
	 * @return
	 * @throws Exception
	 */
	public static DataTable DB_AuthorEmpWorks(String empNo) throws Exception {
		if (WebUser.getNo() == null) {
			throw new RuntimeException("@ 非法用户，请执行登录后再试。");
		}

		bp.wf.port.WFEmp emp = new bp.wf.port.WFEmp(empNo);
		if (!DataType.IsNullOrEmpty(emp.getAuthor()) && emp.getAuthor().equals(WebUser.getNo())
				&& emp.getAuthorIsOK() == true) {
			String sql = "";
			String wfSql = "  WFState=" + WFState.Askfor.getValue() + " OR WFState=" + WFState.Runing.getValue()
					+ "  OR WFState=" + WFState.AskForReplay.getValue() + " OR WFState=" + WFState.Shift.getValue()
					+ " OR WFState=" + WFState.ReturnSta.getValue() + " OR WFState=" + WFState.Fix.getValue();
			switch (emp.getHisAuthorWay()) {
			case All:
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					sql = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' AND TaskSta!=1  ORDER BY ADT DESC";
				} else {
					sql = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' ORDER BY ADT DESC";
				}

				break;
			case SpecFlows:
				if (bp.wf.Glo.getIsEnableTaskPool() == true) {
					sql = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' AND  FK_Flow IN " + emp.getAuthorFlows() + " AND TaskSta!=0    ORDER BY ADT DESC";
				} else {
					sql = "SELECT * FROM WF_EmpWorks WHERE (" + wfSql + ") AND FK_Emp='" + emp.getNo()
							+ "' AND  FK_Flow IN " + emp.getAuthorFlows() + "   ORDER BY ADT DESC";
				}

				break;
			}
			return DBAccess.RunSQLReturnTable(sql);
		}
		return null;
	}

	/// 与流程有关的接口

	/// 与流程有关的接口

	/// get 属性节口
	/**
	 * 获得流程运行过程中的参数
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @return 如果没有就返回null,有就返回@参数名0=参数值0@参数名1=参数值1
	 * @throws Exception
	 */
	public static String GetFlowParas(int nodeID, long workid) throws Exception {
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "SELECT Paras FROM WF_GenerWorkerlist WHERE FK_Node=" + dbstr + "FK_Node AND WorkID=" + dbstr
				+ "WorkID";
		ps.Add(GenerWorkerListAttr.FK_Node, nodeID);
		ps.Add(GenerWorkerListAttr.WorkID, workid);
		return DBAccess.RunSQLReturnStringIsNull(ps, null);
	}

	/// get 属性节口

	/// 工作有关接口
	/**
	 * 发起流程
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param ht
	 *            节点表单:主表数据以Key Value 方式传递(可以为空)
	 * @param workDtls
	 *            节点表单:从表数据，从表名称与从表单的从表编号要对应(可以为空)
	 * @param nextNodeID
	 *            发起后要跳转到的节点(可以为空)
	 * @param nextWorker
	 *            发起后要跳转到的节点并指定的工作人员(可以为空)
	 * @return 发送到第二个节点的执行信息
	 * @throws Exception
	 */
	public static SendReturnObjs Node_StartWork(String flowNo, Hashtable ht, DataSet workDtls, int nextNodeID,
			String nextWorker) throws Exception {
		return Node_StartWork(flowNo, ht, workDtls, nextNodeID, nextWorker, 0, null);
	}

	/**
	 * 发起流程
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param htWork
	 *            节点表单:主表数据以Key Value 方式传递(可以为空)
	 * @param workDtls
	 *            节点表单:从表数据，从表名称与从表单的从表编号要对应(可以为空)
	 * @param nextNodeID
	 *            发起后要跳转到的节点(可以为空)
	 * @param nextWorker
	 *            发起后要跳转到的节点并指定的工作人员(可以为空)
	 * @param parentWorkID
	 *            父流程的workid，如果没有可以为0
	 * @param parentFlowNo
	 *            父流程的编号，如果没有可以为空
	 * @return 发送到第二个节点的执行信息
	 * @throws Exception
	 */
	public static SendReturnObjs Node_StartWork(String flowNo, Hashtable htWork, DataSet workDtls, int nextNodeID,
			String nextWorker, long parentWorkID, String parentFlowNo) throws Exception {
		// 给全局变量赋值.
		bp.wf.Glo.setSendHTOfTemp(htWork);

		Flow fl = new Flow(flowNo);
		Work wk = fl.NewWork();
		long workID = wk.getOID();
		if (htWork != null) {
			for (Object str : htWork.keySet()) {
				if (str == null)
					continue;
				switch (str.toString()) {
				case GERptAttr.OID:
				case WorkAttr.MD5:
				case WorkAttr.Emps:
				case GERptAttr.FID:
				case GERptAttr.FK_Dept:
				case GERptAttr.Rec:
				case GERptAttr.Title:
					continue;
				default:
					break;
				}
				wk.SetValByKey(str.toString(), htWork.get(str));
			}
		}

		wk.setOID(workID);
		if (workDtls != null) {
			// 保存从表
			for (DataTable dt : workDtls.Tables) {
				for (MapDtl dtl : wk.getHisMapDtls().ToJavaList()) {
					if (!dt.TableName.equals(dtl.getNo())) {
						continue;
					}

					// 获取dtls
					GEDtls daDtls = new GEDtls(dtl.getNo());
					daDtls.Delete(GEDtlAttr.RefPK, wk.getOID()); // 清除现有的数据.

					GEDtl daDtl = daDtls.getGetNewEntity() instanceof GEDtl ? (GEDtl) daDtls.getGetNewEntity() : null;
					daDtl.setRefPK(String.valueOf(wk.getOID()));

					// 为从表复制数据.
					for (DataRow dr : dt.Rows) {
						daDtl.ResetDefaultVal();

						// 明细列.
						for (DataColumn dc : dt.Columns) {
							// 设置属性.
							daDtl.SetValByKey(dc.ColumnName, dr.getValue(dc.ColumnName));
						}

						daDtl.setRefPK(String.valueOf(wk.getOID()));
						daDtl.InsertAsOID(DBAccess.GenerOID("Dtl")); // 插入数据.
					}
				}
			}
		}

		WorkNode wn = new WorkNode(wk, fl.getHisStartNode());

		Node nextNoode = null;
		if (nextNodeID != 0) {
			nextNoode = new Node(nextNodeID);
		}

		SendReturnObjs objs = wn.NodeSend(nextNoode, nextWorker);
		if (parentWorkID != 0) {
			DBAccess.RunSQL("UPDATE WF_GenerWorkFlow SET PWorkID=" + parentWorkID + ",PFlowNo='" + parentFlowNo
					+ "' WHERE WorkID=" + objs.getVarWorkID());
		}

		/// 更新发送参数.
		if (htWork != null) {
			String paras = "";
			for (Object key : htWork.keySet()) {
				paras += "@" + key + "=" + htWork.get(key).toString();
			}

			if (DataType.IsNullOrEmpty(paras) == false && Glo.getIsEnableTrackRec() == true) {
				String dbstr = SystemConfig.getAppCenterDBVarStr();
				Paras ps = new Paras();
				ps.SQL = "UPDATE WF_GenerWorkerlist SET AtPara=" + dbstr + "Paras WHERE WorkID=" + dbstr
						+ "WorkID AND FK_Node=" + dbstr + "FK_Node";
				ps.Add(GenerWorkerListAttr.Paras, paras);
				ps.Add(GenerWorkerListAttr.WorkID, workID);
				ps.Add(GenerWorkerListAttr.FK_Node, Integer.parseInt(flowNo + "01"));
				try {
					DBAccess.RunSQL(ps);
				} catch (java.lang.Exception e) {
					GenerWorkerList gwl = new GenerWorkerList();
					gwl.CheckPhysicsTable();
					DBAccess.RunSQL(ps);
				}
			}
		}

		/// 更新发送参数.

		return objs;
	}

	public static void CopyDataFromParentFlow(String pFlowNo, long pFID, long pWorkID, Work currEnt) {
		/// copy 首先从父流程的NDxxxRpt copy.
		// Int64 pWorkIDReal = 0;
		// Flow pFlow = new Flow(pFlowNo);
		// string pOID = "";
		// if (DataType.IsNullOrEmpty(PFIDStr) == true || PFIDStr == "0")
		// pOID = PWorkID.ToString();
		// else
		// pOID = PFIDStr;

		// string sql = "SELECT * FROM " + pFlow.PTable + " WHERE OID=" + pOID;
		// DataTable dt = DBAccess.RunSQLReturnTable(sql);
		// if (dt.Rows.size() != 1)
		// throw new Exception("@不应该查询不到父流程的数据[" + sql + "],
		// 可能的情况之一,请确认该父流程的调用节点是子线程，但是没有把子线程的FID参数传递进来。");

		// wk.Copy(dt.Rows.get(0));
		// rpt.Copy(dt.Rows.get(0));

		//// 设置单号为空.
		// wk.SetValByKey("BillNo", "");
		// rpt.BillNo = "";
		/// copy 首先从父流程的NDxxxRpt copy.

		/// 从调用的节点上copy.
		// BP.WF.Node fromNd = new BP.WF.Node(int.Parse(PNodeIDStr));
		// Work wkFrom = fromNd.HisWork;
		// wkFrom.setOID( PWorkID;
		// if (wkFrom.RetrieveFromDBSources() == 0)
		// throw new Exception("@父流程的工作ID不正确，没有查询到数据" + PWorkID);
		//// wk.Copy(wkFrom);
		//// rpt.Copy(wkFrom);
		/// 从调用的节点上copy.

		/// 获取web变量.
		// foreach (string k in paras.Keys)
		// {
		// if (k == "OID")
		// continue;

		// wk.SetValByKey(k, paras[k]);
		// rpt.SetValByKey(k, paras[k]);
		// }
		/// 获取web变量.

		/// 特殊赋值.
		// wk.setOID( newOID;
		// rpt.setOID( newOID;

		//// 在执行copy后，有可能这两个字段会被冲掉。
		// if (CopyFormWorkID != null)
		// {
		// /*如果不是 执行的从已经完成的流程copy.*/

		// wk.SetValByKey(GERptAttr.PFlowNo, PFlowNo);
		// wk.SetValByKey(GERptAttr.PNodeID, PNodeID);
		// wk.SetValByKey(GERptAttr.PWorkID, PWorkID);

		// rpt.SetValByKey(GERptAttr.PFlowNo, PFlowNo);
		// rpt.SetValByKey(GERptAttr.PNodeID, PNodeID);
		// rpt.SetValByKey(GERptAttr.PWorkID, PWorkID);

		// //忘记了增加这句话.
		// rpt.SetValByKey(GERptAttr.PEmp, WebUser.getNo());

		// //要处理单据编号 BillNo .
		// if (this.BillNoFormat != "")
		// {
		// rpt.SetValByKey(GERptAttr.BillNo,
		// BP.WF.WorkFlowBuessRole.GenerBillNo(this.BillNoFormat, rpt.OID, rpt,
		// this.PTable));

		// //设置单据编号.
		// wk.SetValByKey(GERptAttr.BillNo, rpt.BillNo);
		// }

		// rpt.SetValByKey(GERptAttr.FID, 0);
		// rpt.SetValByKey(GERptAttr.FlowStartRDT,
		// DataType.getCurrentDataTime());
		// rpt.SetValByKey(GERptAttr.FlowEnderRDT,
		// DataType.getCurrentDataTime());
		// rpt.SetValByKey(GERptAttr.MyNum, 0);
		// rpt.SetValByKey(GERptAttr.WFState, (int)WFState.Blank);
		// rpt.SetValByKey(GERptAttr.FlowStarter, emp.getNo());
		// rpt.SetValByKey(GERptAttr.FlowEnder, emp.getNo());
		// rpt.SetValByKey(GERptAttr.FlowEndNode, this.StartNodeID);
		// rpt.SetValByKey(GERptAttr.FK_Dept, emp.FK_Dept);
		// rpt.SetValByKey(GERptAttr.FK_NY, DataType.getCurrentYearMonth());

		// if (Glo.UserInfoShowModel == UserInfoShowModel.UserNameOnly)
		// rpt.SetValByKey(GERptAttr.FlowEmps, "@" + emp.getName());

		// if (Glo.UserInfoShowModel == UserInfoShowModel.UserIDUserName)
		// rpt.SetValByKey(GERptAttr.FlowEmps, "@" + emp.getNo());

		// if (Glo.UserInfoShowModel == UserInfoShowModel.UserIDUserName)
		// rpt.SetValByKey(GERptAttr.FlowEmps, "@" + emp.No + "," +
		// emp.getName());

		// }

		// if (rpt.getEnMap().getPhysicsTable() !=
		// wk.getEnMap().getPhysicsTable())
		// wk.Update(); //更新工作节点数据.
		// rpt.Update(); // 更新流程数据表.
		/// 特殊赋值.

		/// 复制其他数据..
		//// 复制明细。
		// MapDtls dtls = wk.HisMapDtls;
		// if (dtls.size() > 0)
		// {
		// MapDtls dtlsFrom = wkFrom.HisMapDtls;
		// int idx = 0;
		// if (dtlsFrom.size() == dtls.size())
		// {
		// foreach (MapDtl dtl in dtls)
		// {
		// if (dtl.IsCopyNDData == false)
		// continue;

		// //new 一个实例.
		// GEDtl dtlData = new GEDtl(dtl.getNo());

		// //检查该明细表是否有数据，如果没有数据，就copy过来，如果有，就说明已经copy过了。
		// // sql = "SELECT COUNT(OID) FROM
		// "+dtlData.getEnMap().getPhysicsTable()+" WHERE RefPK="+wk.OID;

		// //删除以前的数据.
		// sql = "DELETE FROM " + dtlData.getEnMap().getPhysicsTable() + " WHERE
		// RefPK=" + wk.OID;
		// DBAccess.RunSQL(sql);

		// MapDtl dtlFrom = dtlsFrom[idx] as MapDtl;

		// GEDtls dtlsFromData = new GEDtls(dtlFrom.getNo());
		// dtlsFromData.Retrieve(GEDtlAttr.RefPK, PWorkID);
		// foreach (GEDtl geDtlFromData in dtlsFromData)
		// {
		// dtlData.Copy(geDtlFromData);
		// dtlData.RefPK = wk.OID.ToString();
		// if (this.setNo(= PFlowNo)
		// {
		// dtlData.InsertAsNew();
		// }
		// else
		// {
		// if (this.StartLimitRole == WF.StartLimitRole.OnlyOneSubFlow)
		// dtlData.SaveAsOID(geDtlFromData.OID); //为子流程的时候，仅仅允许被调用1次.
		// else
		// dtlData.InsertAsNew();
		// }
		// }
		// }
		// }
		// }

		//// 复制附件数据。
		// if (wk.HisFrmAttachments.size() > 0)
		// {
		// if (wkFrom.HisFrmAttachments.size() > 0)
		// {
		// int toNodeID = wk.NodeID;

		// //删除数据。
		// DBAccess.RunSQL("DELETE FROM Sys_FrmAttachmentDB WHERE
		// FK_MapData='ND" + toNodeID + "' AND RefPKVal='" + wk.OID + "'");
		// FrmAttachmentDBs athDBs = new FrmAttachmentDBs("ND" + PNodeIDStr,
		// PWorkID.ToString());

		// foreach (FrmAttachmentDB athDB in athDBs)
		// {
		// FrmAttachmentDB athDB_N = new FrmAttachmentDB();
		// athDB_N.Copy(athDB);
		// athDB_N.setFK_MapData("ND" + toNodeID;
		// athDB_N.RefPKVal = wk.OID.ToString();
		// athDB_N.FK_FrmAttachment = athDB_N.FK_FrmAttachment.replace("ND" +
		// PNodeIDStr,
		// "ND" + toNodeID);

		// if (athDB_N.HisAttachmentUploadType == AttachmentUploadType.Single)
		// {
		// /*如果是单附件.*/
		// athDB_N.setMyPK(athDB_N.FK_FrmAttachment + "_" + wk.OID;
		// if (athDB_N.getIsExits() == true)
		// continue; /*说明上一个节点或者子线程已经copy过了, 但是还有子线程向合流点传递数据的可能，所以不能用break.*/
		// athDB_N.Insert();
		// }
		// else
		// {
		// athDB_N.setMyPK(athDB_N.UploadGUID + "_" + athDB_N.FK_MapData + "_" +
		// wk.OID;
		// athDB_N.Insert();
		// }
		// }
		// }
		// }
		/// 复制表单其他数据.

		/// 复制独立表单数据.
		//// 求出来被copy的节点有多少个独立表单.
		// FrmNodes fnsFrom = new Template.FrmNodes(fromnd.getNodeID());
		// if (fnsFrom.size() != 0)
		// {
		// //求当前节点表单的绑定的表单.
		// FrmNodes fns = new Template.FrmNodes(nd.getNodeID());
		// if (fns.size() != 0)
		// {
		// //开始遍历当前绑定的表单.
		// foreach (FrmNode fn in fns)
		// {
		// foreach (FrmNode fnFrom in fnsFrom)
		// {
		// if (fn.FK_Frm != fnFrom.FK_Frm)
		// continue;

		// bp.sys.GEEntity geEnFrom = new GEEntity(fnFrom.FK_Frm);
		// geEnFrom.setOID( PWorkID;
		// if (geEnFrom.RetrieveFromDBSources() == 0)
		// continue;

		// //执行数据copy , 复制到本身.
		// geEnFrom.CopyToOID(wk.OID);
		// }
		// }
		// }
		// }
		/// 复制独立表单数据.
	}

	/**
	 * 创建一个空白的WorkID
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param userNo
	 *            用户编号
	 * @return 执行结果
	 */
	public static long Node_CreateBlankWork(String flowNo, String userNo) throws Exception {
		return Node_CreateBlankWork(flowNo, null, null, userNo);
	}

	/**
	 * 创建WorkID
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param ht
	 *            表单参数，可以为null。
	 * @param workDtls
	 *            明细表参数，可以为null。
	 * @param starter
	 *            流程的发起人
	 * @param title
	 *            创建工作时的标题，如果为null，就按设置的规则生成。
	 * @param parentWorkID
	 *            父流程的WorkID,如果没有父流程就传入为0.
	 * @param parentFID
	 *            父流程的FID,如果没有父流程就传入为0.
	 * @param parentFlowNo
	 *            父流程的流程编号,如果没有父流程就传入为null.
	 * @param jumpToNode
	 *            要跳转到的节点,如果没有则为0.
	 * @param jumpToEmp
	 *            要跳转到的人员,如果没有则为null.
	 * @param todoEmps
	 *            待办人员,如果没有则为null.
	 * @return 为开始节点创建工作后产生的WorkID.
	 */

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID, String parentFlowNo, int parentNodeID, String parentEmp,
			int jumpToNode, String jumpToEmp, String todoEmps) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, parentFlowNo,
				parentNodeID, parentEmp, jumpToNode, jumpToEmp, todoEmps, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID, String parentFlowNo, int parentNodeID, String parentEmp,
			int jumpToNode, String jumpToEmp) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, parentFlowNo,
				parentNodeID, parentEmp, jumpToNode, jumpToEmp, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID, String parentFlowNo, int parentNodeID, String parentEmp,
			int jumpToNode) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, parentFlowNo,
				parentNodeID, parentEmp, jumpToNode, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID, String parentFlowNo, int parentNodeID, String parentEmp)
					throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, parentFlowNo,
				parentNodeID, parentEmp, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID, String parentFlowNo, int parentNodeID) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, parentFlowNo,
				parentNodeID, null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID, String parentFlowNo) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, parentFlowNo, 0,
				null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID, long parentFID) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, parentFID, null, 0, null, 0,
				null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title, long parentWorkID) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, parentWorkID, 0, null, 0, null, 0, null, null,
				null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter,
			String title) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, title, 0, 0, null, 0, null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls, String starter)
			throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, starter, null, 0, 0, null, 0, null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht, DataSet workDtls) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, workDtls, null, null, 0, 0, null, 0, null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, java.util.Hashtable ht) throws Exception {
		return Node_CreateBlankWork(flowNo, ht, null, null, null, 0, 0, null, 0, null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo) throws Exception {
		return Node_CreateBlankWork(flowNo, null, null, null, null, 0, 0, null, 0, null, 0, null, null, null);
	}

	public static long Node_CreateBlankWork(String flowNo, Hashtable ht, DataSet workDtls, String starter, String title,
			long parentWorkID, long parentFID, String parentFlowNo, int parentNodeID, String parentEmp, int jumpToNode,
			String jumpToEmp, String todoEmps, String isStartSameLevelFlow) throws Exception {

		// 把一些其他的参数也增加里面去,传递给ccflow.
		Hashtable htPara = new Hashtable();

		if (parentWorkID != 0) {
			htPara.put(StartFlowParaNameList.PWorkID, parentWorkID);
			htPara.put(StartFlowParaNameList.PFID, parentFID);
			htPara.put(StartFlowParaNameList.PFlowNo, parentFlowNo);
			htPara.put(StartFlowParaNameList.PNodeID, parentNodeID);
			
			if (parentEmp==null)
				parentEmp=WebUser.getNo();
			
			htPara.put(StartFlowParaNameList.PEmp, parentEmp);
		}

		// 给全局变量赋值.
		bp.wf.Glo.setSendHTOfTemp(ht);
		
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		if (DataType.IsNullOrEmpty(starter)) {
			starter = WebUser.getNo();

		}

		Flow fl = new Flow(flowNo);
		Node nd = new Node(fl.getStartNodeID());

		// 下一个工作人员。
		Emp empStarter = new Emp(starter);
		if( starter.equals(WebUser.getNo())){
			empStarter.setFK_Dept(WebUser.getFK_Dept());
			empStarter.SetValByKey("FK_DeptText",WebUser.getFK_DeptName());
		}
		Work wk = fl.NewWork(empStarter, htPara);
		long workID = wk.getOID();

		/// 给各个属性-赋值
		if (ht != null) {
			for (Object str : ht.keySet()) {
				if (str == null)
					continue;
				switch (str.toString()) {
				case GERptAttr.OID:
				case WorkAttr.MD5:
				case WorkAttr.Emps:
				case GERptAttr.FID:
				case GERptAttr.FK_Dept:
				case GERptAttr.Rec:
				case GERptAttr.Title:
					continue;
				default:
					break;
				}
				wk.SetValByKey(str.toString(), ht.get(str));
			}
			wk.Update();
		}
		wk.setOID(workID);
		if (workDtls != null) {
			// 保存从表
			for (DataTable dt : workDtls.Tables) {
				for (MapDtl dtl : wk.getHisMapDtls().ToJavaList()) {
					if (!dt.TableName.equals(dtl.getNo())) {
						continue;
					}

					// 获取dtls
					GEDtls daDtls = new GEDtls(dtl.getNo());
					daDtls.Delete(GEDtlAttr.RefPK, wk.getOID()); // 清除现有的数据.

					GEDtl daDtl = daDtls.getGetNewEntity() instanceof GEDtl ? (GEDtl) daDtls.getGetNewEntity() : null;
					daDtl.setRefPK(String.valueOf(wk.getOID()));

					// 为从表复制数据.
					for (DataRow dr : dt.Rows) {
						daDtl.ResetDefaultVal();
						daDtl.setRefPK(String.valueOf(wk.getOID()));

						// 明细列.
						for (DataColumn dc : dt.Columns) {
							// 设置属性.
							daDtl.SetValByKey(dc.ColumnName, dr.getValue(dc.ColumnName));
						}
						daDtl.InsertAsOID(DBAccess.GenerOID("Dtl")); // 插入数据.
					}
				}
			}
		}

		/// 赋值

		Paras ps = new Paras();
		if (DataType.IsNullOrEmpty(title) == true) {
			title = bp.wf.WorkFlowBuessRole.GenerTitle(fl, wk);
		}

		// 执行对报表的数据表WFState状态的更新,让它为runing的状态.
		ps = new Paras();
		ps.SQL = "UPDATE " + fl.getPTable() + " SET WFState=0,FK_Dept=" + dbstr + "FK_Dept,Title=" + dbstr
				+ "Title WHERE OID=" + dbstr + "OID";
		ps.Add(GERptAttr.FK_Dept, empStarter.getFK_Dept());
		ps.Add(GERptAttr.Title, title);
		ps.Add(GERptAttr.OID, wk.getOID());
		DBAccess.RunSQL(ps);

		// 设置父流程信息.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(wk.getOID());
		int i = gwf.RetrieveFromDBSources();

		// 将流程信息提前写入wf_GenerWorkFlow,避免查询不到。
		gwf.setFlowName(fl.getName());
		gwf.setFK_Flow(flowNo);
		gwf.setFK_FlowSort(fl.getFK_FlowSort());
		gwf.setSysType(fl.getSysType());

		gwf.setFK_Dept(empStarter.getFK_Dept());
		gwf.setDeptName(empStarter.getFK_DeptText());
		gwf.setFK_Node(fl.getStartNodeID());
		gwf.setNodeName(nd.getName());
		gwf.setWFState(WFState.Blank);
		gwf.setTitle(title);

		gwf.setStarter(empStarter.getNo());
		gwf.setStarterName(empStarter.getName());
		gwf.setRDT(DataType.getCurrentDataTime());
		gwf.setPWorkID(parentWorkID);
		gwf.setPFID(parentFID);
		gwf.setPFlowNo(parentFlowNo);
		gwf.setPNodeID(parentNodeID);
		if (i == 0) {
			gwf.Insert();
		} else {
			gwf.Update();
		}

		if (parentWorkID != 0) {
			bp.wf.Dev2Interface.SetParentInfo(flowNo, wk.getOID(), parentWorkID); // 设置父流程信息
		}
		
		   //更新 domian 
        DBAccess.RunSQL("UPDATE WF_GenerWorkFlow  SET Domain=(SELECT Domain FROM WF_FlowSort WHERE WF_FlowSort.No=WF_GenerWorkFlow.FK_FlowSort) WHERE WorkID=" + wk.getOID());


		/// #warning 增加是防止手动启动子流程或者平级子流程时关闭子流程页面找不到待办 保存到待办。

		if (isStartSameLevelFlow != null) {
			bp.wf.Dev2Interface.Node_SaveWork(flowNo, Integer.parseInt(flowNo + "01"), wk.getOID());
		}
		// 如果有跳转.
		if (jumpToNode != 0) {
			bp.wf.Dev2Interface.Node_SendWork(flowNo, wk.getOID(), null, null, jumpToNode, jumpToEmp);
		}
		return wk.getOID();
	}

	/**
	 * 增加待办人员
	 * 
	 * @param workid
	 *            工作ID
	 * @param todoEmps
	 *            要增加的处理人员,多个人员用逗号分开.
	 * @throws Exception
	 */
	public static void Node_AddTodolist(long workid, String todoEmps) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		if (gwf.getWFState() == WFState.Complete) {
			throw new RuntimeException("流程：" + gwf.getTitle() + "已经完成,您不能增加接受人.");
		}

		/// 增加待办人员.

		GenerWorkerList gwl = new GenerWorkerList();
		gwl.Retrieve(GenerWorkerListAttr.WorkID, workid, GenerWorkerListAttr.FK_Node, gwf.getFK_Node());

		String[] emps = todoEmps.split("[,]", -1); // 分开字符串.
		String tempStrs = ""; // 临时变量，防止重复插入.
		for (String emp : emps) {
			if (DataType.IsNullOrEmpty(emp) == true) {
				continue;
			}
			if (tempStrs.contains("," + emp + ",") == true) {
				continue;
			}

			// 插入待办.
			gwl = new GenerWorkerList();
			gwl.setWorkID(workid);
			gwl.setFK_Node(gwf.getFK_Node());
			gwl.setFK_Emp(emp);
			int i = gwl.RetrieveFromDBSources();
			if (i == 1) {
				continue;
			}

			Emp empEn = new Emp(emp);

			gwl.setFK_EmpText(empEn.getName());
			gwl.setFK_NodeText(gwf.getNodeName());
			gwl.setFID(0);
			gwl.setFK_Flow(gwf.getFK_Flow());
			gwl.setFK_Dept(empEn.getFK_Dept());
			gwl.setFK_DeptT(empEn.getFK_DeptText());

			gwl.setSDT("无");
			gwl.setDTOfWarning(DataType.getCurrentDataTime());
			gwl.setIsEnable(true);
			gwl.setIsPass(false);
			gwl.setPRI(gwf.getPRI());
			gwl.Insert();

			tempStrs += "," + emp + ",";
		}

		/// 增加待办人员.

		if (gwf.getWFState() == WFState.Blank) {
			gwf.setWFState(WFState.Runing);
			gwf.Update();
		}
	}

	/**
	 * 创建开始节点工作 创建后可以创办人形成一个待办.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param htWork
	 *            表单参数，可以为null。
	 * @param workDtls
	 *            明细表参数，可以为null。
	 * @param flowStarter
	 *            流程的发起人，如果为null就是当前人员。
	 * @param title
	 *            创建工作时的标题，如果为null，就按设置的规则生成。
	 * @param parentWorkID
	 *            父流程的WorkID,如果没有父流程就传入为0.
	 * @param parentFlowNo
	 *            父流程的流程编号,如果没有父流程就传入为null.
	 * @return 为开始节点创建工作后产生的WorkID.
	 * @throws Exception
	 */

	public static long Node_CreateStartNodeWork(String flowNo, java.util.Hashtable htWork, DataSet workDtls,
			String flowStarter, String title, long parentWorkID, String parentFlowNo) throws Exception {
		return Node_CreateStartNodeWork(flowNo, htWork, workDtls, flowStarter, title, parentWorkID, parentFlowNo, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo, java.util.Hashtable htWork, DataSet workDtls,
			String flowStarter, String title, long parentWorkID) throws Exception {
		return Node_CreateStartNodeWork(flowNo, htWork, workDtls, flowStarter, title, parentWorkID, null, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo, java.util.Hashtable htWork, DataSet workDtls,
			String flowStarter, String title) throws Exception {
		return Node_CreateStartNodeWork(flowNo, htWork, workDtls, flowStarter, title, 0, null, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo, java.util.Hashtable htWork, DataSet workDtls,
			String flowStarter) throws Exception {
		return Node_CreateStartNodeWork(flowNo, htWork, workDtls, flowStarter, null, 0, null, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo, java.util.Hashtable htWork, DataSet workDtls)
			throws Exception {
		return Node_CreateStartNodeWork(flowNo, htWork, workDtls, null, null, 0, null, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo, java.util.Hashtable htWork) throws Exception {
		return Node_CreateStartNodeWork(flowNo, htWork, null, null, null, 0, null, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo) throws Exception {
		return Node_CreateStartNodeWork(flowNo, null, null, null, null, 0, null, 0);
	}

	public static long Node_CreateStartNodeWork(String flowNo, Hashtable htWork, DataSet workDtls, String flowStarter,
			String title, long parentWorkID, String parentFlowNo, int parentNDFrom) throws Exception {
		// 给全局变量赋值.
		bp.wf.Glo.setSendHTOfTemp(htWork);

		if (DataType.IsNullOrEmpty(flowStarter)) {
			flowStarter = WebUser.getNo();
		}

		Flow fl = new Flow(flowNo);
		Node nd = new Node(fl.getStartNodeID());

		// 下一个工作人员。
		Emp emp = new Emp(flowStarter);
		Work wk = fl.NewWork(flowStarter);

		/// 给各个属性-赋值
		if (htWork != null) {
			for (Object str : htWork.keySet()) {
				if (str == null)
					continue;
				switch (str.toString()) {
				case GERptAttr.OID:
				case WorkAttr.MD5:
				case GERptAttr.FID:
				case GERptAttr.FK_Dept:
				case GERptAttr.Rec:
				case GERptAttr.Title:
					continue;
				default:
					break;
				}
				wk.SetValByKey(str.toString(), htWork.get(str));
			}
			// 将参数保存到业务表
			wk.DirectUpdate();
		}

		if (workDtls != null) {
			// 保存从表
			for (DataTable dt : workDtls.Tables) {
				for (MapDtl dtl : wk.getHisMapDtls().ToJavaList()) {
					if (!dt.TableName.equals(dtl.getNo())) {
						continue;
					}

					// 获取dtls
					GEDtls daDtls = new GEDtls(dtl.getNo());
					daDtls.Delete(GEDtlAttr.RefPK, wk.getOID()); // 清除现有的数据.

					GEDtl daDtl = daDtls.getGetNewEntity() instanceof GEDtl ? (GEDtl) daDtls.getGetNewEntity() : null;
					daDtl.setRefPK(String.valueOf(wk.getOID()));

					// 为从表复制数据.
					for (DataRow dr : dt.Rows) {
						daDtl.ResetDefaultVal();
						daDtl.setRefPK(String.valueOf(wk.getOID()));

						// 明细列.
						for (DataColumn dc : dt.Columns) {
							// 设置属性.
							daDtl.SetValByKey(dc.ColumnName, dr.getValue(dc.ColumnName));
						}
						daDtl.InsertAsOID(DBAccess.GenerOID("Dtl")); // 插入数据.
					}
				}
			}
		}

		/// 赋值

		/// 为开始工作创建待办
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(wk.getOID());
		int i = gwf.RetrieveFromDBSources();

		gwf.setFlowName(fl.getName());
		gwf.setFK_Flow(flowNo);
		gwf.setFK_FlowSort(fl.getFK_FlowSort());
		gwf.setSysType(fl.getSysType());

		gwf.setFK_Dept(emp.getFK_Dept());
		gwf.setDeptName(emp.getFK_DeptText());
		gwf.setFK_Node(fl.getStartNodeID());

		gwf.setNodeName(nd.getName());

		// 默认是空白流程
		// gwf.WFSta = WFSta.Etc;
		gwf.setWFState(WFState.Blank);
		// 保存到草稿
		if (fl.getDraftRole() == DraftRole.SaveToDraftList) {
			gwf.setWFState(WFState.Draft);
		} else if (fl.getDraftRole() == DraftRole.SaveToTodolist) {
			// 保存到待办
			// gwf.WFSta = WFSta.Runing;
			gwf.setWFState(WFState.Runing);
		}

		if (DataType.IsNullOrEmpty(title)) {
			gwf.setTitle(bp.wf.WorkFlowBuessRole.GenerTitle(fl, wk));
		} else {
			gwf.setTitle(title);
		}

		gwf.setStarter(emp.getNo());
		gwf.setStarterName(emp.getName());
		gwf.setRDT(DataType.getCurrentDataTime());

		if (htWork != null && htWork.containsKey("PRI") == true) {
			gwf.setPRI(Integer.parseInt(htWork.get("PRI").toString()));
		}

		if (htWork != null && htWork.containsKey("SDTOfNode") == true) {
			/* 节点应完成时间 */
			gwf.setSDTOfNode(htWork.get("SDTOfNode").toString());
		}

		if (htWork != null && htWork.containsKey("SDTOfFlow") == true) {
			/* 流程应完成时间 */
			gwf.setSDTOfNode(htWork.get("SDTOfFlow").toString());
		}

		gwf.setPWorkID(parentWorkID);
		gwf.setPFlowNo(parentFlowNo);
		gwf.setPNodeID(parentNDFrom);
		if (i == 0) {
			gwf.Insert();
		} else {
			gwf.Update();
		}

		// 产生工作列表.
		GenerWorkerList gwl = new GenerWorkerList();
		gwl.setWorkID(wk.getOID());
		if (gwl.RetrieveFromDBSources() == 0) {
			gwl.setFK_Emp(emp.getNo());
			gwl.setFK_EmpText(emp.getName());

			gwl.setFK_Node(nd.getNodeID());
			gwl.setFK_NodeText(nd.getName());
			gwl.setFID(0);

			gwl.setFK_Flow(fl.getNo());
			gwl.setFK_Dept(emp.getFK_Dept());
			gwl.setFK_DeptT(emp.getFK_DeptText());

			gwl.setSDT("无");
			gwl.setDTOfWarning(DataType.getCurrentDataTime());
			gwl.setIsEnable(true);

			gwl.setIsPass(false);
			// gwl.Sender = WebUser.getNo();
			gwl.setPRI(gwf.getPRI());
			gwl.Insert();
		}

		/// 为开始工作创建待办

		// 执行对报表的数据表WFState状态的更新
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE " + fl.getPTable() + " SET WFState=" + dbstr + "WFState,WFSta=" + dbstr + "WFSta,Title="
				+ dbstr + "Title,FK_Dept=" + dbstr + "FK_Dept,PFlowNo=" + dbstr + "PFlowNo,PWorkID=" + dbstr
				+ "PWorkID WHERE OID=" + dbstr + "OID";

		// 默认启用草稿.
		if (fl.getDraftRole() == DraftRole.None) {
			ps.Add("WFState", WFState.Blank.getValue());
			ps.Add("WFSta", WFSta.Etc.getValue());
		} else if (fl.getDraftRole() == DraftRole.SaveToDraftList) {
			// 保存到草稿
			ps.Add("WFState", WFState.Draft.getValue());
			ps.Add("WFSta", WFSta.Etc.getValue());
		} else if (fl.getDraftRole() == DraftRole.SaveToTodolist) {
			// 保存到待办
			ps.Add("WFState", WFState.Runing.getValue());
			ps.Add("WFSta", WFSta.Runing.getValue());
		}
		ps.Add("Title", gwf.getTitle());
		ps.Add("FK_Dept", gwf.getFK_Dept());

		ps.Add("PFlowNo", gwf.getPFlowNo());
		ps.Add("PWorkID", gwf.getPWorkID());

		ps.Add("OID", wk.getOID());
		DBAccess.RunSQL(ps);

		//// 写入日志.
		// WorkNode wn = new WorkNode(wk, nd);
		// wn.AddToTrack(ActionType.CallSubFlow, flowStarter, emp.Name,
		//// nd.getNodeID(), nd.Name, "来自" + WebUser.getNo() + "," +
		//// WebUser.getName()
		// + "工作发起.");

		/// 更新发送参数.
		if (htWork != null) {
			String paras = "";
			for (Object key : htWork.keySet()) {
				paras += "@" + key + "=" + htWork.get(key).toString();
			}

			if (DataType.IsNullOrEmpty(paras) == false && Glo.getIsEnableTrackRec() == true) {
				ps = new Paras();
				ps.SQL = "UPDATE WF_GenerWorkerlist SET AtPara=" + dbstr + "Paras WHERE WorkID=" + dbstr
						+ "WorkID AND FK_Node=" + dbstr + "FK_Node";
				ps.Add(GenerWorkerListAttr.Paras, paras);
				ps.Add(GenerWorkerListAttr.WorkID, wk.getOID());
				ps.Add(GenerWorkerListAttr.FK_Node, nd.getNodeID());
				DBAccess.RunSQL(ps);
			}
		}

		/// 更新发送参数.

		return wk.getOID();
	}

	/**
	 * 执行工作发送
	 * 
	 * @param fk_flow
	 *            工作编号
	 * @param workID
	 *            工作ID
	 * @param ht
	 *            节点表单数据
	 * @param dsDtl
	 *            节点表单从表数据
	 * @return 返回发送结果
	 * @throws Exception
	 */

	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, java.util.Hashtable ht) throws Exception {
		return Node_SendWork(fk_flow, workID, ht, null);
	}

	public static SendReturnObjs Node_SendWork(String fk_flow, long workID) throws Exception {
		return Node_SendWork(fk_flow, workID, null, null);
	}

	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, Hashtable ht, DataSet dsDtl)
			throws Exception {
		return Node_SendWork(fk_flow, workID, ht, dsDtl, 0, null);
	}

	/**
	 * 发送工作
	 * 
	 * @param nodeID
	 *            节点编号
	 * @param workID
	 *            工作ID
	 * @param toNodeID
	 *            发送到的节点编号，如果是0就让ccflow自动计算.
	 * @param toEmps
	 *            发送到的人员,多个人员用逗号分开比如：zhangsan,lisi. 如果是null则表示让ccflow自动计算.
	 * @return 返回执行信息
	 * @throws Exception
	 */
	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, int toNodeID, String toEmps)
			throws Exception {
		return Node_SendWork(fk_flow, workID, null, null, toNodeID, toEmps);
	}

	/**
	 * 发送工作
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param htWork
	 *            节点表单数据(Hashtable中的key与节点表单的字段名相同,value 就是字段值)
	 * @return 执行信息
	 * @throws Exception
	 */
	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, Hashtable htWork, int toNodeID,
			String nextWorkers) throws Exception {
		return Node_SendWork(fk_flow, workID, htWork, null, toNodeID, nextWorkers, WebUser.getNo(), WebUser.getName(),
				WebUser.getFK_Dept(), WebUser.getFK_DeptName(), null, 0, 0);
	}

	/**
	 * 发送工作
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param htWork
	 *            节点表单数据(Hashtable中的key与节点表单的字段名相同,value 就是字段值)
	 * @param workDtls
	 *            节点表单明从表数据(dataset可以包含多个table，每个table的名称与从表名称相同，列名与从表的字段相同,
	 *            OID,RefPK列需要为空或者null )
	 * @param toNodeID
	 *            到达的节点，如果是0表示让ccflow自动寻找，否则就按照该参数发送。
	 * @param nextWorkers
	 *            下一步的接受人，如果多个人员用逗号分开，比如:zhangsan,lisi,
	 *            如果为空，则标识让ccflow按照节点访问规则自动寻找。
	 * @return 执行信息
	 * @throws Exception
	 */
	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, Hashtable htWork, DataSet workDtls,
			int toNodeID, String nextWorkers) throws Exception {
		return Node_SendWork(fk_flow, workID, htWork, workDtls, toNodeID, nextWorkers, WebUser.getNo(),
				WebUser.getName(), WebUser.getFK_Dept(), WebUser.getFK_DeptName(), null, 0, 0);
	}

	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, Hashtable htWork, DataSet workDtls,
											   int toNodeID, String toEmps, String execUserNo, String execUserName, String execUserDeptNo,
											   String execUserDeptName, String title, long fid, long pworkid)throws Exception{
		return Node_SendWork(fk_flow, workID, htWork, workDtls, toNodeID, toEmps, execUserNo,
				execUserName,execUserDeptNo,execUserDeptName, null, 0, 0,false);
	}
	/**
	 * 发送工作
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param htWork
	 *            节点表单数据(Hashtable中的key与节点表单的字段名相同,value 就是字段值)
	 * @param workDtls
	 *            节点表单明从表数据(dataset可以包含多个table，每个table的名称与从表名称相同，列名与从表的字段相同,
	 *            OID,RefPK列需要为空或者null )
	 * @param toNodeID
	 *            到达的节点，如果是0表示让ccflow自动寻找，否则就按照该参数发送。
	 * @param nextWorkers
	 *            下一步的接受人，如果多个人员用逗号分开，比如:zhangsan,lisi,
	 *            如果为空，则标识让ccflow按照节点访问规则自动寻找。
	 * @param execUserNo
	 *            执行人编号
	 * @param execUserName
	 *            执行人名称
	 * @param execUserDeptNo
	 *            执行人部门名称
	 * @param execUserDeptName
	 *            执行人部门编号
	 * @return 发送的结果对象
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static SendReturnObjs Node_SendWork(String fk_flow, long workID, Hashtable htWork, DataSet workDtls,
			int toNodeID, String toEmps, String execUserNo, String execUserName, String execUserDeptNo,
			String execUserDeptName, String title, long fid, long pworkid,boolean isReturnNode) throws NumberFormatException, Exception {

		// 给临时的发送变量赋值，解决带有参数的转向。
		Glo.setSendHTOfTemp(htWork);

		int currNodeId = Dev2Interface.Node_GetCurrentNodeID(fk_flow, workID);
		Node nd = new Node();
		nd.setNodeID(currNodeId);
		nd.RetrieveFromDBSources();
		if (htWork != null) {
			bp.wf.Dev2Interface.Node_SaveWork(fk_flow, currNodeId, workID, htWork, workDtls, fid, pworkid);
		}

		// 变量.

		Work sw = nd.getHisWork();
		sw.setOID(workID);
		sw.RetrieveFromDBSources();

		Node ndOfToNode = null; // 到达节点ID
		if (toNodeID != 0) {
			ndOfToNode = new Node(toNodeID);
		}

		// 补偿性修复.
		if (nd.getHisRunModel() != RunModel.SubThread) {
			if (sw.getFID() != 0) {
				sw.DirectUpdate();
			}
		}

		SendReturnObjs objs;
		// 执行流程发送.
		WorkNode wn = new WorkNode(sw, nd);
		wn.setExecer(execUserNo);
		wn.setExecerName(execUserName);
		wn.title = title; // 设置标题，有可能是从外部传递过来的标题.
		wn.SendHTOfTemp = htWork;

		if (ndOfToNode == null) {
			objs = wn.NodeSend(null, toEmps,isReturnNode);
		} else {
			objs = wn.NodeSend(ndOfToNode, toEmps,isReturnNode);
		}

		/// 更新发送参数.
		if (htWork != null) {
			String dbstr = SystemConfig.getAppCenterDBVarStr();
			Paras ps = new Paras();

			String paras = "";
			for (Object key : htWork.keySet()) {
				if (key == null)
					continue;
				paras += "@" + key + "=" + htWork.get(key).toString();
				switch (key.toString()) {
				case WorkSysFieldAttr.SysSDTOfFlow:
					ps = new Paras();
					ps.SQL = "UPDATE WF_GenerWorkFlow SET SDTOfFlow=" + dbstr + "SDTOfFlow WHERE WorkID=" + dbstr
							+ "WorkID";
					ps.Add(GenerWorkFlowAttr.SDTOfFlow, htWork.get(key).toString());
					ps.Add(GenerWorkerListAttr.WorkID, workID);
					DBAccess.RunSQL(ps);

					break;
				case WorkSysFieldAttr.SysSDTOfNode:
					ps = new Paras();
					ps.SQL = "UPDATE WF_GenerWorkFlow SET SDTOfNode=" + dbstr + "SDTOfNode WHERE WorkID=" + dbstr
							+ "WorkID";
					ps.Add(GenerWorkFlowAttr.SDTOfNode, htWork.get(key).toString());
					ps.Add(GenerWorkerListAttr.WorkID, workID);
					DBAccess.RunSQL(ps);

					ps = new Paras();
					ps.SQL = "UPDATE WF_GenerWorkerlist SET SDT=" + dbstr + "SDT WHERE WorkID=" + dbstr
							+ "WorkID AND FK_Node=" + dbstr + "FK_Node";
					ps.Add(GenerWorkerListAttr.SDT, htWork.get(key).toString());
					ps.Add(GenerWorkerListAttr.WorkID, workID);
					ps.Add(GenerWorkerListAttr.FK_Node, objs.getVarToNodeID());
					DBAccess.RunSQL(ps);
					break;
				default:
					break;
				}
			}

			if (DataType.IsNullOrEmpty(paras) == false && Glo.getIsEnableTrackRec() == true) {
				ps = new Paras();
				ps.SQL = "UPDATE WF_GenerWorkerlist SET AtPara=" + dbstr + "Paras WHERE WorkID=" + dbstr
						+ "WorkID AND FK_Node=" + dbstr + "FK_Node";
				ps.Add(GenerWorkerListAttr.Paras, paras);
				ps.Add(GenerWorkerListAttr.WorkID, workID);
				ps.Add(GenerWorkerListAttr.FK_Node, nd.getNodeID());
				DBAccess.RunSQL(ps);
			}
		} else {
			// 判断流程是否启动流程时限
			if (nd.getIsStartNode() && wn.getHisGenerWorkFlow().getWFState() != WFState.ReturnSta) {
				Date dtOfFlow = new Date();
				Date dtOfFlowWarning = new Date();
				Part part = new Part();
				part.setMyPK(nd.getFK_Flow() + "_0_DeadLineRole");
				int count = part.RetrieveFromDBSources();
				if (count != 0) {
					int tag1 = Integer.parseInt(part.getTag1());
					int tag2 = Integer.parseInt(part.getTag2());
					int tag7 = Integer.parseInt(part.getTag7());
					switch (tag7) {
					case 0:
						tag7 = 12;
						break;
					case 1:
						tag7 = 24;
						break;
					case 2:
						tag7 = 48;
						break;
					case 3:
						tag7 = 72;
						break;
					default:
						break;
					}
					// 获取时限时间
					dtOfFlow = Glo.AddDayHoursSpan(new Date(), tag1, tag2, Integer.parseInt(part.getTag3()),
							TWay.forValue(Integer.parseInt(part.getTag4())));
					// 计算警告日期. 时限时间-预警时间
					dtOfFlowWarning = Glo.AddDayHoursSpan(new Date(), (tag1 * 24 + tag2 - tag7) / 24,
							(tag1 * 24 + tag2 - tag7) % 24, Integer.parseInt(part.getTag3()),
							TWay.forValue(Integer.parseInt(part.getTag4())));
					String dbstr = SystemConfig.getAppCenterDBVarStr();
					Paras ps = new Paras();
					ps.SQL = "UPDATE WF_GenerWorkFlow SET SDTOfFlow=" + dbstr + "SDTOfFlow,SDTOfFlowWarning=" + dbstr
							+ "SDTOfFlowWarning WHERE WorkID=" + dbstr + "WorkID";
					ps.Add(GenerWorkFlowAttr.SDTOfFlow, DateUtils.format(dtOfFlow, DataType.getSysDataTimeFormat()));
					ps.Add(GenerWorkFlowAttr.SDTOfFlowWarning,
							DateUtils.format(dtOfFlowWarning, DataType.getSysDataTimeFormat()));
					ps.Add(GenerWorkerListAttr.WorkID, workID);
					DBAccess.RunSQL(ps);

				}
			}
		}

		/// 更新发送参数.

		return objs;

	}

	/**
	 * 增加在队列工作中增加一个处理人. 这个处理顺序系统已经自动处理了.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeID
	 *            工作ID
	 * @param workid
	 *            workid
	 * @param fid
	 *            fid
	 * @param empNo
	 *            要增加的处理人编号
	 * @param empName
	 *            要增加的处理人名称
	 * @throws Exception
	 */
	public static void Node_InsertOrderEmp(String flowNo, int nodeID, long workid, long fid, String empNo,
			String empName) throws Exception {
		GenerWorkerList gwl = new GenerWorkerList();
		int i = gwl.Retrieve(GenerWorkerListAttr.WorkID, workid, GenerWorkerListAttr.FK_Node, nodeID);
		if (i == 0) {
			throw new RuntimeException("@没有找到当前工作人员的待办，请检查该流程是否已经运行到该节点上来了。");
		}

		gwl.setIsPassInt(100);
		gwl.setIsEnable(true);
		gwl.setFK_Emp(empNo);
		gwl.setFK_EmpText(empName);

		try {
			gwl.Insert();
		} catch (java.lang.Exception e) {
			throw new RuntimeException("@该人员已经存在处理队列中，您不能增加.");
		}

		// 开始更新他们的顺序, 首先从数据库里获取他们的顺序. lxl职位由小到大
		String sql = "SELECT No,Name FROM Port_Emp WHERE No IN (SELECT FK_Emp FROM WF_GenerWorkerList WHERE WorkID="
				+ workid + " AND FK_Node=" + nodeID + " AND IsPass >=100 ) ORDER BY IDX desc";
		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		int idx = 100;
		for (DataRow dr : dt.Rows) {
			idx++;
			String myEmpNo = dr.getValue(0).toString();
			sql = "UPDATE WF_GenerWorkerList SET IsPass=" + idx + " WHERE FK_Emp='" + myEmpNo + "' AND WorkID=" + workid
					+ " AND FK_Node=" + nodeID;
			DBAccess.RunSQL(sql);
		}
	}

	/**
	 * 写入抄送
	 * 
	 * @param workid
	 *            工作ID
	 * @param toEmps
	 *            抄送给: zhangsan,lisi,wangwu
	 * @return 执行结果
	 * @throws Exception
	 */
	public static String Node_CCTo(long workid, String toEmps) throws Exception {
		if (DataType.IsNullOrEmpty(toEmps) == true) {
			return "没有指定人";
		}
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		// if (gwf.WFState == WFState.Complete)
		// throw new Exception("err@");

		Node fromNode = new Node(gwf.getFK_Node());

		toEmps = toEmps.replace(";", ",");
		String[] strs = toEmps.split("[,]", -1);
		bp.port.Emp emp = new Emp();
		bp.port.Dept dept = new Dept();
		CCList list = new CCList();

		String names = "";
		for (String str : strs) {
			if (DataType.IsNullOrEmpty(str) == true) {
				continue;
			}

			emp.setNo(str);
			int i = emp.RetrieveFromDBSources();
			if (i == 0) {
				continue;
			}

			// 根据人员的部门编号获取所在部门名称
			dept.setNo(emp.getFK_Dept());
			dept.RetrieveFromDBSources();

			names += emp.getName() + "、";

			// list.setMyPK(DBAccess.GenerOIDByGUID().ToString(); // workID +
			// "_" + fk_node + "_" + empNo;
			list.setMyPK(gwf.getWorkID() + "_" + gwf.getFK_Node() + "_" + emp.getNo());
			if (list.getIsExits() == true) {
				continue; // 判断是否存在?
			}

			list.setFK_Flow(gwf.getFK_Flow());
			list.setFlowName(gwf.getFlowName());
			list.setFK_Node(fromNode.getNodeID());
			list.setNodeName(gwf.getNodeName());
			list.setTitle(gwf.getTitle());
			list.setDoc(gwf.getTitle());
			list.setCCTo(emp.getNo());
			list.setCCToName(emp.getName());

			// 增加抄送人部门.
			list.setCCToDept(emp.getFK_Dept());
			list.setCCToDeptName(dept.getName());
			list.setRDT(DataType.getCurrentDataTime());
			list.setRec(WebUser.getNo());
			list.setWorkID(gwf.getWorkID());
			list.setFID(gwf.getFID());
			list.setPFlowNo(gwf.getPFlowNo());
			list.setPWorkID(gwf.getPWorkID());

			// 是否要写入待办.
			if (fromNode.getCCWriteTo() == CCWriteTo.CCList) {
				list.setInEmpWorks(false); // added by liuxc,2015.7.6
			} else {
				list.setInEmpWorks(true); // added by liuxc,2015.7.6
			}

			// 写入待办和写入待办与抄送列表,状态不同
			if (fromNode.getCCWriteTo() == CCWriteTo.All || fromNode.getCCWriteTo() == CCWriteTo.Todolist) {
				list.setHisSta(fromNode.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
			}

			if (fromNode.getIsEndNode() == true) // 结束节点只写入抄送列表
			{
				list.setHisSta(CCSta.UnRead);
				list.setInEmpWorks(false);
			}

			try {
				list.Insert();
			} catch (java.lang.Exception e) {
				list.CheckPhysicsTable();
				list.Update();
			}

			// 发送消息给他们.
			bp.wf.Dev2Interface.Port_SendMsg(emp.getNo(), gwf.getTitle(), "抄送消息:" + gwf.getTitle(),
					"CC" + gwf.getFK_Node() + "_" + gwf.getWorkID() + "_" + emp.getNo(), SMSMsgType.CC,
					gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getWorkID(), gwf.getFID());
		}

		// 记录日志.
		Glo.AddToTrack(ActionType.CC, gwf.getFK_Flow(), gwf.getWorkID(), gwf.getFID(), gwf.getFK_Node(),
				gwf.getNodeName(), WebUser.getNo(), WebUser.getName(), gwf.getFK_Node(), gwf.getNodeName(), toEmps,
				names, gwf.getTitle(), null);

		return "已经成功的把工作抄送给:" + names;
	}

	/**
	 * 把抄送写入待办列表
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @param ccToEmpNo
	 *            抄送给
	 * @param ccToEmpName
	 *            抄送给名称
	 * @return
	 * @throws Exception
	 */
	public static String Node_CC_WriteTo_Todolist(int fk_node, long workID, String ccToEmpNo, String ccToEmpName)
			throws Exception {
		return Node_CC_WriteTo_CClist(fk_node, workID, ccToEmpNo, ccToEmpName, "", "");
	}

	/**
	 * 执行抄送
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param toEmpNo
	 *            抄送人员编号
	 * @param toEmpName
	 *            抄送人员人员名称
	 * @param msgTitle
	 *            标题
	 * @param msgDoc
	 *            内容
	 * @return 执行信息
	 * @throws Exception
	 */
	public static String Node_CC_WriteTo_CClist(int fk_node, long workID, String toEmpNo, String toEmpName,
			String msgTitle, String msgDoc) throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workID);
		if (gwf.RetrieveFromDBSources() == 0) {
			Node nd = new Node(fk_node);
			gwf.setFK_Node(fk_node);
			gwf.setFK_Flow(nd.getFK_Flow());
			gwf.setFlowName(nd.getFlowName());
			gwf.setNodeName(nd.getName());
		}

		Node fromNode = new Node(fk_node);

		CCList list = new CCList();
		list.setMyPK(String.valueOf(DBAccess.GenerOIDByGUID())); // workID + "_"
																	// + fk_node
																	// + "_" +
																	// empNo;
		list.setFK_Flow(gwf.getFK_Flow());
		list.setFlowName(gwf.getFlowName());
		list.setFK_Node(fk_node);
		list.setNodeName(gwf.getNodeName());
		list.setTitle(msgTitle);
		list.setDoc(msgDoc);
		list.setCCTo(toEmpNo);
		list.setCCToName(toEmpName);

		// 增加抄送人部门.
		Emp emp = new Emp(toEmpNo);
		list.setCCToDept(emp.getFK_Dept());
		list.setRDT(DataType.getCurrentDataTime());
		list.setRec(WebUser.getNo());
		list.setWorkID(gwf.getWorkID());
		list.setFID(gwf.getFID());
		list.setPFlowNo(gwf.getPFlowNo());
		list.setPWorkID(gwf.getPWorkID());
		// list.NDFrom = ndFrom;

		// 是否要写入待办.
		if (fromNode.getCCWriteTo() == CCWriteTo.CCList) {
			list.setInEmpWorks(false); // added by liuxc,2015.7.6
		} else {
			list.setInEmpWorks(true); // added by liuxc,2015.7.6
		}

		// 写入待办和写入待办与抄送列表,状态不同
		if (fromNode.getCCWriteTo() == CCWriteTo.All || fromNode.getCCWriteTo() == CCWriteTo.Todolist) {
			list.setHisSta(fromNode.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
		}

		if (fromNode.getIsEndNode() == true) // 结束节点只写入抄送列表
		{
			list.setHisSta(CCSta.UnRead);
			list.setInEmpWorks(false);
		}

		try {
			list.Insert();
		} catch (java.lang.Exception e) {
			list.CheckPhysicsTable();
			list.Update();
		}

		//
		bp.wf.Dev2Interface.Port_SendMsg(toEmpNo, msgTitle, msgDoc, "CC" + gwf.getFK_Node() + "_" + gwf.getWorkID(),
				SMSMsgType.CC, gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getWorkID(), gwf.getFID());

		// 记录日志.
		Glo.AddToTrack(ActionType.CC, gwf.getFK_Flow(), workID, gwf.getFID(), gwf.getFK_Node(), gwf.getNodeName(),
				WebUser.getNo(), WebUser.getName(), gwf.getFK_Node(), gwf.getNodeName(), toEmpNo, toEmpName, msgTitle,
				null);

		return "已经成功的把工作抄送给:" + toEmpNo + "," + toEmpName;
	}

	/**
	 * 执行抄送
	 * 
	 * @param fk_node
	 *            节点
	 * @param workID
	 *            工作ID
	 * @param title
	 *            标题
	 * @param doc
	 *            内容
	 * @param toEmps
	 *            到人员(zhangsan,张三;lisi,李四;wangwu,王五;)
	 * @param toDepts
	 *            到部门，格式:001,002,003
	 * @param toStations
	 *            到岗位 格式:001,002,003
	 * @param toStations
	 *            到权限组 格式:001,002,003
	 * @throws Exception
	 */

	public static String Node_CC_WriteTo_CClist(int fk_node, long workID, String title, String doc, String toEmps,
			String toDepts, String toStations) throws Exception {
		return Node_CC_WriteTo_CClist(fk_node, workID, title, doc, toEmps, toDepts, toStations, null);
	}

	public static String Node_CC_WriteTo_CClist(int fk_node, long workID, String title, String doc, String toEmps)
			throws Exception {
		return Node_CC_WriteTo_CClist(fk_node, workID, title, doc, toEmps, null, null, null);
	}

	public static String Node_CC_WriteTo_CClist(int fk_node, long workID, String title, String doc) throws Exception {
		return Node_CC_WriteTo_CClist(fk_node, workID, title, doc, null, null, null, null);
	}

	public static String Node_CC_WriteTo_CClist(int fk_node, long workID, String title, String doc, String toEmps,
			String toDepts, String toStations, String toGroups) throws Exception {

		Node nd = new Node(fk_node);

		// 计算出来曾经抄送过的人.
		String sql = "SELECT CCTo FROM WF_CCList WHERE FK_Node=" + fk_node + " AND WorkID=" + workID;
		DataTable mydt = DBAccess.RunSQLReturnTable(sql);
		String toAllEmps = ",";
		for (DataRow dr : mydt.Rows) {
			toAllEmps += dr.getValue(0).toString() + ",";
		}

		// 录制本次抄送的人员.
		String ccRec = "";

		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workID);
		if (gwf.RetrieveFromDBSources() == 0) {
			gwf.setFK_Node(fk_node);
			gwf.setFK_Flow(nd.getFK_Flow());
			gwf.setFlowName(nd.getFlowName());
			gwf.setNodeName(nd.getName());
		}

		/// 处理抄送到人员.
		if (toEmps != null) {
			String[] emps = toEmps.split("[;]", -1);
			for (String empStr : emps) {
				if (DataType.IsNullOrEmpty(empStr) == true || empStr.contains(",") == false) {
					continue;
				}

				String[] strs = empStr.split("[,]", -1);
				String empNo = strs[0];
				String empName = strs[1];

				if (toAllEmps.contains("," + empNo + ",") == true) {
					continue;
				}

				CCList list = new CCList();
				list.setMyPK(String.valueOf(DBAccess.GenerOIDByGUID())); // workID
																			// +
																			// "_"
																			// +
																			// fk_node
																			// +
																			// "_"
																			// +
																			// empNo;
				list.setFK_Flow(gwf.getFK_Flow());
				list.setFlowName(gwf.getFlowName());
				list.setFK_Node(fk_node);
				list.setNodeName(gwf.getNodeName());
				list.setTitle(title);
				list.setDoc(doc);
				list.setCCTo(empNo);
				list.setCCToName(empName);
				list.setRDT(DataType.getCurrentDataTime());
				list.setRec(WebUser.getNo());
				list.setWorkID(gwf.getWorkID());
				list.setFID(gwf.getFID());
				list.setPFlowNo(gwf.getPFlowNo());
				list.setPWorkID(gwf.getPWorkID());
				// list.NDFrom = ndFrom;

				// 是否要写入待办.
				if (nd.getCCWriteTo() == CCWriteTo.CCList) {
					list.setInEmpWorks(false); // added by liuxc,2015.7.6
				} else {
					list.setInEmpWorks(true); // added by liuxc,2015.7.6
				}

				// 写入待办和写入待办与抄送列表,状态不同
				if (nd.getCCWriteTo() == CCWriteTo.All || nd.getCCWriteTo() == CCWriteTo.Todolist) {
					list.setHisSta(nd.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
				}

				if (nd.getIsEndNode() == true) // 结束节点只写入抄送列表
				{
					list.setHisSta(CCSta.UnRead);
					list.setInEmpWorks(false);
				}

				try {
					list.Insert();
				} catch (java.lang.Exception e) {
					list.CheckPhysicsTable();
					list.Update();
				}

				ccRec += "" + list.getCCToName() + ";";
				// 人员编号,加入这个集合.
				toAllEmps += empNo + ",";
			}
		}

		/// 处理抄送到人员.

		/// 处理抄送到部门.
		if (toDepts != null) {
			toDepts = toDepts.replace(";", ",");

			String[] depts = toDepts.split("[,]", -1);
			for (String deptNo : depts) {
				if (DataType.IsNullOrEmpty(deptNo) == true) {
					continue;
				}

				sql = "SELECT No,Name,FK_Dept FROM Port_Emp WHERE FK_Dept='" + deptNo + "'";
				DataTable dt = DBAccess.RunSQLReturnTable(sql);
				for (DataRow dr : dt.Rows) {
					String empNo = dr.getValue(0).toString();
					String empName = dr.getValue(1).toString();
					if (toAllEmps.contains("," + empNo + ",") == true) {
						continue;
					}

					CCList list = new CCList();
					list.setMyPK(String.valueOf(DBAccess.GenerOIDByGUID())); // workID
																				// +
																				// "_"
																				// +
																				// fk_node
																				// +
																				// "_"
																				// +
																				// empNo;
					list.setFK_Flow(gwf.getFK_Flow());
					list.setFlowName(gwf.getFlowName());
					list.setFK_Node(fk_node);
					list.setNodeName(gwf.getNodeName());
					list.setTitle(title);
					list.setDoc(doc);
					list.setCCTo(empNo);
					list.setCCToName(empName);
					list.setRDT(DataType.getCurrentDataTime());
					list.setRec(WebUser.getNo());
					list.setWorkID(gwf.getWorkID());
					list.setFID(gwf.getFID());
					list.setPFlowNo(gwf.getPFlowNo());
					list.setPWorkID(gwf.getPWorkID());
					// list.NDFrom = ndFrom;

					// 是否要写入待办.
					if (nd.getCCWriteTo() == CCWriteTo.CCList) {
						list.setInEmpWorks(false); // added by liuxc,2015.7.6
					} else {
						list.setInEmpWorks(true); // added by liuxc,2015.7.6
					}

					// 写入待办和写入待办与抄送列表,状态不同
					if (nd.getCCWriteTo() == CCWriteTo.All || nd.getCCWriteTo() == CCWriteTo.Todolist) {
						list.setHisSta(nd.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
					}

					if (nd.getIsEndNode() == true) // 结束节点只写入抄送列表
					{
						list.setHisSta(CCSta.UnRead);
						list.setInEmpWorks(false);
					}

					try {
						list.Insert();
					} catch (java.lang.Exception e2) {
						list.CheckPhysicsTable();
						list.Update();
					}

					// 录制本次抄送到的人员.
					ccRec += "" + list.getCCToName() + ";";

					// 人员编号,加入这个集合.
					toAllEmps += empNo + ",";
				}
			}
		}

		/// 处理抄送到部门.

		/// 处理抄送到岗位.
		if (toStations != null) {
			toStations = toStations.replace(";", ",");
			String[] stas = toStations.split("[,]", -1);
			for (String staNo : stas) {
				if (DataType.IsNullOrEmpty(staNo) == true) {
					continue;
				}

				sql = "SELECT No,Name, a.FK_Dept FROM Port_Emp a, " + Glo.getEmpStation()
						+ " B  WHERE a.No=B.FK_Emp AND B.FK_Station='" + staNo + "'";

				DataTable dt = DBAccess.RunSQLReturnTable(sql);
				for (DataRow dr : dt.Rows) {
					String empNo = dr.getValue(0).toString();
					String empName = dr.getValue(1).toString();
					if (toAllEmps.contains("," + empNo + ",") == true) {
						continue;
					}

					CCList list = new CCList();
					list.setMyPK(String.valueOf(DBAccess.GenerOIDByGUID())); // workID
																				// +
																				// "_"
																				// +
																				// fk_node
																				// +
																				// "_"
																				// +
																				// empNo;
					list.setFK_Flow(gwf.getFK_Flow());
					list.setFlowName(gwf.getFlowName());
					list.setFK_Node(fk_node);
					list.setNodeName(gwf.getNodeName());
					list.setTitle(title);
					list.setDoc(doc);
					list.setCCTo(empNo);
					list.setCCToName(empName);
					list.setRDT(DataType.getCurrentDataTime());
					list.setRec(WebUser.getNo());
					list.setWorkID(gwf.getWorkID());
					list.setFID(gwf.getFID());
					list.setPFlowNo(gwf.getPFlowNo());
					list.setPWorkID(gwf.getPWorkID());
					// list.NDFrom = ndFrom;

					// 是否要写入待办.
					if (nd.getCCWriteTo() == CCWriteTo.CCList) {
						list.setInEmpWorks(false); // added by liuxc,2015.7.6
					} else {
						list.setInEmpWorks(true); // added by liuxc,2015.7.6
					}

					// 写入待办和写入待办与抄送列表,状态不同
					if (nd.getCCWriteTo() == CCWriteTo.All || nd.getCCWriteTo() == CCWriteTo.Todolist) {
						list.setHisSta(nd.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
					}

					if (nd.getIsEndNode() == true) // 结束节点只写入抄送列表
					{
						list.setHisSta(CCSta.UnRead);
						list.setInEmpWorks(false);
					}

					try {
						list.Insert();
					} catch (java.lang.Exception e3) {
						list.CheckPhysicsTable();
						list.Update();
					}

					// 录制本次抄送到的人员.
					ccRec += "" + list.getCCToName() + ";";

					// 人员编号,加入这个集合.
					toAllEmps += empNo + ",";
				}
			}
		}

		/// .

		/// 抄送到组.
		if (toGroups != null) {
			toGroups = toGroups.replace(";", ",");
			String[] groups = toGroups.split("[,]", -1);

			for (String group : groups) {
				if (DataType.IsNullOrEmpty(group) == true) {
					continue;
				}

				// 解决分组下的岗位人员.
				sql = "SELECT a.No,a.Name, A.FK_Dept FROM Port_Emp A, " + Glo.getEmpStation()
						+ " B, GPM_GroupStation C  WHERE A.No=B.FK_Emp AND B.FK_Station=C.FK_Station AND C.FK_Group='"
						+ group + "'";
				sql += " UNION ";
				sql += "SELECT A.No, A.Name, A.FK_Dept FROM Port_Emp A, Port_TeamEmp B  WHERE A.No=B.FK_Emp AND B.FK_Group='"
						+ group + "'";

				DataTable dt = DBAccess.RunSQLReturnTable(sql);
				for (DataRow dr : dt.Rows) {
					String empNo = dr.getValue(0).toString();
					String empName = dr.getValue(1).toString();
					if (toAllEmps.contains("," + empNo + ",") == true) {
						continue;
					}

					CCList list = new CCList();
					list.setMyPK(String.valueOf(DBAccess.GenerOIDByGUID())); // workID
																				// +
																				// "_"
																				// +
																				// fk_node
																				// +
																				// "_"
																				// +
																				// empNo;
					list.setFK_Flow(gwf.getFK_Flow());
					list.setFlowName(gwf.getFlowName());
					list.setFK_Node(fk_node);
					list.setNodeName(gwf.getNodeName());
					list.setTitle(title);
					list.setDoc(doc);
					list.setCCTo(empNo);
					list.setCCToName(empName);
					list.setRDT(DataType.getCurrentDataTime());
					list.setRec(WebUser.getNo());
					list.setWorkID(gwf.getWorkID());
					list.setFID(gwf.getFID());
					list.setPFlowNo(gwf.getPFlowNo());
					list.setPWorkID(gwf.getPWorkID());
					// list.NDFrom = ndFrom;

					// 是否要写入待办.
					if (nd.getCCWriteTo() == CCWriteTo.CCList) {
						list.setInEmpWorks(false); // added by liuxc,2015.7.6
					} else {
						list.setInEmpWorks(true); // added by liuxc,2015.7.6
					}

					// 写入待办和写入待办与抄送列表,状态不同
					if (nd.getCCWriteTo() == CCWriteTo.All || nd.getCCWriteTo() == CCWriteTo.Todolist) {
						list.setHisSta(nd.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
					}

					if (nd.getIsEndNode() == true) // 结束节点只写入抄送列表
					{
						list.setHisSta(CCSta.UnRead);
						list.setInEmpWorks(false);
					}

					try {
						list.Insert();
					} catch (java.lang.Exception e4) {
						list.CheckPhysicsTable();
						list.Update();
					}

					// 录制本次抄送到的人员.
					ccRec += "" + list.getCCToName() + ";";

					// 人员编号,加入这个集合.
					toAllEmps += empNo + ",";
				}
			}
		}

		/// 抄送到组

		return ccRec;

	}

	/**
	 * 执行删除
	 * 
	 * @param mypk
	 *            删除
	 */
	public static void Node_CC_DoDel(String mypk) {
		Paras ps = new Paras();
		ps.SQL = "DELETE FROM WF_CCList WHERE MyPK=" + SystemConfig.getAppCenterDBVarStr() + "MyPK";
		ps.Add(CCListAttr.MyPK, mypk);
		DBAccess.RunSQL(ps);
	}

	/**
	 * 设置抄送状态
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @param empNo
	 *            人员编号
	 * @param sta
	 *            状态
	 */
	public static void Node_CC_SetSta(int nodeID, long workid, String empNo, CCSta sta) {
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_CCList   SET Sta=" + dbstr + "Sta,CDT=" + dbstr + "CDT WHERE WorkID=" + dbstr
				+ "WorkID AND FK_Node=" + dbstr + "FK_Node AND CCTo=" + dbstr + "CCTo";
		ps.Add(CCListAttr.Sta, sta.getValue());
		ps.Add(CCListAttr.CDT, DataType.getCurrentDataTime());
		ps.Add(CCListAttr.WorkID, workid);
		ps.Add(CCListAttr.FK_Node, nodeID);
		ps.Add(CCListAttr.CCTo, empNo);
		DBAccess.RunSQL(ps);
	}

	/**
	 * 执行读取
	 * 
	 * @param mypk
	 *            主键
	 */

	public static void Node_CC_SetRead(String mypk) {
		Node_CC_SetRead(mypk, null);
	}

	// ORIGINAL LINE: public static void Node_CC_SetRead(string mypk, string
	// bbsSetInfo = null)
	public static void Node_CC_SetRead(String mypk, String bbsSetInfo) {
		if (DataType.IsNullOrEmpty(mypk)) {
			return;
		}

		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_CCList SET Sta=" + SystemConfig.getAppCenterDBVarStr() + "Sta,ReadDT="
				+ SystemConfig.getAppCenterDBVarStr() + "ReadDT  WHERE MyPK=" + SystemConfig.getAppCenterDBVarStr()
				+ "MyPK";
		ps.Add(CCListAttr.Sta, CCSta.Read.getValue());
		ps.Add(CCListAttr.ReadDT, DataType.getCurrentDataTime()); // 设置读取日期.
		ps.Add(CCListAttr.MyPK, mypk);
		DBAccess.RunSQL(ps);
	}

	/**
	 * 设置抄送执行完成
	 * 
	 * @param workid
	 *            工作ID
	 * @param checkInfo
	 *            审核信息
	 * @throws Exception
	 */

	public static String Node_CC_SetCheckOver(long workid) throws Exception {
		return Node_CC_SetCheckOver(workid, null);
	}

	public static String Node_CC_SetCheckOver(long workid, String checkInfo) throws Exception {
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_CCList SET Sta=" + SystemConfig.getAppCenterDBVarStr() + "Sta,CDT="
				+ SystemConfig.getAppCenterDBVarStr() + "CDT  WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr()
				+ "WorkID AND CCTo=" + SystemConfig.getAppCenterDBVarStr() + "CCTo ";
		ps.Add(CCListAttr.Sta, CCSta.CheckOver.getValue());
		ps.Add(CCListAttr.CDT, DataType.getCurrentDataTime()); // 设置完成日期.
		ps.Add(CCListAttr.WorkID, workid);
		ps.Add(CCListAttr.CCTo, WebUser.getNo());
		int val = DBAccess.RunSQL(ps);
		if (val == 0) {
			return "err@执行失败,没有更新到workid=" + workid + ",CCTo=" + WebUser.getNo() + ", 的抄送数据.";
		}

		GenerWorkFlow gwf = new GenerWorkFlow(workid);

		bp.wf.Dev2Interface.Flow_BBSAdd(gwf.getFK_Flow(), workid, gwf.getFID(), checkInfo, WebUser.getNo(),
				WebUser.getName());
		return "执行成功.";
	}

	/**
	 * 设置抄送执行完成
	 * 
	 * @param workid
	 *            工作ID
	 * @param checkInfo
	 *            审核信息
	 * @throws Exception
	 */

	public static String Node_CC_SetCheckOver(String flowNo, long workid, long fid, String checkInfo, String empNo)
			throws Exception {
		return Node_CC_SetCheckOver(flowNo, workid, fid, checkInfo, empNo, null);
	}

	public static String Node_CC_SetCheckOver(String flowNo, long workid, long fid, String checkInfo) throws Exception {
		return Node_CC_SetCheckOver(flowNo, workid, fid, checkInfo, null, null);
	}

	public static String Node_CC_SetCheckOver(String flowNo, long workid, long fid) throws Exception {
		return Node_CC_SetCheckOver(flowNo, workid, fid, null, null, null);
	}

	public static String Node_CC_SetCheckOver(String flowNo, long workid, long fid, String checkInfo, String empNo,
			String empName) throws Exception {
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_CCList SET Sta=" + SystemConfig.getAppCenterDBVarStr() + "Sta,CDT="
				+ SystemConfig.getAppCenterDBVarStr() + "CDT  WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr()
				+ "WorkID AND CCTo=" + SystemConfig.getAppCenterDBVarStr() + "CCTo ";
		ps.Add(CCListAttr.Sta, CCSta.CheckOver.getValue());
		ps.Add(CCListAttr.CDT, DataType.getCurrentDataTime()); // 设置完成日期.
		ps.Add(CCListAttr.WorkID, workid);
		ps.Add(CCListAttr.CCTo, WebUser.getNo());
		int val = DBAccess.RunSQL(ps);
		if (val == 0) {
			return "err@执行失败,没有更新到workid=" + workid + ",CCTo=" + WebUser.getNo() + ", 的抄送数据.";
		}

		bp.wf.Dev2Interface.Flow_BBSAdd(flowNo, workid, fid, checkInfo, empNo, empName);
		return "执行成功.";
	}

	/**
	 * 批量审核
	 * 
	 * @param workids
	 *            多个工作ID使用逗号分割比如:'111,233,444'
	 * @param checkInfo
	 *            批量审核意见
	 * @throws Exception
	 */

	public static String Node_CC_SetCheckOverBatch(String workids) throws Exception {
		return Node_CC_SetCheckOverBatch(workids, null);
	}

	public static String Node_CC_SetCheckOverBatch(String workids, String checkInfo) throws Exception {
		if (checkInfo == null) {
			checkInfo = "已阅";
		}

		String[] ids = workids.split("[,]", -1);
		String info = "";
		for (String id : ids) {
			if (DataType.IsNullOrEmpty(id) == true) {
				continue;
			}

			GenerWorkFlow gwf = new GenerWorkFlow(Long.parseLong(id));

			// 表单方案.
			String frmID = null;
			FrmNodes fns = new FrmNodes();
			fns.Retrieve(FrmNodeAttr.FK_Node, gwf.getFK_Node());
			for (FrmNode fn : fns.ToJavaList()) {
				if (fn.getFK_Frm().equals("ND" + gwf.getFK_Node()) == true) {
					continue;
				}

				frmID = fn.getFK_Frm();
				break;
			}

			if (frmID == null) {
				Node_CC_SetCheckOver(gwf.getWorkID(), checkInfo);
				continue;
			}

			// 设置阅读track. 这里已经设置了已阅.
			bp.wf.Dev2Interface.Track_WriteBBS(frmID, frmID, gwf.getWorkID(), checkInfo, gwf.getFID(), gwf.getFK_Flow(),
					gwf.getFlowName(), gwf.getFK_Node(), gwf.getNodeName());

			// info += Node_CC_SetCheckOver(Int64.Parse(id), checkInfo);
		}
		return "执行成功.";
	}

	/**
	 * 设置抄送读取
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @param empNo
	 *            读取人员编号
	 */

	public static void Node_CC_SetRead(int nodeID, long workid, String empNo) {
		Node_CC_SetRead(nodeID, workid, empNo, null);
	}

	// ORIGINAL LINE: public static void Node_CC_SetRead(int nodeID, Int64
	// workid, string empNo, string bbsCheckInfo = null)
	public static void Node_CC_SetRead(int nodeID, long workid, String empNo, String bbsCheckInfo) {
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_CCList SET Sta=" + SystemConfig.getAppCenterDBVarStr() + "Sta,ReadDT="
				+ SystemConfig.getAppCenterDBVarStr() + "ReadDT  WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr()
				+ "WorkID AND FK_Node=" + SystemConfig.getAppCenterDBVarStr() + "FK_Node AND CCTo="
				+ SystemConfig.getAppCenterDBVarStr() + "CCTo";
		ps.Add(CCListAttr.Sta, CCSta.UnRead.getValue());
		ps.Add(CCListAttr.ReadDT, DataType.getCurrentDataTime()); // 设置读取日期.
		ps.Add(CCListAttr.WorkID, workid);
		ps.Add(CCListAttr.FK_Node, nodeID);
		ps.Add(CCListAttr.CCTo, empNo);

		ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkerlist SET IsRead=1 WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr()
				+ "WorkID AND FK_Node=" + SystemConfig.getAppCenterDBVarStr() + "FK_Node AND FK_Emp="
				+ SystemConfig.getAppCenterDBVarStr() + "FK_Emp";
		ps.Add(GenerWorkerListAttr.WorkID, workid);
		ps.Add(GenerWorkerListAttr.FK_Node, nodeID);
		ps.Add(GenerWorkerListAttr.FK_Emp, empNo);
		DBAccess.RunSQL(ps);

		// if (bbsCheckInfo!=null)
		// BP.WF.Dev2Interface.Track_WriteBBS()
	}

	/**
	 * 执行抄送
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param fk_node
	 *            节点编号
	 * @param workID
	 *            工作ID
	 * @param toEmpNo
	 *            抄送给人员编号
	 * @param toEmpName
	 *            抄送给人员名称
	 * @param msgTitle
	 *            消息标题
	 * @param msgDoc
	 *            消息内容
	 * @param pFlowNo
	 *            父流程编号(可以为null)
	 * @param pWorkID
	 *            父流程WorkID(可以为0)
	 * @return
	 * @throws Exception
	 */

	public static String Node_CC(String fk_flow, int fk_node, long workID, String toEmpNo, String toEmpName,
			String msgTitle, String msgDoc, String pFlowNo) throws Exception {
		return Node_CC(fk_flow, fk_node, workID, toEmpNo, toEmpName, msgTitle, msgDoc, pFlowNo, 0);
	}

	public static String Node_CC(String fk_flow, int fk_node, long workID, String toEmpNo, String toEmpName,
			String msgTitle, String msgDoc) throws Exception {
		return Node_CC(fk_flow, fk_node, workID, toEmpNo, toEmpName, msgTitle, msgDoc, null, 0);
	}

	public static String Node_CC(String fk_flow, int fk_node, long workID, String toEmpNo, String toEmpName,
			String msgTitle, String msgDoc, String pFlowNo, long pWorkID) throws Exception {
		Flow fl = new Flow(fk_flow);
		Node nd = new Node(fk_node);

		GenerWorkFlow gwf = new GenerWorkFlow(workID);

		CCList list = new CCList();
		// list.setMyPK(DBAccess.GenerOIDByGUID().ToString(); // workID + "_" +
		// fk_node + "_" + empNo;
		list.setMyPK(workID + "_" + fk_node + "_" + toEmpNo);

		list.setFK_Flow(fk_flow);
		list.setFlowName(fl.getName());
		list.setFK_Node(fk_node);
		list.setNodeName(nd.getName());
		list.setTitle(msgTitle);
		list.setDoc(msgDoc);
		list.setCCTo(toEmpNo);
		list.setCCToName(toEmpName);
		list.setInEmpWorks(nd.getCCWriteTo() == CCWriteTo.CCList ? false : true); // added
																					// by
																					// liuxc,2015.7.6
		// 写入待办和写入待办与抄送列表,状态不同
		if (nd.getCCWriteTo() == CCWriteTo.All || nd.getCCWriteTo() == CCWriteTo.Todolist) {
			list.setHisSta(nd.getCCWriteTo() == CCWriteTo.All ? CCSta.UnRead : CCSta.Read);
		}
		if (nd.getIsEndNode() == true) // 结束节点只写入抄送列表
		{
			list.setHisSta(CCSta.UnRead);
			list.setInEmpWorks(false);
		}
		// 增加抄送人部门.
		Emp emp = new Emp(toEmpNo);
		list.setCCToDept(emp.getFK_Dept());
		list.setCCToDeptName(emp.getFK_DeptText());

		//
		if (bp.wf.Glo.getCCBPMRunModel() != CCBPMRunModel.Single) {
			bp.gpm.Emp gpmEmp = new bp.gpm.Emp(toEmpNo);
			list.setCCToOrgNo(gpmEmp.getOrgNo());

			Org org = new Org(gpmEmp.getOrgNo());
			list.setCCToOrgName(org.getName());
		}

		list.setRDT(DataType.getCurrentDataTime()); // 抄送日期.
		list.setRec(WebUser.getNo());
		list.setWorkID(workID);
		list.setFID(0);
		list.setPFlowNo(pFlowNo);
		list.setPWorkID(pWorkID);

		list.setDomain(gwf.getDomain());
		list.setOrgNo(gwf.getOrgNo()); // 设置组织编号.

		try {
			list.Insert();
		} catch (java.lang.Exception e) {
			// list.CheckPhysicsTable();
			list.Update();
		}

		// 记录日志.
		Glo.AddToTrack(ActionType.CC, fk_flow, workID, 0, nd.getNodeID(), nd.getName(), WebUser.getNo(),
				WebUser.getName(), nd.getNodeID(), nd.getName(), toEmpNo, toEmpName, msgTitle, null);

		// 发送邮件.
		bp.wf.Dev2Interface.Port_SendMsg(toEmpNo, WebUser.getName() + "把工作:" + gwf.getTitle(), "抄送:" + msgTitle,
				"CC" + nd.getNodeID() + "_" + workID + "_", bp.wf.SMSMsgType.CC, gwf.getFK_Flow(), gwf.getFK_Node(),
				gwf.getWorkID(), gwf.getFID());

		return "已经成功的把工作抄送给:" + toEmpNo + "," + toEmpName;

	}

	/**
	 * 删除草稿
	 * 
	 * @param workID
	 *            工作ID
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static void Node_DeleteDraft(long workID) throws NumberFormatException, Exception {
		// 设置引擎表.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workID);
		if (gwf.RetrieveFromDBSources() == 1) {
			if (gwf.getFK_Node() != Integer.parseInt(gwf.getFK_Flow() + "01")) {
				throw new RuntimeException("@该流程非草稿流程不能删除:" + gwf.getTitle());
			}

			if (gwf.getWFState() != WFState.Draft) {
				throw new RuntimeException("@非草稿状态不能删除");
			}

			gwf.Delete();
		}

		// 删除流程.
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Flow fl = new Flow(gwf.getFK_Flow());
		Paras ps = new Paras();
		ps.SQL = "DELETE FROM " + fl.getPTable() + " WHERE OID=" + dbstr + "OID ";
		ps.Add(GERptAttr.OID, workID);
		DBAccess.RunSQL(ps);
	}

public static void Node_SetDraft2Todolist(String fk_flow, long workID,String todoEmpNo, String todoEmpName) throws Exception {
		
		
		// 设置引擎表.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workID);
		if (gwf.RetrieveFromDBSources() == 1 && (gwf.getWFState() == WFState.Draft 
				|| gwf.getWFState() == WFState.Blank
				|| gwf.getWFState() == WFState.Runing)) {
			if (gwf.getFK_Node() != Integer.parseInt(fk_flow + "01")) {
				throw new RuntimeException("@设置待办错误，只有在开始节点时才能设置待办，现在的节点是:" + gwf.getNodeName());
			}
			 
			
			NodeSimple nd=new NodeSimple(gwf.getFK_Node());

			gwf.setTodoEmps( todoEmpNo + "," +todoEmpName + ";");
			gwf.setTodoEmpsNum(1);
			gwf.setWFState(WFState.Runing);
			gwf.setStarter(todoEmpNo);
			gwf.setStarterName(todoEmpName);
			gwf.setNodeName(nd.getName());
			gwf.Update();
			
			
			//处理工作人员.
			GenerWorkerList gwl=new GenerWorkerList();
			gwl.setWorkID(workID);
			gwl.setFK_Node(gwf.getFK_Node());
			gwl.setFK_Emp(todoEmpNo);
			if (gwl.RetrieveFromDBSources()==1)
			{
				gwl.setIsPass(false);
				gwl.setRDT(DataType.getCurrentDataTime());
				gwl.setFK_EmpText(todoEmpName);
				gwl.setFK_NodeText(nd.getName() );
				gwl.setFK_Flow( gwf.getFK_Flow());
				gwl.Update();
			}else
			{
				gwl.setIsPass(false);
				gwl.setRDT(DataType.getCurrentDataTime());
				gwl.setFK_EmpText(todoEmpName);
				gwl.setFK_NodeText(nd.getName() );
				gwl.setFK_Flow( gwf.getFK_Flow());
				gwl.Insert();
			}
				 
			// 重置标题
			Flow_ReSetFlowTitle(fk_flow, gwf.getFK_Node(), gwf.getWorkID());
			  
		}
	}


	/**
	 * 设置当前工作的应该完成日期.
	 * 
	 * @param workID
	 *            设置的WorkID.
	 * @param sdt
	 *            应完成日期
	 */
	public static void Node_SetSDT(long workID, String sdt) {
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkerlist SET SDT=" + SystemConfig.getAppCenterDBVarStr() + "SDT WHERE WorkID="
				+ SystemConfig.getAppCenterDBVarStr() + "WorkID AND IsPass=0";
		ps.Add("SDT", sdt);
		ps.Add("WorkID", workID);
		DBAccess.RunSQL(ps);

		ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkFlow SET SDTOfNode=" + SystemConfig.getAppCenterDBVarStr()
				+ "SDTOfNode WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr() + "WorkID ";
		ps.Add("SDTOfNode", sdt);
		ps.Add("WorkID", workID);
		DBAccess.RunSQL(ps);

	}

	/**
	 * 设置当前工作状态为草稿,如果启用了草稿, 请在开始节点的表单保存按钮下增加上它. 注意:必须是在开始节点时调用.
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @throws Exception
	 */
	public static void Node_SetDraft(String fk_flow, long workID) throws Exception {
		// 设置引擎表.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workID);
		if (gwf.RetrieveFromDBSources() == 0) {
			throw new RuntimeException("@工作丢失..");
		}

		if (gwf.getWFState() == WFState.Blank) {
			if (gwf.getFK_Node() != Integer.parseInt(fk_flow + "01")) {
				throw new RuntimeException("@设置草稿错误，只有在开始节点时才能设置草稿，现在的节点是:" + gwf.getTitle());
			}

			gwf.setTodoEmps(WebUser.getNo() + "," + WebUser.getName() + ";");
			gwf.setTodoEmpsNum(1);
			gwf.setWFState(WFState.Draft);
			gwf.Update();

			GenerWorkerList gwl = new GenerWorkerList();
			gwl.setWorkID(workID);
			gwl.setFK_Node(Integer.parseInt(fk_flow + "01"));
			gwl.setFK_Emp(WebUser.getNo());
			if (gwl.RetrieveFromDBSources() == 0) {
				gwl.setFK_EmpText(WebUser.getName());
				gwl.setIsPassInt(0);
				gwl.setSDT(DataType.getCurrentDataTimess());
				gwl.setDTOfWarning(DataType.getCurrentDataTime());
				gwl.setIsEnable(true);
				gwl.setIsRead(true);
				gwl.setIsPass(false);
				gwl.Insert();
			}
		}

		Flow fl = new Flow(fk_flow);
		// string sql = "UPDATE "+fl.PTable+" SET WFStarter=1,
		// FlowStater='"+WebUser.getNo()+"' WHERE OID="+workID;

		String sql = "UPDATE " + fl.getPTable() + " SET  FlowStarter='" + WebUser.getNo() + "',WFState=1 WHERE OID="
				+ workID;
		DBAccess.RunSQL(sql);
	}

	/**
	 * 保存参数，向工作流引擎传入的参数变量.
	 * 
	 * @param workID
	 *            工作ID
	 * @param paras
	 *            参数
	 * @return
	 * @throws Exception
	 */
	public static boolean Flow_SaveParas(long workID, String paras) throws Exception {
		AtPara ap = new AtPara(paras);
		GenerWorkFlow gwf = new GenerWorkFlow(workID);
		for (String key : ap.getHisHT().keySet()) {
			gwf.SetPara(key, ap.GetValStrByKey(key));
		}
		gwf.Update();
		return true;
	}

	/**
	 * 保存
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @return 返回保存的信息
	 * @throws Exception
	 */
	public static String Node_SaveWork(String fk_flow, int fk_node, long workID) throws Exception {
		return Node_SaveWork(fk_flow, fk_node, workID, new Hashtable(), null, 0, 0);
	}

	/**
	 * 保存
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            workid
	 * @param wk
	 *            节点表单参数
	 * @return
	 * @throws Exception
	 */
	public static String Node_SaveWork(String fk_flow, int fk_node, long workID, Hashtable wk) throws Exception {
		return Node_SaveWork(fk_flow, fk_node, workID, wk, null, 0, 0);
	}

	/**
	 * 保存
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @param htWork
	 *            工作数据
	 * @return 返回执行信息
	 * @throws Exception
	 */
	public static String Node_SaveWork(String fk_flow, int fk_node, long workID, Hashtable htWork, DataSet dsDtls,
			long fid, long pworkid) throws Exception {
		if (htWork == null) {
			throw new RuntimeException("参数错误，htWork 不能为空, 保存失败。");
		}

		try {
			Node nd = new Node(fk_node);
			if (nd.getIsStartNode() == false) {
				if (nd.getIsEndNode() == false) {
					if (Dev2Interface.Flow_IsCanDoCurrentWork(workID, WebUser.getNo()) == false) {
						throw new RuntimeException("err@工作已经发送到下一个环节,您不能执行保存.");
					}
				}
			}

			if (nd.getHisFormType() == NodeFormType.RefOneFrmTree) {
				FrmNode frmNode = new FrmNode(nd.getNodeID(), nd.getNodeFrmID());
				switch (frmNode.getWhoIsPK()) {
				case FID:
					workID = fid;
					break;
				case PWorkID:
					workID = pworkid;
					break;
				case P2WorkID:
					GenerWorkFlow gwf = new GenerWorkFlow(pworkid);
					workID = gwf.getPWorkID();
					break;
				case P3WorkID:
					String sql = "Select PWorkID From WF_GenerWorkFlow Where WorkID=(Select PWorkID From WF_GenerWorkFlow Where WorkID="
							+ pworkid + ")";
					workID = DBAccess.RunSQLReturnValInt(sql, 0);
					break;
				default:
					break;
				}
			}
			Work wk = nd.getHisWork();
			if (workID != 0) {
				wk.setOID(workID);
				wk.RetrieveFromDBSources();
			}
			wk.ResetDefaultVal();

			/// 赋值.
			Attrs attrs = wk.getEnMap().getAttrs();
			for (Object str : htWork.keySet()) {
				if (str == null)
					continue;
				switch (str.toString()) {
				case GERptAttr.OID:
				case WorkAttr.MD5:
				case WorkAttr.Emps:
				case GERptAttr.FID:
				case GERptAttr.FK_Dept:
				case GERptAttr.Rec:
				case GERptAttr.Title:
					continue;
				default:
					break;
				}

				if (wk.getRow().containsKey(str)) {
					// if (nd.IsStartNode == true)
					// {
					// Attr attr = attrs.GetAttrByKey(str);
					// string defVal = attrs.GetAttrByKey(str).DefaultValOfReal;
					// if (attr.getUIIsReadonly() == true && defVal != null &&
					// defVal.Equals("@RDT") == true)
					// {
					// if (attr.getMyDataType() == DataType.AppDate)
					// wk.SetValByKey(attr.getKey(), DataType.getCurrentDate());
					// if (attr.getMyDataType() == DataType.AppDateTime)
					// wk.SetValByKey(attr.getKey(),
					// DataType.getCurrentDataTime());
					// continue;
					// }
					// }
					wk.SetValByKey(str.toString(), htWork.get(str.toString()));
				} else {
					wk.getRow().put(str.toString(), htWork.get(str.toString()));
				}
			}

			/// 赋值.

			wk.setRec(WebUser.getNo());
			// wk.RecText = WebUser.getName();
			wk.SetValByKey(GERptAttr.FK_Dept, WebUser.getFK_Dept());
			wk.Save();

			/// 保存从表
			if (dsDtls != null) {
				// 保存从表
				for (DataTable dt : dsDtls.Tables) {
					for (MapDtl dtl : wk.getHisMapDtls().ToJavaList()) {
						if (!dt.TableName.equals(dtl.getNo())) {
							continue;
						}
						// 获取dtls
						GEDtls daDtls = new GEDtls(dtl.getNo());
						daDtls.Delete(GEDtlAttr.RefPK, workID); // 清除现有的数据.

						// 为从表复制数据.
						for (DataRow dr : dt.Rows) {
							GEDtl daDtl = daDtls.getGetNewEntity() instanceof GEDtl ? (GEDtl) daDtls.getGetNewEntity()
									: null;
							daDtl.setRefPK(String.valueOf(workID));
							// 明细列.
							for (DataColumn dc : dt.Columns) {
								// 设置属性.
								daDtl.SetValByKey(dc.ColumnName, dr.getValue(dc.ColumnName));
							}

							daDtl.ResetDefaultVal();

							daDtl.setRefPK(String.valueOf(workID));
							daDtl.setRDT(DataType.getCurrentDataTime());

							// 执行保存.
							daDtl.InsertAsOID(DBAccess.GenerOID("Dtl")); // 插入数据.
						}
					}
				}
			}

			/// 保存从表结束

			/// 更新发送参数.
			if (htWork != null) {
				String paras = "";
				for (Object key : htWork.keySet()) {
					paras += "@" + key + "=" + htWork.get(key).toString();
				}

				if (DataType.IsNullOrEmpty(paras) == false && Glo.getIsEnableTrackRec() == true) {
					String dbstr = SystemConfig.getAppCenterDBVarStr();
					Paras ps = new Paras();
					ps.SQL = "UPDATE WF_GenerWorkerlist SET AtPara=" + dbstr + "Paras WHERE WorkID=" + dbstr
							+ "WorkID AND FK_Node=" + dbstr + "FK_Node";
					ps.Add(GenerWorkerListAttr.Paras, paras);
					ps.Add(GenerWorkerListAttr.WorkID, workID);
					ps.Add(GenerWorkerListAttr.FK_Node, nd.getNodeID());
					DBAccess.RunSQL(ps);
				}
			}

			/// 更新发送参数.

			if (nd.getSaveModel() == SaveModel.NDAndRpt) {
				/* 如果保存模式是节点表与Node与Rpt表. */
				WorkNode wn = new WorkNode(wk, nd);
				GERpt rptGe = nd.getHisFlow().getHisGERpt();
				rptGe.SetValByKey("OID", workID);
				wn.rptGe = rptGe;
				if (rptGe.RetrieveFromDBSources() == 0) {
					rptGe.SetValByKey("OID", workID);
					wn.DoCopyWorkToRpt(wk);

					if (Glo.getUserInfoShowModel() == UserInfoShowModel.UserIDUserName) {
						rptGe.SetValByKey(GERptAttr.FlowEmps, "@" + WebUser.getNo() + "," + WebUser.getName() + "@");
					}

					if (Glo.getUserInfoShowModel() == UserInfoShowModel.UserIDOnly) {
						rptGe.SetValByKey(GERptAttr.FlowEmps, "@" + WebUser.getNo() + "@");
					}

					if (Glo.getUserInfoShowModel() == UserInfoShowModel.UserNameOnly) {
						rptGe.SetValByKey(GERptAttr.FlowEmps, "@" + WebUser.getName() + "@");
					}

					rptGe.SetValByKey(GERptAttr.FlowStarter, WebUser.getNo());
					rptGe.SetValByKey(GERptAttr.FlowStartRDT, DataType.getCurrentDataTime());
					rptGe.SetValByKey(GERptAttr.WFState, 0);
					rptGe.SetValByKey(GERptAttr.FK_NY, DataType.getCurrentYearMonth());
					rptGe.SetValByKey(GERptAttr.FK_Dept, WebUser.getFK_Dept());
					rptGe.Insert();
				} else {
					wn.DoCopyWorkToRpt(wk);
					rptGe.Update();
				}
			}
			// 获取表单树的数据
			bp.wf.WorkNode workNode = new WorkNode(workID, fk_node);
			Work treeWork = workNode.CopySheetTree();
			if (treeWork != null) {
				wk.Copy(treeWork);
				wk.Update();
			}

			/// 处理保存后事件
			boolean isHaveSaveAfter = false;
			try {
				// 处理表单保存后.
				String s = ExecEvent.DoFrm(nd.getMapData(), EventListFrm.SaveAfter, wk);

				// 执行保存前事件.
				s += ExecEvent.DoNode(EventListNode.NodeFrmSaveAfter, workNode, null);

				if (s != null) {
					/* 如果不等于null,说明已经执行过数据保存，就让其从数据库里查询一次。 */
					wk.RetrieveFromDBSources();
					isHaveSaveAfter = true;
				}
			} catch (RuntimeException ex) {
				return "err@在执行保存后的事件期间出现错误:" + ex.getMessage();
			}

			///

			/// 为开始工作创建待办.
			if (nd.getIsStartNode() == true) {
				GenerWorkFlow gwf = new GenerWorkFlow();
				Flow fl = new Flow(fk_flow);
				if (fl.getDraftRole() == DraftRole.None) {
					return "保存成功";
				}

				// 规则设置为写入待办，将状态置为运行中，其他设置为草稿.
				WFState wfState = WFState.Blank;
				if (fl.getDraftRole() == DraftRole.SaveToDraftList) {
					wfState = WFState.Draft;
				}

				if (fl.getDraftRole() == DraftRole.SaveToTodolist) {
					wfState = WFState.Runing;
				}

				// 设置标题.
				String title = bp.wf.WorkFlowBuessRole.GenerTitle(fl, wk);

				// 修改RPT表的标题
				wk.SetValByKey(GERptAttr.Title, title);
				wk.Update();

				gwf.setWorkID(workID);
				int i = gwf.RetrieveFromDBSources();

				gwf.setTitle(title); // 标题.
				if (i == 0) {
					gwf.setFlowName(fl.getName());
					gwf.setFK_Flow(fk_flow);
					gwf.setFK_FlowSort(fl.getFK_FlowSort());
					gwf.setSysType(fl.getSysType());

					gwf.setFK_Node(fk_node);
					gwf.setNodeName(nd.getName());
					gwf.setWFState(wfState);

					gwf.setFK_Dept(WebUser.getFK_Dept());
					gwf.setDeptName(WebUser.getFK_DeptName());
					gwf.setStarter(WebUser.getNo());
					gwf.setStarterName(WebUser.getName());
					gwf.setRDT(DataType.getCurrentDataTime());
					gwf.Insert();

				} else {
					if (gwf.getWFState() != WFState.ReturnSta) {
						gwf.setWFState(wfState);
						gwf.DirectUpdate();
					}
				}

				// 产生工作列表.
				GenerWorkerList gwl = new GenerWorkerList();
				gwl.setWorkID(workID);
				gwl.setFK_Emp(WebUser.getNo());
				gwl.setFK_EmpText(WebUser.getName());

				gwl.setFK_Node(fk_node);
				gwl.setFK_NodeText(nd.getName());
				gwl.setFID(0);

				gwl.setFK_Flow(fk_flow);
				gwl.setFK_Dept(WebUser.getFK_Dept());
				gwl.setFK_DeptT(WebUser.getFK_DeptName());

				gwl.setSDT("无");
				gwl.setDTOfWarning(DataType.getCurrentDataTime());
				gwl.setIsEnable(true);

				gwl.setIsPass(false);
				// gwl.Sender = WebUser.getNo();
				gwl.setPRI(gwf.getPRI());
				gwl.Save();

			}

			/// 为开始工作创建待办

			return "保存成功.";

		} catch (RuntimeException ex) {
			throw new RuntimeException("err@Node_SaveWork保存错误:" + ex.getMessage() + ", 技术信息：" + ex.getStackTrace());
		}
	}

	/**
	 * 保存独立表单
	 * 
	 * @param fk_mapdata
	 *            独立表单ID
	 * @param workID
	 *            工作ID
	 * @param htData
	 *            独立表单数据Key Value 格式存放.
	 * @return 返回执行信息
	 * @throws Exception
	 */
	public static void Node_SaveFlowSheet(String fk_mapdata, long workID, Hashtable htData) throws Exception {
		Node_SaveFlowSheet(fk_mapdata, workID, htData, null);
	}

	/**
	 * 保存独立表单
	 * 
	 * @param fk_mapdata
	 *            独立表单ID
	 * @param workID
	 *            工作ID
	 * @param htData
	 *            独立表单数据Key Value 格式存放.
	 * @param workDtls
	 *            从表数据
	 * @return 返回执行信息
	 * @throws Exception
	 */
	public static void Node_SaveFlowSheet(String fk_mapdata, long workID, Hashtable htData, DataSet workDtls)
			throws Exception {
		MapData md = new MapData(fk_mapdata);
		GEEntity en = md.getHisGEEn();
		en.SetValByKey("OID", workID);
		int i = en.RetrieveFromDBSources();

		for (Object key : htData.keySet()) {
			if (key == null)
				continue;
			en.SetValByKey(key.toString(), htData.get(key).toString());
		}

		en.SetValByKey("OID", workID);

		ExecEvent.DoFrm(md, EventListFrm.SaveBefore, en);

		if (i == 0) {
			en.Insert();
		} else {
			en.Update();
		}

		ExecEvent.DoFrm(md, EventListFrm.SaveAfter, en);

		if (workDtls != null) {
			MapDtls dtls = new MapDtls(fk_mapdata);
			// 保存从表
			for (DataTable dt : workDtls.Tables) {
				for (MapDtl dtl : dtls.ToJavaList()) {
					if (!dt.TableName.equals(dtl.getNo())) {
						continue;
					}
					// 获取dtls
					GEDtls daDtls = new GEDtls(dtl.getNo());
					daDtls.Delete(GEDtlAttr.RefPK, workID); // 清除现有的数据.

					GEDtl daDtl = daDtls.getGetNewEntity() instanceof GEDtl ? (GEDtl) daDtls.getGetNewEntity() : null;
					daDtl.setRefPK(String.valueOf(workID));

					// 为从表复制数据.
					for (DataRow dr : dt.Rows) {
						daDtl.ResetDefaultVal();
						daDtl.setRefPK(String.valueOf(workID));

						// 明细列.
						for (DataColumn dc : dt.Columns) {
							// 设置属性.
							daDtl.SetValByKey(dc.ColumnName, dr.getValue(dc.ColumnName));
						}
						daDtl.InsertAsOID(DBAccess.GenerOID("Dtl")); // 插入数据.
					}
				}
			}
		}

		ExecEvent.DoFrm(md, EventListFrm.SaveAfter, en);
	}

	/**
	 * 从任务池里取出来一个子任务
	 * 
	 * @param nodeid
	 *            节点编号
	 * @param workid
	 *            工作ID
	 * @param empNo
	 *            取出来的人员编号
	 * @throws Exception
	 */
	public static boolean Node_TaskPoolTakebackOne(long workid) throws Exception {
		if (Glo.getIsEnableTaskPool() == false) {
			throw new RuntimeException("@配置没有设置成共享任务池的状态。");
		}

		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		if (gwf.getTaskSta() == TaskSta.None) {
			throw new RuntimeException("@该任务非共享任务。");
		}

		if (gwf.getTaskSta() == TaskSta.Takeback) {
			throw new RuntimeException("@该任务已经被其他人取走。");
		}

		// 更新状态。
		gwf.setTaskSta(TaskSta.Takeback);
		gwf.Update();

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		// 设置已经被取走的状态。
		ps.SQL = "UPDATE WF_GenerWorkerlist SET IsEnable=-1 WHERE IsEnable=1 AND WorkID=" + dbstr
				+ "WorkID AND FK_Node=" + dbstr + "FK_Node AND FK_Emp!=" + dbstr + "FK_Emp ";
		ps.Add(GenerWorkerListAttr.WorkID, workid);
		ps.Add(GenerWorkerListAttr.FK_Node, gwf.getFK_Node());
		ps.Add(GenerWorkerListAttr.FK_Emp, WebUser.getNo());
		int i = DBAccess.RunSQL(ps);

		bp.wf.Dev2Interface.WriteTrackInfo(gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getNodeName(), workid, 0,
				"任务被" + WebUser.getName() + "从任务池取走.", "获取");
		if (i > 0) {
			Paras ps1 = new Paras();
			// 取走后 将WF_GenerWorkFlow 中的 TodoEmps,TodoEmpsNum 修改下 杨玉慧
			ps1.SQL = "UPDATE WF_GenerWorkFlow SET TodoEmps=" + dbstr + "TodoEmps,TodoEmpsNum=1 WHERE  WorkID=" + dbstr
					+ "WorkID";
			String toDoEmps = WebUser.getNo() + "," + WebUser.getName();
			ps1.Add(GenerWorkFlowAttr.TodoEmps, toDoEmps);
			ps1.Add(GenerWorkerListAttr.WorkID, workid);
			bp.da.Log.DefaultLogWriteLineInfo(toDoEmps);
			bp.da.Log.DefaultLogWriteLineInfo(ps1.SQL);
			DBAccess.RunSQL(ps1);
		}

		if (i == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 放入一个任务
	 * 
	 * @param nodeid
	 *            节点编号
	 * @param workid
	 *            工作ID
	 * @param empNo
	 *            人员ID
	 * @throws Exception
	 */
	public static void Node_TaskPoolPutOne(long workid) throws Exception {
		if (Glo.getIsEnableTaskPool() == false) {
			throw new RuntimeException("@配置没有设置成共享任务池的状态。");
		}

		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		if (gwf.getTaskSta() == TaskSta.None) {
			throw new RuntimeException("@该任务非共享任务。");
		}

		if (gwf.getTaskSta() == TaskSta.Sharing) {
			throw new RuntimeException("@该任务已经是共享状态。");
		}

		// 更新 状态。
		gwf.setTaskSta(TaskSta.Sharing);
		gwf.Update();

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		// 设置已经被取走的状态。
		ps.SQL = "UPDATE WF_GenerWorkerlist SET IsEnable=1 WHERE IsEnable=-1 AND WorkID=" + dbstr + "WorkID ";
		ps.Add(GenerWorkerListAttr.WorkID, workid);
		int i = DBAccess.RunSQL(ps);
		if (i < 0) // 有可能是只有一个人
		{
			throw new RuntimeException("@流程数据错误,不应当更新不到数据。");
		}

		if (i > 0) {
			Paras ps1 = new Paras();
			// 设置已经被取走的状态。
			ps1.SQL = "SELECT FK_Emp,FK_EmpText FROM WF_GenerWorkerlist  WHERE IsEnable=1 AND WorkID=" + dbstr
					+ "WorkID AND FK_Node=" + dbstr + "FK_Node ";
			ps1.Add(GenerWorkerListAttr.WorkID, workid);
			ps1.Add(GenerWorkerListAttr.FK_Node, gwf.getFK_Node());
			ps1.Add(GenerWorkerListAttr.FK_Emp, WebUser.getNo());
			DataTable toDoEmpsTable = DBAccess.RunSQLReturnTable(ps1);
			String toDoEmps = "";
			String toDoEmpsNum = "";
			if (toDoEmpsTable == null || toDoEmpsTable.Rows.size() == 0) {
				throw new RuntimeException("@流程数据错误,没有找到需更新的待处理人。");
			}

			toDoEmpsNum = String.valueOf(toDoEmpsTable.Rows.size());
			for (DataRow dr : toDoEmpsTable.Rows) {
				toDoEmps += String.format("%1$s,%2$s", dr.getValue("FK_Emp").toString(),
						dr.getValue("FK_EmpText").toString()) + ";";
			}
			Paras ps2 = new Paras();
			// 将任务放回后 将WF_GenerWorkFlow 中的 TodoEmps,TodoEmpsNum 修改下 杨玉慧
			ps2.SQL = "UPDATE WF_GenerWorkFlow SET TodoEmps=" + dbstr + "TodoEmps,TodoEmpsNum=" + dbstr
					+ "TodoEmpsNum WHERE  WorkID=" + dbstr + "WorkID";
			ps2.Add(GenerWorkFlowAttr.TodoEmps, toDoEmps);
			ps2.Add(GenerWorkFlowAttr.TodoEmpsNum, toDoEmpsNum);
			ps2.Add(GenerWorkerListAttr.WorkID, workid);
			bp.da.Log.DefaultLogWriteLineInfo(toDoEmps);
			bp.da.Log.DefaultLogWriteLineInfo(ps2.SQL);
			DBAccess.RunSQL(ps2);
		}

		bp.wf.Dev2Interface.WriteTrackInfo(gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getNodeName(), workid, 0,
				"任务被" + WebUser.getName() + "放入了任务池.", "放入");
	}

	/**
	 * 增加下一步骤的接受人(用于当前步骤向下一步骤发送时增加接受人)
	 * 
	 * @param workID
	 *            工作ID
	 * @param toNodeID
	 *            到达的节点ID
	 * @param emps
	 *            如果多个就用逗号分开
	 * @param Del_Selected
	 *            是否删除历史选择
	 * @throws Exception
	 */

	public static void Node_AddNextStepAccepters(long workID, int toNodeID, String fk_emp) throws Exception {
		Node_AddNextStepAccepters(workID, toNodeID, fk_emp, true);
	}

	public static void Node_AddNextStepAccepters(long workID, int toNodeID, String fk_emp, boolean del_Selected)
			throws Exception {
		if (DataType.IsNullOrEmpty(fk_emp) == true) {
			return;
		}

		SelectAccper sa = new SelectAccper();
		// 删除历史选择
		if (del_Selected == true) {
			sa.Delete(SelectAccperAttr.FK_Node, toNodeID, SelectAccperAttr.WorkID, workID);
		}

		// 检查是否是单选？
		bp.wf.template.Selector st = new Selector(toNodeID);
		if (st.getIsSimpleSelector() == true) {
			sa.Delete(SelectAccperAttr.FK_Node, toNodeID, SelectAccperAttr.WorkID, workID);
		}

		String[] emps = fk_emp.split("[,]", -1);
		for (String empNo : emps) {
			if (DataType.IsNullOrEmpty(empNo) == true) {
				continue;
			}
			Emp emp = new Emp();
			emp.setNo(empNo);
			if (emp.RetrieveFromDBSources() == 0) {
				return;
			}

			sa.setFK_Emp(emp.getNo());
			sa.setEmpName(emp.getName());
			sa.setDeptName(emp.getFK_DeptText());
			sa.setFK_Node(toNodeID);
			sa.setWorkID(workID);
			sa.ResetPK();
			if (sa.getIsExits() == false) {
				sa.Insert();
			}
		}
	}

	/**
	 * 增加下一步骤的接受人(用于当前步骤向下一步骤发送时增加接受人)
	 * 
	 * @param workID
	 *            工作ID
	 * @param formNodeID
	 *            从节点ID
	 * @param emp
	 *            接收人
	 * @param tag
	 *            分组维度，可以为空.是为了分流节点向下发送时候，可能有一个工作人员两个或者两个以上的子线程的情况出现。 tag
	 *            是个维度，这个维度可能是一个类别，一个批次，一个标记，总之它是一个字符串。详细:
	 *            http://bbs.ccflow.org/showtopic-3065.aspx
	 * @throws Exception
	 */
	public static void Node_AddNextStepAccepter(long workID, int formNodeID, String emp, String tag) throws Exception {
		SelectAccper sa = new SelectAccper();
		sa.Delete(SelectAccperAttr.FK_Node, formNodeID, SelectAccperAttr.WorkID, workID, SelectAccperAttr.FK_Emp, emp,
				SelectAccperAttr.Tag, tag);

		Emp empEn = new Emp(emp);
		sa.setMyPK(formNodeID + "_" + workID + "_" + emp + "_" + tag);
		sa.setTag(tag);
		sa.setFK_Emp(emp);
		sa.setEmpName(empEn.getName());
		sa.setFK_Node(formNodeID);

		sa.setWorkID(workID);
		sa.Insert();
	}

	/**
	 * 节点工作挂起
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param way
	 *            挂起方式
	 * @param reldata
	 *            解除挂起日期(可以为空)
	 * @param hungNote
	 *            挂起原因
	 * @return 返回执行信息
	 * @throws Exception
	 */
	public static String Node_HungUpWork(long workid, int wayInt, String reldata, String hungNote) throws Exception {
		HungUpWay way = HungUpWay.forValue(wayInt);
		bp.wf.WorkFlow wf = new WorkFlow(workid);
		return wf.DoHungUp(way, reldata, hungNote);
	}

	/**
	 * 节点工作取消挂起
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @param msg
	 *            取消挂起原因
	 * @return 执行信息
	 * @throws Exception
	 */
	public static void Node_UnHungUpWork(long workid, String msg) throws Exception {
		bp.wf.WorkFlow wf = new WorkFlow(workid);
		wf.DoUnHungUp();
	}

	/**
	 * 获取该节点上的挂起时间
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @return 返回时间串，如果没有挂起的动作就抛出异常.
	 */
	public static Date Node_GetHungUpTimeSpan(String flowNo, int nodeID, long workid) {
		String dbstr = SystemConfig.getAppCenterDBVarStr();

		String instr = ActionType.HungUp.getValue() + "," + ActionType.UnHungUp.getValue();
		Paras ps = new Paras();
		ps.SQL = "SELECT * FROM ND" + Integer.parseInt(flowNo) + "Track WHERE WorkID=" + dbstr + "WorkID AND "
				+ TrackAttr.ActionType + " in (" + instr + ")  and  NDFrom=" + dbstr + "NDFrom ";
		ps.Add(TrackAttr.WorkID, workid);
		ps.Add(TrackAttr.NDFrom, nodeID);
		DataTable dt = DBAccess.RunSQLReturnTable(ps);

		Date dtStart = new Date();
		Date dtEnd = new Date();
		for (DataRow item : dt.Rows) {
			ActionType at = (ActionType) item.getValue(TrackAttr.ActionType);

			// 挂起时间.
			if (at == ActionType.HungUp) {
				dtStart = DataType.ParseSysDateTime2DateTime(item.getValue(TrackAttr.RDT).toString());
			}

			// 解除挂起时间.
			if (at == ActionType.UnHungUp) {
				dtEnd = DataType.ParseSysDateTime2DateTime(item.getValue(TrackAttr.RDT).toString());
			}
		}

		Date ts = new Date(dtEnd.getTime() - dtStart.getTime());
		return ts;
	}

	/**
	 * 执行加签
	 * 
	 * @param workid
	 *            工作ID
	 * @param askfor
	 *            加签方式
	 * @param askForEmp
	 *            请求人员
	 * @param askForNote
	 *            内容
	 * @return
	 * @throws Exception
	 */
	public static String Node_Askfor(long workid, AskforHelpSta askforSta, String askForEmp, String askForNote)
			throws Exception {
		// 检查人员是否存在.
		Emp emp = new Emp();
		emp.setNo(askForEmp);
		if (emp.RetrieveFromDBSources() == 0) {
			throw new RuntimeException("@要加签的人员编号错误:" + askForEmp);
		}

		// 获得当前流程注册信息.
		bp.wf.GenerWorkFlow gwf = new GenerWorkFlow(workid);

		// 检查当前人员是否开可以执行当前的工作?
		if (Flow_IsCanDoCurrentWork(gwf.getWorkID(), WebUser.getNo()) == false) {
			throw new RuntimeException("@当前的工作已经被别人处理或者您没有处理该工作的权限.");
		}

		// 检查被加签的人是否在当前的队列中.
		GenerWorkerLists gwls = new GenerWorkerLists(workid, gwf.getFK_Node());
		if (gwls.Contains(GenerWorkerListAttr.FK_Emp, askForEmp, GenerWorkerListAttr.IsEnable, 0) == true) {
			throw new RuntimeException("@加签失败，您选择的加签人可以处理当前的工作。");
		}

		gwf.setWFState(WFState.Askfor); // 更新流程为加签状态.
		gwf.Update();

		// 设置当前状态为 2 表示加签状态.
		if (gwls.size() == 0) {
			/* 可能是第一个节点. */
			GenerWorkerList gwl = new GenerWorkerList();
			gwl.Copy(gwf);
			gwl.setWorkID(workid);
			gwl.setFK_Emp(askForEmp);
			gwl.setFK_Node(gwf.getFK_Node());
			gwl.setFK_NodeText(gwl.getFK_NodeText());
			gwl.setFK_Emp(WebUser.getNo());
			gwl.setFK_EmpText(WebUser.getName());
			gwl.setFK_Dept(WebUser.getFK_Dept());
			gwl.setFK_DeptT(WebUser.getFK_DeptName());

			gwl.setIsPassInt(askforSta.getValue());
			gwl.Insert();
			// 重新查询.
			gwls = new GenerWorkerLists(workid, gwf.getFK_Node());

			// 设置流程标题.
			if (gwf.getTitle().length() == 0) {
				Flow_SetFlowTitle(gwf.getFK_Flow(), workid, "来自" + WebUser.getName() + "的工作加签.");
			}
		}
		// endWarning.

		// 处理状态.
		for (GenerWorkerList item : gwls.ToJavaList()) {
			if (item.getIsEnable() == false) {
				continue;
			}

			if (item.getFK_Emp().equals(WebUser.getNo())) {
				// GenerWorkerList gwl = gwls[0] as GenerWorkerList;
				item.setIsPassInt(askforSta.getValue());
				item.Update();

				// 更换主键后，执行insert ,让被加签人有代办工作.
				item.setIsPassInt(0);
				item.setFK_Emp(emp.getNo());
				item.setFK_EmpText(emp.getName());
				try {
					item.Insert();
				} catch (java.lang.Exception e) {
					item.Update();
				}
			} else {
				item.Update();
			}
		}

		// 写入日志.
		bp.wf.Dev2Interface.WriteTrack(gwf.getFK_Flow(), gwf.getFK_Node(), gwf.getNodeName(), workid, gwf.getFID(),
				askForNote, ActionType.AskforHelp, "", null, null, emp.getNo(), emp.getName());

		Flow fl = new Flow(gwf.getFK_Flow());
		bp.wf.Dev2Interface.Port_SendMsg(askForEmp, gwf.getTitle(), askForNote,
				"AK" + gwf.getFK_Node() + "_" + gwf.getWorkID(), SMSMsgType.AskFor, gwf.getFK_Flow(), gwf.getFK_Node(),
				workid, gwf.getFID());
		// 更新状态.
		DBAccess.RunSQL(
				"UPDATE " + fl.getPTable() + " SET WFState=" + WFState.Askfor.getValue() + " WHERE OID=" + workid);

		// 设置成工作未读。
		bp.wf.Dev2Interface.Node_SetWorkUnRead(workid, askForEmp);

		String msg = "您的工作已经提交给(" + askForEmp + " " + emp.getName() + ")加签了。";

		// 加签后事件
		bp.wf.Node hisNode = new bp.wf.Node(gwf.getFK_Node());
		Work currWK = hisNode.getHisWork();
		currWK.setOID(gwf.getWorkID());
		currWK.Retrieve();

		WorkNode wn = new WorkNode(currWK, hisNode);

		// 执行加签后的事件.
		msg += ExecEvent.DoNode(EventListNode.AskerAfter, wn, null);

		return msg;
	}

	/**
	 * 答复加签信息
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param fk_node
	 *            节点编号
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            FID
	 * @param replyNote
	 *            答复信息
	 * @return
	 * @throws Exception
	 */
	public static String Node_AskforReply(long workid, String replyNote) throws Exception {
		// 把回复信息临时的写入 流程注册信息表以便让发送方法获取这个信息写入日志.
		GenerWorkFlow gwf = new GenerWorkFlow(workid);
		gwf.setParasAskForReply(replyNote);
		gwf.Update();

		Node nd = new Node(gwf.getFK_Node());
		String info = "";
		try {
			// 执行发送, 在发送的方法里面已经做了判断了,并且把 回复的信息写入了日志.
			info = bp.wf.Dev2Interface.Node_SendWork(gwf.getFK_Flow(), workid).ToMsgOfHtml();
		} catch (RuntimeException ex) {
			if (ex.getMessage().contains("请选择下一步骤工作") == true || ex.getMessage().contains("用户没有选择发送到的节点") == true) {
				if (nd.getCondModel() == DirCondModel.ByUserSelected) {
					/* 如果抛出异常，我们就让其转入选择到达的节点里, 在节点里处理选择人员. */
					return "SelectNodeUrl@./WorkOpt/ToNodes.htm?FK_Flow=" + gwf.getFK_Flow() + "&FK_Node="
							+ gwf.getFK_Node() + "&WorkID=" + gwf.getWorkID() + "&FID=" + gwf.getFID();

				}
				return "err@下一个节点的接收人规则是，当前节点选择来选择，在当前节点属性里您没有启动接受人按钮，系统自动帮助您启动了，请关闭窗口重新打开。" + ex.getMessage();
			}
			return ex.getMessage();
		}

		Node node = new Node(gwf.getFK_Node());
		Work wk = node.getHisWork();
		wk.setOID(workid);
		wk.RetrieveFromDBSources();

		// 恢复加签后执行事件.
		WorkNode wn = new WorkNode(wk, node);
		info += ExecEvent.DoNode(EventListNode.AskerReAfter, wn, null);
		return info;
	}

	/**
	 * 执行工作分配
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeID
	 *            节点ID
	 * @param workID
	 *            工作ID
	 * @param fid
	 *            FID
	 * @param toEmps
	 *            要分配的人，多个人用逗号分开.
	 * @param msg
	 *            分配原因.
	 * @return 分配信息.
	 * @throws Exception
	 */
	public static String Node_Allot(String flowNo, int nodeID, long workID, long fid, String toEmps, String msg)
			throws Exception {
		// 生成实例.
		GenerWorkerLists gwls = new GenerWorkerLists(workID, nodeID);

		// 要分配给的人员.
		String[] emps = toEmps.split("[,]", -1);
		for (String empNo : emps) {
			if (DataType.IsNullOrEmpty(empNo) == true) {
				continue;
			}

			// 人员实体.
			Emp empEmp = new Emp(empNo);

			GenerWorkerList gwl = null; // 接收人

			// 开始找接收人.
			for (GenerWorkerList item : gwls.ToJavaList()) {
				if (item.getFK_Emp().equals(empNo)) {
					gwl = item;
					break;
				}
			}

			// 没有找到的情况, 就获得一个实例，作为数据样本然后把数据insert.
			if (gwl == null) {
				gwl = gwls.get(0) instanceof GenerWorkerList ? (GenerWorkerList) gwls.get(0) : null;
				gwl.setFK_Emp(empEmp.getNo());
				gwl.setFK_EmpText(empEmp.getName());
				gwl.setIsEnable(true);
				gwl.setIsPassInt(0);
				gwl.Insert();
				continue;
			}

			// 如果被禁用了，就启用他.
			if (gwl.getIsEnable() == false) {
				gwl.setIsEnable(true);
				gwl.Update();
			}
		}
		return "分配成功.";
	}

	/**
	 * 工作移交
	 * 
	 * @param flowNo
	 * @param nodeID
	 * @param workID
	 * @param fid
	 * @param toEmp
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public static String Node_Shift(String flowNo, int nodeID, long workID, long fid, String toEmp, String msg)
			throws Exception {
		return Node_Shift(workID, toEmp, msg);
	}

	/**
	 * 工作移交
	 * 
	 * @param workID
	 *            工作ID
	 * @param toEmp
	 *            要移交的人
	 * @param msg
	 *            移交信息
	 * @return 执行结果
	 * @throws Exception
	 */
	public static String Node_Shift(long workID, String toEmp, String msg) throws Exception {
		if (toEmp.equals(WebUser.getNo()) == true)
			throw new Exception("err@您不能移交给您自己。");

		GenerWorkFlow gwf = new GenerWorkFlow(workID);

		if (gwf.getWFSta() == WFSta.Complete)
			throw new RuntimeException("err@流程已经完成，您不能执行移交了。");

		int i = 0;
		// 人员.
		Emp emp = new Emp(toEmp);
		Node nd = new Node(gwf.getFK_Node());
		Work work = nd.getHisWork();
		work.setOID(workID);
		if (nd.getTodolistModel() == TodolistModel.Order || nd.getTodolistModel() == TodolistModel.Teamup
				|| nd.getTodolistModel() == TodolistModel.TeamupGroupLeader) {
			/* 如果是队列模式，或者是协作模式. */
			// 检查被移交人是否在当前的待办列表里否？
			GenerWorkerList gwl = new GenerWorkerList();
			i = gwl.Retrieve(GenerWorkerListAttr.FK_Emp, emp.getNo(), GenerWorkerListAttr.FK_Node, nd.getNodeID(),
					GenerWorkerListAttr.WorkID, workID);
			if (i == 1)
				return "err@移交失败，您所移交的人员(" + emp.getNo() + " " + emp.getName() + ")已经在代办列表里.";

			// 把自己的待办更新到被移交人身上.
			String sql = "UPDATE WF_GenerWorkerlist SET FK_Emp='" + emp.getNo() + "', FK_EmpText='" + emp.getName()
					+ "' WHERE FK_Emp='" + WebUser.getNo() + "' AND FK_Node=" + gwf.getFK_Node() + " AND WorkID="
					+ workID;
			DBAccess.RunSQL(sql);

			// 移交后事件 @yuanlina
			String atPara1 = "@SendToEmpIDs=" + emp.getNo();

			String info = "@" + ExecEvent.DoNode(EventListNode.ShitAfter, nd, work, null, atPara1);

			// info += "@<a href='" + Glo.getCCFlowAppPath() +
			// "WF/MyFlowInfo.htm?DoType=UnShift&FK_Flow=" + nd.getFK_Flow() +
			// "&WorkID=" + workID + "&FK_Node=" + gwf.getFK_Node() + "&FID=" +
			// gwf.getFID() + "' ><img src='./Img/Action/UnSend.png' border=0
			// />撤消工作移交</a>.";

			// 处理移交后发送的消息事件 @yuanlina
			PushMsgs pms1 = new PushMsgs();
			pms1.Retrieve(PushMsgAttr.FK_Node, nd.getNodeID(), PushMsgAttr.FK_Event, EventListNode.ShitAfter);
			for (PushMsg pm : pms1.ToJavaList()) {
				pm.DoSendMessage(nd, nd.getHisWork(), null, null, null, emp.getNo());
			}

			return "移交成功.";
		}

		GenerWorkerLists gwls = new GenerWorkerLists();
		gwls.Retrieve(GenerWorkerListAttr.FK_Node, gwf.getFK_Node(), GenerWorkerListAttr.WorkID, gwf.getWorkID());
		gwls.Delete(GenerWorkerListAttr.FK_Node, gwf.getFK_Node(), GenerWorkerListAttr.WorkID, gwf.getWorkID());

		for (GenerWorkerList item : gwls.ToJavaList()) {
			item.setFK_Emp(emp.getNo());
			item.setFK_EmpText(emp.getName());
			item.setIsEnable(true);
			item.Insert();
			break;
		}

		gwf.setWFState(WFState.Shift);
		gwf.setTodoEmpsNum(1);
		gwf.setTodoEmps(WebUser.getNo() + "," + WebUser.getName() + ";");
		gwf.Update();

		// 记录日志.
		Glo.AddToTrack(ActionType.Shift, nd.getFK_Flow(), workID, gwf.getFID(), nd.getNodeID(), nd.getName(),
				WebUser.getNo(), WebUser.getName(), nd.getNodeID(), nd.getName(), toEmp, emp.getName(), msg, null);

		String inf1o = "@工作移交成功。@您已经成功的把工作移交给：" + emp.getNo() + " , " + emp.getName();
		// 移交后事件 @yuanlina
		String atPara = "@SendToEmpIDs=" + emp.getNo();
		WorkNode wn = new WorkNode(work, nd);
		inf1o += "@" + ExecEvent.DoNode(EventListNode.ShitAfter, wn, null, atPara);

		return inf1o;
	}

	/**
	 * 撤销移交
	 * 
	 * @param flowNo
	 *            撤销编号
	 * @param workID
	 *            工作ID
	 * @return 返回撤销信息
	 * @throws Exception
	 */
	public static String Node_ShiftUn(long workID) throws Exception {
		WorkFlow mwf = new WorkFlow(workID);
		return mwf.DoUnShift();
	}

	/**
	 * 执行工作退回(退回指定的点)
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param fid
	 *            流程ID
	 * @param currentNodeID
	 *            当前节点ID
	 * @param returnToNodeID
	 *            退回到的工作ID
	 * @param returnToEmp
	 *            退回到人员
	 * @param msg
	 *            退回原因
	 * @param isBackToThisNode
	 *            退回后是否要原路返回？
	 * @return 执行结果，此结果要提示给用户。
	 * @throws Exception
	 */

	public static String Node_ReturnWork(String fk_flow, long workID, long fid, int currentNodeID, int returnToNodeID,
			String returnToEmp, String msg, boolean isBackToThisNode) throws Exception {
		return Node_ReturnWork(fk_flow, workID, fid, currentNodeID, returnToNodeID, returnToEmp, msg, isBackToThisNode,
				null);
	}

	public static String Node_ReturnWork(String fk_flow, long workID, long fid, int currentNodeID, int returnToNodeID,
			String returnToEmp, String msg) throws Exception {
		return Node_ReturnWork(fk_flow, workID, fid, currentNodeID, returnToNodeID, returnToEmp, msg, false, null);
	}

	public static String Node_ReturnWork(String fk_flow, long workID, long fid, int currentNodeID, int returnToNodeID,
			String returnToEmp) throws Exception {
		return Node_ReturnWork(fk_flow, workID, fid, currentNodeID, returnToNodeID, returnToEmp, "无", false, null);
	}

	public static String Node_ReturnWork(String fk_flow, long workID, long fid, int currentNodeID, int returnToNodeID,
			String returnToEmp, String msg, boolean isBackToThisNode, String pageData) throws Exception {
		WorkReturn wr = new WorkReturn(fk_flow, workID, fid, currentNodeID, returnToNodeID, returnToEmp,
				isBackToThisNode, msg, pageData);
		return wr.DoIt();
	}

	/**
	 * 退回
	 * 
	 * @param workID
	 *            工作ID
	 * @param returnToNodeID
	 *            要退回的节点,0 表示上一个节点或者指定的节点.
	 * @param msg
	 *            退回信息
	 * @param isBackToThisNode
	 *            是否原路返回
	 * @return 执行结果
	 * @throws Exception
	 */
	public static String Node_ReturnWork(long workID, int returnToNodeID, String msg, boolean isBackToThisNode)
			throws Exception {
		GenerWorkFlow gwf = new GenerWorkFlow(workID);
		return Node_ReturnWork(gwf.getFK_Flow(), workID, gwf.getFID(), gwf.getFK_Node(), returnToNodeID, null, msg,
				isBackToThisNode);
	}

	/**
	 * 退回
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workID
	 *            工作ID
	 * @param fid
	 *            流程ID
	 * @param currentNodeID
	 *            当前节点
	 * @param returnToNodeID
	 *            退回到节点
	 * @param msg
	 *            退回消息
	 * @param isBackToThisNode
	 *            是否原路返回
	 * @return 退回执行的信息，执行不成功就抛出异常。
	 * @throws Exception
	 */
	public static String Node_ReturnWork(String fk_flow, long workID, long fid, int currentNodeID, int returnToNodeID,
			String msg, boolean isBackToThisNode) throws Exception {
		return Node_ReturnWork(fk_flow, workID, fid, currentNodeID, returnToNodeID, null, msg, isBackToThisNode);
	}

	/**
	 * 获取当前工作的NodeID
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param workid
	 *            工作ID
	 * @return 指定工作的NodeID.
	 */
	public static int Node_GetCurrentNodeID(String fk_flow, long workid) {
		int nodeID = DBAccess.RunSQLReturnValInt(
				"SELECT FK_Node FROM WF_GenerWorkFlow WHERE WorkID=" + workid + " AND FK_Flow='" + fk_flow + "'", 0);
		if (nodeID == 0) {
			return Integer.parseInt(fk_flow + "01");
		}

		return nodeID;
	}

	/**
	 * 删除子线程
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param fid
	 *            流程ID
	 * @param workid
	 *            工作ID
	 * @throws Exception
	 */
	public static void Node_FHL_KillSubFlow(String fk_flow, long fid, long workid) throws Exception {
		WorkFlow wkf = new WorkFlow(workid);
		wkf.DoDeleteWorkFlowByReal(true);
	}

	/**
	 * 合流点驳回子线程
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param fid
	 *            流程ID
	 * @param workid
	 *            子线程ID
	 * @param msg
	 *            驳回消息
	 * @throws Exception
	 */
	public static String Node_FHL_DoReject(int NodeSheetfReject, long fid, long workid, String msg) throws Exception {
		WorkFlow wkf = new WorkFlow(workid);
		return wkf.DoReject(fid, NodeSheetfReject, msg);
	}

	/**
	 * 跳转审核取回
	 * 
	 * @param fromNodeID
	 *            从节点ID
	 * @param workid
	 *            工作ID
	 * @param tackToNodeID
	 *            取回到的节点ID
	 * @return
	 * @throws Exception
	 */

	public static String Node_Tackback(int fromNodeID, long workid, int tackToNodeID) throws Exception {
		return Node_Tackback(fromNodeID, workid, tackToNodeID, null);
	}

	public static String Node_Tackback(int fromNodeID, long workid, int tackToNodeID, String doMsg) throws Exception {
		if (doMsg == null) {
			doMsg = " 执行跳转审核的取回";
		}

		/*
		 * 1,首先检查是否有此权限. 2, 执行工作跳转. 3, 执行写入日志.
		 */
		Node nd = new Node(tackToNodeID);
		switch (nd.getHisDeliveryWay()) {
		case ByPreviousNodeFormEmpsField:
			break;
		}

		WorkNode wn = new WorkNode(workid, fromNodeID);
		String msg = wn.NodeSend(new Node(tackToNodeID), WebUser.getNo()).ToMsgOfHtml();
		wn.AddToTrack(ActionType.Tackback, WebUser.getNo(), WebUser.getName(), tackToNodeID, nd.getName(), doMsg);
		return msg;
	}

	/**
	 * 执行抄送已阅
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param fk_node
	 *            流程节点
	 * @param workid
	 *            工作id
	 * @param fid
	 *            流程id
	 * @param checkNote
	 *            填写意见
	 * @throws Exception
	 */
	public static void Node_DoCCCheckNote(String fk_flow, int fk_node, long workid, long fid, String checkNote)
			throws Exception {
		NodeWorkCheck fwc = new NodeWorkCheck(fk_node);

		bp.wf.Dev2Interface.WriteTrackWorkCheck(fk_flow, fk_node, workid, fid, checkNote, fwc.getFWCOpLabel());

		// 设置审核完成.
		bp.wf.Dev2Interface.Node_CC_SetSta(fk_node, workid, WebUser.getNo(), bp.wf.CCSta.CheckOver);
	}

	/**
	 * 设置是此工作为读取状态
	 * 
	 * @param nodeID
	 *            节点编号
	 * @param workid
	 *            工作ID
	 * @throws Exception
	 */
	public static void Node_SetWorkRead(int nodeID, long workid) throws Exception {
		Node_SetWorkRead(nodeID, workid, WebUser.getNo());
	}

	/**
	 * 设置是此工作为读取状态
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            WorkID
	 * @param empNo
	 *            操作员
	 * @throws Exception
	 */
	public static void Node_SetWorkRead(int nodeID, long workid, String empNo) throws Exception {
		Node nd = new Node(nodeID);

		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkerList SET IsRead=1 WHERE WorkID=" + dbstr + "WorkID AND FK_Node=" + dbstr
				+ "FK_Node AND FK_Emp=" + dbstr + "FK_Emp";
		ps.Add("WorkID", workid);
		ps.Add("FK_Node", nodeID);
		ps.Add("FK_Emp", empNo);
		if (DBAccess.RunSQL(ps) == 0) {
			// throw new Exception("设置的工作不存在，或者当前的登陆人员[" + empNo +
			// "]已经改变，请重新登录。");
		}

		// 判断当前节点的已读回执.
		if (nd.getReadReceipts() == ReadReceipts.None || nd.getIsStartNode() == true) {
			return;
		}

		boolean isSend = false;
		if (nd.getReadReceipts() == ReadReceipts.Auto) {
			isSend = true;
		}

		if (nd.getReadReceipts() == ReadReceipts.BySysField) {
			/* 获取上一个节点ID */
			Nodes fromNodes = nd.getFromNodes();
			int fromNodeID = 0;
			for (Node item : fromNodes.ToJavaList()) {
				ps = new Paras();
				ps.SQL = "SELECT FK_Node FROM WF_GenerWorkerlist WHERE WorkID=" + dbstr + "WorkID AND FK_Node=" + dbstr
						+ "FK_Node ";
				ps.Add("WorkID", workid);
				ps.Add("FK_Node", item.getNodeID());
				DataTable dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() == 0) {
					continue;
				}

				fromNodeID = item.getNodeID();
				break;
			}
			if (fromNodeID == 0) {
				throw new RuntimeException("@没有找到它的上一步工作。");
			}

			try {
				ps = new Paras();
				ps.SQL = "SELECT " + bp.wf.WorkSysFieldAttr.SysIsReadReceipts + " FROM ND" + fromNodeID
						+ "    WHERE OID=" + dbstr + "OID";
				ps.Add("OID", workid);
				DataTable dt1 = DBAccess.RunSQLReturnTable(ps);
				if (dt1.Rows.get(0).getValue(0).toString().equals("1")) {
					isSend = true;
				}
			} catch (RuntimeException ex) {
				throw new RuntimeException(
						"@流程设计错误:" + ex.getMessage() + " 在当前节点上个您设置了安上一步的表单字段决定是否回执，但是上一个节点表单中没有约定的字段。");
			}
		}

		if (nd.getReadReceipts() == ReadReceipts.BySDKPara) {
			/* 如果是按开发者参数 */

			/* 获取上一个节点ID */
			Nodes fromNodes = nd.getFromNodes();
			int fromNodeID = 0;
			for (Node item : fromNodes.ToJavaList()) {
				ps = new Paras();
				ps.SQL = "SELECT FK_Node FROM WF_GenerWorkerlist  WHERE WorkID=" + dbstr + "WorkID AND FK_Node=" + dbstr
						+ "FK_Node ";
				ps.Add("WorkID", workid);
				ps.Add("FK_Node", item.getNodeID());
				DataTable dt = DBAccess.RunSQLReturnTable(ps);
				if (dt.Rows.size() == 0) {
					continue;
				}

				fromNodeID = item.getNodeID();
				break;
			}
			if (fromNodeID == 0) {
				throw new RuntimeException("@没有找到它的上一步工作。");
			}

			String paras = bp.wf.Dev2Interface.GetFlowParas(fromNodeID, workid);
			if (DataType.IsNullOrEmpty(paras)
					|| paras.contains("@" + bp.wf.WorkSysFieldAttr.SysIsReadReceipts + "=") == false) {
				throw new RuntimeException("@流程设计错误:在当前节点上个您设置了按开发者参数决定是否回执，但是没有找到该参数。");
			}

			// 开发者参数.
			if (paras.contains("@" + bp.wf.WorkSysFieldAttr.SysIsReadReceipts + "=1") == true) {
				isSend = true;
			}
		}

		if (isSend == true) {
			/* 如果是自动的已读回执，就让它发送给当前节点的上一个发送人。 */

			// 获取流程标题.
			ps = new Paras();
			ps.SQL = "SELECT Title FROM WF_GenerWorkFlow WHERE WorkID=" + dbstr + "WorkID ";
			ps.Add("WorkID", workid);
			DataTable dt = DBAccess.RunSQLReturnTable(ps);
			String title = dt.Rows.get(0).getValue(0).toString();

			// 获取流程的发送人.
			ps = new Paras();
			ps.SQL = "SELECT " + GenerWorkerListAttr.Sender + " FROM WF_GenerWorkerlist WHERE WorkID=" + dbstr
					+ "WorkID AND FK_Node=" + dbstr + "FK_Node ";
			ps.Add("WorkID", workid);
			ps.Add("FK_Node", nodeID);
			dt = DBAccess.RunSQLReturnTable(ps);
			String sender = dt.Rows.get(0).getValue(0).toString();

			// 发送已读回执。
			bp.wf.Dev2Interface.Port_SendMsg(sender, "已读回执:" + title,
					"您发送的工作已经被" + WebUser.getName() + "在" + DataType.getCurrentDataTimeCNOfShort() + " 打开.",
					"RP" + workid + "_" + nodeID, bp.wf.SMSMsgType.Self, nd.getFK_Flow(), nd.getNodeID(), workid, 0);
		}

		// 执行节点打开后事件.
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.RetrieveFromDBSources();

		WorkNode wn = new WorkNode(wk, nd);

		// 执行事件.
		ExecEvent.DoNode(EventListNode.WhenReadWork, wn, null, null);

	}

	/**
	 * 设置工作未读取
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @param userNo
	 *            要设置的人
	 */
	public static void Node_SetWorkUnRead(long workid, String userNo) {
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		ps.SQL = "UPDATE WF_GenerWorkerList SET IsRead=0 WHERE WorkID=" + dbstr + "WorkID AND FK_Emp=" + dbstr
				+ "FK_Emp";
		ps.Add("WorkID", workid);
		ps.Add("FK_Emp", userNo);
		DBAccess.RunSQL(ps);
	}

	/**
	 * 设置工作未读取
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @throws Exception
	 */
	public static void Node_SetWorkUnRead(long workid) throws Exception {
		Node_SetWorkUnRead(workid, WebUser.getNo());
	}

	/// 工作有关接口

	/// 写入轨迹.
	/**
	 * 写入BBS
	 * 
	 * @param frmID
	 *            表单ID
	 * @param frmName
	 *            表单名称
	 * @param workID
	 *            工作ID
	 * @param msg
	 *            消息
	 * @param fid
	 *            流程ID
	 * @param flowNo
	 *            流程编号
	 * @param flowName
	 *            流程名称
	 * @param nodeID
	 *            节点ID
	 * @param nodeName
	 *            节点名称
	 * @throws Exception
	 */

	public static void Track_WriteBBS(String frmID, String frmName, long workID, String msg, long fid, String flowNo,
			String flowName, int nodeID) throws Exception {
		Track_WriteBBS(frmID, frmName, workID, msg, fid, flowNo, flowName, nodeID, "");
	}

	public static void Track_WriteBBS(String frmID, String frmName, long workID, String msg, long fid, String flowNo,
			String flowName) throws Exception {
		Track_WriteBBS(frmID, frmName, workID, msg, fid, flowNo, flowName, 0, "");
	}

	public static void Track_WriteBBS(String frmID, String frmName, long workID, String msg, long fid, String flowNo)
			throws Exception {
		Track_WriteBBS(frmID, frmName, workID, msg, fid, flowNo, "", 0, "");
	}

	public static void Track_WriteBBS(String frmID, String frmName, long workID, String msg, long fid)
			throws Exception {
		Track_WriteBBS(frmID, frmName, workID, msg, fid, "", "", 0, "");
	}

	public static void Track_WriteBBS(String frmID, String frmName, long workID, String msg) throws Exception {
		Track_WriteBBS(frmID, frmName, workID, msg, 0, "", "", 0, "");
	}

	public static void Track_WriteBBS(String frmID, String frmName, long workID, String msg, long fid, String flowNo,
			String flowName, int nodeID, String nodeName) throws Exception {
		bp.ccbill.Track tk = new bp.ccbill.Track();
		tk.setWorkID(String.valueOf(workID));
		tk.setFrmID(frmID);
		tk.setFrmName(frmName);
		tk.setActionType(bp.ccbill.FrmActionType.BBS);
		tk.setActionTypeText("评论");

		tk.setRec(WebUser.getNo());
		tk.setRecName(WebUser.getName());
		tk.setDeptNo(WebUser.getFK_Dept());
		tk.setDeptName(WebUser.getFK_DeptName());

		tk.setMyPK(tk.getFrmID() + "_" + tk.getWorkID() + "_" + tk.getRec() + "_"
				+ bp.ccbill.FrmActionType.BBS);
		tk.setMsg(msg);
		tk.setRDT(DataType.getCurrentDataTime());

		// 流程信息.
		tk.setNodeID(nodeID);
		tk.setNodeName(nodeName);
		tk.setFlowNo(flowNo);
		tk.setFlowName(flowName);
		tk.setFID(fid);

		tk.Save();

		// 修改抄送状态
		bp.wf.Dev2Interface.Node_CC_SetCheckOver(workID);
	}

	/// 写入轨迹.

	/// 流程属性与节点属性变更接口.
	/**
	 * 更改流程属性
	 * 
	 * @param fk_flow
	 *            流程编号
	 * @param attr1
	 *            字段1
	 * @param v1
	 *            值1
	 * @param attr2
	 *            字段2(可为null)
	 * @param v2
	 *            值2(可为null)
	 * @return 执行结果
	 * @throws Exception
	 */
	public static String ChangeAttr_Flow(String fk_flow, String attr1, Object v1, String attr2, Object v2)
			throws Exception {
		Flow fl = new Flow(fk_flow);
		if (attr1 != null) {
			fl.SetValByKey(attr1, v1);
		}

		if (attr2 != null) {
			fl.SetValByKey(attr2, v2);
		}

		fl.Update();
		return "修改成功";
	}

	/// 流程属性与节点属性变更接口.

	/// ccform 接口

	/**
	 * 获得指定轨迹的json数据.
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param mypk
	 *            流程主键
	 * @return 返回当时的表单json字符串
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static String CCFrom_GetFrmDBJson(String flowNo, String mypk) throws NumberFormatException, Exception {
		return DBAccess.GetBigTextFromDB("ND" + Integer.parseInt(flowNo) + "Track", "MyPK", mypk, "FrmDB");
	}

	/**
	 * SDK签章接口
	 * 
	 * @param workid
	 *            工作ID
	 * @param nodeid
	 *            签章节点ID
	 * @param deptno
	 *            部门编号
	 * @param stationno
	 *            岗位编号
	 * @return 返回非null值时，为签章失败
	 * @throws Exception
	 */
	public static String CCForm_Seal(long workid, int nodeid, String deptno, String stationno) throws Exception {
		try {
			FrmEleDBs eleDBs = new FrmEleDBs("ND" + nodeid, String.valueOf(workid));

			if (eleDBs.size() > 0) {
				eleDBs.Delete(FrmEleDBAttr.FK_MapData, "ND" + nodeid, FrmEleDBAttr.RefPKVal, workid);
			}

			String sealimg = bp.wf.Glo.getCCFlowAppPath() + "DataUser/Seal/" + deptno + "_" + stationno + ".jpg";

			if ((new File(SystemConfig.getPathOfWebApp() + sealimg)).isFile() == false) {
				return "签章文件：" + sealimg + "不存在，请联系管理员！";
			}

			FrmEleDB athDB_N = new FrmEleDB();
			athDB_N.setFK_MapData("ND" + nodeid);
			athDB_N.setRefPKVal(String.valueOf(workid));
			athDB_N.setEleID(String.valueOf(workid));
			athDB_N.GenerPKVal();
			athDB_N.setTag1(sealimg);
			athDB_N.DirectInsert();

			return null;
		} catch (RuntimeException ex) {
			return "签章错误：" + ex.getMessage();
		}
	}

	/**
	 * 增加附件
	 * 
	 * @param nodeid
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @param athNo
	 *            附件编号，如果当前节点只有一个附件可以为空.
	 * @param frmID
	 *            表单ID
	 * @param filePath
	 *            文件路径:比如：c:\\xxx.xls
	 * @param fileName
	 *            文件名称:比如：我的文档.xls
	 * @return 执行结果
	 */
	public static String CCForm_AddAth(int nodeid, long workid, String athNo, String frmID, String filePath,
			String fileName) {
		return "增加成功";
	}

	/// ccform 接口

	/// 页面.
	/**
	 * 附件上传接口
	 * 
	 * @param nodeid
	 *            节点ID
	 * @param flowid
	 *            流程ID
	 * @param workid
	 *            工作ID
	 * @param athNo
	 *            附件属性No
	 * @param frmID
	 *            FK_MapData
	 * @param filePath
	 *            附件路径
	 * @param fileName
	 *            附件名称
	 * @param sort
	 *            分类
	 * @return
	 * @throws Exception
	 */

	public static String CCForm_AddAth(int nodeid, String flowid, long workid, String athNo, String frmID,
			String filePath, String fileName, String sort, int fid) throws Exception {
		return CCForm_AddAth(nodeid, flowid, workid, athNo, frmID, filePath, fileName, sort, fid, 0);
	}

	public static String CCForm_AddAth(int nodeid, String flowid, long workid, String athNo, String frmID,
			String filePath, String fileName, String sort) throws Exception {
		return CCForm_AddAth(nodeid, flowid, workid, athNo, frmID, filePath, fileName, sort, 0, 0);
	}

	public static String CCForm_AddAth(int nodeid, String flowid, long workid, String athNo, String frmID,
			String filePath, String fileName) throws Exception {
		return CCForm_AddAth(nodeid, flowid, workid, athNo, frmID, filePath, fileName, null, 0, 0);
	}

	public static String CCForm_AddAth(int nodeid, String flowid, long workid, String athNo, String frmID,
			String filePath, String fileName, String sort, long fid, long pworkid) throws Exception {
		String pkVal = String.valueOf(workid);
		// 多附件描述.
		FrmAttachment athDesc = new FrmAttachment(athNo);
		MapData mapData = new MapData(frmID);
		String msg = null;

		/// 获取表单方案
		// 求主键. 如果该表单挂接到流程上.
		if (nodeid != 0) {
			// 判断表单方案。
			FrmNode fn = new FrmNode(nodeid, frmID);
			if (fn.getFrmSln() == FrmSln.Readonly) {
				return "err@不允许上传附件.";
			}

			// 是默认的方案的时候.
			if (fn.getFrmSln() == FrmSln.Default) {
				// 判断当前方案设置的whoIsPk ，让附件集成 whoIsPK 的设置。
				if (fn.getWhoIsPK() == WhoIsPK.FID) {
					pkVal = String.valueOf(fid);
				}

				if (fn.getWhoIsPK() == WhoIsPK.PWorkID) {
					pkVal = String.valueOf(pworkid);
				}
			}

			// 自定义方案.
			if (fn.getFrmSln() == FrmSln.Self) {
				athDesc = new FrmAttachment(athNo + "_" + nodeid);
				if (athDesc.getHisCtrlWay() == AthCtrlWay.FID) {
					pkVal = String.valueOf(fid);
				}

				if (athDesc.getHisCtrlWay() == AthCtrlWay.PWorkID) {
					 if (pworkid == 0)
		                    pworkid = DBAccess.RunSQLReturnValInt("SELECT PWorkID FROM WF_GenerWorkFlow WHERE WorkID=" + workid,0);

					 pkVal = String.valueOf(pworkid);
				}
			}
		}

		/// 获取表单方案

		// 获取上传文件是否需要加密
		boolean fileEncrypt = SystemConfig.getIsEnableAthEncrypt();

		/// 文件上传的iis服务器上 or db数据库里.
		if (athDesc.getAthSaveWay() == AthSaveWay.IISServer) {
			String savePath = athDesc.getSaveTo();
			if (savePath.contains("@") == true || savePath.contains("*") == true) {
				/* 如果有变量 */
				savePath = savePath.replace("*", "@");

				if (savePath.contains("@") && nodeid != 0) {
					/* 如果包含 @ */
					bp.wf.Flow flow = new bp.wf.Flow(flowid);
					bp.wf.data.GERpt myen = flow.getHisGERpt();
					myen.setOID(workid);
					myen.RetrieveFromDBSources();
					savePath = bp.wf.Glo.DealExp(savePath, myen, null);
				}
				if (savePath.contains("@") == true) {
					throw new RuntimeException("@路径配置错误,变量没有被正确的替换下来." + savePath);
				}
			} else {
				savePath = athDesc.getSaveTo() + "/" + pkVal;
			}

			// 替换关键的字串.
			savePath = savePath.replace("\\\\", "/");

			try {
				savePath = SystemConfig.getPathOfWebApp() + savePath;
			} catch (RuntimeException ex) {
				savePath = SystemConfig.getPathOfDataUser() + "UploadFile/" + mapData.getNo() + "/";
				// return "err@获取路径错误" + ex.Message + ",配置的路径是:" + savePath +
				// ",您需要在附件属性上修改该附件的存储路径.";
			}

			try {
				if ((new File(savePath)).isDirectory() == false) {
					(new File(savePath)).mkdirs();
				}
			} catch (RuntimeException ex) {
				throw new RuntimeException("err@创建路径出现错误，可能是没有权限或者路径配置有问题:" + savePath + "@异常信息:" + ex.getMessage());
			}

			String guid = DBAccess.GenerGUID();
			String ext = fileName.substring(fileName.lastIndexOf("."));
			String realSaveTo = savePath + "/" + guid + "." + fileName;
			realSaveTo = realSaveTo.replace("~", "-");
			realSaveTo = realSaveTo.replace("'", "-");
			realSaveTo = realSaveTo.replace("*", "-");
			if (fileEncrypt == true) {
				String strtmp = realSaveTo + ".tmp";
				if ((new File(filePath)).isFile() == true) {
					Files.copy(Paths.get(filePath), Paths.get(strtmp), StandardCopyOption.COPY_ATTRIBUTES); // 先明文保存到本地(加个后缀名.tmp)
				} else {
					return "err@需要保存的文件不存在";
				}
				AesEncodeUtil.encryptFile(strtmp, strtmp.replace(".tmp", "")); // 加密
				(new File(strtmp)).delete(); // 删除临时文件
			} else {
				// 文件保存的路径
				if ((new File(filePath)).isFile() == true) {
					Files.copy(Paths.get(filePath), Paths.get(realSaveTo), StandardCopyOption.COPY_ATTRIBUTES);
				} else {
					return "err@需要保存的文件不存在";
				}
			}

			File info = new File(realSaveTo);
			FrmAttachmentDB dbUpload = new FrmAttachmentDB();
			dbUpload.setMyPK(guid); // athDesc.FK_MapData + oid.ToString();
			dbUpload.setFK_FrmAttachment(athNo);
			dbUpload.setSort(sort);
			dbUpload.setFK_MapData(athDesc.getFK_MapData());
			dbUpload.setFileExts(ext);
			dbUpload.setFID(fid);
			dbUpload.setNodeID(nodeid);
			if (fileEncrypt == true) {
				dbUpload.SetPara("IsEncrypt", 1);
			}

			/// 处理文件路径，如果是保存到数据库，就存储pk.
			if (athDesc.getAthSaveWay() == AthSaveWay.IISServer) {
				// 文件方式保存
				dbUpload.setFileFullName(realSaveTo);
			}

			if (athDesc.getAthSaveWay() == AthSaveWay.FTPServer) {
				// 保存到数据库
				dbUpload.setFileFullName(dbUpload.getMyPK());
			}

			/// 处理文件路径，如果是保存到数据库，就存储pk.

			dbUpload.setFileName(fileName);
			dbUpload.setFileSize((float) info.length());
			dbUpload.setRDT(DataType.getCurrentDataTimess());
			dbUpload.setRec(WebUser.getNo());
			dbUpload.setRecName(WebUser.getName());
			dbUpload.setFID(fid);
			dbUpload.setUploadGUID(guid);
			dbUpload.setRefPKVal(pkVal);
			dbUpload.Insert();

			if (athDesc.getAthSaveWay() == AthSaveWay.DB) {
				// 执行文件保存.
				DBAccess.SaveFileToDB(realSaveTo, dbUpload.getEnMap().getPhysicsTable(), "MyPK", dbUpload.getMyPK(),
						"FDB");
			}
		}

		/// 文件上传的iis服务器上 or db数据库里.

		/// 保存到数据库 / FTP服务器上.
		if (athDesc.getAthSaveWay() == AthSaveWay.DB || athDesc.getAthSaveWay() == AthSaveWay.FTPServer) {
			String guid = DBAccess.GenerGUID();

			// 把文件临时保存到一个位置.
			String temp = SystemConfig.getPathOfTemp() + guid + ".tmp";

			if (fileEncrypt == true) {

				String strtmp = SystemConfig.getPathOfTemp() + guid + "_Desc" + ".tmp";
				if ((new File(filePath)).isFile() == true) {
					Files.copy(Paths.get(filePath), Paths.get(strtmp), StandardCopyOption.COPY_ATTRIBUTES); // 先明文保存到本地(加个后缀名.tmp)
				} else {
					return "err@需要保存的文件不存在";
				}

				AesEncodeUtil.encryptFile(strtmp, temp); // 加密
				(new File(strtmp)).delete(); // 删除临时文件
			} else {
				// 文件保存的路径
				if ((new File(filePath)).isFile() == true) {
					Files.copy(Paths.get(filePath), Paths.get(temp), StandardCopyOption.COPY_ATTRIBUTES);
				} else {
					return "err@需要保存的文件不存在";
				}
			}
			String ext = fileName.substring(fileName.lastIndexOf("."));

			File info = new File(temp);
			FrmAttachmentDB dbUpload = new FrmAttachmentDB();
			dbUpload.setMyPK(DBAccess.GenerGUID());
			dbUpload.setSort(sort);
			dbUpload.setNodeID(nodeid);
			dbUpload.setFK_MapData(athDesc.getFK_MapData());

			dbUpload.setFK_FrmAttachment(athDesc.getMyPK());
			dbUpload.setFileExts(ext);
			dbUpload.setFID(fid); // 流程id.
			if (fileEncrypt == true) {
				dbUpload.SetPara("IsEncrypt", 1);
			}

			if (athDesc.getAthUploadWay() == AthUploadWay.Inherit) {
				/* 如果是继承，就让他保持本地的PK. */
				dbUpload.setRefPKVal(pkVal.toString());
			}

			if (athDesc.getAthUploadWay() == AthUploadWay.Interwork) {
				/* 如果是协同，就让他是PWorkID. */
				Paras ps = new Paras();
				ps.SQL = "SELECT PWorkID FROM WF_GenerWorkFlow WHERE WorkID=" + SystemConfig.getAppCenterDBVarStr()
						+ "WorkID";
				ps.Add("WorkID", pkVal);
				String pWorkID = String.valueOf(DBAccess.RunSQLReturnValInt(ps, 0));
				if (pWorkID == null || pWorkID.equals("0")) {
					pWorkID = pkVal;
				}

				dbUpload.setRefPKVal(pWorkID);
			}

			dbUpload.setFK_MapData(athDesc.getFK_MapData());
			dbUpload.setFK_FrmAttachment(athDesc.getMyPK());
			dbUpload.setFileName(fileName);
			dbUpload.setFileSize((float) info.length());
			dbUpload.setRDT(DataType.getCurrentDataTimess());
			dbUpload.setRec(WebUser.getNo());
			dbUpload.setRecName(WebUser.getName());
			dbUpload.setFK_Dept(WebUser.getFK_Dept());
			dbUpload.setFK_DeptName(WebUser.getFK_DeptName());
			dbUpload.setUploadGUID(guid);

			if (athDesc.getAthSaveWay() == AthSaveWay.DB) {
				dbUpload.Insert();
				// 把文件保存到指定的字段里.
				dbUpload.SaveFileToDB("FileDB", temp);
			}

			if (athDesc.getAthSaveWay() == AthSaveWay.FTPServer) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
				String ny = sdf.format(new Date());

				String workDir = ny + "/" + athDesc.getFK_MapData() + "/";

				// 特殊处理文件路径.
				if (SystemConfig.getCustomerNo().equals("BWDA")) {

					sdf = new SimpleDateFormat("yyyy_MM_dd");
					ny = sdf.format(new Date());

					ny = ny.replace("_", "/");
					ny = ny.replace("_", "/");

					workDir = ny + "/" + WebUser.getNo() + "/";
				}

				boolean isOK = false;

				if (SystemConfig.getFTPServerType().equals("FTP")) {

					FtpUtil ftpUtil = bp.wf.Glo.getFtpUtil();

					ftpUtil.changeWorkingDirectory(workDir, true);

					// 把文件放在FTP服务器上去.
					isOK = ftpUtil.uploadFile(guid + "." + dbUpload.getFileExts(), temp);

					ftpUtil.releaseConnection();
				}

				if (SystemConfig.getFTPServerType().equals("SFTP")) {

					SftpUtil ftpUtil = bp.wf.Glo.getSftpUtil();

					ftpUtil.changeWorkingDirectory(workDir, true);
					// 把文件放在FTP服务器上去.
					isOK = ftpUtil.uploadFile(guid + "." + dbUpload.getFileExts(), temp);
					ftpUtil.releaseConnection();
				}

				// 删除临时文件
				new File(temp).delete();
				new File(SystemConfig.getPathOfTemp() + "" + guid + "_Desc" + ".tmp").delete();

				// 设置路径.
				dbUpload.setFileFullName(workDir + guid + "." + dbUpload.getFileExts());

				if (isOK == false)
					throw new com.sun.star.uno.Exception("err文件上传失败，请检查ftp服务器配置信息");

				dbUpload.Insert();

			}

		}

		/// 保存到数据库.

		return "";
	}

	/**
	 * sdk表单加载初始化信息
	 * 
	 * @param workid
	 *            工作ID
	 * @return 请参考相关的文档,或者baidu ccbpm sdk表单 SDK_Page_Init
	 * @throws Exception
	 */
	public static String SDK_Page_Init(long workid) throws Exception {
		return bp.wf.AppClass.SDK_Page_Init(workid);
	}

	/// 页面.

	/// 与工作处理器相关的接口
	/**
	 * 获得一个节点要转向的节点
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param ndFrom
	 *            节点从
	 * @param workid
	 *            工作ID
	 * @return 返回可以到达的节点
	 * @throws Exception
	 */
	public static Nodes WorkOpt_GetToNodes(String flowNo, int ndFrom, long workid, long FID) throws Exception {
		Nodes nds = new Nodes();

		Node nd = new Node(ndFrom);
		Nodes toNDs = nd.getHisToNodes();

		Flow fl = nd.getHisFlow();
		GERpt rpt = fl.getHisGERpt();
		rpt.setOID(FID == 0 ? workid : FID);
		rpt.Retrieve();

		// 方向.
		Directions dirs = new Directions(nd.getNodeID());

		// 同表单的子线程.
		Nodes sameSheetNodes = new Nodes();

		// 首先输出普通的节点。
		for (Direction dir : dirs.ToJavaList()) {
			Node mynd = new Node(dir.getToNode());
			if (mynd.getHisRunModel() == RunModel.SubThread) {
				sameSheetNodes.AddEntity(mynd);
				continue; // 如果是子线程节点.
			}

			// 是否可以处理？
			boolean bIsCanDo = true;

			/// 判断方向条件,如果设置了方向条件，判断是否可以通过，不能通过的，就不让其显示.
			Conds conds = new Conds();
			int i = conds.Retrieve(CondAttr.FK_Node, nd.getNodeID(), CondAttr.ToNodeID, mynd.getNodeID(),
					CondAttr.CondType, CondType.Dir.getValue(), CondAttr.Idx);
			// 设置方向条件，就判断它。
			if (i > 0) {
				bIsCanDo = conds.GenerResult(rpt);
			}

			// 条件不符合则不通过
			if (bIsCanDo == false) {
				continue;
			}

			///

			nds.AddEntity(mynd);
		}

		// 同表单子线程.
		for (Node mynd : sameSheetNodes.ToJavaList()) {
			if (mynd.getHisRunModel() != RunModel.SubThread) {
				continue; // 如果是子线程节点.
			}

			if (mynd.getHisSubThreadType() == SubThreadType.UnSameSheet) {
				continue; // 如果是异表单的分合流.
			}

			/// 判断方向条件,如果设置了方向条件，判断是否可以通过，不能通过的，就不让其显示.
			Conds conds = new Conds();
			int i = conds.Retrieve(CondAttr.FK_Node, nd.getNodeID(), CondAttr.ToNodeID, mynd.getNodeID(),
					CondAttr.CondType, CondType.Dir.getValue(), CondAttr.Idx);

			// 数量.
			if (conds.size() == 0) {
				nds.AddEntity(mynd);
				continue;
			}

			// 是否可以处理.
			boolean bIsCanDo = false;
			for (Direction dir : dirs.ToJavaList()) {
				if (dir.getToNode() == mynd.getNodeID()) {
					bIsCanDo = conds.GenerResult(rpt);
				}
			}

			///

			// 如果通过了.
			if (bIsCanDo == true) {
				nds.AddEntity(mynd);
			}
		}

		// 检查是否具有异表单的子线程.
		boolean isHave = false;
		for (Node mynd : toNDs.ToJavaList()) {
			if (mynd.getHisSubThreadType() == SubThreadType.UnSameSheet) {
				isHave = true;
			}
		}

		if (isHave) {
			Node myn1d = new Node();
			myn1d.setNodeID(0);
			myn1d.setName("可以分发启动的节点");
			nds.AddEntity(myn1d);

			/* 增加异表单的子线程 */
			for (Node mynd : toNDs.ToJavaList()) {
				if (mynd.getHisSubThreadType() != SubThreadType.UnSameSheet) {
					continue;
				}

				/// 判断方向条件,如果设置了方向条件，判断是否可以通过，不能通过的，就不让其显示.
				Conds conds = new Conds();
				int i = conds.Retrieve(CondAttr.FK_Node, nd.getNodeID(), CondAttr.ToNodeID, mynd.getNodeID(),
						CondAttr.CondType, CondType.Dir.getValue(), CondAttr.Idx);
				// 设置方向条件，就判断它。
				if (i > 0) {

					// 判断是否可以通过.
					if (conds.GenerResult(rpt) == false) {
						continue;
					}
				}

				///

				nds.AddEntity(mynd);
			}
		}

		// 返回它.
		return nds;
	}

	/**
	 * 在节点选择转向功能界面，获得当前人员上一次选择的节点，在界面里让其自动选择。 以改善用户操作体验，就类似于默认记忆上一次的操作功能。
	 * 
	 * @param flowNo
	 *            流程编号
	 * @param nodeID
	 *            当前节点编号
	 * @return 返回上一次当前用户选择的节点,如果没有找到（当前用户第一次发送的情况下找不到）就返回0.
	 * @throws Exception
	 * @throws NumberFormatException
	 */
	public static int WorkOpt_ToNodes_GetLasterSelectNodeID(String flowNo, int nodeID)
			throws NumberFormatException, Exception {
		String sql = "";
		switch (SystemConfig.getAppCenterDBType()) {
		case MSSQL:
		case Access:
			sql = "SELECT TOP 1 NDTo FROM ND" + Integer.parseInt(flowNo) + "Track WHERE EmpFrom='" + WebUser.getNo()
					+ "' AND NDFrom=" + nodeID + " AND (ActionType=" + ActionType.Forward.getValue() + " OR ActionType="
					+ ActionType.ForwardFL.getValue() + " OR ActionType=" + ActionType.SubThreadForward.getValue()
					+ ")  ORDER BY RDT DESC";
			break;
		case Oracle:
		case KingBaseR3:
		case KingBaseR6:
			sql = "SELECT NDTo FROM ND" + Integer.parseInt(flowNo) + "Track WHERE  RowNum=1 AND EmpFrom='"
					+ WebUser.getNo() + "' AND NDFrom=" + nodeID + " AND (ActionType=" + ActionType.Forward.getValue()
					+ " OR ActionType=" + ActionType.ForwardFL.getValue() + " OR ActionType="
					+ ActionType.SubThreadForward.getValue() + ")  ORDER BY RDT DESC";
			break;
		case MySQL:
			sql = "SELECT NDTo FROM ND" + Integer.parseInt(flowNo) + "Track WHERE EmpFrom='" + WebUser.getNo()
					+ "' AND NDFrom=" + nodeID + " AND (ActionType=" + ActionType.Forward.getValue() + " OR ActionType="
					+ ActionType.ForwardFL.getValue() + " OR ActionType=" + ActionType.SubThreadForward.getValue()
					+ ") limit 0,1";
			break;
		case Informix:
			sql = "SELECT first 1 NDTo FROM ND" + Integer.parseInt(flowNo) + "Track WHERE EmpFrom='" + WebUser.getNo()
					+ "' AND NDFrom=" + nodeID + " AND (ActionType=" + ActionType.Forward.getValue() + " OR ActionType="
					+ ActionType.ForwardFL.getValue() + " OR ActionType=" + ActionType.SubThreadForward.getValue()
					+ ")  ORDER BY RDT DESC";
			break;
		case PostgreSQL:
			sql = "SELECT NDTo FROM ND" + Integer.parseInt(flowNo) + "Track WHERE EmpFrom='" + WebUser.getNo()
					+ "' AND NDFrom=" + nodeID + " AND (ActionType=" + ActionType.Forward.getValue() + " OR ActionType="
					+ ActionType.ForwardFL.getValue() + " OR ActionType=" + ActionType.SubThreadForward.getValue()
					+ ") ORDER BY RDT DESC limit 1";
			break;
		default:
			throw new RuntimeException("@没有实现该类型的数据库支持.");
		}
		return DBAccess.RunSQLReturnValInt(sql, 0);
	}

	/**
	 * 发送到节点
	 * 
	 * @param flowNo
	 * @param node
	 * @param workid
	 * @param fid
	 * @param toNodes
	 * @throws Exception
	 */
	public static SendReturnObjs WorkOpt_SendToNodes(String flowNo, int nodeID, long workid, long fid, String toNodes)
			throws Exception {
		// 把参数更新到数据库里面.
		GenerWorkFlow gwf = new GenerWorkFlow();
		gwf.setWorkID(workid);
		gwf.RetrieveFromDBSources();
		gwf.setParasToNodes(toNodes);
		gwf.Save();

		Node nd = new Node(nodeID);
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.Retrieve();

		// 以下代码是从 MyFlow.htm Send 方法copy 过来的，需要保持业务逻辑的一致性，所以代码需要保持一致.
		WorkNode firstwn = new WorkNode(wk, nd);
		String msg = "";
		SendReturnObjs objs = firstwn.NodeSend();
		return objs;
	}

	/**
	 * 获得接收人的数据源
	 * 
	 * @param nodeID
	 *            指定节点
	 * @param WorkID
	 *            工作ID
	 * @param FID
	 *            流程ID
	 * @return
	 * @throws Exception
	 */

	public static DataSet WorkOpt_AccepterDB(int nodeID, long WorkID) throws Exception {
		return WorkOpt_AccepterDB(nodeID, WorkID, 0);
	}

	public static DataSet WorkOpt_AccepterDB(int nodeID, long WorkID, long FID) throws Exception {
		DataSet ds = new DataSet();

		Selector en = new Selector(nodeID);
		switch (en.getSelectorModel()) {
		case Station:
			DataTable dt = WorkOpt_Accepter_ByStation(nodeID);
			dt.TableName = "Port_Emp";
			ds.Tables.add(dt);
			break;
		case SQL:
			ds = WorkOpt_Accepter_BySQL(nodeID);
			break;
		case Dept:
			ds = WorkOpt_Accepter_ByDept(nodeID);
			break;
		case Emp:
			ds = WorkOpt_Accepter_ByEmp(nodeID);
			break;
		case Url:
		default:
			break;
		}
		return ds;
	}

	/**
	 * 获取节点绑定岗位人员
	 * 
	 * @param nodeID
	 *            指定的节点
	 * @return
	 * @throws Exception
	 */
	private static DataTable WorkOpt_Accepter_ByStation(int nodeID) throws Exception {
		if (nodeID == 0) {
			throw new RuntimeException(
					"@流程设计错误，没有转向的节点。举例说明: 当前是A节点。如果您在A点的属性里启用了[接受人]按钮，那么他的转向节点集合中(就是A可以转到的节点集合比如:A到B，A到C, 那么B,C节点就是转向节点集合)，必须有一个节点是的节点属性的[访问规则]设置为[由上一步发送人员选择]");
		}

		NodeStations stas = new NodeStations(nodeID);
		if (stas.size() == 0) {
			bp.wf.Node toNd = new bp.wf.Node(nodeID);
			throw new RuntimeException("@流程设计错误：设计员没有设计节点[" + toNd.getName() + "]，接受人的岗位范围。");
		}
		// 优先解决本部门的问题。
		String sql = "";

		sql = "SELECT A.No,A.Name, A.FK_Dept, B.Name as DeptName FROM Port_Emp A,Port_Dept B WHERE A.FK_Dept=B.No AND a.NO IN (";
		sql += "SELECT FK_EMP FROM Port_DeptEmpStation WHERE FK_STATION ";
		sql += "IN (SELECT FK_STATION FROM WF_NodeStation WHERE FK_Node=" + nodeID + ") ";
		sql += ") AND a.No IN (SELECT No FROM Port_Emp WHERE FK_Dept ='" + WebUser.getFK_Dept() + "')";
		sql += " ORDER BY B.Idx,B.No,A.Idx,A.No ";

		DataTable dt = DBAccess.RunSQLReturnTable(sql);
		if (dt.Rows.size() != 0) {
			return dt;
		}

		// 组织结构中所有岗位人员
		sql = "SELECT A.No,A.Name, A.FK_Dept, B.Name as DeptName FROM Port_Emp A,Port_Dept B WHERE A.FK_Dept=B.No AND a.NO IN (";
		sql += "SELECT FK_EMP FROM " + bp.wf.Glo.getEmpStation() + " WHERE FK_STATION ";
		sql += "IN (SELECT FK_STATION FROM WF_NodeStation WHERE FK_Node=" + nodeID + ") ";
		sql += ") ORDER BY A.FK_DEPT,A.No ";
		return DBAccess.RunSQLReturnTable(sql);
	}

	/**
	 * 按sql方式
	 * 
	 * @throws Exception
	 */
	private static DataSet WorkOpt_Accepter_BySQL(int nodeID) throws Exception {
		DataSet ds = new DataSet();
		Selector MySelector = new Selector(nodeID);
		String sqlGroup = MySelector.getSelectorP1();
		sqlGroup = sqlGroup.replace("@WebUser.No", WebUser.getNo());
		sqlGroup = sqlGroup.replace("@WebUser.Name", WebUser.getName());
		sqlGroup = sqlGroup.replace("@WebUser.FK_Dept", WebUser.getFK_Dept());

		String sqlDB = MySelector.getSelectorP2();
		sqlDB = sqlDB.replace("@WebUser.No", WebUser.getNo());
		sqlDB = sqlDB.replace("@WebUser.Name", WebUser.getName());
		sqlDB = sqlDB.replace("@WebUser.FK_Dept", WebUser.getFK_Dept());

		DataTable dtGroup = DBAccess.RunSQLReturnTable(sqlGroup);
		dtGroup.TableName = "Port_Dept";
		ds.Tables.add(dtGroup);
		DataTable dtDB = DBAccess.RunSQLReturnTable(sqlDB);
		dtDB.TableName = "Port_Emp";
		ds.Tables.add(dtDB);

		return ds;
	}

	/**
	 * 获取接收人选择器，按部门绑定
	 * 
	 * @param ToNode
	 * @return
	 */
	private static DataSet WorkOpt_Accepter_ByDept(int nodeID) {
		DataSet ds = new DataSet();
		String orderByIdx = "Idx,";
		String sqlGroup = "SELECT No,Name FROM Port_Dept WHERE No IN (SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node='"
				+ nodeID + "') ORDER BY " + orderByIdx + "No";
		String sqlDB = "SELECT No,Name, FK_Dept FROM Port_Emp WHERE FK_Dept IN (SELECT FK_Dept FROM WF_NodeDept WHERE FK_Node='"
				+ nodeID + "') ORDER BY " + orderByIdx + "No";

		DataTable dtGroup = DBAccess.RunSQLReturnTable(sqlGroup);
		dtGroup.TableName = "Port_Dept";
		ds.Tables.add(dtGroup);

		DataTable dtDB = DBAccess.RunSQLReturnTable(sqlDB);
		dtDB.TableName = "Port_Emp";
		ds.Tables.add(dtDB);
		return ds;
	}

	/**
	 * 按BindByEmp 方式
	 */
	private static DataSet WorkOpt_Accepter_ByEmp(int nodeID) {
		String orderByIdx = "Idx,";
		String sqlGroup = "SELECT No,Name FROM Port_Dept WHERE No IN (SELECT FK_Dept FROM Port_Emp WHERE No in(SELECT FK_EMP FROM WF_NodeEmp WHERE FK_Node='"
				+ nodeID + "')) ORDER BY " + orderByIdx + "No";
		String sqlDB = "SELECT No,Name,FK_Dept FROM Port_Emp WHERE No in (SELECT FK_EMP FROM WF_NodeEmp WHERE FK_Node='"
				+ nodeID + "') ORDER BY " + orderByIdx + "No";

		DataSet ds = new DataSet();
		DataTable dtGroup = DBAccess.RunSQLReturnTable(sqlGroup);
		dtGroup.TableName = "Port_Dept";
		ds.Tables.add(dtGroup);

		DataTable dtDB = DBAccess.RunSQLReturnTable(sqlDB);
		dtDB.TableName = "Port_Emp";
		ds.Tables.add(dtDB);
		return ds;
	}

	/**
	 * 设置指定的节点接受人
	 * 
	 * @param nodeID
	 *            节点ID
	 * @param workid
	 *            工作ID
	 * @param fid
	 *            流程ID
	 * @param emps
	 *            指定的人员集合zhangsan,lisi,wangwu
	 * @param isNextTime
	 *            是否下次自动设置
	 * @throws Exception
	 */
	public static void WorkOpt_SetAccepter(int nodeID, long workid, long fid, String emps, boolean isNextTime)
			throws Exception {
		SelectAccpers ens = new SelectAccpers();
		ens.Delete(SelectAccperAttr.FK_Node, nodeID, SelectAccperAttr.WorkID, workid);

		// 下次是否记忆选择，清空掉。
		String sql = "UPDATE WF_SelectAccper SET " + SelectAccperAttr.IsRemember + " = 0 WHERE Rec='" + WebUser.getNo()
				+ "' AND IsRemember=1 AND FK_Node=" + nodeID;
		DBAccess.RunSQL(sql);

		// 开始执行保存.
		String[] strs = emps.split("[,]", -1);
		for (String str : strs) {
			if (DataType.IsNullOrEmpty(str) == true) {
				continue;
			}

			SelectAccper en = new SelectAccper();
			en.setMyPK(nodeID + "_" + workid + "_" + str);
			en.setFK_Emp(str);
			en.setFK_Node(nodeID);

			en.setWorkID(workid);
			en.setRec(WebUser.getNo());
			en.setIsRemember(isNextTime);
			en.Insert();
		}
	}

	/**
	 * 发送到节点
	 * 
	 * @param flowNo
	 * @param node
	 * @param workid
	 * @param fid
	 * @param toNodes
	 * @throws Exception
	 */
	public static SendReturnObjs WorkOpt_SendToEmps(String flowNo, int nodeID, long workid, long fid, int toNodeID,
			String toEmps, boolean isRememberMe) throws Exception {
		WorkOpt_SetAccepter(toNodeID, workid, fid, toEmps, isRememberMe);

		Node nd = new Node(nodeID);
		Work wk = nd.getHisWork();
		wk.setOID(workid);
		wk.Retrieve();

		// 以下代码是从 MyFlow.htm Send 方法copy 过来的，需要保持业务逻辑的一致性，所以代码需要保持一致.
		WorkNode firstwn = new WorkNode(wk, nd);
		String msg = "";
		SendReturnObjs objs = firstwn.NodeSend();
		return objs;
	}

	///

	/// 附件上传
	/**
	 * 上传文件.
	 * 
	 * @param FileByte
	 * @param fileName
	 * @return
	 */
	/*
	 * public static String UploadFile(byte[] FileByte, String fileName) {
	 * String path = HttpContextHelper.RequestApplicationPath +
	 * "\\DataUser\\UploadFile"; if (!(new File(path)).isDirectory()) { (new
	 * File(path)).mkdirs(); }
	 * 
	 * String filePath = path + "\\" + fileName; if ((new
	 * File(filePath)).isFile()) { (new File(filePath)).delete(); }
	 * 
	 * //这里使用绝对路径来索引 FileOutputStream stream = new FileOutputStream(filePath);
	 * stream.write(FileByte, 0, FileByte.length); stream.close();
	 * 
	 * return filePath; }
	 * 
	 */
	///

	/// 调度相关的操作.
	/**
	 * 更新时间状态, 交付给 huangzhimin. 作用：按照当前的时间，每天两次更新WF_GenerWorkFlow 的 TodoSta
	 * 状态字段。 该字段： 0=正常(绿牌), 1=预警(黄牌), 2=逾期(红牌), 3=按时完成(绿牌) , 4=逾期完成(红牌).
	 * 该方法作用是，每天，中午时间段，与下午时间段，执行更新这两个状态，仅仅更新两次。
	 * 
	 * @throws Exception
	 */
	public static void DTS_GenerWorkFlowTodoSta() throws Exception {
		// 中午的更新, 与发送邮件通知.
		boolean isPM = false;

		/// 求出是否可以更新状态.
		if (DateUtils.getHour(new Date()) >= 9 && DateUtils.getHour(new Date()) < 12) {
			isPM = true;
			String timeKey = "DTSTodoStaPM" + DataType.getCurrentDateByFormart("yyMMdd");
			Paras ps = new Paras();
			ps.SQL = "SELECT Val FROM Sys_GloVar WHERE No='" + timeKey + "'";
			String time = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (time == null) {
				GloVar var = new GloVar();
				var.setNo(timeKey);
				var.setName("时效调度 WFTodoSta PM 调度");
				var.setGroupKey("WF");
				var.setVal(timeKey); // 更新时间点.
				var.setNote("时效调度PM" + timeKey);
				var.Insert();
				time = var.getVal();
			} else {
				/* 如果有数据，就返回，说明已经执行过了。 */
				return;
			}
		}

		// 下午时间段.
		if (DateUtils.getHour(new Date()) >= 13 && DateUtils.getHour(new Date()) < 18) {
			String timeKey = "DTSTodoStaAM" + DataType.getCurrentDateByFormart("yyMMdd");
			Paras ps = new Paras();
			ps.SQL = "SELECT Val FROM Sys_GloVar WHERE No='" + timeKey + "'";
			String time = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (time == null) {
				GloVar var = new GloVar();
				var.setNo(timeKey);
				var.setName("时效调度 WFTodoSta AM 调度");
				var.setGroupKey("WF");
				var.setVal(timeKey); // 更新时间点.
				var.setNote("时效调度AM" + timeKey);
				var.Insert();
				time = var.getVal();
			} else {
				/* 如果有数据，就返回，说明已经执行过了。 */
				return;
			}
		}

		/// 求出是否可以更新状态.

		bp.wf.dts.DTS_GenerWorkFlowTodoSta en = new bp.wf.dts.DTS_GenerWorkFlowTodoSta();
		en.Do();

	}

	/**
	 * 预警与逾期的提醒.
	 * 
	 * @throws Exception
	 */
	private static void DTS_SendMsgToWorker() throws Exception {

		/// 处理预警的消息发送.
		if (DateUtils.getHour(new Date()) >= 0 && DateUtils.getHour(new Date()) < 12) {
			String timeKey = "DTSWarningPM" + DataType.getCurrentDateByFormart("yyMMdd");
			Paras ps = new Paras();
			ps.SQL = "SELECT Val FROM Sys_GloVar WHERE No='" + timeKey + "'";
			String time = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (time != null) {
				return;
			}

			bp.wf.dts.DTS_SendMsgToWarningWorker en = new bp.wf.dts.DTS_SendMsgToWarningWorker();
			en.Do();

		}

		///
	}

	/**
	 * 生成工作的 TimeSpan
	 * 
	 * @throws Exception
	 */
	public static void DTS_GenerWorkFlowTimeSpan() throws Exception {
		if (DateUtils.getHour(new Date()) >= 8 && DateUtils.getHour(new Date()) < 10
				&& Integer.parseInt(DateUtils.dayForWeek(new Date())) == DayOfWeek.MONDAY.getValue()) {
			String timeKey = "DTSTimeSpanPM" + DataType.getCurrentDateByFormart("yyMMdd");
			Paras ps = new Paras();
			ps.SQL = "SELECT Val FROM Sys_GloVar WHERE No='" + timeKey + "'";
			String time = DBAccess.RunSQLReturnStringIsNull(ps, null);
			if (time == null) {
				GloVar var = new GloVar();
				var.setNo(timeKey);
				var.setName("设置时间段" + timeKey + "一周执行一次.");
				var.setGroupKey("WF");
				var.setVal(timeKey); // 更新时间点.
				var.setNote("设置时间段PM" + timeKey);
				var.Insert();
			} else {
				return;
			}
		}

		// 执行调度.
		bp.wf.dts.DTS_GenerWorkFlowTimeSpan ts = new bp.wf.dts.DTS_GenerWorkFlowTimeSpan();
		ts.Do();
	}

	public static String CCForm_AddAth(int nodeid, int workid, String athNo, String frmID, String filePath,
			String fileName) {
		return "增加成功";
	}

	/**
	 * WhoIsPK
	 * @param workid
	 * @param pworkid
	 * @param fid
	 * @param fk_node
	 * @param fk_mapData
	 * @param athDesc
	 * @return
	 */
	public static String GetAthRefPKVal(long workid, long pworkid, long fid, int fk_node, String fk_mapData, FrmAttachment athDesc)throws Exception
	{
		long pkval = 0;
		if (fk_node == 0 || fk_node == 9999)
			return "0";

		AthCtrlWay athCtrlWay = athDesc.getHisCtrlWay();
		Node nd = new Node(fk_node);
		//表单方案
		FrmNode fn = new FrmNode(fk_node, fk_mapData);
		//树形表单
		if (nd.getHisFormType() == NodeFormType.SheetTree)
			athCtrlWay = AthCtrlWay.WorkID;
			//单表单
		else if (nd.getHisFormType() == NodeFormType.RefOneFrmTree)
		{
			switch (fn.getWhoIsPK())
			{
				case OID:
					athCtrlWay = AthCtrlWay.WorkID;
					break;
				case FID:
					athCtrlWay = AthCtrlWay.FID;
					break;
				case PWorkID:
					athCtrlWay = AthCtrlWay.PWorkID;
					break;
				case P2WorkID:
					athCtrlWay = AthCtrlWay.P2WorkID;
					break;
				case P3WorkID:
					athCtrlWay = AthCtrlWay.P3WorkID;
					break;
				case RootFlowWorkID:
					athCtrlWay = AthCtrlWay.RootFlowWorkID;
					break;
				default:
					athCtrlWay = athDesc.getHisCtrlWay();
					break;
			}

		}

		//根据控制权限获取RefPK的值
		if (athCtrlWay == AthCtrlWay.WorkID)
			pkval = workid;

		if (athCtrlWay == AthCtrlWay.FID)
			pkval = fid;


		//如果是父流程的数据. @lizhen
		if (athCtrlWay == AthCtrlWay.PWorkID)
		{
			if (pworkid == 0)
				pworkid = DBAccess.RunSQLReturnValInt("SELECT PWorkID FROM WF_GenerWorkFlow WHERE WorkID=" + workid, 0);

			pkval = pworkid;
		}


		if (athCtrlWay == AthCtrlWay.P2WorkID)
		{
			//根据流程的PWorkID获取他的爷爷流程
			pkval = DBAccess.RunSQLReturnValInt("SELECT PWorkID FROM WF_GenerWorkFlow WHERE WorkID=" + pworkid, 0);
		}
		if (athCtrlWay == AthCtrlWay.P3WorkID)
		{
			String sql = "Select PWorkID From WF_GenerWorkFlow Where WorkID=(Select PWorkID From WF_GenerWorkFlow Where WorkID=" + pworkid + ")";
			//根据流程的PWorkID获取他的P2流程
			pkval = DBAccess.RunSQLReturnValInt(sql, 0);
		}
		if (athCtrlWay == AthCtrlWay.RootFlowWorkID)
			pkval = bp.wf.Dev2Interface.GetRootWorkIDBySQL(workid, pworkid);
		return String.valueOf(pkval);
	}

	/// <summary>
	/// 根据WorkID获取根节点的WorkID
	/// </summary>
	/// <param name="workId"></param>
	/// <returns></returns>
	public static long GetRootWorkIDBySQL(long workId, long pworkid) throws Exception
	{
		if (pworkid == 0)
			return workId;
		GenerWorkFlow gwf = new GenerWorkFlow(pworkid);
		if (gwf.getPWorkID() == 0)
			return pworkid;
		gwf = new GenerWorkFlow(gwf.getPWorkID());
		if (gwf.getPWorkID() == 0)
			return gwf.getWorkID();
		return gwf.getPWorkID();


	}


}