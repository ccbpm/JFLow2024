package bp.sys.frmui;

import bp.da.*;
import bp.en.*;
import bp.pub.*;
import bp.*;
import bp.sys.*;
import java.util.*;

/** 
 超连接s
*/
public class FrmLinks extends EntitiesMyPK
{

		///构造
	/** 
	 超连接s
	*/
	public FrmLinks()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new FrmLink();
	}

		///


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final java.util.List<FrmLink> ToJavaList()
	{
		return (java.util.List<FrmLink>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<FrmLink> Tolist()
	{
		ArrayList<FrmLink> list = new ArrayList<FrmLink>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((FrmLink)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}