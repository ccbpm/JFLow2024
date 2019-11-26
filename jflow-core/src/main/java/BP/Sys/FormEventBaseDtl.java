package BP.Sys;

import BP.DA.*;
import BP.Difference.SystemConfig;
import BP.En.*;
import BP.Web.*;
import java.math.*;
import java.util.Date;
import java.util.Enumeration;

/**
 * 表单从表事件基类 0,集成该基类的子类,可以重写事件的方法与基类交互. 1,一个子类必须与一个表单模版绑定.
 * 2,基类里有很多表单运行过程中的变量，这些变量可以辅助开发者在编写复杂的业务逻辑的时候使用.
 * 3,该基类有一个子类模版，位于:\CCForm\WF\Admin\AttrForm\F001Templepte.cs .
 */
public abstract class FormEventBaseDtl {
	/**
	 * 表单编号/表单标记. 该参数用于说明要把此事件注册到那一个表单模版上.
	 */
	public abstract String getFormDtlMark();

	/// #region 属性/内部变量(表单在运行的时候触发各类事件，子类可以访问这些属性来获取引擎内部的信息).
	/**
	 * 实体，一般是工作实体
	 */
	public Entity HisEn = null;
	/** 
	 
	*/
	public Entity HisEnDtl = null;
	/**
	 * 参数对象.
	 */
	private Row _SysPara = null;

	/**
	 * 参数
	 */
	public final Row getSysPara() {
		if (_SysPara == null) {
			_SysPara = new Row();
		}
		return _SysPara;
	}

	public final void setSysPara(Row value) {
		_SysPara = value;
	}
	/// #endregion 属性/内部变量(表单在运行的时候触发各类事件，子类可以访问这些属性来获取引擎内部的信息).

	/**
	 * 表单ID
	 */
	public final String getFK_Mapdata() {
		return this.GetValStr("FK_MapData");
	}

	/**
	 * 从表ID
	 */
	public final String getFK_MapDtl() {
		return this.GetValStr("FK_MapDtl");
	}

	/**
	 * 从表ID
	 */
	public final int getOID() {
		return this.GetValInt("OID");
	}

	/**
	 * 主表ID
	 */
	public final int getOIDOfMainEn() {
		return this.GetValInt("OIDOfMainEn");
	}

	/**
	 * 事件参数
	 * 
	 * @param key
	 *            时间字段
	 * @return 根据字段返回一个时间,如果为Null,或者不存在就抛出异常.
	 */
	public final Date GetValDateTime(String key) {
		try {
			String str = this.getSysPara().GetValByKey(key).toString();
			return DataType.ParseSysDateTime2DateTime(str);
		} catch (RuntimeException ex) {
			throw new RuntimeException("@表单从表事件实体在获取参数期间出现错误，请确认字段(" + key + ")是否拼写正确,技术信息:" + ex.getMessage());
		}
	}

	/**
	 * 获取字符串参数
	 * 
	 * @param key
	 *            key
	 * @return 如果为Null,或者不存在就抛出异常
	 */
	public final String GetValStr(String key) {
		try {
			return this.getSysPara().GetValByKey(key).toString();
		} catch (RuntimeException ex) {
			throw new RuntimeException("@表单从表事件实体在获取参数期间出现错误，请确认字段(" + key + ")是否拼写正确,技术信息:" + ex.getMessage());
		}
	}

	/**
	 * 获取Int64的数值
	 * 
	 * @param key
	 *            键值
	 * @return 如果为Null,或者不存在就抛出异常
	 */
	public final long GetValInt64(String key) {
		return Long.parseLong(this.GetValStr(key));
	}

	/**
	 * 获取int的数值
	 * 
	 * @param key
	 *            键值
	 * @return 如果为Null,或者不存在就抛出异常
	 */
	public final int GetValInt(String key) {
		return Integer.parseInt(this.GetValStr(key));
	}

	/**
	 * 获取Boolen值
	 * 
	 * @param key
	 *            字段
	 * @return 如果为Null,或者不存在就抛出异常
	 */
	public final boolean GetValBoolen(String key) {
		if (Integer.parseInt(this.GetValStr(key)) == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 获取decimal的数值
	 * 
	 * @param key
	 *            字段
	 * @return 如果为Null,或者不存在就抛出异常
	 */
	public final BigDecimal GetValDecimal(String key) {
		return this.GetValDecimal(key);
	}

	/// #region 构造方法
	/**
	 * 表单从表事件基类
	 */
	public FormEventBaseDtl() {
	}

	public String FrmLoadAfter() {
		return null;
	}

	public String FrmLoadBefore() {
		return null;
	}

	/// #region 要求子类重写的方法(表单从表事件).
	/**
	 * 表单删除前
	 * 
	 * @return 返回null,不提示信息,返回信息，提示删除警告/提示信息, 抛出异常阻止删除操作.
	 */
	public String BeforeFormDel() {
		return null;
	}

	/**
	 * 表单删除后
	 * 
	 * @return 返回null,不提示信息,返回信息，提示删除警告/提示信息, 抛出异常不处理.
	 */
	public String AfterFormDel() {
		return null;
	}

	/**
	 * 附件上传前
	 */
	public String AthUploadeBefore() {
		return null;
	}

	/**
	 * 上传后
	 */
	public String AthUploadeAfter() {
		return null;
	}

	/**
	 * 从表保存前
	 */
	public String RowSaveBefore() {
		return null;
	}

	/**
	 * 从表保存后
	 */
	public String RowSaveAfter() {
		return null;
	}

	/**
	 * 从表del前
	 */
	public String DtlRowDelBefore() {
		return null;
	}

	/**
	 * 从表del后
	 */
	public String DtlRowDelAfter() {
		return null;
	}

	/**
	 * 创建工作ID后的事件
	 * 
	 * @return
	 */
	public String CreateOID() {
		return null;
	}

	/**
	 * 执行事件
	 * 
	 * @param eventType
	 *            事件类型
	 * @param en
	 *            主表实体
	 * @param enDtl
	 *            从表实体
	 * @param atPara
	 *            参数
	 * @return 返回执行的结果
	 * @throws Exception
	 */
	public final String DoIt(String eventType, Entity en, Entity enDtl, String atPara) throws Exception {
		this.HisEn = en;

		/// #region 处理参数.

		if (en == null) {
			return null;
		}
		Row r = en.getRow();
		try {
			// 系统参数.
			r.put("FK_MapData", en.getClassID());
		} catch (java.lang.Exception e) {
			r.put("FK_MapData", en.getClassID());
		}

		if (atPara != null) {
			AtPara ap = new AtPara(atPara);
			for (String s : ap.getHisHT().keySet()) {
				try {
					r.put(s, ap.GetValStrByKey(s));
				} catch (java.lang.Exception e2) {
					r.put(s, ap.GetValStrByKey(s));
				}
			}
		}

		if (SystemConfig.getIsBSsystem() == true) {
			/* 如果是bs系统, 就加入外部url的变量. */
			Enumeration enu = BP.Sys.Glo.getRequest().getParameterNames();
			while (enu.hasMoreElements())
			{
				// 判断是否有内容，hasNext()
				String key = (String) enu.nextElement();
				r.put(key, BP.Sys.Glo.getRequest().getParameter(key));
			}
		}
		this.setSysPara(r);

		/// #region 执行事件.
		switch (eventType) {
		case FrmEventListDtl.RowSaveBefore: // 从表-保存前.。
			return this.RowSaveBefore();
		case FrmEventListDtl.RowSaveAfter: // 从表-保存后.。
			return this.RowSaveAfter();
		case FrmEventListDtl.DtlRowDelBefore: // 从表-保存前.。
			return this.DtlRowDelBefore();
		case FrmEventListDtl.DtlRowDelAfter: // 从表-保存后.。
			return this.DtlRowDelAfter();
		default:
			throw new RuntimeException("@没有判断的表单从表事件类型:" + eventType);
		}

	}

}