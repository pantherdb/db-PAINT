package org.paint.util;


public class EnvironmentUtil {

	public static boolean isWindows() {
		String osname = System.getProperty("os.name");
		if (osname.startsWith("Windows"))
			return true;
		else
			return false;
	}

	public static boolean isMac() {
		String osname = System.getProperty("os.name");
		if (osname.startsWith("Mac"))
			return true;
		else
			return false;
	}

	public static boolean isUnix() {
		String osname = System.getProperty("os.name");
		// Any other cases??  What about Alphas?
		if (osname.startsWith("Linux") || osname.startsWith("Solaris") || osname.startsWith("Sun") || osname.startsWith("Mac OS X") ||
				osname.indexOf("ix") > 0)
			return true;
		else
			return false;
	}

}
