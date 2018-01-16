app=PDMJDBServer01.jar
status()
{
  PID=$(cat pid)
  ps -ef|grep java| grep $app|grep $PID
  if [ $? -eq 0 ]
     then
       return 1
     else
      return 0
  fi
}
start(){    
  
  status
  if [ $? -eq 0 ]
    then
      nohup  java -Xbootclasspath/a:config:  -jar $app>log.log &
       echo $!>pid
       tail -f log.log
    else
       echo "server has started."
  fi
}    
#停止方法    
stop(){    
   mpid=$(cat pid)
   ps -ef|grep java|grep $app|grep $mpid |awk '{print $2}'|while read pid    
   do    
     echo "kill process $pid"	   
     kill -9 $pid    
    done
}    
stopall()
{
   ps -ef|grep java|grep $app|awk '{print $2}'|while read pid
   do
     echo "kill process $pid"
     kill -9 $pid
    done
}  
showstatus()
{
  status
  if [ $? -eq 0 ]
  then
    echo "server is not running."
  else
    echo "server is running."
  fi
}
case "$1" in    
start)    
start    
;;    
stop)    
stop    
;;      
restart)    
stop    
start    
;;
stopall)
stopall
;;
status)
showstatus
;;    
*)    
printf 'Usage: %s {start|stop|restart|status|stopall}\n' "$prog"    
exit 1    
;;    
esac  
