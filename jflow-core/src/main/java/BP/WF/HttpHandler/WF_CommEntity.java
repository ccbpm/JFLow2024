package BP.WF.HttpHandler;

import BP.DA.*;
import BP.Difference.SystemConfig;
import BP.Difference.Handler.WebContralBase;
import BP.Sys.*;
import BP.Tools.FtpUtil;
import BP.Tools.ZipCompress;
import BP.Web.*;
import BP.En.*;
import java.io.*;
import java.nio.file.*;

/**
 * 页面功能实体
 */
public class WF_CommEntity extends WebContralBase {

	/**
	 * 构造函数
	 */
	public WF_CommEntity() {
	}

	/// #region 从表.
	/**
	 * 初始化
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String Dtl_Save() throws Exception {
		try {

			/// #region 查询出来从表数据.
			Entities dtls = ClassFactory.GetEns(this.getEnsName());
			Entity dtl = dtls.getNewEntity();
			dtls.Retrieve(this.GetRequestVal("RefKey"), this.GetRequestVal("RefVal"));
			Map map = dtl.getEnMap();
			for (Entity item : dtls) {
				String pkval = item.GetValStringByKey(dtl.getPK());
				for (Attr attr : map.getAttrs()) {
					if (attr.getIsRefAttr() == true) {
						continue;
					}

					if (attr.getMyDataType() == DataType.AppDateTime || attr.getMyDataType() == DataType.AppDate) {
						if (attr.getUIIsReadonly() == true) {
							continue;
						}

						String val = this.GetValFromFrmByKey("TB_" + pkval + "_" + attr.getKey(), null);
						item.SetValByKey(attr.getKey(), val);
						continue;
					}

					if (attr.getUIContralType() == UIContralType.TB && attr.getUIIsReadonly() == false) {
						String val = this.GetValFromFrmByKey("TB_" + pkval + "_" + attr.getKey(), null);
						item.SetValByKey(attr.getKey(), val);
						continue;
					}

					if (attr.getUIContralType() == UIContralType.DDL && attr.getUIIsReadonly() == true) {
						String val = this.GetValFromFrmByKey("DDL_" + pkval + "_" + attr.getKey());
						item.SetValByKey(attr.getKey(), val);
						continue;
					}

					if (attr.getUIContralType() == UIContralType.CheckBok && attr.getUIIsReadonly() == true) {
						String val = this.GetValFromFrmByKey("CB_" + pkval + "_" + attr.getKey(), "-1");
						if (val.equals("-1")) {
							item.SetValByKey(attr.getKey(), 0);
						} else {
							item.SetValByKey(attr.getKey(), 1);
						}
						continue;
					}
				}

				item.Update(); // 执行更新.
			}

			/// #endregion 查询出来从表数据.

			/// #region 保存新加行.
			int newRowCount = this.GetRequestValInt("NewRowCount");
			boolean isEntityOID = dtl.getIsOIDEntity();
			boolean isEntityNo = dtl.getIsNoEntity();
			for (int i = 0; i < newRowCount; i++) {
				String val = "";
				for (Attr attr : map.getAttrs()) {

					if (attr.getMyDataType() == DataType.AppDateTime || attr.getMyDataType() == DataType.AppDate) {
						if (attr.getUIIsReadonly() == true) {
							continue;
						}

						val = this.GetValFromFrmByKey("TB_" + i + "_" + attr.getKey(), null);
						dtl.SetValByKey(attr.getKey(), val);
						continue;
					}

					if (attr.getUIContralType() == UIContralType.TB && attr.getUIIsReadonly() == false) {
						val = this.GetValFromFrmByKey("TB_" + i + "_" + attr.getKey());
						if (attr.getIsNum() && val.equals("")) {
							val = "0";
						}
						dtl.SetValByKey(attr.getKey(), val);
						continue;
					}

					if (attr.getUIContralType() == UIContralType.DDL && attr.getUIIsReadonly() == true) {
						val = this.GetValFromFrmByKey("DDL_" + i + "_" + attr.getKey());
						dtl.SetValByKey(attr.getKey(), val);
						continue;
					}

					if (attr.getUIContralType() == UIContralType.CheckBok && attr.getUIIsReadonly() == true) {
						val = this.GetValFromFrmByKey("CB_" + i + "_" + attr.getKey(), "-1");
						if (val.equals("-1")) {
							dtl.SetValByKey(attr.getKey(), 0);
						} else {
							dtl.SetValByKey(attr.getKey(), 1);
						}
						continue;
					}
				}
				// dtl.SetValByKey(pkval, 0);
				dtl.SetValByKey(this.GetRequestVal("RefKey"), this.GetRequestVal("RefVal"));

				if (isEntityOID == true)
				{
					dtl.setPKVal("0") ;
					dtl.Insert();
					continue;
				}

				if (isEntityNo == true && dtl.getEnMap().getIsAutoGenerNo()==true)
				{
					dtl.setPKVal(dtl.GenerNewNoByKey("No"));
					dtl.Insert();
					continue;
				}
//				dtl.setPKVal("0");
				dtl.Insert();
			}

			/// #endregion 保存新加行.

			return "保存成功.";
		} catch (RuntimeException ex) {
			return "err@" + ex.getMessage();
		}
	}

	/**
	 * 保存
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String Dtl_Init() throws Exception {
		// 定义容器.
		DataSet ds = new DataSet();

		// 查询出来从表数据.
		Entities dtls = ClassFactory.GetEns(this.getEnsName());
		dtls.Retrieve(this.GetRequestVal("RefKey"), this.GetRequestVal("RefVal"));
		ds.Tables.add(dtls.ToDataTableField("Dtls"));

		// 实体.
		Entity dtl = dtls.getNewEntity();
		// 定义Sys_MapData.
		MapData md = new MapData();
		md.setNo(this.getEnName());
		md.setName(dtl.getEnDesc());

		/// #region 加入权限信息.
		// 把权限加入参数里面.
		if (dtl.getHisUAC().IsInsert) {
			md.SetPara("IsInsert", "1");
		}
		if (dtl.getHisUAC().IsUpdate) {
			md.SetPara("IsUpdate", "1");
		}
		if (dtl.getHisUAC().IsDelete) {
			md.SetPara("IsDelete", "1");
		}
		if (dtl.getHisUAC().IsImp) {
			md.SetPara("IsImp", "1");
		}

		/// #endregion 加入权限信息.

		ds.Tables.add(md.ToDataTableField("Sys_MapData"));

		/// #region 字段属性.
		MapAttrs attrs = dtl.getEnMap().getAttrs().ToMapAttrs();
		DataTable sys_MapAttrs = attrs.ToDataTableField("Sys_MapAttr");
		ds.Tables.add(sys_MapAttrs);

		/// #endregion 字段属性.

		/// #region 把外键与枚举放入里面去.
		for (DataRow dr : sys_MapAttrs.Rows) {
			String uiBindKey = dr.getValue("UIBindKey").toString();
			String lgType = dr.getValue("LGType").toString();
			if (lgType.equals("2") == false) {
				continue;
			}

			String UIIsEnable = dr.getValue("UIVisible").toString();
			if (UIIsEnable.equals("0")) {
				continue;
			}

			if (DataType.IsNullOrEmpty(uiBindKey) == true) {
				String myPK = dr.getValue("MyPK").toString();
				/* 如果是空的 */
				// throw new Exception("@属性字段数据不完整，流程:" + fl.No + fl.Name +
				// ",节点:" + nd.NodeID + nd.Name + ",属性:" + myPK + ",的UIBindKey
				// IsNull ");
			}

			// 检查是否有下拉框自动填充。
			String keyOfEn = dr.getValue("KeyOfEn").toString();
			String fk_mapData = dr.getValue("FK_MapData").toString();

			// 判断是否存在.
			if (ds.Tables.contains(uiBindKey) == true) {
				continue;
			}

			ds.Tables.add(BP.Sys.PubClass.GetDataTableByUIBineKey(uiBindKey));
		}

		for (Attr attr : dtl.getEnMap().getAttrs()) {
			if (attr.getIsRefAttr() == true) {
				continue;
			}

			if (DataType.IsNullOrEmpty(attr.getUIBindKey()) || attr.getUIBindKey().length() <= 10) {
				continue;
			}

			if (attr.getUIIsReadonly() == true) {
				continue;
			}

			if (attr.getUIBindKey().contains("SELECT") == true || attr.getUIBindKey().contains("select") == true) {
				/* 是一个sql */
				Object tempVar = attr.getUIBindKey();
				String sqlBindKey = tempVar instanceof String ? (String) tempVar : null;

				// 判断是否存在.
				if (ds.Tables.contains(sqlBindKey) == true) {
					continue;
				}

				sqlBindKey = BP.WF.Glo.DealExp(sqlBindKey, null, null);

				DataTable dt = DBAccess.RunSQLReturnTable(sqlBindKey);
				dt.TableName = attr.getKey();

				// @杜. 翻译当前部分.
				if (SystemConfig.getAppCenterDBType() == DBType.Oracle
						|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL) {
					dt.Columns.get("NO").ColumnName = "No";
					dt.Columns.get("NAME").ColumnName = "Name";
				}

				ds.Tables.add(dt);
			}
		}

		String enumKeys = "";
		for (Attr attr : dtl.getEnMap().getAttrs()) {
			if (attr.getMyFieldType() == FieldType.Enum) {
				enumKeys += "'" + attr.getUIBindKey() + "',";
			}
		}

		if (enumKeys.length() > 2) {
			enumKeys = enumKeys.substring(0, enumKeys.length() - 1);
			// Sys_Enum
			String sqlEnum = "SELECT * FROM Sys_Enum WHERE EnumKey IN (" + enumKeys + ")";
			DataTable dtEnum = DBAccess.RunSQLReturnTable(sqlEnum);
			dtEnum.TableName = "Sys_Enum";

			if (SystemConfig.getAppCenterDBType() == DBType.Oracle
					|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL) {
				dtEnum.Columns.get("MYPK").ColumnName = "MyPK";
				dtEnum.Columns.get("LAB").ColumnName = "Lab";
				dtEnum.Columns.get("ENUMKEY").ColumnName = "EnumKey";
				dtEnum.Columns.get("INTKEY").ColumnName = "IntKey";
				dtEnum.Columns.get("LANG").ColumnName = "Lang";
			}
			ds.Tables.add(dtEnum);
		}

		/// #endregion 把外键与枚举放入里面去.

		return BP.Tools.Json.ToJson(ds);
	}

	public final String Dtl_Exp() throws Exception {
		String refPKVal = this.GetRequestVal("RefVal");
		Entities dtls = ClassFactory.GetEns(this.getEnsName());
		dtls.Retrieve(this.GetRequestVal("RefKey"), this.GetRequestVal("RefVal"));
		Entity en = dtls.getNewEntity();
		String name = "数据导出";
		if (refPKVal.contains("/") == true) {
			refPKVal = refPKVal.replace("/", "_");
		}
		String filename = refPKVal + "_" + en.toString() + "_" + DataType.getCurrentDate() + "_" + name + ".xls";
		String filePath = ExportDGToExcel(dtls.ToDataTableField(), en, name, null);

		filePath = SystemConfig.getPathOfTemp() + filename;

		String tempPath = SystemConfig.getPathOfTemp() + refPKVal + "/";
		if ((new File(tempPath)).isDirectory() == false) {
			(new File(tempPath)).mkdirs();
		}

		String myFilePath = SystemConfig.getPathOfDataUser()
				+ this.getEnsName().substring(0, this.getEnsName().length() - 1);

		for (Entity dt : dtls) {
			String pkval = dt.getPKVal().toString();
			Object tempVar = dt.GetValByKey("MyFileExt");
			String ext = DataType.IsNullOrEmpty(tempVar instanceof String ? (String) tempVar : null) ? ""
					: dt.GetValByKey("MyFileExt").toString();
			if (DataType.IsNullOrEmpty(ext) == true) {
				continue;
			}
			myFilePath = myFilePath + "/" + pkval + "." + ext;
			if ((new File(myFilePath)).isFile() == true) {
				Files.copy(Paths.get(myFilePath), Paths.get(tempPath + pkval + "." + ext),
						StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		Files.copy(Paths.get(filePath), Paths.get(tempPath + filename), StandardCopyOption.COPY_ATTRIBUTES,
				StandardCopyOption.REPLACE_EXISTING);

		// 生成压缩文件
		String zipFile = SystemConfig.getPathOfTemp() + refPKVal + "_" + en.toString() + "_"
				+ DataType.getCurrentDate() + "_" + name + ".zip";

		// 执行压缩.
		ZipCompress fz = new ZipCompress(zipFile, tempPath);
		fz.zip();

		return "/DataUser/Temp/" + refPKVal + "_" + en.toString() + "_" + DataType.getCurrentDate() + "_" + name
				+ ".zip";
	}

	/**
	 * 实体初始化
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String EntityOnly_Init() throws Exception {
		try {
			// 是否是空白记录.
			boolean isBlank = DataType.IsNullOrEmpty(this.getPKVal());

			// 初始化entity.
			String enName = this.getEnName();
			Entity en = null;
			if (isBlank == true) {
				if (DataType.IsNullOrEmpty(this.getEnsName()) == true) {
					return "err@类名没有传递过来";
				}
				Entities ens = ClassFactory.GetEns(this.getEnsName());

				if (ens == null) {
					return "err@类名错误" + this.getEnsName(); // @李国文.
				}

				en = ens.getNewEntity();
			} else {
				en = ClassFactory.GetEn(this.getEnName());
			}

			if (en == null) {
				return "err@参数类名不正确.";
			}

			// 获得描述.
			Map map = en.getEnMap();
			String pkVal = this.getPKVal();
			if (isBlank == false) {
				en.setPKVal(pkVal);
				int i = en.RetrieveFromDBSources();
				if (i == 0) {
					return "err@数据[" + map.getEnDesc() + "]主键为[" + pkVal + "]不存在，或者没有保存。";
				}
			} else {
				for (Attr attr : en.getEnMap().getAttrs()) {
					en.SetValByKey(attr.getKey(), attr.getDefaultVal());
				}

				// 设置默认的数据.
				en.ResetDefaultVal();

				en.SetValByKey("RefPKVal", this.getRefPKVal());

				// 自动生成一个编号.
				if (en.getIsNoEntity() == true && en.getEnMap().getIsAutoGenerNo() == true) {
					en.SetValByKey("No", en.GenerNewNoByKey("No"));
				}
			}

			// 定义容器.
			DataSet ds = new DataSet();

			// 定义Sys_MapData.
			MapData md = new MapData();
			md.setNo(this.getEnName());
			md.setName(map.getEnDesc());

			// 附件类型.
			md.SetPara("BPEntityAthType", map.HisBPEntityAthType.toString());

			// 多附件上传
			if (map.HisBPEntityAthType.compareTo(BPEntityAthType.Multi) == 0) {
				// 增加附件分类
				DataTable attrFiles = new DataTable("AttrFiles");
				attrFiles.Columns.Add("FileNo");
				attrFiles.Columns.Add("FileName");
				for (AttrFile attrFile : map.getHisAttrFiles()) {
					DataRow dr = attrFiles.NewRow();
					dr.setValue("FileNo", attrFile.FileNo);
					dr.setValue("FileName", attrFile.FileName);
					attrFiles.Rows.add(dr);
				}
				ds.Tables.add(attrFiles);

				// 增加附件列表
				SysFileManagers sfs = new SysFileManagers(en.toString(), en.getPKVal().toString());
				ds.Tables.add(sfs.ToDataTableField("Sys_FileManager"));
			}

			/// #region 加入权限信息.
			// 把权限加入参数里面.
			if (en.getHisUAC().IsInsert) {
				md.SetPara("IsInsert", "1");
			}
			if (en.getHisUAC().IsUpdate) {
				md.SetPara("IsUpdate", "1");
			}
			if (isBlank == true) {
				if (en.getHisUAC().IsDelete) {
					md.SetPara("IsDelete", "0");
				}
			} else {
				if (en.getHisUAC().IsDelete) {
					md.SetPara("IsDelete", "1");
				}
			}

			/// #endregion 加入权限信息.

			ds.Tables.add(md.ToDataTableField("Sys_MapData"));

			// 把主数据放入里面去.
			DataTable dtMain = en.ToDataTableField("MainTable");
			ds.Tables.add(dtMain);

			/// #region 增加上分组信息.
			EnCfg ec = new EnCfg(this.getEnName());
			String groupTitle = ec.getGroupTitle();
			if (DataType.IsNullOrEmpty(groupTitle) == true) {
				groupTitle = "@" + en.getPK() + ",基本信息," + map.getEnDesc() + "";
			}

			// 增加上.
			DataTable dtGroups = new DataTable("Sys_GroupField");
			dtGroups.Columns.Add("OID");
			dtGroups.Columns.Add("Lab");
			dtGroups.Columns.Add("Tip");
			dtGroups.Columns.Add("CtrlType");
			dtGroups.Columns.Add("CtrlID");

			String[] strs = groupTitle.split("[@]", -1);
			for (String str : strs) {
				if (DataType.IsNullOrEmpty(str)) {
					continue;
				}

				String[] vals = str.split("[=]", -1);
				if (vals.length == 1) {
					vals = str.split("[,]", -1);
				}

				if (vals.length == 0) {
					continue;
				}

				DataRow dr = dtGroups.NewRow();
				dr.setValue("OID", vals[0].trim());
				dr.setValue("Lab", vals[1].trim());
				if (vals.length == 3) {
					dr.setValue("Tip", vals[2].trim());
				}
				dtGroups.Rows.add(dr);
			}
			ds.Tables.add(dtGroups);

			/// #endregion 增加上分组信息.

			/// #region 字段属性.
			MapAttrs attrs = en.getEnMap().getAttrs().ToMapAttrs();
			DataTable sys_MapAttrs = attrs.ToDataTableField("Sys_MapAttr");
//			sys_MapAttrs.Columns.remove(MapAttrAttr.GroupID);
//			sys_MapAttrs.Columns.Add("GroupID");

			sys_MapAttrs.Columns.get(MapAttrAttr.GroupID).setDataType(String.class); //改变列类型.

			// 给字段增加分组.
			String currGroupID = "";
			for (DataRow drAttr : sys_MapAttrs.Rows) {
				if (currGroupID.equals("") == true) {
					currGroupID = dtGroups.Rows.get(0).getValue("OID").toString();
				}

				String keyOfEn = drAttr.get(MapAttrAttr.KeyOfEn).toString();
				for (DataRow drGroup : dtGroups.Rows) {
					String field = drGroup.getValue("OID").toString();
					if (keyOfEn.equals(field)) {
						currGroupID = field;
					}
				}
				drAttr.setValue(MapAttrAttr.GroupID, currGroupID);
			}
			ds.Tables.add(sys_MapAttrs);

			/// #endregion 字段属性.

			/// #region 加入扩展属性.
			MapExts mapExts = new MapExts(this.getEnName() + "s");
			DataTable Sys_MapExt = mapExts.ToDataTableField("Sys_MapExt");
			ds.Tables.add(Sys_MapExt);

			/// #endregion 加入扩展属性.

			/// #region 把外键与枚举放入里面去.

			// 加入外键.
			for (DataRow dr : sys_MapAttrs.Rows) {
				String uiBindKey = dr.getValue("UIBindKey").toString();
				String lgType = dr.getValue("LGType").toString();
				if (lgType.equals("2") == false) {
					continue;
				}

				String UIIsEnable = dr.getValue("UIVisible").toString();

				if (UIIsEnable.equals("0") == true) {
					continue;
				}

				if (DataType.IsNullOrEmpty(uiBindKey) == true) {
					String myPK = dr.getValue("MyPK").toString();
					/* 如果是空的 */
					// throw new Exception("@属性字段数据不完整，流程:" + fl.No + fl.Name +
					// ",节点:" + nd.NodeID + nd.Name + ",属性:" + myPK +
					// ",的UIBindKey IsNull ");
				}

				// 检查是否有下拉框自动填充。
				String keyOfEn = dr.getValue("KeyOfEn").toString();
				String fk_mapData = dr.getValue("FK_MapData").toString();

				// 判断是否存在.
				if (ds.Tables.contains(uiBindKey) == true) {
					continue;
				}

				DataTable dt = BP.Sys.PubClass.GetDataTableByUIBineKey(uiBindKey);
				dt.TableName = keyOfEn;

				ds.Tables.add(dt);
			}

			// 加入sql模式的外键.
			for (Attr attr : en.getEnMap().getAttrs()) {
				if (attr.getIsRefAttr() == true) {
					continue;
				}

				if (DataType.IsNullOrEmpty(attr.getUIBindKey()) || attr.getUIBindKey().length() <= 10) {
					continue;
				}

				if (attr.getUIIsReadonly() == true) {
					continue;
				}

				if (attr.getUIBindKey().contains("SELECT") == true || attr.getUIBindKey().contains("select") == true) {
					/* 是一个sql */
					Object tempVar = attr.getUIBindKey();
					String sqlBindKey = tempVar instanceof String ? (String) tempVar : null;
					sqlBindKey = BP.WF.Glo.DealExp(sqlBindKey, en, null);

					DataTable dt = DBAccess.RunSQLReturnTable(sqlBindKey);
					dt.TableName = attr.getKey();

					// @杜. 翻译当前部分.
					if (SystemConfig.getAppCenterDBType() == DBType.Oracle
							|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL
							|| SystemConfig.getAppCenterDBType() == DBType.DM) {
						dt.Columns.get("NO").ColumnName = "No";
						dt.Columns.get("NAME").ColumnName = "Name";
					}

					ds.Tables.add(dt);
				}
			}

			// 加入枚举的外键.
			String enumKeys = "";
			for (Attr attr : map.getAttrs()) {
				if (attr.getMyFieldType() == FieldType.Enum) {
					enumKeys += "'" + attr.getUIBindKey() + "',";
				}
			}

			if (enumKeys.length() > 2) {
				enumKeys = enumKeys.substring(0, enumKeys.length() - 1);
				// Sys_Enum
				String sqlEnum = "SELECT * FROM Sys_Enum WHERE EnumKey IN (" + enumKeys + ")";
				DataTable dtEnum = DBAccess.RunSQLReturnTable(sqlEnum);
				dtEnum.TableName = "Sys_Enum";

				if (SystemConfig.getAppCenterDBType() == DBType.Oracle
						|| SystemConfig.getAppCenterDBType() == DBType.PostgreSQL
						|| SystemConfig.getAppCenterDBType() == DBType.DM) {
					dtEnum.Columns.get("MYPK").ColumnName = "MyPK";
					dtEnum.Columns.get("LAB").ColumnName = "Lab";
					dtEnum.Columns.get("ENUMKEY").ColumnName = "EnumKey";
					dtEnum.Columns.get("INTKEY").ColumnName = "IntKey";
					dtEnum.Columns.get("LANG").ColumnName = "Lang";
				}

				ds.Tables.add(dtEnum);
			}

			/// #endregion 把外键与枚举放入里面去.

			/// #region 增加 上方法.
			DataTable dtM = new DataTable("dtM");
			dtM.Columns.Add("No");
			dtM.Columns.Add("Title");
			dtM.Columns.Add("Tip");
			dtM.Columns.Add("Visable");

			dtM.Columns.Add("Url");
			dtM.Columns.Add("Target");
			dtM.Columns.Add("Warning");
			dtM.Columns.Add("RefMethodType");
			dtM.Columns.Add("GroupName");
			dtM.Columns.Add("W");
			dtM.Columns.Add("H");
			dtM.Columns.Add("Icon");
			dtM.Columns.Add("IsCanBatch");
			dtM.Columns.Add("RefAttrKey");

			RefMethods rms = map.getHisRefMethods();
			for (RefMethod item : rms) {
				item.HisEn = en;
				// item.HisAttrs = en.getEnMap().getAttrs();B
				String myurl = "";
				if (item.refMethodType != RefMethodType.Func) {
					Object tempVar2 = item.Do(null);
					myurl = tempVar2 instanceof String ? (String) tempVar2 : null;
					if (myurl == null) {
						continue;
					}
				} else {
					myurl = "../RefMethod.htm?Index=" + item.Index + "&EnName=" + en.toString() + "&EnsName="
							+ en.getGetNewEntities().toString() + "&PKVal=" + this.getPKVal();
				}

				DataRow dr = dtM.NewRow();

				dr.setValue("No", item.Index);
				dr.setValue("Title", item.Title);
				dr.setValue("Tip", item.ToolTip);
				dr.setValue("Visable", item.Visable);
				dr.setValue("Warning", item.Warning);

				dr.setValue("RefMethodType", item.refMethodType.getValue());
				dr.setValue("RefAttrKey", item.RefAttrKey);
				dr.setValue("Url", myurl);
				dr.setValue("W", item.Width);
				dr.setValue("H", item.Height);
				dr.setValue("Icon", item.Icon);
				dr.setValue("IsCanBatch", item.IsCanBatch);
				dr.setValue("GroupName", item.GroupName);

				dtM.Rows.add(dr); // 增加到rows.
			}

			/// #endregion 增加 上方法.

			// 增加方法。
			ds.Tables.add(dtM);

			return BP.Tools.Json.ToJson(ds);
		} catch (RuntimeException ex) {
			return "err@" + ex.getMessage();
		}
	}

	/**
	 * 删除实体多附件上传的信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String EntityMultiFile_Delete() throws Exception {
		int oid = (int) this.getOID();
		SysFileManager fileManager = new SysFileManager(oid);
		// 获取上传的附件路径，删除附件
		String filepath = fileManager.getMyFilePath();
		if (SystemConfig.getIsUploadFileToFTP() == false) {
			if ((new File(filepath)).isFile() == true) {
				(new File(filepath)).delete();
			}
		} else {
			/* 保存到fpt服务器上. */
			FtpUtil ftpUtil = BP.WF.Glo.getFtpUtil();
			String msg = ftpUtil.openConnection();
			if (msg.contains("err@") == true) {
				return msg;
			}

			ftpUtil.deleteFile(filepath);

		}
		fileManager.Delete();
		return fileManager.getMyFileName() + "删除成功";
	}

	/**
	 * 实体初始化
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String Entity_Init() throws Exception {
		try {
			// 是否是空白记录.
			boolean isBlank = DataType.IsNullOrEmpty(this.getPKVal());
			// if (DataType.IsNullOrEmpty(this.PKVal) == true)
			// return "err@主键数据丢失，不能初始化En.htm";

			// 初始化entity.
			String enName = this.getEnName();
			Entity en = null;
			if (DataType.IsNullOrEmpty(enName) == true) {
				if (DataType.IsNullOrEmpty(this.getEnsName()) == true) {
					return "err@类名没有传递过来";
				}
				Entities ens = ClassFactory.GetEns(this.getEnsName());
				en = ens.getNewEntity();
			} else {
				en = ClassFactory.GetEn(this.getEnName());
			}

			if (en == null) {
				return "err@参数类名不正确.";
			}

			// 获得描述.
			Map map = en.getEnMap();

			String pkVal = this.getPKVal();

			if (isBlank == false) {
				en.setPKVal(pkVal);
				en.RetrieveFromDBSources();
			}

			// 定义容器.
			DataSet ds = new DataSet();

			// 把主数据放入里面去.
			DataTable dtMain = en.ToDataTableField("MainTable");
			ds.Tables.add(dtMain);

			/// #region 增加 上方法.
			DataTable dtM = new DataTable("dtM");
			dtM.Columns.Add("No");
			dtM.Columns.Add("Title");
			dtM.Columns.Add("Tip");
			dtM.Columns.Add("Visable", Boolean.class);

			dtM.Columns.Add("Url");
			dtM.Columns.Add("Target");
			dtM.Columns.Add("Warning");
			dtM.Columns.Add("RefMethodType");
			dtM.Columns.Add("GroupName");
			dtM.Columns.Add("W");
			dtM.Columns.Add("H");
			dtM.Columns.Add("Icon");
			dtM.Columns.Add("IsCanBatch");
			dtM.Columns.Add("RefAttrKey");
			// 判断Func是否有参数
			dtM.Columns.Add("FunPara");

			RefMethods rms = map.getHisRefMethods();
			for (RefMethod item : rms) {
				item.HisEn = en;

				String myurl = "";
				if (item.refMethodType == RefMethodType.LinkeWinOpen
						|| item.refMethodType == RefMethodType.RightFrameOpen
						|| item.refMethodType == RefMethodType.LinkModel) {
					try {
						Object tempVar = item.Do(null);
						myurl = tempVar instanceof String ? (String) tempVar : null;
						if (myurl == null) {
							continue;
						}
					} catch (RuntimeException ex) {
						throw new RuntimeException("err@系统错误:根据方法名生成url出现错误:@" + ex.getMessage() + "@" + ex.getCause()
								+ " @方法名:" + item.ClassMethodName);
					}
				} else {
					myurl = "../RefMethod.htm?Index=" + item.Index + "&EnName=" + en.toString() + "&EnsName="
							+ en.getGetNewEntities().toString() + "&PKVal=" + this.getPKVal();
				}

				DataRow dr = dtM.NewRow();

				dr.setValue("No", item.Index);
				dr.setValue("Title", item.Title);
				dr.setValue("Tip", item.ToolTip);
				dr.setValue("Visable", item.Visable);
				dr.setValue("Warning", item.Warning);

				dr.setValue("RefMethodType", item.refMethodType.getValue());
				dr.setValue("RefAttrKey", item.RefAttrKey);
				dr.setValue("Url", myurl);
				dr.setValue("W", item.Width);
				dr.setValue("H", item.Height);
				dr.setValue("Icon", item.Icon);
				dr.setValue("IsCanBatch", item.IsCanBatch);
				dr.setValue("GroupName", item.GroupName);
				Attrs attrs = item.getHisAttrs();
				if (attrs.size() == 0) {
					dr.setValue("FunPara", "false");
				} else {
					dr.setValue("FunPara", "true");
				}

				dtM.Rows.add(dr); // 增加到rows.
			}

			/// #endregion 增加 上方法.

			/// #region 加入一对多的实体编辑
			AttrsOfOneVSM oneVsM = en.getEnMap().getAttrsOfOneVSM();
			String sql = "";
			int i = 0;
			if (oneVsM.size() > 0) {
				for (AttrOfOneVSM vsM : oneVsM) {
					String rootNo = vsM.RootNo;
					if (rootNo!=null && rootNo.contains("@") == true)
					{
						rootNo = rootNo.replace("@WebUser.FK_Dept", WebUser.getFK_Dept());
						rootNo = rootNo.replace("@WebUser.OrgNo", WebUser.getOrgNo());
					}

					// 判断该dot2dot是否显示？
					Entity enMM = vsM.getEnsOfMM().getNewEntity();
					enMM.SetValByKey(vsM.getAttrOfOneInMM(), this.getPKVal());
					if (enMM.getHisUAC().IsView == false) {
						continue;
					}
					DataRow dr = dtM.NewRow();
					dr.setValue("No", enMM.toString());
					if (en.getPKVal() != null) {
						// 判断模式.
						String url = "";
						if (vsM.dot2DotModel == Dot2DotModel.TreeDept) {
							url = "Branches.htm?EnName=" + this.getEnName() + "&Dot2DotEnsName="
									+ vsM.getEnsOfMM().toString();
							url += "&Dot2DotEnName=" + vsM.getEnsOfMM().getNewEntity().toString(); // 存储实体类.
							url += "&AttrOfOneInMM=" + vsM.getAttrOfOneInMM(); // 存储表那个与主表关联.
																				// 比如:
																				// FK_Node
							url += "&AttrOfMInMM=" + vsM.getAttrOfMInMM(); // dot2dot存储表那个与实体表.
																			// 比如:FK_Station.
							url += "&EnsOfM=" + vsM.getEnsOfM().toString(); // 默认的B实体分组依据.
																			// 比如:FK_Station.
							url += "&DefaultGroupAttrKey=" + vsM.DefaultGroupAttrKey; // 默认的B实体分组依据.
							url += "&RootNo=" + rootNo;

						} else if (vsM.dot2DotModel == Dot2DotModel.TreeDeptEmp) {
							
							url = "BranchesAndLeaf.htm?EnName=" + this.getEnName() + "&Dot2DotEnsName="
									+ vsM.getEnsOfMM().toString();
							url += "&Dot2DotEnName=" + vsM.getEnsOfMM().getNewEntity().toString(); // 存储实体类.
							url += "&AttrOfOneInMM=" + vsM.getAttrOfOneInMM(); // 存储表那个与主表关联.
																				// 比如:
																				// FK_Node
							url += "&AttrOfMInMM=" + vsM.getAttrOfMInMM(); // dot2dot存储表那个与实体表.
																			// 比如:FK_Station.
							url += "&EnsOfM=" + vsM.getEnsOfM().toString(); // 默认的B实体分组依据.
																			// 比如:FK_Station.
							url += "&DefaultGroupAttrKey=" + vsM.DefaultGroupAttrKey; // 默认的B实体分组依据.
							url += "&RootNo=" + rootNo;
						} else {
							url = "Dot2Dot.htm?EnName=" + this.getEnName() + "&Dot2DotEnsName="
									+ vsM.getEnsOfMM().toString(); // 比如:BP.WF.Template.NodeStations
							url += "&AttrOfOneInMM=" + vsM.getAttrOfOneInMM(); // 存储表那个与主表关联.
																				// 比如:
																				// FK_Node
							url += "&AttrOfMInMM=" + vsM.getAttrOfMInMM(); // dot2dot存储表那个与实体表.
																			// 比如:FK_Station.
							url += "&EnsOfM=" + vsM.getEnsOfM().toString(); // 默认的B实体.
																			// //比如:BP.Port.Stations
							url += "&DefaultGroupAttrKey=" + vsM.DefaultGroupAttrKey; // 默认的B实体分组依据.
																						// 比如:FK_Station.

						}

						dr.setValue("Url", url + "&" + en.getPK() + "=" + en.getPKVal() + "&PKVal=" + en.getPKVal());
						dr.setValue("Icon", "../Img/M2M.png");

					}

					dr.setValue("W", "900");
					dr.setValue("H", "500");
					dr.setValue("RefMethodType", RefMethodType.RightFrameOpen);

					// 获得选择的数量.
					try {
						sql = "SELECT COUNT(*) as NUM FROM "
								+ vsM.getEnsOfMM().getNewEntity().getEnMap().getPhysicsTable() + " WHERE "
								+ vsM.getAttrOfOneInMM() + "='" + en.getPKVal() + "'";
						i = DBAccess.RunSQLReturnValInt(sql);
					} catch (java.lang.Exception e) {
						sql = "SELECT COUNT(*) as NUM FROM "
								+ vsM.getEnsOfMM().getNewEntity().getEnMap().getPhysicsTable() + " WHERE "
								+ vsM.getAttrOfOneInMM() + "=" + en.getPKVal();
						try {
							i = DBAccess.RunSQLReturnValInt(sql);
						} catch (java.lang.Exception e2) {
							vsM.getEnsOfMM().getNewEntity().CheckPhysicsTable();
						}
					}
					dr.setValue("Title", vsM.getDesc() + "(" + i + ")");
					dtM.Rows.add(dr);
				}
			}

			/// #endregion 增加 一对多.

			/// #region 从表
			EnDtls enDtls = en.getEnMap().getDtls();
			for (EnDtl enDtl : enDtls) {
				// 判断该dtl是否要显示?
				Entity myEnDtl = enDtl.getEns().getNewEntity(); // 获取他的en
				myEnDtl.SetValByKey(enDtl.getRefKey(), this.getPKVal()); // 给refpk赋值.
				if (myEnDtl.getHisUAC().IsView == false) {
					continue;
				}

				DataRow dr = dtM.NewRow();
				// string url = "Dtl.aspx?EnName=" + this.EnName + "&PK=" +
				// this.PKVal + "&EnsName=" + enDtl.getEnsName() + "&RefKey=" +
				// enDtl.getRefKey() + "&RefVal=" + en.getPKVal().ToString() +
				// "&MainEnsName=" + en.ToString() ;
				String url = "Dtl.htm?EnName=" + this.getEnName() + "&PK=" + this.getPKVal() + "&EnsName="
						+ enDtl.getEnsName() + "&RefKey=" + enDtl.getRefKey() + "&RefVal=" + en.getPKVal().toString()
						+ "&MainEnsName=" + en.toString();
				try {
					i = DBAccess.RunSQLReturnValInt(
							"SELECT COUNT(*) FROM " + enDtl.getEns().getNewEntity().getEnMap().getPhysicsTable()
									+ " WHERE " + enDtl.getRefKey() + "='" + en.getPKVal() + "'");
				} catch (java.lang.Exception e3) {
					try {
						i = DBAccess.RunSQLReturnValInt(
								"SELECT COUNT(*) FROM " + enDtl.getEns().getNewEntity().getEnMap().getPhysicsTable()
										+ " WHERE " + enDtl.getRefKey() + "=" + en.getPKVal());
					} catch (java.lang.Exception e4) {
						enDtl.getEns().getNewEntity().CheckPhysicsTable();
					}
				}

				dr.setValue("No", enDtl.getEnsName());
				dr.setValue("Title", enDtl.getDesc() + "(" + i + ")");
				dr.setValue("Url", url);
				dr.setValue("GroupName", enDtl.getGroupName());

				dr.setValue("RefMethodType", RefMethodType.RightFrameOpen);

				dtM.Rows.add(dr);
			}

			/// #endregion 增加 从表.

			ds.Tables.add(dtM);

			return BP.Tools.Json.ToJson(ds);
		} catch (RuntimeException ex) {
			return "err@Entity_Init错误:" + ex.getMessage();
		}
	}

	/// #endregion 实体的操作.

	/// #region 部门人员模式.
	public final String BranchesAndLeaf_SearchByNodeID() throws Exception {
		String dot2DotEnsName = this.GetRequestVal("Dot2DotEnsName");
		String defaultGroupAttrKey = this.GetRequestVal("DefaultGroupAttrKey");
		String key = this.GetRequestVal("Key"); // 查询关键字.

		String ensOfM = this.GetRequestVal("EnsOfM"); // 多的实体.
		Entities ensMen = ClassFactory.GetEns(ensOfM);
		QueryObject qo = new QueryObject(ensMen); // 集合.
		qo.AddWhere(defaultGroupAttrKey, key);
		qo.DoQuery();

		return ensMen.ToJson();
	}

	public final String BranchesAndLeaf_SearchByKey() throws Exception {
		String dot2DotEnsName = this.GetRequestVal("Dot2DotEnsName");
		String defaultGroupAttrKey = this.GetRequestVal("DefaultGroupAttrKey");

		String key = this.GetRequestVal("Key"); // 查询关键字.

		String ensOfM = this.GetRequestVal("EnsOfM"); // 多的实体.
		Entities ensMen = ClassFactory.GetEns(ensOfM);
		QueryObject qo = new QueryObject(ensMen); // 集合.
		qo.AddWhere("No", " LIKE ", "%" + key + "%");
		qo.addOr();
		qo.AddWhere("Name", " LIKE ", "%" + key + "%");
		qo.DoQuery();

		return ensMen.ToJson();
	}

	public final String BranchesAndLeaf_Delete() throws Exception {
		try {
			String dot2DotEnName = this.GetRequestVal("Dot2DotEnName");
			String AttrOfOneInMM = this.GetRequestVal("AttrOfOneInMM");
			String AttrOfMInMM = this.GetRequestVal("AttrOfMInMM");
			Entity mm = ClassFactory.GetEn(dot2DotEnName);
			mm.Delete(AttrOfOneInMM, this.getPKVal(), AttrOfMInMM, this.GetRequestVal("Key"));
			return "删除成功.";
		} catch (RuntimeException ex) {
			return "err@" + ex.getMessage();
		}
	}

	/**
	 * 初始化
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String BranchesAndLeaf_Init() throws Exception {
		String dot2DotEnsName = this.GetRequestVal("Dot2DotEnsName");
		String defaultGroupAttrKey = this.GetRequestVal("DefaultGroupAttrKey");

		Entity en = ClassFactory.GetEn(this.getEnName());
		en.setPKVal(this.getPKVal());
		en.Retrieve();

		// 找到映射.
		AttrsOfOneVSM oneVsM = en.getEnMap().getAttrsOfOneVSM();
		AttrOfOneVSM vsM = null;
		for (AttrOfOneVSM item : oneVsM) {
			if (item.dot2DotModel == Dot2DotModel.TreeDeptEmp && item.getEnsOfMM().toString().equals(dot2DotEnsName)
					&& item.DefaultGroupAttrKey.equals(defaultGroupAttrKey)) {
				vsM = item;
				break;
			}
		}
		if (vsM == null) {
			return "err@参数错误,没有找到VSM";
		}

		// 组织数据.
		DataSet ds = new DataSet();
		String rootNo = vsM.RootNo;
		if (rootNo.equals("@WebUser.FK_Dept") || rootNo.equals("WebUser.getFK_Dept()")) {
			rootNo = WebUser.getFK_Dept();
		}

		if (rootNo.equals("@WebUser.OrgNo") || rootNo.equals("WebUser.OrgNo"))
			rootNo = WebUser.getOrgNo();

		/// #region 生成树目录.
		String ensOfM = this.GetRequestVal("EnsOfM"); // 多的实体.
		Entities ensMen = ClassFactory.GetEns(ensOfM);
		Entity enMen = ensMen.getNewEntity();

		Attr attr = enMen.getEnMap().GetAttrByKey(defaultGroupAttrKey);
		if (attr == null) {
			return "err@在实体[" + ensOfM + "]指定的分树的属性[" + defaultGroupAttrKey + "]不存在，请确认是否删除了该属性?";
		}

		if (attr.getMyFieldType() == FieldType.Normal) {
			return "err@在实体[" + ensOfM + "]指定的分树的属性[" + defaultGroupAttrKey + "]不能是普通字段，必须是外键或者枚举.";
		}

		Entities trees = attr.getHisFKEns();
		Entity tree = trees.getNewEntity();
		if (DBAccess.IsExitsTableCol(tree.getEnMap().getPhysicsTable(), "Idx") == true
				&& tree.getEnMap().getAttrs().Contains("Idx") == true)
		{
			if(rootNo.equals("0"))
				trees.Retrieve("ParentNo", rootNo, "Idx");
			else
				trees.Retrieve("No", rootNo, "Idx");
		}
		else
		{
			if (rootNo.equals("0"))
				trees.Retrieve("ParentNo", rootNo);
			else
				trees.Retrieve("No", rootNo);
		}

		DataTable dt = trees.ToDataTableField("DBTrees");
		// 如果没有parnetNo 列，就增加上, 有可能是分组显示使用这个模式.
		if (dt.Columns.contains("ParentNo") == false) {
			dt.Columns.Add("ParentNo");
			for (DataRow dr : dt.Rows) {
				dr.setValue("ParentNo", rootNo);
			}
		}
		ds.Tables.add(dt);

		/// #endregion 生成树目录.

		/// #region 生成选择的数据.
		Entities dot2Dots = ClassFactory.GetEns(dot2DotEnsName);
		dot2Dots.Retrieve(vsM.getAttrOfOneInMM(), this.getPKVal());

		DataTable dtSelected = dot2Dots.ToDataTableField("DBMMs");

		String attrOfMInMM = this.GetRequestVal("AttrOfMInMM");
		String AttrOfOneInMM = this.GetRequestVal("AttrOfOneInMM");

		dtSelected.Columns.get(attrOfMInMM).setColumnName("No");

		if (dtSelected.Columns.contains(attrOfMInMM + "Text") == false) {
			return "err@MM实体类字段属性需要按照外键属性编写:" + dot2DotEnsName + " - " + attrOfMInMM;
		}

		dtSelected.Columns.get(attrOfMInMM + "Text").ColumnName = "Name";

		dtSelected.Columns.remove(AttrOfOneInMM);
		ds.Tables.add(dtSelected); // 已经选择的数据.

		/// #endregion 生成选择的数据.

		return BP.Tools.Json.ToJson(ds);
	}

	public String BranchesAndLeaf_GetTreesByParentNo() throws Exception
	{
		String rootNo = GetRequestVal("RootNo");
		if (DataType.IsNullOrEmpty(rootNo))
			rootNo = "0";

		String defaultGroupAttrKey = this.GetRequestVal("DefaultGroupAttrKey");
		String ensOfM = this.GetRequestVal("EnsOfM"); //多的实体.
		Entities ensMen = ClassFactory.GetEns(ensOfM);
		Entity enMen = ensMen.getNewEntity();

		Attr attr = enMen.getEnMap().GetAttrByKey(defaultGroupAttrKey);
		if (attr == null)
			return "err@在实体[" + ensOfM + "]指定的分树的属性[" + defaultGroupAttrKey + "]不存在，请确认是否删除了该属性?";

		if (attr.getMyFieldType() == FieldType.Normal)
			return "err@在实体[" + ensOfM + "]指定的分树的属性[" + defaultGroupAttrKey + "]不能是普通字段，必须是外键或者枚举.";

		Entities trees = attr.getHisFKEns();
		//判断改类是否存在Idx
		Entity tree = trees.getNewEntity();
		if (DBAccess.IsExitsTableCol(tree.getEnMap().getPhysicsTable(), "Idx") == true
				&& tree.getEnMap().getAttrs().Contains("Idx") == true)
			trees.Retrieve("ParentNo",rootNo,"Idx");
		else
			trees.Retrieve("ParentNo", rootNo);

		DataTable dt = trees.ToDataTableField("DBTrees");
		//如果没有parnetNo 列，就增加上, 有可能是分组显示使用这个模式.
		if (dt.Columns.contains("ParentNo") == false) {
			dt.Columns.Add("ParentNo");
			for (DataRow dr : dt.Rows) {
				dr.setValue("ParentNo", rootNo);
			}
		}
		return BP.Tools.Json.ToJson(dt);
	}

	/// #endregion 部门人员模式.

	/// #region 分组数据.
	/**
	 * 执行保存
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String Dot2Dot_Save() throws Exception {

		try {
			String eles = this.GetRequestVal("ElesAAA");

			// 实体集合.
			String dot2DotEnsName = this.GetRequestVal("Dot2DotEnsName");
			String attrOfOneInMM = this.GetRequestVal("AttrOfOneInMM");
			String attrOfMInMM = this.GetRequestVal("AttrOfMInMM");

			// 获得点对点的实体.
			Entity en = ClassFactory.GetEns(dot2DotEnsName).getNewEntity();
			en.Delete(attrOfOneInMM, this.getPKVal()); // 首先删除.

			String[] strs = eles.split("[,]", -1);
			for (String str : strs) {
				if (DataType.IsNullOrEmpty(str) == true) {
					continue;
				}

				en.SetValByKey(attrOfOneInMM, this.getPKVal());
				en.SetValByKey(attrOfMInMM, str);
				en.Insert();
			}
			return "数据保存成功.";
		} catch (RuntimeException ex) {
			return "err@" + ex.getMessage();
		}
	}

	/**
	 * 获得分组的数据源
	 * 
	 * @return
	 * @throws Exception
	 */
	public final String Dot2Dot_GenerGroupEntitis() throws Exception {
		String key = this.GetRequestVal("DefaultGroupAttrKey");

		// 实体集合.
		String ensName = this.GetRequestVal("EnsOfM");
		Entities ens = ClassFactory.GetEns(ensName);
		Entity en = ens.getNewEntity();

		Attrs attrs = en.getEnMap().getAttrs();
		Attr attr = attrs.GetAttrByKey(key);

		if (attr == null) {
			return "err@设置的分组外键错误[" + key + "],不存在[" + ensName + "]或者已经被删除.";
		}

		if (attr.getMyFieldType() == FieldType.Normal) {
			return "err@设置的默认分组[" + key + "]不能是普通字段.";
		}

		if (attr.getMyFieldType() == FieldType.FK) {
			Entities ensFK = attr.getHisFKEns();
			ensFK.clear();
			ensFK.RetrieveAll();
			return ensFK.ToJson();
		}

		if (attr.getMyFieldType() == FieldType.Enum) {
			/* 如果是枚举 */
			SysEnums ses = new SysEnums();
			ses.Retrieve(SysEnumAttr.IntKey, attr.getUIBindKey());
		}

		return "err@设置的默认分组[" + key + "]不能是普通字段.";
	}

	/// #endregion 分组数据.

}