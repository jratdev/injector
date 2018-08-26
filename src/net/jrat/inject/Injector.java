package net.jrat.inject;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public abstract class Injector
{
	private final ClassPool cpool = ClassPool.getDefault();
	private final CtClass clazz;
	
	private ClassPath cpath;
	
	public Injector(String input, String cpath) throws Exception
	{
		this.cpath = this.cpool.insertClassPath(input);
		this.clazz = this.cpool.get(cpath);
		
		this.setup();
	}
	
	public abstract void setup();
	
	public void insertBefore(String method, String src) throws Exception
	{
		final CtMethod cmethod = this.clazz.getDeclaredMethod(method);
		cmethod.insertBefore(src);
	}
	
	public void insertAt(String method, int linenum, String src) throws Exception
	{
		final CtMethod cmethod = this.clazz.getDeclaredMethod(method);
		cmethod.insertAt(linenum, src);
	}
	
	public void insertAfter(String method, String src) throws Exception
	{
		final CtMethod cmethod = this.clazz.getDeclaredMethod(method);
		cmethod.insertAfter(src);
	}
	
	public void addMethod(String src) throws Exception
	{
		final CtMethod cmethod = CtMethod.make(src, this.clazz);
		this.clazz.addMethod(cmethod);
	}
	
	public void addField(String src) throws Exception
	{
		final CtField cfield = CtField.make(src, this.clazz);
		this.clazz.addField(cfield);
	}
	
	public byte[] getBytecode() throws Exception
	{
		return this.clazz.toBytecode();
	}
	
	public void close()
	{
		this.cpool.removeClassPath(this.cpath);
	}
}