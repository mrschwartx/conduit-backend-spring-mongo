package com.conduit.backend.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conduit.backend.shared.domain.PageWindow;
import com.conduit.backend.shared.domain.ValidationException;
import org.junit.jupiter.api.Test;

class PageWindowTest {

	@Test
	void usesDefaultsWhenMissing() {
		PageWindow window = PageWindow.parse(null, null);

		assertThat(window.offset()).isZero();
		assertThat(window.limit()).isEqualTo(20);
	}

	@Test
	void parsesValuesAndCapsTheLimit() {
		assertThat(PageWindow.parse("5", "10")).isEqualTo(new PageWindow(5, 10));
		assertThat(PageWindow.parse("0", "5000").limit()).isEqualTo(50);
	}

	@Test
	void rejectsInvalidValues() {
		assertThatThrownBy(() -> PageWindow.parse("-1", null)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> PageWindow.parse("abc", null)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> PageWindow.parse(null, "0")).isInstanceOf(ValidationException.class);
	}
}
