package com.conduit.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.conduit.backend.shared.domain.EntityIds;
import org.junit.jupiter.api.Test;

class EntityIdsTest {

	@Test
	void generatesValidIds() {
		assertThat(EntityIds.isValid(EntityIds.generate().toString())).isTrue();
	}

	@Test
	void rejectsMalformedIds() {
		assertThat(EntityIds.isValid("nope")).isFalse();
		assertThat(EntityIds.isValid(null)).isFalse();
		assertThat(EntityIds.isValid(42)).isFalse();
	}
}
