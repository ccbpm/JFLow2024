package bp.en;

import bp.da.*;
import bp.*;
import java.util.*;

/** 
 树结构实体s
*/
public class GETrees extends EntitiesNoName
{
	private static final long serialVersionUID = 1L;
	/** 
	 物理表
	*/
	public String SFTable = null;
	public String Desc = null;

	/** 
	 GETrees
	*/
	public GETrees()
	{
	}
	public GETrees(String sftable, String tableDesc)
	{
		this.SFTable = sftable;
		this.Desc = tableDesc;
	}
	@Override
	public Entity getGetNewEntity()
	{
		return new GETree(this.SFTable, this.Desc);
	}
	@Override
	public int RetrieveAll() throws Exception
	{
		return this.RetrieveAllFromDBSource();
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final java.util.List<GETree> ToJavaList()
	{
		return (java.util.List<GETree>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<GETree> Tolist()
	{
		ArrayList<GETree> list = new ArrayList<GETree>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((GETree)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}