package bp.wf.httphandler;

import bp.da.*;
import bp.difference.SystemConfig;
import bp.difference.handler.WebContralBase;
import bp.sys.*;
import bp.tools.HttpClientUtil;
import bp.web.*;
import bp.port.*;
import bp.en.*;
import bp.wf.*;
import net.sf.json.JSONObject;

import java.util.*;
import java.io.*;

/** 
 页面功能实体
*/
public class CCMobile extends WebContralBase
{
	/** 
	 构造函数
	 * @throws Exception 
	*/
	public CCMobile() throws Exception
	{
		WebUser.setSheBei("Mobile");
	}


		///执行父类的重写方法.
	/** 
	 默认执行的方法
	 
	 @return 
	*/
	@Override
	protected String DoDefaultMethod()
	{
		switch (this.getDoType())
		{

			case "DtlFieldUp": //字段上移
				return "执行成功.";
			default:
				break;
		}

		//找不不到标记就抛出异常.
		throw new RuntimeException("@标记[" + this.getDoType() + "]，没有找到.");
	}

		/// 执行父类的重写方法.

	public final String Login_Init() throws Exception
	{
		bp.wf.httphandler.WF ace = new WF();
		return ace.Login_Init();
	}

	public final String Login_Submit() throws Exception
	{
		String userNo = this.GetRequestVal("TB_No");
		String pass = this.GetRequestVal("TB_PW");

		bp.port.Emp emp = new Emp();
		emp.setNo(userNo);
		if (emp.RetrieveFromDBSources() == 0)
		{
			if (DBAccess.IsExitsTableCol("Port_Emp", "NikeName") == true)
			{
				/*如果包含昵称列,就检查昵称是否存在.*/
				Paras ps = new Paras();
				ps.SQL="SELECT No FROM Port_Emp WHERE NikeName=" + SystemConfig.getAppCenterDBVarStr() + "userNo";
				ps.Add("userNo", userNo);
				//string sql = "SELECT No FROM Port_Emp WHERE NikeName='" + userNo + "'";
				String no = DBAccess.RunSQLReturnStringIsNull(ps, null);
				if (no == null)
				{
					return "err@用户名或者密码错误.";
				}

				emp.setNo(no);
				int i = emp.RetrieveFromDBSources();
				if (i == 0)
				{
					return "err@用户名或者密码错误.";
				}
			}
			else
			{
				return "err@用户名或者密码错误.";
			}
		}

		if (emp.CheckPass(pass) == false)
		{
			return "err@用户名或者密码错误.";
		}

		//调用登录方法.
		bp.wf.Dev2Interface.Port_Login(emp.getNo());

		return "登录成功.";
	}
	/** 
	 会签列表
	 
	 @return 
	 * @throws Exception 
	*/
	public final String HuiQianList_Init() throws Exception
	{
		WF wf = new WF();
		return wf.HuiQianList_Init();
	}

	public final String GetUserInfo() throws Exception
	{
		if (WebUser.getNo() == null)
		{
			return "{err:'nologin'}";
		}

		StringBuilder append = new StringBuilder();
		append.append("{");
		String userPath = SystemConfig.getPathOfWebApp() + "/DataUser/UserIcon/";
		String userIcon = userPath + WebUser.getNo() + "Biger.png";
		if ((new File(userIcon)).isFile())
		{
			append.append("UserIcon:'" + WebUser.getNo() + "Biger.png'");
		}
		else
		{
			append.append("UserIcon:'DefaultBiger.png'");
		}
		append.append(",UserName:'" + WebUser.getName() + "'");
		append.append(",UserDeptName:'" + WebUser.getFK_DeptName() + "'");
		append.append("}");
		return append.toString();
	}
	public final String StartGuide_MulitSend() throws Exception
	{
		WF_MyFlow en = new WF_MyFlow();
		return en.StartGuide_MulitSend();
	}
	public final String Home_Init() throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("UserNo", WebUser.getNo());
		ht.put("UserName", WebUser.getName());

		//系统名称.
		ht.put("SysName", SystemConfig.getSysName());
		ht.put("CustomerName", SystemConfig.getCustomerName());

		ht.put("Todolist_EmpWorks", bp.wf.Dev2Interface.getTodolistEmpWorks());
		ht.put("Todolist_Runing", bp.wf.Dev2Interface.getTodolistRuning());
		ht.put("Todolist_Complete", bp.wf.Dev2Interface.getTodolistComplete());
		//ht.Add("Todolist_Sharing", BP.WF.Dev2Interface.Todolist_Sharing);
		ht.put("Todolist_CCWorks", bp.wf.Dev2Interface.getTodolistCCWorks());
		//ht.Add("Todolist_Apply", BP.WF.Dev2Interface.Todolist_Apply); //申请下来的任务个数.
		//ht.Add("Todolist_Draft", BP.WF.Dev2Interface.Todolist_Draft); //草稿数量.

		ht.put("Todolist_HuiQian", bp.wf.Dev2Interface.getTodolistHuiQian()); //会签数量.

		return bp.tools.Json.ToJsonEntityModel(ht);
	}
	/** 
	 查询
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Home_Init_WorkCount() throws Exception
	{
		Paras ps = new Paras();
		ps.SQL="SELECT  TSpan as No, '' as Name, COUNT(WorkID) as Num, FROM WF_GenerWorkFlow WHERE Emps LIKE '%" + SystemConfig.getAppCenterDBVarStr() + "Emps%' GROUP BY TSpan";
		ps.Add("Emps", WebUser.getNo());
		//string sql = "SELECT  TSpan as No, '' as Name, COUNT(WorkID) as Num, FROM WF_GenerWorkFlow WHERE Emps LIKE '%" + WebUser.getNo() + "%' GROUP BY TSpan";
		DataSet ds = new DataSet();
		DataTable dt = DBAccess.RunSQLReturnTable(ps);
		ds.Tables.add(dt);
		if (SystemConfig.getAppCenterDBType() == DBType.Oracle 
				|| SystemConfig.getAppCenterDBType() == DBType.KingBase
				|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL)
		{
			dt.Columns.get(0).setColumnName("TSpan");
			dt.Columns.get(1).setColumnName("Num");
		}

		String sql = "SELECT IntKey as No, Lab as Name FROM "+bp.wf.Glo.SysEnum()+" WHERE EnumKey='TSpan'";
		DataTable dt1 = DBAccess.RunSQLReturnTable(sql);
		for (DataRow dr : dt.Rows)
		{
			for (DataRow mydr : dt1.Rows)
			{

			}
		}

		return bp.tools.Json.ToJson(dt);
	}
	public final String MyFlow_Init() throws Exception
	{
		bp.wf.httphandler.WF_MyFlow wfPage = new WF_MyFlow();
		return wfPage.MyFlow_Init();
	}

	public final String Runing_Init() throws Exception
	{
		bp.wf.httphandler.WF wfPage = new WF();
	  return wfPage.Runing_Init();
	}

	/** 
	 新版本.
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Todolist_Init() throws Exception
	{
		String fk_node = this.GetRequestVal("FK_Node");
		String showWhat = this.GetRequestVal("ShowWhat");
		DataTable dt = bp.wf.Dev2Interface.DB_GenerEmpWorksOfDataTable(WebUser.getNo(), this.getFK_Node(), showWhat);
		return bp.tools.Json.ToJson(dt);
	}
	/** 
	 查询已完成.
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Complete_Init() throws Exception
	{
		DataTable dt = null;
		dt = bp.wf.Dev2Interface.DB_FlowComplete();
		return bp.tools.Json.ToJson(dt);
	}
	public final String DB_GenerReturnWorks() throws Exception
	{
		/* 如果工作节点退回了*/
		ReturnWorks rws = new ReturnWorks();
		rws.Retrieve(ReturnWorkAttr.ReturnToNode, this.getFK_Node(), ReturnWorkAttr.WorkID, this.getWorkID(),ReturnWorkAttr.RDT);
		StringBuilder append = new StringBuilder();
		append.append("[");
		if (rws.size() != 0)
		{
			for (ReturnWork rw : rws.ToJavaList())
			{
				append.append("{");
				append.append("ReturnNodeName:'" + rw.getReturnNodeName() + "',");
				append.append("ReturnerName:'" + rw.getReturnerName() + "',");
				append.append("RDT:'" + rw.getRDT() + "',");
				append.append("NoteHtml:'" + rw.getBeiZhuHtml() + "'");
				append.append("},");
			}
			append.deleteCharAt(append.length() - 1);
		}
		append.append("]");
		return append.toString();
	}

	public final String Start_Init() throws Exception
	{
		bp.wf.httphandler.WF wfPage = new WF();
		return wfPage.Start_Init();
	}

	public final String HandlerMapExt() throws Exception
	{
		WF_CCForm en = new WF_CCForm();
		return en.HandlerMapExt();
	}

	/** 
	 打开手机端
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Do_OpenFlow() throws Exception
	{
		String sid = this.GetRequestVal("SID");
		String[] strs = sid.split("[_]", -1);
		GenerWorkerList wl = new GenerWorkerList();
		int i = wl.Retrieve(GenerWorkerListAttr.FK_Emp, strs[0], GenerWorkerListAttr.WorkID, strs[1], GenerWorkerListAttr.IsPass, 0);

		if (i == 0)
		{
			return "err@提示:此工作已经被别人处理或者此流程已删除。";
		}

		bp.port.Emp empOF = new bp.port.Emp(wl.getFK_Emp());
		WebUser.SignInOfGener(empOF);
		return "MyFlow.htm?FK_Flow=" + wl.getFK_Flow() + "&WorkID=" + wl.getWorkID() + "&FK_Node=" + wl.getFK_Node() + "&FID=" + wl.getFID();
	}
	/** 
	 流程单表单查看.
	 
	 @return json
	 * @throws Exception 
	*/
	public final String FrmView_Init() throws Exception
	{
		bp.wf.httphandler.WF wf = new WF();
		return wf.FrmView_Init();
	}
	/** 
	 撤销发送
	 
	 @return 
	 * @throws Exception 
	*/
	public final String FrmView_UnSend() throws Exception
	{
		bp.wf.httphandler.WF_WorkOpt_OneWork en = new WF_WorkOpt_OneWork();
		return en.OP_UnSend();
	}

	public final String AttachmentUpload_Down() throws Exception
	{
		WF_CCForm ccform = new WF_CCForm();
		return ccform.AttachmentUpload_Down();
	}

	public final String AttachmentUpload_DownByStream() throws Exception
	{
		WF_CCForm ccform = new WF_CCForm();
		return ccform.AttachmentUpload_DownByStream();
	}


		///关键字查询.
	/** 
	 打开表单
	 
	 @return 
	 * @throws Exception 
	*/
	public final String SearchKey_OpenFrm() throws Exception
	{
		bp.wf.httphandler.WF_RptSearch search = new WF_RptSearch();
		return search.KeySearch_OpenFrm();
	}
	/** 
	 执行查询
	 
	 @return 
	 * @throws Exception 
	 * @throws NumberFormatException 
	*/
	public final String SearchKey_Query() throws NumberFormatException, Exception
	{
		bp.wf.httphandler.WF_RptSearch search = new WF_RptSearch();
		return search.KeySearch_Query();
	}

		/// 关键字查询.


		///查询.
	/** 
	 初始化
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Search_Init() throws Exception
	{
		DataSet ds = new DataSet();
		String sql = "";

		String tSpan = this.GetRequestVal("TSpan");
		if (tSpan.equals(""))
		{
			tSpan = null;
		}


			///1、获取时间段枚举/总数.
		SysEnums ses = new SysEnums("TSpan");
		DataTable dtTSpan = ses.ToDataTableField();
		dtTSpan.TableName = "TSpan";
		ds.Tables.add(dtTSpan);

		if (this.getFK_Flow() == null)
		{
			sql = "SELECT  TSpan as No, COUNT(WorkID) as Num FROM WF_GenerWorkFlow WHERE (Emps LIKE '%" + WebUser.getNo() + "%' OR Starter='" + WebUser.getNo() + "') AND WFState > 1 GROUP BY TSpan";
		}
		else
		{
			sql = "SELECT  TSpan as No, COUNT(WorkID) as Num FROM WF_GenerWorkFlow WHERE FK_Flow='" + this.getFK_Flow() + "' AND (Emps LIKE '%" + WebUser.getNo() + "%' OR Starter='" + WebUser.getNo() + "')  AND WFState > 1 GROUP BY TSpan";
		}

		DataTable dtTSpanNum = DBAccess.RunSQLReturnTable(sql);
		for (DataRow drEnum : dtTSpan.Rows)
		{
			String no = drEnum.getValue("IntKey").toString();
			for (DataRow dr : dtTSpanNum.Rows)
			{
				if (dr.getValue("No").toString().equals(no))
				{
					drEnum.setValue("Lab", drEnum.getValue("Lab").toString() + "(" + dr.getValue("Num") + ")");
					break;
				}
			}
		}

			///


			///2、处理流程类别列表.
		if (tSpan.equals("-1"))
		{
			sql = "SELECT  FK_Flow as No, FlowName as Name, COUNT(WorkID) as Num FROM WF_GenerWorkFlow WHERE (Emps LIKE '%" + WebUser.getNo() + "%' OR TodoEmps LIKE '%" + WebUser.getNo() + ",%' OR Starter='" + WebUser.getNo() + "')  AND WFState > 1 AND FID = 0 GROUP BY FK_Flow, FlowName";
		}
		else
		{
			sql = "SELECT  FK_Flow as No, FlowName as Name, COUNT(WorkID) as Num FROM WF_GenerWorkFlow WHERE TSpan=" + tSpan + " AND (Emps LIKE '%" + WebUser.getNo() + "%' OR TodoEmps LIKE '%" + WebUser.getNo() + ",%' OR Starter='" + WebUser.getNo() + "')  AND WFState > 1 AND FID = 0 GROUP BY FK_Flow, FlowName";
		}

		DataTable dtFlows = DBAccess.RunSQLReturnTable(sql);
		if (SystemConfig.getAppCenterDBType() == DBType.Oracle 
				|| SystemConfig.getAppCenterDBType() == DBType.KingBase
				|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL)
		{
			dtFlows.Columns.get(0).setColumnName("No");
			dtFlows.Columns.get(1).setColumnName("Name");
			dtFlows.Columns.get(2).setColumnName("Num");
		}
		dtFlows.TableName = "Flows";
		ds.Tables.add(dtFlows);

			///


			///3、处理流程实例列表.
		GenerWorkFlows gwfs = new GenerWorkFlows();
		String sqlWhere = "";
		sqlWhere = "(1 = 1)AND (((Emps LIKE '%" + WebUser.getNo() + "%')OR(TodoEmps LIKE '%" + WebUser.getNo() + "%')OR(Starter = '" + WebUser.getNo() + "')) AND (WFState > 1)";
		if (!tSpan.equals("-1"))
		{
			sqlWhere += "AND (TSpan = '" + tSpan + "') ";
		}

		if (this.getFK_Flow() != null)
		{
			sqlWhere += "AND (FK_Flow = '" + this.getFK_Flow() + "')) ";
		}
		else
		{
			sqlWhere += ")";
		}
		sqlWhere += "ORDER BY RDT DESC";

		if (SystemConfig.getAppCenterDBType() == DBType.Oracle
				|| SystemConfig.getAppCenterDBType() == DBType.KingBase)
		{
			sql = "SELECT NVL(WorkID, 0) WorkID,NVL(FID, 0) FID ,FK_Flow,FlowName,Title, NVL(WFSta, 0) WFSta,WFState,  Starter, StarterName,Sender,NVL(RDT, '2018-05-04 19:29') RDT,NVL(FK_Node, 0) FK_Node,NodeName, TodoEmps FROM (select * from WF_GenerWorkFlow where " + sqlWhere + ") where rownum <= 500";
		}
		else if (SystemConfig.getAppCenterDBType() == DBType.MSSQL)
		{
			sql = "SELECT  TOP 500 ISNULL(WorkID, 0) WorkID,ISNULL(FID, 0) FID ,FK_Flow,FlowName,Title, ISNULL(WFSta, 0) WFSta,WFState,  Starter, StarterName,Sender,ISNULL(RDT, '2018-05-04 19:29') RDT,ISNULL(FK_Node, 0) FK_Node,NodeName, TodoEmps FROM WF_GenerWorkFlow where " + sqlWhere;
		}
		else if (SystemConfig.getAppCenterDBType() == DBType.MySQL)
		{
			sql = "SELECT IFNULL(WorkID, 0) WorkID,IFNULL(FID, 0) FID ,FK_Flow,FlowName,Title, IFNULL(WFSta, 0) WFSta,WFState,  Starter, StarterName,Sender,IFNULL(RDT, '2018-05-04 19:29') RDT,IFNULL(FK_Node, 0) FK_Node,NodeName, TodoEmps FROM WF_GenerWorkFlow where " + sqlWhere + " LIMIT 500";
		}
		else if (SystemConfig.getAppCenterDBType() == DBType.PostgreSQL)
		{
			sql = "SELECT COALESCE(WorkID, 0) WorkID,COALESCE(FID, 0) FID ,FK_Flow,FlowName,Title, COALESCE(WFSta, 0) WFSta,WFState,  Starter, StarterName,Sender,COALESCE(RDT, '2018-05-04 19:29') RDT,COALESCE(FK_Node, 0) FK_Node,NodeName, TodoEmps FROM WF_GenerWorkFlow where " + sqlWhere + " LIMIT 500";
		}
		DataTable mydt = DBAccess.RunSQLReturnTable(sql);
		if (SystemConfig.getAppCenterDBType() == DBType.Oracle 
				|| SystemConfig.getAppCenterDBType() == DBType.KingBase
				|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL)
		{
			mydt.Columns.get(0).setColumnName("WorkID");
			mydt.Columns.get(1).setColumnName("FID");
			mydt.Columns.get(2).setColumnName("FK_Flow");
			mydt.Columns.get(3).setColumnName("FlowName");
			mydt.Columns.get(4).setColumnName("Title");
			mydt.Columns.get(5).setColumnName("WFSta");
			mydt.Columns.get(6).setColumnName("WFState");
			mydt.Columns.get(7).setColumnName("Starter");
			mydt.Columns.get(8).setColumnName("StarterName");
			mydt.Columns.get(9).setColumnName("Sender");
			mydt.Columns.get(10).setColumnName("RDT");
			mydt.Columns.get(11).setColumnName("FK_Node");
			mydt.Columns.get(12).setColumnName("NodeName");
			mydt.Columns.get(13).setColumnName("TodoEmps");


		}
		mydt.TableName = "WF_GenerWorkFlow";
		if (mydt != null)
		{
			mydt.Columns.Add("TDTime");
			for (DataRow dr : mydt.Rows)
			{
				dr.setValue("TDTime", GetTraceNewTime(dr.getValue("FK_Flow").toString(), Integer.parseInt(dr.getValue("WorkID").toString()), Integer.parseInt(dr.getValue("FID").toString())));
			}
		}

			///


		ds.Tables.add(mydt);

		return bp.tools.Json.ToJson(ds);
	}
	public static String GetTraceNewTime(String fk_flow, long workid, long fid) throws Exception
	{

			///获取track数据.
		String sqlOfWhere2 = "";
		String sqlOfWhere1 = "";
		String dbStr = SystemConfig.getAppCenterDBVarStr();
		Paras ps = new Paras();
		if (fid == 0)
		{
			sqlOfWhere1 = " WHERE (FID=" + dbStr + "WorkID11 OR WorkID=" + dbStr + "WorkID12 )  ";
			ps.Add("WorkID11", workid);
			ps.Add("WorkID12", workid);
		}
		else
		{
			sqlOfWhere1 = " WHERE (FID=" + dbStr + "FID11 OR WorkID=" + dbStr + "FID12 ) ";
			ps.Add("FID11", fid);
			ps.Add("FID12", fid);
		}

		String sql = "";
		sql = "SELECT MAX(RDT) FROM ND" + Integer.parseInt(fk_flow) + "Track " + sqlOfWhere1;
		 sql = "SELECT RDT FROM  ND" + Integer.parseInt(fk_flow) + "Track  WHERE RDT=(" + sql + ")";
		ps.SQL=sql;

		try
		{
			return DBAccess.RunSQLReturnString(ps);
		}
		catch (java.lang.Exception e)
		{
			// 处理track表.
			Track.CreateOrRepairTrackTable(fk_flow);
			return DBAccess.RunSQLReturnString(ps);
		}

			/// 获取track数据.
	}
	/** 
	 查询
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Search_Search() throws Exception
	{
		String TSpan = this.GetRequestVal("TSpan");
		String FK_Flow = this.GetRequestVal("FK_Flow");

		GenerWorkFlows gwfs = new GenerWorkFlows();
		QueryObject qo = new QueryObject(gwfs);
		qo.AddWhere(GenerWorkFlowAttr.Emps, " LIKE ", "%" + WebUser.getNo() + "%");
		if (!DataType.IsNullOrEmpty(TSpan))
		{
			qo.addAnd();
			qo.AddWhere(GenerWorkFlowAttr.TSpan, this.GetRequestVal("TSpan"));
		}
		if (!DataType.IsNullOrEmpty(FK_Flow))
		{
			qo.addAnd();
			qo.AddWhere(GenerWorkFlowAttr.FK_Flow, this.GetRequestVal("FK_Flow"));
		}
		qo.setTop(50);

		if (SystemConfig.getAppCenterDBType() == DBType.Oracle 
				|| SystemConfig.getAppCenterDBType() == DBType.KingBase
				|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL)
		{
			qo.DoQuery();
			DataTable dt = gwfs.ToDataTableField("Ens");
			return bp.tools.Json.ToJson(dt);
		}
		else
		{
			DataTable dt = qo.DoQueryToTable();
			return bp.tools.Json.ToJson(dt);
		}
	}

	public String weChatLogin() throws Exception{
		System.out.println("开始签名校验");
		//第一步，获取菜单配置url中的code
		String code=this.GetRequestVal("code");
		String state=this.GetRequestVal("state");
		//第二步：通过code获取网页授权access_token
		String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+SystemConfig.getWX_CorpID()+"&corpsecret=" + SystemConfig.getWX_AppSecret();
		String json=HttpClientUtil.doGet(url);
		JSONObject jsonObject = JSONObject.fromObject(json);
		String access_token = jsonObject.getString("access_token");
		if(DataType.IsNullOrEmpty(access_token)){
			System.out.println("-----access_token获取失败-----");
			return "err@access_token获取失败-----";
		}
		//第三步：获取用户信息
		String infoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=" + access_token + "&code=" + code;
		String info=HttpClientUtil.doGet(infoUrl);
		JSONObject userInfo = JSONObject.fromObject(info);
		String errcode=userInfo.getString("errcode");
		if(errcode.equals("40029")){
			return "err@获取用户信息失败-----";
		}
		//人员在企业号中的ID
		String userID=userInfo.getString("UserId");
		//人员设置的手机号
		String mobile="";

		//第四步：获取人员的完整信息
		String userInfoDtil="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=" + access_token + "&userid="+userID;
		String userJson=HttpClientUtil.doGet(userInfoDtil);
		JSONObject userJsonInfo = JSONObject.fromObject(userJson);
		errcode=userJsonInfo.getString("errcode");
		if(errcode.equals("0")){
			mobile=userJsonInfo.getString("mobile");
		}else
			return "err@获取用户信息详情失败-----";

		//第五步：验证人员是否存在本系统中
		Paras ps=new Paras();
		String dbstr = SystemConfig.getAppCenterDBVarStr();
		ps.SQL="SELECT No,Name from Port_Emp where Tel="+dbstr+"Wei_UserID or No="+dbstr+"No ";
		ps.Add("Wei_UserID", mobile);
		ps.Add("No", mobile);
		DataTable dt=DBAccess.RunSQLReturnTable(ps);

		if(dt.Rows.size()<=0){
			return "err@不存在此用户信息，userID:"+userID+"-----";
		}
		else{
			//执行登录
			bp.wf.Dev2Interface.Port_Login(dt.Rows.get(0).getValue("No").toString());
			if(state.equals("Start"))
				return "/CCMobile/Start.htm";
			else if(state.equals("Todolist"))
				return "/CCMobile/Todolist.htm";
			else if(state.equals("Runing"))
				return "/CCMobile/Runing.htm";
			else if(state.equals("Complete"))
				return "/CCMobile/Complete.htm";
			else if(state.equals("Search"))
				return "/CCMobile/Search.htm";
			else
				return "/CCMobilePortal/Home.htm";
		}
	}

		///

}