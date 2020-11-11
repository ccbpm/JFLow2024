﻿var webUser;
var pageData = {};
var workModel = GetQueryString("WorkModel");
if (workModel == null || workModel == undefined)
    workModel = 0;
//页面启动函数.
$(function () {
    //导入关联表单按钮
    $("#RefDict").hide();

    //单据属性按钮
    if (workModel == 0 || workModel == 1)
        $("#FrmillBtn").hide();

    if (workModel == 3)
        $("#RefDict").show();

    $("#state").css("left",($("#Btn_Save").position().left-150-34)+"px");

    $(".wrapper-dropdown-2").on("mousedown", function (e) {
        var v_id = $(e.target).attr("id");
        var dd = new DropDown($("#" + v_id + ""));
    });

    $(".wrapper-dropdown-2").click(function () {
        // all dropdowns
        //$('.wrapper-dropdown-2').removeClass('active');
    });
    $(document).click(function () {
        // all dropdowns
        $('.wrapper-dropdown-2').removeClass('active');
    });


    webUser = new WebUser();
    pageData.fk_mapdata = GetQueryString("FK_MapData");
    pageData.FK_Flow = GetQueryString("FK_Flow");
    pageData.FK_Node = GetQueryString("FK_Node");
    //pageData.IsReadonly = 1;

    $("#Msg").html("<img src=../../Img/loading.gif />&nbsp;正在加载,请稍后......");

    if (GetQueryString("FK_Node") == "0")
        $("#FrmNodeComponent").hide();
    //初始化groupID.
    var fk_mapData = GetQueryString("FK_MapData");
    var isF = GetQueryString("IsFirst"); //是否第一次加载?

    var hander = new HttpHandler("BP.WF.HttpHandler.WF_Admin_FoolFormDesigner");
    hander.Clear();
    hander.AddPara("IsFirst", isF);
    hander.AddPara("FK_MapData", fk_mapData);
    hander.AddPara("FK_Flow", GetQueryString("FK_Flow"));
    hander.AddPara("FK_Node", GetQueryString("FK_Node"));
    var data = hander.DoMethodReturnString("Designer_Init");

    if (data.indexOf('err@') == 0) {
        alert(data);
        return;
    }

    if (data.indexOf('url@') == 0) {
        data = data.replace('url@', '');
        window.location.href = data;
        return;
    }

    //里面有三个对象. Sys_MapAttr, Sys_GroupField, Sys_MapData
    data = JSON.parse(data);


    //拼接 TABLE
    //按分组拼接
    var groupFields = data.Sys_GroupField;
    var tbody = $('<tbody></tbody>');

    var frmName = data.Sys_MapData[0].Name;

    var tableCol = data.Sys_MapData[0].TableCol;
    if (tableCol == 0)
        tableCol = 4;
    else if (tableCol == 1)
        tableCol = 6;
    else if (tableCol == 2)
        tableCol = 3;
    else
        tableCol = 4;

    var html = "";

    html += "<tr>";
    html += "<td colspan=" + tableCol + " ><div style='float:left;margin-top:1px' ><img src='../../../DataUser/ICON/LogBiger.png'  style='height:50px;' /></div> <h2 style='float:right;margin-top:8px' >" + frmName + "</h2></td>";
    html += "</tr>";

    for (var k = 0; k < groupFields.length; k++) {

        var groupObj = groupFields[k];
        //附件类的控件.
        if (groupObj.CtrlType == 'Ath') {
            //获取附件的主键
            var MyPK = groupObj.CtrlID;
            if (MyPK == "")
                continue;
            //创建附件描述信息.
            var ath = new Entity("BP.Sys.FrmAttachment");
            ath.MyPK = groupObj.CtrlID;
            if (ath.RetrieveFromDBSources() == 0)
                continue;
            if (ath.IsVisable == "0" || ath.NoOfObj == "FrmWorkCheck")
                continue;
        }
        //生成工具栏.
        html += GenerGroupTR(groupObj, tableCol, data);

        //生成内容.
        html += GenerGroupContext(groupObj, data, tableCol);

        //过滤attrs
        var mapAttrs = $.grep(data.Sys_MapAttr, function (val) { return val.GroupID == groupObj.OID; });
        if (tableCol == 4 || tableCol == 6)
            html += InitMapAttr(mapAttrs, tableCol);

        if (tableCol == 3)
            html += InitThreeColMapAttr(mapAttrs, tableCol);
        continue;
    }

    // alert(html);
    tbody.append($(html));
   
    $(".NewChild").hide();
    //contentTable
    $('#contentTable').children().remove();
    $('#contentTable').append(tbody);
    if ($("#WorkCheck").length == 1)
        loadScript("../../WorkOpt/WorkCheck.js");

    if ($("#FlowBBS").length == 1)
        loadScript("../../WorkOpt/FlowBBS.js");

    if (data.Sys_FrmAttachment.length != 0) {
        Skip.addJs("../../CCForm/Ath.js");
        Skip.addJs("../../CCForm/JS/FileUpload/fileUpload.js");
        $('head').append("<link href='../../CCForm/JS/FileUpload/css/fileUpload.css' rel='stylesheet' type='text/css' />");
    }


    $.each(data.Sys_FrmAttachment, function (idex, ath) {
        AthTable_Init(ath, "Div_" + ath.MyPK);
    });


    var mapAttrs = data.Sys_MapAttr;
    //解析设置表单字段联动显示与隐藏.
    for (var i = 0; i < mapAttrs.length; i++) {

        var mapAttr = mapAttrs[i];
        if (mapAttr.UIVisible == 0)
            continue;

        if (mapAttr.MyDataType == 2 && mapAttr.LGType == 1) {  // AppInt Enum
            if (mapAttr.AtPara != null && mapAttr.AtPara.indexOf('@IsEnableJS=1') >= 0) {
                if (mapAttr.UIContralType == 1) {
                    /*启用了显示与隐藏.*/
                    var ddl = $("#DDL_" + mapAttr.KeyOfEn);
                    //初始化页面的值
                    var nowKey = ddl.val();
                    if (nowKey == undefined || nowKey == "")
                        continue;
                    
                    setEnable(mapAttr.FK_MapData, mapAttr.KeyOfEn, nowKey);

                }
                if (mapAttr.UIContralType == 3) {
                    /*启用了显示与隐藏.*/
                    var nowKey = $('input[name="RB_' + mapAttr.KeyOfEn + '"]:checked').val();
                    if (nowKey == undefined || nowKey == "")
                        continue;

                    setEnable(mapAttr.FK_MapData, mapAttr.KeyOfEn, nowKey);

                }
            }
        }
    }

    
    $("#Msg").html("");
    ResizeWindow();

});


//解析表单是三列的情况
function InitThreeColMapAttr(Sys_MapAttr, tableCol) {
    var html = "";
    var isDropTR = true;

    var lab = "";
    var colSpan = 1;
    var rowSpan = 1;
    var textColSpan = 1;
    var textWidth = "33%";
    var colWidth = "33%";

    //记录一行已占用的列输
    var UseColSpan = 0;
    var IsMiddle = false;
    //跨行问题
    for (var i = 0; i < Sys_MapAttr.length; i++) {
        var attr = Sys_MapAttr[i];
        if (attr.UIVisible == 0)
            continue;
        rowSpan = attr.RowSpan;
        colSpan = attr.ColSpan;
        textColSpan = attr.TextColSpan;

        colWidth = 33 * parseInt(colSpan) + "%";
        textWidth = 33 * parseInt(textColSpan) + "%";

        //大文本备注信息 独占一行
        if (attr.UIContralType == 60) {
            //获取文本信息
            var filename = basePath + "/DataUser/CCForm/BigNoteHtmlText/" + attr.FK_MapData + ".htm";
            var htmlobj = $.ajax({ url: filename, async: false });
            var str = htmlobj.responseText;
            if (htmlobj.status == 404)
                str = filename + "这个文件不存在，请联系管理员";
            html += "<tr>";
            html += "<td  ColSpan='" + tableCol + "' class='FDesc' style='text-align:left:height:auto'>" + str + "</td>";
            html += "</tr>";
            isDropTR = true;
            UseColSpan = 0;
            continue;
        }
        //跨列设置(显示的是文本)
        if (colSpan == 0) {

            if (textColSpan == tableCol) {
                html += "<td  class='LabelFDesc' rowSpan=" + rowSpan + " colSpan=" + textColSpan + ">" + GenerLabel(attr) + "</td>";
                isDropTR = true;
                continue;
            }
            //线性展示都跨一个单元格
            //换行的情况
            if (isDropTR == true) {
                html += "<tr >";
                UseColSpan = 0;
                UseColSpan += colSpan + textColSpan;
                html += "<td class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " colSpan=" + textColSpan + ">" + GenerLabel(attr) + "</td>";

                if (UseColSpan == tableCol) {
                    isDropTR = true;
                } else {
                    isDropTR = false;
                }
                continue;
            }

            if (isDropTR == false) {
                UseColSpan += colSpan + textColSpan;
                html += "<td class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " colSpan=" + textColSpan + ">" + GenerLabel(attr) + "</td>";
                if (UseColSpan == tableCol) {
                    html += "</tr>";
                    isDropTR = true;
                } else {
                    isDropTR = false;
                }
                continue;
            }
        }
        //解析占一行的情况
        if (colSpan == tableCol) {
            html += "<tr>";
            html += "<td  ColSpan='" + colSpan + "' rowSpan=" + rowSpan + " class='LabelFDesc' style='text-align:left'>" + GenerLabel(attr) + "</br>";
            html += InitMapAttrOfCtrlFool(attr);
            html += "</td>";
            html += "</tr>";
            isDropTR = true;
            UseColSpan = 0;
            continue;
        }

        //换行的情况
        if (isDropTR == true) {
            html += "<tr >";
            UseColSpan = 0;

            UseColSpan += colSpan;
            html += "<td  id='Td_" + attr.KeyOfEn + "' class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " ColSpan=" + colSpan + " class='tdSpan'>" + GenerLabel(attr) + "<br/>";
            html += InitMapAttrOfCtrlFool(attr);
            html += "</td>";
            if (UseColSpan == tableCol) {
                isDropTR = true;
            } else {
                isDropTR = false;
            }


            continue;
        }

        if (isDropTR == false) {

            UseColSpan += colSpan;
            html += "<td  id='Td_" + attr.KeyOfEn + "' class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " ColSpan=" + colSpan + " class='tdSpan'>" + GenerLabel(attr) + "<br/>";
            html += InitMapAttrOfCtrlFool(attr);
            html += "</td>";

            if (UseColSpan == tableCol) {
                html += "</tr>";
                isDropTR = true;
            } else {
                isDropTR = false;
            }


            continue;
        }
    }
    return html;
}



//解析表单字段 MapAttr.(表单4列/6列)
function InitMapAttr(Sys_MapAttr, tableCol) {
    var html = "";
    var isDropTR = true;
    //跨行问题定义的字段
    //右侧跨行
    var IsShowRight = true; // 是否显示右侧列
    var rRowSpan = 0; //跨的行数
    var ruRowSpan = 0; //已近解析的行数
    var ruColSpan = 0; //该跨行总共跨的列数

    //左侧跨行
    var IsShowLeft = true; // 是否显示左侧列
    var lRowSpan = 0; //跨的行数
    var luRowSpan = 0; //已近解析的行数
    var luColSpan = 0; //该跨行总共跨的列数


    var lab = "";
    var rowSpan = 1;
    var colSpan = 1;
    var textColSpan = 1;
    var textWidth = "15%";
    var colWidth = "15%";

    //记录一行已占用的列输
    var UseColSpan = 0;
    var IsMiddle = false;

    //跨行问题
    for (var i = 0; i < Sys_MapAttr.length; i++) {
        var attr = Sys_MapAttr[i];
        if (attr.UIVisible == 0)
            continue;
        rowSpan = attr.RowSpan;
        colSpan = attr.ColSpan;
        textColSpan = attr.TextColSpan;
        if (tableCol == 4) {
            if (colSpan == 1)
                colWidth = "35%";
            else if (colSpan == 2)
                colWidth = "50%";
            else if (colSpan == 3)
                colWidth = "85%";
            textWidth = 15 * parseInt(textColSpan) + "%";
        } else {
            colWidth = 25 * parseInt(colSpan) + "%";
            textWidth = 8 * parseInt(textColSpan) + "%";
        }
        //大文本备注信息 独占一行
        if (attr.UIContralType == 60) {
            //获取文本信息
            var filename = basePath + "/DataUser/CCForm/BigNoteHtmlText/" + attr.FK_MapData + ".htm?r=" + Math.random();
            var htmlobj = $.ajax({ url: filename, async: false });
            var str = htmlobj.responseText;
            if (htmlobj.status == 404)
                str = filename + "这个文件不存在，请联系管理员";
            html += "<tr>";
            html += "<td  ColSpan='" + tableCol + "' class='FDesc' style='text-align:left:height:auto'><a href='#' onclick='EditBigText(\"" + attr.MyPK + "\",\"" + attr.FK_MapData + "\")'>" + str + "</a></td>";
            html += "</tr>";
            isDropTR = true;
            UseColSpan = 0;
            continue;
        }
        //跨列设置(显示的是文本)
        if (colSpan == 0) {

            if (textColSpan == tableCol) {
                rowSpan = 1;
                html += "<td  class='LabelFDesc' rowSpan=" + rowSpan + " colSpan=" + textColSpan + ">" + GenerLabel(attr) + "</td>";
                isDropTR = true;
                continue;
            }
            //线性展示都跨一个单元格
            //换行的情况
            if (isDropTR == true) {
                html += "<tr >";
                UseColSpan = 0;
                luColSpan = 0;
                if (IsShowLeft == true) {
                    UseColSpan += colSpan + textColSpan + ruColSpan;
                    lRowSpan = rowSpan;
                    luColSpan += colSpan + textColSpan;
                    html += "<td class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " colSpan=" + textColSpan + ">" + GenerLabel(attr) + "</td>";
                    if (rowSpan != 1) {
                        IsShowLeft = false;
                    }

                }
                if (UseColSpan == tableCol) {
                    ruRowSpan++;
                    isDropTR = true;
                } else {
                    isDropTR = false;
                }

                //复位右侧信息
                if (ruRowSpan == rRowSpan) {
                    ruRowSpan = 0;
                    rRowSpan = 0;
                    IsShowRight = true;
                    if (rowSpan == 1)
                        luColSpan = 0;
                    ruColSpan = 0;


                }


                if (IsShowRight == false && (UseColSpan == tableCol)) {
                    html += "</tr>";
                    isDropTR = true;
                    UseColSpan = ruColSpan;

                }

                continue;
            }

            if (isDropTR == false) {
                ruColSpan = 0;
                if (IsShowRight == true) {
                    UseColSpan += colSpan + textColSpan;
                    rRowSpan = rowSpan;
                    ruColSpan += colSpan + textColSpan;
                    html += "<td class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " colSpan=" + textColSpan + ">" + GenerLabel(attr) + "</td>";
                    if (UseColSpan == tableCol) {
                        isDropTR = true;
                        if (rowSpan != 1) {
                            ruRowSpan++;
                        }
                    }
                    if (rowSpan != 1) {
                        IsShowRight = false;
                        lRowSpan = rowSpan;
                    }
                }

                if (UseColSpan == tableCol) {
                    luRowSpan++;
                    html += "</tr>";
                }

                //复位左侧信息
                if (luRowSpan == lRowSpan) {
                    luRowSpan = 0;
                    lRowSpan = 0;
                    IsShowLeft = true;
                    ruColSpan = 0;

                }

                if (IsShowLeft == false && (UseColSpan == tableCol)) {
                    html += "<tr>";
                    UseColSpan = 0;
                    isDropTR = false;
                    UseColSpan = luColSpan;
                }
                continue;
            }
        }
        //解析占一行的情况
        if (colSpan == tableCol) {
            rowSpan = 1;
            html += "<tr>";
            html += "<td  ColSpan='" + colSpan + "' rowSpan=" + rowSpan + " class='LabelFDesc' style='text-align:left'>" + GenerLabel(attr) + "</br>";
            html += "</tr>";
            html += "<tr>";
            html += "<td  id='Td_" + attr.KeyOfEn + "' ColSpan='" + colSpan + "' rowSpan=" + rowSpan + " class='FDesc' style='text-align:left'>";
            html += InitMapAttrOfCtrlFool(attr);
            html += "</td>";
            html += "</tr>";
            isDropTR = true;
            UseColSpan = 0;
            continue;
        }
        var sumColSpan = colSpan + textColSpan;
        if (sumColSpan == tableCol) {
            isDropTR = true;
            UseColSpan = 0;
            rowSpan = 1;
            html += "<tr >";
            html += "<td  id='Td_" + attr.KeyOfEn + "' class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " ColSpan=" + textColSpan + " class='tdSpan'>" + GenerLabel(attr) + "</td>";
            html += "<td  class='FDesc' id='Td_" + attr.KeyOfEn + "'  style='width:" + colWidth + ";' ColSpan=" + colSpan + " rowSpan=" + rowSpan + " class='tdSpan'>";
            html += InitMapAttrOfCtrlFool(attr);
            html += "</td>";
            html += "</tr>";
            continue;
        }

        //换行的情况
        if (isDropTR == true) {
            html += "<tr >";
            UseColSpan = 0;
            luColSpan = 0;
            if (IsShowLeft == true) {
                UseColSpan += colSpan + textColSpan + ruColSpan;
                lRowSpan = rowSpan;
                luColSpan += colSpan + textColSpan;
                html += "<td  id='Td_" + attr.KeyOfEn + "' class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " ColSpan=" + textColSpan + " class='tdSpan'>" + GenerLabel(attr) + "</td>";
                html += "<td  class='FDesc' id='Td_" + attr.KeyOfEn + "'  style='width:" + colWidth + ";' ColSpan=" + colSpan + " rowSpan=" + rowSpan + " class='tdSpan'>";
                html += InitMapAttrOfCtrlFool(attr);
                html += "</td>";
                if (rowSpan != 1) {
                    IsShowLeft = false;
                }

            }
            if (UseColSpan == tableCol) {
                ruRowSpan++;
                isDropTR = true;
            } else {
                isDropTR = false;
            }

            //复位右侧信息
            if (ruRowSpan == rRowSpan) {
                ruRowSpan = 0;
                rRowSpan = 0;
                IsShowRight = true;
                if (rowSpan == 1)
                    luColSpan = 0;
                ruColSpan = 0;

            }


            if (IsShowRight == false && (UseColSpan == tableCol)) {
                html += "</tr>";
                isDropTR = true;
                UseColSpan = ruColSpan;

            }

            continue;
        }

        if (isDropTR == false) {
            ruColSpan = 0;
            if (IsShowRight == true) {
                UseColSpan += colSpan + textColSpan;
                rRowSpan = rowSpan;
                ruColSpan += colSpan + textColSpan;
                html += "<td  id='Td_" + attr.KeyOfEn + "' class='LabelFDesc' style='width:" + textWidth + ";' rowSpan=" + rowSpan + " ColSpan=" + textColSpan + " class='tdSpan'>" + GenerLabel(attr) + "</td>";
                html += "<td  class='FDesc' id='Td_" + attr.KeyOfEn + "'  style='width:" + colWidth + ";' ColSpan=" + colSpan + " rowSpan=" + rowSpan + " class='tdSpan'>";
                html += InitMapAttrOfCtrlFool(attr);
                html += "</td>";
                if (UseColSpan == tableCol) {
                    isDropTR = true;
                    if (rowSpan != 1) {
                        ruRowSpan++;
                    }
                }
                if (rowSpan != 1) {
                    IsShowRight = false;
                    lRowSpan = rowSpan;
                }
            }

            if (UseColSpan == tableCol) {
                luRowSpan++;
                html += "</tr>";
            }

            //复位左侧信息
            if (luRowSpan == lRowSpan) {
                luRowSpan = 0;
                lRowSpan = 0;
                IsShowLeft = true;
                ruColSpan = 0;

            }

            if (IsShowLeft == false && (UseColSpan == tableCol)) {
                html += "<tr>";
                UseColSpan = 0;
                isDropTR = false;
                UseColSpan = luColSpan;
            }
            continue;
        }
    }
    return html;
}


function InitMapAttrOfCtrlFool(mapAttr) {
    var elemHtml = "";

    if (mapAttr.MyDataType == "1" && mapAttr.UIContralType == 2) {
        var rbHtmls = "";
        var ses = new Entities("BP.Sys.SysEnums");
        ses.Retrieve("EnumKey", mapAttr.UIBindKey, "IntKey");

        //显示方式,默认为横向展示.
        var RBShowModel = 0;
        if (mapAttr.AtPara.indexOf('@RBShowModel=0') > 0)
            RBShowModel = 1;

        for (var i = 0; i < ses.length; i++) {
            var se = ses[i];

            var br = "";
            if (RBShowModel == 1)
                br = "<br>";

            var checked = "";
            if (se.IntKey == mapAttr.DefVal)
                checked = " checked=true";

            rbHtmls += "<label style='font-weight:normal;'><input type=checkbox name='CB_" + mapAttr.KeyOfEn + "' id='CB_" + mapAttr.KeyOfEn + "_" + se.IntKey + "' value='" + se.IntKey + "' " + checked + " onclick='clickEnable( this ,\"" + mapAttr.FK_MapData + "\",\"" + mapAttr.KeyOfEn + "\",\"" + mapAttr.AtPara + "\")' />" + se.Lab + " </label>&nbsp;" + br;
        }

        return "<div id='DIV_" + mapAttr.KeyOfEn + "'>" + rbHtmls + "</div>";
    }
    if (mapAttr.MyDataType == "1") {
        //字段附件
        if (mapAttr.UIContralType == 6) {
            return "<div style='text-align:left;padding-left:10px' id='athModel_" + mapAttr.KeyOfEn + "'><label>请点击[" + mapAttr.Name + "]执行上传</label></div>";
        }
        //写字板
        if (mapAttr.UIContralType == 8) {
            return "<img  src='../../../DataUser/Siganture/admin.jpg'  style='border:0px;height:" + mapAttr.UIHeight + "px;' id='Img" + mapAttr.KeyOfEn + "' />";
        }
        //地图控件
        if (mapAttr.UIContralType == 4) {
            var eleHtml = "<div style='text-align:left;padding-left:0px' id='athModel_" + mapAttr.KeyOfEn + "' data-type='1'>";
            eleHtml += "<input type='button' name='select' value='选择' />";
            eleHtml += "<input type = text style='width:75%' maxlength=" + mapAttr.MaxLen + "  id='TB_" + mapAttr.KeyOfEn + "' />";
            eleHtml += "</div>";
            return eleHtml;
        }
        //身份证
        if (mapAttr.UIContralType == 13 && mapAttr.KeyOfEn == "IDCardAddress") {
            var eleHtml = "<div style='text-align:left;padding-left:0px'  data-type='1'>";
            eleHtml += "<input type = text style='width:75% !important;display:inline;' class='form-control' maxlength=" + mapAttr.MaxLen + "  id='TB_" + mapAttr.KeyOfEn + "' name='TB_" + mapAttr.KeyOfEn + "'/>";
            eleHtml += "<label class='image-local' style='margin-left:5px'><input type='file' accept='image/png,image/bmp,image/jpg,image/jpeg' style='width:25% !important;display:none' onchange='GetIDCardInfo()'/>上传身份证</label>";
            eleHtml += "</div>";
            return eleHtml;
        }
        //评分
        if (mapAttr.UIContralType == 101) {
            var eleHtml = "<div style='text-align:left;padding-left:0px'  data-type='1'>";
            eleHtml += "<span class='simplestar'>";

            var num = mapAttr.Tag2;
            for (var i = 0; i < num; i++) {

                eleHtml += "<img src='../../Style/Img/star_2.png' />";
            }
            eleHtml += "&nbsp;&nbsp;<span class='score-tips' style='vertical-align: middle;color:#ff6600;font: 12px/1.5 tahoma,arial,\"Hiragino Sans GB\",宋体,sans-serif;'><strong>" + num + "  分</strong></span>";
            eleHtml += "</span></div>";
            return eleHtml;
        }
        //按钮
        if (mapAttr.UIContralType == 18) {
            "<input type='button'  id='TB_" + mapAttr.KeyOfEn + "' name='TB_" + mapAttr.KeyOfEn + "' value='" + mapAttr.Name + "' style='width:98%' onclick=''/>";
            return;
        }
        //工作进度图
        if (mapAttr.UIContralType == 50) {
            return "<img  src='./Img/JobSchedule.png'  style='border:0px;height:" + mapAttr.UIHeight + "px;width:100%;' id='Img" + mapAttr.KeyOfEn + "' />";
        }

        if (mapAttr.UIContralType == 16) {
            var eleHtml = "<div style='text-align:left;padding-left:0px' >";
            eleHtml += "<input type='button' name='select' value='系统定位' />";
            eleHtml += "</div>";
            return eleHtml;
        }

        if (mapAttr.UIHeight <= 40)
            return "<div id='DIV_" + mapAttr.KeyOfEn + "'><input class='form-control' style='width:95%;' value='" + mapAttr.DefVal + "' maxlength=" + mapAttr.MaxLen + "  name='TB_" + mapAttr.KeyOfEn + "' id='TB_" + mapAttr.KeyOfEn + "'placeholder='" + (mapAttr.Tip || '') + "' type='text' " + (mapAttr.UIIsEnable == 1 ? '' : ' disabled="disabled"') + "/></div>";

        if (mapAttr.UIHeight > 23) {
            var uiHeight = mapAttr.UIHeight;
            return "<div id='DIV_" + mapAttr.KeyOfEn + "'> <textarea class='form-control' maxlength=" + mapAttr.MaxLen + " style='height:" + uiHeight + "px;width:100%;' name='TB_" + mapAttr.KeyOfEn + "' id='TB_" + mapAttr.KeyOfEn + "'placeholder='" + (mapAttr.Tip || '') + "' type='text' " + (mapAttr.UIIsEnable == 1 ? '' : ' disabled="disabled"') + "/></div>";
        }


        return "<div id='DIV_" + mapAttr.KeyOfEn + "'> <input class='form-control' maxlength=" + mapAttr.MaxLen + "  value='" + mapAttr.DefVal + "' name='TB_" + mapAttr.KeyOfEn + "' id='TB_" + mapAttr.KeyOfEn + "'placeholder='" + (mapAttr.Tip || '') + "' type='text' " + (mapAttr.UIIsEnable == 1 ? '' : ' disabled="disabled"') + " /></div>";
    }

    if (mapAttr.MyDataType == 2 && mapAttr.LGType == 0) {
        return "<div id='DIV_" + mapAttr.KeyOfEn + "'><input  value='0' style='text-align:right;' class='form-control' onkeyup=" + '"' + "valitationAfter(this, 'int');if(isNaN(value) || (value%1 !== 0))execCommand('undo')" + '"' + " onafterpaste=" + '"' + "valitationAfter(this, 'int');if(isNaN(value) || (value%1 !== 0))execCommand('undo')" + '"' + " maxlength=" + mapAttr.MaxLen / 2 + "   type='text'" + enableAttr + " id='TB_" + mapAttr.KeyOfEn + "'placeholder='" + (mapAttr.Tip || '') + "'/></div>";
    }

    if (mapAttr.MyDataType == 5 || mapAttr.MyDataType == 3) {
        var attrdefVal = mapAttr.DefVal;
        var bit;
        if (attrdefVal != null && attrdefVal !== "" && attrdefVal.indexOf(".") >= 0)
            bit = attrdefVal.substring(attrdefVal.indexOf(".") + 1).length;

        return "<input  value='0.00' style='text-align:right;'class='form-control'  onkeyup=" + '"' + "valitationAfter(this, 'float');if(isNaN(value)) execCommand('undo');limitLength(this," + bit + ");" + '"' + " onafterpaste=" + '"' + " valitationAfter(this, 'float');if(isNaN(value))execCommand('undo')" + '"' + " maxlength=" + mapAttr.MaxLen / 2 + "   type='text' id='TB_" + mapAttr.KeyOfEn + "' placeholder='" + (mapAttr.Tip || '') + "'/>";
    }

    if (mapAttr.MyDataType == 6 || mapAttr.MyDataType == 7) {
        //生成中间的部分.
        var enableAttr = '';
        var dateFmt = "yyyy-MM-dd"; //日期格式.
        if (mapAttr.MyDataType == 7)
            dateFmt = "yyyy-MM-dd HH:mm";

        if (mapAttr.UIIsEnable == 1) {
            enableAttr = '  onfocus="WdatePicker({dateFmt:' + "'" + dateFmt + "'})" + '";';
        } else {
            enableAttr = "disabled='disabled'";
        }
        return "<div id='DIV_" + mapAttr.KeyOfEn + "'> <input  class='form-control Wdate' style='width:96%;' maxlength=" + mapAttr.MaxLen + " value='" + mapAttr.DefVal + "'  type='text' " + enableAttr + " name='TB_" + mapAttr.KeyOfEn + "' id='TB_" + mapAttr.KeyOfEn + "'/></div>";
    }
    if (mapAttr.MyDataType == 8) {
        //获取DefVal,根据默认的小数点位数来限制能输入的最多小数位数
        var attrdefVal = mapAttr.DefVal;
        var bit;
        if (attrdefVal != null && attrdefVal !== "" && attrdefVal.indexOf(".") >= 0)
            bit = attrdefVal.substring(attrdefVal.indexOf(".") + 1).length;
        else
            bit = 2;
        return "<div id='DIV_" + mapAttr.KeyOfEn + "'><input value='0.00' style='text-align:right;' class='form-control' onkeyup=" + '"' + "valitationAfter(this, 'money');limitLength(this," + bit + "); FormatMoney(this, " + bit + ", ',')" + '"' + " onafterpaste=" + '"' + "valitationAfter(this, 'money');if(isNaN(value))execCommand('undo');" + '"' + " maxlength=" + mapAttr.MaxLen / 2 + "   type='text' id='TB_" + mapAttr.KeyOfEn + "' value='0.00' placeholder='" + (mapAttr.Tip || '') + "'/></div>";
    }


    if (mapAttr.MyDataType == 4) {
        if (mapAttr.UIIsEnable == 0) {
            enableAttr = "disabled='disabled'";
        } else {
            enableAttr = "";
        }
        return "<div class='checkbox' id='DIV_" + mapAttr.KeyOfEn + "'><label for='CB_" + mapAttr.KeyOfEn + "' ><input " + (mapAttr.DefVal == 1 ? "checked='checked'" : "") + " type='checkbox' " + enableAttr + " name='CB_" + mapAttr.KeyOfEn + "' id='CB_" + mapAttr.KeyOfEn + "' />&nbsp;" + mapAttr.Name + "</label></div>";
    }


    if (mapAttr.MyDataType == 2 && mapAttr.LGType == 1) {
        if (mapAttr.UIContralType == 1) { //下拉框

            var ses = GetSysEnums(mapAttr.UIBindKey);

            var operations = "";
            $.each(ses, function (i, obj) {
                operations += "<option  value='" + obj.IntKey + "'>" + obj.Lab + "</option>";
            });

            return "<div id='DIV_" + mapAttr.KeyOfEn + "'><select class='form-control' name='DDL_" + mapAttr.KeyOfEn + "' id='DDL_" + mapAttr.KeyOfEn + "' " + (mapAttr.UIIsEnable == 1 ? '' : 'disabled="disabled"') + "  onchange='changeEnable(this,\"" + mapAttr.FK_MapData + "\",\"" + mapAttr.KeyOfEn + "\",\"" + mapAttr.AtPara + "\")'>" + operations + "</select></div>";

        } else if (mapAttr.UIContralType == 3) { //单选按钮

            var rbHtmls = "";
            var ses = GetSysEnums(mapAttr.UIBindKey);
             
            //显示方式,默认为横向展示.
            var RBShowModel = 0;
            if (mapAttr.AtPara.indexOf('@RBShowModel=0') > 0)
                RBShowModel = 1;

            for (var i = 0; i < ses.length; i++) {
                var se = ses[i];

                var br = "";
                if (RBShowModel == 1)
                    br = "<br>";

                var checked = "";
                if (se.IntKey == mapAttr.DefVal)
                    checked = " checked=true";

                rbHtmls += "<label style='font-weight:normal;'><input type=radio name='RB_" + mapAttr.KeyOfEn + "' id='RB_" + mapAttr.KeyOfEn + "_" + se.IntKey + "' value='" + se.IntKey + "' " + checked + " onclick='clickEnable( this ,\"" + mapAttr.FK_MapData + "\",\"" + mapAttr.KeyOfEn + "\",\"" + mapAttr.AtPara + "\")' />" + se.Lab + " </label>&nbsp;" + br;
            }

            return "<div id='DIV_" + mapAttr.KeyOfEn + "'>" + rbHtmls + "</div>";
        }
    }
}

function GetSysEnums(enumKey) {

    if (webUser.CCBPMRunModel == 0 || webUser.CCBPMRunModel == 1) {
        var ses = new Entities("BP.Sys.SysEnums");
        ses.Retrieve("EnumKey", enumKey, "IntKey");
        return ses;
    }

    var ses = new Entities("BP.Cloud.Sys.SysEnums");
    ses.Retrieve("RefPK", enumKey, "IntKey");
    return ses;
}

function GenerLabel(attr) {

    var fk_mapdata = GetQueryString("FK_MapData");

    var tdUp = "<a href=\"javascript:Up('" + attr.MyPK + "','1');\" class='easyui-linkbutton l-btn l-btn-plain' data-options='iconCls:icon-left,plain:true'  alt='向左动顺序' ><span class='l-btn-left'><span class='l-btn-text'><span class='l-btn-empty icon-left'>&nbsp;</span></span></span></a>";
    var tdDown = "<a href=\"javascript:Down('" + attr.MyPK + "','1');\" class='easyui-linkbutton l-btn l-btn-plain' data-options='iconCls:icon-right,plain:true' alt='向右动顺序' ><span class='l-btn-left'><span class='l-btn-text'><span class='l-btn-empty icon-right'>&nbsp;</span></span></span></a>";

    if (attr.LGType == 0 && attr.UIContralType == 1) {
        return tdUp + "<a href=\"javascript:EditTableSQL('" + attr.MyPK + "','" + attr.KeyOfEn + "');\" > " + attr.Name + "</a>" + tdDown;
    }

    if (attr.LGType == 0) {

        return tdUp + "<a href=\"javascript:Edit('" + attr.MyPK + "','" + attr.MyDataType + "','" + attr.GroupID + "','" + attr.LGType + "','" + attr.UIContralType + "');\" > " + attr.Name + "</a>" + tdDown;
    }

    if (attr.LGType == 1)
        return tdUp + "<a href=\"javascript:EditEnum('" + attr.FK_MapData + "','" + attr.MyPK + "','" + attr.KeyOfEn + "');\" > " + attr.Name + "</a>" + tdDown;

    if (attr.LGType == 2)
        return tdUp + "<a href=\"javascript:EditTable('" + attr.FK_MapData + "','" + attr.MyPK + "','" + attr.KeyOfEn + "');\" > " + attr.Name + "</a>" + tdDown;
}