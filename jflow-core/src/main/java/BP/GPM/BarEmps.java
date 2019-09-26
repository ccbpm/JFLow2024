package BP.GPM;

import BP.DA.*;
import BP.En.*;
import BP.Web.WebUser;

import java.util.*;

/** 
 人员信息块s
*/
public class BarEmps extends EntitiesMyPK
{

		///#region 构造
	/** 
	 人员信息块s
	*/
	public BarEmps()
	{
	}
	/** 
	 得到它的 Entity
	*/
	@Override
	public Entity getNewEntity()
	{
		return new BarEmp();
	}

		///#endregion

	public final String InitMyBars() throws Exception
	{
		Bars bars = new Bars();
		bars.RetrieveAll();
		for (Bar b : bars.ToJavaList())
		{
			BarEmp be = new BarEmp();
			be.setMyPK( b.getNo() + "_" + WebUser.getNo());
			if (be.RetrieveFromDBSources() == 1)
			{
				continue;
			}

			be.setFK_Bar(b.getNo());
			be.setFK_Emp(WebUser.getNo());
			be.setIsShow(true);
			be.setTitle(b.getName());
			be.Insert();
		}

		return "执行成功";
	}


		///#region 为了适应自动翻译成java的需要,把实体转换成List.
	/** 
	 转化成 java list,C#不能调用.
	 
	 @return List
	*/
	public final List<BarEmp> ToJavaList()
	{
		return (List<BarEmp>)(Object)this;
	}
	/** 
	 转化成list
	 
	 @return List
	*/
	public final ArrayList<BarEmp> Tolist()
	{
		ArrayList<BarEmp> list = new ArrayList<BarEmp>();
		for (int i = 0; i < this.size(); i++)
		{
			list.add((BarEmp)this.get(i));
		}
		return list;
	}

		///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}