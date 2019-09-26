package BP.Sys;

import BP.En.*;
import java.util.*;

/** 
 excel表单实体s
*/
public class GEEntityExcelFrms extends EntitiesOID
{

		///#region 重载基类方法
	@Override
	public String toString()
	{
		return this.FK_MapData;
	}
	/** 
	 主键
	*/
	public String FK_MapData = null;

		///#endregion


		///#region 方法
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getNewEntity()
	{
			//if (this.FK_MapData == null)
			//    throw new Exception("@没有能 FK_MapData 给值。");

		if (this.FK_MapData == null)
		{
			return new GEEntity();
		}
		return new GEEntity(this.FK_MapData);
	}
	/** 
	 通用实体ID
	*/
	public GEEntityExcelFrms()
	{
	}
	/** 
	 通用实体ID
	 
	 @param fk_mapdtl
	*/
	public GEEntityExcelFrms(String fk_mapdata)
	{
		this.FK_MapData = fk_mapdata;
	}

		///#endregion


		///#region 为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<GEEntityExcelFrm> ToJavaList()
	{
		return (List<GEEntityExcelFrm>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<GEEntityExcelFrm> Tolist()
	{
		ArrayList<GEEntityExcelFrm> list = new ArrayList<GEEntityExcelFrm>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((GEEntityExcelFrm)this.get(i));
		}
		return list;
	}

		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}