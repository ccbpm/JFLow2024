package bp.wf.template;

import bp.da.*;
import bp.en.*;
import bp.en.Map;
import bp.wf.*;
import bp.wf.*;
import java.util.*;

/** 
 公文模板
*/
public class DocTemplate extends EntityNoName
{

		/// 属性
	/** 
	 文件
	 * @throws Exception 
	*/
	public final byte[] getFileBytes() throws Exception
	{
			//转化为字节.
		byte[] bytes = null;
		bytes = bp.da.DataType.ConvertFileToByte(this.getFilePath());
		return bytes;
	}
	/** 
	 UI界面上的访问控制
	 * @throws Exception 
	*/
	@Override
	public UAC getHisUAC() throws Exception
	{
		UAC uac = new UAC();
		uac.OpenForSysAdmin();
		return uac;
	}

	/** 
	 路径
	 * @throws Exception 
	*/
	public final String getFilePath() throws Exception
	{
		return this.GetValStrByKey(DocTemplateAttr.FilePath);
	}
	public final void setFilePath(String value) throws Exception
	{
		this.SetValByKey(DocTemplateAttr.FilePath, value);
	}
	/** 
	 节点ID
	*/
	public final int getFK_Node()throws Exception
	{
		return this.GetValIntByKey(DocTemplateAttr.FK_Node);
	}
	public final void setFK_Node(int value) throws Exception
	{
		this.SetValByKey(DocTemplateAttr.FK_Node, value);
	}
	/** 
	 流程编号
	*/
	public final String getFK_Flow()throws Exception
	{
		return this.GetValStrByKey(DocTemplateAttr.FK_Flow);
	}
	public final void setFK_Flow(String value) throws Exception
	{
		this.SetValByKey(DocTemplateAttr.FK_Flow, value);
	}

		///


		///构造函数
	/** 
	 公文模板
	*/
	public DocTemplate()
	{
	}
	/** 
	 公文模板
	 
	 @param no
	*/
	public DocTemplate(String no) throws Exception
	{
		super(no);
	}
	/** 
	 重写基类方法
	*/
	@Override
	public Map getEnMap() throws Exception
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}

		Map map = new Map("WF_DocTemplate", "公文模板");
			//map.Java_SetCodeStruct("6");

		map.AddTBStringPK(DocTemplateAttr.No, null, "No", true, true, 1, 50, 20);
		map.AddTBString(DocTemplateAttr.Name, null, "名称", true, false, 0, 200, 20);
		map.AddTBString(DocTemplateAttr.FilePath, null, "模板路径", true, false, 0, 200, 20);
		map.AddTBInt(DocTemplateAttr.FK_Node, 0, "节点ID", true, false);
		map.AddTBString(DocTemplateAttr.FK_Flow, null, "流程编号", true, false, 0, 4, 20);
		this.set_enMap(map);
		return this.get_enMap();
	}

		///
}