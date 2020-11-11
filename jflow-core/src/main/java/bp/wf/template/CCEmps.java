package bp.wf.template;

import bp.da.*;
import bp.en.*;
import bp.wf.port.*;
import bp.wf.*;
import java.util.*;

/** 
 节点到人员
*/
public class CCEmps extends EntitiesMM
{
	/** 
	 他的到人员
	 * @throws Exception 
	*/
	public final Emps getHisEmps() throws Exception
	{
		Emps ens = new Emps();
		for (CCEmp ns : this.ToJavaList())
		{
			ens.AddEntity(new Emp(ns.getFK_Emp()));
		}
		return ens;
	}
	/** 
	 他的工作节点
	 * @throws Exception 
	*/
	public final Nodes getHisNodes() throws Exception
	{
		Nodes ens = new Nodes();
		for (CCEmp ns : this.ToJavaList())
		{
			ens.AddEntity(new Node(ns.getFK_Node()));
		}
		return ens;

	}
	/** 
	 节点到人员
	*/
	public CCEmps()
	{
	}
	/** 
	 节点到人员
	 
	 @param NodeID 节点ID
	 * @throws Exception 
	*/
	public CCEmps(int NodeID) throws Exception
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(CCEmpAttr.FK_Node, NodeID);
		qo.DoQuery();
	}
	/** 
	 节点到人员
	 
	 @param EmpNo EmpNo 
	 * @throws Exception 
	*/
	public CCEmps(String EmpNo) throws Exception
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(CCEmpAttr.FK_Emp, EmpNo);
		qo.DoQuery();
	}
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new CCEmp();
	}
	/** 
	 取到一个到人员集合能够访问到的节点s
	 
	 @param sts 到人员集合
	 @return 
	 * @throws Exception 
	*/
	public final Nodes GetHisNodes(Emps sts) throws Exception
	{
		Nodes nds = new Nodes();
		Nodes tmp = new Nodes();
		for (Emp st : sts.ToJavaList())
		{
			tmp = this.GetHisNodes(st.getNo());
			for (Node nd : tmp.ToJavaList())
			{
				if (nds.Contains(nd))
				{
					continue;
				}
				nds.AddEntity(nd);
			}
		}
		return nds;
	}
	/** 
	 到人员对应的节点
	 
	 @param EmpNo 到人员编号
	 @return 节点s
	 * @throws Exception 
	*/
	public final Nodes GetHisNodes(String EmpNo) throws Exception
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(CCEmpAttr.FK_Emp, EmpNo);
		qo.DoQuery();

		Nodes ens = new Nodes();
		for (CCEmp en : this.ToJavaList())
		{
			ens.AddEntity(new Node(en.getFK_Node()));
		}
		return ens;
	}
	/** 
	 转向此节点的集合的 Nodes
	 
	 @param nodeID 此节点的ID
	 @return 转向此节点的集合的Nodes (FromNodes) 
	 * @throws Exception 
	*/
	public final Emps GetHisEmps(int nodeID) throws Exception
	{
		QueryObject qo = new QueryObject(this);
		qo.AddWhere(CCEmpAttr.FK_Node, nodeID);
		qo.DoQuery();

		Emps ens = new Emps();
		for (CCEmp en : this.ToJavaList())
		{
			ens.AddEntity(new Emp(en.getFK_Emp()));
		}
		return ens;
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<CCEmp> ToJavaList()
	{
		return (List<CCEmp>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<CCEmp> Tolist()
	{
		ArrayList<CCEmp> list = new ArrayList<CCEmp>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((CCEmp)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.

}