package org.uncertweb.ps.encoding.xml;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.uncertweb.api.om.observation.BooleanObservation;
import org.uncertweb.api.om.observation.CategoryObservation;
import org.uncertweb.api.om.observation.DiscreteNumericObservation;
import org.uncertweb.api.om.observation.Measurement;
import org.uncertweb.api.om.observation.ReferenceObservation;
import org.uncertweb.api.om.observation.TextObservation;
import org.uncertweb.api.om.observation.UncertaintyObservation;
import org.uncertweb.api.om.observation.collections.BooleanObservationCollection;
import org.uncertweb.api.om.observation.collections.CategoryObservationCollection;
import org.uncertweb.api.om.observation.collections.DiscreteNumericObservationCollection;
import org.uncertweb.api.om.observation.collections.MeasurementCollection;
import org.uncertweb.api.om.observation.collections.ReferenceObservationCollection;
import org.uncertweb.api.om.observation.collections.TextObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;

public class OMEncodingTest {

	private OMEncoding encoding;

	@Before
	public void setUp() {
		encoding = new OMEncoding();
	}
	
	@Test
	public void supportedTypes() {
		Class<?>[] types = { BooleanObservation.class, CategoryObservation.class, DiscreteNumericObservation.class,
				Measurement.class, ReferenceObservation.class, TextObservation.class, UncertaintyObservation.class,
				BooleanObservationCollection.class, CategoryObservationCollection.class, DiscreteNumericObservationCollection.class,
				MeasurementCollection.class, ReferenceObservationCollection.class, TextObservationCollection.class,
				UncertaintyObservationCollection.class
		};
		for (Class<?> type : types) {
			Assert.assertTrue(encoding.isSupportedType(type));
		}
	}
	
	@Test
	public void supportedMimeType() {
		Assert.assertTrue(encoding.isSupportedMimeType("text/xml"));
	}
	
	@Test
	public void namespace() {
		Assert.assertEquals("http://www.opengis.net/om/2.0", encoding.getNamespace());
	}
	
	@Test
	public void schemaLocation() {
		Assert.assertEquals("http://52north.org/schema/geostatistics/uncertweb/Profiles/OM/UncertWeb_OM.xsd", encoding.getSchemaLocation());
	}
	
}
