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
	private static String ver = "20180812";
	private static Dbhm instance = null;
	
	public static Dbhm getInstance()
		throws FileNotFoundException, IOException
	{
		synchronized(ver) {
			if (instance==null) {
				instance = new Dbhm();
			}
		}
		return instance;
	}
	
	LCommon cache;
	
	protected Dbhm()
		throws FileNotFoundException, IOException
	{
		RandomAccessFile mmf = new RandomAccessFile("dbhm.dat","rw");
		ByteBuffer cachemem = mmf.getChannel().map(FileChannel.MapMode.READ_WRITE,0,O.getDbhmSize());
		cache = new LCommon(
			cachemem,
			O.getConflictResolvingStrategy(),
			O.getCellSize()
			);
	}
	
	int getCapacity(){
		return cache.getCapacity();
	}
	
	public void putObject(Object key, Object payload) 
		throws IOException
	{
		cache.putObject(key,payload);
	}
	
	public void removeKey(Object key) 
		throws IOException
	{
		cache.removeKey(key);
	}
	
	public Object getObject(Object key)
		throws IOException, ClassNotFoundException
	{
		return cache.getObject(key);
	}
	
}
