import java.io.File;

public class ProcessorInput {

	private final File[] files;
	private final String[] excluded;
	private final boolean excludeSmall;
	private final int resultsNo;

	public ProcessorInput(File[] files, String[] excluded, boolean excludeSmall, int resultsNo) {
		this.files = files;
		this.excluded = excluded;
		this.excludeSmall = excludeSmall;
		this.resultsNo = resultsNo;
	}

	public File[] getFiles() {
		return files;
	}

	public String[] getExcluded() {
		return excluded;
	}

	public boolean isExcludeSmall() {
		return excludeSmall;
	}

	public int getResultsNo() {
		return resultsNo;
	}
}
