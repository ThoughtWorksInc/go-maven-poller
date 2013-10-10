package com.tw.go.plugin.maven.nexus;

import maven.MavenVersion;

import javax.xml.bind.annotation.XmlElement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @see com.tw.go.plugin.maven.nexus.Content
 *
 * @author mrumpf
 */
public class ContentItem {
    public static final SimpleDateFormat MAVEN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S 'UTC'");
    private String resourceURI;
	private String relativePath;
	private String text;
	private String leaf;
	private String lastModified;
	private String sizeOnDisk;

	public String getResourceURI() {
		return resourceURI;
	}

	@XmlElement
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}

	public String getRelativePath() {
		return relativePath;
	}

	@XmlElement
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getText() {
		return text;
	}

	@XmlElement
	public void setText(String text) {
		this.text = text;
	}

	public String getLeaf() {
		return leaf;
	}

	@XmlElement
	public void setLeaf(String leaf) {
		this.leaf = leaf;
	}

	public String getLastModified() {
		return lastModified;
	}

	@XmlElement
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getSizeOnDisk() {
		return sizeOnDisk;
	}

	@XmlElement
	public void setSizeOnDisk(String sizeOnDisk) {
		this.sizeOnDisk = sizeOnDisk;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ContentItem[");
		sb.append("resourceURI=");
		sb.append(resourceURI);
		sb.append(", relativePath=");
		sb.append(relativePath);
		sb.append(", text=");
		sb.append(text);
		sb.append(", leaf=");
		sb.append(leaf);
		sb.append(", lastModified=");
		sb.append(lastModified);
		sb.append(", sizeOnDisk=");
		sb.append(sizeOnDisk);
		sb.append("]");
		return sb.toString();
	}

    public MavenVersion toVersion() {
        MavenVersion result = new MavenVersion(text);
        try {
            result.setLastModified(MAVEN_DATE_FORMAT.parse(lastModified));
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Error parsing date %s for resource %s", lastModified, resourceURI), e);
        }
        return result;
    }
}
