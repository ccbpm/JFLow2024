﻿
var step = 0;
$(function () {
    var workid = GetQueryString("WorkID");

    var handler = new HttpHandler("BP.WF.HttpHandler.WF_WorkOpt_OneWork");
    handler.AddPara("WorkID", workid);

    var ds = handler.DoMethodReturnJSON("JobSchedule_Init");

    var gwf = ds["WF_GenerWorkFlow"][0]; //工作记录.
    var nodes = ds["WF_Node"]; //节点.
    var dirs = ds["WF_Direction"]; //连接线.
    var tracks = ds["Track"]; //历史记录.

    var html = "<table style='height:100px;width: 100%; table-layout: fixed;'>";
    html += "<tr>";

    var step = 0;
    //循环历史记录.
    for (var i = 0; i < tracks.length; i++) {

        var tk = tracks[i];

        var doc = "<br><b>" + tk.NodeName + "</b>";
        doc += "<br>" + tk.EmpName;
        doc += "<br>" + tk.RDT.substring(0, 16);

        var info = "";
        if (tk.FK_Node == gwf.FK_Node)
            info = GenerIcon("DotGreen", i + 1, doc);
        else
            info = GenerIcon("DotBlue", i + 1, doc);

        step = i + 1;

        html += "<td style='text-align:center;vertical-align:top;'>" + info + "</td>";
    }
    //debugger
    //流程未完成的状态.
    if (gwf.WFState != 3) {

        //当前停留的节点.
        var currNode = gwf.FK_Node;

        var nodeName = "";
        for (var i = 0; i < 100; i++) {

            var nextNode = GetNextNodeID(currNode, dirs);
            if (nextNode == 0)
                break;

            for (var idx = 0; idx < nodes.length; idx++) {
                var nd = nodes[idx];
                if (nd.NodeID == nextNode) {
                    nodeName = nd.Name;
                    break;
                }
            }

            var doc = "<b><i>" + nodeName + "</i></b>";
            doc += "<br>";
            doc += "<br>";
            doc += "<br>";

            step = step + 1;
            currNode = nextNode;

            if (nextNode == 0)
                var info = GenerIcon("DotEnd", step, doc, true);
            else
                var info = GenerIcon("DotEnd", step, doc, false);

            html += "<td style='text-align:center;vertical-align:top;'>" + info + "</td>";

            if (nextNode == 0)
                break;
        }
    }

    // var info = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/DotEnd.png' />";
    // html += "<td style='text-align:center;vertical-align:top;'>" + info + "<br>结束</td>";

    html += "</tr>";
    html += "</table>";

    //html += "<img src='./Admin/FoolFormDesigner/Img/JobSchedule.png' />";

    $("#JobSchedule").html(html);
    // alert('sss');
    return;
});


function GenerIcon(icon, step, docs, isEndNode) {

    var url = basePath + "/WF/WorkOpt/OneWork/Img/" + icon + "-" + step + ".png";

    var barUrlLeft = "";
    var barUrlRight = "";

    if (icon == 'DotGreen') {
        barUrlRight = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/BarGreen.png' style='width:100%;margin-right:0px;margin-left:0px;padding-left:0px;padding-right:0px;' />";
        barUrlLeft = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/BarGreen.png' style='width:100%;margin-right:0px;margin-left:0px;padding-left:0px;padding-right:0px;' />";
    }

    if (icon == "DotBlue") {

        barUrlRight = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/BarGreen.png' style='width:100%;margin-right:0px;margin-left:0px;padding-left:0px;padding-right:0px;' />";
        barUrlLeft = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/BarGreen.png' style='width:100%;margin-right:0px;margin-left:0px;padding-left:0px;padding-right:0px;' />";

        if (step == 1)
            barUrlLeft = "";
    }

    if (icon == 'DotEnd') {
        barUrlRight = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/BarGiay.png' style='width:100%;margin-right:0px;margin-left:0px;padding-left:0px;padding-right:0px;' />";
        barUrlLeft = "<img src='" + basePath + "/WF/WorkOpt/OneWork/Img/BarGiay.png' style='width:100%;margin-right:0px;margin-left:0px;padding-left:0px;padding-right:0px;' />";
    }

    if (isEndNode == true)
        barUrlRight = "";

    var html = "";
    html += "<table style='height:100px;width: 100%; table-layout: fixed;border:none;margin:0px; padding:0px;'>";
    html += "<tr>";
    html += "<td style='border:none;width:40%;text-align:center;vertical-align:middle;margin:0px; padding:0px;'>" + barUrlLeft + "</td>";
    html += "<td style='border:none;margin:0px; padding:0px;width:20%;text-align:center;vertical-align:top;background-image: url('" + url + "'); background-repeat: no-repeat; background-attachment: fixed; background-position: center center'><img src='" + url + "'/></td>";
    html += "<td style='border:none;margin:0px; padding:0px;width:40%;text-align:center;vertical-align:middle;'>" + barUrlRight + "</td>";
    html += "<tr>";

    html += "<tr>";
    html += "<td colspan=3 style='border:none;' >" + docs + "</td>";
    html += "<tr>";
    html += "</table>";

    return html;
}

function GenerStart() {
    var str = "<div><img src='" + basePath + "/WF/WorkOpt/OneWork/Img/DotGreen1.png' /></div>";
}

//根据当前节点获得下一个节点.
function GetNextNodeID(nodeID, dirs) {
 //   debugger

    var toNodeID = 0;
    for (var i = 0; i < dirs.length; i++) {
        var dir = dirs[i];
        if (dir.Node == nodeID) {
            toNodeID = dir.ToNode;
            break;
        }
    }

    var toNodeID2 = 0;
    for (var i = 0; i < dirs.length; i++) {
        var dir = dirs[i];
        if (dir.Node == nodeID) {
            toNodeID2 = dir.ToNode;
        }
    }

    //两次去的不一致，就有分支，有分支就reutrn 0 .
    if (toNodeID2 == toNodeID)
        return toNodeID;

    return 0;
}

function GetNextNodeIDExpSpecNode_del(nodeID, specToNode, dirs) {

    for (var i = 0; i < dirs.length; i++) {
        var dir = dirs[i];
        if (dir.Node == currNode) {

            if (dir.ToNode == specToNode)
                return 0;
        }
    }
    return 0;
}
      
      