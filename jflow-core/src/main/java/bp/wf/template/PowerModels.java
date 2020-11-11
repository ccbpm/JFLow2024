package bp.wf.template;

import bp.da.*;
import bp.web.*;
import bp.en.*;
import bp.port.*;
import bp.sys.*;
import bp.wf.*;
import java.util.*;

/** 
 权限模型
*/
public class PowerModels extends EntitiesMyPK
{
	/** 
	 权限模型
	*/
	public PowerModels()
	{
	}
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new PowerModel();
	}

		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<PowerModel> ToJavaList()
	{
		return (List<PowerModel>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<PowerModel> Tolist()
	{
		ArrayList<PowerModel> list = new ArrayList<PowerModel>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((PowerModel)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}