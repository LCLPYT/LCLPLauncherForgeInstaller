package work.lclpnet.forgeinstaller;

import net.minecraftforge.installer.SimpleInstaller;
import net.minecraftforge.installer.actions.ActionCanceledException;
import net.minecraftforge.installer.actions.ClientInstall;
import net.minecraftforge.installer.actions.ProgressCallback;
import net.minecraftforge.installer.json.Util;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Locale;

public class ForgeInstaller {

    public static void main(String[] args) {
		File tempDir = new File(System.getProperty("java.io.tmpdir"), "lclplauncher_forge");
		if(!tempDir.exists() && !tempDir.mkdirs()) throw new IllegalStateException("Could not create temp directory.");
		File logFile = new File(tempDir, String.format("llforgei_%s_stdout.txt", System.currentTimeMillis() / 1000L));
		File errFile = new File(tempDir, String.format("llforgei_%s_stderr.txt", System.currentTimeMillis() / 1000L));
		
		System.out.println("Writing logs to: " + tempDir.getAbsolutePath());

		FileOutputStream outOut, outErr;
		try {
			outOut = new FileOutputStream(logFile);
			outErr = new FileOutputStream(errFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
			return;
		}
        
		PrintStream origOut = System.out, origErr = System.err;
        LoggingPrintStream interceptor = new LoggingPrintStream(origOut, outOut);
		System.setOut(interceptor);
        System.setErr(new LoggingPrintStream(origErr, outErr));

        ClientInstall install = new ClientInstall(Util.loadInstallProfile(), ProgressCallback.withOutputs(interceptor));

        File installer = null;
        try {
            installer = new File(SimpleInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        if (installer.getAbsolutePath().contains("!/")) {
            System.err.println("Due to java limitation, please do not run this jar in a folder ending with !");
            System.exit(1);
            return;
        }

        try {
            install.run(getMCDir(), input -> true, installer);
        } catch (ActionCanceledException e) {
            e.printStackTrace();
            System.exit(1);
        }

		try {
			outOut.close();
			outErr.close();
			System.setOut(origOut);
			System.setErr(origErr);
		} catch (IOException e) {
			e.printStackTrace(origErr);
		}
        
        System.exit(0);
    }

    public static File getMCDir() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null)
            return new File(System.getenv("APPDATA"), mcDir);
        else if (osType.contains("mac"))
            return new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
        return new File(userHomeDir, mcDir);
    }

}
