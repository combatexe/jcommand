package org.jcommand.prevayler.classloader;

import java.util.ArrayList;
import java.util.List;

public class PrevaylerOSGiClassLoader extends ClassLoader {

	private List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();

	public void addClassLoader(ClassLoader classLoader) {
		classLoaders.add(classLoader);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		for (ClassLoader classLoader : classLoaders) {
			try {
				Class<?> clazz = classLoader.loadClass(name);
				if (clazz != null) {
					return clazz;
				}
			} catch (ClassNotFoundException cnfe) {
				//check next classloader 
			}
		}
		throw new ClassNotFoundException("Prevayler ClassLoader can't find class:" + name);
	}

}
