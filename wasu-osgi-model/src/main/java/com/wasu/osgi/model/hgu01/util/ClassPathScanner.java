package com.wasu.osgi.model.hgu01.util;

import lombok.Getter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author glmx_
 */
@Getter
public class ClassPathScanner {

    private final Set<Class<?>> classes = new HashSet<>();

    public void scanPackage(String packageName) throws BundleException, ClassNotFoundException {
        BundleContext context = FrameworkUtil.getBundle(ClassPathScanner.class).getBundleContext();
        String packagePath = packageName.replace('.', '/');
        Bundle[] bundles = context.getBundles();

        if (bundles != null) {
            for (Bundle bundle : bundles) {
                findClassesInDirectory(bundle, packagePath);
            }
        }
    }

    private void findClassesInDirectory(Bundle bundle, String directory) throws BundleException, ClassNotFoundException {
        Enumeration<URL> entries = bundle.findEntries(directory, "*", false);
        while (entries != null && entries.hasMoreElements()) {
            URL entry = entries.nextElement();
            String classFullName = entry.getPath().substring(1).replace('/', '.');
            String className = classFullName.split(".class")[0];
            ClassLoader classLoaderFromBundle = getClassLoaderFromBundle(bundle);
            Class<?> clazz = classLoaderFromBundle.loadClass(className);
            if (clazz != null) {
                classes.add(clazz);
            }
        }
    }

    private ClassLoader getClassLoaderFromBundle(Bundle bundle) throws ClassNotFoundException {
        return bundle.loadClass(this.getClass().getName()).getClassLoader();
    }
}
