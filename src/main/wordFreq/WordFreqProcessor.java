import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WordFreqProcessor implements DataProcessor {

	private final ProcessorInput processorInput;
	private final String FILE_SPLITTER ="[\\p{Punct}\\s]+";

	public WordFreqProcessor(ProcessorInput processorInput) {
		this.processorInput = processorInput;
	}

	@Override public Boolean process() throws Exception {

		Predicate<String> isWord = (word) -> {
			if(word.isEmpty())
				return false;
			for (char c : word.toCharArray()) {
				if (!Character.isLetter(c))
					return false;
			}
			return true;
		};
		Predicate<String> satisfiesConstraints = (word) -> {
			boolean exclSmallConstraint = true, notExclWordConstraint = true;
			if (processorInput.isExcludeSmall()) {
				exclSmallConstraint = word.length() > 3;
			}
			if (processorInput.getExcluded() != null && processorInput.getExcluded().length > 0) {
				notExclWordConstraint = Arrays.stream(processorInput.getExcluded()).filter(isWord).noneMatch(x -> x.equals(word));
			}
			return exclSmallConstraint && notExclWordConstraint;
		};
		Function<List<String>, Map<String, Long>> processor = (words) -> words.stream()
				.flatMap(line -> Arrays.stream(line.trim().split(FILE_SPLITTER)))
				.map(String::toLowerCase)
				.filter(isWord)
				.filter(satisfiesConstraints)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		AtomicBoolean isIOException = new AtomicBoolean(false);
		final Map<String, Long> map = new HashMap<>();
		if(processorInput.getFiles().length > 0) {
			Arrays.stream(processorInput.getFiles()).map(
					file -> {
						List<String> words = new ArrayList<>();
						if (file != null) {
							if (file.getName().contains(".doc") || file.getName().contains(".docx")) {
								try {
									POITextExtractor poiTextExtractor = ExtractorFactory.createExtractor(new FileInputStream(file));
									words = Arrays.asList(poiTextExtractor.getText().split(FILE_SPLITTER));
								} catch (IOException | OpenXML4JException | XmlException e) {
									e.printStackTrace();
									isIOException.set(true);
									return Collections.emptyMap();
								}
							} else {
								try {
									words = Files.readAllLines(file.toPath());
								} catch (IOException e) {
									e.printStackTrace();
									isIOException.set(true);
									return Collections.emptyMap();
								}
							}
						} else{
							return Collections.emptyMap();
						}
						return processor.apply(words);
					}
			).forEach(entry -> entry.forEach((key, value) -> {
				if (map.get((String) key) != null) {
					Long count = map.get(key);
					map.put((String) key, count + (Long) value);
				} else {
					map.put((String) key, (Long) value);
				}
			}));
			List<Entry> entries = new ArrayList<>();
			map.forEach((word, freq) -> entries.add(new Entry(word, freq.intValue())));
			if (isIOException.get()) {
				throw new Exception("Could not parse files");
			}
			ProcessorState.getState().setProcessResult(entries);
			ProcessorState.getState().setProcessorInput(processorInput);
			return true;
		}
		ProcessorState.getState().setProcessResult(new ArrayList<>());
		return false;
	}

	public static class ProcessorState {

		private List<Entry> processResult;
		private ProcessorInput processorInput;
		private static ProcessorState processorState;

		private ProcessorState() {
			processorState = this;
			processResult = new ArrayList<>();
		}

		public static ProcessorState getState() {
			if (processorState == null) {
				processorState = new ProcessorState();
			}
			return processorState;
		}

		public List<Entry> getProcessResult() {
			return processResult;
		}

		public void setProcessResult(List<Entry> processResult) {
			this.processResult = processResult;
		}

		public ProcessorInput getProcessorInput() {
			return processorInput;
		}

		public void setProcessorInput(ProcessorInput processorInput) {
			this.processorInput = processorInput;
		}
	}
}
