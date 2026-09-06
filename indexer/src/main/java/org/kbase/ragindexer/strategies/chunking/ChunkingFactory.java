package org.kbase.ragindexer.strategies.chunking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kbase.ragindexer.constants.ChunkingStrategy;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChunkingFactory {

    private static Map<ChunkingStrategy, IChunkingStrategy> strategies = Map.of(
            ChunkingStrategy.Naive, new NaiveChunking()
    );

    public static IChunkingStrategy getStrategy(ChunkingStrategy strategy){
        return Optional.ofNullable(strategies.get(strategy)).orElseThrow();
    }
}
