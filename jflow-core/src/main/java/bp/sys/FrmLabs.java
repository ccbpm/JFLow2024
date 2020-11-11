package bp.sys;

import bp.da.*;
import bp.en.*;
import bp.pub.*;
import bp.*;
import java.util.*;

/** 
 标签s
*/
public class FrmLabs extends EntitiesMyPK
{

		///构造
	/** 
	 标签s
	*/
	public FrmLabs()
	{
	}
	/** 
	 标签s
	 
	 @param fk_mapdata s
	*/
	public FrmLabs(String fk_mapdata) throws Exception
	{
	   this.Retrieve(FrmLineAttr.FK_MapData, fk_mapdata);
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new FrmLab();
	}

		///


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final java.util.List<FrmLab> ToJavaList()
	{
		return (java.util.List<FrmLab>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<FrmLab> Tolist()
	{
		ArrayList<FrmLab> list = new ArrayList<FrmLab>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((FrmLab)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}