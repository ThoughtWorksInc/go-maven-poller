//        The MIT License
//
//        Copyright (c) 2012, Michael Rumpf
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:

//        The above copyright notice and this permission notice shall be included in
//        all copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//        THE SOFTWARE.
package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.util.List;


/**
 * This represents a content-item of a Maven repository response with mime-type
 * application/xml for the Accept HTTP header.
 *
 * <pre>
 * <content>
 *   <data>
 *     <content-item>
 *       <resourceURI>https://www.mycompany.com/nexus/content/groups/repo/com/mycompany/abc/maven-metadata.xml.md5</resourceURI>
 *       <relativePath>/groups/repo/com/mycompany/abc/maven-metadata.xml.md5</relativePath>
 *       <text>maven-metadata.xml.md5</text> <leaf>true</leaf>
 *       <lastModified>2012-04-22 20:08:56.0 CEST</lastModified>
 *       <sizeOnDisk>33</sizeOnDisk>
 *     </content-item>
 *     <!-- ... -->
 *   </data>
 * </content>
 * </pre>
 * 
 * @author mrumpf
 */
@XmlRootElement
public class Content {
	private List<ContentItem> contentitems;

	public List<ContentItem> getContentItems() {
		return contentitems;
	}

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "content-item")
	public void setContentItems(List<ContentItem> contentitems) {
		this.contentitems = contentitems;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Content[");
		sb.append(contentitems);
		sb.append("]");
		return sb.toString();
	}
    public Content unmarshal(String xml) {
        Content content = null;
        if (xml != null) {
            try {
                StringReader sr = new StringReader(xml);
                JAXBContext jaxbContext = JAXBContext
                        .newInstance(Content.class);

                Unmarshaller jaxbUnmarshaller = jaxbContext
                        .createUnmarshaller();
                content = (Content) jaxbUnmarshaller.unmarshal(sr);

            } catch (JAXBException e) {
                LOGGER.error(String.format("Unmarshaling of XML data failed: %s", e.getMessage()));
            }
        }
        return content;
    }
    private static final Logger LOGGER = Logger.getLoggerFor(Content.class);
}
