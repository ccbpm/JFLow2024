package bp.sys;

import bp.da.*;
import bp.en.*;
import bp.web.*;
import bp.*;
import java.util.*;

/** 
 用户自定义表s
*/
public class SFTables extends EntitiesNoName
{

		///构造
	/** 
	 用户自定义表s
	*/
	public SFTables()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new SFTable();
	}

		///


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final java.util.List<SFTable> ToJavaList()
	{
		return (java.util.List<SFTable>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<SFTable> Tolist()
	{
		ArrayList<SFTable> list = new ArrayList<SFTable>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((SFTable)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}