package WebServiceImp;

import java.util.Hashtable;

import javax.jws.WebService;

import BP.DA.DataRow;
import BP.DA.DataSet;
import BP.DA.DataTable;
import BP.DA.DataType;
import BP.En.Entity;
import BP.GPM.Dev2Interface;
import BP.WF.ActionType;
import BP.WF.Flow;
import BP.WF.GenerWorkFlow;
import BP.WF.GenerWorkerList;
import BP.WF.GenerWorkerLists;
import BP.WF.Node;
import BP.WF.Nodes;
import BP.WF.WFState;
import BP.WF.WorkFlow;
import BP.WF.Data.GERpt;

import BP.WF.Template.CondModel;
import BP.WF.Template.Directions;
import BP.WF.Template.FlowExt;
import BP.WF.Template.Selector;
import BP.WF.XML.Tools;
import WebService.LocalWSI;

public class LocalWS implements LocalWSI{
	
	/**
	 * 待办
	 * @param userNo 用户编号
	 * @param sysNo 系统编号,为空时返回平台所有数据
	 * @return
	 */

	@Override
	public String DB_Todolist(String userNo, String sysNo) {
		try {
			BP.WF.Dev2Interface.Port_Login(userNo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 String sql = "";
         if (DataType.IsNullOrEmpty(sysNo) == true)
             sql = "SELECT * FROM WF_EmpWorks WHERE FK_Emp='" + userNo + "'";
         else
             sql = "SELECT * FROM WF_EmpWorks WHERE Domain='" + sysNo + "' AND FK_Emp='" + userNo + "'";

         DataTable dt = BP.DA.DBAccess.RunSQLReturnTable(sql);
         return BP.Tools.Json.ToJson(dt);
	}
	
	/**
	 * 获得在途
	 * @param userNo 用户编号
	 * @param sysNo 系统编号,为空时返回平台所有数据
	 * @return
	 * @throws Exception 
	 */
	@Override
	public String DB_Runing(String userNo, String sysNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		 DataTable dt = BP.WF.Dev2Interface.DB_GenerRuning(userNo, null, false);
         return BP.Tools.Json.ToJson(dt);
	}
	
	/**
	 * 我可以发起的流程
	 * @param userNo 用户编号
	 * @param sysNo  系统编号,为空时返回平台所有数据
	 * @return 返回我可以发起的流程列表.
	 * @throws Exception 
	 */

	@Override
	public String DB_StarFlows(String userNo, String sysNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		DataTable dt= BP.WF.Dev2Interface.DB_StarFlows(userNo);
        return BP.Tools.Json.ToJson(dt);
	}
	
	/**
	 * 我发起的流程实例
	 * @param userNo 用户编号
	 * @param sysNo 统编号,为空时返回平台所有数据
	 * @param pageSize
	 * @param pageIdx
	 * @return
	 */
	@Override
	public String DB_MyStartFlowInstance(String userNo, String sysNo, int pageSize, int pageIdx) {
		try {
			BP.WF.Dev2Interface.Port_Login(userNo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sql = "";
        if (sysNo == null)
            sql = "SELECT * FROM WF_GenerWorkFlow WHERE Starter='" + userNo + "'";
        else
            sql = "SELECT * FROM WF_GenerWorkFlow WHERE Domain='" + sysNo + "' AND Starter='" + userNo + "'";

        DataTable dt = BP.DA.DBAccess.RunSQLReturnTable(sql);
        return BP.Tools.Json.ToJson(dt);
	}

/**
	 * 创建WorkID
	 * @param flowNo 流程编号
	 * @param userNo 工作人员编号
	 * @return 一个长整型的工作流程实例
	 * @throws Exception 
	 */

	@Override
	public long CreateWorkID(String flowNo, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		 return BP.WF.Dev2Interface.Node_CreateBlankWork(flowNo, userNo);
	}

	/**
	 * 执行发送
	 * @param flowNo 流的程模版ID
	 * @param workid 工作ID
	 * @param ht 参数，或者表单字段.
	 * @param toNodeID 到达的节点ID.如果让系统自动计算就传入0
	 * @param toEmps 到达的人员IDs,比如:zhangsan,lisi,wangwu. 如果为Null就标识让系统自动计算
	 * @return 发送的结果信息.
	 * @throws Exception 
	 */

	@Override
	public String SendWork(String flowNo, long workid, Hashtable ht, int toNodeID, String toEmps, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		BP.WF.SendReturnObjs objs = BP.WF.Dev2Interface.Node_SendWork(flowNo, workid, ht, toNodeID, toEmps);

        String msg = objs.ToMsgOfText();
        

        Hashtable myht = new Hashtable();
        myht.put("Message", msg);
        myht.put("IsStopFlow", objs.getIsStopFlow());

        if (objs.getIsStopFlow()==false)
        {
        	myht.put("VarAcceptersID", objs.getVarAcceptersID()==null?"":objs.getVarAcceptersID());
	        myht.put("VarAcceptersName", objs.getVarAcceptersName() == null ?"":objs.getVarAcceptersName());
	        myht.put("VarToNodeID", objs.getVarToNodeID());
	        myht.put("VarToNodeName", objs.getVarToNodeName()==null?"":objs.getVarToNodeName());
        }
        return BP.Tools.Json.ToJson(myht);
	}

	/**
	 * 保存参数
	 * @param workid 工作ID
	 * @param paras 用于控制流程运转的参数，比如方向条件. 格式为:@JinE=1000@QingJaiTianShu=100
	 * @throws Exception 
	 */

	@Override
	public void SaveParas(long workid, String paras, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		BP.WF.Dev2Interface.Flow_SaveParas(workid, paras);
		
	}

	/**
	 * 获得下一个节点信息
	 * @param flowNo 流程编号
	 * @param workid 流程实例
	 * @param paras 方向条件所需要的参数，可以为空。
	 * @return 下一个节点的JSON.
	 * @throws Exception 
	 */

	@Override
	public String GenerNextStepNode(String flowNo, long workid, String paras, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		if (paras != null)
            BP.WF.Dev2Interface.Flow_SaveParas(workid, paras);

        int nodeID = BP.WF.Dev2Interface.Node_GetNextStepNode(flowNo, workid);
        BP.WF.Node nd = new BP.WF.Node(nodeID);

       //如果字段 DeliveryWay = 4 就表示到达的接点是由当前节点发送人选择接收人.
		//自定义参数的字段是 SelfParas, DeliveryWay 
		// CondModel = 方向条件计算规则.
        return nd.ToJson();
	}

	/**
	 * 获得下一步节点的接收人
	 * @param flowNo 流程ID
	 * @param toNodeID 节点ID
	 * @param workid 工作事例ID
	 * @return 返回两个结果集一个是分组的Depts(No,Name)，另外一个是人员的Emps(No, Name, FK_Dept),接受后，用于构造人员选择器.
	 * @throws Exception 
	 */

	@Override
	public String GenerNextStepNodeEmps(String flowNo, int toNodeID, int workid, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		Selector select = new Selector(toNodeID);
        Node nd = new Node(toNodeID);

        GERpt rpt = new GERpt("ND" + Integer.parseInt(flowNo) + "Rpt", workid);
        DataSet ds = select.GenerDataSet(toNodeID, rpt);
        return BP.Tools.Json.ToJson(ds);
	}
	
	/**
	 * 将要退回到的节点
	 * @param workID
	 * @return 返回节点集合的json.
	 * @throws Exception 
	 */

	@Override
	public String WillReturnToNodes(int workID, String userNo) throws Exception {
		
		try
		{
			
		BP.WF.Dev2Interface.Port_Login(userNo);
		
		GenerWorkFlow gwf=new GenerWorkFlow(workID);
		
		DataTable dt=BP.WF.Dev2Interface.DB_GenerWillReturnNodes(gwf.getFK_Node(), workID, gwf.getFID()); 
        return BP.Tools.Json.ToJson(dt);
		}catch(Exception ex)
		{
		  return "err@"+ex.getMessage();
		}
	}

	/**
	 * 将要达到的节点
	 * @param currNodeID 当前节点ID
	 * @return 返回节点集合的json.
	 * @throws Exception 
	 */

	@Override
	public String WillToNodes(int currNodeID, String userNo) throws Exception {
		
		try
		{
		BP.WF.Dev2Interface.Port_Login(userNo);
		Node nd = new Node(currNodeID);
		
        Directions dirs = new Directions();
        Nodes nds = dirs.GetHisToNodes(currNodeID, false);
        return nds.ToJson();
		}catch(Exception ex)
		{
		  return "err@"+ex.getMessage();
		}
	}

	/**
	 * 获得当前节点信息.
	 * @param currNodeID  当前节点ID
	 * @return
	 * @throws Exception 
	 */

	@Override
	public String CurrNodeInfo(int currNodeID, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		Node nd = new Node(currNodeID);
         return nd.ToJson();
	}

	/**
	 * 获得当前流程信息.
	 * @param flowNo 流程ID
	 * @return 当前节点信息
	 * @throws Exception 
	 */

	@Override
	public String CurrFlowInfo(String flowNo, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		Flow fl = new Flow(flowNo);
          return fl.ToJson();
	}

	/**
	 * 获得当前流程信息.
	 * @param workID 流程ID
	 * @return 当前节点信息
	 * @throws Exception 
	 */
	@Override
	public String CurrGenerWorkFlowInfo(long workID, String userNo) throws Exception {
		BP.WF.Dev2Interface.Port_Login(userNo);
		GenerWorkFlow gwf = new GenerWorkFlow(workID);
         return gwf.ToJson();
	}

	
	/**
	 * 退回.
	 * @param workID 流程ID
	 * @param retunrnToNodeID 流程退回的节点ID
	 * @param returnMsg 退回原因
	 * @return 退回结果信息
	 * @throws Exception 
	 */
  @Override
   public String Node_ReturnWork(long workID, int returnToNodeID, String returnMsg, String userNo) throws Exception {
	  BP.WF.Dev2Interface.Port_Login(userNo);
	  GenerWorkFlow gwf=new GenerWorkFlow(workID);
      return BP.WF.Dev2Interface.Node_ReturnWork(gwf.getFK_Flow(), workID, gwf.getFID(), gwf.getFK_Node(), returnToNodeID,null, returnMsg,false);
	  
	
   }
  
/**
	 * 执行流程结束 说明:强制流程结束.
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

  @Override
	public  String Flow_DoFlowOverQiangZhi(String flowNo, long workID, String msg, String userNo) throws Exception {
	  BP.WF.Dev2Interface.Port_Login(userNo);
		return BP.WF.Dev2Interface.Flow_DoFlowOver(flowNo, workID, msg);
		
  }

	@Override
	public void Port_Login(String userNo) throws Exception{
		
		BP.WF.Dev2Interface.Port_Login(userNo);
	}
	
	/**
	 * 执行撤销
	 * @param flowNo 流程编码
	 * @param workID 工作ID
	 * @param unSendToNode 撤销到的节点
	 * @param fid 
	 * @return
	 * @throws Exception
	 */
	public String Runing_UnSend(String userNo,String flowNo, long workID, int unSendToNode,long fid) throws Exception{
		
		BP.WF.Dev2Interface.Port_Login(userNo);
		
		return BP.WF.Dev2Interface.Flow_DoUnSend(flowNo, workID,unSendToNode,fid);
	}
	
	/**
	 * 流程结束后回滚
	 * @param flowNo 流程编码
	 * @param workId 工作ID
	 * @param backToNodeID 回滚到的节点ID
	 * @param backMsg 回滚原因
	 * @return 回滚信息
	 * @throws Exception 
	 */
	public String DoRebackFlowData(String flowNo,long workId,int backToNodeID,String backMsg, String userNo) throws Exception{
		BP.WF.Dev2Interface.Port_Login(userNo);
		FlowExt flow = new FlowExt(flowNo);
		return flow.DoRebackFlowData(workId, backToNodeID, backMsg);
	}
	/** 
	 获得当前流程信息.
	 
	 @param flowNo 流程ID.
	 @return 当前节点信息
	*/
    public String CurrFlowInfo(String flowNo) throws Exception
    {
        Flow fl = new Flow(flowNo);
        return fl.ToJson();
    }
    /** 
	 获得当前流程信息.
	 
	 @param flowNo 流程ID.
	 @return 当前节点信息
	*/
    public String CurrGenerWorkFlowInfo(long workID) throws Exception
    {
        GenerWorkFlow gwf = new GenerWorkFlow(workID);
        return gwf.ToJson();
    }
    
	/** 
	 获得工作进度-用于展示流程的进度图
	 
	 @param workID workID
	 @param userNo 用户编号
	 @return 返回待办
	*/
    public String WorkProgressBar(long  workID, String userNo) throws Exception
    {
    	String sql = "";
		DataSet ds = new DataSet();

		//流程控制主表, 可以得到流程状态，停留节点，当前的执行人.
		GenerWorkFlow gwf = new GenerWorkFlow(workID);
		
		DataTable dt1 = gwf.ToDataTableField("WF_GenerWorkFlow");
		dt1.TableName = "WF_GenerWorkFlow";
		ds.Tables.add(dt1);

		//节点信息.
		Nodes nds = new Nodes(gwf.getFK_Flow());
		DataTable dt2 = nds.ToDataTableField("WF_Node");
		ds.Tables.add(dt2);

		//方向。
		Directions dirs = new Directions(gwf.getFK_Flow());
		ds.Tables.add(dirs.ToDataTableField("WF_Direction"));


		DataTable dtHistory = new DataTable();
		dtHistory.TableName = "Track";
		dtHistory.Columns.Add("FK_Node");
		dtHistory.Columns.Add("NodeName");
		dtHistory.Columns.Add("EmpNo");
		dtHistory.Columns.Add("EmpName");
		dtHistory.Columns.Add("RDT"); //记录日期.
		dtHistory.Columns.Add("SDT"); //应完成日期.

		//执行人.
		if (gwf.getWFState() == WFState.Complete)
		{
			//历史执行人. 
			sql = "SELECT * FROM ND" + Integer.parseInt(gwf.getFK_Flow()) + "Track WHERE WorkID=" + workID + " AND (ActionType=1 OR ActionType=0)  ORDER BY RDT DESC";
			DataTable dtTrack = BP.DA.DBAccess.RunSQLReturnTable(sql);

			for (DataRow drTrack : dtTrack.Rows)
			{
				DataRow dr = dtHistory.NewRow();
				dr.setValue("FK_Node", drTrack.getValue("NDFrom"));
			   // dr["ActionType"] = drTrack["NDFrom"];
				dr.setValue("NodeName", drTrack.getValue("NDFromT"));
				dr.setValue("EmpNo", drTrack.getValue("EmpFrom"));
				dr.setValue("EmpName", drTrack.getValue("EmpFromT"));
				dr.setValue("RDT", drTrack.getValue("RDT"));
				dr.setValue("SDT", drTrack.getValue(""));
				dtHistory.Rows.add(dr);
			}
		}
		else
		{
			GenerWorkerLists gwls = new GenerWorkerLists(workID);
			for (GenerWorkerList gwl : gwls.ToJavaList())
			{
				DataRow dr = dtHistory.NewRow();
				    dr.setValue("FK_Node", gwl.getFK_Node());
					dr.setValue("NodeName",gwl.getFK_NodeText());
					dr.setValue("EmpNo",gwl.getFK_Emp());
					dr.setValue("EmpName",gwl.getFK_EmpText());
					dr.setValue("RDT",gwl.getRDT());
					dr.setValue("SDT",gwl.getSDT());
				
			}
		}

		ds.Tables.add(dtHistory);

		return BP.Tools.Json.ToJson(ds);

    }

    /** 
	 查询数据	 
	 @param sqlOfSelect 要查询的sql
	 @param password 用户密码
	 @return 返回查询数据
	*/
    public String DB_RunSQLReturnJSON(String sqlOfSelect, String password)
    {
        if ( password.equals(password) == false)
            return "err@密码错误";

        DataTable dt = BP.DA.DBAccess.RunSQLReturnTable(sqlOfSelect);
        return BP.Tools.Json.ToJson(dt);
    }
	
}
