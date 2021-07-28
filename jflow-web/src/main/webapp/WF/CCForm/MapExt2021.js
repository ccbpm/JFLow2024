﻿/**
 * 获取数据的方法
 * @param {any} dbSrc 请求数据集合的内容
 * @param {any} dbType 请求数据的集合了类型 SQL,函数,URL
 * @param {any} dbSource 如果是SQL的时，SQL的查询来源，本地，外部数据源
 * @param {any} keyVal 选择替换的值
 */
function GetDataTableByDB(dbSrc, dbType, dbSource, keyVal) {
    if (dbSrc == null || dbSrc == undefined || dbSrc == "")
        return null;
    //处理sql，url参数.
    dbSrc = dbSrc.replace(/~/g, "'");
    if (keyVal != null) {
        if (dbType == 0)
            keyVal = keyVal.replace(/'/g, '');

        dbSrc = dbSrc.replace(/@Key/g, keyVal);
        dbSrc = dbSrc.replace(/@key/g, keyVal);
        dbSrc = dbSrc.replace(/@KEY/g, keyVal);
    }
    dbSrc = DealExp(dbSrc, null, false);
    //获取数据源.
    dataObj = DBAccess.RunDBSrc(dbSrc, dbType, dbSource);
    return dataObj;
}
/**
 * 获取文本字段的小范围多选或者单选的数据
 * @param {any} mapExt
 * @param {any} frmData
 * @param {any} defVal
 */
function GetDataTableOfTBChoice(mapExt, frmData, defVal) {
    defVal = defVal == null || defVal == undefined ? "" :","+ defVal + ",";
    var data = [];
    switch (parseInt(mapExt.DoWay)) {
        case 1:
            var tag1 = mapExt.Tag1;
            tag1 = tag1.replace(/;/g, ',');

            $.each(tag1.split(","), function (i, o) {
                data.push({
                    value: i,
                    name: o,
                    selected: defVal.indexOf("," + i + ",") != -1 ? true : false
                })
            });
            break;
        case 2: //枚举.
            if (frmData != null && frmData != undefined) {
                data = frmData[mapExt.AttrOfOper];
                if (data == undefined)
                    data = frmData[mapExt.Tag2];
                if (data == undefined) {
                    var enums = flowData.Sys_Enum;
                    enums = $.grep(enums, function (value) {
                        return value.EnumKey == mapExt.Tag2;
                    });
                    $.each(enums, function (i, o) {
                        data.push({
                            value: o.IntKey,
                            no: o.Lab,
                            selected: defVal.indexOf("," + o.IntKey + ",") != -1 ? true : false
                        })
                    });
                }
            } else {
                var enums = new Entities("BP.Sys.SysEnums");
                enums.Retrieve("EnumKey", mapExt.Tag2);

                $.each(enums, function (i, o) {
                    data.push({
                        value: o.IntKey,
                        no: o.Lab,
                        selected: defVal.indexOf("," + o.IntKey + ",") != -1 ? true : false

                    })
                });
            }
           
            break;
        case 3: //外键表.
            if (frmData != null && frmData != undefined) {

                var dt = frmData[mapExt.Tag3];
                if (dt == undefined) {
                    var en = new Entity("BP.Sys.SFTable", mapExt.Tag3);
                    dt = en.DoMethodReturnJSON("GenerDataOfJson");
                    if (dt.length > 400) {
                        layer.alert("数据量太大，请检查配置是否有逻辑问题，或者您可以使用搜索多选或者pop弹出窗选择:" + mapExt.Tag3);
                        return null;
                    }

                    frmData[mapExt.Tag3] = dt;
                    data = [];
                    dt.forEach(function (item) {
                        data.push({
                            value: item.No,
                            name: item.Name,
                            selected: defVal.indexOf("," + item.No + ",") != -1 ? true : false

                        })
                    })
                }
            } else {
                var en = new Entity("BP.Sys.SFTable", mapExt.Tag3);
                var dt = en.DoMethodReturnJSON("GenerDataOfJson");

                if (dt.length > 400) {
                    layer.alert("数据量太大，请检查配置是否有逻辑问题，或者您可以使用搜索多选或者pop弹出窗选择:" + mapExt.Tag3);
                    return null;
                }
                data = [];
                dt.forEach(function (item) {
                    data.push({
                        value: item.No,
                        name: item.Name,
                        selected: defVal.indexOf(","+item.No + ",") != -1 ? true :false

                    })
                })
            }
            break;
        case 4:
            var tag4SQL = mapExt.Tag4;

            tag4SQL = DealExp(tag4SQL, webUser);
            if (tag4SQL.indexOf('@') == 0) {
                layer.alert('约定的变量错误:' + tag4SQL + ", 没有替换下来.");
                return null;
            }
            tag4SQL = tag4SQL.replace(/~/g, "'");

            var dt = DBAccess.RunSQLReturnTable(tag4SQL);
            if (dt.length > 400) {
                layer.alert("数据量太大，请检查配置是否有逻辑问题，或者您可以使用搜索多选或者pop弹出窗选择:" + mapExt.Tag3);
                return null;
            }
            data = [];
            dt.forEach(function (item) {
                data.push({
                    value: item.No,
                    name: item.Name,
                    selected: defVal.indexOf("," + item.No + ",") != -1 ? true:false

                })
            })
            break;
        default:
           layer. alert("未判断的模式");
            return null;
    }
    return data;

}
/**
 * 级联下拉框
 * @param {any} selectVal
 * @param {any} ddlChild
 * @param {any} mapExt
 */
function DDLAnsc(selectVal, ddlChild, mapExt) {
    selectVal = selectVal == null || selectVal == undefined ? "" : selectVal;

    if ($("#" + ddlChild).length==0) {
        layer.alert(ddlChild + "丢失,或者该字段被删除.");
        return;
    }
    //1.初始值为空或者NULL时，相关联的字段没有数据显示
    if (selectVal == "" || selectVal == "all") {
        $("#" + ddlChild).empty();
        $("#" + ddlChild).append("<option value='' selected='selected' >" + selectVal==""?"":"全部"+"</option");
        layui.form.render("select");
        var select = 'dd[lay-value=' + selectVal + ']';
        $("#" + ddlChild).siblings("div.layui-form-select").find('dl').find(select).click();//触发

        return;
    }  
   //获得数据源.
    var dataObj = GetDataTableByDB(mapExt.Doc, mapExt.DBType, mapExt.FK_DBSrc, selectVal);

    var oldVal = $("#" + ddlChild).val();
   
    //清空级联字段
    $("#" + ddlChild).empty();

    //查询数据为空时为级联字段赋值
    if (dataObj == null || dataObj.length == 0) {
        $("#" + ddlChild).append("<option value='' selected='selected' ></option");
        layui.form.render("select");
        var select = 'dd[lay-value=' + selectVal + ']';
        $("#" + ddlChild).siblings("div.layui-form-select").find('dl').find(select).click();//触发
    }

    //不为空的时候赋值
    var isHaveSelect = false;
    $.each(dataObj, function (idx, item) {
        var no = item.No;
        if (no == undefined)
            no = item.NO;
        if (no == undefined)
            no = item.no;
        var name = item.Name;
        if (name == undefined)
            name = item.NAME;
        if (name == undefined)
            name = item.name;
        if (oldVal == no)
            isHaveSelect = true;
        $("#" + ddlChild).append("<option value='" + no + "'>" + name + "</option");

    });

   
    if (isHaveSelect == false) {
        $('#' + ddlChild + ' option:first').prop('selected', 'selected');
        layui.form.render("select");
        var select = 'dd[lay-value=' + $('#' + ddlChild).val() + ']';
        $("#" + ddlChild).siblings("div.layui-form-select").find('dl').find(select).click();//触发

    } else
        $('#' + ddlChild).val(oldVal);
    layui.form.render("select");
}

/**
 * 自动填充
 * @param {any} selectVal
 * @param {any} ddlChild
 * @param {any} fk_mapExt
 */
function DDLFullCtrl(selectVal, ddlChild, fk_mapExt) {

    FullIt(selectVal, fk_mapExt, ddlChild);
}
/**
* 填充其他控件
* @param selectVal 选中的值
* @param refPK mapExt关联的主键
* @param elementId 元素ID
*/
function FullIt(selectVal, refPK, elementId) {
    var oid = GetQueryString('OID');

    if (oid == null || oid==undefined)
        oid = GetQueryString('WorkID');

    if (oid == null || oid == undefined)
        oid = GetQueryString("RefPKVal");

    if (oid == null || oid == undefined) {
        oid = 0;
        return;
    }
    var mypk = "";
    if (refPK.indexOf("FullData") != -1)
        mypk = refPK;
    else
        mypk = refPK + "_FullData";

    //获得对象.
    var mapExt = new Entity("BP.Sys.MapExt");
    mapExt.SetPKVal(mypk);
    var i = mapExt.RetrieveFromDBSources();

    //没有填充其他控件
    if (i == 0)
        return;


    //执行填充主表的控件.
    FullCtrl(selectVal, elementId, mapExt);

    //执行个性化填充下拉框，比如填充ddl下拉框的范围.
    FullCtrlDDL(selectVal, elementId, mapExt);

    //执行填充从表.
    FullDtl(selectVal, mapExt);

    //执行确定后执行的JS
    var backFunc = mapExt.Tag2;
    if (backFunc != null && backFunc != "" && backFunc != undefined)
        DBAccess.RunFunctionReturnStr(DealSQL(backFunc, selectVal));
}


/**
*  填充主表数据信息.
*/
function FullCtrl(selectVal, ctrlIdBefore, mapExt) {
    var dbSrc = mapExt.Doc;
    if (dbSrc == null || dbSrc == "") {
        return;
    }

    //针对主表或者从表的文本框自动填充功能，需要确定填充的ID
    var beforeID = null;
    var endId = null;

    // 根据ddl 与 tb 不同。
    if (ctrlIdBefore.indexOf('DDL_') > 1) {
        beforeID = ctrlIdBefore.substring(0, ctrlIdBefore.indexOf('DDL_'));
        endId = ctrlIdBefore.substring(ctrlIdBefore.lastIndexOf('_'));
    } else {
        beforeID = ctrlIdBefore.substring(0, ctrlIdBefore.indexOf('TB_'));
        endId = ctrlIdBefore.substring(ctrlIdBefore.lastIndexOf('_'));
    }
    var isDtlField = endId !== "" ? true : false;

    var dataObj = GetDataTableByDB(dbSrc, mapExt.DBType, mapExt.FK_DBSrc, selectVal);

    TableFullCtrl(dataObj, ctrlIdBefore, beforeID, endId);

    //如果含有FullDataDtl也需要处理
    var mapExts = new Entities("BP.Sys.MapExts");
    mapExts.Retrieve("FK_MapData", mapExt.FK_MapData, "AttrOfOper", mapExt.AttrOfOper, "ExtType", "FullDataDtl");
    for (var i = 0; i < mapExts.length; i++) {
        var item = mapExts[i];
        var dbSrc = item.Doc;
        if (dbSrc != null && dbSrc != "") {
            var dataObj = GenerDB(dbSrc, selectVal, item.DBType, item.FK_DBSrc, isDtlField);

            TableFullCtrl(dataObj, ctrlIdBefore, beforeID, endId);
        }

    }
}

function TableFullCtrl(dataObj, ctrlIdBefore, beforeID, endId) {

    if ($.isEmptyObject(dataObj)) {
        return;
    }

    var data = dataObj[0]; //获得这一行数据.
    if (typeof frmMapAttrs != "undefined" && frmMapAttrs != null && frmMapAttrs.length != 0)
        data = DealDataTableColName(data, frmMapAttrs);
    else
        data = DealDataTableColName(data, mapAttrs);
    //遍历属性，给属性赋值.
    var valID;
    var tbs;
    var selects;
    for (var key in data) {

        var val = data[key];
        valID = $("#" + beforeID + "TB_" + key);
        if (valID.length == 1) {
            valID.val(val);
            continue;
        }
        valID = $("#" + beforeID + "TB_" + key + endId);
        if (valID.length == 1) {
            valID.val(val);
            continue;
        }

        valID = $("#" + beforeID + "DDL_" + key)
        if (valID.length == 1) {
            valID.val(val);
            continue;
        }
        valID = $("#" + beforeID + "DDL_" + key + endId);
        if (valID.length == 1) {
            valID.val(val);
            continue;
        }

        valID = $("#" + beforeID + 'CB_' + key);
        if (valID.length == 1) {
            if (val == '1') {
                valID.attr("checked", true);
            } else {
                valID.attr("checked", false);

            }
            continue;
        }
        valID = $("#" + beforeID + 'CB_' + key + endId);
        if (valID.length == 1) {
            if (val == '1') {
                valID.attr("checked", true);
            } else {
                valID.attr("checked", false);

            }
            continue;
        }

        //获取表单中所有的字段
        if (valID.length == 0) {
            if (tbs == undefined || tbs == "") {
                if (endId != "") {
                    //获取所在的行
                    tbs = $("#" + ctrlIdBefore).parent().parent().find("input")
                }
                else
                    tbs = $('input');
            }
            if (selects == undefined || selects == "") {
                if (endId != "") {
                    //获取所在的行
                    selects = $("#" + ctrlIdBefore).parent().parent().find("select")
                }
                else
                    selects = $('select');
            }

            $.each(tbs, function (i, tb) {
                var name = $(tb).attr("id");
                if (name == null || name == undefined)
                    return false;
                if (name.toUpperCase().indexOf(key) >= 0) {
                    if (name.indexOf("TB_") == 0)
                        $("#" + name).val(val);
                    if (name.indexOf("CB_")) {
                        if (val == '1') {
                            $("#" + name).attr("checked", true);
                        } else {
                            $("#" + name).attr("checked", false);

                        }
                    }
                    return false;
                }
            });
            $.each(selects, function (i, select) {
                var name = $(select).attr("id");
                if (name.toUpperCase().indexOf(key) >= 0) {
                    $("#" + name).val(val);
                    return false;
                }
            });
        }
    }
}

/**填充下拉框信息**/
function FullCtrlDDL(selectVal, ctrlID, mapExt) {

    var doc = mapExt.Tag;
    if (doc == "" || doc == null)
        return;

    var dbSrcs = doc.split('$'); //获得集合.
    for (var i = 0; i < dbSrcs.length; i++) {

        var dbSrc = dbSrcs[i];
        if (dbSrc == "" || dbSrc.length == 0)
            continue;
        var ctrlID = dbSrc.substring(0, dbSrc.indexOf(':'));
        var src = dbSrc.substring(dbSrc.indexOf(':') + 1);

        var db = GetDataTableByDB(src, mapExt.DBType, mapExt.FK_DBSrc, selectVal); //获得数据源.
        //重新绑定下拉框.
        GenerBindDDL("DDL_" + ctrlID, db);
    }
}
//填充明细.
function FullDtl(selectVal, mapExt) {
    if (mapExt.Tag1 == "" || mapExt.Tag1 == null)
        return;

    var dbType = mapExt.DBType;
    var dbSrc = mapExt.Tag1;
    var url = GetLocalWFPreHref();
    var dataObj;

    if (dbType == 1) {

        dbSrc = DealSQL(DealExp(dbSrc), e, kvs);
        dataObj = DBAccess.RunDBSrc(dbSrc, 1);

        //JQuery 获取数据源
    } else if (dbType == 2) {
        dataObj = DBAccess.RunDBSrc(dbSrc, 2);
    } else {
        var handler = new HttpHandler("BP.WF.HttpHandler.WF_CCForm");
        handler.AddPara("Key", selectVal);
        handler.AddPara("FK_MapExt", mapExt.MyPK);
        handler.AddPara("KVs", kvs);
        handler.AddPara("DoTypeExt", "ReqDtlFullList");
        handler.AddPara("OID", oid);
        var data = handler.DoMethodReturnString("HandlerMapExt");
        if (data == "")
            return;

        if (data.indexOf('err@') == 0) {
            alert(data);
            return;
        }
        dataObj = eval("(" + data + ")"); //转换为json对象 	
    }

    for (var i in dataObj.Head) {
        if (typeof (i) == "function")
            continue;

        for (var k in dataObj.Head[i]) {
            var fullDtl = dataObj.Head[i][k];
            //  alert('您确定要填充从表吗?，里面的数据将要被删除。' + key + ' ID= ' + fullDtl);
            var frm = document.getElementById('Dtl_' + fullDtl);

            var src = frm.src;
            if (src != undefined || src != null) {
                var idx = src.indexOf("&Key");
                if (idx == -1)
                    src = src + '&Key=' + selectVal + '&FK_MapExt=' + mapExt.MyPK;
                else
                    src = src.substring(0, idx) + '&ss=d&Key=' + selectVal + '&FK_MapExt=' + mapExt.MyPK;
                frm.src = src;
            }
        }
    }
}

