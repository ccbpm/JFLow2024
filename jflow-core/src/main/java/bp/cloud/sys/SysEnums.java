package bp.cloud.sys;

import bp.da.*;
import bp.en.*;
import bp.sys.*;
import bp.tools.StringHelper;

/**
 * 纳税人集合
 */
public class SysEnums extends Entities {
    /**
     * 此枚举类型的个数
     */
    public int Num = -1;

    public final String ToDesc() {
        String strs = "";
        for (SysEnum se : this.ToJavaList()) {
            strs += se.getIntKey() + " " + se.getLab() + ";";
        }
        return strs;
    }

    public final String GenerCaseWhenForOracle(String enName, String mTable, String key, String field, String enumKey, int def) throws Exception {
        String sql = (String) Cash.GetObjFormApplication("ESQL" + enName + mTable + key + "_" + enumKey, null);
        // string sql = "";
        if (sql != null) {
            return sql;
        }

        if (this.size() == 0) {
            throw new RuntimeException("@枚举值" + enumKey + "已被删除。");
        }

        sql = " CASE NVL(" + mTable + field + "," + def + ")";
        for (SysEnum se1 : this.ToJavaList()) {
            sql += " WHEN " + se1.getIntKey() + " THEN '" + se1.getLab() + "'";
        }

        SysEnum se = (SysEnum) this.GetEntityByKey(SysEnumAttr.IntKey, def);
        if (se == null) {
            sql += " END \"" + key + "Text\"";
        } else {
            sql += " WHEN NULL THEN '" + se.getLab() + "' END \"" + key + "Text\"";
        }

        Cash.AddObj("ESQL" + enName + mTable + key + "_" + enumKey, Depositary.Application, sql);
        return sql;
    }

    public final String GenerCaseWhenForOracle(String mTable, String key, String field, String enumKey, int def) throws Exception {
        if (this.size() == 0) {
            throw new RuntimeException("@枚举值（" + enumKey + "）已被删除，无法形成期望的SQL。");
        }


        String sql = "";
        sql = " CASE " + mTable + field;
        for (SysEnum se1 : this.ToJavaList()) {
            sql += " WHEN " + se1.getIntKey() + " THEN '" + se1.getLab() + "'";
        }

        SysEnum se = (SysEnum) this.GetEntityByKey(SysEnumAttr.IntKey, def);
        if (se == null) {
            sql += " END \"" + key + "Text\"";
        } else {
            sql += " WHEN NULL THEN '" + se.getLab() + "' END \"" + key + "Text\"";
        }

        // Cash.AddObj("ESQL" + enName + key + "_" + enumKey, Depositary.Application, sql);
        return sql;
    }

    public final void LoadIt(String enumKey) throws Exception {
        if (this.Full(enumKey) == false) {

            try {
                bp.da.DBAccess.RunSQL("UPDATE Sys_Enum SET Lang='" + bp.web.WebUser.getSysLang() + "' WHERE LANG IS NULL ");

                bp.da.DBAccess.RunSQL("UPDATE Sys_Enum SET MyPK=EnumKey+'_'+Lang+'_'+cast(IntKey as NVARCHAR )");

                //增加数据库类型判断
                DBUrl dbUrl = new DBUrl();
                if (DBType.MSSQL == dbUrl.getDBType()) {
                    bp.da.DBAccess.RunSQL("UPDATE Sys_Enum SET MyPK=EnumKey+'_'+Lang+'_'+cast(IntKey as NVARCHAR )");
                } else if (DBType.Oracle == dbUrl.getDBType() || DBType.KingBaseR3 == dbUrl.getDBType() || DBType.KingBaseR6 == dbUrl.getDBType()) {
                    bp.da.DBAccess.RunSQL("UPDATE Sys_Enum SET MyPK = EnumKey || '_' || Lang || '_' || cast(IntKey  as VARCHAR(5))");
                } else if (DBType.MySQL == dbUrl.getDBType()) {
                    bp.da.DBAccess.RunSQL("UPDATE Sys_Enum SET MyPK = CONCAT (EnumKey,'_', Lang,'_',CAST(IntKey AS CHAR(5)))");
                }

            } catch (java.lang.Exception e) {

            }

            try {
                bp.sys.xml.EnumInfoXml xml = new bp.sys.xml.EnumInfoXml(enumKey);
                this.RegIt(enumKey, xml.getVals());
            } catch (Exception ex) {
                throw new RuntimeException("@你没有预制[" + enumKey + "]枚举值。@在修复枚举值出现错误:" + ex.getMessage());
            }
        }
    }

    /**
     * 把所有的枚举注册一遍.
     */
    public static void RegAll() throws Exception {
        bp.sys.xml.EnumInfoXmls xmls = new bp.sys.xml.EnumInfoXmls();
        xmls.RetrieveAll();
        SysEnums ses = new SysEnums();
        for (bp.sys.xml.EnumInfoXml xml : xmls.ToJavaList()) {
            ses.RegIt(xml.getKey(), xml.getVals());
        }
    }

    /**
     * SysEnums
     *
     * @param enumKey
     */
    public SysEnums(String enumKey) throws Exception {
        this.LoadIt(enumKey);
    }

    public SysEnums(String enumKey, String vals) throws Exception {
        if (DataType.IsNullOrEmpty(vals)) {
            this.LoadIt(enumKey);
            return;
        }

        if (this.Full(enumKey) == false) {
            this.RegIt(enumKey, vals);
        }
    }

    public final void RegIt(String EnumKey, String vals) {
        try {
            String[] strs = vals.split("[@]", -1);
            SysEnums ens = new SysEnums();
            ens.Delete(SysEnumAttr.EnumKey, EnumKey);
            this.clear();

            for (String s : strs) {
                if (DataType.IsNullOrEmpty(s)) {
                    continue;
                }

                String[] vk = s.split("[=]", -1);
                SysEnum se = new SysEnum();
                se.setIntKey(Integer.parseInt(vk[0]));
                //杨玉慧
                //解决当  枚举值含有 ‘=’号时，保存不进去的方法
                String[] kvsValues = new String[vk.length - 1];
                for (int i = 0; i < kvsValues.length; i++) {
                    kvsValues[i] = vk[i + 1];
                }
                se.setLab(StringHelper.join("=", kvsValues));
                se.setEnumKey(EnumKey);
                se.setLang(bp.web.WebUser.getSysLang());
                se.Insert();
                this.AddEntity(se);
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex.getMessage() + " - " + vals);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //  this.Full(EnumKey);
    }

    public final boolean Full(String enumKey) throws Exception {
        Entities ens = (Entities) Cash.GetObjFormApplication("EnumOf" + enumKey + bp.web.WebUser.getSysLang(), null);
        if (ens != null) {
            this.AddEntities(ens);
            return true;
        }

        QueryObject qo = new QueryObject(this);
        qo.AddWhere(SysEnumAttr.EnumKey, enumKey);
        qo.addAnd();
        qo.AddWhere(SysEnumAttr.Lang, bp.web.WebUser.getSysLang());
        qo.addOrderBy(SysEnumAttr.IntKey);
        if (qo.DoQuery() == 0) {
            // 看看xml配置里面是否有?
            return false;
        }

        Cash.AddObj("EnumOf" + enumKey + bp.web.WebUser.getSysLang(), Depositary.Application, this);
        return true;
    }
    ///// <summary>
    ///// DBSimpleNoNames
    ///// </summary>
    ///// <returns></returns>
    //public DBSimpleNoNames ToEntitiesNoName()
    //{
    //    DBSimpleNoNames ens = new DBSimpleNoNames();
    //    foreach (SysEnum en in this)
    //    {
    //        ens.AddByNoName(en.IntKey.ToString(), en.Lab);
    //    }
    //    return ens;
    //}

    /**
     * @param key
     * @param val
     * @return
     */
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new int Delete(string key, object val)
    public final int Delete(String key, Object val) throws Exception {
        try {
            Entity en = this.getGetNewEntity();
            Paras ps = new Paras();

            ps.SQL = "DELETE FROM " + en.getEnMap().getPhysicsTable() + " WHERE OrgNo=" + key + "," + key + "=" + en.getHisDBVarStr() + "p";
            ps.Add("p", val);
            return en.RunSQL(ps);
        } catch (java.lang.Exception e) {
            Entity en = this.getGetNewEntity();
            en.CheckPhysicsTable();

            Paras ps = new Paras();
            ps.SQL = "DELETE FROM " + en.getEnMap().getPhysicsTable() + " WHERE " + key + "=" + en.getHisDBVarStr() + "p";
            ps.Add("p", val);
            return en.RunSQL(ps);
        }
    }

    /**
     * SysEnums
     */
    public SysEnums() {
    }

    /**
     * 得到它的 Entity
     */
    @Override
    public Entity getGetNewEntity() {
        return new SysEnum();
    }

    /**
     * 通过int 得到Lab
     *
     * @param val val
     * @return string val
     */
    public final String GetLabByVal(int val) {
        for (SysEnum en : this.ToJavaList()) {
            if (en.getIntKey() == val) {
                return en.getLab();
            }
        }
        return null;
    }


    ///#region 为了适应自动翻译成java的需要,把实体转换成List.

    /**
     * 转化成 java list,C#不能调用.
     *
     * @return List
     */
    public final java.util.List<SysEnum> ToJavaList() {
        return (java.util.List<SysEnum>) (Object) this;
    }

    /**
     * 转化成list
     *
     * @return List
     */
    public final java.util.ArrayList<SysEnum> Tolist() {
        java.util.ArrayList<SysEnum> list = new java.util.ArrayList<SysEnum>();
        for (int i = 0; i < this.size(); i++) {
            list.add((SysEnum) this.get(i));
        }
        return list;
    }

    ///#endregion 为了适应自动翻译成java的需要,把实体转换成List.
}