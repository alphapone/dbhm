package org.alphapone.dbhm;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;

/**
 * dbhm options
 */
public class O {
	private short cellSize = 256;
	private long dbhmSize = 100000000;
	private ConflictResolvingStrategy crs = ConflictResolvingStrategy.replace;
	private int serverPort = 8000;

	private static O instance = new O();
	
	private O()
	{
		try {
			String configFile = System.getProperty("org.alphapone.dbhm.O.configure");
			if (configFile!=null) {
				Properties p = new Properties();
				p.load(new FileInputStream(new File(configFile)));
				String s = p.getProperty("cellSize");
				if (s!=null) {
					cellSize = Short.parseShort(s);
				}
				s = p.getProperty("dbhmSize");
				if (s!=null) {
					dbhmSize = Long.parseLong(s);
				}
				s = p.getProperty("serverPort");
				if (s!=null) {
					serverPort = Integer.parseInt(s);
				}
				s = p.getProperty("crs");
				if (s!=null) {
					if ("keep".equals(s)) {
						crs = ConflictResolvingStrategy.keep;
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public enum ConflictResolvingStrategy {
		keep,
		replace
	}
	public static int getCellSize() {
		return instance.cellSize;
	}
	public static long getDbhmSize() {
		return instance.dbhmSize;
	}
	public static ConflictResolvingStrategy getConflictResolvingStrategy() {
		return instance.crs;
	}
	public static int getServerPort() {
		return instance.serverPort;
	}
}
