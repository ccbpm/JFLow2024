package BP.WF.XML;

import BP.DA.*;
import BP.En.*;
import BP.Sys.XML.*;
import BP.Sys.*;
import BP.WF.*;

/** 
 
*/
public class AdminMenus extends XmlEns
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 构造
	/** 
	 考核率的数据元素
	*/
	public AdminMenus()
	{
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#region 重写基类属性或方法。
	/** 
	 得到它的 Entity 
	*/
	@Override
	public XmlEn getNewEntity()
	{
		return new AdminMenu();
	}
	@Override
	public String getFile()
	{
		return SystemConfig.getPathOfWebApp() + "\\DataUser\\XML\\AdminMenu.xml";
	}
	/** 
	 物理表名
	*/
	@Override
	public String getTableName()
	{
		return "Item";
	}
	@Override
	public Entities getRefEns()
	{
		return null; //new BP.ZF1.AdminAdminMenus();
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
		///#endregion

}