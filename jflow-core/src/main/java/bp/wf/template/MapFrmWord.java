package bp.wf.template;

import bp.da.*;
import bp.difference.SystemConfig;
import bp.en.*;
import bp.en.Map;
import bp.sys.*;
import bp.web.WebUser;

/** 
 Word表单属性
*/
public class MapFrmWord extends EntityNoName
{

		///文件模版属性.
	/** 
	 模版版本号
	 * @throws Exception 
	*/
	public final String getTemplaterVer() throws Exception
	{
		return this.GetValStringByKey(MapFrmWordAttr.TemplaterVer);
	}
	public final void setTemplaterVer(String value) throws Exception
	{
		this.SetValByKey(MapFrmWordAttr.TemplaterVer, value);
	}
	/** 
	 Word数据存储字段
	 为了处理多个Word文件映射到同一张表上.
	*/
	public final String getDBSave()throws Exception
	{
		String str = this.GetValStringByKey(MapFrmWordAttr.DBSave);
		if (DataType.IsNullOrEmpty(str))
		{
			return "DBFile";
		}
		return str;
	}
	public final void setDBSave(String value) throws Exception
	{
		this.SetValByKey(MapFrmWordAttr.DBSave, value);
	}

		/// 文件模版属性.


		///属性
	/** 
	 是否是节点表单?
	 * @throws Exception 
	*/
	public final boolean getIsNodeFrm() throws Exception
	{
		if (this.getNo().contains("ND") == false)
		{
			return false;
		}

		if (this.getNo().contains("Rpt") == true)
		{
			return false;
		}

		if (this.getNo().substring(0, 2).equals("ND"))
		{
			return true;
		}

		return false;
	}
	/** 
	 节点ID.
	 * @throws Exception 
	 * @throws NumberFormatException 
	*/
	public final int getNodeID()  throws Exception 
	{
		return Integer.parseInt(this.getNo().replace("ND", ""));
	}


		///


		///权限控制.
	@Override
	public UAC getHisUAC() throws Exception
	{
		UAC uac = new UAC();
		if (WebUser.getNo().equals("admin") == true)
		{
			uac.IsDelete = false;
			uac.IsUpdate = true;
			return uac;
		}
		uac.Readonly();
		return uac;
	}

		/// 权限控制.


		///构造方法
	/** 
	 Word表单属性
	*/
	public MapFrmWord()
	{
	}
	/** 
	 Word表单属性
	 
	 @param no 表单ID
	*/
	public MapFrmWord(String no) throws Exception
	{
		super(no);
	}
	/** 
	 EnMap
	*/
	@Override
	public Map getEnMap() throws Exception
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}
		Map map = new Map("Sys_MapData", "Word表单属性");





			///基本属性.
		map.AddTBStringPK(MapFrmWordAttr.No, null, "表单编号", true, true, 1, 190, 20);
		map.AddTBString(MapFrmWordAttr.PTable, null, "存储表", true, false, 0, 100, 20);
		map.AddTBString(MapFrmWordAttr.Name, null, "表单名称", true, false, 0, 500, 20, true);

			//数据源.
		map.AddDDLEntities(MapFrmWordAttr.DBSrc, "local", "数据源", new SFDBSrcs(), true);
		map.AddDDLEntities(MapFrmWordAttr.FK_FormTree, "01", "表单类别", new SysFormTrees(), true);

			//表单的运行类型.
		map.AddDDLSysEnum(MapFrmWordAttr.FrmType, FrmType.FreeFrm.getValue(), "表单类型", true, false, MapFrmWordAttr.FrmType);

			/// 基本属性.


			///模版属性。
		map.AddTBString(MapFrmWordAttr.TemplaterVer, null, "模版编号", true, false, 0, 30, 20);
		map.AddTBString(MapFrmWordAttr.DBSave, null, "Word数据文件存储", true, false, 0, 50, 20);
		map.SetHelperAlert(MapFrmWordAttr.DBSave, "二进制的Word文件存储到表的那个字段里面？默认为DBFile, 如果此表对应多个Word文件就会导致二进制Word文件存储覆盖.");

			/// 模版属性。


			///设计者信息.
		map.AddTBString(MapFrmWordAttr.Designer, null, "设计者", true, false, 0, 500, 20);
		map.AddTBString(MapFrmWordAttr.DesignerContact, null, "联系方式", true, false, 0, 500, 20);
		map.AddTBString(MapFrmWordAttr.DesignerUnit, null, "单位", true, false, 0, 500, 20, true);
		map.AddTBString(MapFrmWordAttr.GUID, null, "GUID", true, true, 0, 128, 20, false);
		map.AddTBString(MapFrmWordAttr.Ver, null, "版本号", true, true, 0, 30, 20);
		//	map.AddTBString(MapFrmFreeAttr.DesignerTool, null, "表单设计器", true, true, 0, 30, 20);

		map.AddTBStringDoc(MapFrmWordAttr.Note, null, "备注", true, false, true);

			//增加参数字段.
		map.AddTBAtParas(4000);
		map.AddTBInt(MapFrmWordAttr.Idx, 100, "顺序号", false, false);

			/// 设计者信息.

		map.AddMyFile("表单模版", null, SystemConfig.getPathOfDataUser() + "\\FrmOfficeTemplate\\");

			//查询条件.
		map.AddSearchAttr(MapFrmWordAttr.DBSrc);


			///方法 - 基本功能.
		RefMethod rm = new RefMethod();

			/* 2017-04-28 10:52:03
			 * Mayy
			 * 去掉此功能（废弃，因在线编辑必须使用ActiveX控件，适用性、稳定性太差）
			rm = new RefMethod();
			rm.Title = "编辑Word表单模版";
			rm.ClassMethodName = this.ToString() + ".DoEditWordTemplate";
			rm.Icon = ../../Img/FileType/xlsx.gif";
			rm.Visable = true;
			rm.Target = "_blank";
			rm.refMethodType = RefMethodType.RightFrameOpen;
			map.AddRefMethod(rm);
			 */

		rm = new RefMethod();
		rm.Title = "启动傻瓜表单设计器";
		rm.ClassMethodName = this.toString() + ".DoDesignerFool";
		rm.Icon = "../../WF/Img/FileType/xlsx.gif";
		rm.Visable = true;
		rm.Target = "_blank";
		rm.refMethodType = RefMethodType.LinkeWinOpen;
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "字段维护";
		rm.ClassMethodName = this.toString() + ".DoEditFiledsList";
		rm.Icon = "../../WF/Img/FileType/xlsx.gif";
			// rm.Icon = ../../Admin/CCBPMDesigner/Img/Field.png";
		rm.Visable = true;
		rm.Target = "_blank";
		rm.refMethodType = RefMethodType.RightFrameOpen;
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "装载填充"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoPageLoadFull";
		rm.Icon = "../../WF/Img/FullData.png";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.RightFrameOpen;
		rm.Target = "_blank";
		map.AddRefMethod(rm);


		rm = new RefMethod();
		rm.Title = "表单事件"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoEvent";
		rm.Icon = "../../WF/Img/Event.png";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.RightFrameOpen;
		rm.Target = "_blank";
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "批量设置验证规则";
		rm.Icon = "../../WF/Img/RegularExpression.png";
		rm.ClassMethodName = this.toString() + ".DoRegularExpressionBatch";
		rm.refMethodType = RefMethodType.RightFrameOpen;
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "批量修改字段"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoBatchEditAttr";
		rm.Icon = "../../WF/Admin/CCBPMDesigner/Img/field.png";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.RightFrameOpen;
		rm.Target = "_blank";
			//map.AddRefMethod(rm);


		rm = new RefMethod();
		rm.Title = "JS编程"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoInitScript";
		rm.Icon = "../../WF/Img/Script.png";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.RightFrameOpen;
		rm.Target = "_blank";
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "表单body属性"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoBodyAttr";
		rm.Icon = "../../WF/Img/Script.png";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.RightFrameOpen;
		rm.Target = "_blank";
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "导出XML表单模版"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoExp";
		rm.refMethodType = RefMethodType.RightFrameOpen;
		rm.Icon = "../../WF/Img/Export.png";
		rm.Visable = true;
		rm.RefAttrLinkLabel = "导出到xml";
		rm.Target = "_blank";
		map.AddRefMethod(rm);


			/// 方法 - 基本功能.


			///高级设置.

			//带有参数的方法.
		rm = new RefMethod();
		rm.Title = "重命名字段";
		rm.GroupName = "高级设置";
		rm.getHisAttrs().AddTBString("FieldOld", null, "旧字段英文名", true, false, 0, 100, 100);
		rm.getHisAttrs().AddTBString("FieldNew", null, "新字段英文名", true, false, 0, 100, 100);
		rm.getHisAttrs().AddTBString("FieldNewName", null, "新字段中文名", true, false, 0, 100, 100);
		rm.ClassMethodName = this.toString() + ".DoChangeFieldName";
		rm.Icon = "../../WF/Img/ReName.png";
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "手机端表单";
		rm.GroupName = "高级设置";
		rm.Icon = "../../WF/Admin/CCFormDesigner/Img/telephone.png";
		rm.ClassMethodName = this.toString() + ".DoSortingMapAttrs";
		rm.refMethodType = RefMethodType.RightFrameOpen;
		map.AddRefMethod(rm);


		rm = new RefMethod();
		rm.Title = "Word表单属性";
		rm.GroupName = "高级设置";
		rm.ClassMethodName = this.toString() + ".DoMapWord";
		rm.refMethodType = RefMethodType.LinkeWinOpen;
		map.AddRefMethod(rm);


			/// 高级设置.


			///方法 - 开发接口.
		rm = new RefMethod();
		rm.Title = "调用查询API"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoSearch";
		rm.Icon = "../../WF/Img/Table.gif";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.LinkeWinOpen;
		rm.Target = "_blank";
		rm.GroupName = "开发接口";
		map.AddRefMethod(rm);

		rm = new RefMethod();
		rm.Title = "调用分析API"; // "设计表单";
		rm.ClassMethodName = this.toString() + ".DoGroup";
		rm.Icon = "../../WF/Img/Table.gif";
		rm.Visable = true;
		rm.refMethodType = RefMethodType.LinkeWinOpen;
		rm.Target = "_blank";
		rm.GroupName = "开发接口";
		map.AddRefMethod(rm);

			/// 方法 - 开发接口.

		this.set_enMap(map);
		return this.get_enMap();
	}

		///


		///节点表单方法.

	public final String DoMapWord() throws Exception
	{
		return SystemConfig.getCCFlowWebPath() + "WF/Comm/En.htm?EnName=BP.WF.Template.MapFrmWord&No=" + this.getNo();
	}
	public final String DoDesignerFool()throws Exception
	{
		return "../../Admin/FoolFormDesigner/Designer.htm?FK_MapData=" + this.getNo()+ "&MyPK=" + this.getNo()+ "&IsEditMapData=True&IsFirst=1";
	}
	public final String DoEditWordTemplate()throws Exception
	{
		return "../../Admin/CCFormDesigner/WordFrmDesigner/Designer.htm?FK_MapData=" + this.getNo();
	}
	/** 
	 表单字段.
	 
	 @return 
	*/
	public final String DoEditFiledsList()throws Exception
	{
		return "../../Admin/FoolFormDesigner/BatchEdit.htm?FK_MapData=" + this.getNo();
	}
	/** 
	 节点表单组件
	 
	 @return 
	*/
	public final String DoNodeFrmCompent()throws Exception
	{
		if (this.getNo().contains("ND") == true)
		{
			return "../../Comm/EnOnly.htm?EnName=BP.WF.Template.FrmNodeComponent&PK=" + this.getNo().replace("ND", "") + "&t=" + DataType.getCurrentDataTime();
		}
		else
		{
			return "../../Admin/FoolFormDesigner/Do.htm&DoType=FWCShowError";
		}
	}


		///


		///通用方法.
	/** 
	 替换名称
	 
	 @param fieldOldName 旧名称
	 @param newField 新字段
	 @param newFieldName 新字段名称(可以为空)
	 @return 
	 * @throws Exception 
	*/
	public final String DoChangeFieldName(String fieldOld, String newField, String newFieldName) throws Exception
	{
		MapAttr attrOld = new MapAttr();
		attrOld.setKeyOfEn(fieldOld);
		attrOld.setFK_MapData(this.getNo());
		attrOld.setMyPK(attrOld.getFK_MapData() + "_" + attrOld.getKeyOfEn());
		if (attrOld.RetrieveFromDBSources() == 0)
		{
			return "@旧字段输入错误[" + attrOld.getKeyOfEn() + "].";
		}

		//检查是否存在该字段？
		MapAttr attrNew = new MapAttr();
		attrNew.setKeyOfEn(newField);
		attrNew.setFK_MapData(this.getNo());
		attrNew.setMyPK(attrNew.getFK_MapData() + "_" + attrNew.getKeyOfEn());
		if (attrNew.RetrieveFromDBSources() == 1)
		{
			return "@该字段[" + attrNew.getKeyOfEn() + "]已经存在.";
		}

		//删除旧数据.
		attrOld.Delete();

		//copy这个数据,增加上它.
		attrNew.Copy(attrOld);
		attrNew.setKeyOfEn(newField);
		attrNew.setFK_MapData(this.getNo());

		if (!newFieldName.equals(""))
		{
			attrNew.setName( newFieldName);
		}

		attrNew.Insert();

		//更新处理他的相关业务逻辑.
		MapExts exts = new MapExts(this.getNo());
		for (MapExt item : exts.ToJavaList())
		{
			item.setMyPK(item.getMyPK().replace("_" + fieldOld, "_" + newField));

			if (fieldOld.equals(item.getAttrOfOper()))
			{
				item.setAttrOfOper(newField);
			}

			if (fieldOld.equals(item.getAttrsOfActive()))
			{
				item.setAttrsOfActive(newField);
			}

			item.setTag(item.getTag().replace(fieldOld, newField));
			item.setTag1(item.getTag1().replace(fieldOld, newField));
			item.setTag2(item.getTag2().replace(fieldOld, newField));
			item.setTag3(item.getTag3().replace(fieldOld, newField));

			item.setAtPara(item.getAtPara().replace(fieldOld, newField));
			item.setDoc(item.getDoc().replace(fieldOld, newField));
			item.Save();
		}
		return "执行成功";
	}
	/** 
	 批量设置正则表达式规则.
	 
	 @return 
	 * @throws Exception 
	*/
	public final String DoRegularExpressionBatch() throws Exception
	{
		return "../../Admin/FoolFormDesigner/MapExt/RegularExpressionBatch.htm?FK_Flow=&FK_MapData=" + this.getNo()+ "&t=" + DataType.getCurrentDataTime();
	}
	/** 
	 批量修改字段
	 
	 @return 
	*/
	public final String DoBatchEditAttr()throws Exception
	{
		return "../../Admin/FoolFormDesigner/BatchEdit.htm?FK_MapData=" + this.getNo()+ "&t=" + DataType.getCurrentDataTime();
	}
	/** 
	 排序字段顺序
	 
	 @return 
	*/
	public final String DoSortingMapAttrs()throws Exception
	{
		return "../../Admin/AttrNode/SortingMapAttrs.htm?FK_Flow=&FK_MapData=" + this.getNo()+ "&t=" + DataType.getCurrentDataTime();
	}
	/** 
	 设计表单
	 
	 @return 
	*/
	public final String DoDFrom()throws Exception
	{
		return "../../Admin/FoolFormDesigner/CCForm/Frm.htm?FK_MapData=" + this.getNo()+ "&UserNo=" + WebUser.getNo() + "&SID=" + WebUser.getSID() + "&AppCenterDBType=" + DBAccess.getAppCenterDBType() + "&CustomerNo=" + SystemConfig.getCustomerNo();
	}
	/** 
	 设计傻瓜表单
	 
	 @return 
	*/
	public final String DoDFromCol4()throws Exception
	{
		String url = "../../Admin/FoolFormDesigner/Designer.htm?FK_MapData=" + this.getNo()+ "&UserNo=" + WebUser.getNo() + "&SID=" + WebUser.getSID() + "&AppCenterDBType=" + DBAccess.getAppCenterDBType() + "&IsFirst=1&CustomerNo=" + SystemConfig.getCustomerNo();
		return url;
	}
	/** 
	 查询
	 
	 @return 
	*/
	public final String DoSearch()throws Exception
	{
		return "../../Comm/Search.htm?s=34&FK_MapData=" + this.getNo()+ "&EnsName=" + this.getNo();
	}
	/** 
	 调用分析API
	 
	 @return 
	*/
	public final String DoGroup()throws Exception
	{
		return "../../Comm/Group.htm?s=34&FK_MapData=" + this.getNo()+ "&EnsName=" + this.getNo();
	}
	/** 
	 数据源管理
	 
	 @return 
	*/
	public final String DoDBSrc()throws Exception
	{
		return "../../Comm/Search.htm?s=34&FK_MapData=" + this.getNo()+ "&EnsName=bp.sys.SFDBSrcs";
	}


	public final String DoPageLoadFull()throws Exception
	{
		return "../../Admin/FoolFormDesigner/MapExt/PageLoadFull.htm?s=34&FK_MapData=" + this.getNo()+ "&ExtType=PageLoadFull&RefNo=";
	}
	public final String DoInitScript()throws Exception
	{
		return "../../Admin/FoolFormDesigner/MapExt/InitScript.htm?s=34&FK_MapData=" + this.getNo()+ "&ExtType=PageLoadFull&RefNo=";
	}
	/** 
	 Word表单属性.
	 
	 @return 
	*/
	public final String DoBodyAttr()throws Exception
	{
		return "../../Admin/FoolFormDesigner/MapExt/BodyAttr.htm?s=34&FK_MapData=" + this.getNo()+ "&ExtType=BodyAttr&RefNo=";
	}
	/** 
	 表单事件
	 
	 @return 
	*/
	public final String DoEvent()throws Exception
	{
		return "../../Admin/CCFormDesigner/Action.htm?FK_MapData=" + this.getNo()+ "&T=sd&FK_Node=0";
	}
	/** 
	 导出表单
	 
	 @return 
	*/
	public final String DoExp()throws Exception
	{
		return "../../Admin/FoolFormDesigner/ImpExp/Exp.htm?FK_MapData=" + this.getNo();
	}

		/// 方法.
}