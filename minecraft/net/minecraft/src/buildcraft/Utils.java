package net.minecraft.src.buildcraft;

import java.io.File;

import net.minecraft.client.Minecraft;

public class Utils {
	static final int field_1193_a[]; /* synthetic field */

	enum EnumOS1
	{
	    linux,
	    solaris,
	    windows,
	    macos,
	    unknown;
	}
	
    static 
    {
        field_1193_a = new int[EnumOS1.values().length];
        try
        {
            field_1193_a[EnumOS1.linux.ordinal()] = 1;
        }
        catch(NoSuchFieldError nosuchfielderror) { }
        try
        {
            field_1193_a[EnumOS1.solaris.ordinal()] = 2;
        }
        catch(NoSuchFieldError nosuchfielderror1) { }
        try
        {
            field_1193_a[EnumOS1.windows.ordinal()] = 3;
        }
        catch(NoSuchFieldError nosuchfielderror2) { }
        try
        {
            field_1193_a[EnumOS1.macos.ordinal()] = 4;
        }
        catch(NoSuchFieldError nosuchfielderror3) { }
    }
    
    private static EnumOS1 getOs()
    {
        String s = System.getProperty("os.name").toLowerCase();
        if(s.contains("win"))
        {
            return EnumOS1.windows;
        }
        if(s.contains("mac"))
        {
            return EnumOS1.macos;
        }
        if(s.contains("solaris"))
        {
            return EnumOS1.solaris;
        }
        if(s.contains("sunos"))
        {
            return EnumOS1.solaris;
        }
        if(s.contains("linux"))
        {
            return EnumOS1.linux;
        }
        if(s.contains("unix"))
        {
            return EnumOS1.linux;
        } else
        {
            return EnumOS1.unknown;
        }
    }
    
    public static File getBuildCraftSave (Minecraft minecraft) {
    	return new File (getSaveFolder(minecraft), "buildcraft.dat");
    }
	
	public static File getSaveFolder(Minecraft minecraft) {
		return new File(new File(getAppDir("minecraft"), "save"),
				minecraft.theWorld.func_22144_v().getWorldName());
	}
    
	public static File getAppDir(String s)
    {
        String s1 = System.getProperty("user.home", ".");
        File file;
        switch(field_1193_a[getOs().ordinal()])
        {
        case 1: // '\001'
        case 2: // '\002'
            file = new File(s1, (new StringBuilder()).append('.').append(s).append('/').toString());
            break;

        case 3: // '\003'
            String s2 = System.getenv("APPDATA");
            if(s2 != null)
            {
                file = new File(s2, (new StringBuilder()).append(".").append(s).append('/').toString());
            } else
            {
                file = new File(s1, (new StringBuilder()).append('.').append(s).append('/').toString());
            }
            break;

        case 4: // '\004'
            file = new File(s1, (new StringBuilder()).append("Library/Application Support/").append(s).toString());
            break;

        default:
            file = new File(s1, (new StringBuilder()).append(s).append('/').toString());
            break;
        }
        if(!file.exists() && !file.mkdirs())
        {
            throw new RuntimeException((new StringBuilder()).append("The working directory could not be created: ").append(file).toString());
        } else
        {
            return file;
        }
    }
}
