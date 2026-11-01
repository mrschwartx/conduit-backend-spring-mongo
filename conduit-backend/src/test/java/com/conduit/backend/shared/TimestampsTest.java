package com.conduit.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.conduit.backend.shared.domain.Timestamps;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimestampsTest {

	@Test
	void formatsAsIsoUtcWithMilliseconds() {
		assertThat(Timestamps.format(Instant.parse("2026-12-08T10:00:00Z"))).isEqualTo("2026-12-08T10:00:00.000Z");
		assertThat(Timestamps.format(Instant.parse("2026-12-08T10:00:00.123456Z")))
				.isEqualTo("2026-12-08T10:00:00.123Z");
	}

	@Test
	void nowHasMillisecondPrecision() {
		assertThat(Timestamps.now().getNano() % 1_000_000).isZero();
	}
}
