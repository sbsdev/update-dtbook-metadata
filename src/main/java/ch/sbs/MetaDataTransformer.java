package ch.sbs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.input.BOMInputStream;

public class MetaDataTransformer {
	private static final String[] keys = { "dc:Title", "dc:Creator", "dc:Subject", "dc:Description", "dc:Publisher",
			"dc:Date", "dc:Type", "dc:Format", "dc:Identifier", "dc:Source", "dc:Language", "dc:Rights", "dtb:uid",
			"dtb:sourceEdition", "dtb:sourcePublisher", "dtb:sourceRights", "prod:series", "prod:seriesNumber",
			"prod:source" };

	private static final String dtb = "http://www.daisy.org/z3986/2005/dtbook/";

	private static final QName metaData = new QName(dtb, "meta");
	private static final QName metaName = new QName("name");

	XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	private static void changeAttribute(XMLEventWriter writer, XMLEventFactory eventFactory, String name, String value)
			throws XMLStreamException {
		writer.add(eventFactory.createStartElement("", dtb, "meta"));
		writer.add(eventFactory.createAttribute("name", name));
		writer.add(eventFactory.createAttribute("content", value));
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: Specify XML File Name");
			System.exit(1);
		}
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

		try {
			XMLEventReader reader = inputFactory.createXMLEventReader(new BOMInputStream(new FileInputStream(args[0])));
			XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(System.out);
			XMLEventFactory eventFactory = XMLEventFactory.newInstance();

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement() && event.asStartElement().getName().equals(metaData)
						&& event.asStartElement().getAttributeByName(metaName) != null) {
					String name = event.asStartElement().getAttributeByName(metaName).getValue();
					boolean found = false;
					for (String entry : keys) {
						String propertyKey = entry.replace(':', '.').toUpperCase();
						if (name.equalsIgnoreCase(entry) && System.getProperty(propertyKey) != null) {
							changeAttribute(writer, eventFactory, name, System.getProperty(propertyKey));
							found = true;
							break;
						}
					}
					if (!found) {
						writer.add(event);
					}
				} else {
					writer.add(event);
				}
			}

			writer.flush();

		} catch (

		FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

}
