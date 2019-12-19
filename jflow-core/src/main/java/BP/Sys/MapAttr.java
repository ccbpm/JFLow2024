package BP.Sys;

import BP.DA.*;
import BP.Difference.SystemConfig;
import BP.En.*;
import BP.En.Map;
import BP.Tools.DateUtils;
import BP.Web.WebUser;

import java.util.*;
import java.io.*;
import java.math.*;

/** 
 实体属性
*/
public class MapAttr extends EntityMyPK
{

		///#region 文本字段参数属性.
	/** 
	 是否是超大文本？
	 * @throws Exception 
	*/
	public final boolean getIsSupperText() throws Exception
	{
		return this.GetParaBoolen(MapAttrAttr.IsSupperText, false);
	}
	public final void setIsSupperText(boolean value) throws Exception
	{
		this.SetPara(MapAttrAttr.IsSupperText, value);
	}
	/** 
	 是否是富文本？
	 * @throws Exception 
	*/
	public final boolean getIsRichText() throws Exception
	{
		return this.GetParaBoolen(MapAttrAttr.IsRichText, false);
	}
	public final void setIsRichText(boolean value) throws Exception
	{
		this.SetPara(MapAttrAttr.IsRichText, value);
	}
	/** 
	 是否启用二维码？
	 * @throws Exception 
	*/
	public final boolean getIsEnableQrCode() throws Exception
	{
		return this.GetParaBoolen("IsEnableQrCode", false);
	}
	public final void setIsEnableQrCode(boolean value) throws Exception
	{
		this.SetPara("IsEnableQrCode", value);
	}

		///#region 数值字段参数属性,2017-1-9,liuxc
	/** 
	 数值字段是否合计(默认true)
	 * @throws Exception 
	*/
	public final boolean getIsSum() throws Exception
	{
		return this.GetParaBoolen(MapAttrAttr.IsSum, true);
	}
	public final void setIsSum(boolean value) throws Exception
	{
		this.SetPara(MapAttrAttr.IsSum, value);
	}
	public final boolean getExtIsSum() throws Exception
	{
		return this.GetParaBoolen(MapAttrAttr.ExtIsSum, true);
	}
	public final void setExtIsSum(boolean value) throws Exception
	{
		this.SetPara(MapAttrAttr.ExtIsSum, value);
	}


		///#endregion


		///#region 参数属性.
	/** 
	 是否必填字段
	 * @throws Exception 
	*/
	public final boolean getUIIsInput() throws Exception
	{
		return this.GetValBooleanByKey(MapAttrAttr.UIIsInput, false);
	}
	public final void setUIIsInput(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIIsInput, value);
	}
	/** 
	 在手机端中是否显示
	 * @throws Exception 
	*/
	public final boolean getIsEnableInAPP() throws Exception
	{
		return this.GetValBooleanByKey(MapAttrAttr.IsEnableInAPP, true);
	}
	public final void setIsEnableInAPP(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.IsEnableInAPP, value);
	}
	/** 
	 是否启用高级JS设置
	 * @throws Exception 
	*/
	public final boolean getIsEnableJS() throws Exception
	{
		return this.GetParaBoolen("IsEnableJS", false);
	}
	public final void setIsEnableJS(boolean value) throws Exception
	{
		this.SetPara("IsEnableJS", value);
	}

		///#endregion


		///#region 属性
	public EntitiesNoName _ens = null;
	/** 
	 实体类
	 * @throws Exception 
	*/
	public final EntitiesNoName getHisEntitiesNoName() throws Exception
	{
		if (this.getUIBindKey().contains("."))
		{
			EntitiesNoName ens = (EntitiesNoName)BP.En.ClassFactory.GetEns(this.getUIBindKey());
			if (ens == null)
			{
				return null;
			}

			ens.RetrieveAll();
			return ens;
		}

		if (_ens == null)
		{
			SFTable sf = new SFTable(this.getUIBindKey());

			if (sf.getFK_SFDBSrc().equals("local"))
			{
				GENoNames myens = new GENoNames(this.getUIBindKey(), this.getName());

				if (sf.getSrcType() == SrcType.SQL)
				{
						//此种类型时，没有物理表或视图，从SQL直接查出数据
					DataTable dt = sf.GenerHisDataTable();
					EntityNoName enn = null;
					for (DataRow row : dt.Rows)
					{
						BP.En.Entity tempVar = myens.getNewEntity();
						enn = tempVar instanceof EntityNoName ? (EntityNoName)tempVar : null;
						enn.setNo(row.getValue("No") instanceof String ? (String)row.getValue("No") : null);
						enn.setName(row.getValue("Name") instanceof String ? (String)row.getValue("Name") : null);

						myens.AddEntity(enn);
					}
				}
				else
				{
					myens.RetrieveAll();
				}

				_ens = myens;
			}
			else
			{
				GENoNames myens = new GENoNames(this.getUIBindKey(), this.getName());
				_ens = myens;
					//throw new Exception("@非实体类实体不能获取EntitiesNoName。");
			}
		}
		return _ens;
	}

	private DataTable _dt = null;
	/** 
	 外部数据表
	 * @throws Exception 
	*/
	public final DataTable getHisDT() throws Exception
	{
		if (_dt == null)
		{
			if (DataType.IsNullOrEmpty(this.getUIBindKey()))
			{
				throw new RuntimeException("@属性：" + this.getMyPK() + " 丢失属性 UIBindKey 字段。");
			}

			SFTable sf = new SFTable(this.getUIBindKey());
			_dt = sf.GenerHisDataTable();
		}
		return _dt;
	}
	/** 
	 是否是导入过来的字段
	 * @throws Exception 
	*/
	public final boolean getIsTableAttr() throws Exception
	{
		return DataType.IsNumStr(this.getKeyOfEn().replace("F", ""));
	}
	/** 
	 转换成属性.
	 * @throws Exception 
	*/
	public final Attr getHisAttr() throws Exception
	{
		Attr attr = new Attr();
		attr.setKey(this.getKeyOfEn());
		attr.setDesc(this.getName());

		String s = this.getDefValReal();
		if (DataType.IsNullOrEmpty(s))
		{
			attr.setDefaultValOfReal(null);
		}
		else
		{
			attr.setDefaultValOfReal(this.getDefValReal());
		}


		attr.setField(this.getField());
		attr.setMaxLength(this.getMaxLen());
		attr.setMinLength(this.getMinLen());
		attr.setUIBindKey(this.getUIBindKey());
		attr.UIIsLine = this.getUIIsLine();
		attr.setUIHeight(0);
		if (this.getUIHeight() > 30)
		{
			attr.setUIHeight((int)this.getUIHeight());
		}

		attr.setUIWidth(this.getUIWidth());
		attr.setMyDataType(this.getMyDataType());
		attr.setUIRefKeyValue(this.getUIRefKey());
		attr.setUIRefKeyText(this.getUIRefKeyText());
		attr.setUIVisible(this.getUIVisible());
		attr.setMyFieldType(FieldType.Normal); //普通类型的字段.
		if (this.getIsPK())
		{
			attr.setMyFieldType(FieldType.PK);
		}
		switch (this.getLGType())
		{
			case Enum:
				attr.setUIContralType(this.getUIContralType());
				attr.setMyFieldType(FieldType.Enum);
				attr.setUIIsReadonly(this.getUIIsEnable());
				break;
			case FK:
				attr.setUIContralType(this.getUIContralType());
				attr.setMyFieldType(FieldType.FK);
				
				attr.setUIIsReadonly(this.getUIIsEnable());
				break;
			default:

				if (this.getIsPK())
				{
					attr.setMyFieldType(FieldType.PK);
				}

				attr.setUIIsReadonly(!this.getUIIsEnable());
				switch (this.getMyDataType())
				{
					case DataType.AppBoolean:
						attr.setUIContralType(UIContralType.CheckBok);
						attr.setUIIsReadonly(this.getUIIsEnable());
						break;
					case DataType.AppDate:
						if (this.getTag().equals("1"))
						{
							attr.setDefaultVal(DataType.getCurrentDate());
						}
						break;
					case DataType.AppDateTime:
						if (this.getTag().equals("1"))
						{
							attr.setDefaultVal(DataType.getCurrentDate());
						}
						break;
					default:
						attr.setUIContralType(this.getUIContralType());
						break;
				}
				break;
		}

		return attr;
	}
	/** 
	 是否主键
	 * @throws Exception 
	*/
	public final boolean getIsPK() throws Exception
	{
		switch (this.getKeyOfEn())
		{
			case "OID":
			case "No":
			case "MyPK":
				return true;
			default:
				return false;
		}
	}
	/** 
	 编辑类型
	 * @throws Exception 
	*/
	public final EditType getHisEditType() throws Exception
	{
		return EditType.forValue(this.GetValIntByKey(MapAttrAttr.EditType));
	}
	public final void setHisEditType(EditType value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.EditType, value.getValue());
	}
	/** 
	 表单ID
	 * @throws Exception 
	*/
	public final String getFK_MapData() throws Exception
	{
		return this.GetValStrByKey(MapAttrAttr.FK_MapData);
	}
	public final void setFK_MapData(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.FK_MapData, value);
	}
	/** 
	 字段名
	 * @throws Exception 
	*/
	public final String getKeyOfEn() throws Exception
	{
		return this.GetValStrByKey(MapAttrAttr.KeyOfEn);
	}
	public final void setKeyOfEn(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.KeyOfEn, value);
	}
	public final FieldTypeS getLGType() throws Exception
	{
		return FieldTypeS.forValue(this.GetValIntByKey(MapAttrAttr.LGType));
	}
	public final void setLGType(FieldTypeS value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.LGType, value.getValue());
	}
	public final String getLGTypeT() throws Exception
	{
		return this.GetValRefTextByKey(MapAttrAttr.LGType);
	}
	/** 
	 描述
	 * @throws Exception 
	*/
	public final String getName() throws Exception
	{
		String s = this.GetValStrByKey(MapAttrAttr.Name);
		if (s == null || s.equals(""))
		{
			return this.getKeyOfEn();
		}
		return s;
	}
	public final void setName(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.Name, value);
	}
	public final boolean getIsNum() throws Exception
	{
		switch (this.getMyDataType())
		{
			case BP.DA.DataType.AppString:
			case BP.DA.DataType.AppDate:
			case BP.DA.DataType.AppDateTime:
			case BP.DA.DataType.AppBoolean:
				return false;
			default:
				return true;
		}
	}
	public final BigDecimal getDefValDecimal() throws Exception
	{
		return new java.math.BigDecimal(this.getDefVal());
	}
	public final String getDefValReal() throws Exception
	{
		return this.GetValStrByKey(MapAttrAttr.DefVal);
	}
	public final void setDefValReal(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.DefVal, value);
	}
	/** 
	 合并单元格数
	 * @throws Exception 
	*/
	public final int getColSpan() throws Exception
	{
		int i = this.GetValIntByKey(MapAttrAttr.ColSpan);
		if (this.getUIIsLine() && i == 1)
		{
			return 3;
		}
		if (i == 0)
		{
			return 1;
		}
		return i;
	}
	public final void setColSpan(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.ColSpan, value);
	}
	/** 
	 默认值
	 * @throws Exception 
	*/
	public final String getDefVal() throws Exception
	{
		String s = this.GetValStrByKey(MapAttrAttr.DefVal);
		if (this.getIsNum())
		{
			if (s.equals(""))
			{
				return "0";
			}
		}

		switch (this.getMyDataType())
		{
			case BP.DA.DataType.AppDate:
				if (this.getTag().equals("1") || s.equals("@RDT"))
					return DataType.getCurrentDate();
				else
					return "          ";
				
			case BP.DA.DataType.AppDateTime:
				if (this.getTag().equals("1") || s.equals("@RDT"))
					return DataType.getCurrentDataTime();
				else
					return "               ";
			default:
				break;
		}

		if (s.contains("@") == false)
		{
			return s;
		}

		switch (s.toLowerCase())
		{
			case "@WebUser.No":
				return WebUser.getNo();
			case "@WebUser.Name":
				return WebUser.getName();
			case "@WebUser.FK_Dept":
				return WebUser.getFK_Dept();
			case "@WebUser.FK_Deptname":
				return WebUser.getFK_DeptName();
			case "@WebUser.FK_Deptfullname":
				return WebUser.getFK_DeptNameOfFull();
			case "@fk_ny":
				return DataType.getCurrentYearMonth();
			case "@fk_nd":
				return DataType.getCurrentYear();
			case "@fk_yf":
				return DataType.getCurrentMonth();
			case "@rdt":
				if (this.getMyDataType() == DataType.AppDate)
				{
					return DataType.getCurrentDate();
				}
				else
				{
					return DataType.getCurrentDataTime();
				}
			case "@rd":
				if (this.getMyDataType() == DataType.AppDate)
				{
					return DataType.getCurrentDate();
				}
				else
				{
					return DataType.getCurrentDataTime();
				}
			case "@yyyy年MM月dd日":
				return DataType.getCurrentDataCNOfLong();
			case "@yyyy年MM月dd日hh时mm分":
				return DateUtils.format(new Date(),"yyyy年MM月dd日HH时mm分");
			case "@yy年MM月dd日":
				return DataType.getCurrentDataCNOfShort();
			case "@yy年MM月dd日hh时mm分":
				return DateUtils.format(new Date(),"yy年MM月dd日HH时mm分");
			default:
				return s;
		}
	}
	public final void setDefVal(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.DefVal, value);
	}
	public final boolean getDefValOfBool() throws Exception
	{
		return this.GetValBooleanByKey(MapAttrAttr.DefVal, false);
	}
	public final void setDefValOfBool(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.DefVal, value);
	}
	/** 
	 字段
	*/
	public final String getField() throws Exception
	{
		return this.getKeyOfEn();
	}
	public final BP.Web.Controls.TBType getHisTBType() throws Exception
	{
		switch (this.getMyDataType()) 
		{
			case BP.DA.DataType.AppMoney:
				return BP.Web.Controls.TBType.Moneny;
			case BP.DA.DataType.AppInt:
			case BP.DA.DataType.AppFloat:
			case BP.DA.DataType.AppDouble:
				return BP.Web.Controls.TBType.Num;
			default:
				return BP.Web.Controls.TBType.TB;
		}
	}
	public final int getMyDataType() throws Exception
	{
		return this.GetValIntByKey(MapAttrAttr.MyDataType);
	}
	public final void setMyDataType(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.MyDataType, value);
	}
	public final String getMyDataTypeS() throws Exception 
	{
		switch (this.getMyDataType())
		{
			case DataType.AppString:
				return "String";
			case DataType.AppInt:
				return "Int";
			case DataType.AppFloat:
				return "Float";
			case DataType.AppMoney:
				return "Money";
			case DataType.AppDate:
				return "Date";
			case DataType.AppDateTime:
				return "DateTime";
			case DataType.AppBoolean:
				return "Bool";
			default:
				throw new RuntimeException("没有判断。");
		}
	}
	public final void setMyDataTypeS(String value) throws Exception
	{

		switch (value)
		{
			case "String":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppString);
				break;
			case "Int":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppInt);
				break;
			case "Float":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppFloat);
				break;
			case "Money":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppMoney);
				break;
			case "Date":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppDate);
				break;
			case "DateTime":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppDateTime);
				break;
			case "Bool":
				this.SetValByKey(MapAttrAttr.MyDataType, DataType.AppBoolean);
				break;
			default:
				throw new RuntimeException("sdsdsd");
		}

	}
	public final String getMyDataTypeStr() throws Exception
	{
		return DataType.GetDataTypeDese(this.getMyDataType());
	}
	/** 
	 最大长度
	*/
	public final int getMaxLen() throws Exception
	{
		switch (this.getMyDataType())
		{
			case DataType.AppDate:
				return 100;
			case DataType.AppDateTime:
				return 100;
			default:
				break;
		}

		int i = this.GetValIntByKey(MapAttrAttr.MaxLen);
		if (i > 4000)
		{
			i = 4000;
		}
		if (i == 0)
		{
			return 50;
		}
		return i;
	}
	public final void setMaxLen(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.MaxLen, value);
	}
	/** 
	 最小长度
	*/
	public final int getMinLen() throws Exception
	{
		return this.GetValIntByKey(MapAttrAttr.MinLen);
	}
	public final void setMinLen(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.MinLen, value);
	}
	/** 
	 是否可以为空, 对数值类型的数据有效.
	 * @throws Exception 
	*/
	public final boolean getIsNull() throws Exception
	{
		if (this.getMinLen() == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	/** 
	 所在的分组
	*/
	public final long getGroupID() throws Exception
	{
		String str = this.GetValStringByKey(MapAttrAttr.GroupID);
		if (str.equals("无") || str.equals(""))
		{
			return 1;
		}
		return Long.parseLong(str);
	}
	public final void setGroupID(long value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.GroupID, value);
	}
	/** 
	 是否是大块文本？
	 * @throws Exception 
	*/
	public final boolean getIsBigDoc() throws Exception
	{
		if (this.getUIRows() > 1 && this.getMyDataType() == DataType.AppString)
		{
			return true;
		}

		return false;
	}
	/** 
	 textbox控件的行数.
	*/
	public final int getUIRows() throws Exception
	{
		if (this.getUIHeight() < 40)
			return 1;
		
		java.math.BigDecimal d = new java.math.BigDecimal((new Float(this.getUIHeight())).toString());
		java.math.BigDecimal c = new java.math.BigDecimal(13);
		return d.divide(c, 0, RoundingMode.HALF_UP).intValue();
	}
	/** 
	 高度
	*/
	public final int getUIHeightInt() throws Exception
	{
		return (int)this.getUIHeight();
	}
	public final void setUIHeightInt(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIHeight, value);
	}
	/** 
	 高度
	*/
	public final float getUIHeight() throws Exception
	{
		return this.GetValFloatByKey(MapAttrAttr.UIHeight);
	}
	public final void setUIHeight(float value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIHeight, value);
	}
	/** 
	 宽度
	*/
	public final int getUIWidthInt() throws Exception
	{
		return (int)this.getUIWidth();
	}
	public final void setUIWidthInt(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIWidth, value);
	}
	/** 
	 宽度
	*/
	public final float getUIWidth() throws Exception
	{
		return this.GetValFloatByKey(MapAttrAttr.UIWidth);
	}
	public final void setUIWidth(float value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIWidth, value);
	}
	public final int getUIWidthOfLab()
	{
		return 0;
	}
	/** 
	 是否是否可见？
	*/
	public final boolean getUIVisible() throws Exception
	{
		return this.GetValBooleanByKey(MapAttrAttr.UIVisible);
	}
	public final void setUIVisible(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIVisible, value);
	}
	/** 
	 是否可用
	*/
	public final boolean getUIIsEnable() throws Exception
	{
		return this.GetValBooleanByKey(MapAttrAttr.UIIsEnable);
	}
	public final void setUIIsEnable(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIIsEnable, value);
	}
	/** 
	 是否单独行显示
	*/
	public final boolean getUIIsLine() throws Exception
	{
		return this.GetValBooleanByKey(MapAttrAttr.UIIsLine);
	}
	public final void setUIIsLine(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIIsLine, value);
	}
	/** 
	 是否数字签名
	*/
	public final boolean getIsSigan() throws Exception
	{
		if (this.getUIIsEnable())
		{
			return false;
		}
		return this.GetValBooleanByKey(MapAttrAttr.IsSigan);
	}
	public final void setIsSigan(boolean value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.IsSigan, value);
	}
 /** 
	 签名类型
 */
	public final SignType getSignType() throws Exception
	{
		return SignType.forValue(this.GetValIntByKey(MapAttrAttr.IsSigan));
	}
	public final void setSignType(SignType value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.IsSigan, value.getValue());
	}
	public final int getPara_FontSize() throws Exception
	{
		return this.GetParaInt(MapAttrAttr.FontSize);
	}
	public final void setPara_FontSize(int value)throws Exception
	{
		this.SetPara(MapAttrAttr.FontSize, value);
	}
	/** 
	 radiobutton的展现方式
	*/
	public final int getRBShowModel() throws Exception
	{
		return this.GetParaInt("RBShowModel");
	}
	public final void setRBShowModel(int value) throws Exception
	{
		this.SetPara("RBShowModel", value);
	}
	/** 
	 操作提示
	*/
	public final String getPara_Tip() throws Exception
	{
		return this.GetParaString(MapAttrAttr.Tip);
	}
	public final void setPara_Tip(String value) throws Exception
	{
		this.SetPara(MapAttrAttr.Tip, value);
	}
	/** 
	 是否数字签名
	*/
	public final String getPara_SiganField() throws Exception
	{
		if (this.getUIIsEnable())
		{
			return "";
		}
		return this.GetParaString(MapAttrAttr.SiganField);
	}
	public final void setPara_SiganField(String value) throws Exception
	{
		this.SetPara(MapAttrAttr.SiganField, value);
	}
	/** 
	 签名类型
	 * @throws Exception 
	*/
	public final PicType getPicType() throws Exception
	{
		if (this.getUIIsEnable())
		{
			return PicType.Auto;
		}
		return PicType.forValue(this.GetParaInt(MapAttrAttr.PicType));
	}
	public final void setPicType(PicType value) throws Exception
	{
		this.SetPara(MapAttrAttr.PicType, value.getValue());
	}
	/** 
	 TextBox类型
	 * @throws Exception 
	*/
	public final TBModel getTBModel() throws Exception
	{
		return TBModel.forValue(this.GetParaInt(MapAttrAttr.TBModel));
	}
	public final void setTBModel(TBModel value) throws Exception
	{
		this.SetPara(MapAttrAttr.TBModel, value.getValue());
	}
	/** 
	 绑定的值
	 * @throws Exception 
	*/
	public final String getUIBindKey() throws Exception
	{
		return this.GetValStrByKey(MapAttrAttr.UIBindKey);
	}
	public final void setUIBindKey(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIBindKey, value);
	}
	/** 
	 关联的表的Key
	 * @throws Exception 
	*/
	public final String getUIRefKey() throws Exception
	{
		String s = this.GetValStrByKey(MapAttrAttr.UIRefKey);
		if (s == null || s.equals(""))
		{
			s = "No";
		}
		return s;
	}
	public final void setUIRefKey(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIRefKey, value);
	}
	/** 
	 关联的表的Lab
	 * @throws Exception 
	*/
	public final String getUIRefKeyText() throws Exception
	{
		String s = this.GetValStrByKey(MapAttrAttr.UIRefKeyText);
		if ( s == null || s.equals(""))
		{
			s = "Name";
		}
		return s;
	}
	public final void setUIRefKeyText(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIRefKeyText, value);
	}
	/** 
	 标识
	 * @throws Exception 
	*/
	public final String getTag() throws Exception
	{
		return this.GetValStrByKey(MapAttrAttr.Tag);
	}
	public final void setTag(String value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.Tag, value);
	}
	/** 
	 控件类型
	 * @throws Exception 
	*/
	public final UIContralType getUIContralType() throws Exception
	{
		return UIContralType.forValue(this.GetValIntByKey(MapAttrAttr.UIContralType));
	}
	public final void setUIContralType(UIContralType value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.UIContralType, value.getValue());
	}
	public final String getF_Desc() throws Exception
	{
		switch (this.getMyDataType())
		{
			case DataType.AppString:
				if (this.getUIVisible() == false)
				{
					return "长度" + this.getMinLen() + "-" + this.getMaxLen() + "不可见";
				}
				else
				{
					return "长度" + this.getMinLen() + "-" + this.getMaxLen();
				}
			case DataType.AppDate:
			case DataType.AppDateTime:
			case DataType.AppInt:
			case DataType.AppFloat:
			case DataType.AppMoney:
				if (this.getUIVisible() == false)
				{
					return "不可见";
				}
				else
				{
					return "";
				}
			default:
				return "";
		}
	}
	/** 
	 TabIdx
	 * @throws Exception 
	*/
	public final int getTabIdx() throws Exception
	{
		return this.GetValIntByKey(MapAttrAttr.TabIdx);
	}
	public final void setTabIdx(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.TabIdx, value);
	}
	/** 
	 序号
	 * @throws Exception 
	*/
	public final int getIdx() throws Exception
	{
		return this.GetValIntByKey(MapAttrAttr.Idx);
	}
	public final void setIdx(int value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.Idx, value);
	}

	public final float getX() throws Exception
	{
		return this.GetValFloatByKey(MapAttrAttr.X);
	}
	public final void setX(float value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.X, value);
	}
	public final float getY() throws Exception
	{
		return this.GetValFloatByKey(MapAttrAttr.Y);
	}
	public final void setY(float value) throws Exception
	{
		this.SetValByKey(MapAttrAttr.Y, value);
	}

	/** 
	 实体属性
	*/
	public MapAttr()
	{
	}
	public MapAttr(String mypk) throws Exception
	{
		this.setMyPK(mypk);
		this.Retrieve();
	}
	public MapAttr(String fk_mapdata, String key) throws Exception
	{
		this.setFK_MapData(fk_mapdata);
		this.setKeyOfEn(key);
		this.Retrieve(MapAttrAttr.FK_MapData, this.getFK_MapData(), MapAttrAttr.KeyOfEn, this.getKeyOfEn());
	}
	public final int getTextColSpan() throws Exception {
		int i = this.GetValIntByKey(MapAttrAttr.TextColSpan);
		if (this.getUIIsLine() && i == 1) {
			return 3;
		}
		
		return i;
	}
	public final int getRowSpan() throws Exception {
		int i = this.GetValIntByKey(MapAttrAttr.RowSpan);
		if (this.getUIIsLine() && i == 1) {
			return 3;
		}
		
		return i;
	}
	/** 
	 EnMap
	*/
	@Override
	public Map getEnMap()
	{
		if (this.get_enMap() != null)
		{
			return this.get_enMap();
		}

		Map map = new Map("Sys_MapAttr", "实体属性");
		map.Java_SetDepositaryOfEntity(Depositary.None);
		map.Java_SetDepositaryOfMap(Depositary.Application);
		map.Java_SetEnType(EnType.Sys);

		map.AddMyPK();

		map.AddTBString(MapAttrAttr.FK_MapData, null, "实体标识", true, true, 1, 100, 20);
		map.AddTBString(MapAttrAttr.KeyOfEn, null, "属性", true, true, 1, 200, 20);

		map.AddTBString(MapAttrAttr.Name, null, "描述", true, false, 0, 200, 20);
		map.AddTBString(MapAttrAttr.DefVal, null, "默认值", false, false, 0, 400, 20);


		map.AddTBInt(MapAttrAttr.UIContralType, 0, "控件", true, false);
		map.AddTBInt(MapAttrAttr.MyDataType, 1, "数据类型", true, false);

		map.AddDDLSysEnum(MapAttrAttr.LGType, 0, "逻辑类型", true, false, MapAttrAttr.LGType, "@0=普通@1=枚举@2=外键@3=打开系统页面");

		map.AddTBFloat(MapAttrAttr.UIWidth, 100, "宽度", true, false);
		map.AddTBFloat(MapAttrAttr.UIHeight, 23, "高度", true, false);

		map.AddTBInt(MapAttrAttr.MinLen, 0, "最小长度", true, false);
		map.AddTBInt(MapAttrAttr.MaxLen, 300, "最大长度", true, false);

		map.AddTBString(MapAttrAttr.UIBindKey, null, "绑定的信息", true, false, 0, 100, 20);
		map.AddTBString(MapAttrAttr.UIRefKey, null, "绑定的Key", true, false, 0, 30, 20);
		map.AddTBString(MapAttrAttr.UIRefKeyText, null, "绑定的Text", true, false, 0, 30, 20);


		map.AddTBInt(MapAttrAttr.ExtIsSum, 0, "是否显示合计(对从表有效)", true, true);
		map.AddTBInt(MapAttrAttr.UIVisible, 1, "是否可见", true, true);
		map.AddTBInt(MapAttrAttr.UIIsEnable, 1, "是否启用", true, true);
		map.AddTBInt(MapAttrAttr.UIIsLine, 0, "是否单独栏显示", true, true);
		map.AddTBInt(MapAttrAttr.UIIsInput, 0, "是否必填字段", true, true);

		map.AddTBInt(MapAttrAttr.IsRichText, 0, "富文本", true, true);
		map.AddTBInt(MapAttrAttr.IsSupperText, 0, "富文本", true, true);
		map.AddTBInt(MapAttrAttr.FontSize, 0, "富文本", true, true);

			// 是否是签字，操作员字段有效。2010-09-23 增加。 @0=无@1=图片签名@2=CA签名.
		map.AddTBInt(MapAttrAttr.IsSigan, 0, "签字？", true, false);

		map.AddTBFloat(MapAttrAttr.X, 5, "X", true, false);
		map.AddTBFloat(MapAttrAttr.Y, 5, "Y", false, false);
		map.AddTBString(MapAttrAttr.GUID, null, "GUID", true, false, 0, 128, 20);

		map.AddTBInt(MapAttrAttr.EditType, 0, "编辑类型", true, false);

		map.AddTBString(MapAttrAttr.Tag, null, "标识", true, false, 0, 100, 20);
		map.AddTBString(MapAttrAttr.Tag1, null, "标识1", true, false, 0, 100, 20);
		map.AddTBString(MapAttrAttr.Tag2, null, "标识2", true, false, 0, 100, 20);
		map.AddTBString(MapAttrAttr.Tag3, null, "标识3", true, false, 0, 100, 20);


		map.AddTBString(MapAttrAttr.Tip, null, "激活提示", false, true, 0, 200, 20);

			//单元格数量。2013-07-24 增加。
		  //  map.AddTBString(MapAttrAttr.ColSpan, "1", "单元格数量", true, false, 0, 3, 3);
		map.AddTBInt(MapAttrAttr.ColSpan, 1, "单元格数量", true, false);

			//文本占单元格数量
		map.AddTBInt(MapAttrAttr.TextColSpan, 1, "文本单元格数量", true, false);

			//文本跨行
		map.AddTBInt(MapAttrAttr.RowSpan, 1, "行数", true, false);


			//显示的分组. @sly.
		map.AddTBInt(MapAttrAttr.GroupID, 1, "显示的分组", true, false);

		map.AddBoolean(MapAttrAttr.IsEnableInAPP, true, "是否在移动端中显示", true, true);
		map.AddTBInt(MapAttrAttr.Idx, 0, "序号", true, false);

			//参数属性.
		map.AddTBAtParas(4000);

		this.set_enMap(map);
		return this.get_enMap();
	}

		///#endregion



	@Override
	protected void afterInsert() throws Exception
	{
		super.afterInsert();
	}
	/** 
	 保存大块html文本
	 
	 @return 
	 * @throws Exception 
	*/
	public final String SaveBigNoteHtmlText(String text) throws Exception
	{
		String file = SystemConfig.getPathOfDataUser() + "/CCForm/BigNoteHtmlText/" + this.getFK_MapData() + ".htm";
		//若文件夹不存在，则创建
		String folder = (new File(file)).getParent();
		if ((new File(folder)).isDirectory() == false)
		{
			(new File(folder)).mkdirs();
		}

		BP.DA.DataType.WriteFile(file, text);
		return "保存成功！";
	}
	/** 
	 读取大块html文本
	 
	 @return 
	 * @throws Exception 
	*/
	public final String ReadBigNoteHtmlText() throws Exception
	{
		String doc = "";
		String file = SystemConfig.getPathOfDataUser() + "/CCForm/BigNoteHtmlText/" + this.getFK_MapData() + ".htm";
		String folder = (new File(file)).getParent();
		if ((new File(folder)).isDirectory() != false)
		{
			if ((new File(file)).isFile())
			{
				doc = BP.DA.DataType.ReadTextFile(file);

			}
		}

		return doc;
	}
	public final void DoDownTabIdx() throws Exception
	{
		this.DoOrderDown(MapAttrAttr.FK_MapData, this.getFK_MapData(), MapAttrAttr.Idx);
	}
	public final void DoUpTabIdx() throws Exception
	{
		this.DoOrderUp(MapAttrAttr.FK_MapData, this.getFK_MapData(), MapAttrAttr.Idx);
	}
	public final String DoUp() throws Exception
	{
		this.DoOrderUp(MapAttrAttr.GroupID, String.valueOf(this.getGroupID()), MapAttrAttr.UIVisible, "1", MapAttrAttr.Idx);

		MapAttr attr = new MapAttr();
		attr.setMyPK(this.getFK_MapData() + "_Title");
		if (attr.RetrieveFromDBSources() == 1)
		{
		  //  attr.setIdx(-1;
			attr.Update("Idx", -1);
		}
		return "执行成功";
	}

	/** 
	 生成他的外键字典数据,转化为json.
	 
	 @return 
	 * @throws Exception 
	*/
	public final String GenerHisFKData() throws Exception
	{
		SFTable sf = new SFTable(this.getUIBindKey());
		return BP.Tools.Json.ToJson(sf.GenerHisDataTable());
	}

	/** 
	 下移
	 * @throws Exception 
	*/
	public final String DoDown() throws Exception
	{
		this.DoOrderDown(MapAttrAttr.GroupID, String.valueOf(this.getGroupID()), MapAttrAttr.UIVisible, "1", MapAttrAttr.Idx);

		MapAttr attr = new MapAttr();
		attr.setMyPK(this.getFK_MapData() + "_Title");
		if (attr.RetrieveFromDBSources() == 1)
		{
			attr.Update("Idx", -1);
		}
		return "执行成功";
	}
	/** 
	 上移for 明细表.
	 * @throws Exception 
	*/
	public final String DoUpForMapDtl() throws Exception
	{
		//规整groupID.
		GroupField gf = new GroupField();
		gf.Retrieve(GroupFieldAttr.FrmID, this.getFK_MapData());
		BP.DA.DBAccess.RunSQL("UPDATE Sys_MapAttr SET GroupID=" + gf.getOID() + " WHERE FK_MapData='" + this.getFK_MapData() + "'");

		this.DoOrderUp(MapAttrAttr.FK_MapData, this.getFK_MapData(), MapAttrAttr.UIVisible, "1", MapAttrAttr.Idx);

		MapAttr attr = new MapAttr();
		attr.setMyPK(this.getFK_MapData() + "_Title");
		if (attr.RetrieveFromDBSources() == 1)
		{
			//  attr.setIdx(-1;
			attr.Update("Idx", -1);
		}
		return "执行成功";
	}
	/** 
	 下移 for 明细表.
	 * @throws Exception 
	*/
	public final String DoDownForMapDtl() throws Exception
	{
		//规整groupID.
		GroupField gf = new GroupField();
		gf.Retrieve(GroupFieldAttr.FrmID, this.getFK_MapData());
		BP.DA.DBAccess.RunSQL("UPDATE Sys_MapAttr SET GroupID=" + gf.getOID() + " WHERE FK_MapData='" + this.getFK_MapData() + "'");

		this.DoOrderDown(MapAttrAttr.FK_MapData, this.getFK_MapData(), MapAttrAttr.UIVisible, "1", MapAttrAttr.Idx);

		MapAttr attr = new MapAttr();
		attr.setMyPK(this.getFK_MapData() + "_Title");
		if (attr.RetrieveFromDBSources() == 1)
		{
			attr.Update("Idx", -1);
		}

		return "执行成功";
	}
	public final void DoJump(MapAttr attrTo) throws Exception
	{
		if (attrTo.getIdx() <= this.getIdx())
		{
			this.DoJumpUp(attrTo);
		}
		else
		{
			this.DoJumpDown(attrTo);
		}
	}
	private String DoJumpUp(MapAttr attrTo) throws Exception
	{
		String sql = "UPDATE Sys_MapAttr SET Idx=Idx+1 WHERE Idx <=" + attrTo.getIdx() + " AND FK_MapData='" + this.getFK_MapData() + "' AND GroupID=" + this.getGroupID();
		DBAccess.RunSQL(sql);
		this.setIdx(attrTo.getIdx() - 1);
		this.setGroupID(attrTo.getGroupID());beforeInsert
		this.Update();
		return null;
	}
	private String DoJumpDown(MapAttr attrTo) throws Exception
	{
		String sql = "UPDATE Sys_MapAttr SET Idx=Idx-1 WHERE Idx <=" + attrTo.getIdx() + " AND FK_MapData='" + this.getFK_MapData() + "' AND GroupID=" + this.getGroupID();
		DBAccess.RunSQL(sql);
		this.setIdx(attrTo.getIdx() + 1);
		this.setGroupID(attrTo.getGroupID());
		this.Update();
		return null;
	}
	@Override
	protected boolean beforeUpdateInsertAction() throws Exception
	{

		//added by liuxc,2016-12-2
		//判断当前属性是否有分组，没有分组，则自动创建一个分组，并关联
		if (String.valueOf(this.getGroupID()).equals("1"))
		{
			//查找分组，查找到的第一个分组，关联当前属性
			GroupField group = new GroupField();
			if (group.Retrieve(GroupFieldAttr.FrmID, this.getFK_MapData()) > 0)
			{
				this.setGroupID(group.getOID());
			}
			else
			{
				group.setFrmID(this.getFK_MapData());
				group.setLab("基础信息");
				group.setIdx(1);
				group.Insert();

				this.setGroupID(group.getOID());
			}
		}

		if (this.getLGType() == FieldTypeS.Enum && this.getUIContralType() == UIContralType.RadioBtn)
		{
			String sql = "UPDATE Sys_FrmRB SET UIIsEnable=" + this.GetValIntByKey(MapAttrAttr.UIIsEnable) + " WHERE FK_MapData='" + this.getFK_MapData() + "' AND KeyOfEn='" + this.getKeyOfEn() + "'";
			DBAccess.RunSQL(sql);
		}

		//为日期类型固定宽度.
		if (this.getMyDataType() == DataType.AppDate)
		{
			this.setUIWidth(125);
		}
		if (this.getMyDataType() == DataType.AppDateTime)
		{
			this.setUIWidth(145);
		}

		return super.beforeUpdateInsertAction();
	}
	@Override
	protected boolean beforeUpdate() throws Exception
	{
		switch (this.getMyDataType())
		{
			case DataType.AppDateTime:
				this.setMaxLen(20);
				break;
			case DataType.AppDate:
				this.setMaxLen(10);
				break;
			default:
				break;
		}

		if (DataType.IsNullOrEmpty(this.getKeyOfEn()))
		{
			this.setMyPK(this.getFK_MapData());
		}
		else
		{
			this.setMyPK(this.getFK_MapData() + "_" + this.getKeyOfEn());
		}

		return super.beforeUpdate();
	}
	/** 
	 插入之间需要做的事情.
	 
	 @return 
	 * @throws Exception 
	*/
	@Override
	protected boolean beforeInsert() throws Exception
	{
		if (DataType.IsNullOrEmpty(this.getName()))
		{
			throw new RuntimeException("@请输入字段名称。");
		}

		if (this.getKeyOfEn() == null || this.getKeyOfEn().trim().equals(""))
		{
			try
			{
				this.setKeyOfEn(CCFormAPI.ParseStringToPinyinField(this.getName(), true, true, 100));

				if (this.getKeyOfEn().length() > 20)
				{
					this.setKeyOfEn(CCFormAPI.ParseStringToPinyinField(this.getName(), false, true, 20));
				}

				if (this.getKeyOfEn() == null || this.getKeyOfEn().trim().equals(""))
				{
					throw new RuntimeException("@请输入字段描述或者字段名称。");
				}
			}
			catch (RuntimeException ex)
			{
				throw new RuntimeException("@请输入字段描述或字段名称，异常信息:" + ex.getMessage());
			}
		}
		else
		{
			this.setKeyOfEn(PubClass.DealToFieldOrTableNames(this.getKeyOfEn()));
		}

		Object tempVar = this.getKeyOfEn();
		String keyofenC = tempVar instanceof String ? (String)tempVar : null;
		keyofenC = keyofenC.toLowerCase();

		if (PubClass.getKeyFields()!=null && PubClass.getKeyFields().contains("," + keyofenC + ",") == true)
		{
			throw new RuntimeException("@错误:[" + this.getKeyOfEn() + "]是字段关键字，您不能用它做字段。");
		}

		if (this.IsExit(MapAttrAttr.KeyOfEn, this.getKeyOfEn(), MapAttrAttr.FK_MapData, this.getFK_MapData()))
		{
			return false;
		}

		if (this.getIdx() == 0)
		{
			this.setIdx(BP.DA.DBAccess.RunSQLReturnValInt(String.format("SELECT %1$s FROM Sys_MapAttr WHERE FK_MapData='" + this.getFK_MapData() + "'", SqlBuilder.GetIsNullInSQL("MAX(Idx)", "0"))) + 1);
		}
		this.setMyPK(this.getFK_MapData() + "_" + this.getKeyOfEn());
		return super.beforeInsert();
	}
	/** 
	 删除之前
	 
	 @return 
	 * @throws Exception 
	*/
	@Override
	protected boolean beforeDelete() throws Exception
	{
		String sqls = "DELETE FROM Sys_MapExt WHERE (AttrOfOper='" + this.getKeyOfEn() + "' OR AttrsOfActive='" + this.getKeyOfEn() + "' ) AND (FK_MapData='" + this.getFK_MapData() + "')";
		//删除权限管理字段.
		sqls += "@DELETE FROM Sys_FrmSln WHERE KeyOfEn='" + this.getKeyOfEn() + "' AND FK_MapData='" + this.getFK_MapData() + "'";

		//如果外部数据，或者ws数据，就删除其影子字段.
		if (this.getUIContralType() == UIContralType.DDL && this.getLGType() == FieldTypeS.Normal)
		{
			sqls += "@DELETE FROM Sys_MapAttr WHERE KeyOfEn='" + this.getKeyOfEn() + "T' AND FK_MapData='" + this.getFK_MapData() + "'";
		}



		BP.DA.DBAccess.RunSQLs(sqls);
		return super.beforeDelete();
	}
	
}