package BP.WF;

import BP.DA.*;
import BP.En.*;
import BP.WF.*;
import BP.Port.*;
import java.util.*;

/** 
 工作人员集合
*/
public class GenerWorkerLists extends Entities
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 方法
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new GenerWorkerList();
	}
	/** 
	 GenerWorkerList
	*/
	public GenerWorkerLists()
	{
	}
	public GenerWorkerLists(long workId)
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(GenerWorkerListAttr.WorkID, workId);
		qo.addOrderBy(GenerWorkerListAttr.RDT);
		qo.DoQuery();
		return;
	}
	/** 
	 
	 
	 @param workId
	 @param nodeId
	*/
	public GenerWorkerLists(long workId, int nodeId)
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(GenerWorkerListAttr.WorkID, workId);
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.FK_Node, nodeId);
		qo.DoQuery();
		return;
	}
	public GenerWorkerLists(long workId, int nodeId, String FK_Emp)
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(GenerWorkerListAttr.WorkID, workId);
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.FK_Node, nodeId);
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.FK_Emp, FK_Emp);
		qo.DoQuery();
		return;
	}
	/** 
	 构造工作人员集合
	 
	 @param workId 工作ID
	 @param nodeId 节点ID
	 @param isWithEmpExts 是否包含为分配的人员
	*/
	public GenerWorkerLists(long workId, int nodeId, boolean isWithEmpExts)
	{
		QueryObject qo = new QueryObject(this);
		qo.addLeftBracket();
		qo.AddWhere(GenerWorkerListAttr.WorkID, workId);
		qo.addOr();
		qo.AddWhere(GenerWorkerListAttr.FID, workId);
		qo.addRightBracket();
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.FK_Node, nodeId);
		int i = qo.DoQuery();

		if (isWithEmpExts == false)
		{
			return;
		}

		if (i == 0)
		{
			throw new RuntimeException("@系统错误，工作人员丢失请与管理员联系。NodeID=" + nodeId + " WorkID=" + workId);
		}

		RememberMe rm = new RememberMe();
		rm.setFK_Emp(BP.Web.WebUser.No);
		rm.setFK_Node(nodeId);
		if (rm.RetrieveFromDBSources() == 0)
		{
			return;
		}

		GenerWorkerList wl = (GenerWorkerList)this[0];
		String[] emps = rm.getEmps().split("[@]", -1);
		for (String emp : emps)
		{
			if (emp == null || emp.equals(""))
			{
				continue;
			}

			if (this.GetCountByKey(GenerWorkerListAttr.FK_Emp, emp) >= 1)
			{
				continue;
			}

			GenerWorkerList mywl = new GenerWorkerList();
			mywl.Copy(wl);
			mywl.setIsEnable(false);
			mywl.setFK_Emp(emp);
			WF.Port.WFEmp myEmp = new BP.WF.Port.WFEmp(emp);
			mywl.setFK_EmpText(myEmp.Name);
			try
			{
				mywl.Insert();
			}
			catch (java.lang.Exception e)
			{
				mywl.Update();
				continue;
			}
			this.AddEntity(mywl);
		}
		return;
	}
	/** 
	 工作者
	 
	 @param workId 工作者ID
	 @param flowNo 流程编号
	*/
	public GenerWorkerLists(long workId, String flowNo)
	{
		if (workId == 0)
		{
			return;
		}

		Flow fl = new Flow(flowNo);
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(GenerWorkerListAttr.WorkID, workId);
		qo.addAnd();
		qo.AddWhere(GenerWorkerListAttr.FK_Flow, flowNo);
		qo.DoQuery();
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<GenerWorkerList> ToJavaList()
	{
		return (List<GenerWorkerList>)this;
	}
	/** 
	 转化成list 为了翻译成java的需要
	 
	 @return List
	*/
	public final ArrayList<BP.WF.GenerWorkerList> Tolist()
	{
		ArrayList<BP.WF.GenerWorkerList> list = new ArrayList<BP.WF.GenerWorkerList>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((BP.WF.GenerWorkerList)this[i]);
		}
		return list;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}