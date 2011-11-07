
import java.awt.List;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;



import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

class wt implements Watcher{

	@Override
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		EventType a = event.getType();
		String path = event.getPath();
		if(a.equals(EventType.NodeCreated)){
			System.out.println("node is created, the path is " + path);
			
		}
	
	}//process
	
}//watcher

public class counter extends Thread{
	
	

	int id;
	
	public counter( int i ){
		id = i;
			
	}

	public void run(){
		
		String addr = "sslab02.cs.purdue.edu:2181";
		int timeout = 2000;
		String remotePath = "/testFromEclipse2/value";

		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(addr, timeout, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(int i = 0 ; i < 1000; i++){
			System.out.println("instance " + id + " got id : "+ getSeqID(zk, remotePath));
			
		}//for
		
		
	}
	
	public static boolean mutexLock(ZooKeeper zk, String remotePath) throws InterruptedException{
		
		
		while(true){
			Stat s1;
			try{
				s1 = zk.exists(remotePath+"/mutex", null);
			
				String s2 = null;
				if(s1 == null){
					s2 = zk.create(remotePath+"/mutex", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
					
					if(s2==null) continue;
					System.out.println("got mutex");
					return true;	
				}
				else{
					
					Thread.sleep(100);
				}
			
			
			}catch (KeeperException e){
				continue;
			}
			
		}
	}
	public static boolean mutexUnlock(ZooKeeper zk, String remotePath) throws InterruptedException, KeeperException{
		zk.delete(remotePath+"/mutex", -1);
		
		return true;
	}
	
	public static  int getSeqID(ZooKeeper zk, String remotePath){
		
		
		int retval = 0;
		
		
		try {	
			Stat s1= zk.exists(remotePath, null);

			if(s1!=null){
			
				byte []fromServer = null;
				//retrieve the date from server
				mutexLock(zk, remotePath);
				
				fromServer = zk.getData(remotePath, null, null);
					
				String counterValue = new String(fromServer);
				System.out.print("hearing back from server: " + counterValue + "\t");
				retval = Integer.parseInt(counterValue);
				int toSet = retval+1;
				
				
				//setID
				String setD = ""+toSet;
				byte []setByte = setD.getBytes();
				zk.setData(remotePath, setByte, -1);
				System.out.println("setting the data to " + setD);
				
					
				mutexUnlock(zk, remotePath);
				//check if the data is set
				//fromServer = zk.getData(remotePath, new wt(), null);
				//counterValue = new String(fromServer);
				//System.out.println("hearing back from server: " + counterValue);
				
				}
						
			else{
				String value = "0";
				byte[] data = value.getBytes();
				String remotePathSet = null;
				mutexLock(zk, remotePath);
				 remotePathSet = zk.create(remotePath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				mutexUnlock(zk, remotePath);
				System.out.println("znode is built: " + remotePathSet );
			    
				}
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
		 return retval;
		
	}


	public static void main(String [] a) throws InterruptedException{
	
		
		Thread[] threads = new Thread[10];

		for (int i = 0; i < threads.length; i++) {
		    threads[i] = new counter(i);
		    threads[i].start();
		   
		    
		     }//for
				
		//350
		}//main
		
	
	
	
}
