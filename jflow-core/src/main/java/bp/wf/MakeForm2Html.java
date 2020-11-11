package bp.wf;

import bp.sys.*;
import bp.tools.AesEncodeUtil;
import bp.tools.BaseFileUtils;
import bp.tools.HtmlToPdfInterceptor;
import bp.tools.QrCodeUtil;
import bp.tools.ZipCompress;
import bp.da.*;
import bp.difference.SystemConfig;
import bp.en.*;
import bp.web.*;
import bp.port.*;
import bp.wf.template.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

public class MakeForm2Html {
	/**
	 * 生成
	 * 
	 * @param mapData
	 * @param frmID
	 * @param workid
	 * @param en
	 * @param path
	 * @param flowNo
	 * @return
	 * @throws Exception 
	 */

	public static StringBuilder GenerHtmlOfFree(MapData mapData, String frmID, long workid, Entity en, String path,
			String flowNo, String FK_Node) throws Exception {
		return GenerHtmlOfFree(mapData, frmID, workid, en, path, flowNo, FK_Node, null);
	}

	public static StringBuilder GenerHtmlOfFree(MapData mapData, String frmID, long workid, Entity en, String path,
			String flowNo) throws Exception {
		return GenerHtmlOfFree(mapData, frmID, workid, en, path, flowNo, null, null);
	}

	public static StringBuilder GenerHtmlOfFree(MapData mapData, String frmID, long workid, Entity en, String path) throws Exception {
		return GenerHtmlOfFree(mapData, frmID, workid, en, path, null, null, null);
	}

	public static StringBuilder GenerHtmlOfFree(MapData mapData, String frmID, long workid, Entity en, String path, String flowNo, String FK_Node, String basePath) throws Exception
	{
		StringBuilder sb = new StringBuilder();

		//字段集合.
		MapAttrs mapAttrs = new MapAttrs(frmID);

		Attrs attrs = en.getEnMap().getAttrs();

		String appPath = "";
		float wtX = MapData.GenerSpanWeiYi(mapData, 1200);
		//float wtX = 0;
		float x = 0;


			///输出竖线与标签 & 超连接 Img.
		FrmLabs labs = mapData.getFrmLabs();
		for (FrmLab lab : labs.ToJavaList())
		{
			 x = lab.getX() + wtX;
             sb.append("\t\n<DIV name=u2 style='position:absolute;left:" + x + "px;top:" + lab.getY() + "px;text-align:left;' >");
             sb.append("\t\n<span style='color:" + lab.getFontColorHtml()+ ";font-family: " + lab.getFontName() + ";font-size: " + lab.getFontSize() + "px;' >" + lab.getTextHtml() + "</span>");
             sb.append("\t\n</DIV>");
		}

		 FrmLines lines = new FrmLines();
        lines.Retrieve(FrmLineAttr.FK_MapData, mapData.getNo(), FrmLineAttr.Y1);
        for(FrmLine line :lines.ToJavaList() )
        {
        	 if (line.getX1() == line.getX2())
             {
            	  /* 一道竖线 */
                 float h = line.getY1() - line.getY2();
                 h = Math.abs(h);
                 if (line.getY1() < line.getY2())
                 {
                     x = line.getX1() + wtX;
                     sb.append("\t\n<img id='" + line.getMyPK() + "'  name='YLine' style=\"padding:0px;position:absolute; left:" + x + "px; top:" + line.getY1()+ "px; width:" + line.getBorderWidth() + "px; height:" + h + "px;background-color:" + line.getBorderColorHtml() + "\" />");
                 }
                 else
                 {
                     x = line.getX2() + wtX;
                     sb.append("\t\n<img id='" + line.getMyPK() + "' name='YLine' style=\"padding:0px;position:absolute; left:" + x + "px; top:" + line.getY2() + "px; width:" + line.getBorderWidth() + "px; height:" + h + "px;background-color:" + line.getBorderColorHtml()  + "\" />");
                 }
             }
        	 else
             {
                 /* 一道横线 */
                 float w = line.getX2() - line.getX1();

                 if (line.getX1() < line.getX2())
                 {
                     x = line.getX1() + wtX;
                     sb.append("\t\n<img id='" + line.getMyPK() + "' name='line' style=\"padding:0px;position:absolute; left:" + x + "px; top:" + line.getY1()+ "px; width:" + w + "px; height:" + line.getBorderWidth() + "px;background-color:" + line.getBorderColorHtml() + "\" />");
                 }
                 else
                 {
                     x = line.getX2() + wtX;
                     sb.append("\t\n<img id='" + line.getMyPK() + "' name='line' style=\"padding:0px;position:absolute; left:" + x + "px; top:" + line.getY2() + "px; width:" + w + "px; height:" + line.getBorderWidth() + "px;background-color:" + line.getBorderColorHtml() + "\" />");
                 }
             }
        }

        FrmLinks links = mapData.getFrmLinks();
        for(FrmLink link :links.ToJavaList())
        {
        	  String url = link.getURLExt();
              if (url.contains("@"))
              {
                  for (MapAttr attr : mapAttrs.ToJavaList())
                  {
                      if (url.contains("@") == false)
                          break;
                      url = url.replace("@" + attr.getKeyOfEn(), en.GetValStrByKey(attr.getKeyOfEn()));
                  }
              }
              x = link.getX() + wtX;
              sb.append("\t\n<DIV id=u2 style='position:absolute;left:" + x + "px;top:" + link.getY() + "px;text-align:left;' >");
              sb.append("\t\n<span style='color:" + link.getFontColorHtml() + ";font-family: " + link.getFontName() + ";font-size: " + link.getFontSize() + "px;' > <a href=\"" + url + "\" target='" + link.getTarget() + "'> " + link.getText() + "</a></span>");
              sb.append("\t\n</DIV>");
        }
       

        FrmImgs imgs = mapData.getFrmImgs();
        for(FrmImg img :imgs.ToJavaList())
        {
            float y = img.getY();
            String imgSrc = "";

           //  ////#region 图片类型
            if (img.getHisImgAppType() == ImgAppType.Img)
            {
                //数据来源为本地.
                if (img.getImgSrcType() == 0)
                {
                    if (img.getImgPath().contains(";") == false)
                        imgSrc = img.getImgPath();
                }

                //数据来源为指定路径.
                if (img.getImgSrcType() == 1)
                {
                    //图片路径不为默认值
                    imgSrc = img.getImgURL();
                    if (imgSrc.contains("@"))
                    {
                        /*如果有变量*/
                        imgSrc = bp.wf.Glo.DealExp(imgSrc, en, "");
                    }
                }

                x = img.getX() + wtX;
                // 由于火狐 不支持onerror 所以 判断图片是否存在
                imgSrc = "icon.png";

                sb.append("\t\n<div id=" + img.getMyPK() + " style='position:absolute;left:" + x + "px;top:" + y + "px;text-align:left;vertical-align:top' >");
                if (DataType.IsNullOrEmpty(img.getLinkURL()) == false)
                    sb.append("\t\n<a href='" + img.getLinkURL() + "' target=" + img.getLinkTarget() + " ><img src='" + imgSrc + "'  onerror=\"this.src='/DataUser/ICON/CCFlow/LogBig.png'\"  style='padding: 0px;margin: 0px;border-width: 0px;width:" + img.getW() + "px;height:" + img.getH() + "px;' /></a>");
                else
                    sb.append("\t\n<img src='" + imgSrc + "'  onerror=\"this.src='/DataUser/ICON/CCFlow/LogBig.png'\"  style='padding: 0px;margin: 0px;border-width: 0px;width:" + img.getW() + "px;height:" + img.getH() + "px;' />");
                sb.append("\t\n</div>");
                
                continue;
            }
         

           //  ////#region 二维码
            
            if (img.getHisImgAppType() == ImgAppType.QRCode)
            {
                x = img.getX() + wtX;
                String pk = String.valueOf(en.getPKVal());
                String myPK = frmID + "_" + img.getMyPK() + "_" + pk;
                FrmEleDB frmEleDB = new FrmEleDB();
                frmEleDB.setMyPK(myPK);
                if (frmEleDB.RetrieveFromDBSources() == 0)
                {
                    //生成二维码
                }

                sb.append("\t\n<DIV id=" + img.getMyPK() + " style='position:absolute;left:" + x + "px;top:" + y + "px;text-align:left;vertical-align:top' >");
                sb.append("\t\n<img src='" + frmEleDB.getTag2() + "' style='padding: 0px;margin: 0px;border-width: 0px;width:" + img.getW() + "px;height:" + img.getH() + "px;' />");
                sb.append("\t\n</DIV>");

                continue;
            }
          //  ////#endregion

           //  ////#region 电子签章
            //图片类型
            if (img.getHisImgAppType() == ImgAppType.Seal)
            {
                //获取登录人岗位
                String stationNo = "";
                //签章对应部门
                String fk_dept = WebUser.getFK_Dept();
                //部门来源类别
                String sealType = "0";
                //签章对应岗位
                String fk_station = img.getTag0();
                //表单字段
                String sealField = "";
                String sql = "";

                //重新加载 可能有缓存
                img.RetrieveFromDBSources();
                //0.不可以修改，从数据表中取，1可以修改，使用组合获取并保存数据
                if ((img.getIsEdit() == 1))
                {
                   //  ////#region 加载签章
                    //如果设置了部门与岗位的集合进行拆分
                    if (!DataType.IsNullOrEmpty(img.getTag0()) && img.getTag0().contains("^") && img.getTag0().split("^").length == 4)
                    {
                        fk_dept = img.getTag0().split("^")[0];
                        fk_station = img.getTag0().split("^")[1];
                        sealType = img.getTag0().split("^")[2];
                        sealField = img.getTag0().split("^")[3];
                        //如果部门没有设定，就获取部门来源
                        if (fk_dept == "all")
                        {
                            //默认当前登陆人
                            fk_dept = WebUser.getFK_Dept();
                            //发起人
                            if (sealType == "1")
                            {
                                sql = "SELECT FK_Dept FROM WF_GenerWorkFlow WHERE WorkID=" + en.GetValStrByKey("OID");
                                fk_dept = DBAccess.RunSQLReturnString(sql);
                            }
                            //表单字段
                            if (sealType == "2" && !DataType.IsNullOrEmpty(sealField))
                            {
                                //判断字段是否存在
                                for (MapAttr attr: mapAttrs.ToJavaList())
                                {
                                    if (attr.getKeyOfEn() == sealField)
                                    {
                                        fk_dept = en.GetValStrByKey(sealField);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    //判断本部门下是否有此人
                    sql = String.format(" select FK_Station from Port_DeptStation where FK_Dept ='{0}' and FK_Station in (select FK_Station from " + bp.wf.Glo.getEmpStation()+ " where FK_Emp='{1}')", fk_dept, WebUser.getNo());
                    DataTable dt = DBAccess.RunSQLReturnTable(sql);
                    for (DataRow dr : dt.Rows)
                    {
                        if (fk_station.contains(dr.getValue(0).toString() + ","))
                        {
                            stationNo = dr.getValue(0).toString();
                            break;
                        }
                    }
                    ////#endregion 加载签章

                    imgSrc = CCFlowAppPath + "DataUser/Seal/" + fk_dept + "_" + stationNo + ".png";
                    //设置主键
                    String myPK = DataType.IsNullOrEmpty(img.getEnPK()) ? "seal" : img.getEnPK();
                    myPK = myPK + "_" + en.GetValStrByKey("OID") + "_" + img.getMyPK();

                   

                    //添加控件
                    x = img.getX() + wtX;
                    sb.append("\t\n<DIV id=" + img.getMyPK() + " style='position:absolute;left:" + x + "px;top:" + y + "px;text-align:left;vertical-align:top' >");
                    sb.append("\t\n<img src='" + imgSrc + "' onerror=\"javascript:this.src='" + appPath + "DataUser/Seal/Def.png'\" style=\"padding: 0px;margin: 0px;border-width: 0px;width:" + img.getW() + "px;height:" + img.getH() + "px;\" />");
                    sb.append("\t\n</DIV>");
                }
                else
                {
                }
            }
         //   ////#endregion
        }


        FrmBtns btns = mapData.getFrmBtns();
        for (FrmBtn btn : btns.ToJavaList())
        {
            x = btn.getX() + wtX;
            sb.append("\t\n<DIV id=u2 style='position:absolute;left:" + x + "px;top:" + btn.getY() + "px;text-align:left;' >");
            sb.append("\t\n<span >");

            String doDoc = bp.wf.Glo.DealExp(btn.getEventContext(), en, null);
            doDoc = doDoc.replaceAll("~", "'");
            switch (btn.getHisBtnEventType())
            {
                case Disable:
                    sb.append("<input type=button class=Btn value='" + btn.getText().replace("&nbsp;", " ") + "' disabled='disabled'/>");
                    break;
                case RunExe:
                case RunJS:
                    sb.append("<input type=button class=Btn value=\"" +btn.getText().replace("&nbsp;", " ") + "\" enable=true onclick=\"" + doDoc + "\" />");
                    break;
                default:
                    sb.append("<input type=button value='" + btn.getText() + "' />");
                    break;
            }
            sb.append("\t\n</span>");
            sb.append("\t\n</DIV>");
        }
			/// 输出 rb.
        FrmRBs myrbs = mapData.getFrmRBs();
        for (FrmRB rb : myrbs.ToJavaList())
        {
            x = rb.getX() + wtX;
            sb.append("<DIV id='F" + rb.getMyPK() + "' style='position:absolute; left:" + x + "px; top:" + rb.getY() + "px; height:16px;text-align: left;word-break: keep-all;' >");
            sb.append("<span style='word-break: keep-all;font-size:12px;'>");

            if (rb.getIntKey() == en.GetValIntByKey(rb.getKeyOfEn()))
                sb.append("<b>" + rb.getLab() + "</b>");
            else
                sb.append(rb.getLab());

            sb.append("</span>");
            sb.append("</DIV>");
        }

			///输出数据控件.
		int fSize = 0;
		for (MapAttr attr : mapAttrs.ToJavaList())
		{
			//处理隐藏字段，如果是不可见并且是启用的就隐藏.
			if (attr.getUIVisible()== false && attr.getUIIsEnable())
			{
				sb.append("<input type=text value='" + en.GetValStrByKey(attr.getKeyOfEn()) + "' style='display:none;' />");
				continue;
			}

			if (attr.getUIVisible()== false)
			{
				continue;
			}

			x = attr.getX() + wtX;
			if (attr.getLGType()== FieldTypeS.Enum || attr.getLGType()== FieldTypeS.FK)
			{
				sb.append("<DIV id='F" + attr.getKeyOfEn() + "' style='position:absolute; left:" + x + "px; top:" + attr.getY() + "px;  height:16px;text-align: left;word-break: keep-all;' >");
			}
			else
			{
				sb.append("<DIV id='F" + attr.getKeyOfEn() + "' style='position:absolute; left:" + x + "px; top:" + attr.getY() + "px; width:" + attr.getUIWidth() + "px; height:16px;text-align: left;word-break: keep-all;' >");
			}

			sb.append("<span>");


				///add contrals.
			if (attr.getMaxLen() >= 3999 && attr.getTBModel() == TBModel.RichText)
			{
				sb.append(en.GetValStrByKey(attr.getKeyOfEn()));

				sb.append("</span>");
				sb.append("</DIV>");
				continue;
			}


				///通过逻辑类型，输出相关的控件.
			String text = "";
			switch (attr.getLGType())
			{
				case Normal: // 输出普通类型字段.
					if (attr.getIsSigan() == true)
					{
						text = en.GetValStrByKey(attr.getKeyOfEn());
						text = SignPic(text);
						break;
					}
					if (attr.getMyDataType() == 1 && attr.getUIContralType().getValue()== DataType.AppString)
					{
						if (attrs.Contains(attr.getKeyOfEn() + "Text") == true)
						{
							text = en.GetValRefTextByKey(attr.getKeyOfEn());
						}
						if (DataType.IsNullOrEmpty(text))
						{
							if (attrs.Contains(attr.getKeyOfEn() + "T") == true)
							{
								text = en.GetValStrByKey(attr.getKeyOfEn() + "T");
							}
						}
					}
					else
					{
						text = en.GetValStrByKey(attr.getKeyOfEn());
					}
					break;
				case Enum:
					if (attr.getUIContralType()== UIContralType.CheckBok)
					{
						String s = en.GetValStrByKey(attr.getKeyOfEn()) + ",";
						SysEnums enums = new SysEnums(attr.getUIBindKey());
						for (SysEnum se : enums.ToJavaList())
						{
							if (s.indexOf(se.getIntKey() + ",") != -1)
							{
								text += se.getLab() + " ";
							}
						}

					}
					else
					{
						text = en.GetValRefTextByKey(attr.getKeyOfEn());
					}
					break;
				case FK:
					text = en.GetValRefTextByKey(attr.getKeyOfEn());
					break;
				default:
					break;
			}

				/// 通过逻辑类型，输出相关的控件.


				/// add contrals.


			if (attr.getIsBigDoc())
			{
				//这几种字体生成 pdf都乱码
				text = text.replace("仿宋,", "宋体,");
				text = text.replace("仿宋;", "宋体;");
				text = text.replace("仿宋\"", "宋体\"");
				text = text.replace("黑体,", "宋体,");
				text = text.replace("黑体;", "宋体;");
				text = text.replace("黑体\"", "宋体\"");
				text = text.replace("楷体,", "宋体,");
				text = text.replace("楷体;", "宋体;");
				text = text.replace("楷体\"", "宋体\"");
				text = text.replace("隶书,", "宋体,");
				text = text.replace("隶书;", "宋体;");
				text = text.replace("隶书\"", "宋体\"");
			}

			if (attr.getMyDataType() == DataType.AppBoolean)
			{
				if (DataType.IsNullOrEmpty(text) || text.equals("0"))
				{
					text = "[&#10005]" + attr.getName();
				}
				else
				{
					text = "[&#10004]" + attr.getName();
				}
			}

			sb.append(text);

			sb.append("</span>");
			sb.append("</DIV>");
		}




			/// 输出数据控件.


			///输出明细.
		int dtlsCount = 0;
		MapDtls dtls = new MapDtls(frmID);
		for (MapDtl dtl : dtls.ToJavaList())
		{
			if (dtl.getIsView() == false)
			{
				continue;
			}

			dtlsCount++;
			x = dtl.getX() + wtX;
			float y = dtl.getY();

			sb.append("<DIV id='Fd" + dtl.getNo() + "' style='position:absolute; left:" + x + "px; top:" + y + "px; width:" + dtl.getW() + "px; height:" + dtl.getH() + "px;text-align: left;' >");
			sb.append("<span>");

			MapAttrs attrsOfDtls = new MapAttrs(dtl.getNo());

			sb.append("<table style='wdith:100%' >");
			sb.append("<tr>");
			for (MapAttr item : attrsOfDtls.ToJavaList())
			{
				if (item.getKeyOfEn().equals("OID"))
				{
					continue;
				}
				if (item.getUIVisible()== false)
				{
					continue;
				}

				sb.append("<th class='DtlTh'>" + item.getName() + "</th>");
			}
			sb.append("</tr>");
			/// 输出标题.


			///输出数据.
			GEDtls gedtls = new GEDtls(dtl.getNo());
			gedtls.Retrieve(GEDtlAttr.RefPK, workid, "OID");
			for (GEDtl gedtl : gedtls.ToJavaList())
			{
				sb.append("<tr>");

				for (MapAttr item : attrsOfDtls.ToJavaList())
				{
					if (item.getKeyOfEn().equals("OID") || item.getUIVisible()== false)
					{
						continue;
					}

					if (item.getUIContralType()== UIContralType.DDL)
					{
						sb.append("<td class='DtlTd'>" + gedtl.GetValRefTextByKey(item.getKeyOfEn()) + "</td>");
						continue;
					}

					if (item.getIsNum())
					{
						sb.append("<td class='DtlTd' style='text-align:right' >" + gedtl.GetValStrByKey(item.getKeyOfEn()) + "</td>");
						continue;
					}

					sb.append("<td class='DtlTd'>" + gedtl.GetValStrByKey(item.getKeyOfEn()) + "</td>");
				}
				sb.append("</tr>");
			}
			/// 输出数据.


			sb.append("</table>");

			//string src = "";
			//if (dtl.HisEditModel == EditModel.TableModel)
			//{
			//    src = SystemConfig.getCCFlowWebPath() + "WF/CCForm/Dtl.htm?EnsName=" + dtl.No + "&RefPKVal=" + en.PKVal + "&IsReadonly=1";
			//}
			//else
			//{
			//    src = appPath + "WF/CCForm/DtlCard.htm?EnsName=" + dtl.No + "&RefPKVal=" + en.PKVal + "&IsReadonly=1";
			//}

			//sb.Append("<iframe ID='F" + dtl.No + "' onload= 'F" + dtl.No + "load();'  src='" + src + "' frameborder=0  style='position:absolute;width:" + dtl.W + "px; height:" + dtl.H + "px;text-align: left;'  leftMargin='0'  topMargin='0' scrolling=auto /></iframe>");

			sb.append("</span>");
			sb.append("</DIV>");
		}

			/// 输出明细.


			///审核组件
		if (flowNo != null)
		{
			NodeWorkCheck fwc = new NodeWorkCheck(frmID);
			if (fwc.getHisFrmWorkCheckSta() != FrmWorkCheckSta.Disable)
			{
				x = fwc.getFwcX() + wtX;
				sb.append("<DIV id='DIVWC" + fwc.getNo() + "' style='position:absolute; left:" + x + "px; top:" + fwc.getFwcY() + "px; width:" + fwc.getFwcW() + "px; height:" + fwc.getFwcH() + "px;text-align: left;' >");
				sb.append("<table   style='border: 1px outset #C0C0C0;padding: inherit; margin: 0;border-collapse:collapse;width:100%;' >");


					///生成审核信息.
				if (flowNo != null)
				{
					String sql = "SELECT EmpFrom, EmpFromT,RDT,Msg,NDFrom,NDFromT FROM ND" + Integer.parseInt(flowNo) + "Track WHERE WorkID=" + workid + " AND ActionType=" + ActionType.WorkCheck.getValue() + " ORDER BY RDT ";
					DataTable dt = DBAccess.RunSQLReturnTable(sql);

					//获得当前待办的人员,把当前审批的人员排除在外,不然就有默认同意的意见可以打印出来.
					sql = "SELECT FK_Emp, FK_Node FROM WF_GenerWorkerList WHERE IsPass!=1 AND WorkID=" + workid;
					DataTable dtOfTodo = DBAccess.RunSQLReturnTable(sql);

					for (DataRow dr : dt.Rows)
					{


							///排除正在审批的人员.
						String nodeID = dr.getValue("NDFrom").toString();
						String empFrom = dr.getValue("EmpFrom").toString();
						if (dtOfTodo.Rows.size() != 0)
						{
							boolean isHave = false;
							for (DataRow mydr : dtOfTodo.Rows)
							{
								if (!mydr.getValue("FK_Node").toString().equals(nodeID))
								{
									continue;
								}

								if (!mydr.getValue("FK_Emp").toString().equals(empFrom))
								{
									continue;
								}
								isHave = true;
							}

							if (isHave == true)
							{
								continue;
							}
						}

							/// 排除正在审批的人员.

						sb.append("<tr>");
						sb.append("<td valign=middle style='border-style: solid;padding: 4px;text-align: left;color: #333333;font-size: 12px;border-width: 1px;border-color: #C2D5E3;' >" + dr.getValue("NDFromT") + "</td>");
						String msg = dr.getValue("Msg").toString();

						msg += "<br>";
						msg += "<br>";
						msg += "审核人:" + dr.getValue("EmpFromT") + " &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日期:" + dr.getValue("RDT").toString();

						sb.append("<td colspan=3 valign=middle style='border-style: solid;padding: 4px;text-align: left;color: #333333;font-size: 12px;border-width: 1px;border-color: #C2D5E3;' >" + msg + "</td>");
						sb.append("</tr>");
					}
				}
				sb.append("</table>");

					/// 生成审核信息.
				sb.append("</DIV>");
			}
		}

			/// 审核组件


			///父子流程组件
		if (flowNo != null)
		{
			FrmSubFlow subFlow = new FrmSubFlow(frmID);
			if (subFlow.getHisFrmSubFlowSta() != FrmSubFlowSta.Disable)
			{
				x = subFlow.getSfX() + wtX;
				sb.append("<DIV id='DIVWC" + subFlow.getNo() + "' style='position:absolute; left:" + x + "px; top:" + subFlow.getSfY() + "px; width:" + subFlow.getSfW() + "px; height:" + subFlow.getSfH() + "px;text-align: left;' >");
				sb.append("<span>");

				String src = appPath + "WF/WorkOpt/SubFlow.htm?s=2";
				String fwcOnload = "";

				if (subFlow.getHisFrmSubFlowSta() == FrmSubFlowSta.Readonly)
				{
					src += "&DoType=View";
				}

				src += "&r=q";
				sb.append("<iframe ID='FSF" + subFlow.getNo() + "' " + fwcOnload + "  src='" + src + "' frameborder=0 style='padding:0px;border:0px;'  leftMargin='0'  topMargin='0' width='" + subFlow.getSfW() + "' height='" + subFlow.getSfH() + "'   scrolling=auto/></iframe>");

				sb.append("</span>");
				sb.append("</DIV>");
			}
		}

			/// 父子流程组件


			///输出附件
		FrmAttachments aths = new FrmAttachments(frmID);
		
		for (FrmAttachment ath : aths.ToJavaList())
		{

			if (ath.getUploadType() == AttachmentUploadType.Single)
			{
				/* 单个文件 */
				FrmAttachmentDBs athDBs = bp.wf.Glo.GenerFrmAttachmentDBs(ath, String.valueOf(workid), ath.getMyPK());
				Object tempVar = athDBs.GetEntityByKey(FrmAttachmentDBAttr.FK_FrmAttachment, ath.getMyPK());
				FrmAttachmentDB athDB = tempVar instanceof FrmAttachmentDB ? (FrmAttachmentDB)tempVar : null;
				x = ath.getX() + wtX;
				float y = ath.getY();
				sb.append("<DIV id='Fa" + ath.getMyPK() + "' style='position:absolute; left:" + x + "px; top:" + y + "px; text-align: left;float:left' >");
				//  sb.Append("<span>");
				sb.append("<DIV>");

				sb.append("附件没有转化:" + athDB.getFileName());


				sb.append("</DIV>");
				sb.append("</DIV>");
			}

			if (ath.getUploadType() == AttachmentUploadType.Multi)
			{
				x = ath.getX() + wtX;
				sb.append("<DIV id='Fd" + ath.getMyPK() + "' style='position:absolute; left:" + x + "px; top:" + ath.getY() + "px; width:" + ath.getW() + "px; height:" + ath.getH() + "px;text-align: left;' >");
				sb.append("<span>");
				sb.append("<ul>");

				//判断是否有这个目录.
				if ((new File(path + "/pdf/")).isDirectory() == false)
				{
					(new File(path + "/pdf/")).mkdirs();
				}

				//文件加密
				boolean fileEncrypt = SystemConfig.getIsEnableAthEncrypt();
				FrmAttachmentDBs athDBs = bp.wf.Glo.GenerFrmAttachmentDBs(ath, String.valueOf(workid), ath.getMyPK());

				for (FrmAttachmentDB item : athDBs.ToJavaList())
				{
					//获取文件是否加密
					boolean isEncrypt = item.GetParaBoolen("IsEncrypt");
					if (ath.getAthSaveWay() == AthSaveWay.FTPServer)
					{
						try
						{
							String toFile = path + "/pdf/" + item.getFileName();
							if ((new File(toFile)).isFile() == false)
							{
								//把文件copy到,
								////获取文件是否加密
								
								String file = item.GenerTempFile(ath.getAthSaveWay());
								String fileTempDecryPath = file;
								if (fileEncrypt == true && isEncrypt == true)
								{
									fileTempDecryPath = file + ".tmp";
									AesEncodeUtil.decryptFile(file, fileTempDecryPath);

								}

								Files.copy(Paths.get(fileTempDecryPath), Paths.get(toFile), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
							}

							sb.append("<li><a href='" + item.getFileName() + "'>" + item.getFileName() + "</a></li>");
						}
						catch (RuntimeException ex)
						{
							sb.append("<li>" + item.getFileName() + "(<font color=red>文件未从ftp下载成功{" + ex.getMessage() + "}</font>)</li>");
						}
					}

					if (ath.getAthSaveWay() == AthSaveWay.IISServer)
					{
						try
						{
							String toFile = path + "/pdf/" + item.getFileName();
							if ((new File(toFile)).isFile() == false)
							{
								//把文件copy到,
								String fileTempDecryPath = item.getFileFullName();
								if (fileEncrypt == true && isEncrypt == true)
								{
									fileTempDecryPath = item.getFileFullName() + ".tmp";
									AesEncodeUtil.decryptFile(item.getFileFullName(), fileTempDecryPath);

								}

								//把文件copy到,
								Files.copy(Paths.get(fileTempDecryPath), Paths.get(path + "/pdf/" + item.getFileName()), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
							}
							sb.append("<li><a href='" + item.getFileName() + "'>" + item.getFileName() + "</a></li>");
						}
						catch (RuntimeException ex)
						{
							sb.append("<li>" + item.getFileName() + "(<font color=red>文件未从ftp下载成功{" + ex.getMessage() + "}</font>)</li>");
						}
					}

				}
				sb.append("</ul>");

				sb.append("</span>");
				sb.append("</DIV>");
			}
		}

			/// 输出附件.

		return sb;
	}

	private static StringBuilder GenerHtmlOfFool(MapData mapData, String frmID, long workid, Entity en, String path,
			String flowNo, String FK_Node, String basePath) throws Exception {
		return GenerHtmlOfFool(mapData, frmID, workid, en, path, flowNo, FK_Node, basePath, NodeFormType.FoolForm);
	}

	private static StringBuilder GenerHtmlOfFool(MapData mapData, String frmID, long workid, Entity en, String path,
			String flowNo, String FK_Node) throws Exception {
		return GenerHtmlOfFool(mapData, frmID, workid, en, path, flowNo, FK_Node, null, NodeFormType.FoolForm);
	}

	private static StringBuilder GenerHtmlOfFool(MapData mapData, String frmID, long workid, Entity en, String path,
			String flowNo) throws Exception {
		return GenerHtmlOfFool(mapData, frmID, workid, en, path, flowNo, null, null, NodeFormType.FoolForm);
	}

	private static StringBuilder GenerHtmlOfFool(MapData mapData, String frmID, long workid, Entity en, String path) throws Exception {
		return GenerHtmlOfFool(mapData, frmID, workid, en, path, null, null, null, NodeFormType.FoolForm);
	}


	private static StringBuilder GenerHtmlOfFool(MapData mapData, String frmID, long workid, Entity en, String path, String flowNo, String FK_Node, String basePath, NodeFormType formType) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		//字段集合.
		MapAttrs mapAttrs = new MapAttrs(frmID);
		Attrs attrs = null;
		GroupFields gfs = null;
		if (formType == NodeFormType.FoolTruck && DataType.IsNullOrEmpty(FK_Node) == false)
		{
			Node nd = new Node(FK_Node);
			Work wk = nd.getHisWork();
			wk.setOID(workid);
			wk.RetrieveFromDBSources();

			/* 求出来走过的表单集合 */
			String sql = "SELECT NDFrom FROM ND" + Integer.parseInt(flowNo) + "Track A, WF_Node B ";
			sql += " WHERE A.NDFrom=B.NodeID  ";
			sql += "  AND (ActionType=" + ActionType.Forward.getValue() + " OR ActionType=" + ActionType.Start.getValue() + "  OR ActionType=" + ActionType.Skip.getValue() + ")  ";
			sql += "  AND B.FormType=" + NodeFormType.FoolTruck.getValue() + " "; // 仅仅找累加表单.
			sql += "  AND NDFrom!=" + Integer.parseInt(FK_Node.replace("ND", "")) + " "; //排除当前的表单.


			sql += "  AND (A.WorkID=" + workid + ") ";
			sql += " ORDER BY A.RDT ";

			// 获得已经走过的节点IDs.
			DataTable dtNodeIDs = DBAccess.RunSQLReturnTable(sql);
			String frmIDs = "";
			if (dtNodeIDs.Rows.size() > 0)
			{
				//把所有的节点字段.
				for (DataRow dr : dtNodeIDs.Rows)
				{
					if (frmIDs.contains("ND" + dr.getValue(0).toString()) == true)
					{
						continue;
					}
					frmIDs += "'ND" + dr.getValue(0).toString() + "',";
				}
			}
			frmIDs = frmIDs.substring(0, frmIDs.length() - 1);
			GenerWorkFlow gwf = new GenerWorkFlow(workid);
			if (gwf.getWFState() == WFState.Complete)
			{
				frmIDs = frmIDs + ",'" + FK_Node + "'";
			}
			gfs = new GroupFields();
			gfs.RetrieveIn(GroupFieldAttr.FrmID, "(" + frmIDs + ")");

			mapAttrs = new MapAttrs();
			mapAttrs.RetrieveIn(MapAttrAttr.FK_MapData, "(" + frmIDs + ")");
		}
		else
		{
			gfs = new GroupFields(frmID);
			attrs = en.getEnMap().getAttrs();
		}

		//生成表头.
		String frmName = mapData.getName();
		if (SystemConfig.getAppSettings().get("CustomerNo").equals("TianYe"))
		{
			frmName = "";
		}

		sb.append(" <table style='width:950px;height:auto;' >");

		///生成头部信息.
		sb.append("<tr>");

		sb.append("<td colspan=4 >");

		sb.append("<table border=0 style='width:950px;'>");

		sb.append("<tr  style='border:0px;' >");

		//二维码显示
		boolean IsHaveQrcode = true;
		if (SystemConfig.GetValByKeyBoolen("IsShowQrCode", false) == false)
		{
			IsHaveQrcode = false;
		}

		//判断当前文件是否存在图片
		boolean IsHaveImg = false;
		String IconPath = path + "/icon.png";
		if ((new File(IconPath)).isFile() == true)
		{
			IsHaveImg = true;
		}
		if (IsHaveImg == true)
		{
			sb.append("<td>");
			sb.append("<img src='icon.png' style='height:100px;border:0px;' />");
			sb.append("</td>");
		}
		if (IsHaveImg == false && IsHaveQrcode == false)
		{
			sb.append("<td  colspan=6>");
		}
		else if ((IsHaveImg == true && IsHaveQrcode == false) || (IsHaveImg == false && IsHaveQrcode == true))
		{
			sb.append("<td  colspan=5>");
		}
		else
		{
			sb.append("<td  colspan=4>");
		}

		sb.append("<br><h2><b>" + frmName + "</b></h2>");
		sb.append("</td>");

		if (IsHaveQrcode == true)
		{
			sb.append("<td>");
			sb.append(" <img src='QR.png' style='height:100px;'  />");
			sb.append("</td>");
		}

		sb.append("</tr>");
		sb.append("</table>");

		sb.append("</td>");
		/// 生成头部信息.

		if (DataType.IsNullOrEmpty(FK_Node) == false && DataType.IsNullOrEmpty(flowNo) == false)
		{
			Node nd = new Node(Integer.parseInt(FK_Node.replace("ND","")));
			if (frmID.startsWith("ND") == true && nd.getFrmWorkCheckSta() != FrmWorkCheckSta.Disable)
			{
				Object tempVar = gfs.GetEntityByKey(GroupFieldAttr.CtrlType, "FWC");
				GroupField gf = tempVar instanceof GroupField ? (GroupField)tempVar : null;
				if (gf == null)
				{
					gf = new GroupField();
					gf.setOID( 100);
					gf.setFrmID(nd.getNodeFrmID());
					gf.setCtrlType("FWC");
					gf.setCtrlID("FWCND" + nd.getNodeID());
					gf.setIdx( 100);
					gf.setLab("审核信息");
					gfs.AddEntity(gf);
				}
			}
		}


		for (GroupField gf : gfs.ToJavaList())
		{
			//输出标题.
			if (!gf.getCtrlType().equals("Ath"))
			{
				sb.append(" <tr>");
				sb.append("  <th colspan=4><b>" + gf.getLab() + "</b></th>");
				sb.append(" </tr>");
			}

			///输出字段.
			if (gf.getCtrlID().equals("") && gf.getCtrlType().equals(""))
			{
				boolean isDropTR = true;
				String html = "";
				for (MapAttr attr : mapAttrs.ToJavaList())
				{
					//处理隐藏字段，如果是不可见并且是启用的就隐藏.
					if (attr.getUIVisible()== false)
					{
						continue;
					}
					if (attr.getGroupID() != gf.getOID())
					{
						continue;
					}
					
					String text = "";

					switch (attr.getLGType())
					{
						case Normal: // 输出普通类型字段.
							if (attr.getMyDataType() == 1 && attr.getUIContralType().getValue()== DataType.AppString)
							{

								if (attrs.Contains(attr.getKeyOfEn() + "Text") == true)
								{
									text = en.GetValRefTextByKey(attr.getKeyOfEn());
								}
								if (DataType.IsNullOrEmpty(text))
								{
									if (attrs.Contains(attr.getKeyOfEn() + "T") == true)
									{
										text = en.GetValStrByKey(attr.getKeyOfEn() + "T");
									}
								}
							}
							else
							{
								//判断是不是图片签名
								if (attr.getIsSigan() == true)
								{
									String SigantureNO = en.GetValStrByKey(attr.getKeyOfEn());
									String src = SystemConfig.getHostURL() + "/DataUser/Siganture/";
									text = "<img src='" + src + SigantureNO + ".JPG' title='" + SigantureNO + "' onerror='this.src=\"" + src + "Siganture.JPG\"' style='height:50px;'  alt='图片丢失' /> ";
								}
								else
								{
									text = en.GetValStrByKey(attr.getKeyOfEn());
								}
								if (attr.getIsRichText() == true)
								{
									text = text.replace("white-space: nowrap;", "");
								}
							}

							break;
						case Enum:
							if (attr.getUIContralType()== UIContralType.CheckBok)
							{
								String s = en.GetValStrByKey(attr.getKeyOfEn()) + ",";
								SysEnums enums = new SysEnums(attr.getUIBindKey());
								for (SysEnum se : enums.ToJavaList())
								{
									if (s.indexOf(se.getIntKey() + ",") != -1)
									{
										text += se.getLab() + " ";
									}
								}

							}
							else
							{
								text = en.GetValRefTextByKey(attr.getKeyOfEn());
							}
							break;
						case FK:
							text = en.GetValRefTextByKey(attr.getKeyOfEn());
							break;
						default:
							break;
					}

					if (attr.getIsBigDoc())
					{
						//这几种字体生成 pdf都乱码
						text = text.replace("仿宋,", "宋体,");
						text = text.replace("仿宋;", "宋体;");
						text = text.replace("仿宋\"", "宋体\"");
						text = text.replace("黑体,", "宋体,");
						text = text.replace("黑体;", "宋体;");
						text = text.replace("黑体\"", "宋体\"");
						text = text.replace("楷体,", "宋体,");
						text = text.replace("楷体;", "宋体;");
						text = text.replace("楷体\"", "宋体\"");
						text = text.replace("隶书,", "宋体,");
						text = text.replace("隶书;", "宋体;");
						text = text.replace("隶书\"", "宋体\"");
					}

					if (attr.getMyDataType() == DataType.AppBoolean)
					{
						if (DataType.IsNullOrEmpty(text) || text.equals("0"))
						{
							text = "[&#10005]" + attr.getName();
						}
						else
						{
							text = "[&#10004]" + attr.getName();
						}
					}

					//线性展示并且colspan=3
					if (attr.getColSpan() == 3 || (attr.getColSpan() == 4 && attr.getUIHeightInt() < 30))
					{
						isDropTR = true;
						html += " <tr>";
						html += " <td  class='FDesc' style='width:143px' >" + attr.getName() + "</td>";
						html += " <td  ColSpan=3 style='width:712.5px' class='FContext'>";
						html += text;
						html += " </td>";
						html += " </tr>";
						continue;
					}

					//线性展示并且colspan=4
					if (attr.getColSpan() == 4)
					{
						isDropTR = true;
						html += " <tr>";
						html += " <td ColSpan=4 class='FDesc' >" + attr.getName() + "</td>";
						html += " </tr>";
						html += " <tr>";
						html += " <td ColSpan=4 class='FContext'>";
						html += text;
						html += " </td>";
						html += " </tr>";
						continue;
					}

					if (isDropTR == true)
					{
						html += " <tr>";
						html += " <td class='FDesc' style='width:143px'>" + attr.getName() + "</td>";
						html += " <td class='FContext' style='width:332px'>";
						html += text;
						html += " </td>";
						isDropTR = !isDropTR;
						continue;
					}

					if (isDropTR == false)
					{
						html += " <td  class='FDesc'style='width:143px'>" + attr.getName() + "</td>";
						html += " <td class='FContext' style='width:332px'>";
						html += text;
						html += " </td>";
						html += " </tr>";
						isDropTR = !isDropTR;
						continue;
					}
				}
				sb.append(html); //增加到里面.
				continue;
			}
			/// 输出字段.

			///如果是从表.
			if (gf.getCtrlType().equals("Dtl"))
			{
				if (DataType.IsNullOrEmpty(gf.getCtrlID()) == true)
				{
					continue;
				}
				/* 如果是从表 */
				MapAttrs attrsOfDtls = null;
				try
				{
					attrsOfDtls = new MapAttrs(gf.getCtrlID());
				}
				catch (RuntimeException ex)
				{
				}

				///输出标题.
				sb.append("<tr><td valign=top colspan=4 >");

				sb.append("<table style='wdith:100%' >");
				sb.append("<tr>");
				for (MapAttr item : attrsOfDtls.ToJavaList())
				{
					if (item.getKeyOfEn().equals("OID"))
					{
						continue;
					}
					if (item.getUIVisible()== false)
					{
						continue;
					}

					sb.append("<th stylle='width:" + item.getUIWidthInt() + "px;'>" + item.getName() + "</th>");
				}
				sb.append("</tr>");
				/// 输出标题.


				///输出数据.
				GEDtls dtls = new GEDtls(gf.getCtrlID());
				dtls.Retrieve(GEDtlAttr.RefPK, workid,"OID");
				for (GEDtl dtl : dtls.ToJavaList())
				{
					sb.append("<tr>");

					for (MapAttr item : attrsOfDtls.ToJavaList())
					{
						if (item.getKeyOfEn().equals("OID") || item.getUIVisible()== false)
						{
							continue;
						}

						String text = "";

						switch (item.getLGType())
						{
							case Normal: // 输出普通类型字段.
								if (item.getMyDataType() == 1 && item.getUIContralType().getValue()== DataType.AppString)
								{

									if (attrs.Contains(item.getKeyOfEn() + "Text") == true)
									{
										text = en.GetValRefTextByKey(item.getKeyOfEn());
									}
									if (DataType.IsNullOrEmpty(text))
									{
										if (attrs.Contains(item.getKeyOfEn() + "T") == true)
										{
											text = en.GetValStrByKey(item.getKeyOfEn() + "T");
										}
									}
								}
								else
								{

									text = en.GetValStrByKey(item.getKeyOfEn());

									if (item.getIsRichText() == true)
									{
										text = text.replace("white-space: nowrap;", "");
									}
								}

								break;
							case Enum:
								if (item.getUIContralType()== UIContralType.CheckBok)
								{
									String s = en.GetValStrByKey(item.getKeyOfEn()) + ",";
									SysEnums enums = new SysEnums(item.getUIBindKey());
									for (SysEnum se : enums.ToJavaList())
									{
										if (s.indexOf(se.getIntKey() + ",") != -1)
										{
											text += se.getLab() + " ";
										}
									}

								}
								else
								{
									text = en.GetValRefTextByKey(item.getKeyOfEn());
								}
								break;
							case FK:
								text = en.GetValRefTextByKey(item.getKeyOfEn());
								break;
							default:
								break;
						}


						if (item.getUIContralType()== UIContralType.DDL)
						{
							sb.append("<td>" + text + "</td>");
							continue;
						}

						if (item.getIsNum())
						{
							sb.append("<td style='text-align:right' >" + text + "</td>");
							continue;
						}

						sb.append("<td>" + text + "</td>");
					}
					sb.append("</tr>");
				}
				/// 输出数据.


				sb.append("</table>");

				sb.append(" </td>");
				sb.append(" </tr>");
			}
			/// 如果是从表.

			///如果是附件.
			if (gf.getCtrlType().equals("Ath"))
			{
				if (DataType.IsNullOrEmpty(gf.getCtrlID()) == true)
				{
					continue;
				}
				FrmAttachment ath = new FrmAttachment();
				ath.setMyPK(gf.getCtrlID());
				if(ath.RetrieveFromDBSources()==0)
					continue;
				if (ath.getIsVisable() == false)
				{
					continue;
				}

				sb.append(" <tr>");
				sb.append("  <th colspan=4><b>" + gf.getLab() + "</b></th>");
				sb.append(" </tr>");

				FrmAttachmentDBs athDBs = bp.wf.Glo.GenerFrmAttachmentDBs(ath, String.valueOf(workid), ath.getMyPK());


				if (ath.getUploadType() == AttachmentUploadType.Single)
				{
					/* 单个文件 */
					sb.append("<tr><td colspan=4>单附件没有转化:" + ath.getMyPK() + "</td></td>");
					continue;
				}

				if (ath.getUploadType() == AttachmentUploadType.Multi)
				{
					sb.append("<tr><td valign=top colspan=4 >");
					sb.append("<ul>");

					//判断是否有这个目录.
					if ((new File(path + "/pdf/")).isDirectory() == false)
					{
						(new File(path + "/pdf/")).mkdirs();
					}

					for (FrmAttachmentDB item : athDBs.ToJavaList())
					{
						String fileTo = path + "/pdf/" + item.getFileName();
						//加密信息
						boolean fileEncrypt = SystemConfig.getIsEnableAthEncrypt();
						boolean isEncrypt = item.GetParaBoolen("IsEncrypt");
						///从ftp服务器上下载.
						if (ath.getAthSaveWay() == AthSaveWay.FTPServer)
						{
							try
							{
								if ((new File(fileTo)).isFile() == true)
								{
									(new File(fileTo)).delete(); //rn "err@删除已经存在的文件错误,请检查iis的权限:" + ex.getMessage();
								}

								//把文件copy到,                                  
								String file = item.GenerTempFile(ath.getAthSaveWay());

								String fileTempDecryPath = file;
								if (fileEncrypt == true && isEncrypt == true)
								{
									fileTempDecryPath = file + ".tmp";
									AesEncodeUtil.decryptFile(file, fileTempDecryPath);

								}
								Files.copy(Paths.get(fileTempDecryPath), Paths.get(fileTo), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

								sb.append("<li><a href='" + SystemConfig.GetValByKey("HostURL", "") + "/DataUser/InstancePacketOfData/" + FK_Node + "/" + workid + "/" + "pdf/" + item.getFileName() + "'>" + item.getFileName() + "</a></li>");
							}
							catch (RuntimeException ex)
							{
								sb.append("<li>" + item.getFileName() + "(<font color=red>文件未从ftp下载成功{" + ex.getMessage() + "}</font>)</li>");
							}
						}
						/// 从ftp服务器上下载.


						///从iis服务器上下载.
						if (ath.getAthSaveWay() == AthSaveWay.IISServer)
						{
							try
							{

								String fileTempDecryPath = item.getFileFullName();
								if (fileEncrypt == true && isEncrypt == true)
								{
									fileTempDecryPath = item.getFileFullName() + ".tmp";
									AesEncodeUtil.decryptFile(item.getFileFullName(), fileTempDecryPath);

								}

								//把文件copy到,
								Files.copy(Paths.get(fileTempDecryPath), Paths.get(fileTo), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

								sb.append("<li><a href='" + SystemConfig.GetValByKey("HostURL", "") + "/DataUser/InstancePacketOfData/" + frmID + "/" + workid + "/" + "pdf/" + item.getFileName() + "'>" + item.getFileName() + "</a></li>");
							}
							catch (RuntimeException ex)
							{
								sb.append("<li>" + item.getFileName() + "(<font color=red>文件未从web下载成功{" + ex.getMessage() + "}</font>)</li>");
							}
						}

					}
					sb.append("</ul>");
					sb.append("</td></tr>");
				}

			}
			/// 如果是附件.

			//如果是IFrame页面
			if (gf.getCtrlType().equals("Frame") && flowNo != null)
			{
				if (DataType.IsNullOrEmpty(gf.getCtrlID()) == true)
				{
					continue;
				}
				sb.append("<tr>");
				sb.append("  <td colspan='4' >");

				//根据GroupID获取对应的
				MapFrame frame = new MapFrame(gf.getCtrlID());
				//获取URL
				String url = frame.getURL();

				//替换URL的
				url = url.replace("@basePath", basePath);
				//替换系统参数
				url = url.replace("@WebUser.No", WebUser.getNo());
				url = url.replace("@WebUser.Name;", WebUser.getName());
				url = url.replace("@WebUser.FK_DeptName;", WebUser.getFK_DeptName());
				url = url.replace("@WebUser.FK_Dept;", WebUser.getFK_Dept());

				//替换参数
				if (url.indexOf("?") > 0)
				{
					//获取url中的参数
					url = url.substring(url.indexOf('?'));
					String[] paramss = url.split("[&]", -1);
					for (String param : paramss)
					{
						if (DataType.IsNullOrEmpty(param) || param.indexOf("@") == -1)
						{
							continue;
						}
						String[] paramArr = param.split("[=]", -1);
						if (paramArr.length == 2 && paramArr[1].indexOf('@') == 0)
						{
							if (paramArr[1].indexOf("@WebUser.") == 0)
							{
								continue;
							}
							url = url.replace(paramArr[1], en.GetValStrByKey(paramArr[1].substring(1)));
						}
					}

				}
				sb.append("<iframe style='width:100%;height:auto;' ID='" + frame.getMyPK() + "'    src='" + url + "' frameborder=0  leftMargin='0'  topMargin='0' scrolling=auto></iframe></div>");
				sb.append("</td>");
				sb.append("</tr>");
			}


			///审核组件
			if (gf.getCtrlType().equals("FWC") && flowNo != null)
			{
				NodeWorkCheck fwc = new NodeWorkCheck(frmID);

				String sql = "";
				DataTable dtTrack = null;
				boolean bl = false;
				try
				{
					bl = DBAccess.IsExitsTableCol("Port_Emp", "SignType");
				}
				catch (RuntimeException ex)
				{

				}
				if (bl)
				{
					String tTable = "ND" + Integer.parseInt(flowNo) + "Track";
					sql = "SELECT a.No, a.SignType FROM Port_Emp a, " + tTable + " b WHERE a.No=b.EmpFrom AND B.WorkID=" + workid;

					dtTrack = DBAccess.RunSQLReturnTable(sql);
					dtTrack.TableName = "SignType";
					if (dtTrack.Columns.contains("No") == false)
					{
						dtTrack.Columns.Add("No");
					}
					if (dtTrack.Columns.contains("SignType") == false)
					{
						dtTrack.Columns.Add("SignType");
					}
				}

				String html = ""; // "<table style='width:100%;valign:middle;height:auto;' >";

				///生成审核信息.
				sql = "SELECT NDFromT,Msg,RDT,EmpFromT,EmpFrom,NDFrom FROM ND" + Integer.parseInt(flowNo) + "Track WHERE WorkID=" + workid + " AND ActionType=" + ActionType.WorkCheck.getValue() + " ORDER BY RDT ";
				DataTable dt = DBAccess.RunSQLReturnTable(sql);

				//获得当前待办的人员,把当前审批的人员排除在外,不然就有默认同意的意见可以打印出来.
				sql = "SELECT FK_Emp, FK_Node FROM WF_GenerWorkerList WHERE IsPass!=1 AND WorkID=" + workid;
				DataTable dtOfTodo = DBAccess.RunSQLReturnTable(sql);

				for (DataRow dr : dt.Rows)
				{
					///排除正在审批的人员.
					String nodeID = dr.getValue("NDFrom").toString();
					String empFrom = dr.getValue("EmpFrom").toString();
					if (dtOfTodo.Rows.size() != 0)
					{
						boolean isHave = false;
						for (DataRow mydr : dtOfTodo.Rows)
						{
							if (!mydr.getValue("FK_Node").toString().equals(nodeID))
							{
								continue;
							}

							if (!mydr.getValue("FK_Emp").toString().equals(empFrom))
							{
								continue;
							}
							isHave = true;
						}

						if (isHave == true)
						{
							continue;
						}
					}
					/// 排除正在审批的人员.


					html += "<tr>";
					html += " <td valign=middle class='FContext'>" + dr.getValue("NDFromT") + "</td>";

					String msg = dr.getValue("Msg").toString();

					msg += "<br>";
					msg += "<br>";

					String empStrs = "";
					if (dtTrack == null)
					{
						empStrs = dr.getValue("EmpFromT").toString();
					}
					else
					{
						String singType = "0";
						for (DataRow drTrack : dtTrack.Rows)
						{
							if (drTrack.getValue("No").toString().equals(dr.getValue("EmpFrom").toString()))
							{
								singType = drTrack.getValue("SignType").toString();
								break;
							}
						}

						if (singType.equals("0") || singType.equals("2"))
						{
							empStrs = dr.getValue("EmpFromT").toString();
						}


						if (singType.equals("1"))
						{
							String src = SystemConfig.getHostURL() + "/DataUser/Siganture/";
							empStrs = "<img src='" + src + dr.getValue("EmpFrom") + ".JPG' title='" + dr.getValue("EmpFromT") + "' style='height:60px;'  alt='图片丢失' /> ";
						}

					}
					msg += "审核人:" + empStrs + " &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;日期:" + dr.getValue("RDT").toString();

					html += " <td colspan=3 valign=middle style='font-size:18px'>" + msg + "</td>";
					html += " </tr>";
				}
				/// 生成审核信息.

				sb.append(" " + html);
			}
		}

		sb.append("</table>");
		return sb;
	}

	private static String GetDtlHtmlByID(MapDtl dtl, long workid, float width) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		MapAttrs attrsOfDtls = new MapAttrs(dtl.getNo());
		int columNum = 0;
		for (MapAttr item : attrsOfDtls.ToJavaList())
		{
			if (item.getKeyOfEn().equals("OID"))
			{
				continue;
			}
			if (item.getUIVisible()== false)
			{
				continue;
			}
			columNum++;
		}
		int columWidth = (int)width * (100 / columNum);

		sb.append("<table style='wdith:100%' >");
		sb.append("<tr>");

		for (MapAttr item : attrsOfDtls.ToJavaList())
		{
			if (item.getKeyOfEn().equals("OID"))
			{
				continue;
			}
			if (item.getUIVisible()== false)
			{
				continue;
			}
			sb.append("<th class='DtlTh' style='width:" + columWidth + "px'>" + item.getName() + "</th>");
		}
		sb.append("</tr>");
		/// 输出标题.


		///输出数据.
		GEDtls gedtls = new GEDtls(dtl.getNo());
		gedtls.Retrieve(GEDtlAttr.RefPK, workid, "OID");
		for (GEDtl gedtl : gedtls.ToJavaList())
		{
			sb.append("<tr>");

			for (MapAttr item : attrsOfDtls.ToJavaList())
			{
				if (item.getKeyOfEn().equals("OID") || item.getUIVisible()== false)
				{
					continue;
				}

				if (item.getUIContralType()== UIContralType.DDL)
				{
					sb.append("<td class='DtlTd'>" + gedtl.GetValRefTextByKey(item.getKeyOfEn()) + "</td>");
					continue;
				}

				if (item.getIsNum())
				{
					sb.append("<td class='DtlTd' style='text-align:right;' >" + gedtl.GetValStrByKey(item.getKeyOfEn()) + "</td>");
					continue;
				}

				sb.append("<td class='DtlTd' >" + gedtl.GetValStrByKey(item.getKeyOfEn()) + "</td>");
			}
			sb.append("</tr>");
		}
		/// 输出数据.


		sb.append("</table>");


		sb.append("</span>");
		return sb.toString();
	}

	private static String GetAthHtmlByID(FrmAttachment ath, long workid, String path) throws IOException, Exception {
		StringBuilder sb = new StringBuilder();

		if (ath.getUploadType() == AttachmentUploadType.Multi) {

			// 判断是否有这个目录.
			if ((new File(path + "/pdf/")).isDirectory() == false) {
				(new File(path + "/pdf/")).mkdirs();
			}

			// 文件加密
			boolean fileEncrypt = SystemConfig.getIsEnableAthEncrypt();
			FrmAttachmentDBs athDBs = bp.wf.Glo.GenerFrmAttachmentDBs(ath, String.valueOf(workid), ath.getMyPK());
			sb.append("<table id = 'ShowTable' class='table' style='width:100%'>");
			sb.append("<thead><tr style = 'border:0px;'>");
			sb.append(
					"<th style='width:50px; border: 1px solid #ddd;padding:8px;background-color:white' nowrap='true'>序</th>");
			sb.append(
					"<th style = 'min -width:200px; border: 1px solid #ddd;padding:8px;background-color:white' nowrap='true'>文件名</th>");
			sb.append(
					"<th style = 'width:50px; border: 1px solid #ddd;padding:8px;background-color:white' nowrap='true'>大小KB</th>");
			sb.append(
					"<th style = 'width:120px; border: 1px solid #ddd;padding:8px;background-color:white' nowrap='true'>上传时间</th>");
			sb.append(
					"<th style = 'width:80px; border: 1px solid #ddd;padding:8px;background-color:white' nowrap='true'>上传人</th>");
			sb.append("</thead>");
			sb.append("<tbody>");
			int idx = 0;
			for (FrmAttachmentDB item : athDBs.ToJavaList()) {
				idx++;
				sb.append("<tr>");
				sb.append("<td class='Idx'>" + idx + "</td>");
				// 获取文件是否加密
				boolean isEncrypt = item.GetParaBoolen("IsEncrypt");
				if (ath.getAthSaveWay() == AthSaveWay.FTPServer) {
					try {
						String toFile = path + "/pdf/" + item.getFileName();
						if ((new File(toFile)).isFile() == false) {
							// 获取文件是否加密
							String file = item.GenerTempFile(ath.getAthSaveWay());
							String fileTempDecryPath = file;
							if (fileEncrypt == true && isEncrypt == true) {
								fileTempDecryPath = file + ".tmp";
								AesEncodeUtil.decryptFile(file, fileTempDecryPath);

							}

							Files.copy(Paths.get(fileTempDecryPath), Paths.get(toFile),
									StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
						}

						sb.append("<td  title='" + item.getFileName() + "'>" + item.getFileName() + "</td>");
					} catch (RuntimeException ex) {
						sb.append("<td>" + item.getFileName() + "(<font color=red>文件未从ftp下载成功{" + ex.getMessage()
								+ "}</font>)</td>");
					}
				}

				if (ath.getAthSaveWay() == AthSaveWay.IISServer) {
					try {
						String toFile = path + "/pdf/" + item.getFileName();
						if ((new File(toFile)).isFile() == false) {
							// 把文件copy到,
							String fileTempDecryPath = item.getFileFullName();
							if (fileEncrypt == true && isEncrypt == true) {
								fileTempDecryPath = item.getFileFullName() + ".tmp";
								AesEncodeUtil.decryptFile(item.getFileFullName(), fileTempDecryPath);

							}

							// 把文件copy到,
							Files.copy(Paths.get(fileTempDecryPath), Paths.get(path + "/pdf/" + item.getFileName()),
									StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
						}
						sb.append("<td>" + item.getFileName() + "</td>");
					} catch (RuntimeException ex) {
						sb.append("<td>" + item.getFileName() + "(<font color=red>文件未从ftp下载成功{" + ex.getMessage()
								+ "}</font>)</td>");
					}
				}
				sb.append("<td>" + item.getFileSize() + "KB</td>");
				sb.append("<td>" + item.getRDT() + "</td>");
				sb.append("<td>" + item.getRecName() + "</td>");
				sb.append("</tr>");

			}
			sb.append("</tbody>");

			sb.append("</table>");
		}
		return sb.toString();
	}

	/**
	 * 树形表单转成PDF.
	 * @throws Exception 
	 */

	public static String MakeCCFormToPDF(Node node, long workid, String flowNo, String fileNameFormat,
			boolean urlIsHostUrl, String basePath) throws Exception {
		return MakeCCFormToPDF(node, workid, flowNo, fileNameFormat, urlIsHostUrl, basePath, null);
	}

	
	public static String MakeCCFormToPDF(Node node, long workid, String flowNo, String fileNameFormat,
			boolean urlIsHostUrl, String basePath, String htmlString) throws Exception {
		// 根据节点信息获取表单方案
		MapData md = new MapData("ND" + node.getNodeID());
		String resultMsg = "";
		GenerWorkFlow gwf = null;

		// 获取主干流程信息
		if (flowNo != null) {
			gwf = new GenerWorkFlow(workid);
		}

		// 存放信息地址
		String hostURL = SystemConfig.GetValByKey("HostURL", "");
		String path = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + "ND" + node.getNodeID() + "/"
				+ workid;
		String frmID = node.getNodeFrmID();

		// 处理正确的文件名.
		if (fileNameFormat == null) {
			if (flowNo != null) {
				fileNameFormat = DBAccess.RunSQLReturnStringIsNull(
						"SELECT Title FROM WF_GenerWorkFlow WHERE WorkID=" + workid, "" + String.valueOf(workid));
			} else {
				fileNameFormat = String.valueOf(workid);
			}
		}

		if (DataType.IsNullOrEmpty(fileNameFormat) == true) {
			fileNameFormat = String.valueOf(workid);
		}

		fileNameFormat = bp.da.DataType.PraseStringToFileName(fileNameFormat);

		Hashtable ht = new Hashtable();

		if (node.getHisFormType().getValue() == NodeFormType.FoolForm.getValue()
				|| node.getHisFormType().getValue() == NodeFormType.FreeForm.getValue()
				|| node.getHisFormType().getValue() == NodeFormType.RefOneFrmTree.getValue()
				|| node.getHisFormType().getValue() == NodeFormType.FoolTruck.getValue()
				|| node.getHisFormType() == NodeFormType.Develop) {
			resultMsg = setPDFPath("ND" + node.getNodeID(), workid, flowNo, gwf);
			if (resultMsg.indexOf("err@") != -1) {
				return resultMsg;
			}

			String billUrl = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + "ND" + node.getNodeID()
					+ "/" + workid + "/index.htm";

			resultMsg = MakeHtmlDocument(frmID, workid, flowNo, fileNameFormat, urlIsHostUrl, path, billUrl,
					"ND" + node.getNodeID(), basePath, htmlString);

			if (resultMsg.indexOf("err@") != -1) {
				return resultMsg;
			}

			ht.put("htm", SystemConfig.GetValByKey("HostURLOfBS", "../../DataUser") + "/InstancePacketOfData/" + "ND"
					+ node.getNodeID() + "/" + workid + "/index.htm");

			/// 把所有的文件做成一个zip文件.
			// 生成pdf文件
			String pdfPath = path + "/pdf";

			if ((new File(pdfPath)).isDirectory() == false) {
				(new File(pdfPath)).mkdirs();
			}

			String pdfFile = pdfPath + "/" + fileNameFormat + ".pdf";
			String pdfFileExe = SystemConfig.getPathOfDataUser() + "ThirdpartySoftware/wkhtmltox/wkhtmltopdf.exe";
			try {
				Html2Pdf(pdfFileExe, billUrl, pdfFile);
				if (urlIsHostUrl == false) {
					ht.put("pdf",
							SystemConfig.GetValByKey("HostURLOfBS", "../../DataUser/") + "InstancePacketOfData/" + "ND"
									+ node.getNodeID() + "/" + workid + "/pdf/"
									+ DataType.PraseStringToUrlFileName(fileNameFormat) + ".pdf");
				} else {
					ht.put("pdf",
							SystemConfig.GetValByKey("HostURL", "") + "/DataUser/InstancePacketOfData/" + "ND"
									+ node.getNodeID() + "/" + workid + "/pdf/"
									+ DataType.PraseStringToUrlFileName(fileNameFormat) + ".pdf");
				}
			} catch (RuntimeException ex) {
				throw new RuntimeException("err@html转PDF错误:PDF的路径" + pdfPath + "可能抛的异常" + ex.getMessage());
			}

			// 生成压缩文件
			String zipFile = path + "/../" + fileNameFormat + ".zip";

			File finfo = new File(zipFile);
			ZipFilePath = finfo.getPath(); // 文件路径.

		  File zipFileFile = new File(zipFile);
    		try {
    			while (zipFileFile.exists() == true) {
    				zipFileFile.delete();
    			}
    			// 执行压缩.
    			ZipCompress fz = new ZipCompress(zipFile, pdfPath);
    			fz.zip();
    			ht.put("zip", SystemConfig.GetValByKey("HostURL","") + "/DataUser/InstancePacketOfData/" + frmID + "/" + workid +"/"+ DataType.PraseStringToUrlFileName(fileNameFormat) + ".zip");
    		} catch (Exception ex) {
    			ht.put("zip","err@执行压缩出现错误:" + ex.getMessage() + ",路径tempPath:" + pdfPath + ",zipFile=" + finfo.getName());
    		}

    		if (zipFileFile.exists() == false)
    			ht.put("zip","err@压缩文件未生成成功,请在点击一次.");

            
            //把所有的文件做成一个zip文件.
            
            return bp.tools.Json.ToJsonEntitiesNoNameMode(ht);
		}

		if (node.getHisFormType().getValue() == NodeFormType.SheetTree.getValue()) {

			// 生成pdf文件
			String pdfPath = path + "/pdf";
			String pdfTempPath = path + "/pdfTemp";

			DataRow dr = null;
			resultMsg = setPDFPath("ND" + node.getNodeID(), workid, flowNo, gwf);
			if (resultMsg.indexOf("err@") != -1) {
				return resultMsg;
			}

			// 获取绑定的表单
			FrmNodes nds = new FrmNodes(node.getFK_Flow(), node.getNodeID());
			for (FrmNode item : nds.ToJavaList()) {
				// 判断当前绑定的表单是否启用
				if (item.getFrmEnableRoleInt() == FrmEnableRole.Disable.getValue()) {
					continue;
				}

				// 判断 who is pk
				if (flowNo != null && item.getWhoIsPK() == WhoIsPK.PWorkID) // 如果是父子流程
				{
					workid = gwf.getPWorkID();
				}
				// 获取表单的信息执行打印
				String billUrl = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + "ND" + node.getNodeID()
						+ "/" + workid + "/" + item.getFK_Frm() + "index.htm";
				resultMsg = MakeHtmlDocument(item.getFK_Frm(), workid, flowNo, fileNameFormat, urlIsHostUrl, path,
						billUrl, "ND" + node.getNodeID(), basePath);

				if (resultMsg.indexOf("err@") != -1) {
					return resultMsg;
				}

				ht.put("htm_" + item.getFK_Frm(),
						SystemConfig.GetValByKey("HostURLOfBS", "../../DataUser/") + "/InstancePacketOfData/" + "ND"
								+ node.getNodeID() + "/" + workid + "/" + item.getFK_Frm() + "index.htm");

				/// 把所有的文件做成一个zip文件.
				if ((new File(pdfTempPath)).isDirectory() == false) {
					(new File(pdfTempPath)).mkdirs();
				}

				fileNameFormat = fileNameFormat.substring(0, fileNameFormat.length() - 1);
				String pdfFormFile = pdfTempPath + "/" + item.getFK_Frm() + ".pdf";
				String pdfFileExe = SystemConfig.getPathOfDataUser() + "ThirdpartySoftware/wkhtmltox/wkhtmltopdf.exe";
				try {
					Html2Pdf(pdfFileExe, resultMsg, pdfFormFile);

				} catch (RuntimeException ex) {
					/* 有可能是因为文件路径的错误， 用补偿的方法在执行一次, 如果仍然失败，按照异常处理. */
					Html2Pdf(pdfFileExe, resultMsg, pdfFormFile);
				}

			}

			// pdf合并
			 String pdfFile = pdfPath + "/" + fileNameFormat + ".pdf"; 
    		//开始合并处理
    		 if (new File(pdfPath).exists() == false)
	        	 new File(pdfPath).mkdirs();
    		 
    		 PDFMergerUtility merger=new PDFMergerUtility();
    		 String[] fileInFolder=BaseFileUtils.getFiles(pdfTempPath);
    		 for(int i=0;i<fileInFolder.length;i++){
    			merger.addSource(fileInFolder[i]);
    		  }
    		 merger.setDestinationFileName(pdfFile);
    		 merger.mergeDocuments();
    		 
    		 //合并完删除文件夹
    		 BaseFileUtils.deleteDirectory(pdfTempPath);
    		 if (urlIsHostUrl == false)
    			 ht.put("pdf", SystemConfig.GetValByKey("HostURLOfBS","../../DataUser/") + "InstancePacketOfData/" + frmID + "/" + workid + "/pdf/" + DataType.PraseStringToUrlFileName(fileNameFormat) + ".pdf");
             else
            	 ht.put("pdf", SystemConfig.GetValByKey("HostURL","") + "/DataUser/InstancePacketOfData/" + frmID + "/" + workid + "/pdf/" + DataType.PraseStringToUrlFileName(fileNameFormat) + ".pdf");
    		
    		//生成压缩文件
             String zipFile = path + "/" + fileNameFormat + ".zip";

             File finfo = new File(zipFile);
             ZipFilePath =finfo.getName();
             
             File zipFileFile = new File(zipFile);
     		try {
     			while (zipFileFile.exists() == true) {
     				zipFileFile.delete();
     			}
     			// 执行压缩.
     			ZipCompress fz = new ZipCompress(zipFile, pdfPath);
     			fz.zip();
     			ht.put("zip", SystemConfig.GetValByKey("HostURL","") + "/DataUser/InstancePacketOfData/" + frmID + "/" + workid +"/"+ DataType.PraseStringToUrlFileName(fileNameFormat) + ".zip");
     		} catch (Exception ex) {
     			ht.put("zip","err@执行压缩出现错误:" + ex.getMessage() + ",路径tempPath:" + pdfPath + ",zipFile=" + finfo.getName());
     		}

     		if (zipFileFile.exists() == false)
     			ht.put("zip","err@压缩文件未生成成功,请在点击一次.");
     		
     		

			return bp.tools.Json.ToJsonEntitiesNoNameMode(ht);
		}

		return "warning@不存在需要打印的表单";

	}

	public static String MakeBillToPDF(String frmId, long workid, String basePath, boolean urlIsHostUrl) throws Exception {
		return MakeBillToPDF(frmId, workid, basePath, urlIsHostUrl, null);
	}

	public static String MakeBillToPDF(String frmId, long workid, String basePath) throws Exception {
		return MakeBillToPDF(frmId, workid, basePath, false, null);
	}


	public static String MakeBillToPDF(String frmId, long workid, String basePath, boolean urlIsHostUrl,
			String htmlString) throws Exception {
		String resultMsg = "";

		// 获取单据的属性信息
		bp.ccbill.FrmBill bill = new bp.ccbill.FrmBill(frmId);
		String fileNameFormat = null;

		// 存放信息地址
		String path = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + bill.getNo() + "/" + workid;

		// 处理正确的文件名.
		if (fileNameFormat == null) {
			fileNameFormat = DBAccess.RunSQLReturnStringIsNull("SELECT Title FROM Frm_GenerBill WHERE WorkID=" + workid,
					"" + String.valueOf(workid));
		}

		if (DataType.IsNullOrEmpty(fileNameFormat) == true) {
			fileNameFormat = String.valueOf(workid);
		}

		fileNameFormat = bp.da.DataType.PraseStringToFileName(fileNameFormat);

		Hashtable ht = new Hashtable();

		// 生成pdf文件
		String pdfPath = path + "/pdf";

		DataRow dr = null;
		resultMsg = setPDFPath(frmId, workid, null, null);
		if (resultMsg.indexOf("err@") != -1) {
			return resultMsg;
		}

		// 获取表单的信息执行打印
		String billUrl = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + bill.getNo() + "/" + workid + "/"
				+ "index.htm";
		resultMsg = MakeHtmlDocument(bill.getNo(), workid, null, fileNameFormat, urlIsHostUrl, path, billUrl, frmId,
				basePath, htmlString);

		if (resultMsg.indexOf("err@") != -1) {
			return resultMsg;
		}

		ht.put("htm", SystemConfig.GetValByKey("HostURLOfBS", "../../DataUser/") + "InstancePacketOfData/" + frmId + "/"
				+ workid + "/" + "index.htm");

		/// 把所有的文件做成一个zip文件.
		if ((new File(pdfPath)).isDirectory() == false) {
			(new File(pdfPath)).mkdirs();
		}

		fileNameFormat = fileNameFormat.substring(0, fileNameFormat.length() - 1);
		String pdfFormFile = pdfPath + "/" + bill.getName() + ".pdf"; // 生成的路径.
		String pdfFileExe = SystemConfig.getPathOfDataUser() + "ThirdpartySoftware/wkhtmltox/wkhtmltopdf.exe";
		try {
			Html2Pdf(pdfFileExe, resultMsg, pdfFormFile);
			if (urlIsHostUrl == false) {
				ht.put("pdf", SystemConfig.GetValByKey("HostURLOfBS", "../../DataUser/") + "InstancePacketOfData/"
						+ frmId + "/" + workid + "/pdf/" + bill.getName() + ".pdf");
			} else {
				ht.put("pdf", SystemConfig.GetValByKey("HostURL", "") + "/DataUser/InstancePacketOfData/" + frmId + "/"
						+ workid + "/pdf/" + bill.getName() + ".pdf");
			}
		} catch (RuntimeException ex) {
			/* 有可能是因为文件路径的错误， 用补偿的方法在执行一次, 如果仍然失败，按照异常处理. */
			fileNameFormat = DBAccess.GenerGUID();
			pdfFormFile = pdfPath + "/" + fileNameFormat + ".pdf";

			Html2Pdf(pdfFileExe, resultMsg, pdfFormFile);
			ht.put("pdf", SystemConfig.GetValByKey("HostURLOfBS", "") + "/InstancePacketOfData/" + frmId + "/" + workid
					+ "/pdf/" + bill.getName() + ".pdf");
		}

		// 生成压缩文件
		String zipFile = path + "/../" + fileNameFormat + ".zip";

		File finfo = new File(zipFile);
		ZipFilePath = finfo.getPath(); // 文件路径.
		 File zipFileFile = new File(zipFile);

		try {
			while (zipFileFile.exists() == true) {
 				zipFileFile.delete();
 			}
 			// 执行压缩.
 			ZipCompress fz = new ZipCompress(zipFile, pdfPath);
 			fz.zip();
 			
		
			ht.put("zip", SystemConfig.getHostURLOfBS() + "/DataUser/InstancePacketOfData/" + frmId + "/"
					+ DataType.PraseStringToUrlFileName(fileNameFormat) + ".zip");
		} catch (RuntimeException ex) {
			ht.put("zip", "err@生成zip文件遇到权限问题:" + ex.getMessage() + " @Path:" + pdfPath);
		}
		return bp.tools.Json.ToJsonEntitiesNoNameMode(ht);
	}

	public static String MakeFormToPDF(String frmId, String frmName, Node node, long workid, String flowNo,
			String fileNameFormat, boolean urlIsHostUrl, String basePath) throws Exception {

		String resultMsg = "";
		GenerWorkFlow gwf = null;

		// 获取主干流程信息
		if (flowNo != null) {
			gwf = new GenerWorkFlow(workid);
		}

		// 存放信息地址
		String hostURL = SystemConfig.GetValByKey("HostURL", "");
		String path = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + "ND" + node.getNodeID() + "/"
				+ workid;

		// 处理正确的文件名.
		if (fileNameFormat == null) {
			if (flowNo != null) {
				fileNameFormat = DBAccess.RunSQLReturnStringIsNull(
						"SELECT Title FROM WF_GenerWorkFlow WHERE WorkID=" + workid, "" + String.valueOf(workid));
			} else {
				fileNameFormat = String.valueOf(workid);
			}
		}

		if (DataType.IsNullOrEmpty(fileNameFormat) == true) {
			fileNameFormat = String.valueOf(workid);
		}

		fileNameFormat = bp.da.DataType.PraseStringToFileName(fileNameFormat);

		Hashtable ht = new Hashtable();

		// 生成pdf文件
		String pdfPath = path + "/pdf";

		DataRow dr = null;
		resultMsg = setPDFPath("ND" + node.getNodeID(), workid, flowNo, gwf);
		if (resultMsg.indexOf("err@") != -1) {
			return resultMsg;
		}

		// 获取绑定的表单
		FrmNode frmNode = new FrmNode();
		frmNode.Retrieve(FrmNodeAttr.FK_Frm, frmId);

		// 判断当前绑定的表单是否启用
		if (frmNode.getFrmEnableRoleInt() == FrmEnableRole.Disable.getValue()) {
			return "warning@" + frmName + "没有被启用";
		}

		// 判断 who is pk
		if (flowNo != null && frmNode.getWhoIsPK() == WhoIsPK.PWorkID) // 如果是父子流程
		{
			workid = gwf.getPWorkID();
		}

		// 获取表单的信息执行打印
		String billUrl = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + "ND" + node.getNodeID() + "/"
				+ workid + "/" + frmNode.getFK_Frm() + "index.htm";
		resultMsg = MakeHtmlDocument(frmNode.getFK_Frm(), workid, flowNo, fileNameFormat, urlIsHostUrl, path, billUrl,
				"ND" + node.getNodeID(), basePath);

		if (resultMsg.indexOf("err@") != -1) {
			return resultMsg;
		}

		// ht.Add("htm", SystemConfig.GetValByKey("HostURLOfBS",
		// "../../DataUser/") + "/InstancePacketOfData/" + "ND" + node.NodeID +
		// "/" + workid + "/" + frmNode.FK_Frm + "index.htm");

		/// 把所有的文件做成一个zip文件.
		if ((new File(pdfPath)).isDirectory() == false) {
			(new File(pdfPath)).mkdirs();
		}

		fileNameFormat = fileNameFormat.substring(0, fileNameFormat.length() - 1);
		String pdfFormFile = pdfPath + "/" + frmNode.getFK_Frm() + ".pdf";
		String pdfFileExe = SystemConfig.getPathOfDataUser() + "ThirdpartySoftware/wkhtmltox/wkhtmltopdf.exe";
		try {
			Html2Pdf(pdfFileExe, resultMsg, pdfFormFile);
			if (urlIsHostUrl == false) {
				ht.put("pdf", SystemConfig.GetValByKey("HostURLOfBS", "../../DataUser/") + "InstancePacketOfData/"
						+ "ND" + node.getNodeID() + "/" + workid + "/pdf/" + frmNode.getFK_Frm() + ".pdf");
			} else {
				ht.put("pdf", SystemConfig.GetValByKey("HostURL", "") + "/DataUser/InstancePacketOfData/" + "ND"
						+ node.getNodeID() + "/" + workid + "/pdf/" + frmNode.getFK_Frm() + ".pdf");
			}

		} catch (RuntimeException ex) {
			/* 有可能是因为文件路径的错误， 用补偿的方法在执行一次, 如果仍然失败，按照异常处理. */
			fileNameFormat = DBAccess.GenerGUID();
			pdfFormFile = pdfPath + "/" + fileNameFormat + ".pdf";

			Html2Pdf(pdfFileExe, resultMsg, pdfFormFile);
			ht.put("pdf", SystemConfig.GetValByKey("HostURLOfBS", "") + "/InstancePacketOfData/" + "ND"
					+ node.getNodeID() + "/" + workid + "/pdf/" + frmNode.getFK_Frm() + ".pdf");
		}

		return bp.tools.Json.ToJsonEntitiesNoNameMode(ht);

	}

	
	// 前期文件的准备
	private static String setPDFPath(String frmID, long workid, String flowNo, GenerWorkFlow gwf) throws Exception {
		// 准备目录文件.
		String path = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + frmID + "/";
		try {

			path = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + frmID + "/";
			if ((new File(path)).isDirectory() == false) {
				(new File(path)).mkdirs();
			}

			path = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/" + frmID + "/" + workid;
			if ((new File(path)).isDirectory() == false) {
				(new File(path)).mkdirs();
			}

			// 把模版文件copy过去.
			String templateFilePath = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/";
			File dir = new File(templateFilePath);
			File[] finfos = dir.listFiles();
			if (finfos.length == 0) {
				return "err@不存在模板文件";
			}
			for (File fl : finfos) {
				if (fl.getName().contains("ShuiYin")) {
					continue;
				}

				if (fl.getName().contains("htm")) {
					continue;
				}
				if ((new File(path + "/" + fl.getPath())).isFile() == true) {
					(new File(path + "/" + fl.getPath())).delete();
				}
				Files.copy(Paths.get(fl.getPath()), Paths.get(path + "/" + fl.getName()),
						StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			}

		} catch (RuntimeException ex) {
			return "err@读写文件出现权限问题，请联系管理员解决。" + ex.getMessage();
		}

		String hostURL = SystemConfig.GetValByKey("HostURL", "");
		String billUrl = hostURL + "/DataUser/InstancePacketOfData/" + frmID + "/" + workid + "/index.htm";

		// begin生成二维码.
		if(SystemConfig.GetValByKeyBoolen("IsShowQrCode",false) == true){
            /*说明是图片文件.*/
            String qrUrl = hostURL + "/WF/WorkOpt/PrintDocQRGuide.htm?FrmID=" + frmID + "&WorkID=" + workid + "&FlowNo=" + flowNo;
            if (flowNo != null)
            {
                gwf = new GenerWorkFlow(workid);
                qrUrl = hostURL + "/WF/WorkOpt/PrintDocQRGuide.htm?AP=" + frmID + "$" + workid + "_" + flowNo + "_" + gwf.getFK_Node() + "_" + gwf.getStarter() + "_" + gwf.getFK_Dept();
            }
            
            //二维码的生成
            QrCodeUtil.createQrCode(qrUrl,path,"QR.png");
        }
		// end生成二维码.
		return "";
	}

	

	/**
	 * zip文件路径.
	 */
	public static String ZipFilePath = "";

	public static String CCFlowAppPath = "/";

	public static String MakeHtmlDocument(String frmID, long workid, String flowNo, String fileNameFormat,
			boolean urlIsHostUrl, String path, String indexFile, String nodeID, String basePath) throws Exception {
		return MakeHtmlDocument(frmID, workid, flowNo, fileNameFormat, urlIsHostUrl, path, indexFile, nodeID, basePath,
				null);
	}

	
	public static String MakeHtmlDocument(String frmID, long workid, String flowNo, String fileNameFormat,
			boolean urlIsHostUrl, String path, String indexFile, String nodeID, String basePath, String htmlString) throws Exception {
		try {
			GenerWorkFlow gwf = null;
			if (flowNo != null) {
				gwf = new GenerWorkFlow(workid);
			}

			/// 定义变量做准备.
			// 生成表单信息.
			MapData mapData = new MapData(frmID);

			if (mapData.getHisFrmType() == FrmType.Url) {
				String url = mapData.getUrl();
				// 替换系统参数
				url = url.replace("@WebUser.No", WebUser.getNo());
				url = url.replace("@WebUser.Name;", WebUser.getName());
				url = url.replace("@WebUser.FK_DeptName;", WebUser.getFK_DeptName());
				url = url.replace("@WebUser.FK_Dept;", WebUser.getFK_Dept());

				// 替换参数
				if (url.indexOf("?") > 0) {
					// 获取url中的参数
					String urlParam = url.substring(url.indexOf('?'));
					String[] paramss = url.split("[&]", -1);
					for (String param : paramss) {
						if (DataType.IsNullOrEmpty(param) || param.indexOf("@") == -1) {
							continue;
						}
						String[] paramArr = param.split("[=]", -1);
						if (paramArr.length == 2 && paramArr[1].indexOf('@') == 0) {
							if (paramArr[1].indexOf("@WebUser.") == 0) {
								continue;
							}
							url = url.replace(paramArr[1], gwf.GetValStrByKey(paramArr[1].substring(1)));
						}
					}

				}
				url = url.replace("@basePath", basePath);
				if (url.contains("http") == false) {
					url = basePath + url;
				}

				String sb="<iframe style='width:100%;height:auto;' ID='" + mapData.getNo() + "'    src='" + url + "' frameborder=0  leftMargin='0'  topMargin='0' scrolling=auto></iframe></div>";
            	String  docs = DataType.ReadTextFile(SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/indexUrl.htm");
            	docs = docs.replace("@Docs", sb.toString());
            	docs = docs.replace("@Width", String.valueOf(mapData.getFrmW())+"px");
            	docs = docs.replace("@Height", String.valueOf(mapData.getFrmH())+"px");
            	if(gwf!=null)
            		docs = docs.replace("@Title", gwf.getTitle());
            	 DataType.WriteFile(indexFile, docs);
				return indexFile;
			} else if (mapData.getHisFrmType() == FrmType.Develop) {
				String ddocs = bp.da.DataType.ReadTextFile(
						SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/indexDevelop.htm");

				// 获取附件

				// 获取从表
				MapDtls dtls = new MapDtls(frmID);
				for (MapDtl dtl : dtls.ToJavaList()) {
					if (dtl.getIsView() == false) {
						continue;
					}
					String html = GetDtlHtmlByID(dtl, workid, mapData.getFrmW());
					htmlString = htmlString.replace("@Dtl_Fd" + dtl.getNo(), html);
				}
				FrmAttachments aths = new FrmAttachments(frmID);
				for (FrmAttachment ath : aths.ToJavaList()) {
					if (ath.getIsVisable() == false) {
						continue;
					}
					String html = GetAthHtmlByID(ath, workid, path);
					htmlString = htmlString.replace("@Ath_" + ath.getMyPK(), html);
				}

				htmlString = htmlString.replace("../../DataUser", SystemConfig.getHostURLOfBS() + "/DataUser");
				htmlString = htmlString.replace("../DataUser", SystemConfig.getHostURLOfBS() + "/DataUser");
				ddocs = ddocs.replace("@Docs", htmlString);

				ddocs = ddocs.replace("@Height", mapData.getFrmH()+ "px");
				ddocs = ddocs.replace("@Title", mapData.getName());

				bp.da.DataType.WriteFile(indexFile, ddocs);
				return indexFile;
			}
			GEEntity en = new GEEntity(frmID, workid);

			/// 生成水文.

			String rdt = "";
			if (en.getEnMap().getAttrs().Contains("RDT")) {
				rdt = en.GetValStringByKey("RDT");
				if (rdt.length() > 10) {
					rdt = rdt.substring(0, 10);
				}
			}
			// 先判断节点中水印的设置
			String words = "";
			Node nd = null;
			if (gwf != null) {
				nd = new Node(gwf.getFK_Node());
				if (nd.getNodeID() != 0) {
					words = nd.getShuiYinModle();
				}
			}
			if (DataType.IsNullOrEmpty(words) == true) {
				words = Glo.getPrintBackgroundWord();
			}
			words = words.replace("@RDT", rdt);

			if (words.contains("@") == true) {
				words = Glo.DealExp(words, en);
			}

			String templateFilePathMy = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/";
            paintWaterMarkPhoto(templateFilePathMy + "ShuiYin.png",words,path + "/ShuiYin.png");


			///

			// 生成 表单的 html.
			StringBuilder sb = new StringBuilder();

			/// 替换模版文件..
			// 首先判断是否有约定的文件.
			String docs = "";
			String tempFile = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/" + mapData.getNo()
					+ ".htm";
			if ((new File(tempFile)).isFile() == false) {
				if (gwf != null) {

					if (nd.getHisFormType() == NodeFormType.FreeForm) {
						mapData.setHisFrmType(FrmType.FreeFrm);
					} else if (nd.getHisFormType() == NodeFormType.FoolForm
							|| nd.getHisFormType() == NodeFormType.FoolTruck) {
						mapData.setHisFrmType(FrmType.FoolForm);
					} else if (nd.getHisFormType() == NodeFormType.SelfForm) {
						mapData.setHisFrmType(FrmType.Url);
					}
				}

				if (mapData.getHisFrmType() == FrmType.FoolForm) {
					docs = bp.da.DataType.ReadTextFile(
							SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/indexFool.htm");
					sb = bp.wf.MakeForm2Html.GenerHtmlOfFool(mapData, frmID, workid, en, path, flowNo, nodeID, basePath,
							nd.getHisFormType());
					docs = docs.replace("@Width", mapData.getFrmW() + "px");
				} else if (mapData.getHisFrmType() == FrmType.FreeFrm) {
					docs = bp.da.DataType.ReadTextFile(
							SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/indexFree.htm");
					sb = bp.wf.MakeForm2Html.GenerHtmlOfFree(mapData, frmID, workid, en, path, flowNo, nodeID,
							basePath);
					docs = docs.replace("@Width", (mapData.getFrmW() * 1.5) + "px");
				}
			}

			docs = docs.replace("@Docs", sb.toString());

			docs = docs.replace("@Height", mapData.getFrmH() + "px");

			String dateFormat = DataType.getCurrentDateByFormart("yyyy年MM月dd日 HH时mm分ss秒");
			docs = docs.replace("@PrintDT", dateFormat);

			if (flowNo != null) {
				gwf = new GenerWorkFlow(workid);
				gwf.setWorkID(workid);
				gwf.RetrieveFromDBSources();

				docs = docs.replace("@Title", gwf.getTitle());

				if (gwf.getWFState() == WFState.Runing) {
					if (SystemConfig.getCustomerNo().equals("TianYe") && gwf.getNodeName().contains("反馈") == true) {
						nd = new Node(gwf.getFK_Node());
						if (nd.getIsEndNode() == true) {
							// 让流程自动结束.
							bp.wf.Dev2Interface.Flow_DoFlowOver(workid, "打印并自动结束", 0);
						}
					}
				}

				// 替换模版尾部的打印说明信息.
				String pathInfo = SystemConfig.getPathOfDataUser() + "InstancePacketOfData/Template/EndInfo/"
						+ flowNo + ".txt";
				if ((new File(pathInfo)).isFile() == false) {
					pathInfo = SystemConfig.getPathOfDataUser()
							+ "InstancePacketOfData/Template/EndInfo/Default.txt";
				}

				docs = docs.replace("@EndInfo", DataType.ReadTextFile(pathInfo));
			}

			// indexFile = SystemConfig.getPathOfDataUser() +
			// "/InstancePacketOfData/" + frmID + "/" + workid +
			// "/index.htm";
			bp.da.DataType.WriteFile(indexFile, docs);

			return indexFile;
		} catch (RuntimeException ex) {
			return "err@报表生成错误:" + ex.getMessage();
		}
	}

	private static void paintWaterMarkPhoto(String targerImagePath,String words,String srcImagePath) {
        Integer degree = -15;
        OutputStream os = null;
        try {
            Image srcImage = ImageIO.read(new File(srcImagePath));
            BufferedImage bufImage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            // 得到画布对象
            Graphics2D graphics2D = bufImage.createGraphics();
            // 设置对线段的锯齿状边缘处理
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(srcImage.getScaledInstance(srcImage.getWidth(null), srcImage.getHeight(null), Image.SCALE_SMOOTH),
                    0, 0, null);
            if (null != degree) {
                // 设置水印旋转角度及坐标
                graphics2D.rotate(Math.toRadians(degree), (double) bufImage.getWidth() / 2, (double) bufImage.getHeight() / 2);
            }
            // 透明度
            float alpha = 0.25f;
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 设置颜色和画笔粗细
            graphics2D.setColor(Color.gray);
            graphics2D.setStroke(new BasicStroke(10));
            graphics2D.setFont(new Font("SimSun", Font.ITALIC, 18));
            // 绘制图案或文字
            String cont = words;
            String dateStr = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
            int charWidth1 = 8;
            int charWidth2 = 8;
            int halfGap = 12;
            graphics2D.drawString(cont, (srcImage.getWidth(null) - cont.length() * charWidth1) / 2,
                    (srcImage.getHeight(null) - (charWidth1 + halfGap)) / 2);
            graphics2D.drawString(dateStr, (srcImage.getWidth(null) - dateStr.length() * charWidth2) / 2,
                    (srcImage.getHeight(null) + (charWidth2 + halfGap)) / 2);
 
            graphics2D.dispose();
 
            os = new FileOutputStream(targerImagePath);
            // 生成图片 (可设置 jpg或者png格式)
            ImageIO.write(bufImage, "png", os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	public static boolean Html2Pdf(String pdfFileExe, String htmFile, String pdf) {
		bp.da.Log.DebugWriteInfo("@开始生成PDF" + pdfFileExe + "@pdf=" + pdf + "@htmFile=" + htmFile);
		StringBuilder cmd = new StringBuilder();
		if (System.getProperty("os.name").indexOf("Windows") == -1) {
			// 非windows 系统
			pdfFileExe = "/home/ubuntu/wkhtmltox/bin/wkhtmltopdf";
		}
		cmd.append(pdfFileExe);
		cmd.append(" ");
		cmd.append(" --header-line");// 页眉下面的线
		// cmd.append(" --header-center 这里是页眉这里是页眉这里是页眉这里是页眉 ");//页眉中间内容
		cmd.append(" --margin-top 3cm ");// 设置页面上边距 (default 10mm)
		// cmd.append(" --header-html
		// file:///"+WebUtil.getServletContext().getRealPath("")+FileUtil.convertSystemFilePath("/style/pdf/head.html"));//
		// (添加一个HTML页眉,后面是网址)
		cmd.append(" --header-spacing 5 ");// (设置页眉和内容的距离,默认0)
		// cmd.append(" --footer-center (设置在中心位置的页脚内容)");//设置在中心位置的页脚内容
		// cmd.append(" --footer-html
		// file:///"+WebUtil.getServletContext().getRealPath("")+FileUtil.convertSystemFilePath("/style/pdf/foter.html"));//
		// (添加一个HTML页脚,后面是网址)
		cmd.append(" --footer-line");// * 显示一条线在页脚内容上)
		cmd.append(" --footer-spacing 5 ");// (设置页脚和内容的距离)

		cmd.append(htmFile);
		cmd.append(" ");
		cmd.append(pdf);
		boolean result = true;
		try {
			Process proc = Runtime.getRuntime().exec(cmd.toString());
			HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
			HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
			error.start();
			output.start();
			proc.waitFor();
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 签名
	 * 
	 * @param userNo
	 * @return
	 * @throws Exception
	 */
	private static String SignPic(String userNo) throws Exception {

		if (DataType.IsNullOrEmpty(userNo)) {
			return "";
		}
		// 如果文件存在
		String path = SystemConfig.getPathOfDataUser() + "Siganture/" + userNo + ".jpg";

		if ((new File(path)).isFile() == false) {
			path = SystemConfig.getPathOfDataUser() + "Siganture/" + userNo + ".JPG";
			if ((new File(path)).isFile() == true) {
				return "<img src='" + path + "' style='border:0px;width:100px;height:30px;'/>";
			} else {
				Emp emp = new Emp(userNo);
				return emp.getName();
			}
		} else {
			return "<img src='" + path + "' style='border:0px;width:100px;height:30px;'/>";
		}

	}

}

///