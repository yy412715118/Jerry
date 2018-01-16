/**
 * 
 */
function sortByKey(list)
{
	list.sort(function(a,b){
        return a.key.toString().localeCompare(b.key.toString())});
}
function sortByKeyAndJoinWithKey(data,prefix)
{
	var list=new Array();
	for(var i in data)
		list.push({key:i,value:data[i]});
	sortByKey(list);
	var str=null;
	for(var i=0;i<list.length;i++)
	{
	  if(str==null)
		  str=(prefix==null?"":prefix)+list[i].key+"="+list[i].value;
	  else
		  str+="&"+(prefix==null?"":prefix)+list[i].key+"="+list[i].value;
	}
	return str;
}
function sortByKeyAndSignWithKey(data,append)
{
	var str=sortByKeyAndJoinWithKey(data);
	if(append!=null)
		str+=append;
	return hex_md5(str);
}