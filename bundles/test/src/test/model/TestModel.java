package test.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class TestModel {

	@Test
	public void smokeIntegrationTest() {
		assertThat("Must run", true, is(true));
	}

}
