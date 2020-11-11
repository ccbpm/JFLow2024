package bp.wf.template;

import bp.da.*;
import bp.en.*;
import bp.wf.port.*;
import bp.wf.*;
import bp.sys.*;
import bp.wf.*;
import java.util.*;

/** 
 表单字段方案s
*/
public class FrmFields extends EntitiesMyPK
{
	public FrmFields()
	{
	}
	/** 
	 查询
	 * @throws Exception 
	*/
	public FrmFields(String fk_mapdata, int nodeID) throws Exception
	{
		this.Retrieve(FrmFieldAttr.FK_MapData, fk_mapdata, FrmFieldAttr.FK_Node, nodeID,FrmFieldAttr.EleType, FrmEleType.Field);
	}
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new FrmField();
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<FrmField> ToJavaList()
	{
		return (List<FrmField>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<FrmField> Tolist()
	{
		ArrayList<FrmField> list = new ArrayList<FrmField>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((FrmField)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}