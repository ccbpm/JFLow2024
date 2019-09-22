package BP.Sys;

import BP.DA.*;
import BP.Sys.*;
import BP.En.*;
import java.util.*;

/** 
 ToolbarExcel表单.
*/
public class ToolbarExcelSlns extends EntitiesMyPK
{
	/** 
	 功能控制
	*/
	public ToolbarExcelSlns()
	{
	}
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new ToolbarExcelSln();
	}

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 为了适应自动翻译成java的需要,把实体转换成IList, c#代码调用会出错误。

	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<ToolbarExcelSln> ToJavaList()
	{
		return (List<ToolbarExcelSln>)this;
	}

	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<ToolbarExcelSln> Tolist()
	{
		ArrayList<ToolbarExcelSln> list = new ArrayList<ToolbarExcelSln>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((ToolbarExcelSln)this[i]);
		}
		return list;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}