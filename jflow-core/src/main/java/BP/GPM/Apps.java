package BP.GPM;

import BP.Sys.*;
import BP.DA.*;
import BP.En.*;
import java.util.*;

/** 
 系统s
*/
public class Apps extends EntitiesNoName
{

		///#region 构造
	/** 
	 系统s
	*/
	public Apps()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getNewEntity()
	{
		return new App();
	}

		///#endregion


		///#region 为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<App> ToJavaList()
	{
		return (List<App>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<App> Tolist()
	{
		ArrayList<App> list = new ArrayList<App>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((App)this.get(i));
		}
		return list;
	}

		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}