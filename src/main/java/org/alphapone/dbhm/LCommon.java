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

import java.util.Arrays;

/**
 * Object persistent store on byte buffer interface
 * It stores serialized objects in fixed size byte buffer area
 * (c) inl@yandex.com
 */
public class LCommon {
	
	/**
	 * Byte buffer for object store 
	 */
	ByteBuffer cachemem = null;
	O.ConflictResolvingStrategy crs = null;
	int cellSize = 256;
	
	LCommon(ByteBuffer cachemem, O.ConflictResolvingStrategy crs, int cellSize) {
		this.cachemem = cachemem;
		this.crs  = crs;
		this.cellSize = cellSize;
		assert(cellSize > 0);
		for (int i=0;i<cachemem.capacity();i+=cellSize) {
			cachemem.put(i,(byte)0);
		}
	}
		
	/**
	 * Cells number in fixed size object store 
	 */
	int getCapacity(){
		return cachemem.capacity()/cellSize;
	}
	
	/**
	 * Serialzie Object to byte array
	 */
	byte[] o2b (Object o)
	    throws IOException
	{
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(o);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;		
    }
    
    /**
     * Load object from byte array
     */
    Object b2o(byte[] a)
    	throws IOException, ClassNotFoundException
    {
        Object o = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(a);
            ois = new ObjectInputStream(bis);
            o = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return o;    	
    }
    
    /**
     * Custom hash function #0
     */
    long bhash(byte[] a, int from, int length) {
    	long hash = 5381;
		for (int i=from,l=from+length;i<l;i++) {
			hash = ((hash << 5) + hash) ^ a[i];
		}
		if (hash < 0) {
			return -hash;
		}
		return hash;
	}
	
	/**
	 * Custom hash function #1
	 */
    long b1hash(byte[] a, int from, int length) {
    	long hash = 0;
    	for (int i=from,l=from+length;i<l;i++) {
    		hash = (hash * 1664525) + a[i] + 1013904223;
        }
        if (hash < 0) {
        	return -hash;
        }
        return hash;
    }        
    
    /**
     * Calculate custom hash from object 
     */
	long obhash(Object o)
		throws IOException
	{
		byte[] a = o2b(o);
		return bhash(a,0,a.length);
	}
	
	/**
	 * Get start index of cell data area
	 */
	int getOffset(int cellId) {
		return cellId*cellSize;
	}
	
	/**
	 * Checks cell belongs to object with key a or empty
	 */
	int checkKeyOrFree(int id1, byte []a) {
		int ido=getOffset(id1);
		byte cflg = cachemem.get(ido); 
		if (cflg==0) {
			return id1;
		}
		if (cflg==1) {
			int szkey = getKeySize(id1);
			if (szkey == a.length) {
				boolean eq = true;
				for (int i=0; i<a.length; i++) {
					if (a[i]!=cachemem.get(ido+3+i)) {
						eq = false;
						break;
					}
				}
				if (eq) {
					return id1;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Returns cell id for specified object key
	 */
	int getCellId(Object key)
		throws IOException
	{
		return getCellId(o2b(key));
	}
	
	/**
	 * Return cell id for serialzied object key
	 */
	int getCellId(byte []a) {
		
		long hash = Arrays.hashCode(a);
		if (hash < 0) {
			hash = -hash;
		}
		int id1 = (int)(hash%(long)getCapacity());
		int ido = getOffset(id1);
		int id = checkKeyOrFree(id1,a);
		if (id>=0) {
			return id;
		}
		
		hash = bhash(a,0,a.length);
		int id2 = (int)(hash%(long)getCapacity());
		id = checkKeyOrFree(id2,a);
		if (id>=0) {
			return id;			
		}
		
		hash = b1hash(a,0,a.length);
		int id3 = (int)(hash%(long)getCapacity());
		id = checkKeyOrFree(id3,a);
		if (id>=0) {
			return id;
		}
		
		switch (crs) {
		case replace:
			cachemem.put(ido,(byte)0);
			return id1;
		case keep:
			return -1;
		}
		return -1;
	}
	
	/**
	 * Return size of key stored in cell
	 */
	int getKeySize(int id) {
		int ido = getOffset(id);
		byte []ksz = new byte[2];
		ksz[0] = cachemem.get(ido+1);
		ksz[1] = cachemem.get(ido+2);
		ByteBuffer bu = ByteBuffer.wrap(ksz);
		return bu.getShort();
	}
	
	/**
	 * Return size of value stored in cell
	 */
	int getValSize(int id) {
		int keysize = getKeySize(id);
		int ido = getOffset(id);
		byte []vsz = new byte[2];
		vsz[0] = cachemem.get(ido+3+keysize);
		vsz[1] = cachemem.get(ido+4+keysize);
		ByteBuffer bu = ByteBuffer.wrap(vsz);
		return bu.getShort();
	}
	
	/**
	 * Put object in the store
	 */
	public void putObject(Object key, Object payload) 
		throws IOException
	{
    	byte []aokey = o2b(key);			
		byte []aopayload = o2b(payload);
		synchronized(this) {
			int id = getCellId(aokey);
			if (id>=0) {
				int kl = aokey.length;
				int pl = aopayload.length;
				int totallength = kl + pl + 5;
				if (totallength <= cellSize) {
					int ido = getOffset(id);
					cachemem.put(ido,(byte)1);
					cachemem.put(ido+1,(byte) ((kl>>8)&0xff));
					cachemem.put(ido+2,(byte) (kl&0xff));
					cachemem.put(ido+3+kl,(byte) (pl>>8));
					cachemem.put(ido+4+kl,(byte) (pl&0xff));
					for (int i=0; i< kl; i++) {
						cachemem.put(ido+3+i, aokey[i]);
					}
					for (int i=0; i< pl; i++) {
						cachemem.put(ido+5+kl+i, aopayload[i]);
					}
				}
			}
		}
	}
	
	/**
	 * Remove object associated with the key
	 */
	public void removeKey(Object key) 
		throws IOException
	{
    	byte []aokey = o2b(key);			
		synchronized(this) {
			int id = getCellId(aokey);
			if (id>=0) {
				cachemem.put(getOffset(id),(byte)0);
			}
		}
	}
	
	/**
	 * Retrieve object by the key, null if object is not stored
	 */
	public Object getObject(Object key)
		throws IOException, ClassNotFoundException
	{
		Object o = null;
    	byte []aokey = o2b(key);	
		byte []a = null;
		synchronized(this) {
			int id = getCellId(aokey);
			if (id>=0) {
				int ido = getOffset(id);
				if (cachemem.get(ido)==1) {
					int ksz = getKeySize(id);
					int psz = getValSize(id);
					a = new byte[psz];
					for (int i=0; i<psz; i++) {
						a[i] = cachemem.get(ido+5+ksz+i);
					}
					
				}
			}
		}
		if (a!=null) {
			o = b2o(a);
		}
		return o;
	}
	
}
