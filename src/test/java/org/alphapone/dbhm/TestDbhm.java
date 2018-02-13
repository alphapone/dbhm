package org.alphapone.dbhm;

import org.junit.Test;


public class TestDbhm {
	
	@Test
	public void testGet()
		throws Exception
	{
		Dbhm dbhm = Dbhm.getInstance();
		String key = "keya";
		String value = "vala";
		dbhm.putObject(key,value);
		Object g = dbhm.getObject(key);
		assert(value.equals(g));
	}
	
	@Test
	public void test10000()
		throws Exception
	{
		Dbhm dbhm = Dbhm.getInstance();
		long start10000 = System.currentTimeMillis();
		for (int i = 0; i<10000; i++) {
			dbhm.putObject(new Integer(i), new Integer(i*i));
		}
	    long end10000 = System.currentTimeMillis();
	    System.out.println("L12:10000pts: " + (end10000-start10000));
	    System.out.println("L12 speed: " + 10000000/(end10000-start10000) + " inserts per second");
		start10000 = System.currentTimeMillis();
		for (int i = 0; i<10000; i++) {
			dbhm.putObject("" + i, "" + (i*i));
	    }
	    end10000 = System.currentTimeMillis();
	    System.out.println("L12:10000 string pts: " + (end10000-start10000));
	    System.out.println("L12 string speed: " + 10000000/(end10000-start10000) + " string inserts per second");
	}

}