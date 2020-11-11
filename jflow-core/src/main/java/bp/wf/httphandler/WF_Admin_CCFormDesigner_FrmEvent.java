package bp.wf.httphandler;

import bp.sys.*;
import bp.da.*;
import bp.difference.handler.WebContralBase;
import bp.en.*;
import bp.wf.xml.*;
import java.util.*;

public class WF_Admin_CCFormDesigner_FrmEvent extends WebContralBase
{

	/** 
	 构造函数
	*/
	public WF_Admin_CCFormDesigner_FrmEvent()
	{
	}


		///事件基类.
	/** 
	 事件类型
	*/
	public final String getShowType()
	{
		if (this.getFK_Node() != 0)
		{
			return "Node";
		}

		if (this.getFK_Node() == 0 && DataType.IsNullOrEmpty(this.getFK_Flow()) == false && this.getFK_Flow().length() >= 3)
		{
			return "Flow";
		}

		if (this.getFK_Node() == 0 && DataType.IsNullOrEmpty(this.getFK_MapData()) == false)
		{
			return "Frm";
		}

		return "Node";
	}
	/** 
	 事件基类
	 
	 @return 
	 * @throws Exception 
	*/
	public final String Action_Init() throws Exception
	{
		DataSet ds = new DataSet();

		//事件实体.
		FrmEvents ndevs = new FrmEvents();
		if (bp.da.DataType.IsNullOrEmpty(this.getFK_MapData()) == false)
		{
			ndevs.Retrieve(FrmEventAttr.FK_MapData, this.getFK_MapData());
		}

		////已经配置的事件类实体.
		//DataTable dtFrm = ndevs.ToDataTableField("FrmEvents");
		//ds.Tables.add(dtFrm);

		//把事件类型列表放入里面.（发送前，发送成功时.）
		EventLists xmls = new EventLists();
		xmls.Retrieve("EventType", this.getShowType());

		DataTable dt = xmls.ToDataTable();
		dt.TableName = "EventLists";
		ds.Tables.add(dt);

		return bp.tools.Json.ToJson(ds);
	}
	/** 
	 获得该节点下已经绑定该类型的实体.
	 
	 @return 
	 * @throws Exception 
	*/
	public final String ActionDtl_Init() throws Exception
	{
		DataSet ds = new DataSet();

		//事件实体.
		FrmEvents ndevs = new FrmEvents();
		ndevs.Retrieve(FrmEventAttr.FK_MapData, this.getFK_MapData());

		DataTable dt = ndevs.ToDataTableField("FrmEvents");
		ds.Tables.add(dt);

		//业务单元集合.
		DataTable dtBuess = new DataTable();
		dtBuess.Columns.Add("No", String.class);
		dtBuess.Columns.Add("Name", String.class);
		dtBuess.TableName = "BuessUnits";
		ArrayList<BuessUnitBase> al = ClassFactory.GetObjects("bp.sys.BuessUnitBase");
		for (BuessUnitBase en : al)
		{
			DataRow dr = dtBuess.NewRow();
			dr.setValue("No", en.toString());
			dr.setValue("Name", en.getTitle());
			dtBuess.Rows.add(dr);
		}

		ds.Tables.add(dtBuess);

		return bp.tools.Json.ToJson(ds);
	}
	/** 
	 执行删除
	 
	 @return 
	 * @throws Exception 
	*/
	public final String ActionDtl_Delete() throws Exception
	{
		//事件实体.
		FrmEvent en = new FrmEvent();
		en.setMyPK(this.getMyPK());
		en.Delete();
		return "删除成功.";
	}
	public final String ActionDtl_Save() throws Exception
	{
		//事件实体.
		FrmEvent en = new FrmEvent();

		en.setFK_Node(this.getFK_Node());
		en.setFK_Event(this.GetRequestVal("FK_Event")); //事件类型.
		en.setHisDoTypeInt(this.GetValIntFromFrmByKey("EventDoType")); //执行类型.
		en.setMyPK(this.getFK_Node() + "_" + en.getFK_Event() + "_" + en.getHisDoTypeInt()); //组合主键.
		en.RetrieveFromDBSources();

		en.setMsgOKString(this.GetValFromFrmByKey("MsgOK")); //成功的消息.
		en.setMsgErrorString(this.GetValFromFrmByKey("MsgError")); //失败的消息.

		//执行内容.
		if (en.getHisDoType() == EventDoType.BuessUnit)
		{
			en.setDoDoc(this.GetValFromFrmByKey("DDL_Doc"));
		}
		else
		{
			en.setDoDoc(this.GetValFromFrmByKey("TB_Doc"));
		}

		en.Save();

		return "保存成功.";
	}

		/// 事件基类.

}