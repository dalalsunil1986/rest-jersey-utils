package com.mercateo.rest.jersey.utils.cors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class AccessControlAllowOriginRequestFilterIntegrationTest extends JerseyTest {
	private static boolean requestGoneThrough = false;

	@Path("/")
	public static final class TestResource {
		@GET
		public void TestException() {
			requestGoneThrough = true;
		}
	}

	private OriginFilter originFilter;

	@Override
	protected Application configure() {
		ResourceConfig rs = new ResourceConfig(TestResource.class, JacksonFeature.class);
		originFilter = mock(OriginFilter.class);
		rs.register(new AccessControlAllowOriginRequestFilter(originFilter));
		return rs;
	}

	@Test
	public void getWithoutOriginSet() {
		requestGoneThrough = false;
		Response resp = target("/").request().get();
		assertTrue(requestGoneThrough);
		verifyNoMoreInteractions(originFilter);
		assertEquals(204, resp.getStatus());
	}

	@Test
	public void getWithoutFalseOriginSet() {
		when(originFilter.isOriginAllowed(anyString())).thenReturn(false);
		requestGoneThrough = false;
		Response resp = target("/").request().header("Origin", "http://localhost:8080").get();
		assertFalse(requestGoneThrough);
		assertEquals(400, resp.getStatus());
	}

	@Test
	public void getWithoutRightOriginSet() {
		when(originFilter.isOriginAllowed(anyString())).thenReturn(true);
		requestGoneThrough = false;
		Response resp = target("/").request().header("Origin", "http://localhost:8080").get();
		assertTrue(requestGoneThrough);
		assertEquals(204, resp.getStatus());
	}
}