package bp.wf.template;

import bp.da.*;
import bp.en.*;
import bp.port.*;
import bp.sys.*;
import bp.wf.*;
import java.util.*;

/** 
 独立表单树
*/
public class SysFormTrees extends EntitiesTree
{
	/** 
	 独立表单树s
	*/
	public SysFormTrees()
	{
	}
	/** 
	 得到它的 Entity 
	*/
	@Override
	public Entity getGetNewEntity()
	{
		return new SysFormTree();
	}

	@Override
	public int RetrieveAll() throws Exception
	{
		int i = super.RetrieveAll();
		if (i == 0)
		{
			SysFormTree fs = new SysFormTree();
			fs.setName("公文类");
			fs.setNo("01");
			fs.Insert();

			fs = new SysFormTree();
			fs.setName("办公类");
			fs.setNo("02");
			fs.Insert();
			i = super.RetrieveAll();
		}
		return i;
	}


		///为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<SysFormTree> ToJavaList()
	{
		return (List<SysFormTree>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<SysFormTree> Tolist()
	{
		ArrayList<SysFormTree> list = new ArrayList<SysFormTree>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((SysFormTree)this.get(i));
		}
		return list;
	}

		/// 为了适应自动翻译成java的需要,把实体转换成List.
}