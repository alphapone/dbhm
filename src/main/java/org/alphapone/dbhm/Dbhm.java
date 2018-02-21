package org.alphapone.dbhm;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Dbhm {
	private static Dbhm instance = null;
	
	public static Dbhm getInstance()
		throws FileNotFoundException, IOException
	{
		if (instance!=null) {
			return instance;
		}
		synchronized(Dbhm.class) {
			if (instance==null) {
				instance = new Dbhm();
			}
		}
		return instance;
	}
	
	LCommon []cache; 
	
	protected Dbhm()
		throws FileNotFoundException, IOException
	{
		RandomAccessFile mmf = new RandomAccessFile("dbhm.dat","rw"); // TODO: move to options
		ByteBuffer cachemem = mmf.getChannel().map(FileChannel.MapMode.READ_WRITE,0,O.getDbhmSize());
		cache = new LCommon[1]; // TODO: add set of files here instead of one
		cache[0] = new LCommon(
			cachemem,
			O.getConflictResolvingStrategy(),
			O.getCellSize()
			);
	}
	
	protected LCommon getCache(Object key) { // TODO: add sharding here by object key
		return cache[0];
	}
	
	long getCapacity(){
		long capacity = 0;
		for (LCommon c:cache) {
			capacity+=c.getCapacity();
		}
		return capacity;
	}
	
	public void putObject(Object key, Object payload) 
		throws IOException
	{
		getCache(key).putObject(key,payload);
	}
	
	public void removeKey(Object key) 
		throws IOException
	{
		getCache(key).removeKey(key);
	}
	
	public Object getObject(Object key)
		throws IOException, ClassNotFoundException
	{
		return getCache(key).getObject(key);
	}
	
}
