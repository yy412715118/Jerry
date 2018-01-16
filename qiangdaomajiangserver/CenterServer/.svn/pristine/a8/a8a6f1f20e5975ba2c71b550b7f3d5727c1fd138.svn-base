package com.hogense.game.server.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hogense.game.server.Config;
import com.hogense.game.server.annotation.HttpService;
@HttpService("/api/record/upload")
public class RecordServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String filename=req.getHeader("filename");
		String savepath=Config.getProperty("recordpath");
		boolean flag=false;
		if(savepath!=null)
		{
			if(!savepath.endsWith("/"))
				savepath+="/";
			File file=new File(savepath+filename);
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			FileOutputStream fout=new FileOutputStream(file);
			InputStream in=req.getInputStream();
			try {
				byte[] data=new byte[1024];
				int len;
				while((len=in.read(data))!=-1)
					fout.write(data, 0, len);
				fout.close();
				fout=null;
				flag=true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if(fout!=null)
						fout.close();
					if(in!=null)
						in.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
				
			}
		}
		resp.getWriter().print(flag?1:0);
	}

}
