// 菜单处理类
// 非系统菜单
var nonSystemItems = ['Forms', 'Frms', 'Flows', 'flows', 'frms', 'forms']

function MenuConvertTools(webUser, data) {
    this.webUser = webUser;
    this.data = data;
}

var webUser = new WebUser();
var sid = GetQueryString("Token");
function Start() {
    if (webUser.CCBPMRunModel == 2)
        vm.openTab('发起', '../../App/Start.htm');
    else
        vm.openTab('发起', '../Start.htm');
}

function Todolist() {
    if (webUser.CCBPMRunModel == 2)
        vm.openTab('待办', '../../App/Todolist.htm');
    else
        vm.openTab('待办', '../Todolist.htm');
}

function Runing() {
    if (webUser.CCBPMRunModel == 2)
        vm.openTab('在途', '../../App/Runing.htm');
    else
        vm.openTab('在途', '../Runing.htm');
}

function Search() {
    if (webUser.CCBPMRunModel == 2)
        vm.openTab('查询', '../../App/Search.htm');
    else
        vm.openTab('查询', '../Search.htm');
}

function Fasts() {
    var urlEnd = "?Token=" + GetQueryString("Token") + "&OrgNo=" + GetQueryString("OrgNo") + "&UserNo=" + GetQueryString("UserNo");
    vm.openTab('低代码', '../GPM/Menus.htm' + urlEnd);
}

function Flows() {
    var urlEnd = "?Token=" + GetQueryString("Token") + "&OrgNo=" + GetQueryString("OrgNo") + "&UserNo=" + GetQueryString("UserNo");
    vm.openTab('流程', 'Flows.htm' + urlEnd);
}

function Frms() {
    var urlEnd = "?Token=" + GetQueryString("Token") + "&OrgNo=" + GetQueryString("OrgNo") + "&UserNo=" + GetQueryString("UserNo");
    vm.openTab('表单', 'Frms.htm' + urlEnd);
}

function OpenOrg() {

    var urlEnd = "?Token=" + GetQueryString("Token") + "&OrgNo=" + GetQueryString("OrgNo") + "&UserNo=" + GetQueryString("UserNo");

    if (webUser.CCBPMRunModel == 2)
        vm.openTab('组织', '../../App/App/Organization.htm' + urlEnd);
    else
        vm.openTab('组织', '../../GPM/Organization.htm' + urlEnd);
}

var systemNodes;
var moduleNode;
var menuNode;

// 获取系统菜单
MenuConvertTools.prototype.getSystemMenus = function () {

    systemNodes = this.data['System'];
    moduleNode = this.data['Module'];
    menuNode = this.data['Menu'];

    var webUser = new WebUser();

    var adminMenuNodes = [];
    //循环系统.
    for (var idx = 0; idx < systemNodes.length; idx++) {
        var systemNode = systemNodes[idx];
        if (nonSystemItems.indexOf(systemNode.No) > -1) continue;
        systemNode.children = [];
        systemNode.open = false;
        if (systemNode.No === "System") {
            systemNode.Icon = 'icon-settings';
            systemNode.Name = "系统管理";
        }

        if (systemNode.Icon === '')
            systemNode.Icon = 'icon-settings';

        //循环模块.
        for (var idxModule = 0; idxModule < moduleNode.length; idxModule++) {

            var moduleEn = moduleNode[idxModule];
            if (moduleEn.SystemNo !== systemNode.No)
                continue; //如果不是本系统的.
            moduleEn.children = [];
            if (moduleEn.Icon === "" || moduleEn.Icon == null || moduleEn.Icon === "")
                moduleEn.Icon = 'icon-list';
            moduleEn.open = false;


            //增加菜单.
            for (var idxMenu = 0; idxMenu < menuNode.length; idxMenu++) {

                var menu = menuNode[idxMenu];
                if (moduleEn.No !== menu.ModuleNo)
                    continue; // 不是本模块的。
                if (menu.MenuModel == "FlowEntityBatchStart")
                    continue;

                menu = DealMenuUrl(menu);

                if (menu.Url.indexOf('@WebUser.FK_Dept') > 0)
                    menu.Url = menu.Url.replace('@WebUser.FK_Dept', webUser.FK_Dept);

                if (menu.Url.indexOf('@WebUser.No') > 0)
                    menu.Url = menu.Url.replace('@WebUser.No', webUser.No);

                if (menu.Url.indexOf('@WebUser.OrgNo') > 0)
                    menu.Url = menu.Url.replace('@WebUser.OrgNo', webUser.OrgNo);

                if (menu.Icon === '')
                    menu.Icon = 'icon-user';

                moduleEn.children.push(menu);
            }
            systemNode.children.push(moduleEn);

        }
        adminMenuNodes.push(systemNode)
    }
    return adminMenuNodes
}

MenuConvertTools.prototype.convertToTreeData = function () {
    var topNodes = [];
    if (this.webUser.No === "admin" || parseInt(this.webUser.IsAdmin) === 1) {
        // if (this.getFlowMenu(this.data).length !== 0)
        //     topNodes.push(this.getFlowMenu(this.data));
        //  if (this.getFormMenu(this.data).length !== 0)
        //     topNodes.push(this.getFormMenu(this.data));
    }
    topNodes = topNodes.concat(this.getSystemMenus(this.data))
    // console.log(topNodes)
    return topNodes
}

function getPortalConfigByKey(key, defVal) {
    if (typeof PortalConfig == "undefined") {
        PortalConfig = {};
        PortalConfig[key] = defVal;
        return defVal;
    }
    if (PortalConfig[key] == undefined)
        PortalConfig[key] = defVal;
    return PortalConfig[key];
}
