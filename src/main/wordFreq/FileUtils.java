import java.io.File;

public class FileUtils {

	public static File[] getAllFiles(File curDir) {
		File[] filesList = curDir.listFiles();
		assert filesList != null;
		File[] result = new File[filesList.length];
		for(int i = 0; i < filesList.length; i++) {
			if((filesList[i].getName().contains(".docx") || filesList[i].getName().contains(".doc") || filesList[i].getName().contains(".txt")) && filesList[i].isFile()) {
				result[i]=filesList[i];
			}
		}
		return result;
	}
}

