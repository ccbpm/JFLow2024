package bp.wf.template;

import bp.da.*;
import bp.sys.*;
import bp.en.*;
import bp.wf.port.*;
import bp.wf.*;
import java.util.*;

/** 
 流程轨迹权限s
*/
public class TruckViewPowers extends EntitiesNoName
{
	/** 
	 流程轨迹权限s
	*/
	public TruckViewPowers()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new TruckViewPower();
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<TruckViewPower> ToJavaList()
	{
		return (List<TruckViewPower>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<TruckViewPower> Tolist()
	{
		ArrayList<TruckViewPower> list = new ArrayList<TruckViewPower>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((TruckViewPower)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}