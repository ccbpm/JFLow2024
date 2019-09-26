package BP.Frm;

import BP.DA.*;
import BP.Web.*;
import BP.En.*;
import BP.Port.*;
import BP.Sys.*;
import java.util.*;

/** 
 表单方法属性
*/
public class MethodAttr extends EntityMyPKAttr
{
	/** 
	 表单ID
	*/
	public static final String FrmID = "FrmID";
	/** 
	 方法ID
	*/
	public static final String MethodID = "MethodID";
	/** 
	 方法名
	*/
	public static final String MethodName = "MethodName";
	/** 
	 方法类型
	*/
	public static final String RefMethodType = "RefMethodType";
	/** 
	 显示方式.
	*/
	public static final String ShowModel = "ShowModel";
	/** 
	 处理内容
	*/
	public static final String MethodDoc_Url = "MethodDoc_Url";
	/** 
	 方法的内容类型
	*/
	public static final String MethodDocTypeOfFunc = "MethodDocTypeOfFunc";
	/** 
	 处理内容 tag.
	*/
	public static final String Idx = "Idx";
	/** 
	 执行警告信息-对功能方法有效
	*/
	public static final String WarningMsg = "WarningMsg";
	/** 
	 成功提示信息
	*/
	public static final String MsgSuccess = "MsgSuccess";
	/** 
	 失败提示信息
	*/
	public static final String MsgErr = "MsgErr";
	/** 
	 执行完毕后干啥？
	*/
	public static final String WhatAreYouTodo = "WhatAreYouTodo";


		///#region 外观.
	/** 
	 宽度.
	*/
	public static final String PopWidth = "PopWidth";
	/** 
	 高度
	*/
	public static final String PopHeight = "PopHeight";

		///#endregion 外观.




		///#region 显示位置
	/** 
	 是否显示myToolBar工具栏上.
	*/
	public static final String IsMyBillToolBar = "IsMyBillToolBar";
	/** 
	 显示在工具栏更多按钮里.
	*/
	public static final String IsMyBillToolExt = "IsMyBillToolExt";
	/** 
	 显示在查询列表工具栏目上，用于执行批处理.
	*/
	public static final String IsSearchBar = "IsSearchBar";

		///#endregion 显示位置
}