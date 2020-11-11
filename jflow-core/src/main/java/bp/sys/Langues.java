package bp.sys;
import java.util.ArrayList;

import bp.en.EntitiesMyPK;
import bp.en.Entity;

/** 
 语言 
*/
public class Langues extends EntitiesMyPK
{
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new Langue();
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final java.util.List<Langue> ToJavaList()
	{
		return (java.util.List<Langue>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<Langue> Tolist()
	{
		ArrayList<Langue> list = new ArrayList<Langue>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((Langue)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}