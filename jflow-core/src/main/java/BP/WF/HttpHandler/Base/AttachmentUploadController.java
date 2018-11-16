package BP.WF.HttpHandler.Base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import BP.DA.DataType;
import BP.En.ClassFactory;
import BP.En.Entities;
import BP.En.Entity;
import BP.Sys.AthCtrlWay;
import BP.Sys.AthSaveWay;
import BP.Sys.AthUploadWay;
import BP.Sys.FrmAttachment;
import BP.Sys.FrmAttachmentDB;
import BP.Sys.GEEntity;
import BP.Sys.MapData;
import BP.Sys.PubClass;
import BP.Sys.SystemConfig;
import BP.Sys.FrmEventList;
import BP.Tools.FileAccess;
import BP.Tools.FtpUtil;
import BP.Tools.SftpUtil;
import BP.WF.HttpHandler.Base.BaseController;
import BP.WF.Template.FrmNode;
import BP.WF.Template.FrmSln;
import BP.WF.Template.WhoIsPK;
import BP.Sys.Glo;
import BP.Web.WebUser;
import BP.Tools.ContextHolderUtils;


@Controller
@RequestMapping("/WF/Ath")
@Scope("request")
public class AttachmentUploadController extends BaseController {

	public String getFK_FrmAttachment() {
		return ContextHolderUtils.getRequest().getParameter("FK_FrmAttachment");
	}

	public String getTB_Note() {
		return ContextHolderUtils.getRequest().getParameter("TB_Note");
	}

	public String getddl() {
		return ContextHolderUtils.getRequest().getParameter("ddl");
	}

	public String getDelPKVal() {
		return ContextHolderUtils.getRequest().getParameter("DelPKVal");
	}

	public String getMyPK() {
		return ContextHolderUtils.getRequest().getParameter("MyPK");
	}

	public String getPKVal() {
		return ContextHolderUtils.getRequest().getParameter("PKVal");
	}

	public String getParasData() {
		return ContextHolderUtils.getRequest().getParameter("parasData");
	}
	public String getSort() {
		String sort =  ContextHolderUtils.getRequest().getParameter("Sort");
		if(DataType.IsNullOrEmpty(sort))
			sort ="";
		return sort;
	}

	@RequestMapping(value = "/AttachmentUpload.do")
	public void upload(@RequestParam("Filedata") MultipartFile multiFile, HttpServletRequest request,
			HttpServletResponse response, BindException errors) throws Exception {
		String savePath = "upload";
		try {
            //使用Apache文件上传组件处理文件上传步骤：
            //1、创建一个DiskFileItemFactory工厂
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
            //2、创建一个文件上传解析器
            ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
            //解决上传文件名的中文乱码
            fileUpload.setHeaderEncoding("UTF-8");
            //3、判断提交上来的数据是否是上传表单的数据
           // if(!fileUpload.isMultipartContent(request)){
            //    //按照传统方式获取数据
             //   return;
           // }
            //4、使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
            List<FileItem> list = fileUpload.parseRequest(request);
            for (FileItem item : list) {
                //如果fileitem中封装的是普通输入项的数据
                if(item.isFormField()){
                    String name = item.getFieldName();
                    //解决普通输入项的数据的中文乱码问题
                    String value = item.getString("UTF-8");
                    String value1 = new String(name.getBytes("iso8859-1"),"UTF-8");
                    System.out.println(name+"  "+value);
                    System.out.println(name+"  "+value1);
                }else{
                    //如果fileitem中封装的是上传文件，得到上传的文件名称，
                    String fileName = item.getName();
                    System.out.println(fileName);
                    if(fileName==null||fileName.trim().equals("")){
                        continue;
                    }
                    //注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：  c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
                    //处理获取到的上传文件的文件名的路径部分，只保留文件名部分
                    fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1);
                    //获取item中的上传文件的输入流
                    InputStream is = item.getInputStream();
                    //创建一个文件输出流
                    FileOutputStream fos = new FileOutputStream(savePath+File.separator+fileName);
                    //创建一个缓冲区
                    byte buffer[] = new byte[1024];
                    //判断输入流中的数据是否已经读完的标识
                    int length = 0;
                    //循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
                    while((length = is.read(buffer))>0){
                        //使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\" + filename)当中
                        fos.write(buffer, 0, length);
                    }
                    //关闭输入流
                    is.close();
                    //关闭输出流
                    fos.close();
                    //删除处理文件上传时生成的临时文件
                    item.delete();
                    //message = "文件上传成功";
                }
            }
        } catch (FileUploadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //message = "文件上传失败";
        }
		
		
		String error = "";
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String parasData = multipartRequest.getParameter("parasData");
		CommonsMultipartFile item = (CommonsMultipartFile) multipartRequest.getFile("file");
		if (item == null)
			item = (CommonsMultipartFile) multiFile;
		int maxSize = 50 * 1024 * 1024; // 单个上传文件大小的上限

		// 获取初始化信息
		FrmAttachment athDesc = new FrmAttachment(this.getFK_FrmAttachment());
		GEEntity en = new GEEntity(athDesc.getFK_MapData());
		en.setPKVal(this.getPKVal());
		en.RetrieveFromDBSources();
		MapData mapData = new MapData(athDesc.getFK_MapData());
		String msg = null;
		
		uploadFile(item, athDesc, en, msg, mapData, this.getFK_FrmAttachment(), parasData);

		return;
 
	}

	@RequestMapping(value = "/AttachmentUploadS.do", method = RequestMethod.POST)
	public void execute(HttpServletRequest request, HttpServletResponse response, BindException errors)
			throws Exception {
		String error = "";
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		CommonsMultipartFile item = (CommonsMultipartFile) multipartRequest.getFile("file");
		int maxSize = 50 * 1024 * 1024; // 单个上传文件大小的上限

		// 获取初始化信息
		FrmAttachment athDesc = new FrmAttachment(this.getFK_FrmAttachment());
		GEEntity en = new GEEntity(athDesc.getFK_MapData());
		en.setPKVal(this.getPKVal());
		en.Retrieve();
		MapData mapData = new MapData(athDesc.getFK_MapData());
		String msg = null;
		uploadFile(item, athDesc, en, msg, mapData, this.getFK_FrmAttachment(), getParasData());
		return;
		 
	}

	@RequestMapping(value = "/downLoad.do", method = RequestMethod.GET)
	public void downLoad(HttpServletRequest request, HttpServletResponse response) {
		FrmAttachmentDB downDB = new FrmAttachmentDB();

		try {
			downDB.setMyPK(this.getDelPKVal() == null ? this.getMyPK() : this.getDelPKVal());
			downDB.Retrieve();
			FrmAttachment dbAtt = new FrmAttachment();
			dbAtt.setMyPK(downDB.getFK_FrmAttachment());
			dbAtt.Retrieve();
			if (dbAtt.getAthSaveWay() == AthSaveWay.WebServer) {
				PubClass.DownloadFile(downDB.getFileFullName(), downDB.getFileName());

			}

			if (dbAtt.getAthSaveWay() == AthSaveWay.FTPServer) {
				// #region 解密下载
				// 1、先下载到本地
				String guid = BP.DA.DBAccess.GenerGUID();
				// 把文件临时保存到一个位置.
				String temp = SystemConfig.getPathOfTemp() + "" + guid + ".tmp";

				// 解密的文件保存的路径
				String jieMiFile = SystemConfig.getPathOfTemp() + "" + guid + downDB.getFileExts();

				if (SystemConfig.getFTPServerType().equals("SFTP") ) {

					// 连接FTP服务器并下载文件到本地
					SftpUtil ftpUtil =BP.WF.Glo.getSftpUtil(); 
					ftpUtil.downloadFile(downDB.getFileFullName(), temp);
				}
				
				
				if (SystemConfig.getFTPServerType().equals("FTP") ) {

					// 连接FTP服务器并下载文件到本地
					FtpUtil ftpUtil =BP.WF.Glo.getFtpUtil();					 
					ftpUtil.downloadFile(downDB.getFileFullName(), temp);
				}


				// 解密文件
				Glo.File_JieMi(temp, jieMiFile);

				// #region 文件下载（并删除临时明文文件）
				jieMiFile = PubClass.toUtf8String(request, jieMiFile);

				response.setContentType("application/octet-stream;charset=utf8");
				response.setHeader("Content-Disposition",
						"attachment;filename=" + PubClass.toUtf8String(request, downDB.getFileName()));
				response.setHeader("Connection", "close");
				// 读取目标文件，通过response将目标文件写到客户端
				// 读取文件
				InputStream in = new FileInputStream(new File(jieMiFile));
				OutputStream out = response.getOutputStream();
				// 写文件
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
				out.close();

				// 删除临时文件
				new File(temp).delete();
				new File(jieMiFile).delete();
			}

			if (dbAtt.getAthSaveWay() == AthSaveWay.DB) {

				PubClass.DownloadFile(downDB.getFileFullName(), downDB.getFileName());
			}

			return;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	@RequestMapping(value = "/EntityFileLoad.do", method = RequestMethod.GET)
	public void EntityFileLoad(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		 //根据EnsName获取Entity
        Entities ens = ClassFactory.GetEns(this.getEnsName());
        Entity en = ens.getGetNewEntity();
        en.setPKVal(this.getDelPKVal());
        int i = en.RetrieveFromDBSources();
        if (i == 0)
            return ;
        
        String filePath = (String)en.GetValByKey("MyFilePath");
        String fileName = (String)en.GetValByKey("MyFileName");
        //获取使用的客户 TianYe集团保存在FTP服务器上
        if (SystemConfig.getCustomerNo().equals("TianYe"))
        {
           
            //临时存储位置
            String guid = BP.DA.DBAccess.GenerGUID();
            String tempFile = SystemConfig.getPathOfTemp() + guid + "." + en.GetValByKey("MyFileExt");

            if (new File(tempFile).exists() == true)
            	new File(tempFile).delete();
            
            // 连接FTP服务器并下载文件到本地
			FtpUtil ftpUtil =BP.WF.Glo.getFtpUtil();					 
			ftpUtil.downloadFile(filePath, tempFile);

     
            PubClass.DownloadFile(tempFile, fileName);
            //删除临时文件
            new File(tempFile);
        }
        else
        {
        	PubClass.DownloadFile(filePath, fileName);
           
        }
        
		

	}

	
	private String GetRealPath(String fileFullName) throws Exception {
		boolean isFile = false;
		String downpath = "";
		try {
			// 如果相对路径获取不到可能存储的是绝对路径
			File downInfo = new File(
					ContextHolderUtils.getRequest().getSession().getServletContext().getRealPath("~/" + fileFullName));
			isFile = true;
			downpath = ContextHolderUtils.getRequest().getSession().getServletContext()
					.getRealPath("~/" + fileFullName);
		} catch (Exception e) {
			File downInfo = new File(fileFullName);
			isFile = true;
			downpath = fileFullName;
		}
		if (!isFile) {
			throw new Exception("没有找到下载的文件路径！");
		}

		return downpath;
	}

	private void uploadFile(CommonsMultipartFile item, FrmAttachment athDesc, GEEntity en, String msg, MapData mapData,
			String attachPk, String parasData) throws Exception {
		
		
		// 获取文件名
		String fileName = item.getOriginalFilename();
		// 扩展名
		String exts = FileAccess.getExtensionName(fileName).toLowerCase().replace(".", "");

		if (athDesc.getAthSaveWay() == AthSaveWay.WebServer) {

			String savePath = athDesc.getSaveTo();
			if (savePath.contains("@") == true || savePath.contains("*") == true) {
				/* 如果有变量 */
				savePath = savePath.replace("*", "@");
				savePath = BP.WF.Glo.DealExp(savePath, en, null);

				if (savePath.contains("@") && this.getFK_Node() != 0) {
					/* 如果包含 @ */
					BP.WF.Flow flow = new BP.WF.Flow(this.getFK_Flow());
					BP.WF.Data.GERpt myen = flow.getHisGERpt();
					myen.setOID(this.getWorkID());
					myen.RetrieveFromDBSources();
					savePath = BP.WF.Glo.DealExp(savePath, myen, null);
				}
				if (savePath.contains("@") == true)
					throw new Exception("@路径配置错误,变量没有被正确的替换下来." + savePath);
				return;
			} else {
				savePath = athDesc.getSaveTo() + "\\" + getPKVal();
			}

			// 替换关键的字串.
			savePath = savePath.replace("\\\\", "\\");
			try {
				if (savePath.indexOf(":") == -1)
					savePath = ContextHolderUtils.getRequest().getSession().getServletContext().getRealPath(savePath);

				File fileInfo = new File(savePath);

				if (fileInfo.exists() == false)
					fileInfo.mkdirs();

			} catch (Exception ex) {
				throw new RuntimeException("@创建路径出现错误，可能是没有权限或者路径配置有问题:"
						+ ContextHolderUtils.getRequest().getSession().getServletContext().getRealPath("~/" + savePath)
						+ "===" + savePath + "@技术问题:" + ex.getMessage());

			}

			String guid = BP.DA.DBAccess.GenerGUID();
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			String ext = FileAccess.getExtensionName(item.getOriginalFilename());
			String realSaveTo = savePath + "\\" + guid + "." + fileName + "." + ext;

			realSaveTo = realSaveTo.replace("~", "-");
			realSaveTo = realSaveTo.replace("'", "-");
			realSaveTo = realSaveTo.replace("*", "-");

			String saveTo = realSaveTo;

			File file = new File(realSaveTo); // 获取根目录对应的真实物理路径
			try {
				// 构造临时对象

				InputStream is = item.getInputStream();
				int buffer = 1024; // 定义缓冲区的大小
				int length = 0;
				byte[] b = new byte[buffer];
				double percent = 0;
				FileOutputStream fos = new FileOutputStream(file);
				while ((length = is.read(b)) != -1) {
					// percent += length / (double) upFileSize * 100D; //
					// 计算上传文件的百分比
					fos.write(b, 0, length); // 向文件输出流写读取的数据
					// session.setAttribute("progressBar",Math.round(percent));
					// //将上传百分比保存到Session中
				}
				fos.close();
			} catch (RuntimeException ex) {
				
				throw new RuntimeException("@文件存储失败,有可能是路径的表达式出问题,导致是非法的路径名称:" + ex.getMessage());

			}

			// 执行附件上传前事件，added by liuxc,2017-7-15
			msg = mapData.DoEvent(FrmEventList.AthUploadeBefore, en,
					"@FK_FrmAttachment=" + athDesc.getMyPK() + "@FileFullName=" + realSaveTo);
			if (!DataType.IsNullOrEmpty(msg)) {
				BP.Sys.Glo.WriteLineError("@AthUploadeBefore事件返回信息，文件：" + file.getName() + "，" + msg);
				file.delete();
				return;
			}

			// Glo.File_JiaMi(realSaveTo,"D:"+ "//" + guid + "." + ext);
			File info = new File(realSaveTo);

			FrmAttachmentDB dbUpload = new FrmAttachmentDB();
			dbUpload.setMyPK(guid); // athDesc.FK_MapData + oid.ToString();			 
			dbUpload.setFK_FrmAttachment(attachPk);
			dbUpload.setSort(this.getSort());
			dbUpload.setFK_MapData(athDesc.getFK_MapData());
			dbUpload.setFK_FrmAttachment(attachPk);
			dbUpload.setFileExts(exts);
			dbUpload.setFID(this.getFID());
			dbUpload.setNodeID( this.getFK_Node());
			if (athDesc.getIsExpCol() == true) {
				if (parasData != null && parasData.length() > 0) {
					for (String para : parasData.split("@")) {
						if (para.split("=").length == 2)
							dbUpload.SetPara(para.split("=")[0], para.split("=")[1]);
					}
				}
			}

			/// #region 处理文件路径，如果是保存到数据库，就存储pk.
			if (athDesc.getAthSaveWay() == AthSaveWay.WebServer) {
				// 文件方式保存
				dbUpload.setFileFullName(realSaveTo);
			}

			if (athDesc.getAthSaveWay() == AthSaveWay.FTPServer) {
				// 保存到数据库
				dbUpload.setFileFullName(dbUpload.getMyPK());
			}
			/// #endregion 处理文件路径，如果是保存到数据库，就存储pk.

			dbUpload.setFileName(item.getOriginalFilename());
			dbUpload.setFileSize((float) info.length());
			dbUpload.setRDT(DataType.getCurrentDataTimess());
			dbUpload.setRec(BP.Web.WebUser.getNo());
			dbUpload.setRecName(BP.Web.WebUser.getName());
			
			String pkVal=this.getPKVal();			
			if (athDesc.getHisCtrlWay()==  AthCtrlWay.FID)
				pkVal=String.valueOf(this.getFID());
			if (athDesc.getHisCtrlWay()==  AthCtrlWay.PWorkID)
				pkVal=String.valueOf(this.getPWorkID());
			 
			dbUpload.setFID(this.getFID());
			dbUpload.setUploadGUID(guid);
			  
			 //求主键. 如果该表单挂接到流程上.
            if (this.getFK_Node() != 0)
            {
            	
                //判断表单方案。
                FrmNode fn = new FrmNode(this.getFK_Flow(), this.getFK_Node(), athDesc.getFK_MapData());
                if (fn.getFrmSln() == FrmSln.Readonly)
                	return  ;
                
                //    return "err@不允许上传附件.";

                //是默认的方案的时候.
                if (fn.getFrmSln() == FrmSln.Default)
                {
                    //判断当前方案设置的whoIsPk ，让附件集成 whoIsPK 的设置。
                    if (fn.getWhoIsPK() == WhoIsPK.FID)
                        pkVal = String.valueOf( this.getFID());

                    if (fn.getWhoIsPK() == WhoIsPK.PWorkID)
                        pkVal = this.getPWorkID().toString();
                }

                //自定义方案.
                if (fn.getFrmSln() == FrmSln.Self)
                {
                    athDesc = new FrmAttachment(attachPk + "_" + this.getFK_Node());
                    if (athDesc.getHisCtrlWay() == AthCtrlWay.FID)
                        pkVal = String.valueOf( this.getFID());

                    if (athDesc.getHisCtrlWay() == AthCtrlWay.PWorkID)
                        pkVal = String.valueOf( this.getPWorkID());
                }
            }
			
			
			dbUpload.setRefPKVal(pkVal);
			
			dbUpload.Insert();

			if (athDesc.getAthSaveWay() == AthSaveWay.DB) {
				// 执行文件保存.
				BP.DA.DBAccess.SaveFileToDB(realSaveTo, dbUpload.getEnMap().getPhysicsTable(), "MyPK",
						dbUpload.getMyPK(), "FDB");
			}

			// 执行附件上传后事件，added by liuxc,2017-7-15
			msg = mapData.DoEvent(FrmEventList.AthUploadeAfter, en,
					"@FK_FrmAttachment=" + dbUpload.getFK_FrmAttachment() + "@FK_FrmAttachmentDB=" + dbUpload.getMyPK()
							+ "@FileFullName=" + dbUpload.getFileFullName());
			if (!DataType.IsNullOrEmpty(msg))
				BP.Sys.Glo.WriteLineError("@AthUploadeAfter事件返回信息，文件：" + dbUpload.getFileName() + "，" + msg);
		}
		/// #endregion 文件上传的iis服务器上 or db数据库里.

		/// #region 保存到数据库 / FTP服务器上.
		if (athDesc.getAthSaveWay() == AthSaveWay.DB || athDesc.getAthSaveWay() == AthSaveWay.FTPServer) {
			String guid = BP.DA.DBAccess.GenerGUID();

			// 把文件临时保存到一个位置.
			String temp = SystemConfig.getPathOfTemp() + "" + guid + ".tmp";
			File tempFile = new File(temp);
			InputStream is = null;
			try {
				// 构造临时对象
				is = item.getInputStream();
				int buffer = 1024; // 定义缓冲区的大小
				int length = 0;
				byte[] b = new byte[buffer];
				double percent = 0;
				FileOutputStream fos = new FileOutputStream(tempFile);
				while ((length = is.read(b)) != -1) {
					fos.write(b, 0, length); // 向文件输出流写读取的数据
				}
				fos.close();
				is.close();
			} catch (Exception ex) {
				tempFile.delete();
				throw new RuntimeException("@文件存储失败,有可能是路径的表达式出问题,导致是非法的路径名称:" + ex.getMessage());

			}

			// 执行附件上传前事件，added by liuxc,2017-7-15
			msg = mapData.DoEvent(FrmEventList.AthUploadeBefore, en,
					"@FK_FrmAttachment=" + athDesc.getMyPK() + "@FileFullName=" + temp);
			if (DataType.IsNullOrEmpty(msg) == false) {
				BP.Sys.Glo.WriteLineError("@AthUploadeBefore事件返回信息，文件：" + fileName + "，" + msg);

				tempFile.delete();

				throw new Exception("err@上传附件错误：" + msg);
			}

			File info = new File(temp);
			FrmAttachmentDB dbUpload = new FrmAttachmentDB();
			dbUpload.setMyPK(BP.DA.DBAccess.GenerGUID());
			dbUpload.setNodeID( getFK_Node());
			dbUpload.setFK_FrmAttachment(athDesc.getMyPK());
			dbUpload.setSort(this.getSort());
			dbUpload.setFID(this.getFID()); // 流程id.
			if (athDesc.getAthUploadWay() == AthUploadWay.Inherit) {
				/* 如果是继承，就让他保持本地的PK. */
				dbUpload.setRefPKVal(String.valueOf(getPKVal()));
			}

			if (athDesc.getAthUploadWay() == AthUploadWay.Interwork) {
				/* 如果是协同，就让他是PWorkID. */
				String pWorkID = String.valueOf(BP.DA.DBAccess
						.RunSQLReturnValInt("SELECT PWorkID FROM WF_GenerWorkFlow WHERE WorkID=" + getPKVal(), 0));
				if (pWorkID == null || pWorkID == "0")
					pWorkID = getPKVal();
				dbUpload.setRefPKVal(pWorkID);
			}
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			String ext = FileAccess.getExtensionName(item.getOriginalFilename());
			dbUpload.setFK_MapData(athDesc.getFK_MapData());
			dbUpload.setFK_FrmAttachment(athDesc.getMyPK());
			dbUpload.setFileName(item.getOriginalFilename());
			dbUpload.setFileExts(exts);
			dbUpload.setFileSize((float) info.length());
			dbUpload.setRDT(DataType.getCurrentDataTimess());
			dbUpload.setRec(BP.Web.WebUser.getNo());
			dbUpload.setRecName(BP.Web.WebUser.getName());
			if (athDesc.getIsExpCol() == true) {
				if (parasData != null && parasData.length() > 0) {
					for (String para : parasData.split("@")) {
						if (para.split("=").length == 2)
							dbUpload.SetPara(para.split("=")[0], para.split("=")[1]);
					}
				}
			}

			dbUpload.setUploadGUID(guid);

			if (athDesc.getAthSaveWay() == AthSaveWay.DB) {
				dbUpload.Insert();
				// 把文件保存到指定的字段里.
				dbUpload.SaveFileToDB("FileDB", temp);
			}
			

			if (athDesc.getAthSaveWay() == AthSaveWay.FTPServer) {
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
				String ny = sdf.format(new Date());

				String workDir = ny + "\\" + athDesc.getFK_MapData() + "\\";
  
				//特殊处理文件路径.
				if (SystemConfig.getCustomerNo().equals( "BWDA") ) {
					
					sdf = new SimpleDateFormat("yyyy_MM_dd");
					ny = sdf.format(new Date());

					ny = ny.replace("_", "/");
					ny = ny.replace("_", "/");
					
					workDir =  ny+ "/" + WebUser.getNo()+"/";
				}
				
				boolean  isOK=false;

				Glo.File_JiaMi(temp, SystemConfig.getPathOfTemp() + "/" + guid + "_Desc" + ".tmp");
				if (SystemConfig.getFTPServerType().equals("FTP") ) {

					FtpUtil ftpUtil = BP.WF.Glo.getFtpUtil();
					
					ftpUtil.changeWorkingDirectory(workDir,true);

					// 把文件放在FTP服务器上去.
					isOK=ftpUtil.uploadFile( guid + "." + dbUpload.getFileExts(),
							SystemConfig.getPathOfTemp() + "/" + guid + "_Desc" + ".tmp");

					ftpUtil.releaseConnection();
				}

				if (SystemConfig.getFTPServerType().equals("SFTP") ) {

					SftpUtil ftpUtil = BP.WF.Glo.getSftpUtil();
					 
					ftpUtil.changeWorkingDirectory(workDir,true);
					// 把文件放在FTP服务器上去.
					isOK=ftpUtil.uploadFile(guid + "." + dbUpload.getFileExts(),
							SystemConfig.getPathOfTemp() + "/" + guid + "_Desc" + ".tmp");
					ftpUtil.releaseConnection();
				}

				// 删除临时文件
				tempFile.delete();
				new File(SystemConfig.getPathOfTemp() + "" + guid + "_Desc" + ".tmp").delete();

					
				// 设置路径.
				dbUpload.setFileFullName( workDir  + guid + "." + dbUpload.getFileExts());
				
				if (isOK==false)
					throw new com.sun.star.uno.Exception("err文件上传失败，请检查ftp服务器配置信息");
					
				dbUpload.Insert();
				
			}

			// 执行附件上传后事件，added by liuxc,2017-7-15
			msg = mapData.DoEvent(FrmEventList.AthUploadeAfter, en,
					"@FK_FrmAttachment=" + dbUpload.getFK_FrmAttachment() + "@FK_FrmAttachmentDB=" + dbUpload.getMyPK()
							+ "@FileFullName=" + temp);
			
			if (DataType.IsNullOrEmpty(msg)==false)
				BP.Sys.Glo.WriteLineError("@AthUploadeAfter事件返回信息，文件：" + dbUpload.getFileName() + "，" + msg);
		}
		/// #endregion 保存到数据库.
		return;
	}

	private char[] getPWorkID() {
		// TODO Auto-generated method stub
		return null;
	}

}