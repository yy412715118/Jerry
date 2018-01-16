package com.hogense.game.server.handler;

import java.io.IOException;
import java.security.IdentityScope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.hogense.game.server.annotation.HttpService;
import com.hogense.game.server.sevice.RoomServerManager;
import com.hogense.game.server.sevice.ServerFilter;
import com.hogense.server.define.ServerInfo;
@HttpService("/admin/server")
public class ServerManagerServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    static Gson gson=new Gson();
	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		Map<String,Object> data=new HashMap<>();
		String op=arg0.getParameter("op");
		if("list".equalsIgnoreCase(op))
		{
			List<ServerInfo>serverInfos=RoomServerManager.getManager().getServerInfos();
			Set<Integer>whiteList=ServerFilter.getWhiteList();
			List<Map<String, Object>>list=new ArrayList<>();
			for(ServerInfo info:serverInfos)
			{
				Map<String, Object>item=new HashMap<>();
				item.put("server", info);
				item.put("allow", whiteList.contains(info.serverid));
				list.add(item);
			}
			data.put("list", list);
			data.put("status", 1);
		}
		else if("add".equalsIgnoreCase(op))
		{
			String[] id=arg0.getParameterValues("id");
			Integer ids[]=toIntegerArray(id);
			if(ids!=null)
				ServerFilter.add(ids);
			data.put("status", 1);
		}
		else if("del".equalsIgnoreCase(op))
		{
			String[] id=arg0.getParameterValues("id");
			Integer ids[]=toIntegerArray(id);
			if(ids!=null)
				ServerFilter.remove(ids);
			data.put("status", 1);
		}
		else 
		{
			data.put("status", 2);
		}
		try {
			arg1.setContentType("application/json");
			arg1.setCharacterEncoding("utf-8");
			arg1.getWriter().print(gson.toJson(data));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public Integer[] toIntegerArray(String[] str)
	{
		Integer[] rs=null;
		try {
			if(str!=null)
			{
				rs=new Integer[str.length];
				for(int i=0;i<rs.length;i++)
					rs[i]=Integer.parseInt(str[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			rs=null;
		}
		return rs;
	}
}
