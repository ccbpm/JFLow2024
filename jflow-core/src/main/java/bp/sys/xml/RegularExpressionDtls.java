package bp.sys.xml;

import bp.da.*;
import bp.difference.SystemConfig;
import bp.sys.*;
import bp.en.*;
import bp.sys.*;
import bp.*;
import bp.sys.*;

/** 
 
*/
public class RegularExpressionDtls extends XmlEns
{

		///构造
	/** 
	 考核率的数据元素
	*/
	public RegularExpressionDtls()
	{
	}

		///


		///重写基类属性或方法。
	/** 
	 得到它的 Entity 
	*/
	@Override
	public XmlEn getGetNewEntity()
	{
		return new RegularExpressionDtl();
	}
	@Override
	public String getFile()
	{
		return SystemConfig.getPathOfData() + "/XML/RegularExpression.xml";
	}
	/** 
	 物理表名
	*/
	@Override
	public String getTableName()
	{
		return "Dtl";
	}
	@Override
	public Entities getRefEns()
	{
		return null;
	}

		///

}