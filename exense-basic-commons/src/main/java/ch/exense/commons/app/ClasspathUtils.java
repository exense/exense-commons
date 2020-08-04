package ch.exense.commons.app;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class ClasspathUtils {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Set<Class> getAllSubTypesOf(Class clazz, String prefix) {
		Reflections reflections = getReflections(clazz, prefix);
		Set<Class> fullTree = new HashSet<Class>();
		
		Set<Class> thisNode = reflections.getSubTypesOf(clazz); 
		fullTree.addAll(thisNode);
		
		for(Class<? extends T> subType : thisNode) {
			fullTree.addAll(getAllSubTypesOf(subType, prefix));
		}
		return fullTree;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static <T> Set<Class> getAllConcreteSubTypesOf(Class clazz, String prefix) {
		return getAllSubTypesOf(clazz, prefix).stream().filter(c -> !Modifier.isAbstract( c.getModifiers())).collect(Collectors.toSet());
	}
	
	private static <T> Reflections getReflections(Class<T> clazz, String prefix) {
		return new Reflections(new ConfigurationBuilder()
			      .filterInputsBy(new FilterBuilder().includePackage(prefix))
			      .setUrls(ClasspathHelper.forPackage(prefix))
			      .setScanners(new SubTypesScanner()));
	}
	
	public static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz, String prefix) {
		Reflections reflections = getReflections(clazz, prefix);
		return reflections.getSubTypesOf(clazz);
	}
	
	public static <T> Collection<Class<? extends Object>> getChildrenClassListFromClasspath(Class<T> clazz) {
		ServiceLoader<T> loader = ServiceLoader.load(clazz);
		
		List<Class<? extends Object>> classList = new ArrayList<>();
		
		for (T serv : loader) {
			classList.add(serv.getClass());
		}
		return classList;
	}
}
