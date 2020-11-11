package bp.wf.port.admin2;

import bp.da.*;
import bp.en.*;
import bp.web.*;
import bp.wf.*;
import bp.wf.port.*;
import java.util.*;

/** 
独立组织集合
*/
public class Orgs extends EntitiesNoName
{
	/** 
	 得到一个新实体
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new Org();
	}
	/** 
	 create ens
	*/
	public Orgs()
	{
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<Org> ToJavaList()
	{
		return (List<Org>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<Org> Tolist()
	{
		ArrayList<Org> list = new ArrayList<Org>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((Org)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}