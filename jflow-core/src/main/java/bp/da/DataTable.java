package bp.da;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bp.tools.StringHelper;

public class DataTable implements Cloneable
{
	
	/**
	 * 保存DataRow的集合，在DataTable初始化時，便會建立
	 */
	public DataRowCollection Rows;
	/**
	 * 保存DataColumn的集合，在DataTable初始化時，便會建立
	 */
	public DataColumnCollection Columns;
	/**
	 * DataTable的名稱，沒什麼用到
	 */
	public String TableName;
	private DataRow[] dataRows;
	
	
	/**
	 * @return 复制DataTable的结构
	 */
	public DataTable clone(){		
		DataTable v = new DataTable();
		for(DataColumn Column: this.Columns){
			v.Columns.Add(Column.ColumnName,Column.DataType);
		}
		return v;
		
	}
	
	/**
	 * @return 复制DataTable的结构与数据。
	 * @throws CloneNotSupportedException
	 */
	public DataTable copy()
	{
		try
		{
			DataTable v = (DataTable) super.clone();
			v.Rows = (DataRowCollection) this.Rows.clone();
			v.Columns = (DataColumnCollection) this.Columns.clone();
			return v;
		} catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}
	
	/**
	 * 初始化DataTable，並建立DataColumnCollection，DataRowCollection
	 */
	public DataTable()
	{
		this.Columns = new DataColumnCollection(this);
		this.Rows = new DataRowCollection(this);		
	}
	
	/**
	 * 除了初始化DataTable， 可以指定DataTable的名字(沒什麼意義)
	 * 
	 * @param tableName DataTable的名字
	 */
	public DataTable(String tableName)
	{
		this();
		this.TableName = tableName;
	}
	
	/**
	 * 由此DataTable物件來建立一個DataRow物件
	 * 
	 * @return DataRow
	 */
	public DataRow NewRow()
	{
		
		DataRow row = new DataRow(this);// DataRow為呼叫此方法DataTable的成員
		
		return row;
	}
	
	/**
	 * 把DataTable當做二維陣列，給列索引和行索引，設定值的方法 <br/>
	 * (發佈者自行寫的方法)
	 * 
	 * @param rowIndex
	 *            列索引(從0算起)
	 * @param columnIndex
	 *            行索引(從0算起)
	 * @param value
	 *            要給的值
	 */
	public void setValue(int rowIndex, int columnIndex, Object value)
	{
		this.Rows.get(rowIndex).setValue(columnIndex, value);
	}
	
	/**
	 * 把DataTable當做二維陣列，給列索引和行名稱，設定值的方法 <br/>
	 * (發佈者自行寫的方法)
	 * 
	 * @param rowIndex 列索引(從0算起)
	 * @param columnName 行名稱
	 * @param value 要給的值
	 */
	public void setValue(int rowIndex, String columnName, Object value)
	{
		this.Rows.get(rowIndex).setValue(columnName.toLowerCase(), value);
	}
	
	/**
	 * 把DataTable當做二維陣列，給列索引和行索引，取得值的方法 <br/>
	 * (發佈者自行寫的方法)
	 * 
	 * @param rowIndex
	 *            列索引(從0算起)
	 * @param columnIndex
	 *            行索引(從0算起)
	 * @return 回傳該位置的值
	 */
	public Object getValue(int rowIndex, int columnIndex)
	{
		return this.Rows.get(rowIndex).getValue(columnIndex);
	}
	
	public void Clear()
	{
		Rows.clear();
		Columns.clear();
	}
	
	public void ClearRow()
	{
		Rows.clear();
	}
	
	public List<DataRow> Select(Map<String, Object> filterMap) throws Exception {
//		return Select(filterMap, false);
//	}
//	
//	public List<DataRow> Select(Map<String, Object> filterMap, boolean isOr) throws Exception {
		List<DataRow> dataRowList = new ArrayList<DataRow>();
		outer: for (int i = 0; i < Rows.size(); i++)
		{
			DataRow row = Rows.get(i);
			for (Object key : filterMap.keySet().toArray())
			{
				Object lefteql = filterMap.get(key);
				Object righteql = row.getValue(key.toString());
				if (lefteql == null || "".equals(lefteql))
				{
					if (righteql == null || "".equals(righteql))
					{
						continue;
					} else
					{
						continue outer;
					}
				} else if (!filterMap.get(key).toString().toUpperCase().equals(
						row.getValue(key.toString()).toString().toUpperCase()))
				{
					continue outer;
				}
			}
			dataRowList.add(row);
		}
		return dataRowList;
	}
	
	/**  
     * 返回符合过滤条件的数据行集合，并返回
     * @param filterString 过滤字符串，例如  a>1 and a>=2 or a<3 and a<=4 or a!=5 and a=6 or a!='b' and a='c'
     * @return 过滤后的 List<DataRow>    
     */
   public List<DataRow> select(String filterString) {
        List<DataRow> rows = new ArrayList<DataRow>();
        if (StringHelper.isNullOrEmpty(filterString) == true)
			return this.Rows;

        boolean bl;
        for (Object row : this.Rows) {
			 DataRow currentRow = (DataRow) row;
			 try {
				 bl = dataRowCompute(filterString, currentRow);
			 } catch (Exception e) {
				 System.err.println("语法错误");
				 e.printStackTrace();
				 continue;
			 }
			 if (bl) {
				 rows.add(currentRow);
			 }
		 }
        return rows;


   }
   
   /**  
    * 返回符合过滤条件的数据行集合，并返回
    * @param filterString 过滤字符串，例如  a>1 and a>=2 or a<3 and a<=4 or a!=5 and a=6 or a!='b' and a='c'
    * @return 过滤后的 List<DataRow>    
    */
  public List<DataRow> selectx(String filterString) {
       List<DataRow> rows = new ArrayList<DataRow>();
       if (StringHelper.isNullOrEmpty(filterString) == true)
		   return rows;

	 boolean bl = false;
	 for (Object row : Rows) {
		DataRow currentRow = (DataRow) row;
		try {
			//bl = dataRowCompute(filterString, currentRow);
			if(filterString.split("=").length>1)
				bl = currentRow.getValue(filterString.split("=")[0].trim()).equals(filterString.split("=")[1].trim());
		} catch (Exception e) {
		 System.err.println("语法错误");
		 e.printStackTrace();
			continue;
		}
		if (bl) {
			rows.add(currentRow);
		}
	 }
	 return rows;


  }
   
   /**
    * 数据行计算，是否符合filterString过滤条件
    * @param filterString 过滤条件，支持  and or > >= < <= != = 操作符，暂不支持括号
    * @param row 数据行
    * @return true 符合
    */
   private boolean dataRowCompute(String filterString, DataRow row){
	   if (filterString == null || row == null){
		   return false;
	   }
	   boolean orResult = false;
	   try {
		   String[] or = filterString.split(" (?i)or ");	// 忽略大小写
		   for (String o : or){
			   boolean andResult = true;
			   String[] and = o.split(" (?i)and ");	// 忽略大小写
			   for (String a : and){
				   String[] kv = null;
				   if (a.contains(">=")){
					   kv = a.split(">=");
				   }else if (a.contains(">")){
					   kv = a.split(">");
				   }else if (a.contains("<=")){
					   kv = a.split("<=");
				   }else if (a.contains("<")){
					   kv = a.split("<");
				   }else if (a.contains("!=")){
					   kv = a.split("!=");
				   }else if (a.contains("=")){
					   kv = a.split("=");
				   }
				   
				   
				   if (kv != null && kv.length == 2){
					   String key = kv[0].trim(), value = kv[1].trim();
					   if (key != null && value != null){
						   Object val = row.getValue(key);
						   // is null
						   if (value.equalsIgnoreCase("is null")){
							   if (!(val == null)){
								   andResult = false;
								   break;
							   }
						   }
						   // is not null
						   else if (value.equalsIgnoreCase("is not null")){
							   if (!(val != null)){
								   andResult = false;
								   break;
							   }
						   }
						   // is string
						   else if (value.startsWith("'") && value.endsWith("'")){
							   String v = value.replaceAll("'", "");
							   if (a.contains("!=")){
								   if (!(!val.toString().equalsIgnoreCase(v))){
									   andResult = false;
									   break;
								   }
							   }else if (a.contains("=")){
								   if (!(val.toString().equalsIgnoreCase(v))){
									   andResult = false;
									   break;
								   }
							   }
						   }
						   // is number
						   else{
							   Integer v = Integer.valueOf(value.toString());
							   Integer dbVal = Integer.valueOf(val.toString());
							   if (a.contains(">=")){
								   if (!(dbVal >= v)){
									   andResult = false;
									   break;
								   }
							   }else if (a.contains(">")){
								   if (!(dbVal > v)){
									   andResult = false;
									   break;
								   }
							   }else if (a.contains("<=")){
								   if (!(dbVal <= v)){
									   andResult = false;
									   break;
								   }
							   }else if (a.contains("<")){
								   if (!(dbVal < v)){
									   andResult = false;
									   break;
								   }
							   }else if (a.contains("!=")){
								   if (!(dbVal != v)){
									   andResult = false;
									   break;
								   }
							   }else if (a.contains("=")){
								   if (!(dbVal == v)){
									   andResult = false;
									   break;
								   }
							   }
						   }
					   }
				   }
			   }
			   // 如果有一个and成立，则成立。
			   if (andResult){
				   orResult = true;
				   break;
			   }
		   }
       } catch (Exception e) {
			System.err.println("语法错误");
			e.printStackTrace();
       }
	   return orResult;
    }
	
	public String getTableName() {
		return TableName;
	}

	public void setTableName(String tableName) {
		TableName = tableName;
	}

	/**
	 * 把DataTable當做二維陣列，給列索引和行名稱，取得值的方法 <br/>
	 * (發佈者自行寫的方法)
	 * 
	 * @param rowIndex 列索引(從0算起)
	 * @param columnName 行名稱
	 * @return 回傳該位置的值
	 */
	public Object getValue(int rowIndex, String columnName)
	{
		return this.Rows.get(rowIndex).getValue(columnName);
	}

	public DataRow[] Select(String string) {
		DataRow[] dataRowsx = null;
		List<DataRow> dataRowList = new ArrayList<DataRow>();
        if (StringHelper.isNullOrEmpty(string)==false) {
        	 boolean bl;
             for (Object row : Rows) {
                 DataRow currentRow = (DataRow) row;
                 try {
                     bl = dataRowCompute(string, currentRow);
                 } catch (Exception e) {
                	 System.err.println("语法错误");
                	 e.printStackTrace();
                     continue;
                 }
                 if (bl) {
                	 dataRowList.add(currentRow);
                 }
             }
             dataRowsx = new DataRow[dataRowList.size()];
             for(int i=0;i<dataRowList.size();i++){
            	 dataRowsx[i] = dataRowList.get(i);
             }
            return dataRowsx;

        } else {
            return dataRowsx;
        }
	}
	
	
	public void WriteXml(String path, DataTable dt) {
		StringBuilder str = new StringBuilder("<?xml version=\"1.0\" standalone=\"yes\"?>");
		for (int k = 0; k < dt.Rows.size(); k++) {
			str.append("<");
			str.append(dt.TableName);
			str.append(">");
			for (int j = 0; j < dt.Columns.size(); j++) {
				DataColumn dc = dt.Columns.get(j);

				str.append("<");
				str.append(dc.ColumnName);
				str.append(">");
				try {
					Object value = dt.Rows.get(k).getValue(dc);
					if (value.toString().contains(">") || value.toString().contains("<") || value.toString().contains("&")
							|| value.toString().contains("'") || value.toString().contains("\"")) {
						value = value.toString().replace(">", "&gt;");
						value = value.toString().replace("<", "&lt;");
						value = value.toString().replace("&", "&amp;");
						value = value.toString().replace("'", "&apos;");
						value = value.toString().replace("\"", "&quot;");
					}
					str.append(value);
				} catch (Exception e) {

				}
				str.append("</");
				str.append(dc.ColumnName);
				str.append(">");

			}
			str.append("</");
			str.append(dt.TableName);
			str.append(">");
		}
		
		// }
		String temp = str.toString();
		// String temp = formatXml(str.toString());

		// 写入文件
		File file = new File(path);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
			BufferedWriter br = new BufferedWriter(osw);
			br.write(temp.toString());
			fos.flush();
			br.close();
			fos.close();
			osw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
