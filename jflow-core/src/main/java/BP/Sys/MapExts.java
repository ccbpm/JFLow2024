package BP.Sys;

import java.util.ArrayList;

import BP.En.*;

/** 
 扩展s
 
*/
public class MapExts extends Entities
{

		
	/** 
	 扩展s
	 
	*/
	public MapExts()
	{
	}
	/** 
	 扩展s
	 
	 @param fk_mapdata s
	 * @throws Exception 
	*/
	public MapExts(String fk_mapdata) throws Exception
	{
		this.Retrieve(MapExtAttr.FK_MapData, fk_mapdata, MapExtAttr.PRI);
	}
	/** 
	 得到它的 Entity
	 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new MapExt();
	}
	public static ArrayList<MapExt> convertMapExts(Object obj)
	{
		return (ArrayList<MapExt>) obj;
	}
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final java.util.List<MapExt> ToJavaList()
	{
		return (java.util.List<MapExt>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final java.util.ArrayList<MapExt> Tolist()
	{
		java.util.ArrayList<MapExt> list = new java.util.ArrayList<MapExt>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((MapExt)this.get(i));
		}
		return list;
	}

		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}