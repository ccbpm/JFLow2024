package bp.wf.data;

import bp.da.*;
import bp.wf.*;
import bp.port.*;
import bp.sys.*;
import bp.en.*;
import bp.wf.template.*;
import bp.wf.*;
import java.util.*;

/** 
 流程实例s
*/
public class GenerWorkFlowViews extends Entities
{

		///方法
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new GenerWorkFlowView();
	}
	/** 
	 流程实例集合
	*/
	public GenerWorkFlowViews()
	{
	}

		///


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<GenerWorkFlowView> ToJavaList()
	{
		return (List<GenerWorkFlowView>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<GenerWorkFlowView> Tolist()
	{
		ArrayList<GenerWorkFlowView> list = new ArrayList<GenerWorkFlowView>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((GenerWorkFlowView)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}