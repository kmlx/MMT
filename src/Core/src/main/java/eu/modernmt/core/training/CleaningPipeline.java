package eu.modernmt.core.training;

import eu.modernmt.core.training.cleaning.DraftFilter;
import eu.modernmt.core.training.cleaning.FilteredBilingualCorpus;
import eu.modernmt.model.BilingualCorpus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.*;

/**
 * Created by davide on 24/02/16.
 */
public class CleaningPipeline {

    public interface OutputCorpusFactory {

        BilingualCorpus getOutput(BilingualCorpus corpus);

    }

    private static final int MAX_IO_THREADS = 10;

    private ArrayList<BilingualCorpus> bilingualCorpora = new ArrayList<>();

    private final OutputCorpusFactory outputFactory;
    private final Locale sourceLanguage;
    private final Locale targetLanguage;

    private int ioThreads = MAX_IO_THREADS;

    public CleaningPipeline(OutputCorpusFactory outputFactory, Locale source, Locale target) {
        this.outputFactory = outputFactory;
        this.sourceLanguage = source;
        this.targetLanguage = target;
    }

    public void add(BilingualCorpus corpus) {
        FilteredBilingualCorpus filteredCorpus = new FilteredBilingualCorpus(corpus);
        filteredCorpus.addFilter(new DraftFilter());

        this.bilingualCorpora.add(filteredCorpus);
    }

    public Locale getSourceLanguage() {
        return sourceLanguage;
    }

    public Locale getTargetLanguage() {
        return targetLanguage;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(int ioThreads) {
        if (ioThreads < 1)
            throw new IllegalArgumentException();

        this.ioThreads = ioThreads;
    }

    public void process() throws InterruptedException, IOException {
        int totalCorporaCount = this.bilingualCorpora.size();
        int ioThreads = Math.min(Math.min(this.ioThreads, MAX_IO_THREADS), totalCorporaCount);

        ExecutorService executor = Executors.newFixedThreadPool(ioThreads);
        ExecutorCompletionService<Void> ecs = new ExecutorCompletionService<>(executor);

        int pendingTasks = 0;

        // Enqueue bilingual corpora tasks
        for (BilingualCorpus corpus : bilingualCorpora) {
            CleaningCorpusTask task = new CleaningCorpusTask(corpus, outputFactory.getOutput(corpus));
            ecs.submit(task);
            pendingTasks++;
        }

        try {
            for (int i = 0; i < pendingTasks; i++) {
                ecs.take().get();
            }
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof InterruptedException)
                throw (InterruptedException) cause;
            else if (cause instanceof IOException)
                throw (IOException) cause;
            else if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            else
                throw new Error("Unexpected exception", cause);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
    }

}
