package BP.GPM;

import BP.DA.*;
import BP.En.*;
import java.util.*;

/** 
 系统类别s
*/
public class AppSorts extends EntitiesNoName
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 构造
	/** 
	 系统类别s
	*/
	public AppSorts()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new AppSort();
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<AppSort> ToJavaList()
	{
		return (List<AppSort>)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<AppSort> Tolist()
	{
		ArrayList<AppSort> list = new ArrayList<AppSort>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((AppSort)this[i]);
		}
		return list;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}