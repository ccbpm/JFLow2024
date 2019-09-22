package BP.WF.Template;

import BP.DA.*;
import BP.En.*;
import BP.Port.*;
import BP.WF.*;
import java.util.*;

/** 
 独立表单树
*/
public class FlowFormTrees extends EntitiesTree
{
	/** 
	 独立表单树s
	*/
	public FlowFormTrees()
	{
	}
	/** 
	 独立表单树
	*/
	public FlowFormTrees(String flowNo)
	{
	   int i = this.Retrieve(FlowFormTreeAttr.FK_Flow, flowNo);
	   if (i == 0)
	   {
		   FlowFormTree tree = new FlowFormTree();
		   tree.No = "100";
		   tree.setFK_Flow(flowNo);
		   tree.Name = "根目录";
		  // tree.IsDir = false;
		   tree.ParentNo = "0";
		   tree.Insert();

		   //创建一个节点.
		   tree.DoCreateSubNode();
	   }
	}
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new FlowFormTree();
	}


//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<FlowFormTree> ToJavaList()
	{
		return (List<FlowFormTree>)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<FlowFormTree> Tolist()
	{
		ArrayList<FlowFormTree> list = new ArrayList<FlowFormTree>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((FlowFormTree)this[i]);
		}
		return list;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}