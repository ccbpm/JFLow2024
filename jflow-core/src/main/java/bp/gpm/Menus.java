package bp.gpm;

import bp.da.*;
import bp.en.*;
import java.util.*;

/** 
 菜单s
*/
public class Menus extends EntitiesTree
{

		///构造
	/** 
	 菜单s
	*/
	public Menus()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new Menu();
	}

		///


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<Menu> ToJavaList()
	{
		return (List<Menu>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<Menu> Tolist()
	{
		ArrayList<Menu> list = new ArrayList<Menu>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((Menu)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}