package bp.wf.template;

import bp.da.*;
import bp.sys.*;
import bp.en.*;
import bp.en.Map;
import bp.port.*;
import bp.wf.data.*;
import bp.wf.template.*;
import bp.wf.port.*;
import bp.wf.*;
import java.util.*;

/** 
 这里存放每个节点的信息.	 
*/
public class NodeSimple extends Entity
{

		///节点属性.
	/** 
	 节点编号
	 * @throws Exception 
	*/
	public final int getNodeID()  throws Exception
	{
		return this.GetValIntByKey(NodeAttr.NodeID);
	}
	public final void setNodeID(int value) throws Exception
	{
		this.SetValByKey(NodeAttr.NodeID, value);
	}
	public final DeliveryWay getHisDeliveryWay()throws Exception
	{
		return DeliveryWay.forValue(this.GetValIntByKey(NodeAttr.DeliveryWay));
	}
	public final String getDeliveryParas()throws Exception
	{
		return this.GetValStringByKey(NodeAttr.DeliveryParas);
	}
	public final String getName()throws Exception
	{
		return this.GetValStringByKey(NodeAttr.Name);
	}
	public final void setName(String value) throws Exception
	{
		this.SetValByKey(NodeAttr.Name, value);
	}
	public final float getX()throws Exception
	{
		return this.GetValFloatByKey(NodeAttr.X);
	}
	public final void setX(float value) throws Exception
	{
		this.SetValByKey(NodeAttr.X, value);
	}
	/** 
	 y
	 * @throws Exception 
	*/
	public final float getY() throws Exception
	{
		return this.GetValFloatByKey(NodeAttr.Y);
	}
	public final void setY(float value) throws Exception
	{
		this.SetValByKey(NodeAttr.Y, value);
	}

		/// 节点属性.


		///构造函数
	/** 
	 节点
	*/
	public NodeSimple()
	{
	}
	/** 
	 节点
	 
	 @param _oid 节点ID	
	*/
	public NodeSimple(int _oid)
	{

	}
	/** 
	 重写基类方法
	*/
	@Override
	public Map getEnMap() throws Exception
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}

		Map map = new Map("WF_Node", "节点");

		map.setDepositaryOfEntity( Depositary.Application);
		map.setDepositaryOfMap(Depositary.Application);


			///基本属性.
		map.AddTBIntPK(NodeAttr.NodeID, 0, "节点ID", true, true);
		map.AddTBString(NodeAttr.Name, null, "名称", true, false, 0, 150, 10);
		map.AddTBString(NodeAttr.FK_Flow, null, "流程编号", true, false, 0, 5, 10);
		map.AddTBInt(NodeAttr.RunModel, 0, "运行模式", true, true);

		map.AddTBInt(NodeAttr.DeliveryWay, 0, "运行模式", true, true);
		map.AddTBString(NodeAttr.DeliveryParas, null, "参数", true, false, 0, 300, 10);

		map.AddTBInt(NodeAttr.Step, 0, "步骤", true, true);

			/// 基本属性.

		map.AddTBInt(NodeAttr.X, 0, "X坐标", false, false);
		map.AddTBInt(NodeAttr.Y, 0, "Y坐标", false, false);

		map.AddTBAtParas(500);

		this.set_enMap(map);
		return this.get_enMap();
	}

		///
}